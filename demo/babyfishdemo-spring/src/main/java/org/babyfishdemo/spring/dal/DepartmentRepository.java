package org.babyfishdemo.spring.dal;

import org.babyfishdemo.spring.entities.Department;

/**
 * @author Tao Chen(&#38472;&#28059;)
 */
public interface DepartmentRepository {

    Department mergeDepartment(Department department);
    
    int deleteAllDepartments();
}
