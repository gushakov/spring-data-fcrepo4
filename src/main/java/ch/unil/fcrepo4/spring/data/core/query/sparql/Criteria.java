package ch.unil.fcrepo4.spring.data.core.query.sparql;

import ch.unil.fcrepo4.spring.data.core.mapping.FedoraResourcePersistentProperty;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.NodeValue;

import java.util.List;

/**
 * @author gushakov
 */
public interface Criteria {

    void substitutePropertyNodeValue(FedoraResourcePersistentProperty property, NodeValue nodeValue);

    BasicPattern buildBgp();

    String getProjectionVariableName();

    void addGreaterThanFilter(FedoraResourcePersistentProperty property, NodeValue nodeValue);

    List<Expr> getFilters();

    Criteria and(Criteria otherCriteria);
}
