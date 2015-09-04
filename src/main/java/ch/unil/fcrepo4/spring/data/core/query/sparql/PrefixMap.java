package ch.unil.fcrepo4.spring.data.core.query.sparql;

import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * @author gushakov
 */
public class PrefixMap {

    private PrefixMapping prefixMapping;

    public PrefixMap() {
        prefixMapping = PrefixMapping.Standard;
    }

    public PrefixMapping getPrefixMapping() {
        return prefixMapping;
    }

    public PrefixMap addPrefix(String prefix, String uri) {
        prefixMapping.setNsPrefix(prefix, uri);
        return this;
    }

    public String fullUri(String prefixedUri) {
        return prefixMapping.expandPrefix(prefixedUri);
    }
}
