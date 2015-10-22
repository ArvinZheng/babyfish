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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

import org.babyfish.collection.conflict.ListConflictVoter;

/**
 * @author Tao Chen
 */
public abstract class AbstractListConflictVoterManager<E> implements Serializable {

    private static final long serialVersionUID = 9052105332440412917L;
    
    @SuppressWarnings("rawtypes")
    private static final ListConflictVoter[] EMPTY_VOTERS = new ListConflictVoter[0];
    
    private static final int[] EMPTY_REFCOUNTS = new int[0];
    
    ListConflictVoter<E>[] voters;
    
    int[] refCounts;
    
    int len;
    
    AbstractListConflictVoterManager(
            ListConflictVoter<E>[] voters, 
            int[] refCounts, 
            int len) {
        this.voters = voters;
        this.refCounts = refCounts;
        this.len = len;
    }
    
    @SuppressWarnings("unchecked")
    static <E> AbstractListConflictVoterManager<E> combineImpl(
            AbstractListConflictVoterManager<E> a, 
            AbstractListConflictVoterManager<E> b, 
            ListSource<E> listSource) {
        if (b == null || b.len == 0) {
            return a;
        }
        if (a == null || a.len == 0) {
            if (listSource != null) {
                if (b instanceof ListConflictVoterManager<?>) {
                    return b;
                }
            } else {
                if (b instanceof TemporaryListConflictVoterManager<?>) {
                    return b;
                }
            }
        }
        ListConflictVoter<E>[] aVoters = a != null ? a.voters : EMPTY_VOTERS;
        int[] aRefCounts = a != null ? a.refCounts : EMPTY_REFCOUNTS;
        int aLen = a != null ? a.len : 0;
        ListConflictVoter<E>[] bVoters = b.voters;
        int[] bRefCounts = b.refCounts;
        int bLen = b.len;
        int capacity = aLen + bLen;
        ListConflictVoter<E>[] voters = new ListConflictVoter[capacity];
        int[] refCounts = new int[capacity];
        int len = aLen;
        System.arraycopy(aVoters, 0, voters, 0, len);
        System.arraycopy(aRefCounts, 0, refCounts, 0, len);
        for (int bI = 0; bI < bLen; bI++) {
            ListConflictVoter<E> bVoter = bVoters[bI];
            boolean matched = false;
            for (int aI = 0; aI < aLen; aI++) {
                if (aVoters[aI].equals(bVoter)) {
                    refCounts[aI] += bRefCounts[bI];
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                voters[len] = bVoter;
                refCounts[len] = bRefCounts[bI];
                len++;
            }
        }
        return of(listSource, voters, refCounts, len);
    }
    
    @SuppressWarnings("unchecked")
    static <E> AbstractListConflictVoterManager<E> removeImpl(
            AbstractListConflictVoterManager<E> a, 
            AbstractListConflictVoterManager<E> b, 
            ListSource<E> listSource) {
        if (a == null || a.len == 0) {
            return null;
        }
        if (b == null || b.len == 0) {
            return a;
        }
        ListConflictVoter<E>[] aVoters = a.voters;
        int[] aRefCounts = a.refCounts;
        int aLen = a.len;
        ListConflictVoter<E>[] bVoters = b.voters;
        int[] bRefCounts = b.refCounts;
        int bLen = b.len;
        ListConflictVoter<E>[] voters = new ListConflictVoter[aLen];
        int[] refCounts = new int[aLen];
        int len = 0;
        for (int aI = 0; aI < aLen; aI++) {
            ListConflictVoter<E> aVoter = aVoters[aI];
            int refCount = aRefCounts[aI];
            for (int bI = 0; bI < bLen; bI++) {
                if (aVoter.equals(bVoters[bI])) {
                    refCount -= bRefCounts[bI];
                    break;
                }
            }
            if (refCount > 0) {
                voters[len] = aVoter;
                refCounts[len] = refCount;
                len++;
            }
        }
        return of(listSource, voters, refCounts, len);
    }
    
    static <E> AbstractListConflictVoterManager<E> of(
            ListSource<E> listSource,
            ListConflictVoter<E>[] voters, 
            int[] refCounts, 
            int len) {
        if (len == 0) {
            return null;
        }
        if (listSource == null) {
            return new TemporaryListConflictVoterManager<>(voters, refCounts, len);
        }
        return new ListConflictVoterManager<>(listSource, voters, refCounts, len);
    }

    @SuppressWarnings("unchecked")
    static <E> AbstractListConflictVoterManager<E> of(ListConflictVoter<E> voter) {
        if (voter == null) {
            return null;
        }
        if (voter instanceof TemporaryListConflictVoterManager<?>.BatchVoter) {
            return ((TemporaryListConflictVoterManager<E>.BatchVoter)voter).manager();
        }
        ListConflictVoter<E>[] voters = new ListConflictVoter[] { voter };
        return new TemporaryListConflictVoterManager<>(voters, new int[] { 1 }, 1);
    }
    
    abstract ListSource<E> listSource();
    
    Object writeReplace() throws ObjectStreamException {
        return new Replacement<>(this);
    }
    
    private static class Replacement<E> implements Serializable {
        
        private static final long serialVersionUID = -5096927889666757040L;
        
        private transient AbstractListConflictVoterManager<E> voterManager;
        
        Replacement(AbstractListConflictVoterManager<E> voterManager) {
            this.voterManager = voterManager;
        }
        
        private void writeObject(ObjectOutputStream out) throws IOException {
            AbstractListConflictVoterManager<E> voterManager = this.voterManager;
            out.writeObject(voterManager.listSource());
            ListConflictVoter<E>[] voters = voterManager.voters;
            int len = voterManager.len;
            int serializableCount = 0;
            for (int i = 0; i < len; i++) {
                if (voters[i] instanceof Serializable) {
                    serializableCount++;
                }
            }
            out.writeInt(serializableCount);
            if (serializableCount != 0) {
                int[] refCounts = voterManager.refCounts;
                for (int i = 0; i < len; i++) {
                    if (voters[i] instanceof Serializable) {
                        out.writeObject(voters[i]);
                        out.writeInt(refCounts[i]);
                    }
                }
            }
        }
        
        @SuppressWarnings("unchecked")
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            ListSource<E> listSource = (ListSource<E>)in.readObject();
            int serializableCount = in.readInt();
            if (serializableCount != 0) {
                ListConflictVoter<E>[] voters = new ListConflictVoter[serializableCount];
                int[] refCounts = new int[serializableCount];
                for (int i = 0; i < serializableCount; i++) {
                    voters[i] = (ListConflictVoter<E>)in.readObject();
                    refCounts[i] = in.readInt();
                }
                this.voterManager = AbstractListConflictVoterManager.of(listSource, voters, refCounts, serializableCount);
            } else {
                this.voterManager = null;
            }
        }
        
        final Object readResolve() throws ObjectStreamException {
            return this.voterManager;
        }
    }
}
