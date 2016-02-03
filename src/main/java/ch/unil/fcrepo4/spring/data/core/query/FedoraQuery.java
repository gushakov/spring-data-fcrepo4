package ch.unil.fcrepo4.spring.data.core.query;

/**
 * @author gushakov
 */
public interface FedoraQuery {
    String JCR_SQL2 = "JCR_SQL2";

    String getLanguage();

    String getSerialized();

    boolean isPaged();

    int getOffset();

    int getRowLimit();
}
