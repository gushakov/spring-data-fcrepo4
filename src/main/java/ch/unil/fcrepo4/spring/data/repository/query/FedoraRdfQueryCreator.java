package ch.unil.fcrepo4.spring.data.repository.query;

import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraPersistentProperty;
import ch.unil.fcrepo4.spring.data.core.mapping.SimpleFedoraPersistentProperty;
import ch.unil.fcrepo4.utils.Utils;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.expr.*;
import com.hp.hpl.jena.sparql.syntax.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// based on code from org.springframework.data.solr.repository.query.SolrQueryCreator

// based on code from https://tmarkus.wordpress.com/2010/04/01/creating-sparql-queries-programmatically-in-java/

/**
 * @author gushakov
 */
public class FedoraRdfQueryCreator extends AbstractQueryCreator<Query, Element> {

    private MappingContext<?, FedoraPersistentProperty> mappingContext;
    private RdfDatatypeConverter rdfDatatypeConverter;
    private VarNameBuilder varNameBuilder;

    public FedoraRdfQueryCreator(PartTree tree, ParameterAccessor parameters, MappingContext<?, FedoraPersistentProperty> mappingContext, RdfDatatypeConverter rdfDatatypeConverter) {
        super(tree, parameters);
        this.mappingContext = mappingContext;
        this.rdfDatatypeConverter = rdfDatatypeConverter;
        this.varNameBuilder = new VarNameBuilder();
    }


    @Override
    protected Element create(Part part, Iterator<Object> iterator) {
        ElementGroup group = new ElementGroup();
        String varName = varNameBuilder.nextVarName();
        Object value = iterator.next();
        Expr valueFilterExpr = getFilterExpression(part, value);
        Triple triple = createTriple(part, varName, value, valueFilterExpr);
        group.addTriplePattern(triple);
        if (valueFilterExpr!=null){
            group.addElementFilter(new ElementFilter(valueFilterExpr));
        }
        return group;
    }

    @Override
    protected Element and(Part part, Element base, Iterator<Object> iterator) {
        ElementGroup baseGroup = (ElementGroup) base;
        ElementGroup group = new ElementGroup();

        String baseVarName = null;
        Expr baseFilterExpr = null;

        for (Element element : baseGroup.getElements()) {
            if (element instanceof ElementTriplesBlock) {
                if (baseVarName == null){
                    baseVarName = ((ElementTriplesBlock) element).patternElts().next().getSubject().getName();
                }
                group.addElement(element);
            } else {
                if (element instanceof ElementFilter) {
                    baseFilterExpr = ((ElementFilter) element).getExpr();
                }
            }
        }

        if (baseVarName == null) {
            throw new IllegalStateException("Cannot resolve variable name");
        }

        Object value = iterator.next();
        Expr valueFilterExpr = getFilterExpression(part, value);
        Triple triple = createTriple(part, baseVarName, value, valueFilterExpr);
        group.addTriplePattern(triple);

        if (baseFilterExpr != null) {
            group.addElementFilter(new ElementFilter(new E_LogicalAnd(baseFilterExpr, valueFilterExpr)));
        } else {
            group.addElementFilter(new ElementFilter(valueFilterExpr));
        }

        return group;
    }

    @Override
    protected Element or(Element base, Element criteria) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    protected Query complete(Element criteria, Sort sort) {
        final List<String> resultVars = new ArrayList<>();

        if (criteria instanceof ElementGroup){
            ElementGroup group = (ElementGroup) criteria;
            ElementTriplesBlock triples = Utils.getTriples(group);
            resultVars.add(triples.patternElts().next().getSubject().getName());
        }
        else {
            throw new UnsupportedOperationException("Not implemented yet");
        }

        Query query = QueryFactory.create();
        query.setQuerySelectType();

        for (String varName: resultVars){
            query.addResultVar(varName);
        }

        query.setQueryPattern(criteria);
        return query;
    }

    private class VarNameBuilder {

        private int varIndex = 0;

        public String nextVarName() {
            return "v" + (++varIndex);
        }
    }


    private SimpleFedoraPersistentProperty getPersistentProperty(Part part) {
        PersistentProperty<?> property = mappingContext.getPersistentEntity(part.getProperty().getOwningType()).getPersistentProperty(part.getProperty().getSegment());

        Assert.state(property != null, "No persistent property: " + part.getProperty());
        Assert.state(property instanceof SimpleFedoraPersistentProperty, "Property " + part.getProperty() + " is not of type SimpleFedoraPersistentProperty");

        if (!(property instanceof SimpleFedoraPersistentProperty)) {
            throw new IllegalStateException("Property " + property + " is not a SimpleFedoraPersistentProperty");
        }

        return (SimpleFedoraPersistentProperty) property;

    }

    private Expr getFilterExpression(Part part, Object value) {
        switch (part.getType()) {
            case GREATER_THAN:
                return new E_GreaterThan(new ExprVar(varNameBuilder.nextVarName()), rdfDatatypeConverter.encodeExpressionValue(value));
            case GREATER_THAN_EQUAL:
                return new E_GreaterThanOrEqual(new ExprVar(varNameBuilder.nextVarName()), rdfDatatypeConverter.encodeExpressionValue(value));
            default:
                return null;
        }
    }

    private Triple createTriple(Part part, String varName, Object value, Expr valueFilterExpr){
        Triple triple;
        Node subjectVar = NodeFactory.createVariable(varName);
        Node predicate =NodeFactory.createURI(getPersistentProperty(part).getUri());
        if (valueFilterExpr != null) {
            triple = new Triple(NodeFactory.createVariable(varName),
                    predicate,
                    NodeFactory.createVariable(valueFilterExpr.getFunction().getArg(1).getVarName()));
        } else {
            triple = new Triple(subjectVar,
                    predicate,
                    rdfDatatypeConverter.encodeLiteralValue(value));

        }
        return triple;
    }

}
