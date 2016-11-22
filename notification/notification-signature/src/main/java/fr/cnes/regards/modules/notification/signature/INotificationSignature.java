/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.notification.signature;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.modules.notification.domain.Notification;
import fr.cnes.regards.modules.notification.domain.NotificationSettings;
import fr.cnes.regards.modules.notification.domain.NotificationStatus;
import fr.cnes.regards.modules.notification.domain.dto.NotificationDTO;
import fr.cnes.regards.modules.notification.domain.dto.NotificationSettingsDTO;

/**
 * REST interface to define the entry points of the module.
 *
 * @author CS SI
 */
@RequestMapping("/notifications")
public interface INotificationSignature {

    /**
     * Define the endpoint for retrieving the list of notifications for the logged user
     *
     * @return A {@link List} of {@link Notification} wrapped in a {@link ResponseEntity}
     * @throws EntityNotFoundException
     *             thrown when no current user could be found
     */
    @RequestMapping(method = RequestMethod.GET)
    ResponseEntity<List<Notification>> retrieveNotifications() throws EntityNotFoundException;

    /**
     * Define the endpoint for creating a new notification in db for later sending by a scheluder.
     *
     * @param pDto
     *            A DTO for easy parsing of the response body. Mapping to true {@link Notification} is done in service.
     * @return The sent notification as {@link Notification} wrapped in a {@link ResponseEntity}
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<Notification> createNotification(NotificationDTO pDto);

    /**
     * Define the endpoint for retrieving a notification
     *
     * @param pId
     *            The notification <code>id</code>
     * @throws EntityNotFoundException
     *             Thrown when no notification with passed <code>id</code> could be found
     * @return The {@link Notification} wrapped in a {@link ResponseEntity}
     */
    @RequestMapping(value = "/{notification_id}", method = RequestMethod.GET)
    ResponseEntity<Notification> retrieveNotification(Long pId) throws EntityNotFoundException;

    /**
     * Define the endpoint for updating the {@link Notification#status}
     *
     * @param pId
     *            The notification <code>id</code>
     * @param pStatus
     *            The new <code>status</code>
     * @throws EntityNotFoundException
     *             Thrown when no notification with passed <code>id</code> could be found
     *
     */
    @ResponseBody
    @RequestMapping(value = "/{notification_id}", method = RequestMethod.PUT)
    void updateNotificationStatus(Long pId, NotificationStatus pStatus) throws EntityNotFoundException;

    /**
     * Define the endpoint for deleting a notification
     *
     * @param pId
     *            The notification <code>id</code>
     * @throws EntityNotFoundException
     *             Thrown when no notification with passed <code>id</code> could be found
     * @return
     */
    @RequestMapping(value = "/{notification_id}", method = RequestMethod.DELETE)
    void deleteNotification(Long pId) throws EntityNotFoundException;

    /**
     * Define the endpoint for retrieving the notification configuration parameters for the logged user
     *
     * @return The {@link NotificationSettings} wrapped in a {@link ResponseEntity}
     * @throws EntityNotFoundException
     *             thrown when no current user could be found
     */
    @RequestMapping(value = "/settings", method = RequestMethod.GET)
    ResponseEntity<NotificationSettings> retrieveNotificationSettings() throws EntityNotFoundException;

    /**
     * Define the endpoint for updating the {@link Notification#status}
     *
     * @param pDto
     *            The facade exposing user updatable fields of notification settings
     * @throws EntityNotFoundException
     *             Thrown when no notification settings with passed <code>id</code> could be found
     */
    @RequestMapping(value = "/settings", method = RequestMethod.PUT)
    void updateNotificationSettings(NotificationSettingsDTO pDto) throws EntityNotFoundException;

}
