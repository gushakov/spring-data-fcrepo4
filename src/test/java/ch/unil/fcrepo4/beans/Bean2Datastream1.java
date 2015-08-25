package ch.unil.fcrepo4.beans;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Datastream;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.DsContent;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Property;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Uuid;

import java.io.InputStream;
import java.util.UUID;

/**
 * @author gushakov
 */
@Datastream
public class Bean2Datastream1 {

    @Uuid
    private UUID uuid;

    @Property
    private String wam;

    @DsContent
    private InputStream xmlStream;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

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
