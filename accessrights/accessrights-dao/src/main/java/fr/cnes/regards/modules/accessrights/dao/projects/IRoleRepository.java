/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessrights.dao.projects;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Example;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import fr.cnes.regards.modules.accessrights.domain.projects.Role;

/**
 * Interface for a JPA auto-generated CRUD repository managing {@link Role}s.<br>
 * Embeds paging/sorting abilities by entending {@link PagingAndSortingRepository}.<br>
 * Allows execution of Query by Example {@link Example} instances.
 *
 * TODO: Switch to JpaRepository instead of CrudRepository
 *
 * @author CS SI
 */
public interface IRoleRepository extends CrudRepository<Role, Long> {

    /**
     * Find the unique {@link Role} where <code>default</code> equal to passed boolean.<br>
     * Custom query auto-implemented by JPA thanks to the method naming convention.
     *
     * @param pIsDefault
     *            <code>True</code> or <code>False</code>
     * @return The found {@link Role}
     */
    Role findByIsDefault(boolean pIsDefault);

    /**
     * Find the unique {@link Role}s where <code>name</code> equal to passed name.<br>
     * Custom query auto-implemented by JPA thanks to the method naming convention.
     *
     * @param pName
     *            The <code>name</code>
     * @return The found {@link Role}
     */
    Role findOneByName(String pName);

    /**
     * Find the all {@link Role}s where <code>name</code> is in passed collection.<br>
     * Custom query auto-implemented by JPA thanks to the method naming convention.
     *
     * @param pNames
     *            The {@link Collection} of <code>name</code>
     * @return The {@link List} of found {@link Role}s
     */
    List<Role> findByNameIn(Collection<String> pNames);
}
