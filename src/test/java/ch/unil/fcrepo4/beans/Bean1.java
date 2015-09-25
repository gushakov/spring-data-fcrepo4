package ch.unil.fcrepo4.beans;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.*;

import java.util.Date;
import java.util.UUID;

/**
 * @author gushakov
 */
@FedoraObject
public class Bean1 {
    @Path
    private String path;

    @Uuid
    private UUID uuid;

    @Created
    private Date created;

    @Property
    private int number;

    @Property
    private String foo;

    public Bean1() {
    }

    public Bean1(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getFoo() {
        return foo;
    }

    public void setFoo(String foo) {
        this.foo = foo;
    }

}
