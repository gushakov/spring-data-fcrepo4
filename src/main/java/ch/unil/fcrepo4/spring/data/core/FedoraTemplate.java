package ch.unil.fcrepo4.spring.data.core;

import ch.unil.fcrepo4.spring.data.core.convert.FedoraConverter;
import ch.unil.fcrepo4.spring.data.core.convert.FedoraMappingConverter;
import ch.unil.fcrepo4.spring.data.core.query.FedoraQuery;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.impl.FedoraRepositoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @author gushakov
 */
public class FedoraTemplate implements FedoraOperations, InitializingBean, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(FedoraTemplate.class);

    private ApplicationContext applicationContext;

    private FedoraConverter fedoraConverter;

    private String fedoraHost;

    private int fedoraPort;

    private int triplestorePort;

    private FedoraRepository repository;

    private static final FedoraExceptionTranslator EXCEPTION_TRANSLATOR = new FedoraExceptionTranslator();

    public FedoraTemplate(String fedoraHost, int fedoraPort, int triplestorePort) {
        Assert.hasLength(fedoraHost);
        Assert.isTrue(fedoraPort > 0);
        this.fedoraHost = fedoraHost;
        this.fedoraPort = fedoraPort;
        this.triplestorePort = triplestorePort;
        this.repository = new FedoraRepositoryImpl("http://" + fedoraHost + ":" + fedoraPort + "/rest");
    }


    public FedoraRepository getRepository() {
        return repository;
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
        FedoraObject fedoraObject = fedoraConverter.getFedoraObject(bean);
        fedoraConverter.updateIndex(fedoraObject);
        fedoraConverter.write(bean, fedoraObject);
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
        FedoraObject fedoraObject = fedoraConverter.getFedoraObject(id, beanType);
        if (fedoraObject != null) {
            try {
                fedoraObject.forceDelete();
            } catch (FedoraException e) {
                handleException(e);
            }
        }
    }

    @Override
    public <T> List<T> query(FedoraQuery query, Class<T> beanType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Page<T> queryForPage(FedoraQuery query, Class<T> beanType) {
        throw new UnsupportedOperationException();
    }


    // test, maybe move to operations
    public <T> List<T> query(String query, Class<T> beanType){
        throw new UnsupportedOperationException();
    }



    private void registerPersistenceExceptionTranslator() {
        if (applicationContext instanceof ConfigurableApplicationContext) {
            if (applicationContext.getBeansOfType(PersistenceExceptionTranslator.class).isEmpty()) {
                ((ConfigurableApplicationContext) applicationContext).getBeanFactory()
                        .registerSingleton("fedoraExceptionTranslator", EXCEPTION_TRANSLATOR);
            }
        }
    }

    private void handleException(Exception e) {
        RuntimeException wrapped = new RuntimeException(e);
        DataAccessException dae = EXCEPTION_TRANSLATOR.translateExceptionIfPossible(wrapped);
        throw dae != null ? dae : wrapped;
    }

}
