package ch.unil.fcrepo4.spring.data.repository;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.*;

import java.time.ZonedDateTime;
import java.util.Date;

/**
 * Simple bean to be persisted in the Fedora repository. {@code FedoraObject} annotation may specify a namespace, a JCR
 * path prefix where all instances of this bean will be stored.
 */
@FedoraObject(namespace = "vehicle")
public class Vehicle {

    // mandatory ID property will be converted to the JCR path suffix
    @Path
    private long id;

    // default resource property
    @Created
    private Date created;

    // simple properties to be stored as attributes of JCR node
    @Property
    private String make;

    @Property
    private String color;

    @Property
    private int miles;

    @Property
    private float consumption;

    // binary datastreams

    @Datastream
    private VehicleDescription description;

    @Datastream
    private VehiclePicture picture;

    // resource (read-only) properties

    @Created
    private ZonedDateTime createdDate;

    @Relation
    private Owner owner;

    public Vehicle() {
    }

    public Vehicle(long id) {
        this.id = id;
    }

    public Vehicle(long id, String make) {
        this.id = id;
        this.make = make;
    }

    public Vehicle(long id, String make, int miles) {
        this.id = id;
        this.make = make;
        this.miles = miles;
    }

    public Vehicle(long id, String make, String color, int miles, float consumption) {
        this.id = id;
        this.make = make;
        this.color = color;
        this.miles = miles;
        this.consumption = consumption;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getCreated() {
        return created;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getMiles() {
        return miles;
    }

    public void setMiles(int miles) {
        this.miles = miles;
    }

    public float getConsumption() {
        return consumption;
    }

    public void setConsumption(float consumption) {
        this.consumption = consumption;
    }

    public VehicleDescription getDescription() {
        return description;
    }

    public void setDescription(VehicleDescription description) {
        this.description = description;
    }

    public VehiclePicture getPicture() {
        return picture;
    }

    public void setPicture(VehiclePicture picture) {
        this.picture = picture;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(ZonedDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }
}
