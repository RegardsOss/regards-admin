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
package fr.cnes.regards.modules.accessrights.instance.service.workflow.listener;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mail.SimpleMailMessage;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.modules.accessrights.instance.domain.Account;
import fr.cnes.regards.modules.accessrights.instance.service.workflow.events.OnRefuseAccountEvent;
import fr.cnes.regards.modules.accessrights.instance.service.workflow.listeners.SendAccountRefusedEmailListener;
import fr.cnes.regards.modules.emails.client.IEmailClient;
import fr.cnes.regards.modules.templates.service.ITemplateService;

/**
 *
 * @author Xavier-Alexandre Brochard
 */
public class SendAccountRefusedEmailListenerTest {

    /**
     * Test method for {@link SendAccountRefusedEmailListener#onApplicationEvent(OnRefuseAccountEvent)}.
     * @throws EntityNotFoundException
     */
    @SuppressWarnings("unchecked")
    @Test
    public final void testOnApplicationEvent_templateNotFound() throws EntityNotFoundException {
        Account account = new Account("email@test.com", "firstname", "lastname", "password");
        OnRefuseAccountEvent event = new OnRefuseAccountEvent(account);

        ITemplateService templateService = Mockito.mock(ITemplateService.class);
        IEmailClient emailClient = Mockito.mock(IEmailClient.class);
        Mockito.when(templateService.writeToEmail(Mockito.anyString(), Mockito.anyMap(), Mockito.any()))
                .thenThrow(EntityNotFoundException.class);

        SendAccountRefusedEmailListener listener = new SendAccountRefusedEmailListener(templateService, emailClient);
        listener.onApplicationEvent(event);

        Mockito.verify(emailClient).sendEmail(Mockito.any());
    }

    /**
     * Test method for {@link SendAccountRefusedEmailListener#onApplicationEvent(OnRefuseAccountEvent)}.
     * @throws EntityNotFoundException
     */
    @SuppressWarnings("unchecked")
    @Test
    public final void testOnApplicationEvent() throws EntityNotFoundException {
        Account account = new Account("email@test.com", "firstname", "lastname", "password");
        OnRefuseAccountEvent event = new OnRefuseAccountEvent(account);

        ITemplateService templateService = Mockito.mock(ITemplateService.class);
        IEmailClient emailClient = Mockito.mock(IEmailClient.class);
        Mockito.when(templateService.writeToEmail(Mockito.anyString(), Mockito.anyMap(), Mockito.any()))
                .thenReturn(new SimpleMailMessage());

        SendAccountRefusedEmailListener listener = new SendAccountRefusedEmailListener(templateService, emailClient);
        listener.onApplicationEvent(event);

        Mockito.verify(emailClient).sendEmail(Mockito.any());
    }

}