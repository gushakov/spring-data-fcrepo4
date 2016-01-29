package ch.unil.fcrepo4.spring.data.repository.query;

import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraObjectPersistentEntity;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraPersistentProperty;
import ch.unil.fcrepo4.spring.data.core.mapping.SimpleFedoraResourcePersistentProperty;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import org.modeshape.jcr.ExecutionContext;
import org.modeshape.jcr.query.QueryBuilder;
import org.modeshape.jcr.query.model.Query;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Iterator;

// based on code from org.springframework.data.solr.repository.query.SolrQueryCreator

/**
 * @author gushakov
 */
public class FedoraJcrSqlQueryCreator extends AbstractQueryCreator<Query, QueryBuilder.ConstraintBuilder> {

    private static final String FEDORA_RESOURCE_NODE = "fedora:Resource";
    private static final String DEFAULT_ALIAS = "n";

    // see org.fcrepo.kernel.modeshape.rdf.converters.ValueConverter.RdfLiteralJcrValueBuilder
    private static final String LITERAL_SEPARATOR = "\30^^\30";


    private QueryBuilder queryBuilder;
    private FedoraParameterAccessor parameterAccessor;
    private MappingContext<?, FedoraPersistentProperty> mappingContext;
    private RdfDatatypeConverter rdfDatatypeConverter;

    public FedoraJcrSqlQueryCreator(PartTree tree, FedoraParameterAccessor parameters, MappingContext<?, FedoraPersistentProperty> mappingContext, RdfDatatypeConverter rdfDatatypeConverter) {
        super(tree, parameters);
        queryBuilder = new QueryBuilder(new ExecutionContext().getValueFactories().getTypeSystem());
        this.parameterAccessor = parameters;
        this.mappingContext = mappingContext;
        this.rdfDatatypeConverter = rdfDatatypeConverter;
    }

    @Override
    protected QueryBuilder.ConstraintBuilder create(Part part, Iterator<Object> iterator) {
        return setConstraintProperty(queryBuilder.selectStar()
                        .from(FEDORA_RESOURCE_NODE + " AS " + DEFAULT_ALIAS)
                        .where().isBelowPath(DEFAULT_ALIAS, "/" + getNamespace(part)),
                part, iterator.next());
    }

    @Override
    protected QueryBuilder.ConstraintBuilder and(Part part, QueryBuilder.ConstraintBuilder base, Iterator<Object> iterator) {
        return base;
    }

    @Override
    protected QueryBuilder.ConstraintBuilder or(QueryBuilder.ConstraintBuilder base, QueryBuilder.ConstraintBuilder criteria) {
        return base;
    }

    @Override
    protected Query complete(QueryBuilder.ConstraintBuilder criteria, Sort sort) {
        return (Query) criteria.end().query();
    }

    private String getNamespace(Part part){
        FedoraObjectPersistentEntity persistentEntity = (FedoraObjectPersistentEntity) mappingContext.getPersistentEntity(part.getProperty().getOwningType());
        return persistentEntity.getNamespace();
    }

    private SimpleFedoraResourcePersistentProperty getPersistentProperty(Part part) {
        PersistentProperty<?> property = mappingContext.getPersistentEntity(part.getProperty().getOwningType()).getPersistentProperty(part.getProperty().getSegment());

        Assert.state(property != null, "No persistent property: " + part.getProperty());
        Assert.state(property instanceof SimpleFedoraResourcePersistentProperty, "Property " + part.getProperty() + " is not of type SimpleFedoraPersistentProperty");

        if (!(property instanceof SimpleFedoraResourcePersistentProperty)) {
            throw new IllegalStateException("Property " + property + " is not a SimpleFedoraPersistentProperty");
        }

        return (SimpleFedoraResourcePersistentProperty) property;

    }

    private QueryBuilder.ConstraintBuilder setConstraintProperty(QueryBuilder.ConstraintBuilder constraintBuilder, Part part, Object value) {
        SimpleFedoraResourcePersistentProperty property = getPersistentProperty(part);
        switch (part.getType()) {
            case SIMPLE_PROPERTY:
                return constraintBuilder.propertyValue(DEFAULT_ALIAS, serializeJcrProperty(property))
                        .isEqualTo(serializeJcrValue(property, value));
            default:
                throw new UnsupportedOperationException("Expressions containing " +
                        Arrays.toString(part.getType().getKeywords().toArray()) + " are not supported yet");
        }
    }

    private String serializeJcrProperty(SimpleFedoraResourcePersistentProperty property) {
        return property.getPrefix() + ":" + property.getName();
    }

    private String serializeJcrValue(SimpleFedoraResourcePersistentProperty property, Object value) {
        RDFDatatype rdfDatatype = rdfDatatypeConverter.convert(property.getType());
        return value.toString() + LITERAL_SEPARATOR + rdfDatatype.getURI();


    }

}
