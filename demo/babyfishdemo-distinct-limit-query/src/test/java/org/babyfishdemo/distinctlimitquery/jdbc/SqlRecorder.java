package org.babyfishdemo.distinctlimitquery.jdbc;

/**
 * @author Tao Chen
 */
public abstract class SqlRecorder implements AutoCloseable {

    private static final ThreadLocal<SqlRecorder> RECORDER_LOCAL = new ThreadLocal<>();

    private SqlRecorder prev;
    
    public SqlRecorder() {
        this.prev = RECORDER_LOCAL.get();
        RECORDER_LOCAL.set(this);
    }
    
    @Override
    public void close() {
        if (RECORDER_LOCAL.get() != this) {
            throw new IllegalStateException(
                    "The current recorder is not the youngest recorder of current thread"
            );
        }
        if (this.prev != null) {
            RECORDER_LOCAL.set(this.prev);
        } else {
            RECORDER_LOCAL.remove();
        }
    }

    protected abstract void prepareStatement(String sql);
    
    static void prepare(String sql) {
        SqlRecorder youngest = RECORDER_LOCAL.get();
        if (youngest != null) {
            youngest.prepareStatement(sql);
        }
    }
}
