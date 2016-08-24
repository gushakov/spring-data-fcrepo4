package ch.unil.fcrepo4.client;

import ch.unil.fcrepo4.utils.Utils;
import com.hp.hpl.jena.graph.Triple;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.List;

/**
 * Default implementation of {@linkplain FedoraResource} which stores the path to a Fedora resource (node) as well as
 * the reference to the shared instance of {@linkplain FedoraClientRepository}.
 * <p>
 * Modeled after {@code org.fcrepo.client.impl.FedoraResourceImpl}.
 *
 * @author gushakov
 */
public class FedoraResourceImpl implements FedoraResource {
    protected FedoraClientRepository repository;

    protected String path;

    protected List<Triple> triples;

    public FedoraResourceImpl(FedoraClientRepository fedoraClientRepository, String path) {
        this.repository = fedoraClientRepository;
        this.path = Utils.normalize(path);
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
    public Iterator<Triple> getProperties() throws FedoraException {
        if (triples == null) {
            triples = repository.getProperties(path);
        }
        return triples.iterator();
    }

    @Override
    public void updateProperties(String sparqlUpdate) throws FedoraException {
        repository.updateProperties(path, sparqlUpdate);
    }

}
