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

import org.babyfish.collection.MAOrderedSet;
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.reference.MAKeyedReference;

/**
 * @author Tao Chen
 */
public class Department {
    
    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = 
        OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Department department) {
        return department.om;
    }
    
    private long id;
    
    private String name;

    public long getId() {
        return this.id;
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
    
    public MAOrderedSet<Employee> getEmployees() {
        return this.om.getEmployees();
    }
    
    public String getCodeInCompany() {
        return this.om.getCompanyReference().getKey();
    }
    
    public void setCodeInCompany(String code) {
        this.om.getCompanyReference().setKey(code);
    }
    
    public Company getCompany() {
        return this.om.getCompanyReference().get();
    }
    
    public void setCompany(Company company) {
        this.om.getCompanyReference().set(company);
    }
    
    @ObjectModelDeclaration
    private interface OM {
        
        @Association(opposite = "departmentReference")
        MAOrderedSet<Employee> getEmployees();
        
        @Association(opposite = "departments")
        MAKeyedReference<String, Company> getCompanyReference();
        
    }
    
}
