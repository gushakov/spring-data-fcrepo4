package ch.unil.fcrepo4.spring.data.repository;

import ch.unil.fcrepo4.spring.data.core.FedoraTemplate;
import ch.unil.fcrepo4.spring.data.core.convert.rdf.ExtendedXsdDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.query.FedoraQuery;
import ch.unil.fcrepo4.spring.data.repository.config.EnableFedoraRepositories;
import org.fcrepo.client.FedoraException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
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
            return spy(new FedoraTemplate());
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
            FedoraQuery query = (FedoraQuery) invocation.getArguments()[0];
            assertThat(query.toString())
                    .isEqualTo("SELECT  ?ch_unil_fcrepo4_spring_data_repository_Vehicle\n" +
                            "WHERE\n" +
                            "  { ?ch_unil_fcrepo4_spring_data_repository_Vehicle <info:fedora/test/make> \"Ford\"^^<http://www.w3.org/2001/XMLSchema#string> .\n" +
                            "    ?ch_unil_fcrepo4_spring_data_repository_Vehicle <info:data/ocm/class> \"ch.unil.fcrepo4.spring.data.repository.Vehicle\"^^<http://www.w3.org/2001/XMLSchema#string>\n" +
                            "  }\n");
            return Collections.emptyList();
        }).when(mockFedoraTemplate).query(any(FedoraQuery.class), any());

        vehicleRepo.findByMake("Ford");
    }

    @Test
    public void testFindByMilesGreaterThan() throws Exception {
        doAnswer(invocation -> {
            FedoraQuery query = (FedoraQuery) invocation.getArguments()[0];
            assertThat(query.toString())
                    .isEqualTo("SELECT  ?ch_unil_fcrepo4_spring_data_repository_Vehicle\n" +
                            "WHERE\n" +
                            "  { ?ch_unil_fcrepo4_spring_data_repository_Vehicle <info:fedora/test/miles> ?ch_unil_fcrepo4_spring_data_repository_Vehicle_miles .\n" +
                            "    ?ch_unil_fcrepo4_spring_data_repository_Vehicle <info:data/ocm/class> \"ch.unil.fcrepo4.spring.data.repository.Vehicle\"^^<http://www.w3.org/2001/XMLSchema#string>\n" +
                            "    FILTER ( ?ch_unil_fcrepo4_spring_data_repository_Vehicle_miles > \"1000\"^^<http://www.w3.org/2001/XMLSchema#int> )\n" +
                            "  }\n");
            return Collections.emptyList();
        }).when(mockFedoraTemplate).query(any(FedoraQuery.class), any());

        vehicleRepo.findByMilesGreaterThan(1000);
    }

    @Test
    public void testFindByDescriptionType() throws Exception {
        doAnswer(invocation -> {
            FedoraQuery query = (FedoraQuery) invocation.getArguments()[0];
            assertThat(query.toString())
                    .isEqualTo("SELECT  ?ch_unil_fcrepo4_spring_data_repository_Vehicle\n" +
                            "WHERE\n" +
                            "  { ?ch_unil_fcrepo4_spring_data_repository_VehicleDescription <info:fedora/test/type> \"full\"^^<http://www.w3.org/2001/XMLSchema#string> .\n" +
                            "    ?ch_unil_fcrepo4_spring_data_repository_Vehicle <http://www.w3.org/ns/ldp#contains> ?ch_unil_fcrepo4_spring_data_repository_VehicleDescription .\n" +
                            "    ?ch_unil_fcrepo4_spring_data_repository_Vehicle <info:data/ocm/class> \"ch.unil.fcrepo4.spring.data.repository.Vehicle\"^^<http://www.w3.org/2001/XMLSchema#string> .\n" +
                            "    ?ch_unil_fcrepo4_spring_data_repository_VehicleDescription <info:data/ocm/class> \"ch.unil.fcrepo4.spring.data.repository.VehicleDescription\"^^<http://www.w3.org/2001/XMLSchema#string>\n" +
                            "  }\n");
            return Collections.emptyList();
        }).when(mockFedoraTemplate).query(any(FedoraQuery.class), any());

        vehicleRepo.findByDescription_Type("full");
    }

}
