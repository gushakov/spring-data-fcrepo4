package ch.unil.fcrepo4.assertj;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import org.fcrepo.kernel.RdfLexicon;
import org.junit.Test;

import static ch.unil.fcrepo4.assertj.Assertions.assertThat;

/**
 * @author gushakov
 */
public class NodeAssertTest {
    @Test
    public void testIsLiteral() throws Exception {
        Node literalNode = NodeFactory.createLiteral("s");
        assertThat(literalNode).isLiteral();
        assertThat(Node.ANY).isNotLiteral();
    }

    @Test
    public void testIsXml() throws Exception {
        Node xmlNode = NodeFactory.createLiteral("s", "lang", true);
        assertThat(xmlNode).isXml();
    }

    @Test
    public void testUri() throws Exception {
        String uri = RdfLexicon.DC_TITLE.getURI();
        Node uriNode = NodeFactory.createURI(uri);
        assertThat(uriNode).isUri().hasUri(uri);
    }
}
