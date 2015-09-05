package ch.unil.fcrepo4.spring.data.core.query.sparql;

import ch.unil.fcrepo4.utils.Utils;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;

/**
 * @author gushakov
 */
public abstract class AbstractSparqlSelectQueryBuilder implements SelectQueryBuilder {

    protected SparqlQueryBuildContext context;

    public AbstractSparqlSelectQueryBuilder(SparqlQueryBuildContext context) {
        this.context = context;
    }

    @Override
    public Query build() {
        Query query = QueryFactory.make();
        query.setQuerySelectType();
        query.setPrefixMapping(context.getPrefixMap().getPrefixMapping());
        if (context.getCountAggregator() != null) {
            query.addResultVar(SparqlQueryBuildContext.COUNT_RESULTS_VARIABLE, query.allocAggregate(context.getCountAggregator()));
        } else {
            if (context.getResultVarName() != null) {
                query.addResultVar(context.getResultVarName());
            } else {
                query.addResultVar(Var.ANON.getVarName());
            }
        }
        ElementGroup pattern = new ElementGroup();

        //TODO:

        context.getFromTriples().iterator().forEachRemaining(pattern::addTriplePattern);

        query.setQueryPattern(pattern);
        return query;

    }
}
