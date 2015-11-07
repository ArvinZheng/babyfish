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
package org.babyfish.hibernate.scalar.laziness;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.babyfish.hibernate.common.ObjectModelScalarLoaderBuilder;
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.Deferrable;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.model.spi.ObjectModelImplementor;
import org.babyfish.persistence.model.metadata.EntityId;
import org.babyfish.persistence.model.metadata.Mapping;
import org.babyfish.reference.Reference;
import org.hibernate.bytecode.internal.javassist.FieldHandled;
import org.hibernate.bytecode.internal.javassist.FieldHandler;

/**
 * @author Tao Chen
 */
@Entity
@Table(name = "EMPLOYEE")
@SequenceGenerator(
        name = "employeeSequence",
        sequenceName = "EMPLOYEE_ID_SEQ",
        initialValue = 1,
        allocationSize = 1)
public class Employee extends ObjectModelScalarLoaderBuilder implements FieldHandled {

    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Employee employee) {
        return employee.om;
    }
    
    @Id
    @Column(name = "EMPLOYEE_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "employeeSequence")
    public Long getId() {
        return om.getOMId();
    }

    public void setId(Long id) {
        om.setOMId(id);
    }

    @Column(name = "name", length = 20)
    public String getName() {
        return om.getOMName();
    }

    public void setName(String name) {
        om.setOMName(name);
    }

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "IMAGE")
    public byte[] getImage() {
        return om.getOMImage();
    }

    public void setImage(byte[] image) {
        om.setOMImage(image);
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    public Department getDepartment() {
        return this.om.getDepartmentReference().get();
    }
    
    public void setDepartment(Department department) {
        this.om.getDepartmentReference().set(department);
    }
    
    @Override
    public FieldHandler getFieldHandler() {
        return (FieldHandler)((ObjectModelImplementor)this.om).getScalarLoader();
    }

    @Override
    public void setFieldHandler(FieldHandler handler) {
        ((ObjectModelImplementor)this.om).setScalarLoader(ObjectModelScalarLoaderBuilder.build(this.om, handler));
    }

    @ObjectModelDeclaration(provider = "jpa")
    private interface OM {
        
        /*
         * In order to test more logic,
         * let the OM property names(omId, omName, omImage) are not same with 
         * the JPA property names(id, name, image) 
         */
        
        @EntityId
        @Mapping("id")
        Long getOMId();
        void setOMId(Long id);
        
        @Scalar
        @Mapping("name")
        String getOMName();
        void setOMName(String name);
        
        @Deferrable
        @Mapping("image")
        byte[] getOMImage();
        void setOMImage(byte[] image);
        
        @Association(opposite = "employees")
        Reference<Department> getDepartmentReference();
    }
}
