package ch.unil.fcrepo4.spring.data.core.query.qom;

import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.impl.XSDBaseStringType;

import javax.jcr.Binary;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;

/**
 * @author gushakov
 */
public class ValueImpl implements javax.jcr.Value {

    private Object rawValue;

    private RDFDatatype rdfDatatype;

    public ValueImpl(RdfDatatypeConverter rdfDatatypeConverter, Object rawValue) {
        this.rawValue = rawValue;
        this.rdfDatatype = rdfDatatypeConverter.convert(rawValue.getClass());
    }

    @Override
    public String getString() throws ValueFormatException, IllegalStateException, RepositoryException {
        return "'" + rdfDatatype.unparse(rawValue) + "\30^^\30" + rdfDatatype.getURI() + "'";
    }

    @Override
    public InputStream getStream() throws RepositoryException {
        return null;
    }

    @Override
    public Binary getBinary() throws RepositoryException {
        return null;
    }

    @Override
    public long getLong() throws ValueFormatException, RepositoryException {
        return 0;
    }

    @Override
    public double getDouble() throws ValueFormatException, RepositoryException {
        return 0;
    }

    @Override
    public BigDecimal getDecimal() throws ValueFormatException, RepositoryException {
        return null;
    }

    @Override
    public Calendar getDate() throws ValueFormatException, RepositoryException {
        return null;
    }

    @Override
    public boolean getBoolean() throws ValueFormatException, RepositoryException {
        return false;
    }

    @Override
    public int getType() {

        if (rdfDatatype instanceof XSDBaseStringType) {
            return PropertyType.STRING;
        }

        throw new IllegalStateException();
    }

    @Override
    public String toString() {
        try {
            return getString();
        } catch (RepositoryException e) {
            throw new IllegalStateException(e);
        }
    }
}
