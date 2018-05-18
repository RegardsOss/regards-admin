/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.project.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.amqp.IInstancePublisher;
import fr.cnes.regards.framework.jpa.instance.transactional.InstanceTransactional;
import fr.cnes.regards.framework.jpa.multitenant.event.TenantConnectionConfigurationCreated;
import fr.cnes.regards.framework.jpa.multitenant.event.TenantConnectionConfigurationDeleted;
import fr.cnes.regards.framework.jpa.multitenant.event.TenantConnectionConfigurationUpdated;
import fr.cnes.regards.framework.jpa.multitenant.properties.TenantConnection;
import fr.cnes.regards.framework.jpa.multitenant.properties.TenantConnectionState;
import fr.cnes.regards.framework.module.rest.exception.EntityAlreadyExistsException;
import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.project.dao.IProjectConnectionRepository;
import fr.cnes.regards.modules.project.dao.IProjectRepository;
import fr.cnes.regards.modules.project.domain.Project;
import fr.cnes.regards.modules.project.domain.ProjectConnection;

/**
 *
 * Service class to manage REGARDS projects.
 *
 * @author Sylvain Vissiere-Guerinet
 * @author Christophe Mertz
 * @author Sébastien Binda
 * @author Xavier-Alexandre Brochard
 *
 * @since 1.0-SNAPSHOT
 */
@Service
@InstanceTransactional
public class ProjectConnectionService implements IProjectConnectionService {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectConnectionService.class);

    /**
     * JPA Repository to query projects from database
     */
    private final IProjectRepository projectRepository;

    /**
     * JPA Repository to query projectConnection from database
     */
    private final IProjectConnectionRepository projectConnectionRepository;

    /**
     * AMQP message publisher
     */
    private final IInstancePublisher instancePublisher;

    /**
     * The constructor.
     *
     * @param pProjectRepository
     *            The JPA {@link Project} repository.
     * @param pProjectConnectionRepository
     *            The JPA {@link ProjectConnection} repository.
     * @param pInstancePublisher
     *            Amqp publisher
     */
    public ProjectConnectionService(final IProjectRepository pProjectRepository,
            final IProjectConnectionRepository pProjectConnectionRepository,
            final IInstancePublisher pInstancePublisher) {
        super();
        projectRepository = pProjectRepository;
        projectConnectionRepository = pProjectConnectionRepository;
        instancePublisher = pInstancePublisher;
    }

    @Override
    public Page<ProjectConnection> retrieveProjectsConnections(final Pageable pPageable) {
        return projectConnectionRepository.findAll(pPageable);
    }

    @Override
    public ProjectConnection retrieveProjectConnection(final String projectName, final String microservice)
            throws EntityNotFoundException {
        final ProjectConnection connection = projectConnectionRepository
                .findOneByProjectNameAndMicroservice(projectName, microservice);
        if (connection == null) {
            String message = String.format("No connection found for project %s and microservice %s", projectName,
                                           microservice);
            LOGGER.error(message);
            throw new EntityNotFoundException(message);
        }
        return connection;
    }

    @Override
    public boolean existsProjectConnection(String projectName, String microservice) {
        return projectConnectionRepository.existsProjectConnection(projectName, microservice);
    }

    @Override
    public ProjectConnection createProjectConnection(final ProjectConnection projectConnection, final boolean silent)
            throws ModuleException {
        final ProjectConnection connection;
        final Project project = projectConnection.getProject();
        // Check referenced project exists
        if ((project.getId() != null) && projectRepository.isActiveProject(project.getId())) {
            // Manage connection conflicts!
            manageProjectConnectionConflicts(projectConnection);
            // Check project connection to create doesn't already exists
            if (projectConnectionRepository
                    .findOneByProjectNameAndMicroservice(project.getName(),
                                                         projectConnection.getMicroservice()) == null) {
                // Connection disabled
                // Multitenant starter is responsible for enabling data source
                projectConnection.setState(TenantConnectionState.DISABLED);
                connection = projectConnectionRepository.save(projectConnection);
            } else {
                throw new EntityAlreadyExistsException(project.getName());
            }
        } else {
            throw new EntityNotFoundException(projectConnection.getId().toString(), ProjectConnection.class);
        }

        // Send event to all microservices that a new connection is available for a new project
        if (!silent) {
            instancePublisher.publish(new TenantConnectionConfigurationCreated(toTenantConnection(connection),
                    connection.getMicroservice()));
        }

        return connection;
    }

    /**
     * Test potential conflict with an existing project connection.<br/>
     * <ul>
     * <li>A same connection can be used by a same project on another microservice</li>
     * <li>A same connection cannot be used by 2 different projects</li>
     * </ul>
     * @param projectConnection project connection to control
     * @throws EntityInvalidException if a conflict is detected
     */
    private void manageProjectConnectionConflicts(ProjectConnection projectConnection) throws EntityInvalidException {
        List<ProjectConnection> connections = projectConnectionRepository
                .findByUserNameAndPasswordAndUrl(projectConnection.getUserName(), projectConnection.getPassword(),
                                                 projectConnection.getUrl());

        String projectName = projectConnection.getProject().getName();
        for (ProjectConnection connection : connections) {
            if (!projectName.equals(connection.getProject().getName())) {
                String message = String
                        .format("A same connection can only be used by a same project on another microservice. Conflict detected between project %s and project %s.",
                                projectName, connection.getProject().getName());
                LOGGER.error(message);
                throw new EntityInvalidException(message);
            }
        }

    }

    @Override
    public ProjectConnection createStaticProjectConnection(ProjectConnection projectConnection) throws ModuleException {

        // Only store connection if it's really does not exist
        if (existsProjectConnection(projectConnection.getProject().getName(), projectConnection.getMicroservice())) {
            LOGGER.warn("Project connection already exists for tenant {} and microservice {}",
                        projectConnection.getProject().getName(), projectConnection.getMicroservice());
            // Skipping silently
            return projectConnection;
        }

        projectConnection.setState(TenantConnectionState.ENABLED);
        return projectConnectionRepository.save(projectConnection);
    }

    /**
     * Transform {@link ProjectConnection} to {@link TenantConnection}
     *
     * @param pConnection
     *            related connection
     */
    private TenantConnection toTenantConnection(ProjectConnection pConnection) {
        // Send event to all microservices that a new connection is available for a new project
        return new TenantConnection(pConnection.getProject().getName(), pConnection.getUrl(), pConnection.getUserName(),
                pConnection.getPassword(), pConnection.getDriverClassName());
    }

    @Override
    public void deleteProjectConnection(final Long pProjectConnectionId) throws EntityNotFoundException {
        ProjectConnection connection = projectConnectionRepository.findOne(pProjectConnectionId);
        if (connection != null) {
            projectConnectionRepository.delete(pProjectConnectionId);
        } else {
            final String message = "Invalid entity <ProjectConnection> for deletion. Entity (id=%d) does not exists";
            LOGGER.error(String.format(message, pProjectConnectionId));
            throw new EntityNotFoundException(pProjectConnectionId.toString(), ProjectConnection.class);
        }

        // Publish configuration deletion
        instancePublisher.publish(new TenantConnectionConfigurationDeleted(toTenantConnection(connection),
                connection.getMicroservice()));
    }

    @Override
    public ProjectConnection updateProjectConnection(final Long pProjectConnectionId,
            final ProjectConnection projectConnection) throws ModuleException {
        final ProjectConnection connection;
        // Check that entity to update exists
        if ((projectConnection.getId() != null) && projectConnectionRepository.exists(projectConnection.getId())) {
            final Project project = projectConnection.getProject();
            // Check that the referenced project exists
            if ((project.getId() != null) && projectRepository.isActiveProject(project.getId())) {
                // Manage connection conflicts!
                manageProjectConnectionConflicts(projectConnection);
                // Disable connection : new configuration may be incorrect
                // Multitenant starter is reponsible for enabling data source
                projectConnection.setState(TenantConnectionState.DISABLED);
                // Update entity
                connection = projectConnectionRepository.save(projectConnection);
            } else {
                throw new EntityNotFoundException(project.getName(), Project.class);
            }
        } else {
            throw new EntityNotFoundException(projectConnection.getId().toString(), ProjectConnection.class);
        }

        // Publish configuration update
        instancePublisher.publish(new TenantConnectionConfigurationUpdated(toTenantConnection(connection),
                connection.getMicroservice()));

        return connection;
    }

    @Override
    public Page<ProjectConnection> retrieveProjectsConnectionsByProject(final String pProjectName,
            final Pageable pPageable) {
        return projectConnectionRepository.findByProjectName(pProjectName, pPageable);
    }

    @Override
    public ProjectConnection retrieveProjectConnectionById(final Long pId) throws EntityNotFoundException {
        final Optional<ProjectConnection> result = Optional.ofNullable(projectConnectionRepository.findOne(pId));
        return result.orElseThrow(() -> new EntityNotFoundException(pId, ProjectConnection.class));
    }

    @Override
    public List<ProjectConnection> retrieveProjectConnections(final String microservice) {
        return projectConnectionRepository.getMicroserviceConnections(microservice);
    }

    @Override
    public ProjectConnection updateState(String microservice, String projectName, TenantConnectionState state,
            Optional<String> errorCause) throws EntityNotFoundException {
        ProjectConnection connection = retrieveProjectConnection(projectName, microservice);
        connection.setState(state);
        connection.setErrorCause(errorCause.orElse(null));
        return projectConnectionRepository.save(connection);
    }
}
