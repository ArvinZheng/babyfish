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
package org.babyfish.collection.event.modification;

import org.babyfish.collection.event.ElementEvent.Modification;
import org.babyfish.immutable.Parameters;


/**
 * @author Tao Chen
 */
public class SetModifications extends CollectionModifications {
    
    private static final Factory FACTORY = getModificationFactory(Factory.class);

    protected SetModifications() {
        
    }
    
    public static <E> SuspendByElementViaFrozenContext<E> suspendViaFrozenContext(E element) {
        return FACTORY.suspendViaFrozenContext(element);
    }
    
    public static <E> ResumeViaFrozenContext<E> resumeViaFrozenContext() {
        return FACTORY.resumeViaFrozenContext();
    }
    
    @Parameters("element")
    public interface SuspendByElementViaFrozenContext<E> extends Modification<E> {
        E getElement();
    }
    
    @Parameters
    public interface ResumeViaFrozenContext<E> extends Modification<E> {
    }
    
    private interface Factory {
        
        <E> SuspendByElementViaFrozenContext<E> suspendViaFrozenContext(E element); 
        
        <E> ResumeViaFrozenContext<E> resumeViaFrozenContext();
    }
}
