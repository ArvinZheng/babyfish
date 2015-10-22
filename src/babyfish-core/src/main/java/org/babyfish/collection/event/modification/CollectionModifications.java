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
import org.babyfish.modificationaware.event.Modifications;

/**
 * @author Tao Chen
 */
public class CollectionModifications extends Modifications {
    
    private static final Factory FACTORY = getModificationFactory(Factory.class);

    protected CollectionModifications() {
        
    }
    
    public static <E> AddByElement<E> add(E element) {
        return FACTORY.add(element);
    }
    
    public static <E> AddAllByCollection<E> addAll(Collection<? extends E> c) {
        return FACTORY.addAll(c);
    }
    
    public static <E> Clear<E> clear() {
        return FACTORY.clear();
    }
    
    public static <E> RemoveByElement<E> remove(Object element) {
        return FACTORY.remove(element);
    }
    
    public static <E> RemoveAllByCollection<E> removeAll(Collection<?> c) {
        return FACTORY.removeAll(c);
    }
    
    public static <E> RetainAllByCollection<E> retainAll(Collection<?> c) {
        return FACTORY.retainAll(c);
    }
    
    @Parameters("element")
    public interface AddByElement<E> extends Modification<E> {
        
        E getElement();
        
    }
    
    @Parameters("collection")
    public interface AddAllByCollection<E> extends Modification<E> {
        
        Collection<? extends E> getCollection();
        
    }
    
    @Parameters
    public interface Clear<E> extends Modification<E> {
        
    }
    
    @Parameters("element")
    public interface RemoveByElement<E> extends Modification<E> {
        
        Object getElement();
        
    }
    
    @Parameters("collection")
    public interface RemoveAllByCollection<E> extends Modification<E> {
            
        Collection<?> getCollection();
        
    }

    @Parameters("collection")
    public interface RetainAllByCollection<E> extends Modification<E> {
        
        Collection<?> getCollection();
        
    }
    
    private interface Factory {
        
        <E> AddByElement<E> add(E element);
        
        <E> AddAllByCollection<E> addAll(Collection<? extends E> c);
        
        <E> Clear<E> clear();
        
        <E> RemoveByElement<E> remove(Object element);
        
        <E> RemoveAllByCollection<E> removeAll(Collection<?> c);
        
        <E> RetainAllByCollection<E> retainAll(Collection<?> c);
    }
    
}
