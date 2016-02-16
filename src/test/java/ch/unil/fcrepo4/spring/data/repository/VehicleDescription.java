package ch.unil.fcrepo4.spring.data.repository;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Binary;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Property;

import java.io.InputStream;

/**
 * @author gushakov
 */
public class VehicleDescription {

    @Binary(mimetype = "text/xml")
    private InputStream desc;

    @Property
    private String type;

    public VehicleDescription() {
    }

    public VehicleDescription(InputStream desc) {
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public InputStream getDesc() {
        return desc;
    }

    public void setDesc(InputStream desc) {
        this.desc = desc;
    }
}
