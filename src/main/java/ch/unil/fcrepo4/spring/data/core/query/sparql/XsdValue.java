package ch.unil.fcrepo4.spring.data.core.query.sparql;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.NodeValue;

/**
 * @author gushakov
 */
public class XsdValue implements Value {
    private NodeValue expr;

    public XsdValue(NodeValue nodeValue) {
        this.expr = nodeValue;
    }

    @Override
    public Expr getExpression() {
        return expr;
    }

    @Override
    public String toString() {
        return expr.toString();
    }
}
