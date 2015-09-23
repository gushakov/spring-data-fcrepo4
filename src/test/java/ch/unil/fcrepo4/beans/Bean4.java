package ch.unil.fcrepo4.beans;

import ch.unil.fcrepo4.spring.data.core.convert.CustomPathCreator;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.FedoraObject;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Path;

/**
 * @author gushakov
 */
@FedoraObject(namespace = "custom")
public class Bean4 {

    @Path(pathCreator = CustomPathCreator.class)
    private long id;

    public Bean4(long id) {
        this.id = id;
    }

}
