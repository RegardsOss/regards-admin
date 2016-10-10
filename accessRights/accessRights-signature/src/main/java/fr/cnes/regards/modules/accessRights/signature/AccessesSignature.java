/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessRights.signature;

import java.util.List;

import javax.naming.OperationNotSupportedException;
import javax.validation.Valid;

import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import fr.cnes.regards.modules.accessRights.domain.AccessRequestDTO;
import fr.cnes.regards.modules.accessRights.domain.projects.ProjectUser;
import fr.cnes.regards.modules.core.exception.AlreadyExistingException;
import fr.cnes.regards.modules.core.exception.InvalidValueException;

public interface AccessesSignature {

    @RequestMapping(value = "/accesses", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    HttpEntity<List<Resource<ProjectUser>>> retrieveAccessRequestList();

    @RequestMapping(value = "/accesses", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    HttpEntity<Resource<AccessRequestDTO>> requestAccess(@Valid @RequestBody AccessRequestDTO pAccessRequest)
            throws AlreadyExistingException;

    @RequestMapping(value = "/accesses/{access_id}/accept", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    HttpEntity<Void> acceptAccessRequest(@PathVariable("access_id") Long pAccessId)
            throws OperationNotSupportedException;

    @RequestMapping(value = "/accesses/{access_id}/deny", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    HttpEntity<Void> denyAccessRequest(@PathVariable("access_id") Long pAccessId) throws OperationNotSupportedException;

    @RequestMapping(value = "/accesses/{access_id}", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    HttpEntity<Void> removeAccessRequest(@PathVariable("access_id") Long pAccessId);

    @RequestMapping(value = "/accesses/settings", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    HttpEntity<List<Resource<String>>> getAccessSettingList();

    @RequestMapping(value = "/accesses/settings", method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    HttpEntity<Void> updateAccessSetting(@Valid @RequestBody String pUpdatedProjectUserSetting)
            throws InvalidValueException;
}
