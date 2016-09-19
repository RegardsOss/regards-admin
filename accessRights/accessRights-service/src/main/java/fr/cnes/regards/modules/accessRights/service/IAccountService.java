/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessRights.service;

import java.util.List;

import javax.naming.OperationNotSupportedException;

import fr.cnes.regards.modules.accessRights.domain.Account;
import fr.cnes.regards.modules.accessRights.domain.CodeType;
import fr.cnes.regards.modules.core.exception.AlreadyExistingException;
import fr.cnes.regards.modules.core.exception.InvalidValueException;

public interface IAccountService {

    List<Account> retrieveAccountList();

    Account createAccount(Account pNewAccount) throws AlreadyExistingException;

    List<String> retrieveAccountSettings();

    void updateAccountSetting(String pUpdatedAccountSetting) throws InvalidValueException;

    /**
     * @param id
     * @return
     */
    boolean existAccount(Long id);

    /**
     * @param pAccountId
     * @return
     */
    Account retrieveAccount(Long pAccountId);

    /**
     * @param pAccountId
     * @param pUpdatedAccount
     * @throws OperationNotSupportedException
     */
    void updateAccount(Long pAccountId, Account pUpdatedAccount) throws OperationNotSupportedException;

    /**
     * @param pAccountId
     */
    void removeAccount(Long pAccountId);

    /**
     * @param pAccountEmail
     * @param pType
     */
    void codeForAccount(String pAccountEmail, CodeType pType);

    /**
     * @param pAccountId
     * @param pUnlockCode
     */
    void unlockAccount(Long pAccountId, String pUnlockCode);

    /**
     * @param pAccountId
     * @param pResetCode
     * @param pNewPassword
     */
    void changeAccountPassword(Long pAccountId, String pResetCode, String pNewPassword);

    /**
     * @param pString
     * @return
     */
    Account retrieveAccount(String pString);

}
