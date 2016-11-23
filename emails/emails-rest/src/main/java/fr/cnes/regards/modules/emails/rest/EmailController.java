/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.emails.rest;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.module.annotation.ModuleInfo;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.emails.domain.Email;
import fr.cnes.regards.modules.emails.service.IEmailService;

/**
 * Controller defining the REST entry points of the module
 *
 * @author Xavier-Alexandre Brochard
 *
 */
@RestController
@ModuleInfo(name = "emails", version = "1.0-SNAPSHOT", author = "REGARDS", legalOwner = "CS",
        documentation = "http://test")
@RequestMapping(value = "/emails")
public class EmailController {

    /**
     * The service responsible for handling CRUD and mailing operations
     */
    @Autowired
    private IEmailService emailService;

    /**
     * Define the endpoint for retrieving the list of sent emails
     *
     * @return A {@link List} of emails as {@link Email} wrapped in an {@link ResponseEntity}
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResourceAccess(description = "Retrieve all emails")
    public ResponseEntity<List<Email>> retrieveEmails() {
        final List<Email> emails = emailService.retrieveEmails();
        return new ResponseEntity<>(emails, HttpStatus.OK);
    }

    /**
     * Define the endpoint for sending an email to recipients
     *
     * @param pEmail
     *            The email in a simple representation.
     * @return The sent email as {@link Email} wrapped in an {@link ResponseEntity}
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    @ResourceAccess(description = "Send an email to recipients")
    public ResponseEntity<SimpleMailMessage> sendEmail(@Valid @RequestBody final SimpleMailMessage pMessage) {
        final SimpleMailMessage created = emailService.sendEmail(pMessage);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Define the endpoint for retrieving an email
     *
     * @param pId
     *            The email id
     * @return The email as a {@link Email} wrapped in an {@link ResponseEntity}
     */
    @RequestMapping(value = "/{mail_id}", method = RequestMethod.GET)
    @ResourceAccess(description = "Retrieve an email")
    public ResponseEntity<Email> retrieveEmail(@PathVariable("mail_id") final Long pId) {
        final Email email = emailService.retrieveEmail(pId);
        return new ResponseEntity<>(email, HttpStatus.OK);
    }

    /**
     * Define the endpoint for re-sending an email
     *
     * @param pId
     *            The email id
     * @return void
     */
    @RequestMapping(value = "/{mail_id}", method = RequestMethod.PUT)
    @ResourceAccess(description = "Send again an email")
    public void resendEmail(@PathVariable("mail_id") final Long pId) {
        emailService.resendEmail(pId);
    }

    /**
     * Define the endpoint for deleting an email
     *
     * @param pId
     *            The email id
     * @return void
     */
    @RequestMapping(value = "/{mail_id}", method = RequestMethod.DELETE)
    @ResourceAccess(description = "Delete an email")
    public void deleteEmail(@PathVariable("mail_id") final Long pId) {
        emailService.deleteEmail(pId);
    }

}