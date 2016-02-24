## Spring Data Fedora Commons Repository (v. 4.x)

[![Build status](https://travis-ci.org/gushakov/spring-data-fcrepo4.svg?branch=master)](https://travis-ci.org/gushakov/spring-data-fcrepo4)

### Description

This project aims at providing a Spring Data module for Fedora Commons Repository. The module should allow to execute CRUD operations
and query against the Fedora repository using (domain) annotated Java objects.

**List of features availability**

|Feature|Status|Comment|
|---|---|---|
|ID to JCR node path mapping|OK|See [PathCreator](https://github.com/gushakov/spring-data-fcrepo4/blob/master/src/main/java/ch/unil/fcrepo4/spring/data/core/mapping/PathCreator.java).|
|Mapping of resource properties (`created`, etc.)|Partially done|See [ZonedDateTimeRdfDatatype](https://github.com/gushakov/spring-data-fcrepo4/blob/master/src/main/java/ch/unil/fcrepo4/spring/data/core/convert/rdf/ZonedDateTimeRdfDatatype.java).|
|Simple properties mapping|Partially done|Need to test CRUD and queries with `boolean`, `double`, etc.|
|Extensible custom RDF to Java converter|OK|See [ExtendedXsdDatatypeConverter](https://github.com/gushakov/spring-data-fcrepo4/blob/master/src/main/java/ch/unil/fcrepo4/spring/data/core/convert/rdf/ExtendedXsdDatatypeConverter.java).|
|Datastream (binary) object persistence|OK|Needs some work.|
|Lazy-load of datastreams and relations|Partially done|For datastreams, for now.|
|Spring data repository abstraction|Partially done|See [SimpleFedoraRepository](https://github.com/gushakov/spring-data-fcrepo4/blob/master/src/main/java/ch/unil/fcrepo4/spring/data/repository/support/SimpleFedoraRepository.java).|
|Paged queries|Not yet||
|Relationships mapping|Not yet||
|Collection properties mapping|Not yet||

### Acknowledgements

This project is heavily based on the code from the following projects (including configuration files: Maven, Travis CI, etc.)

 * [fcrepo4-client](https://github.com/fcrepo4-labs/fcrepo4-client)

 * [spring-data-solr](https://github.com/spring-projects/spring-data-solr)

### FedoraTemplate

[FedoraTemplate](https://github.com/gushakov/spring-data-fcrepo4/blob/master/src/main/java/ch/unil/fcrepo4/spring/data/core/FedoraTemplate.java)
is the main object responsible for executing CRUD and query operations against Fedora backend.

This is an example of configuring `FedoraTemplate` using Spring Java configuration. It just requires a hostname and a port of Fedora installation.

```java
@Bean
public FedoraTemplate fedoraTemplate() throws FedoraException {
	return new FedoraTemplate(/* config omitted */);
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

Assuming the Fedora instance is running under `http://localhost:8080/fcrepo/rest/vehicle/1`, a set of corresponding resources will be created at the backend with the corresponding RDF properties (shown here as decoded RDF graph for clarity).

```turtle
<http://localhost:8080/fcrepo/rest/vehicle/1>
	<!-- default resource properties are omitted -->
	<info:fedora/test/color>        "Green"^^<http://www.w3.org/2001/XMLSchema#string> ;
	<info:fedora/test/consumption>  "6.5"^^<http://www.w3.org/2001/XMLSchema#float> ;
	<info:fedora/test/make>         "Ford"^^<http://www.w3.org/2001/XMLSchema#string> ;
	<info:fedora/test/miles>        "15000"^^<http://www.w3.org/2001/XMLSchema#int> .
```

### Spring Data Repository

This module implements [Spring Data repository](http://docs.spring.io/spring-data/data-commons/docs/1.11.0.RELEASE/reference/html/#repositories) abstraction,
in order to facilitate queries against the triplestore. This is how to create a Spring Data enabled repository.

```java
// declare the interface

public interface VehicleCrudRepository extends FedoraCrudRepository<Vehicle, Long> {

    List<Vehicle> findByMake(String make);

    List<Vehicle> findByMilesGreaterThan(int miles);

    Page<Vehicle> findByMilesGreaterThan(int miles, Pageable pageable);

    // other useful queries
}

// use EnableFedoraRepositories annotation to scan the package for FedoraRepository interfaces

@Configuration
@PropertySource("classpath:fcrepo4.properties")
@EnableFedoraRepositories
public static class AppConfig {
	@Bean
    public FedoraTemplate fedoraTemplate() throws FedoraException {
    	return new FedoraTemplate(/* config omitted */);
    }
}

// use the the Autowired instance of VehicleCrudRepository like this

List<Vehicle> vehiclesWithLargeMileage = vehicleRepo.findByMilesGreaterThan(15000);
```

The module will then issue a SPARQL query (approximately) as follows

```sparql
SELECT  ?ch_unil_fcrepo4_spring_data_repository_Vehicle
WHERE
  { ?ch_unil_fcrepo4_spring_data_repository_Vehicle <info:fedora/test/miles> ?ch_unil_fcrepo4_spring_data_repository_Vehicle_miles
	FILTER ( ?ch_unil_fcrepo4_spring_data_repository_Vehicle_miles > "1000"^^<http://www.w3.org/2001/XMLSchema#int> )
  }

```

### Datastreams

A relationship between object and a (binary) datastream can be declared as follows

```java
class VehiclePicture {

    @Binary(mimetype="image/jpg")
    private InputStream picture;
}

@FedoraObject
class Vehicle {

    @Datastream
    private VehiclePicture picture;

}
```

A datastream property must be annotated with `Datastream` annotation. The `java.io.InputStream` property providing the actual binary content of the datastream must be annotated with `Binary`
annotation. The datastreams will be persisted as direct children of their parent `FedoraObject` bean, using the name specified with `Datastream`
annotation or the name of the datastream property (by default) as a the path suffix.

All datastreams are lazy-loaded as dynamic proxies generated using [Byte Buddy](http://bytebuddy.net/#/), so datastream content is only requested
only when the client code actually tries to access either content or properties of the datastream.

### Resource properties

Some of the default resource properties automatically generated and updated by the Fedora repository can be accessed by specifying bean properties
with the corresponding annotations. For example, if one wants to access `http://fedora.info/definitions/v4/repository#created`
properties of the persisted resource, then the corresponding attribute of the bean should be annotated as following:

```java
@FedoraObject
class Vehicle {
    @Created
    private ZonedDateTime createdDate;
}
```

The module will automatically perform some useful conversions, i.e. to `ZonedDateTime` from the `created` timestamp if needed.

### Java to RDF type conversion

To serialize values of Java properties as RDF properties uses in SPARQL updates and queries this module uses [TypeMapper](https://jena.apache.org/documentation/notes/typed-literals.html)
provided by `jena-core` module and also used by Fedora repository. See [ExtendedXsdDatatypeConverter](https://github.com/gushakov/spring-data-fcrepo4/blob/master/src/main/java/ch/unil/fcrepo4/spring/data/core/convert/rdf/ExtendedXsdDatatypeConverter.java) for implementation details.