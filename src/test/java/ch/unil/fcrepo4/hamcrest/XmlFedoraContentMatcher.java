package ch.unil.fcrepo4.hamcrest;

import ch.unil.fcrepo4.spring.data.core.Constants;
import org.hamcrest.Factory;
import org.xmlmatchers.XmlMatchers;
import org.xmlmatchers.transform.XmlConverters;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author gushakov
 */
public class XmlFedoraContentMatcher extends AbstractFedoraContentMatcher {

    private String xml;

    public XmlFedoraContentMatcher(String xml) {
        super(Constants.MIME_TYPE_TEXT_XML);
        this.xml = xml;
    }

    @Override
    protected boolean contentMatches(InputStream inputStream) {
        try {
            //FIXME: ignoring second invocation of this method with 0 available bytes
            return inputStream != null &&
                    (inputStream.available() == 0 ||
                            XmlMatchers.isEquivalentTo(new StreamSource(inputStream)).matches(XmlConverters.the(xml)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Factory
    public static XmlFedoraContentMatcher equalsFedoraContentWithXml(String xml) {
        return new XmlFedoraContentMatcher(xml);
    }
}
