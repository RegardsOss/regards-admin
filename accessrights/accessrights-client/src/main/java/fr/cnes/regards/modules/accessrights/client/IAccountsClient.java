/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessrights.client;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import fr.cnes.regards.framework.feign.annotation.RestClient;
import fr.cnes.regards.modules.accessrights.domain.CodeType;
import fr.cnes.regards.modules.accessrights.domain.instance.Account;
import fr.cnes.regards.modules.accessrights.domain.passwordreset.PerformResetPasswordDto;
import fr.cnes.regards.modules.accessrights.domain.passwordreset.RequestResetPasswordDto;

/**
 * Feign client for rs-admin Accounts controller.
 *
 * @author Sébastien Binda
 * @author Xavier-Alexandre Brochard
 * @since 1.0-SNAPSHOT
 */
@RestClient(name = "rs-admin")
@RequestMapping(path = "/accounts", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public interface IAccountsClient {

    /**
     * Retrieve the list of all {@link Account}s.
     *
     * @return The accounts list
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    ResponseEntity<PagedResources<Resource<Account>>> retrieveAccountList(@RequestParam("page") int pPage,
            @RequestParam("size") int pSize);

    /**
     * Create a new account in state PENDING from the passed values
     *
     * @param pNewAccount
     *            The data transfer object containing values to create the account from
     * @return the created account
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<Resource<Account>> createAccount(@Valid @RequestBody Account pNewAccount);

    /**
     * Retrieve the {@link Account} of passed <code>id</code>.
     *
     * @param pAccountId
     *            The {@link Account}'s <code>id</code>
     * @return The account
     */
    @ResponseBody
    @RequestMapping(value = "/{account_id}", method = RequestMethod.GET)
    ResponseEntity<Resource<Account>> retrieveAccount(@PathVariable("account_id") Long pAccountId);

    /**
     *
     * Retrieve an account by his unique email
     *
     * @param pAccountEmail
     *            email of the account to retrieve
     * @return Account
     */
    @ResponseBody
    @RequestMapping(value = "/account/{account_email}", method = RequestMethod.GET)
    ResponseEntity<Resource<Account>> retrieveAccounByEmail(@PathVariable("account_email") String pAccountEmail);

    /**
     * Update an {@link Account} with passed values.
     *
     * @param pAccountId
     *            The <code>id</code> of the {@link Account} to update
     * @param pUpdatedAccount
     *            The new values to set
     */
    @ResponseBody
    @RequestMapping(value = "/{account_id}", method = RequestMethod.PUT)
    ResponseEntity<Resource<Account>> updateAccount(@PathVariable("account_id") Long pAccountId,
            @Valid @RequestBody Account pUpdatedAccount);

    /**
     * Remove on {@link Account} from db.<br>
     * Only remove if no project user for any tenant.
     *
     * @param pAccountId
     *            The account <code>id</code>
     */
    @ResponseBody
    @RequestMapping(value = "/{account_id}", method = RequestMethod.DELETE)
    ResponseEntity<Void> removeAccount(@PathVariable("account_id") Long pAccountId);

    /**
     * Do not respect REST architecture because the request comes from a mail client, ideally should be a PUT
     *
     * @param pAccountId
     *            The account id
     * @param pUnlockCode
     *            the unlock code
     * @return void
     */
    @ResponseBody
    @RequestMapping(value = "/{account_id}/unlock/{unlock_code}", method = RequestMethod.GET)
    ResponseEntity<Void> unlockAccount(@PathVariable("account_id") Long pAccountId,
            @PathVariable("unlock_code") String pUnlockCode);

    /**
     * Send to the user an email containing a link with limited validity to reset its password.
     *
     * @param pEmail
     *            The {@link Account}'s <code>email</code>
     * @param pDto
     *            The DTO containing<br>
     *            - The url of the app from where was issued the query<br>
     *            - The url to redirect the user to the password reset interface
     * @return void
     * @throws EntityNotFoundException
     */
    @ResponseBody
    @RequestMapping(value = "/{account_email}/resetPassword", method = RequestMethod.POST)
    public ResponseEntity<Void> requestResetPassword(@PathVariable("account_email") final String pAccountEmail,
            @Valid @RequestBody final RequestResetPasswordDto pDto);

    /**
     * Change the passord of an {@link Account}.
     *
     * @param pEmail
     *            The {@link Account}'s <code>email</code>
     * @param pDto
     *            The DTO containing : 1) the token 2) the new password
     * @return void
     * @throws EntityException
     */
    @ResponseBody
    @RequestMapping(value = "/{account_email}/resetPassword", method = RequestMethod.PUT)
    ResponseEntity<Void> performResetPassword(@PathVariable("account_email") final String pAccountEmail,
            @Valid @RequestBody final PerformResetPasswordDto pDto);

    /**
     * Send to the user an email containing a code:<br>
     * - to reset password<br>
     * - to unlock the account
     *
     * @param pEmail
     *            The {@link Account}'s <code>email</code>
     * @param pType
     *            The type of code
     */
    @ResponseBody
    @RequestMapping(value = "/code", method = RequestMethod.GET)
    ResponseEntity<Void> sendAccountCode(@RequestParam("email") String pEmail, @RequestParam("type") CodeType pType);

    /**
     * Return <code>true</code> if the passed <code>pPassword</code> is equal to the one set on the {@link Account} of
     * passed <code>email</code>
     *
     * @param pEmail
     *            The {@link Account}'s <code>email</code>
     * @param pPassword
     *            The password to check
     * @return <code>true</code> if the password is valid, else <code>false</code>
     */
    @ResponseBody
    @RequestMapping(value = "/{account_email}/validate", method = RequestMethod.GET)
    ResponseEntity<Boolean> validatePassword(@PathVariable("account_email") String pEmail,
            @RequestParam("password") String pPassword);
}
