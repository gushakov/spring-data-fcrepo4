package ch.unil.fcrepo4.spring.data.repository;

import ch.unil.fcrepo4.spring.data.core.FedoraTemplate;
import ch.unil.fcrepo4.spring.data.core.convert.rdf.ExtendedXsdDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.query.FedoraPageRequest;
import ch.unil.fcrepo4.spring.data.core.query.FedoraQuery;
import ch.unil.fcrepo4.spring.data.core.query.result.FedoraResultPage;
import ch.unil.fcrepo4.spring.data.repository.config.EnableFedoraRepositories;
import org.fcrepo.client.FedoraException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.PageImpl;
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

        @Bean
        public RdfDatatypeConverter rdfDatatypeConverter(){
            return new ExtendedXsdDatatypeConverter();
        }

    }

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private VehicleCrudRepository vehicleRepo;

    @Autowired
    private FedoraTemplate mockFedoraTemplate;

    @Autowired
    private RdfDatatypeConverter rdfDatatypeConverter;

    @Test
    public void testFindByMake() throws Exception {
        doAnswer(invocation -> {
            FedoraQuery query = (FedoraQuery) invocation.getArguments()[0];
            assertThat(query.toString())
                    .isEqualTo("SELECT * FROM [fedora:Resource] AS n WHERE (ISDESCENDANTNODE(n,'/vehicle') AND n.[test:make] = 'Ford\u0018^^\u0018http://www.w3.org/2001/XMLSchema#string')");
            return Collections.emptyList();
        }).when(mockFedoraTemplate).query(any(), any());
        vehicleRepo.findByMake("Ford");
    }

    @Test
    public void testFindByMilesGreaterThan() throws Exception {
        doAnswer(invocation -> {
            FedoraQuery query = (FedoraQuery) invocation.getArguments()[0];
            assertThat(query.toString())
                    .isEqualTo("SELECT * FROM [fedora:Resource] AS n WHERE (ISDESCENDANTNODE(n,'/vehicle') AND n.[test:miles] > '1000\u0018^^\u0018http://www.w3.org/2001/XMLSchema#int')");
            return Collections.emptyList();
        }).when(mockFedoraTemplate).query(any(), any());
        vehicleRepo.findByMilesGreaterThan(1000);
    }

    @Test
    public void testFindByColorLike() throws Exception {
        doAnswer(invocation -> {
            FedoraQuery query = (FedoraQuery) invocation.getArguments()[0];
            assertThat(query.toString())
                    .isEqualTo("SELECT * FROM [fedora:Resource] AS n WHERE (ISDESCENDANTNODE(n,'/vehicle') AND n.[test:color] LIKE '%green%')");
            return Collections.emptyList();
        }).when(mockFedoraTemplate).query(any(), any());
        vehicleRepo.findByColorLike("green");
    }

    @Test
    public void testFindByMakeAndColor() throws Exception {
        doAnswer(invocation -> {
            FedoraQuery query = (FedoraQuery) invocation.getArguments()[0];
            assertThat(query.toString())
                    .isEqualTo("SELECT * FROM [fedora:Resource] AS n WHERE (ISDESCENDANTNODE(n,'/vehicle') AND (n.[test:make] = 'Ford\u0018^^\u0018http://www.w3.org/2001/XMLSchema#string' AND n.[test:color] = 'green\u0018^^\u0018http://www.w3.org/2001/XMLSchema#string'))");
            return Collections.emptyList();
        }).when(mockFedoraTemplate).query(any(), any());
        vehicleRepo.findByMakeAndColor("Ford", "green");
    }

    @Test
    public void testFindByMakeOrColor() throws Exception {
        doAnswer(invocation -> {
            FedoraQuery query = (FedoraQuery) invocation.getArguments()[0];
            assertThat(query.toString())
                    .isEqualTo("SELECT * FROM [fedora:Resource] AS n WHERE (ISDESCENDANTNODE(n,'/vehicle') AND (n.[test:make] = 'Ford\u0018^^\u0018http://www.w3.org/2001/XMLSchema#string' OR n.[test:color] LIKE '%red%'))");
            return Collections.emptyList();
        }).when(mockFedoraTemplate).query(any(), any());
        vehicleRepo.findByMakeOrColorLike("Ford", "red");
    }

    @Test
    public void testFindByMakeAndMilesOrColorAndConsumption() throws Exception {
        doAnswer(invocation -> {
            FedoraQuery query = (FedoraQuery) invocation.getArguments()[0];
            assertThat(query.toString())
                    .isEqualTo("SELECT * FROM [fedora:Resource] AS n WHERE (ISDESCENDANTNODE(n,'/vehicle') AND ((n.[test:make] = 'Ford\u0018^^\u0018http://www.w3.org/2001/XMLSchema#string' AND n.[test:miles] = '1000\u0018^^\u0018http://www.w3.org/2001/XMLSchema#int') OR (n.[test:color] = 'red\u0018^^\u0018http://www.w3.org/2001/XMLSchema#string' AND n.[test:consumption] = '4.5\u0018^^\u0018http://www.w3.org/2001/XMLSchema#float')))");
            return Collections.emptyList();
        }).when(mockFedoraTemplate).query(any(), any());
        vehicleRepo.findByMakeAndMilesOrColorAndConsumption("Ford", 1000, "red", 4.5f);

    }

    @Test
    public void testFindByCreatedGreaterThan() throws Exception {
        doAnswer(invocation -> {
            FedoraQuery query = (FedoraQuery) invocation.getArguments()[0];
            assertThat(query.toString())
                    .isIn("SELECT * FROM [fedora:Resource] AS n WHERE (ISDESCENDANTNODE(n,'/vehicle') AND n.[jcr:created] > CAST('1970-01-01T01:00:00.000+01:00' AS DATE))",
                            "SELECT * FROM [fedora:Resource] AS n WHERE (ISDESCENDANTNODE(n,'/vehicle') AND n.[jcr:created] > CAST('1970-01-01T00:00:00.000Z' AS DATE))");
            return Collections.emptyList();
        }).when(mockFedoraTemplate).query(any(), any());
        vehicleRepo.findByCreatedGreaterThan(new Date(0L));

    }

    @Test
    public void testFindByCreatedGreaterThanByPage() throws Exception {
        doAnswer(invocation -> {
            FedoraQuery query = (FedoraQuery) invocation.getArguments()[0];
            assertThat(query.toString())
                    .isIn("SELECT * FROM [fedora:Resource] AS n WHERE (ISDESCENDANTNODE(n,'/vehicle') AND n.[jcr:created] > CAST('1970-01-01T01:00:00.000+01:00' AS DATE)) LIMIT 10 OFFSET 10",
                            "SELECT * FROM [fedora:Resource] AS n WHERE (ISDESCENDANTNODE(n,'/vehicle') AND n.[jcr:created] > CAST('1970-01-01T01:00:00.000Z' AS DATE)) LIMIT 10 OFFSET 10");
            return new FedoraResultPage<Vehicle>(Collections.emptyList(), null);
        }).when(mockFedoraTemplate).queryForPage(any(), any());
        vehicleRepo.findByCreatedGreaterThan(new Date(0L), new FedoraPageRequest(10, 10));
    }

    @Test
    public void testFindAllByPage() throws Exception {
        doAnswer(invocation -> {
            FedoraQuery query = (FedoraQuery) invocation.getArguments()[0];
            assertThat(query.toString())
                    .isEqualTo("SELECT * FROM [fedora:Resource] AS n WHERE ISDESCENDANTNODE(n,'/vehicle') LIMIT 2147483647");
            return new PageImpl<Vehicle>(Collections.emptyList());
        }).when(mockFedoraTemplate).queryForPage(any(), any());
        vehicleRepo.findAll(new FedoraPageRequest(0, Integer.MAX_VALUE));
    }

}
