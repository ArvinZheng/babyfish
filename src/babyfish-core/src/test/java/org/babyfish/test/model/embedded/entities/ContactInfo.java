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
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.ObjectModelMode;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;

/**
 * @author Tao Chen
 */
public class ContactInfo {

    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(ContactInfo contactInfo) {
        return contactInfo.om;
    }
    
    public String getPhone() {
        return om.getPhone();
    }

    public void setPhone(String phone) {
        om.setPhone(phone);
    }

    public String getEmail() {
        return om.getEmail();
    }

    public void setEmail(String email) {
        om.setEmail(email);
    }

    public Address getAddress() {
        return om.getAddress();
    }

    public void setAddress(Address address) {
        om.setAddress(address);
    }

    @Override
    public String toString() {
        return 
                "{ phone: '" +
                this.getPhone() + 
                "', email: '" +
                this.getEmail() + 
                "', address: " +
                this.getAddress() +
                " }";
    }

    @ObjectModelDeclaration(
            mode = ObjectModelMode.EMBEDDABLE, 
            declaredPropertiesOrder = "phone, email, address"
    )
    private interface OM {
        
        @Scalar
        String getPhone();
        void setPhone(String phone);
        
        @Scalar
        String getEmail();
        void setEmail(String email);
        
        @Scalar
        Address getAddress();
        void setAddress(Address address);
    }
}
