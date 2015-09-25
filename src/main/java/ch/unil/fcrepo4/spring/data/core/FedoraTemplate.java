package ch.unil.fcrepo4.spring.data.core;

import ch.unil.fcrepo4.client.TransactionalFedoraRepository;
import ch.unil.fcrepo4.client.TransactionalFedoraRepositoryImpl;
import ch.unil.fcrepo4.spring.data.core.convert.FedoraConverter;
import ch.unil.fcrepo4.spring.data.core.convert.FedoraMappingConverter;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Resource;
import org.apache.commons.lang3.StringUtils;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author gushakov
 */
public class FedoraTemplate implements FedoraOperations, InitializingBean, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(FedoraTemplate.class);
    private ApplicationContext applicationContext;

    private FedoraConverter fedoraConverter;

    private TransactionalFedoraRepository repository;

    private String triplestoreQueryUrl;

    private static final FedoraExceptionTranslator EXCEPTION_TRANSLATOR = new FedoraExceptionTranslator();

    public FedoraTemplate(TransactionalFedoraRepository repository) {
        Assert.notNull(repository);
        this.repository = repository;
    }

    public FedoraTemplate(String repositoryUrl) {
        Assert.notNull(repositoryUrl);
        this.repository = new TransactionalFedoraRepositoryImpl(repositoryUrl);
    }

    public FedoraTemplate(String repositoryUrl, String triplestoreQueryUrl) {
        Assert.notNull(repositoryUrl);
        Assert.notNull(triplestoreQueryUrl);
        this.repository = new TransactionalFedoraRepositoryImpl(repositoryUrl);
        this.triplestoreQueryUrl = triplestoreQueryUrl;

    }

    abstract class TransactionExecution {
        public void execute(){
            try {
                repository.startTransaction();
                doInTransaction();
                repository.commitTransaction();
            } catch (RuntimeException | FedoraException e) {
                try {
                    repository.rollbackTransaction();
                } catch (FedoraException fe) {
                    handleException(fe);
                }
                handleException(e);
            }
        }
        public abstract void doInTransaction() throws FedoraException;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        if (fedoraConverter == null) {
            // get the default FedoraConverter
            fedoraConverter = new FedoraMappingConverter(repository);
        }

        registerPersistenceExceptionTranslator();
    }

    @Override
    public FedoraConverter getConverter() {
        return fedoraConverter;
    }

    @Override
    public <T> void save(T bean) {
        new TransactionExecution(){
            @Override
            public void doInTransaction() throws FedoraException {
                FedoraObject fedoraObject = fedoraConverter.getFedoraObject(bean);
                fedoraConverter.updateIndex(fedoraObject);
                fedoraConverter.write(bean, fedoraObject);
            }
        }.execute();
    }

    @Override
    public <T, ID> T load(ID id, Class<T> beanType) {
        return fedoraConverter.read(beanType, fedoraConverter.getFedoraObject(id, beanType));
    }

    @Override
    public <T, ID> boolean exists(ID id, Class<T> beanType) {
        return fedoraConverter.exists(fedoraConverter.getFedoraObjectPath(id, beanType));
    }

    @Override
    public <T> List<T> query(Query rdfQuery, Class<T> beanType) {
        Assert.notNull(rdfQuery);
        Assert.notNull(beanType);
        Assert.notNull(triplestoreQueryUrl, "Triple store query URL must be specified");
        List<T> beans = new ArrayList<>();
        logger.debug("Query: {}", rdfQuery);
        try (QueryExecution queryExecution = QueryExecutionFactory.sparqlService(triplestoreQueryUrl, rdfQuery)) {
            ResultSet results = queryExecution.execSelect();
            while (results.hasNext()) {
                List<String> resultVars = rdfQuery.getResultVars();
                Resource queryResultResource = getFirstAvailableResource(results.next(), resultVars);
                if (queryResultResource == null) {
                    throw new IllegalStateException("Query solution has no resource for variables " + Arrays.toString(resultVars.toArray(new String[resultVars.size()])));
                }
                String path = parsePathFromUri(queryResultResource.getURI());
                try {
                    if (repository.exists(path)) {
                        beans.add(fedoraConverter.read(beanType, repository.getObject(path)));
                    } else {
                        throw new IllegalStateException("Resource with path " + path + " was found in the triplestore but it does not exist in the repository");
                    }
                } catch (FedoraException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return beans;
    }

    @Override
    public long count(Query rdfQuery) {
        long number = -1;
        logger.debug("Query (count): {}", rdfQuery);
        try (QueryExecution queryExecution = QueryExecutionFactory.sparqlService(triplestoreQueryUrl, rdfQuery)) {
            ResultSet results = queryExecution.execSelect();
            if (results.hasNext()){
               number = Long.parseLong(results.next().getLiteral("count").getLexicalForm());
            }
        }
        logger.debug("Query (count) result: {}", number);
        return number;
    }

    @Override
    public <T, ID> void delete(ID id, Class<T> beanType) {
        new TransactionExecution(){
            @Override
            public void doInTransaction() throws FedoraException {
                FedoraObject fedoraObject = fedoraConverter.getFedoraObject(id, beanType);
                if (fedoraObject != null){
                    fedoraObject.forceDelete();
                }
            }
        }.execute();
    }

    private void registerPersistenceExceptionTranslator() {
        if (applicationContext instanceof ConfigurableApplicationContext) {
            if (applicationContext.getBeansOfType(PersistenceExceptionTranslator.class).isEmpty()) {
                ((ConfigurableApplicationContext) applicationContext).getBeanFactory()
                        .registerSingleton("fedoraExceptionTranslator", EXCEPTION_TRANSLATOR);
            }
        }
    }

    private Resource getFirstAvailableResource(QuerySolution querySolution, List<String> varNames) {
        Resource resource = null;
        for (String varName : varNames) {
            resource = querySolution.getResource(varName);
            if (resource != null) {
                break;
            }
        }
        return resource;
    }

    private String parsePathFromUri(String uri) {
        return StringUtils.removeStart(uri, repository.getRepositoryUrl());
    }

    private void handleException(Exception e) {
        RuntimeException wrapped = new RuntimeException(e);
        DataAccessException dae = EXCEPTION_TRANSLATOR.translateExceptionIfPossible(wrapped);
        throw dae != null ? dae : wrapped;
    }

}
