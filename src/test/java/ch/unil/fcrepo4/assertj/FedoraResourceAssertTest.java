package ch.unil.fcrepo4.assertj;

import ch.unil.fcrepo4.client.FedoraResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static ch.unil.fcrepo4.assertj.Assertions.assertThat;
import static ch.unil.fcrepo4.assertj.TimeUtils.getUtcDate;
import static ch.unil.fcrepo4.assertj.TimeUtils.getUtcInstant;
import static org.mockito.Mockito.when;

/**
 * @author gushakov
 */
@RunWith(MockitoJUnitRunner.class)
public class FedoraResourceAssertTest {

    private static final String NAME = "test";
    private static final String CREATED_TIMESTAMP = "2015-04-27T14:50:03.661Z";

    @Mock
    private FedoraResource mockResource;

    @Before
    public void setUp() throws Exception {
        when(mockResource.getName()).thenReturn(NAME);
        when(mockResource.getCreatedDate()).then(invocation -> getUtcDate(CREATED_TIMESTAMP));
    }

    @Test
    public void testHasName() throws Exception {
        assertThat(mockResource).hasName(NAME);
    }

    @Test
    public void testCreatedBefore() throws Exception {
        assertThat(mockResource).createdBefore(getUtcInstant(CREATED_TIMESTAMP).plusSeconds(1));
    }

    @Test(expected = AssertionError.class)
    public void testCreatedBeforeFail1() throws Exception {
        assertThat(mockResource).createdBefore(getUtcInstant(CREATED_TIMESTAMP));
    }

    @Test(expected = AssertionError.class)
    public void testCreatedBeforeFail2() throws Exception {
        assertThat(mockResource).createdBefore(getUtcInstant(CREATED_TIMESTAMP).minusSeconds(1));
    }

    @Test
    public void testCreatedOnOrBefore() throws Exception {
         assertThat(mockResource).createdAtOrBefore(getUtcInstant(CREATED_TIMESTAMP));
    }
}
