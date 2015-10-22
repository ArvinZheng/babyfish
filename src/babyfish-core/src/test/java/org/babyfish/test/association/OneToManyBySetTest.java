/*
 * BabyFish, Object Model Framework for Java and JPA.
 * https://github.com/babyfish-ct/babyfish
 *
 * Copyright (c) 2008-2015, Tao Chen
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * Please visit "http://opensource.org/licenses/LGPL-3.0" to know more.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 */
package org.babyfish.test.association;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Assert;

import org.babyfish.association.AssociatedEndpoint;
import org.babyfish.association.AssociatedEndpointType;
import org.babyfish.association.AssociatedReference;
import org.babyfish.association.AssociatedSet;
import org.babyfish.collection.MAHashSet;
import org.babyfish.collection.MALinkedHashSet;
import org.babyfish.collection.MASet;
import org.babyfish.collection.MATreeSet;
import org.babyfish.collection.UnifiedComparator;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class OneToManyBySetTest {
    
    private TestSite.EmployeeSetFactory[] EMPLOYEE_SET_FACTORIES =
        new TestSite.EmployeeSetFactory[] {
            new TestSite.EmployeeSetFactory() {
                @Override
                public MASet<TestSite.Employee> createEmployeeSet() {
                    return new MAHashSet<TestSite.Employee>();
                }
            },
            new TestSite.EmployeeSetFactory() {
                @Override
                public MASet<TestSite.Employee> createEmployeeSet() {
                    return new MALinkedHashSet<TestSite.Employee>();
                }
            },
            new TestSite.EmployeeSetFactory() {
                @Override
                public MASet<TestSite.Employee> createEmployeeSet() {
                    return new MATreeSet<TestSite.Employee>();
                }
            }
        };
    
    private TestSite[] testSites;
    
    @Before
    public void initalize() {
        TestSite[] sites = new TestSite[EMPLOYEE_SET_FACTORIES.length];
        int index = 0;
        for (TestSite.EmployeeSetFactory employeeSetFactory : EMPLOYEE_SET_FACTORIES) {
            sites[index++] = new TestSite(employeeSetFactory);
        }
        this.testSites = sites;
    }
    
    @Test
    public void testModiyDepartment() {
        for (TestSite testSite : this.testSites) {
            testSite.testModifyDepartment();
        }
    }
    
    @Test
    public void testModiyEmloyee() {
        for (TestSite testSite : this.testSites) {
            testSite.testModifyEmployee();
        }
    }

    private static class TestSite {
        
        private EmployeeSetFactory employeeSetFactory;
        
        TestSite(EmployeeSetFactory employeeSetFactory) {
            this.employeeSetFactory = employeeSetFactory;
        }

        interface EmployeeSetFactory {
            
            MASet<Employee> createEmployeeSet();
            
        }
        
        @Test
        public void testModifyDepartment() {
            
            Department department = this.new Department();
            Employee employee = new Employee();
            Department newDepartment = this.new Department();
            assertSet(department.getEmployees());
            Assert.assertNull(employee.getDepartment());
            assertSet(newDepartment.getEmployees());
            
            department.getEmployees().add(employee);
            assertSet(department.getEmployees(), employee);
            Assert.assertEquals(department, employee.getDepartment());
            assertSet(newDepartment.getEmployees());
            
            newDepartment.getEmployees().add(employee);
            assertSet(department.getEmployees());
            Assert.assertEquals(newDepartment, employee.getDepartment());
            assertSet(newDepartment.getEmployees(), employee);
            
            Iterator<Employee> itr = newDepartment.getEmployees().iterator();
            itr.next();
            itr.remove();
            assertSet(department.getEmployees());
            Assert.assertNull(employee.getDepartment());
            assertSet(newDepartment.getEmployees());
        }
        
        @Test
        public void testModifyEmployee() {
            
            Department department = this.new Department();
            Employee employee = new Employee();
            Department newDepartment = this.new Department();
            assertSet(department.getEmployees());
            Assert.assertNull(employee.getDepartment());
            assertSet(newDepartment.getEmployees());
            
            employee.setDepartment(department);
            assertSet(department.getEmployees(), employee);
            Assert.assertEquals(department, employee.getDepartment());
            assertSet(newDepartment.getEmployees());
            
            employee.setDepartment(newDepartment);
            assertSet(department.getEmployees());
            Assert.assertEquals(newDepartment, employee.getDepartment());
            assertSet(newDepartment.getEmployees(), employee);
            
            employee.setDepartment(null);
            assertSet(department.getEmployees());
            Assert.assertNull(employee.getDepartment());
            assertSet(newDepartment.getEmployees());
        }
        
        @SuppressWarnings("unchecked")
        private static <E> void assertSet(Set<E> set, E ... elements) {
            Set<E> other = new HashSet<E>();
            for (E element : elements) {
                other.add(element);
            }
            Assert.assertEquals(other, set);
        }
        
        class Department {
            
            private AssociatedSet<Department, Employee> employees =
                new AssociatedSet<Department, Employee>() {
                
                    private static final long serialVersionUID = -8440585241587146463L;

                    public Department getOwner() {
                        return Department.this;
                    }
                    
                    @Override
                    protected AssociatedEndpointType onGetOppositeEndpointType() {
                        return AssociatedEndpointType.REFERENCE;
                    }

                    @Override
                    public AssociatedEndpoint<Employee, Department> onGetOppositeEndpoint(Employee opposite) {
                        return opposite.departmentReference;
                    }
    
                    @Override
                    protected RootData<Employee> createRootData() {
                        return new RootData<Employee>() {
                            
                            private static final long serialVersionUID = 3361901959842434898L;

                            @Override
                            protected MASet<Employee> createDefaultBase(
                                    UnifiedComparator<? super Employee> unifiedComparator) {
                                return TestSite.this.employeeSetFactory.createEmployeeSet();
                            }
                        };
                    }
                };
            
            Set<Employee> getEmployees() {
                return this.employees;
            }
            
        }
        
        static class Employee implements Comparable<Employee> {
            
            private final int id = AssociationTests.nextValOfSequence();
            
            private AssociatedReference<Employee, Department> departmentReference =
                new AssociatedReference<Employee, Department>() {
                
                    private static final long serialVersionUID = -2832996823733949096L;
                    
                    public Employee getOwner() {
                        return Employee.this;
                    }
                    
                    @Override
                    protected AssociatedEndpointType onGetOppositeEndpointType() {
                        return AssociatedEndpointType.SET;
                    }

                    @Override
                    public AssociatedEndpoint<Department, Employee> onGetOppositeEndpoint(Department opposite) {
                        return opposite.employees;
                    }
                };
            
            Department getDepartment() {
                return this.departmentReference.get();
            }
            
            void setDepartment(Department department) {
                this.departmentReference.set(department);
            }
    
            @Override
            public int compareTo(Employee o) {
                return this.id - o.id;
            }
            
        }
    }
    
}
