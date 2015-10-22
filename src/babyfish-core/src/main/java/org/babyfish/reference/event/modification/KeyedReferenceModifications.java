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
import org.babyfish.reference.event.KeyedValueEvent.KeyedModification;

/**
 * @author Tao Chen
 */
public class KeyedReferenceModifications extends ReferenceModifications {
    
    private static final Factory FACTORY = getModificationFactory(Factory.class);

    protected KeyedReferenceModifications() {
        
    }
    
    public static <T> SetByValue<?, T> set(T value) {
        return FACTORY.set(value);
    }
    
    public static <K, T> SetByKey<K, T> setKey(K key) {
        return FACTORY.setKey(key);
    }
    
    public static <K, T> SetByKeyAndValue<K, T> set(K key, T value) {
        return FACTORY.set(key, value);
    }
    
    @Parameters("value")
    public interface SetByValue<K, T>
    extends KeyedModification<K, T>, ReferenceModifications.SetByValue<T> {
        
    }
    
    @Parameters("key")
    public interface SetByKey<K, T> 
    extends KeyedModification<K, T> {
        
        K getKey();
        
    }
    
    @Parameters("key, value")
    public interface SetByKeyAndValue<K, T> 
    extends KeyedModification<K, T>, SetByKey<K, T>, SetByValue<K, T> {
        
    }
    
    private interface Factory {
        
        <K, T> SetByValue<K, T> set(T value);
        
        <K, T> SetByKey<K, T> setKey(K key);
        
        <K, T> SetByKeyAndValue<K, T> set(K key, T value);
        
    }
}
