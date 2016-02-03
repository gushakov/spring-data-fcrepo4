package ch.unil.fcrepo4.spring.data.repository.query;

import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraObjectPersistentEntity;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraPersistentProperty;
import ch.unil.fcrepo4.spring.data.core.mapping.SimpleFedoraResourcePersistentProperty;
import ch.unil.fcrepo4.spring.data.core.query.FedoraJcrSqlQuery;
import ch.unil.fcrepo4.spring.data.core.query.FedoraJcrSqlQueryBuilder;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import org.modeshape.jcr.query.QueryBuilder;
import org.modeshape.jcr.query.model.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.util.Assert;

import javax.jcr.query.qom.QueryObjectModelConstants;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static ch.unil.fcrepo4.spring.data.core.query.FedoraJcrSqlQueryBuilder.FROM;
import static ch.unil.fcrepo4.spring.data.core.query.FedoraJcrSqlQueryBuilder.VAR;

// based on code from org.springframework.data.solr.repository.query.SolrQueryCreator

/**
 * @author gushakov
 */
public class FedoraJcrSqlQueryCreator extends AbstractQueryCreator<FedoraJcrSqlQuery, ComparisonCriteria> {

    // see org.fcrepo.kernel.modeshape.rdf.converters.ValueConverter.RdfLiteralJcrValueBuilder  \30 = \u0018 = [CANCEL] control character
    private static final String SEPARATOR = "\u0018^^\u0018";

    private FedoraObjectPersistentEntity entity;
    private FedoraParameterAccessor parameterAccessor;
    private MappingContext<?, FedoraPersistentProperty> mappingContext;
    private RdfDatatypeConverter rdfDatatypeConverter;
    private FedoraJcrSqlQueryBuilder queryBuilder;

    public FedoraJcrSqlQueryCreator(PartTree tree, FedoraParameterAccessor parameters, MappingContext<?, FedoraPersistentProperty> mappingContext, RdfDatatypeConverter rdfDatatypeConverter) {
        super(tree, parameters);
        this.parameterAccessor = parameters;
        this.mappingContext = mappingContext;
        this.rdfDatatypeConverter = rdfDatatypeConverter;
        queryBuilder = new FedoraJcrSqlQueryBuilder();
    }

    @Override
    protected ComparisonCriteria create(Part part, Iterator<Object> iterator) {
        if (entity == null) {
            entity = (FedoraObjectPersistentEntity) mappingContext.getPersistentEntity(part.getProperty().getOwningType());
        }
        return new ComparisonCriteria(buildComparison(part, iterator.next()));
    }

    @Override
    protected ComparisonCriteria and(Part part, ComparisonCriteria baseCriteria, Iterator<Object> iterator) {
        return new ComparisonCriteria(baseCriteria, buildComparison(part, iterator.next()));
    }

    @Override
    protected ComparisonCriteria or(ComparisonCriteria baseCriteria, ComparisonCriteria additionalCriteria) {
        return new ComparisonCriteria(baseCriteria, additionalCriteria);
    }

    @Override
    protected FedoraJcrSqlQuery complete(ComparisonCriteria criteria, Sort sort) {
        queryBuilder.clear();

        QueryBuilder.ConstraintBuilder constraintBuilder = queryBuilder.selectStar().from(FROM)
                .where().isBelowPath(VAR, "/" + entity.getNamespace()).and().openParen();

        List<Comparison> baseComparisons = criteria.getBaseComparisons();
        addComparisonConstraint(constraintBuilder, baseComparisons.get(0));
        if (baseComparisons.size() > 1) {
            for (int i = 1; i < baseComparisons.size(); i++) {
                addComparisonConstraint(constraintBuilder.and(), baseComparisons.get(i));
            }
        }

        List<Comparison> additionalComparisons = criteria.getAdditionalComparisons();
        if (additionalComparisons != null) {
            addComparisonConstraint(constraintBuilder.or(), additionalComparisons.get(0));
            if (additionalComparisons.size() > 1) {
                for (int i = 1; i < additionalComparisons.size(); i++) {
                    addComparisonConstraint(constraintBuilder, additionalComparisons.get(i));
                }
            }
        }

        constraintBuilder.closeParen().end();

        Pageable pageable = parameterAccessor.getPageable();
        if (pageable != null) {
            queryBuilder.limit(pageable.getPageSize()).offset(pageable.getPageNumber() * pageable.getPageSize());
        }
        return new FedoraJcrSqlQuery(queryBuilder.query(), pageable != null);
    }

    private QueryBuilder.ConstraintBuilder addComparisonConstraint(QueryBuilder.ConstraintBuilder constraintBuilder, Comparison comparison) {
        PropertyValue propValue = (PropertyValue) comparison.getOperand1();
        Literal literal = (Literal) comparison.getOperand2();
        QueryBuilder.ComparisonBuilder comparisonBuilder = constraintBuilder.propertyValue(propValue.getSelectorName(), propValue.getPropertyName());
        String operator = comparison.getOperator();
        switch (operator) {
            case QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO:
                return comparisonBuilder.isEqualTo(literal);
            case QueryObjectModelConstants.JCR_OPERATOR_GREATER_THAN:
                return comparisonBuilder.isGreaterThan(literal);
            case QueryObjectModelConstants.JCR_OPERATOR_GREATER_THAN_OR_EQUAL_TO:
                return comparisonBuilder.isGreaterThanOrEqualTo(literal);
            case QueryObjectModelConstants.JCR_OPERATOR_LESS_THAN:
                return comparisonBuilder.isLessThan(literal);
            case QueryObjectModelConstants.JCR_OPERATOR_LESS_THAN_OR_EQUAL_TO:
                return comparisonBuilder.isLessThanOrEqualTo(literal);
            case QueryObjectModelConstants.JCR_OPERATOR_LIKE:
                return comparisonBuilder.isLike(literal);
            default:
                throw new IllegalArgumentException("Cannot resolve operator: " + operator);
        }
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

    private class ComparisonVisitor extends Visitors.AbstractVisitor {

        private Comparison comparison;

        @Override
        public void visit(Comparison comparison) {
            this.comparison = comparison;
        }

        public Comparison getComparison() {
            return comparison;
        }
    }

    private String serializeJcrProperty(SimpleFedoraResourcePersistentProperty property) {
        return property.getPrefix() + ":" + property.getName();
    }

    private String serializeJcrValue(SimpleFedoraResourcePersistentProperty property, Object value) {
        RDFDatatype rdfDatatype = rdfDatatypeConverter.convert(property.getType());
        if (isDate(value)) {
            return rdfDatatype.unparse(value);
        } else {
            return rdfDatatype.unparse(value) + SEPARATOR + rdfDatatype.getURI();
        }
    }

    private boolean isDate(Object value) {
        return Date.class.isInstance(value) || ZonedDateTime.class.isInstance(value);
    }

    private Comparison getComparison(Visitable visitable) {
        ComparisonVisitor comparisonVisitor = new ComparisonVisitor();
        Visitors.visitAll(visitable, comparisonVisitor);
        return comparisonVisitor.getComparison();
    }

    private Comparison buildComparison(Part part, Object value) {
        QueryBuilder.ConstraintBuilder constraintBuilder = queryBuilder.clear().where();
        SimpleFedoraResourcePersistentProperty property = getPersistentProperty(part);
        QueryBuilder.ComparisonBuilder comparisonBuilder = constraintBuilder.propertyValue(VAR, serializeJcrProperty(property));
        switch (part.getType()) {
            case SIMPLE_PROPERTY:
                if (isDate(value)) {
                    return getComparison(comparisonBuilder.isEqualTo()
                            .cast(serializeJcrValue(property, value)).asDate().end().query());
                } else {
                    return getComparison(comparisonBuilder
                            .isEqualTo(serializeJcrValue(property, value)).end().query());
                }
            case GREATER_THAN:
                if (isDate(value)) {
                    return getComparison(comparisonBuilder.isGreaterThan()
                            .cast(serializeJcrValue(property, value)).asDate().end().query());
                } else {
                    return getComparison(comparisonBuilder
                            .isGreaterThan(serializeJcrValue(property, value)).end().query());
                }
            case GREATER_THAN_EQUAL:
                if (isDate(value)) {
                    return getComparison(comparisonBuilder.isGreaterThanOrEqualTo()
                            .cast(serializeJcrValue(property, value)).asDate().end().query());
                } else {
                    return getComparison(comparisonBuilder
                            .isGreaterThanOrEqualTo(serializeJcrValue(property, value)).end().query());
                }
            case LESS_THAN:
                if (isDate(value)) {
                    return getComparison(comparisonBuilder.isLessThan()
                            .cast(serializeJcrValue(property, value)).asDate().end().query());
                } else {
                    return getComparison(comparisonBuilder
                            .isLessThan(serializeJcrValue(property, value)).end().query());
                }
            case LESS_THAN_EQUAL:
                if (isDate(value)) {
                    return getComparison(comparisonBuilder.isLessThanOrEqualTo()
                            .cast(serializeJcrValue(property, value)).asDate().end().query());
                } else {
                    return getComparison(comparisonBuilder
                            .isLessThanOrEqualTo(serializeJcrValue(property, value)).end().query());
                }
            case LIKE:
                return getComparison(comparisonBuilder
                        .isLike("%" + value + "%").end().query());
            default:
                throw new UnsupportedOperationException("Expressions containing " +
                        Arrays.toString(part.getType().getKeywords().toArray()) + " are not supported yet");
        }

    }

}
