package ch.unil.fcrepo4.spring.data.core.query;

import org.modeshape.jcr.ExecutionContext;
import org.modeshape.jcr.query.QueryBuilder;
import org.modeshape.jcr.query.model.TypeSystem;

/**
 * Patches MODE-2564 issue
 */
public class FedoraJcrSqlQueryBuilder extends QueryBuilder {

    public static final String VAR = "n";
    public static final String FROM = "fedora:Resource AS " + VAR;

    public FedoraJcrSqlQueryBuilder() {
        super(new ExecutionContext().getValueFactories().getTypeSystem());
    }

    @Override
    public QueryBuilder limit(int rowLimit) {
        this.limit = this.limit.withRowLimit(rowLimit);
        return this;
    }

    @Override
    public QueryBuilder offset(int offset) {
        this.limit  = this.limit.withOffset(offset);
        return this;
    }
}
