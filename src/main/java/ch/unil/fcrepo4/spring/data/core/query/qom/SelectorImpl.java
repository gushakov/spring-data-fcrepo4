package ch.unil.fcrepo4.spring.data.core.query.qom;

import ch.unil.fcrepo4.spring.data.core.mapping.FedoraObjectPersistentEntity;

/**
 * @author gushakov
 */
public class SelectorImpl implements Selector {

    private static final String FEDORA_RESOURCE_NODE_TYPE_NAME = "[fedora:Container]";

    FedoraObjectPersistentEntity<?> entity;

    public SelectorImpl(FedoraObjectPersistentEntity<?> entity) {
        this.entity = entity;
    }

    @Override
    public String getNodeTypeName() {
        return FEDORA_RESOURCE_NODE_TYPE_NAME;
    }

    @Override
    public String getSelectorName() {
        return entity.getType().getSimpleName();
    }

    @Override
    public String toString() {
        return getNodeTypeName() + " AS " + getSelectorName();
    }

    @Override
    public String getNamespace() {
        return entity.getNamespace();
    }
}
