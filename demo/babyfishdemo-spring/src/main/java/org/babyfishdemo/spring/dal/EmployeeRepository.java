package org.babyfishdemo.spring.dal;

import java.util.List;

import org.babyfishdemo.spring.entities.Employee;
import org.babyfishdemo.spring.entities.Employee__;
import org.babyfishdemo.spring.model.EmployeeSpecification;
import org.babyfishdemo.spring.model.Page;

/**
 * @author Tao Chen
 */
public interface EmployeeRepository {
    
    List<Employee> getEmployees(
            EmployeeSpecification specification,
            Employee__ ... queryPaths);
    
    Page<Employee> getEmployees(
            EmployeeSpecification specification,
            int pageIndex,
            int pageSize,
            Employee__ ... queryPaths);
    
    Employee mergeEmployee(Employee employee);
    
    int deleteAllEmployees();
}
