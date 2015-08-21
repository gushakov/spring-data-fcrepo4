package ch.unil.fcrepo4.spring.data.core.mapping;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.*;
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
        String path = "/foo/bar/123";

        @Uuid
        String uuid;

    }

    @FedoraObject
    static class Bean2 {

        @Path
        String path = "/foo/bar/456";

        @Uuid
        UUID uuid;

    }

    @FedoraObject
    static class Bean3 {

        @Path
        String path = "/foo/bar/456";

        @Created
        Date created;

    }

    @FedoraObject
    static class Bean4 {

        @Path
        String path = "/foo/bar/456";

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
        context.setInitialEntitySet(new HashSet<>(Arrays.asList(Bean1.class, Bean2.class)));
        context.initialize();
        assertThat(context.getPersistentEntities().size()).isGreaterThan(0);
        GenericFedoraPersistentEntity<?> entity1 = context.getPersistentEntity(Bean1.class);
        assertThat(entity1).isNotNull();
        assertThat(entity1.getIdProperty()).isNotNull();
        UuidPersistentProperty uuidProp1 = (UuidPersistentProperty) entity1.getPersistentProperty(Uuid.class);
        assertThat(uuidProp1).isNotNull();
        assertThat(uuidProp1.isUUID()).isFalse();
        GenericFedoraPersistentEntity<?> entity2 = context.getPersistentEntity(Bean2.class);
        UuidPersistentProperty uuidProp2 = (UuidPersistentProperty) entity2.getPersistentProperty(Uuid.class);
        assertThat(uuidProp2).isNotNull();
        assertThat(uuidProp2.isUUID()).isTrue();
    }

    @Test
    public void testCreatedDate() throws Exception {
        FedoraMappingContext context = new FedoraMappingContext();
        context.setInitialEntitySet(new HashSet<>(Collections.singletonList(Bean3.class)));
        context.initialize();
        GenericFedoraPersistentEntity<?> entity = context.getPersistentEntity(Bean3.class);
        CreatedPersistentProperty createdProp = (CreatedPersistentProperty) entity.getPersistentProperty("created");
        assertThat(createdProp).isNotNull();
        assertThat(createdProp.isDate()).isTrue();
        assertThat(createdProp.isZonedDateTime()).isFalse();
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
