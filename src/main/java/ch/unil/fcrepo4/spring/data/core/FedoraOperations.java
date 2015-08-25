package ch.unil.fcrepo4.spring.data.core;

/**
 * @author gushakov
 */
public interface FedoraOperations {

   String save(Object source);

   <T> T load(String path, Class<T> beanType);

}
