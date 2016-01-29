package ch.unil.fcrepo4.beans;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Datastream;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.DsContent;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Property;

import java.io.InputStream;

/**
 * @author gushakov
 */
@Datastream
public class Bean2Datastream1 {

    @Property
    private String wam;

    @DsContent
    private InputStream xmlStream;

    public String getWam() {
        return wam;
    }

    public void setWam(String wam) {
        this.wam = wam;
    }

    public InputStream getXmlStream() {
        return xmlStream;
    }

    public void setXmlStream(InputStream xmlStream) {
        this.xmlStream = xmlStream;
    }
}
