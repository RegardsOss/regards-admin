/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessRights.service.stubs;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.naming.OperationNotSupportedException;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import fr.cnes.regards.modules.accessRights.dao.projects.IProjectUserRepository;
import fr.cnes.regards.modules.accessRights.dao.projects.IRoleRepository;
import fr.cnes.regards.modules.accessRights.domain.Couple;
import fr.cnes.regards.modules.accessRights.domain.UserStatus;
import fr.cnes.regards.modules.accessRights.domain.UserVisibility;
import fr.cnes.regards.modules.accessRights.domain.projects.MetaData;
import fr.cnes.regards.modules.accessRights.domain.projects.ProjectUser;
import fr.cnes.regards.modules.accessRights.domain.projects.ResourcesAccess;
import fr.cnes.regards.modules.accessRights.domain.projects.Role;
import fr.cnes.regards.modules.accessRights.service.IProjectUserService;
import fr.cnes.regards.modules.accessRights.service.IRoleService;

/**
 * @author svissier
 *
 */
@Service
@Profile("test")
@Primary
public class ProjectUserServiceStub implements IProjectUserService {

    public static List<ProjectUser> projectUsers_;

    private final IProjectUserRepository projectUserRepository_;

    private final IRoleService roleService_;

    private final IRoleRepository roleRepository_;

    public ProjectUserServiceStub(final IProjectUserRepository pDaoProjectUser, final IRoleService pRoleService,
            final IRoleRepository pRoleRepository) {
        projectUserRepository_ = pDaoProjectUser;
        roleService_ = pRoleService;
        roleRepository_ = pRoleRepository;
        projectUsers_ = StreamSupport.stream(projectUserRepository_.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    /*
     * (non-Javadoc)
     * fr.cnes.regards.modules.accessRights.service.IProjectUserServicee.IProjectUserService#retrieveUserList()
     */
    @Override
    public List<ProjectUser> retrieveUserList() {
        return projectUsers_.stream().filter(p -> !p.getStatus().equals(UserStatus.WAITING_ACCES))
                .collect(Collectors.toList());
    }

    /*
     * (non-Javadocfr.cnes.regards.modules.accessRights.service.IProjectUserService.service.IProjectUserService#
     * retrieveUser(int)
     */
    @Override
    public ProjectUser retrieveUser(final Long pUserId) {
        final List<ProjectUser> notWaitingAccess = projectUsers_.stream()
                .filter(p -> !p.getStatus().equals(UserStatus.WAITING_ACCES)).collect(Collectors.toList());
        final ProjectUser wanted = notWaitingAccess.stream().filter(p -> p.getId() == pUserId).findFirst().get();
        final List<MetaData> visible = wanted.getMetaData().stream()
                .filter(m -> !m.getVisibility().equals(UserVisibility.HIDDEN)).collect(Collectors.toList());
        final ProjectUser sent = new ProjectUser(wanted.getId(), wanted.getLastConnection(), wanted.getLastUpdate(),
                wanted.getStatus(), visible, wanted.getRole(), wanted.getPermissions(), wanted.getAccount());
        return sent;
    }

    @Override
    public ProjectUser retrieveUser(final String pLogin) {
        return projectUserRepository_.findOneByLogin(pLogin);
    }

    /*
     * (non-fr.cnes.regards.modules.accessRights.service.IProjectUserServicesRights.service.IProjectUserService#
     * updateUser(int, fr.cnes.regards.modules.accessRights.domain.ProjectUser)
     */
    @Override
    public void updateUser(final Long pUserId, final ProjectUser pUpdatedProjectUser)
            throws OperationNotSupportedException {
        if (existUser(pUserId)) {
            if (pUpdatedProjectUser.getId() == pUserId) {
                projectUsers_ = projectUsers_.stream().map(a -> a.getId() == pUserId ? pUpdatedProjectUser : a)
                        .collect(Collectors.toList());
                return;
            }
            throw new OperationNotSupportedException("Account id specified differs from updated account id");
        }
        throw new NoSuchElementException(pUserId + "");
    }

    /**
     * @param pUserId
     * @return
     */
    @Override
    public boolean existUser(final Long pUserId) {
        return projectUsers_.stream().filter(p -> !p.getStatus().equals(UserStatus.WAITING_ACCES))
                .filter(p -> p.getId() == pUserId).findFirst().isPresent();
    }

    @Override
    public boolean existUser(final String pUserLogin) {
        return projectUserRepository_.exists(pUserLogin);
    }

    /*
     * fr.cnes.regards.modules.accessRights.service.IProjectUserServices.accessRights.service.IProjectUserService#
     * removeUser(int)
     */
    @Override
    public void removeUser(final Long pUserId) {
        projectUsers_ = projectUsers_.stream().filter(p -> p.getId() != pUserId).collect(Collectors.toList());
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.accessRights.service.IUserService#retrieveUserAccessRights(int)
     */
    @Override
    public Couple<List<ResourcesAccess>, Role> retrieveProjectUserAccessRights(final String pLogin,
            final String pBorrowedRoleName)
            throws OperationNotSupportedException {
        final ProjectUser projectUser = projectUserRepository_.findOneByLogin(pLogin);
        final Role userRole = projectUser.getRole();
        Role returnedRole = userRole;

        if (pBorrowedRoleName != null) {
            final Role borrowedRole = roleRepository_.findOneByName(pBorrowedRoleName);
            if (roleService_.isHierarchicallyInferior(borrowedRole, returnedRole)) {
                returnedRole = borrowedRole;
            } else {
                throw new OperationNotSupportedException(
                        "Borrowed role must be hierachically inferior to the project user's role");
            }
        }

        return new Couple<>(projectUser.getPermissions(), returnedRole);
    }

    @Override
    public void updateUserAccessRights(final String pLogin, final List<ResourcesAccess> pUpdatedUserAccessRights) {
        if (!existUser(pLogin)) {
            throw new NoSuchElementException("ProjectUser of given login (" + pLogin + ") could not be found");
        }
        final ProjectUser user = retrieveUser(pLogin);

        // Finder method
        // Pass the id and the list to search, returns the element with passed id
        final Function<Long, List<ResourcesAccess>> find = (id) -> {
            return pUpdatedUserAccessRights.stream().filter(e -> e.getId().equals(id)).collect(Collectors.toList());
        };
        final Function<Long, Boolean> contains = (id) -> {
            return !find.apply(id).isEmpty();
        };

        final List<ResourcesAccess> permissions = user.getPermissions();
        // If an element with the same id is found in the pResourcesAccessList list, replace with it
        // Else keep the old element
        permissions.replaceAll(p -> contains.apply(p.getId()) ? find.apply(p.getId()).get(0) : p);
    }

    @Override
    public void removeUserAccessRights(final String pLogin) {
        final ProjectUser user = retrieveUser(pLogin);
        user.setPermissions(new ArrayList<>());
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
    }

    @Override
    public void removeUserMetaData(final Long pUserId) {
        final ProjectUser user = retrieveUser(pUserId);
        user.setMetaData(new ArrayList<>());

    }

}
