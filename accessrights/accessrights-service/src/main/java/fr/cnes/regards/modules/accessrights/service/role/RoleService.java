/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessrights.service.role;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.amqp.ISubscriber;
import fr.cnes.regards.framework.amqp.domain.IHandler;
import fr.cnes.regards.framework.amqp.domain.TenantWrapper;
import fr.cnes.regards.framework.amqp.exception.RabbitMQVhostException;
import fr.cnes.regards.framework.module.rest.exception.EntityAlreadyExistsException;
import fr.cnes.regards.framework.module.rest.exception.EntityException;
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.EntityOperationForbiddenException;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.multitenant.ITenantResolver;
import fr.cnes.regards.framework.security.role.DefaultRole;
import fr.cnes.regards.framework.security.utils.endpoint.RoleAuthority;
import fr.cnes.regards.framework.security.utils.jwt.JWTService;
import fr.cnes.regards.framework.security.utils.jwt.exception.JwtException;
import fr.cnes.regards.modules.accessrights.dao.projects.IProjectUserRepository;
import fr.cnes.regards.modules.accessrights.dao.projects.IRoleRepository;
import fr.cnes.regards.modules.accessrights.domain.projects.ProjectUser;
import fr.cnes.regards.modules.accessrights.domain.projects.ResourcesAccess;
import fr.cnes.regards.modules.accessrights.domain.projects.Role;
import fr.cnes.regards.modules.accessrights.domain.projects.RoleFactory;
import fr.cnes.regards.modules.accessrights.domain.projects.RoleLineageAssembler;
import fr.cnes.regards.modules.accessrights.service.RegardsStreamUtils;
import fr.cnes.regards.modules.project.domain.event.NewProjectConnectionEvent;

/**
 * {@link IRoleService} implementation
 *
 * @author Xavier-Alexandre Brochard
 * @author Sébastien Binda
 *
 */
@Service
@ImportResource({ "classpath*:defaultRoles.xml" })
public class RoleService implements IRoleService {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(RoleService.class);

    /**
     * Error message
     */
    private static final String NATIVE_ROLE_NOT_REMOVABLE = "Modifications on native roles are forbidden";

    /**
     * Current microservice name
     */
    private final String microserviceName;

    /**
     * CRUD repository managing {@link Role}s. Autowired by Spring.
     */
    private final IRoleRepository roleRepository;

    /**
     * CRUD repository managing {@link ProjectUser}s. Autowired by Spring.
     */
    private final IProjectUserRepository projectUserRepository;

    /**
     * Tenant resolver to access all configured tenant
     */
    private final ITenantResolver tenantResolver;

    /**
     * Runtime tenant resolver
     */
    private final IRuntimeTenantResolver runtimeTenantResolver;

    /**
     * Security service
     */
    private final JWTService jwtService;

    /**
     * AMQP Message subscriber
     */
    private final ISubscriber subscriber;

    /**
     * The default roles. Autowired by Spring.
     */
    @Resource
    private List<Role> defaultRoles;

    public RoleService(@Value("${spring.application.name}") final String pMicroserviceName,
            final IRoleRepository pRoleRepository, final IProjectUserRepository pProjectUserRepository,
            final ITenantResolver pTenantResolver, IRuntimeTenantResolver pRuntimeTenantResolver,
            final JWTService pJwtService, final ISubscriber pSubscriber) {
        super();
        roleRepository = pRoleRepository;
        projectUserRepository = pProjectUserRepository;
        tenantResolver = pTenantResolver;
        this.runtimeTenantResolver = pRuntimeTenantResolver;
        jwtService = pJwtService;
        microserviceName = pMicroserviceName;
        subscriber = pSubscriber;
    }

    /**
     * Init medthod
     *
     * @throws RabbitMQVhostException
     *             initialization error
     */
    @PostConstruct
    public void init() {

        subscriber.subscribeTo(NewProjectConnectionEvent.class,
                               new NewProjectConnectionEventHandler(runtimeTenantResolver, this));

        initDefaultRoles();
    }

    private class NewProjectConnectionEventHandler implements IHandler<NewProjectConnectionEvent> {

        private final IRoleService roleService;

        private final IRuntimeTenantResolver runtimeTenantResolver;

        public NewProjectConnectionEventHandler(IRuntimeTenantResolver pRuntimeTenantResolver,
                IRoleService pRoleService) {
            super();
            roleService = pRoleService;
            runtimeTenantResolver = pRuntimeTenantResolver;
        }

        /**
         *
         * Initialize default roles in the new project connection
         *
         * @see fr.cnes.regards.framework.amqp.domain.IHandler#handle(fr.cnes.regards.framework.amqp.domain.TenantWrapper)
         * @since 1.0-SNAPSHOT
         */
        @Override
        public void handle(final TenantWrapper<NewProjectConnectionEvent> pWrapper) {
            runtimeTenantResolver.forceTenant(pWrapper.getTenant());
            roleService.initDefaultRoles();
        }
    }

    /**
     * Ensure the existence of default roles. If not, add them from their bean definition in defaultRoles.xml
     */
    // FIXME method à revoir avec IRuntimeTenantResolver
    @Override
    public void initDefaultRoles() {

        // Define a consumer injecting the passed tenant in the context
        final Consumer<? super String> injectTenant = tenant -> {
            try {
                jwtService.injectToken(tenant, RoleAuthority.getSysRole(microserviceName), microserviceName);
            } catch (final JwtException e) {
                LOG.error(e.getMessage(), e);
            }
        };

        // Return the role with same name in db if exists
        final UnaryOperator<Role> replaceWithRoleFromDb = r -> {
            try {
                return retrieveRole(r.getName());
            } catch (final EntityNotFoundException e) {
                LOG.debug("Could not find a role in DB, falling back to xml definition.", e);
                return r;
            }
        };

        // For passed role, replace parent with its equivalent from the defaultRoles list
        final Consumer<Role> setParentFromDefaultRoles = r -> {
            if (r.getParentRole() != null) {
                final Role parent = defaultRoles.stream().filter(el -> el.getName().equals(r.getParentRole().getName()))
                        .findFirst().orElse(null);
                r.setParentRole(parent);
            }
        };

        // Define a consumer creating if needed all default roles on current tenant
        final Consumer<? super String> createDefaultRolesOnTenant = t -> {
            // Replace all default roles with their db version if exists
            defaultRoles.replaceAll(replaceWithRoleFromDb);
            // Re-plug the parent roles
            defaultRoles.forEach(setParentFromDefaultRoles);
            // Save everything
            defaultRoles.forEach(roleRepository::save);
        };

        // For each tenant, inject tenant in context and create (if needed) default roles
        try (Stream<String> tenantsStream = tenantResolver.getAllTenants().stream()) {
            tenantsStream.peek(injectTenant).forEach(createDefaultRolesOnTenant);
        }
    }

    @Override
    public List<Role> retrieveRoleList() {
        try (Stream<Role> stream = StreamSupport.stream(roleRepository.findAllDistinctLazy().spliterator(), true)) {
            return stream.collect(Collectors.toList());
        }
    }

    @Override
    public Role createRole(final Role pNewRole) throws EntityAlreadyExistsException {
        if (existByName(pNewRole.getName())) {
            throw new EntityAlreadyExistsException(pNewRole.getName());
        }
        return roleRepository.save(pNewRole);
    }

    @Override
    public Role retrieveRole(final String pRoleName) throws EntityNotFoundException {
        return roleRepository.findOneByName(pRoleName)
                .orElseThrow(() -> new EntityNotFoundException(pRoleName, Role.class));
    }

    @Override
    public Role updateRole(final Long pRoleId, final Role pUpdatedRole) throws EntityException {
        if (!pRoleId.equals(pUpdatedRole.getId())) {
            throw new EntityInconsistentIdentifierException(pRoleId, pUpdatedRole.getId(), Role.class);
        }
        if (!existRole(pRoleId)) {
            throw new EntityNotFoundException(pRoleId.toString(), Role.class);
        }
        return roleRepository.save(pUpdatedRole);
    }

    @Override
    public void removeRole(final Long pRoleId) throws EntityOperationForbiddenException {
        final Role previous = roleRepository.findOne(pRoleId);
        if ((previous != null) && previous.isNative()) {
            throw new EntityOperationForbiddenException(pRoleId.toString(), Role.class, NATIVE_ROLE_NOT_REMOVABLE);
        }
        roleRepository.delete(pRoleId);
    }

    /**
     * Les droits d’accès d’un utilisateur sont la fusion des droits d’accès de son rôle, des rôles hiérarchiquement
     * liés et de ses propres droits.
     *
     * @see SGDS-CP-12200-0010-CS p. 73
     * @see REGARDS_DSL_ADM_ADM_260
     */
    @Override
    public List<ResourcesAccess> retrieveRoleResourcesAccessList(final Long pRoleId) throws EntityNotFoundException {
        if (!existRole(pRoleId)) {
            throw new EntityNotFoundException(pRoleId.toString(), Role.class);
        }

        final List<Role> roleAndHisAncestors = new ArrayList<>();

        final Role role = roleRepository.findOne(pRoleId);
        roleAndHisAncestors.add(role);

        final RoleLineageAssembler roleLineageAssembler = new RoleLineageAssembler();
        roleAndHisAncestors.addAll(roleLineageAssembler.of(role).get());

        return roleAndHisAncestors.stream().map(r -> r.getPermissions()).flatMap(l -> l.stream())
                .collect(Collectors.toList());
    }

    @Override
    public Role updateRoleResourcesAccess(final Long pRoleId, final List<ResourcesAccess> pResourcesAccessList)
            throws EntityNotFoundException {
        if (!existRole(pRoleId)) {
            throw new EntityNotFoundException(pRoleId.toString(), Role.class);
        }
        final Role role = roleRepository.findOne(pRoleId);
        final List<ResourcesAccess> permissions = role.getPermissions();
        final Predicate<ResourcesAccess> filter = RegardsStreamUtils.distinctByKey(r -> r.getId());

        try (final Stream<ResourcesAccess> previous = permissions.stream();
                final Stream<ResourcesAccess> toMerge = pResourcesAccessList.stream();
                final Stream<ResourcesAccess> merged = Stream.concat(toMerge, previous)) {
            role.setPermissions(merged.filter(filter).collect(Collectors.toList()));
            roleRepository.save(role);
        }

        return role;
    }

    @Override
    public void clearRoleResourcesAccess(final Long pRoleId) throws EntityNotFoundException {
        if (!existRole(pRoleId)) {
            throw new EntityNotFoundException(pRoleId.toString(), Role.class);
        }
        final Role role = roleRepository.findOne(pRoleId);
        role.setPermissions(new ArrayList<>());
        roleRepository.save(role);
    }

    @Override
    public Page<ProjectUser> retrieveRoleProjectUserList(final Long pRoleId, final Pageable pPageable)
            throws EntityNotFoundException {
        if (!existRole(pRoleId)) {
            throw new EntityNotFoundException(pRoleId.toString(), Role.class);
        }
        final Role role = roleRepository.findOne(pRoleId);
        final List<Role> roles = retrieveInheritedRoles(role);
        roles.add(role);
        final List<String> roleNames = roles.stream().map(r -> r.getName()).collect(Collectors.toList());
        return projectUserRepository.findByRoleNameIn(roleNames, pPageable);
    }

    @Override
    public boolean existRole(final Long pRoleId) {
        return roleRepository.exists(pRoleId);
    }

    @Override
    public boolean existRole(final Role pRole) {
        return roleRepository.exists(pRole.getId());
    }

    @Override
    public boolean existByName(final String pName) {
        return roleRepository.findOneByName(pName).isPresent();
    }

    @Override
    public Role getDefaultRole() {
        return roleRepository.findOneByIsDefault(true).orElse(getRolePublic());
    }

    /**
     * Return true if {@link pRole} is an ancestor of {@link pOther} through the {@link Role#getParentRole()} chain.
     */
    @Override
    public boolean isHierarchicallyInferior(final Role pRole, final Role pOther) {
        final RoleLineageAssembler roleLineageAssembler = new RoleLineageAssembler();
        final List<Role> ancestors = roleLineageAssembler.of(pOther).get();
        try (Stream<Role> stream = ancestors.stream()) {
            return stream.anyMatch(r -> r.getName().equals(pRole.getName()));
        }
    }

    /**
     * @return the role public. Create it if not found
     */
    public Role getRolePublic() {
        final RoleFactory factory = new RoleFactory();
        return roleRepository.findOneByName(DefaultRole.PUBLIC.toString())
                .orElseGet(() -> roleRepository.save(factory.createPublic()));
    }

    @Override
    public List<Role> retrieveInheritedRoles(final Role pRole) {
        final List<Role> results = new ArrayList<>();
        final List<Role> inheritedRoles = roleRepository.findByParentRoleName(pRole.getName());
        if (inheritedRoles != null) {
            final Predicate<Role> filter = RegardsStreamUtils.distinctByKey(r -> r.getId());

            inheritedRoles.stream().filter(filter).forEach(r -> results.add(r));

            for (final Role role : inheritedRoles) {
                retrieveInheritedRoles(role).stream().filter(filter).forEach(r -> results.add(r));
            }
        }
        return results;
    }
}
