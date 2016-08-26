package ch.unil.fcrepo4.utils;

import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author gushakov
 */
public class UriBuilder {

    private URIBuilder delegateBuilder;

    public UriBuilder(String fromUri) {
        try {
            delegateBuilder = new URIBuilder(fromUri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public UriBuilder appendPathSegment(String pathSegment) {
        if (pathSegment == null || pathSegment.matches("\\s*")) {
            return this;
        }
        try {
            final String path = delegateBuilder.getPath();
            final URI uri = delegateBuilder.build();
            delegateBuilder = new URIBuilder(uri);
            delegateBuilder.setPath(Utils.normalize(path, pathSegment));
            return this;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public String getPath() {
        return delegateBuilder.getPath();
    }

    public void setPath(String path) {
        delegateBuilder.setPath(Utils.normalize(path));
    }

    public URI build() {
        try {
            return delegateBuilder.build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
