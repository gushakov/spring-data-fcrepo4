package ch.unil.fcrepo4.spring.data.core.convert;

import ch.unil.fcrepo4.spring.data.core.mapping.FedoraObjectPersistentEntity;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraPersistentEntity;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraPersistentProperty;
import ch.unil.fcrepo4.spring.data.core.mapping.GenericFedoraPersistentEntity;
import org.fcrepo.client.FedoraDatastream;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraResource;
import org.springframework.data.convert.EntityConverter;
import org.springframework.data.convert.EntityReader;
import org.springframework.data.convert.EntityWriter;

/**
 * @author gushakov
 */
public interface FedoraConverter extends EntityReader<Object, FedoraResource>, EntityWriter<Object, FedoraResource>,
        EntityConverter<FedoraPersistentEntity<?>, FedoraPersistentProperty, Object, FedoraResource> {

    @Override
    <T> T read(Class<T> beanType, FedoraResource fedoraResource);

    @Override
    void write(Object bean, FedoraResource fedoraResource);

    <T> FedoraObject getFedoraObject(T bean);

    <T, ID> FedoraObject getFedoraObject(ID id, Class<T> beanType);

    FedoraDatastream fetchDatastream(String dsPath);

    <T> String getFedoraObjectPath(T bean);

    <T, ID> String getFedoraObjectPath(ID id, Class<T> entityClass);

    boolean exists(String path);
}
