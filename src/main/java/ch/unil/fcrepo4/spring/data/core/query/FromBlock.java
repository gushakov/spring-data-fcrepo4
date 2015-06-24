package ch.unil.fcrepo4.spring.data.core.query;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;

/**
 * @author gushakov
 */
public class FromBlock {

    private ElementTriplesBlock bgp;
    private SelectQueryBuilder selectQuery;

    public FromBlock(SelectQueryBuilder selectQuery, Triple triple) {
        this.selectQuery = selectQuery;
        bgp = new ElementTriplesBlock();
        bgp.addTriple(triple);
    }

    public WhereBlock where(Expr expr){
        return new WhereBlock(this, expr);
    }

    Query build(ElementFilter filter){
       return selectQuery.build(buildElementGroup(filter));
    }

    public Query build(){
        return selectQuery.build(buildElementGroup(null));
    }

    private ElementGroup buildElementGroup(ElementFilter filter){
        ElementGroup elementGroup = new ElementGroup();
        elementGroup.addElement(bgp);
        if (filter != null){
            elementGroup.addElement(filter);
        }
        return elementGroup;
    }
}
