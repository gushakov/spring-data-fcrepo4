package ch.unil.fcrepo4.spring.data.repository.query;

import ch.unil.fcrepo4.spring.data.core.FedoraOperations;
import com.hp.hpl.jena.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;


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
        FedoraParameterAccessor parameterAccessor = new FedoraParametersParameterAccessor(fedoraQueryMethod.getParameters(),
                parameters);
        Object result;
        if (fedoraQueryMethod.isPageQuery()) {
            // check if this is a first page of a paging query and we need to run a counting query
            // for the total number of results
            if (parameterAccessor.needsCount()) {
                parameterAccessor.setTotalCount(fedoraOperations.count(createQuery(parameterAccessor)));
            }
            result = new PageImpl<>(fedoraOperations.query(createQuery(parameterAccessor),
                    fedoraQueryMethod.getEntityInformation().getJavaType()), parameterAccessor.getPageable(), parameterAccessor.getTotalCount());
        } else {
            result = fedoraOperations.query(createQuery(parameterAccessor),
                    fedoraQueryMethod.getEntityInformation().getJavaType());
        }
        return result;
    }

    @Override
    public QueryMethod getQueryMethod() {
        return fedoraQueryMethod;
    }

    protected abstract Query createQuery(FedoraParameterAccessor parameterAccessor);
}
