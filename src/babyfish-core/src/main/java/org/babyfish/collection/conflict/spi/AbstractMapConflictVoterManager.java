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

import org.babyfish.collection.conflict.MAMapConflictVoter;
import org.babyfish.collection.conflict.MapConflictVoter;
import org.babyfish.lang.Action;

/**
 * @author Tao Chen
 */
public abstract class AbstractMapConflictVoterManager<K, V> implements Serializable {

    private static final long serialVersionUID = 9052105332440412917L;
    
    @SuppressWarnings("rawtypes")
    private static final MapConflictVoter[] EMPTY_VOTERS = new MapConflictVoter[0];
    
    private static final int[] EMPTY_REFCOUNTS = new int[0];
    
    MapConflictVoter<K, V>[] voters;
    
    int[] refCounts;
    
    int len;
    
    int maCount;
    
    AbstractMapConflictVoterManager(
            MapConflictVoter<K, V>[] voters, 
            int[] refCounts, 
            int len, 
            int maCount) {
        if (maCount < 0) {
            maCount = 0;
            for (int i = len - 1; i >= 0; i--) {
                if (voters[i] instanceof MAMapConflictVoter<?, ?>) {
                    maCount++;
                }
            }
        }
        this.voters = voters;
        this.refCounts = refCounts;
        this.len = len;
        this.maCount = maCount;
    }
    
    public static boolean isModificationAware(AbstractMapConflictVoterManager<?, ?> voterManager) {
        return voterManager != null && voterManager.maCount != 0;
    }
    
    @SuppressWarnings("unchecked")
    static <K, V> AbstractMapConflictVoterManager<K, V> combineImpl(
            AbstractMapConflictVoterManager<K, V> a, 
            AbstractMapConflictVoterManager<K, V> b, 
            MapSource<K, V> mapSource) {
        if (b == null || b.len == 0) {
            return a;
        }
        if (a == null || a.len == 0) {
            if (mapSource != null) {
                if (b instanceof MapConflictVoterManager<?, ?>) {
                    return b;
                }
            } else {
                if (b instanceof TemporaryMapConflictVoterManager<?, ?>) {
                    return b;
                }
            }
        }
        MapConflictVoter<K, V>[] aVoters = a != null ? a.voters : EMPTY_VOTERS;
        int[] aRefCounts = a != null ? a.refCounts : EMPTY_REFCOUNTS;
        int aLen = a != null ? a.len : 0;
        int maCount = a != null ? a.maCount : 0;
        MapConflictVoter<K, V>[] bVoters = b.voters;
        int[] bRefCounts = b.refCounts;
        int bLen = b.len;
        int capacity = aLen + bLen;
        MapConflictVoter<K, V>[] voters = new MapConflictVoter[capacity];
        int[] refCounts = new int[capacity];
        int len = aLen;
        System.arraycopy(aVoters, 0, voters, 0, len);
        System.arraycopy(aRefCounts, 0, refCounts, 0, len);
        for (int bI = 0; bI < bLen; bI++) {
            MapConflictVoter<K, V> bVoter = bVoters[bI];
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
                if (bVoter instanceof MAMapConflictVoter<?, ?>) {
                    maCount++;
                }
            }
        }
        return of(mapSource, voters, refCounts, len, maCount);
    }
    
    @SuppressWarnings("unchecked")
    static <K, V> AbstractMapConflictVoterManager<K, V> removeImpl(
            AbstractMapConflictVoterManager<K, V> a, 
            AbstractMapConflictVoterManager<K, V> b, 
            MapSource<K, V> mapSource,
            Action<MAMapConflictVoter<K, V>> deleteHandler) {
        if (a == null || a.len == 0) {
            return null;
        }
        if (b == null || b.len == 0) {
            return a;
        }
        MapConflictVoter<K, V>[] aVoters = a.voters;
        int[] aRefCounts = a.refCounts;
        int aLen = a.len;
        MapConflictVoter<K, V>[] bVoters = b.voters;
        int[] bRefCounts = b.refCounts;
        int bLen = b.len;
        MapConflictVoter<K, V>[] voters = new MapConflictVoter[aLen];
        int[] refCounts = new int[aLen];
        int len = 0;
        int maCount = a.maCount;
        for (int aI = 0; aI < aLen; aI++) {
            MapConflictVoter<K, V> aVoter = aVoters[aI];
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
            } else {
                if (aVoter instanceof MAMapConflictVoter<?, ?>) {
                    maCount--;
                }
                if (deleteHandler != null) {
                    if (aVoter instanceof MAMapConflictVoter<?, ?>) {
                        deleteHandler.run((MAMapConflictVoter<K, V>)aVoter);
                    }
                }
            }
        }
        return of(mapSource, voters, refCounts, len, maCount);
    }
    
    static <K, V> AbstractMapConflictVoterManager<K, V> of(
            MapSource<K, V> mapSource,
            MapConflictVoter<K, V>[] voters, 
            int[] refCounts, 
            int len, 
            int maCount) {
        if (len == 0) {
            return null;
        }
        if (mapSource == null) {
            return new TemporaryMapConflictVoterManager<>(voters, refCounts, len, maCount);
        }
        return new MapConflictVoterManager<>(mapSource, voters, refCounts, len, maCount);
    }

    @SuppressWarnings("unchecked")
    static <K, V> AbstractMapConflictVoterManager<K, V> of(MapConflictVoter<K, V> voter) {
        if (voter == null) {
            return null;
        }
        if (voter instanceof TemporaryMapConflictVoterManager<?, ?>.BatchVoter) {
            return ((TemporaryMapConflictVoterManager<K, V>.BatchVoter)voter).manager();
        }
        MapConflictVoter<K, V>[] voters = new MapConflictVoter[] { voter };
        int maCount = voter instanceof MAMapConflictVoter<?, ?> ? 1 : 0;
        return new TemporaryMapConflictVoterManager<>(voters, new int[] { 1 }, 1, maCount);
    }
    
    abstract MapSource<K, V> mapSource();
    
    Object writeReplace() throws ObjectStreamException {
        return new Replacement<>(this);
    }
    
    private static class Replacement<K, V> implements Serializable {
        
        private static final long serialVersionUID = -5096927889666757040L;
        
        private transient AbstractMapConflictVoterManager<K, V> voterManager;
        
        Replacement(AbstractMapConflictVoterManager<K, V> voterManager) {
            this.voterManager = voterManager;
        }
        
        private void writeObject(ObjectOutputStream out) throws IOException {
            AbstractMapConflictVoterManager<K, V> voterManager = this.voterManager;
            out.writeObject(voterManager.mapSource());
            MapConflictVoter<K, V>[] voters = voterManager.voters;
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
            MapSource<K, V> mapSource = (MapSource<K, V>)in.readObject();
            int serializableCount = in.readInt();
            if (serializableCount != 0) {
                MapConflictVoter<K, V>[] voters = new MapConflictVoter[serializableCount];
                int[] refCounts = new int[serializableCount];
                for (int i = 0; i < serializableCount; i++) {
                    voters[i] = (MapConflictVoter<K, V>)in.readObject();
                    refCounts[i] = in.readInt();
                }
                this.voterManager = AbstractMapConflictVoterManager.of(mapSource, voters, refCounts, serializableCount, -1);
            } else {
                this.voterManager = null;
            }
        }
        
        final Object readResolve() throws ObjectStreamException {
            return this.voterManager;
        }
    }
}
