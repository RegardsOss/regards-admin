/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessrights.registration;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.modules.accessrights.domain.instance.Account;
import fr.cnes.regards.modules.accessrights.domain.registration.VerificationToken;

/**
 * Interface defining the service managing the registration tokens
 *
 * @author Xavier-Alexandre Brochard
 */
public interface IVerificationTokenService {

    /**
     * Create a verification token with passed attributes
     *
     * @param pVerificationToken
     *            the token
     * @return the account
     * @throws EntityNotFoundException
     *             if the token could not be found
     */
    void create(final Account pAccount, final String pOriginUrl, final String pRequestLink);

    /**
     * Retrieve the verification token by token
     *
     * @param pVerificationToken
     *            the token
     * @return the token
     * @throws EntityNotFoundException
     *             if the token could not be foud
     */
    VerificationToken findByToken(final String pVerificationToken) throws EntityNotFoundException;

    /**
     * Retrieve the verification token by account
     *
     * @param pAccout
     *            the account
     * @return the token
     * @throws EntityNotFoundException
     *             if the token could not be foud
     */
    VerificationToken findByAccount(final Account pAccount) throws EntityNotFoundException;

    /**
     * Return the account linked to the passed verification token
     *
     * @param pVerificationToken
     *            the token
     * @return the account
     * @throws EntityNotFoundException
     *             if the token could not be found
     */
    Account getAccountByVerificationToken(String pVerificationToken) throws EntityNotFoundException;

}