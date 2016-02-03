package ch.unil.fcrepo4.spring.data.core.query;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

// based on the code from org.springframework.data.solr.core.query.SolrPageRequest

/**
 * @author gushakov
 */
public class FedoraPageRequest implements Pageable {

    private int offset;
    private int rowLimit;

    public FedoraPageRequest(int offset, int rowLimit) {
        this.offset = offset;
        this.rowLimit = rowLimit;
    }

    @Override
    public int getPageNumber() {
        return offset / rowLimit;
    }

    @Override
    public int getPageSize() {
        return rowLimit;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return null;
    }

    @Override
    public Pageable next() {
        return new FedoraPageRequest(offset + rowLimit, rowLimit);
    }

    @Override
    public Pageable previousOrFirst() {
        return hasPrevious() ? previous() : first();
    }

    @Override
    public Pageable first() {
        return new FedoraPageRequest(0, rowLimit);
    }

    @Override
    public boolean hasPrevious() {
        return getPageNumber() > 0;
    }

    public Pageable previous(){
        return new PageRequest(offset - rowLimit, rowLimit);
    }
}
