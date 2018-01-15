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
package fr.cnes.regards.modules.project.rest;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.hateoas.LinkRels;
import fr.cnes.regards.framework.hateoas.MethodParamFactory;
import fr.cnes.regards.framework.module.annotation.ModuleInfo;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.framework.security.role.DefaultRole;
import fr.cnes.regards.modules.project.domain.Project;
import fr.cnes.regards.modules.project.service.IProjectService;

/**
 *
 * Class ProjectsController
 *
 * Controller for REST Access to Project entities
 *
 * @author Sébastien Binda
 * @since 1.0-SNAPSHOT
 */
@RestController
@ModuleInfo(name = "project", version = "1.0-SNAPSHOT", author = "REGARDS", legalOwner = "CS",
        documentation = "http://test")
@RequestMapping("/projects")
public class ProjectController implements IResourceController<Project> {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProjectController.class);

    /**
     * Business service for Project entities. Autowired.
     */
    private final IProjectService projectService;

    /**
     * Resource service to manage visibles hateoas links
     */
    private final IResourceService resourceService;

    public ProjectController(final IProjectService pProjectService, final IResourceService pResourceService) {
        super();
        projectService = pProjectService;
        resourceService = pResourceService;
    }

    /**
     *
     * Retrieve projects list
     *
     * @return List of projects
     * @since 1.0-SNAPSHOT
     */
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResourceAccess(description = "retrieve the list of project of instance", role = DefaultRole.INSTANCE_ADMIN)
    public ResponseEntity<PagedResources<Resource<Project>>> retrieveProjectList(final Pageable pPageable,
            final PagedResourcesAssembler<Project> pAssembler) {
        final Page<Project> projects = projectService.retrieveProjectList(pPageable);
        return ResponseEntity.ok(toPagedResources(projects, pAssembler));
    }

    /**
     *
     * Retrieve projects list
     *
     * @return List of projects
     * @since 1.0-SNAPSHOT
     */
    @RequestMapping(value = "/public", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResourceAccess(description = "retrieve the list of project of instance", role = DefaultRole.PUBLIC)
    public ResponseEntity<PagedResources<Resource<Project>>> retrievePublicProjectList(final Pageable pPageable,
            final PagedResourcesAssembler<Project> pAssembler) {
        final Page<Project> projects = projectService.retrievePublicProjectList(pPageable);
        return ResponseEntity.ok(toPagedResources(projects, pAssembler));
    }

    /**
     *
     * Create a new project
     *
     * @param pNewProject
     *            new Project to create
     * @return Created project
     * @throws ModuleException
     *             If Project already exists for the given name
     * @since 1.0-SNAPSHOT
     */
    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    @ResourceAccess(description = "create a new project", role = DefaultRole.INSTANCE_ADMIN)
    public ResponseEntity<Resource<Project>> createProject(@Valid @RequestBody final Project pNewProject)
            throws ModuleException {
        final Project project = projectService.createProject(pNewProject);
        return new ResponseEntity<>(toResource(project), HttpStatus.CREATED);
    }

    /**
     *
     * Retrieve a project by name
     *
     * @param pProjectName
     *            Project name
     * @return Project
     * @throws ModuleException
     *             {@link EntityNotFoundException} project does not exists
     * @since 1.0-SNAPSHOT
     */
    @RequestMapping(method = RequestMethod.GET, value = "/{project_name}", produces = "application/json")
    @ResponseBody
    @ResourceAccess(description = "retrieve the project project_name", role = DefaultRole.INSTANCE_ADMIN)
    public ResponseEntity<Resource<Project>> retrieveProject(@PathVariable("project_name") final String pProjectName)
            throws ModuleException {

        final Project project = projectService.retrieveProject(pProjectName);
        return ResponseEntity.ok(toResource(project));
    }

    /**
     *
     * Update given project.
     *
     * @param pProjectName
     *            project name
     * @param pProjectToUpdate
     *            project to update
     * @throws ModuleException
     *             {@link EntityNotFoundException} project does not exists
     * @return Updated Project
     * @since 1.0-SNAPSHOT
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/{project_name}")
    @ResponseBody
    @ResourceAccess(description = "update the project project_name", role = DefaultRole.INSTANCE_ADMIN)
    public ResponseEntity<Resource<Project>> updateProject(@PathVariable("project_name") final String pProjectName,
            @Valid @RequestBody final Project pProjectToUpdate) throws ModuleException {
        final Project project = projectService.updateProject(pProjectName, pProjectToUpdate);
        return ResponseEntity.ok(toResource(project));
    }

    /**
     *
     * Delete given project
     *
     * @param pProjectName
     *            Project name to delete
     * @throws ModuleException
     *             {@link EntityNotFoundException} project does not exists
     * @return Void
     * @since 1.0-SNAPSHOT
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/{project_name}")
    @ResponseBody
    @ResourceAccess(description = "remove the project project_name", role = DefaultRole.INSTANCE_ADMIN)
    public ResponseEntity<Void> deleteProject(@PathVariable("project_name") final String pProjectName)
            throws ModuleException {
        projectService.deleteProject(pProjectName);
        return ResponseEntity.noContent().build();
    }

    @Override
    public Resource<Project> toResource(final Project pElement, final Object... pExtras) {

        Resource<Project> resource = null;
        if ((pElement != null) && (pElement.getName() != null)) {
            resource = resourceService.toResource(pElement);
            resourceService.addLink(resource, this.getClass(), "retrieveProject", LinkRels.SELF,
                                    MethodParamFactory.build(String.class, pElement.getName()));
            resourceService.addLink(resource, this.getClass(), "deleteProject", LinkRels.DELETE,
                                    MethodParamFactory.build(String.class, pElement.getName()));
            resourceService.addLink(resource, this.getClass(), "updateProject", LinkRels.UPDATE,
                                    MethodParamFactory.build(String.class, pElement.getName()),
                                    MethodParamFactory.build(Project.class, pElement));
            resourceService.addLink(resource, this.getClass(), "createProject", LinkRels.CREATE,
                                    MethodParamFactory.build(Project.class, pElement));
        } else {
            LOG.warn(String.format("Invalid %s entity. Cannot create hateoas resources", this.getClass().getName()));
        }
        return resource;
    }
}
