package ch.unil.fcrepo4.spring.data.core;

import ch.unil.fcrepo4.spring.data.core.convert.FedoraConverter;
import ch.unil.fcrepo4.spring.data.core.convert.FedoraMappingConverter;
import org.apache.commons.lang3.StringUtils;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.impl.FedoraRepositoryImpl;
import org.modeshape.jcr.query.model.Limit;
import org.modeshape.jcr.query.model.Query;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gushakov
 */
public class FedoraTemplate implements FedoraOperations, InitializingBean, ApplicationContextAware {
    private ApplicationContext applicationContext;

    private FedoraConverter fedoraConverter;

    private String fedoraHost;

    private int fedoraPort;

    private FedoraRepository repository;

    private RestTemplate restTemplate;

    private static final FedoraExceptionTranslator EXCEPTION_TRANSLATOR = new FedoraExceptionTranslator();

    public FedoraTemplate(String fedoraHost, int fedoraPort) {
        Assert.hasLength(fedoraHost);
        Assert.isTrue(fedoraPort > 0);
        this.fedoraHost = fedoraHost;
        this.fedoraPort = fedoraPort;
        this.repository = new FedoraRepositoryImpl("http://" + fedoraHost + ":" + fedoraPort + "/rest");
        this.restTemplate = new RestTemplate();
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

    public FedoraRepository getRepository() {
        return repository;
    }

    // for testing
    RestTemplate getRestTemplate(){
        return restTemplate;
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
    public <T, ID> void delete(ID id, Class<T> beanType) {
        new TransactionExecution() {
            @Override
            public void doInTransaction() throws FedoraException {
                FedoraObject fedoraObject = fedoraConverter.getFedoraObject(id, beanType);
                if (fedoraObject != null) {
                    fedoraObject.forceDelete();
                }
            }
        }.execute();
    }

    @Override
    public <T> List<T> query(Query jcrSqlQuery, Class<T> beanType) {
        Assert.notNull(jcrSqlQuery);
        return processJcrSqlQueryQueryResponse(executeJcrSqlQuery(jcrSqlQuery), beanType);
    }

    @Override
    public <T> Page<T> queryForPage(Query jcrSqlQuery, Class<T> beanType) {
        Limit limits = jcrSqlQuery.getLimits();
        Assert.state(!limits.isUnlimited());
        int pageSize = limits.getRowLimit();
        int page = limits.getOffset() / pageSize;
        return new PageImpl<>(processJcrSqlQueryQueryResponse(executeJcrSqlQuery(jcrSqlQuery), beanType),
                new PageRequest(page, pageSize), Integer.MAX_VALUE);
    }

    private ResponseEntity<String[]> executeJcrSqlQuery(Query jcrSqlQuery){
        HttpHeaders headers = new HttpHeaders();
        headers.set("queryString", jcrSqlQuery.toString());
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        return restTemplate.exchange("http://" + fedoraHost + ":" + fedoraPort + "/query",
                HttpMethod.GET, httpEntity, String[].class);
    }

    private <T> List<T> processJcrSqlQueryQueryResponse(ResponseEntity<String[]> responseEntity, Class<T> beanType){
        List<T> beans = new ArrayList<>();
        for (String jcrPath: responseEntity.getBody()){
            try {
                beans.add(fedoraConverter.read(beanType, repository.getObject(jcrPath)));
            } catch (FedoraException e) {
                throw new RuntimeException(e);
            }
        }
        return beans;
    }

    private void registerPersistenceExceptionTranslator() {
        if (applicationContext instanceof ConfigurableApplicationContext) {
            if (applicationContext.getBeansOfType(PersistenceExceptionTranslator.class).isEmpty()) {
                ((ConfigurableApplicationContext) applicationContext).getBeanFactory()
                        .registerSingleton("fedoraExceptionTranslator", EXCEPTION_TRANSLATOR);
            }
        }
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
