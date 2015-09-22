## Spring Data Fedora Commons Repository (v. 4.x)

[![Build status](https://travis-ci.org/gushakov/spring-data-fcrepo4.svg?branch=master)](https://travis-ci.org/gushakov/spring-data-fcrepo4)

*This is still work in progress.*

Spring Data module for Fedora Commons Repository (version 4.x or later) allowing for CRUD operations and query on Fedora repository with automatic
conversion to annotated POJO beans.

### Acknowledgements

This project is heavily based on the code from the following projects (including configuration files: Maven, Travis CI, etc.)

 * [fcrepo4-client](https://github.com/fcrepo4-labs/fcrepo4-client)

 * [spring-data-solr](https://github.com/spring-projects/spring-data-solr)

### Synopsis

The module's functionality centers around [FedoraTemplate](https://github.com/gushakov/spring-data-fcrepo4/blob/master/src/main/java/ch/unil/fcrepo4/spring/data/core/FedoraTemplate.java)
which allows to execute CRUD operations and queries against Fedora/triplestore instance with automatic conversion to and from Java objects decorated with a minimal set
of annotations.

This is an example of configuring `FedoraTemplate` with Spring.

```java
@Autowired
private Environment env;

@Bean
public FedoraTemplate fedoraTemplate() throws FedoraException {
	return new FedoraTemplate(env.getProperty("fedora.repository.url"), env.getProperty("triplestore.sparql.query.url"));
}
```

Once the template is initialized one can use it to store and retrieve a bean from the Fedora repository. For example,

```java
Vehicle vehicle = new Vehicle();
fedoraTemplate.save(vehicle);
```

This is how the instances to be processed by the module look like. For example,

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

Given an object defined as `new Vehicle(1L, "Ford", "Green", 15000, 6.5f)` and assuming the Fedora instance is running under http://localhost:9090/rest,
a `FedoraResource` (of type container) will be created with the following properties (exported as RDF):

```xml
<http://localhost:9090/rest/vehicle/1>
	<!-- system properties are omitted -->
	<info:fedora/test/color>        "Green"^^<http://www.w3.org/2001/XMLSchema#string> ;
	<info:fedora/test/consumption>  "6.5"^^<http://www.w3.org/2001/XMLSchema#float> ;
	<info:fedora/test/make>         "Ford"^^<http://www.w3.org/2001/XMLSchema#string> ;
	<info:fedora/test/miles>        "15000"^^<http://www.w3.org/2001/XMLSchema#int> .
```