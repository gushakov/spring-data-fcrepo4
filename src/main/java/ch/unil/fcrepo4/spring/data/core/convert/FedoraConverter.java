package ch.unil.fcrepo4.spring.data.core.convert;

import ch.unil.fcrepo4.spring.data.core.mapping.DatastreamPersistentProperty;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraPersistentEntity;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraPersistentProperty;
import org.fcrepo.client.FedoraObject;
import org.springframework.data.convert.EntityConverter;
import org.springframework.data.convert.EntityReader;
import org.springframework.data.convert.EntityWriter;

import java.io.InputStream;

/**
 * @author gushakov
 */
public interface FedoraConverter extends EntityReader<Object, FedoraObject>, EntityWriter<Object, FedoraObject>,
        EntityConverter<FedoraPersistentEntity<?>, FedoraPersistentProperty, Object, FedoraObject>{

    FedoraObject write(Object source);

    <T> T read(Class<T> beanType, FedoraObject fedoraObject);

    InputStream readDatastream(FedoraObject fedoraObject, DatastreamPersistentProperty dsProp);
}
