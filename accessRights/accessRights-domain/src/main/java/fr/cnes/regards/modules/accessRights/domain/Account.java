/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessRights.domain;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Email;
import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Account extends ResourceSupport {

    private static int maxAccountId_ = 0;

    private int accountId_;

    public void setAccountId(int pAccountId) {
        accountId_ = pAccountId;
    }

    @NotNull
    @Email
    private String email_;

    private String firstName_;

    private String lastName_;

    @NotNull
    private String login_;

    // TODO: validation du mot de passe
    private String password_;

    private AccountStatus status_;

    @JsonIgnore
    private String code_;

    public Account() {
        super();
        accountId_ = maxAccountId_;
        maxAccountId_++;
        status_ = AccountStatus.PENDING;
    }

    public Account(String email) {
        this();
        email_ = email;
    }

    public Account(String email, String firstName, String lastName, String password) {
        this(email);
        firstName_ = firstName;
        lastName_ = lastName;
        login_ = email;
        password_ = password;
    }

    public Account(String email, String firstName, String lastName, String login, String password) {
        this(email);
        firstName_ = firstName;
        lastName_ = lastName;
        login_ = login;
        password_ = password;
    }

    public String getEmail() {
        return email_;
    }

    public void setEmail(String pEmail) {
        email_ = pEmail;
    }

    public String getFirstName() {
        return firstName_;
    }

    public void setFirstName(String pFirstName) {
        firstName_ = pFirstName;
    }

    public String getLastName() {
        return lastName_;
    }

    public void setLastName(String pLastName) {
        lastName_ = pLastName;
    }

    public String getLogin() {
        return login_;
    }

    public void setLogin(String pLogin) {
        login_ = pLogin;
    }

    public String getPassword() {
        return password_;
    }

    public void setPassword(String pPassword) {
        password_ = pPassword;
    }

    public AccountStatus getStatus() {
        return status_;
    }

    public void setStatus(AccountStatus pStatus) {
        status_ = pStatus;
    }

    public void unlock() {
        if (status_.equals(AccountStatus.LOCKED)) {
            status_ = AccountStatus.ACTIVE;
        }
    }

    public int getAccountId() {
        return accountId_;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Account) && ((Account) o).email_.equals(email_);

    }

    public String getCode() {
        return code_;
    }

    public void setCode(String code) {
        code_ = code;
    }

}
