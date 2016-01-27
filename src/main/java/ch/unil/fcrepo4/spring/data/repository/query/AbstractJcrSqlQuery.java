package ch.unil.fcrepo4.spring.data.repository.query;

// based on code from org.springframework.data.solr.repository.query.AbstractSolrQuery

import ch.unil.fcrepo4.spring.data.core.FedoraOperations;
import org.modeshape.jcr.query.model.Query;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;

/**
 * @author gushakov
 */
public abstract class AbstractJcrSqlQuery implements RepositoryQuery {

    private FedoraOperations fedoraOperations;

    private FedoraQueryMethod fedoraQueryMethod;

    public AbstractJcrSqlQuery(FedoraOperations fedoraOperations, FedoraQueryMethod fedoraQueryMethod) {
        this.fedoraOperations = fedoraOperations;
        this.fedoraQueryMethod = fedoraQueryMethod;
    }

    @Override
    public Object execute(Object[] parameters) {
        FedoraParameterAccessor parameterAccessor = new FedoraParametersParameterAccessor(fedoraQueryMethod.getParameters(),
                parameters);

        return fedoraOperations.query(createQuery(parameterAccessor),
                fedoraQueryMethod.getEntityInformation().getJavaType());
    }

    @Override
    public QueryMethod getQueryMethod() {
        return fedoraQueryMethod;
    }

    protected abstract Query createQuery(FedoraParameterAccessor parameterAccessor);
}
