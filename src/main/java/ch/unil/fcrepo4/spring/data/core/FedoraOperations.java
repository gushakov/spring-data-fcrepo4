package ch.unil.fcrepo4.spring.data.core;

import ch.unil.fcrepo4.spring.data.core.convert.FedoraConverter;
import ch.unil.fcrepo4.spring.data.core.query.FedoraQuery;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @author gushakov
 */
public interface FedoraOperations {

   FedoraConverter getConverter();

   <T> void save(T bean);

   <T, ID> T load(ID id, Class<T> beanType);

   <T, ID> boolean exists(ID id, Class<T> beanType);

   <T, ID> void delete(ID id, Class<T> beanType);

   <T> List<T> query(FedoraQuery query, Class<T> beanType);

   <T> Page<T> queryForPage(FedoraQuery query, Class<T> beanType);

}
