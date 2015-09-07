package ch.unil.fcrepo4.spring.data.core.convert.rdf;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import org.junit.Test;

import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author gushakov
 */
public class ExtendedXsdDatatypeConverterTest {

    @Test
    public void testConvertDate() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.set(2000, Calendar.JANUARY, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 10);
        Date date = calendar.getTime();
        ExtendedXsdDatatypeConverter converter = new ExtendedXsdDatatypeConverter();
        RDFDatatype rdfDatatype = converter.convert(Date.class);
        Node literal = converter.encodeLiteralValue(date);
        assertThat(literal.getLiteralDatatypeURI()).isEqualTo(rdfDatatype.getURI());
        assertThat(literal.getLiteralLexicalForm()).isEqualTo("2000-01-01T00:00:00.010Z");
    }

    @Test
    public void testConvertDateTime() throws Exception {
        ZonedDateTime dateTime = ZonedDateTime.of(2000, Month.JANUARY.getValue(), 1, 0, 0, 0, 10000000, ZoneId.of("UTC"));
        ExtendedXsdDatatypeConverter converter = new ExtendedXsdDatatypeConverter();
        RDFDatatype rdfDatatype = converter.convert(ZonedDateTime.class);
        Node literal = converter.encodeLiteralValue(dateTime);
        assertThat(literal.getLiteralDatatypeURI()).isEqualTo(rdfDatatype.getURI());
        assertThat(literal.getLiteralLexicalForm()).isEqualTo("2000-01-01T00:00:00.010Z");
    }

    @Test
    public void testConvertUuid() throws Exception {
        //9509aaf7-148b-44f2-9863-4f5a0ee62c6c
        UUID uuid = UUID.fromString("9509aaf7-148b-44f2-9863-4f5a0ee62c6c");
        ExtendedXsdDatatypeConverter converter = new ExtendedXsdDatatypeConverter();
        Node literal = converter.encodeLiteralValue(uuid);
        RDFDatatype rdfDatatype = converter.convert(UUID.class);
        assertThat(literal.getLiteralDatatypeURI()).isEqualTo(rdfDatatype.getURI());
        assertThat(literal.getLiteralLexicalForm())
                .isEqualTo("9509aaf7-148b-44f2-9863-4f5a0ee62c6c");
    }

}
