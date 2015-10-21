package org.babyfishdemo.war3shop.dal;

import java.util.List;

import org.babyfishdemo.war3shop.entities.Role;
import org.babyfishdemo.war3shop.entities.Role__;

/**
 * @author Tao Chen
 */
public interface RoleRepository extends AbstractRepository<Role, Long> {

    Role getRoleById(Long id, Role__ ... queryPaths);
    
    List<Role> getRolesByIds(Iterable<Long> ids, Role__ ... queryPaths);

    List<Role> getAllRoles(Role__ ... queryPaths);

    Role getRoleByNameInsensitively(String name, Role__ ... queryPaths);
}
