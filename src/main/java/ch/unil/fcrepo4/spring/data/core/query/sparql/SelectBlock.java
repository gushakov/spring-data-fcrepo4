package ch.unil.fcrepo4.spring.data.core.query.sparql;

/**
 * @author gushakov
 */
public interface SelectBlock extends SelectQueryBuilder {

    SelectBlock select(String varName);

    SelectBlock count(boolean distinct);

    FromBlock from(String varName, String predicateUri, Object value);

}
