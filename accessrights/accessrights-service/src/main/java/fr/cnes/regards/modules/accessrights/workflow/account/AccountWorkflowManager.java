/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessrights.workflow.account;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.module.rest.exception.EntityException;
import fr.cnes.regards.framework.module.rest.exception.EntityOperationForbiddenException;
import fr.cnes.regards.framework.module.rest.exception.EntityTransitionForbiddenException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.accessrights.domain.instance.Account;
import fr.cnes.regards.modules.accessrights.domain.registration.VerificationToken;

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
     * Constructor
     *
     * @param pAccountStateProvider
     *            the state factory
     */
    public AccountWorkflowManager(final AccountStateProvider pAccountStateProvider) {
        super();
        accountStateProvider = pAccountStateProvider;
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
    public void acceptAccount(final Account pAccount) throws EntityException {
        accountStateProvider.getState(pAccount).acceptAccount(pAccount);
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
     * fr.cnes.regards.modules.accessrights.workflow.account.IAccountTransitions#requestUnlockAccount(fr.cnes.regards.
     * modules.accessrights.accountunlock.OnAccountUnlockEvent)
     */
    @Override
    public void requestUnlockAccount(final Account pAccount, final String pOriginUrl, final String pRequestLink)
            throws EntityOperationForbiddenException {
        accountStateProvider.getState(pAccount).requestUnlockAccount(pAccount, pOriginUrl, pRequestLink);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.cnes.regards.modules.accessrights.workflow.account.IAccountTransitions#performUnlockAccount(fr.cnes.regards.
     * modules.accessrights.domain.instance.Account, java.lang.String)
     */
    @Override
    public void performUnlockAccount(final Account pAccount, final String pToken) throws EntityException {
        accountStateProvider.getState(pAccount).performUnlockAccount(pAccount, pToken);
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
