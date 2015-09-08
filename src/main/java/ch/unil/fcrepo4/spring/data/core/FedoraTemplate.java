package ch.unil.fcrepo4.spring.data.core;

import ch.unil.fcrepo4.spring.data.core.convert.FedoraConverter;
import ch.unil.fcrepo4.spring.data.core.convert.FedoraMappingConverter;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Resource;
import org.apache.commons.lang3.StringUtils;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.impl.FedoraRepositoryImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author gushakov
 */
public class FedoraTemplate implements FedoraOperations, InitializingBean, ApplicationContextAware {
    private ApplicationContext applicationContext;

    private FedoraConverter fedoraConverter;

    private FedoraRepository repository;

    private String triplestoreQueryUrl;

    public FedoraTemplate(FedoraRepository repository) {
        Assert.notNull(repository);
        this.repository = repository;
    }

    public FedoraTemplate(String repositoryUrl) {
        Assert.notNull(repositoryUrl);
        this.repository = new FedoraRepositoryImpl(repositoryUrl);
    }

    public FedoraTemplate(String repositoryUrl, String triplestoreQueryUrl) {
        Assert.notNull(repositoryUrl);
        Assert.notNull(triplestoreQueryUrl);
        this.repository = new FedoraRepositoryImpl(repositoryUrl);
        this.triplestoreQueryUrl = triplestoreQueryUrl;

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
    public <T> String save(T bean) {
        FedoraObject fedoraObject = fedoraConverter.getFedoraObject(bean);
        fedoraConverter.write(bean, fedoraObject);
        try {
            return fedoraObject.getPath();
        } catch (FedoraException e) {
            throw new RuntimeException(e);
        }
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
        Assert.notNull(beanType);
        Assert.notNull(triplestoreQueryUrl, "Triple store query URL must be specified");
        List<T> beans = new ArrayList<>();
        try (QueryExecution queryExecution = QueryExecutionFactory.sparqlService(triplestoreQueryUrl, rdfQuery)) {
            ResultSet results = queryExecution.execSelect();
            if (results.hasNext()) {
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

    private void registerPersistenceExceptionTranslator() {
        if (applicationContext instanceof ConfigurableApplicationContext) {
            if (applicationContext.getBeansOfType(PersistenceExceptionTranslator.class).isEmpty()) {
                ((ConfigurableApplicationContext) applicationContext).getBeanFactory()
                        .registerSingleton("fedoraExceptionTranslator", FedoraExceptionTranslator.class);
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
}
