package ch.unil.fcrepo4.spring.data.core.mapping;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.FedoraObject;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Path;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Uuid;
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
        assertThat(uuidProp1.isUUID).isFalse();
        GenericFedoraPersistenceEntity<?> entity2 = context.getPersistentEntity(Bean2.class);
        UuidPersistentProperty uuidProp2 = (UuidPersistentProperty) entity2.getPersistentProperty(Uuid.class);
        assertThat(uuidProp2).isNotNull();
        assertThat(uuidProp2.isUUID).isTrue();
        System.out.println("|||||||||||||TESTING TRAVIS||||||||||||||||||||");
    }

}
