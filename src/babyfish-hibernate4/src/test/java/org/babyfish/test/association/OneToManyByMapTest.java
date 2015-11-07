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

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.babyfish.association.AssociatedEndpoint;
import org.babyfish.association.AssociatedEndpointType;
import org.babyfish.association.AssociatedKeyedReference;
import org.babyfish.association.AssociatedMap;
import org.babyfish.collection.ArrayList;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.MAHashMap;
import org.babyfish.collection.MALinkedHashMap;
import org.babyfish.collection.MAMap;
import org.babyfish.collection.MATreeMap;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.hibernate.collection.type.MANavigableMapType;
import org.babyfish.hibernate.collection.type.MAOrderedMapType;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class OneToManyByMapTest {
    
    private static final TestSite.EmployeeMapFactory[] EMPLOYEE_MAP_FACTORIES =
        new TestSite.EmployeeMapFactory[] {
            new TestSite.EmployeeMapFactory() {
                @Override
                public MAMap<String, TestSite.Employee> createEmployeeMap() {
                    return new MAHashMap<String, TestSite.Employee>();
                }
            },
            new TestSite.EmployeeMapFactory() {
                @Override
                public MAMap<String, TestSite.Employee> createEmployeeMap() {
                    return new MALinkedHashMap<String, TestSite.Employee>();
                }
            },
            new TestSite.EmployeeMapFactory() {
                @Override
                public MAMap<String, TestSite.Employee> createEmployeeMap() {
                    return new MATreeMap<String, TestSite.Employee>();
                }
            },
            new TestSite.EmployeeMapFactory() {
                @SuppressWarnings("unchecked")
                @Override
                public MAMap<String, TestSite.Employee> createEmployeeMap() {
                    return (MAMap<String, org.babyfish.test.association.OneToManyByMapTest.TestSite.Employee>)
                            new MAOrderedMapType().wrap(null, new MALinkedHashMap<String, TestSite.Employee>());
                }
            },
            new TestSite.EmployeeMapFactory() {
                @SuppressWarnings("unchecked")
                @Override
                public MAMap<String, TestSite.Employee> createEmployeeMap() {
                    return (MAMap<String, org.babyfish.test.association.OneToManyByMapTest.TestSite.Employee>)
                            new MANavigableMapType().wrap(null, new MATreeMap<String, TestSite.Employee>());
                }
            },
        };
    
    private TestSite[] testSites;
    
    @Before
    public void initalize() {
        TestSite[] sites = new TestSite[EMPLOYEE_MAP_FACTORIES.length];
        int index = 0;
        for (TestSite.EmployeeMapFactory employeeMapFactory : EMPLOYEE_MAP_FACTORIES) {
            sites[index++] = new TestSite(employeeMapFactory);
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
    public void testModifyKeyAndValueOfEmployee() {
        for (TestSite testSite : this.testSites) {
            testSite.testModifyKeyAndValueOfEmployee();
        }
    }
    
    @Test
    public void testModifyValueAndKeyOfEmployee() {
        for (TestSite testSite : this.testSites) {
            testSite.testModifyValueAndKeyOfEmployee();
        }
    }
    
    @Test
    public void testModifyEmployee() {
        for (TestSite testSite : this.testSites) {
            testSite.testModifyEmployee();
        }
    }
    
    @Test
    public void testChangeKeyByDepartment() {
        for (TestSite testSite : this.testSites) {
            testSite.testChangeKeyByDepartment();
        }
    }
    
    @Test
    public void testBatchChangeKeyByDepartment() {
        for (TestSite testSite : this.testSites) {
            testSite.testBatchChangeKeyByDepartment();
        }
    }
    
    @Test
    public void testChangeKeyBySetValueOfEntryOfDepartment() {
        for (TestSite testSite : this.testSites) {
            testSite.testChangeKeyBySetValueOfEntryOfDepartment();
        }
    }
    
    @Test
    public void testChangeKeyByEmployee() {
        for (TestSite testSite : this.testSites) {
            testSite.testChangeKeyByEmployee();
        }
    }
    
    private static class TestSite {
        
        private EmployeeMapFactory employeeMapFactory;
        
        TestSite(EmployeeMapFactory employeeMapFactory) {
            this.employeeMapFactory = employeeMapFactory;
        }

        interface EmployeeMapFactory {
            
            MAMap<String, Employee> createEmployeeMap();
            
        }
        
        void testModifyDepartment() {
            Department department1 = this.new Department();
            Department department2 = this.new Department();
            Employee employee1 = new Employee();
            Employee employee2 = new Employee();
            assertCollection(department1.getEmployees().keySet());
            assertCollection(department1.getEmployees().values());
            assertCollection(department2.getEmployees().keySet());
            assertCollection(department2.getEmployees().values());
            Assert.assertNull(employee1.getCodeInDepartment());
            Assert.assertNull(employee1.getDepartment());
            Assert.assertNull(employee2.getCodeInDepartment());
            Assert.assertNull(employee2.getDepartment());
            
            department1.getEmployees().put("I", employee1);
            assertCollection(department1.getEmployees().keySet(), "I");
            assertCollection(department1.getEmployees().values(), employee1);
            assertCollection(department2.getEmployees().keySet());
            assertCollection(department2.getEmployees().values());
            Assert.assertEquals("I", employee1.getCodeInDepartment());
            Assert.assertEquals(department1, employee1.getDepartment());
            Assert.assertNull(employee2.getCodeInDepartment());
            Assert.assertNull(employee2.getDepartment());
            
            department1.getEmployees().put("II", employee2);
            assertCollection(department1.getEmployees().keySet(), "I", "II");
            assertCollection(department1.getEmployees().values(), employee1, employee2);
            assertCollection(department2.getEmployees().keySet());
            assertCollection(department2.getEmployees().values());
            Assert.assertEquals("I", employee1.getCodeInDepartment());
            Assert.assertEquals(department1, employee1.getDepartment());
            Assert.assertEquals("II", employee2.getCodeInDepartment());
            Assert.assertEquals(department1, employee2.getDepartment());
            
            department2.getEmployees().put("III", employee1);
            assertCollection(department1.getEmployees().keySet(), "II");
            assertCollection(department1.getEmployees().values(), employee2);
            assertCollection(department2.getEmployees().keySet(), "III");
            assertCollection(department2.getEmployees().values(), employee1);
            Assert.assertEquals("III", employee1.getCodeInDepartment());
            Assert.assertEquals(department2, employee1.getDepartment());
            Assert.assertEquals("II", employee2.getCodeInDepartment());
            Assert.assertEquals(department1, employee2.getDepartment());
            
            department2.getEmployees().put("IV", employee2);
            assertCollection(department1.getEmployees().keySet());
            assertCollection(department1.getEmployees().values());
            assertCollection(department2.getEmployees().keySet(), "III", "IV");
            assertCollection(department2.getEmployees().values(), employee1, employee2);
            Assert.assertEquals("III", employee1.getCodeInDepartment());
            Assert.assertEquals(department2, employee1.getDepartment());
            Assert.assertEquals("IV", employee2.getCodeInDepartment());
            Assert.assertEquals(department2, employee2.getDepartment());
            
            department2.getEmployees().keySet().remove("III");
            assertCollection(department1.getEmployees().keySet());
            assertCollection(department1.getEmployees().values());
            assertCollection(department2.getEmployees().keySet(), "IV");
            assertCollection(department2.getEmployees().values(), employee2);
            Assert.assertNull(employee1.getCodeInDepartment());
            Assert.assertNull(employee1.getDepartment());
            Assert.assertEquals("IV", employee2.getCodeInDepartment());
            Assert.assertEquals(department2, employee2.getDepartment());
            
            department2.getEmployees().values().remove(employee2);
            assertCollection(department1.getEmployees().keySet());
            assertCollection(department1.getEmployees().values());
            assertCollection(department2.getEmployees().keySet());
            assertCollection(department2.getEmployees().values());
            Assert.assertNull(employee1.getCodeInDepartment());
            Assert.assertNull(employee1.getDepartment());
            Assert.assertNull(employee2.getCodeInDepartment());
            Assert.assertNull(employee2.getDepartment());
        }
        
        void testModifyKeyAndValueOfEmployee() {
            Department department1 = this.new Department();
            Department department2 = this.new Department();
            Employee employee1 = new Employee();
            Employee employee2 = new Employee();
            assertCollection(department1.getEmployees().keySet());
            assertCollection(department1.getEmployees().values());
            assertCollection(department2.getEmployees().keySet());
            assertCollection(department2.getEmployees().values());
            Assert.assertNull(employee1.getCodeInDepartment());
            Assert.assertNull(employee1.getDepartment());
            Assert.assertNull(employee2.getCodeInDepartment());
            Assert.assertNull(employee2.getDepartment());
            
            employee1.setCodeInDepartment("I");
            employee1.setDepartment(department1);
            assertCollection(department1.getEmployees().keySet(), "I");
            assertCollection(department1.getEmployees().values(), employee1);
            assertCollection(department2.getEmployees().keySet());
            assertCollection(department2.getEmployees().values());
            Assert.assertEquals("I", employee1.getCodeInDepartment());
            Assert.assertEquals(department1, employee1.getDepartment());
            Assert.assertNull(employee2.getCodeInDepartment());
            Assert.assertNull(employee2.getDepartment());
            
            employee2.setCodeInDepartment("II");
            employee2.setDepartment(department1);
            assertCollection(department1.getEmployees().keySet(), "I", "II");
            assertCollection(department1.getEmployees().values(), employee1, employee2);
            assertCollection(department2.getEmployees().keySet());
            assertCollection(department2.getEmployees().values());
            Assert.assertEquals("I", employee1.getCodeInDepartment());
            Assert.assertEquals(department1, employee1.getDepartment());
            Assert.assertEquals("II", employee2.getCodeInDepartment());
            Assert.assertEquals(department1, employee2.getDepartment());
            
            employee1.setCodeInDepartment("III");
            employee1.setDepartment(department2);
            assertCollection(department1.getEmployees().keySet(), "II");
            assertCollection(department1.getEmployees().values(), employee2);
            assertCollection(department2.getEmployees().keySet(), "III");
            assertCollection(department2.getEmployees().values(), employee1);
            Assert.assertEquals("III", employee1.getCodeInDepartment());
            Assert.assertEquals(department2, employee1.getDepartment());
            Assert.assertEquals("II", employee2.getCodeInDepartment());
            Assert.assertEquals(department1, employee2.getDepartment());
            
            employee2.setCodeInDepartment("IV");
            employee2.setDepartment(department2);
            assertCollection(department1.getEmployees().keySet());
            assertCollection(department1.getEmployees().values());
            assertCollection(department2.getEmployees().keySet(), "III", "IV");
            assertCollection(department2.getEmployees().values(), employee1, employee2);
            Assert.assertEquals("III", employee1.getCodeInDepartment());
            Assert.assertEquals(department2, employee1.getDepartment());
            Assert.assertEquals("IV", employee2.getCodeInDepartment());
            Assert.assertEquals(department2, employee2.getDepartment());
            
            employee1.setCodeInDepartment(null);
            assertCollection(department1.getEmployees().keySet());
            assertCollection(department1.getEmployees().values());
            assertCollection(department2.getEmployees().keySet(), "IV");
            assertCollection(department2.getEmployees().values(), employee2);
            Assert.assertNull(employee1.getCodeInDepartment());
            Assert.assertNull(employee1.getDepartment());
            Assert.assertEquals("IV", employee2.getCodeInDepartment());
            Assert.assertEquals(department2, employee2.getDepartment());
            
            employee2.setCodeInDepartment(null);
            assertCollection(department1.getEmployees().keySet());
            assertCollection(department1.getEmployees().values());
            assertCollection(department2.getEmployees().keySet());
            assertCollection(department2.getEmployees().values());
            Assert.assertNull(employee1.getCodeInDepartment());
            Assert.assertNull(employee1.getDepartment());
            Assert.assertNull(employee2.getCodeInDepartment());
            Assert.assertNull(employee2.getDepartment());
        }
        
        void testModifyValueAndKeyOfEmployee() {
            Department department1 = this.new Department();
            Department department2 = this.new Department();
            Employee employee1 = new Employee();
            Employee employee2 = new Employee();
            assertCollection(department1.getEmployees().keySet());
            assertCollection(department1.getEmployees().values());
            assertCollection(department2.getEmployees().keySet());
            assertCollection(department2.getEmployees().values());
            Assert.assertNull(employee1.getCodeInDepartment());
            Assert.assertNull(employee1.getDepartment());
            Assert.assertNull(employee2.getCodeInDepartment());
            Assert.assertNull(employee2.getDepartment());
            
            employee1.setDepartment(department1);
            employee1.setCodeInDepartment("I");
            assertCollection(department1.getEmployees().keySet(), "I");
            assertCollection(department1.getEmployees().values(), employee1);
            assertCollection(department2.getEmployees().keySet());
            assertCollection(department2.getEmployees().values());
            Assert.assertEquals("I", employee1.getCodeInDepartment());
            Assert.assertEquals(department1, employee1.getDepartment());
            Assert.assertNull(employee2.getCodeInDepartment());
            Assert.assertNull(employee2.getDepartment());
            
            employee2.setDepartment(department1);
            employee2.setCodeInDepartment("II");
            assertCollection(department1.getEmployees().keySet(), "I", "II");
            assertCollection(department1.getEmployees().values(), employee1, employee2);
            assertCollection(department2.getEmployees().keySet());
            assertCollection(department2.getEmployees().values());
            Assert.assertEquals("I", employee1.getCodeInDepartment());
            Assert.assertEquals(department1, employee1.getDepartment());
            Assert.assertEquals("II", employee2.getCodeInDepartment());
            Assert.assertEquals(department1, employee2.getDepartment());
            
            employee1.setDepartment(department2);
            employee1.setCodeInDepartment("III");
            assertCollection(department1.getEmployees().keySet(), "II");
            assertCollection(department1.getEmployees().values(), employee2);
            assertCollection(department2.getEmployees().keySet(), "III");
            assertCollection(department2.getEmployees().values(), employee1);
            Assert.assertEquals("III", employee1.getCodeInDepartment());
            Assert.assertEquals(department2, employee1.getDepartment());
            Assert.assertEquals("II", employee2.getCodeInDepartment());
            Assert.assertEquals(department1, employee2.getDepartment());
            
            employee2.setDepartment(department2);
            employee2.setCodeInDepartment("IV");
            assertCollection(department1.getEmployees().keySet());
            assertCollection(department1.getEmployees().values());
            assertCollection(department2.getEmployees().keySet(), "III", "IV");
            assertCollection(department2.getEmployees().values(), employee1, employee2);
            Assert.assertEquals("III", employee1.getCodeInDepartment());
            Assert.assertEquals(department2, employee1.getDepartment());
            Assert.assertEquals("IV", employee2.getCodeInDepartment());
            Assert.assertEquals(department2, employee2.getDepartment());
            
            employee1.setDepartment(null);
            assertCollection(department1.getEmployees().keySet());
            assertCollection(department1.getEmployees().values());
            assertCollection(department2.getEmployees().keySet(), "IV");
            assertCollection(department2.getEmployees().values(), employee2);
            Assert.assertNull(employee1.getCodeInDepartment());
            Assert.assertNull(employee1.getDepartment());
            Assert.assertEquals("IV", employee2.getCodeInDepartment());
            Assert.assertEquals(department2, employee2.getDepartment());
            
            employee2.setDepartment(null);
            assertCollection(department1.getEmployees().keySet());
            assertCollection(department1.getEmployees().values());
            assertCollection(department2.getEmployees().keySet());
            assertCollection(department2.getEmployees().values());
            Assert.assertNull(employee1.getCodeInDepartment());
            Assert.assertNull(employee1.getDepartment());
            Assert.assertNull(employee2.getCodeInDepartment());
            Assert.assertNull(employee2.getDepartment());
        }
        
        void testModifyEmployee() {
            Department department1 = this.new Department();
            Department department2 = this.new Department();
            Employee employee1 = new Employee();
            Employee employee2 = new Employee();
            assertCollection(department1.getEmployees().keySet());
            assertCollection(department1.getEmployees().values());
            assertCollection(department2.getEmployees().keySet());
            assertCollection(department2.getEmployees().values());
            Assert.assertNull(employee1.getCodeInDepartment());
            Assert.assertNull(employee1.getDepartment());
            Assert.assertNull(employee2.getCodeInDepartment());
            Assert.assertNull(employee2.getDepartment());
            
            employee1.setDepartment("I", department1);
            assertCollection(department1.getEmployees().keySet(), "I");
            assertCollection(department1.getEmployees().values(), employee1);
            assertCollection(department2.getEmployees().keySet());
            assertCollection(department2.getEmployees().values());
            Assert.assertEquals("I", employee1.getCodeInDepartment());
            Assert.assertEquals(department1, employee1.getDepartment());
            Assert.assertNull(employee2.getCodeInDepartment());
            Assert.assertNull(employee2.getDepartment());
            
            employee2.setDepartment("II", department1);
            assertCollection(department1.getEmployees().keySet(), "I", "II");
            assertCollection(department1.getEmployees().values(), employee1, employee2);
            assertCollection(department2.getEmployees().keySet());
            assertCollection(department2.getEmployees().values());
            Assert.assertEquals("I", employee1.getCodeInDepartment());
            Assert.assertEquals(department1, employee1.getDepartment());
            Assert.assertEquals("II", employee2.getCodeInDepartment());
            Assert.assertEquals(department1, employee2.getDepartment());
            
            employee1.setDepartment("III", department2);
            assertCollection(department1.getEmployees().keySet(), "II");
            assertCollection(department1.getEmployees().values(), employee2);
            assertCollection(department2.getEmployees().keySet(), "III");
            assertCollection(department2.getEmployees().values(), employee1);
            Assert.assertEquals("III", employee1.getCodeInDepartment());
            Assert.assertEquals(department2, employee1.getDepartment());
            Assert.assertEquals("II", employee2.getCodeInDepartment());
            Assert.assertEquals(department1, employee2.getDepartment());
            
            employee2.setDepartment("IV", department2);
            assertCollection(department1.getEmployees().keySet());
            assertCollection(department1.getEmployees().values());
            assertCollection(department2.getEmployees().keySet(), "III", "IV");
            assertCollection(department2.getEmployees().values(), employee1, employee2);
            Assert.assertEquals("III", employee1.getCodeInDepartment());
            Assert.assertEquals(department2, employee1.getDepartment());
            Assert.assertEquals("IV", employee2.getCodeInDepartment());
            Assert.assertEquals(department2, employee2.getDepartment());
            
            employee1.setDepartment(null, null);
            assertCollection(department1.getEmployees().keySet());
            assertCollection(department1.getEmployees().values());
            assertCollection(department2.getEmployees().keySet(), "IV");
            assertCollection(department2.getEmployees().values(), employee2);
            Assert.assertNull(employee1.getCodeInDepartment());
            Assert.assertNull(employee1.getDepartment());
            Assert.assertEquals("IV", employee2.getCodeInDepartment());
            Assert.assertEquals(department2, employee2.getDepartment());
            
            employee2.setDepartment(null, null);
            assertCollection(department1.getEmployees().keySet());
            assertCollection(department1.getEmployees().values());
            assertCollection(department2.getEmployees().keySet());
            assertCollection(department2.getEmployees().values());
            Assert.assertNull(employee1.getCodeInDepartment());
            Assert.assertNull(employee1.getDepartment());
            Assert.assertNull(employee2.getCodeInDepartment());
            Assert.assertNull(employee2.getDepartment());
        }
        
        void testChangeKeyByDepartment() {
            
            Department department = this.new Department();
            Employee employee1 = new Employee();
            Employee employee2 = new Employee();
            Employee employee3 = new Employee();
            department.getEmployees().put("I", employee1);
            department.getEmployees().put("II", employee2);
            department.getEmployees().put("III", employee3);
            assertCollection(department.getEmployees().keySet(), "I", "II", "III");
            assertCollection(department.getEmployees().values(), employee1, employee2, employee3);
            Assert.assertEquals("I", employee1.getCodeInDepartment());
            Assert.assertEquals(department, employee1.getDepartment());
            Assert.assertEquals("II", employee2.getCodeInDepartment());
            Assert.assertEquals(department, employee2.getDepartment());
            Assert.assertEquals("III", employee3.getCodeInDepartment());
            Assert.assertEquals(department, employee3.getDepartment());
            
            department.getEmployees().put("IV", employee2);
            assertCollection(department.getEmployees().keySet(), "I", "III", "IV");
            assertCollection(department.getEmployees().values(), employee1, employee3, employee2);
            Assert.assertEquals("I", employee1.getCodeInDepartment());
            Assert.assertEquals(department, employee1.getDepartment());
            Assert.assertEquals("IV", employee2.getCodeInDepartment());
            Assert.assertEquals(department, employee2.getDepartment());
            Assert.assertEquals("III", employee3.getCodeInDepartment());
            Assert.assertEquals(department, employee3.getDepartment());
            
            department.getEmployees().put("III", employee1);
            assertCollection(department.getEmployees().keySet(), "III", "IV");
            assertCollection(department.getEmployees().values(), employee1, employee2);
            Assert.assertEquals("III", employee1.getCodeInDepartment());
            Assert.assertEquals(department, employee1.getDepartment());
            Assert.assertEquals("IV", employee2.getCodeInDepartment());
            Assert.assertEquals(department, employee2.getDepartment());
            Assert.assertNull(employee3.getCodeInDepartment());
            Assert.assertNull(employee3.getDepartment());
            
            department.getEmployees().put("III", employee2);
            assertCollection(department.getEmployees().keySet(), "III");
            assertCollection(department.getEmployees().values(), employee2);
            Assert.assertNull(employee1.getCodeInDepartment());
            Assert.assertNull(employee1.getDepartment());
            Assert.assertEquals("III", employee2.getCodeInDepartment());
            Assert.assertEquals(department, employee2.getDepartment());
            Assert.assertNull(employee3.getCodeInDepartment());
            Assert.assertNull(employee3.getDepartment());
        }
        
        void testBatchChangeKeyByDepartment() {
            Department department = this.new Department();
            Employee employee1 = new Employee();
            Employee employee2 = new Employee();
            Employee employee3 = new Employee();
            department.getEmployees().put("I", employee1);
            department.getEmployees().put("II", employee2);
            department.getEmployees().put("III", employee3);
            assertCollection(department.getEmployees().keySet(), "I", "II", "III");
            assertCollection(department.getEmployees().values(), employee1, employee2, employee3);
            Assert.assertEquals("I", employee1.getCodeInDepartment());
            Assert.assertEquals(department, employee1.getDepartment());
            Assert.assertEquals("II", employee2.getCodeInDepartment());
            Assert.assertEquals(department, employee2.getDepartment());
            Assert.assertEquals("III", employee3.getCodeInDepartment());
            Assert.assertEquals(department, employee3.getDepartment());
            
            Map<String, Employee> map = new LinkedHashMap<String, Employee>();
            map.put("I", employee1);
            map.put("II", employee1);
            map.put("IV", employee3);
            department.getEmployees().putAll(map);
            assertCollection(department.getEmployees().keySet(), "II", "IV");
            assertCollection(department.getEmployees().values(), employee1, employee3);
            Assert.assertEquals("II", employee1.getCodeInDepartment());
            Assert.assertEquals(department, employee1.getDepartment());
            Assert.assertNull(employee2.getCodeInDepartment());
            Assert.assertNull(employee2.getDepartment());
            Assert.assertEquals("IV", employee3.getCodeInDepartment());
            Assert.assertEquals(department, employee3.getDepartment());
        }
        
        void testChangeKeyBySetValueOfEntryOfDepartment() {
            Department department = this.new Department();
            Employee employee1 = new Employee();
            Employee employee2 = new Employee();
            Employee employee3 = new Employee();
            department.getEmployees().put("I", employee1);
            department.getEmployees().put("II", employee2);
            department.getEmployees().put("III", employee3);
            assertCollection(department.getEmployees().keySet(), "I", "II", "III");
            assertCollection(department.getEmployees().values(), employee1, employee2, employee3);
            Assert.assertEquals("I", employee1.getCodeInDepartment());
            Assert.assertEquals(department, employee1.getDepartment());
            Assert.assertEquals("II", employee2.getCodeInDepartment());
            Assert.assertEquals(department, employee2.getDepartment());
            Assert.assertEquals("III", employee3.getCodeInDepartment());
            Assert.assertEquals(department, employee3.getDepartment());
            
            findEntry(department.getEmployees(), "I").setValue(employee2);
            assertCollection(department.getEmployees().keySet(), "I", "III");
            assertCollection(department.getEmployees().values(), employee2, employee3);
            Assert.assertNull(employee1.getCodeInDepartment());
            Assert.assertNull(employee1.getDepartment());
            Assert.assertEquals("I", employee2.getCodeInDepartment());
            Assert.assertEquals(department, employee2.getDepartment());
            Assert.assertEquals("III", employee3.getCodeInDepartment());
            Assert.assertEquals(department, employee3.getDepartment());
            
            findEntry(department.getEmployees(), "I").setValue(employee3);
            assertCollection(department.getEmployees().keySet(), "I");
            assertCollection(department.getEmployees().values(), employee3);
            Assert.assertNull(employee1.getCodeInDepartment());
            Assert.assertNull(employee1.getDepartment());
            Assert.assertNull(employee2.getCodeInDepartment());
            Assert.assertNull(employee2.getDepartment());
            Assert.assertEquals("I", employee3.getCodeInDepartment());
            Assert.assertEquals(department, employee3.getDepartment());
        }
        
        void testChangeKeyByEmployee() {
            
            Department department = this.new Department();
            Employee employee1 = new Employee();
            Employee employee2 = new Employee();
            Employee employee3 = new Employee();
            department.getEmployees().put("I", employee1);
            department.getEmployees().put("II", employee2);
            department.getEmployees().put("III", employee3);
            assertCollection(department.getEmployees().keySet(), "I", "II", "III");
            assertCollection(department.getEmployees().values(), employee1, employee2, employee3);
            Assert.assertEquals("I", employee1.getCodeInDepartment());
            Assert.assertEquals(department, employee1.getDepartment());
            Assert.assertEquals("II", employee2.getCodeInDepartment());
            Assert.assertEquals(department, employee2.getDepartment());
            Assert.assertEquals("III", employee3.getCodeInDepartment());
            Assert.assertEquals(department, employee3.getDepartment());
            
            employee2.setCodeInDepartment("IV");
            assertCollection(department.getEmployees().keySet(), "I", "IV", "III");
            assertCollection(department.getEmployees().values(), employee1, employee2, employee3);
            Assert.assertEquals("I", employee1.getCodeInDepartment());
            Assert.assertEquals(department, employee1.getDepartment());
            Assert.assertEquals("IV", employee2.getCodeInDepartment());
            Assert.assertEquals(department, employee2.getDepartment());
            Assert.assertEquals("III", employee3.getCodeInDepartment());
            Assert.assertEquals(department, employee3.getDepartment());
            
            employee1.setCodeInDepartment("III");
            assertCollection(department.getEmployees().keySet(), "III", "IV");
            assertCollection(department.getEmployees().values(), employee1, employee2);
            Assert.assertEquals("III", employee1.getCodeInDepartment());
            Assert.assertEquals(department, employee1.getDepartment());
            Assert.assertEquals("IV", employee2.getCodeInDepartment());
            Assert.assertEquals(department, employee2.getDepartment());
            Assert.assertNull(employee3.getCodeInDepartment());
            Assert.assertNull(employee3.getDepartment());
            
            //employee3 is still not a element of the map because it 
            //removed from it automatically in the previous step
            employee3.setCodeInDepartment("I");
            assertCollection(department.getEmployees().keySet(), "III", "IV");
            assertCollection(department.getEmployees().values(), employee1, employee2);
            Assert.assertEquals("III", employee1.getCodeInDepartment());
            Assert.assertEquals(department, employee1.getDepartment());
            Assert.assertEquals("IV", employee2.getCodeInDepartment());
            Assert.assertEquals(department, employee2.getDepartment());
            Assert.assertNull(employee3.getCodeInDepartment());
            Assert.assertNull(employee3.getDepartment());
            
            employee3.setDepartment(department);
            assertCollection(department.getEmployees().keySet(), "III", "IV", "I");
            assertCollection(department.getEmployees().values(), employee1, employee2, employee3);
            Assert.assertEquals("III", employee1.getCodeInDepartment());
            Assert.assertEquals(department, employee1.getDepartment());
            Assert.assertEquals("IV", employee2.getCodeInDepartment());
            Assert.assertEquals(department, employee2.getDepartment());
            Assert.assertEquals("I", employee3.getCodeInDepartment());
            Assert.assertEquals(department, employee3.getDepartment());
        }
        
        @SuppressWarnings("unchecked")
        private static <E> void assertCollection(Collection<E> c, E ... elements) {
            if (elements.length == 0) {
                Assert.assertTrue(c.isEmpty());
            } else {
                Collection<E> other = new ArrayList<E>();
                for (int i = elements.length - 1; i >= 0; i--) {
                    other.add(elements[i]);
                }
                Assert.assertEquals(other.size(), c.size());
                Assert.assertTrue(other.containsAll(c));
                Assert.assertTrue(c.containsAll(other));
            }
        }
    
        class Department {
            
            private AssociatedMap<Department, String, Employee> employees =
                new AssociatedMap<Department, String, Employee>() {
                
                    private static final long serialVersionUID = -3078737281377939966L;

                    public Department getOwner() {
                        return Department.this;
                    }
                    
                    @Override
                    protected AssociatedEndpointType onGetOppositeEndpointType() {
                        return AssociatedEndpointType.KEYED_REFERENCE;
                    }

                    @Override
                    public AssociatedEndpoint<Employee, Department> onGetOppositeEndpoint(Employee opposite) {
                        return opposite.departmentReference;
                    }
    
                    @Override
                    protected RootData<String, Employee> createRootData() {
                        return new RootData<String, Employee>() {
                            
                            private static final long serialVersionUID = -6958491555426027076L;

                            @Override
                            protected MAMap<String, Employee> createDefaultBase(
                                    UnifiedComparator<? super String> keyUnifiedComparator,
                                    UnifiedComparator<? super Employee> valueUnifiedComparator) {
                                return TestSite.this.employeeMapFactory.createEmployeeMap();
                            }
                        };
                    }
                    
                };
                
            Map<String, Employee> getEmployees() {
                return this.employees;
            }
            
        }
        
        static class Employee {
            
            private AssociatedKeyedReference<Employee, String, Department> departmentReference =
                new AssociatedKeyedReference<Employee, String, Department>() {
                    
                    private static final long serialVersionUID = -5192390922623918323L;
                    
                    public Employee getOwner() {
                        return Employee.this;
                    }           
                    
                    @Override
                    public AssociatedEndpoint<Department, Employee> onGetOppositeEndpoint(Department opposite) {
                        return opposite.employees;
                    }
                };
                
            String getCodeInDepartment() {
                return this.departmentReference.getKey();
            }
            
            void setCodeInDepartment(String code) {
                this.departmentReference.setKey(code);
            }
            
            Department getDepartment() {
                return this.departmentReference.get();
            }
            
            void setDepartment(Department department) {
                this.departmentReference.set(department);
            }
            
            void setDepartment(String code, Department department) {
                this.departmentReference.set(code, department);
            }
            
        }
        
        private static <K, V> Entry<K, V> findEntry(Map<K, V> map, K key) {
            for (Entry<K, V> entry : map.entrySet()) {
                if (key.equals(entry.getKey())) {
                    return entry;
                }
            }
            return null;
        }
        
    }
    
}
