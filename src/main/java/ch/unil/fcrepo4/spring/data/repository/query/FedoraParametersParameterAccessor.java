package ch.unil.fcrepo4.spring.data.repository.query;

// based on code from org.springframework.data.solr.repository.query.SolrParametersParameterAccessor

import ch.unil.fcrepo4.spring.data.core.query.CountingPageable;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.ParametersParameterAccessor;

/**
 * @author gushakov
 */
public class FedoraParametersParameterAccessor extends ParametersParameterAccessor implements FedoraParameterAccessor {

    /**
     * Creates a new {@link ParametersParameterAccessor}.
     *
     * @param parameters method parameters
     * @param values     values
     */
    public FedoraParametersParameterAccessor(Parameters<?, ?> parameters, Object[] values) {
        super(parameters, values);
    }

    @Override
    public boolean needsCount() {
        return getPageable() != null && getTotalCount() == -1;
    }

    @Override
    public long getTotalCount() {
        return getPageable() != null ? ((CountingPageable) getPageable()).getTotalCount() : -1;
    }

    @Override
    public void setTotalCount(long totalCount) {
        CountingPageable pageable = (CountingPageable) getPageable();
        if (pageable != null) {
            pageable.setTotalCount(totalCount);
        }
    }

}
