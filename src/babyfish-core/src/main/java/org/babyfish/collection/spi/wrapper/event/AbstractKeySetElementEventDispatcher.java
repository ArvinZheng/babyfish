/*
 * BabyFish, Object Model Framework for Java and JPA.
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
package org.babyfish.collection.spi.wrapper.event;

import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.event.KeySetElementEvent;

/**
 * @author Tao Chen
 */
public abstract class AbstractKeySetElementEventDispatcher<K, V> extends AbstractElementEventDispatcher<K> {

    protected AbstractKeySetElementEventDispatcher(Object owner) {
        super(owner);
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    @Override
    protected final void executePreDispatchedEvent(ElementEvent<K> dispatchedEvent) {
        this.executePreDispatchedEvent((KeySetElementEvent<K, V>)dispatchedEvent);
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    @Override
    protected final void executePostDispatchedEvent(ElementEvent<K> dispatchedEvent) {
        this.executePostDispatchedEvent((KeySetElementEvent<K, V>)dispatchedEvent);
    }
    
    protected abstract void executePreDispatchedEvent(KeySetElementEvent<K, V> dispatchedEvent);

    protected abstract void executePostDispatchedEvent(KeySetElementEvent<K, V> dispatchedEvent);

}
