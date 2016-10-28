/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessrights.fallback;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import fr.cnes.regards.modules.accessrights.client.IRolesClient;
import fr.cnes.regards.modules.accessrights.domain.projects.ProjectUser;
import fr.cnes.regards.modules.accessrights.domain.projects.ResourcesAccess;
import fr.cnes.regards.modules.accessrights.domain.projects.Role;

/**
 *
 * Class RolesFallback
 *
 * Fallback for Roles Feign client. This implementation is used in case of error during feign client calls.
 *
 * @author CS
 * @since 1.0-SNAPSHOT
 */
@Component
public class RolesFallback implements IRolesClient {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProjectUsersFallback.class);

    /**
     * Common error message to log
     */
    private static final String fallBackErrorMessage = "RS-ADMIN /roles request error. Fallback.";

    @Override
    public ResponseEntity<List<Resource<Role>>> retrieveRoleList() {
        LOG.error(fallBackErrorMessage);
        return null;
    }

    @Override
    public ResponseEntity<Resource<Role>> createRole(final Role pNewRole) {
        LOG.error(fallBackErrorMessage);
        return null;
    }

    @Override
    public ResponseEntity<Resource<Role>> retrieveRole(final String pRoleName) {
        LOG.error(fallBackErrorMessage);
        return null;
    }

    @Override
    public ResponseEntity<Void> updateRole(final Long pRoleId, final Role pUpdatedRole) {
        LOG.error(fallBackErrorMessage);
        return null;
    }

    @Override
    public ResponseEntity<Void> removeRole(final Long pRoleId) {
        LOG.error(fallBackErrorMessage);
        return null;
    }

    @Override
    public ResponseEntity<List<Resource<ResourcesAccess>>> retrieveRoleResourcesAccessList(final Long pRoleId) {
        LOG.error(fallBackErrorMessage);
        return null;
    }

    @Override
    public ResponseEntity<Void> updateRoleResourcesAccess(final Long pRoleId,
            final List<ResourcesAccess> pResourcesAccessList) {
        LOG.error(fallBackErrorMessage);
        return null;
    }

    @Override
    public ResponseEntity<Void> clearRoleResourcesAccess(final Long pRoleId) {
        LOG.error(fallBackErrorMessage);
        return null;
    }

    @Override
    public ResponseEntity<List<Resource<ProjectUser>>> retrieveRoleProjectUserList(final Long pRoleId) {
        LOG.error(fallBackErrorMessage);
        return null;
    }

}
