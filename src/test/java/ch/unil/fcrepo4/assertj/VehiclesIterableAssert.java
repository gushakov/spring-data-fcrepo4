package ch.unil.fcrepo4.assertj;

import ch.unil.fcrepo4.spring.data.repository.Vehicle;
import org.assertj.core.api.IterableAssert;

import java.util.Iterator;
import java.util.stream.StreamSupport;

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

    @SuppressWarnings("unchecked")
    public VehiclesIterableAssert withMilesGreaterThan(int miles) {
        isNotNull();
        return new VehiclesIterableAssert((Iterable<? extends Vehicle>) StreamSupport.stream(actual.spliterator(), false)
                .filter(vehicle -> vehicle.getMiles() > miles));
    }
}
