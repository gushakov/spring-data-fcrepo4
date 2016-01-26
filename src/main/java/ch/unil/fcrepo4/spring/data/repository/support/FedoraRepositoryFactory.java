package ch.unil.fcrepo4.spring.data.repository.support;

// based on code from org.springframework.data.solr.repository.support.SolrRepositoryFactory

import ch.unil.fcrepo4.spring.data.core.FedoraOperations;
import ch.unil.fcrepo4.spring.data.repository.query.FedoraEntityInformationCreator;
import ch.unil.fcrepo4.spring.data.repository.query.FedoraEntityInformationCreatorImpl;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author gushakov
 */
public class FedoraRepositoryFactory extends RepositoryFactorySupport {

    private FedoraOperations fedoraOperations;

    private FedoraEntityInformationCreator entityInformationCreator;

    public FedoraRepositoryFactory(FedoraOperations fedoraOperations) {
        Assert.notNull(fedoraOperations);
        this.fedoraOperations = fedoraOperations;
        this.entityInformationCreator = new FedoraEntityInformationCreatorImpl(fedoraOperations.getConverter().getMappingContext());
    }

    @Override
    public <T, ID extends Serializable> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
        return entityInformationCreator.getEntityInformation(domainClass);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Object getTargetRepository(RepositoryInformation metadata) {
        SimpleFedoraRepository repository = getTargetRepositoryViaReflection(metadata,
                getEntityInformation(metadata.getDomainType()), this.fedoraOperations);
        repository.setEntityClass(metadata.getDomainType());
        return repository;
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        return SimpleFedoraRepository.class;
    }

    @Override
    protected QueryLookupStrategy getQueryLookupStrategy(QueryLookupStrategy.Key key, EvaluationContextProvider evaluationContextProvider) {
        return new FedoraQueryLookupStrategy();
    }

    private class FedoraQueryLookupStrategy implements QueryLookupStrategy {

        @Override
        public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, NamedQueries namedQueries) {
            return null;
        }
    }

}
