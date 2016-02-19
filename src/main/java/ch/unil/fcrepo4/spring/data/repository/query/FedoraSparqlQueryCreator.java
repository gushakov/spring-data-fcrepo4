package ch.unil.fcrepo4.spring.data.repository.query;

import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraPersistentProperty;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraResourcePersistentProperty;
import ch.unil.fcrepo4.spring.data.core.query.FedoraQuery;
import ch.unil.fcrepo4.spring.data.core.query.sparql.BgpCriteria;
import ch.unil.fcrepo4.spring.data.core.query.sparql.Criteria;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.context.PersistentPropertyPath;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;

import java.util.Iterator;

/**
 * @author gushakov
 */
public class FedoraSparqlQueryCreator extends AbstractQueryCreator<FedoraQuery, Criteria> {
    private FedoraParameterAccessor parameterAccessor;
    private MappingContext<?, FedoraPersistentProperty> mappingContext;
    private RdfDatatypeConverter rdfDatatypeConverter;


    public FedoraSparqlQueryCreator(PartTree tree, FedoraParameterAccessor parameters,
                                    MappingContext<?, FedoraPersistentProperty> mappingContext,
                                    RdfDatatypeConverter rdfDatatypeConverter) {
        super(tree, parameters);
        this.parameterAccessor = parameters;
        this.mappingContext = mappingContext;
        this.rdfDatatypeConverter = rdfDatatypeConverter;
    }


    @Override
    protected Criteria create(Part part, Iterator<Object> iterator) {
        PersistentPropertyPath<FedoraPersistentProperty> persistentPropertyPath = mappingContext.getPersistentPropertyPath(part.getProperty());
        BgpCriteria bgpCriteria = new BgpCriteria<>(persistentPropertyPath);
        bgpCriteria.substitutePropertyNodeValue((FedoraResourcePersistentProperty) persistentPropertyPath.getLeafProperty(),
                rdfDatatypeConverter.encodeExpressionValue(iterator.next()));
        System.out.println(bgpCriteria);
        return bgpCriteria;
    }

    @Override
    protected Criteria and(Part part, Criteria base, Iterator<Object> iterator) {
        return null;
    }

    @Override
    protected Criteria or(Criteria base, Criteria criteria) {
        return null;
    }

    @Override
    protected FedoraQuery complete(Criteria criteria, Sort sort) {
        return null;
    }
}
