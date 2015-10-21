package org.babyfishdemo.querypath;

import java.util.List;

import org.babyfish.persistence.XEntityManager;
import org.babyfishdemo.querypath.entities.Employee;
import org.babyfishdemo.querypath.entities.Employee__;

/**
 * @author Tao Chen
 */
public class EmployeeRepository {

    public List<Employee> getEmployees(Employee__ ... queryPaths) {
        
        // Close the entity manager soon, say goodbye for spring-open-session-in-view
        // (In real project, please close the entity manager in services, not repositories)
        try (XEntityManager em = JPAContext.createEntityManager()) {
            return 
                    em
                    .createQuery("select e from Employee e", Employee.class)
                    .setQueryPaths(queryPaths)
                    .getResultList();
        }
    }
}
