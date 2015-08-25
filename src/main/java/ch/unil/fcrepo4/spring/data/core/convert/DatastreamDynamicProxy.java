package ch.unil.fcrepo4.spring.data.core.convert;

/**
 * @author gushakov
 */
public interface DatastreamDynamicProxy {

    String GET_TARGET_BEAN_METHOD_NAME = DatastreamDynamicProxy.class.getMethods()[0].getName();

    Object __getTargetDatastreamBean();

}
