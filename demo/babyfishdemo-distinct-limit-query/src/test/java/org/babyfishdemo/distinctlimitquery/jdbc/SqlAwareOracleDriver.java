package org.babyfishdemo.distinctlimitquery.jdbc;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.babyfish.lang.UncheckedException;

/**
 * @author Tao Chen
 */
public class SqlAwareOracleDriver extends SqlAwareDriver {

    public SqlAwareOracleDriver() {
        super(createRawDriver());
    }

    private static Driver createRawDriver() {
        try {
            Class<?> clazz = Class.forName("oracle.jdbc.OracleDriver");
            return (Driver)clazz.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            throw UncheckedException.rethrow(ex);
        }
    }

    static {
        try {
            DriverManager.registerDriver(new SqlAwareOracleDriver());
        } catch (SQLException ex) {
            UncheckedException.rethrow(ex);
        }
    }
}
