package ch.unil.fcrepo4.spring.data.core.query.qom;

/**
 * @author gushakov
 */
public class LimitImpl implements Limit {

    private static final int UNLIMITED = -1;

    private int offset;

    private int rowLimit;

    public LimitImpl() {
        this.offset = 0;
        this.rowLimit = -1;
    }

    public LimitImpl(int offset, int rowLimit) {
        this.offset = offset;
        this.rowLimit = rowLimit;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public int getRowLimit() {
        return rowLimit;
    }

    @Override
    public boolean isUnlimited() {
        return rowLimit != UNLIMITED;
    }

    @Override
    public boolean isOffset() {
        return offset > 0;
    }
}
