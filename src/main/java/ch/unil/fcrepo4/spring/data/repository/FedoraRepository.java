package ch.unil.fcrepo4.spring.data.repository;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import java.io.Serializable;

/**
 * @author gushakov
 */
@NoRepositoryBean
public interface FedoraRepository<T, ID extends Serializable> extends Repository<T, ID> {
}
