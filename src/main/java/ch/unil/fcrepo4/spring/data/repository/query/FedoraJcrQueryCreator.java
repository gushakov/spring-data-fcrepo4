package ch.unil.fcrepo4.spring.data.repository.query;

import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraObjectPersistentEntity;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraPersistentProperty;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraResourcePersistentProperty;
import ch.unil.fcrepo4.spring.data.core.query.qom.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PropertyPath;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.data.util.TypeInformation;

import javax.jcr.query.qom.QueryObjectModelConstants;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author gushakov
 */
public class FedoraJcrQueryCreator extends AbstractQueryCreator<Query, Constraint> {

    private ParameterAccessor parameterAccessor;
    private MappingContext<?, FedoraPersistentProperty> mappingContext;
    private RdfDatatypeConverter rdfDatatypeConverter;


    private Map<TypeInformation<?>, Selector> selectors;

    public FedoraJcrQueryCreator(PartTree tree, ParameterAccessor parameters,
                                 MappingContext<?, FedoraPersistentProperty> mappingContext,
                                 RdfDatatypeConverter rdfDatatypeConverter) {
        super(tree, parameters);
        this.parameterAccessor = parameters;
        this.mappingContext = mappingContext;
        this.rdfDatatypeConverter = rdfDatatypeConverter;
        this.selectors = new HashMap<>();
    }

    @Override
    protected Constraint create(Part part, Iterator<Object> iterator) {

        return buildConstraint(part, iterator.next());
    }

    @Override
    protected Constraint and(Part part, Constraint base, Iterator<Object> iterator) {
        return new AndImpl(base, buildConstraint(part, iterator.next()));
    }

    @Override
    protected Constraint or(Constraint base, Constraint criteria) {
        throw new UnsupportedOperationException("Disjunctions are not supported yet.");
    }

    @Override
    protected Query complete(Constraint criteria, Sort sort) {
        Pageable pageable = parameterAccessor.getPageable();
        return new JcrQuery(selectors,
                selectors.values().stream()
                        .map(DescendantNodeImpl::new)
                        .collect(new AndCollector(criteria)),
                pageable != null ? new LimitImpl(pageable.getOffset(), pageable.getPageSize()) : new LimitImpl());
    }

    private Constraint buildConstraint(Part part, Object value) {

        PropertyPath property = part.getProperty().getLeafProperty();
        TypeInformation typeInformation = property.getOwningType();
        FedoraObjectPersistentEntity entity = (FedoraObjectPersistentEntity) mappingContext.getPersistentEntity(typeInformation);

        addSelector(entity);

        switch (part.getType()) {
            case SIMPLE_PROPERTY:

                return buildPropertyValueComparison(entity, property.getSegment(), QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO, value);

            default:
                throw new UnsupportedOperationException("Expressions containing " +
                        Arrays.toString(part.getType().getKeywords().toArray()) + " are not supported yet");
        }


    }

    private void addSelector(FedoraObjectPersistentEntity entity) {
        if (!selectors.containsKey(entity.getTypeInformation())) {
            selectors.put(entity.getTypeInformation(), new SelectorImpl(entity));
        }
    }

    private ComparisonImpl buildPropertyValueComparison(FedoraObjectPersistentEntity<?> entity, String segment, String operator, Object value) {
        return new ComparisonImpl(buildPropertyValue(entity, segment),
                operator, buildLiteral(value, entity.getTypeInformation()));
    }

    private PropertyValueImpl buildPropertyValue(FedoraObjectPersistentEntity<?> entity, String segment) {
        return new PropertyValueImpl(buildPropertySelector(entity.getTypeInformation()),
                buildPropertyName(entity, segment));
    }

    private String buildPropertySelector(TypeInformation typeInformation) {
        return selectors.get(typeInformation).getSelectorName();
    }

    private String buildPropertyName(FedoraObjectPersistentEntity<?> entity, String segment) {
        FedoraResourcePersistentProperty persistentProperty = (FedoraResourcePersistentProperty) entity.getPersistentProperty(segment);
        return persistentProperty.getPrefixedName();
    }

    private LiteralImpl buildLiteral(Object value, TypeInformation typeInformation) {
        return new LiteralImpl(typeInformation, rdfDatatypeConverter, value);
    }
}
