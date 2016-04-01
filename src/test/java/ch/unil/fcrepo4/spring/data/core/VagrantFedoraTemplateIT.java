package ch.unil.fcrepo4.spring.data.core;

import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import ch.unil.fcrepo4.spring.data.repository.*;
import org.apache.commons.io.IOUtils;
import org.fcrepo.client.FedoraDatastream;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import static ch.unil.fcrepo4.assertj.Assertions.assertThat;
import static ch.unil.fcrepo4.spring.data.core.Constants.TEST_FEDORA_URI_NAMESPACE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author gushakov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {VagrantFedoraTemplateIT.TestConfig.class})
public class VagrantFedoraTemplateIT {

    @Configuration
    static class TestConfig {

        @Bean
        public FedoraTemplate fedoraTemplate() {
            // default setup: 8080, /fcrepo/rest, /fuseki (db: test)
            return new FedoraTemplate();
        }

    }

    @Autowired
    private FedoraTemplate fedoraTemplate;

    @Test
    public void testDelete() throws Exception {
        fedoraTemplate.delete(1459493033751L, Vehicle.class);
    }

    @Test
    public void testSave() throws Exception {
        final RdfDatatypeConverter rdfDatatypeConverter = fedoraTemplate.getConverter().getRdfDatatypeConverter();
        final FedoraRepository fcrepo = fedoraTemplate.getRepository();
        final long id = System.currentTimeMillis();
        try {
            Vehicle vehicle = new Vehicle(id, "Nice car", 10000);
            vehicle.setColor("dark blue");
            vehicle.setConsumption(6.7f);
            fedoraTemplate.save(vehicle);
            final String path = "/vehicle/" + id;
            assertThat(fcrepo.exists(path));
            FedoraObject fo = fcrepo.getObject(path);
            assertThat(fo.getProperties())
                    .containsPredicateWithObjectValue(TEST_FEDORA_URI_NAMESPACE + "make",
                            rdfDatatypeConverter.serializeLiteralValue("Nice car"));
        } finally {
            fedoraTemplate.delete(id, Vehicle.class);
        }
    }

    @Test
    public void testSaveWithDatastream() throws Exception {
        final FedoraRepository fcrepo = fedoraTemplate.getRepository();
        final RdfDatatypeConverter rdfDatatypeConverter = fedoraTemplate.getConverter().getRdfDatatypeConverter();
        final long id = System.currentTimeMillis();
        try {
            Vehicle vehicle = new Vehicle(id, "Nice car", 10000);
            vehicle.setColor("dark blue");
            vehicle.setConsumption(6.7f);
            VehicleDescription description = new VehicleDescription(new ByteArrayInputStream("Lorem ipsum".getBytes()));
            description.setType("full");
            vehicle.setDescription(description);
            VehiclePicture picture = new VehiclePicture(new ClassPathResource("picture.png").getInputStream());
            vehicle.setPicture(picture);
            fedoraTemplate.save(vehicle);
            final String descDsPath = "/vehicle/" + id + "/description";
            assertThat(fcrepo.exists(descDsPath));
            FedoraDatastream descDs = fcrepo.getDatastream(descDsPath);
            StringWriter descSw = new StringWriter();
            IOUtils.copy(descDs.getContent(), descSw);
            descSw.flush();
            descSw.close();
            assertThat(descSw.toString()).isEqualTo("Lorem ipsum");
            assertThat(descDs.getProperties()).containsPredicateWithObjectValue(TEST_FEDORA_URI_NAMESPACE + "type",
                    rdfDatatypeConverter.serializeLiteralValue("full"));
            assertThat(fcrepo.exists("/vehicle/" + id + "/picture"));
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

            final FedoraRepository repository = fedoraTemplate.getRepository();
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
