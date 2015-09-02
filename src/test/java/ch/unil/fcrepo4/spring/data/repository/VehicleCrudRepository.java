package ch.unil.fcrepo4.spring.data.repository;

import java.util.List;

/**
 * @author gushakov
 */
public interface VehicleCrudRepository extends FedoraCrudRepository<Vehicle, Long> {

    List<Vehicle> findByMake(String make);

    List<Vehicle> findByMakeAndMilesGreaterThan(String make, int miles);
}
