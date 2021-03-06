package org.babyfishdemo.war3shop.web;

import java.util.Map;

import javax.annotation.Resource;

import org.babyfish.collection.LinkedHashMap;
import org.babyfishdemo.war3shop.bll.ConfigurationService;
import org.babyfishdemo.war3shop.web.json.JsonpModelAndView;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Tao Chen
 */
@Controller
@RequestMapping("/configuration")
public class ConfigurationController {

    @Resource
    private ConfigurationService configurationService;
    
    @RequestMapping("/sys-mail")
    public JsonpModelAndView sysMail() {
        Map<String, String> sysMailConfiguration =
                this.configurationService.getVariables(
                        ConfigurationService.SYS_EMAIL_PROTOCOL,
                        ConfigurationService.SYS_EMAIL_SSL,
                        ConfigurationService.SYS_EMAIL_HOST,
                        ConfigurationService.SYS_EMAIL_PORT,
                        ConfigurationService.SYS_EMAIL_USER
                );
        return new JsonpModelAndView(sysMailConfiguration);
    }
    
    @RequestMapping("/change-sys-mail")
    public JsonpModelAndView changeSysMail(
            @RequestParam("protocol") String protocol,
            @RequestParam("ssl") boolean ssl,
            @RequestParam("host") String host,
            @RequestParam("port") int port,
            @RequestParam("user") String user,
            @RequestParam(value = "password", required = false) String password) {
        Map<String, String> sysMailConfigurationMap = new LinkedHashMap<>();
        sysMailConfigurationMap.put(ConfigurationService.SYS_EMAIL_PROTOCOL, protocol);
        sysMailConfigurationMap.put(ConfigurationService.SYS_EMAIL_SSL, Boolean.toString(ssl));
        sysMailConfigurationMap.put(ConfigurationService.SYS_EMAIL_HOST, host);
        sysMailConfigurationMap.put(ConfigurationService.SYS_EMAIL_PORT, Integer.toString(port));
        sysMailConfigurationMap.put(ConfigurationService.SYS_EMAIL_USER, user);
        this.configurationService.setSysMailConfiguration(sysMailConfigurationMap, password);
        return new JsonpModelAndView(null);
    }
}
