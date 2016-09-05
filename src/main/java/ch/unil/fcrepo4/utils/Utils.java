package ch.unil.fcrepo4.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author gushakov
 */
public class Utils {

    /*
    Based on http://stackoverflow.com/a/29010716
     */

    public static Stream<Triple> triplesStream(Iterator<Triple> triples) {
        final Iterable<Triple> iterable = () -> triples;
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    public static Node getObjectLiteral(Iterator<Triple> props, String predicateUri) {
        boolean found = false;
        Node literal = null;
        while (props.hasNext() && !found) {
            Triple triple = props.next();
            if (triple.getPredicate().getURI().equals(predicateUri)) {
                if (!triple.getObject().isLiteral()) {
                    throw new RuntimeException("Property node " + triple.getObject() + " is not literal");
                }
                literal = triple.getObject();
                found = true;
            }
        }

        return literal;
    }

    public static ElementTriplesBlock getTriples(ElementGroup group) {
        ElementTriplesBlock triples = null;
        for (Element element : group.getElements()) {
            if (element instanceof ElementTriplesBlock) {
                triples = (ElementTriplesBlock) element;
            }
        }
        return triples;
    }

    public static ElementFilter getFilter(ElementGroup group) {
        ElementFilter filter = null;
        for (Element element : group.getElements()) {
            if (element instanceof ElementFilter) {
                filter = (ElementFilter) element;
            }
        }
        return filter;
    }

    // based on code from http://pinesong.ghost.io/how-to-upload-rdf-file-to-jena-fuseki-server-using-java-code/

    public static long reloadDefaultGraphFromFile(String dataServiceUri, String filename) throws IOException {
        Model model = ModelFactory.createDefaultModel();
        model.read(new ClassPathResource(filename).getURL().toString());
        DatasetAccessor datasetAccessor = DatasetAccessorFactory.createHTTP(dataServiceUri);
        datasetAccessor.deleteDefault();
        datasetAccessor.putModel(model);
        return model.size();
    }

    public static String normalize(String... paths) {
        return "/" + Arrays.stream(paths).map(p -> StringUtils.strip(p, "/").replaceAll("/+", "/")).collect(Collectors.joining("/"));
    }

    public static String relativePath(String base, String uri) {
        return normalize(StringUtils.removeStart(uri, base));
    }

}
