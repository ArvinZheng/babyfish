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

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NavigableSet;
import java.util.Set;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.TreeSet;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XList;
import org.babyfish.collection.XNavigableSet;
import org.babyfish.collection.XNavigableSet.XNavigableSetView;
import org.babyfish.collection.conflict.ListConflictVoter;
import org.babyfish.collection.conflict.ListConflictVoterArgs;
import org.babyfish.collection.conflict.ListReader;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.Ref;
import org.babyfish.util.LazyResource;

/**
 * @author Tao Chen
 */
public class ListConflictVoterManager<E> extends AbstractListConflictVoterManager<E> {
    
    private static final long serialVersionUID = 5134215833220952460L;
    
    private static LazyResource<CommonResource> LAZY_COMMON_RESOURCE = LazyResource.of(CommonResource.class);
    
    private ListSource<E> listSource;

    ListConflictVoterManager(
            ListSource<E> listSource,
            ListConflictVoter<E>[] voters, 
            int[] refCounts,
            int len) {
        super(voters, refCounts, len);
        this.listSource = listSource;
    }
    
    public static <E> ListConflictVoterManager<E> combine(
            ListConflictVoterManager<E> voterManager, 
            ListConflictVoter<E> voter, 
            ListSource<E> listSource) {
        Arguments.mustNotBeNull("listSource", listSource);
        if (voterManager != null) {
            Arguments.mustBeEqualToOtherWhen(
                    LAZY_COMMON_RESOURCE.get().whenParameterIsNull("voterManager"),
                    "listSource", 
                    listSource,
                    "voterManager.listSource", 
                    voterManager.listSource());
        }
        return (ListConflictVoterManager<E>)combineImpl(voterManager, of(voter), listSource);
    }
    
    public static <E> ListConflictVoterManager<E> remove(
            ListConflictVoterManager<E> voterManager, 
            ListConflictVoter<E> voter, 
            ListSource<E> listSource) {
        Arguments.mustNotBeNull("listSource", listSource);
        if (voterManager != null) {
            Arguments.mustBeEqualToOtherWhen(
                    LAZY_COMMON_RESOURCE.get().whenParameterIsNull("voterManager"),
                    "listSource", 
                    listSource,
                    "voterManager.listSource", 
                    voterManager.listSource());
        }
        return (ListConflictVoterManager<E>)removeImpl(
                voterManager, 
                of(voter), 
                listSource);
    }
    
    public NavigableSet<Integer> voteAll(Ref<Collection<? extends E>> collectionRef) {
        Arguments.mustNotBeNull("collectionRef", collectionRef);
        Collection<? extends E> deltaCollection = collectionRef.get();
        Arguments.mustNotBeNull("collectionRef.get()", deltaCollection);
        VoteAllArgsImpl args = this.new VoteAllArgsImpl(deltaCollection.size());
        try {
            for (E element : deltaCollection) {
                args.vote(element);
            }
        } finally {
            args.finish();
        }
        collectionRef.set(args.deltaList());
        return args.conflictIndexes();
    }
    
    public NavigableSet<Integer> vote(E element) {
        VoteArgsImpl args = this.new VoteArgsImpl(element);
        this.invokeVote(args);
        return args.conflictIndexes();
    }
    
    private void invokeVote(ListConflictVoterArgs<E> args) {
        ListConflictVoter<E>[] voters = this.voters;
        for (ListConflictVoter<E> voter : voters) {
            voter.vote(args);
        }
    }
    
    @Override
    public ListSource<E> listSource() {
        return this.listSource;
    }

    private class VoteAllArgsImpl implements ListConflictVoterArgs<E> {
        
        private XList<E> list;
        
        private XList<E> deltaList;
        
        private int modCount;
        
        private XNavigableSet<Integer> conflictIndexes;
        
        private E newElement;
        
        VoteAllArgsImpl(int size) {
            XList<E> list = ListConflictVoterManager.this.listSource.getList();
            this.list = list;
            this.deltaList = new ArrayList<>(list.unifiedComparator(), size);
            this.conflictIndexes = new TreeSet<>();
        }
        
        @Override
        public UnifiedComparator<? super E> unifiedComparator() {
            return this.list.unifiedComparator();
        }

        @Override
        public ListReader<E> reader() {
            return this.new Reader();
        }

        @Override
        public E newElement() {
            return this.newElement;
        }

        @Override
        public void conflictFound(int conflictIndex) {
            if (conflictIndex >= 0 && conflictIndex < this.list.size() + this.deltaList.size()) {
                this.conflictIndexes.add(conflictIndex);
            }
        }
        
        @Override
        public void conflictFound(Collection<Integer> conflictIndexes) {
            XNavigableSet<Integer> set = this.conflictIndexes;
            for (Integer conflictIndex : conflictIndexes) {
                if (conflictIndex >= 0 && conflictIndex < this.list.size() + this.deltaList.size()) {
                    set.add(conflictIndex);
                }
            }
        }
        
        List<? extends E> deltaList() {
            return this.deltaList;
        }
        
        void vote(E element) {
            this.newElement = element;
            XList<E> deltaList = this.deltaList;
            ListConflictVoterManager.this.invokeVote(this);
            deltaList.add(element);
            this.modCount++;
        }
        
        void finish() {
            int offset = this.list.size();
            XNavigableSetView<Integer> tailSet = this.conflictIndexes.tailSet(offset);
            for (Integer index : tailSet.descendingSet()) {
                deltaList.remove(index - offset);
            }
            tailSet.clear();
        }
        
        NavigableSet<Integer> conflictIndexes() {
            return this.conflictIndexes;
        }
        
        private class Reader implements ListReader<E> {
            
            private int expectedModCount;
            
            private Iterator<E> listItr;
            
            private Iterator<E> deltaListItr;
            
            private int index;
            
            private E element;
            
            Reader() {
                VoteAllArgsImpl owner = VoteAllArgsImpl.this;
                this.expectedModCount = owner.modCount;
                this.listItr = owner.list.iterator();
                this.deltaListItr = owner.deltaList.iterator();
                this.index = -1;
            }
            
            @Override
            public boolean read() {
                this.checkModCount();
                Iterator<E> itr = this.listItr;
                VoteAllArgsImpl owner = VoteAllArgsImpl.this;
                Set<Integer> conflictIndexes = owner.conflictIndexes;
                int index = this.index;
                E e;
                while (itr.hasNext()) {
                    e = itr.next();
                    int i = ++index;
                    if (conflictIndexes.contains(i)) {
                        continue;
                    }
                    this.index = i;
                    this.element = e;
                    return true;
                }
                itr = this.deltaListItr;
                while (itr.hasNext()) {
                    e = itr.next();
                    int i = ++index;
                    if (conflictIndexes.contains(i)) {
                        continue;
                    }
                    this.index = i;
                    this.element = e;
                    return true;
                }
                this.index = -1;
                this.element = null;
                return false;
            }

            @Override
            public int index() {
                this.checkModCount();
                return this.index;
            }

            @Override
            public E element() {
                this.checkModCount();
                return this.element;
            }
            
            private void checkModCount() {
                if (this.expectedModCount != VoteAllArgsImpl.this.modCount) {
                    throw new ConcurrentModificationException();
                }
            }
        }
    }
    
    private class VoteArgsImpl implements ListConflictVoterArgs<E> {

        private XList<E> list;
        
        private E newElement;
        
        private XNavigableSet<Integer> conflictIndexes;
        
        VoteArgsImpl(E element) {
            this.list = ListConflictVoterManager.this.listSource.getList();
            this.newElement = element;
            this.conflictIndexes = new TreeSet<>();
        }
        
        @Override
        public UnifiedComparator<? super E> unifiedComparator() {
            return this.list.unifiedComparator();
        }
        
        @Override
        public ListReader<E> reader() {
            return this.new Reader();
        }

        @Override
        public E newElement() {
            return this.newElement;
        }

        @Override
        public void conflictFound(int conflictIndex) {
            if (conflictIndex >= 0 && conflictIndex < this.list.size()) {
                this.conflictIndexes.add(conflictIndex);
            }
        }
        
        @Override
        public void conflictFound(Collection<Integer> conflictIndexes) {
            XNavigableSet<Integer> set = this.conflictIndexes;
            for (Integer conflictIndex : conflictIndexes) {
                if (conflictIndex >= 0 && conflictIndex < this.list.size()) {
                    set.add(conflictIndex);
                }
            }
        }
        
        NavigableSet<Integer> conflictIndexes() {
            return this.conflictIndexes;
        }
        
        private class Reader implements ListReader<E> {
            
            private ListIterator<E> itr;
            
            private E e;
            
            Reader() {
                this.itr = VoteArgsImpl.this.list.iterator();
            }

            @Override
            public boolean read() {
                if (!itr.hasNext()) {
                    return false;
                }
                this.e = itr.next();
                return true;
            }

            @Override
            public int index() {
                return this.itr.nextIndex() - 1;
            }

            @Override
            public E element() {
                return this.e;
            }
        }
    }
}
