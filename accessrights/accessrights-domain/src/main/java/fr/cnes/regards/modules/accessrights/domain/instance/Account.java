/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessrights.domain.instance;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import fr.cnes.regards.framework.gson.annotation.GsonIgnore;
import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.framework.jpa.annotation.InstanceEntity;
import fr.cnes.regards.modules.accessrights.domain.AccountStatus;

@InstanceEntity
@Entity(name = "T_ACCOUNT")
@SequenceGenerator(name = "accountSequence", initialValue = 1, sequenceName = "SEQ_ACCOUNT")
public class Account implements IIdentifiable<Long> {

    @Transient
    private static final int RANDOM_STRING_LENGTH = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accountSequence")
    @Column(name = "id")
    private Long id;

    @Email
    @Column(name = "email", unique = true)
    private String email;

    @NotBlank
    @Column(name = "firstName")
    private String firstName;

    @NotBlank
    @Column(name = "lastName")
    private String lastName;

    @Column
    private LocalDateTime invalidityDate;

    /**
     * By default an account is considered internal and not relying on an external identity service provider
     */
    @Column
    private Boolean external = false;

    @Column(name = "authentication_failed_counter")
    private Long authenticationFailedCounter;

    @GsonIgnore
    @Column(name = "password")
    private String password;

    @Column(name = "password_update_date")
    private LocalDateTime passwordUpdateDate;

    @NotNull
    @Column(name = "status")
    private AccountStatus status;

    @NotBlank
    @GsonIgnore
    @Column(name = "code")
    private String code;

    /**
     * Default empty constructor used by serializers
     */
    @SuppressWarnings("unused")
    private Account() {
        super();
        status = AccountStatus.PENDING;
        code = RandomStringUtils.randomAlphanumeric(RANDOM_STRING_LENGTH);
    }

    /**
     * Creates new Account
     *
     * @param pEmail the email
     * @param pFirstName the first name
     * @param pLastName the last name
     * @param pPassword the password
     */
    public Account(final String pEmail, final String pFirstName, final String pLastName, final String pPassword) {
        super();
        status = AccountStatus.PENDING;
        code = RandomStringUtils.randomAlphanumeric(RANDOM_STRING_LENGTH);
        email = pEmail;
        firstName = pFirstName;
        lastName = pLastName;
        setPassword(pPassword);
    }

    /**
     * @return the id
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * @param pId the id to set
     */
    public void setId(final Long pId) {
        id = pId;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param pEmail the email to set
     */
    public void setEmail(final String pEmail) {
        email = pEmail;
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param pFirstName the firstName to set
     */
    public void setFirstName(final String pFirstName) {
        firstName = pFirstName;
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param pLastName the lastName to set
     */
    public void setLastName(final String pLastName) {
        lastName = pLastName;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param pPassword the password to set
     */
    public void setPassword(final String pPassword) {
        passwordUpdateDate = LocalDateTime.now();
        password = pPassword;
    }

    /**
     * @return the status
     */
    public AccountStatus getStatus() {
        return status;
    }

    /**
     * @param pStatus the status to set
     */
    public void setStatus(final AccountStatus pStatus) {
        status = pStatus;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param pCode the code to set
     */
    public void setCode(final String pCode) {
        code = pCode;
    }

    public LocalDateTime getPasswordUpdateDate() {
        return passwordUpdateDate;
    }

    public void setPasswordUpdateDate(LocalDateTime passwordUpdateDate) {
        this.passwordUpdateDate = passwordUpdateDate;
    }

    public Boolean getExternal() {
        return external;
    }

    public void setExternal(Boolean pExternal) {
        external = pExternal;
    }

    public Long getAuthenticationFailedCounter() {
        return authenticationFailedCounter;
    }

    public void setAuthenticationFailedCounter(Long pAuthenticationFailedCounter) {
        authenticationFailedCounter = pAuthenticationFailedCounter;
    }

    public LocalDateTime getInvalidityDate() {
        return invalidityDate;
    }

    public void setInvalidityDate(LocalDateTime pInvalidityDate) {
        invalidityDate = pInvalidityDate;
    }

    @Override
    public boolean equals(final Object pObject) {
        if ((pObject == null) || !(pObject instanceof Account)) {
            return false;
        }
        // An account can be uniquely identified by its email
        final Account other = (Account) pObject;
        return new EqualsBuilder().append(getEmail(), other.getEmail()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getEmail()).toHashCode();
    }

}
