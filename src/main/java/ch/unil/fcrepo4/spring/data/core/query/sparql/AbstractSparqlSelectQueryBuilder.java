package ch.unil.fcrepo4.spring.data.core.query.sparql;

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
        if (context.getCountAggregator() != null) {
            query.addResultVar(SparqlQueryBuildContext.COUNT_RESULTS_VARIABLE, query.allocAggregate(context.getCountAggregator()));
        } else {
            if (context.getResultVarName() != null) {
                query.addResultVar(context.getResultVarName());
            } else {
                query.addResultVar(Var.ANON);
            }
        }
        ElementGroup pattern = new ElementGroup();
//        context.getFromBlocks().ifPresent(bs -> bs.stream().forEach(pattern::addElement));
//        context.getWhereFilter().ifPresent(pattern::addElementFilter);
        query.setQueryPattern(pattern);
        return query;

    }
}
