/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessrights.domain.projects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.modules.accessrights.domain.HttpVerb;

@Entity(name = "T_RESOURCES_ACCESS")
@SequenceGenerator(name = "resourcesAccessSequence", initialValue = 1, sequenceName = "SEQ_RESOURCES_ACCESS")
public class ResourcesAccess implements IIdentifiable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "resourcesAccessSequence")
    @Column(name = "id")
    private Long id;

    @Column(name = "description")
    private String description;

    @NotBlank
    @Column(name = "microservice")
    private String microservice;

    @NotBlank
    @Column(name = "resource")
    private String resource;

    @NotNull
    @Column(name = "verb")
    @Enumerated(EnumType.STRING)
    private HttpVerb verb;

    public ResourcesAccess() {
        super();
        verb = HttpVerb.GET;
    }

    public ResourcesAccess(final Long pResourcesAccessId) {
        this();
        id = pResourcesAccessId;
    }

    public ResourcesAccess(final Long pResourcesAccessId, final String pDescription, final String pMicroservice,
            final String pResource, final HttpVerb pVerb) {
        super();
        id = pResourcesAccessId;
        description = pDescription;
        microservice = pMicroservice;
        resource = pResource;
        verb = pVerb;
    }

    public ResourcesAccess(final String pDescription, final String pMicroservice, final String pResource,
            final HttpVerb pVerb) {
        super();
        description = pDescription;
        microservice = pMicroservice;
        resource = pResource;
        verb = pVerb;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(final Long pId) {
        id = pId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String pDescription) {
        description = pDescription;
    }

    public String getMicroservice() {
        return microservice;
    }

    public void setMicroservice(final String pMicroservice) {
        microservice = pMicroservice;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(final String pResource) {
        resource = pResource;
    }

    public HttpVerb getVerb() {
        return verb;
    }

    public void setVerb(final HttpVerb pVerb) {
        verb = pVerb;
    }

}
