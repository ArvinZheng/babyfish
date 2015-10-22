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
package org.babyfish.collection.event;

import org.babyfish.collection.event.modification.EntryModifications;
import org.babyfish.modificationaware.event.Cause;
import org.babyfish.modificationaware.event.EventDeclaration;
import org.babyfish.modificationaware.event.EventFactory;
import org.babyfish.modificationaware.event.EventProperty;

/**
 * @author Tao Chen
 */
@EventDeclaration(properties = {
        @EventProperty(name = "key", shared = true)
})
public abstract class EntryElementEvent<K, V> extends ElementEvent<V> {

    private static final long serialVersionUID = -1932889773822973807L;
    
    private static final Factory<?, ?> FACTORY = 
        (Factory<?, ?>)EntryElementEvent.getFactory(Factory.class);

    protected EntryElementEvent(Object source) {
        super(source);
    }
    
    protected EntryElementEvent(Object source, Cause cause) {
        super(source, cause);
    }
    
    protected EntryElementEvent(Object source, EntryElementEvent<K, V> dispatchSourceEvent) {
        super(source, dispatchSourceEvent);
    }
    
    public abstract K getKey();
    
    @Override
    public abstract EntryModifications.SetByValue<V> getModification();
    
    @Override
    public abstract EntryElementEvent<K, V> dispatch(Object source);
    
    @SuppressWarnings("unchecked")
    public static <K, V> EntryElementEvent<K, V> createDetachEvent(
            Object source,
            EntryModifications.SetByValue<V> modification,
            V value,
            K key) {
        return ((Factory<K, V>)FACTORY).createDetachEvent(
                source, modification, value, key);
    }
    
    @SuppressWarnings("unchecked")
    public static <K, V> EntryElementEvent<K, V> createReplaceEvent(
            Object source,
            EntryModifications.SetByValue<V> modification,
            V valueToDeatch,
            V valueToAttach,
            K key) {
        return ((Factory<K, V>)FACTORY).createReplaceEvent(
                source, modification, valueToDeatch, valueToAttach, key);
    }
    
    @EventFactory
    private interface Factory<K, V> {
        
        EntryElementEvent<K, V> createDetachEvent(
                Object source,
                EntryModifications.SetByValue<V> modification,
                V value,
                K key);
        
        EntryElementEvent<K, V> createReplaceEvent(
                Object source,
                EntryModifications.SetByValue<V> modification,
                V valueToDeatch,
                V valueToAttach,
                K key);
        
    }

}
