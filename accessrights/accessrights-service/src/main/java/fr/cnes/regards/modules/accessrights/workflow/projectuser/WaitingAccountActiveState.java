/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessrights.workflow.projectuser;

import org.springframework.stereotype.Component;

import fr.cnes.regards.framework.module.rest.exception.EntityTransitionForbiddenException;
import fr.cnes.regards.modules.accessrights.dao.projects.IProjectUserRepository;
import fr.cnes.regards.modules.accessrights.domain.UserStatus;
import fr.cnes.regards.modules.accessrights.domain.projects.ProjectUser;

/**
 * State class of the State Pattern implementing the available actions on a {@link ProjectUser} in status
 * WAITING_ACCOUNT_ACTIVE.
 *
 * @author Xavier-Alexandre Brochard
 * @since 1.1-SNAPSHOT
 */
@Component
public class WaitingAccountActiveState extends AbstractDeletableState {

    /**
     * Creates a new PENDING state
     *
     * @param pProjectUserRepository
     *            the project user repository
     * @param pAccessSettingsService
     *            the project user settings repository
     */
    public WaitingAccountActiveState(final IProjectUserRepository pProjectUserRepository) {
        super(pProjectUserRepository);
    }

    /* (non-Javadoc)
     * @see fr.cnes.regards.modules.accessrights.workflow.projectuser.AbstractProjectUserState#makeProjectUserWaitForQualification(fr.cnes.regards.modules.accessrights.domain.projects.ProjectUser)
     */
    @Override
    public void makeProjectUserWaitForQualification(ProjectUser pProjectUser)
            throws EntityTransitionForbiddenException {
        pProjectUser.setStatus(UserStatus.WAITING_ACCESS);
        getProjectUserRepository().save(pProjectUser);
    }

}
