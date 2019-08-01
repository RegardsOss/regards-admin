/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.sessionmanager.service;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.EntityOperationForbiddenException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.sessionmanager.domain.Session;
import fr.cnes.regards.modules.sessionmanager.domain.SessionState;
import fr.cnes.regards.modules.sessionmanager.dao.ISessionRepository;
import fr.cnes.regards.modules.sessionmanager.dao.SessionSpecifications;
import fr.cnes.regards.modules.sessionmanager.domain.event.DeleteSessionNotification;
import fr.cnes.regards.modules.sessionmanager.domain.event.SessionMonitoringEvent;
import fr.cnes.regards.modules.sessionmanager.domain.event.SessionNotificationOperator;
import fr.cnes.regards.modules.sessionmanager.domain.event.SessionNotificationState;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SessionService implements ISessionService {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(SessionService.class);

    /**
     * CRUD repository managing sessions. Autowired by Spring.
     */
    @Autowired
    private ISessionRepository sessionRepository;

    /**
     * Publisher to notify system of files events (stored, retrieved or deleted).
     */
    @Autowired
    private IPublisher publisher;

    @Override
    @Transactional(readOnly = true)
    public Page<Session> retrieveSessions(String source, String name, OffsetDateTime from, OffsetDateTime to, SessionState state, boolean onlyLastSession, Pageable page) {
        return sessionRepository.findAll(SessionSpecifications.search(source, name, from, to, state, onlyLastSession), page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> retrieveSessionNames(String name) {
        return sessionRepository.findAllSessionName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> retrieveSessionSources(String source) {
        return sessionRepository.findAllSessionSource(source);
    }

    @Override
    public Session updateSessionState(Long id, SessionState state) throws ModuleException {
        Session s = getSession(id);
        // Allow user to mark as acknowledged a session previously in error
        if (s.getState() == SessionState.ERROR && state == SessionState.ACKNOWLEDGED) {
            s.setState(state);
            return this.updateSession(s);
        }
        String errorMessage = String.format("Changing session state from %s to %s isn't allowed", s.getState(), state);
        LOG.debug(errorMessage);
        throw new EntityOperationForbiddenException(String.valueOf(s.getId()), Session.class, errorMessage);
    }

    @Override
    public void deleteSession(Long id, boolean force) throws ModuleException {
        Session s = getSession(id);
        if (s.getState() == SessionState.DELETED && !force) {
            String errorMessage = String.format("Can't delete the session %s %s as it's already marked as deleted", s.getSource(), s.getName());
            LOG.debug(errorMessage);
            throw new EntityOperationForbiddenException(String.valueOf(s.getId()), Session.class, errorMessage);
        }
        // If the Session is already mark as deleted, don't send the notification
        if (s.getState() != SessionState.DELETED) {
            this.sendDeleteNotification(s);
        }
        if (force) {
            LOG.info("Delete definitely session {} {}", s.getSource(), s.getName());
            sessionRepository.delete(s);
        } else {
            LOG.info("Mark session {} {} as deleted", s.getSource(), s.getName());
            s.setState(SessionState.DELETED);
            updateSession(s);
        }
    }

    @Override
    public Session updateSessionProperty(SessionMonitoringEvent sessionMonitoringEvent) {
        // Retrieve the session to update or create it
        Optional<Session> sessionOpt = sessionRepository.findOneBySourceAndName(sessionMonitoringEvent.getSource(), sessionMonitoringEvent.getName());
        Session sessionToUpdate;
        if (!sessionOpt.isPresent()) {
            sessionToUpdate = createSession(sessionMonitoringEvent.getName(), sessionMonitoringEvent.getSource());
        } else {
            sessionToUpdate = sessionOpt.get();
        }

        // Set the new value inside the map
        boolean isKeyExisting = sessionToUpdate.isStepPropertyExisting(sessionMonitoringEvent.getStep(), sessionMonitoringEvent.getProperty());
        if (isKeyExisting && (
                sessionMonitoringEvent.getOperator() == SessionNotificationOperator.INC ||
                        sessionMonitoringEvent.getOperator() == SessionNotificationOperator.DEC
        )) {
            // Handle mathematical operators
            Object previousValueAsObject = sessionToUpdate.getStepPropertyValue(sessionMonitoringEvent.getStep(), sessionMonitoringEvent.getProperty());
            long previousValue;
            // We support only numerical value, so we fallback previousValue to zero if its type is string
            if (previousValueAsObject instanceof String) {
                previousValue = 0;
            } else {
                previousValue = (long) sessionToUpdate.getStepPropertyValue(sessionMonitoringEvent.getStep(), sessionMonitoringEvent.getProperty());
            }
            long updatedValue;
            switch (sessionMonitoringEvent.getOperator()) {
                case INC:
                    updatedValue = previousValue + (long) sessionMonitoringEvent.getValue();
                    break;
                case DEC:
                default:
                    updatedValue = previousValue - (long) sessionMonitoringEvent.getValue();
            }
            sessionToUpdate.setStepPropertyValue(sessionMonitoringEvent.getStep(), sessionMonitoringEvent.getProperty(), updatedValue);
        } else {
            switch (sessionMonitoringEvent.getOperator()) {
                case INC:
                case REPLACE:
                    // Just use the provided value
                    sessionToUpdate.setStepPropertyValue(sessionMonitoringEvent.getStep(), sessionMonitoringEvent.getProperty(), sessionMonitoringEvent.getValue());
                    break;
                case DEC:
                    // If we create using the DEC operator, we use the opposite value
                    double valueDec = -(long) sessionMonitoringEvent.getValue();
                    sessionToUpdate.setStepPropertyValue(sessionMonitoringEvent.getStep(), sessionMonitoringEvent.getProperty(), valueDec);
                    break;
            }
        }
        // Update the state if we receive an error
        if (sessionMonitoringEvent.getState() == SessionNotificationState.ERROR) {
            sessionToUpdate.setState(SessionState.ERROR);
        }
        // Save the session
        return this.updateSession(sessionToUpdate);
    }

    /**
     * Create a new session and remove the flag isLatest to the previous entity having the same source
     * @param name   The session name
     * @param source The session source
     */
    private Session createSession(String name, String source) {

        Session newSession = sessionRepository.save(new Session(source, name));
        // Remove the flag isLatest to the previous one session sharing the same source
        Optional<Session> oldLatestSessionOpt = sessionRepository.findOneBySourceAndIsLatestTrue(source);
        if (oldLatestSessionOpt.isPresent()) {
            Session oldLatestSession = oldLatestSessionOpt.get();
            oldLatestSession.setLatest(false);
            sessionRepository.save(oldLatestSession);
        }
        return newSession;
    }


    private Session getSession(Long id) throws EntityNotFoundException {
        Optional<Session> sessionOpt = sessionRepository.findById(id);
        if (!sessionOpt.isPresent()) {
            throw new EntityNotFoundException(sessionOpt.toString(), Session.class);
        }
        return sessionOpt.get();
    }

    /**
     * Update the provided session and set the lastUpdate date
     * @param session
     * @return
     */
    private Session updateSession(Session session) {
        session.setLastUpdateDate(OffsetDateTime.now());
        return sessionRepository.save(session);
    }

    /**
     * Send a notification this session have been deleted
     * @param session
     */
    private void sendDeleteNotification(Session session) {
        DeleteSessionNotification notif = DeleteSessionNotification.build(session.getSource(), session.getName());
        publisher.publish(notif);
    }
}
