package ch.unil.fcrepo4.utils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.query.DatasetAccessorFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Iterator;
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

    public static ElementTriplesBlock getTriples(ElementGroup group){
        ElementTriplesBlock triples = null;
        for (Element element: group.getElements()){
            if (element instanceof ElementTriplesBlock){
                triples = (ElementTriplesBlock) element;
            }
        }
        return triples;
    }

    public static ElementFilter getFilter(ElementGroup group){
        ElementFilter filter = null;
        for (Element element: group.getElements()){
            if (element instanceof ElementFilter){
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

    public static String concatenate(String base, String path) {
        return concatenate(base, path, true);
    }

    public static String concatenate(String base, String path, boolean withEndSeparator) {
        return StringUtils.stripEnd(base, "/") + "/" + StringUtils.strip(path, "/") + (withEndSeparator ? "/" : "");
    }

}
