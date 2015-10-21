package org.babyfishdemo.war3shop.bll;

import java.util.Map;

/**
 * @author Tao Chen
 */
public interface ConfigurationService {
    
    String SYS_EMAIL_PROTOCOL = "sys.email.protocol";
    
    String SYS_EMAIL_HOST = "sys.email.host";
    
    String SYS_EMAIL_SSL = "sys.email.ssl";
    
    String SYS_EMAIL_PORT = "sys.email.port";
    
    String SYS_EMAIL_USER = "sys.email.user";
    
    String SYS_EMAIL_PASSWORD = "*sys.email.password";

    String getVariable(String variableName);
    
    String getVariable(String variableName, String defaultValue);
    
    Map<String, String> getVariables(String ... variableNames);
    
    int getVariableInt(String variableName, int defaultValue);
    
    void setVariable(String variableName, String variableValue);
            
    void setSysMailConfiguration(
            Map<String, String> sysMailConfiguration,
            String password);
}
