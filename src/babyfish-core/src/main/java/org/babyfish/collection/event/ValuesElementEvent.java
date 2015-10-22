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

import org.babyfish.modificationaware.event.BubbledPropertyConverter;
import org.babyfish.modificationaware.event.BubbledSharedPropertyConverter;
import org.babyfish.modificationaware.event.Cause;
import org.babyfish.modificationaware.event.EventDeclaration;
import org.babyfish.modificationaware.event.EventFactory;
import org.babyfish.modificationaware.event.EventProperty;

/**
 * @author Tao Chen
 */
@EventDeclaration(
        properties = @EventProperty(name = "key", shared = true))
public abstract class ValuesElementEvent<K, V> extends ElementEvent<V> {
    
    private static final long serialVersionUID = -7534940042738752795L;

    private static final Factory<?, ?> FACTORY = getFactory(Factory.class);
    
    protected ValuesElementEvent(Object source) {
        super(source);
    }
    
    protected ValuesElementEvent(Object source, Cause cause) {
        super(source, cause);
    }
    
    protected ValuesElementEvent(Object source, ValuesElementEvent<K, V> dispatchSourceEvent) {
        super(source, dispatchSourceEvent);
    }
    
    public abstract K getKey();
    
    @Override
    public abstract ValuesElementEvent<K, V> dispatch(Object source);
    
    @SuppressWarnings("unchecked")
    public static <K, V> ValuesElementEvent<K, V> createDetachEvent(
            Object source, Modification<V> modification, V element, K key) {
        return ((Factory<K, V>)FACTORY).createDetachEvent(source, modification, element, key);
    }
    
    @SuppressWarnings("unchecked")
    public static <K, V> ValuesElementEvent<K, V> createAttachEvent(
            Object source, Modification<V> modification, V element, K key) {
        return ((Factory<K, V>)FACTORY).createAttachEvent(source, modification, element, key);
    }
    
    @SuppressWarnings("unchecked")
    public static <K, V> ValuesElementEvent<K, V> createReplaceEvent(
            Object source, Modification<V> modification, V detachedElement, V attachedElement, K key) {
        return ((Factory<K, V>)FACTORY).createReplaceEvent(source, modification, detachedElement, attachedElement, key);
    }
    
    @SuppressWarnings("unchecked")
    public static <K, V> ValuesElementEvent<K, V> bubbleEvent(
            Object source, 
            Cause cause, 
            BubbledPropertyConverter<V> elementConverter, 
            BubbledSharedPropertyConverter<K> keyConverter) {
        return ((Factory<K, V>)FACTORY).bubbleEvent(
                source, 
                cause, 
                elementConverter, 
                keyConverter);
    }
    
    @EventFactory
    private interface Factory<K, V> {
        
        ValuesElementEvent<K, V> createDetachEvent(
                Object source, Modification<V> modification, V element, K key);
        
        ValuesElementEvent<K, V> createAttachEvent(
                Object source, Modification<V> modification, V element, K key);
        
        ValuesElementEvent<K, V> createReplaceEvent(
                Object source, Modification<V> modification, V detachedElement, V attachedElement, K key);
        
        ValuesElementEvent<K, V> bubbleEvent(
                Object source, 
                Cause cause, 
                BubbledPropertyConverter<V> elementConverter, 
                BubbledSharedPropertyConverter<K> keyConverter);
    }
    
}
