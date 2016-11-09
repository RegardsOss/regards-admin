/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessrights.rest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.bind.annotation.RequestMethod;

import fr.cnes.regards.framework.security.endpoint.MethodAuthorizationService;
import fr.cnes.regards.framework.security.utils.jwt.JWTService;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.accessrights.domain.AccountStatus;
import fr.cnes.regards.modules.accessrights.domain.instance.Account;
import fr.cnes.regards.modules.accessrights.domain.instance.AccountSettings;
import fr.cnes.regards.modules.accessrights.service.IAccountService;
import fr.cnes.regards.modules.accessrights.service.IAccountSettingsService;

/**
 *
 * Class AccountControllerIT
 *
 * Just Test the REST API so status code. Correction is left to others.
 *
 * @author svissier
 * @author sbinda
 * @author Xavier-Alexandre Brochard
 * @since 1.0-SNAPSHOT
 */
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
public class AccountControllerIT extends AbstractAdministrationIT {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AccountControllerIT.class);

    @Autowired
    private JWTService jwtService;

    @Autowired
    private MethodAuthorizationService authService;

    private String jwt;

    private String apiAccounts;

    private String apiAccountId;

    private String apiAccountSetting;

    private String apiUnlockAccount;

    private String apiChangePassword;

    private final String apiValidatePassword = "/accounts/{account_login}/validate?password={account_password}";

    private String errorMessage;

    @Autowired
    private IAccountService accountService;

    @Autowired
    private IAccountSettingsService settingsService;

    @Value("${root.admin.login:admin}")
    private String rootAdminLogin;

    @Value("${root.admin.password:admin}")
    private String rootAdminPassword;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private String apiAccountCode;

    @Override
    public void init() {

        final String tenant = AbstractAdministrationIT.PROJECT_TEST_NAME;
        jwt = jwtService.generateToken(tenant, "email", "SVG", "USER");
        authService.setAuthorities(tenant, "/accounts", RequestMethod.GET, "USER");
        authService.setAuthorities(tenant, "/accounts", RequestMethod.POST, "USER");
        authService.setAuthorities(tenant, "/accounts/{account_id}", RequestMethod.GET, "USER");
        authService.setAuthorities(tenant, "/accounts/{account_id}", RequestMethod.PUT, "USER");
        authService.setAuthorities(tenant, "/accounts/{account_id}", RequestMethod.DELETE, "USER");
        authService.setAuthorities(tenant, "/accounts/code", RequestMethod.GET, "USER");
        authService.setAuthorities(tenant, "/accounts/{account_id}/password/{reset_code}", RequestMethod.PUT, "USER");
        authService.setAuthorities(tenant, "/accounts/{account_id}/unlock/{unlock_code}", RequestMethod.GET, "USER");
        authService.setAuthorities(tenant, "/accounts/settings", RequestMethod.GET, "USER");
        authService.setAuthorities(tenant, "/accounts/settings", RequestMethod.PUT, "USER");
        authService.setAuthorities(tenant, "/accounts/{account_login}/validate", RequestMethod.GET, "USER");
        errorMessage = "Cannot reach model attributes";
        apiAccounts = "/accounts";
        apiAccountId = apiAccounts + "/{account_id}";
        apiAccountSetting = apiAccounts + "/settings";
        apiUnlockAccount = apiAccountId + "/unlock/{unlock_code}";
        apiChangePassword = apiAccountId + "/password/{reset_code}";
        apiAccountCode = apiAccounts + "/code";
    }

    @Test
    @Requirement("REGARDS_DSL_ADM_ADM_300")
    @Purpose("Check that the system allows to retrieve all users for an instance.")
    public void getAllAccounts() {
        final List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isOk());
        performGet(apiAccounts, jwt, expectations, errorMessage);
    }

    @Test
    @Requirement("?")
    @Purpose("Check that the system allows to retrieve account settings for an instance.")
    public void getSettings() {
        final List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isOk());
        performGet(apiAccountSetting, jwt, expectations, errorMessage);
    }

    @Test
    @DirtiesContext
    @Requirement("REGARDS_DSL_ADM_ADM_300")
    @Purpose("Check that the system allows to create a user for an instance and handle fail cases.")
    public void createAccount() {
        Account newAccount;
        newAccount = new Account(1584L, "pEmail@email.email", "pFirstName", "pLastName", "pLogin", "pPassword",
                AccountStatus.PENDING, "pCode");

        final List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isCreated());
        performPost(apiAccounts, jwt, newAccount, expectations, errorMessage);

        expectations.clear();
        expectations.add(status().isConflict());
        performPost(apiAccounts, jwt, newAccount, expectations, errorMessage);

        final Account containNulls = new Account();

        expectations.clear();
        expectations.add(status().isUnprocessableEntity());
        performPost(apiAccounts, jwt, containNulls, expectations, errorMessage);
    }

    @Test
    @Requirement("REGARDS_DSL_ADM_ADM_300")
    @Purpose("Check that the system allows to retrieve a specific user for an instance and handle fail cases.")
    public void getAccount() {
        final Long accountId = accountService.retrieveAccountList().get(0).getId();

        final List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isOk());
        performGet(apiAccountId, jwt, expectations, errorMessage, accountId);

        expectations.clear();
        expectations.add(status().isNotFound());
        performGet(apiAccountId, jwt, expectations, errorMessage, Integer.MAX_VALUE);
    }

    @Ignore
    @Test
    @Requirement("?")
    @Purpose("?")
    public void updateAccountSetting() {
        final List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isOk());
        final AccountSettings toUpdate = settingsService.retrieve();
        toUpdate.setMode("manual");
        performPut(apiAccountSetting, jwt, toUpdate, expectations, errorMessage);

        expectations.clear();
        expectations.add(status().isOk());
        toUpdate.setMode("auto-accept");
        performPut(apiAccountSetting, jwt, toUpdate, expectations, errorMessage);

        expectations.clear();
        expectations.add(status().isBadRequest());
        toUpdate.setMode("sdfqjkmfsdq");
        performPut(apiAccountSetting, jwt, toUpdate, expectations, errorMessage);
    }

    @Test
    @Requirement("REGARDS_DSL_ADM_ADM_440")
    @Requirement("REGARDS_DSL_ADM_ADM_450")
    @Requirement("REGARDS_DSL_ADM_ADM_460")
    @Requirement("REGARDS_DSL_ADM_ADM_470")
    @Purpose("Check that the system allows to provide a reset/unlock code associated to an instance user.")
    public void getCode() {
        final Account account = accountService.retrieveAccountList().get(0);
        final String accountEmail = account.getEmail();

        final List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isOk());
        performGet(apiAccountCode + "?email=" + accountEmail + "&type=UNLOCK", jwt, expectations, errorMessage);
    }

    @Test
    @Requirement("REGARDS_DSL_ADM_ADM_300")
    @Purpose("Check that the system allows to update user for an instance and handle fail cases.")
    public void updateAccount() {
        final Account updated = accountService.retrieveAccountList().get(0);
        updated.setFirstName("AnOtherFirstName");
        final Long accountId = updated.getId();

        // if that's the same functional ID and the parameter is valid:
        final List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isOk());
        performPut(apiAccountId, jwt, updated, expectations, errorMessage, accountId);

        // if that's not the same functional ID and the parameter is valid:
        final Account notSameID = new Account("othereamil@test.com", "firstName", "lastName", "login", "password");
        expectations.clear();
        expectations.add(status().isBadRequest());
        performPut(apiAccountId, jwt, notSameID, expectations, errorMessage, accountId);

        // If entity not found
        final Long inexistentId = 99L;
        final Account inexistentAccount = new Account(inexistentId, "email@test.com", "firstname", "lastname", "login",
                "password", AccountStatus.ACTIVE, "code");
        expectations.clear();
        expectations.add(status().isNotFound());
        performPut(apiAccountId, jwt, inexistentAccount, expectations, errorMessage, inexistentId);
    }

    @Test
    @Requirement("REGARDS_DSL_ADM_ADM_450")
    @Purpose("Check that the system allows to unlock an instance user's account.")
    public void unlockAccount() {
        final Account account = accountService.retrieveAccountList().get(0);
        final Long accountId = account.getId();

        final List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isOk());
        performGet(apiUnlockAccount, jwt, expectations, errorMessage, accountId, account.getCode());
    }

    @Test
    @Requirement("REGARDS_DSL_ADM_ADM_470")
    @Purpose("Check that the system allows to reset an instance user's password.")
    public void changeAccountPassword() {
        final Account account = accountService.retrieveAccountList().get(0);
        final Long accountId = account.getId();

        final List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isOk());
        performPut(apiChangePassword, jwt, "newPassword", expectations, errorMessage, accountId, account.getCode());

    }

    @Test
    @DirtiesContext
    @Requirement("REGARDS_DSL_ADM_ADM_300")
    @Purpose("Check that the system allows to delete an instance user.")
    public void deleteAccount() {
        final Long accountId = accountService.retrieveAccountList().get(0).getId();

        final List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isOk());
        performDelete(apiAccountId, jwt, expectations, errorMessage, accountId);

    }

    @Test
    @Requirement("REGARDS_DSL_SYS_SEC_100")
    @Purpose("Check that the system allows validate an instance user's password.")
    public void validatePassword() {
        final Account account = accountService.retrieveAccountList().get(0);
        final String login = account.getLogin();
        final String rightPassword = account.getPassword();
        final String wrongPassword = "wrongPassword";
        assertNotEquals(rightPassword, wrongPassword);

        final List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(status().isOk());
        performGet(apiValidatePassword, jwt, expectations, errorMessage, login, rightPassword);

        expectations.clear();
        expectations.add(status().isUnauthorized());
        performGet(apiValidatePassword, jwt, expectations, errorMessage, login, wrongPassword);

        final String wrongLogin = "wrongLogin";
        assertFalse(accountService.existAccount(wrongLogin));
        expectations.clear();
        expectations.add(status().isNotFound());
        performGet(apiValidatePassword, jwt, expectations, errorMessage, wrongLogin, rightPassword);
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
