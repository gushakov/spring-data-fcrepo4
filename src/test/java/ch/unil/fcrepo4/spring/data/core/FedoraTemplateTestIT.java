package ch.unil.fcrepo4.spring.data.core;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Datastream;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.FedoraObject;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Path;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Property;
import com.hp.hpl.jena.datatypes.xsd.impl.XSDBaseNumericType;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.vocabulary.XSD;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.impl.FedoraRepositoryImpl;
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
import java.io.InputStream;
import java.util.UUID;

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
        public FedoraRepository fedoraRepository() throws FedoraException {
            String repoUrl = env.getProperty("fedora.repository.url");
            return new FedoraRepositoryImpl(repoUrl);
        }

        @Bean
        public FedoraTemplate fedoraTemplate() throws FedoraException {
            return new FedoraTemplate(fedoraRepository());
        }

    }

    @FedoraObject
    static class Bean1 {
        @Path
        String path = "foo-bar-101";
    }

    @FedoraObject
    static class Bean2 {
        @Path
        String path = "102|103";
    }

    @FedoraObject
    public static class Bean3 {
        @Path
        String path = UUID.randomUUID().toString();

        @Datastream
        InputStream foobarDs = new ByteArrayInputStream("<foo>bar</foo>".getBytes());
    }

    @FedoraObject
    static class Bean4 {
        @Path
        String path = UUID.randomUUID().toString();

        @Property
        int number = 1;
    }

    @Autowired
    private FedoraTemplate fedoraTemplate;

    @Autowired
    private FedoraRepository repository;

    @Test
    public void testSave1() throws Exception {
        fedoraTemplate.save(new Bean1());
    }

    @Test(expected = FedoraResourcePathException.class)
    public void testSave2() throws Exception {
        fedoraTemplate.save(new Bean2());
    }

    @Test
    public void testSaveXmlDatastream() throws Exception {
        Bean3 bean = new Bean3();
        fedoraTemplate.save(bean);
        System.out.println(bean.path);
    }

    @Test
    public void testSavePropertyInt() throws Exception {
        Bean4 bean4 = new Bean4();
        fedoraTemplate.save(bean4);
        assertThat(repository.getObject("/test/"+bean4.path).getProperties())
        .contains(NodeFactory.createURI(Constants.TEST_FEDORA_URI_NAMESPACE+"number"),
                NodeFactory.createLiteral(""+bean4.number, new XSDBaseNumericType(XSD.integer.getLocalName())));
    }

    @Test
    public void testLoadXmlDatastream() throws Exception {
        Bean3 beanSave = new Bean3();
        fedoraTemplate.save(beanSave);
        System.out.println(beanSave.path);
        Bean3 beanLoad = fedoraTemplate.load("/test/" + beanSave.path, Bean3.class);
        assertThat(beanLoad).isNotNull();
        assertThat(beanLoad.foobarDs).isNotNull();
    }

}
