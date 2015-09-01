package ch.unil.fcrepo4.spring.data.repository.query;

// based on the code from org.springframework.data.solr.repository.query.PartTreeSolrQuery

import ch.unil.fcrepo4.spring.data.core.FedoraOperations;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraPersistentProperty;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.query.parser.PartTree;

/**
 * @author gushakov
 */
public class PartTreeRdfQuery extends AbstractRdfQuery {

    private PartTree tree;
    private MappingContext<?, FedoraPersistentProperty> mappingContext;

    public PartTreeRdfQuery(FedoraQueryMethod method, FedoraOperations fedoraOperations) {
        super(fedoraOperations, method);
        this.tree = new PartTree(method.getName(), method.getEntityInformation().getJavaType());
        this.mappingContext = fedoraOperations.getConverter().getMappingContext();
    }


}
