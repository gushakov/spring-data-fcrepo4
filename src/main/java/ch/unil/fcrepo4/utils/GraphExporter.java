package ch.unil.fcrepo4.utils;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.query.DatasetAccessorFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.apache.commons.lang3.SystemUtils;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.FedoraResource;
import org.fcrepo.client.impl.FedoraObjectImpl;
import org.fcrepo.client.impl.FedoraRepositoryImpl;

import java.io.FileOutputStream;
import java.util.Collection;

/**
 * @author gushakov
 */
public class GraphExporter {

    private static GraphExporter instance;

    public static GraphExporter getInstance() {
        if (instance == null) {
            instance = new GraphExporter();
        }

        return instance;
    }

    private GraphExporter() {
    }

    public void exportToFile(String rootPath, String fedoraRepositoryUrl, String dirPath) throws Exception {
        FedoraRepository fedoraRepository = new FedoraRepositoryImpl(fedoraRepositoryUrl);
        FedoraObjectImpl fedoraObject = (FedoraObjectImpl) fedoraRepository.getObject(rootPath);
        Graph graph = fedoraObject.getGraph();
        addTriples(graph, fedoraObject.getChildren("Resource"));
        Model model = ModelFactory.createModelForGraph(graph);
//        RDFDataMgr.write(System.out, model, Lang.TURTLE);
        try (FileOutputStream fos = new FileOutputStream(dirPath + SystemUtils.FILE_SEPARATOR + "graph_" + System.currentTimeMillis() + ".ttl")) {
            RDFDataMgr.write(fos, model, Lang.TURTLE);
        }
    }

    // based on code from http://pinesong.ghost.io/how-to-upload-rdf-file-to-jena-fuseki-server-using-java-code/

    public void exportToFuseki(String rootPath, String fedoraRepositoryUrl, String fusekiDataServiceUri) throws FedoraException {

        FedoraRepository fedoraRepository = new FedoraRepositoryImpl(fedoraRepositoryUrl);
        FedoraObjectImpl fedoraObject = (FedoraObjectImpl) fedoraRepository.getObject(rootPath);
        Graph graph = fedoraObject.getGraph();
        addTriples(graph, fedoraObject.getChildren("Resource"));
        Model model = ModelFactory.createModelForGraph(graph);
        DatasetAccessor datasetAccessor = DatasetAccessorFactory.createHTTP(fusekiDataServiceUri);
        datasetAccessor.putModel(model);
    }

    private void addTriples(Graph graph, Collection<FedoraResource> fedoraResources) throws FedoraException {
        if (fedoraResources.isEmpty()) {
            return;
        }

        for (FedoraResource resource : fedoraResources) {
            Utils.triplesStream(resource.getProperties()).forEach(graph::add);
            if (resource instanceof FedoraObject) {
                addTriples(graph, ((FedoraObject) resource).getChildren("Resource"));
            }
        }
    }
}
