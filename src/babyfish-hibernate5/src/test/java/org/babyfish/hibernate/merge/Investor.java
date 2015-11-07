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

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
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

/**
 * @author Tao Chen
 */
@Entity
@Table(name = "INVESTOR")
@SequenceGenerator(
        name = "investorSequence", 
        sequenceName = "INVESTOR_SEQUENCE", 
        initialValue = 1, 
        allocationSize = 1)
public class Investor {

    private static final ObjectModelFactory<OM> OM_FACTORY = 
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Investor investor) {
        return investor.om;
        
    }
    @Id
    @Column(name = "INVESTOR_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "investorSequence")
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

    @ManyToMany(mappedBy = "investors")
    public Collection<Company> getCompanys() {
        return this.om.getCompanys();
    }
    
    @ObjectModelDeclaration(provider = "jpa")
    private interface OM {
        @EntityId
        Long getId();
        void setId(Long id);
        
        @Scalar
        String getName();
        void setName(String name);
        
        @Association(opposite = "investors")
        @Inverse
        Collection<Company> getCompanys();
    }
}
