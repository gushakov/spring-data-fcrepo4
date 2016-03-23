package ch.unil.fcrepo4.spring.data.core;

import ch.unil.fcrepo4.spring.data.repository.*;
import org.fcrepo.client.*;
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
    public void testSaveWithDatastream() throws Exception {
        Vehicle vehicle = new Vehicle(System.currentTimeMillis(), "Nice car", 10000);
        vehicle.setColor("dark blue");
        vehicle.setConsumption(6.7f);
        VehicleDescription description = new VehicleDescription(new ByteArrayInputStream("Lorem ipsum".getBytes()));
        description.setType("full");
        vehicle.setDescription(description);
        VehiclePicture picture = new VehiclePicture(new ClassPathResource("picture.png").getInputStream());
        vehicle.setPicture(picture);
        fedoraTemplate.save(vehicle);
    }

    @Test
    public void testName() throws Exception {
        System.out.println(fedoraTemplate.getConverter().getRdfDatatypeConverter().encodeLiteralValue(12345).getLiteralLexicalForm());
        System.out.println(fedoraTemplate.getConverter().getRdfDatatypeConverter().serializeLiteralValue(12345));
    }

    @Test
    public void testSaveWithRelation() throws Exception {
        final long vehicleId = System.currentTimeMillis();
        Vehicle vehicle = new Vehicle(vehicleId, "Ford Mustang", 3000);
        final long ownerId = System.currentTimeMillis() + 1;
        Owner owner = new Owner(ownerId, "Lucky Luke");
        final long addressId = System.currentTimeMillis() + 2;
        Address address = new Address(addressId, "Main St. 123");
        owner.setAddress(address);
        vehicle.setOwner(owner);
        fedoraTemplate.save(vehicle);

        final org.fcrepo.client.FedoraRepository repository = fedoraTemplate.getRepository();
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
    }

}
