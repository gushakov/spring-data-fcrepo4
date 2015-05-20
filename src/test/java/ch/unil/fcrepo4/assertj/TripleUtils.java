package ch.unil.fcrepo4.assertj;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;

import java.util.*;

/**
 * @author gushakov
 */
public class TripleUtils {

    public static Iterator<Triple> emptyTriples(){
        return Collections.emptyIterator();
    }

    public static Triple triple(String template){
       String[] parts = template.split("\\s+");
       return new Triple(NodeFactory.createURI(parts[0]), NodeFactory.createURI(parts[1]), NodeFactory.createURI(parts[2]));
    }

    public static Iterator<Triple> triples(String... templates){
        return Arrays.stream(templates).map(TripleUtils::triple).iterator();
    }

}
