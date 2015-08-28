package ch.unil.fcrepo4.spring.data.repository.query;

// based on code from org.springframework.data.solr.repository.query.SolrEntityInformation

import org.springframework.data.repository.core.EntityInformation;

import java.io.Serializable;

/**
 * @author gushakov
 */
public interface FedoraEntityInformation<T, ID extends Serializable> extends EntityInformation<T, ID> {
}
