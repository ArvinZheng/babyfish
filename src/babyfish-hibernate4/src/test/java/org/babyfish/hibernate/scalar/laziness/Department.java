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
import javax.persistence.Version;

import org.babyfish.hibernate.common.ObjectModelScalarLoaderBuilder;
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.AllowDisability;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.Deferrable;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.model.spi.ObjectModelImplementor;
import org.babyfish.persistence.model.metadata.EntityId;
import org.babyfish.persistence.model.metadata.Inverse;
import org.babyfish.persistence.model.metadata.Mapping;
import org.babyfish.persistence.model.metadata.OptimisticLock;
import org.hibernate.bytecode.internal.javassist.FieldHandled;
import org.hibernate.bytecode.internal.javassist.FieldHandler;

/**
 * @author Tao Chen
 */
@Entity
@Table(name = "DEPARTMENT")
@SequenceGenerator(
        name = "departmentSequence",
        sequenceName = "DEPARTMENT_ID_SEQ",
        initialValue = 1,
        allocationSize = 1)
public class Department extends ObjectModelScalarLoaderBuilder implements FieldHandled {

    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Department department) {
        return department.om;
    }
    
    @Id
    @Column(name = "DEPARMENT_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "departmentSequence")
    public Long getId() {
        return om.getOMId();
    }

    public void setId(Long id) {
        om.setOMId(id);
    }
    
    @Version
    @Column(name = "VERSION")
    public int getVersion() {
        return om.getOMVersion();
    }
    
    public void setVersion(int version) {
        om.setOMVersion(version);
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
    
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "DESCRIPTION")
    public String getDescription() {
        return om.getOMDescription();
    }
    
    public void setDescription(String description) {
        om.setOMDescription(description);
    }
    
    @OneToMany(mappedBy = "department")
    public Set<Employee> getEmployees() {
        return om.getEmployees();
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
    @AllowDisability
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
        
        @OptimisticLock
        @Mapping("version")
        int getOMVersion();
        void setOMVersion(int version);
        
        @Scalar
        @Mapping("name")
        String getOMName();
        void setOMName(String name);
        
        @Deferrable
        @Mapping("image")
        byte[] getOMImage();
        void setOMImage(byte[] image);
        
        @Deferrable
        @Mapping("description")
        String getOMDescription();
        void setOMDescription(String description);
        
        @Association(opposite = "departmentReference")
        @Inverse
        Set<Employee> getEmployees();
    }
}
