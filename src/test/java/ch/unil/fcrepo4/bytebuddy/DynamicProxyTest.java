package ch.unil.fcrepo4.bytebuddy;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author gushakov
 */
public class DynamicProxyTest {

      @Test
      public void testSubclass() throws Exception {

        A a =  new ByteBuddy()
                  .subclass(A.class)
                  .method(ElementMatchers.isGetter().or(ElementMatchers.isSetter()))
                  .intercept(MethodDelegation.to(new AnyInterceptor()))
                  .make()
                  .load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                  .getLoaded()
                  .newInstance();

//          a.setFoo("bar");

//          System.out.println(a.getFoo());

//          System.out.println(Arrays.toString(a.someOtherMethod()));

          System.out.println(a.getClass().getSuperclass());

      }

}
