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
package org.babyfish.hibernate.fetch;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.ObjectModelMode;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;

/**
 * @author Tao Chen
 */
@Embeddable
public class Name implements Comparable<Name> {
    
    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Name name) {
        return name.om;
    }
    
    public Name() {
        
    }
    
    public Name(String firstName, String lastName) {
        this.setFirstName(firstName);
        this.setLastName(lastName);
    }

    @Column(name = "FIRST_NAME", length = 20)
    public String getFirstName() {
        return om.getFirstName();
    }

    public void setFirstName(String firstName) {
        om.setFirstName(firstName);
    }

    @Column(name = "LAST_NAME", length = 20)
    public String getLastName() {
        return om.getLastName();
    }

    public void setLastName(String lastName) {
        om.setLastName(lastName);
    }

    @Override
    public int hashCode() {
        return OM_FACTORY.getObjectModelMetadata().getOwnerEqualityComparator().hashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return OM_FACTORY.getObjectModelMetadata().getOwnerEqualityComparator().equals(this, obj);
    }

    @Override
    public int compareTo(Name o) {
        return OM_FACTORY.getObjectModelMetadata().getOwnerComparator().compare(this, o);
    }

    @Override
    public String toString() {
        //TODO:
        return "{ firstName: " +
                this.getFirstName() +
                ", lastName: " +
                this.getLastName() +
                "}";
    }

    @ObjectModelDeclaration(
            provider = "jpa", 
            mode = ObjectModelMode.EMBEDDABLE, 
            declaredPropertiesOrder = "firstName, lastName")
    private interface OM {
        
        @Scalar
        String getFirstName();
        void setFirstName(String firstName);
        
        @Scalar
        String getLastName();
        void setLastName(String lastName);
    }
}
