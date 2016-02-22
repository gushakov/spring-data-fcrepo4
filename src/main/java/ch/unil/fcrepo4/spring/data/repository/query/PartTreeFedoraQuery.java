package ch.unil.fcrepo4.spring.data.repository.query;

import ch.unil.fcrepo4.spring.data.core.FedoraOperations;
import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraPersistentProperty;
import ch.unil.fcrepo4.spring.data.core.query.FedoraQuery;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.query.parser.PartTree;

// based on the code from org.springframework.data.solr.repository.query.PartTreeSolrQuery

/**
 * @author gushakov
 */
public class PartTreeFedoraQuery extends AbstractFedoraQuery {

    private PartTree tree;
    private MappingContext<?, FedoraPersistentProperty> mappingContext;
    private RdfDatatypeConverter rdfDatatypeConverter;
    private FedoraQueryMethod method;

    public PartTreeFedoraQuery(FedoraQueryMethod method, FedoraOperations fedoraOperations) {
        super(fedoraOperations, method);
        this.method = method;
        this.tree = new PartTree(method.getName(), this.method.getEntityInformation().getJavaType());
        this.mappingContext = fedoraOperations.getConverter().getMappingContext();
        this.rdfDatatypeConverter = fedoraOperations.getConverter().getRdfDatatypeConverter();
    }

    @Override
    protected FedoraQuery createQuery(FedoraParameterAccessor parameterAccessor) {
        return new FedoraSparqlQueryCreator(tree, parameterAccessor, this.method.getEntityInformation().getJavaType(),
                mappingContext, rdfDatatypeConverter).createQuery();
    }
}
