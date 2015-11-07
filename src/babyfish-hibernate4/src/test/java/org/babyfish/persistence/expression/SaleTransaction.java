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
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.persistence.model.metadata.EntityId;
import org.babyfish.reference.Reference;

/**
 * @author Tao Chen
 */
@Entity
@Table(name = "SALE_TRANSACTION")
@SequenceGenerator(
        name = "saleTransactionSequence", 
        sequenceName = "SALE_TRANSACTION_ID_SEQ",
        initialValue = 1,
        allocationSize = 1)
public class SaleTransaction {

    private static final ObjectModelFactory<OM> OM_FACTORY = 
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(SaleTransaction saleTransaction) {
        return saleTransaction.om;
    }
    
    @Id
    @Column(name = "SALE_TRANSACTION_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "saleTransactionSequence")
    public Long getId() {
        return om.getId();
    }

    protected void setId(Long id) {
        om.setId(id);
    }

    @Column(name = "DATE")
    @Temporal(TemporalType.TIME)
    public Date getDate() {
        return om.getDate();
    }

    public void setDate(Date date) {
        om.setDate(date);
    }

    @Column(name = "QUANTITY")
    public int getQuantity() {
        return om.getQuantity();
    }
    
    public void setQuantity(int quantity) {
        this.om.setQuantity(quantity);
    }

    @Column(name = "PRODUCT_TRANSACTION_PRICE")
    public BigDecimal getProductTransactionPrice() {
        return om.getProductTransactionPrice();
    }
    
    public void setProductTransactionPrice(BigDecimal productTransactionPrice) {
        om.setProductTransactionPrice(productTransactionPrice);
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BUYER_CUSTOMER_ID")
    public Customer getBuyer() {
        return this.om.getBuyerReference().get();
    }
    
    public void setBuyer(Customer buyer) {
        this.om.getBuyerReference().set(buyer);
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID")
    public Product getProduct() {
        return this.om.getProductReference().get();
    }
    
    public void setProduct(Product product) {
        this.om.getProductReference().set(product);
    }

    @ObjectModelDeclaration(provider = "jpa")
    private interface OM {
        @EntityId
        Long getId();
        void setId(Long id);
        
        @Scalar
        Date getDate();
        void setDate(Date date);
        
        @Scalar
        int getQuantity();
        void setQuantity(int quantity);
        
        @Scalar
        BigDecimal getProductTransactionPrice();
        
        void setProductTransactionPrice(BigDecimal productTransactionPrice);
        
        @Association(opposite = "buyedTransactions")
        Reference<Customer> getBuyerReference();
        
        @Association(opposite = "transactions")
        Reference<Product> getProductReference();
    }
}
