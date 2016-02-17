package ch.unil.fcrepo4.spring.data.core.query.sparql;

import com.hp.hpl.jena.sparql.expr.Expr;

/**
 * @author gushakov
 */
public interface Constraint extends Expression {

    Variable getVariable();

    Value getValue();

}
