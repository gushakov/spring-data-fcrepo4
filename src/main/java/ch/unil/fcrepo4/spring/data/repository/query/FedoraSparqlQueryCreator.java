package ch.unil.fcrepo4.spring.data.repository.query;

import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraPersistentProperty;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraResourcePersistentProperty;
import ch.unil.fcrepo4.spring.data.core.query.FedoraQuery;
import ch.unil.fcrepo4.spring.data.core.query.sparql.BgpCriteria;
import ch.unil.fcrepo4.spring.data.core.query.sparql.Criteria;
import ch.unil.fcrepo4.spring.data.core.query.sparql.SparqlQuery;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.context.PersistentPropertyPath;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.data.util.TypeInformation;

import java.util.Arrays;
import java.util.Iterator;

/**
 * @author gushakov
 */
public class FedoraSparqlQueryCreator extends AbstractQueryCreator<FedoraQuery, Criteria> {
    private FedoraParameterAccessor parameterAccessor;
    private MappingContext<?, FedoraPersistentProperty> mappingContext;
    private RdfDatatypeConverter rdfDatatypeConverter;
    private TypeInformation<?> domainTypeInfo;

    public FedoraSparqlQueryCreator(PartTree tree, FedoraParameterAccessor parameters,
                                    Class<?> domainClass, MappingContext<?, FedoraPersistentProperty> mappingContext,
                                    RdfDatatypeConverter rdfDatatypeConverter) {
        super(tree, parameters);
        this.parameterAccessor = parameters;
        this.domainTypeInfo = mappingContext.getPersistentEntity(domainClass).getTypeInformation();
        this.mappingContext = mappingContext;
        this.rdfDatatypeConverter = rdfDatatypeConverter;
    }

    @Override
    protected Criteria create(Part part, Iterator<Object> arguments) {
        PersistentPropertyPath<FedoraPersistentProperty> persistentPropertyPath = mappingContext.getPersistentPropertyPath(part.getProperty());
        Criteria bgpCriteria = new BgpCriteria(persistentPropertyPath, domainTypeInfo, rdfDatatypeConverter);
        processCriteriaForPart(persistentPropertyPath, bgpCriteria, part, arguments);
        return bgpCriteria;
    }

    @Override
    protected Criteria and(Part part, Criteria base, Iterator<Object> iterator) {
        return base.and(create(part, iterator));
    }

    @Override
    protected Criteria or(Criteria base, Criteria criteria) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected FedoraQuery complete(Criteria criteria, Sort sort) {
        final Pageable pageable = parameterAccessor.getPageable();
        if (pageable != null) {
            // paged query
            return new SparqlQuery(criteria, pageable.getOffset(), pageable.getPageSize());
        } else {
            return new SparqlQuery(criteria);
        }
    }

    private void processCriteriaForPart(PersistentPropertyPath<FedoraPersistentProperty> persistentPropertyPath,
                                        Criteria bgpCriteria, Part part, Iterator<Object> arguments) {
        switch (part.getType()) {
            case SIMPLE_PROPERTY:
                bgpCriteria.substitutePropertyNodeValue((FedoraResourcePersistentProperty) persistentPropertyPath.getLeafProperty(),
                        rdfDatatypeConverter.encodeExpressionValue(arguments.next()));
                break;

            case GREATER_THAN:
                bgpCriteria.addGreaterThanFilter((FedoraResourcePersistentProperty) persistentPropertyPath.getLeafProperty(),
                        rdfDatatypeConverter.encodeExpressionValue(arguments.next()));
                break;
            default:
                throw new UnsupportedOperationException("Expressions containing " +
                        Arrays.toString(part.getType().getKeywords().toArray()) + " are not supported yet");
        }
    }
}
