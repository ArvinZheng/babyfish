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
package org.babyfish.test.hibernate.model.bagandref;

import java.util.Collection;

import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ReferenceComparisonRule;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.persistence.model.metadata.EntityId;
import org.babyfish.persistence.model.metadata.Inverse;
import org.babyfish.persistence.model.metadata.Mapping;

/**
 * @author Tao Chen
 */
public class Department {

    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Department department) {
        return department.om;
    }
    
    public Long getId() {
        return this.om.getModelId();
    }

    @SuppressWarnings("unused")
    private void setId(Long id) {
        this.om.setModelId(id);
    }

    public String getName() {
        return this.om.getModelName();
    }

    public void setName(String name) {
        this.om.setModelName(name);
    }
    
    public Collection<Employee> getEmployees() {
        return this.om.getModelEmployees();
    }

    @ObjectModelDeclaration(provider = "jpa")
    @ReferenceComparisonRule("modelName")
    private interface OM {
        
        @EntityId
        @Mapping("id")
        Long getModelId();
        void setModelId(Long id);
        
        @Scalar
        @Mapping("name")
        String getModelName();
        void setModelName(String name);
        
        @Association(opposite = "modelDepartmentReference")
        @Inverse
        @Mapping("employees")
        Collection<Employee> getModelEmployees();
        
    }
}
