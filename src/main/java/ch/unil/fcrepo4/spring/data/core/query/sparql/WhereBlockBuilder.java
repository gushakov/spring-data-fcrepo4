package ch.unil.fcrepo4.spring.data.core.query.sparql;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;

/**
 * @author gushakov
 */
public class WhereBlockBuilder extends AbstractSparqlSelectQueryBuilder implements WhereBlock {

    public WhereBlockBuilder(SparqlQueryBuildContext context, Expr filterExpr) {
        super(context);
        ElementFilter whereFilter = new ElementFilter(filterExpr);
        context.setWhereFilter(whereFilter);
    }
}
