/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessrights.rest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.cnes.regards.framework.hateoas.LinkRels;
import fr.cnes.regards.framework.jpa.instance.transactional.InstanceTransactional;
import fr.cnes.regards.framework.module.rest.exception.EntityAlreadyExistsException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.security.endpoint.MethodAuthorizationService;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.accessrights.dao.instance.IAccountRepository;
import fr.cnes.regards.modules.accessrights.dao.projects.IProjectUserRepository;
import fr.cnes.regards.modules.accessrights.domain.AccountStatus;
import fr.cnes.regards.modules.accessrights.domain.instance.Account;
import fr.cnes.regards.modules.accessrights.domain.instance.AccountSettings;
import fr.cnes.regards.modules.accessrights.service.account.IAccountService;
import fr.cnes.regards.modules.accessrights.service.account.IAccountSettingsService;

/**
 * Integration tests for accounts.
 *
 * @author svissier
 * @author Sébastien Binda
 * @author Xavier-Alexandre Brochard
 * @author Christophe Mertz
 * @since 1.0-SNAPSHOT
 */
@InstanceTransactional
public class AccountControllerIT extends AbstractRegardsTransactionalIT {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AccountControllerIT.class);

    /**
     * A dummy account
     */
    private static Account account;

    private static Account accountInstance;

    /**
     * Dummy email
     */
    private static final String EMAIL = "email@test.com";

    /**
     * Dummy first name
     */
    private static final String FIRST_NAME = "Firstname";

    /**
     * Dummy last name
     */
    private static final String LAST_NAME = "Lastname";

    /**
     * Dummy password
     */
    private static final String PASSWORD = "password";

    @Autowired
    private MethodAuthorizationService authService;

    private String apiAccounts;

    private String apiAccountId;

    private String apiAccountEmail;

    private String apiAccountSetting;

    private String apiUnlockAccount;

    private String apiChangePassword;

    private final String apiValidatePassword = "/accounts/{account_email}/validate?password={account_password}";

    private final String apiEmailValidation = "/accounts/{account_email}/emailValidation/{validation_code}";

    private String errorMessage;

    @Autowired
    private IAccountService accountService;

    @Autowired
    private IAccountRepository accountRepository;

    @Autowired
    private IAccountSettingsService settingsService;

    @Autowired
    private IProjectUserRepository projectUserRepository;

    @Value("${root.admin.login:admin}")
    private String rootAdminLogin;

    @Value("${root.admin.password:admin}")
    private String rootAdminPassword;

    @Value("${regards.accounts.root.user.login}")
    private String rootAdminInstanceLogin;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private String apiAccountCode;

    @Before
    public void setUp() {
        errorMessage = "Cannot reach model attributes";
        apiAccounts = "/accounts";
        apiAccountId = apiAccounts + "/{account_id}";
        apiAccountEmail = apiAccounts + "/account/{account_email}";
        apiAccountSetting = apiAccounts + "/settings";
        apiUnlockAccount = apiAccountId + "/unlock/{unlock_code}";
        apiChangePassword = apiAccountId + "/password/{reset_code}";
        apiAccountCode = apiAccounts + "/code";

        // And start with a single account for convenience
        account = accountRepository.save(new Account(EMAIL, FIRST_NAME, LAST_NAME, PASSWORD));
        accountInstance = accountRepository.save(new Account(rootAdminInstanceLogin, FIRST_NAME, LAST_NAME, PASSWORD));
    }

    @Test
    @Requirement("REGARDS_DSL_ADM_ADM_300")
    @Purpose("Check that the system allows to retrieve all users for an instance.")
    public void getAllAccounts() {
        final List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isOk());
        performDefaultGet(apiAccounts, expectations, errorMessage);
    }

    @Test
    public void getSettings() {
        final List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isOk());
        performDefaultGet(apiAccountSetting, expectations, errorMessage);
    }

    @Test
    @Requirement("REGARDS_DSL_ADM_ADM_300")
    @Purpose("Check that the system allows to create a user for an instance and handle fail cases.")
    public void createAccount() {
        accountRepository.deleteAll();

        // Regular success case
        final List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isCreated());
        performDefaultPost(apiAccounts, account, expectations, errorMessage);

        // Conflict case
        expectations.clear();
        expectations.add(status().isConflict());
        performDefaultPost(apiAccounts, account, expectations, errorMessage);

        // Malformed case
        final Account containNulls = new Account("notanemail", "", null, null);
        expectations.clear();
        expectations.add(status().isBadRequest());
        performDefaultPost(apiAccounts, containNulls, expectations, errorMessage);
    }

    @Test
    @Requirement("REGARDS_DSL_ADM_ADM_300")
    @Purpose("Check that the system allows to retrieve a specific user for an instance and handle fail cases.")
    public void getAccount() throws EntityAlreadyExistsException {
        final List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isOk());
        performDefaultGet(apiAccountId, expectations, errorMessage, account.getId());

        expectations.clear();
        expectations.add(status().isNotFound());
        performDefaultGet(apiAccountId, expectations, errorMessage, Integer.MAX_VALUE);
    }

    @Test
    @Requirement("REGARDS_DSL_ADM_ADM_300")
    @Purpose("Check that the system allows to retrieve a specific user for an instance and handle fail cases.")
    public void getAccountByEmail() throws EntityAlreadyExistsException {
        final List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isOk());
        performDefaultGet(apiAccountEmail, expectations, errorMessage, account.getEmail());

        expectations.clear();
        expectations.add(status().isNotFound());
        performDefaultGet(apiAccountEmail, expectations, errorMessage, "error@regards.fr");
    }

    @Test
    @Purpose("Check that the system allows to update account settings.")
    public void updateAccountSetting() {
        final List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isOk());
        final AccountSettings toUpdate = settingsService.retrieve();
        toUpdate.setMode("manual");
        performDefaultPut(apiAccountSetting, toUpdate, expectations, errorMessage);

        expectations.clear();
        expectations.add(status().isOk());
        toUpdate.setMode("auto-accept");
        performDefaultPut(apiAccountSetting, toUpdate, expectations, errorMessage);

        expectations.clear();
        expectations.add(status().isBadRequest());
        toUpdate.setMode("sdfqjkmfsdq");
        performDefaultPut(apiAccountSetting, toUpdate, expectations, errorMessage);
    }

    @Test
    @Requirement("REGARDS_DSL_ADM_ADM_440")
    @Requirement("REGARDS_DSL_ADM_ADM_450")
    @Requirement("REGARDS_DSL_ADM_ADM_460")
    @Requirement("REGARDS_DSL_ADM_ADM_470")
    @Purpose("Check that the system allows to provide a reset/unlock code associated to an instance user.")
    public void getCode() throws EntityAlreadyExistsException {
        final List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isNoContent());
        performDefaultGet(apiAccountCode + "?email=" + account.getEmail() + "&type=UNLOCK", expectations, errorMessage);
    }

    @Test
    @Requirement("REGARDS_DSL_ADM_ADM_300")
    @Purpose("Check that the system allows to update user for an instance and handle fail cases.")
    public void updateAccount() throws EntityAlreadyExistsException {
        // Prepare the account
        account.setFirstName("AnOtherFirstName");

        // if that's the same functional ID and the parameter is valid:
        final List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isOk());
        performDefaultPut(apiAccountId, account, expectations, errorMessage, account.getId());

        // if that's not the same functional ID and the parameter is valid:
        expectations.clear();
        expectations.add(status().isBadRequest());
        performDefaultPut(apiAccountId, account, expectations, errorMessage, 99L);

        // If entity not found
        final Long unknownId = account.getId();
        accountRepository.delete(account);

        expectations.clear();
        expectations.add(status().isNotFound());
        performDefaultPut(apiAccountId, account, expectations, errorMessage, unknownId);
    }

    @Test
    @Requirement("REGARDS_DSL_ADM_ADM_450")
    @Purpose("Check that the system allows to unlock an instance user's account.")
    public void unlockAccount() throws EntityAlreadyExistsException {
        // Prepare the account
        account.setStatus(AccountStatus.LOCKED);
        accountRepository.save(account);

        final List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isNoContent());
        performDefaultGet(apiUnlockAccount, expectations, errorMessage, account.getId(), account.getCode());
    }

    @Test
    @Requirement("REGARDS_DSL_ADM_ADM_470")
    @Purpose("Check that the system allows to reset an instance user's password.")
    public void changeAccountPassword() throws EntityAlreadyExistsException {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(status().isNoContent());
        performDefaultPut(apiChangePassword, "newPassword", expectations, errorMessage, account.getId(),
                          account.getCode());

    }

    /**
     * Check that the system prevents from deleting an account linked to any project users.
     *
     * @throws EntityNotFoundException
     *             when no role with name 'PUBLIC' could be found
     */
    @Test
    @Requirement("REGARDS_DSL_ADM_ADM_300")
    @Purpose("Check that the system prevents from deleting an account linked to any project users.")
    public void deleteAccount_notAllowedBecauseOfLinkedProjectUser() throws EntityNotFoundException {
        // jwtService.injectMockToken(AbstractAdministrationIT.PROJECT_TEST_NAME, ROLE_TEST);
        // projectUserRepository.save(new ProjectUser(EMAIL, roleTest, roleTest.getPermissions(), new ArrayList<>()));

        // Prepare the account. Must have a status allowing deletion
        account.setStatus(AccountStatus.INACTIVE);
        accountRepository.save(account);

        final List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isForbidden());
        performDefaultDelete(apiAccountId, expectations, errorMessage, account.getId());
    }

    /**
     * Check that the system allows to delete an account linked to no project user.
     */
    @Test
    @Requirement("REGARDS_DSL_ADM_ADM_300")
    @Purpose("Check that the system allows to delete an account linked to no project user.")
    public void deleteAccount() {
        // Prepare the account. Must have a status allowing deletion and have no project user
        account.setEmail("randomEmailNotMatchingAnyProjectUser@test.com");
        account.setStatus(AccountStatus.INACTIVE);
        accountRepository.save(account);

        final List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isNoContent());
        performDefaultDelete(apiAccountId, expectations, errorMessage, account.getId());
    }

    @Test
    @Requirement("REGARDS_DSL_SYS_SEC_100")
    @Purpose("Check that the system allows validate an instance user's password.")
    public void validatePassword() {
        final String wrongPassword = "wrongPassword";
        Assert.assertNotEquals(PASSWORD, wrongPassword);

        final List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isOk());
        expectations.add(MockMvcResultMatchers.content().string("\"" + AccountStatus.ACTIVE.toString() + "\""));
        performDefaultGet(apiValidatePassword, expectations, errorMessage, EMAIL, PASSWORD);

        expectations.clear();
        expectations.add(status().isOk());
        expectations.add(MockMvcResultMatchers.content().string("\"" + AccountStatus.INACTIVE.toString() + "\""));
        performDefaultGet(apiValidatePassword, expectations, errorMessage, EMAIL, wrongPassword);

        final String wrongEmail = "wrongEmail";
        Assert.assertFalse(accountService.existAccount(wrongEmail));
        expectations.clear();
        expectations.add(status().isNotFound());
        performDefaultGet(apiValidatePassword, expectations, errorMessage, wrongEmail, PASSWORD);
    }

    @Test
    public void getAccountUser() {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath("$._links", Matchers.notNullValue()));
//        expectations.add(MockMvcResultMatchers.jsonPath("$._links.delete", Matchers.notNullValue()));
//        expectations.add(MockMvcResultMatchers.jsonPath("$._links.self", Matchers.notNullValue()));
//        expectations.add(MockMvcResultMatchers.jsonPath("$._links.update", Matchers.notNullValue()));
//        expectations.add(MockMvcResultMatchers.jsonPath("$._links.delete.href", Matchers.notNullValue()));
//        expectations.add(MockMvcResultMatchers.jsonPath("$._links.self.href", Matchers.notNullValue()));
//        expectations.add(MockMvcResultMatchers.jsonPath("$._links.update.href", Matchers.notNullValue()));

        performDefaultGet(apiAccountId, expectations, errorMessage, account.getId());
    }

    @Test
    public void getAccountInstance() {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath("$._links", Matchers.notNullValue()));
//        expectations.add(MockMvcResultMatchers.jsonPath("$._links.self", Matchers.notNullValue()));
//        expectations.add(MockMvcResultMatchers.jsonPath("$._links.update", Matchers.notNullValue()));
//        expectations.add(MockMvcResultMatchers.jsonPath("$._links.self.href", Matchers.notNullValue()));
//        expectations.add(MockMvcResultMatchers.jsonPath("$._links.update.href", Matchers.notNullValue()));
//        expectations.add(MockMvcResultMatchers.jsonPath("$._links",
//                                                        Matchers.not(Matchers.containsString(LinkRels.DELETE))));

        performDefaultGet(apiAccountId, expectations, errorMessage, accountInstance.getId());
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
