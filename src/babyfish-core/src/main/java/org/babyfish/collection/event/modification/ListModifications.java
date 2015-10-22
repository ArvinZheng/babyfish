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

import java.util.Collection;

import org.babyfish.collection.event.ElementEvent.Modification;
import org.babyfish.immutable.Parameters;

/**
 * @author Tao Chen
 */
public class ListModifications extends CollectionModifications {

    private static final Factory FACTORY = getModificationFactory(Factory.class);
    
    protected ListModifications() {
        
    }
    
    public static <E> AddByIndexAndElement<E> add(int index, E element) {
        return FACTORY.add(index, element);
    }
    
    public static <E> AddAllByIndexAndCollection<E> addAll(int index, Collection<? extends E> c) {
        return FACTORY.addAll(index, c);
    }
    
    public static <E> RemoveByIndex<E> remove(int index) {
        return FACTORY.remove(index);
    }
    
    public static <E> SetByIndexAndElement<E> set(int index, E element) {
        return FACTORY.set(index, element);
    }
    
    @Parameters("index, element")
    public interface AddByIndexAndElement<E> extends CollectionModifications.AddByElement<E> {
        
        int getIndex();
        
    }
    
    @Parameters("index, collection")
    public interface AddAllByIndexAndCollection<E> extends CollectionModifications.AddAllByCollection<E> {
        
        int getIndex();
        
    }
    
    @Parameters("index")
    public interface RemoveByIndex<E> extends Modification<E> {
        
        int getIndex();
        
    }
    
    @Parameters("index, element")
    public interface SetByIndexAndElement<E> extends Modification<E> {
        
        int getIndex();
        
        E getElement();
    }
    
    private interface Factory {
        
        <E> AddByIndexAndElement<E> add(int index, E element);
        
        <E> AddAllByIndexAndCollection<E> addAll(int index, Collection<? extends E> c);
        
        <E> RemoveByIndex<E> remove(int index);
        
        <E> SetByIndexAndElement<E> set(int index, E element);
    }
}
