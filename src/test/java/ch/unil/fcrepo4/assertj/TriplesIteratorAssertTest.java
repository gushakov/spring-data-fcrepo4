package ch.unil.fcrepo4.assertj;

import org.junit.Test;

import static ch.unil.fcrepo4.assertj.Assertions.assertThat;
import static ch.unil.fcrepo4.assertj.TripleUtils.triples;

/**
 * @author gushakov
 */
public class TriplesIteratorAssertTest {
    @Test
    public void testTriplesIterator() throws Exception {
        assertThat(triples()).isEmpty();
        assertThat(triples("s1:s p o", "s1:t p o"))
                .extractingSubjectsWithNamespace("s1:")
                .extracting("localName", String.class)
                .containsExactly("s", "t");
        assertThat(triples("http://test#s p o", "http://test#t p o"))
                .extractingSubjectsWithNamespace("http://test#")
                .extracting("localName", String.class)
                .containsExactly("s", "t");
    }
}
