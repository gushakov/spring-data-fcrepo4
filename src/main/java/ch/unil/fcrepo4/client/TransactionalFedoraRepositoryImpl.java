package ch.unil.fcrepo4.client;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.fcrepo.client.*;
import org.fcrepo.client.impl.FedoraRepositoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;

/**
 * Adds methods to create, commit, and rollback transactions in Fedora repository. Appends current transaction id to the
 * resource path of relevant methods. This functionality will probably be incorporated into {@linkplain
 * FedoraRepositoryImpl}, so it is here just for time being. The code is copied and modified from {@code
 * FedoraRepositoryImpl}.
 *
 * @author gushakov
 * @see FedoraRepositoryImpl
 * @see TransactionHolder
 */
public class TransactionalFedoraRepositoryImpl extends FedoraRepositoryImpl implements TransactionalFedoraRepository {

    private static final Logger logger = LoggerFactory.getLogger(TransactionalFedoraRepositoryImpl.class);

    private static final String TX = "/tx:";
    private static final String FCR_TX = "/fcr:tx";
    private static final String FCR_COMMIT = "/fcr:commit";
    private static final String FCR_ROLLBACK = "/fcr:rollback";

    public TransactionalFedoraRepositoryImpl(String repositoryURL) {
        super(repositoryURL);
    }

    /*
    curl -i -X POST "http://localhost:8080/rest/fcr:tx"
     */

    @Override
    public String startTransaction() throws FedoraException {
        HttpPost post = httpHelper.createPostMethod(FCR_TX, null);
        try {

            // template code copied from org.fcrepo.client.impl.FedoraRepositoryImpl

            final HttpResponse response = httpHelper.execute(post);
            final String uri = post.getURI().toString();
            final StatusLine status = response.getStatusLine();
            final int statusCode = status.getStatusCode();

            if (statusCode == SC_CREATED) {
                String txId = response.getFirstHeader("Location").getValue().substring(repositoryURL.length());
                TransactionHolder.setCurrentTransactionId(txId);
                logger.debug("Started transaction {}", txId);
                return txId;
            } else if (statusCode == SC_FORBIDDEN) {
                logger.error("request to start new transaction {} is not authorized.", uri);
                throw new ForbiddenException("request to start new transaction " + uri + " is not authorized.");
            } else {
                logger.error("error start new transaction {}: {} {}", uri, statusCode, status.getReasonPhrase());
                throw new FedoraException("error start new transaction " + uri + ": " + statusCode + " " +
                        status.getReasonPhrase());
            }


        } catch (final Exception e) {
            logger.error("could not encode URI parameter", e);
            throw new FedoraException(e);
        } finally {
            post.releaseConnection();
        }
    }

    /*
    curl -i -X POST "http://localhost:8080/rest/tx:123456789/fcr:tx/fcr:commit"
     */

    @Override
    public void commitTransaction() throws FedoraException {
        HttpPost post = httpHelper.createPostMethod(TransactionHolder.getCurrentTransactionId() + FCR_TX + FCR_COMMIT, null);
        try {

            // template code copied from org.fcrepo.client.impl.FedoraRepositoryImpl

            final HttpResponse response = httpHelper.execute(post);
            final String uri = post.getURI().toString();
            final StatusLine status = response.getStatusLine();
            final int statusCode = status.getStatusCode();

            if (statusCode == SC_FORBIDDEN) {
                logger.error("request to commit transaction {} is not authorized.", uri);
                throw new ForbiddenException("request to commit transaction " + uri + " is not authorized.");
            } else {
                if (statusCode != SC_NO_CONTENT) {
                    logger.error("error committing back transaction {}: {} {}", uri, statusCode, status.getReasonPhrase());
                    throw new FedoraException("error committing transaction " + uri + ": " + statusCode + " " +
                            status.getReasonPhrase());
                }
            }

            logger.debug("Committed transaction {}", TransactionHolder.getCurrentTransactionId());
        } catch (final Exception e) {
            logger.error("could not encode URI parameter", e);
            throw new FedoraException(e);
        } finally {
            post.releaseConnection();
            TransactionHolder.removeCurrentTransactionId();
        }
    }

        /*
    curl -i -X POST "http://localhost:8080/rest/tx:123456789/fcr:tx/fcr:rollback"

    204 No Content: if the transaction is discarded successfully
410 Gone: if the transaction doesn't exist

     */


    @Override
    public void rollbackTransaction() throws FedoraException {
        HttpPost post = httpHelper.createPostMethod(TransactionHolder.getCurrentTransactionId() + FCR_TX + FCR_ROLLBACK, null);
        try {

            // template code copied from org.fcrepo.client.impl.FedoraRepositoryImpl

            final HttpResponse response = httpHelper.execute(post);
            final String uri = post.getURI().toString();
            final StatusLine status = response.getStatusLine();
            final int statusCode = status.getStatusCode();

            if (statusCode == SC_FORBIDDEN) {
                logger.error("request to rollback transaction {} is not authorized.", uri);
                throw new ForbiddenException("request to rollback transaction " + uri + " is not authorized.");
            } else {
                if (statusCode != SC_NO_CONTENT) {
                    logger.error("error rolling back transaction {}: {} {}", uri, statusCode, status.getReasonPhrase());
                    throw new FedoraException("error rolling back transaction " + uri + ": " + statusCode + " " +
                            status.getReasonPhrase());
                }
            }

            logger.debug("Rolled back transaction {}", TransactionHolder.getCurrentTransactionId());
        } catch (final Exception e) {
            logger.error("could not encode URI parameter", e);
            throw new FedoraException(e);
        } finally {
            post.releaseConnection();
            TransactionHolder.removeCurrentTransactionId();
        }
    }

    @Override
    public boolean exists(String path) throws FedoraException {
        return super.exists(appendTransactionId(path));
    }

    @Override
    public FedoraDatastream getDatastream(String path) throws FedoraException {
        return super.getDatastream(appendTransactionId(path));
    }

    @Override
    public FedoraObject getObject(String path) throws FedoraException {
        return super.getObject(appendTransactionId(path));
    }

    @Override
    public FedoraDatastream createDatastream(String path, FedoraContent content) throws FedoraException {
        return super.createDatastream(appendTransactionId(path), content);
    }

    @Override
    public FedoraDatastream createOrUpdateRedirectDatastream(String path, String url) throws FedoraException {
        return super.createOrUpdateRedirectDatastream(appendTransactionId(path), url);
    }

    @Override
    public FedoraObject createObject(String path) throws FedoraException {
        return super.createObject(appendTransactionId(path));
    }

    @Override
    public FedoraObject createResource(String containerPath) throws FedoraException {
        return super.createResource(appendTransactionId(containerPath));
    }

    @Override
    public FedoraDatastream findOrCreateDatastream(String path) throws FedoraException {
        return super.findOrCreateDatastream(appendTransactionId(path));
    }

    @Override
    public FedoraObject findOrCreateObject(String path) throws FedoraException {
        return super.findOrCreateObject(appendTransactionId(path));
    }

    private String appendTransactionId(String path) {
        String txId = TransactionHolder.getCurrentTransactionId();
        return path.startsWith(TX) || txId == null ? path : txId + path;
    }

}
