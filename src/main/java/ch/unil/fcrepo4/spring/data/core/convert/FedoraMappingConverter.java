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
import org.apache.commons.lang3.StringUtils;
import org.fcrepo.client.*;
import org.fcrepo.kernel.RdfLexicon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.mapping.PersistentProperty;
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
    public <T> T read(Class<T> type, FedoraObject fedoraObject) {
        FedoraObjectPersistentEntity<?> entity = (FedoraObjectPersistentEntity<?>) mappingContext.getPersistentEntity(type);
        T bean;
        try {
            bean = type.newInstance();
            readFedoraResourceProperties(bean, entity, fedoraObject);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return bean;
    }

    @Override
    public void write(Object source, FedoraObject fedoraObject) {
        writeSimpleProperties(source, (FedoraObjectPersistentEntity<?>) mappingContext.getPersistentEntity(source.getClass()), fedoraObject);
    }

    @Override
    public FedoraObject write(Object source) {
        Assert.notNull(source);
        FedoraPersistentEntity<?> entity = mappingContext.getPersistentEntity(source.getClass());
        FedoraObject fedoraObject = createFedoraObject(source, (FedoraObjectPersistentEntity<?>) entity);
        readFedoraResourceProperties(source, (FedoraObjectPersistentEntity<?>) entity, fedoraObject);
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


    protected void readFedoraResourceProperties(Object bean, GenericFedoraPersistentEntity<?> entity, FedoraResource fedoraResource) {
        PersistentPropertyAccessor propsAccessor = entity.getPropertyAccessor(bean);

        //
        // Uuid
        //
        UuidPersistentProperty uuidProp = (UuidPersistentProperty) entity.getPersistentProperty(Uuid.class);
        if (uuidProp != null) {

            // ignore if uuid property is set on the source bean

            try {
                Object uuid = Utils.getFedoraObjectProperty(fedoraResource, RdfLexicon.HAS_PRIMARY_IDENTIFIER.getLocalName());

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
                Object created = Utils.getFedoraObjectProperty(fedoraResource, RdfLexicon.CREATED_DATE.getLocalName());

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

    protected FedoraObject createFedoraObject(Object source, FedoraObjectPersistentEntity<?> entity) {
        String namespace = entity.getNamespace();

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

        // get path value of the source bean
        final Object path = entity.getPropertyAccessor(source).getProperty(pathProp);

        if (path == null) {
            throw new MappingException("Path cannot be null");
        }

        // create full JCR path for the target Fedora object
        String fullPath = pathCreator.createPath(namespace, entity.getType(), idProp.getType(), idProp.getName(), path);
        try {
            return repository.findOrCreateObject(fullPath);
        } catch (FedoraException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new FedoraResourcePathException("Invalid resource path: " + fullPath);
        }

    }

    protected void writeDatastreams(Object source, FedoraObjectPersistentEntity<?> entity, FedoraObject fedoraObject) {
        entity.doWithDatastreams(dsProp -> {
            Object dsBean = entity.getPropertyAccessor(source).getProperty(dsProp);
            if (dsBean == null) {
                throw new MappingException("Datastream " + dsProp.getName() + " must not be null, entity " + entity.getType().getSimpleName());
            }

            DatastreamPersistentEntity<?> dsEntity = (DatastreamPersistentEntity<?>) mappingContext.getPersistentEntity(dsProp.getType());

            FedoraDatastream datastream = createDatastream(fedoraObject, dsBean, (DatastreamPersistentProperty) dsProp, dsEntity);
            readFedoraResourceProperties(dsBean, dsEntity, datastream);
        });
    }

    protected FedoraDatastream createDatastream(FedoraObject fedoraObject, Object dsBean, DatastreamPersistentProperty dsProp, DatastreamPersistentEntity<?> dsEntity) {
        FedoraContent fedoraContent = new FedoraContent();
        fedoraContent.setContentType(dsEntity.getMimetype());

        InputStream dsContent = getDatastreamContent(dsBean, dsEntity);
        if (dsContent == null) {
            throw new MappingException("Content of datastream " + dsProp.getName() + " must not be null");
        }

        fedoraContent.setContent(dsContent);

        try {
            String dsName = dsEntity.getDsName();
            if (dsName.equals(Constants.DEFAULT_ANNOTATION_STRING_VALUE_TOKEN)) {
                // use the name of the datastream property (association) by default
                dsName = dsProp.getName();
            }
            String dsPath = fedoraObject.getPath() + "/" + dsName;

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

    protected InputStream getDatastreamContent(Object dsBean, DatastreamPersistentEntity<?> dsEntity) {
        DatastreamContentPersistentProperty dsContentProp = dsEntity.getContentProperty();
        return (InputStream) dsEntity.getPropertyAccessor(dsBean).getProperty(dsContentProp);
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
}
