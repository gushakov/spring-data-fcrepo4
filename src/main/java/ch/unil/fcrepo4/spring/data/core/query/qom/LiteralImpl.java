package ch.unil.fcrepo4.spring.data.core.query.qom;

import ch.unil.fcrepo4.spring.data.core.convert.rdf.RdfDatatypeConverter;
import org.springframework.data.util.TypeInformation;

import javax.jcr.Value;

/**
 * @author gushakov
 */
public class LiteralImpl extends StaticOperandImpl implements Literal {

    private RdfDatatypeConverter rdfDatatypeConverter;

    private Object rawValue;

    private TypeInformation typeInformation;

    public LiteralImpl(TypeInformation typeInformation, RdfDatatypeConverter rdfDatatypeConverter, Object rawValue) {
        this.typeInformation = typeInformation;
        this.rdfDatatypeConverter = rdfDatatypeConverter;
        this.rawValue = rawValue;
    }

    @Override
    public Value getLiteralValue() {
        return new ValueImpl(rdfDatatypeConverter, rawValue);
    }

    @Override
    public String toString() {
        return getLiteralValue().toString();
    }
}
