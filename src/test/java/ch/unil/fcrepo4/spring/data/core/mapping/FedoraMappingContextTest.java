package ch.unil.fcrepo4.spring.data.core.mapping;

import ch.unil.fcrepo4.jaxb.foobar.FoobarType;
import ch.unil.fcrepo4.spring.data.core.Constants;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.*;
import org.junit.Test;

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

        @Datastream(jaxbContextPath = "ch.unil.fcrepo4.jaxb.foobar")
        FoobarType foobarDs;

        @Datastream()
        Object anotherDs;

    }

    @FedoraObject
    static class Bean4 {

        @Path
        String path = "/foo/bar/456";

        @Created
        Date created;

    }

    @Test
    public void testFedoraObjectProperties() throws Exception {
        FedoraMappingContext context = new FedoraMappingContext();
        context.setInitialEntitySet(new HashSet<>(Arrays.asList(Bean1.class, Bean2.class)));
        context.initialize();
        assertThat(context.getPersistentEntities().size()).isGreaterThan(0);
        GenericFedoraPersistenceEntity<?> entity1 = context.getPersistentEntity(Bean1.class);
        assertThat(entity1).isNotNull();
        assertThat(entity1.getIdProperty()).isNotNull();
        UuidPersistentProperty uuidProp1 = (UuidPersistentProperty) entity1.getPersistentProperty(Uuid.class);
        assertThat(uuidProp1).isNotNull();
        assertThat(uuidProp1.isUUID()).isFalse();
        GenericFedoraPersistenceEntity<?> entity2 = context.getPersistentEntity(Bean2.class);
        UuidPersistentProperty uuidProp2 = (UuidPersistentProperty) entity2.getPersistentProperty(Uuid.class);
        assertThat(uuidProp2).isNotNull();
        assertThat(uuidProp2.isUUID()).isTrue();
    }

    @Test
    public void testJaxbDatastream() throws Exception {
        FedoraMappingContext context = new FedoraMappingContext();
        context.setInitialEntitySet(new HashSet<>(Collections.singletonList(Bean3.class)));
        context.initialize();
        GenericFedoraPersistenceEntity<?> entity = context.getPersistentEntity(Bean3.class);
        DatastreamPersistentProperty dsProp = (DatastreamPersistentProperty) entity.getPersistentProperty("foobarDs");
        assertThat(dsProp).isNotNull();
        assertThat(dsProp.getMimetype()).isEqualTo(Constants.MIME_TYPE_TEXT_XML);
        assertThat(dsProp.getJaxbContextPath()).isEqualTo("ch.unil.fcrepo4.jaxb.foobar");
        assertThat(dsProp.getPath()).isEqualTo("foobards");
    }

    @Test
    public void testCreatedDate() throws Exception {
        FedoraMappingContext context = new FedoraMappingContext();
        context.setInitialEntitySet(new HashSet<>(Collections.singletonList(Bean4.class)));
        context.initialize();
        GenericFedoraPersistenceEntity<?> entity = context.getPersistentEntity(Bean4.class);
        CreatedPersistentProperty createdProp = (CreatedPersistentProperty) entity.getPersistentProperty("created");
        assertThat(createdProp).isNotNull();
        assertThat(createdProp.isDate()).isTrue();
        assertThat(createdProp.isZonedDateTime()).isFalse();
    }

}
