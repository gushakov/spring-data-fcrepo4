package ch.unil.fcrepo4.assertj;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import org.junit.Test;

import static ch.unil.fcrepo4.assertj.Assertions.assertThat;

/**
 * @author gushakov
 */
public class TripleAssertTest {

    @Test
    public void testHasSubject() throws Exception {
        assertThat(new Triple(NodeFactory.createURI("s:foobar"), Node.ANY, Node.ANY)).hasSubject("s:foobar");
    }
}
