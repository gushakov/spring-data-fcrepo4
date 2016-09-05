package ch.unil.fcrepo4.assertj;

import ch.unil.fcrepo4.client.FedoraResource;
import ch.unil.fcrepo4.spring.data.repository.Vehicle;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import java.io.InputStream;
import java.util.Iterator;

/**
 * @author gushakov
 */
public class Assertions  {

    public static FedoraResourceAssert assertThat(FedoraResource fedoraResource){
        return new FedoraResourceAssert(fedoraResource);
    }

    public static NodeAssert assertThat(Node node){
        return new NodeAssert(node);
    }

    public static TripleAssert assertThat(Triple triple) {
        return new TripleAssert(triple);
    }

    public static TriplesIteratorAssert assertThat(Iterator<Triple> triplesIterator){
        return new TriplesIteratorAssert(triplesIterator);
    }

    public static XmlInputStreamAssert assertThat(InputStream xmlStream){
        return new XmlInputStreamAssert(xmlStream);
    }

    public static VehicleAssert assertThat(Vehicle vehicle) {
        return new VehicleAssert(vehicle);
    }

    public static VehiclesIterableAssert assertThat(Iterable<Vehicle> vehicles){
        return new VehiclesIterableAssert(vehicles);
    }
}
