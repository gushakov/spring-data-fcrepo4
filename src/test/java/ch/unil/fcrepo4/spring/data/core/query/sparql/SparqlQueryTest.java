package ch.unil.fcrepo4.spring.data.core.query.sparql;

import ch.unil.fcrepo4.spring.data.core.Constants;
import ch.unil.fcrepo4.spring.data.core.convert.rdf.ExtendedXsdDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_LessThan;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.nodevalue.NodeValueInteger;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
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
