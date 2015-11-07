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
package org.babyfish.hibernate.collection.type;

import java.io.IOException;
import java.util.Properties;

import org.babyfish.hibernate.model.metadata.HibernateMetadatas;
import org.babyfish.lang.Arguments;
import org.babyfish.persistence.model.metadata.JPAAssociationProperty;

/**
 * @author Tao Chen
 */
public class MACollectionProperties extends Properties {

    private static final long serialVersionUID = 3541994016030119246L;
    
    private transient JPAAssociationProperty jpaAssociationProperty;
    
    public MACollectionProperties(
            JPAAssociationProperty jpaAssociationProperty, Properties properties) {
        Arguments.mustNotBeNull("jpaAssociationProperty", jpaAssociationProperty);
        this.jpaAssociationProperty = jpaAssociationProperty;
        if (properties != null) {
            this.putAll(properties);
        }
    }
    
    public JPAAssociationProperty getJPAAssociationProperty() {
        return this.jpaAssociationProperty;
    }
    
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeUTF(
                this
                .jpaAssociationProperty
                .getDeclaringObjectModelMetadata()
                .getOwnerClass()
                .getName());
        out.writeUTF(this.jpaAssociationProperty.getName());
    }
    
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        Class<?> clazz = Class.forName(in.readUTF());
        String name = in.readUTF();
        this.jpaAssociationProperty = 
            HibernateMetadatas
            .of(clazz)
            .getAssociationProperty(name);
    }
    
}
