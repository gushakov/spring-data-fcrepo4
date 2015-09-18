package ch.unil.fcrepo4.spring.data.repository;

import ch.unil.fcrepo4.beans.Fruit;

import java.util.List;

/**
 * @author gushakov
 */
public interface FruitCrudRepository extends FedoraCrudRepository<Fruit, Long> {

    List<Fruit> findByWeightGreaterThan(double weight);

}
