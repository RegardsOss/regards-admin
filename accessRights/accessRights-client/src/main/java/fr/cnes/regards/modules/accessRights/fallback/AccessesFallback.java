/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessRights.fallback;

import java.util.List;

import javax.naming.OperationNotSupportedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;

import fr.cnes.regards.modules.accessRights.client.AccessesClient;
import fr.cnes.regards.modules.accessRights.domain.projects.ProjectUser;
import fr.cnes.regards.modules.core.exception.AlreadyExistingException;
import fr.cnes.regards.modules.core.exception.InvalidValueException;

@Component
public class AccessesFallback implements AccessesClient {

    private static final Logger LOG = LoggerFactory.getLogger(AccessesFallback.class);

    private static final String fallBackErrorMessage = "RS-ADMIN /accesses request error. Fallback.";

    @Override
    public HttpEntity<List<Resource<ProjectUser>>> retrieveAccessRequestList() {
        LOG.error(fallBackErrorMessage);
        return null;
    }

    @Override
    public HttpEntity<Resource<ProjectUser>> requestAccess(ProjectUser pAccessRequest) throws AlreadyExistingException {
        LOG.error(fallBackErrorMessage);
        return null;
    }

    @Override
    public HttpEntity<Void> acceptAccessRequest(Long pAccessId) throws OperationNotSupportedException {
        LOG.error(fallBackErrorMessage);
        return null;
    }

    @Override
    public HttpEntity<Void> denyAccessRequest(Long pAccessId) throws OperationNotSupportedException {
        LOG.error(fallBackErrorMessage);
        return null;
    }

    @Override
    public HttpEntity<Void> removeAccessRequest(Long pAccessId) {
        LOG.error(fallBackErrorMessage);
        return null;
    }

    @Override
    public HttpEntity<List<Resource<String>>> getAccessSettingList() {
        LOG.error(fallBackErrorMessage);
        return null;
    }

    @Override
    public HttpEntity<Void> updateAccessSetting(String pUpdatedProjectUserSetting) throws InvalidValueException {
        LOG.error(fallBackErrorMessage);
        return null;
    }

}