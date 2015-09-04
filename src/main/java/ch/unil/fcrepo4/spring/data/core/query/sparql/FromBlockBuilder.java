package ch.unil.fcrepo4.spring.data.core.query.sparql;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;

/**
 * @author gushakov
 */
public class FromBlockBuilder extends AbstractSparqlSelectQueryBuilder implements FromBlock {

    public FromBlockBuilder(SparqlQueryBuildContext context, BgpFragment bgp) {
        super(context);
        ElementTriplesBlock triplesBlock = new ElementTriplesBlock();
        triplesBlock.addTriple(bgp.getTriple());
        context.addFromBlock(triplesBlock);
    }


    @Override
    public FromBlock and(String varName, String predicateUri, Object value) {
        //TODO add triple to the triples block
        return this;
    }

    @Override
    public WhereBlock where(Expr filterExpr) {
        return new WhereBlockBuilder(context, filterExpr);
    }
}
