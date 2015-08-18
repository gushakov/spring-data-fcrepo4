package ch.unil.fcrepo4.spring.data.core.convert;

import ch.unil.fcrepo4.jaxb.foobar.FoobarType;
import ch.unil.fcrepo4.jaxb.foobar.ObjectFactory;
import ch.unil.fcrepo4.spring.data.core.Constants;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.*;
import ch.unil.fcrepo4.utils.Utils;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import org.fcrepo.client.FedoraContent;
import org.fcrepo.client.FedoraDatastream;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.impl.FedoraDatastreamImpl;
import org.fcrepo.client.impl.FedoraObjectImpl;
import org.fcrepo.client.utils.HttpHelper;
import org.fcrepo.kernel.RdfLexicon;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.xmlmatchers.transform.StringSource;

import javax.xml.bind.JAXBElement;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.xmlmatchers.XmlMatchers.isEquivalentTo;
import static org.xmlmatchers.transform.XmlConverters.the;


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

        @Property
        int number = 1;

        @Property
        Integer anotherNumber = 2;

        @Property
        String baz = "baz";
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
                                makeMockFedoraObject((String) invocation.getArguments()[0])

                );
    }

    private org.fcrepo.client.FedoraObject makeMockFedoraObject(String path) throws FedoraException {
        List<Triple> triples = new ArrayList<Triple>();
        switch (path) {
            case "/test" + PATH_1:
                Node uri = NodeFactory.createURI("http://localhost:9090/rest/test" + PATH_1);
                triples.add(new Triple(uri,
                        NodeFactory.createURI(RdfLexicon.HAS_PRIMARY_IDENTIFIER.getURI()),
                        NodeFactory.createLiteral("df0ce28e-2ec7-4c96-8b21-235a98a0da74")));
                triples.add(new Triple(uri,
                        NodeFactory.createURI(RdfLexicon.CREATED_DATE.getURI()),
                        NodeFactory.createLiteral("2015-07-23T08:18:21.327Z")));
                break;
        }
        org.fcrepo.client.FedoraObject fo = spy(new FedoraObjectImpl(mockRepository, mockHelper, path));
        doReturn(triples.iterator())
                .when(fo).getProperties();
        doNothing().when(fo).updateProperties(anyString());
        return fo;
    }

    @Test
    public void testWriteUuid() throws Exception {
        FedoraMappingConverter mappingConverter = new FedoraMappingConverter(mockRepository);
        Bean1 source = new Bean1();
        mappingConverter.write(source);
        assertThat(source.uuid).isNotNull();
        assertThat(source.uuid.toString()).isEqualTo("df0ce28e-2ec7-4c96-8b21-235a98a0da74");
        System.out.println(source.uuid);
    }

    @Test
    public void testWriteCreated() throws Exception {
        FedoraMappingConverter mappingConverter = new FedoraMappingConverter(mockRepository);
        Bean1 source = new Bean1();
        mappingConverter.write(source);
        assertThat(source.created)
                .isNotNull()
                .hasTime(ZonedDateTime.parse("2015-07-23T08:18:21.327Z").toInstant().toEpochMilli());
    }

    @Test
    public void testWritePropertyInt() throws Exception {
        FedoraMappingConverter mappingConverter = new FedoraMappingConverter(mockRepository);
        Bean1 source = new Bean1();
        org.fcrepo.client.FedoraObject fo = mappingConverter.write(source);
        verify(fo).updateProperties(contains("<> <" +
                Constants.TEST_FEDORA_URI_NAMESPACE +
                "number> " +
                Utils.encodeLiteralValue(source.number, int.class)));
    }

    @Test
    public void testWriteDatastreams() throws Exception {
        FedoraMappingConverter mappingConverter = new FedoraMappingConverter(mockRepository);
        Bean1 source = new Bean1();
        ObjectFactory jaxbFactory = new ObjectFactory();
        FoobarType foobarType = jaxbFactory.createFoobarType();
        foobarType.setFoo("foo");
        foobarType.setBar(1);
        source.foobarDs = jaxbFactory.createFoobar(foobarType);

        doAnswer(invocation -> {
            assertThat(isEquivalentTo(
                    the("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><foobar xmlns=\"http://foobar\"><foo>foo</foo><bar>1</bar></foobar>"))
                    .matches(the(new StreamSource(((FedoraContent) invocation.getArguments()[1]).getContent())))).isTrue();
            return null;
        }).when(mockRepository).createDatastream(anyString(), any());

        mappingConverter.write(source);
    }


    @Test
    public void testRead() throws Exception {

        when(mockRepository.exists(eq("/test" + PATH_1 + "/foobards"))).thenReturn(true);
        doAnswer(invocation -> {
            FedoraDatastream mockDatastream = mock(FedoraDatastreamImpl.class);
            when(mockDatastream.getContentType()).thenReturn(Constants.MIME_TYPE_TEXT_XML);
            when(mockDatastream.getContent())
                    .thenReturn(new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><foobar xmlns=\"http://foobar\"><foo>foo</foo><bar>1</bar></foobar>".getBytes()));
            return mockDatastream;
        }).when(mockRepository).getDatastream(eq("/test" + PATH_1 + "/foobards"));

        FedoraMappingConverter mappingConverter = new FedoraMappingConverter(mockRepository);
        org.fcrepo.client.FedoraObject mockFedoraObject = makeMockFedoraObject("/test" + PATH_1);
        Bean1 bean = mappingConverter.read(Bean1.class, mockFedoraObject);
        assertThat(bean.created).hasTime(ZonedDateTime.parse("2015-07-23T08:18:21.327Z").toInstant().toEpochMilli());
        assertThat(bean.uuid.toString()).isEqualTo("df0ce28e-2ec7-4c96-8b21-235a98a0da74");
        assertThat(bean.foobarDs).isInstanceOf(JAXBElement.class);
        assertThat(bean.foobarDs.getValue().getFoo()).isEqualTo("foo");
        assertThat(bean.foobarDs.getValue().getBar()).isEqualTo(1);
    }

}
