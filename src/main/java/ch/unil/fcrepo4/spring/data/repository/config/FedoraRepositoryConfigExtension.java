package ch.unil.fcrepo4.spring.data.repository.config;

import ch.unil.fcrepo4.spring.data.repository.support.FedoraRepositoryFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;

// based on code from org.springframework.data.solr.repository.config.SolrRepositoryConfigExtension

/**
 * @author gushakov
 */
public class FedoraRepositoryConfigExtension extends RepositoryConfigurationExtensionSupport {

    // copied from org.springframework.data.solr.repository.config.SolrRepositoryConfigExtension

    enum BeanDefinition {
        FEDORA_MAPPING_CONTEXT("fedoraMappingContext"),
        FEDORA_OPERATIONS("fedoraOperations");

        String beanName;

        BeanDefinition(String beanName) {
            this.beanName = beanName;
        }

        public String getBeanName() {
            return beanName;
        }
    }

    @Override
    protected String getModulePrefix() {
        return "fedora";
    }

    @Override
    public String getRepositoryFactoryClassName() {
        return FedoraRepositoryFactoryBean.class.getName();
    }

    @Override
    public void postProcess(BeanDefinitionBuilder builder, AnnotationRepositoryConfigurationSource config) {

        AnnotationAttributes attributes = config.getAttributes();

        // add reference to FedoraTemplate bean (must be available in the application context) to this bean factory definition,
        // template bean name is set through the EnableFedoraRepositories annotation attribute "fedoraTemplateRef", defaults to "fedoraTemplate"
        builder.addPropertyReference(BeanDefinition.FEDORA_OPERATIONS.getBeanName(),
                attributes.getString("fedoraTemplateRef"));

    }
}
