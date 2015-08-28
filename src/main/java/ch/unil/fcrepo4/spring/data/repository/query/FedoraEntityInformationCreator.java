package ch.unil.fcrepo4.spring.data.repository.query;

// based on code from org.springframework.data.solr.repository.query.SolrEntityInformationCreator

import java.io.Serializable;

/**
 * @author gushakov
 */
public interface FedoraEntityInformationCreator {

    <T, ID extends Serializable> FedoraEntityInformation<T, ID> getEntityInformation(Class<T> domainClass);

}
