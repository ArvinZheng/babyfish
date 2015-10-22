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
import org.babyfish.reference.KeyedReference;

/**
 * @author Tao Chen
 */
public class Site {

    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Site site) {
        return site.om;
    }
    
    public String getName() {
        return om.getName();
    }

    public void setName(String name) {
        om.setName(name);
    }
    
    public ContactInfo getContactInfo() {
        return this.om.getCompanyReference().getKey();
    }
    
    public void setContactInfo(ContactInfo contactInfo) {
        this.om.getCompanyReference().setKey(contactInfo);
    }
    
    public Company getCompany() {
        return this.om.getCompanyReference().get();
    }
    
    public void setCompany(Company company) {
        this.om.getCompanyReference().set(company);
    }

    @Override
    public String toString() {
        return "{ name: '" +
                this.getName() +
                "', contactInfo: " +
                this.getContactInfo() +
                " }";
    }

    @ObjectModelDeclaration
    private interface OM {
        
        @Scalar
        String getName();
        void setName(String name);
        
        @Association(opposite = "sites")
        KeyedReference<ContactInfo, Company> getCompanyReference();
    }
}
