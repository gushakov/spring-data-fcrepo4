package ch.unil.fcrepo4.client;

import ch.unil.fcrepo4.utils.UriBuilder;
import ch.unil.fcrepo4.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;

import java.util.Date;
import java.util.Iterator;
import java.util.Optional;

/**
 * Default implementation of {@linkplain FedoraResource} which stores the path to a Fedora resource (node) as well as
 * the reference to the shared instance of {@linkplain FedoraClientRepository}.
 * <p>
 * Some code copied from {@code org.fcrepo.client.impl.FedoraResourceImpl}.
 *
 * @author gushakov
 */
public class FedoraResourceImpl implements FedoraResource {

    protected FedoraClientRepository repository;

    protected String path;

    protected Node subject;

    protected Graph graph;

    public FedoraResourceImpl(FedoraClientRepository fedoraClientRepository, String path) {
        this.repository = fedoraClientRepository;
        this.path = Utils.normalize(path);
        this.subject = NodeFactory.createURI(new UriBuilder(fedoraClientRepository.getRepositoryUrl()).appendPathSegment(path).build().toString());
    }

    @Override
    public void delete() throws FedoraException {
        repository.delete(path);
    }

    @Override
    public void forceDelete() throws FedoraException {
        repository.forceDelete(path);
    }

    @Override
    public String getName() {
        return StringUtils.substringAfterLast(path, "/");
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getPropertyLiteralValueOrUri(String predicateUri) throws FedoraException {
        if (graph == null) {
            graph = repository.getGraph(path);
        }
        final Optional<Triple> triple = graph.find(subject, NodeFactory.createURI(predicateUri), Node.ANY).toList().stream().findAny();
        if (triple.isPresent()) {
            final Node object = triple.get().getObject();
            if (object.isLiteral()){
                return object.getLiteralValue().toString();
            }
            else if (object.isURI()){
                return object.getURI();
            }
            else {
                return null;
            }
        }
        return null;
    }

    @Override
    public Date getCreatedDate() throws FedoraException {
        if (graph == null) {
            graph = repository.getGraph(path);
        }
        final Optional<Triple> triple = graph.find(subject, FcrepoConstants.CREATED_DATE.asNode(), Node.ANY).toList().stream().findAny();
        if (triple.isPresent()) {
            return repository.getRdfDatatypeConverter().parseLiteralValue(triple.get().getObject().getLiteralValue().toString(),
                    Date.class);
        }
        return null;
    }

    @Override
    public Iterator<Triple> getProperties() throws FedoraException {
        if (graph == null) {
            graph = repository.getGraph(path);
        }
        return graph.find(Node.ANY, Node.ANY, Node.ANY).toList().iterator();
    }

    @Override
    public void updateProperties(String sparqlUpdate) throws FedoraException {
        repository.updateProperties(path, sparqlUpdate);
    }

}
