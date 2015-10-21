package org.babyfishdemo.war3shop.dal;

import java.util.Collection;
import java.util.List;

import org.babyfishdemo.war3shop.entities.Privilege;
import org.babyfishdemo.war3shop.entities.Privilege__;

/**
 * @author Tao Chen
 */
public interface PrivilegeRepository {
    
    List<Privilege> getAllPrivileges(Privilege__ ... queryPaths);
    
    Privilege getPrivilegeByName(String name, Privilege__ ... queryPaths);

    List<Privilege> getPrivilegesByIds(Collection<Long> ids, Privilege__ ... queryPaths);
    
    List<Privilege> getPrivilegesByNamesAndAdministratorId(Collection<String> names, long administratorId, Privilege__ ...queryPaths);
    
    Privilege getPrivilegeByNameAndRoleIds(String name, Collection<Long> ids, Privilege__ ...queryPaths);
}
