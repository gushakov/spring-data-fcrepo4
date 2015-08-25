package ch.unil.fcrepo4.beans;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Datastream;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.DsContent;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Property;

import java.io.InputStream;

/**
 * @author gushakov
 */
@Datastream
public class Bean3Datastream1 {

    @Property
    private int number;

    @DsContent
    private InputStream xmlStream;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public InputStream getXmlStream() {
        return xmlStream;
    }

    public void setXmlStream(InputStream xmlStream) {
        this.xmlStream = xmlStream;
    }
}
