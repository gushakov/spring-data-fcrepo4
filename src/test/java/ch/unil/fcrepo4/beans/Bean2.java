package ch.unil.fcrepo4.beans;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.FedoraObject;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Path;

/**
 * @author gushakov
 */
@FedoraObject
public class Bean2 {

    @Path
    private String path;

    private Bean2Datastream1 xmlDs;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Bean2Datastream1 getXmlDs() {
        return xmlDs;
    }

    public void setXmlDs(Bean2Datastream1 xmlDs) {
        this.xmlDs = xmlDs;
    }
}
