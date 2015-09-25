package ch.unil.fcrepo4.spring.data.core.convert;

import ch.unil.fcrepo4.assertj.Assertions;
import ch.unil.fcrepo4.beans.Bean1;
import ch.unil.fcrepo4.beans.Bean2;
import ch.unil.fcrepo4.beans.Bean2Datastream1;
import ch.unil.fcrepo4.beans.Bean4;
import ch.unil.fcrepo4.spring.data.core.Constants;
import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.convert.rdf.ExtendedXsdDatatypeConverter;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import org.fcrepo.client.FedoraContent;
import org.fcrepo.client.FedoraDatastream;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.impl.FedoraDatastreamImpl;
import org.fcrepo.client.utils.HttpHelper;
import org.fcrepo.kernel.api.RdfLexicon;
import org.junit.Before;
import org.junit.Test;
import org.mockito.AdditionalMatchers;
import org.mockito.Mock;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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

    private static final String PATH = "/foo/bar/1";
    private static final String FO_UUID = "df0ce28e-2ec7-4c96-8b21-235a98a0da74";
    private static final String FO_CREATED = "2015-07-23T08:18:21.327Z";
    private static final String DS_PATH = "/foo/bar/1/xmlDs";
    private static final String DS_UUID = "b0e99a09-72b7-4c30-ab16-8dcd66ce2004";
    private static final String DS_CREATED = "2015-08-21T11:41:59.364Z";
    private static final String DS_XML = "<foo>bar</foo>";

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

    private org.fcrepo.client.FedoraObject makeMockFedoraObject(String path, String uuid, String created) throws FedoraException {
        org.fcrepo.client.FedoraObject fo = mock(org.fcrepo.client.FedoraObject.class);
        Node uri = NodeFactory.createURI(REPO_URL + path);
        List<Triple> triples = makeResourceProperties(uuid, created, uri);
        when(fo.getPath()).thenReturn(path);
        when(fo.getProperties()).thenReturn(triples.iterator());
        return fo;
    }

    private List<Triple> makeResourceProperties(String uuid, String created, Node uri) {
        List<Triple> triples = new ArrayList<>();
        triples.add(new Triple(uri,
                NodeFactory.createURI(RdfLexicon.HAS_PRIMARY_IDENTIFIER.getURI()),
                NodeFactory.createLiteral(uuid)));
        triples.add(new Triple(uri,
                NodeFactory.createURI(RdfLexicon.CREATED_DATE.getURI()),
                NodeFactory.createLiteral(created)));
        return triples;
    }

    private FedoraDatastream makeMockDatastream(String path, String dsXml, String uuid, String created) throws FedoraException {
        return makeMockDatastream(path, new ByteArrayInputStream(dsXml.getBytes()), uuid, created);
    }

    private FedoraDatastream makeMockDatastream(String path, InputStream dsContent, String uuid, String created) throws FedoraException {
        FedoraDatastream mockDatastream = mock(FedoraDatastreamImpl.class);
        when(mockDatastream.getPath()).thenReturn(path);
        when(mockDatastream.getContentType()).thenReturn(Constants.MIME_TYPE_TEXT_XML);
        when(mockDatastream.getContent()).thenReturn(dsContent);
        Node uri = NodeFactory.createURI(REPO_URL + path);
        List<Triple> triples = makeResourceProperties(uuid, created, uri);
        when(mockDatastream.getProperties()).thenReturn(triples.iterator());
        return mockDatastream;
    }

    @Test
    public void testWriteBean1() throws Exception {
        FedoraMappingConverter mappingConverter = new FedoraMappingConverter(mockRepository);
        org.fcrepo.client.FedoraObject fedoraObject = makeMockFedoraObject(PATH, FO_UUID, FO_CREATED);
        Bean1 bean1 = new Bean1();
        bean1.setPath(PATH);
        bean1.setNumber(3);
        bean1.setFoo("bar");
        mappingConverter.write(bean1, fedoraObject);
        assertThat(bean1.getUuid()).isNotNull();
        assertThat(bean1.getUuid().toString()).isEqualTo(FO_UUID);
        assertThat(bean1.getCreated())
                .isNotNull()
                .hasTime(ZonedDateTime.parse(FO_CREATED).toInstant().toEpochMilli());
        verify(fedoraObject).updateProperties(AdditionalMatchers.and(
                contains("<> <" + Constants.TEST_FEDORA_URI_NAMESPACE + "number> " + rdfDatatypeConverter.serializeLiteralValue(bean1.getNumber())),
                contains("<> <" + Constants.TEST_FEDORA_URI_NAMESPACE + "foo> " + rdfDatatypeConverter.serializeLiteralValue(bean1.getFoo()))));
    }

    @Test
    public void testWriteBean2() throws Exception {
        FedoraMappingConverter mappingConverter = new FedoraMappingConverter(mockRepository);
        org.fcrepo.client.FedoraObject fedoraObject = makeMockFedoraObject(PATH, FO_UUID, FO_CREATED);
        when(mockRepository.createDatastream(eq(DS_PATH), any()))
                .thenAnswer(invocation -> {
                    FedoraContent fedoraContent = (FedoraContent) invocation.getArguments()[1];
                    Assertions.assertThat(fedoraContent.getContent()).hasXmlContentEquivalentTo(DS_XML);
                    return makeMockDatastream(DS_PATH, fedoraContent.getContent(), DS_UUID, DS_CREATED);
                });
        Bean2 bean2 = new Bean2();
        bean2.setPath(PATH);
        Bean2Datastream1 xmlDs = new Bean2Datastream1();
        xmlDs.setXmlStream(new ByteArrayInputStream(DS_XML.getBytes()));
        bean2.setXmlDs(xmlDs);
        mappingConverter.write(bean2, fedoraObject);
        verify(mockRepository, times(1)).createDatastream(eq(DS_PATH), any());
    }

    @Test
    public void testCustomPathConverter() throws Exception {
        FedoraMappingConverter mappingConverter = new FedoraMappingConverter(mockRepository);
        Bean4 bean4 = new Bean4(123456789L);
        String path = mappingConverter.getFedoraObjectPath(bean4);
        assertThat(path).isEqualTo("/custom/123/456/789");
    }

}
