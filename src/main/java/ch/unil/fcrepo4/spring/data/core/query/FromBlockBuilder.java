package ch.unil.fcrepo4.spring.data.core.query;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;

/**
 * @author gushakov
 */
public class FromBlockBuilder extends SelectQueryBuilder implements FromBlock {

    public FromBlockBuilder(QueryBuildContext context, BgpFragment bgp) {
        super(context);
        ElementTriplesBlock triplesBlock = new ElementTriplesBlock();
        triplesBlock.addTriple(bgp.getTriple());
        context.addFromBlock(triplesBlock);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends SelectQuery> T where(Expr filterExpr) {
        return (T) new WhereBlockBuilder(context, filterExpr);
    }

}
