package ch.unil.fcrepo4.client;

import com.hp.hpl.jena.graph.Triple;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.List;

/**
 * Strait-forward implementation of {@linkplain FedoraResource} storing the path and the collection
 * of {@linkplain Triple}s describing a Fedora object.
 *
 * @author gushakov
 */
public class FedoraResourceImpl implements FedoraResource {
    private String path;

    private List<Triple> triples;

    public FedoraResourceImpl(String path, List<Triple> triples) {
        this.path = path;
        this.triples = triples;
    }

    @Override
    public String getName() {
        // find the last segment of the path
        final String pathString = StringUtils.stripEnd(path, "/");
        final String lastSegment = StringUtils.substringAfterLast(pathString, "/");
        if (lastSegment.equals("")){
            return pathString;
        }
        else {
            return lastSegment;
        }
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Iterator<Triple> getProperties() {
        return triples.iterator();
    }
}
