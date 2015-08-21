package ch.unil.fcrepo4.readme;

import ch.unil.fcrepo4.spring.data.core.mapping.annotation.Datastream;
import ch.unil.fcrepo4.spring.data.core.mapping.annotation.DsContent;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author gushakov
 */
@Datastream(mimetype = "image/png")
public class CarPhoto {

    @DsContent
    InputStream photoSource;

    public CarPhoto(String fileName) {
        try {
            photoSource = new ClassPathResource(fileName).getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
