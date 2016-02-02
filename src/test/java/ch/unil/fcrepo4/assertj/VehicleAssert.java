package ch.unil.fcrepo4.assertj;

// based on http://www.petrikainulainen.net/programming/unit-testing/turning-assertions-into-a-domain-specific-language/

import ch.unil.fcrepo4.spring.data.repository.Vehicle;
import org.assertj.core.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author gushakov
 */
public class VehicleAssert extends AbstractAssert<VehicleAssert, Vehicle> {
    protected VehicleAssert(Vehicle actual) {
        super(actual, VehicleAssert.class);
    }

    public VehicleAssert hasMake(String make){
        isNotNull();
        String actualMake = actual.getMake();
        assertThat(actualMake).isEqualTo(make)
                .overridingErrorMessage("Expecting vehicle to have make: <%s> but was <%s>", make, actualMake);
        return this;
    }

    public VehicleAssert hasColor(String color){
        isNotNull();
        String actualColor = actual.getColor();
        assertThat(actualColor).isEqualTo(color)
                .overridingErrorMessage("Expecting vehicle to have color: <%s> but was <%s>", color, actualColor);
        return this;
    }

    public VehicleAssert hasMilesGreaterThan(int miles){
        isNotNull();
        int actualMiles = actual.getMiles();
        assertThat(actualMiles).isGreaterThan(miles)
                .overridingErrorMessage("Expecting vehicle to have miles greater than: <%d> but was <%d>", miles, actualMiles);
        return this;
    }

    public VehicleAssert hasColorLike(String color){
        isNotNull();
        String actualColor = actual.getColor();
        assertThat(actualColor)
                .overridingErrorMessage("Expecting vehicle to have color like: <%s> but was <%s>", color, actualColor)
                .containsIgnoringCase(color);
        return this;
    }
}
