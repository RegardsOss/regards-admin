/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessrights.service.account;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.framework.module.rest.exception.EntityException;
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.modules.accessrights.domain.AccountStatus;
import fr.cnes.regards.modules.accessrights.domain.instance.Account;

/**
 * Define the base interface for any implementation of an Account Service.
 *
 * @author CS SI
 */
public interface IAccountService {

    /**
     * Create an account.
     *
     * @param pAccount
     *            The {@link Account}
     * @return The account
     */
    Account createAccount(Account pAccount);

    /**
     * Retrieve the list of all {@link Account}s.
     *
     * @param pPageable
     *            pagination informations
     * @return The accounts list
     */
    Page<Account> retrieveAccountList(final Pageable pPageable);

    /**
     * Retrieve the list of all {@link Account}s with given status.
     *
     * @param pPageable
     *            pagination informations
     * @param pStatus
     *            status required for retrieved accounts
     * @return The accounts list
     */
    Page<Account> retrieveAccountList(AccountStatus pStatus, final Pageable pPageable);

    /**
     * Retrieve the {@link Account} of passed <code>id</code>.
     *
     * @param pAccountId
     *            The {@link Account}'s <code>id</code>
     * @throws EntityNotFoundException
     *             Thrown if no {@link Account} with passed <code>id</code> could be found
     * @return The account
     */
    Account retrieveAccount(Long pAccountId) throws EntityNotFoundException;

    /**
     * Retrieve the {@link Account} of passed <code>email</code>
     *
     * @param pEmail
     *            The {@link Account}'s <code>email</code>
     * @return the account
     * @throws EntityNotFoundException
     *             Thrown if no {@link Account} with passed <code>email</code> could be found
     */
    Account retrieveAccountByEmail(String pEmail) throws EntityNotFoundException;

    /**
     * Return <code>true</code> if an {@link Account} of passed <code>id</code> exists.
     *
     * @param pId
     *            The {@link Account}'s <code>id</code>
     * @return <code>true</code> if exists, else <code>false</code>
     */
    boolean existAccount(Long pId);

    /**
     * Return <code>true</code> if an {@link Account} of passed <code>email</code> exists.
     *
     * @param pEmail
     *            The {@link Account}'s <code>email</code>
     * @return <code>true</code> if exists, else <code>false</code>
     */
    boolean existAccount(String pEmail);

    /**
     * Update an {@link Account} with passed values. Passwords and emails are not updated by this method.
     *
     * @param pAccountId
     *            The <code>id</code> of the {@link Account} to update
     * @param pUpdatedAccount
     *            The new values to set
     * @return the {@link Account} created
     * @throws EntityException
     *             <br>
     *             {@link EntityInconsistentIdentifierException} Thrown when <code>pAccountId</code> is different from
     *             the id of <code>pUpdatedAccount</code><br>
     *             {@link EntityNotFoundException} Thrown when no {@link Account} could be found with id
     *             <code>pAccountId</code><br>
     */
    Account updateAccount(Long pAccountId, Account pUpdatedAccount) throws EntityException;

    /**
     * Return <code>true</code> if the passed <code>pPassword</code> is equal to the one set on the {@link Account} of
     * passed <code>email</code>
     *
     * @param pEmail
     *            The {@link Account}'s <code>email</code>
     * @param pPassword
     *            The password to check
     * @throws EntityNotFoundException
     *             Thrown when no {@link Account} could be found with id <code>pAccountId</code>
     * @return <code>true</code> if the password is valid, else <code>false</code>
     * @throws EntityException
     */
    boolean validatePassword(String pEmail, String pPassword);

    /**
     * Validate the password according to the regex provided by file. Mainly used by create and update methods so an
     * invalid password wouldn't be used
     *
     * @throws EntityInvalidException
     *             thrown if the provided password does not respect the configured regex
     */
    void checkPassword(Account pNewAccount) throws EntityInvalidException;

    /**
     * @return whether the password respect the regex
     */
    boolean validPassword(String pPassword);

    String getPasswordRules();

    String encryptPassword(String pPassword);

    void checkAccountValidity();

    void changePassword(Long pId, String pEncryptPassword) throws EntityNotFoundException;

}
