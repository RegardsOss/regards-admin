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
package fr.cnes.regards.modules.project.service;

import javax.annotation.PostConstruct;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.amqp.IInstancePublisher;
import fr.cnes.regards.framework.amqp.event.tenant.TenantCreatedEvent;
import fr.cnes.regards.framework.amqp.event.tenant.TenantDeletedEvent;
import fr.cnes.regards.framework.jpa.instance.transactional.InstanceTransactional;
import fr.cnes.regards.framework.jpa.multitenant.properties.MultitenantDaoProperties;
import fr.cnes.regards.framework.jpa.multitenant.properties.TenantConnection;
import fr.cnes.regards.framework.module.rest.exception.EntityAlreadyExistsException;
import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.project.dao.IProjectRepository;
import fr.cnes.regards.modules.project.domain.Project;

/**
 *
 * Service class to manage REGARDS projects.
 *
 * @author Sylvain Vissiere-Guerinet
 * @author Christophe Mertz
 * @author Sébastien Binda
 *
 * @since 1.0-SNAPSHOT
 */
@Service
@InstanceTransactional
public class ProjectService implements IProjectService {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProjectService.class);

    /**
     * JPA Repository to query projects from database
     */
    private final IProjectRepository projectRepository;

    /**
     * JPA Multitenants default configuration from properties file.
     */
    private final MultitenantDaoProperties defaultProperties;

    /**
     * AMQP message publisher
     */
    private final IInstancePublisher instancePublisher;

    /**
     * The constructor.
     *
     * @param pProjectRepository
     *            The JPA repository.
     */
    public ProjectService(final IProjectRepository pProjectRepository,
            final MultitenantDaoProperties pDefaultProperties, IInstancePublisher instancePublisher) {
        super();
        projectRepository = pProjectRepository;
        defaultProperties = pDefaultProperties;
        this.instancePublisher = instancePublisher;
    }

    @PostConstruct
    public void projectsInitialization() throws ModuleException {

        // Create project from properties files it does not exists yet
        for (final TenantConnection tenant : defaultProperties.getTenants()) {
            if (projectRepository.findOneByNameIgnoreCase(tenant.getTenant()) == null) {
                LOG.info(String.format("Creating new project %s from static properties configuration",
                                       tenant.getTenant()));
                Project project=new Project("", "", true, tenant.getTenant());
                project.setLabel(tenant.getTenant());
                project.setAccessible(true);
                createProject(project);
            }
        }
    }

    @Override
    public Project retrieveProject(final String pProjectName) throws ModuleException {
        final Project project = projectRepository.findOneByNameIgnoreCase(pProjectName);
        if (project == null) {
            throw new EntityNotFoundException(pProjectName, Project.class);
        }
        return project;
    }

    @Override
    public void deleteProject(final String pProjectName) throws ModuleException {
        final Project deleted = retrieveProject(pProjectName);
        deleted.setDeleted(true);
        projectRepository.save(deleted);
        // Publish tenant deletion
        TenantDeletedEvent tde = new TenantDeletedEvent();
        tde.setTenant(pProjectName);
        instancePublisher.publish(tde);
    }

    @Override
    public Project updateProject(final String pProjectName, final Project pProject) throws ModuleException {
        final Project theProject = projectRepository.findOneByNameIgnoreCase(pProjectName);
        if (theProject == null) {
            throw new EntityNotFoundException(pProjectName, Project.class);
        }
        if (theProject.isDeleted()) {
            throw new IllegalStateException("This project is deleted.");
        }
        if (!pProject.getName().equals(pProjectName)) {
            throw new EntityInvalidException("projectId and updated project does not match.");
        }
        return projectRepository.save(pProject);
    }

    @Override
    public Page<Project> retrieveProjectList(final Pageable pPageable) {
        return projectRepository.findAll(pPageable);
    }

    @Override
    public List<Project> retrieveProjectList() {
        return projectRepository.findAll();
    }

    @Override
    public Page<Project> retrievePublicProjectList(final Pageable pPageable) {
        return projectRepository.findByIsPublicTrue(pPageable);
    }

    @Override
    public Project createProject(final Project pNewProject) throws ModuleException {
        final Project theProject = projectRepository.findOneByNameIgnoreCase(pNewProject.getName());
        if (theProject != null) {
            throw new EntityAlreadyExistsException("A Project with name "+pNewProject.getName()+" already exists");
        }

        Project project = projectRepository.save(pNewProject);

        // Publish tenant creation
        TenantCreatedEvent tce = new TenantCreatedEvent();
        tce.setTenant(pNewProject.getName());
        instancePublisher.publish(tce);

        return project;
    }

}
