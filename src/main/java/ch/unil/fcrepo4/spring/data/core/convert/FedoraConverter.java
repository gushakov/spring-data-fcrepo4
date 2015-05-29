package ch.unil.fcrepo4.spring.data.core.convert;

import org.fcrepo.client.FedoraObject;
import org.springframework.data.convert.EntityReader;
import org.springframework.data.convert.EntityWriter;

/**
 * @author gushakov
 */
public interface FedoraConverter extends EntityReader<Object, FedoraObject>, EntityWriter<Object, FedoraObject> {

    FedoraObject write(Object source);

}
