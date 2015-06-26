package ch.unil.fcrepo4.spring.data.core.query;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gushakov
 */
public class PrefixMap {

    private Map<String, String> map;

    public PrefixMap() {
        map = new HashMap<>();
    }

    public PrefixMap addPrefix(String prefix, String uri) {
        map.put(prefix, uri);
        return this;
    }

    public String fullUri(String prefixedUri) {
        String[] split = prefixedUri.split(":", 2);
        return map.containsKey(split[0]) ? map.get(split[0]) + split[1] : prefixedUri;
    }
}
