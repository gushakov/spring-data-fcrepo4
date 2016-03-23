package ch.unil.fcrepo4.spring.data.repository;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.FedoraObject;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Path;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Property;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Relation;

/**
 * @author gushakov
 */
@FedoraObject(namespace = "owner")
public class Owner {

    @Path
    private long id;

    @Property
    private String fullName;

    @Relation
    private Address address;

    public Owner() {
    }

    public Owner(long id) {
        this.id = id;
    }

    public Owner(long id, String fullName) {
        this.id = id;
        this.fullName = fullName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
