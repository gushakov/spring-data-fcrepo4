package ch.unil.fcrepo4.spring.data.core.query.sparql;

import ch.unil.fcrepo4.spring.data.core.convert.RdfDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.convert.XsdDatatypeConverter;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.expr.aggregate.Aggregator;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;

/**
 * @author gushakov
 */
public class SelectQueryBuildContextHolder implements SparqlQueryBuildContext {

    private RdfDatatypeConverter rdfDatatypeConverter;

    private PrefixMap prefixMap;

    private Aggregator countAggregator;

    private String resultVarName;

    private BasicPattern fromTriples;

    private ElementFilter whereFilter;

    public SelectQueryBuildContextHolder() {
        init();
    }

    public SelectQueryBuildContextHolder(PrefixMap prefixMap) {
        this.prefixMap = prefixMap;
        init();
    }

    public SelectQueryBuildContextHolder(RdfDatatypeConverter rdfDatatypeConverter) {
        this.rdfDatatypeConverter = rdfDatatypeConverter;
        init();
    }

    public SelectQueryBuildContextHolder(PrefixMap prefixMap, RdfDatatypeConverter rdfDatatypeConverter) {
        this.rdfDatatypeConverter = rdfDatatypeConverter;
        this.prefixMap = prefixMap;
        init();
    }

    private void init() {
        if (prefixMap == null) {
            prefixMap = new PrefixMap();
        }

        if (fromTriples == null) {
            fromTriples = new BasicPattern();
        }

        if (rdfDatatypeConverter == null) {
            rdfDatatypeConverter = new XsdDatatypeConverter();
        }
    }

    @Override
    public RdfDatatypeConverter getRdfDatatypeConverter() {
        return rdfDatatypeConverter;
    }

    @Override
    public void setDatatypeConverter(RdfDatatypeConverter converter) {
        this.rdfDatatypeConverter = converter;
    }

    @Override
    public PrefixMap getPrefixMap() {
        return prefixMap;
    }

    @Override
    public void setPrefixMap(PrefixMap prefixMap) {
        this.prefixMap = prefixMap;
    }


    @Override
    public Aggregator getCountAggregator() {
        return countAggregator;
    }

    @Override
    public void setCountAggregator(Aggregator aggregator) {
        this.countAggregator = aggregator;
    }

    @Override
    public String getResultVarName() {
        return resultVarName;
    }

    @Override
    public void setResultVarName(String resultVarName) {
        this.resultVarName = resultVarName;
    }

    @Override
    public BasicPattern getFromTriples() {
        return fromTriples;
    }

    @Override
    public void addFromTriple(Triple fromTriple) {
        fromTriples.add(fromTriple);
    }


    @Override
    public ElementFilter getWhereFilter() {
        return whereFilter;
    }

    @Override
    public void setWhereFilter(ElementFilter whereFilter) {
        this.whereFilter = whereFilter;
    }

}
