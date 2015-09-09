package ch.unil.fcrepo4.spring.data.repository;

import ch.unil.fcrepo4.beans.Vehicle;

import java.util.List;

/**
 * @author gushakov
 */
public interface VehicleCrudRepository extends FedoraCrudRepository<Vehicle, Long> {

    List<Vehicle> findByMake(String make);

    List<Vehicle> findByMilesGreaterThan(int miles);

    List<Vehicle> findByMakeLike(String make);

    List<Vehicle> findByMilesGreaterThanAndConsumptionGreaterThan(int miles, float consumption);

    List<Vehicle> findByMakeAndMilesOrColorAndConsumption(String make, int miles, String color, float consumption);
}
