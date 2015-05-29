package ch.unil.fcrepo4.spring.data.core.mapping;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.FedoraObject;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Uuid;
import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author gushakov
 */
public class FedoraMappingContextTest {

    @FedoraObject
    static class Bean1 {

        @Uuid
        String uuid = UUID.randomUUID().toString();

    }

    @Test
    public void testUuidProperty() throws Exception {
        FedoraMappingContext context = new FedoraMappingContext();
        context.setInitialEntitySet(Collections.singleton(Bean1.class));
        context.initialize();
        assertThat(context.getPersistentEntities()).hasSize(1);
        assertThat(context.getPersistentEntity(Bean1.class)).isNotNull();
        assertThat(context.getPersistentEntity(Bean1.class).getIdProperty()).isNotNull();
    }

}
