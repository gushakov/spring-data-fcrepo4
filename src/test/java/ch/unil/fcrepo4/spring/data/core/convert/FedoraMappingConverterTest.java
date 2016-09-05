package ch.unil.fcrepo4.spring.data.core.convert;

import ch.unil.fcrepo4.client.FcrepoConstants;
import ch.unil.fcrepo4.client.FedoraClientRepository;
import ch.unil.fcrepo4.client.FedoraException;
import ch.unil.fcrepo4.spring.data.core.Constants;
import ch.unil.fcrepo4.spring.data.core.convert.rdf.ExtendedXsdDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.*;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalMatchers.and;
import static org.mockito.AdditionalMatchers.or;
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

    private static final String REPO_URL = "http://localhost:8080/fcrepo/rest";

    private static final long BEAN_1_ID = 1L;
    private static final String BEAN_1_PATH = "/bean1/" + BEAN_1_ID;
    private static final String BEAN_1_FO_CREATED = "2015-07-23T08:18:21.327Z";
    private static final int BEAN_1_NUMBER = 3;
    private static final String BEAN_1_FOO = "bar";

    private static final long BEAN_2_ID = 2L;
    private static final String BEAN_2_PATH = "/bean2/" + BEAN_2_ID;
    private static final String BEAN_2_FO_CREATED = "2016-03-23T08:09:20.123Z";

    private static final long BEAN_3_ID = 3L;
    private static final String BEAN_3_PATH = "/bean3/" + BEAN_3_ID;
    private static final String BEAN_3_FO_CREATED = "2016-03-23T08:10:21.123Z";

    private RdfDatatypeConverter rdfDatatypeConverter = new ExtendedXsdDatatypeConverter();

    @Mock
    private FedoraClientRepository mockRepository;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        doReturn(REPO_URL).when(mockRepository).getRepositoryUrl();
    }

    private ch.unil.fcrepo4.client.FedoraObject makeMockFedoraObject(String path, String created) throws FedoraException {
        ch.unil.fcrepo4.client.FedoraObject fo = mock(ch.unil.fcrepo4.client.FedoraObject.class);
        Node uri = NodeFactory.createURI(REPO_URL + path);
        List<Triple> triples = makeResourceProperties(uri, created);
        when(fo.getPath()).thenReturn(path);
        when(fo.getProperties()).thenReturn(triples.iterator());
        return fo;
    }

    private ch.unil.fcrepo4.client.FedoraObject makeMockFedoraObject(String path, String created, int number, String foo) throws FedoraException {
        ch.unil.fcrepo4.client.FedoraObject fo = mock(ch.unil.fcrepo4.client.FedoraObject.class);
        Node uri = NodeFactory.createURI(REPO_URL + path);
        List<Triple> triples = makeResourceProperties(uri, created, number, foo);
        when(fo.getPath()).thenReturn(path);
        when(fo.getProperties()).thenReturn(triples.iterator());
        return fo;
    }

    private List<Triple> makeResourceProperties(Node uri, String created) {
        List<Triple> triples = new ArrayList<>();
        triples.add(new Triple(uri,
                NodeFactory.createURI(FcrepoConstants.CREATED_DATE.getURI()),
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

    @FedoraObject
    public static class Bean1 {

        @Path
        private long id;

        @Created
        private Date created;

        @Property
        private int number;

        @Property
        private String foo;

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

    @FedoraObject
    public static class Bean2 {
        @Path
        private long id;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }
    }

    @FedoraObject
    public static class Bean3 {
        @Path
        private long id;

        @Relation(uriNs = "info:relation/has/")
        private Bean2 bean2;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public Bean2 getBean2() {
            return bean2;
        }

        public void setBean2(Bean2 bean2) {
            this.bean2 = bean2;
        }
    }

    @Test
    public void testGetFedoraObjectUrl() throws Exception {
        FedoraMappingConverter mappingConverter = new FedoraMappingConverter(mockRepository);
        Bean1 bean1 = new Bean1();
        bean1.setId(BEAN_1_ID);
        String url = mappingConverter.getFedoraObjectUrl(bean1);
        assertThat(url).isEqualTo(REPO_URL + BEAN_1_PATH);
    }

    @Test
    public void testWriteBean1() throws Exception {
        FedoraMappingConverter mappingConverter = new FedoraMappingConverter(mockRepository);
        ch.unil.fcrepo4.client.FedoraObject fedoraObject = makeMockFedoraObject(BEAN_1_PATH, BEAN_1_FO_CREATED);
        Bean1 bean1 = new Bean1();
        bean1.setId(BEAN_1_ID);
        bean1.setNumber(BEAN_1_NUMBER);
        bean1.setFoo(BEAN_1_FOO);
        mappingConverter.write(bean1, fedoraObject);
        verify(fedoraObject).updateProperties(
                or(contains("DELETE"),
                        and(contains("<" + Constants.TEST_FEDORA_URI_NAMESPACE + "number>  " + rdfDatatypeConverter.serializeLiteralValue(bean1.number)),
                                contains("<" + Constants.TEST_FEDORA_URI_NAMESPACE + "foo>  " + rdfDatatypeConverter.serializeLiteralValue(bean1.foo)))));
    }

    @Test
    public void testReadBean1() throws Exception {
        FedoraMappingConverter mappingConverter = new FedoraMappingConverter(mockRepository);
        ch.unil.fcrepo4.client.FedoraObject fedoraObject = makeMockFedoraObject(BEAN_1_PATH, BEAN_1_FO_CREATED, BEAN_1_NUMBER, BEAN_1_FOO);
        Bean1 bean1 = mappingConverter.read(Bean1.class, fedoraObject);
        assertThat(bean1).isInstanceOf(DynamicBeanProxy.class);
        assertThat(bean1.getId()).isEqualTo(BEAN_1_ID);
        assertThat(bean1.getNumber()).isEqualTo(BEAN_1_NUMBER);
        assertThat(bean1.getFoo()).isEqualTo(BEAN_1_FOO);
        assertThat(bean1.getCreated()).hasTime(ZonedDateTime.parse(BEAN_1_FO_CREATED).toInstant().toEpochMilli());
    }

    @Test
    public void testWriteBean3() throws Exception {
        // create a mock Fedora object for /bean2/2 when requested
        doReturn(false).when(mockRepository).exists(BEAN_2_PATH);
        doReturn(makeMockFedoraObject(BEAN_2_PATH, BEAN_2_FO_CREATED)).when(mockRepository).createObject(BEAN_2_PATH);

        FedoraMappingConverter mappingConverter = new FedoraMappingConverter(mockRepository);
        ch.unil.fcrepo4.client.FedoraObject fedoraObject = makeMockFedoraObject(BEAN_3_PATH, BEAN_3_FO_CREATED);
        Bean2 bean2 = new Bean2();
        bean2.setId(BEAN_2_ID);
        Bean3 bean3 = new Bean3();
        bean3.setId(BEAN_3_ID);
        bean3.setBean2(bean2);
        mappingConverter.write(bean3, fedoraObject);
        verify(fedoraObject).updateProperties(contains("<info:relation/has/bean2>  <" + REPO_URL + BEAN_2_PATH + ">"));
    }

}
