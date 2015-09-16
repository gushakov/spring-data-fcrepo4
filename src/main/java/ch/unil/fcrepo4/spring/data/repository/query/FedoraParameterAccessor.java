package ch.unil.fcrepo4.spring.data.repository.query;

import ch.unil.fcrepo4.spring.data.core.query.CountingPageable;
import org.springframework.data.repository.query.ParameterAccessor;

// based on code from org.springframework.data.solr.repository.query.SolrParameterAccessor

/**
 * @author gushakov
 */
public interface FedoraParameterAccessor extends ParameterAccessor {
    boolean needsCount();
    long getTotalCount();
    void setTotalCount(long totalCount);
}
