package fr.cnes.regards.modules.accessrights.service;

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
import fr.cnes.regards.framework.security.utils.endpoint.RoleAuthority;
import fr.cnes.regards.modules.accessrights.dao.projects.IProjectUserRepository;
import fr.cnes.regards.modules.accessrights.domain.UserStatus;
import fr.cnes.regards.modules.accessrights.domain.UserVisibility;
import fr.cnes.regards.modules.accessrights.domain.projects.MetaData;
import fr.cnes.regards.modules.accessrights.domain.projects.ProjectUser;
import fr.cnes.regards.modules.accessrights.domain.projects.ResourcesAccess;
import fr.cnes.regards.modules.accessrights.domain.projects.Role;
import fr.cnes.regards.modules.core.utils.RegardsStreamUtils;

/**
 * {@link IProjectUserService} implementation
 *
 * @author xbrochar
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
    private final Predicate<? super MetaData> visibleMetaDataFilter = m -> !UserVisibility.HIDDEN
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

    @Override
    public List<ProjectUser> retrieveUserList() {
        return projectUserRepository.findByStatus(UserStatus.ACCESS_GRANTED);
    }

    @Override
    public ProjectUser retrieveUser(final Long pUserId) {
        final ProjectUser user = projectUserRepository.findOne(pUserId);
        // Filter out hidden meta data
        try (final Stream<MetaData> stream = user.getMetaData().stream()) {
            user.setMetaData(stream.filter(visibleMetaDataFilter).collect(Collectors.toList()));
        }
        return user;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.accessrights.service.IProjectUserService#retrieveOneByEmail(java.lang.String)
     */
    @Override
    public ProjectUser retrieveOneByEmail(final String pUserEmail) throws EntityNotFoundException {
        final ProjectUser user;
        if (instanceAdminUserEmail.equals(pUserEmail)) {
            user = new ProjectUser(pUserEmail, new Role(0L, RoleAuthority.INSTANCE_ADMIN_VIRTUAL_ROLE, null, new ArrayList<>(), new ArrayList<>()), new ArrayList<>(), new ArrayList<>());
        } else {
            user = projectUserRepository.findOneByEmail(pUserEmail);
            if (user == null) {
                throw new EntityNotFoundException(pUserEmail, ProjectUser.class);
            }
            // Filter out hidden meta data
            try (final Stream<MetaData> stream = user.getMetaData().stream()) {
                stream.filter(visibleMetaDataFilter);
            }
        }
        return user;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.accessrights.service.IProjectUserService#retrieveCurrentUser()
     */
    @Override
    public ProjectUser retrieveCurrentUser() {
        final String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return projectUserRepository.findOneByEmail(email);
    }

    @Override
    public void updateUser(final Long pUserId, final ProjectUser pUpdatedProjectUser)
            throws InvalidValueException, EntityNotFoundException {
        if (pUpdatedProjectUser.getId() != pUserId) {
            throw new InvalidValueException("Account id specified differs from updated account id");
        }
        if (!existUser(pUserId)) {
            throw new EntityNotFoundException(pUserId.toString(), ProjectUser.class);
        }
        save(pUpdatedProjectUser);
    }

    @Override
    public void removeUser(final Long pUserId) {
        projectUserRepository.delete(pUserId);
    }

    @Override
    public void updateUserAccessRights(final String pLogin, final List<ResourcesAccess> pUpdatedUserAccessRights)
            throws EntityNotFoundException {
        if (!existUser(pLogin)) {
            throw new EntityNotFoundException(pLogin, ProjectUser.class);
        }
        final ProjectUser user = projectUserRepository.findOneByEmail(pLogin);

        try (Stream<ResourcesAccess> previous = user.getPermissions().stream();
                Stream<ResourcesAccess> updated = pUpdatedUserAccessRights.stream()) {
            user.setPermissions(Stream.concat(updated, previous)
                    .filter(RegardsStreamUtils.distinctByKey(r -> r.getId())).collect(Collectors.toList()));
        }

        save(user);
    }

    @Override
    public void removeUserAccessRights(final String pLogin) {
        final ProjectUser user = projectUserRepository.findOneByEmail(pLogin);
        user.setPermissions(new ArrayList<>());
        save(user);
    }

    @Override
    public List<MetaData> retrieveUserMetaData(final Long pUserId) {
        final ProjectUser user = retrieveUser(pUserId);
        return user.getMetaData();
    }

    @Override
    public void updateUserMetaData(final Long pUserId, final List<MetaData> pUpdatedUserMetaData) {
        final ProjectUser user = retrieveUser(pUserId);
        user.setMetaData(pUpdatedUserMetaData);
        save(user);
    }

    @Override
    public void removeUserMetaData(final Long pUserId) {
        final ProjectUser user = retrieveUser(pUserId);
        user.setMetaData(new ArrayList<>());
        save(user);
    }

    @Override
    public List<ResourcesAccess> retrieveProjectUserAccessRights(final String pLogin, final String pBorrowedRoleName)
            throws InvalidValueException {
        final ProjectUser projectUser = projectUserRepository.findOneByEmail(pLogin);
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
        } catch (final EntityNotFoundException e) {
            LOG.debug("Could not retrieve permissions from role", e);
        }
        return merged;
    }

    @Override
    public boolean existUser(final String pEmail) {
        final ProjectUser projectUser = projectUserRepository.findOneByEmail(pEmail);
        return projectUser != null;
    }

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
