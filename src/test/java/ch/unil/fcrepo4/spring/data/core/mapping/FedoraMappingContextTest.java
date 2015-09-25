package ch.unil.fcrepo4.spring.data.core.mapping;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.*;
import org.fcrepo.kernel.api.RdfLexicon;
import org.junit.Test;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentProperty;

import java.io.InputStream;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author gushakov
 */
public class FedoraMappingContextTest {

    @FedoraObject
    static class Bean1 {

        @Path
        long id = 1L;

        @Uuid
        String uuid;

    }

    @FedoraObject
    static class Bean2 {

        @Path
        long id = 1L;

        @Uuid
        UUID uniqueId;

    }

    @FedoraObject
    static class Bean3 {

        @Path
        long id = 1L;

        @Created
        Date createdAt;

    }

    @FedoraObject
    static class Bean4 {

        @Path
        long id = 1L;

        Datastream1 datastream1 = new Datastream1();

    }

    @Datastream
    static class Datastream1 {

        @DsContent
        InputStream dsContent;
    }


    @Test
    public void testFedoraObjectProperties() throws Exception {
        FedoraMappingContext context = new FedoraMappingContext();
        context.setInitialEntitySet(new HashSet<>(Arrays.asList(Bean1.class, Bean2.class, Bean3.class)));
        context.initialize();
        assertThat(context.getPersistentEntities().size()).isGreaterThan(0);
        GenericFedoraPersistentEntity<?> entity1 = context.getPersistentEntity(Bean1.class);
        assertThat(entity1).isNotNull();
        assertThat(entity1.getIdProperty()).isNotNull();
        GenericFedoraPersistentEntity<?> entity2 = context.getPersistentEntity(Bean2.class);
        UuidPersistentProperty uniqueIdProp = (UuidPersistentProperty) entity2.getPersistentProperty(Uuid.class);
        assertThat(uniqueIdProp.getLocalName()).isEqualTo(RdfLexicon.HAS_PRIMARY_IDENTIFIER.getLocalName());
        assertThat(uniqueIdProp.getUriNs()).isEqualTo(RdfLexicon.HAS_PRIMARY_IDENTIFIER.getNameSpace());
        GenericFedoraPersistentEntity<?> entity3 = context.getPersistentEntity(Bean3.class);
        CreatedPersistentProperty createdAtProperty = (CreatedPersistentProperty) entity3.getPersistentProperty("createdAt");
        assertThat(createdAtProperty.getLocalName()).isEqualTo(RdfLexicon.CREATED_DATE.getLocalName());
        assertThat(createdAtProperty.getUriNs()).isEqualTo(RdfLexicon.CREATED_DATE.getNameSpace());
    }

    @Test
    public void testDatastreamAssociation() throws Exception {
        FedoraMappingContext context = new FedoraMappingContext();
        context.setInitialEntitySet(new HashSet<>(Arrays.asList(Bean4.class, Datastream1.class)));
        context.initialize();
        GenericFedoraPersistentEntity<?> entity = context.getPersistentEntity(Bean4.class);
        final int[] count = new int[]{0};
        entity.doWithAssociations((Association<? extends PersistentProperty<?>> association) -> {
            if (association.getInverse() instanceof DatastreamPersistentProperty){
                count[0]++;
                DatastreamPersistentProperty dsProp = (DatastreamPersistentProperty) association.getInverse();
                DatastreamPersistentEntity<?> dsEntity = (DatastreamPersistentEntity<?>) context.getPersistentEntity(dsProp.getType());
                assertThat(dsEntity.getPersistentProperty(DsContent.class)).isNotNull();
            }
        });
        assertThat(count[0]).isEqualTo(1);
    }

}
