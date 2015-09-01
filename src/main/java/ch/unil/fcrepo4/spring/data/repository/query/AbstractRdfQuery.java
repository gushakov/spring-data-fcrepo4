package ch.unil.fcrepo4.spring.data.repository.query;

import ch.unil.fcrepo4.spring.data.core.FedoraOperations;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;


// based on code from org.springframework.data.solr.repository.query.AbstractSolrQuery

/**
 * @author gushakov
 */
public abstract class AbstractRdfQuery implements RepositoryQuery {

    private FedoraOperations fedoraOperations;

    private FedoraQueryMethod fedoraQueryMethod;

    public AbstractRdfQuery(FedoraOperations fedoraOperations, FedoraQueryMethod fedoraQueryMethod) {
        this.fedoraOperations = fedoraOperations;
        this.fedoraQueryMethod = fedoraQueryMethod;
    }

    @Override
    public Object execute(Object[] parameters) {




        return null;
    }

    @Override
    public QueryMethod getQueryMethod() {
        return null;
    }
}
