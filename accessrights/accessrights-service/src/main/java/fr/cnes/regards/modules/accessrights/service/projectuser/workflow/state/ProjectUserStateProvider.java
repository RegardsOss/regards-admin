/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessrights.service.projectuser.workflow.state;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.cnes.regards.modules.accessrights.domain.UserStatus;
import fr.cnes.regards.modules.accessrights.domain.projects.ProjectUser;

/**
 * Class providing the right {@link IProjectUserTransitions} for the passed {@link ProjectUser} according to its
 * <code>state</code> field.
 *
 * @author Xavier-Alexandre Brochard
 */
@Component
public class ProjectUserStateProvider {

    /**
     * Waiting account active state
     */
    @Autowired
    private WaitingAccountActiveState waitingAccountActiveState;

    /**
     * Waiting access state
     */
    @Autowired
    private WaitingAccessState waitingAccessState;

    /**
     * Access denied state
     */
    @Autowired
    private AccessDeniedState accessDeniedState;

    /**
     * Access granted state
     */
    @Autowired
    private AccessGrantedState accessGrantedState;

    /**
     * Access inactive state
     */
    @Autowired
    private AccessInactiveState accessInactiveState;

    /**
     * Waiting email verification state
     */
    @Autowired
    private WaitingEmailVerification waitingEmailVerification;

    /**
     * Creates the right account state based on the passed status
     *
     * @param pStatus
     *            The project user status
     * @return the project user state object
     */
    public IProjectUserTransitions createState(final UserStatus pStatus) {
        final IProjectUserTransitions state;
        switch (pStatus) {
            case WAITING_EMAIL_VERIFICATION:
                state = waitingEmailVerification;
                break;
            case WAITING_ACCESS:
                state = waitingAccessState;
                break;
            case ACCESS_DENIED:
                state = accessDeniedState;
                break;
            case ACCESS_GRANTED:
                state = accessGrantedState;
                break;
            case ACCESS_INACTIVE:
                state = accessInactiveState;
                break;
            case WAITING_ACCOUNT_ACTIVE:
            default:
                state = waitingAccountActiveState;
                break;
        }
        return state;
    }

    /**
     * Creates the right account state based on the passed account's status
     *
     * @param pProjectUser
     *            The project user
     * @return the project user state object
     */
    public IProjectUserTransitions createState(final ProjectUser pProjectUser) {
        return createState(pProjectUser.getStatus());
    }

}
