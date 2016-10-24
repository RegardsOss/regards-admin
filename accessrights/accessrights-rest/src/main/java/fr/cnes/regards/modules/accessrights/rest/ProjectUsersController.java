/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessrights.rest;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.security.utils.endpoint.annotation.ResourceAccess;
import fr.cnes.regards.modules.accessrights.domain.Couple;
import fr.cnes.regards.modules.accessrights.domain.projects.MetaData;
import fr.cnes.regards.modules.accessrights.domain.projects.ProjectUser;
import fr.cnes.regards.modules.accessrights.domain.projects.ResourcesAccess;
import fr.cnes.regards.modules.accessrights.domain.projects.Role;
import fr.cnes.regards.modules.accessrights.service.IProjectUserService;
import fr.cnes.regards.modules.accessrights.signature.IProjectUsersSignature;
import fr.cnes.regards.modules.core.annotation.ModuleInfo;
import fr.cnes.regards.modules.core.exception.EntityNotFoundException;
import fr.cnes.regards.modules.core.exception.InvalidValueException;
import fr.cnes.regards.modules.core.rest.Controller;

/**
 *
 * Controller responsible for the /users(/*)? endpoints
 *
 * @author svissier
 *
 */
@RestController
@ModuleInfo(name = "accessrights", version = "1.0-SNAPSHOT", author = "REGARDS", legalOwner = "CS",
        documentation = "http://test")
public class ProjectUsersController extends Controller implements IProjectUsersSignature {

    @Autowired
    private IProjectUserService projectUserService;

    @Override
    @ResourceAccess(description = "retrieve the list of users of the project", name = "")
    public HttpEntity<List<Resource<ProjectUser>>> retrieveProjectUserList() {
        final List<ProjectUser> users = projectUserService.retrieveUserList();
        final List<Resource<ProjectUser>> resources = users.stream().map(u -> new Resource<>(u))
                .collect(Collectors.toList());
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    @Override
    @ResourceAccess(description = "retrieve the project user and only display  metadata")
    public HttpEntity<Resource<ProjectUser>> retrieveProjectUser(@PathVariable("user_id") final Long userId) {
        final ProjectUser user = projectUserService.retrieveUser(userId);
        final Resource<ProjectUser> resource = new Resource<ProjectUser>(user);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    @Override
    @ResourceAccess(description = "update the project user")
    public HttpEntity<Void> updateProjectUser(@PathVariable("user_id") final Long userId,
            @RequestBody final ProjectUser pUpdatedProjectUser) throws InvalidValueException, EntityNotFoundException {
        projectUserService.updateUser(userId, pUpdatedProjectUser);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    @ResourceAccess(description = "remove the project user from the instance")
    public HttpEntity<Void> removeProjectUser(@PathVariable("user_id") final Long userId) {
        projectUserService.removeUser(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    @ResourceAccess(description = "retrieve the list of all metadata of the user")
    public HttpEntity<List<Resource<MetaData>>> retrieveProjectUserMetaData(@PathVariable("user_id") final Long pUserId)
            throws EntityNotFoundException {
        final List<MetaData> metaDatas = projectUserService.retrieveUserMetaData(pUserId);
        final List<Resource<MetaData>> resources = metaDatas.stream().map(m -> new Resource<>(m))
                .collect(Collectors.toList());
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    @Override
    @ResourceAccess(description = "update the list of all metadata of the user")
    public HttpEntity<Void> updateProjectUserMetaData(@PathVariable("user_id") final Long userId,
            @Valid @RequestBody final List<MetaData> pUpdatedUserMetaData) throws EntityNotFoundException {
        projectUserService.updateUserMetaData(userId, pUpdatedUserMetaData);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    @ResourceAccess(description = "remove all the metadata of the user")
    public HttpEntity<Void> removeProjectUserMetaData(@PathVariable("user_id") final Long userId)
            throws EntityNotFoundException {
        projectUserService.removeUserMetaData(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    @ResourceAccess(description = "retrieve the list of specific access rights and the role of the project user")
    public HttpEntity<Resource<Couple<List<ResourcesAccess>, Role>>> retrieveProjectUserAccessRights(
            @PathVariable("user_login") final String pUserLogin,
            @RequestParam(value = "borrowedRoleName", required = false) final String pBorrowedRoleName)
            throws InvalidValueException {
        final Couple<List<ResourcesAccess>, Role> couple = projectUserService
                .retrieveProjectUserAccessRights(pUserLogin, pBorrowedRoleName);
        final Resource<Couple<List<ResourcesAccess>, Role>> resource = new Resource<>(couple);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    @Override
    @ResourceAccess(description = "update the list of specific user access rights")
    public HttpEntity<Void> updateProjectUserAccessRights(@PathVariable("user_login") final String pLogin,
            @Valid @RequestBody final List<ResourcesAccess> pUpdatedUserAccessRights) throws EntityNotFoundException {
        projectUserService.updateUserAccessRights(pLogin, pUpdatedUserAccessRights);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    @ResourceAccess(description = "remove all the specific access rights")
    public @ResponseBody HttpEntity<Void> removeProjectUserAccessRights(
            @PathVariable("user_login") final String pUserLogin) throws EntityNotFoundException {
        projectUserService.removeUserAccessRights(pUserLogin);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}