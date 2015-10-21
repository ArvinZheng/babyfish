/*
 * BabyFish, Object Model Framework for Java and JPA.
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

import org.babyfish.collection.event.MapElementEvent.MapModification;
import org.babyfish.immutable.Parameters;

/**
 * @author Tao Chen
 */
public class OrderedMapModifications extends MapModifications {

    private static final Factory FACTORY = getModificationFactory(Factory.class);

    protected OrderedMapModifications() {
        
    }
    
    public static <K, V> PollFirstEntry<K, V> pollFirstEntry() {
        return FACTORY.pollFirstEntry();
    }
    
    public static <K, V> PollLastEntry<K, V> pollLastEntry() {
        return FACTORY.pollLastEntry();
    }
    
    public static <K, V> AccessByKey<K, V> access(K key) {
        return FACTORY.access(key);
    }
    
    @Parameters
    public interface PollFirstEntry<K, V> extends MapModification<K, V> {
        
    }
    
    @Parameters
    public interface PollLastEntry<K, V> extends MapModification<K, V> {
        
    }
    
    @Parameters("key")
    public interface AccessByKey<K, V> extends MapModification<K, V> {
        K getKey();
    }
    
    private interface Factory {
        
        <K, V> PollFirstEntry<K, V> pollFirstEntry();
        
        <K, V> PollLastEntry<K, V> pollLastEntry();
        
        <K, V> AccessByKey<K, V> access(K key);
        
    }
    
}
