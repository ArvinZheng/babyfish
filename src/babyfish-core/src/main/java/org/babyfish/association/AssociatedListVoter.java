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
package org.babyfish.association;

import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.conflict.ListConflictVoter;
import org.babyfish.collection.conflict.ListConflictVoterArgs;
import org.babyfish.collection.conflict.ListReader;

/**
 * @author Tao Chen
 */
final class AssociatedListConflictVoter<E> implements ListConflictVoter<E> {
    
    private Object identity;
    
    public AssociatedListConflictVoter(Object identity) {
        this.identity = identity;
    }

    @Override
    public void vote(ListConflictVoterArgs<E> args) {
        UnifiedComparator<? super E> unifiedComparator = args.unifiedComparator();
        E newElement = args.newElement();
        ListReader<E> reader = args.reader();
        while (reader.read()) {
            E element = reader.element();
            if (element != null) {
                if (unifiedComparator.equals(newElement, element)) {
                    args.conflictFound(reader.index());
                }
            }
        }
    }
    
    @Override
    public int hashCode() {
        return System.identityHashCode(this.identity);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AssociatedListConflictVoter<?>)) {
            return false;
        }
        AssociatedListConflictVoter<?> other = (AssociatedListConflictVoter<?>)obj;
        return this.identity == other.identity;
    }
    
}
