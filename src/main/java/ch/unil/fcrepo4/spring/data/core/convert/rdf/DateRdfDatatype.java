package ch.unil.fcrepo4.spring.data.core.convert.rdf;


import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.xsd.XSDDatatype;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

// based on example from https://jena.apache.org/documentation/notes/typed-literals.html

/**
 * @author gushakov
 */
public class DateRdfDatatype extends BaseDatatype {

    private static DateRdfDatatype instance;

    public static DateRdfDatatype getInstance() {
        if (instance == null) {
            instance = new DateRdfDatatype();
        }
        return instance;
    }

    private DateRdfDatatype() {
        super(XSDDatatype.XSDdateTime.getURI());
    }

    @Override
    public Class<?> getJavaClass() {
        return Date.class;
    }

    @Override
    public String unparse(Object value) {
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(((Date) value).toInstant(), ZoneId.of("UTC"));
        return dateTime.format(DateTimeFormatter.ISO_INSTANT);
    }

    @Override
    public Object parse(String lexicalForm) throws DatatypeFormatException {
        try {
            return Date.from(Instant.from(DateTimeFormatter.ISO_INSTANT.parse(lexicalForm)));
        } catch (DateTimeParseException e) {
            throw new DatatypeFormatException(lexicalForm, instance, e.getMessage());
        }
    }
}
