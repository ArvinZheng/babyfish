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
public class Address {
    
    private static final ObjectModelFactory<OM> OM_FACTORY = 
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Address address) {
        return address.om;
    }
    
    public String getCity() {
        return om.getCity();
    }

    public void setCity(String city) {
        om.setCity(city);
    }

    public int getStreetNo() {
        return om.getStreetNo();
    }

    public void setStreetNo(int streeNo) {
        om.setStreetNo(streeNo);
    }

    @Override
    public String toString() {
        return 
                "{ city: '" +
                this.getCity() + 
                "', streetNo: " +
                this.getStreetNo() +
                " }";
    }

    @ObjectModelDeclaration(
            mode = ObjectModelMode.EMBEDDABLE, 
            declaredPropertiesOrder = "city, streetNo"
    )
    private interface OM {
        
        @Scalar
        String getCity();
        void setCity(String city);
        
        @Scalar
        int getStreetNo();
        void setStreetNo(int streeNo);
    }
}
