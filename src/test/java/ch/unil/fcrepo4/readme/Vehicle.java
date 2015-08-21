package ch.unil.fcrepo4.readme;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Created;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.FedoraObject;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Path;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Uuid;

import java.util.Date;
import java.util.UUID;

/**
 * @author gushakov
 */
@FedoraObject
public class Vehicle {

    @Path
    private String path = "/car/1";

    @Uuid
    private UUID uuid;

    @Created
    private Date createdDate;

    private CarPhoto photo = new CarPhoto("test.png");
}
