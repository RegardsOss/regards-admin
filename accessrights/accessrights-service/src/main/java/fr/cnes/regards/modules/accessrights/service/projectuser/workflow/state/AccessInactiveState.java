/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.accessrights.service.projectuser.workflow.state;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.modules.accessrights.dao.projects.IProjectUserRepository;
import fr.cnes.regards.modules.accessrights.domain.UserStatus;
import fr.cnes.regards.modules.accessrights.domain.projects.ProjectUser;
import fr.cnes.regards.modules.accessrights.service.projectuser.emailverification.IEmailVerificationTokenService;
import fr.cnes.regards.modules.accessrights.service.projectuser.workflow.events.OnActiveEvent;

/**
 * State class of the State Pattern implementing the available actions on a {@link ProjectUser} in status ACCESS_INACTIVE.
 *
 * @author Xavier-Alexandre Brochard
 */
@Component
public class AccessInactiveState extends AbstractDeletableState {

    /**
     * Use this to publish Spring application events
     */
    private final ApplicationEventPublisher eventPublisher;

    /**
     * @param pProjectUserRepository
     * @param pEmailVerificationTokenService
     * @param pPublisher
     * @param pEventPublisher
     */
    public AccessInactiveState(IProjectUserRepository pProjectUserRepository,
            IEmailVerificationTokenService pEmailVerificationTokenService, IPublisher pPublisher,
            ApplicationEventPublisher pEventPublisher) {
        super(pProjectUserRepository, pEmailVerificationTokenService, pPublisher);
        eventPublisher = pEventPublisher;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.cnes.regards.modules.accessrights.service.projectuser.IProjectUserTransitions#activeAccess(fr.cnes.regards.
     * modules.accessrights.domain.projects.ProjectUser)
     */
    @Override
    public void activeAccess(final ProjectUser pProjectUser) {
        pProjectUser.setStatus(UserStatus.ACCESS_GRANTED);
        getProjectUserRepository().save(pProjectUser);
        eventPublisher.publishEvent(new OnActiveEvent(pProjectUser));
    }

}
