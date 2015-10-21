package org.babyfishdemo.om4java.fec;

import java.util.Set;

import org.babyfish.collection.HashSet;
import org.babyfish.collection.MACollections;
import org.junit.Assert;
import org.junit.Test;
 
/**
 * @author Tao Chen
 */
public class FrozenEqualityComparatorTest {
 
    /*
     * "Department.getEmployees()" uses the "firstName" and "lastName" to calculate the hashCode 
     * of Employee and check whether two Employee objects are equal, but in order to keep the simplicity 
     * of our demo, the test code will NOT change the property "lastName" of Employee so that it always 
     * is null, ONLY the property "firstName" of Employee will be used by the test class.
     */
    @Test
    public void test() {
        Department department = new Department();
        Employee employee1 = new Employee();
        Employee employee2 = new Employee();
        Employee employee3 = new Employee();
        employee1.setFirstName("E-1");
        employee2.setFirstName("E-2");
        employee3.setFirstName("E-3");
        
        {
	        /*
	         * Validate the initialized state of those objects.
	         */
	        assertDepartment(department);
	        Assert.assertNull(employee1.getDepartment());
	        Assert.assertNull(employee2.getDepartment());
	        Assert.assertNull(employee3.getDepartment());
        }
        
        {
	        /*
	         * Add all the employees into the department,
	         * the property "department" of all employees will be changed automatically and implicitly.
	         */
	        department.getEmployees().addAll(MACollections.wrap(employee1, employee2, employee3));
	        assertDepartment(department, employee1, employee2, employee3);
	        Assert.assertSame(department, employee1.getDepartment());
	        Assert.assertSame(department, employee2.getDepartment());
	        Assert.assertSame(department, employee3.getDepartment());
        }
        
        {
	        /*
	         * Change the firstName of employee1 from "E-1" to "Boss",
	         * "department.getEmployees()" will be adjusted automatically.
	         *
	         * Unfortunately, XOrderedSet will not changed its order when it's adjusted 
	         * by "Unstable Collection Elements", it's not easy to demonstrate it.
	         * 
	         * But "contains" can demonstrate it easily.
	         */
	        employee1.setFirstName("Boss");
	        assertDepartment(department, employee1, employee2, employee3); // Changed automatically
	        Assert.assertTrue(department.getEmployees().contains(createEmployeeByFirstName("Boss")));
	        Assert.assertFalse(department.getEmployees().contains(createEmployeeByFirstName("E-1")));
	        Assert.assertSame(department, employee1.getDepartment());
	        Assert.assertSame(department, employee2.getDepartment());
	        Assert.assertSame(department, employee3.getDepartment());
        }
        
        {
	        /*
	         * Change the name of employee2 from "E-2" to "Boss",
	         * "department.getEmployees()" will be adjusted automatically.
	         * 
	         * Finally, employee1 whose firstName is "Boss" too
	         * will be removed from the collection 
	         * and 
	         * its property "department" will be set to be null 
	         * automatically and implicitly.
	         */
	        employee2.setFirstName("Boss");
	        
	        /*
	         * Changed automatically
	         * (1) employee1 is removed automatically
	         * (2) employee2 is refreshed automatically(removed and added again)
	         */
	        assertDepartment(department, employee3, employee2);
	        Assert.assertTrue(department.getEmployees().contains(createEmployeeByFirstName("Boss")));
	        Assert.assertFalse(department.getEmployees().contains(createEmployeeByFirstName("E-2")));
	        
	        Assert.assertNull(employee1.getDepartment()); 
	        Assert.assertSame(department, employee2.getDepartment());
	        Assert.assertSame(department, employee3.getDepartment());
        }
        
        {
	        /*
	         * Change the name of employee3 from "E-3" to "Boss",
	         * "department.getEmployees()" will be adjusted automatically.
	         * 
	         * Finally, employee2 whose firstName is "Boss" too
	         * will be removed from the collection 
	         * and 
	         * its property "department" will be set to be null 
	         * automatically and implicitly.
	         */
	        employee3.setFirstName("Boss");
	        
	        /*
	         * Changed automatically
	         * (1) employee2 is removed automatically
	         * (2) employee3 is refreshed automatically(removed and added again)
	         */
	        assertDepartment(department, employee3);
	        Assert.assertTrue(department.getEmployees().contains(createEmployeeByFirstName("Boss")));
	        Assert.assertFalse(department.getEmployees().contains(createEmployeeByFirstName("E-3")));
	        
	        Assert.assertNull(employee1.getDepartment());
	        Assert.assertNull(employee2.getDepartment()); 
	        Assert.assertSame(department, employee3.getDepartment());
        }
    }
    
    private static void assertDepartment(Department department, Employee ... employees) {
        Assert.assertEquals(employees.length, department.getEmployees().size());
        Set<Employee> set = new HashSet<>((employees.length * 4 + 2) / 3);
        for (Employee employee : employees) {
            set.add(employee);
        }
        Assert.assertEquals(set, department.getEmployees());
    }
    
    private static Employee createEmployeeByFirstName(String firstName) {
    	Employee employee = new Employee();
    	employee.setFirstName(firstName);
    	return employee;
    }
}
