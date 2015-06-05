package ch.unil.fcrepo4.spring.data.core;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.FedoraObject;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Uuid;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.impl.FedoraObjectImpl;
import org.fcrepo.client.utils.HttpHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author gushakov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {FedoraTemplateTest.TestConfig.class})
public class FedoraTemplateTest {

    @Configuration
    static class TestConfig {

        @Bean
        public FedoraRepository mockFedoraRepository() throws FedoraException {
            FedoraRepository mockRepository = mock(FedoraRepository.class);
            HttpHelper mockHelper = mock(HttpHelper.class);
            when(mockRepository.getRepositoryUrl()).thenReturn("http://localhost:9090/rest");
            when(mockRepository.findOrCreateObject(anyString()))
                    .thenAnswer(invocation -> new FedoraObjectImpl(mockRepository, mockHelper, (String) invocation.getArguments()[0]));
            return mockRepository;
        }

        @Bean
        public FedoraTemplate fedoraTemplate() throws FedoraException {
            return new FedoraTemplate(mockFedoraRepository());
        }

    }

    @Autowired
    private FedoraRepository mockRepository;

    @Autowired
    private FedoraTemplate template;

    @FedoraObject
    static class Bean1 {
        @Uuid
        String uuid = "foo-bar";
    }

    @Test
    public void testSave() throws Exception {
        template.save(new Bean1());
        verify(mockRepository).findOrCreateObject("/test/foo/bar");
    }

}
