package ch.unil.fcrepo4.utils;

import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author gushakov
 */
public class UriBuilder {

    private URIBuilder delegateBuilder;

    public UriBuilder(){
        delegateBuilder = new URIBuilder();
    }

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

    public String getScheme(){
        return delegateBuilder.getScheme();
    }

    public UriBuilder setScheme(String scheme){
        delegateBuilder.setScheme(scheme);
        return this;
    }

    public String getHost(){
        return delegateBuilder.getHost();
    }

    public UriBuilder setHost(String host){
        delegateBuilder.setHost(host);
        return this;
    }

    public int getPort(){
        return delegateBuilder.getPort();
    }

    public UriBuilder setPort(int port){
        delegateBuilder.setPort(port);
        return this;
    }

    public String getPath() {
        return delegateBuilder.getPath();
    }

    public UriBuilder setPath(String path) {
        delegateBuilder.setPath(Utils.normalize(path));
        return this;
    }

    public URI build() {
        try {
            return delegateBuilder.build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
