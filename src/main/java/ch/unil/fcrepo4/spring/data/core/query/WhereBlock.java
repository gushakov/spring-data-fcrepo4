package ch.unil.fcrepo4.spring.data.core.query;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;

/**
 * @author gushakov
 */
public class WhereBlock {

    private FromBlock fromBlock;
    private ElementFilter filter;

    public WhereBlock(FromBlock fromBlock, Expr expr) {
        this.fromBlock = fromBlock;
        filter = new ElementFilter(expr);
    }

    public Query build(){
        return fromBlock.build(filter);
    }
}
