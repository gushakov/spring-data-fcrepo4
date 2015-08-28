package ch.unil.fcrepo4.spring.data.repository.support;

// based on code from org.springframework.data.solr.repository.support.SolrRepositoryFactoryBean

import ch.unil.fcrepo4.spring.data.core.FedoraOperations;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraMappingContext;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport;
import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * @author gushakov
 */
public class FedoraRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable> extends TransactionalRepositoryFactoryBeanSupport<T, S, ID> {

    private FedoraMappingContext fedoraMappingContext;
    private FedoraOperations fedoraOperations;

    public FedoraMappingContext getFedoraMappingContext() {
        return fedoraMappingContext;
    }

    public void setFedoraMappingContext(FedoraMappingContext fedoraMappingContext) {
        this.fedoraMappingContext = fedoraMappingContext;
    }

    public FedoraOperations getFedoraOperations() {
        return fedoraOperations;
    }

    public void setFedoraOperations(FedoraOperations fedoraOperations) {
        this.fedoraOperations = fedoraOperations;
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        Assert.notNull(fedoraOperations, "FedoraOperations must be supplied for FedoraRepositoryFactoryBean.");
    }

    @Override
    protected RepositoryFactorySupport doCreateRepositoryFactory() {
        return new FedoraRepositoryFactory(fedoraOperations);
    }
}
