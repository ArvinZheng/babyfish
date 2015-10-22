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

import org.babyfish.modificationaware.event.Modification;
import org.babyfish.modificationaware.event.BubbledPropertyConverter;
import org.babyfish.modificationaware.event.Cause;
import org.babyfish.modificationaware.event.EventDeclaration;
import org.babyfish.modificationaware.event.EventFactory;
import org.babyfish.modificationaware.event.EventProperty;
import org.babyfish.modificationaware.event.ModificationEvent;
import org.babyfish.modificationaware.event.PropertyVersion;

/**
 * @author Tao Chen
 */
@EventDeclaration(properties = {
        @EventProperty(name = "key"),
        @EventProperty(name = "value")
})
public abstract class MapElementEvent<K, V> extends ModificationEvent {
    
    private static final long serialVersionUID = -7534940042738752795L;

    private static final Factory<?, ?> FACTORY = getFactory(Factory.class);
    
    protected MapElementEvent(Object source) {
        super(source);
    }
    
    protected MapElementEvent(Object source, Cause cause) {
        super(source, cause);
    }
    
    protected MapElementEvent(Object source, MapElementEvent<K, V> dispatchSourceEvent) {
        super(source, dispatchSourceEvent);
    }
    
    @Override
    public abstract MapElementEvent<K, V> dispatch(Object source);
    
    @Override
    public abstract MapModification<K, V> getModification();
    
    public abstract K getKey(PropertyVersion version);
    
    public abstract V getValue(PropertyVersion version);
    
    @SuppressWarnings("unchecked")
    public static <K, V> MapElementEvent<K, V> createDetachEvent(
            Object source, 
            MapModification<K, V> modification,
            K key,
            V value) {
        return ((Factory<K, V>)FACTORY).createDetachEvent(source, modification, key, value);
    }
    
    @SuppressWarnings("unchecked")
    public static <K, V> MapElementEvent<K, V> createAttachEvent(
            Object source, 
            MapModification<K, V> modification,
            K key,
            V value) {
        return ((Factory<K, V>)FACTORY).createAttachEvent(source, modification, key, value);
    }
    
    @SuppressWarnings("unchecked")
    public static <K, V> MapElementEvent<K, V> createReplaceEvent(
            Object source, 
            MapModification<K, V> modification,
            K deatchedKey,
            K attachedKey,
            V deatchedElement, 
            V attachedElement) {
        return ((Factory<K, V>)FACTORY).createReplaceEvent(
                source, 
                modification, 
                deatchedKey, 
                attachedKey,
                deatchedElement,
                attachedElement);
    }
    
    @SuppressWarnings("unchecked")
    public static <K, V> MapElementEvent<K, V> bubbleEvent(
            Object source, 
            Cause cause, 
            BubbledPropertyConverter<K> keyConverter,
            BubbledPropertyConverter<V> valueConverter) {
        return ((Factory<K, V>)FACTORY).bubbleEvent(
                source, 
                cause, 
                keyConverter, 
                valueConverter);
    }

    public interface MapModification<K, V> extends Modification {
        
    }
    
    @EventFactory
    private interface Factory<K, V> {
        
        MapElementEvent<K, V> createDetachEvent(
                Object source, 
                MapModification<K, V> modification,
                K key,
                V value);
        
        MapElementEvent<K, V> createAttachEvent(
                Object source, 
                MapModification<K, V> modification,
                K key,
                V value);
        
        MapElementEvent<K, V> createReplaceEvent(
                Object source, 
                MapModification<K, V> modification,
                K deatchedKey,
                K attachedKey,
                V deatchedElement, 
                V attachedElement);
        
        MapElementEvent<K, V> bubbleEvent(
                Object source, 
                Cause cause, 
                BubbledPropertyConverter<K> keyConverter,
                BubbledPropertyConverter<V> valueConverter);
    }
    
}
