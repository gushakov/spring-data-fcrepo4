package ch.unil.fcrepo4.spring.data.core.convert;


/*
 * Based on code from:
 * org.springframework.data.mongodb.core.convert.MappingMongoConverter,
 * org.springframework.data.solr.core.convert.MappingSolrConverter
 */


import ch.unil.fcrepo4.spring.data.core.Constants;
import ch.unil.fcrepo4.spring.data.core.mapping.*;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Created;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Uuid;
import ch.unil.fcrepo4.utils.Utils;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.commons.lang3.StringUtils;
import org.fcrepo.client.*;
import org.fcrepo.kernel.RdfLexicon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.util.Assert;

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author gushakov
 */
public class FedoraMappingConverter implements FedoraConverter {
    private static final Logger logger = LoggerFactory.getLogger(FedoraMappingConverter.class);

    private FedoraRepository repository;

    private MappingContext<? extends FedoraPersistentEntity<?>, FedoraPersistentProperty> mappingContext;

    private ConversionService conversionService;

    public FedoraMappingConverter(FedoraRepository repository) {
        Assert.notNull(repository);
        this.repository = repository;
        final FedoraMappingContext context = new FedoraMappingContext();
        context.afterPropertiesSet();
        this.mappingContext = context;
        this.conversionService = new DefaultConversionService();
    }

    @Override
    public <T> T read(Class<T> beanType, FedoraResource fedoraResource) {
        FedoraPersistentEntity<?> entity = mappingContext.getPersistentEntity(beanType);
        // read common resource properties
        T bean;
        try {
            bean = beanType.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new MappingException("Cannot instantiate bean of type " + beanType.getName(), e);
        }
        readSimpleProperties(bean, entity, fedoraResource);
        readFedoraResourceProperties(bean, (GenericFedoraPersistentEntity<?>) entity, fedoraResource);

        // if we are reading from a Fedora object
        if (entity instanceof FedoraObjectPersistentEntity && fedoraResource instanceof FedoraObject){
            readDatastreams(bean, (FedoraObjectPersistentEntity<?>)entity, (FedoraObject) fedoraResource);
        }
        else if (entity instanceof DatastreamPersistentEntity && fedoraResource instanceof FedoraDatastream){
            // or if we are reading from a datastream
            readDatastreamContent(bean, (DatastreamPersistentEntity<?>) entity, (FedoraDatastream) fedoraResource);
        }

        return bean;
    }

    @Override
    public void write(Object bean, FedoraResource fedoraResource) {
        FedoraPersistentEntity<?> entity = mappingContext.getPersistentEntity(bean.getClass());
        // read common resource properties
        readFedoraResourceProperties(bean, (GenericFedoraPersistentEntity<?>) entity, fedoraResource);
        // write simple properties
        writeSimpleProperties(bean, entity, fedoraResource);

        // if we are writing to a Fedora object
        if (entity instanceof FedoraObjectPersistentEntity && fedoraResource instanceof FedoraObject) {
            writeDatastreams(bean, (FedoraObjectPersistentEntity<?>) entity, (FedoraObject) fedoraResource);
        }
    }

    @Override
    public MappingContext<? extends FedoraPersistentEntity<?>, FedoraPersistentProperty> getMappingContext() {
        return mappingContext;
    }

    @Override
    public ConversionService getConversionService() {
        return conversionService;
    }


    @Override
    public FedoraObject getFedoraObject(Object bean) {
        FedoraPersistentEntity<?> ent = mappingContext.getPersistentEntity(bean.getClass());
        if (!(ent instanceof FedoraObjectPersistentEntity)) {
            throw new MappingException("Cannot map a bean of type " + bean.getClass() + " to a Fedora object");
        }
        FedoraObjectPersistentEntity<?> entity = (FedoraObjectPersistentEntity<?>) ent;

        FedoraPersistentProperty idProp = entity.getIdProperty();
        if (idProp == null) {
            throw new MappingException("No path property for entity " + entity.getName());
        }

        if (!PathPersistentProperty.class.isAssignableFrom(idProp.getClass())) {
            throw new MappingException("ID property " + idProp.getName() + " is not of type UuidPersistentProperty");
        }

        final PathPersistentProperty pathProp = (PathPersistentProperty) idProp;

        // get path creator registered via Path annotation
        final PathCreator pathCreator = pathProp.getPathCreator();

        // get path value of the bean
        Object path = entity.getPropertyAccessor(bean).getProperty(pathProp);

        // create full path for the target Fedora object
        String fullPath = pathCreator.createPath(entity.isDefaultNamespace() ? null : entity.getNamespace(), entity.getType(),
                idProp.getType(), idProp.getName(), path);

        try {
            // get object from the repository if exists
            if (repository.exists(fullPath)) {
                return repository.getObject(fullPath);
            } else {
                // or create a new one
                return repository.createObject(fullPath);
            }
        } catch (FedoraException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> FedoraObject getFedoraObject(String path, Class<T> beanType) {
        FedoraPersistentEntity<?> ent = mappingContext.getPersistentEntity(beanType);
        if (!(ent instanceof FedoraObjectPersistentEntity)) {
            throw new MappingException("Cannot map a bean of type " + beanType + " to a Fedora object");
        }
        FedoraObjectPersistentEntity<?> entity = (FedoraObjectPersistentEntity<?>) ent;

        FedoraPersistentProperty idProp = entity.getIdProperty();
        if (idProp == null) {
            throw new MappingException("No path property for entity " + entity.getName());
        }

        if (!PathPersistentProperty.class.isAssignableFrom(idProp.getClass())) {
            throw new MappingException("ID property " + idProp.getName() + " is not of type UuidPersistentProperty");
        }

        final PathPersistentProperty pathProp = (PathPersistentProperty) idProp;

        // get path creator registered via Path annotation
        final PathCreator pathCreator = pathProp.getPathCreator();

        // create full path for the target Fedora object
        String fullPath = pathCreator.createPath(entity.isDefaultNamespace() ? null : entity.getNamespace(), entity.getType(),
                idProp.getType(), idProp.getName(), path);

        try {
            // get object from the repository if exists
            if (repository.exists(fullPath)) {
                return repository.getObject(fullPath);
            } else {
                // or create a new one
                return repository.createObject(fullPath);
            }
        } catch (FedoraException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FedoraDatastream fetchDatastream(String dsPath) {
        try {
            return repository.getDatastream(dsPath);
        } catch (FedoraException e) {
            throw new RuntimeException(e);
        }
    }

    private void readFedoraResourceProperties(Object bean, GenericFedoraPersistentEntity<?> entity, FedoraResource fedoraResource) {
        PersistentPropertyAccessor propsAccessor = entity.getPropertyAccessor(bean);

        //
        // Uuid
        //
        UuidPersistentProperty uuidProp = (UuidPersistentProperty) entity.getPersistentProperty(Uuid.class);
        if (uuidProp != null) {

            // ignore if uuid property is set on the source bean

            try {
                Object uuid = Utils.getLiteralValue(fedoraResource.getProperties(), RdfLexicon.HAS_PRIMARY_IDENTIFIER.getURI());

                if (uuid == null) {
                    throw new MappingException("No " +
                            RdfLexicon.HAS_PRIMARY_IDENTIFIER.getURI() +
                            " property found");
                }

                // convert to UUID if needed
                if (uuidProp.isUUID()) {
                    propsAccessor.setProperty(uuidProp, UUID.fromString((String) uuid));
                } else {
                    propsAccessor.setProperty(uuidProp, uuid);
                }
            } catch (FedoraException e) {
                throw new RuntimeException(e);
            }

        }

        //
        // Created
        //
        CreatedPersistentProperty createdProp = (CreatedPersistentProperty) entity.getPersistentProperty(Created.class);
        if (createdProp != null) {
            // ignore if created property is set on the source bean

            try {
                Object created = Utils.getLiteralValue(fedoraResource.getProperties(), RdfLexicon.CREATED_DATE.getURI());

                if (created == null) {
                    throw new MappingException("No " + RdfLexicon.CREATED_DATE.getURI() + " property found");
                }

                ZonedDateTime dateTime = ZonedDateTime.parse(created.toString());

                if (createdProp.isDate()) {
                    propsAccessor.setProperty(createdProp, Date.from(dateTime.toInstant()));
                } else {
                    propsAccessor.setProperty(createdProp, dateTime);
                }
            } catch (FedoraException e) {
                throw new RuntimeException(e);
            }

        }

    }

    private void writeSimpleProperties(Object bean, FedoraPersistentEntity<?> entity, FedoraResource fedoraResource) {
        PersistentPropertyAccessor propsAccessor = entity.getPropertyAccessor(bean);
        final List<String> inserts = new ArrayList<>();
        entity.doWithSimplePersistentProperties(simpleProp -> {
            Object propValue = propsAccessor.getProperty(simpleProp);
            // ignore if property value is null
            if (propValue != null) {
                inserts.add("<> <" + ((SimpleFedoraPersistentProperty) simpleProp).getUri() + "> "
                        + Utils.encodeLiteralValue(propValue, simpleProp.getType()));
            }
        });
        if (inserts.size() > 0) {
            logger.debug("Update: {}", "INSERT DATA { " + StringUtils.join(inserts, " . ") + " . }");
            try {
                fedoraResource.updateProperties("INSERT DATA { " + StringUtils.join(inserts, " . ") + " . }");
            } catch (FedoraException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void readSimpleProperties(Object bean, FedoraPersistentEntity<?> entity, FedoraResource fedoraResource) {
        PersistentPropertyAccessor propsAccessor = entity.getPropertyAccessor(bean);
        entity.doWithSimplePersistentProperties(simpleProp -> {
            try {
                propsAccessor.setProperty(simpleProp, Utils.getLiteralValue(fedoraResource.getProperties(),
                        ((SimpleFedoraPersistentProperty) simpleProp).getUri()));
            } catch (FedoraException e) {
                throw new RuntimeException(e);
            }
        });

    }

    private void writeDatastreams(Object source, FedoraObjectPersistentEntity<?> entity, FedoraObject fedoraObject) {
        entity.doWithDatastreams(dsProp -> {
            Object dsBean = entity.getPropertyAccessor(source).getProperty(dsProp);
            if (dsBean == null) {
                throw new MappingException("Datastream " + dsProp.getName() + " must not be null, entity " + entity.getType().getSimpleName());
            }

            DatastreamPersistentEntity<?> dsEntity = (DatastreamPersistentEntity<?>) mappingContext.getPersistentEntity(dsProp.getType());

            FedoraDatastream datastream = createDatastream(dsBean, (DatastreamPersistentProperty) dsProp, dsEntity, fedoraObject);
            readFedoraResourceProperties(dsBean, dsEntity, datastream);
            writeSimpleProperties(dsBean, dsEntity, datastream);
        });
    }

    private FedoraDatastream createDatastream(Object dsBean, DatastreamPersistentProperty dsProp, DatastreamPersistentEntity<?> dsEntity, FedoraObject fedoraObject) {
        FedoraContent fedoraContent = new FedoraContent();
        fedoraContent.setContentType(dsEntity.getMimetype());

        InputStream dsContent = (InputStream) dsEntity.getPropertyAccessor(dsBean).getProperty(dsEntity.getContentProperty());

        if (dsContent == null) {
            throw new MappingException("Content of datastream " + dsProp.getName() + " must not be null");
        }

        fedoraContent.setContent(dsContent);

        try {
            String dsPath = fedoraObject.getPath() + "/" +
                    (dsEntity.getDsName().equals(Constants.DEFAULT_ANNOTATION_STRING_VALUE_TOKEN) ? dsProp.getName() : dsEntity.getDsName());

            if (repository.exists(dsPath)) {
                FedoraDatastream datastream = repository.getDatastream(dsPath);
                datastream.updateContent(fedoraContent);
                return datastream;
            } else {
                return repository.createDatastream(dsPath, fedoraContent);
            }

        } catch (FedoraException e) {
            throw new RuntimeException(e);
        }


    }

    private void readDatastreams(Object bean, FedoraObjectPersistentEntity<?> entity, FedoraObject fedoraObject) {
        entity.doWithDatastreams(dsProp -> {
            DatastreamPersistentEntity<?> dsEntity = (DatastreamPersistentEntity<?>) mappingContext.getPersistentEntity(dsProp.getType());
            PersistentPropertyAccessor propsAccessor = entity.getPropertyAccessor(bean);
            try {
                String dsPath = fedoraObject.getPath() + "/" +
                        (dsEntity.isDefaultDatastreamName() ? dsProp.getName() : dsEntity.getDsName());
                propsAccessor.setProperty(dsProp, createDatastreamBeanProxy((DatastreamPersistentProperty) dsProp, dsPath, dsEntity));
                logger.debug("Created a dynamic proxy for a datastream property " + dsProp.getName() + " of bean " +
                        bean.getClass().getSimpleName());
            } catch (FedoraException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Object createDatastreamBeanProxy(DatastreamPersistentProperty dsProp, String dsPath, DatastreamPersistentEntity<?> dsEntity) {
        try {
            return new ByteBuddy()
                    .subclass(dsProp.getType())
                    .implement(DatastreamDynamicProxy.class)
                    .method(ElementMatchers.isGetter())
                    .intercept(MethodDelegation.to(new DatastreamDynamicProxyInterceptor(dsPath, dsEntity, this)))
                    .make()
                    .load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                    .getLoaded()
                    .newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new MappingException("Cannot instantiate a dynamic proxy for a datastream property " +
                    dsProp.getName() + " of type " + dsEntity.getType().getSimpleName(), e);
        }
    }

    private void readDatastreamContent(Object dsBean, DatastreamPersistentEntity<?> dsEntity, FedoraDatastream fedoraDatastream){
        try {
            dsEntity.getPropertyAccessor(dsBean).setProperty(dsEntity.getContentProperty(), fedoraDatastream.getContent());
        } catch (FedoraException e) {
            throw new RuntimeException(e);
        }
    }
}
