package ch.unil.fcrepo4.spring.data.core.query.sparql;

import ch.unil.fcrepo4.spring.data.core.Constants;
import ch.unil.fcrepo4.spring.data.core.convert.rdf.ExtendedXsdDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpAsQuery;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_LessThan;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueInteger;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author gushakov
 */
public class SparqlQueryTest {

    @Test
    public void testTriples() throws Exception {
        RdfDatatypeConverter rdfDatatypeConverter = new ExtendedXsdDatatypeConverter();

        Triple triple = new Triple(NodeFactory.createURI(""),
                NodeFactory.createURI(Constants.OCM_URI_NAMESPACE+"class"),
                rdfDatatypeConverter.encodeLiteralValue("package.name.FooBar"));

        System.out.println(triple);

        Query query = QueryFactory.make();
        ElementTriplesBlock block = new ElementTriplesBlock();
        block.addTriple(triple);
        block.addTriple(Triple.ANY);
        System.out.println(block);
        query.setQueryPattern(block);
        System.out.println(query);
    }

    @Test
    public void testAlgebra() throws Exception {
        // from https://jena.apache.org/documentation/query/manipulating_sparql_using_arq.html

        // ?s ?p ?o .
        Triple pattern =
                Triple.create(Var.alloc("s"), Var.alloc("p"), Var.alloc("o"));
        // ( ?s < 20 )
        Expr e = new E_LessThan(new ExprVar("s"), new NodeValueInteger(20));

        Op op;
        BasicPattern pat = new BasicPattern();                 // Make a pattern
        pat.add(pattern);                                      // Add our pattern match
        op = new OpBGP(pat);                                   // Make a BGP from this pattern
        System.out.println(op);
        op = OpFilter.filter(e, op);                           // Filter that pattern with our expression
        System.out.println(op);
        op = new OpProject(op, Arrays.asList(Var.alloc("s"))); // Reduce to just ?s
        System.out.println(op);
        Query q = OpAsQuery.asQuery(op);                       // Convert to a query
        q.setQuerySelectType();

        System.out.println(q);
    }
}
