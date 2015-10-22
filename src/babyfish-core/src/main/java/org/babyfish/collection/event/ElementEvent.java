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
import org.babyfish.modificationaware.event.Cause;
import org.babyfish.modificationaware.event.EventDeclaration;
import org.babyfish.modificationaware.event.EventFactory;
import org.babyfish.modificationaware.event.EventProperty;
import org.babyfish.modificationaware.event.ModificationEvent;
import org.babyfish.modificationaware.event.PropertyVersion;

/**
 * @author Tao Chen
 */
@EventDeclaration(
        properties = @EventProperty(name = "element", shared = false))
public abstract class ElementEvent<E> extends ModificationEvent {
    
    private static final long serialVersionUID = -9010088909717171149L;
    
    private static final Factory<?> FACTORY = getFactory(Factory.class);

    protected ElementEvent(Object source) {
        super(source);
    }
    
    protected ElementEvent(Object source, Cause cause) {
        super(source, cause);
    }
    
    protected ElementEvent(Object source, ElementEvent<E> dispatchSourceEvent) {
        super(source, dispatchSourceEvent);
    }
    
    @Override
    public abstract ElementEvent<E> dispatch(Object source);
    
    @Override
    public abstract Modification<E> getModification();
    
    public abstract E getElement(PropertyVersion version);
    
    @SuppressWarnings("unchecked")
    public static <E> ElementEvent<E> createDetachEvent(
            Object source, Modification<E> modification, E element) {
        return ((Factory<E>)FACTORY).createDetachEvent(source, modification, element);
    }
    
    @SuppressWarnings("unchecked")
    public static <E> ElementEvent<E> createAttachEvent(
            Object source, Modification<E> modification, E element) {
        return ((Factory<E>)FACTORY).createAttachEvent(source, modification, element);
    }
    
    @SuppressWarnings("unchecked")
    public static <E> ElementEvent<E> createReplaceEvent(
            Object source, Modification<E> modification, E detachedElement, E attachedElement) {
        return ((Factory<E>)FACTORY).createReplaceEvent(
                source, modification, detachedElement, attachedElement);
    }
    
    @SuppressWarnings("unchecked")
    public static <E> ElementEvent<E> bubbleEvent(
            Object source, Cause cause, BubbledPropertyConverter<E> elementConverter) {
        return ((Factory<E>)FACTORY).bubbleEvent(source, cause, elementConverter);
    }
    
    public interface Modification<E> extends org.babyfish.modificationaware.event.Modification {
        
    }
    
    @EventFactory
    private interface Factory<E> {
        
        ElementEvent<E> createDetachEvent(
                Object source, Modification<E> modification, E element);
        
        ElementEvent<E> createAttachEvent(
                Object source, Modification<E> modification, E element);
        
        ElementEvent<E> createReplaceEvent(
                Object source, Modification<E> modification, E detachedElement, E attachedElement);
        
        ElementEvent<E> bubbleEvent(
                Object source, Cause cause, BubbledPropertyConverter<E> elementConverter);
    }
    
}
