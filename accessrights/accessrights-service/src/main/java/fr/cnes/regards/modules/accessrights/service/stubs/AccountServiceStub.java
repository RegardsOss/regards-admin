/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessrights.service.stubs;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import fr.cnes.regards.modules.accessrights.domain.AccountStatus;
import fr.cnes.regards.modules.accessrights.domain.CodeType;
import fr.cnes.regards.modules.accessrights.domain.instance.Account;
import fr.cnes.regards.modules.accessrights.service.IAccountService;
import fr.cnes.regards.modules.core.exception.AlreadyExistingException;
import fr.cnes.regards.modules.core.exception.EntityNotFoundException;
import fr.cnes.regards.modules.core.exception.InvalidValueException;

/**
 * Stubbed {@link IAccountService} implementation
 *
 * @author CS SI
 */
@Service
@Profile("test")
@Primary
public class AccountServiceStub implements IAccountService {

    /**
     * An email
     */
    private static final String EMAIL_0 = "instance_admin@cnes.fr";

    /**
     * An other email
     */
    private static final String EMAIL_1 = "toto@toto.toto";

    /**
     * A first name
     */
    private static final String FIRSTNAME_0 = "Firstname";

    /**
     * An other first name
     */
    private static final String FIRSTNAME_1 = "Otherfirstname";

    /**
     * A first name
     */
    private static final String LASTNAME_0 = "Lastname";

    /**
     * An other first name
     */
    private static final String LASTNAME_1 = "Otherlastname";

    /**
     * A password
     */
    private static final String MDP_0 = "password";

    /**
     * An other password
     */
    private static final String MDP_1 = "otherpassword";

    /**
     * A code
     */
    private static final String CODE_0 = "code";

    /**
     * An other code
     */
    private static final String CODE_1 = "othercode";

    /**
     * The account setting
     */
    @Value("${regards.instance.account_acceptance}")
    private String accountSetting;

    /**
     * The stub {@link Account}s data base
     */
    private List<Account> accounts = new ArrayList<>();

    /**
     * Create a new stub implementation of {@link IAccountService} for testing purposes.
     */
    @PostConstruct
    public void init() {
        accounts.add(new Account(0L, EMAIL_0, FIRSTNAME_0, LASTNAME_0, EMAIL_0, MDP_0, AccountStatus.ACCEPTED, CODE_0));
        accounts.add(new Account(1L, EMAIL_1, FIRSTNAME_1, LASTNAME_1, EMAIL_1, MDP_1, AccountStatus.PENDING, CODE_1));
    }

    @Override
    public List<Account> retrieveAccountList() {
        return accounts;
    }

    @Override
    public Account createAccount(final Account pNewAccount) throws AlreadyExistingException {
        if (existAccount(pNewAccount)) {
            throw new AlreadyExistingException(pNewAccount.getId() + "");
        }
        accounts.add(pNewAccount);
        return pNewAccount;
    }

    @Override
    public Account retrieveAccount(final Long pAccountId) throws EntityNotFoundException {
        return accounts.stream().filter(a -> a.getId() == pAccountId).findFirst()
                .orElseThrow(() -> new EntityNotFoundException(pAccountId.toString(), Account.class));
    }

    @Override
    public void updateAccount(final Long pAccountId, final Account pUpdatedAccount)
            throws InvalidValueException, EntityNotFoundException {
        if (!existAccount(pAccountId)) {
            throw new EntityNotFoundException(pAccountId.toString(), Account.class);
        }
        if (!pUpdatedAccount.getId().equals(pAccountId)) {
            throw new InvalidValueException("Account id specified differs from updated account id");
        }

        final Function<Account, Account> replaceWithUpdatedIfRightId = a -> {
            Account result = null;
            if (a.getEmail().equals(pAccountId)) {
                result = pUpdatedAccount;
            } else {
                result = a;
            }
            return result;
        };
        accounts.stream().map(replaceWithUpdatedIfRightId).collect(Collectors.toList());

    }

    @Override
    public void removeAccount(final Long pAccountId) {
        accounts = accounts.stream().filter(a -> a.getId() != pAccountId).collect(Collectors.toList());
    }

    @Override
    public void codeForAccount(final String pAccountEmail, final CodeType pType) {
        final String code = generateCode(pType);
        final Account account = this.retrieveAccountByEmail(pAccountEmail);
        account.setCode(code);
        // TODO: sendEmail(pEmail,code);
    }

    private String generateCode(final CodeType pType) {
        return pType.toString() + "-" + UUID.randomUUID().toString();
    }

    @Override
    public void unlockAccount(final Long pAccountId, final String pUnlockCode)
            throws InvalidValueException, EntityNotFoundException {
        final Account toUnlock = this.retrieveAccount(pAccountId);
        check(toUnlock, pUnlockCode);
        toUnlock.unlock();

    }

    @Override
    public void changeAccountPassword(final Long pAccountId, final String pResetCode, final String pNewPassword)
            throws InvalidValueException, EntityNotFoundException {
        final Account account = this.retrieveAccount(pAccountId);
        check(account, pResetCode);
        account.setPassword(pNewPassword);
    }

    @Override
    public List<String> retrieveAccountSettings() {
        final List<String> accountSettings = new ArrayList<>();
        accountSettings.add(accountSetting);
        return accountSettings;
    }

    @Override
    public void updateAccountSetting(final String pUpdatedAccountSetting) throws InvalidValueException {
        if ("manual".equalsIgnoreCase(pUpdatedAccountSetting)
                || "auto-accept".equalsIgnoreCase(pUpdatedAccountSetting)) {
            accountSetting = pUpdatedAccountSetting.toLowerCase();
            return;
        }
        throw new InvalidValueException("Only value accepted : manual or auto-accept");
    }

    @Override
    public boolean existAccount(final Long pId) {
        return accounts.stream().filter(p -> p.getId() == pId).findFirst().isPresent();
    }

    /**
     * Check if the passed {@link Account} exists in db.
     *
     * @param pNewAccount
     *            The {@link Account} to check existence
     * @return <code>True</code> if exists, else <code>False</code>
     */
    private boolean existAccount(final Account pNewAccount) {
        return accounts.stream().filter(p -> p.equals(pNewAccount)).findFirst().isPresent();
    }

    /**
     * @param pLogin
     * @return
     */
    @Override
    public boolean existAccount(final String pLogin) {
        return accounts.stream().filter(p -> p.getLogin().equals(pLogin)).findFirst().isPresent();
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.accessrights.service.IAccountService#retrieveAccount(java.lang.String)
     */
    @Override
    public Account retrieveAccountByEmail(final String pEmail) {
        return accounts.stream().filter(p -> p.getEmail().equals(pEmail)).findFirst().get();
    }

    private void check(final Account pAccount, final String pCode) throws InvalidValueException {
        if (!pAccount.getCode().equals(pCode)) {
            throw new InvalidValueException("this is not the right code");
        }
    }

    @Override
    public boolean validatePassword(final String pLogin, final String pPassword) throws EntityNotFoundException {
        final Account account = retrieveAccountByLogin(pLogin);
        return account.getPassword().equals(pPassword);
    }

    @Override
    public Account retrieveAccountByLogin(final String pLogin) throws EntityNotFoundException {
        return accounts.stream().filter(p -> p.getLogin().equals(pLogin)).findFirst()
                .orElseThrow(() -> new EntityNotFoundException(pLogin, Account.class));
    }
}