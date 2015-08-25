package ch.unil.fcrepo4.beans;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Datastream;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.DsContent;

import java.io.InputStream;

/**
 * @author gushakov
 */
@Datastream
public class Bean3Datastream1 {

    @DsContent
    private InputStream xmlStream;

    public InputStream getXmlStream() {
        return xmlStream;
    }

    public void setXmlStream(InputStream xmlStream) {
        this.xmlStream = xmlStream;
    }
}
