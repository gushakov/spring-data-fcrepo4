package ch.unil.fcrepo4.spring.data.repository;

import ch.unil.fcrepo4.spring.data.core.FedoraTemplate;
import ch.unil.fcrepo4.spring.data.core.query.FedoraPageRequest;
import ch.unil.fcrepo4.spring.data.repository.config.EnableFedoraRepositories;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.fcrepo.client.FedoraException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static ch.unil.fcrepo4.assertj.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author gushakov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SimpleFedoraRepositoryTestIT.TestConfig.class})
public class SimpleFedoraRepositoryTestIT {

    private static final long DELAY = 1000L;

    @Configuration
    @PropertySource("classpath:fcrepo4.properties")
    @EnableFedoraRepositories
    public static class TestConfig {

        //@formatter:off
        @Bean
        public FedoraTemplate fedoraTemplate(
                @Value("#{environment.getProperty('fedora.host')}")         String fedoraHost,
                @Value("#{environment.getProperty('fedora.port')}")         int fedoraPort,
                @Value("#{environment.getProperty('fedora.path')}")         String fedoraPath,
                @Value("#{environment.getProperty('triplestore.host')}")    String triplestoreHost,
                @Value("#{environment.getProperty('triplestore.port')}")    int triplestorePort,
                @Value("#{environment.getProperty('triplestore.path')}")    String triplestorePath,
                @Value("#{environment.getProperty('triplestore.db')}")      String triplestoreDb
        ) throws FedoraException {
            return new FedoraTemplate(fedoraHost, fedoraPort, fedoraPath,
                    triplestoreHost, triplestorePort, triplestorePath, triplestoreDb);
        }
        //@formatter:on

    }

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private VehicleCrudRepository vehicleRepo;

    @Autowired
    private FedoraTemplate fedoraTemplate;

    @Test
    public void testFindByMake() throws Exception {
        try {
            vehicleRepo.save(new Vehicle(1L, "Ford", "light green", 1000, 6.5f));

            Thread.sleep(DELAY);

            List<Vehicle> vehicles = vehicleRepo.findByMake("Ford");
            ch.unil.fcrepo4.assertj.Assertions.assertThat(vehicles).isNotEmpty();
            assertThat(vehicles.get(0)).hasMake("Ford");
        } finally {
            vehicleRepo.delete(1L);
        }
    }

    @Test
    public void testFindByMakeWithAccents() throws Exception {
        try {
            vehicleRepo.save(new Vehicle(1L, "Citroën", "silver", 1000, 4.0f));

            Thread.sleep(DELAY);

            List<Vehicle> vehicles = vehicleRepo.findByMake("Citroën");
            ch.unil.fcrepo4.assertj.Assertions.assertThat(vehicles).isNotEmpty();
            assertThat(vehicles.get(0)).hasMake("Citroën");
        } finally {
            vehicleRepo.delete(1L);
        }
    }

    @Test
    public void testFindByMilesGreaterThan() throws Exception {
        vehicleRepo.save(new Vehicle(1L, "Ford", "light green", 1000, 6.5f));
        vehicleRepo.save(new Vehicle(2L, "Toyota", "red", 15000, 4.5f));
        vehicleRepo.save(new Vehicle(3L, "Ferrari", "red", 10000, 7.5f));

        Thread.sleep(DELAY);

        try {
            assertThat((List<? extends Vehicle>) vehicleRepo.findByMilesGreaterThan(10000))
                    .extracting("id").containsOnly(2L);
        } finally {
            vehicleRepo.delete(1L);
            vehicleRepo.delete(2L);
            vehicleRepo.delete(3L);
        }
    }

    @Test
    public void testFindByMilesGreaterThanWithPageable() throws Exception {
        try {
            vehicleRepo.save(new Vehicle(1L, "Ford", "light green", 1000, 6.5f));
            vehicleRepo.save(new Vehicle(2L, "Toyota", "red", 15000, 4.5f));
            vehicleRepo.save(new Vehicle(3L, "Ferrari", "red", 10000, 7.5f));
            vehicleRepo.save(new Vehicle(4L, "Volkswagen", "blue", 20000, 5.5f));
            vehicleRepo.save(new Vehicle(5L, "Honda", "green", 1500, 5.0f));
            vehicleRepo.save(new Vehicle(6L, "Lexus", "light brown", 3000, 7.0f));
            vehicleRepo.save(new Vehicle(7L, "Citroën", "silver", 1000, 4.0f));
            vehicleRepo.save(new Vehicle(8L, "Dodge", "red", 1500, 6.0f));

            Thread.sleep(DELAY);

            final Page<Vehicle> firstPage = vehicleRepo.findByMilesGreaterThan(100, new FedoraPageRequest(0, 3));
            assertThat(firstPage.getNumber()).isEqualTo(0);
            assertThat(firstPage.getNumberOfElements()).isEqualTo(3);
            assertThat(firstPage.isFirst()).isTrue();
            assertThat(firstPage.isLast()).isFalse();
            assertThat((List<? extends Vehicle>) firstPage.getContent()).extracting("miles", Integer.class)
                    .have(new Condition<Integer>() {
                        @Override
                        public boolean matches(Integer miles) {
                            return miles > 100;
                        }
                    });
            final Page<Vehicle> secondPage = vehicleRepo.findByMilesGreaterThan(100, firstPage.nextPageable());
            assertThat(secondPage.getNumber()).isEqualTo(1);
            assertThat(secondPage.getNumberOfElements()).isEqualTo(3);
            assertThat(secondPage.isFirst()).isFalse();
            assertThat(secondPage.isLast()).isFalse();
        } finally {
            vehicleRepo.delete(1L);
            vehicleRepo.delete(2L);
            vehicleRepo.delete(3L);
            vehicleRepo.delete(4L);
            vehicleRepo.delete(5L);
            vehicleRepo.delete(6L);
            vehicleRepo.delete(7L);
            vehicleRepo.delete(8L);
        }
    }

    @Test
    public void testFindByOwner_FullName() throws Exception {
        try {

            Owner owner = new Owner(1L, "George Smith");
            Vehicle vehicle = new Vehicle(1L, "Ford", "light green", 1000, 6.5f);
            vehicle.setOwner(owner);
            vehicleRepo.save(vehicle);

            Thread.sleep(DELAY);

            final List<Vehicle> vehicles = vehicleRepo.findByOwner_FullName("George Smith");
            Assertions.assertThat(vehicles).hasSize(1);

        } finally {
            fedoraTemplate.delete(1L, Owner.class);
            vehicleRepo.delete(1L);
        }

    }

    @Test
    public void testFindByOwner_FullNameAndOwner_Address_ZipCode() throws Exception {
        try {

            Address address = new Address(1L, "123 Main St.", 12345);
            Owner owner = new Owner(1L, "Joe Taylor");
            owner.setAddress(address);
            Vehicle vehicle = new Vehicle(1L, "Toyota", "gray", 1000, 6.5f);
            vehicle.setOwner(owner);
            vehicleRepo.save(vehicle);

            Thread.sleep(DELAY);

            List<Vehicle> vehicles = vehicleRepo.findByOwner_FullNameAndOwner_Address_ZipCode("Joe Taylor", 12345);

            // Note: cannot use extract in the assertion below since the reflection calls (used by AssertJ) on proxy
            // properties returns nulls, need to explicitly use getters in assertions.

            Assertions.assertThat(vehicles).hasSize(1);
        } finally {
            fedoraTemplate.delete(1L, Address.class);
            fedoraTemplate.delete(1L, Owner.class);
            vehicleRepo.delete(1L);
        }


    }
}
