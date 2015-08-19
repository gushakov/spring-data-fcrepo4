package ch.unil.fcrepo4.spring.data.core.convert;

import org.fcrepo.client.FedoraDatastream;

/**
 * @author gushakov
 */
public interface DatastreamDynamicProxy extends FedoraDatastream {
    String DELEGATE_METHOD_NAME = DatastreamDynamicProxy.class.getMethods()[0].getName();
    FedoraDatastream getDelegate();
}
