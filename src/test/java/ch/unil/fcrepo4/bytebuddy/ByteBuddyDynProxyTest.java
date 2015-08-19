package ch.unil.fcrepo4.bytebuddy;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.Test;

import javax.xml.bind.JAXBElement;

import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * @author gushakov
 */
public class ByteBuddyDynProxyTest {

    @Test
    public void testSimpleProxy() throws Exception {

        Class<?> dynType = new ByteBuddy()
                .subclass(Object.class)
                .method(named("toString")).intercept(FixedValue.value("Hello World"))
                .make()
                .load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();

        System.out.println(dynType.newInstance());

    }

}
