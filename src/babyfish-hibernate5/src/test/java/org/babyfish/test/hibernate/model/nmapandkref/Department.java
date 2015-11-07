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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.persistence.model.metadata.EntityId;
import org.babyfish.persistence.model.metadata.KeyMapping;
import org.babyfish.persistence.model.metadata.Mapping;
import org.babyfish.reference.KeyedReference;

/**
 * @author Tao Chen
 */
@Entity
@Table(name = "nmkr_DEPARTMENT")
@SequenceGenerator(
        name = "departmentSequence", 
        sequenceName = "nmkr_DEPARTMENT_ID_SEQ",
        initialValue = 1,
        allocationSize = 1)
public class Department {

    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Department department) {
        return department.om;
    }
    
    public Department() {
        
    }
    
    public Department(String name) {
        this.setName(name);
    }
    
    @Id
    @Column(name = "DEPARTMENT_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "departmentSequence")
    public Long getId() {
        return this.om.getModelId();
    }

    @SuppressWarnings("unused")
    private void setId(Long id) {
        this.om.setModelId(id);
    }
    
    @Column(name = "NAME")
    public String getName() {
        return this.om.getModelCompanyReference().getKey(true);
    }
    
    public void setName(String name) {
        this.om.getModelCompanyReference().setKey(name);
    }
    
    @ManyToOne
    @JoinColumn(name = "COMPANY_ID")
    public Company getCompany() {
        return this.om.getModelCompanyReference().get();
    }
    
    public void setCompany(Company company) {
        this.om.getModelCompanyReference().set(company);
    }
    
    @ObjectModelDeclaration(provider = "jpa")
    private interface OM {
        
        @EntityId
        @Mapping("id")
        Long getModelId();
        void setModelId(Long id);
        
        @Association(opposite = "modelDepartments")
        @Mapping("company")
        @KeyMapping("name")
        KeyedReference<String, Company> getModelCompanyReference();
        
    }
}
