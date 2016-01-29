package ch.unil.fcrepo4.assertj;

import ch.unil.fcrepo4.beans.Vehicle;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import org.fcrepo.client.FedoraResource;

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
}
