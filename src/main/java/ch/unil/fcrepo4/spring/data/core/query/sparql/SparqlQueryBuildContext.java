package ch.unil.fcrepo4.spring.data.core.query.sparql;

import ch.unil.fcrepo4.spring.data.core.convert.RdfDatatypeConverter;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.expr.aggregate.Aggregator;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;

import java.util.List;

/**
 * @author gushakov
 */
public interface SparqlQueryBuildContext {
    String COUNT_RESULTS_VARIABLE = "count";

    void setDatatypeConverter(RdfDatatypeConverter converter);

    RdfDatatypeConverter getDatatypeConverter();

    PrefixMap getPrefixMap();

    Aggregator getCountAggregator();

    void setCountAggregator(Aggregator aggregator);

    String getResultVarName();

    void setResultVarName(String resultVarName);

    List<ElementTriplesBlock> getFromBlocks();

    void addFromBlock(ElementTriplesBlock fromBlock);

    ElementFilter getWhereFilter();

    void setWhereFilter(ElementFilter whereFilter);



}
