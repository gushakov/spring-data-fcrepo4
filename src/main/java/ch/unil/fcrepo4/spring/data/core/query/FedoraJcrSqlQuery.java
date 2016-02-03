package ch.unil.fcrepo4.spring.data.core.query;

import org.modeshape.jcr.api.query.qom.QueryCommand;

/**
 * @author gushakov
 */
public class FedoraJcrSqlQuery implements FedoraQuery {
    private QueryCommand queryCommand;

    private boolean paged;

    public FedoraJcrSqlQuery(QueryCommand queryCommand, boolean paged) {
        this.queryCommand = queryCommand;
        this.paged = paged;
    }

    @Override
    public String getLanguage() {
        return FedoraQuery.JCR_SQL2;
    }

    @Override
    public String getSerialized() {
        if (isPaged() && getRowLimit() == Integer.MAX_VALUE) {
            return queryCommand.toString() + " LIMIT " + getRowLimit();
        }
        else {
            return queryCommand.toString();
        }
    }

    @Override
    public boolean isPaged() {
        return paged;
    }

    @Override
    public int getOffset() {
        return queryCommand.getLimits().getOffset();
    }

    @Override
    public int getRowLimit() {
        return queryCommand.getLimits().getRowLimit();
    }

    @Override
    public String toString() {
        return getSerialized();
    }
}
