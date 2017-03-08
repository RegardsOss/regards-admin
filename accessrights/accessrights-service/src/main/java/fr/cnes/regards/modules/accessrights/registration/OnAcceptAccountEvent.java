/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessrights.registration;

import org.springframework.context.ApplicationEvent;

import fr.cnes.regards.modules.accessrights.domain.instance.Account;

/**
 * Event transporting the logic needed for account email validation.
 *
 * @author Xavier-Alexandre Brochard
 */
public class OnAcceptAccountEvent extends ApplicationEvent {

    /**
     * Generated serial
     */
    private static final long serialVersionUID = -7099682370525387294L;

    /**
     * The registered account
     */
    private Account account;

    private String originUrl;

    private String requestLink;

    /**
     * @param pAccount
     * @param pOriginUrl
     * @param pRequestLink
     */
    public OnAcceptAccountEvent(final Account pAccount, final String pOriginUrl, final String pRequestLink) {
        super(pAccount);
        account = pAccount;
        originUrl = pOriginUrl;
        requestLink = pRequestLink;
    }

    /**
     * @return the account
     */
    public Account getAccount() {
        return account;
    }

    /**
     * @param pAccount
     *            the account to set
     */
    public void setAccount(final Account pAccount) {
        account = pAccount;
    }

    /**
     * @return the originUrl
     */
    public String getOriginUrl() {
        return originUrl;
    }

    /**
     * @param pOriginUrl
     *            the originUrl to set
     */
    public void setOriginUrl(final String pOriginUrl) {
        originUrl = pOriginUrl;
    }

    /**
     * @return the requestLink
     */
    public String getRequestLink() {
        return requestLink;
    }

    /**
     * @param pRequestLink
     *            the requestLink to set
     */
    public void setRequestLink(final String pRequestLink) {
        requestLink = pRequestLink;
    }

}