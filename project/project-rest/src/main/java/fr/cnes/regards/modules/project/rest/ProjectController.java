package fr.cnes.regards.modules.project.rest;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.cnes.regards.microservices.core.auth.MethodAutorizationService;
import fr.cnes.regards.microservices.core.auth.ResourceAccess;
import fr.cnes.regards.microservices.core.auth.RoleAuthority;
import fr.cnes.regards.microservices.core.information.ModuleInfo;
import fr.cnes.regards.modules.project.domain.Project;
import fr.cnes.regards.modules.project.service.AlreadyExistingException;
import fr.cnes.regards.modules.project.service.IProjectService;

@RestController
@ModuleInfo(name = "project", version = "1.0-SNAPSHOT", author = "REGARDS", legalOwner = "CS", documentation = "http://test")
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private MethodAutorizationService authService;

    @Autowired
    private IProjectService projectService;

    /**
     * Method to initiate REST resources authorizations.
     */
    @PostConstruct
    public void initAuthorisations() {
        // admin can do everything!
        authService.setAutorities("/projects@GET", new RoleAuthority("ADMIN"));
        authService.setAutorities("/projects@POST", new RoleAuthority("ADMIN"));
        authService.setAutorities("/projects/{project_id}@GET", new RoleAuthority("ADMIN"));
        authService.setAutorities("/projects/{project_id}@PUT", new RoleAuthority("ADMIN"));
        authService.setAutorities("/projects/{project_id}@DELETE", new RoleAuthority("ADMIN"));
        // users can just get!
        authService.setAutorities("/projects@GET", new RoleAuthority("USER"));
        authService.setAutorities("/projects/{project_id}@GET", new RoleAuthority("USER"));
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Data Not Found")
    public void dataNotFound() {
    }

    @ExceptionHandler(AlreadyExistingException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public void dataAlreadyExisting() {
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResourceAccess
    public @ResponseBody HttpEntity<List<Project>> retrieveProjectList() {
        List<Project> projects = projectService.retrieveProjectList();
        addLinksToProjects(projects);
        return new ResponseEntity<>(projects, HttpStatus.OK);
    }

    @RequestMapping(value = "", method = RequestMethod.POST, consumes = "application/json")
    @ResourceAccess
    public @ResponseBody HttpEntity<Project> createProject(@RequestParam Project newProject)
            throws AlreadyExistingException {
        Project project = projectService.createProject(newProject);
        addLinksToProject(project);
        return new ResponseEntity<>(project, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{project_id}")
    @ResourceAccess
    public @ResponseBody HttpEntity<Project> retrieveProject(@PathVariable("project_id") String projectId) {
        Project project = projectService.retrieveProject(projectId);
        addLinksToProject(project);
        return new ResponseEntity<>(project, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/{project_id}")
    @ResourceAccess
    public @ResponseBody HttpEntity<Project> modifyProject(@PathVariable("project_id") String projectId,
            @RequestParam("project") Project projectUpdated) {
        Project project = projectService.modifyProject(projectId, projectUpdated);
        addLinksToProject(project);
        return new ResponseEntity<>(project, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{project_id}")
    @ResourceAccess
    public @ResponseBody HttpEntity<List<Project>> deleteProject(@PathVariable("project_id") String projectId) {
        List<Project> projects = projectService.deleteProject(projectId);
        addLinksToProjects(projects);
        return new ResponseEntity<>(projects, HttpStatus.OK);
    }

    private void addLinksToProject(Project project) {
        ObjectMapper jacksonMapper = new ObjectMapper();
        try {
            System.out.println(jacksonMapper.writeValueAsString(project));
        }
        catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        project.add(linkTo(methodOn(ProjectController.class).retrieveProject(project.getName())).withSelfRel());
        // project.add(linkTo(methodOn(ProjectController.class).modifyProject(project.getName(), project))
        // .withRel("update"));
        project.add(linkTo(methodOn(ProjectController.class).deleteProject(project.getName())).withRel("delete"));
    }

    private void addLinksToProjects(List<Project> projects) {
        for (Project project : projects) {
            addLinksToProject(project);
        }
    }
}
