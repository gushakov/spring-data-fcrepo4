package ch.unil.fcrepo4.assertj;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author gushakov
 */
public class TimeUtils {
    public static Instant getUtcInstant(String timestamp){
        return Instant.from(DateTimeFormatter.ISO_INSTANT.parse(timestamp));
    }
    public static Date getUtcDate(String timestamp){
        return Date.from(Instant.from(DateTimeFormatter.ISO_INSTANT.parse(timestamp)));
    }
}
