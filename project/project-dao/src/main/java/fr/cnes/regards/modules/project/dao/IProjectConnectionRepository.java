/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.project.dao;

import org.springframework.data.repository.CrudRepository;

import fr.cnes.regards.domain.annotation.InstanceEntity;
import fr.cnes.regards.modules.project.domain.ProjectConnection;

/**
 *
 * Class IProjectConnectionRepository
 *
 * JPA Repository to access ProjectConnection entities.
 *
 * @author CS
 * @since 1.0-SNAPSHOT
 */
@InstanceEntity
public interface IProjectConnectionRepository extends CrudRepository<ProjectConnection, Long> {

    ProjectConnection findOneByProjectNameAndMicroservice(final String pProjectName, final String pMicroService);

}