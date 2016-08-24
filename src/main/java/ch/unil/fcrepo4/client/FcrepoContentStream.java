package ch.unil.fcrepo4.client;

import org.fcrepo.client.FcrepoResponse;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Closeable stream which delegates to the body of a {@linkplain FcrepoResponse} and properly closes it when closed.
 *
 * @author gushakov
 */
public class FcrepoContentStream extends InputStream implements Closeable {
    private FcrepoResponse response;
    private InputStream stream;

    public FcrepoContentStream(FcrepoResponse response) {
        this.response = response;
        this.stream = response.getBody();
    }

    @Override
    public int read() throws IOException {
        return stream.read();
    }

    @Override
    public void close() throws IOException {
        response.close();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.close();
    }
}
