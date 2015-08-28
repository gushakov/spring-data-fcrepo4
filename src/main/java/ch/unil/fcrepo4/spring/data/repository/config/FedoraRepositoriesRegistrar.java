package ch.unil.fcrepo4.spring.data.repository.config;

import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

// based on code from org.springframework.data.solr.repository.config.SolrRepositoriesRegistrar

/**
 * @author gushakov
 */
public class FedoraRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {
    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableFedoraRepositories.class;
    }

    @Override
    protected RepositoryConfigurationExtension getExtension() {
        return new FedoraRepositoryConfigExtension();
    }
}
