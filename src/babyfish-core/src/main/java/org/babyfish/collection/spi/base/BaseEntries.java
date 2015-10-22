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
package org.babyfish.collection.spi.base;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.babyfish.collection.ElementMatcher;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.conflict.MapConflictVoter;
import org.babyfish.lang.Ref;
import org.babyfish.modificationaware.ModificationAware;
import org.babyfish.validator.Validator;

/**
 * @author Tao Chen
 */
public interface BaseEntries<K, V> extends BaseContainer {
    
    Object PRESENT = new Object();
    
    boolean isRoot();
    
    void initMAOwner(ModificationAware owner);
    
    ModificationAware getMAOwner();
    
    ReplacementRule keyReplacementRule();
    
    UnifiedComparator<? super K> keyUnifiedComparator();
    
    UnifiedComparator<? super V> valueUnifiedComparator();
    
    UnifiedComparator<? super Entry<K, V>> entryUnifiedComparator();
    
    void combineKeyValidator(Validator<K> validator);
    
    void removeKeyValidator(Validator<K> validator);
    
    void validateKey(K key);
    
    void combineValueValidator(Validator<V> validator);
    
    void validateValue(V value);
    
    void removeValueValidator(Validator<V> validator);
    
    void combineConflictVoter(MapConflictVoter<K, V> voter);
    
    void removeConflictVoter(MapConflictVoter<K, V> voter);

    int size();
    
    boolean isEmpty();
    
    BaseEntry<K, V> getBaseEntry(Object key);
    
    boolean containsEntry(Object e, ElementMatcher<? super Entry<K, V>> entryMatcher);
    
    boolean containsKey(Object key, ElementMatcher<? super K> keyMatcher);
    
    boolean containsValue(Object value, ElementMatcher<? super V> valueMatcher);
    
    Ref<K> keyOf(V value, ElementMatcher<? super V> valueMatcher);
    
    V put(
            K key, 
            V value, 
            BaseEntriesHandler<K, V> handler);
    
    void putAll(
            Map<? extends K, ? extends V> m, 
            BaseEntriesHandler<K, V> handler);
    
    boolean addAll(
            Collection<? extends K> kc, 
            BaseEntriesHandler<K, V> handler);
    
    void clear(BaseEntriesHandler<K, V> handler);
    
    BaseEntry<K, V> removeByEntry(Object e, BaseEntriesHandler<K, V> handler);
    
    BaseEntry<K, V> removeByKey(Object key, BaseEntriesHandler<K, V> handler);
    
    BaseEntry<K, V> removeByValue(Object value, BaseEntriesHandler<K, V> handler);
    
    boolean removeAllByEntryCollection(Collection<?> ec, BaseEntriesHandler<K, V> handler);
    
    boolean removeAllByKeyCollection(Collection<?> kc, BaseEntriesHandler<K, V> handler);
    
    boolean removeAllByValueCollection(Collection<?> vc, BaseEntriesHandler<K, V> handler);
    
    boolean retainAllByEntryCollection(Collection<?> ec, BaseEntriesHandler<K, V> handler);
    
    boolean retainAllByKeyCollection(Collection<?> kc, BaseEntriesHandler<K, V> handler);
    
    boolean retainAllByValueCollection(Collection<?> vc, BaseEntriesHandler<K, V> handler);
    
    FrozenContextSuspending<K, V> suspendByKeyViaFrozenContext(K key, BaseEntriesHandler<K, V> handler);
    
    void resumeViaFronzeContext(FrozenContextSuspending<K, V> suspending, BaseEntriesHandler<K, V> handler);
    
    BaseEntryIterator<K, V> iterator();
}
