/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.notification.dao;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import fr.cnes.regards.modules.notification.domain.Notification;
import fr.cnes.regards.modules.notification.domain.NotificationStatus;

/**
 * Interface for an JPA auto-generated CRUD repository managing Notifications.<br>
 * Embeds paging/sorting abilities by entending {@link PagingAndSortingRepository}.<br>
 * Allows execution of Query by Example {@link Example} instances.
 *
 * @author Xavier-Alexandre Brochard
 */
public interface INotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find all notifications having the passed project user or the passed role as recipient.
     *
     * @param projectUser The required project user recipient
     * @param role The required role recipient
     * @return The list of found notifications
     */
    @Query("select distinct n from Notification n left join n.projectUserRecipients p left join n.roleRecipients r where p = ?1 or r = ?2")
    List<Notification> findByRecipientsContaining(String projectUser, String role);

    /**
     * Find all notifications with passed <code>status</code>
     *
     * @param pStatus
     *            The notification status
     * @return The list of notifications
     */
    List<Notification> findByStatus(NotificationStatus pStatus);

    /**
     * Find all notifications which recipients contains the given user, represented by its email
     * @param email
     * @return all notifications which recipients contains the given user, represented by its email
     */
    Set<Notification> findAllByProjectUserRecipientsContaining(String email);

    /**
     * Find all notifications which recipients contains the given role, represented by its name
     * @param role
     * @return all notifications which recipients contains the given role, represented by its name
     */
    Set<Notification> findAllByRoleRecipientsContaining(String role);
}
