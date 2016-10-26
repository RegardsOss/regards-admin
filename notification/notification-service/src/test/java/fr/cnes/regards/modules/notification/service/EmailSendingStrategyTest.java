/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.notification.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mail.SimpleMailMessage;

import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.emails.client.IEmailClient;
import fr.cnes.regards.modules.notification.domain.Notification;

/**
 * Test class for {@link EmailService}.
 *
 * @author Xavier-Alexandre Brochard
 */
public class EmailSendingStrategyTest {

    /**
     * The notification's sender
     */
    private static final String SENDER = "Sender";

    /**
     * The recipients
     */
    private static final String[] RECIPIENTS = { "recipient0", "recipient1" };

    /**
     * The message
     */
    private static final String MESSAGE = "Message";

    /**
     * Feign client from module Email
     */
    private IEmailClient emailClient;

    /**
     * Tested class
     */
    private ISendingStrategy strategy;

    /**
     * Sent notification
     */
    private Notification notification;

    /**
     * Do some setup before each test
     */
    @Before
    public void setUp() {
        // Mock
        emailClient = Mockito.mock(IEmailClient.class);

        // Instanciate the tested class
        strategy = new EmailSendingStrategy(emailClient);

        // Define the sent notification
        notification = new Notification();
        notification.setId(0L);
        notification.setMessage(MESSAGE);
        notification.setSender(SENDER);
    }

    /**
     * Check that the system allows te send notifications as email through the email feign client.
     */
    @Test
    @Requirement("?")
    @Purpose("Check that the system allows te send notifications as email through the email feign client.")
    public void send() {
        // Define expected mail
        final SimpleMailMessage expected = new SimpleMailMessage();
        expected.setFrom(SENDER);
        expected.setText(MESSAGE);
        expected.setTo(RECIPIENTS);

        // Call the tested method
        strategy.send(notification, RECIPIENTS);

        // // Verify method call.
        Mockito.verify(emailClient, Mockito.times(1)).sendEmail(Mockito.refEq(expected, "sentDate"));
    }

}