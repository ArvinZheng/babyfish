package org.babyfishdemo.distinctlimitquery.base;

import org.babyfish.junit.Filter;

/**
 * @author Tao Chen
 */
public class OracleFilter implements Filter {

    private boolean usingOracle;
    
    public OracleFilter() {
        this.usingOracle = System.getProperty("oracle") != null;
    }
    
    @Override
    public boolean accept() {
        return this.usingOracle;
    }
}
