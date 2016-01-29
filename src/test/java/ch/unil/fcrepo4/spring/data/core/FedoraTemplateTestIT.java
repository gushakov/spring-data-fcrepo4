package ch.unil.fcrepo4.spring.data.core;

import ch.unil.fcrepo4.beans.Bean1;
import ch.unil.fcrepo4.beans.Vehicle;
import com.hp.hpl.jena.datatypes.xsd.impl.XSDBaseNumericType;
import com.hp.hpl.jena.graph.NodeFactory;
import org.fcrepo.client.FedoraException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modeshape.jcr.ExecutionContext;
import org.modeshape.jcr.query.QueryBuilder;
import org.modeshape.jcr.query.model.Query;
import org.modeshape.jcr.query.model.QueryCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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

    @Test
    public void testSaveBean1() throws Exception {
        Bean1 bean1 = new Bean1();
        String path = "/foobar/" + System.currentTimeMillis();
        bean1.setPath(path);
        bean1.setNumber(3);
        bean1.setFoo("bar");
        fedoraTemplate.save(bean1);
        assertThat(bean1).isNotNull();
        org.fcrepo.client.FedoraObject fo = fedoraTemplate.getRepository().getObject(path);
        assertThat(fo.getProperties()).contains(NodeFactory.createURI("info:fedora/test/number"),
                NodeFactory.createLiteral("3", XSDBaseNumericType.XSDinteger));
        assertThat(fo.getProperties()).contains(NodeFactory.createURI("info:fedora/test/foo"),
                NodeFactory.createLiteral("bar", XSDBaseNumericType.XSDstring));
    }

    @Test
    public void testQuery() throws Exception {
        QueryBuilder builder = new QueryBuilder(new ExecutionContext().getValueFactories().getTypeSystem());
        QueryCommand query = builder.selectDistinctStar()
                .from("fedora:Resource")
                .query();
        List<Bean1> beans = fedoraTemplate.query((Query)query, Bean1.class);
        System.out.println(beans);
    }

    @Test
    public void testSaveVehicle() throws Exception {
        long id = System.currentTimeMillis();
        Vehicle vehicle = new Vehicle(id, "Ford");
        fedoraTemplate.save(vehicle);
        assertThat(fedoraTemplate.getRepository().exists("/vehicle/" + id));
    }

}
