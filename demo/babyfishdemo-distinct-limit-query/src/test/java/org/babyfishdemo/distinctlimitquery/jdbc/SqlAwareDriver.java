package org.babyfishdemo.distinctlimitquery.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import org.babyfish.lang.Arguments;

/**
 * @author Tao Chen
 */
public abstract class SqlAwareDriver implements Driver {
    
    private Driver targetDriver;
    
    protected SqlAwareDriver(Driver targetDriver) {
        this.targetDriver = Arguments.mustNotBeNull("targetDriver", targetDriver);
    }
    
    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        return SqlAwareUtil.wrap(
                this.targetDriver.connect(
                        url.replace(":sqlaware:", ":"), 
                        info
                )
        );
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return url.startsWith("jdbc:sqlaware:");
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return this.targetDriver.getPropertyInfo(url, info);
    }

    @Override
    public int getMajorVersion() {
        return this.targetDriver.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return this.targetDriver.getMajorVersion();
    }

    @Override
    public boolean jdbcCompliant() {
        return this.targetDriver.jdbcCompliant();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return this.targetDriver.getParentLogger();
    }
}
