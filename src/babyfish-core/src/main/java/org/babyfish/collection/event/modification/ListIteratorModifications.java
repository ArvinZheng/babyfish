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
public class ListIteratorModifications extends IteratorModifications {

    private static final Factory FACTORY = getModificationFactory(Factory.class);
    
    protected ListIteratorModifications() {
        
    }
    
    public static <E> AddByElement<E> add(E element) {
        return FACTORY.add(element);
    }
    
    public static <E> SetByElement<E> set(E element) {
        return FACTORY.set(element);
    }
    
    @Parameters("element")
    public interface AddByElement<E> extends Modification<E> {
        
        E getElement();
        
    }
    
    @Parameters("element")
    public interface SetByElement<E> extends Modification<E> {
        
        E getElement();
        
    }
    
    private interface Factory {
        
        <E> AddByElement<E> add(E element);
        
        <E> SetByElement<E> set(E element);
        
    }
    
}
