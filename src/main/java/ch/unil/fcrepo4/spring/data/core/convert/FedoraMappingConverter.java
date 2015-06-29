package ch.unil.fcrepo4.spring.data.core.convert;


/*
 * Based by org.springframework.data.mongodb.core.convert.MappingMongoConverter
 */


import ch.unil.fcrepo4.spring.data.core.mapping.*;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Uuid;
import ch.unil.fcrepo4.utils.Utils;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraRepository;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.util.Assert;

import java.util.UUID;

/**
 * @author gushakov
 */
public class FedoraMappingConverter implements FedoraConverter {
    private FedoraRepository repository;

    private MappingContext<? extends FedoraPersistentEntity<?>, FedoraPersistentProperty> mappingContext;

    private SimpleTypeHolder simpleTypeHolder;

    private ConversionService conversionService;

    public FedoraMappingConverter(FedoraRepository repository) {
        Assert.notNull(repository);
        this.repository = repository;
        final FedoraMappingContext context = new FedoraMappingContext();
        context.afterPropertiesSet();
        this.mappingContext = context;
        this.conversionService = new DefaultConversionService();
        this.simpleTypeHolder = new SimpleTypeHolder();
    }

    @Override
    public <T> T read(Class<T> type, FedoraObject source) {
        return null;
    }

    @Override
    public void write(Object source, FedoraObject sink) {
        // write properties

    }

    @Override
    public FedoraObject write(Object source) {
        Assert.notNull(source);
        final FedoraPersistentEntity<?> entity = getFedoraPersistentEntity(source);
        final FedoraObject fedoraObject = createFedoraObject(source, (FedoraObjectPersistentEntity<?>) entity);
        readFedoraObjectProperties(source, (FedoraObjectPersistentEntity<?>) entity, fedoraObject);
        write(source, fedoraObject);
        return fedoraObject;
    }

    @Override
    public MappingContext<? extends FedoraPersistentEntity<?>, FedoraPersistentProperty> getMappingContext() {
        return mappingContext;
    }

    @Override
    public ConversionService getConversionService() {
        return conversionService;
    }


    private FedoraPersistentEntity<?> getFedoraPersistentEntity(Object source) {
        final FedoraPersistentEntity<?> entity = mappingContext.getPersistentEntity(source.getClass());

        if (entity == null) {
            throw new MappingException("Cannot find entity for " + source.getClass().getName());
        }

        if (!FedoraObjectPersistentEntity.class.isAssignableFrom(entity.getClass())) {
            throw new MappingException("Entity " + entity.getName() + " is not of type FedoraObjectPersistentEntity");
        }

        return entity;
    }

    protected void readFedoraObjectProperties(Object bean, FedoraObjectPersistentEntity<?> entity, FedoraObject fedoraObject) {
        PersistentPropertyAccessor propsAccessor = entity.getPropertyAccessor(bean);

        UuidPersistentProperty uuidProp = (UuidPersistentProperty) entity.getPersistentProperty(Uuid.class);
        if (uuidProp != null) {
            // read uuid
            if (propsAccessor.getProperty(uuidProp) != null) {
                throw new MappingException("Uuid property is assigned automatically and cannot be changed");
            }

            try {
                Object uuid = Utils.getFedoraObjectProperty(fedoraObject, "uuid");

                if (uuid == null){
                    throw new MappingException("No \"uuid\" property found for Fedora object");
                }

                // convert to UUID if needed
                if (uuidProp.isUUID()){
                   propsAccessor.setProperty(uuidProp, UUID.fromString((String)uuid));
                }
                else {
                    propsAccessor.setProperty(uuidProp, uuid);
                }
            } catch (FedoraException e) {
                throw new MappingException("Cannot read Fedora object property \"uuid\"", e);
            }

        }
    }

    protected FedoraObject createFedoraObject(Object source, FedoraObjectPersistentEntity<?> entity) {
        final String namespace = entity.getNamespace();

        FedoraPersistentProperty idProp = entity.getIdProperty();
        if (idProp == null) {
            throw new MappingException("No ID property for entity " + entity.getName());
        }

        if (!PathPersistentProperty.class.isAssignableFrom(idProp.getClass())) {
            throw new MappingException("ID property " + idProp.getName() + " is not of type UuidPersistentProperty");
        }

        final PathPersistentProperty pathProp = (PathPersistentProperty) idProp;

        // get path creator registered via Path annotation
        final PathCreator pathCreator = pathProp.getPathCreator();

        // get path value of the source bean
        final Object path = entity.getPropertyAccessor(source).getProperty(pathProp);

        if (path == null) {
            throw new IllegalStateException("Uuid must not be null for source object");
        }

        if (!(path instanceof String)) {
            throw new IllegalStateException("Uuid must be of type String");
        }

        // create full JCR path for the target Fedora object
        final String fullPath = pathCreator.createPath(namespace, (String) path);
        try {
            return repository.findOrCreateObject(fullPath);
        } catch (FedoraException e) {
            throw new MappingException("Cannot find or create Fedora object with path " + path);
        }
    }
}
