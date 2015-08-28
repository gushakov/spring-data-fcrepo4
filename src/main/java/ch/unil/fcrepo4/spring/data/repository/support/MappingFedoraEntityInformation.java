package ch.unil.fcrepo4.spring.data.repository.support;

// based on code from org.springframework.data.solr.repository.support.MappingSolrEntityInformation

import ch.unil.fcrepo4.spring.data.repository.query.FedoraEntityInformation;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.repository.core.support.PersistentEntityInformation;

import java.io.Serializable;

/**
 * @author gushakov
 */
public class MappingFedoraEntityInformation<T, ID extends Serializable> extends PersistentEntityInformation<T, ID>
        implements FedoraEntityInformation<T, ID> {
    /**
     * Creates a new {@link PersistableEntityInformation} for the given {@link PersistentEntity}.
     *
     * @param entity must not be {@literal null}.
     */
    public MappingFedoraEntityInformation(PersistentEntity<T, ?> entity) {
        super(entity);
    }
}
