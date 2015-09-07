package ch.unil.fcrepo4.spring.data.core.convert.rdf;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;

import java.util.UUID;

/**
 * @author gushakov
 */
public class UuidRdfDatatype extends BaseDatatype {
    private static UuidRdfDatatype instance;

    public static UuidRdfDatatype getInstance() {
        if(instance == null){
            instance = new UuidRdfDatatype();
        }
        return instance;
    }

    private UuidRdfDatatype() {
        super(XSDDatatype.XSDinteger.getURI());
    }

    @Override
    public Class<?> getJavaClass() {
        return UUID.class;
    }

    @Override
    public String unparse(Object value) {
        return value.toString();
    }

    @Override
    public Object parse(String lexicalForm) throws DatatypeFormatException {
        try {
            return UUID.fromString(lexicalForm);
        } catch (IllegalArgumentException e) {
            throw new DatatypeFormatException(lexicalForm, instance, e.getMessage());
        }
    }
}
