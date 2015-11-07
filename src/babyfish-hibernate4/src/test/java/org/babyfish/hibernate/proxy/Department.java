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
package org.babyfish.hibernate.proxy;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.persistence.model.metadata.EntityId;
import org.babyfish.persistence.model.metadata.Inverse;
import org.babyfish.persistence.model.metadata.Mapping;
import org.babyfish.reference.Reference;

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
public class Department {

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
        return this.om.getOMId();
    }

    public void setId(Long Id) {
        this.om.setOMId(Id);
    }

    @Column(name = "NAME")
    public String getName() {
        return this.om.getOMName();
    }

    public void setName(String name) {
        this.om.setOMName(name);
    }

    @OneToMany(mappedBy = "department")
    public Set<Employee> getEmployees() {
        return this.om.getEmployees();
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPANY_ID")
    public Company getCompany() {
        return this.om.getCompanyReference().get();
    }
    
    public void setCompany(Company company) {
        this.om.getCompanyReference().set(company);
    }

    @ObjectModelDeclaration(provider = "jpa")
    private interface OM {
        
        @EntityId
        @Mapping("id")
        Long getOMId();
        void setOMId(Long Id);
        
        @Scalar
        @Mapping("name")
        String getOMName();
        void setOMName(String name);
        
        @Association(opposite = "departmentReference")
        @Inverse
        Set<Employee> getEmployees();
        
        @Association(opposite = "departments")
        Reference<Company> getCompanyReference();
    }
}
