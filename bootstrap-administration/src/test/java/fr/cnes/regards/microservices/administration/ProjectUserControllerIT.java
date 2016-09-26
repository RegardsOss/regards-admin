/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.microservices.administration;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultMatcher;

import fr.cnes.regards.microservices.core.security.jwt.JWTService;
import fr.cnes.regards.microservices.core.test.RegardsIntegrationTest;
import fr.cnes.regards.modules.accessRights.dao.IRoleRepository;
import fr.cnes.regards.modules.accessRights.domain.HttpVerb;
import fr.cnes.regards.modules.accessRights.domain.MetaData;
import fr.cnes.regards.modules.accessRights.domain.ProjectUser;
import fr.cnes.regards.modules.accessRights.domain.ResourcesAccess;
import fr.cnes.regards.modules.accessRights.domain.Role;
import fr.cnes.regards.modules.accessRights.service.IRoleService;
import fr.cnes.regards.modules.accessRights.service.IUserService;

/**
 * @author svissier
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProjectUserControllerIT extends RegardsIntegrationTest {

    @Autowired
    private JWTService jwtService_;

    private String jwt_;

    private String apiUsers;

    private String apiUserId;

    private String apiUserPermissions;

    private String apiUserPermissionsBorrowedRole;

    private String apiUserMetaData;

    private String errorMessage;

    @Autowired
    private IUserService userService_;

    @Autowired
    private IRoleRepository roleRepository_;

    @Autowired
    private IRoleService roleService_;

    @Before
    public void init() {
        setLogger(LoggerFactory.getLogger(ProjectUserControllerIT.class));
        jwt_ = jwtService_.generateToken("PROJECT", "email", "SVG", "USER");
        apiUsers = "/users";
        apiUserId = apiUsers + "/{user_id}";
        apiUserPermissions = apiUserId + "/permissions";
        apiUserPermissionsBorrowedRole = apiUserPermissions + "?borrowedRoleName=";
        apiUserMetaData = apiUserId + "/metadata";
        errorMessage = "Cannot reach model attributes";
    }

    @Test
    public void aGetAllUsers() {

        List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isOk());
        performGet(apiUsers, jwt_, expectations, errorMessage);

    }

    @Test
    public void cGetUser() {

        Long userId = userService_.retrieveUserList().get(0).getId();

        List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isOk());
        performGet(apiUserId, jwt_, expectations, errorMessage, userId);

        expectations.clear();
        expectations.add(status().isNotFound());
        performGet(apiUserId, jwt_, expectations, errorMessage, Long.MAX_VALUE);

    }

    @Test
    public void cGetUserMetaData() {
        Long userId = userService_.retrieveUserList().get(0).getId();

        List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isOk());
        performGet(apiUserMetaData, jwt_, expectations, errorMessage, userId);
    }

    @Test
    public void cGetUserPermissions() {

        Long userId = userService_.retrieveUserList().get(0).getId();

        List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isOk());
        performGet(apiUserPermissions, jwt_, expectations, errorMessage, userId);

        expectations.clear();
        expectations.add(status().isNotFound());
        performGet(apiUserPermissions, jwt_, expectations, errorMessage, Long.MAX_VALUE);
    }

    @Test
    public void cGetUserPermissionsWithBorrowedRole() {
        Long projectUserId = 1L;
        ProjectUser projectUser = userService_.retrieveUser(projectUserId);
        Role projectUserRole = projectUser.getRole();
        List<ResultMatcher> expectations = new ArrayList<>(1);

        // Borrowing a hierarchically inferior role
        String borrowedRoleName = "Registered User";
        assertTrue(roleRepository_.findOneByName(borrowedRoleName) != null);
        Role borrowedRole = roleRepository_.findOneByName(borrowedRoleName);
        assertTrue(roleService_.isHierarchicallyInferior(borrowedRole, projectUserRole));
        expectations.add(status().isOk());
        performGet(apiUserPermissionsBorrowedRole + borrowedRoleName, jwt_, expectations, errorMessage, projectUserId);

        // Borrowing a hierarchically superior role
        borrowedRoleName = "Instance Admin";
        assertTrue(roleRepository_.findOneByName(borrowedRoleName) != null);
        borrowedRole = roleRepository_.findOneByName(borrowedRoleName);
        assertTrue(!roleService_.isHierarchicallyInferior(borrowedRole, projectUserRole));
        expectations.clear();
        expectations.add(status().isBadRequest());
        performGet(apiUserPermissionsBorrowedRole + borrowedRoleName, jwt_, expectations, errorMessage, projectUserId);
    }

    @Test
    public void dUpdateUserMetaData() {
        Long userId = userService_.retrieveUserList().get(0).getId();
        List<MetaData> newPermissionList = new ArrayList<>();
        newPermissionList.add(new MetaData());
        newPermissionList.add(new MetaData());

        List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isOk());
        performPut(apiUserMetaData, jwt_, newPermissionList, expectations, errorMessage, userId);
    }

    @Test
    public void dUpdateUserPermissions() {
        Long userId = userService_.retrieveUserList().get(0).getId();

        List<ResourcesAccess> newPermissionList = new ArrayList<>();
        newPermissionList.add(new ResourcesAccess(463L, "new", "new", "new", HttpVerb.PUT));
        newPermissionList.add(new ResourcesAccess(350L, "neww", "neww", "neww", HttpVerb.DELETE));

        List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isOk());
        performPut(apiUserPermissions, jwt_, newPermissionList, expectations, errorMessage, userId);
    }

    @Test
    public void dDeleteUserMetaData() {
        Long userId = userService_.retrieveUserList().get(0).getId();

        List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isOk());
        performDelete(apiUserMetaData, jwt_, expectations, errorMessage, userId);
    }

    @Test
    public void dDeleteUserPermissions() {
        Long userId = userService_.retrieveUserList().get(0).getId();

        List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isOk());
        performDelete(apiUserPermissions, jwt_, expectations, errorMessage, userId);
    }

    @Test
    public void dUpdateUser() {
        Long userId = userService_.retrieveUserList().get(0).getId();
        ProjectUser updated = userService_.retrieveUser(userId);
        updated.setLastConnection(LocalDateTime.now());

        // if that's the same functional ID and the parameter is valid:
        List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isOk());
        performPut(apiUserId, jwt_, updated, expectations, errorMessage, userId);

        // if that's not the same functional ID and the parameter is valid:
        ProjectUser notSameID = new ProjectUser();

        expectations.clear();
        expectations.add(status().isBadRequest());
        performPut(apiUserId, jwt_, notSameID, expectations, errorMessage, userId);
    }

    @Test
    public void eDeleteUser() {
        Long userId = userService_.retrieveUserList().get(0).getId();

        List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isOk());
        performDelete(apiUserId, jwt_, expectations, errorMessage, userId);
    }
}