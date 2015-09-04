package ch.unil.fcrepo4.spring.data.core.query.sparql;

import com.hp.hpl.jena.query.Query;

/**
 * @author gushakov
 */
public interface SelectQueryBuilder {

    Query build();

}
