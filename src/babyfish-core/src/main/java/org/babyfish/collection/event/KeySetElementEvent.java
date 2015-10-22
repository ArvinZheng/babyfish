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
        properties = @EventProperty(name = "value", shared = true))
public abstract class KeySetElementEvent<K, V> extends ElementEvent<K> {
    
    private static final long serialVersionUID = -4544417201962006707L;
    
    private static final Factory<?, ?> FACTORY = getFactory(Factory.class);

    protected KeySetElementEvent(Object source) {
        super(source);
    }
    
    protected KeySetElementEvent(Object source, Cause cause) {
        super(source, cause);
    }
    
    protected KeySetElementEvent(Object source, KeySetElementEvent<K, V> dispatchSourceEvent) {
        super(source, dispatchSourceEvent);
    }
    
    public abstract V getValue();
    
    @Override
    public abstract KeySetElementEvent<K, V> dispatch(Object source);
    
    @SuppressWarnings("unchecked")
    public static <K, V> KeySetElementEvent<K, V> createDetachEvent(
            Object source, Modification<K> modification, K element, V value) {
        return ((Factory<K, V>)FACTORY).createDetachEvent(
                source, modification, element, value);
    }
    
    @SuppressWarnings("unchecked")
    public static <K, V> KeySetElementEvent<K, V> bubbleEvent(
            Object source, 
            Cause cause, 
            BubbledPropertyConverter<K> elementConverter, 
            BubbledSharedPropertyConverter<V> valueConverter) {
        return ((Factory<K, V>)FACTORY).bubbleEvent(
                source, cause, elementConverter, valueConverter);
    }
    
    @EventFactory
    private interface Factory<K, V> {
        
        KeySetElementEvent<K, V> createDetachEvent(
                Object source, Modification<K> modification, K element, V value);
        
        KeySetElementEvent<K, V> bubbleEvent(
                Object source, 
                Cause cause, 
                BubbledPropertyConverter<K> elementConverter, 
                BubbledSharedPropertyConverter<V> valueConverter);
    }
    
}
