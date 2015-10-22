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
package org.babyfish.reference.event;

import org.babyfish.modificationaware.event.BubbledPropertyConverter;
import org.babyfish.modificationaware.event.Cause;
import org.babyfish.modificationaware.event.EventDeclaration;
import org.babyfish.modificationaware.event.EventFactory;
import org.babyfish.modificationaware.event.EventProperty;
import org.babyfish.modificationaware.event.PropertyVersion;

/**
 * @author Tao Chen
 */
@EventDeclaration(properties = {
        @EventProperty(name = "key"),
        @EventProperty(name = "value")
})
public abstract class KeyedValueEvent<K, T> extends ValueEvent<T> {

    private static final long serialVersionUID = -2701463033512462141L;
    
    private static final Factory<?, ?> FACTORY = getFactory(Factory.class);

    protected KeyedValueEvent(Object source) {
        super(source);
    }
    
    protected KeyedValueEvent(Object source, Cause cause) {
        super(source, cause);
    }
    
    protected KeyedValueEvent(Object source, KeyedValueEvent<K, T> dispatchSourceEvent) {
        super(source, dispatchSourceEvent);
    }
    
    @Override
    public abstract KeyedValueEvent<K, T> dispatch(Object source);
    
    @Override
    public abstract KeyedModification<K, T> getModification();
    
    public abstract K getKey(PropertyVersion version);
    
    @SuppressWarnings("unchecked")
    public static <K, T> KeyedValueEvent<K, T> createReplaceEvent(
            Object source, 
            KeyedModification<K, T> modification,
            K keyToDetach,
            K keyToAttach,
            T valueToDetach,
            T valueToAttach) {
        return ((Factory<K, T>)FACTORY).createReplaceEvent(
                source, 
                modification,
                keyToDetach,
                keyToAttach,
                valueToDetach, 
                valueToAttach);
    }
    
    @SuppressWarnings("unchecked")
    public static <K, T> KeyedValueEvent<K, T> bubbleEvent(
            Object source,
            Cause cause,
            BubbledPropertyConverter<K> keyConverter,
            BubbledPropertyConverter<T> valueConverter) {
        return ((Factory<K, T>)FACTORY).bubbleEvent(
                source, 
                cause, 
                keyConverter, 
                valueConverter);
    }
    
    public interface KeyedModification<K, T> extends Modification<T> {
        
    }
    
    @EventFactory
    private interface Factory<K, T> {
        
        KeyedValueEvent<K, T> createReplaceEvent(
                Object source, 
                KeyedModification<K, T> modification,
                K keyToDetach,
                K keyToAttach,
                T valueToDetach,
                T valueToAttach);
        
        KeyedValueEvent<K, T> bubbleEvent(
                Object source,
                Cause cause,
                BubbledPropertyConverter<K> keyConverter,
                BubbledPropertyConverter<T> valueConverter);
    }
    
}
