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
import org.babyfish.collection.conflict.MapConflictVoter;
import org.babyfish.collection.conflict.MapConflictVoterArgs;
import org.babyfish.collection.conflict.MapReader;

/**
 * @author Tao Chen
 */
final class AssociatedMapConflictVoter<K, V> implements MapConflictVoter<K, V> {
    
    private Object identity;
    
    public AssociatedMapConflictVoter(Object identity) {
        this.identity = identity;
    }
    
    @Override
    public void vote(MapConflictVoterArgs<K, V> args) {
        UnifiedComparator<? super V> valueComparator = args.valueUnifiedComparator();
        V newValue = args.newValue();
        MapReader<K, V> reader = args.reader();
        while (reader.read()) {
            if (valueComparator.equals(newValue, reader.value())) {
                args.conflictFound(reader.key());
            }
        }
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this.identity);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AssociatedMapConflictVoter<?, ?>)) {
            return false;
        }
        AssociatedMapConflictVoter<K, V> other = (AssociatedMapConflictVoter<K, V>)obj;
        return this.identity == other.identity;
    }
    
}

