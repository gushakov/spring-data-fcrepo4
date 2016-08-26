package ch.unil.fcrepo4.spring.data.core.convert;

import ch.unil.fcrepo4.client.FedoraDatastream;
import ch.unil.fcrepo4.client.FedoraObject;
import ch.unil.fcrepo4.client.FedoraResource;
import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.mapping.DatastreamPersistentEntity;
import ch.unil.fcrepo4.spring.data.core.mapping.DatastreamPersistentProperty;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraPersistentEntity;
import ch.unil.fcrepo4.spring.data.core.mapping.FedoraPersistentProperty;
import org.springframework.data.convert.EntityConverter;
import org.springframework.data.convert.EntityReader;
import org.springframework.data.convert.EntityWriter;

import java.io.InputStream;

/**
 * @author gushakov
 */
public interface FedoraConverter extends EntityReader<Object, FedoraResource>, EntityWriter<Object, FedoraResource>,
        EntityConverter<FedoraPersistentEntity<?>, FedoraPersistentProperty, Object, FedoraResource> {

    RdfDatatypeConverter getRdfDatatypeConverter();

    @Override
    <T> T read(Class<T> beanType, FedoraResource fedoraResource);

    @Override
    void write(Object bean, FedoraResource fedoraResource);

    <T> FedoraObject getFedoraObject(T bean);

    <T, ID> FedoraObject getFedoraObject(ID id, Class<T> beanType);

    FedoraDatastream fetchDatastream(String dsPath);

    <T> String getFedoraObjectPath(T bean);

    <T, ID> String getFedoraObjectPath(ID id, Class<T> entityClass);

    <T> String getFedoraObjectUrl(T bean);

    boolean exists(String path);

    <T> String getDatastreamPath(T bean, DatastreamPersistentProperty dsProp);

    <T> Object readDatastream(T bean, FedoraPersistentEntity<?> entity, DatastreamPersistentProperty dsProp);

    InputStream readDatastreamContent(Object dsBean, DatastreamPersistentEntity<?> dsEntity, FedoraDatastream fedoraDatastream);
}
