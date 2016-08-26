package ch.unil.fcrepo4.spring.data.core;

import ch.unil.fcrepo4.client.FedoraClientRepository;
import ch.unil.fcrepo4.client.FedoraDatastream;
import ch.unil.fcrepo4.client.FedoraException;
import ch.unil.fcrepo4.client.FedoraObject;
import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import ch.unil.fcrepo4.spring.data.repository.*;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import static ch.unil.fcrepo4.assertj.Assertions.assertThat;
import static ch.unil.fcrepo4.spring.data.core.Constants.TEST_FEDORA_URI_NAMESPACE;

/**
 * @author gushakov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {FedoraTemplateTestIT.TestConfig.class})
public class FedoraTemplateTestIT {

    @Configuration
    @PropertySource("classpath:fcrepo4.properties")
    static class TestConfig {

        //@formatter:off
        @Bean
        public FedoraTemplate fedoraTemplate(
                @Value("#{environment.getProperty('fedora.host')}") String fedoraHost,
                @Value("#{environment.getProperty('fedora.port')}") int fedoraPort,
                @Value("#{environment.getProperty('fedora.path')}") String fedoraPath,
                @Value("#{environment.getProperty('triplestore.host')}") String triplestoreHost,
                @Value("#{environment.getProperty('triplestore.port')}") int triplestorePort,
                @Value("#{environment.getProperty('triplestore.path')}") String triplestorePath,
                @Value("#{environment.getProperty('triplestore.db')}") String triplestoreDb
        ) throws FedoraException {
            return new FedoraTemplate(fedoraHost, fedoraPort, fedoraPath,
                    triplestoreHost, triplestorePort, triplestorePath, triplestoreDb);
        }
        //@formatter:on

    }

    @Autowired
    private FedoraTemplate fedoraTemplate;

    @Test
    public void testSave() throws Exception {
        final RdfDatatypeConverter rdfDatatypeConverter = fedoraTemplate.getConverter().getRdfDatatypeConverter();
        final FedoraClientRepository fcrepo = fedoraTemplate.getRepository();
        final long id = System.currentTimeMillis();
        try {
            Vehicle vehicle = new Vehicle(id, "Ford", 10000);
            vehicle.setColor("dark blue");
            vehicle.setConsumption(6.7f);
            fedoraTemplate.save(vehicle);
            final String path = "/vehicle/" + id;
            Assertions.assertThat(fcrepo.exists(path));
            FedoraObject fo = fcrepo.getObject(path);
            assertThat(fo.getProperties())
                    .containsPredicateWithObjectValue(TEST_FEDORA_URI_NAMESPACE + "make",
                            rdfDatatypeConverter.serializeLiteralValue("Ford"));
        } finally {
            fedoraTemplate.delete(id, Vehicle.class);
        }
    }

    @Test
    public void testSaveWithDatastream() throws Exception {
        final FedoraClientRepository fcrepo = fedoraTemplate.getRepository();
        final RdfDatatypeConverter rdfDatatypeConverter = fedoraTemplate.getConverter().getRdfDatatypeConverter();
        final long id = System.currentTimeMillis();
        try {
            Vehicle vehicle = new Vehicle(id, "Ford", 10000);
            vehicle.setColor("dark blue");
            vehicle.setConsumption(6.7f);
            VehicleDescription description = new VehicleDescription(new ByteArrayInputStream("Lorem ipsum".getBytes()));
            description.setType("full");
            vehicle.setDescription(description);
            VehiclePicture picture = new VehiclePicture(new ClassPathResource("picture.png").getInputStream());
            vehicle.setPicture(picture);
            fedoraTemplate.save(vehicle);
            final String descDsPath = "/vehicle/" + id + "/description";
            Assertions.assertThat(fcrepo.exists(descDsPath));
            FedoraDatastream descDs = fcrepo.getDatastream(descDsPath);
            StringWriter descSw = new StringWriter();
            IOUtils.copy(descDs.getContent(), descSw, "UTF-8");
            descSw.flush();
            descSw.close();
            Assertions.assertThat(descSw.toString()).isEqualTo("Lorem ipsum");
            assertThat(descDs.getProperties()).containsPredicateWithObjectValue(TEST_FEDORA_URI_NAMESPACE + "type",
                    rdfDatatypeConverter.serializeLiteralValue("full"));
            Assertions.assertThat(fcrepo.exists("/vehicle/" + id + "/picture"));
        } finally {
            fedoraTemplate.delete(id, Vehicle.class);
        }
    }

    @Test
    public void testSaveWithRelation() throws Exception {
        final long vehicleId = System.currentTimeMillis();
        final long ownerId = System.currentTimeMillis() + 1;
        final long addressId = System.currentTimeMillis() + 2;

        try {
            Vehicle vehicle = new Vehicle(vehicleId, "Ford Mustang", 3000);
            Owner owner = new Owner(ownerId, "Lucky Luke");
            Address address = new Address(addressId, "Main St. 123");
            owner.setAddress(address);
            vehicle.setOwner(owner);
            fedoraTemplate.save(vehicle);

            final FedoraClientRepository repository = fedoraTemplate.getRepository();
            final FedoraObject vehicleFo = repository.getObject("/vehicle/" + vehicleId);
            final String repoUrl = repository.getRepositoryUrl();
            assertThat(vehicleFo.getProperties())
                    .containsPredicateWithObjectUri(TEST_FEDORA_URI_NAMESPACE + "owner",
                            repoUrl + "/owner/" + ownerId);

            owner.setFullName("Mickey Mouse");
            address.setZipCode(12345);
            fedoraTemplate.save(vehicle);
            final FedoraObject addressFo = repository.getObject("/address/" + addressId);
            assertThat(addressFo.getProperties()).containsPredicateWithObjectValue(TEST_FEDORA_URI_NAMESPACE + "zipCode",
                    fedoraTemplate.getConverter().getRdfDatatypeConverter().serializeLiteralValue(12345));
        } finally {
            fedoraTemplate.delete(vehicleId, Vehicle.class);
            fedoraTemplate.delete(ownerId, Owner.class);
            fedoraTemplate.delete(addressId, Address.class);
        }
    }

}
