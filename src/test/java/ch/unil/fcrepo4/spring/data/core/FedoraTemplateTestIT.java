package ch.unil.fcrepo4.spring.data.core;

import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraObjectPersistentEntity;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Created;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.FedoraObject;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Path;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Property;
import ch.unil.fcrepo4.spring.data.repository.Vehicle;
import ch.unil.fcrepo4.spring.data.repository.VehicleDescription;
import com.hp.hpl.jena.graph.NodeFactory;
import org.assertj.core.api.Assertions;
import org.fcrepo.client.FedoraException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayInputStream;
import java.time.ZonedDateTime;
import java.util.Date;

import static ch.unil.fcrepo4.assertj.Assertions.assertThat;

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

    @Autowired
    private FedoraTemplate fedoraTemplate;

    @FedoraObject
    public static class Bean2 {

        @Path
        long id;

        @Created
        ZonedDateTime created;

        @Property
        int number = 3;

        @Property
        String foo = "bar";

    }


    @Test
    public void testSave() throws Exception {
        RdfDatatypeConverter rdfDatatypeConverter = fedoraTemplate.getConverter().getRdfDatatypeConverter();
        long id = System.currentTimeMillis();
        Bean2 bean = new Bean2();
        bean.id = id;
        fedoraTemplate.save(bean);
        String namespace = ((FedoraObjectPersistentEntity<?>) fedoraTemplate.getConverter().getMappingContext().getPersistentEntity(Bean2.class))
                .getNamespace();
        org.fcrepo.client.FedoraObject fo = fedoraTemplate.getRepository().getObject("/" + namespace + "/" + id);
        assertThat(fo.getProperties()).contains(NodeFactory.createURI("info:fedora/test/number"),
                rdfDatatypeConverter.encodeLiteralValue(3));
        assertThat(fo.getProperties()).contains(NodeFactory.createURI("info:fedora/test/foo"),
                rdfDatatypeConverter.encodeLiteralValue("bar"));
    }

    @Test
    public void testSaveAndLoad() throws Exception {
        long id = System.currentTimeMillis();
        Bean2 write = new Bean2();
        write.id = id;
        fedoraTemplate.save(write);
        Bean2 read = fedoraTemplate.load(id, Bean2.class);
        System.out.println(read);
        Assertions.assertThat(read.foo).isEqualTo("bar");
        System.out.println(read.created);
    }

    @Test
    public void testSaveWithDatastream() throws Exception {
        Vehicle vehicle = new Vehicle(System.currentTimeMillis(), "Batmobile");
        VehicleDescription description = new VehicleDescription(new ByteArrayInputStream("toto".getBytes()));
        description.setType("full");
        vehicle.setDescription(description);
        fedoraTemplate.save(vehicle);
    }

    @Test
    public void testQuery() throws Exception {
    }
}
