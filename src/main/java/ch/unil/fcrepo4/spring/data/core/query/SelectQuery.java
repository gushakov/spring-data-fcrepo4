package ch.unil.fcrepo4.spring.data.core.query;

import ch.unil.fcrepo4.spring.data.core.convert.RdfDatatypeConverter;
import com.github.jsonldjava.utils.Obj;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;

/**
 * @author gushakov
 */
public interface SelectQuery {

    <T extends SelectQuery> T withDatatypeConverter(RdfDatatypeConverter rdfDatatypeConverter);

    <T extends SelectQuery> T select(String varName);

    <T extends SelectQuery> T count(boolean distinct);

    <T extends FromBlock> T from(String varName, String predicateUri, Object value);

    Query build();
}
