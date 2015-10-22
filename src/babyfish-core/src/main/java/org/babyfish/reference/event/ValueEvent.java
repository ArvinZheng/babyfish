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
        properties = @EventProperty(name = "value", shared = false))
public abstract class ValueEvent<T> extends ModificationEvent {

    private static final Factory<?> FACTORY = getFactory(Factory.class);
    
    private static final long serialVersionUID = -4752853601889848794L;

    protected ValueEvent(Object source) {
        super(source);
    }
    
    protected ValueEvent(Object source, Cause cause) {
        super(source, cause);
    }
    
    protected ValueEvent(Object source, ValueEvent<T> dispatchSourceEvent) {
        super(source, dispatchSourceEvent);
    }
    
    @Override
    public abstract ValueEvent<T> dispatch(Object source);
    
    @Override
    public abstract Modification<T> getModification();
    
    public abstract T getValue(PropertyVersion version);
    
    @SuppressWarnings("unchecked")
    public static <T> ValueEvent<T> createReplaceEvent(
            Object source, 
            Modification<T> modification, 
            T valueToDetach,
            T valueToAttach) {
        return ((Factory<T>)FACTORY).createReplaceEvent(
                source, 
                modification, 
                valueToDetach, 
                valueToAttach);
    }
    
    public interface Modification<T> extends org.babyfish.modificationaware.event.Modification {
        
    }
    
    @EventFactory
    private interface Factory<T> {
        
        ValueEvent<T> createReplaceEvent(
                Object source, 
                Modification<T> modification, 
                T valueToDetach,
                T valueToAttach);
    }
    
}
