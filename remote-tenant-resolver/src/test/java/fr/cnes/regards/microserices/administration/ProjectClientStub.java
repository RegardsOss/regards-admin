/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.microserices.administration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import fr.cnes.regards.modules.core.exception.EntityException;
import fr.cnes.regards.modules.core.exception.EntityNotFoundException;
import fr.cnes.regards.modules.project.client.IProjectsClient;
import fr.cnes.regards.modules.project.domain.Project;
import fr.cnes.regards.modules.project.domain.ProjectConnection;

/**
 *
 * Class ProjectClientStub
 *
 * Stub class for administration service client.
 *
 * @author CS
 * @since 1.0-SNAPSHOT
 */
@Component
public class ProjectClientStub implements IProjectsClient {

    /**
     * Project name return by this stub
     */
    public static final String PROJECT_NAME = "stub-project";

    /**
     * Project managed by this stub
     */
    private final Project project = new Project(0L, "", "", true, PROJECT_NAME);

    @Override
    public ResponseEntity<List<Resource<Project>>> retrieveProjectList() {
        final List<Resource<Project>> resouces = new ArrayList<>();
        resouces.add(new Resource<Project>(project));
        return new ResponseEntity<>(resouces, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Resource<ProjectConnection>> retrieveProjectConnection(final String pProjectName,
            final String pMicroService) throws EntityNotFoundException {

        final ProjectConnection connection = new ProjectConnection(0L, project, pMicroService, "", "", "", "");
        final Resource<ProjectConnection> resource = new Resource<ProjectConnection>(connection);
        return new ResponseEntity<Resource<ProjectConnection>>(resource, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Resource<Project>> createProject(final Project pNewProject) throws EntityException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResponseEntity<Resource<Project>> retrieveProject(final String pProjectName) throws EntityException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResponseEntity<Resource<Project>> updateProject(final String pProjectName, final Project pProjectToUpdate)
            throws EntityException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteProject(final String pProjectName) throws EntityException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResponseEntity<Resource<ProjectConnection>> createProjectConnection(
            final ProjectConnection pProjectConnection) throws EntityException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResponseEntity<Resource<ProjectConnection>> updateProjectConnection(
            final ProjectConnection pProjectConnection) throws EntityException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteProjectConnection(final String pProjectName, final String pMicroservice)
            throws EntityException {
        // TODO Auto-generated method stub
        return null;
    }

}