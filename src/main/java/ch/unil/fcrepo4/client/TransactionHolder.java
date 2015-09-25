package ch.unil.fcrepo4.client;

/**
 * Holds id of the transaction associated with current thread in a {@linkplain ThreadLocal} variable.
 * @author gushakov
 *
 * @see ThreadLocal
 */
public class TransactionHolder {

    private static final ThreadLocal<String> threadLocalVar = new ThreadLocal<>();

    public static String getCurrentTransactionId() {
        return threadLocalVar.get();
    }

    public static void setCurrentTransactionId(String txId) {
        threadLocalVar.set(txId);
    }

    public static void removeCurrentTransactionId() {
        threadLocalVar.remove();
    }

}
