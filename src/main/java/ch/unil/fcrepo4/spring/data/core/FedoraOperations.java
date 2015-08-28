package ch.unil.fcrepo4.spring.data.core;

import ch.unil.fcrepo4.spring.data.core.convert.FedoraConverter;

/**
 * @author gushakov
 */
public interface FedoraOperations {

   FedoraConverter getConverter();

   String save(Object source);

   <T> T load(String path, Class<T> beanType);

   <T, ID> boolean exists(ID id, Class<T> beanType);

}
