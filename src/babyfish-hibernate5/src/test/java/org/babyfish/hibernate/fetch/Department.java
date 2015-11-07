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
package org.babyfish.hibernate.fetch;

import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.babyfish.collection.XOrderedSet;
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
import org.babyfish.persistence.model.metadata.Inverse;
import org.hibernate.bytecode.internal.javassist.FieldHandled;
import org.hibernate.bytecode.internal.javassist.FieldHandler;

/**
 * @author Tao Chen
 */
@Entity
@Table(name = "DEPARTMENT")
@SequenceGenerator(
        name = "departmentSequence", sequenceName = "DEPARTMENT_ID_SEQ", 
        initialValue = 1, allocationSize = 1)
public class Department extends ObjectModelScalarLoaderBuilder implements FieldHandled {
    
    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Department department) {
        return department.om;
    }
    
    @Id
    @Column(name = "DEPARTMENT_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "departmentSequence")
    public Long getId() {
        return this.om.getId();
    }
    
    @SuppressWarnings("unused")
    private void setId(Long id) {
        this.om.setId(id);
    }
    
    @Column(name = "NAME", length = 50)
    public String getName() {
        return this.om.getName();
    }
    
    public void setName(String name) {
        this.om.setName(name);
    }
    
    @Basic(fetch = FetchType.LAZY)
    @Lob
    @Column(name = "IMAGE")
    public byte[] getImage() {
        return this.om.getImage();
    }
    
    public void setImage(byte[] image) {
        this.om.setImage(image);
    }
    
    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    public Set<Employee> getEmployees() {
        return this.om.getEmployees();
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
    
        @EntityId
        Long getId();
        void setId(Long id);
        
        @Scalar
        String getName();
        void setName(String name);
        
        @Deferrable
        byte[] getImage();
        void setImage(byte[] image);
        
        @Association(opposite = "departmentReference")
        @Inverse
        XOrderedSet<Employee> getEmployees();
    }
}
