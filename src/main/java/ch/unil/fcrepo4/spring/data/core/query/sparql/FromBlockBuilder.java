package ch.unil.fcrepo4.spring.data.core.query.sparql;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;

/**
 * @author gushakov
 */
public class FromBlockBuilder extends AbstractSparqlSelectQueryBuilder implements FromBlock {

    public FromBlockBuilder(SparqlQueryBuildContext context, BgpFragment bgp) {
        super(context);
        context.addFromTriple(bgp.getTriple());
    }


    @Override
    public FromBlock and(String varName, String predicateUri, Object value) {
        context.addFromTriple(new BgpFragmentBuilder(context, varName, predicateUri, value).getTriple());
        return this;
    }

    @Override
    public WhereBlock where(Expr filterExpr) {
        return new WhereBlockBuilder(context, filterExpr);
    }
}
