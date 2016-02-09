package ch.unil.fcrepo4.spring.data.repository;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Binary;

import java.io.InputStream;

/**
 * @author gushakov
 */
public class VehiclePicture {

    @Binary(mimetype = "image/png")
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
