package ch.unil.fcrepo4.beans;

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
    private String color;

    @Property
    private int miles;

    @Property
    private float consumption;

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
}
