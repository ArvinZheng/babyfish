package org.babyfishdemo.war3shop.web.json;

import org.babyfishdemo.war3shop.entities.AccountManager;
import org.babyfishdemo.war3shop.entities.Administrator;
import org.babyfishdemo.war3shop.entities.Customer;

import com.alibaba.fastjson.serializer.AfterFilter;

/**
 * @author Tao Chen
 */
public class PolymorphismFilter extends AfterFilter {
    
    public static final PolymorphismFilter INSTANCE = new PolymorphismFilter();
    
    private PolymorphismFilter() {
        
    }

    @Override
    public void writeAfter(Object object) {
        if (object instanceof Customer) {
            this.writeKeyValue("isCustomer", true);
        } else if (object instanceof Administrator) {
            this.writeKeyValue("isAdministrator", true);
        } else if (object instanceof AccountManager) {
            this.writeKeyValue("isAccountManager", true);
        }
    }
}
