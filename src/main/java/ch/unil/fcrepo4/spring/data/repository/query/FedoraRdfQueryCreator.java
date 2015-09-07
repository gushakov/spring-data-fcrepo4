package ch.unil.fcrepo4.spring.data.repository.query;

import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraPersistentProperty;
import ch.unil.fcrepo4.spring.data.core.mapping.SimpleFedoraPersistentProperty;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.util.Assert;

import java.util.Iterator;

// based on code from org.springframework.data.solr.repository.query.SolrQueryCreator

/**
 * @author gushakov
 */
public class FedoraRdfQueryCreator extends AbstractQueryCreator<Query, Element> {

    private MappingContext<?, FedoraPersistentProperty> mappingContext;
    private RdfDatatypeConverter rdfDatatypeConverter;
    private SelectQueryBuildContext buildContext;

    public FedoraRdfQueryCreator(PartTree tree, ParameterAccessor parameters, MappingContext<?, FedoraPersistentProperty> mappingContext, RdfDatatypeConverter rdfDatatypeConverter) {
        super(tree, parameters);
        this.mappingContext = mappingContext;
        this.rdfDatatypeConverter = rdfDatatypeConverter;
        this.buildContext = new SelectQueryBuildContext();
    }


    @Override
    protected Element create(Part part, Iterator<Object> iterator) {
        ElementTriplesBlock triples = new ElementTriplesBlock();
        triples.addTriple(new Triple(NodeFactory.createVariable(buildContext.nextVarName()),
                NodeFactory.createURI(getPersistentProperty(part).getUri()),
                rdfDatatypeConverter.encodeLiteralValue(iterator.next())));

        return triples;
    }

    @Override
    protected Element and(Part part, Element base, Iterator<Object> iterator) {
        ElementTriplesBlock triples = (ElementTriplesBlock) base;
        triples.addTriple(new Triple(NodeFactory.createVariable(buildContext.getCurrentVarName()),
                NodeFactory.createURI(getPersistentProperty(part).getUri()),
                rdfDatatypeConverter.encodeLiteralValue(iterator.next())));
        return base;
    }

    @Override
    protected Element or(Element base, Element criteria) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    protected Query complete(Element criteria, Sort sort) {
        Query query = QueryFactory.create();
        query.setQuerySelectType();
        query.addResultVar("s");
        query.setQueryPattern(criteria);
        return query;
    }

    private class SelectQueryBuildContext {

        private int varIndex = 0;

        public String getCurrentVarName() {
            return "v" + varIndex;
        }

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

}
