package ch.unil.fcrepo4.spring.data.core;

import ch.unil.fcrepo4.spring.data.core.convert.FedoraConverter;
import com.hp.hpl.jena.query.Query;

import java.util.List;

/**
 * @author gushakov
 */
public interface FedoraOperations {

   FedoraConverter getConverter();

   <T> String save(T bean);

   <T, ID> T load(ID id, Class<T> beanType);

   <T, ID> boolean exists(ID id, Class<T> beanType);

   <T> List<T> query(Query rdfQuery, Class<T> beanType);

   long count(Query rdfQuery);

}
