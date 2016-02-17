package ch.unil.fcrepo4.spring.data.core.query.sparql;

import ch.unil.fcrepo4.spring.data.core.convert.rdf.ExtendedXsdDatatypeConverter;
import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author gushakov
 */
public class SparqlQueryTest {

    @Test
    public void testConstraint() throws Exception {
        RdfDatatypeConverter rdfDatatypeConverter = new ExtendedXsdDatatypeConverter();
        Constraint constraint = new EqualsPredicate(new NamedVariable("s"), new XsdValue(rdfDatatypeConverter.encodeExpressionValue(10)));

        assertThat(constraint.toString()).isEqualTo("( ?s = \"10\"^^xsd:int )");


    }

}
