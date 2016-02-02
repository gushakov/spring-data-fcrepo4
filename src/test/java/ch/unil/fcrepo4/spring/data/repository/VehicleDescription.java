package ch.unil.fcrepo4.spring.data.repository;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Datastream;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.DsContent;

import java.io.InputStream;

/**
 * @author gushakov
 */
@Datastream(mimetype = "text/xml")
public class VehicleDescription {

    @DsContent
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
