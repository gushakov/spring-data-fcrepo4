package ch.unil.fcrepo4.spring.data.core.convert;

/**
 * @author gushakov
 */
public interface DelegatingDynamicProxy<T> {
    static final String DELEGATE_GETTER_NAME = "getDelegate";
    T getDelegate();
}
