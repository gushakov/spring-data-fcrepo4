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
public interface FedoraConverter extends EntityReader<Object, FedoraObject>, EntityWriter<Object, FedoraObject>,
        EntityConverter<FedoraPersistentEntity<?>, FedoraPersistentProperty, Object, FedoraObject>{

    void readFedoraResourceProperties(Object bean, GenericFedoraPersistentEntity<?> entity, FedoraResource fedoraResource);

    FedoraObject write(Object source);

    <T> T read(String path, Class<T> beanType);

    <T> T read(Class<T> beanType, FedoraObject fedoraObject);

    FedoraDatastream readDatastream(String dsPath);

}
