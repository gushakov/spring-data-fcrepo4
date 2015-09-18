package ch.unil.fcrepo4.beans;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.FedoraObject;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Path;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Property;

/**
 * @author gushakov
 */
@FedoraObject(namespace = "fruit")
public class Fruit {

    @Path
    private long id;

    @Property
    private double weight;

    public Fruit() {
    }

    public Fruit(long id, double weight) {
        this.id = id;
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
