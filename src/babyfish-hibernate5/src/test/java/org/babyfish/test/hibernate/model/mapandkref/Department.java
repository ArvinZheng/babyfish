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
package org.babyfish.test.hibernate.model.mapandkref;

import java.util.Map;

import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.persistence.model.metadata.EntityId;
import org.babyfish.persistence.model.metadata.Inverse;

/**
 * @author Tao Chen
 */
public class Department {
    
    private final static ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    public Long getId() {
        return this.om.getId();
    }

    @SuppressWarnings("unused")
    private void setId(Long id) {
        this.om.setId(id);
    }

    public String getName() {
        return this.om.getName();
    }

    public void setName(String name) {
        this.om.setName(name);
    }
    
    public Map<String, Employee> getEmployees() {
        return this.om.getEmployees();
    }

    @StaticMethodToGetObjectModel
    static OM om(Department department) {
        return department.om;
    }

    @ObjectModelDeclaration(provider = "jpa")
    private interface OM {
        
        @EntityId
        Long getId();
        void setId(Long id);
        
        @Scalar
        String getName();
        void setName(String name);
        
        @Association(opposite = "departmentReference")
        @Inverse
        Map<String, Employee> getEmployees();
        
    }
    
}
