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
package org.babyfish.collection;

import java.util.List;
import java.util.ListIterator;

import org.babyfish.collection.conflict.ListConflictVoter;
import org.babyfish.view.View;
import org.babyfish.view.ViewInfo;

/**
 * @author Tao Chen
 */
public interface XList<E> extends List<E>, XCollection<E> {
    
    void addConflictVoter(ListConflictVoter<E> voter);
    
    void removeConflictVoter(ListConflictVoter<E> voter);
    
    int indexOf(Object o, ElementMatcher<? super E> matcher);
    
    int lastIndexOf(Object o, ElementMatcher<? super E> matcher);
    
    @Override
    XListView<E> subList(int fromIndex, int toIndex);
    
    @Override
    XListIterator<E> iterator();
    
    @Override
    XListIterator<E> listIterator();
    
    @Override
    XListIterator<E> listIterator(int index);
    
    interface XListIterator<E> extends XIterator<E>, ListIterator<E> {
        
        @Override
        ViewInfo viewInfo();
    }
    
    interface XListView<E> extends XList<E>, View {
    }
}
