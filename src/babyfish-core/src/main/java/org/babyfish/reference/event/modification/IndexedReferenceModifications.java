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
import org.babyfish.reference.event.ValueEvent.Modification;

/**
 * @author Tao Chen
 */
public class IndexedReferenceModifications extends ReferenceModifications {
    
    private static final Factory FACTORY = getModificationFactory(Factory.class);
    
    public static <T> SetByIndex<T> setIndex(int index) {
        return FACTORY.setIndex(index);
    }
    
    public static <T> SetByIndexAndValue<T> set(int index, T value) {
        return FACTORY.set(index, value);
    }

    protected IndexedReferenceModifications() {
        
    }
    
    @Parameters("index")
    public interface SetByIndex<T> 
    extends Modification<T> {
        
        int getIndex();
        
    }
    
    @Parameters("index, value")
    public interface SetByIndexAndValue<T> 
    extends SetByIndex<T>, SetByValue<T> {
        
    }
    
    private interface Factory {
        
        <T> SetByIndex<T> setIndex(int index);
        
        <T> SetByIndexAndValue<T> set(int index, T value);
        
    }
    
}
