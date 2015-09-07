package ch.unil.fcrepo4.spring.data.core.convert.rdf;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;

import java.time.ZoneId;
import java.time.ZonedDateTime;
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
        return dateTime.format(RdfDatatypeConstants.THREE_DIGITS_MILLIS_ISO_FORMATTER);
    }

    @Override
    public Object parse(String lexicalForm) throws DatatypeFormatException {
        try {
            return RdfDatatypeConstants.THREE_DIGITS_MILLIS_ISO_FORMATTER.parse(lexicalForm);
        } catch (DateTimeParseException e) {
            throw new DatatypeFormatException(lexicalForm, instance, e.getMessage());
        }
    }
}
