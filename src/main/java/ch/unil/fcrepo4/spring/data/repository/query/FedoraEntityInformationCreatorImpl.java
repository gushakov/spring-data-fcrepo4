package ch.unil.fcrepo4.spring.data.repository.query;

import ch.unil.fcrepo4.spring.data.core.mapping.FedoraPersistentEntity;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraPersistentProperty;
import ch.unil.fcrepo4.spring.data.repository.support.MappingFedoraEntityInformation;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.util.Assert;

import java.io.Serializable;

// based on code from org.springframework.data.solr.repository.support.SolrEntityInformationCreatorImpl

/**
 * @author gushakov
 */
public class FedoraEntityInformationCreatorImpl implements FedoraEntityInformationCreator {

    private MappingContext<? extends FedoraPersistentEntity<?>, FedoraPersistentProperty> mappingContext;

    public FedoraEntityInformationCreatorImpl(MappingContext<? extends FedoraPersistentEntity<?>, FedoraPersistentProperty> mappingContext) {
        Assert.notNull(mappingContext);
        this.mappingContext = mappingContext;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, ID extends Serializable> FedoraEntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
        FedoraPersistentEntity<T> persistentEntity = (FedoraPersistentEntity<T>) mappingContext.getPersistentEntity(domainClass);
        Assert.notNull(persistentEntity, "No persistent entity for " + domainClass);
        return new MappingFedoraEntityInformation<>(persistentEntity);
    }
}
