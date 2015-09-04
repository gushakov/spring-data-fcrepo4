package ch.unil.fcrepo4.spring.data.core.query.sparql;

import ch.unil.fcrepo4.spring.data.core.convert.RdfDatatypeConverter;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.expr.aggregate.Aggregator;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;

import java.util.List;

/**
 * @author gushakov
 */
public interface SparqlQueryBuildContext {
    String COUNT_RESULTS_VARIABLE = "count";

    RdfDatatypeConverter getRdfDatatypeConverter();

    void setDatatypeConverter(RdfDatatypeConverter converter);

    PrefixMap getPrefixMap();

    void setPrefixMap(PrefixMap prefixMap);

    Aggregator getCountAggregator();

    void setCountAggregator(Aggregator aggregator);

    String getResultVarName();

    void setResultVarName(String resultVarName);

    BasicPattern getFromTriples();

    void addFromTriple(Triple fromTriple);

    ElementFilter getWhereFilter();

    void setWhereFilter(ElementFilter whereFilter);



}
