/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessRights.service.stubs;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.naming.OperationNotSupportedException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import fr.cnes.regards.modules.accessRights.domain.Account;
import fr.cnes.regards.modules.accessRights.domain.CodeType;
import fr.cnes.regards.modules.accessRights.service.IAccountService;
import fr.cnes.regards.modules.core.exception.AlreadyExistingException;
import fr.cnes.regards.modules.core.exception.InvalidValueException;

@Service
@Profile("test")
@Primary
public class AccountServiceStub implements IAccountService {

    private static List<Account> accounts = new ArrayList<>();

    @Value("${regards.instance.account_acceptance}")
    private String accountSetting;

    @Override
    public List<Account> retrieveAccountList() {
        return accounts;
    }

    @Override
    public Account createAccount(String pEmail) {
        return new Account(pEmail);
    }

    @Override
    public Account createAccount(Account pNewAccount) throws AlreadyExistingException {
        if (existAccount(pNewAccount)) {
            throw new AlreadyExistingException(pNewAccount.getId() + "");
        }
        accounts.add(pNewAccount);
        return pNewAccount;
    }

    @Override
    public Account retrieveAccount(Long pAccountId) {
        return accounts.stream().filter(a -> a.getId() == pAccountId).findFirst().get();
    }

    @Override
    public void updateAccount(Long pAccountId, Account pUpdatedAccount) throws OperationNotSupportedException {
        if (existAccount(pAccountId)) {
            if (pUpdatedAccount.getId() == pAccountId) {
                accounts = accounts.stream().map(a -> a.getEmail().equals(pAccountId) ? pUpdatedAccount : a)
                        .collect(Collectors.toList());
                return;
            }
            throw new OperationNotSupportedException("Account id specified differs from updated account id");
        }
        throw new NoSuchElementException(pAccountId + "");
    }

    @Override
    public void removeAccount(Long pAccountId) {
        accounts = accounts.stream().filter(a -> a.getId() != pAccountId).collect(Collectors.toList());
    }

    @Override
    public void codeForAccount(String pAccountEmail, CodeType pType) {
        String code = generateCode(pType);
        Account account = this.retrieveAccount(pAccountEmail);
        account.setCode(code);
        // TODO: sendEmail(pEmail,code);
    }

    private String generateCode(CodeType pType) {
        return UUID.randomUUID().toString();
    }

    @Override
    public void unlockAccount(Long pAccountId, String pUnlockCode) throws InvalidValueException {
        Account toUnlock = this.retrieveAccount(pAccountId);
        check(toUnlock, pUnlockCode);
        toUnlock.unlock();

    }

    @Override
    public void changeAccountPassword(Long pAccountId, String pResetCode, String pNewPassword)
            throws InvalidValueException {
        Account account = this.retrieveAccount(pAccountId);
        check(account, pResetCode);
        account.setPassword(pNewPassword);
    }

    @Override
    public List<String> retrieveAccountSettings() {
        List<String> accountSettings = new ArrayList<>();
        accountSettings.add(this.accountSetting);
        return accountSettings;
    }

    @Override
    public void updateAccountSetting(String pUpdatedAccountSetting) throws InvalidValueException {
        if (pUpdatedAccountSetting.toLowerCase().equals("manual") || pUpdatedAccountSetting.equals("auto-accept")) {
            this.accountSetting = pUpdatedAccountSetting.toLowerCase();
            return;
        }
        throw new InvalidValueException("Only value accepted : manual or auto-accept");
    }

    @Override
    public boolean existAccount(Long id) {
        return accounts.stream().filter(p -> p.getId() == id).findFirst().isPresent();
    }

    /**
     * @param pNewAccount
     * @return
     */
    private boolean existAccount(Account pNewAccount) {
        return accounts.stream().filter(p -> p.equals(pNewAccount)).findFirst().isPresent();
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.accessRights.service.IAccountService#retrieveAccount(java.lang.String)
     */
    @Override
    public Account retrieveAccount(String pEmail) {
        return accounts.stream().filter(p -> p.getEmail().equals(pEmail)).findFirst().get();
    }

    private void check(Account account, String pCode) throws InvalidValueException {
        if (!account.getCode().equals(pCode)) {
            throw new InvalidValueException("this is not the right code");
        }
    }
}