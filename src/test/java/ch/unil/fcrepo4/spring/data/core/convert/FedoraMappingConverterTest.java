package ch.unil.fcrepo4.spring.data.core.convert;

import ch.unil.fcrepo4.jaxb.foobar.FoobarType;
import ch.unil.fcrepo4.jaxb.foobar.ObjectFactory;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.*;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import org.apache.commons.io.IOUtils;
import org.fcrepo.client.FedoraContent;
import org.fcrepo.client.FedoraDatastream;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.impl.FedoraDatastreamImpl;
import org.fcrepo.client.impl.FedoraObjectImpl;
import org.fcrepo.client.utils.HttpHelper;
import org.fcrepo.kernel.RdfLexicon;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.xml.bind.JAXBElement;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/*
Based on org.fcrepo.client.impl.FedoraObjectImplTest
 */

/**
 * @author gushakov
 */
public class FedoraMappingConverterTest {

    private static final String PATH_1 = "/foo/bar/1";

    @FedoraObject
    static class Bean1 {
        @Path
        String path = PATH_1;

        @Uuid
        UUID uuid;

        @Created
        Date created;

        @Datastream(jaxbContextPath = "ch.unil.fcrepo4.jaxb.foobar")
        JAXBElement<FoobarType> foobarDs;
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
                            List<Triple> triples = new ArrayList<Triple>();
                            switch (path) {
                                case "/test" + PATH_1:
                                    triples.add(new Triple(NodeFactory.createURI("http://localhost:9090/rest/test" + PATH_1),
                                            NodeFactory.createURI(RdfLexicon.HAS_PRIMARY_IDENTIFIER.getURI()),
                                            NodeFactory.createLiteral("df0ce28e-2ec7-4c96-8b21-235a98a0da74")));
                                    triples.add(new Triple(NodeFactory.createURI("http://localhost:9090/rest/test" + PATH_1),
                                            NodeFactory.createURI(RdfLexicon.CREATED_DATE.getURI()),
                                            NodeFactory.createLiteral("2015-07-23T08:18:21.327Z")));
                                    break;
                            }
                            org.fcrepo.client.FedoraObject fo = spy(new FedoraObjectImpl(mockRepository, mockHelper, (String) invocation.getArguments()[0]));
                            doReturn(triples.iterator())
                                    .when(fo).getProperties();
                            return fo;
                        }

                );
    }

    @Test
    public void testUuid() throws Exception {
        FedoraMappingConverter mappingConverter = new FedoraMappingConverter(mockRepository);
        Bean1 source = new Bean1();
        mappingConverter.write(source);
        assertThat(source.uuid).isNotNull();
        assertThat(source.uuid.toString()).isEqualTo("df0ce28e-2ec7-4c96-8b21-235a98a0da74");
        System.out.println(source.uuid);
    }

    @Test
    public void testCreated() throws Exception {
        FedoraMappingConverter mappingConverter = new FedoraMappingConverter(mockRepository);
        Bean1 source = new Bean1();
        mappingConverter.write(source);
        assertThat(source.created)
                .isNotNull()
                .hasTime(ZonedDateTime.parse("2015-07-23T08:18:21.327Z").toInstant().toEpochMilli());
    }

    @Test
    public void testDatastreams() throws Exception {
        FedoraMappingConverter mappingConverter = new FedoraMappingConverter(mockRepository);
        Bean1 source = new Bean1();
        ObjectFactory jaxbFactory = new ObjectFactory();
        FoobarType foobarType = jaxbFactory.createFoobarType();
        foobarType.setFoo("foo");
        foobarType.setBar(1);
        source.foobarDs = jaxbFactory.createFoobar(foobarType);

        when(mockRepository.findOrCreateDatastream(anyString()))
                .thenAnswer(invocation -> {
                    FedoraDatastream ds = spy(new FedoraDatastreamImpl(mockRepository, mockHelper, (String) invocation.getArguments()[0]));
                    doAnswer(answer -> {
                        FedoraContent content = (FedoraContent) answer.getArguments()[0];
                        StringWriter writer = new StringWriter();
                        IOUtils.copy(content.getContent(), writer);
                        assertThat(writer.toString()).isXmlEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><foobar xmlns=\"http://foobar\"><foo>foo</foo><bar>1</bar></foobar>");
                        return null;
                    }).when(ds).updateContent(any());
                    return ds;
                });


        org.fcrepo.client.FedoraObject fo = mappingConverter.write(source);
        verify(mockRepository).createDatastream(eq("/test" + PATH_1 + "/foobards"), any());
    }

}
