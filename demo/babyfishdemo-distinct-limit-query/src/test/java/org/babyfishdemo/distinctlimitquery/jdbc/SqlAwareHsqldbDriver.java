package org.babyfishdemo.distinctlimitquery.jdbc;

import java.sql.DriverManager;
import java.sql.SQLException;

import org.babyfish.lang.UncheckedException;

/**
 * @author Tao Chen
 */
public class SqlAwareHsqldbDriver extends SqlAwareDriver {

    public SqlAwareHsqldbDriver() {
        super(new org.hsqldb.jdbcDriver());
    }

    static {
        try {
            DriverManager.registerDriver(new SqlAwareHsqldbDriver());
        } catch (SQLException ex) {
            UncheckedException.rethrow(ex);
        }
    }
}
