package ch.unil.fcrepo4.spring.data.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

/**
 * @author gushakov
 */
public interface VehicleCrudRepository extends FedoraCrudRepository<Vehicle, Long> {

    List<Vehicle> findByMake(String make);

    List<Vehicle> findByMakeAndColor(String make, String color);

    List<Vehicle> findByMakeOrColorLike(String make, String color);

    List<Vehicle> findByMilesGreaterThan(int miles);

    Page<Vehicle> findByMilesGreaterThan(int miles, Pageable pageable);

    List<Vehicle> findByColorLike(String color);

    List<Vehicle> findByMilesGreaterThanAndConsumptionGreaterThan(int miles, float consumption);

    List<Vehicle> findByMakeAndMilesOrColorAndConsumption(String make, int miles, String color, float consumption);

    List<Vehicle> findByCreatedGreaterThan(Date date);

    Page<Vehicle> findByCreatedGreaterThan(Date date, Pageable pageable);

}
