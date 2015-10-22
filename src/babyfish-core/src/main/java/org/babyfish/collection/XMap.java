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
package org.babyfish.collection;

import java.util.Collection;
import java.util.Map;

import org.babyfish.collection.conflict.MapConflictVoter;
import org.babyfish.lang.LockDescriptor;
import org.babyfish.lang.Ref;
import org.babyfish.validator.Validator;
import org.babyfish.view.View;

/**
 * @author Tao Chen
 */
public interface XMap<K, V> extends Map<K, V>, LockDescriptor {
    
    ReplacementRule keyReplacementRule();
    
    UnifiedComparator<? super K> keyUnifiedComparator();
    
    UnifiedComparator<? super V> valueUnifiedComparator();
    
    UnifiedComparator<? super Entry<K, V>> entryUnifiedComparator();
    
    void addKeyValidator(Validator<K> validator);
    
    void removeKeyValidator(Validator<K> validator);
    
    void validateKey(K key);
    
    void addValueValidator(Validator<V> validator);
    
    void removeValueValidator(Validator<V> validator);
    
    void validateValue(V value);
    
    void addConflictVoter(MapConflictVoter<K, V> voter);
    
    void removeConflictVoter(MapConflictVoter<K, V> voter);
    
    Ref<K> keyOf(V value);
    
    Ref<K> keyOf(V value, ElementMatcher<? super V> valueMatcher);
    
    XEntry<K, V> real(K key);
    
    boolean containsKey(Object key, ElementMatcher<? super K> keyMatcher);
    
    boolean containsValue(Object value, ElementMatcher<? super V> valueMatcher);
    
    @Override
    XEntrySetView<K, V> entrySet();
    
    @Override
    XKeySetView<K> keySet();
    
    @Override
    XValuesView<V> values();
    
    interface XEntrySetView<K, V> extends XSet<Entry<K, V>>, View {
        
        @Deprecated
        @Override
        boolean add(Entry<K, V> e) throws UnsupportedOperationException;
        
        @Deprecated
        @Override
        boolean addAll(Collection<? extends Entry<K, V>> c) throws UnsupportedOperationException;
        
        @Deprecated
        @Override
        void addValidator(Validator<Entry<K, V>> validator) throws UnsupportedOperationException;
        
        @Deprecated
        @Override
        void removeValidator(Validator<Entry<K, V>> validator) throws UnsupportedOperationException;
        
        @Override
        XEntrySetIterator<K, V> iterator();
        
        interface XEntrySetIterator<K, V> extends XIterator<Entry<K, V>> {
            
            @Override
            XEntry<K, V> next();
            
        }
    }
    
    interface XKeySetView<K> extends XSet<K>, View {
        
        @Deprecated
        @Override
        boolean add(K e) throws UnsupportedOperationException;
        
        @Deprecated
        @Override
        boolean addAll(Collection<? extends K> c) throws UnsupportedOperationException;
        
        @Deprecated
        @Override
        void addValidator(Validator<K> validator) throws UnsupportedOperationException;
        
        @Deprecated
        @Override
        void removeValidator(Validator<K> validator) throws UnsupportedOperationException;
    }
    
    interface XValuesView<V> extends XCollection<V>, View {
        
        @Deprecated
        @Override
        boolean add(V e) throws UnsupportedOperationException;
        
        @Deprecated
        @Override
        boolean addAll(Collection<? extends V> c) throws UnsupportedOperationException;
        
        @Deprecated
        @Override
        void addValidator(Validator<V> validator) throws UnsupportedOperationException;
        
        @Deprecated
        @Override
        void removeValidator(Validator<V> validator) throws UnsupportedOperationException;
    }
    
    interface XEntry<K, V> extends Entry<K, V>, LockDescriptor, View {
        
        UnifiedComparator<? super K> keyUnifiedComparator();
        
        UnifiedComparator<? super V> valueUnifiedComparator();
        
        UnifiedComparator<? super Entry<K, V>> unifiedComparator();
        
        boolean isAlive();
    }
}
