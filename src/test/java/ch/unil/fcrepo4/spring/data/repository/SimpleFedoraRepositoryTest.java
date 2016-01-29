package ch.unil.fcrepo4.spring.data.repository;

import ch.unil.fcrepo4.spring.data.core.FedoraTemplate;
import ch.unil.fcrepo4.spring.data.repository.config.EnableFedoraRepositories;
import org.assertj.core.api.Assertions;
import org.fcrepo.client.FedoraException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modeshape.jcr.query.model.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

/**
 * @author gushakov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleFedoraRepositoryTest.TestConfig.class})
public class SimpleFedoraRepositoryTest {

    @Configuration
    @PropertySource("classpath:fcrepo4.properties")
    @EnableFedoraRepositories
    public static class TestConfig {

        @Bean
        public FedoraTemplate fedoraTemplate() throws FedoraException {
            return spy(new FedoraTemplate("anything", 1));
        }

    }

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private VehicleCrudRepository vehicleRepo;

    @Autowired
    private FedoraTemplate mockFedoraTemplate;

    @Test
    public void testQuerySimplePropertyStringValue() throws Exception {

        doAnswer(invocation -> {
            Query query = (Query) invocation.getArguments()[0];
            assertThat(query.toString())
                    .isEqualTo("SELECT * FROM [fedora:Resource] AS n WHERE (ISDESCENDANTNODE(n,'/vehicle') AND n.[test:make] = 'Ford\30^^\30http://www.w3.org/2001/XMLSchema#string')");
            return Collections.emptyList();
        }).when(mockFedoraTemplate).query(any(), any());

        vehicleRepo.findByMake("Ford");

    }

    @Test
    public void testQueryLikeStringValue() throws Exception {

        doAnswer(invocation -> {
            Query query = (Query) invocation.getArguments()[0];
            assertThat(query.toString())
                    .isEqualTo("SELECT * FROM [fedora:Resource] AS n WHERE (ISDESCENDANTNODE(n,'/vehicle') AND CONTAINS(n.[test:color],'green'))");
            return Collections.emptyList();
        }).when(mockFedoraTemplate).query(any(), any());

        vehicleRepo.findByColorLike("green");

    }

}
