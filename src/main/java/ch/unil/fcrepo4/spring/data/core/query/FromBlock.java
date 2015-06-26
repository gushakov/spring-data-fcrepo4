package ch.unil.fcrepo4.spring.data.core.query;

import com.hp.hpl.jena.sparql.expr.Expr;

/**
 * @author gushakov
 */
public interface FromBlock extends SelectQuery {

    <T extends SelectQuery> T where(Expr filterExpr);

}
