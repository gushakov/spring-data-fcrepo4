package ch.unil.fcrepo4.spring.data.core.convert;


/*
 * Based on code from:
 * org.springframework.data.mongodb.core.convert.MappingMongoConverter,
 * org.springframework.data.solr.core.convert.MappingSolrConverter
 */


import ch.unil.fcrepo4.spring.data.core.Constants;
import ch.unil.fcrepo4.spring.data.core.convert.rdf.ExtendedXsdDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.mapping.*;
import ch.unil.fcrepo4.utils.Utils;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.fcrepo.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.util.Assert;

/**
 * @author gushakov
 */
public class FedoraMappingConverter implements FedoraConverter {
    private static final Logger logger = LoggerFactory.getLogger(FedoraMappingConverter.class);

    private FedoraRepository repository;

    private MappingContext<? extends FedoraPersistentEntity<?>, FedoraPersistentProperty> mappingContext;

    private ConversionService conversionService;

    private RdfDatatypeConverter rdfDatatypeConverter;

    public FedoraMappingConverter(FedoraRepository repository) {
        Assert.notNull(repository);
        this.repository = repository;
        final FedoraMappingContext context = new FedoraMappingContext();
        context.afterPropertiesSet();
        this.mappingContext = context;
        this.conversionService = new DefaultConversionService();
        this.rdfDatatypeConverter = new ExtendedXsdDatatypeConverter();
    }

    @Override
    public RdfDatatypeConverter getRdfDatatypeConverter() {
        return rdfDatatypeConverter;
    }

    @Override
    public <T> T read(Class<T> beanType, FedoraResource fedoraResource) {
        FedoraPersistentEntity<?> entity = mappingContext.getPersistentEntity(beanType);
        T bean;
        try {
            bean = beanType.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new MappingException("Cannot instantiate bean of type " + beanType.getName(), e);
        }
        readSimpleProperties(bean, entity, fedoraResource);

        // set the ID (path) for the bean if we are reading from a Fedora object
        if (entity instanceof FedoraObjectPersistentEntity && fedoraResource instanceof FedoraObject) {
            readPath(bean, (FedoraObjectPersistentEntity<?>) entity, (FedoraObject) fedoraResource);
        }

        // wrap the bean in the dynamic proxy
        return wrapInDynamicBeanProxy(bean, beanType, entity, fedoraResource);
    }

    private <T> T wrapInDynamicBeanProxy(T bean, Class<T> beanType, FedoraPersistentEntity<?> entity, FedoraResource fedoraResource) {
        try {
            return new ByteBuddy()
                    .subclass(beanType)
                    .implement(DynamicBeanProxy.class)
                    .method(ElementMatchers.is(DynamicBeanProxy.GET_BEAN_ENTITY_METHOD)
                            .or(ElementMatchers.is(DynamicBeanProxy.GET_PROPERTY_ACCESSOR_METHOD))
                            .or(ElementMatchers.is(DynamicBeanProxy.DO_WITH_PROPERTIES_METHOD))
                            .or(ElementMatchers.isGetter())
                            .or(ElementMatchers.isSetter()))
                    .intercept(MethodDelegation.to(new DynamicBeanProxyInterceptor(bean, entity, fedoraResource, this)))
                    .make()
                    .load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                    .getLoaded()
                    .newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new MappingException("Cannot create dynamic bean proxy for " + bean, e);
        }
    }

    @Override
    public void write(Object bean, FedoraResource fedoraResource) {
        writeSimpleProperties(bean, fedoraResource);

    }

    private void writeSimpleProperties(Object bean, FedoraResource fedoraResource) {
        TriplesCollectingPropertyHandler triplesCollector;
        if (bean instanceof DynamicBeanProxy) {
            DynamicBeanProxy beanProxy = (DynamicBeanProxy) bean;
            triplesCollector = new TriplesCollectingPropertyHandler(beanProxy.__getPropertyAccessor(), rdfDatatypeConverter);
            beanProxy.doWithProperties(triplesCollector, true);
            ElementTriplesBlock insertTriples = triplesCollector.getInsertTriples();
            ElementTriplesBlock deleteWhereTriples = triplesCollector.getDeleteWhereTriples();
            if (!insertTriples.isEmpty()) {
                try {
                    String update = "DELETE { " + deleteWhereTriples + " }\n" +
                            "INSERT { " + insertTriples + " }\n" +
                            "WHERE { " + deleteWhereTriples + "}";
                    logger.debug("SPARQL (update): {}", update);
                    fedoraResource.updateProperties(update);
                } catch (FedoraException e) {
                    throw new MappingException("Cannot update properties of the resource " + fedoraResource);
                }
            }

        } else {
            FedoraPersistentEntity<?> entity = mappingContext.getPersistentEntity(bean.getClass());
            PersistentPropertyAccessor propsAccessor = entity.getPropertyAccessor(bean);
            triplesCollector = new TriplesCollectingPropertyHandler(propsAccessor, rdfDatatypeConverter);
            ElementTriplesBlock insertTriples = triplesCollector.getInsertTriples();
            // add ocm class name for the new bean
            insertTriples.addTriple(0, new Triple(NodeFactory.createURI(""),
                    NodeFactory.createURI(Constants.OCM_URI_NAMESPACE + Constants.OCM_CLASS_PROPERTY),
                    rdfDatatypeConverter.encodeLiteralValue(entity.getType().getName())));
            entity.doWithProperties(triplesCollector);

            try {
                String insert = "INSERT DATA { " + insertTriples + " }";
                logger.debug("SPARQL (insert): {}", insert);
                fedoraResource.updateProperties(insert);
            } catch (FedoraException e) {
                throw new MappingException("Cannot update properties of the resource " + fedoraResource);
            }
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
    public <T> FedoraObject getFedoraObject(T bean) {
        String fullPath = getFedoraObjectPath(bean);

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
    public <T, ID> FedoraObject getFedoraObject(ID path, Class<T> beanType) {
        String fullPath = getFedoraObjectPath(path, beanType);

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

    @Override
    @SuppressWarnings("unchecked")
    public <T> String getFedoraObjectPath(T bean) {
        FedoraPersistentEntity<?> ent;
        if (bean instanceof DynamicBeanProxy){
            ent = ((DynamicBeanProxy)bean).__getBeanEntity();
        }
        else {
            ent = mappingContext.getPersistentEntity(bean.getClass());
        }
        if (!(ent instanceof FedoraObjectPersistentEntity)) {
            throw new MappingException("Cannot map a persistent entity of type " + ent.getType() + " to a Fedora object");
        }
        FedoraObjectPersistentEntity<?> entity = (FedoraObjectPersistentEntity<?>) ent;

        FedoraPersistentProperty idProp = entity.getIdProperty();
        if (idProp == null) {
            throw new MappingException("No path property for entity " + entity.getName());
        }

        if (!PathPersistentProperty.class.isAssignableFrom(idProp.getClass())) {
            throw new MappingException("ID property " + idProp.getName() + " is not of type " + idProp.getClass().getName());
        }

        final PathPersistentProperty pathProp = (PathPersistentProperty) idProp;

        // get path creator registered via Path annotation
        final PathCreator pathCreator = pathProp.getPathCreator();

        // get path value of the bean
        Object path;
        if (bean instanceof DynamicBeanProxy){
            path = ((DynamicBeanProxy)bean).__getPropertyAccessor().getProperty(pathProp);
        }
        else {
            path = entity.getPropertyAccessor(bean).getProperty(pathProp);
        }

        // create full path for the target Fedora object
        return pathCreator.createPath(entity.getNamespace(), entity.getType(), idProp.getType(), idProp.getName(), path);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, ID> String getFedoraObjectPath(ID id, Class<T> beanType) {
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
            throw new MappingException("ID property " + idProp.getName() + " is not of type PathPersistentProperty");
        }

        final PathPersistentProperty pathProp = (PathPersistentProperty) idProp;

        // get path creator registered via Path annotation
        final PathCreator pathCreator = pathProp.getPathCreator();

        // create full path for the target Fedora object
        return pathCreator.createPath(entity.getNamespace(), entity.getType(), idProp.getType(), idProp.getName(), id);
    }

    @Override
    public boolean exists(String path) {
        Assert.notNull(path);
        try {
            return repository.exists(path);
        } catch (FedoraException e) {
            throw new RuntimeException(e);
        }
    }

    private void readSimpleProperties(Object bean, FedoraPersistentEntity<?> entity, FedoraResource fedoraResource) {
        PersistentPropertyAccessor propsAccessor = entity.getPropertyAccessor(bean);
        entity.doWithProperties((PersistentProperty<?> property) -> {
            if (property instanceof SimpleFedoraResourcePersistentProperty) {
                SimpleFedoraResourcePersistentProperty simpleProp = (SimpleFedoraResourcePersistentProperty) property;
                try {
                    Node literal = Utils.getObjectLiteral(fedoraResource.getProperties(),
                            simpleProp.getUri());
                    if (literal != null) {
                        propsAccessor.setProperty(property, rdfDatatypeConverter.parseLiteralValue(literal.getLiteralLexicalForm(), property.getType()));
                    }
                } catch (FedoraException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

/*
    private void writeDatastreams(Object source, FedoraObjectPersistentEntity<?> entity, FedoraObject fedoraObject) {
        entity.doWithDatastreams(dsProp -> {
            Object dsBean = entity.getPropertyAccessor(source).getProperty(dsProp);
            if (dsBean != null) {
                DatastreamPersistentEntity<?> dsEntity = (DatastreamPersistentEntity<?>) mappingContext.getPersistentEntity(dsProp.getType());
                FedoraDatastream datastream = createDatastream(dsBean, (DatastreamPersistentProperty) dsProp, dsEntity, fedoraObject);
                writeSimpleProperties(dsBean, dsEntity, datastream);
            }
        });
    }
*/

    /*private FedoraDatastream createDatastream(Object dsBean, DatastreamPersistentProperty dsProp, DatastreamPersistentEntity<?> dsEntity, FedoraObject fedoraObject) {
        FedoraContent fedoraContent = new FedoraContent();
        fedoraContent.setContentType(dsEntity.getContentProperty().getMimetype());

        InputStream dsContent = (InputStream) dsEntity.getPropertyAccessor(dsBean).getProperty(dsEntity.getContentProperty());

        if (dsContent == null) {
            throw new MappingException("Content of datastream " + dsProp.getName() + " must not be null");
        }

        fedoraContent.setContent(dsContent);

        try {
            String dsPath = fedoraObject.getPath() + "/" +
                    (dsProp.getDsName().equals(Constants.DEFAULT_ANNOTATION_STRING_VALUE_TOKEN) ? dsProp.getName() : dsProp.getDsName());

            //TODO: bug in fcrepo-client 4.4.1.snapshot
            dsPath = dsPath.replaceFirst("*//*", "/");

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


    }*/

    @Override
    public void readDatastreamContent(Object dsBean, DatastreamPersistentEntity<?> dsEntity, FedoraDatastream fedoraDatastream) {
        try {
            dsEntity.getPropertyAccessor(dsBean).setProperty(dsEntity.getContentProperty(), fedoraDatastream.getContent());
        } catch (FedoraException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void readPath(Object bean, FedoraObjectPersistentEntity<?> entity, FedoraObject fedoraObject) {
        PathPersistentProperty pathProp = (PathPersistentProperty) entity.getIdProperty();
        try {
            entity.getPropertyAccessor(bean).setProperty(pathProp,
                    pathProp.getPathCreator().parsePath(entity.getNamespace(), bean.getClass(), pathProp.getType(), pathProp.getName(), fedoraObject.getPath()));
        } catch (FedoraException e) {
            throw new RuntimeException(e);
        }
    }
}
