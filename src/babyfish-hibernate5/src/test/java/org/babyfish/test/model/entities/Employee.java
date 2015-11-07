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
package org.babyfish.test.model.entities;

import org.babyfish.collection.MAList;
import org.babyfish.collection.MASet;
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.reference.MAReference;

/**
 * @author Tao Chen
 */
public class Employee {
    
    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Employee employee) {
        return employee.om;
    }

    private long id;
    
    private String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Department getDepartment() {
        return this.om.getDepartmentReference().get();
    }
    
    public void setDepartment(Department department) {
        this.om.getDepartmentReference().set(department);
    }
    
    public MAList<AnnualLeave> getAnualLeaves() {
        return this.om.getAnnualLeaves();
    }
    
    public MASet<Employee> getSubordinates() {
        return this.om.getSubordinates();
    }

    public Employee getManager() {
        return this.om.getManagerReference().get();
    }
    
    public void setManager(Employee manager) {
        this.om.getManagerReference().set(manager);
    }
    
    @ObjectModelDeclaration(provider = "jpa")
    private interface OM {
        
        @Association(opposite = "employees")
        MAReference<Department> getDepartmentReference();
        
        @Association(opposite = "ownerReference")
        MAList<AnnualLeave> getAnnualLeaves();
        
        @Association(opposite = "managerReference")
        MASet<Employee> getSubordinates();
        
        @Association(opposite = "subordinates")
        MAReference<Employee> getManagerReference();
        
    }
    
}
