package ch.unil.fcrepo4.readme;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.FedoraObject;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Path;

/**
 * @author gushakov
 */
@FedoraObject(namespace = "test2")
public class Person {

    @Path(pathCreator = PersonPathCreator.class)
    private long id = 1L;

}
