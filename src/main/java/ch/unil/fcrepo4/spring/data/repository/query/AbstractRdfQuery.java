package ch.unil.fcrepo4.spring.data.repository.query;

import ch.unil.fcrepo4.spring.data.core.FedoraOperations;
import com.hp.hpl.jena.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;

import java.util.List;


// based on code from org.springframework.data.solr.repository.query.AbstractSolrQuery

/**
 * @author gushakov
 */
public abstract class AbstractRdfQuery implements RepositoryQuery {

    private static final Logger logger = LoggerFactory.getLogger(AbstractRdfQuery.class);

    private FedoraOperations fedoraOperations;

    private FedoraQueryMethod fedoraQueryMethod;

    public AbstractRdfQuery(FedoraOperations fedoraOperations, FedoraQueryMethod fedoraQueryMethod) {
        this.fedoraOperations = fedoraOperations;
        this.fedoraQueryMethod = fedoraQueryMethod;
    }

    @Override
    public Object execute(Object[] parameters) {
        ParametersParameterAccessor parameterAccessor = new ParametersParameterAccessor(fedoraQueryMethod.getParameters(),
                parameters);

        Query query = createQuery(parameterAccessor);

        if (fedoraQueryMethod.isPageQuery()) {
            Pageable pageable = parameterAccessor.getPageable();
            query.setLimit(pageable.getPageSize());
            query.setOffset(pageable.getOffset());
            logger.debug("Query: {}", query);
            return new PageImpl<>(fedoraOperations.query(query, fedoraQueryMethod.getEntityInformation().getJavaType()));
        } else {
            logger.debug("Query: {}", query);
            return fedoraOperations.query(query, fedoraQueryMethod.getEntityInformation().getJavaType());
        }
    }

    @Override
    public QueryMethod getQueryMethod() {
        return fedoraQueryMethod;
    }

    protected abstract Query createQuery(ParametersParameterAccessor parameterAccessor);
}
