package ch.unil.fcrepo4.spring.data.core;

import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Created;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.FedoraObject;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Path;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Property;
import ch.unil.fcrepo4.spring.data.repository.Vehicle;
import ch.unil.fcrepo4.spring.data.repository.VehicleDescription;
import com.hp.hpl.jena.graph.NodeFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.assertj.jodatime.api.Assertions;
import org.fcrepo.client.FedoraException;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.List;

import static ch.unil.fcrepo4.assertj.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author gushakov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {FedoraTemplateTestIT.TestConfig.class})
public class FedoraTemplateTestIT {

    @Configuration
    @PropertySource("classpath:fcrepo4.properties")
    static class TestConfig {

        @Autowired
        private Environment env;

        @Bean
        public FedoraTemplate fedoraTemplate() throws FedoraException {
            return new FedoraTemplate(env.getProperty("fedora.host"), env.getProperty("fedora.port", Integer.class));
        }

    }

    @Autowired
    private FedoraTemplate fedoraTemplate;


    @FedoraObject
    public static class Bean1 {

        @Path
        long id;

        @Created
        Date created;

        @Property
        int number = 3;

        @Property
        String foo = "bar";


    }


    @Test
    public void testSaveBean1() throws Exception {
        RdfDatatypeConverter rdfDatatypeConverter = fedoraTemplate.getConverter().getRdfDatatypeConverter();
        long id = System.currentTimeMillis();
        Bean1 bean = new Bean1();
        bean.id = id;
        fedoraTemplate.save(bean);
        org.fcrepo.client.FedoraObject fo = fedoraTemplate.getRepository().getObject("/bean1/" + id);
        assertThat(fo.getProperties()).contains(NodeFactory.createURI("info:fedora/test/number"),
                rdfDatatypeConverter.encodeLiteralValue(3));
        assertThat(fo.getProperties()).contains(NodeFactory.createURI("info:fedora/test/foo"),
                rdfDatatypeConverter.encodeLiteralValue("bar"));
    }

    @Test
    public void testLoadBean1() throws Exception {
        DateTime before = DateTime.now();
        long id = System.currentTimeMillis();
        Bean1 write = new Bean1();
        write.id = id;
        fedoraTemplate.save(write);
        Bean1 read = fedoraTemplate.load(id, Bean1.class);
        assertThat(read.number).isEqualTo(3);
        assertThat(read.foo).isEqualTo("bar");
        assertThat(read.created).isNotNull();
        Assertions.assertThat(new DateTime(read.created.getTime())).isAfter(before);
    }

    @Test
    public void testSaveWithDatastream() throws Exception {
        Vehicle vehicle = new Vehicle(System.currentTimeMillis(), "Batmobile");
        vehicle.setDescription(new VehicleDescription(new ByteArrayInputStream("foobar".getBytes())));
        fedoraTemplate.save(vehicle);
    }

    @Test
    public void testQuery() throws Exception {
       List<Vehicle> vehicles = fedoraTemplate.query(
                "SELECT * from [fedora:Container] as v where v.[ns001:beans] = 'object'",
                Vehicle.class
        );

        StringBuilderWriter writer = new StringBuilderWriter();

        IOUtils.copy(vehicles.get(0).getDescription().getDesc(), writer);

        System.out.println(writer.toString());

    }
}
