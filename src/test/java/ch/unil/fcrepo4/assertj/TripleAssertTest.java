package ch.unil.fcrepo4.assertj;

import org.junit.Test;

import static ch.unil.fcrepo4.assertj.Assertions.assertThat;
import static ch.unil.fcrepo4.assertj.TripleUtils.triple;

/**
 * @author gushakov
 */
public class TripleAssertTest {

    @Test
    public void testHasSubject() throws Exception {
        assertThat(triple("s p o")).hasSubject("s");
        assertThat(triple("s1:s p o")).hasSubject("s1:s");
    }
}
