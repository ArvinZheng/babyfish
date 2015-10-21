package org.babyfishdemo.distinctlimitquery.base;

import java.util.List;

/**
 * @author Tao Chen
 */
public class LimitedResult<T> {

    private long unlimitedRowCount;
    
    private List<T> limitedRows;

    public LimitedResult(long unlimitedRowCount, List<T> limitedRows) {
        this.unlimitedRowCount = unlimitedRowCount;
        this.limitedRows = limitedRows;
    }

    public long getUnlimitedRowCount() {
        return this.unlimitedRowCount;
    }

    public List<T> getLimitedRows() {
        return this.limitedRows;
    }
}
