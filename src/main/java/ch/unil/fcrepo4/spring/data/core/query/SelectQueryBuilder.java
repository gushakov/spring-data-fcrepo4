package ch.unil.fcrepo4.spring.data.core.query;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.expr.aggregate.Aggregator;
import com.hp.hpl.jena.sparql.expr.aggregate.AggregatorFactory;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;

import java.util.List;

/**
 * @author gushakov
 */
public class SelectQueryBuilder {

    private String resultVarName;
    private Aggregator countAggregator;

    public SelectQueryBuilder select(String varName) {
        resultVarName = varName;
        return this;
    }

    public SelectQueryBuilder count(boolean distinct){
        countAggregator = AggregatorFactory.createCount(distinct);
        return this;
    }

    public FromBlock from(Triple triple) {
        return new FromBlock(this, triple);
    }

    Query build(ElementGroup fromBlock) {
        Query query = QueryFactory.make();
        query.setQuerySelectType();
        if (countAggregator != null){
            query.addResultVar("count", query.allocAggregate(countAggregator));
        }
        else {
            query.addResultVar(resultVarName);
        }
        ElementGroup pattern = new ElementGroup();
        List<Element> elements = fromBlock.getElements();
        pattern.addElement(elements.get(0));
        if (elements.size() == 2){
            pattern.addElement(elements.get(1));
        }
        query.setQueryPattern(pattern);
        return query;
    }
}
