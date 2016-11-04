/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessrights.service.test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;

import fr.cnes.regards.framework.security.utils.jwt.JWTAuthentication;
import fr.cnes.regards.framework.security.utils.jwt.UserDetails;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.accessrights.dao.projects.IProjectUserRepository;
import fr.cnes.regards.modules.accessrights.dao.projects.IRoleRepository;
import fr.cnes.regards.modules.accessrights.domain.HttpVerb;
import fr.cnes.regards.modules.accessrights.domain.UserStatus;
import fr.cnes.regards.modules.accessrights.domain.UserVisibility;
import fr.cnes.regards.modules.accessrights.domain.projects.AccessSettings;
import fr.cnes.regards.modules.accessrights.domain.projects.MetaData;
import fr.cnes.regards.modules.accessrights.domain.projects.ProjectUser;
import fr.cnes.regards.modules.accessrights.domain.projects.ResourcesAccess;
import fr.cnes.regards.modules.accessrights.domain.projects.Role;
import fr.cnes.regards.modules.accessrights.service.IProjectUserService;
import fr.cnes.regards.modules.accessrights.service.IRoleService;
import fr.cnes.regards.modules.accessrights.service.ProjectUserService;
import fr.cnes.regards.modules.core.exception.EntityNotFoundException;
import fr.cnes.regards.modules.core.exception.InvalidValueException;

/**
 * Test class for {@link ProjectUserService}.
 *
 * @author xbrochar
 */
public class ProjectUserServiceTest {

    /**
     * A sample project user
     */
    private static ProjectUser projectUser = new ProjectUser();

    /**
     * A sample id
     */
    private static final Long ID = 0L;

    /**
     * A sample email
     */
    private static final String EMAIL = "user@email.com";

    /**
     * A sample last connection date
     */
    private static final LocalDateTime LAST_CONNECTION = LocalDateTime.now().minusDays(2);

    /**
     * A sample last update date
     */
    private static final LocalDateTime LAST_UPDATE = LocalDateTime.now().minusHours(1);

    /**
     * A sample status
     */
    private static final UserStatus STATUS = UserStatus.ACCESS_GRANTED;

    /**
     * A sample meta data list
     */
    private static final List<MetaData> META_DATA = new ArrayList<>();

    /**
     * A sample role
     */
    private static final Role ROLE = new Role(0L, "name", null, new ArrayList<>(), new ArrayList<>());

    /**
     * A sample list of permissions
     */
    private static final List<ResourcesAccess> PERMISSIONS = new ArrayList<>();

    /**
     * The tested service
     */
    private IProjectUserService projectUserService;

    /**
     * Mocked CRUD repository managing {@link ProjectUser}s
     */
    private IProjectUserRepository projectUserRepository;

    /**
     * Mocked service handling CRUD operation on {@link Role}s
     */
    private IRoleService roleService;

    /**
     * Mocked CRUD repository managing {@link Role}s
     */
    private IRoleRepository roleRepository;

    /**
     * Do some setup before each test
     */
    @Before
    public void setUp() {
        // Initialize a sample user
        projectUser.setId(ID);
        projectUser.setEmail(EMAIL);
        projectUser.setLastConnection(LAST_CONNECTION);
        projectUser.setLastUpdate(LAST_UPDATE);
        projectUser.setStatus(STATUS);
        projectUser.setMetaData(META_DATA);
        projectUser.setPermissions(PERMISSIONS);
        projectUser.getPermissions().add(new ResourcesAccess(0L, "desc0", "ms0", "res0", HttpVerb.GET));
        projectUser.getPermissions().add(new ResourcesAccess(1L, "desc1", "ms1", "res1", HttpVerb.PUT));
        projectUser.setRole(ROLE);

        // Mock untested services & repos
        projectUserRepository = Mockito.mock(IProjectUserRepository.class);
        roleService = Mockito.mock(IRoleService.class);
        roleRepository = Mockito.mock(IRoleRepository.class);

        // Construct the tested service
        projectUserService = new ProjectUserService(projectUserRepository, roleService, roleRepository,
                "instance_admin@regards.fr");
    }

    /**
     * Check that the system allows to retrieve the users of a project.
     */
    @Test
    @Requirement("REGARDS_DSL_ADM_ADM_300")
    @Requirement("REGARDS_DSL_ADM_ADM_310")
    @Requirement("REGARDS_DSL_ADM_ADM_320")
    @Purpose("Check that the system allows to retrieve the users of a project.")
    public void retrieveUserList() {
        // Define expected
        final List<ProjectUser> expected = new ArrayList<>();
        expected.add(projectUser);
        projectUser.setStatus(UserStatus.ACCESS_GRANTED);

        // Mock the repository returned value
        Mockito.when(projectUserRepository.findByStatus(UserStatus.ACCESS_GRANTED)).thenReturn(expected);

        // Retrieve actual value
        final List<ProjectUser> actual = projectUserService.retrieveUserList();

        // Check that the expected and actual role have same values
        Assert.assertEquals(expected, actual);

        // Check that the repository's method was called with right arguments
        Mockito.verify(projectUserRepository).findByStatus(UserStatus.ACCESS_GRANTED);
    }

    /**
     * Check that the system allows to retrieve a specific user without exposing hidden meta data.
     */
    @Test
    @Requirement("REGARDS_DSL_ADM_ADM_300")
    @Requirement("REGARDS_DSL_ADM_ADM_310")
    @Requirement("REGARDS_DSL_ADM_ADM_320")
    @Purpose("Check that the system allows to retrieve a specific user without exposing hidden meta data.")
    public void retrieveUser() {
        // Define user as in db
        final MetaData metaData0 = new MetaData();
        metaData0.setVisibility(UserVisibility.HIDDEN);
        projectUser.getMetaData().add(metaData0);
        final MetaData metaData1 = new MetaData();
        metaData1.setVisibility(UserVisibility.READABLE);
        projectUser.getMetaData().add(metaData1);
        final MetaData metaData2 = new MetaData();
        metaData2.setVisibility(UserVisibility.WRITEABLE);
        projectUser.getMetaData().add(metaData2);

        // Define user as expected
        final ProjectUser expected = new ProjectUser();
        final List<MetaData> visibleMetaData = new ArrayList<>();
        visibleMetaData.add(metaData1);
        visibleMetaData.add(metaData2);
        expected.setId(ID);
        expected.setEmail(EMAIL);
        expected.setLastUpdate(LAST_UPDATE);
        expected.setLastConnection(LAST_CONNECTION);
        expected.setStatus(STATUS);
        expected.setPermissions(PERMISSIONS);
        expected.setRole(ROLE);
        expected.setMetaData(visibleMetaData);

        // Mock the repository returned value
        Mockito.when(projectUserRepository.findOne(ID)).thenReturn(projectUser);

        // Retrieve actual value
        final ProjectUser actual = projectUserService.retrieveUser(ID);

        // Check same values
        Assert.assertThat(actual, Matchers.samePropertyValuesAs(expected));

        // Check that the repository's method was called with right arguments
        Mockito.verify(projectUserRepository).findOne(ID);
    }

    /**
     * Check that the system allows to retrieve a specific user by email.
     *
     * @throws EntityNotFoundException
     *             Thrown when no {@link ProjectUser} with passed <code>id</code> could be found
     */
    @Test
    @Requirement("REGARDS_DSL_ADM_ADM_300")
    @Requirement("REGARDS_DSL_ADM_ADM_310")
    @Requirement("REGARDS_DSL_ADM_ADM_320")
    @Purpose("Check that the system allows to retrieve a specific user by email.")
    public void retrieveOneByEmail() throws EntityNotFoundException {
        // Mock the repository returned value
        Mockito.when(projectUserRepository.findOneByEmail(EMAIL)).thenReturn(projectUser);

        // Retrieve actual value
        final ProjectUser actual = projectUserService.retrieveOneByEmail(EMAIL);

        // Check same values
        Assert.assertThat(actual, Matchers.samePropertyValuesAs(projectUser));

        // Check that the repository's method was called with right arguments
        Mockito.verify(projectUserRepository).findOneByEmail(EMAIL);
    }

    /**
     * Check that the system fails when trying to retrieve a user with unknown email.
     *
     * @throws EntityNotFoundException
     *             Thrown when no {@link ProjectUser} with passed <code>id</code> could be found
     */
    @Test(expected = EntityNotFoundException.class)
    @Requirement("REGARDS_DSL_ADM_ADM_300")
    @Requirement("REGARDS_DSL_ADM_ADM_310")
    @Requirement("REGARDS_DSL_ADM_ADM_320")
    @Purpose("Check that the system fails when trying to retrieve a user with unknown email.")
    public void retrieveOneByEmailNotFound() throws EntityNotFoundException {
        // Mock the repository returned value
        Mockito.when(projectUserRepository.findOneByEmail(EMAIL)).thenReturn(null);

        // Trigger the exception
        projectUserService.retrieveOneByEmail(EMAIL);
    }

    /**
     * Check that the system allows to retrieve the current logged user.
     */
    @Test
    @Requirement("REGARDS_DSL_ADM_ADM_300")
    @Requirement("REGARDS_DSL_ADM_ADM_310")
    @Requirement("REGARDS_DSL_ADM_ADM_320")
    @Purpose("Check that the system allows to retrieve the current logged user.")
    public void retrieveCurrentUser() {
        // Mock authentication
        final JWTAuthentication jwtAuth = new JWTAuthentication("foo");
        final UserDetails details = new UserDetails();
        details.setName(EMAIL);
        jwtAuth.setUser(details);
        SecurityContextHolder.getContext().setAuthentication(jwtAuth);

        // Mock the repository returned value
        Mockito.when(projectUserRepository.findOneByEmail(EMAIL)).thenReturn(projectUser);

        // Retrieve actual value
        final ProjectUser actual = projectUserService.retrieveCurrentUser();

        // Check
        Assert.assertThat(actual, Matchers.is(Matchers.equalTo(projectUser)));

        // Check that the repository's method was called with right arguments
        Mockito.verify(projectUserRepository).findOneByEmail(EMAIL);
    }

    /**
     * Check that the system fails when trying to update a non existing project user.
     *
     * @throws EntityNotFoundException
     *             Thrown when an {@link AccountSettings} with passed id could not be found
     * @throws InvalidValueException
     *             Thrown when user id differs from the passed id
     */
    @Test(expected = EntityNotFoundException.class)
    @Requirement("REGARDS_DSL_ADM_ADM_300")
    @Requirement("REGARDS_DSL_ADM_ADM_310")
    @Requirement("REGARDS_DSL_ADM_ADM_320")
    @Purpose("Check that the system fails when trying to update a non existing project user.")
    public void updateUserEntityNotFound() throws EntityNotFoundException, InvalidValueException {
        // Mock the repository returned value
        Mockito.when(projectUserRepository.exists(ID)).thenReturn(false);

        // Trigger the exception
        projectUserService.updateUser(ID, projectUser);
    }

    /**
     * Check that the system fails when user id differs from the passe id.
     *
     * @throws EntityNotFoundException
     *             Thrown when an {@link AccountSettings} with passed id could not be found
     * @throws InvalidValueException
     *             Thrown when user id differs from the passed id
     */
    @Test(expected = InvalidValueException.class)
    @Requirement("REGARDS_DSL_ADM_ADM_300")
    @Requirement("REGARDS_DSL_ADM_ADM_310")
    @Requirement("REGARDS_DSL_ADM_ADM_320")
    @Purpose("Check that the system fails when user id differs from the passed id.")
    public void updateUserInvalidValue() throws EntityNotFoundException, InvalidValueException {
        // Mock the repository returned value
        Mockito.when(projectUserRepository.exists(ID)).thenReturn(true);

        // Trigger the exception
        projectUserService.updateUser(1L, projectUser);
    }

    /**
     * Check that the system allows to update a project user.
     *
     * @throws EntityNotFoundException
     *             Thrown when an {@link AccountSettings} with passed id could not be found
     * @throws InvalidValueException
     *             Thrown when user id differs from the passed id
     */
    @Test
    @Requirement("REGARDS_DSL_ADM_ADM_300")
    @Requirement("REGARDS_DSL_ADM_ADM_310")
    @Requirement("REGARDS_DSL_ADM_ADM_320")
    @Purpose("Check that the system allows to update a project user.")
    public void updateUser() throws EntityNotFoundException, InvalidValueException {
        // Mock repository
        Mockito.when(projectUserRepository.exists(ID)).thenReturn(true);

        // Try to update a user
        projectUserService.updateUser(ID, projectUser);

        // Check that the repository's method was called with right arguments
        Mockito.verify(projectUserRepository).save(projectUser);
    }

    /**
     * Check that the system allows to delete a project user.
     */
    @Test
    @Requirement("REGARDS_DSL_ADM_ADM_300")
    @Requirement("REGARDS_DSL_ADM_ADM_310")
    @Requirement("REGARDS_DSL_ADM_ADM_320")
    @Purpose("Check that the system allows to delete a project user.")
    public void removeUser() {
        // Try to update a user
        projectUserService.removeUser(ID);

        // Check that the repository's method was called with right arguments
        Mockito.verify(projectUserRepository).delete(ID);
    }

    /**
     * Check that the system fails when trying to override a not exisiting user's access rights.
     *
     * @throws EntityNotFoundException
     *             Thrown when no user of passed login could be found
     */
    @Test(expected = EntityNotFoundException.class)
    @Requirement("REGARDS_DSL_ADM_ADM_230")
    @Requirement("REGARDS_DSL_ADM_ADM_480")
    @Purpose("Check that the system fails when trying to override a not exisiting user's access rights.")
    public void updateUserAccessRightsEntityNotFound() throws EntityNotFoundException {
        // Mock the repository returned value
        Mockito.when(projectUserRepository.findOneByEmail(EMAIL)).thenReturn(null);

        // Trigger the exception
        projectUserService.updateUserAccessRights(EMAIL, new ArrayList<>());
    }

    /**
     * Check that the system allows to override role's access rights for a user.
     *
     * @throws EntityNotFoundException
     *             Thrown when no user of passed login could be found
     */
    @Test
    @Requirement("REGARDS_DSL_ADM_ADM_230")
    @Requirement("REGARDS_DSL_ADM_ADM_480")
    @Purpose("Check that the system allows to override role's access rights for a user.")
    public void updateUserAccessRights() throws EntityNotFoundException {
        // Mock the repository returned value
        Mockito.when(projectUserRepository.findOneByEmail(EMAIL)).thenReturn(projectUser);

        // Define updated permissions
        final List<ResourcesAccess> input = new ArrayList<>();
        // Updating an existing one
        final ResourcesAccess updatedPermission = new ResourcesAccess(0L, "updated desc0", "updated ms0",
                "updated res0", HttpVerb.POST);
        input.add(updatedPermission);
        // Adding a new permission
        final ResourcesAccess newPermission = new ResourcesAccess(2L, "desc2", "ms2", "res2", HttpVerb.GET);
        input.add(newPermission);

        // Define expected result
        final ProjectUser expected = new ProjectUser();
        expected.setId(ID);
        expected.setEmail(EMAIL);
        expected.setLastConnection(LAST_CONNECTION);
        expected.setLastUpdate(LAST_UPDATE);
        expected.setStatus(STATUS);
        expected.setMetaData(META_DATA);
        expected.setRole(ROLE);
        expected.setPermissions(new ArrayList<>());
        expected.getPermissions().add(updatedPermission);
        expected.getPermissions().add(newPermission);
        expected.getPermissions().add(projectUser.getPermissions().get(1));

        // Call method
        projectUserService.updateUserAccessRights(EMAIL, input);

        // Check
        Mockito.verify(projectUserRepository).save(Mockito.refEq(expected, "lastConnection", "lastUpdate"));
    }
}
