/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.emails.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.emails.dao.IEmailRepository;
import fr.cnes.regards.modules.emails.domain.Email;

/**
 * An implementation of {@link IEmailService}
 *
 * @author Xavier-Alexandre Brochard
 * @author Christophe Mertz
 *
 */
@Service
public class EmailService implements IEmailService {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    /**
     * CRUD repository managing emails
     */
    private final IEmailRepository emailRepository;

    /**
     * Spring Framework interface for sending email
     */
    private final JavaMailSender mailSender;

    /**
     * Tenant resolver used to know current tenant.
     */
    private final IRuntimeTenantResolver runtimeTenantResolver;

    /**
     * Creates an {@link EmailService} wired to the given {@link IEmailRepository}.
     *
     * @param pEmailRepository
     *            Autowired by Spring. Must not be {@literal null}.
     * @param pMailSender
     *            Autowired by Spring. Must not be {@literal null}.
     */
    public EmailService(final IEmailRepository pEmailRepository, final JavaMailSender pMailSender,
            final IRuntimeTenantResolver pRuntimeTenantResolver) {
        super();
        emailRepository = pEmailRepository;
        mailSender = pMailSender;
        runtimeTenantResolver = pRuntimeTenantResolver;
    }

    @Override
    public List<Email> retrieveEmails() {
        final List<Email> emails = new ArrayList<>();
        final Iterable<Email> results = emailRepository.findAll();
        if (results != null) {
            results.forEach(emails::add);
        }
        return emails;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.cnes.regards.modules.emails.service.IEmailService#sendEmail(org.springframework.mail.SimpleMailMessage)
     */
    @Override
    public SimpleMailMessage sendEmail(final SimpleMailMessage pMessage) {
        // Create the savable DTO
        final Email email = createEmailFromSimpleMailMessage(pMessage);

        if (!runtimeTenantResolver.isInstance()) {
            // Persist in DB
            emailRepository.save(email);
        }

        final MimeMessage message = mailSender.createMimeMessage();
        try {
            final MimeMessageHelper helper = new MimeMessageHelper(message, false);
            helper.setText(pMessage.getText(), true);
            helper.setTo(pMessage.getTo());
            if (pMessage.getBcc() != null) {
                helper.setBcc(pMessage.getBcc());
            }
            if (pMessage.getCc() != null) {
                helper.setCc(pMessage.getCc());
            }
            if (pMessage.getFrom() != null) {
                helper.setFrom(pMessage.getFrom());
            }
            if (pMessage.getReplyTo() != null) {
                helper.setReplyTo(pMessage.getReplyTo());
            }
            if (pMessage.getSentDate() != null) {
                helper.setSentDate(pMessage.getSentDate());
            }
            if (pMessage.getSubject() != null) {
                helper.setSubject(pMessage.getSubject());
            }
            // Send the mail
            mailSender.send(message);
        } catch (final MessagingException e) {
            // TODO Throw error mail exception
            LOGGER.error("Error sending mail", e);
        }

        return pMessage;
    }

    @Override
    public Email retrieveEmail(final Long pId) throws ModuleException {
        final Email email = emailRepository.findOne(pId);
        if (email == null) {
            throw new EntityNotFoundException(pId, Email.class);
        }
        return email;
    }

    @Override
    public void resendEmail(final Long pId) throws ModuleException {
        final Email email = retrieveEmail(pId);
        final SimpleMailMessage message = createSimpleMailMessageFromEmail(email);
        mailSender.send(message);
    }

    @Override
    public void deleteEmail(final Long pId) {
        emailRepository.delete(pId);
    }

    @Override
    public boolean exists(final Long pId) {
        return emailRepository.exists(pId);
    }

    /**
     * Create a {@link SimpleMailMessage} with same content as the passed {@link Email}.
     *
     * @param pEmail
     *            The {@link Email}
     * @return The {@link SimpleMailMessage}
     */
    private SimpleMailMessage createSimpleMailMessageFromEmail(final Email pEmail) {
        final SimpleMailMessage message = new SimpleMailMessage();
        message.setBcc(pEmail.getBcc());
        message.setCc(pEmail.getCc());
        message.setFrom(pEmail.getFrom());
        message.setReplyTo(pEmail.getReplyTo());
        if (pEmail.getSentDate() != null) {
            message.setSentDate(Date.from(pEmail.getSentDate().atZone(ZoneId.systemDefault()).toInstant()));
        }
        message.setSubject(pEmail.getSubject());
        message.setText(pEmail.getText());
        message.setTo(pEmail.getTo());
        return message;
    }

    /**
     * Create a domain {@link Email} with same content as the passed {@link SimpleMailMessage} in order to save it in
     * db.
     *
     * @param pMessage
     *            The message
     * @return The savable email
     */
    private Email createEmailFromSimpleMailMessage(final SimpleMailMessage pMessage) {
        final Email email = new Email();
        email.setBcc(pMessage.getBcc());
        email.setCc(pMessage.getCc());
        email.setFrom(pMessage.getFrom());
        email.setReplyTo(pMessage.getReplyTo());
        if (pMessage.getSentDate() != null) {
            email.setSentDate(LocalDateTime.ofInstant(pMessage.getSentDate().toInstant(), ZoneId.systemDefault()));
        }
        email.setSubject(pMessage.getSubject());
        email.setText(pMessage.getText());
        email.setTo(pMessage.getTo());
        return email;
    }

}
