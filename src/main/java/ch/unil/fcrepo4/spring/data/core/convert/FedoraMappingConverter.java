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
import org.springframework.data.mapping.*;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.util.Assert;

import java.io.InputStream;

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
                    .method(ElementMatchers.isDeclaredBy(DynamicBeanProxy.class)
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
        writeAssociations(bean);

        writeProperties(bean, fedoraResource);
    }

    private void writeProperties(Object bean, FedoraResource fedoraResource) {
        if (bean instanceof DynamicBeanProxy) {
            writePropertiesForProxy((DynamicBeanProxy) bean, fedoraResource);
        } else {
            writePropertiesForBean(bean, fedoraResource);
        }
    }

    private void writePropertiesForProxy(DynamicBeanProxy beanProxy, FedoraResource fedoraResource) {
        TriplesCollectingPropertyHandler triplesCollector = new TriplesCollectingPropertyHandler(beanProxy.__getPropertyAccessor(), this);
        beanProxy.__getBeanEntity().doWithProperties((SimplePropertyHandler) property -> {
            if (beanProxy.__isPropertyUpdated(property)) {
                triplesCollector.doWithPersistentProperty(property);
            }
        });
        ElementTriplesBlock insertTriples = triplesCollector.getInsertTriples();
        ElementTriplesBlock deleteWhereTriples = triplesCollector.getDeleteWhereTriples();
        if (!insertTriples.isEmpty()) {
            updateTriples(deleteWhereTriples, insertTriples, fedoraResource);
        }
    }

    private void writePropertiesForBean(Object bean, FedoraResource fedoraResource) {
        FedoraPersistentEntity<?> entity = mappingContext.getPersistentEntity(bean.getClass());
        PersistentPropertyAccessor propsAccessor = entity.getPropertyAccessor(bean);
        TriplesCollectingPropertyHandler triplesCollector = new TriplesCollectingPropertyHandler(propsAccessor, this);
        ElementTriplesBlock insertTriples = triplesCollector.getInsertTriples();
        // add ocm class name for the new bean
        insertTriples.addTriple(0, new Triple(NodeFactory.createURI(""),
                NodeFactory.createURI(Constants.OCM_URI_NAMESPACE + Constants.OCM_CLASS_PROPERTY),
                rdfDatatypeConverter.encodeLiteralValue(entity.getType().getName())));

        // add triples for simple properties
        entity.doWithProperties(triplesCollector);

        // add triples for relations
        entity.doWithAssociations(triplesCollector);

        ElementTriplesBlock deleteWhereTriples = triplesCollector.getDeleteWhereTriples();
        updateTriples(deleteWhereTriples, insertTriples, fedoraResource);
    }

   private void updateTriples(ElementTriplesBlock deleteWhereTriples, ElementTriplesBlock insertTriples, FedoraResource fedoraResource) {
       try {
           // delete all the updated properties first
           String delete = "DELETE { " + deleteWhereTriples + " }\n" +
                   "INSERT { }\n" +
                   "WHERE { " + deleteWhereTriples + "}";
           logger.debug("SPARQL (delete): {}", delete);
           fedoraResource.updateProperties(delete);
           // insert new values for updated properties
           String insert = "INSERT DATA { " + insertTriples + " }";
           logger.debug("SPARQL (insert): {}", insert);
           fedoraResource.updateProperties(insert);
       } catch (FedoraException e) {
           throw new MappingException("Cannot update properties of the resource " + fedoraResource, e);
       }
   }

    private void writeAssociations(Object bean) {
        if (bean instanceof DynamicBeanProxy) {
            DynamicBeanProxy beanProxy = (DynamicBeanProxy) bean;
            beanProxy.__getBeanEntity().doWithAssociations(

                    (SimpleAssociationHandler) association -> writeAssociation(beanProxy.__getPropertyAccessor().getBean(),
                            beanProxy.__getPropertyAccessor(), association)

            );

        } else {
            writeAssociations(bean, mappingContext.getPersistentEntity(bean.getClass()));
        }
    }

    private void writeAssociations(Object bean, FedoraPersistentEntity<?> entity) {
        PersistentPropertyAccessor propertyAccessor = entity.getPropertyAccessor(bean);
        entity.doWithAssociations((Association<? extends PersistentProperty<?>> association) -> {
            writeAssociation(bean, propertyAccessor, association);
        });
    }

    private void writeAssociation(Object bean, PersistentPropertyAccessor propertyAccessor, Association<? extends PersistentProperty<?>> association) {
        PersistentProperty<?> property = association.getInverse();

        if (property == null) {
            throw new IllegalStateException("Inverse property of an association " + association + " is null");
        }

        if (property instanceof DatastreamPersistentProperty) {

            // write datastream association

            DatastreamPersistentProperty dsProp = (DatastreamPersistentProperty) property;

            Object dsBean = propertyAccessor.getProperty(dsProp);

            if (dsBean != null) {
                DatastreamPersistentEntity<?> dsEntity = (DatastreamPersistentEntity<?>) mappingContext.getPersistentEntity(dsProp.getType());
                String dsPath = getDatastreamPath(bean, dsProp);
                FedoraResource datastream = createDatastream(dsBean, dsEntity, dsPath);
                writeProperties(dsBean, datastream);
            }
        }
        else if (property instanceof RelationPersistentProperty) {

            // write relation association
            RelationPersistentProperty relProp = (RelationPersistentProperty) property;
            Object relBean = propertyAccessor.getProperty(relProp);
            if (relBean != null){
                write(relBean, getFedoraObject(relBean));
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
            throw new MappingException(e.getMessage(), e);
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
            throw new MappingException(e.getMessage(), e);
        }
    }

    @Override
    public FedoraDatastream fetchDatastream(String dsPath) {
        try {
            return repository.getDatastream(dsPath);
        } catch (FedoraException e) {
            throw new MappingException(e.getMessage(), e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> String getFedoraObjectPath(T bean) {
        FedoraPersistentEntity<?> ent;
        if (bean instanceof DynamicBeanProxy) {
            ent = ((DynamicBeanProxy) bean).__getBeanEntity();
        } else {
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
        if (bean instanceof DynamicBeanProxy) {
            path = ((DynamicBeanProxy) bean).__getPropertyAccessor().getProperty(pathProp);
        } else {
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
    public <T> String getFedoraObjectUrl(T bean) {
        return repository.getRepositoryUrl() + getFedoraObjectPath(bean);
    }

    @Override
    public boolean exists(String path) {
        Assert.notNull(path);
        try {
            return repository.exists(path);
        } catch (FedoraException e) {
            throw new MappingException(e.getMessage(), e);
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
                    throw new MappingException(e.getMessage(), e);
                }
            }
        });
    }

    @Override
    public <T> String getDatastreamPath(T bean, DatastreamPersistentProperty dsProp) {
        return getFedoraObjectPath(bean) + "/" + dsProp.getDsName();
    }

    @Override
    public <T> Object readDatastream(T bean, FedoraPersistentEntity<?> entity, DatastreamPersistentProperty dsProp) {
        String dsPath = getDatastreamPath(bean, dsProp);

        // check if datastream exists, read it into the bean if it does
        if (exists(dsPath)) {
            Object dsBean = read(dsProp.getType(), fetchDatastream(dsPath));
            entity.getPropertyAccessor(bean).setProperty(dsProp, dsBean);
            return dsBean;
        }

        return null;
    }

    private FedoraDatastream createDatastream(Object dsBean, DatastreamPersistentEntity<?> dsEntity, String dsPath) {

        InputStream dsContent;
        if (dsBean instanceof DynamicBeanProxy) {
            DynamicBeanProxy dsBeanProxy = (DynamicBeanProxy) dsBean;
            dsContent = (InputStream) dsBeanProxy.__getPropertyAccessor().getProperty(dsEntity.getContentProperty());

            // binary content might not have been loaded for the proxy
            try {
                if (dsContent == null && repository.exists(dsPath)) {
                    dsContent = readDatastreamContent(dsBeanProxy.__getBean(), dsEntity, fetchDatastream(dsPath));
                }
            } catch (FedoraException e) {
                throw new MappingException(e.getMessage(), e);
            }
        } else {
            dsContent = (InputStream) dsEntity.getPropertyAccessor(dsBean).getProperty(dsEntity.getContentProperty());
        }

        // at this point if binary content is null throw an exception
        if (dsContent == null) {
            throw new MappingException("Binary content for datastream: " + dsPath + " is null.");
        }

        FedoraContent fedoraContent = new FedoraContent();
        fedoraContent.setContentType(dsEntity.getContentProperty().getMimetype());
        fedoraContent.setContent(dsContent);

        FedoraDatastream datastream;
        try {

            if (repository.exists(dsPath)) {
                datastream = repository.getDatastream(dsPath);
                datastream.updateContent(fedoraContent);
            } else {
                datastream = repository.createDatastream(dsPath, fedoraContent);
            }

        } catch (FedoraException e) {
            throw new MappingException(e.getMessage(), e);
        }

        return datastream;
    }

    @Override
    public InputStream readDatastreamContent(Object dsBean, DatastreamPersistentEntity<?> dsEntity, FedoraDatastream fedoraDatastream) {
        try {
            InputStream content = fedoraDatastream.getContent();
            dsEntity.getPropertyAccessor(dsBean).setProperty(dsEntity.getContentProperty(), content);
            return content;
        } catch (FedoraException e) {
            throw new MappingException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private void readPath(Object bean, FedoraObjectPersistentEntity<?> entity, FedoraObject fedoraObject) {
        PathPersistentProperty pathProp = (PathPersistentProperty) entity.getIdProperty();
        try {
            entity.getPropertyAccessor(bean).setProperty(pathProp,
                    pathProp.getPathCreator().parsePath(entity.getNamespace(), bean.getClass(), pathProp.getType(), pathProp.getName(), fedoraObject.getPath()));
        } catch (FedoraException e) {
            throw new MappingException(e.getMessage(), e);
        }
    }
}
