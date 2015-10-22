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

import java.util.NavigableSet;

import org.babyfish.collection.event.spi.ConflictAbsoluteIndexes;
import org.babyfish.immutable.Autonomy;
import org.babyfish.modificationaware.event.BubbledPropertyConverter;
import org.babyfish.modificationaware.event.Cause;
import org.babyfish.modificationaware.event.EventDeclaration;
import org.babyfish.modificationaware.event.EventFactory;
import org.babyfish.modificationaware.event.EventProperty;
import org.babyfish.modificationaware.event.PropertyVersion;

/**
 * @author Tao Chen
 */
@EventDeclaration(properties = @EventProperty(name = "index"))
public abstract class ListElementEvent<E> extends ElementEvent<E> {

    private static final long serialVersionUID = 3324398334282566379L;
    
    private static final Factory<?> FACTORY = getFactory(Factory.class);
    
    protected ListElementEvent(Object source) {
        super(source);
    }
    
    protected ListElementEvent(Object source, Cause cause) {
        super(source, cause);
    }
    
    protected ListElementEvent(Object source, ListElementEvent<E> dispatchSourceEvent) {
        super(source, dispatchSourceEvent);
    }
    
    @Override
    public abstract ListElementEvent<E> dispatch(Object source);
    
    public abstract int getIndex(PropertyVersion version);
    
    @Autonomy
    public NavigableSet<Integer> getConflictAbsoluteIndexes() {
        return ConflictAbsoluteIndexes.get(this);
    }
    
    @SuppressWarnings("unchecked")
    public static <E> ListElementEvent<E> createDetachEvent(
            Object source, Modification<E> modification, E element, int index) {
        return ((Factory<E>)FACTORY).createDetachEvent(source, modification, element, index);
    }
    
    @SuppressWarnings("unchecked")
    public static <E> ListElementEvent<E> createAttachEvent(
            Object source, Modification<E> modification, E element, int index) {
        return ((Factory<E>)FACTORY).createAttachEvent(source, modification, element, index);
    }
    
    @SuppressWarnings("unchecked")
    public static <E> ListElementEvent<E> createReplaceEvent(
            Object source, 
            Modification<E> modification, 
            E detachedElement, 
            E attachedElement, 
            int detachedIndex,
            int attachedIndex) {
        return ((Factory<E>)FACTORY).createReplaceEvent(
                source, 
                modification, 
                detachedElement, 
                attachedElement, 
                detachedIndex,
                attachedIndex);
    }
    
    @SuppressWarnings("unchecked")
    public static <E> ListElementEvent<E> bubbleEvent(
            Object source, 
            Cause cause, 
            BubbledPropertyConverter<E> elementConverter, 
            BubbledPropertyConverter<Integer> indexConverter) {
        return ((Factory<E>)FACTORY).bubbleEvent(
                source, 
                cause, 
                elementConverter, 
                indexConverter);
    }
    
    @EventFactory
    private interface Factory<E> {
        
        ListElementEvent<E> createDetachEvent(
                Object source, 
                Modification<E> modification, 
                E element, 
                int detachedIndex);
        
        ListElementEvent<E> createAttachEvent(
                Object source, 
                Modification<E> modification, 
                E element, 
                int attachedIndex);
        
        ListElementEvent<E> createReplaceEvent(
                Object source, 
                Modification<E> modification, 
                E detachedElement, 
                E attachedElement, 
                int detachedIndex,
                int attachedIndex);
        
        ListElementEvent<E> bubbleEvent(
                Object source, 
                Cause cause, 
                BubbledPropertyConverter<E> elementConverter, 
                BubbledPropertyConverter<Integer> indexConverter);
    }
    
}
