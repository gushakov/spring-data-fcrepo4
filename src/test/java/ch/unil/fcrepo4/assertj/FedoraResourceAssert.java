package ch.unil.fcrepo4.assertj;

import org.assertj.core.api.AbstractAssert;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraResource;

import java.time.Instant;

/**
 * Provides custom assertions for {@linkplain FedoraResource}. Uses {@code java.time} API for date and time assertions.
 *
 * @author gushakov
 */
public class FedoraResourceAssert extends AbstractAssert<FedoraResourceAssert, FedoraResource> {
    public FedoraResourceAssert(FedoraResource actual) {
        super(actual, FedoraResourceAssert.class);
    }

    /**
     * Asserts whether the tested {@linkplain FedoraResource} has the matching {@code name}.
     */
    public FedoraResourceAssert hasName(String name) throws FedoraException {
        isNotNull();

        String actualName = actual.getName();
        if (!actualName.equals(name)) {
            failWithMessage("Expected Fedora resource name to be <%s> but was <%s>", name, actualName);
        }
        return this;
    }

    /**
     * Asserts whether the tested {@linkplain FedoraResource} was created <em>strictly before</em> the given instant in
     * time.
     */
    public FedoraResourceAssert createdBefore(Instant instant) throws FedoraException {
        isNotNull();
        Instant actualDateInstant = actual.getCreatedDate().toInstant();
        if (!instant.isAfter(actualDateInstant)) {
            failWithMessage("Expected Fedora resource to be created strictly before <%s> but was created <%s>",
                    instant, actualDateInstant);
        }
        return this;
    }

    /**
     * Asserts whether the tested {@linkplain FedoraResource} was created <em>at or before</em> the given instant in
     * time.
     */
    public FedoraResourceAssert createdAtOrBefore(Instant instant) throws FedoraException {
        isNotNull();
        Instant actualDateInstant = actual.getCreatedDate().toInstant();
        if (!instant.equals(actualDateInstant)
                && !instant.isAfter(actualDateInstant)) {
            failWithMessage("Expected Fedora resource to be created at or before <%s> but was created <%s>",
                    instant, actualDateInstant);
        }
        return this;
    }
}
