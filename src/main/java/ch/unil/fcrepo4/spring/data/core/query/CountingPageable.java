package ch.unil.fcrepo4.spring.data.core.query;

import org.springframework.data.domain.Pageable;

/**
 * @author gushakov
 */
public interface CountingPageable extends Pageable {

    void setTotalCount(long totalCount);

    long getTotalCount();
}
