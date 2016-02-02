package ch.unil.fcrepo4.spring.data.repository;

import ch.unil.fcrepo4.spring.data.core.FedoraTemplate;
import ch.unil.fcrepo4.spring.data.repository.config.EnableFedoraRepositories;
import org.fcrepo.client.FedoraException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modeshape.jcr.query.model.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.Date;

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
    public void testFindByMake() throws Exception {
        doAnswer(invocation -> {
            Query query = (Query) invocation.getArguments()[0];
            assertThat(query.toString())
                    .isEqualTo("SELECT * FROM [fedora:Resource] AS n WHERE (ISDESCENDANTNODE(n,'/vehicle') AND n.[test:make] = 'Ford\u0018^^\u0018http://www.w3.org/2001/XMLSchema#string')");
            return Collections.emptyList();
        }).when(mockFedoraTemplate).query(any(), any());
        vehicleRepo.findByMake("Ford");
    }

    @Test
    public void testFindByMilesGreaterThan() throws Exception {
        doAnswer(invocation -> {
            Query query = (Query) invocation.getArguments()[0];
            assertThat(query.toString())
                    .isEqualTo("SELECT * FROM [fedora:Resource] AS n WHERE (ISDESCENDANTNODE(n,'/vehicle') AND n.[test:miles] > '1000\u0018^^\u0018http://www.w3.org/2001/XMLSchema#int')");
            return Collections.emptyList();
        }).when(mockFedoraTemplate).query(any(), any());
        vehicleRepo.findByMilesGreaterThan(1000);
    }

    @Test
    public void testFindByColorLike() throws Exception {
        doAnswer(invocation -> {
            Query query = (Query) invocation.getArguments()[0];
            assertThat(query.toString())
                    .isEqualTo("SELECT * FROM [fedora:Resource] AS n WHERE (ISDESCENDANTNODE(n,'/vehicle') AND n.[test:color] LIKE '%green%')");
            return Collections.emptyList();
        }).when(mockFedoraTemplate).query(any(), any());
        vehicleRepo.findByColorLike("green");
    }

    @Test
    public void testFindByMakeAndColor() throws Exception {
        doAnswer(invocation -> {
            Query query = (Query) invocation.getArguments()[0];
            assertThat(query.toString())
                    .isEqualTo("SELECT * FROM [fedora:Resource] AS n WHERE (ISDESCENDANTNODE(n,'/vehicle') AND (n.[test:make] = 'Ford\u0018^^\u0018http://www.w3.org/2001/XMLSchema#string' AND n.[test:color] = 'green\u0018^^\u0018http://www.w3.org/2001/XMLSchema#string'))");
            return Collections.emptyList();
        }).when(mockFedoraTemplate).query(any(), any());
        vehicleRepo.findByMakeAndColor("Ford", "green");
    }

    @Test
    public void testFindByMakeOrColor() throws Exception {
        doAnswer(invocation -> {
            Query query = (Query) invocation.getArguments()[0];
            assertThat(query.toString())
                    .isEqualTo("SELECT * FROM [fedora:Resource] AS n WHERE (ISDESCENDANTNODE(n,'/vehicle') AND (n.[test:make] = 'Ford\u0018^^\u0018http://www.w3.org/2001/XMLSchema#string' OR n.[test:color] LIKE '%red%'))");
            return Collections.emptyList();
        }).when(mockFedoraTemplate).query(any(), any());
        vehicleRepo.findByMakeOrColorLike("Ford", "red");
    }

    @Test
    public void testFindByMakeAndMilesOrColorAndConsumption() throws Exception {
        doAnswer(invocation -> {
            Query query = (Query) invocation.getArguments()[0];
            assertThat(query.toString())
                    .isEqualTo("SELECT * FROM [fedora:Resource] AS n WHERE (ISDESCENDANTNODE(n,'/vehicle') AND ((n.[test:make] = 'Ford\u0018^^\u0018http://www.w3.org/2001/XMLSchema#string' AND n.[test:miles] = '1000\u0018^^\u0018http://www.w3.org/2001/XMLSchema#int') OR (n.[test:color] = 'red\u0018^^\u0018http://www.w3.org/2001/XMLSchema#string' AND n.[test:consumption] = '4.5\u0018^^\u0018http://www.w3.org/2001/XMLSchema#float')))");
            return Collections.emptyList();
        }).when(mockFedoraTemplate).query(any(), any());
        vehicleRepo.findByMakeAndMilesOrColorAndConsumption("Ford", 1000, "red", 4.5f);

    }

    @Test
    public void testFindByCreatedGreaterThen() throws Exception {
        doAnswer(invocation -> {
            Query query = (Query) invocation.getArguments()[0];
            assertThat(query.toString())
                    .isEqualTo("SELECT * FROM [fedora:Resource] AS n WHERE (ISDESCENDANTNODE(n,'/vehicle') AND n.[jcr:created] > CAST('1970-01-01T01:00:00.000+01:00' AS DATE))");
            return Collections.emptyList();
        }).when(mockFedoraTemplate).query(any(), any());
        vehicleRepo.findByCreatedGreaterThan(new Date(0L));

    }

}
