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

import java.util.Map;

import org.babyfish.collection.event.MapElementEvent.MapModification;
import org.babyfish.immutable.Parameters;
import org.babyfish.modificationaware.event.Modifications;

/**
 * @author Tao Chen
 */
public class MapModifications extends Modifications {
    
    private static final Factory FACTORY = getModificationFactory(Factory.class);

    protected MapModifications() {
        
    }
    
    public static <K, V> PutByKeyAndValue<K, V> put(K key, V value) {
        return FACTORY.put(key, value);
    }
    
    public static <K, V> PutAllByMap<K, V> putAll(Map<? extends K, ? extends V> m) {
        return FACTORY.putAll(m);
    }
    
    public static <K, V> Clear<K, V> clear() {
        return FACTORY.clear();
    }
    
    public static <K, V> RemoveByKey<K, V> remove(K key) {
        return FACTORY.remove(key);
    }
    
    @Parameters("key, value")
    public interface PutByKeyAndValue<K, V> extends MapModification<K, V> {
        
        K getKey();
        
        V getValue();
        
    }
    
    @Parameters("map")
    public interface PutAllByMap<K, V> extends MapModification<K, V> {
        
        Map<? extends K, ? extends V> getMap();
        
    }
    
    @Parameters
    public interface Clear<K, V> extends MapModification<K, V> {
        
    }
    
    @Parameters("key")
    public interface RemoveByKey<K, V> extends MapModification<K, V> {
        
        K getKey();
        
    }
    
    public static <K, V> SuspendByKeyViaFrozenContext<K, V> suspendViaFrozenContext(K key) {
        return FACTORY.suspendViaFrozenContext(key);
    }
    
    public static <K, V> ResumeViaFrozenContext<K, V> resumeViaFrozenContext() {
        return FACTORY.resumeViaFrozenContext();
    }
    
    @Parameters("key")
    public interface SuspendByKeyViaFrozenContext<K, V> extends MapModification<K, V> {
        
        K getKey();
    }
    
    @Parameters
    public interface ResumeViaFrozenContext<K, V> extends MapModification<K, V> {
    }
    
    private interface Factory {
        
        <K, V> PutByKeyAndValue<K, V> put(K key, V value);
        
        <K, V> PutAllByMap<K, V> putAll(Map<? extends K, ? extends V> m);
        
        <K, V> Clear<K, V> clear();
        
        <K, V> RemoveByKey<K, V> remove(K key);
        
        <K, V> SuspendByKeyViaFrozenContext<K, V> suspendViaFrozenContext(K key); 
        
        <K, V> ResumeViaFrozenContext<K, V> resumeViaFrozenContext();
    }
    
}
