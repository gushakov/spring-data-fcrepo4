package ch.unil.fcrepo4.spring.data.repository;

import ch.unil.fcrepo4.beans.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @author gushakov
 */
public interface VehicleCrudRepository extends FedoraCrudRepository<Vehicle, Long> {

    List<Vehicle> findByMake(String make);

    List<Vehicle> findByMilesGreaterThan(int miles);

    List<Vehicle> findByColorLike(String color);

    List<Vehicle> findByMilesGreaterThanAndConsumptionGreaterThan(int miles, float consumption);

    List<Vehicle> findByMakeAndMilesOrColorAndConsumption(String make, int miles, String color, float consumption);

    Page<Vehicle> findByMilesGreaterThan(int miles, Pageable pageable);

}
