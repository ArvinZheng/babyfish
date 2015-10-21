package org.babyfishdemo.spring.dal;

import org.babyfishdemo.spring.entities.Department;

/**
 * @author Tao Chen
 */
public interface DepartmentRepository {

    Department mergeDepartment(Department department);
    
    int deleteAllDepartments();
}
