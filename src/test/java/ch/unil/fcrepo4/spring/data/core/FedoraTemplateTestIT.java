package ch.unil.fcrepo4.spring.data.core;

import ch.unil.fcrepo4.beans.Bean1;
import ch.unil.fcrepo4.beans.Bean2;
import ch.unil.fcrepo4.beans.Bean2Datastream1;
import com.hp.hpl.jena.datatypes.xsd.impl.XSDBaseNumericType;
import com.hp.hpl.jena.graph.NodeFactory;
import org.fcrepo.client.FedoraDatastream;
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

    @Autowired
    private FedoraTemplate fedoraTemplate;

    @Autowired
    private FedoraRepository fedoraRepository;

    @Test
    public void testSaveBean1() throws Exception {
        Bean1 bean1 = new Bean1();
        bean1.setPath("/foo/bar/" + System.currentTimeMillis());
        bean1.setNumber(3);
        bean1.setFoo("bar");
        String path = fedoraTemplate.save(bean1);
        assertThat(path).isEqualTo(bean1.getPath());
        assertThat(bean1).isNotNull();
        assertThat(bean1.getUuid()).isNotNull();
        assertThat(bean1.getCreated()).isNotNull();
        org.fcrepo.client.FedoraObject fo = fedoraRepository.getObject(path);
        assertThat(fo.getProperties()).contains(NodeFactory.createURI("info:fedora/test/number"),
                NodeFactory.createLiteral("3", XSDBaseNumericType.XSDinteger));
        assertThat(fo.getProperties()).contains(NodeFactory.createURI("info:fedora/test/foo"),
                NodeFactory.createLiteral("bar", XSDBaseNumericType.XSDstring));
    }

    @Test
    public void testLoadBean1() throws Exception {
        Bean1 bean1Save = new Bean1();
        bean1Save.setPath("/foo/bar/" + System.currentTimeMillis());
        bean1Save.setNumber(3);
        bean1Save.setFoo("bar");
        String path = fedoraTemplate.save(bean1Save);
        Bean1 bean1Load = fedoraTemplate.load(path, Bean1.class);
        assertThat(bean1Load).isNotNull();
        assertThat(bean1Load.getUuid()).isNotNull();
        assertThat(bean1Load.getCreated()).isNotNull();
        assertThat(bean1Load.getNumber()).isEqualTo(3);
        assertThat(bean1Load.getFoo()).isEqualTo("bar");
    }

    @Test
    public void testSaveBean2() throws Exception {
        Bean2 bean2 = new Bean2();
        bean2.setPath("/foo/bar/" + System.currentTimeMillis());
        Bean2Datastream1 dsBean = new Bean2Datastream1();
        dsBean.setWam("baz");
        dsBean.setXmlStream(new ByteArrayInputStream("<foo>bar</foo>".getBytes()));
        bean2.setXmlDs(dsBean);
        String path = fedoraTemplate.save(bean2);
        FedoraDatastream ds = fedoraRepository.getDatastream(path+"/xmlDs");
        assertThat(ds.getProperties()).contains(NodeFactory.createURI("info:fedora/test/wam"),
                NodeFactory.createLiteral("baz", XSDBaseNumericType.XSDstring));

    }

}
