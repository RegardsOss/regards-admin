/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.notification.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.cnes.regards.modules.accessrights.dao.projects.IProjectUserRepository;
import fr.cnes.regards.modules.accessrights.dao.projects.IRoleRepository;
import fr.cnes.regards.modules.accessrights.domain.projects.ProjectUser;
import fr.cnes.regards.modules.accessrights.domain.projects.Role;
import fr.cnes.regards.modules.accessrights.service.IProjectUserService;
import fr.cnes.regards.modules.core.exception.EntityNotFoundException;
import fr.cnes.regards.modules.notification.dao.INotificationRepository;
import fr.cnes.regards.modules.notification.domain.Notification;
import fr.cnes.regards.modules.notification.domain.NotificationStatus;
import fr.cnes.regards.modules.notification.domain.dto.NotificationDTO;

/**
 * {@link INotificationService} implementation
 *
 * @author CS SI
 *
 */
@Service
@Transactional
public class NotificationService implements INotificationService {

    /**
     * CRUD repository managing notifications. Autowired by Spring.
     */
    private final INotificationRepository notificationRepository;

    /**
     * Service handle CRUD operations on {@link ProjectUser}s. Autowired by Spring.
     */
    private final IProjectUserService projectUserService;

    /**
     * CRUD repository managing project users. Autowired by Spring.
     */
    private final IProjectUserRepository projectUserRepository;

    /**
     * CRUD repository managing roles. Autowired by Spring.
     */
    private final IRoleRepository roleRepository;

    /**
     * Creates a {@link NotificationService} wired to the given {@link INotificationRepository}.
     *
     * @param pProjectUserService
     *            Autowired by Spring. Must not be {@literal null}.
     * @param pNotificationRepository
     *            Autowired by Spring. Must not be {@literal null}.
     * @param pProjectUserRepository
     *            Autowired by Spring. Must not be {@literal null}.
     * @param pRoleRepository
     *            Autowired by Spring. Must not be {@literal null}.
     */
    public NotificationService(final IProjectUserService pProjectUserService,
            final INotificationRepository pNotificationRepository, final IProjectUserRepository pProjectUserRepository,
            final IRoleRepository pRoleRepository) {
        super();
        projectUserService = pProjectUserService;
        notificationRepository = pNotificationRepository;
        projectUserRepository = pProjectUserRepository;
        roleRepository = pRoleRepository;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.notification.service.INotificationService#retrieveNotifications()
     */
    @Override
    public List<Notification> retrieveNotifications() {
        final ProjectUser projectUser = projectUserService.retrieveCurrentUser();
        final Role role = projectUser.getRole();
        return notificationRepository.findByRecipientsContaining(projectUser, role);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.notification.service.INotificationService#sendNotification(fr.cnes.regards.modules.
     * notification.domain.NotificationDTO)
     */
    @Override
    public Notification createNotification(final NotificationDTO pDto) {
        final Notification notification = new Notification();
        notification.setDate(LocalDateTime.now());
        notification.setMessage(pDto.getMessage());
        notification.setTitle(pDto.getTitle());
        notification.setSender(pDto.getSender());
        notification.setStatus(NotificationStatus.UNREAD);

        final List<ProjectUser> projectUserRecipients = projectUserRepository
                .findByEmailIn(pDto.getProjectUserRecipients());
        notification.setProjectUserRecipients(projectUserRecipients);

        final List<Role> roleRecipients = roleRepository.findByNameIn(pDto.getRoleRecipients());
        notification.setRoleRecipients(roleRecipients);

        // Save it in db
        notificationRepository.save(notification);

        // TODO Trigger NOTIFICATION event on message broker

        return notification;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.notification.service.INotificationService#retrieveNotification(java.lang.Long)
     */
    @Override
    public Notification retrieveNotification(final Long pId) throws EntityNotFoundException {
        if (!notificationRepository.exists(pId)) {
            throw new EntityNotFoundException(pId.toString(), Notification.class);
        }
        return notificationRepository.findOne(pId);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.notification.service.INotificationService#updateNotificationStatus(java.lang.Long)
     */
    @Override
    public void updateNotificationStatus(final Long pId, final NotificationStatus pStatus)
            throws EntityNotFoundException {
        if (!notificationRepository.exists(pId)) {
            throw new EntityNotFoundException(pId.toString(), Notification.class);
        }
        final Notification notification = notificationRepository.findOne(pId);
        notification.setStatus(pStatus);
        notificationRepository.save(notification);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.notification.service.INotificationService#deleteNotification(java.lang.Long)
     */
    @Override
    public void deleteNotification(final Long pId) throws EntityNotFoundException {
        if (!notificationRepository.exists(pId)) {
            throw new EntityNotFoundException(pId.toString(), Notification.class);
        }
        notificationRepository.delete(pId);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.notification.service.INotificationService#retrieveNotificationsToSend()
     */
    @Override
    public List<Notification> retrieveNotificationsToSend() {
        return notificationRepository.findByStatus(NotificationStatus.UNREAD);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.cnes.regards.modules.notification.service.INotificationService#assembleRecipients(fr.cnes.regards.modules.
     * notification.domain.Notification)
     */
    @Override
    public Stream<ProjectUser> findRecipients(final Notification pNotification) {
        try (final Stream<Role> rolesStream = pNotification.getRoleRecipients().parallelStream();
                final Stream<ProjectUser> usersStream = pNotification.getProjectUserRecipients().parallelStream()) {
            return Stream.concat(usersStream, rolesStream.flatMap(r -> r.getProjectUsers().stream())).distinct();
        }
    }

}