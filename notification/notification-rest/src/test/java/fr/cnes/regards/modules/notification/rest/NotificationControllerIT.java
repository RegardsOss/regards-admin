package fr.cnes.regards.modules.notification.rest;

import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.cnes.regards.framework.hateoas.HateoasUtils;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.jpa.utils.RegardsTransactional;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.security.role.DefaultRole;
import fr.cnes.regards.framework.test.integration.AbstractRegardsIT;
import fr.cnes.regards.modules.accessrights.client.IProjectUsersClient;
import fr.cnes.regards.modules.accessrights.client.IRolesClient;
import fr.cnes.regards.modules.accessrights.dao.projects.IProjectUserRepository;
import fr.cnes.regards.modules.accessrights.domain.projects.ProjectUser;
import fr.cnes.regards.modules.accessrights.domain.projects.Role;
import fr.cnes.regards.modules.accessrights.service.projectuser.ProjectUserService;
import fr.cnes.regards.modules.accessrights.service.role.IRoleService;
import fr.cnes.regards.modules.accessrights.service.role.RoleService;
import fr.cnes.regards.modules.emails.client.IEmailClient;
import fr.cnes.regards.modules.notification.domain.dto.NotificationDTO;
import fr.cnes.regards.modules.notification.service.SendingScheduler;

/**
 * @author Sylvain VISSIERE-GUERINET
 */
@TestPropertySource(locations = "classpath:application.properties")
@MultitenantTransactional
public class NotificationControllerIT extends AbstractRegardsIT {

    @Autowired
    private SendingScheduler sendingScheduler;

    @Autowired
    private IRuntimeTenantResolver runtimeTenantResolver;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private IProjectUserRepository projectUserRepository;

    @Configuration
    static class Conf {

        @Bean
        public IProjectUsersClient projectUsersClient() {
            return Mockito.mock(IProjectUsersClient.class);
        }

        @Bean
        public IEmailClient emailClient() {
            return Mockito.mock(IEmailClient.class);
        }

    }

    @Autowired
    private IProjectUsersClient projectUsersClient;

    @Override
    protected Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }

    @Test
    public void testCreateNotification() throws EntityNotFoundException {
        String roleName=DefaultRole.PROJECT_ADMIN.name();
        Role pa=roleService.retrieveRole(roleName);
        ProjectUser pu=projectUserRepository.save(new ProjectUser("project.admin@test.fr", pa, Lists.newArrayList(), Lists.newArrayList()));
        Mockito.when(projectUsersClient.retrieveRoleProjectUserList(pa.getId(), 0, 100)).thenReturn(new ResponseEntity<PagedResources<Resource<ProjectUser>>>(
                HateoasUtils.wrapToPagedResources(
                        Lists.newArrayList(pu)),
                HttpStatus.OK));
        NotificationDTO notif = new NotificationDTO("Lets test", Lists.newArrayList(),
                                                    Lists.newArrayList(roleName),
                                                    "microservice", "test");

        List<ResultMatcher> expectations = Lists.newArrayList();
        expectations.add(MockMvcResultMatchers.status().isCreated());

        performDefaultPost(NotificationController.NOTIFICATION_PATH, notif, expectations, "error");
        runtimeTenantResolver.forceTenant(DEFAULT_TENANT);
        sendingScheduler.sendDaily();
    }
}
