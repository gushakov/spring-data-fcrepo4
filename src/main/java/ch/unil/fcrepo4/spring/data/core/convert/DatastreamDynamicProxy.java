package ch.unil.fcrepo4.spring.data.core.convert;

import java.lang.reflect.Method;

/**
 * @author gushakov
 */
public interface DatastreamDynamicProxy {

    Method GET_TARGET_DATASTREAM_BEAN_METHOD = DatastreamDynamicProxy.class.getDeclaredMethods()[0];

    Object __getTargetDatastreamBean();

}
