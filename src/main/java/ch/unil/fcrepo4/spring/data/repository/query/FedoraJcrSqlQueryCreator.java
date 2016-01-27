package ch.unil.fcrepo4.spring.data.repository.query;

import ch.unil.fcrepo4.spring.data.core.mapping.FedoraPersistentProperty;
import org.modeshape.jcr.query.QueryBuilder;
import org.modeshape.jcr.query.model.Query;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;

import java.util.Iterator;

// based on code from org.springframework.data.solr.repository.query.SolrQueryCreator

/**
 * @author gushakov
 */
public class FedoraJcrSqlQueryCreator extends AbstractQueryCreator<Query, QueryBuilder.ConstraintBuilder> {

    private FedoraParameterAccessor parameterAccessor;
    private MappingContext<?, FedoraPersistentProperty> mappingContext;

    public FedoraJcrSqlQueryCreator(PartTree tree, FedoraParameterAccessor parameters, MappingContext<?, FedoraPersistentProperty> mappingContext) {
        super(tree, parameters);
        this.parameterAccessor = parameters;
        this.mappingContext = mappingContext;
    }

    @Override
    protected QueryBuilder.ConstraintBuilder create(Part part, Iterator<Object> iterator) {
        return null;
    }

    @Override
    protected QueryBuilder.ConstraintBuilder and(Part part, QueryBuilder.ConstraintBuilder base, Iterator<Object> iterator) {
        return null;
    }

    @Override
    protected QueryBuilder.ConstraintBuilder or(QueryBuilder.ConstraintBuilder base, QueryBuilder.ConstraintBuilder criteria) {
        return null;
    }

    @Override
    protected Query complete(QueryBuilder.ConstraintBuilder criteria, Sort sort) {
        return null;
    }
}
