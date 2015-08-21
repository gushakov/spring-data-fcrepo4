# Spring Data Fedora Commons Repository (v. 4.x)

[![Build status](https://travis-ci.org/gushakov/spring-data-fcrepo4.svg?branch=master)](https://travis-ci.org/gushakov/spring-data-fcrepo4)

Spring Data module for persisting and querying annotated POJOs to Fedora Commons repository (v. 4.x).

## Acknowledgements

This project is heavily based on the code from the following projects (including configuration files: Maven, Travis CI, etc.)

 * [fcrepo4-client](https://github.com/fcrepo4-labs/fcrepo4-client)

 * [spring-data-solr](https://github.com/spring-projects/spring-data-solr)

## Synopsis

### Create an instance of `FedoraTemplate` for working with Fedora repository.

```java
// in Spring configuration class, assume Java configuration via @Configuration annotation

@Bean
public FedoraTemplate fedoraTemplate() throws FedoraException {
    return new FedoraTemplate(new FedoraRepositoryImpl("http://localhost:8080/rest"));
}
```

### Create new Fedora object resource in the default namespace with some simple properties.

```java

// bean

@FedoraObject
public class Vehicle {

    @Path
    private String path = "/car/1";

    @Property
    private int numberOfWheels = 4;

}

// converts the bean into a `FedoraObject` and saves it into the repository

fedoraTemplate.save(new Vehicle());
```

The newly created object will be accessible at `http://localhost:8080/rest/test/car/1` and will contain a `numberOfWheels`
property with the default namespace, `info:fedora/test/`.

### Exposing common Fedora resource properties.

The common properties of a Fedora resource (Fedora object, datastream) are exposed, when saving or loading beans to the repository.

```java
public class Bean {

    // expose "uuid" property as instance of java.util.UUID
    @Uuid
    private UUID

    // expose "created" property as instance of java.util.Date or java.time.ZonedDateTime
    @Created
    private Date createdDate;

}
```