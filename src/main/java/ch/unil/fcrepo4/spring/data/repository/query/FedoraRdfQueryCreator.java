package ch.unil.fcrepo4.spring.data.repository.query;

import ch.unil.fcrepo4.spring.data.core.mapping.FedoraPersistentProperty;
import ch.unil.fcrepo4.spring.data.core.mapping.SimpleFedoraPersistentProperty;
import ch.unil.fcrepo4.spring.data.core.query.SelectQuery;
import ch.unil.fcrepo4.spring.data.core.query.SelectQueryBuilder;
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
public class FedoraRdfQueryCreator extends AbstractQueryCreator<Query, SelectQuery> {
    enum Vars {s("s"), v("v");

        String varName;

        Vars(String varName) {
           this.varName = varName;
        }

        @Override
        public String toString() {
            return varName;
        }

        public String getVariableName() {
            return varName;
        }
    }

    private MappingContext<?, FedoraPersistentProperty> mappingContext;

    private SelectQueryBuilder queryBuilder;

    public FedoraRdfQueryCreator(PartTree tree, ParameterAccessor parameters, MappingContext<?, FedoraPersistentProperty> mappingContext) {
        super(tree, parameters);
        this.mappingContext = mappingContext;
        this.queryBuilder = new SelectQueryBuilder();
    }

    @Override
    protected SelectQuery create(Part part, Iterator<Object> iterator) {
        return queryBuilder.from(Vars.s.getVariableName(), getPersistentProperty(part).getUri(), iterator.next());
    }

    @Override
    protected SelectQuery and(Part part, SelectQuery base, Iterator<Object> iterator) {
        return base.from(Vars.s.getVariableName(), getPersistentProperty(part).getUri(), iterator.next());
    }

    @Override
    protected SelectQuery or(SelectQuery base, SelectQuery criteria) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    protected Query complete(SelectQuery criteria, Sort sort) {
        return criteria.build();
    }

    private SimpleFedoraPersistentProperty getPersistentProperty(Part part) {
        PersistentProperty<?> property = mappingContext.getPersistentEntity(part.getProperty().getOwningType()).getPersistentProperty(part.getProperty().getSegment());

        Assert.state(property != null, "No persistent property: " + part.getProperty());
        Assert.state(property instanceof SimpleFedoraPersistentProperty, "Property " + part.getProperty() + " is not of type SimpleFedoraPersistentProperty");

        return  (SimpleFedoraPersistentProperty) property;

    }
}
