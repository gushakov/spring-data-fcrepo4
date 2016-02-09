package ch.unil.fcrepo4.spring.data.repository;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Binary;

import java.io.InputStream;

/**
 * @author gushakov
 */
public class VehicleDescription {

    @Binary(mimetype = "text/xml")
    private InputStream desc;

    public VehicleDescription() {
    }

    public VehicleDescription(InputStream desc) {
        this.desc = desc;
    }

    public InputStream getDesc() {
        return desc;
    }

    public void setDesc(InputStream desc) {
        this.desc = desc;
    }
}
