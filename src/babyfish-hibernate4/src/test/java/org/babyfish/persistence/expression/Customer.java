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
package org.babyfish.persistence.expression;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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

/**
 * @author Tao Chen
 */
@Entity
@Table(name = "CUSTOMER")
@SequenceGenerator(
        name = "customerSequence", 
        sequenceName = "CUSTOMER_ID_SEQ",
        initialValue = 1,
        allocationSize = 1)
public class Customer {

    private static final ObjectModelFactory<OM> OM_FACTORY = 
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Customer customer) {
        return customer.om;
    }
    
    @Id
    @Column(name = "CUSTOMER_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customerSequence")
    public Long getId() {
        return om.getId();
    }

    protected void setId(Long id) {
        om.setId(id);
    }

    @Column(name = "NAME")
    public String getName() {
        return om.getName();
    }

    public void setName(String name) {
        om.setName(name);
    }

    @Column(name = "ADDRESS")
    public String getAddress() {
        return om.getAddress();
    }

    public void setAddress(String name) {
        om.setAddress(name);
    }

    @OneToMany(mappedBy = "buyer", fetch = FetchType.LAZY)
    public Collection<SaleTransaction> getBuyedTransactions() {
        return om.getBuyedTransactions();
    }

    @ObjectModelDeclaration(provider = "jpa")
    private interface OM {
        @EntityId
        Long getId();
        void setId(Long id);
        
        @Scalar
        String getName();
        void setName(String name);
        
        @Scalar
        String getAddress();
        void setAddress(String name);
        
        @Association(opposite = "buyerReference")
        @Inverse
        Collection<SaleTransaction> getBuyedTransactions();
    }
}
