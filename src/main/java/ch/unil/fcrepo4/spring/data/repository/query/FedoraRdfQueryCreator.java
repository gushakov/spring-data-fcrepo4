package ch.unil.fcrepo4.spring.data.repository.query;

import ch.unil.fcrepo4.spring.data.core.mapping.FedoraPersistentProperty;
import ch.unil.fcrepo4.spring.data.core.mapping.SimpleFedoraPersistentProperty;
import ch.unil.fcrepo4.spring.data.core.query.sparql.FromBlock;
import ch.unil.fcrepo4.spring.data.core.query.sparql.SelectBlock;
import ch.unil.fcrepo4.spring.data.core.query.sparql.SparqlSelectQueryBuilder;
import com.hp.hpl.jena.query.Query;
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
public class FedoraRdfQueryCreator extends AbstractQueryCreator<Query, FromBlock> {

    private MappingContext<?, FedoraPersistentProperty> mappingContext;

    private SparqlSelectQueryBuilder queryBuilder;

    public FedoraRdfQueryCreator(PartTree tree, ParameterAccessor parameters, MappingContext<?, FedoraPersistentProperty> mappingContext) {
        super(tree, parameters);
        this.mappingContext = mappingContext;
        this.queryBuilder = new SparqlSelectQueryBuilder();
    }

    @Override
    protected FromBlock create(Part part, Iterator<Object> iterator) {
        return queryBuilder.select("s").from("s", getPersistentProperty(part).getUri(), iterator.next());
    }

    @Override
    protected FromBlock and(Part part, FromBlock base, Iterator<Object> iterator) {
        return base.and("s", getPersistentProperty(part).getUri(), iterator.next());
    }

    @Override
    protected FromBlock or(FromBlock base, FromBlock criteria) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    protected Query complete(FromBlock criteria, Sort sort) {
        return criteria.build();
    }

    private SimpleFedoraPersistentProperty getPersistentProperty(Part part) {
        PersistentProperty<?> property = mappingContext.getPersistentEntity(part.getProperty().getOwningType()).getPersistentProperty(part.getProperty().getSegment());

        Assert.state(property != null, "No persistent property: " + part.getProperty());
        Assert.state(property instanceof SimpleFedoraPersistentProperty, "Property " + part.getProperty() + " is not of type SimpleFedoraPersistentProperty");

        return  (SimpleFedoraPersistentProperty) property;

    }
}
