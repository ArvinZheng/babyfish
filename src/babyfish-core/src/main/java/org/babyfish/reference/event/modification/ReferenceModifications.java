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
package org.babyfish.reference.event.modification;

import org.babyfish.immutable.Parameters;
import org.babyfish.modificationaware.event.Modifications;
import org.babyfish.reference.event.ValueEvent.Modification;

/**
 * @author Tao Chen
 */
public class ReferenceModifications extends Modifications {
    
    private static final Factory FACTORY = getModificationFactory(Factory.class);

    protected ReferenceModifications() {
        
    }
    
    public static <T> SetByValue<T> set(T value) {
        return FACTORY.set(value);
    }
    
    @Parameters("value")
    public interface SetByValue<T> extends Modification<T> {
        
        T getValue();
        
    }
    
    private interface Factory {
        
        <T> SetByValue<T> set(T value);
        
    }
    
}
