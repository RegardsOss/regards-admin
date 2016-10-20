/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessRights.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import fr.cnes.regards.modules.accessRights.dao.projects.IRoleRepository;
import fr.cnes.regards.modules.accessRights.domain.projects.ProjectUser;
import fr.cnes.regards.modules.accessRights.domain.projects.ResourcesAccess;
import fr.cnes.regards.modules.accessRights.domain.projects.Role;
import fr.cnes.regards.modules.core.exception.AlreadyExistingException;
import fr.cnes.regards.modules.core.exception.EntityNotFoundException;
import fr.cnes.regards.modules.core.exception.InvalidValueException;

@Service
public class RoleService implements IRoleService {

    private final IRoleRepository roleRepository;

    @Resource
    private List<Role> defaultRoles;

    public RoleService(final IRoleRepository pRoleRepository) {
        super();
        roleRepository = pRoleRepository;
    }

    @PostConstruct
    public void init() throws AlreadyExistingException {
        // Ensure the existence of default roles
        // If not, add them from their bean definition in defaultRoles.xml
        // Get all projects in database
        // for (final Role role : defaultRoles) {
        // if (!existRole(role)) {
        // createRole(role);
        // }
        // }
    }

    @Override
    public List<Role> retrieveRoleList() {
        try (Stream<Role> stream = StreamSupport.stream(roleRepository.findAll().spliterator(), true)) {
            return stream.collect(Collectors.toList());
        }
    }

    @Override
    public Role createRole(final Role pNewRole) throws AlreadyExistingException {
        if (existRole(pNewRole)) {
            throw new AlreadyExistingException(pNewRole.toString());
        }
        return roleRepository.save(pNewRole);
    }

    @Override
    public Role retrieveRole(final Long pRoleId) {
        return roleRepository.findOne(pRoleId);
    }

    @Override
    public void updateRole(final Long pRoleId, final Role pUpdatedRole)
            throws InvalidValueException, EntityNotFoundException {
        if (!pRoleId.equals(pUpdatedRole.getId())) {
            throw new InvalidValueException();
        }
        if (!existRole(pRoleId)) {
            throw new EntityNotFoundException(pRoleId.toString(), Role.class);
        }
        roleRepository.save(pUpdatedRole);
    }

    @Override
    public void removeRole(final Long pRoleId) {
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
    public void updateRoleResourcesAccess(final Long pRoleId, final List<ResourcesAccess> pResourcesAccessList)
            throws EntityNotFoundException {
        if (!existRole(pRoleId)) {
            throw new EntityNotFoundException(pRoleId.toString(), Role.class);
        }
        final Role role = roleRepository.findOne(pRoleId);
        final List<ResourcesAccess> permissions = role.getPermissions();

        try (Stream<ResourcesAccess> stream = Stream.concat(pResourcesAccessList.stream(), permissions.stream())) {
            role.setPermissions(stream.distinct().collect(Collectors.toList()));
            role.setPermissions(permissions);
            roleRepository.save(role);
        }
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
    public List<ProjectUser> retrieveRoleProjectUserList(final Long pRoleId) throws EntityNotFoundException {
        if (!existRole(pRoleId)) {
            throw new EntityNotFoundException(pRoleId.toString(), Role.class);
        }
        final Role role = roleRepository.findOne(pRoleId);
        return role.getProjectUsers();
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
    public Role getDefaultRole() {
        return roleRepository.findByIsDefault(true);
    }

    /**
     * Return true if {@link pRole} is an ancestor of {@link pOther} through the {@link Role#getParentRole()} chain.
     */
    @Override
    public boolean isHierarchicallyInferior(final Role pRole, final Role pOther) {

        final RoleLineageAssembler roleLineageAssembler = new RoleLineageAssembler();
        final List<Role> ancestors = roleLineageAssembler.of(pOther).get();

        return ancestors.contains(pRole);
    }

}
