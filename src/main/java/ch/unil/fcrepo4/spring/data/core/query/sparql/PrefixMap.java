package ch.unil.fcrepo4.spring.data.core.query.sparql;

import com.hp.hpl.jena.shared.PrefixMapping;

import java.util.Map;

/**
 * @author gushakov
 */
public class PrefixMap {

    private PrefixMapping prefixMapping;

    public PrefixMap() {
        prefixMapping = PrefixMapping.Factory.create();
    }

    public PrefixMap(boolean withStandardPrefixes) {
        prefixMapping = PrefixMapping.Factory.create();
   prefixMapping.setNsPrefixes(PrefixMapping.Standard);

    }


    public PrefixMapping getPrefixMapping() {
        return prefixMapping;
    }

    public PrefixMap addPrefix(String prefix, String uri) {
        prefixMapping.setNsPrefix(prefix, uri);
        return this;
    }



    public String resolveUri(String prefixedUri) {
        return prefixMapping.shortForm(prefixedUri);
    }
}
