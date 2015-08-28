package ch.unil.fcrepo4.spring.data.repository.support;

// based on code from org.springframework.data.solr.repository.support.SimpleSolrRepository

import ch.unil.fcrepo4.spring.data.core.FedoraOperations;
import ch.unil.fcrepo4.spring.data.repository.FedoraCrudRepository;
import ch.unil.fcrepo4.spring.data.repository.query.FedoraEntityInformation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * @author gushakov
 */
public class SimpleFedoraRepository<T, ID extends Serializable> implements FedoraCrudRepository<T, ID> {

    private FedoraOperations fedoraOperations;
    private Class<T> entityClass;
    private FedoraEntityInformation<T, ?> entityInformation;

    public SimpleFedoraRepository() {
    }

    /**
     * Will be called from @{@linkplain FedoraRepositoryFactory#getTargetRepository(RepositoryInformation)}.
     *
     * @param entityInformation
     * @param fedoraOperations
     */
    public SimpleFedoraRepository(FedoraEntityInformation<T, ?> entityInformation, FedoraOperations fedoraOperations) {
        this(fedoraOperations);
        Assert.notNull(entityInformation);
        this.entityInformation = entityInformation;
        setEntityClass(entityInformation.getJavaType());
    }

    public SimpleFedoraRepository(FedoraOperations fedoraOperations) {
        Assert.notNull(fedoraOperations);
        this.fedoraOperations = fedoraOperations;
    }

    @Override
    public Iterable<T> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public <S extends T> S save(S entity) {
        return null;
    }

    @Override
    public <S extends T> Iterable<S> save(Iterable<S> entities) {
        return null;
    }

    @Override
    public T findOne(ID id) {
        return null;
    }

    @Override
    public boolean exists(ID id) {
        return false;
    }

    @Override
    public Iterable<T> findAll() {
        return null;
    }

    @Override
    public Iterable<T> findAll(Iterable<ID> ids) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void delete(ID id) {

    }

    @Override
    public void delete(T entity) {

    }

    @Override
    public void delete(Iterable<? extends T> entities) {

    }

    @Override
    public void deleteAll() {

    }

    public Class<T> getEntityClass() {
        return entityClass;
    }

    public final void setEntityClass(Class<T> entityClass) {
        Assert.notNull(entityClass, "Entity class must not be null");
        this.entityClass = entityClass;
    }
}
