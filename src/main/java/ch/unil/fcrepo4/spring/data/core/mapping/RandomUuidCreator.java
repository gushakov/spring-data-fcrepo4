package ch.unil.fcrepo4.spring.data.core.mapping;

import java.util.UUID;

/**
 * @author gushakov
 */
public class RandomUuidCreator implements UuidCreator {
    @Override
    public String createUuid() {
        return UUID.randomUUID().toString();
    }
}
