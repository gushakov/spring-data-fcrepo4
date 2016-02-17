package ch.unil.fcrepo4.spring.data.core.query.sparql;

import com.hp.hpl.jena.sparql.expr.Expr;

/**
 * @author gushakov
 */
public abstract class Predicate implements Constraint {

    private Expr expr;

    private Variable variable;

    private Value value;

    public Predicate(Variable variable, Value value) {
        this.variable = variable;
        this.value = value;
        expr = makeExpression();
    }

    protected Expr makeExpression(){
       return null;
    }

    @Override
    public Variable getVariable() {
        return variable;
    }

    @Override
    public Value getValue() {
        return value;
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
