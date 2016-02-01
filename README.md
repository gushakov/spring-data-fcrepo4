## Spring Data Fedora Commons Repository (v. 4.x)

[![Build status](https://travis-ci.org/gushakov/spring-data-fcrepo4.svg?branch=master)](https://travis-ci.org/gushakov/spring-data-fcrepo4)

Spring Data module for Fedora Commons Repository (version 4.x or later) allowing for CRUD operations and query using annotated POJO beans.

Note: This branch uses JCR-SQL2 queries against a patched Fedora webapp. For version using SPARQL queries against a triplestore
see [query-triplestore](https://github.com/gushakov/spring-data-fcrepo4/tree/query-triplestore) branch.

*This is just a proof-of-concept implementation and still largely work in progress.*

**Partially implemented:**

* ID to JCR path mapping
* Mapping of resource properties (`uuid`, `created`, etc.)
* Mapping of simple properties (not collections)
* Custom RDF to Java mapper (based on XSD types)
* Datastream (binary) object persistence
* Lazy-load of datastreams
* Spring Data enabled repository implementation
* JSR-SQL2 queries for "findBy" query methods (some types of queries only)
* Paged queries

**To be done:**

* Enable custom extensions to Java to RDF converter
* RELS-EXT type relationships with lazy-load
* Transaction support
* Named queries
* Fixity checks support
* Support versions
* Javadoc

### Acknowledgements

This project is heavily based on the code from the following projects (including configuration files: Maven, Travis CI, etc.)

 * [fcrepo4-client](https://github.com/fcrepo4-labs/fcrepo4-client)

 * [spring-data-solr](https://github.com/spring-projects/spring-data-solr)

### Patch for JCR-SQL2 query

Since as of this writting (v. 4.4.0) Fedora repository does not expose any way to query the underlying JCR implementation. This module
provides a [JcrSqlQueryServlet](https://github.com/gushakov/spring-data-fcrepo4/blob/master/src/main/java/ch/unil/fcrepo4/servlet/JcrSqlQueryServlet.java) which allows
a REST-based access to the querying of the JCR implementations via a JCR-SQL2 query (supported by Modeshape).

So the functionning of this Spring Data module depends on having a JAR with this servet on in the `WEB-INF/lib` directory of the Fedora web application.

### FedoraTemplate

[FedoraTemplate](https://github.com/gushakov/spring-data-fcrepo4/blob/master/src/main/java/ch/unil/fcrepo4/spring/data/core/FedoraTemplate.java)
is the main object responsible for executing CRUD and query operations against Fedora backend.

This is an example of configuring `FedoraTemplate` using Spring Java configuration. It just requires a hostname and a port of Fedora installation.

```java
@Autowired
private Environment env;

@Bean
        public FedoraTemplate fedoraTemplate() throws FedoraException {
            return new FedoraTemplate(env.getProperty("fedora.host"),
                    env.getProperty("fedora.port", Integer.class));

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

```turtle
<http://localhost:9090/rest/vehicle/1>
	<!-- default resource properties are omitted -->
	<info:fedora/test/color>        "Green"^^<http://www.w3.org/2001/XMLSchema#string> ;
	<info:fedora/test/consumption>  "6.5"^^<http://www.w3.org/2001/XMLSchema#float> ;
	<info:fedora/test/make>         "Ford"^^<http://www.w3.org/2001/XMLSchema#string> ;
	<info:fedora/test/miles>        "15000"^^<http://www.w3.org/2001/XMLSchema#int> .
```

### PathCreator

The mandatory `Path` annotation may specify a custom implementation of [PathCreator](https://github.com/gushakov/spring-data-fcrepo4/blob/master/src/main/java/ch/unil/fcrepo4/spring/data/core/mapping/PathCreator.java)
interface to customize how JCR paths are created for the resources mapped to the instances of a bean. For example, if `pathCreator` is specified as follows

```java
public class CustomPathCreator<T> implements PathCreator<T, Long> {
    @Override
    public String createPath(String namespace, Class<T> beanType, Class<Long> pathPropType, String pathPropName, Long pathPropValue) {
        String value = pathPropValue.toString();
        String part1 = value.substring(0, 3);
        String part2 = value.substring(3, 6);
        String part3 = value.substring(6);
        return "/" + namespace + "/" + part1 + "/" + part2 + "/" + part3;
    }

    @Override
    public Long parsePath(String namespace, Class<T> beanType, Class<Long> pathPropType, String pathPropName, String path) {
        return Long.parseLong(StringUtils.removeStart(StringUtils.remove(path, "/"), namespace));
    }
}

// and the bean

@FedoraObject(namespace = "custom")
public class Bean {

    @Path(pathCreator = CustomPathCreator.class)
    private long id;

    public Bean4(long id) {
        this.id = id;
    }

}
```

then the bean `new Bean(123456789L)` will be mapped to a resource with `/custom/123/456/789` path.

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

The module automatically converts the candidate query methods into [JCR-SQL2](https://docs.jboss.org/author/display/MODE/JCR-SQL2) queries. For example,
the call to the query method `findByMake` with argument "Ford" will be translated into

```sql
SELECT * FROM [fedora:Resource] AS n WHERE (ISDESCENDANTNODE(n,'/vehicle') AND n.[test:make] = 'Ford[CAN]^^[CAN]http://www.w3.org/2001/XMLSchema#string')
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
with the corresponding annotations. For example, if one wants to access `http://fedora.info/definitions/v4/repository#created`
properties of the persisted resource, then the corresponding attribute of the bean should be annotated as following:

```java
class Vehicle {
    @Created
    private ZonedDateTime createdDate;
}
```

This module will automatically perform some useful conversions, i.e. to `ZonedDateTime` from the `created` timestamp if needed.

### Java to RDF type conversion

To serialize values of Java properties as RDF properties uses in SPARQL updates and JCR-SQL2 queries this module uses [TypeMapper](https://jena.apache.org/documentation/notes/typed-literals.html)
provided by `jena-core` module and also used by Fedora repository. See [ExtendedXsdDatatypeConverter](https://github.com/gushakov/spring-data-fcrepo4/blob/master/src/main/java/ch/unil/fcrepo4/spring/data/core/convert/rdf/ExtendedXsdDatatypeConverter.java) for implementation details.