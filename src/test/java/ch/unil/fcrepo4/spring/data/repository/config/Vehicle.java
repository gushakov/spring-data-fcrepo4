package ch.unil.fcrepo4.spring.data.repository.config;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.FedoraObject;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Path;

/**
 * @author gushakov
 */
@FedoraObject
public class Vehicle {

    @Path
    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
