package ch.unil.fcrepo4.assertj;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import org.fcrepo.client.FedoraResource;

/**
 * @author gushakov
 */
public class Assertions extends org.assertj.core.api.Assertions {

    public static FedoraResourceAssert assertThat(FedoraResource fedoraResource){
        return new FedoraResourceAssert(fedoraResource);
    }

    public static NodeAssert assertThat(Node node){
        return new NodeAssert(node);
    }

    public static TripleAssert assertThat(Triple triple) {
        return new TripleAssert(triple);
    }
}
