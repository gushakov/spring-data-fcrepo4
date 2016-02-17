package ch.unil.fcrepo4.spring.data.core.query.sparql;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;

/**
 * @author gushakov
 */
public class NamedVariable implements Variable {

    private String name;

    private ExprVar expr;

    public NamedVariable(String name) {
        this.name = name;
        this.expr = new ExprVar(name);
    }

    @Override
    public String getName() {
        return name;
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
