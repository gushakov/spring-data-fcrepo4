package ch.unil.fcrepo4.spring.data.core.convert;

import ch.unil.fcrepo4.spring.data.core.mapping.DatastreamPersistentProperty;
import ch.unil.fcrepo4.utils.Utils;
import org.fcrepo.client.FedoraDatastream;
import org.fcrepo.client.FedoraObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author gushakov
 */
public class DatastreamDynamicProxyInterceptor extends AbstractDelegatingDynamicProxyInterceptor<FedoraDatastream> {
    private static final Logger logger = LoggerFactory.getLogger(DatastreamDynamicProxyInterceptor.class);

    private FedoraObject fedoraObject;

    private DatastreamPersistentProperty dsProp;

    public DatastreamDynamicProxyInterceptor(FedoraObject fedoraObject, DatastreamPersistentProperty dsProp, FedoraConverter fedoraConverter) {
        super(Utils.getDatastreamPath(fedoraObject, dsProp), FedoraDatastream.class, fedoraConverter);
        this.fedoraObject = fedoraObject;
        this.dsProp = dsProp;
    }

    @Override
    public void loadProxy() {
        delegate = fedoraConverter.readDatastream(fedoraObject, dsProp);
        logger.debug("Loaded delegate " + delegate +
                "for the datastream dynamic proxy with path " + Utils.getDatastreamPath(fedoraObject, dsProp));
    }
}
