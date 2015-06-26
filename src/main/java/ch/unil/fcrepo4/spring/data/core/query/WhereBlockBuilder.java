package ch.unil.fcrepo4.spring.data.core.query;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;

/**
 * @author gushakov
 */
public class WhereBlockBuilder extends SelectQueryBuilder implements WhereBlock {

    public WhereBlockBuilder(QueryBuildContext context, Expr filterExpr) {
        super(context);
        ElementFilter whereFilter = new ElementFilter(filterExpr);
        context.setWhereFilter(whereFilter);
    }
}
