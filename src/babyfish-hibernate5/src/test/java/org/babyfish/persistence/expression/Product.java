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

import java.math.BigDecimal;
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
@Table(name = "PRODUCT")
@SequenceGenerator(
        name = "productSequence", 
        sequenceName = "PRODUCT_ID_SEQ",
        initialValue = 1,
        allocationSize = 1)
public class Product {

    private static final ObjectModelFactory<OM> OM_FACTORY = 
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Product product) {
        return product.om;
    }
    
    @Id
    @Column(name = "PRODUCT_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "productSequence")
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

    @Column(name = "PRICE")
    public BigDecimal getPrice() {
        return om.getPrice();
    }

    public void setPrice(BigDecimal price) {
        om.setPrice(price);
    }
    
    @Column(name = "WEIGHT")
    public float getWeight() {
        return this.om.getWeight();
    }
    
    public void setWeight(float weight) {
        this.om.setWeight(weight);
    }

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    public Collection<SaleTransaction> getTransactions() {
        return om.getTransactions();
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
        float getWeight();
        void setWeight(float weight);
        
        @Scalar
        BigDecimal getPrice();
        void setPrice(BigDecimal price);
        
        @Association(opposite = "productReference")
        @Inverse
        Collection<SaleTransaction> getTransactions();
    }
}
