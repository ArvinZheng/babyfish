package org.babyfishdemo.spring.dal.base;

import javax.sql.DataSource;

import org.babyfishdemo.spring.dal.jdbc.SqlAwareUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * @author Tao Chen
 */
@Configuration
@ImportResource("classpath:data-access-layer.xml")
public class SpringConfiguration {

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        String oracle = System.getProperty("oracle");
        if (oracle != null) {
            // For eclipse run/debug configuration, "-Doracle" means 
            // empty string, but for shell, it means "-Doracle=true"
            if (!oracle.startsWith("jdbc:oracle:")) { 
                oracle = "jdbc:oracle:thin:@localhost:1521:babyfish";
            }
            String oracleUser = System.getProperty("oracle.user", "babyfish_demo");
            String oraclePassword = System.getProperty("oracle.password", "123");
            dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
            dataSource.setUrl(oracle);
            dataSource.setUsername(oracleUser);
            dataSource.setPassword(oraclePassword);
        } else {
            dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
            dataSource.setUrl("jdbc:hsqldb:mem:babyfishdemo-spring");
            dataSource.setUsername("sa");
        }
        return SqlAwareUtil.wrap(dataSource);
    }
    
    @Bean
    public String hibernateDialect() {
        return System.getProperty("oracle") != null ?
                
                // Oracle: This is babyfish's dialect, not hibernate's dialect
                "org.babyfish.hibernate.dialect.Oracle10gDialect" :
                    
                // HSQLDB
                "org.hibernate.dialect.HSQLDialect";
    }
}
