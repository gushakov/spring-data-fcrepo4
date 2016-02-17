package ch.unil.fcrepo4.spring.data.repository.query;

import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraPersistentProperty;
import ch.unil.fcrepo4.spring.data.core.query.FedoraQuery;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;

import java.util.Iterator;

/**
 * @author gushakov
 */
public class FedoraSparqlQueryCreator extends AbstractQueryCreator<FedoraQuery, Object> {
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
    protected Object create(Part part, Iterator<Object> iterator) {
        return null;
    }

    @Override
    protected Object and(Part part, Object base, Iterator<Object> iterator) {
        return null;
    }

    @Override
    protected Object or(Object base, Object criteria) {
        return null;
    }

    @Override
    protected FedoraQuery complete(Object criteria, Sort sort) {
        return null;
    }
}
