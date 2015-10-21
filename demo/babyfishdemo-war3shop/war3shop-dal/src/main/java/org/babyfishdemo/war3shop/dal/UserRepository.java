package org.babyfishdemo.war3shop.dal;

import java.util.Collection;
import java.util.List;

import org.babyfishdemo.war3shop.entities.Administrator;
import org.babyfishdemo.war3shop.entities.Administrator__;
import org.babyfishdemo.war3shop.entities.Customer;
import org.babyfishdemo.war3shop.entities.Customer__;
import org.babyfishdemo.war3shop.entities.Page;
import org.babyfishdemo.war3shop.entities.User;
import org.babyfishdemo.war3shop.entities.User__;
import org.babyfishdemo.war3shop.entities.specification.AdministratorSpecification;
import org.babyfishdemo.war3shop.entities.specification.CustomerSpecification;

/**
 * @author Tao Chen
 */
public interface UserRepository extends AbstractRepository<User, Long> {
    
    User getUserById(long id, User__ ... queryPaths);
    
    Administrator getAdministratorById(long id, Administrator__ ... queryPaths);
    
    List<Administrator> getAdministratorsByIds(Collection<Long> ids, Administrator__ ... queryPaths);
    
    User getUserByNameInsensitively(String name, User__ ... queryPaths);
    
    Page<Customer> getCustomers(
            CustomerSpecification specification, 
            int pageIndex, 
            int pageSize, 
            Customer__ ... queryPaths);
    
    Page<Administrator> getAdministrators(
            AdministratorSpecification specification, 
            int pageIndex, 
            int pageSize, 
            Administrator__... queryPaths);
}
