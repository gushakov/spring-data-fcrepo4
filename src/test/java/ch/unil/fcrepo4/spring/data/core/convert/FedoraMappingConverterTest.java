package ch.unil.fcrepo4.spring.data.core.convert;

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
import org.fcrepo.client.utils.HttpHelper;
import org.fcrepo.kernel.RdfLexicon;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static ch.unil.fcrepo4.hamcrest.XmlFedoraContentMatcher.equalsFedoraContentWithXml;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
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
    private static final String FO_PATH = "/test" + PATH;
    private static final String FO_UUID = "df0ce28e-2ec7-4c96-8b21-235a98a0da74";
    private static final String FO_CREATED = "2015-07-23T08:18:21.327Z";
    private static final String DS_PATH = FO_PATH + "/foobards";
    private static final String DS_UUID = "b0e99a09-72b7-4c30-ab16-8dcd66ce2004";
    private static final String DS_CREATED = "2015-08-21T11:41:59.364Z";
    private static final String DS_XML = "<foo>bar</foo>";

    @FedoraObject
    static class Bean1 {
        @Path
        String path = PATH;

        @Uuid
        UUID uuid;

        @Created
        Date created;

        @Property
        int number = 1;

        @Property
        Integer anotherNumber = 2;

        @Property
        String baz = "baz";
    }

    @FedoraObject
    static class Bean2 {

        @Path
        String path = PATH;

        Bean2Datastream foobards = new Bean2Datastream();
    }

    @Datastream
    static class Bean2Datastream {

        @Uuid
        UUID uuid;

        @DsContent
        InputStream dsContent = new ByteArrayInputStream(DS_XML.getBytes());

    }

    @Mock
    private FedoraRepository mockRepository;

    @Mock
    private HttpHelper mockHelper;

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        // setup mock repository, fedora objects (with preset properties) and datastreams

        doReturn(REPO_URL).when(mockRepository).getRepositoryUrl();

        org.fcrepo.client.FedoraObject mockFo = makeMockFedoraObject(FO_PATH, FO_UUID, FO_CREATED);
        doReturn(mockFo)
                .when(mockRepository).findOrCreateObject(FO_PATH);
        doReturn(mockFo)
                .when(mockRepository).getObject(FO_PATH);
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
    public void testWrite() throws Exception {

        FedoraMappingConverter mappingConverter = new FedoraMappingConverter(mockRepository);
        Bean1 source = new Bean1();
        org.fcrepo.client.FedoraObject fo = mappingConverter.write(source);
        assertThat(source.uuid).isNotNull();
        assertThat(source.uuid.toString()).isEqualTo(FO_UUID);
        System.out.println(source.uuid);
        assertThat(source.created)
                .isNotNull()
                .hasTime(ZonedDateTime.parse(FO_CREATED).toInstant().toEpochMilli());
        System.out.println(source.created);
        verify(fo).updateProperties(contains("<> <" +
                Constants.TEST_FEDORA_URI_NAMESPACE +
                "number> " +
                Utils.encodeLiteralValue(source.number, int.class)));
    }

    @Test
    public void testWriteDatastream() throws Exception {
        when(mockRepository.createDatastream(eq(DS_PATH), any()))
                .thenAnswer(invocation -> makeMockDatastream(DS_PATH, ((FedoraContent) invocation.getArguments()[1]).getContent(), DS_UUID, DS_CREATED));
        FedoraMappingConverter mappingConverter = new FedoraMappingConverter(mockRepository);
        Bean2 bean2 = new Bean2();
        mappingConverter.write(bean2);
        verify(mockRepository, times(1)).createDatastream(eq(DS_PATH),
                argThat(equalsFedoraContentWithXml(DS_XML)));
        assertThat(bean2.foobards.uuid).isNotNull();
        assertThat(bean2.foobards.uuid.toString()).isEqualTo(DS_UUID);
    }


}
