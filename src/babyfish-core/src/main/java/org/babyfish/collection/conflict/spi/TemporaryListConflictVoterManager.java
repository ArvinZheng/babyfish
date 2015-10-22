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

import org.babyfish.collection.XList;
import org.babyfish.collection.conflict.ListConflictVoter;
import org.babyfish.collection.conflict.ListConflictVoterArgs;

/**
 * @author Tao Chen
 */
public class TemporaryListConflictVoterManager<E> extends AbstractListConflictVoterManager<E> {

    private static final long serialVersionUID = -5510158913780821846L;

    TemporaryListConflictVoterManager(
            ListConflictVoter<E>[] voters,
            int[] refCounts, 
            int len) {
        super(voters, refCounts, len);
    }
    
    public static <E> TemporaryListConflictVoterManager<E> combine(
            TemporaryListConflictVoterManager<E> voterManager, 
            ListConflictVoter<E> voter) {
        return (TemporaryListConflictVoterManager<E>)combineImpl(voterManager, of(voter), null);
    }
    
    public static <E> TemporaryListConflictVoterManager<E> remove(
            TemporaryListConflictVoterManager<E> voterManager, 
            ListConflictVoter<E> voter) {
        return (TemporaryListConflictVoterManager<E>)removeImpl(voterManager, of(voter), null);
    }
    
    public void addTo(XList<E> list) {
        list.addConflictVoter(this.new BatchVoter());
    }
    
    public void removeFrom(XList<E> list) {
        list.removeConflictVoter(this.new BatchVoter());
    }
    
    @Override
    ListSource<E> listSource() {
        return null;
    }

    class BatchVoter implements ListConflictVoter<E> {
        
        TemporaryListConflictVoterManager<E> manager() {
            return TemporaryListConflictVoterManager.this;
        }

        @Override
        public void vote(ListConflictVoterArgs<E> args) {
            throw new UnsupportedOperationException();
        }
    }
}
