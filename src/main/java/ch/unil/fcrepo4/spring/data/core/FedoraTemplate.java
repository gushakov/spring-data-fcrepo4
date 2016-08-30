package ch.unil.fcrepo4.spring.data.core;

import ch.unil.fcrepo4.client.FedoraClientRepository;
import ch.unil.fcrepo4.client.FedoraClientRepositoryImpl;
import ch.unil.fcrepo4.client.FedoraException;
import ch.unil.fcrepo4.client.FedoraObject;
import ch.unil.fcrepo4.spring.data.core.convert.FedoraConverter;
import ch.unil.fcrepo4.spring.data.core.convert.FedoraMappingConverter;
import ch.unil.fcrepo4.spring.data.core.query.FedoraPageRequest;
import ch.unil.fcrepo4.spring.data.core.query.FedoraQuery;
import ch.unil.fcrepo4.spring.data.core.query.result.FedoraResultPage;
import ch.unil.fcrepo4.utils.UriBuilder;
import ch.unil.fcrepo4.utils.Utils;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Resource;
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

    private String fedoraHost;

    private int fedoraPort;

    private String fedoraPath;

    private String triplestoreHost;

    private int triplestorePort;

    private String triplestorePath;

    private String triplestoreDb;

    private FedoraClientRepository repository;

    private static final FedoraExceptionTranslator EXCEPTION_TRANSLATOR = new FedoraExceptionTranslator();

    /**
     * Assumes default settings as set in <a href="https://github.com/fcrepo4-exts/fcrepo4-vagrant">fcrepo4-vagrant</a>
     * project: <ul> <li>Fedora: http://localhost:8080/fcrepo</li> <li>Fuseki: http://localhost:8080/fuseki, databse:
     * "test"</li> </ul>
     */
    public FedoraTemplate() {
        this.fedoraHost = "localhost";
        this.fedoraPort = 8080;
        this.fedoraPath = "/fcrepo";
        this.triplestoreHost = "localhost";
        this.triplestorePort = 8080;
        this.triplestorePath = "/fuseki";
        this.triplestoreDb = "test";
    }

    public FedoraTemplate(String fedoraHost, int fedoraPort, String fedoraPath,
                          String triplestoreHost, int triplestorePort, String triplestorePath, String triplestoreDb) {
        this.fedoraHost = fedoraHost;
        this.fedoraPort = fedoraPort;
        this.fedoraPath = fedoraPath;
        this.triplestoreHost = triplestoreHost;
        this.triplestorePort = triplestorePort;
        this.triplestorePath = triplestorePath;
        this.triplestoreDb = triplestoreDb;
    }

    public FedoraClientRepository getRepository() {
        return repository;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initClientRepository();

        if (fedoraConverter == null) {
            // get the default FedoraConverter
            fedoraConverter = new FedoraMappingConverter(repository);
        }

        registerPersistenceExceptionTranslator();
    }

    private void initClientRepository() {

        // build client repository URL
        String fedoraUrl = new UriBuilder()
                .setScheme("http")
                .setHost(fedoraHost)
                .setPort(fedoraPort)
                .setPath(Utils.normalize(fedoraPath, "rest"))
                .build().toString();
        repository = new FedoraClientRepositoryImpl(fedoraUrl);

    }

    @Override
    public FedoraConverter getConverter() {
        return fedoraConverter;
    }

    @Override
    public <T> void save(T bean) {
        FedoraObject fedoraObject = fedoraConverter.getFedoraObject(bean);
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
        String queryUrl = new UriBuilder().setScheme("http")
                .setHost(triplestoreHost)
                .setPort(triplestorePort)
                .setPath(Utils.normalize(triplestorePath, triplestoreDb, "query")).build().toString();

        List<T> beans = new ArrayList<>();
        Query sparqlQuery = QueryFactory.create(query.getSerialized());
        logger.debug("Query: {}", query);
        try (QueryExecution queryExecution = QueryExecutionFactory.sparqlService(queryUrl, sparqlQuery)) {
            ResultSet results = queryExecution.execSelect();
            while (results.hasNext()) {
                List<String> resultVars = sparqlQuery.getResultVars();
                Resource queryResultResource = getFirstAvailableResource(results.next(), resultVars);
                if (queryResultResource == null) {
                    throw new IllegalStateException("Query solution has no resource for variables " + Arrays.toString(resultVars.toArray(new String[resultVars.size()])));
                }

                String path = Utils.relativePath(repository.getRepositoryUrl(), queryResultResource.getURI());
                logger.debug("Found resource: {}", path);

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
    public <T> Page<T> queryForPage(FedoraQuery query, Class<T> beanType) {
        return new FedoraResultPage<>(query(query, beanType), new FedoraPageRequest((int) query.getOffset(), (int) query.getLimit()));
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
        if (dae != null) {
            throw dae;
        } else {
            throw wrapped;
        }
    }

}
