package org.babyfishdemo.jpacriteria.base;

import org.babyfish.hibernate.dialect.LimitedListDialect;
import org.hibernate.dialect.HSQLDialect;

/**
 * @author Tao Chen
 */
public class BadHSQLDialect extends HSQLDialect implements LimitedListDialect {
    
    @Override
    public int getMaxListLength() {
        return 10;
    }
}
