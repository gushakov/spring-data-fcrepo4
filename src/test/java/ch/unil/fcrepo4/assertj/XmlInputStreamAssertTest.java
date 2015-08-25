package ch.unil.fcrepo4.assertj;

import org.junit.Test;

import java.io.ByteArrayInputStream;

import static ch.unil.fcrepo4.assertj.Assertions.assertThat;

/**
 * @author gushakov
 */
public class XmlInputStreamAssertTest {

    @Test
    public void testHasXmlContentEquivalentToMatch() throws Exception {
        assertThat(new ByteArrayInputStream("<foo>bar</foo>".getBytes()))
                .hasXmlContentEquivalentTo("<foo><!--waz-->bar</foo>");
    }

    @Test(expected = AssertionError.class)
    public void testHasXmlContentEquivalentToNoMatch() throws Exception {
        try {
            assertThat(new ByteArrayInputStream("<foo>bar</foo>".getBytes()))
                    .hasXmlContentEquivalentTo("<foo>baz</foo>");
        } catch (AssertionError e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }
}
