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
package org.babyfish.collection.spi.base;

import java.util.Collection;

import org.babyfish.collection.ElementMatcher;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.conflict.ListConflictVoter;
import org.babyfish.validator.Validator;

/**
 * @author Tao Chen
 */
public interface BaseElements<E> extends BaseContainer {
    
    UnifiedComparator<? super E> unifiedComparator();
    
    boolean randomAccess();
    
    void combineValidator(Validator<E> validator);
    
    void removeValidator(Validator<E> validator);
    
    void validate(E element);
    
    void combineConflictVoter(ListConflictVoter<E> voter);
    
    void removeConflictVoter(ListConflictVoter<E> voter);
    
    int allSize();
    
    int indexOf(int subListHeadHide, int subListTailHide, Object o, ElementMatcher<? super E> matcher);
    
    int lastIndexOf(int subListHeadHide, int subListTailHide, Object o, ElementMatcher<? super E> matcher);
    
    E get(int subListHeadHide, int subListTailHide, int index);
    
    E set(
            int subListHeadHide, 
            int subListTailHide, 
            int index, 
            E element, 
            BaseElementsHandler<E> handler,
            BaseElementsConflictHandler rangeChangeHandler);
    
    void add(
            int subListHeadHide, 
            int subListTailHide, 
            int index, 
            E element, 
            BaseElementsHandler<E> handler,
            BaseElementsConflictHandler rangeChangeHandler);
    
    boolean addAll(
            int subListHeadHide, 
            int subListTailHide, 
            int index, 
            Collection<? extends E> c, 
            BaseElementsHandler<E> handler,
            BaseElementsConflictHandler rangeChangeHandler);
    
    void clear(int subListHeadHide, int subListTailHide, BaseElementsHandler<E> handler);

    boolean remove(int subListHeadHide, int subListTailHide, Object o, BaseElementsHandler<E> handler);
    
    E removeAt(int subListHeadHide, int subListTailHide, int index, BaseElementsHandler<E> handler);
    
    boolean removeAll(int subListHeadHide, int subListTailHide, Collection<?> c, BaseElementsHandler<E> handler);
    
    boolean retainAll(int subListHeadHide, int subListTailHide, Collection<?> c, BaseElementsHandler<E> handler);
    
    BaseListIterator<E> listIterator(
            int subListHeadHide, 
            int subListTailHide, 
            int index, 
            BaseElementsConflictHandler rangeChangeHandler);
}
