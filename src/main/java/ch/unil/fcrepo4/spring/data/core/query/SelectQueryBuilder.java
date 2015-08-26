package ch.unil.fcrepo4.spring.data.core.query;

import ch.unil.fcrepo4.spring.data.core.convert.RdfDatatypeConverter;
import com.hp.hpl.jena.datatypes.xsd.impl.XSDBaseNumericType;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.aggregate.AggregatorFactory;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * @author gushakov
 */
public class SelectQueryBuilder implements SelectQuery {
    private static final String COUNT_RESULTS_VARIABLE = "count";

    protected QueryBuildContext context;

    public static Triple t(String varName, String predicateUri, int intValue) {
        return new Triple(Var.alloc(varName), NodeFactory.createURI(predicateUri),
                NodeFactory.createLiteral(Integer.toString(intValue), new XSDBaseNumericType(XSD.integer.getLocalName())));
    }

    public SelectQueryBuilder() {
        this.context = new DefaultQueryBuildContext();
    }

    public SelectQueryBuilder(PrefixMap prefixMap){
        this.context = new DefaultQueryBuildContext(prefixMap);
    }

    public SelectQueryBuilder(QueryBuildContext context) {
        this.context = context;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends SelectQuery> T withDatatypeConverter(RdfDatatypeConverter rdfDatatypeConverter) {
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends SelectQuery> T select(String varName) {
        context.setResultVarName(varName);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends SelectQuery> T count(boolean distinct) {
        context.setCountAggregator(AggregatorFactory.createCount(distinct));
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends FromBlock> T from(String varName, String predicateUri, Object value) {
        return (T) new FromBlockBuilder(context, new BgpFragmentBuilder(context.getPrefixMap(), varName, predicateUri, value, context.getDatatypeConverter()));
    }

    @Override
    public Query build() {
        Query query = QueryFactory.make();
        query.setQuerySelectType();
        if (context.getCountAggregator().isPresent()) {
            query.addResultVar(COUNT_RESULTS_VARIABLE, query.allocAggregate(context.getCountAggregator().get()));
        }
        else {
            context.getResultVarName().ifPresent(query::addResultVar);
        }
        ElementGroup pattern = new ElementGroup();
        context.getFromBlocks().ifPresent(bs -> bs.stream().forEach(pattern::addElement));
        context.getWhereFilter().ifPresent(pattern::addElementFilter);
        query.setQueryPattern(pattern);
        return query;
    }
}
