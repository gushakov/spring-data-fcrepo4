package ch.unil.fcrepo4.beans;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.FedoraObject;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Path;

/**
 * @author gushakov
 */
@FedoraObject(namespace = "foo:bar")
public class IllegalPathBean {

    @Path
    private long id = 1L;

}
