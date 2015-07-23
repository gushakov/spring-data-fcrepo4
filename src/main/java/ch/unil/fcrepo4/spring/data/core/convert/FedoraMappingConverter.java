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
import org.fcrepo.client.*;
import org.fcrepo.kernel.RdfLexicon;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

/**
 * @author gushakov
 */
public class FedoraMappingConverter implements FedoraConverter {
    private FedoraRepository repository;

    private MappingContext<? extends FedoraPersistentEntity<?>, FedoraPersistentProperty> mappingContext;

    private ConversionService conversionService;

    private TaskExecutor jaxbMarshallingTaskExecutor;

    public FedoraMappingConverter(FedoraRepository repository) {
        Assert.notNull(repository);
        this.repository = repository;
        final FedoraMappingContext context = new FedoraMappingContext();
        context.afterPropertiesSet();
        this.mappingContext = context;
        this.conversionService = new DefaultConversionService();
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(2);
        taskExecutor.setMaxPoolSize(4);
        taskExecutor.initialize();
        this.jaxbMarshallingTaskExecutor = taskExecutor;
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
        FedoraPersistentEntity<?> entity = getFedoraPersistentEntity(source);
        FedoraObject fedoraObject = createFedoraObject(source, (FedoraObjectPersistentEntity<?>) entity);
        readFedoraObjectProperties(source, (FedoraObjectPersistentEntity<?>) entity, fedoraObject);
        writeDatastreams(source, (FedoraObjectPersistentEntity<?>) entity, fedoraObject);
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
        FedoraPersistentEntity<?> entity = mappingContext.getPersistentEntity(source.getClass());

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

    protected void writeDatastreams(Object source, FedoraObjectPersistentEntity<?> entity, FedoraObject sink) {
        entity.doWithProperties((PersistentProperty<?> property) -> {
            if (property instanceof DatastreamPersistentProperty) {
                DatastreamPersistentProperty dsProp = (DatastreamPersistentProperty) property;

                if (dsProp.getPath().matches("\\s*")) {
                    throw new MappingException("Datastream path cannot be empty");
                }
                createDatastream(source, entity, sink, dsProp);
            }
        });
    }

    protected FedoraDatastream createDatastream(Object source, FedoraObjectPersistentEntity<?> entity, FedoraObject sink, DatastreamPersistentProperty dsProp) {

        FedoraContent content = new FedoraContent();
        content.setContentType(dsProp.getMimetype());
        Object dsContent = entity.getPropertyAccessor(source).getProperty(dsProp);
        if (dsContent != null) {
            if (dsProp.getMimetype().equals(Constants.DATASTREAM_MIME_TYPE_TEXT_XML)) {

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
                    jaxbMarshallingTaskExecutor.execute(() -> {
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
            String dsPath = sink.getPath() + "/" + dsProp.getPath().replaceFirst("^/*", "");
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
}
