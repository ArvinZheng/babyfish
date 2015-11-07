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

import junit.framework.Assert;

import org.babyfish.association.AssociatedCollection;
import org.babyfish.association.AssociatedEndpoint;
import org.babyfish.association.AssociatedEndpointType;
import org.babyfish.association.AssociatedReference;
import org.babyfish.collection.MAArrayList;
import org.babyfish.collection.MACollection;
import org.babyfish.collection.MALinkedHashSet;
import org.babyfish.collection.MALinkedList;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.hibernate.collection.type.MAOrderedSetType;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class OneToManyByBagTest {
    
    private static final TestSite.EmployeeBagFactory[] EMPLOYE_BAG_FACTORIES =
        new TestSite.EmployeeBagFactory[] {
            new TestSite.EmployeeBagFactory() {
                @Override
                public MACollection<TestSite.Employee> createEmployeeBag() {
                    return new MAArrayList<TestSite.Employee>();
                }
            },
            new TestSite.EmployeeBagFactory() {
                @Override
                public MACollection<TestSite.Employee> createEmployeeBag() {
                    return new MALinkedList<TestSite.Employee>();
                }
            },
            new TestSite.EmployeeBagFactory() {
                @SuppressWarnings("unchecked")
                @Override
                public MACollection<TestSite.Employee> createEmployeeBag() {
                    return (MACollection<org.babyfish.test.association.OneToManyByBagTest.TestSite.Employee>)
                            new MAOrderedSetType().wrap(null, new MALinkedHashSet<TestSite.Employee>());
                }
            },
        };
    
    private TestSite[] testSites;
    
    @Before
    public void initialize() {
        TestSite[] sites = new TestSite[EMPLOYE_BAG_FACTORIES.length];
        int index = 0;
        for (TestSite.EmployeeBagFactory employeeBagFactory : EMPLOYE_BAG_FACTORIES) {
            sites[index++] = new TestSite(employeeBagFactory);
        }
        this.testSites = sites;
    }
    
    @Test
    public void testModifyDepartment() {
        for (TestSite testSite : this.testSites) {
            testSite.testModifyDepartment();
        }
    }
    
    @Test
    public void testModifyEmployee() {
        for (TestSite testSite : this.testSites) {
            testSite.testModifyEmployee();
        }
    }
    
    private static class TestSite {
        
        private EmployeeBagFactory employeeBagFactory;
        
        TestSite(EmployeeBagFactory employeeBagFactory) {
            this.employeeBagFactory = employeeBagFactory;
        }

        interface EmployeeBagFactory {
            MACollection<Employee> createEmployeeBag();
        }

        void testModifyDepartment() {
            
            Department department1 = this.new Department();
            Department department2 = this.new Department();
            Employee employee1 = new Employee();
            Employee employee2 = new Employee();
            AssociationTests.assertCollection(department1.getEmployees());
            AssociationTests.assertCollection(department2.getEmployees());
            Assert.assertNull(employee1.getDepartment());
            Assert.assertNull(employee2.getDepartment());
            
            department1.getEmployees().add(employee1);
            AssociationTests.assertCollection(department1.getEmployees(), employee1);
            AssociationTests.assertCollection(department2.getEmployees());
            Assert.assertEquals(department1, employee1.getDepartment());
            Assert.assertNull(employee2.getDepartment());
            
            department1.getEmployees().add(employee2);
            AssociationTests.assertCollection(department1.getEmployees(), employee1, employee2);
            AssociationTests.assertCollection(department2.getEmployees());
            Assert.assertEquals(department1, employee1.getDepartment());
            Assert.assertEquals(department1, employee2.getDepartment());
            
            department2.getEmployees().add(employee1);
            AssociationTests.assertCollection(department1.getEmployees(), employee2);
            AssociationTests.assertCollection(department2.getEmployees(), employee1);
            Assert.assertEquals(department2, employee1.getDepartment());
            Assert.assertEquals(department1, employee2.getDepartment());
            
            department2.getEmployees().add(employee2);
            AssociationTests.assertCollection(department1.getEmployees());
            AssociationTests.assertCollection(department2.getEmployees(), employee1, employee2);
            Assert.assertEquals(department2, employee1.getDepartment());
            Assert.assertEquals(department2, employee2.getDepartment());
            
            department2.getEmployees().remove(department2.getEmployees().iterator().next());
            AssociationTests.assertCollection(department1.getEmployees());
            AssociationTests.assertCollection(department2.getEmployees(), employee2);
            Assert.assertNull(employee1.getDepartment());
            Assert.assertEquals(department2, employee2.getDepartment());
            
            department2.getEmployees().remove(department2.getEmployees().iterator().next());
            AssociationTests.assertCollection(department1.getEmployees());
            AssociationTests.assertCollection(department2.getEmployees());
            Assert.assertNull(employee1.getDepartment());
            Assert.assertNull(employee2.getDepartment());
        }
        
        void testModifyEmployee() {
            
            Department department1 = this.new Department();
            Department department2 = this.new Department();
            Employee employee1 = new Employee();
            Employee employee2 = new Employee();
            AssociationTests.assertCollection(department1.getEmployees());
            AssociationTests.assertCollection(department2.getEmployees());
            Assert.assertNull(employee1.getDepartment());
            Assert.assertNull(employee2.getDepartment());
            
            employee1.setDepartment(department1);
            AssociationTests.assertCollection(department1.getEmployees(), employee1);
            AssociationTests.assertCollection(department2.getEmployees());
            Assert.assertEquals(department1, employee1.getDepartment());
            Assert.assertNull(employee2.getDepartment());
            
            employee2.setDepartment(department1);
            AssociationTests.assertCollection(department1.getEmployees(), employee1, employee2);
            AssociationTests.assertCollection(department2.getEmployees());
            Assert.assertEquals(department1, employee1.getDepartment());
            Assert.assertEquals(department1, employee2.getDepartment());
            
            employee1.setDepartment(department2);
            AssociationTests.assertCollection(department1.getEmployees(), employee2);
            AssociationTests.assertCollection(department2.getEmployees(), employee1);
            Assert.assertEquals(department2, employee1.getDepartment());
            Assert.assertEquals(department1, employee2.getDepartment());
            
            employee2.setDepartment(department2);
            AssociationTests.assertCollection(department1.getEmployees());
            AssociationTests.assertCollection(department2.getEmployees(), employee1, employee2);
            Assert.assertEquals(department2, employee1.getDepartment());
            Assert.assertEquals(department2, employee2.getDepartment());
            
            employee2.setDepartment(null);
            AssociationTests.assertCollection(department1.getEmployees());
            AssociationTests.assertCollection(department2.getEmployees(), employee1);
            Assert.assertEquals(department2, employee1.getDepartment());
            Assert.assertNull(employee2.getDepartment());
            
            employee1.setDepartment(null);
            AssociationTests.assertCollection(department1.getEmployees());
            AssociationTests.assertCollection(department2.getEmployees());
            Assert.assertNull(employee1.getDepartment());
            Assert.assertNull(employee2.getDepartment());
        }
        
        class Department {
            
            private AssociatedCollection<Department, Employee> employees = 
                new AssociatedCollection<Department, Employee>() {
                    
                    private static final long serialVersionUID = 8858129296343561928L;

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

                            private static final long serialVersionUID = -1026023298628596534L;

                            @Override
                            protected MACollection<Employee> createDefaultBase(
                                    UnifiedComparator<? super Employee> unifiedComparator) {
                                return TestSite.this.employeeBagFactory.createEmployeeBag();
                            }
                        };
                    }
                };
                
            MACollection<Employee> getEmployees() {
                return this.employees;
            }
            
        } 
        
        class Employee {
            
            private AssociatedReference<Employee, Department> departmentReference = 
                new AssociatedReference<Employee, Department>() {
                    
                    private static final long serialVersionUID = 8708994229193620001L;
                    
                    public Employee getOwner() {
                        return Employee.this;
                    }
                    
                    @Override
                    protected AssociatedEndpointType onGetOppositeEndpointType() {
                        return AssociatedEndpointType.COLLECTION;
                    }

                    @Override
                    public AssociatedEndpoint<Department, Employee> onGetOppositeEndpoint(Department opposite) {
                        return opposite.employees;
                    }
                };
                
            Department getDepartment() {
                return this.departmentReference.get();
            }
            
            public void setDepartment(Department department) {
                this.departmentReference.set(department);
            }
            
        }
    }
    
}
