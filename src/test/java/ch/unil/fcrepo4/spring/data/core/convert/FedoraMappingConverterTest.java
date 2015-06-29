package ch.unil.fcrepo4.spring.data.core.convert;

import ch.unil.fcrepo4.spring.data.core.Constants;
import ch.unil.fcrepo4.spring.data.core.mapping.SimplePathCreator;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.FedoraObject;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Path;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Uuid;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.impl.FedoraObjectImpl;
import org.fcrepo.client.utils.HttpHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/*
Based on org.fcrepo.client.impl.FedoraObjectImplTest
 */

/**
 * @author gushakov
 */
public class FedoraMappingConverterTest {

    private static final String PATH_1 = "/foo/bar/1/2/3";
    private static final String PATH_2 = "/foo/bar/1/2/4";

    @FedoraObject
    static class Bean1 {
        @Path
        String path = PATH_1;

        @Uuid
        String uuid;
    }

    @FedoraObject
    static class Bean2 {
        @Path
        String path = PATH_2;

        @Uuid
        UUID uuid;
    }

    @Mock
    private FedoraRepository mockRepository;

    @Mock
    private HttpHelper mockHelper;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        when(mockRepository.getRepositoryUrl()).thenReturn("http://localhost:9090/rest");
        when(mockRepository.findOrCreateObject(anyString()))
                .thenAnswer(invocation ->
                        {
                            String path = (String) invocation.getArguments()[0];
                            Triple uuidNode = null;
                            switch (path) {
                                case "/test" + PATH_1:
                                    uuidNode = new Triple(NodeFactory.createURI("http://localhost:9090/rest/test" + PATH_1),
                                            NodeFactory.createURI("http://fedora.info/definitions/v4/repository#uuid"),
                                            NodeFactory.createLiteral("366cae78-151f-4110-adfc-08f635639f6a"));
                                    break;
                                case "/test" + PATH_2:
                                    uuidNode = new Triple(NodeFactory.createURI("http://localhost:9090/rest/test" + PATH_2),
                                            NodeFactory.createURI("http://fedora.info/definitions/v4/repository#uuid"),
                                            NodeFactory.createLiteral("df0ce28e-2ec7-4c96-8b21-235a98a0da74"));

                            }
                            org.fcrepo.client.FedoraObject fo = spy(new FedoraObjectImpl(mockRepository, mockHelper, (String) invocation.getArguments()[0]));
                            doReturn(Collections.singletonList(uuidNode).iterator())
                                    .when(fo).getProperties();
                            return fo;
                        }

                );

    }

    @Test
    public void testWrite() throws Exception {
        FedoraMappingConverter mappingConverter = new FedoraMappingConverter(mockRepository);
        Bean1 source = new Bean1();
        org.fcrepo.client.FedoraObject fo = mappingConverter.write(source);
        assertThat(fo.getPath()).isEqualTo(new SimplePathCreator().createPath(Constants.DEFAULT_NAMESPACE, source.path));
        System.out.println(fo.getPath());
        assertThat(source.uuid).isEqualTo("366cae78-151f-4110-adfc-08f635639f6a");
        System.out.println(source.uuid);
    }

    @Test
    public void testUUID() throws Exception {
        FedoraMappingConverter mappingConverter = new FedoraMappingConverter(mockRepository);
        Bean2 source = new Bean2();
        mappingConverter.write(source);
        assertThat(source.uuid).isInstanceOf(UUID.class);
        assertThat(source.uuid.toString()).isEqualTo("df0ce28e-2ec7-4c96-8b21-235a98a0da74");
        System.out.println(source.uuid);
    }

}
