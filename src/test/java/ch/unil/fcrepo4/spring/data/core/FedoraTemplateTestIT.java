package ch.unil.fcrepo4.spring.data.core;

import ch.unil.fcrepo4.spring.data.repository.Vehicle;
import ch.unil.fcrepo4.spring.data.repository.VehicleDescription;
import ch.unil.fcrepo4.spring.data.repository.VehiclePicture;
import org.fcrepo.client.FedoraException;
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
import java.io.InputStream;

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
        Vehicle vehicle = new Vehicle(System.currentTimeMillis(), "toto", 10000);
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
    public void testLoadWithDataStreamAndSave() throws Exception {
        Vehicle vehicle = fedoraTemplate.load(1L, Vehicle.class);
        //  System.out.println(vehicle);
//        vehicle.getMake();
        //     vehicle.setMake("hello");

        try (InputStream is = new ByteArrayInputStream("this is a second test".getBytes())) {

            //  VehicleDescription description = new VehicleDescription(is);
            //      vehicle.setDescription(description);
            vehicle.getDescription().setDesc(is);
            fedoraTemplate.save(vehicle);
        }


    }

}
