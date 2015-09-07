package ch.unil.fcrepo4.spring.data.repository;

import java.util.List;

/**
 * @author gushakov
 */
public interface VehicleCrudRepository extends FedoraCrudRepository<Vehicle, Long> {

    List<Vehicle> findByMake(String make);

    List<Vehicle> findByMakeLike(String make);

    List<Vehicle> findByMakeAndMilesGreaterThan(String make, int miles);

    List<Vehicle> findByMakeAndMilesGreaterThanAndConsumptionGreaterThan(String make, int miles, float consumption);

    List<Vehicle> findByMakeAndMilesOrColorAndConsumption(String make, int miles, String color, float consumption);
}
