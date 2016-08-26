package ch.unil.fcrepo4.utils;

import org.junit.Test;

import static ch.unil.fcrepo4.utils.Utils.normalize;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author gushakov
 */
public class UtilsTest {

    @Test
    public void testCombine() throws Exception {
        assertThat(normalize("foo", "bar")).isEqualTo("/foo/bar");
        assertThat(normalize("/foo/", "bar")).isEqualTo("/foo/bar");
        assertThat(normalize("/foo//", "bar")).isEqualTo("/foo/bar");
        assertThat(normalize("/foo//", "//bar")).isEqualTo("/foo/bar");
        assertThat(normalize("//foo//", "//bar//")).isEqualTo("/foo/bar");
        assertThat(normalize("foo//bar/wam", "baz")).isEqualTo("/foo/bar/wam/baz");
    }

}
