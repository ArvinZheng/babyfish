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
package org.babyfish.test.model.embedded.entities;

import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.reference.Reference;

/**
 * @author Tao Chen
 */
public class Customer {

    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Customer customer) {
        return customer.om;
    }
    
    public ContactInfo getContactInfo() {
        return om.getContactInfo();
    }
    
    public void setContactInfo(ContactInfo contactInfo) {
        om.setContactInfo(contactInfo);
    }
    
    public SalesSpecialist getSalesSpecialist() {
        return this.om.getSalesSpecialistReference().get();
    }
    
    public void setSalesSpecialist(SalesSpecialist salesSpecialist) {
        this.om.getSalesSpecialistReference().set(salesSpecialist);
    }
    
    public Name getName() {
        return om.getName();
    }

    public void setName(Name name) {
        om.setName(name);
    }

    @Override
    public String toString() {
        return 
                "{ name: " +
                this.getName() +
                ", contactInfo: " +
                this.getContactInfo() +
                " }";
    }

    @ObjectModelDeclaration
    private interface OM {
        
        @Scalar
        Name getName();
        void setName(Name name);
        
        @Scalar
        ContactInfo getContactInfo();
        void setContactInfo(ContactInfo contactInfo);
        
        @Association(opposite = "customers")
        Reference<SalesSpecialist> getSalesSpecialistReference();
    }
}
