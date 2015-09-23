## Spring Data Fedora Commons Repository (v. 4.x)

[![Build status](https://travis-ci.org/gushakov/spring-data-fcrepo4.svg?branch=master)](https://travis-ci.org/gushakov/spring-data-fcrepo4)

Spring Data module for Fedora Commons Repository (version 4.x or later) allowing for CRUD operations and query using annotated POJO beans.

*This is still work in progress.*

### Acknowledgements

This project is heavily based on the code from the following projects (including configuration files: Maven, Travis CI, etc.)

 * [fcrepo4-client](https://github.com/fcrepo4-labs/fcrepo4-client)

 * [spring-data-solr](https://github.com/spring-projects/spring-data-solr)

### FedoraTemplate

[FedoraTemplate](https://github.com/gushakov/spring-data-fcrepo4/blob/master/src/main/java/ch/unil/fcrepo4/spring/data/core/FedoraTemplate.java)
is the main object responsible for executing CRUD and query operations against Fedora backend.

This is an example of configuring `FedoraTemplate` using Spring Java configuration. It uses two URLs one for the Fedora repository itself,
and one for [external triplestore](https://wiki.duraspace.org/display/FEDORA40/External+Search) indexed by the repository.

```java
@Autowired
private Environment env;

@Bean
public FedoraTemplate fedoraTemplate() throws FedoraException {
	return new FedoraTemplate(env.getProperty("fedora.repository.url"),
	    env.getProperty("triplestore.sparql.query.url"));
}
```

Once the template is initialized one can use it to store and retrieve a bean from the Fedora repository. For example, if a bean is declared
as following

```java
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

	// constructors/getters/setters are omitted
}
```

This is how using `FedoraTemplate` for basic CRUD operations may look like.

```java
Vehicle vehicle = new Vehicle(1L, "Ford", "Green", 15000, 6.5f);
fedoraTemplate.save(vehicle);

Vehicle anotherVehicle = fedoraTemplate.load(1L, Vehicle.class);
```

Assuming the Fedora instance is running under `http://localhost:9090/rest`, a set of corresponding [FeodoraResources](https://github.com/fcrepo4-labs/fcrepo4-client/blob/master/fcrepo-client/src/main/java/org/fcrepo/client/FedoraResource.java)
will be created at the backend with the corresponding JCR properties (shown here as decoded RDF graph for clarity).

```xml
<http://localhost:9090/rest/vehicle/1>
	<!-- system properties are omitted -->
	<info:fedora/test/color>        "Green"^^<http://www.w3.org/2001/XMLSchema#string> ;
	<info:fedora/test/consumption>  "6.5"^^<http://www.w3.org/2001/XMLSchema#float> ;
	<info:fedora/test/make>         "Ford"^^<http://www.w3.org/2001/XMLSchema#string> ;
	<info:fedora/test/miles>        "15000"^^<http://www.w3.org/2001/XMLSchema#int> .
```

## Spring Data Repository

This module implements [Spring Data repository](http://docs.spring.io/spring-data/data-commons/docs/1.11.0.RELEASE/reference/html/#repositories) abstraction,
in order to facilitate queries against the triplestore.

This is how to bootstrap a Spring Data enabled Fedora repository.

```java
public interface VehicleCrudRepository extends FedoraCrudRepository<Vehicle, Long> {

    List<Vehicle> findByMake(String make);

    List<Vehicle> findByMilesGreaterThan(int miles);

    Page<Vehicle> findByMilesGreaterThan(int miles, Pageable pageable);

    // other useful queries
}
```

Declare repository interface to extend [FedoraCrudRepository](https://github.com/gushakov/spring-data-fcrepo4/blob/master/src/main/java/ch/unil/fcrepo4/spring/data/repository/FedoraCrudRepository.java)
interface.

Then bootstrap the repository using `EnableFedoraRepositories` annotation on the application context configuration.

```java
@Configuration
@PropertySource("classpath:fcrepo4.properties")
@EnableFedoraRepositories
public static class AppConfig {
	// Fedora template bean declaration is omitted
}

// use the the Autowired instance of VehicleCrudRepository like this

List<Vehicle> vehiclesWithLargeMileage = vehicleRepo.findByMilesGreaterThan(15000);
```

The module automatically converts the candidate query methods into SPARQL queries run against the triplestore. For example,
the call to the query method `findByMilesGreaterThan` will be translated into

```
SELECT  ?v1
WHERE
  { ?v1  <info:fedora/test/miles>  ?v2 .
    FILTER ( ?v2 > "15000"^^<http://www.w3.org/2001/XMLSchema#int> )
  }
```

## Datastreams

A relationship between object and a (binary) datastream can be declared as follows

```java
@Datastream(mimetype="image/png")
class VehiclePicture {

    @DsContent
    private InputStream picture;
}

@FedoraObject
class Vehicle {

    // PNG datastream
    private VehiclePicture picture;

}
```

A datastream type must be annotated with `Datastream` annotation optionally specifying the name and the mimetype of the
datastream. The `InputStream` property providing the actual content of the datastream must be annotated with `DsContent`
annotation. The datastreams will be persisted as direct children of their parent `FedoraObject` bean, using the name specified with `Datastream`
annotation or the name of the datastream property (by default) as a the path suffix.

All datastreams are lazy-loaded as dynamic proxies generated using [Byte Buddy](http://bytebuddy.net/#/), so datastream content is only requested
only when the client code actually tries to access either content or properties of the datastream.

## Resource properties

Some of the default resource properties automatically generated and updated by the Fedora repository can be accessed by specifying bean properties
with the corresponding annotations. For example, if one wants to access `http://fedora.info/definitions/v4/repository#uuid`
or `http://fedora.info/definitions/v4/repository#created` properties of the persisted resource

```java
class Vehicle {
    @Uuid
    private UUID uuid;

    @Created
    private ZonedDateTime createdDate;
}
```

The module will automatically perform some useful conversions, i.e. to `UUID` from the `uuid` property or to `ZonedDateTime`
from the `created` timestamp if needed.