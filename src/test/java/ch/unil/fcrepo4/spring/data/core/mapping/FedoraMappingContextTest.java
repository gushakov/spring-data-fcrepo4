package ch.unil.fcrepo4.spring.data.core.mapping;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.*;
import org.fcrepo.kernel.api.RdfLexicon;
import org.junit.Test;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentProperty;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author gushakov
 */
public class FedoraMappingContextTest {

    @FedoraObject
    static class Bean1 {

        @Path
        long id = 1L;

    }

    @FedoraObject
    static class Bean2 {

        @Path
        long id = 1L;

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

        @Datastream
        Datastream1 datastream1 = new Datastream1();

    }

    static class Datastream1 {

        @Binary(mimetype = "text/plain")
        InputStream dsContent;
    }

    @FedoraObject(namespace = "")
    static class Bean5 {
        @Path
        long id = 1L;
    }

    @FedoraObject
    static class Bean6 {
        @Path
        long id = 1L;

        @Property(uriNs = "http://another/namespace/uri/")
        String foo = "bar";
    }

    @FedoraObject
    static class Bean7 {
        @Path
        long id = 1L;

    }

    @FedoraObject
    static class Bean8 {
        @Path
        long id = 1L;

        @Relation
        Bean7 bean7 = new Bean7();

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
                assertThat(dsEntity.getPersistentProperty(Binary.class)).isNotNull();
            }
        });
        assertThat(count[0]).isEqualTo(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBlankNamespace() throws Exception {
        FedoraMappingContext context = new FedoraMappingContext();
        context.setInitialEntitySet(new HashSet<>(Collections.singleton(Bean5.class)));
        context.initialize();
    }

    @Test
    public void testDefaultNamespace() throws Exception {
        FedoraMappingContext context = new FedoraMappingContext();
        context.setInitialEntitySet(new HashSet<>(Collections.singleton(Bean1.class)));
        context.initialize();
        FedoraObjectPersistentEntity<?> entity = (FedoraObjectPersistentEntity<?>) context.getPersistentEntity(Bean1.class);
        assertThat(entity.isDefaultNamespace()).isTrue();
        assertThat(entity.getNamespace()).isEqualTo("bean1");
    }

    @Test
    public void testPropertyUriNamespace() throws Exception {
        FedoraMappingContext context = new FedoraMappingContext();
        context.setInitialEntitySet(new HashSet<>(Collections.singleton(Bean6.class)));
        context.initialize();
        FedoraObjectPersistentEntity<?> entity = (FedoraObjectPersistentEntity<?>) context.getPersistentEntity(Bean6.class);
        FedoraResourcePersistentProperty property = (FedoraResourcePersistentProperty) entity.getPersistentProperty("foo");
        assertThat(property.getUri()).isEqualTo("http://another/namespace/uri/foo");
    }

    @Test
    public void testRelation() throws Exception {
        FedoraMappingContext context = new FedoraMappingContext();
        context.setInitialEntitySet(new HashSet<>(Collections.singleton(Bean8.class)));
        context.initialize();
        FedoraPersistentEntity<?> entity = context.getPersistentEntity(Bean8.class);
        final FedoraPersistentProperty bean7 = entity.getPersistentProperty("bean7");
        assertThat(bean7).isInstanceOf(FedoraRelationPersistentProperty.class);
    }

}
