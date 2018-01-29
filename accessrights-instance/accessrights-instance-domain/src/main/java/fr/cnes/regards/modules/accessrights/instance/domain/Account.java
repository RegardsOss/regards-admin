/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.accessrights.instance.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import fr.cnes.regards.framework.gson.annotation.GsonIgnore;
import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.framework.jpa.annotation.InstanceEntity;

/**
 * Account entity
 *
 * @author Xavier-Alexandre Brochard
 */
@InstanceEntity
@Entity
@Table(name = "t_account", uniqueConstraints = @UniqueConstraint(name = "uk_account_email", columnNames = { "email" }))
@SequenceGenerator(name = "accountSequence", initialValue = 1, sequenceName = "seq_account")
public class Account implements IIdentifiable<Long> {

    @Transient
    private static final int RANDOM_STRING_LENGTH = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accountSequence")
    @Column(name = "id")
    private Long id;

    @Email
    @Column(name = "email", length = 100)
    private String email;

    @NotBlank
    @Column(name = "firstName", length = 100)
    private String firstName;

    @NotBlank
    @Column(name = "lastName", length = 100)
    private String lastName;

    /**
     * invalidity date of the account
     */
    @Column
    private LocalDateTime invalidityDate;

    /**
     * By default an account is considered internal and not relying on an external identity service provider
     */
    @Column
    private Boolean external = false;

    @Column(name = "authentication_failed_counter")
    private Long authenticationFailedCounter = 0L;

    @GsonIgnore
    @Column(name = "password", length = 200)
    private String password;

    /**
     * last password update date
     */
    @Column(name = "password_update_date")
    private LocalDateTime passwordUpdateDate;

    @NotNull
    @Column(name = "status", length = 10)
    @Enumerated(value = EnumType.STRING)
    private AccountStatus status;

    /**
     * Default empty constructor used by serializers
     */
    @SuppressWarnings("unused")
    private Account() {
        super();
        status = AccountStatus.PENDING;
    }

    /**
     * Creates new Account
     *
     * @param pEmail
     *            the email
     * @param pFirstName
     *            the first name
     * @param pLastName
     *            the last name
     * @param pPassword
     *            the password
     */
    public Account(final String pEmail, final String pFirstName, final String pLastName, final String pPassword) {
        super();
        status = AccountStatus.PENDING;
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
     * @param pId
     *            the id to set
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
     * @param pEmail
     *            the email to set
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
     * @param pFirstName
     *            the firstName to set
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
     * @param pLastName
     *            the lastName to set
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
     * @param pPassword
     *            the password to set
     */
    public final void setPassword(final String pPassword) {
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
     * @param pStatus
     *            the status to set
     */
    public void setStatus(final AccountStatus pStatus) {
        status = pStatus;
    }

    /**
     * @return the last password update date
     */
    public LocalDateTime getPasswordUpdateDate() {
        return passwordUpdateDate;
    }

    public void setPasswordUpdateDate(final LocalDateTime passwordUpdateDate) {
        this.passwordUpdateDate = passwordUpdateDate;
    }

    /**
     * @return whether this account is external to REGARDS
     */
    public Boolean getExternal() {
        return external;
    }

    public void setExternal(final Boolean pExternal) {
        external = pExternal;
    }

    /**
     * @return the authentication failed counter
     */
    public Long getAuthenticationFailedCounter() {
        return authenticationFailedCounter;
    }

    public void setAuthenticationFailedCounter(final Long pAuthenticationFailedCounter) {
        authenticationFailedCounter = pAuthenticationFailedCounter;
    }

    /**
     * @return the account invalidity date
     */
    public LocalDateTime getInvalidityDate() {
        return invalidityDate;
    }

    public void setInvalidityDate(final LocalDateTime pInvalidityDate) {
        invalidityDate = pInvalidityDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Account account = (Account) o;

        return email.equals(account.email);
    }

    @Override
    public int hashCode() {
        return email.hashCode();
    }
}