package fr.cnes.regards.modules.accessrights.service.projectuser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.InvalidValueException;
import fr.cnes.regards.framework.module.rest.exception.ModuleEntityNotFoundException;
import fr.cnes.regards.framework.security.utils.endpoint.RoleAuthority;
import fr.cnes.regards.modules.accessrights.dao.projects.IProjectUserRepository;
import fr.cnes.regards.modules.accessrights.domain.UserStatus;
import fr.cnes.regards.modules.accessrights.domain.UserVisibility;
import fr.cnes.regards.modules.accessrights.domain.projects.MetaData;
import fr.cnes.regards.modules.accessrights.domain.projects.ProjectUser;
import fr.cnes.regards.modules.accessrights.domain.projects.ResourcesAccess;
import fr.cnes.regards.modules.accessrights.domain.projects.Role;
import fr.cnes.regards.modules.accessrights.service.role.IRoleService;
import fr.cnes.regards.modules.core.utils.RegardsStreamUtils;

/**
 * {@link IProjectUserService} implementation
 *
 * @author xbrochar
 * @author Sébastien Binda
 */
@Service
public class ProjectUserService implements IProjectUserService {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProjectUserService.class);

    /**
     * CRUD repository managing {@link ProjectUser}s. Autowired by Spring.
     */
    private final IProjectUserRepository projectUserRepository;

    /**
     * Service handling CRUD operation on {@link Role}s
     */
    private final IRoleService roleService;

    /**
     * A filter on meta data to keep visible ones only
     */
    private final Predicate<? super MetaData> keepVisibleMetaData = m -> !UserVisibility.HIDDEN
            .equals(m.getVisibility());

    /**
     * Configured instance administrator user email/login
     */
    private final String instanceAdminUserEmail;

    /**
     * Creates a new instance of the service with passed services/repos
     *
     * @param pProjectUserRepository
     *            The project user repo
     * @param pRoleService
     *            The role service
     * @param pInstanceAdminUserEmail
     *            The instance admin user email
     */
    public ProjectUserService(final IProjectUserRepository pProjectUserRepository, final IRoleService pRoleService,
            @Value("${regards.accounts.root.user.login}") final String pInstanceAdminUserEmail) {
        super();
        projectUserRepository = pProjectUserRepository;
        roleService = pRoleService;
        instanceAdminUserEmail = pInstanceAdminUserEmail;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.accessrights.service.projectuser.IProjectUserService#retrieveUserList()
     */
    @Override
    public List<ProjectUser> retrieveUserList() {
        return projectUserRepository.findByStatus(UserStatus.ACCESS_GRANTED);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.accessrights.service.projectuser.IProjectUserService#retrieveUser(java.lang.Long)
     */
    @Override
    public ProjectUser retrieveUser(final Long pUserId) throws EntityNotFoundException {
        final ProjectUser user = projectUserRepository.findOne(pUserId);
        // Check found
        if (user == null) {
            throw new EntityNotFoundException(pUserId.toString(), ProjectUser.class);
        }
        // Filter out hidden meta data
        try (final Stream<MetaData> stream = user.getMetaData().stream()) {
            user.setMetaData(stream.filter(keepVisibleMetaData).collect(Collectors.toList()));
        }
        return user;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.accessrights.service.role.IProjectUserService#retrieveOneByEmail(java.lang.String)
     */
    @Override
    public ProjectUser retrieveOneByEmail(final String pUserEmail) throws ModuleEntityNotFoundException {
        final ProjectUser user;
        if (instanceAdminUserEmail.equals(pUserEmail)) {
            user = new ProjectUser(pUserEmail, new Role(RoleAuthority.INSTANCE_ADMIN_VIRTUAL_ROLE, null),
                    new ArrayList<>(), new ArrayList<>());
        } else {
            user = projectUserRepository.findOneByEmail(pUserEmail)
                    .orElseThrow(() -> new ModuleEntityNotFoundException(pUserEmail, ProjectUser.class));
            // Filter out hidden meta data
            try (final Stream<MetaData> stream = user.getMetaData().stream()) {
                stream.filter(keepVisibleMetaData);
            }
        }
        return user;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.accessrights.service.role.IProjectUserService#retrieveCurrentUser()
     */
    @Override
    public ProjectUser retrieveCurrentUser() throws ModuleEntityNotFoundException {
        final String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return projectUserRepository.findOneByEmail(email)
                .orElseThrow(() -> new ModuleEntityNotFoundException("Current user", ProjectUser.class));
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.accessrights.service.projectuser.IProjectUserService#retrieveAccessRequestList()
     */
    @Override
    public List<ProjectUser> retrieveAccessRequestList() {
        return projectUserRepository.findByStatus(UserStatus.WAITING_ACCESS);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.accessrights.service.projectuser.IProjectUserService#updateUser(java.lang.Long,
     * fr.cnes.regards.modules.accessrights.domain.projects.ProjectUser)
     */
    @Override
    public void updateUser(final Long pUserId, final ProjectUser pUpdatedProjectUser)
            throws InvalidValueException, ModuleEntityNotFoundException {
        if (!pUpdatedProjectUser.getId().equals(pUserId)) {
            throw new InvalidValueException("Account id specified differs from updated account id");
        }
        if (!existUser(pUserId)) {
            throw new ModuleEntityNotFoundException(pUserId.toString(), ProjectUser.class);
        }
        save(pUpdatedProjectUser);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.cnes.regards.modules.accessrights.service.projectuser.IProjectUserService#updateUserAccessRights(java.lang.
     * String, java.util.List)
     */
    @Override
    public void updateUserAccessRights(final String pLogin, final List<ResourcesAccess> pUpdatedUserAccessRights)
            throws ModuleEntityNotFoundException {
        final ProjectUser user = projectUserRepository.findOneByEmail(pLogin)
                .orElseThrow(() -> new ModuleEntityNotFoundException(pLogin, ProjectUser.class));

        try (final Stream<ResourcesAccess> previous = user.getPermissions().stream();
                final Stream<ResourcesAccess> updated = pUpdatedUserAccessRights.stream();
                final Stream<ResourcesAccess> merged = Stream.concat(updated, previous)) {
            user.setPermissions(merged.filter(RegardsStreamUtils.distinctByKey(r -> r.getId()))
                    .collect(Collectors.toList()));
        }

        save(user);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.cnes.regards.modules.accessrights.service.projectuser.IProjectUserService#removeUserAccessRights(java.lang.
     * String)
     */
    @Override
    public void removeUserAccessRights(final String pLogin) throws ModuleEntityNotFoundException {
        final ProjectUser user = projectUserRepository.findOneByEmail(pLogin)
                .orElseThrow(() -> new ModuleEntityNotFoundException(pLogin, ProjectUser.class));
        user.setPermissions(new ArrayList<>());
        save(user);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.cnes.regards.modules.accessrights.service.projectuser.IProjectUserService#retrieveUserMetaData(java.lang.Long)
     */
    @Override
    public List<MetaData> retrieveUserMetaData(final Long pUserId) throws EntityNotFoundException {
        final ProjectUser user = retrieveUser(pUserId);
        return user.getMetaData();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.cnes.regards.modules.accessrights.service.projectuser.IProjectUserService#updateUserMetaData(java.lang.Long,
     * java.util.List)
     */
    @Override
    public void updateUserMetaData(final Long pUserId, final List<MetaData> pUpdatedUserMetaData)
            throws EntityNotFoundException {
        final ProjectUser user = retrieveUser(pUserId);
        user.setMetaData(pUpdatedUserMetaData);
        save(user);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.cnes.regards.modules.accessrights.service.projectuser.IProjectUserService#removeUserMetaData(java.lang.Long)
     */
    @Override
    public void removeUserMetaData(final Long pUserId) throws EntityNotFoundException {
        final ProjectUser user = retrieveUser(pUserId);
        user.setMetaData(new ArrayList<>());
        save(user);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.cnes.regards.modules.accessrights.service.projectuser.IProjectUserService#retrieveProjectUserAccessRights(java
     * .lang.String, java.lang.String)
     */
    @Override
    public List<ResourcesAccess> retrieveProjectUserAccessRights(final String pEmail, final String pBorrowedRoleName)
            throws InvalidValueException, ModuleEntityNotFoundException {
        final ProjectUser projectUser = retrieveOneByEmail(pEmail);
        final Role userRole = projectUser.getRole();
        Role returnedRole = userRole;

        if (pBorrowedRoleName != null) {
            final Role borrowedRole = roleService.retrieveRole(pBorrowedRoleName);
            if (roleService.isHierarchicallyInferior(borrowedRole, returnedRole)) {
                returnedRole = borrowedRole;
            } else {
                throw new InvalidValueException(
                        "Borrowed role must be hierachically inferior to the project user's role");
            }
        }

        // Merge permissions from the project user and from the role
        final List<ResourcesAccess> merged = new ArrayList<>();
        final List<ResourcesAccess> fromUser = projectUser.getPermissions();
        merged.addAll(fromUser);
        try {
            final List<ResourcesAccess> fromRole = roleService.retrieveRoleResourcesAccessList(returnedRole.getId());
            merged.addAll(fromRole);
        } catch (final ModuleEntityNotFoundException e) {
            LOG.debug("Could not retrieve permissions from role", e);
        }
        return merged;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.accessrights.service.projectuser.IProjectUserService#existUser(java.lang.String)
     */
    @Override
    public boolean existUser(final String pEmail) {
        return projectUserRepository.findOneByEmail(pEmail).isPresent();
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.accessrights.service.projectuser.IProjectUserService#existUser(java.lang.Long)
     */
    @Override
    public boolean existUser(final Long pId) {
        return projectUserRepository.exists(pId);
    }

    /**
     * Specific on-save operations
     *
     * @param pProjectUser
     *            The user to save
     */
    private void save(final ProjectUser pProjectUser) {
        pProjectUser.setLastUpdate(LocalDateTime.now());
        projectUserRepository.save(pProjectUser);
    }

}
