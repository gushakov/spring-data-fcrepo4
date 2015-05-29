package ch.unil.fcrepo4.spring.data.core.convert;

import ch.unil.fcrepo4.spring.data.core.Constants;
import ch.unil.fcrepo4.spring.data.core.mapping.ConcatenatingPathCreator;
import ch.unil.fcrepo4.spring.data.core.mapping.DefaultPathCreator;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.FedoraObject;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Uuid;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.impl.FedoraObjectImpl;
import org.fcrepo.client.utils.HttpHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/*
Based on org.fcrepo.client.impl.FedoraObjectImplTest
 */

/**
 * @author gushakov
 */
public class FedoraMappingConverterTest {

    @FedoraObject
    static class Bean1 {
        @Uuid
        String uuid = UUID.randomUUID().toString();
    }

    @FedoraObject(namespace = "foobar")
    static class Bean2 {
        @Uuid
        String uuid = UUID.randomUUID().toString();
    }

    @FedoraObject
    static class Bean3 {
        @Uuid(pathCreator = ConcatenatingPathCreator.class)
        String uuid = "foobar";
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
                .thenAnswer(invocation -> new FedoraObjectImpl(mockRepository, mockHelper, (String) invocation.getArguments()[0]));

    }

    @Test
    public void testWriteDefaultPath() throws Exception {
        FedoraMappingConverter mappingConverter = new FedoraMappingConverter(mockRepository);
        Bean1 source = new Bean1();
        org.fcrepo.client.FedoraObject fedoraObject = mappingConverter.write(source);
        assertThat(fedoraObject.getPath()).isEqualTo(new DefaultPathCreator().createPath(Constants.DEFAULT_NAMESPACE, source.uuid));
        System.out.println(fedoraObject.getPath());
    }

    @Test
    public void testWriteCustomNamespace() throws Exception {
        FedoraMappingConverter mappingConverter = new FedoraMappingConverter(mockRepository);
        Bean2 source = new Bean2();
        org.fcrepo.client.FedoraObject fedoraObject = mappingConverter.write(source);
        assertThat(fedoraObject.getPath()).isEqualTo(new DefaultPathCreator().createPath("foobar", source.uuid));
        System.out.println(fedoraObject.getPath());
    }

    @Test
    public void testWriteCustomPathCreator() throws Exception {
        FedoraMappingConverter mappingConverter = new FedoraMappingConverter(mockRepository);
        Bean3 source = new Bean3();
        org.fcrepo.client.FedoraObject fedoraObject = mappingConverter.write(source);
        assertThat(fedoraObject.getPath()).isEqualTo(new ConcatenatingPathCreator().createPath(Constants.DEFAULT_NAMESPACE, source.uuid));
        System.out.println(fedoraObject.getPath());
    }

}
