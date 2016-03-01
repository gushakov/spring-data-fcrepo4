package ch.unil.fcrepo4.bytebuddy;

import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * @author gushakov
 */
public class AnyInterceptor {

    private Object delegate;

    @RuntimeType
    public Object intercept(
            @AllArguments Object[] allArguments,
            @Origin Method method) {

        System.out.println("Intercepted: " + method + ", by " + this);
        if (delegate == null){
            delegate = new A();
            System.out.println("Created new A: " + delegate);
        }



        try {
          return   method.invoke(delegate, allArguments);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
