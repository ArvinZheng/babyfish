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
package org.babyfish.hibernate.model.spi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

import org.babyfish.hibernate.model.metadata.HibernateObjectModelMetadata;
import org.babyfish.lang.Arguments;
import org.babyfish.model.ObjectModel;
import org.babyfish.model.ObjectModelFactory;
import org.hibernate.proxy.HibernateProxy;

/**
 * @author Tao Chen
 */
public final class HibernateObjectModelProxyWritingReplacment implements Serializable {

    private static final long serialVersionUID = -7666684273925556727L;
    
    private ObjectModel target;
    
    public HibernateObjectModelProxyWritingReplacment(ObjectModel target) {
        Arguments.mustNotBeNull("target", target);
        Arguments.mustBeCompatibleWithValue(
                "target.getObjectModelMetadata().getClass()", 
                target.getObjectModelMetadata().getClass(), 
                HibernateObjectModelMetadata.class);
        Arguments.mustNotBeInstanceOfValue(
                "target.getOwner()", 
                target.getOwner(), 
                HibernateProxy.class);
        this.target = target;
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(this.target.getObjectModelFactory());
        out.writeObject(this.target.getOwner());
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        ObjectModelFactory<?> objectModelFactory = (ObjectModelFactory<?>)in.readObject();
        Object owner = in.readObject();
        this.target = (ObjectModel)objectModelFactory.create(owner);
    }
    
    protected final Object readResolve() throws ObjectStreamException {
        return this.target;
    }
}
