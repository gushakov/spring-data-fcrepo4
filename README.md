## Spring Data Fedora Commons Repository (v. 4.x)

[![Build status](https://travis-ci.org/gushakov/spring-data-fcrepo4.svg?branch=master)](https://travis-ci.org/gushakov/spring-data-fcrepo4)

Spring Data module for Fedora Commons Repository (version 4.x or later) allowing for CRUD operations and query using annotated POJO beans.

*This is just a proof-of-concept implementation and still largely work in progress.*

Partially implemented:

* Mapping of resource properties (`uuid`, `created`, etc.)
* Mapping of simple properties (not collections)
* Custom RDF to Java mapper (based on XSD types)
* Datastream (binary) object persistence
* Lazy-load of datastreams
* Spring Data enabled repository implementation
* SPARQL queries for "findBy" query methods
* Paged queries

To be done:

* Enable custom extensions to Java to RDF converter
* RELS-EXT type relationships with lazy-load
* Transaction support
* Named queries
* Fluent SPARQL DSL for programmatic query specification
* Fixity checks support
* Support versions
* Javadoc

### Acknowledgements

This project is heavily based on the code from the following projects (including configuration files: Maven, Travis CI, etc.)

 * [fcrepo4-client](https://github.com/fcrepo4-labs/fcrepo4-client)

 * [spring-data-solr](https://github.com/spring-projects/spring-data-solr)

### Running integration tests

Executing `mvn verify` with default active profile `cargo-integration-tests` will use `cargo-maven2-plugin` to automatically download and setup the
following test environment before running any integration tests (actual versions may vary, Tomcat 7.0.62 is used for deployment):

1. `jena-fuseki-war` (v. 2.0.0), configured from `${basedir}/etc/fuseki`, sets up empty dataset `/test`
2. `fcrepo-webapp` (v. 4.2.0), configured using `${basedir}/etc/fedora-node-types.cnd`, see [Indexable Node Type](https://wiki.duraspace.org/display/FEDORA40/Indexable+Node+Type)
3. `fcrepo-message-consumer-webapp` (v. 4.2.0)

All the ports needed for the setup are provided by `build-helper-maven-plugin` from the randomly selected available ports. Check the Maven build console
output for actual port numbers.

There is a project property `indexer.start.mode` which can be set to `run` (instead of `start`) to pause Maven build
(for example, `mvn pre-integration-test`) right after the indexer webapp has been configured. This allows to access all three applications
online at the localhost instance (under the ports assigned by `build-helper-maven-plugin`).

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
	// Fedora template bean declaration is omitted
}

// use the the Autowired instance of VehicleCrudRepository like this

List<Vehicle> vehiclesWithLargeMileage = vehicleRepo.findByMilesGreaterThan(15000);
```

The module automatically converts the candidate query methods into [SPARQL](http://jena.apache.org/tutorials/sparql.html) queries run against the triplestore. For example,
the call to the query method `findByMilesGreaterThan` will be translated into

```
SELECT  ?v1
WHERE
  { ?v1  <info:fedora/test/miles>  ?v2 .
    FILTER ( ?v2 > "15000"^^<http://www.w3.org/2001/XMLSchema#int> )
  }
```

### Datastreams

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

### Resource properties

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

This module will automatically perform some useful conversions, i.e. to `UUID` from the `uuid` property or to `ZonedDateTime`
from the `created` timestamp if needed.

### Java to RDF type conversion

To serialize values of Java properties as RDF properties uses in SPARQL updates and queries this module uses (TypeMapper)[https://jena.apache.org/documentation/notes/typed-literals.html]
provided by `jena-core` module and also used by Fedora repository. See [ExtendedXsdDatatypeConverter](https://github.com/gushakov/spring-data-fcrepo4/blob/master/src/main/java/ch/unil/fcrepo4/spring/data/core/convert/rdf/ExtendedXsdDatatypeConverter.java) for implementation details.