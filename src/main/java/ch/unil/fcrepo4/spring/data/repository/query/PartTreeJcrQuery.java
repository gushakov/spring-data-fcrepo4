package ch.unil.fcrepo4.spring.data.repository.query;

import ch.unil.fcrepo4.spring.data.core.FedoraOperations;
import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraPersistentProperty;
import ch.unil.fcrepo4.spring.data.core.query.FedoraQuery;
import ch.unil.fcrepo4.spring.data.core.query.qom.Query;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.query.parser.PartTree;

// based on the code from org.springframework.data.solr.repository.query.PartTreeSolrQuery

/**
 * @author gushakov
 */
public class PartTreeJcrQuery extends AbstractFedoraQuery {

    private PartTree tree;
    private MappingContext<?, FedoraPersistentProperty> mappingContext;
    private RdfDatatypeConverter rdfDatatypeConverter;

    public PartTreeJcrQuery(FedoraQueryMethod method, FedoraOperations fedoraOperations) {
        super(fedoraOperations, method);
        this.tree = new PartTree(method.getName(), method.getEntityInformation().getJavaType());
        this.mappingContext = fedoraOperations.getConverter().getMappingContext();
        this.rdfDatatypeConverter = fedoraOperations.getConverter().getRdfDatatypeConverter();
    }

    @Override
    protected Query createQuery(FedoraParameterAccessor parameterAccessor) {
        return new FedoraJcrQueryCreator(tree, parameterAccessor, mappingContext, rdfDatatypeConverter).createQuery();
    }
}
