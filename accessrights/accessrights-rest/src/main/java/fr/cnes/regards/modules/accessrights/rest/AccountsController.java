/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessrights.rest;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.security.utils.endpoint.annotation.ResourceAccess;
import fr.cnes.regards.modules.accessrights.domain.CodeType;
import fr.cnes.regards.modules.accessrights.domain.instance.Account;
import fr.cnes.regards.modules.accessrights.service.IAccountService;
import fr.cnes.regards.modules.accessrights.signature.IAccountsSignature;
import fr.cnes.regards.modules.core.annotation.ModuleInfo;
import fr.cnes.regards.modules.core.exception.AlreadyExistingException;
import fr.cnes.regards.modules.core.exception.EntityNotFoundException;
import fr.cnes.regards.modules.core.exception.InvalidValueException;
import fr.cnes.regards.modules.core.rest.Controller;

@RestController
@ModuleInfo(name = "users", version = "1.0-SNAPSHOT", author = "REGARDS", legalOwner = "CS",
        documentation = "http://test")
public class AccountsController extends Controller implements IAccountsSignature {

    @Autowired
    private IAccountService accountService;

    @Override
    @ResourceAccess(description = "retrieve the list of account in the instance", name = "")
    public HttpEntity<List<Resource<Account>>> retrieveAccountList() {
        final List<Account> accounts = accountService.retrieveAccountList();
        final List<Resource<Account>> resources = accounts.stream().map(a -> new Resource<>(a))
                .collect(Collectors.toList());
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    @Override
    @ResourceAccess(description = "create an new account", name = "")
    public HttpEntity<Resource<Account>> createAccount(@Valid @RequestBody final Account pNewAccount)
            throws AlreadyExistingException {
        final Account created = accountService.createAccount(pNewAccount);
        final Resource<Account> resource = new Resource<>(created);
        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    @Override
    @ResourceAccess(description = "retrieve the account account_id", name = "")
    public HttpEntity<Resource<Account>> retrieveAccount(@PathVariable("account_id") final Long accountId)
            throws EntityNotFoundException {
        final Account account = accountService.retrieveAccount(accountId);
        final Resource<Account> resource = new Resource<>(account);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    @Override
    @ResourceAccess(description = "update the account account_id according to the body specified", name = "")
    public HttpEntity<Void> updateAccount(@PathVariable("account_id") final Long accountId,
            @Valid @RequestBody final Account pUpdatedAccount) throws EntityNotFoundException, InvalidValueException {
        accountService.updateAccount(accountId, pUpdatedAccount);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    @ResourceAccess(description = "remove the account account_id", name = "")
    public HttpEntity<Void> removeAccount(@PathVariable("account_id") final Long accountId) {
        accountService.removeAccount(accountId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    @ResourceAccess(description = "unlock the account account_id according to the code unlock_code", name = "")
    public HttpEntity<Void> unlockAccount(@PathVariable("account_id") final Long accountId,
            @PathVariable("unlock_code") final String unlockCode)
            throws InvalidValueException, EntityNotFoundException {
        accountService.unlockAccount(accountId, unlockCode);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    @ResourceAccess(description = "change the passsword of account account_id according to the code reset_code",
            name = "")
    public HttpEntity<Void> changeAccountPassword(@PathVariable("account_id") final Long accountId,
            @PathVariable("reset_code") final String resetCode, @Valid @RequestBody final String pNewPassword)
            throws EntityNotFoundException, InvalidValueException {
        accountService.changeAccountPassword(accountId, resetCode, pNewPassword);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    @ResourceAccess(description = "send a code of type type to the email specified", name = "")
    public HttpEntity<Void> codeForAccount(@RequestParam("email") final String email,
            @RequestParam("type") final CodeType type) {
        accountService.codeForAccount(email, type);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    @ResourceAccess(description = "retrieve the list of setting managing the accounts", name = "")
    public HttpEntity<List<Resource<String>>> retrieveAccountSettings() {
        final List<String> accountSettings = accountService.retrieveAccountSettings();
        final List<Resource<String>> resources = accountSettings.stream().map(a -> new Resource<>(a))
                .collect(Collectors.toList());
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    @Override
    @ResourceAccess(description = "update the setting managing the account", name = "")
    public HttpEntity<Void> updateAccountSetting(@Valid @RequestBody final String pUpdatedAccountSetting)
            throws InvalidValueException {
        accountService.updateAccountSetting(pUpdatedAccountSetting);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    @ResourceAccess(description = "Validate the account password", name = "")
    public HttpEntity<Boolean> validatePassword(@PathVariable("account_login") final String pLogin,
            @RequestParam("password") final String pPassword) throws EntityNotFoundException {
        final Boolean valid = accountService.validatePassword(pLogin, pPassword);
        return new ResponseEntity<>(valid, HttpStatus.OK);
    }
}