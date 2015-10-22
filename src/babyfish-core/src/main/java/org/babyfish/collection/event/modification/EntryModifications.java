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
package org.babyfish.collection.event.modification;

import org.babyfish.collection.event.ElementEvent.Modification;
import org.babyfish.immutable.Parameters;
import org.babyfish.modificationaware.event.Modifications;

/**
 * @author Tao Chen
 */
public class EntryModifications extends Modifications {
    
    private static final Factory FACTORY = getModificationFactory(Factory.class);
    
    protected EntryModifications() {
        
    }
    
    public static <V> SetByValue<V> set(V value) {
        return FACTORY.set(value);
    }
    
    @Parameters("value")
    public interface SetByValue<V> extends Modification<V> {
        
        V getValue();
        
    }
    
    private interface Factory {
        
        <V> SetByValue<V> set(V value);
        
    }

}
