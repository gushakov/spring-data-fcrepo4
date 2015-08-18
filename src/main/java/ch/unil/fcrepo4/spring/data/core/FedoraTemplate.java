package ch.unil.fcrepo4.spring.data.core;

import ch.unil.fcrepo4.spring.data.core.convert.FedoraConverter;
import ch.unil.fcrepo4.spring.data.core.convert.FedoraMappingConverter;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraRepository;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.dao.support.PersistenceExceptionTranslator;

/**
 * @author gushakov
 */
public class FedoraTemplate implements FedoraOperations, InitializingBean, ApplicationContextAware {
    private ApplicationContext applicationContext;

    private FedoraConverter fedoraConverter;

    private FedoraRepository repository;

//    private FedoraExceptionTranslator fedoraExceptionTranslator;

    public FedoraTemplate(FedoraRepository repository) {
        this.repository = repository;
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

    private void registerPersistenceExceptionTranslator() {
        if (applicationContext instanceof ConfigurableApplicationContext) {
            if (applicationContext.getBeansOfType(PersistenceExceptionTranslator.class).isEmpty()) {
                ((ConfigurableApplicationContext) applicationContext).getBeanFactory()
                        .registerSingleton("fedoraExceptionTranslator", FedoraExceptionTranslator.class);
            }
//            fedoraExceptionTranslator = applicationContext.getBean(FedoraExceptionTranslator.class);
        }
    }

    @Override
    public void save(Object source) {
         fedoraConverter.write(source);
    }

    @Override
    public <T> T load(String path, Class<T> beanType) {
        try {
            return fedoraConverter.read(beanType, repository.getObject(path));
        } catch (FedoraException e) {
            throw new RuntimeException(e);
        }
    }
}
