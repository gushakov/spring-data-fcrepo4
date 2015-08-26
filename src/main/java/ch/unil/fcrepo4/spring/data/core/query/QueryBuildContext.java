package ch.unil.fcrepo4.spring.data.core.query;

import ch.unil.fcrepo4.spring.data.core.convert.RdfDatatypeConverter;
import com.hp.hpl.jena.sparql.expr.aggregate.Aggregator;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;

import java.util.List;
import java.util.Optional;

/**
 * @author gushakov
 */
public interface QueryBuildContext {

    void setDatatypeConverter(RdfDatatypeConverter converter);

    RdfDatatypeConverter getDatatypeConverter();

    PrefixMap getPrefixMap();

    Optional<Aggregator> getCountAggregator();

    void setCountAggregator(Aggregator aggregator);

    Optional<String> getResultVarName();

    void setResultVarName(String resultVarName);

    Optional<List<ElementTriplesBlock>> getFromBlocks();

    void addFromBlock(ElementTriplesBlock fromBlock);

    Optional<ElementFilter> getWhereFilter();

    void setWhereFilter(ElementFilter whereFilter);

}
