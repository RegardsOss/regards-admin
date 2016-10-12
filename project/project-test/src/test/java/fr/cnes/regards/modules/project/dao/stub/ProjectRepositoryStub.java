/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.project.dao.stub;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import fr.cnes.regards.microservices.core.test.repository.RepositoryStub;
import fr.cnes.regards.modules.project.dao.IProjectRepository;
import fr.cnes.regards.modules.project.domain.Project;

@Repository
@Profile("test")
@Primary
public class ProjectRepositoryStub extends RepositoryStub<Project> implements IProjectRepository {

    public ProjectRepositoryStub() {
        this.entities.add(new Project(0L, "desc", "icon", true, "name"));
    }

    @Override
    public Project findOneByName(final String pName) {
        return this.entities.stream().filter(e -> e.getName().equals(pName)).findFirst().get();
    }
}