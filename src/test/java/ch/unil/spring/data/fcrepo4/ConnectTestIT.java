package ch.unil.spring.data.fcrepo4;

import org.assertj.core.api.Assertions;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.impl.FedoraObjectImpl;
import org.fcrepo.client.impl.ReadOnlyFedoraRepositoryImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author gushakov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ConnectTestIT {

    @Configuration
    static class TestConfig {

    }

    @Test
    public void testConnect() throws Exception {
       // read an existing object from Fedora
        FedoraRepository repository = new ReadOnlyFedoraRepositoryImpl("http://localhost:9090");
        FedoraObject fo = new FedoraObjectImpl(repository, null, "rest/ef/72/ee/79/ef72ee79-8a7f-4a8d-a4fb-1d1a63c26258");
        assertThat(fo).isNotNull();
    }

}
