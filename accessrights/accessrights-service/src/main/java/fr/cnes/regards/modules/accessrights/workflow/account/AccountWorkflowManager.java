/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessrights.workflow.account;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import fr.cnes.regards.framework.module.rest.exception.EntityAlreadyExistsException;
import fr.cnes.regards.framework.module.rest.exception.EntityException;
import fr.cnes.regards.framework.module.rest.exception.EntityOperationForbiddenException;
import fr.cnes.regards.framework.module.rest.exception.EntityTransitionForbiddenException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.accessrights.dao.instance.IAccountRepository;
import fr.cnes.regards.modules.accessrights.domain.AccountStatus;
import fr.cnes.regards.modules.accessrights.domain.instance.Account;
import fr.cnes.regards.modules.accessrights.domain.instance.AccountSettings;
import fr.cnes.regards.modules.accessrights.domain.registration.AccessRequestDto;
import fr.cnes.regards.modules.accessrights.domain.registration.VerificationToken;
import fr.cnes.regards.modules.accessrights.service.account.IAccountSettingsService;

/**
 * Class managing the workflow of an account by applying the right transitions according to its status.<br>
 * Proxies the transition methods by instanciating the right state class (ActiveState, LockedState...).
 *
 * @author Xavier-Alexandre Brochard
 * @since 1.1-SNAPSHOT
 */
@Service
@Primary
public class AccountWorkflowManager implements IAccountTransitions {

    /**
     * Class providing the right state (i.e. implementation of the {@link IAccountTransitions}) according to the account
     * status
     */
    private final AccountStateProvider accountStateProvider;

    /**
     * CRUD repository handling {@link Account}s. Autowired by Spring.
     */
    private final IAccountRepository accountRepository;

    /**
     * CRUD repository handling {@link AccountSettingst}s. Autowired by Spring.
     */
    private final IAccountSettingsService accountSettingsService;

    /**
     * Constructor
     *
     * @param pAccountStateProvider
     *            the state factory
     * @param pAccountRepository
     *            the account repository
     * @param pAccountSettingsService
     *            the account settings service
     */
    public AccountWorkflowManager(final AccountStateProvider pAccountStateProvider,
            final IAccountRepository pAccountRepository, final IAccountSettingsService pAccountSettingsService) {
        super();
        accountStateProvider = pAccountStateProvider;
        accountRepository = pAccountRepository;
        accountSettingsService = pAccountSettingsService;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.cnes.regards.modules.accessrights.service.account.IAccountTransitions#requestAccount(fr.cnes.regards.modules.
     * accessrights.domain.AccessRequestDTO)
     */
    @Override
    public Account requestAccount(final AccessRequestDto pDto, final String pValidationUrl) throws EntityException {
        // Check existence
        if (accountRepository.findOneByEmail(pDto.getEmail()).isPresent()) {
            throw new EntityAlreadyExistsException("The email " + pDto.getEmail() + "is already in use.");
        }
        // Create the new account
        final Account account = new Account(pDto.getEmail(), pDto.getFirstName(), pDto.getLastName(),
                pDto.getPassword());
        // Check status
        Assert.isTrue(AccountStatus.PENDING.equals(account.getStatus()),
                      "Trying to create an Account with other status than PENDING.");

        // Auto-accept if configured so
        final AccountSettings settings = accountSettingsService.retrieve();
        if ("auto-accept".equals(settings.getMode())) {
            acceptAccount(account, pValidationUrl);
        }

        // Save
        accountRepository.save(account);

        return account;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.cnes.regards.modules.accessrights.service.account.IAccountTransitions#emailValidation(fr.cnes.regards.modules.
     * accessrights.domain.instance.Account)
     */
    @Override
    public void validateAccount(final VerificationToken pVerificationToken) throws EntityOperationForbiddenException {
        accountStateProvider.getState(pVerificationToken.getAccount()).validateAccount(pVerificationToken);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.cnes.regards.modules.accessrights.workflow.account.IAccountTransitions#acceptAccount(fr.cnes.regards.modules.
     * accessrights.domain.instance.Account)
     */
    @Override
    public void acceptAccount(final Account pAccount, final String pValidationUrl) throws EntityException {
        accountStateProvider.getState(pAccount).acceptAccount(pAccount, pValidationUrl);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.cnes.regards.modules.accessrights.service.account.IAccountTransitions#lockAccount(fr.cnes.regards.modules.
     * accessrights.domain.instance.Account)
     */
    @Override
    public void lockAccount(final Account pAccount) throws EntityTransitionForbiddenException {
        accountStateProvider.getState(pAccount).lockAccount(pAccount);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.cnes.regards.modules.accessrights.service.account.IAccountTransitions#unlockAccount(fr.cnes.regards.modules.
     * accessrights.domain.instance.Account)
     */
    @Override
    public void unlockAccount(final Account pAccount, final String pUnlockCode)
            throws EntityOperationForbiddenException {
        accountStateProvider.getState(pAccount).unlockAccount(pAccount, pUnlockCode);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.cnes.regards.modules.accessrights.service.account.IAccountTransitions#inactiveAccount(fr.cnes.regards.modules.
     * accessrights.domain.instance.Account)
     */
    @Override
    public void inactiveAccount(final Account pAccount) throws EntityTransitionForbiddenException {
        accountStateProvider.getState(pAccount).inactiveAccount(pAccount);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.cnes.regards.modules.accessrights.service.account.IAccountTransitions#activeAccount(fr.cnes.regards.modules.
     * accessrights.domain.instance.Account)
     */
    @Override
    public void activeAccount(final Account pAccount) throws EntityTransitionForbiddenException {
        accountStateProvider.getState(pAccount).activeAccount(pAccount);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.accessrights.service.account.IAccountTransitions#delete(fr.cnes.regards.modules.
     * accessrights. domain.instance.Account)
     */
    @Override
    public void deleteAccount(final Account pAccount) throws ModuleException {
        accountStateProvider.getState(pAccount).deleteAccount(pAccount);
    }

}