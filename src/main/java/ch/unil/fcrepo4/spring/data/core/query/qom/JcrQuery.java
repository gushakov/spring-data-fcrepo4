package ch.unil.fcrepo4.spring.data.core.query.qom;

import org.springframework.data.util.TypeInformation;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.QueryResult;
import javax.jcr.version.VersionException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author gushakov
 */
public class JcrQuery implements Query {

    private Map<TypeInformation<?>, Selector> selectors;

    private Constraint constraint;

    private Limit limit;

    public JcrQuery(Map<TypeInformation<?>, Selector> selectors, Constraint constraint, Limit limit) {
        this.selectors = selectors;
        this.constraint = constraint;
        this.limit = limit;
    }

    @Override
    public QueryResult execute() throws InvalidQueryException, RepositoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLimit(long limit) {
        this.limit = new LimitImpl(getOffset(), (int) limit);
    }

    @Override
    public void setOffset(long offset) {
        this.limit = new LimitImpl((int) offset, getRowLimit());
    }

    @Override
    public String getStatement() {
        return "SELECT DISTINCT * FROM " +
                printFrom() + " WHERE " +
                (constraint.isSimpleOrConjunctionsOnly() ? constraint : "(" + constraint + ")");
    }

    private String printFrom() {
        return selectors.values().stream().map(Object::toString).collect(Collectors.joining(", "));
    }

    @Override
    public String getLanguage() {
        return javax.jcr.query.Query.JCR_JQOM;
    }

    @Override
    public String getStoredQueryPath() throws ItemNotFoundException, RepositoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node storeAsNode(String absPath) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void bindValue(String varName, Value value) throws IllegalArgumentException, RepositoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getBindVariableNames() throws RepositoryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return getStatement();
    }

    @Override
    public int getOffset() {
        return limit.getOffset();
    }

    @Override
    public int getRowLimit() {
        return limit.getRowLimit();
    }
}
