package ch.unil.fcrepo4.client;

import java.io.InputStream;

/**
 * Modeled after {@code org.fcrepo.client.FedoraContent}
 * @author gushakov
 */
public class FedoraContent {

    private InputStream content;

    private String contentType;

    private String filename;

    public InputStream getContent() {
        return content;
    }

    public void setContent(InputStream content) {
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
