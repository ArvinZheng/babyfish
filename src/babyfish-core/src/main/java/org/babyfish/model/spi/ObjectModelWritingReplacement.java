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
package org.babyfish.model.spi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

import org.babyfish.lang.Arguments;
import org.babyfish.model.ObjectModel;
import org.babyfish.model.ObjectModelFactory;

/**
 * @author Tao Chen
 */
public final class ObjectModelWritingReplacement implements Serializable {

    private static final long serialVersionUID = -2058225394044836701L;
    
    private ObjectModel target;
    
    public ObjectModelWritingReplacement(ObjectModel target) {
        this.target = 
                Arguments.mustBeInstanceOfValue(
                        "target", 
                        Arguments.mustNotBeNull("target", target), 
                        ObjectModelIO.class);
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        ObjectModel target = this.target;
        out.writeObject(target.getObjectModelFactory());
        out.writeObject(target.getOwner());
        ((ObjectModelIO)target).writeProperties(out);
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        ObjectModelFactory<?> objectModelFactory = (ObjectModelFactory<?>)in.readObject();
        Object owner = in.readObject();
        ObjectModel target = (ObjectModel)objectModelFactory.create(owner);
        ((ObjectModelIO)target).readProperties(in);
        this.target = target;
    }
    
    protected final Object readResolve() throws ObjectStreamException {
        return this.target;
    }
}
