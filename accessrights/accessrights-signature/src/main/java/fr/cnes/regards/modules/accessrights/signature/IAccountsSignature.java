/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessrights.signature;

import java.util.List;

import javax.validation.Valid;

import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import fr.cnes.regards.framework.module.rest.exception.EntityException;
import fr.cnes.regards.framework.module.rest.exception.EntityTransitionForbiddenException;
import fr.cnes.regards.framework.module.rest.exception.InvalidValueException;
import fr.cnes.regards.framework.module.rest.exception.ModuleAlreadyExistsException;
import fr.cnes.regards.framework.module.rest.exception.ModuleEntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.module.rest.exception.OperationForbiddenException;
import fr.cnes.regards.modules.accessrights.domain.AccountStatus;
import fr.cnes.regards.modules.accessrights.domain.CodeType;
import fr.cnes.regards.modules.accessrights.domain.instance.Account;
import fr.cnes.regards.modules.accessrights.domain.instance.AccountSettings;

/**
 * Define the common interface of REST clients for {@link Account}s.
 *
 * @author CS SI
 */
@RequestMapping(path = "/accounts")
public interface IAccountsSignature {

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<Resource<Account>>> retrieveAccountList();

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Resource<Account>> createAccount(@Valid @RequestBody Account pNewAccount)
            throws EntityTransitionForbiddenException, ModuleAlreadyExistsException;

    @RequestMapping(value = "/{account_id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    ResponseEntity<Resource<Account>> retrieveAccount(@PathVariable("account_id") Long pAccountId)
            throws ModuleEntityNotFoundException;

    /**
     *
     * Retrieve an account by his unique email
     *
     * @param pAccountEmail
     *            email of the account to retrieve
     * @return Account
     */
    @RequestMapping(value = "/account/{account_email:.+}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    ResponseEntity<Resource<Account>> retrieveAccounByEmail(@PathVariable("account_email") String pAccountEmail);

    /**
     * Update an {@link Account} with passed values.
     *
     * @param pAccountId
     *            The <code>id</code> of the {@link Account} to update
     * @param pUpdatedAccount
     *            The new values to set
     * @throws ModuleEntityNotFoundException
     *             Thrown when no {@link Account} could be found with id <code>pAccountId</code>
     * @throws InvalidValueException
     *             Thrown when <code>pAccountId</code> is different from the id of <code>pUpdatedAccount</code><br>
     */
    @ResponseBody
    @RequestMapping(value = "/{account_id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> updateAccount(@PathVariable("account_id") Long pAccountId,
            @Valid @RequestBody Account pUpdatedAccount) throws ModuleEntityNotFoundException, InvalidValueException;

    /**
     * Remove on {@link Account} from db.<br>
     * Only remove if no project user for any tenant.
     *
     * @param pAccountId
     *            The account <code>id</code>
     * @throws ModuleException
     *             - <br>
     *             {@link ModuleEntityNotFoundException} Thrown if the {@link Account} is still linked to project users
     *             and therefore cannot be removed<br>
     *             {@link EntityTransitionForbiddenException} if the accout is not in status allowing removal
     * @throws OperationForbiddenException
     */
    @ResponseBody
    @RequestMapping(value = "/{account_id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> removeAccount(@PathVariable("account_id") Long pAccountId) throws ModuleException;

    /**
     * Do not respect REST architecture because the request comes from a mail client, ideally should be a PUT
     *
     * @param pAccountId
     *            The account id
     * @param pUnlockCode
     *            the unlock code
     * @return
     * @throws EntityException
     *             <br>
     *             {@link EntityNotFoundException} Thrown when no {@link Account} could be found with id
     *             <code>pAccountId</code><br>
     *             {@link EntityTransitionForbiddenException} Thrown if the account is not in status LOCKED
     * @throws InvalidValueException
     * @throws ModuleEntityNotFoundException
     *             Thrown when no {@link Account} could be found with id <code>pAccountId</code>
     * @throws ModuleException
     */
    @RequestMapping(value = "/{account_id}/unlock/{unlock_code}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    ResponseEntity<Void> unlockAccount(@PathVariable("account_id") Long pAccountId,
            @PathVariable("unlock_code") String pUnlockCode) throws InvalidValueException, ModuleException;

    /**
     * Change the passord of an {@link Account}.
     *
     * @param pAccountId
     *            The {@link Account}'s <code>id</code>
     * @param pResetCode
     *            The reset code. Required to allow a password change
     * @param pNewPassword
     *            The new <code>password</code>
     * @throws InvalidValueException
     *             Thrown when the passed reset code is different from the one expected
     * @throws ModuleEntityNotFoundException
     *             Thrown when no {@link Account} could be found with id <code>pAccountId</code>
     */
    @ResponseBody
    @RequestMapping(value = "/{account_id}/password/{reset_code}", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> changeAccountPassword(@PathVariable("account_id") Long pAccountId,
            @PathVariable("reset_code") String pResetCode, @Valid @RequestBody String pNewPassword)
            throws InvalidValueException, ModuleEntityNotFoundException;

    /**
     * Send to the user an email containing a code:<br>
     * - to reset password<br>
     * - to unlock the account
     *
     * @param pEmail
     *            The {@link Account}'s <code>email</code>
     * @param pType
     *            The type of code
     * @throws ModuleEntityNotFoundException
     *             Thrown when no {@link Account} with passed <code>email</code> could be found
     */
    @ResponseBody
    @RequestMapping(value = "/code", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> sendAccountCode(@RequestParam("email") String pEmail, @RequestParam("type") CodeType pType)
            throws ModuleEntityNotFoundException;

    /**
     * Retrieve the {@link AccountSettings} for the instance.
     *
     * @return The {@link AccountSettings} wrapped in a {@link Resource} and a {@link ResponseEntity}
     */
    @ResponseBody
    @RequestMapping(value = "/settings", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Resource<AccountSettings>> retrieveAccountSettings();

    @ResponseBody
    @RequestMapping(value = "/settings", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> updateAccountSetting(@Valid @RequestBody AccountSettings pUpdatedAccountSetting);

    @ResponseBody
    @RequestMapping(value = "/{account_login}/validate", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<AccountStatus> validatePassword(@PathVariable("account_login") String pLogin,
            @RequestParam("password") String pPassword) throws ModuleEntityNotFoundException;
}
