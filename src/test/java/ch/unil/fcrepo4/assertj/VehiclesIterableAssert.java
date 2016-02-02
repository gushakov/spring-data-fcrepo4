package ch.unil.fcrepo4.assertj;

import ch.unil.fcrepo4.spring.data.repository.Vehicle;
import org.apache.commons.collections4.CollectionUtils;
import org.assertj.core.api.IterableAssert;

import java.util.Iterator;

/**
 * @author gushakov
 */
public class VehiclesIterableAssert extends IterableAssert<Vehicle> {
    protected VehiclesIterableAssert(Iterable<? extends Vehicle> actual) {
        super(actual);
    }

    protected VehiclesIterableAssert(Iterator<? extends Vehicle> actual) {
        super(actual);
    }

    public VehiclesIterableAssert withMilesGreaterThan(int miles) {
        isNotNull();
        boolean filtered = CollectionUtils.filter(actual,
                vehicle -> vehicle.getMiles() > miles);
        return new VehiclesIterableAssert(actual);
    }
}
