/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.accessRights.dao.stubs;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import fr.cnes.regards.framework.test.repository.RepositoryStub;
import fr.cnes.regards.modules.accessRights.dao.instance.IAccountRepository;
import fr.cnes.regards.modules.accessRights.domain.AccountStatus;
import fr.cnes.regards.modules.accessRights.domain.instance.Account;

@Repository
@Profile("test")
@Primary
public class AccountRepositoryStub extends RepositoryStub<Account> implements IAccountRepository {

    public AccountRepositoryStub() {
        entities.add(new Account(0L, "email@email.email", "firstName", "lastName", "flastname", "password",
                AccountStatus.ACCEPTED, "code"));
        entities.add(new Account(0L, "toto@toto.toto", "Toto", "toto", "tttoto", "mdp", AccountStatus.PENDING,
                "anotherCode"));
    }

    @Override
    public Account findOneByEmail(final String pEmail) {
        // TODO Auto-generated method stub
        return null;
    }
}
