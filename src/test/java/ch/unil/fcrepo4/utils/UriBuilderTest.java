package ch.unil.fcrepo4.utils;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author gushakov
 */
public class UriBuilderTest {

    private static final String baseUrl = "http://localhost:8080/fcrepo/rest";

    @Test
    public void testBaseUrl() throws Exception {
        final UriBuilder uriBuilder = new UriBuilder(baseUrl);
        assertThat(uriBuilder.build().toString()).isEqualTo(baseUrl);
    }

    @Test
    public void testSetPath() throws Exception {
        final UriBuilder uriBuilder = new UriBuilder(baseUrl);
        uriBuilder.setPath("/foobar");
        assertThat(uriBuilder.getPath()).isEqualTo("/foobar");
        uriBuilder.setPath("foobar");
        assertThat(uriBuilder.getPath()).isEqualTo("/foobar");
        uriBuilder.setPath("/");
        assertThat(uriBuilder.getPath()).isEqualTo("/");
        uriBuilder.setPath("");
        assertThat(uriBuilder.getPath()).isEqualTo("/");
    }

    @Test
    public void testAppendPathSegmentWithLeadingSlash() throws Exception {
        final UriBuilder uriBuilder = new UriBuilder(baseUrl);
        final String path = "/foobar";
        uriBuilder.appendPathSegment(path);
        assertThat(uriBuilder.build().toString()).isEqualTo(baseUrl + path);
    }

    @Test
    public void testAppendPathSegmentWithTrailingSlash() throws Exception {
        final UriBuilder uriBuilder = new UriBuilder(baseUrl);
        final String path = "foobar/";
        uriBuilder.appendPathSegment(path);
        assertThat(uriBuilder.build().toString()).isEqualTo(baseUrl + "/" + path);
    }

    @Test
    public void testAppendPathSegmentWithoutSlash() throws Exception {
        final UriBuilder uriBuilder = new UriBuilder(baseUrl);
        final String path = "foobar";
        uriBuilder.appendPathSegment(path);
        assertThat(uriBuilder.build().toString()).isEqualTo(baseUrl + "/" + path);
    }

    @Test
    public void testAppendPathSegmentWithNullOrEmpty() throws Exception {
        final UriBuilder uriBuilder = new UriBuilder(baseUrl);
        uriBuilder.appendPathSegment(null);
        assertThat(uriBuilder.build().toString()).isEqualTo(baseUrl);
        uriBuilder.appendPathSegment("");
        assertThat(uriBuilder.build().toString()).isEqualTo(baseUrl);
        uriBuilder.appendPathSegment(" ");
        assertThat(uriBuilder.build().toString()).isEqualTo(baseUrl);
    }

}
