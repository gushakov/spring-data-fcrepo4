package ch.unil.fcrepo4.spring.data.repository;

// based on code from org.springframework.data.solr.repository.SolrCrudRepository

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.io.Serializable;

/**
 * @author gushakov
 */
@NoRepositoryBean
public interface FedoraCrudRepository<T, ID extends Serializable> extends FedoraRepository<T, ID>,
    PagingAndSortingRepository<T, ID>{
}
