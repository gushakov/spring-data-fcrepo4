package ch.unil.fcrepo4.spring.data.core.query;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

// based on code from org.springframework.data.solr.core.query.SolrPageRequest

/**
 * @author gushakov
 */
public class FedoraRdfQueryPageRequest extends PageRequest implements CountingPageable {
    private long totalCount = -1;

    public FedoraRdfQueryPageRequest(int page, int size) {
        super(page, size);
    }

    @Override
    public void setTotalCount(long totalCount) {
       this.totalCount = totalCount;
    }

    @Override
    public long getTotalCount() {
        return totalCount;
    }

    @Override
    public Pageable next() {
        return new FedoraRdfQueryPageRequest(getPageNumber() + 1, getPageSize());
    }

    //TODO: override first, previous to return FedoraRdfQueryPageRequest
}
