package ch.unil.fcrepo4.spring.data.core.convert.rdf;


import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(DateRdfDatatype.class);

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
            logger.debug("Converting {} to Date instance", lexicalForm);
            return Date.from(ZonedDateTime.from(DateTimeFormatter.ISO_ZONED_DATE_TIME
                    .parse(lexicalForm)).toInstant());
        } catch (DateTimeParseException e) {
            throw new DatatypeFormatException(lexicalForm, instance, e.getMessage());
        }
    }
}
