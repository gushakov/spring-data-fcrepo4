package ch.unil.fcrepo4.bytebuddy;

/**
 * @author gushakov
 */
public class A {

    private String foo;

    private String[] list = {"toto"};

    public String getFoo() {
        return foo;
    }

    public void setFoo(String foo) {
        this.foo = foo;
    }

    public String[] someOtherMethod(){
        return list;
    }

}
