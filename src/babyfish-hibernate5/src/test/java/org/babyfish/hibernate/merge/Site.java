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
package org.babyfish.hibernate.merge;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
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
import org.babyfish.reference.Reference;

/**
 * @author Tao Chen
 */
@Entity
@Table(name = "SITE")
@SequenceGenerator(
        name = "siteSequence", 
        sequenceName = "SITE_SEQUENCE", 
        initialValue = 1, 
        allocationSize = 1)
public class Site {

    private static final ObjectModelFactory<OM> OM_FACTORY = 
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Site site) {
        return site.om;
    }
    
    @Id
    @Column(name = "SITE_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "siteSequence")
    public Long getId() {
        return this.om.getId();
    }

    public void setId(Long id) {
        this.om.setId(id);
    }

    @Column(name = "NAME")
    public String getName() {
        return this.om.getName();
    }

    public void setName(String name) {
        this.om.setName(name);
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPANY_ID", insertable = false, updatable = false)
    public Company getCompany() {
        return this.om.getCompanyReference().get();
    }
    
    public void setCompany(Company company) {
        this.om.getCompanyReference().set(company);
    }
    
    @OneToMany
    @OrderColumn(name = "INDEX")
    @JoinColumn(name = "SITE_ID")
    public List<Office> getOffices() {
        return this.om.getOffices();
    }

    @ObjectModelDeclaration(provider = "jpa")
    private interface OM {
        
        @EntityId
        Long getId();
        void setId(Long id);
        
        @Scalar
        String getName();
        void setName(String name);
        
        @Association(opposite = "sites")
        @Inverse
        Reference<Company> getCompanyReference();
        
        @Association(opposite = "siteReference")
        List<Office> getOffices();
    }
}
