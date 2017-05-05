/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessrights.domain.projects;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Email;

import fr.cnes.regards.framework.gson.annotation.GsonIgnore;
import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.framework.jpa.converters.OffsetDateTimeAttributeConverter;
import fr.cnes.regards.framework.jpa.validator.PastOrNow;
import fr.cnes.regards.modules.accessrights.domain.UserStatus;
import fr.cnes.regards.modules.accessrights.domain.projects.listeners.ProjectUserListener;

/**
 * Domain class representing a REGARDS project user.
 *
 * @author CS
 */
@Entity
@Table(name = "T_PROJECT_USER")
@EntityListeners(ProjectUserListener.class)
@SequenceGenerator(name = "projectUserSequence", initialValue = 1, sequenceName = "SEQ_PROJECT_USER")
public class ProjectUser implements IIdentifiable<Long> {

    /**
     * The id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "projectUserSequence")
    @Column(name = "id")
    private Long id;

    /**
     * The user's email. Used to associate the Project User to its Account.
     */
    @Email
    @Column(name = "email")
    private String email;

    /**
     * The last connection date
     */
    @PastOrNow
    @Column(name = "lastConnection")
    @Convert(converter = OffsetDateTimeAttributeConverter.class)
    private OffsetDateTime lastConnection;

    /**
     * The last update date
     */
    @PastOrNow
    @Column(name = "lastUpdate")
    @Convert(converter = OffsetDateTimeAttributeConverter.class)
    private OffsetDateTime lastUpdate;

    /**
     * The status of the user
     */
    @NotNull
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    /**
     * The list of meta data on the user
     */
    @Valid
    @OneToMany(fetch = FetchType.EAGER)
    @Column(name = "metaData")
    private List<MetaData> metaData;

    /**
     * The user's role.
     */
    @Valid
    @ManyToOne
    @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "FK_USER_ROLE"))
    private Role role;

    /**
     * The list of specific permissions for this user, augmenting the permissions granted by the role.
     */
    @Valid
    @OneToMany(fetch = FetchType.LAZY)
    @Column(name = "permissions")
    @GsonIgnore
    private List<ResourcesAccess> permissions;

    /**
     * whether the project user accepted the licence or not.
     */
    private boolean licenseAccepted;

    /**
     * Create a new {@link ProjectUser} with empty values.
     */
    public ProjectUser() {
        super();
        permissions = new ArrayList<>();
        metaData = new ArrayList<>();
        status = UserStatus.WAITING_ACCESS;
    }

    /**
     * Creates a new {@link ProjectUser}
     *
     * @param pEmail
     *            The email
     * @param pRole
     *            The role
     * @param pPermissions
     *            The list of permissions
     * @param pMetaData
     *            The list of meta data
     */
    public ProjectUser(final String pEmail, final Role pRole, final List<ResourcesAccess> pPermissions,
            final List<MetaData> pMetaData) {
        email = pEmail;
        role = pRole;
        permissions = pPermissions;
        metaData = pMetaData;
        status = UserStatus.WAITING_ACCESS;
    }

    /**
     * Get <code>id</code>
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * Set <code>id</code>
     *
     * @param pId
     *            The id
     */
    public void setId(final Long pId) {
        id = pId;
    }

    /**
     * Get <code>lastConnection</code>
     *
     * @return The last connection date
     */
    public OffsetDateTime getLastConnection() {
        return lastConnection;
    }

    /**
     * Set <code>lastConnection</code>
     *
     * @param pLastConnection
     *            The last connection date
     */

    public void setLastConnection(final OffsetDateTime pLastConnection) {
        lastConnection = pLastConnection;
    }

    /**
     * Get <code>lastUpdate</code>
     *
     * @return The last update date
     */
    public OffsetDateTime getLastUpdate() {
        return lastUpdate;
    }

    /**
     * Set <code>lastUpdate</code>
     *
     * @param pLastUpdate
     *            The last update date
     */
    public void setLastUpdate(final OffsetDateTime pLastUpdate) {
        lastUpdate = pLastUpdate;
    }

    /**
     * Get <code>status</code>
     *
     * @return The status
     */
    public UserStatus getStatus() {
        return status;
    }

    /**
     * Set <code>status</code>
     *
     * @param pStatus
     *            The status
     */
    public void setStatus(final UserStatus pStatus) {
        status = pStatus;
    }

    /**
     * Get <code>metaData</code>
     *
     * @return The {@link List} of {@link MetaData}
     */
    public List<MetaData> getMetaData() {
        return metaData;
    }

    /**
     * Set <code>metaData</code>
     *
     * @param pMetaData
     *            {@link List} of {@link MetaData}
     */
    public void setMetaData(final List<MetaData> pMetaData) {
        metaData = pMetaData;
    }

    /**
     * Get <code>role</code>
     *
     * @return The role
     */
    public Role getRole() {
        return role;
    }

    /**
     * Set <code>role</code>
     *
     * @param pRole
     *            The role
     */
    public void setRole(final Role pRole) {
        role = pRole;
    }

    /**
     * Get <code>permissions</code>
     *
     * @return The {@link List} of {@link ResourcesAccess}
     */
    public List<ResourcesAccess> getPermissions() {
        return permissions;
    }

    /**
     * Set <code>permissions</code>
     *
     * @param pPermissions
     *            The {@link List} of {@link ResourcesAccess}
     */
    public void setPermissions(final List<ResourcesAccess> pPermissions) {
        permissions = pPermissions;
    }

    /**
     * Set <code>email</code>
     *
     * @return The project user's email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set the project user's <code>email</code>.
     *
     * @param pEmail
     *            Must be a valid email format.
     */
    public void setEmail(final String pEmail) {
        email = pEmail;
    }

    public boolean isLicenseAccepted() {
        return licenseAccepted;
    }

    public void setLicenseAccepted(final boolean pLicenceAccepted) {
        licenseAccepted = pLicenceAccepted;
    }

}
