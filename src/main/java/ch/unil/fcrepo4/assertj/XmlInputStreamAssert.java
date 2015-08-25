package ch.unil.fcrepo4.assertj;

import org.apache.commons.io.IOUtils;
import org.assertj.core.api.AbstractInputStreamAssert;
import org.xmlmatchers.XmlMatchers;
import org.xmlmatchers.transform.XmlConverters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author gushakov
 */
public class XmlInputStreamAssert extends AbstractInputStreamAssert<XmlInputStreamAssert, InputStream> {
    protected XmlInputStreamAssert(InputStream actual) {
        super(actual, XmlInputStreamAssert.class);
    }

    public XmlInputStreamAssert hasXmlContentEquivalentTo(String xml) {
        isNotNull();

        if (xml == null) {
            failWithMessage("Cannot compare to null");
        } else {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                IOUtils.copy(actual, baos);
                String actualXml = new String(baos.toByteArray());
                boolean match = XmlMatchers.isEquivalentTo(XmlConverters.the(actualXml))
                        .matches(XmlConverters.the(xml));
                if (!match){
                   failWithMessage("Expected XML input stream to be equivalent to %s, but was %s", xml, actualXml);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return this;
    }

}
