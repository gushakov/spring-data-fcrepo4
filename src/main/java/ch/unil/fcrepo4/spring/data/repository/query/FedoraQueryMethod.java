package ch.unil.fcrepo4.spring.data.repository.query;

import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;

import java.lang.reflect.Method;

// based on code from org.springframework.data.solr.repository.query.SolrQueryMethod

/**
 * @author gushakov
 */
public class FedoraQueryMethod extends QueryMethod {
    /**
     * Creates a new {@link QueryMethod} from the given parameters. Looks up the correct query to use for following
     * invocations of the method given.
     *
     * @param method   must not be {@literal null}
     * @param metadata must not be {@literal null}
     */
    public FedoraQueryMethod(Method method, RepositoryMetadata metadata) {
        super(method, metadata);
    }
}
