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
package org.babyfish.collection.conflict.spi;

import org.babyfish.collection.XMap;
import org.babyfish.collection.conflict.MapConflictVoter;
import org.babyfish.collection.conflict.MapConflictVoterArgs;

/**
 * @author Tao Chen
 */
public class TemporaryMapConflictVoterManager<K, V> extends AbstractMapConflictVoterManager<K, V> {

    private static final long serialVersionUID = -5510158913780821846L;

    TemporaryMapConflictVoterManager(
            MapConflictVoter<K, V>[] voters,
            int[] refCounts, 
            int len, 
            int maCount) {
        super(voters, refCounts, len, maCount);
    }
    
    public static <K, V> TemporaryMapConflictVoterManager<K, V> combine(
            TemporaryMapConflictVoterManager<K, V> voterManager, 
            MapConflictVoter<K, V> voter) {
        return (TemporaryMapConflictVoterManager<K, V>)combineImpl(voterManager, of(voter), null);
    }
    
    public static <K, V> TemporaryMapConflictVoterManager<K, V> remove(
            TemporaryMapConflictVoterManager<K, V> voterManager, 
            MapConflictVoter<K, V> voter) {
        return (TemporaryMapConflictVoterManager<K, V>)removeImpl(voterManager, of(voter), null, null);
    }
    
    public void addTo(XMap<K, V> map) {
        map.addConflictVoter(this.new BatchVoter());
    }
    
    public void removeFrom(XMap<K, V> map) {
        map.removeConflictVoter(this.new BatchVoter());
    }
    
    @Override
    MapSource<K, V> mapSource() {
        return null;
    }

    class BatchVoter implements MapConflictVoter<K, V> {
        
        TemporaryMapConflictVoterManager<K, V> manager() {
            return TemporaryMapConflictVoterManager.this;
        }

        @Override
        public void vote(MapConflictVoterArgs<K, V> args) {
            throw new UnsupportedOperationException();
        }
    }
}
