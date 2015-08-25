package ch.unil.fcrepo4.spring.data.core.mapping;

import ch.unil.fcrepo4.spring.data.core.Constants;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Datastream;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.DsContent;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.data.util.TypeInformation;

/**
 * @author gushakov
 */
public class DatastreamPersistentEntity<T> extends GenericFedoraPersistentEntity<T> implements FedoraPersistentEntity<T> {

    private Datastream dsAnnot;

    private String fedoraObjectUriNs;

    private FedoraObjectPersistentEntity<?> foEntity;

    public DatastreamPersistentEntity(TypeInformation<T> information) {
        super(information);
        this.dsAnnot = findAnnotation(Datastream.class);
        checkName();
    }

    private void checkName(){
        // name must not contain slashes
        if (!isDefaultDatastreamName() &&  dsAnnot.name().contains("/")){
            throw new MappingException("Invalid datastream name: " + dsAnnot);
        }
    }

    public FedoraObjectPersistentEntity<?> getFedoraObjectEntity() {
        return foEntity;
    }

    public void setFedoraObjectEntity(FedoraObjectPersistentEntity<?> foEntity) {
        this.foEntity = foEntity;
    }

    /**
     * Name of the datastream as specified on {@code @Datastream} type annotation.
     * @return datastream resource name
     */
    public String getDsName(){
        return dsAnnot.name();
    }

    public boolean isDefaultDatastreamName(){
        return dsAnnot.name().equals(Constants.DEFAULT_ANNOTATION_STRING_VALUE_TOKEN) || dsAnnot.name().matches("\\s*");
    }

    /**
     * Content type (mimetype) of the datastream as specified on {@code @Datastream} type annotation.
     * @return content type of the datastream
     */
    public String getMimetype(){
        return dsAnnot.mimetype();
    }

   public DatastreamContentPersistentProperty getContentProperty(){
       return (DatastreamContentPersistentProperty) getPersistentProperty(DsContent.class);
   }

}
