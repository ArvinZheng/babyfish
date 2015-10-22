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
package org.babyfish.reference;

import org.babyfish.lang.Singleton;

/**
 * @author Tao Chen
 */
public class ReferenceComparators {
    
    private static final ReferenceComparator<?> EMPTY = Empty.getInstance();

    @Deprecated
    protected ReferenceComparators() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
    
    @SuppressWarnings("unchecked")
    public static <T> ReferenceComparator<T> empty() {
        return (ReferenceComparator<T>)EMPTY;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> ReferenceComparator<T> nullToEmpty(ReferenceComparator<T> comparator) {
        if (comparator == null) {
            return (ReferenceComparator<T>)EMPTY;
        }
        return comparator;
    }
    
    public static <T> ReferenceComparator<T> emptyToNull(ReferenceComparator<T> comparator) {
        if (comparator == EMPTY) {
            return null;
        }
        return comparator;
    }
    
    private static class Empty extends Singleton implements ReferenceComparator<Object> {
        
        static Empty getInstance() {
            return getInstance(Empty.class);
        }
        
        @Override
        public boolean same(Object obj1, Object obj2) {
            return obj1 == obj2;
        }
    }
}
