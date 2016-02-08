package ch.unil.fcrepo4.spring.data.core.query.qom;

/**
 * @author gushakov
 */
public class PropertyValueImpl extends DynamicOperandImpl implements PropertyValue {
    private String selectorName;

    private String propertyName;

    public PropertyValueImpl(String selectorName, String propertyName) {
        this.selectorName = selectorName;
        this.propertyName = propertyName;
    }

    @Override
    public String getSelectorName() {
        return selectorName;
    }

    @Override
    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public String toString() {
        return selectorName + ".[" + propertyName + "]";
    }
}
