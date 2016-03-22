package ch.unil.fcrepo4.spring.data.core.convert;

import ch.unil.fcrepo4.spring.data.core.Constants;
import ch.unil.fcrepo4.spring.data.core.convert.rdf.ExtendedXsdDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Created;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.FedoraObject;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Path;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Property;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import org.fcrepo.client.FedoraDatastream;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.impl.FedoraDatastreamImpl;
import org.fcrepo.client.utils.HttpHelper;
import org.fcrepo.kernel.api.RdfLexicon;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalMatchers.and;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
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

    private static final String REPO_URL = "http://localhost:9090/rest";

    private static final long ID = 123L;
    private static final String PATH = "/bean1/" + ID;
    private static final String FO_CREATED = "2015-07-23T08:18:21.327Z";
    private static final int NUMBER = 3;
    private static final String FOO = "bar";

    private RdfDatatypeConverter rdfDatatypeConverter = new ExtendedXsdDatatypeConverter();

    @Mock
    private FedoraRepository mockRepository;

    @Mock
    private HttpHelper mockHelper;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        doReturn(REPO_URL).when(mockRepository).getRepositoryUrl();
    }

    private org.fcrepo.client.FedoraObject makeMockFedoraObject(String path, String created) throws FedoraException {
        org.fcrepo.client.FedoraObject fo = mock(org.fcrepo.client.FedoraObject.class);
        Node uri = NodeFactory.createURI(REPO_URL + path);
        List<Triple> triples = makeResourceProperties(uri, created);
        when(fo.getPath()).thenReturn(path);
        when(fo.getProperties()).thenReturn(triples.iterator());
        return fo;
    }

    private org.fcrepo.client.FedoraObject makeMockFedoraObject(String path, String created, int number, String foo) throws FedoraException {
        org.fcrepo.client.FedoraObject fo = mock(org.fcrepo.client.FedoraObject.class);
        Node uri = NodeFactory.createURI(REPO_URL + path);
        List<Triple> triples = makeResourceProperties(uri, created, number, foo);
        when(fo.getPath()).thenReturn(path);
        when(fo.getProperties()).thenReturn(triples.iterator());
        return fo;
    }

    private List<Triple> makeResourceProperties(Node uri, String created) {
        List<Triple> triples = new ArrayList<>();
        triples.add(new Triple(uri,
                NodeFactory.createURI(RdfLexicon.CREATED_DATE.getURI()),
                NodeFactory.createLiteral(created)));
        return triples;
    }

    private List<Triple> makeResourceProperties(Node uri, String created, int number, String foo) {
        List<Triple> triples = makeResourceProperties(uri, created);
        triples.add(new Triple(uri,
                NodeFactory.createURI(Constants.TEST_FEDORA_URI_NAMESPACE + "number"),
                rdfDatatypeConverter.encodeLiteralValue(number)));
        triples.add(new Triple(uri,
                NodeFactory.createURI(Constants.TEST_FEDORA_URI_NAMESPACE + "foo"),
                rdfDatatypeConverter.encodeLiteralValue(foo)));
        return triples;
    }

    private FedoraDatastream makeMockDatastream(String path, String dsXml, String created) throws FedoraException {
        return makeMockDatastream(path, new ByteArrayInputStream(dsXml.getBytes()), created);
    }

    private FedoraDatastream makeMockDatastream(String path, InputStream dsContent, String created) throws FedoraException {
        FedoraDatastream mockDatastream = mock(FedoraDatastreamImpl.class);
        when(mockDatastream.getPath()).thenReturn(path);
        when(mockDatastream.getContentType()).thenReturn(Constants.MIME_TYPE_TEXT_XML);
        when(mockDatastream.getContent()).thenReturn(dsContent);
        Node uri = NodeFactory.createURI(REPO_URL + path);
        List<Triple> triples = makeResourceProperties(uri, created);
        when(mockDatastream.getProperties()).thenReturn(triples.iterator());
        return mockDatastream;
    }

    @FedoraObject
    public static class Bean1 {

        @Path
        long id;

        @Created
        Date created;

        @Property
        int number;

        @Property
        String foo;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public Date getCreated() {
            return created;
        }

        public void setCreated(Date created) {
            this.created = created;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        public String getFoo() {
            return foo;
        }

        public void setFoo(String foo) {
            this.foo = foo;
        }

        @Override
        public String toString() {
            return "Bean1 {id: " +
                    id +
                    ", created: " +
                    created +
                    ", number: " +
                    number +
                    ", foo: " +
                    foo +
                    "}";
        }
    }

    @Test
    public void testGetFedoraObjectUrl() throws Exception {
        FedoraMappingConverter mappingConverter = new FedoraMappingConverter(mockRepository);
        Bean1 bean1 = new Bean1();
        bean1.id = ID;
        String url = mappingConverter.getFedoraObjectUrl(bean1);
        assertThat(url).isEqualTo(REPO_URL + PATH);
    }

    @Test
    public void testWriteBean1() throws Exception {
        FedoraMappingConverter mappingConverter = new FedoraMappingConverter(mockRepository);
        org.fcrepo.client.FedoraObject fedoraObject = makeMockFedoraObject(PATH, FO_CREATED);
        Bean1 bean1 = new Bean1();
        bean1.id = ID;
        bean1.number = NUMBER;
        bean1.foo = FOO;
        mappingConverter.write(bean1, fedoraObject);
        verify(fedoraObject).updateProperties(and(
                contains("<" + Constants.TEST_FEDORA_URI_NAMESPACE + "number>  " + rdfDatatypeConverter.serializeLiteralValue(bean1.number)),
                contains("<" + Constants.TEST_FEDORA_URI_NAMESPACE + "foo>  " + rdfDatatypeConverter.serializeLiteralValue(bean1.foo))));
    }

    @Test
    public void testReadBean1() throws Exception {
        FedoraMappingConverter mappingConverter = new FedoraMappingConverter(mockRepository);
        org.fcrepo.client.FedoraObject fedoraObject = makeMockFedoraObject(PATH, FO_CREATED, NUMBER, FOO);
        Bean1 bean1 = mappingConverter.read(Bean1.class, fedoraObject);
        assertThat(bean1).isInstanceOf(DynamicBeanProxy.class);
        assertThat(bean1.getId()).isEqualTo(ID);
        assertThat(bean1.getNumber()).isEqualTo(NUMBER);
        assertThat(bean1.getFoo()).isEqualTo(FOO);
        assertThat(bean1.getCreated()).hasTime(ZonedDateTime.parse(FO_CREATED).toInstant().toEpochMilli());
    }

}
