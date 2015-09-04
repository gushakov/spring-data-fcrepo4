package ch.unil.fcrepo4.spring.data.core.query.sparql;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Var;

import java.util.Map;

/**
 * @author gushakov
 */
public interface BgpFragment {

    Triple getTriple();

}
