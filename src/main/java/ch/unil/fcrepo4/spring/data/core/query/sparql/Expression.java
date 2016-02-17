package ch.unil.fcrepo4.spring.data.core.query.sparql;

import com.hp.hpl.jena.sparql.expr.Expr;

/**
 * @author gushakov
 */
public interface Expression {

    Expr getExpression();
}
