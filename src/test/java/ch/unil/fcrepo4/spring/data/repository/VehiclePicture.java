package ch.unil.fcrepo4.spring.data.repository;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Datastream;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.DsContent;

import java.io.InputStream;

/**
 * @author gushakov
 */
@Datastream(mimetype = "image/png")
public class VehiclePicture {

    @DsContent
    private InputStream picture;

    public VehiclePicture() {
    }

    public VehiclePicture(InputStream picture) {
        this.picture = picture;
    }

    public InputStream getPicture() {
        return picture;
    }

    public void setPicture(InputStream picture) {
        this.picture = picture;
    }
}
