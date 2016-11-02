/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.microservices.administration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;

import fr.cnes.regards.framework.security.domain.ResourceMapping;
import fr.cnes.regards.framework.security.endpoint.IAuthoritiesProvider;
import fr.cnes.regards.modules.accessrights.client.IResourcesClient;
import fr.cnes.regards.modules.accessrights.client.IRolesClient;
import fr.cnes.regards.modules.accessrights.domain.projects.ResourcesAccess;
import fr.cnes.regards.modules.accessrights.domain.projects.Role;

/**
 *
 * Class MicroserviceAuthoritiesProvider
 *
 * IAuthoritiesProvider implementation for all microservices exception administration.
 *
 * @author sbinda
 * @since 1.0-SNAPSHOT
 */
public class MicroserviceAuthoritiesProvider implements IAuthoritiesProvider {

    /**
     * Administration microservice REST client
     */
    private final IResourcesClient resourcesClient;

    /**
     * Administration microservice REST client
     */
    private final IRolesClient roleClient;

    /**
     *
     * Constructor
     *
     * @param pRoleClient
     *            Feign client to query administration service for roles
     * @param pResourcesClient
     *            Feign client to query administration service for resources
     * @since 1.0-SNAPSHOT
     */
    public MicroserviceAuthoritiesProvider(final IRolesClient pRoleClient, final IResourcesClient pResourcesClient) {
        super();
        resourcesClient = pResourcesClient;
        roleClient = pRoleClient;
    }

    @Override
    public List<ResourceMapping> getResourcesAccessConfiguration() {
        final List<ResourceMapping> resourcesMapping = new ArrayList<>();
        final ResponseEntity<List<Resource<ResourcesAccess>>> results = resourcesClient.getResourceAccessList();
        if (results.getStatusCode().equals(HttpStatus.OK)) {
            final List<ResourcesAccess> resources = new ArrayList<>();
            results.getBody().forEach(resource -> resources.add(resource.getContent()));
            for (final ResourcesAccess resource : resources) {
                final ResourceMapping mapping = new ResourceMapping(resource.getResource(),
                        RequestMethod.valueOf(resource.getVerb().toString()));
                resourcesMapping.add(mapping);
            }
        }
        return resourcesMapping;
    }

    @Override
    public List<String> getRoleAuthorizedAddress(final String pRole) {

        final List<String> addresses = new ArrayList<>();
        final ResponseEntity<Resource<Role>> result = roleClient.retrieveRole(pRole);
        if (result.getStatusCode().equals(HttpStatus.OK)) {
            final Resource<Role> body = result.getBody();
            if (body != null) {
                addresses.addAll(body.getContent().getAuthorizedAddresses());
            }
        }
        return addresses;
    }

    @Override
    public boolean hasCorsRequestsAccess(final String pRole) {
        boolean access = false;
        final ResponseEntity<Resource<Role>> result = roleClient.retrieveRole(pRole);
        if (result.getStatusCode().equals(HttpStatus.OK)) {
            final Resource<Role> body = result.getBody();
            if (body != null) {
                access = body.getContent().isCorsRequestsAuthorized();
            }
        }
        return access;
    }

}