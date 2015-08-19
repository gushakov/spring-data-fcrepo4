package ch.unil.fcrepo4.spring.data.core.convert;


/*
 * Based by org.springframework.data.mongodb.core.convert.MappingMongoConverter
 */


import ch.unil.fcrepo4.spring.data.core.Constants;
import ch.unil.fcrepo4.spring.data.core.FedoraResourcePathException;
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
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.util.Assert;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author gushakov
 */
public class FedoraMappingConverter implements FedoraConverter, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(FedoraMappingConverter.class);

    private FedoraRepository repository;

    private MappingContext<? extends FedoraPersistentEntity<?>, FedoraPersistentProperty> mappingContext;

    private ConversionService conversionService;

    private ExecutorService threadPool;

    public FedoraMappingConverter(FedoraRepository repository) {
        Assert.notNull(repository);
        this.repository = repository;
        final FedoraMappingContext context = new FedoraMappingContext();
        context.afterPropertiesSet();
        this.mappingContext = context;
        this.conversionService = new DefaultConversionService();
        this.threadPool = Executors.newFixedThreadPool(4);
    }

    @Override
    public <T> T read(Class<T> type, FedoraObject fedoraObject) {
        FedoraObjectPersistentEntity<?> entity = (FedoraObjectPersistentEntity<?>) getFedoraPersistentEntity(type);
        T bean;
        try {
            bean = type.newInstance();
            readFedoraObjectProperties(bean, entity, fedoraObject);
            readDatastreams(bean, entity, fedoraObject);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return bean;
    }

    @Override
    public void write(Object source, FedoraObject fedoraObject) {
        writeSimpleProperties(source, (FedoraObjectPersistentEntity<?>) getFedoraPersistentEntity(source), fedoraObject);
    }

    @Override
    public FedoraObject write(Object source) {
        Assert.notNull(source);
        FedoraPersistentEntity<?> entity = getFedoraPersistentEntity(source);
        FedoraObject fedoraObject = createFedoraObject(source, (FedoraObjectPersistentEntity<?>) entity);
        readFedoraObjectProperties(source, (FedoraObjectPersistentEntity<?>) entity, fedoraObject);
        writeSimpleProperties(source, (FedoraObjectPersistentEntity<?>) entity, fedoraObject);
        writeDatastreams(source, (FedoraObjectPersistentEntity<?>) entity, fedoraObject);
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
        FedoraPersistentEntity<?> entity = mappingContext.getPersistentEntity(source.getClass());

        if (entity == null) {
            throw new MappingException("Cannot find entity for " + source.getClass().getName());
        }

        if (!FedoraObjectPersistentEntity.class.isAssignableFrom(entity.getClass())) {
            throw new MappingException("Entity " + entity.getName() + " is not of type FedoraObjectPersistentEntity");
        }

        return entity;
    }

    private FedoraPersistentEntity<?> getFedoraPersistentEntity(Class<?> beanType) {

        FedoraPersistentEntity<?> entity = mappingContext.getPersistentEntity(beanType);

        if (entity == null) {
            throw new MappingException("Cannot find entity for " + beanType.getName());
        }

        if (!FedoraObjectPersistentEntity.class.isAssignableFrom(entity.getClass())) {
            throw new MappingException("Entity " + entity.getName() + " is not of type FedoraObjectPersistentEntity");
        }

        return entity;

    }

    protected void readFedoraObjectProperties(Object bean, FedoraObjectPersistentEntity<?> entity, FedoraObject fedoraObject) {
        PersistentPropertyAccessor propsAccessor = entity.getPropertyAccessor(bean);

        //
        // Uuid
        //
        UuidPersistentProperty uuidProp = (UuidPersistentProperty) entity.getPersistentProperty(Uuid.class);
        if (uuidProp != null) {

            // ignore if uuid property is set on the source bean

            try {
                Object uuid = Utils.getFedoraObjectProperty(fedoraObject, RdfLexicon.HAS_PRIMARY_IDENTIFIER.getLocalName());

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
        if (uuidProp != null) {
            // ignore if created property is set on the source bean

            try {
                Object created = Utils.getFedoraObjectProperty(fedoraObject, RdfLexicon.CREATED_DATE.getLocalName());

                if (created == null) {
                    throw new MappingException("No " + RdfLexicon.CREATED_DATE.getURI() + " property found");
                }

                ZonedDateTime dateTime = ZonedDateTime.parse((String) created);

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

    protected FedoraObject createFedoraObject(Object source, FedoraObjectPersistentEntity<?> entity) {
        String namespace = entity.getNamespace();

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
            throw new MappingException("Path cannot be null");
        }

        if (!(path instanceof String)) {
            throw new MappingException("Path must be of type String");
        }

        // create full JCR path for the target Fedora object
        String fullPath = pathCreator.createPath(namespace, (String) path);
        try {
            return repository.findOrCreateObject(fullPath);
        } catch (FedoraException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new FedoraResourcePathException("Invalid resource path: " + fullPath);
        }

    }

    protected void writeDatastreams(Object source, FedoraObjectPersistentEntity<?> entity, FedoraObject fedoraObject) {
        entity.doWithProperties((PersistentProperty<?> property) -> {
            if (property instanceof DatastreamPersistentProperty) {
                DatastreamPersistentProperty dsProp = (DatastreamPersistentProperty) property;

                if (dsProp.getPath().matches("\\s*")) {
                    throw new MappingException("Datastream path cannot be empty");
                }
                createDatastream(source, entity, fedoraObject, dsProp);
            }
        });
    }

    protected void readDatastreams(Object bean, FedoraObjectPersistentEntity<?> entity, FedoraObject fedoraObject) {
        PersistentPropertyAccessor propsAccessor = entity.getPropertyAccessor(bean);
        entity.doWithProperties((PersistentProperty<?> property) -> {
            if (property instanceof DatastreamPersistentProperty) {
                DatastreamPersistentProperty dsProp = (DatastreamPersistentProperty) property;
                if (dsProp.getPath().matches("\\s*")) {
                    throw new MappingException("Datastream path cannot be empty");
                }

                // see if we need to create a reference to a dynamic proxy
                if (dsProp.getLazyLoad()) {
                    try {
                        Object proxy = new ByteBuddy()
                                .subclass(dsProp.getType())
                                .implement(DelegatingDynamicProxy.class)
                                .method(ElementMatchers.isGetter().or(ElementMatchers.isSetter()))
                                .intercept(MethodDelegation.to(new DatastreamDynamicProxyInterceptor(fedoraObject, dsProp, this)))
                                .make()
                                .load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                                .getLoaded()
                                .newInstance();
                        propsAccessor.setProperty(dsProp, proxy);
                        logger.debug("Created a dynamic proxy for a datastream property " + dsProp.getName() + " of bean " +
                                bean.getClass().getSimpleName());
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new MappingException("Cannot instantiate a dynamic proxy for a datastream property " +
                                dsProp.getName() + " of bean " + bean.getClass().getSimpleName(), e);
                    }
                } else {
                    // or just read the datastream object directly
                    propsAccessor.setProperty(dsProp, readDatastream(fedoraObject, dsProp));
                }
            }
        });
    }

    protected FedoraDatastream createDatastream(Object source, FedoraObjectPersistentEntity<?> entity, FedoraObject fedoraObject, DatastreamPersistentProperty dsProp) {
        FedoraContent content = new FedoraContent();
        content.setContentType(dsProp.getMimetype());
        Object dsContent = entity.getPropertyAccessor(source).getProperty(dsProp);
        if (dsContent != null) {
            if (dsProp.getMimetype().equals(Constants.MIME_TYPE_TEXT_XML)) {

                Marshaller marshaller;
                try {
                    JAXBContext jc = JAXBContext.newInstance(dsProp.getJaxbContextPath());
                    marshaller = jc.createMarshaller();
                } catch (JAXBException e) {
                    throw new MappingException(e.getMessage(), e);
                }

                PipedInputStream pipedIs = new PipedInputStream();
                content.setContent(pipedIs);
                try {
                    final PipedOutputStream pipedOs = new PipedOutputStream(pipedIs);

                    // see http://stackoverflow.com/a/1250655

                    threadPool.execute(() -> {
                        try {
                            marshaller.marshal(dsContent, new StreamResult(pipedOs));
                        } catch (JAXBException e) {
                            throw new MappingException(e.getMessage(), e);
                        } finally {
                            try {
                                pipedOs.flush();
                                pipedOs.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                } catch (IOException e) {
                    throw new MappingException(e.getMessage(), e);
                }

            } else {
                if (!(dsContent instanceof InputStream)) {
                    throw new MappingException("Invalid datastream content: " + dsContent.getClass());
                }
                content.setContent((InputStream) dsContent);
            }

        }

        try {
            String dsPath = fedoraObject.getPath() + "/" + dsProp.getPath().replaceFirst("^/*", "");
            if (repository.exists(dsPath)) {
                FedoraDatastream datastream = repository.getDatastream(dsPath);
                datastream.updateContent(content);
                return datastream;
            } else {
                return repository.createDatastream(dsPath, content);
            }

        } catch (FedoraException e) {
            throw new RuntimeException(e);
        }

    }

    public Object readDatastream(FedoraObject fedoraObject, DatastreamPersistentProperty dsProp) {

        try {
            String dsPath = Utils.getDatastreamPath(fedoraObject, dsProp);
            if (repository.exists(dsPath)) {
                FedoraDatastream datastream = repository.getDatastream(dsPath);

                //FIXME: datastream.getContentType() returns null

                String mimeType = datastream.getContentType();
                if (mimeType == null) {
                    mimeType = (String) Utils.getProperty(datastream.getProperties(), RdfLexicon.HAS_MIME_TYPE);
                }

                if (mimeType == null) {
                    throw new RuntimeException("Cannot determine mimetype of datastream " + dsPath);
                }

                if (mimeType.equals(Constants.MIME_TYPE_TEXT_XML)) {

                    Unmarshaller unmarshaller;
                    try {
                        JAXBContext jc = JAXBContext.newInstance(dsProp.getJaxbContextPath());
                        unmarshaller = jc.createUnmarshaller();
                        return unmarshaller.unmarshal(datastream.getContent());
                    } catch (JAXBException e) {
                        throw new MappingException(e.getMessage(), e);
                    }

                } else {
                    return datastream.getContent();
                }


            } else {
                throw new RuntimeException("No datastream with path " + dsPath);
            }

        } catch (FedoraException e) {
            throw new RuntimeException(e);
        }
    }

    protected void writeSimpleProperties(Object source, FedoraObjectPersistentEntity<?> entity, FedoraObject fedoraObject) {
        PersistentPropertyAccessor propsAccessor = entity.getPropertyAccessor(source);
        final List<String> inserts = new ArrayList<>();
        entity.doWithProperties((PersistentProperty<?> property) -> {
            if (property instanceof SimpleFedoraPersistentProperty) {
                SimpleFedoraPersistentProperty simpleProp = (SimpleFedoraPersistentProperty) property;
                Object propValue = propsAccessor.getProperty(simpleProp);
                inserts.add("<> <" + simpleProp.getUri() + "> "
                        + Utils.encodeLiteralValue(propValue, simpleProp.getType()));
            }
        });
        if (inserts.size() > 0) {
            logger.debug("Update: {}", "INSERT DATA { " + StringUtils.join(inserts, " . ") + " . }");
            try {
                fedoraObject.updateProperties("INSERT DATA { " + StringUtils.join(inserts, " . ") + " . }");
            } catch (FedoraException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        logger.debug("Shutting down thread pool before destroying FedoraConverter instance");
        threadPool.shutdown();
        threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }
}
