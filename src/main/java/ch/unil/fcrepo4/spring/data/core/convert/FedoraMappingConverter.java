package ch.unil.fcrepo4.spring.data.core.convert;


/*
 * Based by org.springframework.data.mongodb.core.convert.MappingMongoConverter
 */


import ch.unil.fcrepo4.spring.data.core.mapping.*;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraRepository;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.mapping.model.BeanWrapper;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.util.Assert;

/**
 * @author gushakov
 */
public class FedoraMappingConverter implements FedoraConverter {
    private FedoraRepository repository;

    private FedoraMappingContext mappingContext;

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
        Assert.notNull(sink);
        if (source == null) {
            return;
        }

    }

    protected FedoraObject prepareFedoraObjectForWrite(Object source) {
        final FedoraPersistentEntity<?> ent = mappingContext.getPersistentEntity(source.getClass());

        if (ent == null) {
            throw new MappingException("Cannot find entity for " + source.getClass().getName());
        }

        if (!FedoraObjectPersistentEntity.class.isAssignableFrom(ent.getClass())) {
            throw new MappingException("Entity " + ent.getName() + " is not of type FedoraObjectPersistentEntity");
        }

        final FedoraObjectPersistentEntity<?> entity = (FedoraObjectPersistentEntity<?>) ent;
        final String namespace = entity.getNamespace();

        FedoraPersistentProperty idProp = entity.getIdProperty();
        if (idProp == null) {
            throw new MappingException("No ID property for entity " + entity.getName());
        }

        if (!UuidPersistentProperty.class.isAssignableFrom(idProp.getClass())) {
            throw new MappingException("ID property " + idProp.getName() + " is not of type UuidPersistentProperty");
        }

        final UuidPersistentProperty uuidProp = (UuidPersistentProperty) idProp;

        // get path creator registered via Uuid annotation
        final PathCreator pathCreator = uuidProp.getPathCreator();

        // get uuid value of the source bean
        final Object uuid = entity.getPropertyAccessor(source).getProperty(uuidProp);

        if (uuid == null) {
            throw new IllegalStateException("Uuid must not be null for source object");
        }

        //TODO: generalize to use Serializable for UUIDs

        if (!(uuid instanceof String)) {
            throw new IllegalStateException("Uuid must be of type String");
        }

        // find or create Fedora object
        final String path = pathCreator.createPath(namespace, (String) uuid);
        try {
            return repository.findOrCreateObject(path);
        } catch (FedoraException e) {
            throw new MappingException("Cannot find or create Fedora object with path " + path);
        }
    }

    @Override
    public FedoraObject write(Object source) {
        Assert.notNull(source);
        final FedoraObject fedoraObject = prepareFedoraObjectForWrite(source);
        write(source, fedoraObject);
        return fedoraObject;
    }
}
