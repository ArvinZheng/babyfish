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
package org.babyfish.test.hibernate.model.nmapandkref;

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.babyfish.collection.XNavigableMap;
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.persistence.model.metadata.EntityId;
import org.babyfish.persistence.model.metadata.Inverse;
import org.babyfish.persistence.model.metadata.Mapping;

/**
 * @author Tao Chen
 */
@Entity
@Table(name = "nmkr_COMPANY")
@SequenceGenerator(
        name = "companySequence", 
        sequenceName = "nmkr_COMPANY_ID_SEQ",
        initialValue = 1,
        allocationSize = 1)
public class Company {

    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    public Company() {
        
    }
    
    public Company(String name) {
        this.setName(name);
    }
    
    @StaticMethodToGetObjectModel
    static OM om(Company company) {
        return company.om;
    }
    
    @Id
    @Column(name = "COMPANY_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "companySequence")
    public Long getId() {
        return this.om.getModelId();
    }
    
    @SuppressWarnings("unused")
    private void setId(Long id) {
        this.om.setModelId(id);
    }
    
    @Column(name = "NAME")
    public String getName() {
        return this.om.getModelName();
    }
    
    public void setName(String name) {
        this.om.setModelName(name);
    }
    
    @OneToMany(mappedBy = "company")
    @MapKey(name = "name")
    public Map<String, Department> getDepartments() {
        return this.om.getModelDepartments();
    }
    
    @ObjectModelDeclaration(provider = "jpa")
    private interface OM {
        
        @EntityId
        @Mapping("id")
        Long getModelId();
        void setModelId(Long id);
        
        @Scalar
        @Mapping("name")
        String getModelName();
        void setModelName(String name);
        
        @Association(opposite = "modelCompanyReference")
        @Inverse
        @Mapping("departments")
        XNavigableMap<String, Department> getModelDepartments();
    }
    
}
