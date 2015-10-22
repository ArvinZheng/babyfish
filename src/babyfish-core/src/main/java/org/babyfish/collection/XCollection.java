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

import java.util.Collection;
import java.util.Iterator;

import org.babyfish.lang.LockDescriptor;
import org.babyfish.validator.Validator;
import org.babyfish.view.View;
import org.babyfish.view.ViewInfo;

/**
 * @author Tao Chen
 */
public interface XCollection<E> extends Collection<E>, LockDescriptor {
    
    UnifiedComparator<? super E> unifiedComparator();
    
    void addValidator(Validator<E> validator);
    
    void removeValidator(Validator<E> validator);
    
    void validate(E e);
    
    boolean contains(Object o, ElementMatcher<? super E> matcher);
    
    boolean containsAll(Collection<?> c, ElementMatcher<? super E> matcher);
    
    @Override
    XIterator<E> iterator();
    
    interface XIterator<E> extends Iterator<E>, View {
        
        UnifiedComparator<? super E> unifiedComparator();
        
        @Override
        ViewInfo viewInfo();
    }
    
}
