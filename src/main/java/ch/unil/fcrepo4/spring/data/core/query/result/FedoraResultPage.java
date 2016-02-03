package ch.unil.fcrepo4.spring.data.core.query.result;

// based on the code from org.springframework.data.solr.core.query.result.SolrResultPage

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @author gushakov
 */
public class FedoraResultPage<T> extends PageImpl<T> {

    private Pageable pageable;

    public FedoraResultPage(List<T> content, Pageable pageable) {
        super(content, pageable, Long.MAX_VALUE);
        this.pageable = pageable;
    }

    @Override
    public long getTotalElements() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getTotalPages() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasNext() {
        return getNumberOfElements() > 0 && getNumberOfElements() <= pageable.getPageSize();
    }
}
