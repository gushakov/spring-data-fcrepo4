package ch.unil.fcrepo4.hamcrest;

import org.fcrepo.client.FedoraContent;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.io.InputStream;

/**
 * @author gushakov
 */
public abstract class AbstractFedoraContentMatcher extends TypeSafeDiagnosingMatcher<FedoraContent> {
    private String mimetype;

    public AbstractFedoraContentMatcher(String mimetype) {
        this.mimetype = mimetype;
    }

    @Override
    protected boolean matchesSafely(FedoraContent fedoraContent, Description mismatchDescription) {
        if (fedoraContent.getContentType().equals(mimetype)) {
            if (contentMatches(fedoraContent.getContent())) {
                return true;
            } else {
                describeTo(mismatchDescription.appendText("but content input stream did not match"));
                return false;

            }
        } else {
            describeTo(mismatchDescription.appendText("but content type was ").appendValue(fedoraContent.getContentType()));
            return false;
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("FedoraContent with mimetype ").appendValue(mimetype);
    }

    protected abstract boolean contentMatches(InputStream inputStream);
}
