/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessrights.rest;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.module.annotation.ModuleInfo;
import fr.cnes.regards.framework.module.rest.exception.AlreadyExistingException;
import fr.cnes.regards.framework.module.rest.exception.EntityException;
import fr.cnes.regards.framework.module.rest.exception.InvalidValueException;
import fr.cnes.regards.framework.module.rest.exception.ModuleEntityNotFoundException;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.accessrights.domain.CodeType;
import fr.cnes.regards.modules.accessrights.domain.instance.Account;
import fr.cnes.regards.modules.accessrights.domain.instance.AccountSettings;
import fr.cnes.regards.modules.accessrights.service.account.IAccountService;
import fr.cnes.regards.modules.accessrights.service.account.IAccountSettingsService;
import fr.cnes.regards.modules.accessrights.signature.IAccountsSignature;

@RestController
@ModuleInfo(name = "users", version = "1.0-SNAPSHOT", author = "REGARDS", legalOwner = "CS",
        documentation = "http://test")
public class AccountsController implements IAccountsSignature {

    @Autowired
    private IAccountService accountService;

    @Autowired
    private IAccountSettingsService accountSettingsService;

    @Override
    @ResourceAccess(description = "retrieve the list of account in the instance", name = "")
    public ResponseEntity<List<Resource<Account>>> retrieveAccountList() {
        final List<Account> accounts = accountService.retrieveAccountList();
        final List<Resource<Account>> resources = accounts.stream().map(a -> new Resource<>(a))
                .collect(Collectors.toList());
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    @Override
    @ResourceAccess(description = "create an new account", name = "")
    public ResponseEntity<Resource<Account>> createAccount(@Valid @RequestBody final Account pNewAccount)
            throws AlreadyExistingException {
        final Account created = accountService.createAccount(pNewAccount);
        final Resource<Account> resource = new Resource<>(created);
        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    @Override
    @ResourceAccess(description = "retrieve the account account_id", name = "")
    public ResponseEntity<Resource<Account>> retrieveAccount(@PathVariable("account_id") final Long accountId)
            throws ModuleEntityNotFoundException {
        final Account account = accountService.retrieveAccount(accountId);
        final Resource<Account> resource = new Resource<>(account);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    @Override
    @ResourceAccess(description = "update the account account_id according to the body specified", name = "")
    public ResponseEntity<Void> updateAccount(@PathVariable("account_id") final Long accountId,
            @Valid @RequestBody final Account pUpdatedAccount)
            throws ModuleEntityNotFoundException, InvalidValueException {
        accountService.updateAccount(accountId, pUpdatedAccount);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    @ResourceAccess(description = "remove the account account_id", name = "")
    public ResponseEntity<Void> removeAccount(@PathVariable("account_id") final Long pAccountId)
            throws EntityException {
        final Account account = accountService.retrieveAccount(pAccountId);
        accountService.delete(account);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    @ResourceAccess(description = "unlock the account account_id according to the code unlock_code", name = "")
    public ResponseEntity<Void> unlockAccount(@PathVariable("account_id") final Long accountId,
            @PathVariable("unlock_code") final String unlockCode)
            throws InvalidValueException, ModuleEntityNotFoundException {
        accountService.unlockAccount(accountId, unlockCode);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    @ResourceAccess(description = "change the passsword of account account_id according to the code reset_code",
            name = "")
    public ResponseEntity<Void> changeAccountPassword(@PathVariable("account_id") final Long accountId,
            @PathVariable("reset_code") final String resetCode, @Valid @RequestBody final String pNewPassword)
            throws ModuleEntityNotFoundException, InvalidValueException {
        accountService.changeAccountPassword(accountId, resetCode, pNewPassword);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    @ResourceAccess(description = "send a code of type type to the email specified", name = "")
    public ResponseEntity<Void> sendAccountCode(@RequestParam("email") final String email,
            @RequestParam("type") final CodeType type) throws ModuleEntityNotFoundException {
        accountService.sendAccountCode(email, type);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    @ResourceAccess(description = "retrieve the list of setting managing the accounts", name = "")
    public ResponseEntity<Resource<AccountSettings>> retrieveAccountSettings() {
        final AccountSettings settings = accountSettingsService.retrieve();
        return new ResponseEntity<>(new Resource<>(settings), HttpStatus.OK);
    }

    @Override
    @ResourceAccess(description = "update the setting managing the account", name = "")
    public ResponseEntity<Void> updateAccountSetting(@Valid @RequestBody final AccountSettings pUpdatedAccountSetting) {
        accountSettingsService.update(pUpdatedAccountSetting);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    @ResourceAccess(description = "Validate the account password", name = "")
    public ResponseEntity<Void> validatePassword(@PathVariable("account_login") final String pLogin,
            @RequestParam("password") final String pPassword) throws ModuleEntityNotFoundException {
        if (accountService.validatePassword(pLogin, pPassword)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
}
