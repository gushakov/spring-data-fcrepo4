package ch.unil.fcrepo4.spring.data.repository;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.FedoraObject;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Path;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Property;

/**
 * @author gushakov
 */
@FedoraObject(namespace = "vehicle")
public class Vehicle {

    @Path
    private long id;

    @Property
    private String make;

    @Property
    private int miles;

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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public int getMiles() {
        return miles;
    }

    public void setMiles(int miles) {
        this.miles = miles;
    }
}