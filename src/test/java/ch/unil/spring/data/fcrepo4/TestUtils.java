package ch.unil.spring.data.fcrepo4;

import org.fcrepo.client.FedoraContent;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.UUID;

// Based on org.fcrepo.client.impl.FedoraRepositoryImplIT in fcrepo4-client

/**
 * @author gushakov
 */
public class TestUtils {

    public static String getRandomUuid(){
        return UUID.randomUUID().toString();
    }

    public static FedoraContent getStringContent(String text){
        return new FedoraContent().setContentType("text/plain").setContent(new ByteArrayInputStream(text.getBytes(Charset.forName("UTF-8"))));
    }
}
