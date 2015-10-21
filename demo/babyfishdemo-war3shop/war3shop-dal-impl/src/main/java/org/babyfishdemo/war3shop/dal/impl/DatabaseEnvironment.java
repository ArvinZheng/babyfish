package org.babyfishdemo.war3shop.dal.impl;

import javax.annotation.PostConstruct;

import org.springframework.orm.jpa.vendor.Database;

/**
 * @author Tao Chen
 */
public class DatabaseEnvironment {

    private Database defaultDatabase;
    
    private String defaultDriverClassName;
    
    private String defaultUrl;
    
    private String defaultUsername;
    
    private String defaultPassword;

    private Database database;
    
    private String driverClassName;
    
    private String url;
    
    private String username;
    
    private String password;
    
    @PostConstruct
    public void initialize() {
        String oracle = System.getProperty("oracle");
        if (oracle != null) {
            // For eclipse run/debug configuration, "-Doracle" means 
            // empty string, but for shell, it means "-Doracle=true"
            if (!oracle.startsWith("jdbc:oracle:")) { 
                oracle = "jdbc:oracle:thin:@localhost:1521:babyfish";
            }
            String oracleUser = System.getProperty("oracle.user", "war3shop");
            String oraclePassword = System.getProperty("oracle.password", "123");
            this.database = Database.ORACLE;
            this.driverClassName = "oracle.jdbc.OracleDriver";
            this.url = oracle;
            this.username = oracleUser;
            this.password = oraclePassword;
        } else {
            this.database = this.defaultDatabase;
            this.driverClassName = this.defaultDriverClassName;
            this.url = this.defaultUrl;
            this.username = this.defaultUsername;
            this.password = this.defaultPassword;
        }
    }
    
    public Database getDatabase() {
        return this.database;
    }
    
    public String getDriverClassName() {
        return this.driverClassName;
    }
    
    public String getUrl() {
        return this.url;
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public String getPassword() {
        return this.password;
    }

    public void setDefaultDatabase(Database defaultDatabase) {
        this.defaultDatabase = defaultDatabase;
    }

    public void setDefaultDriverClassName(String defaultDriverClassName) {
        this.defaultDriverClassName = defaultDriverClassName;
    }

    public void setDefaultUrl(String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }

    public void setDefaultUsername(String defaultUsername) {
        this.defaultUsername = defaultUsername;
    }

    public void setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
    }
}
