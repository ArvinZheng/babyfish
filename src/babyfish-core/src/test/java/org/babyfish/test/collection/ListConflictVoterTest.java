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
package org.babyfish.test.collection;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import junit.framework.Assert;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.LinkedList;
import org.babyfish.collection.MAArrayList;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.MALinkedList;
import org.babyfish.collection.MAList;
import org.babyfish.collection.XList;
import org.babyfish.collection.conflict.ListConflictVoter;
import org.babyfish.collection.conflict.ListConflictVoterArgs;
import org.babyfish.collection.conflict.ListReader;
import org.babyfish.collection.event.ListElementAdapter;
import org.babyfish.collection.event.ListElementEvent;
import org.babyfish.collection.spi.laziness.AbstractLazyMAList;
import org.babyfish.collection.spi.laziness.AbstractLazyXList;
import org.babyfish.collection.spi.wrapper.AbstractWrapperMAList;
import org.babyfish.collection.spi.wrapper.AbstractWrapperXList;
import org.babyfish.collection.viewinfo.ListViewInfos;
import org.babyfish.lang.Arguments;
import org.babyfish.modificationaware.event.PropertyVersion;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class ListConflictVoterTest {
    
    @Test
    public void testAdd() {
        new Stub(new ArrayList<String>()).testAdd();
        new Stub(new LinkedList<String>()).testAdd();
        new Stub(new MAArrayList<String>()).testAdd();
        new Stub(new MALinkedList<String>()).testAdd();
        new Stub(new SimpleWrapperXList<String>(new LinkedList<String>()) {}).testAdd();
        new Stub(new SimpleWrapperMAList<String>(new MALinkedList<String>()) {}).testAdd();
        new Stub(new SimpleLazyXList<String>(new LinkedList<String>()) {}).testAdd();
        new Stub(new SimpleLazyMAList<String>(new MALinkedList<String>()) {}).testAdd();
    }
    
    @Test
    public void testAddAll() {
        new Stub(new ArrayList<String>()).testAddAll();
        new Stub(new LinkedList<String>()).testAddAll();
        new Stub(new MAArrayList<String>()).testAddAll();
        new Stub(new MALinkedList<String>()).testAddAll();
        new Stub(new SimpleWrapperXList<String>(new LinkedList<String>()) {}).testAddAll();
        new Stub(new SimpleWrapperMAList<String>(new MALinkedList<String>()) {}).testAddAll();
        new Stub(new SimpleLazyXList<String>(new LinkedList<String>()) {}).testAddAll();
        new Stub(new SimpleLazyMAList<String>(new MALinkedList<String>()) {}).testAddAll();
    }
    
    @Test
    public void testSet() {
        new Stub(new ArrayList<String>()).testSet();
        new Stub(new LinkedList<String>()).testSet();
        new Stub(new MAArrayList<String>()).testSet();
        new Stub(new MALinkedList<String>()).testSet();
        new Stub(new SimpleWrapperXList<String>(new LinkedList<String>()) {}).testSet();
        new Stub(new SimpleWrapperMAList<String>(new MALinkedList<String>()) {}).testSet();
        new Stub(new SimpleLazyXList<String>(new LinkedList<String>()) {}).testSet();
        new Stub(new SimpleLazyMAList<String>(new MALinkedList<String>()) {}).testSet();
    }
    
    @Test
    public void testIteratorAdd() {
        new Stub(new ArrayList<String>()).testIteratorAdd();
        new Stub(new LinkedList<String>()).testIteratorAdd();
        new Stub(new MAArrayList<String>()).testIteratorAdd();
        new Stub(new MALinkedList<String>()).testIteratorAdd();
        new Stub(new SimpleWrapperXList<String>(new LinkedList<String>()) {}).testIteratorAdd();
        new Stub(new SimpleWrapperMAList<String>(new MALinkedList<String>()) {}).testIteratorAdd();
        new Stub(new SimpleLazyXList<String>(new LinkedList<String>()) {}).testIteratorAdd();
        new Stub(new SimpleLazyMAList<String>(new MALinkedList<String>()) {}).testIteratorAdd();
    }
    
    @Test
    public void testIteratorSet() {
        new Stub(new ArrayList<String>()).testIteratorSet();
        new Stub(new LinkedList<String>()).testIteratorSet();
        new Stub(new MAArrayList<String>()).testIteratorSet();
        new Stub(new MALinkedList<String>()).testIteratorSet();
        new Stub(new SimpleWrapperXList<String>(new LinkedList<String>()) {}).testIteratorSet();
        new Stub(new SimpleWrapperMAList<String>(new MALinkedList<String>()) {}).testIteratorSet();
        new Stub(new SimpleLazyXList<String>(new LinkedList<String>()) {}).testIteratorSet();
        new Stub(new SimpleLazyMAList<String>(new MALinkedList<String>()) {}).testIteratorSet();
    }

    private static class Stub {
        
        private XList<String> list;
        
        private XList<String> subList1;
        
        private XList<String> subList2;
        
        private ListenerImpl<String> listenerImpl;

        public Stub(XList<String> list) {
            Arguments.mustBeEqualToValue("list.isEmpty()", list.isEmpty(), true);
            this.list = list;
            list.addConflictVoter(new ListConflictVoter<String>() {
                @Override
                public void vote(ListConflictVoterArgs<String> args) {
                    String conflictPrefix = null;
                    if (args.newElement().startsWith("+")) {
                        conflictPrefix = "-";
                    } else if (args.newElement().startsWith("-")) {
                        conflictPrefix = "+";
                    }
                    if (conflictPrefix != null) {
                        ListReader<String> reader = args.reader();
                        while (reader.read()) {
                            if (reader.element().startsWith(conflictPrefix)) {
                                args.conflictFound(reader.index());
                            }
                        }
                    }
                }
            });
            list.addAll(MACollections.wrap("+A", "A", "+B", "B", "+C", "C", "+D", "D", "+E", "E"));
            assertCollection(list, "+A", "A", "+B", "B", "+C", "C", "+D", "D", "+E", "E");
            
            this.subList1 = list.subList(2, 8);
            assertCollection(this.subList1, "+B", "B", "+C", "C", "+D", "D");
            assertSubListBound(this.subList1, 2, 8);
            
            this.subList2 = this.subList1.subList(2, 4);
            assertCollection(this.subList2, "+C", "C");
            assertSubListBound(this.subList2, 2, 4);
            
            if (list instanceof MAList<?>) {
                ((MAList<String>)list).addListElementListener(this.listenerImpl = new ListenerImpl<>());
            }
        }
        
        public void testAdd() {
            
            this.subList2.add(1, "-C");
            
            assertCollection(this.list, "A", "B", "-C", "C", "D", "E");
            
            assertCollection(subList1, "B", "-C", "C", "D");
            assertSubListBound(this.subList1, 1, 5);
            
            assertCollection(subList2, "-C", "C");
            assertSubListBound(this.subList2, 1, 3);
            
            if (this.listenerImpl == null) {
                return;
            }
            Assert.assertEquals(
                    "ListElementEvent{ "
                    +   "detachedIndex: 0, "
                    +   "detachedElement: +A, "
                    +   "cause: { "
                    +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +       "fromIndex : 2, "
                    +       "toIndex : 8 "
                    +     "}, "
                    +     "event: ListElementEvent{ "
                    +       "detachedIndex: -2, "
                    +       "detachedElement: +A, "
                    +       "cause: { "
                    +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +           "fromIndex : 2, "
                    +           "toIndex : 4 "
                    +         "}, "
                    +         "event: ListElementEvent{ "
                    +           "detachedIndex: -4, "
                    +           "detachedElement: +A, "
                    +           "modification: org.babyfish.collection.event.modification.ListModifications$AddByIndexAndElement{ "
                    +             "index : 1, "
                    +             "element : -C "
                    +           "} "
                    +         "} "
                    +       "} "
                    +     "} "
                    +   "} "
                    + "}"
                    + "ListElementEvent{ "
                    +   "detachedIndex: 2, "
                    +   "detachedElement: +B, "
                    +   "cause: { "
                    +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +       "fromIndex : 2, "
                    +       "toIndex : 8 "
                    +     "}, "
                    +     "event: ListElementEvent{ "
                    +       "detachedIndex: 0, "
                    +       "detachedElement: +B, "
                    +       "cause: { "
                    +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +           "fromIndex : 2, "
                    +           "toIndex : 4 "
                    +         "}, "
                    +         "event: ListElementEvent{ "
                    +           "detachedIndex: -2, "
                    +           "detachedElement: +B, "
                    +           "modification: org.babyfish.collection.event.modification.ListModifications$AddByIndexAndElement{ "
                    +             "index : 1, "
                    +             "element : -C "
                    +           "} "
                    +         "} "
                    +       "} "
                    +     "} "
                    +   "} "
                    + "}"
                    + "ListElementEvent{ "
                    +   "detachedIndex: 4, "
                    +   "detachedElement: +C, "
                    +   "cause: { "
                    +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +       "fromIndex : 2, "
                    +       "toIndex : 8 "
                    +     "}, "
                    +     "event: ListElementEvent{ "
                    +       "detachedIndex: 2, "
                    +       "detachedElement: +C, "
                    +       "cause: { "
                    +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +           "fromIndex : 2, "
                    +           "toIndex : 4 "
                    +         "}, "
                    +         "event: ListElementEvent{ "
                    +           "detachedIndex: 0, "
                    +           "detachedElement: +C, "
                    +           "modification: org.babyfish.collection.event.modification.ListModifications$AddByIndexAndElement{ "
                    +             "index : 1, "
                    +             "element : -C "
                    +           "} "
                    +         "} "
                    +       "} "
                    +     "} "
                    +   "} "
                    + "}"
                    + "ListElementEvent{ "
                    +   "detachedIndex: 6, "
                    +   "detachedElement: +D, "
                    +   "cause: { "
                    +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +       "fromIndex : 2, "
                    +       "toIndex : 8 "
                    +     "}, "
                    +     "event: ListElementEvent{ "
                    +       "detachedIndex: 4, "
                    +       "detachedElement: +D, "
                    +       "cause: { "
                    +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +           "fromIndex : 2, "
                    +           "toIndex : 4 "
                    +         "}, "
                    +         "event: ListElementEvent{ "
                    +           "detachedIndex: 2, "
                    +           "detachedElement: +D, "
                    +           "modification: org.babyfish.collection.event.modification.ListModifications$AddByIndexAndElement{ "
                    +             "index : 1, "
                    +             "element : -C "
                    +           "} "
                    +         "} "
                    +       "} "
                    +     "} "
                    +   "} "
                    + "}ListElementEvent{ "
                    +   "detachedIndex: 8, "
                    +   "detachedElement: +E, "
                    +   "cause: { "
                    +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +       "fromIndex : 2, "
                    +       "toIndex : 8 "
                    +     "}, "
                    +     "event: ListElementEvent{ "
                    +       "detachedIndex: 6, "
                    +       "detachedElement: +E, "
                    +       "cause: { "
                    +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +           "fromIndex : 2, "
                    +           "toIndex : 4 "
                    +         "}, "
                    +         "event: ListElementEvent{ "
                    +           "detachedIndex: 4, "
                    +           "detachedElement: +E, "
                    +           "modification: org.babyfish.collection.event.modification.ListModifications$AddByIndexAndElement{ "
                    +             "index : 1, "
                    +             "element : -C "
                    +           "} "
                    +         "} "
                    +       "} "
                    +     "} "
                    +   "} "
                    + "}"
                    + "ListElementEvent{ "
                    +   "attachedIndex: 2, "
                    +   "attachedElement: -C, "
                    +   "cause: { "
                    +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +       "fromIndex : 2, "
                    +       "toIndex : 8 "
                    +     "}, "
                    +     "event: ListElementEvent{ "
                    +       "attachedIndex: 1, "
                    +       "attachedElement: -C, "
                    +       "cause: { "
                    +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +           "fromIndex : 2, "
                    +           "toIndex : 4 "
                    +         "}, "
                    +         "event: ListElementEvent{ "
                    +           "attachedIndex: 0, "
                    +           "attachedElement: -C, "
                    +           "modification: org.babyfish.collection.event.modification.ListModifications$AddByIndexAndElement{ "
                    +             "index : 1, "
                    +             "element : -C "
                    +           "} "
                    +         "} "
                    +       "} "
                    +     "} "
                    +   "} "
                    + "}",
                    this.listenerImpl.toString()
            );
        }
        
        public void testAddAll() {
            
            this.subList2.addAll(1, MACollections.wrap("+C", "+C", "-C"));
            
            assertCollection(this.list, "A", "B", "-C", "C", "D", "E");
            
            assertCollection(subList1, "B", "-C", "C", "D");
            assertSubListBound(this.subList1, 1, 5);
            
            assertCollection(subList2, "-C", "C");
            assertSubListBound(this.subList2, 1, 3);
            
            if (this.listenerImpl == null) {
                return;
            }
            Assert.assertEquals(
                    "ListElementEvent{ "
                    +   "detachedIndex: 0, "
                    +   "detachedElement: +A, "
                    +   "cause: { "
                    +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +       "fromIndex : 2, "
                    +       "toIndex : 8 "
                    +     "}, "
                    +     "event: ListElementEvent{ "
                    +       "detachedIndex: -2, "
                    +       "detachedElement: +A, "
                    +       "cause: { "
                    +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +           "fromIndex : 2, "
                    +           "toIndex : 4 "
                    +         "}, "
                    +         "event: ListElementEvent{ "
                    +           "detachedIndex: -4, "
                    +           "detachedElement: +A, "
                    +           "modification: org.babyfish.collection.event.modification.ListModifications$AddAllByIndexAndCollection{ "
                    +             "index : 1, "
                    +             "collection : [+C, +C, -C] "
                    +           "} "
                    +         "} "
                    +       "} "
                    +     "} "
                    +   "} "
                    + "}"
                    + "ListElementEvent{ "
                    +   "detachedIndex: 2, "
                    +   "detachedElement: +B, "
                    +   "cause: { "
                    +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +       "fromIndex : 2, "
                    +       "toIndex : 8 "
                    +     "}, "
                    +     "event: ListElementEvent{ "
                    +       "detachedIndex: 0, "
                    +       "detachedElement: +B, "
                    +       "cause: { "
                    +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +           "fromIndex : 2, "
                    +           "toIndex : 4 "
                    +         "}, "
                    +         "event: ListElementEvent{ "
                    +           "detachedIndex: -2, "
                    +           "detachedElement: +B, "
                    +           "modification: org.babyfish.collection.event.modification.ListModifications$AddAllByIndexAndCollection{ "
                    +             "index : 1, "
                    +             "collection : [+C, +C, -C] "
                    +           "} "
                    +         "} "
                    +       "} "
                    +     "} "
                    +   "} "
                    + "}"
                    + "ListElementEvent{ "
                    +   "detachedIndex: 4, "
                    +   "detachedElement: +C, "
                    +   "cause: { "
                    +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +       "fromIndex : 2, "
                    +       "toIndex : 8 "
                    +     "}, "
                    +     "event: ListElementEvent{ "
                    +       "detachedIndex: 2, "
                    +       "detachedElement: +C, "
                    +       "cause: { "
                    +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +           "fromIndex : 2, "
                    +           "toIndex : 4 "
                    +         "}, "
                    +         "event: ListElementEvent{ "
                    +           "detachedIndex: 0, "
                    +           "detachedElement: +C, "
                    +           "modification: org.babyfish.collection.event.modification.ListModifications$AddAllByIndexAndCollection{ "
                    +             "index : 1, "
                    +             "collection : [+C, +C, -C] "
                    +           "} "
                    +         "} "
                    +       "} "
                    +     "} "
                    +   "} "
                    + "}"
                    + "ListElementEvent{ "
                    +   "detachedIndex: 6, "
                    +   "detachedElement: +D, "
                    +   "cause: { "
                    +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +       "fromIndex : 2, "
                    +       "toIndex : 8 "
                    +     "}, "
                    +     "event: ListElementEvent{ "
                    +       "detachedIndex: 4, "
                    +       "detachedElement: +D, "
                    +       "cause: { "
                    +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +           "fromIndex : 2, "
                    +           "toIndex : 4 "
                    +         "}, "
                    +         "event: ListElementEvent{ "
                    +           "detachedIndex: 2, "
                    +           "detachedElement: +D, "
                    +           "modification: org.babyfish.collection.event.modification.ListModifications$AddAllByIndexAndCollection{ "
                    +             "index : 1, "
                    +             "collection : [+C, +C, -C] "
                    +           "} "
                    +         "} "
                    +       "} "
                    +     "} "
                    +   "} "
                    + "}ListElementEvent{ "
                    +   "detachedIndex: 8, "
                    +   "detachedElement: +E, "
                    +   "cause: { "
                    +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +       "fromIndex : 2, "
                    +       "toIndex : 8 "
                    +     "}, "
                    +     "event: ListElementEvent{ "
                    +       "detachedIndex: 6, "
                    +       "detachedElement: +E, "
                    +       "cause: { "
                    +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +           "fromIndex : 2, "
                    +           "toIndex : 4 "
                    +         "}, "
                    +         "event: ListElementEvent{ "
                    +           "detachedIndex: 4, "
                    +           "detachedElement: +E, "
                    +           "modification: org.babyfish.collection.event.modification.ListModifications$AddAllByIndexAndCollection{ "
                    +             "index : 1, "
                    +             "collection : [+C, +C, -C] "
                    +           "} "
                    +         "} "
                    +       "} "
                    +     "} "
                    +   "} "
                    + "}"
                    + "ListElementEvent{ "
                    +   "attachedIndex: 2, "
                    +   "attachedElement: -C, "
                    +   "cause: { "
                    +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +       "fromIndex : 2, "
                    +       "toIndex : 8 "
                    +     "}, "
                    +     "event: ListElementEvent{ "
                    +       "attachedIndex: 1, "
                    +       "attachedElement: -C, "
                    +       "cause: { "
                    +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +           "fromIndex : 2, "
                    +           "toIndex : 4 "
                    +         "}, "
                    +         "event: ListElementEvent{ "
                    +           "attachedIndex: 0, "
                    +           "attachedElement: -C, "
                    +           "modification: org.babyfish.collection.event.modification.ListModifications$AddAllByIndexAndCollection{ "
                    +             "index : 1, "
                    +             "collection : [+C, +C, -C] "
                    +           "} "
                    +         "} "
                    +       "} "
                    +     "} "
                    +   "} "
                    + "}",
                    this.listenerImpl.toString()
            );
        }
        
        public void testSet() {
            
            this.subList2.set(1, "-C");
            
            assertCollection(this.list, "A", "B", "-C", "D", "E");
            
            assertCollection(subList1, "B", "-C", "D");
            assertSubListBound(this.subList1, 1, 4);
            
            assertCollection(subList2, "-C");
            assertSubListBound(this.subList2, 1, 2);
            
            if (this.listenerImpl == null) {
                return;
            }
            Assert.assertEquals(
                    "ListElementEvent{ "
                    +   "detachedIndex: 0, "
                    +   "detachedElement: +A, "
                    +   "cause: { "
                    +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +       "fromIndex : 2, "
                    +       "toIndex : 8 "
                    +     "}, "
                    +     "event: ListElementEvent{ "
                    +       "detachedIndex: -2, "
                    +       "detachedElement: +A, "
                    +       "cause: { "
                    +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +           "fromIndex : 2, "
                    +           "toIndex : 4 "
                    +         "}, "
                    +         "event: ListElementEvent{ "
                    +           "detachedIndex: -4, "
                    +           "detachedElement: +A, "
                    +           "modification: org.babyfish.collection.event.modification.ListModifications$SetByIndexAndElement{ "
                    +             "index : 1, "
                    +             "element : -C "
                    +           "} "
                    +         "} "
                    +       "} "
                    +     "} "
                    +   "} "
                    + "}"
                    + "ListElementEvent{ "
                    +   "detachedIndex: 2, "
                    +   "detachedElement: +B, "
                    +   "cause: { "
                    +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +       "fromIndex : 2, "
                    +       "toIndex : 8 "
                    +     "}, "
                    +     "event: ListElementEvent{ "
                    +       "detachedIndex: 0, "
                    +       "detachedElement: +B, "
                    +       "cause: { "
                    +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +           "fromIndex : 2, "
                    +           "toIndex : 4 "
                    +         "}, "
                    +         "event: ListElementEvent{ "
                    +           "detachedIndex: -2, "
                    +           "detachedElement: +B, "
                    +           "modification: org.babyfish.collection.event.modification.ListModifications$SetByIndexAndElement{ "
                    +             "index : 1, "
                    +             "element : -C "
                    +           "} "
                    +         "} "
                    +       "} "
                    +     "} "
                    +   "} "
                    + "}"
                    + "ListElementEvent{ "
                    +   "detachedIndex: 4, "
                    +   "detachedElement: +C, "
                    +   "cause: { "
                    +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +       "fromIndex : 2, "
                    +       "toIndex : 8 "
                    +     "}, "
                    +     "event: ListElementEvent{ "
                    +       "detachedIndex: 2, "
                    +       "detachedElement: +C, "
                    +       "cause: { "
                    +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +           "fromIndex : 2, "
                    +           "toIndex : 4 "
                    +         "}, "
                    +         "event: ListElementEvent{ "
                    +           "detachedIndex: 0, "
                    +           "detachedElement: +C, "
                    +           "modification: org.babyfish.collection.event.modification.ListModifications$SetByIndexAndElement{ "
                    +             "index : 1, "
                    +             "element : -C "
                    +           "} "
                    +         "} "
                    +       "} "
                    +     "} "
                    +   "} "
                    + "}"
                    + "ListElementEvent{ "
                    +   "detachedIndex: 6, "
                    +   "detachedElement: +D, "
                    +   "cause: { "
                    +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +       "fromIndex : 2, "
                    +       "toIndex : 8 "
                    +     "}, "
                    +     "event: ListElementEvent{ "
                    +       "detachedIndex: 4, "
                    +       "detachedElement: +D, "
                    +       "cause: { "
                    +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +           "fromIndex : 2, "
                    +           "toIndex : 4 "
                    +         "}, "
                    +         "event: ListElementEvent{ "
                    +           "detachedIndex: 2, "
                    +           "detachedElement: +D, "
                    +           "modification: org.babyfish.collection.event.modification.ListModifications$SetByIndexAndElement{ "
                    +             "index : 1, "
                    +             "element : -C "
                    +           "} "
                    +         "} "
                    +       "} "
                    +     "} "
                    +   "} "
                    + "}ListElementEvent{ "
                    +   "detachedIndex: 8, "
                    +   "detachedElement: +E, "
                    +   "cause: { "
                    +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +       "fromIndex : 2, "
                    +       "toIndex : 8 "
                    +     "}, "
                    +     "event: ListElementEvent{ "
                    +       "detachedIndex: 6, "
                    +       "detachedElement: +E, "
                    +       "cause: { "
                    +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +           "fromIndex : 2, "
                    +           "toIndex : 4 "
                    +         "}, "
                    +         "event: ListElementEvent{ "
                    +           "detachedIndex: 4, "
                    +           "detachedElement: +E, "
                    +           "modification: org.babyfish.collection.event.modification.ListModifications$SetByIndexAndElement{ "
                    +             "index : 1, "
                    +             "element : -C "
                    +           "} "
                    +         "} "
                    +       "} "
                    +     "} "
                    +   "} "
                    + "}"
                    + "ListElementEvent{ "
                    +   "detachedIndex: 5, "
                    +   "detachedElement: C, "
                    +   "attachedIndex: 2, "
                    +   "attachedElement: -C, "
                    +   "cause: { "
                    +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +       "fromIndex : 2, "
                    +       "toIndex : 8 "
                    +     "}, "
                    +     "event: ListElementEvent{ "
                    +       "detachedIndex: 3, "
                    +       "detachedElement: C, "
                    +       "attachedIndex: 1, "
                    +       "attachedElement: -C, "
                    +       "cause: { "
                    +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +           "fromIndex : 2, "
                    +           "toIndex : 4 "
                    +         "}, "
                    +         "event: ListElementEvent{ "
                    +           "detachedIndex: 1, "
                    +           "detachedElement: C, "
                    +           "attachedIndex: 0, "
                    +           "attachedElement: -C, "
                    +           "modification: org.babyfish.collection.event.modification.ListModifications$SetByIndexAndElement{ "
                    +             "index : 1, "
                    +             "element : -C "
                    +           "} "
                    +         "} "
                    +       "} "
                    +     "} "
                    +   "} "
                    + "}",
                    this.listenerImpl.toString()
            );
        }
        
        public void testIteratorAdd() {
            
            ListIterator<String> itr = this.subList2.listIterator(2);
            itr.previous();
            itr.add("-C");
            
            assertCollection(this.list, "A", "B", "-C", "C", "D", "E");
            
            assertCollection(subList1, "B", "-C", "C", "D");
            assertSubListBound(this.subList1, 1, 5);
            
            assertCollection(subList2, "-C", "C");
            assertSubListBound(this.subList2, 1, 3);
            
            if (this.listenerImpl == null) {
                return;
            }
            Assert.assertEquals(
                    "ListElementEvent{ "
                    +   "detachedIndex: 0, "
                    +   "detachedElement: +A, "
                    +   "cause: { "
                    +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +       "fromIndex : 2, "
                    +       "toIndex : 8 "
                    +     "}, "
                    +     "event: ListElementEvent{ "
                    +       "detachedIndex: -2, "
                    +       "detachedElement: +A, "
                    +       "cause: { "
                    +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +           "fromIndex : 2, "
                    +           "toIndex : 4 "
                    +         "}, "
                    +         "event: ListElementEvent{ "
                    +           "detachedIndex: -4, "
                    +           "detachedElement: +A, "
                    +           "cause: { "
                    +             "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$ListIteratorByIndex{ "
                    +               "index : 2 "
                    +             "}, "
                    +             "event: ListElementEvent{ "
                    +               "detachedIndex: -4, "
                    +               "detachedElement: +A, "
                    +               "modification: org.babyfish.collection.event.modification.ListIteratorModifications$AddByElement{ "
                    +                 "element : -C "
                    +               "} "
                    +             "} "
                    +           "} "
                    +         "} "
                    +       "} "
                    +     "} "
                    +   "} "
                    + "}"
                    + "ListElementEvent{ "
                    +   "detachedIndex: 2, "
                    +   "detachedElement: +B, "
                    +   "cause: { "
                    +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +       "fromIndex : 2, "
                    +       "toIndex : 8 "
                    +     "}, "
                    +     "event: ListElementEvent{ "
                    +       "detachedIndex: 0, "
                    +       "detachedElement: +B, "
                    +       "cause: { "
                    +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +           "fromIndex : 2, "
                    +           "toIndex : 4 "
                    +         "}, "
                    +         "event: ListElementEvent{ "
                    +           "detachedIndex: -2, "
                    +           "detachedElement: +B, "
                    +           "cause: { "
                    +             "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$ListIteratorByIndex{ "
                    +              "index : 2 "
                    +             "}, "
                    +             "event: ListElementEvent{ "
                    +               "detachedIndex: -2, "
                    +               "detachedElement: +B, "
                    +               "modification: org.babyfish.collection.event.modification.ListIteratorModifications$AddByElement{ "
                    +                 "element : -C "
                    +               "} "
                    +             "} "
                    +           "} "
                    +         "} "
                    +       "} "
                    +     "} "
                    +   "} "
                    + "}"
                    + "ListElementEvent{ "
                    +   "detachedIndex: 4, "
                    +   "detachedElement: +C, "
                    +   "cause: { "
                    +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +       "fromIndex : 2, "
                    +       "toIndex : 8 "
                    +     "}, "
                    +     "event: ListElementEvent{ "
                    +       "detachedIndex: 2, "
                    +       "detachedElement: +C, "
                    +       "cause: { "
                    +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +           "fromIndex : 2, "
                    +           "toIndex : 4 "
                    +         "}, "
                    +         "event: ListElementEvent{ "
                    +           "detachedIndex: 0, "
                    +           "detachedElement: +C, "
                    +           "cause: { "
                    +             "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$ListIteratorByIndex{ "
                    +               "index : 2 "
                    +             "}, "
                    +             "event: ListElementEvent{ "
                    +               "detachedIndex: 0, "
                    +               "detachedElement: +C, "
                    +               "modification: org.babyfish.collection.event.modification.ListIteratorModifications$AddByElement{ "
                    +                 "element : -C "
                    +               "} "
                    +             "} "
                    +           "} "
                    +         "} "
                    +       "} "
                    +     "} "
                    +   "} "
                    + "}"
                    + "ListElementEvent{ "
                    +   "detachedIndex: 6, "
                    +   "detachedElement: +D, "
                    +   "cause: { "
                    +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +       "fromIndex : 2, "
                    +       "toIndex : 8 "
                    +     "}, "
                    +     "event: ListElementEvent{ "
                    +       "detachedIndex: 4, "
                    +       "detachedElement: +D, "
                    +       "cause: { "
                    +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +           "fromIndex : 2, "
                    +           "toIndex : 4 "
                    +         "}, "
                    +         "event: ListElementEvent{ "
                    +           "detachedIndex: 2, "
                    +           "detachedElement: +D, "
                    +           "cause: { "
                    +             "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$ListIteratorByIndex{ "
                    +               "index : 2 "
                    +             "}, "
                    +             "event: ListElementEvent{ "
                    +               "detachedIndex: 2, "
                    +               "detachedElement: +D, "
                    +               "modification: org.babyfish.collection.event.modification.ListIteratorModifications$AddByElement{ "
                    +                 "element : -C "
                    +               "} "
                    +             "} "
                    +           "} "
                    +         "} "
                    +       "} "
                    +     "} "
                    +   "} "
                    + "}"
                    + "ListElementEvent{ "
                    +   "detachedIndex: 8, "
                    +   "detachedElement: +E, "
                    +   "cause: { "
                    +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +       "fromIndex : 2, "
                    +       "toIndex : 8 "
                    +     "}, "
                    +     "event: ListElementEvent{ "
                    +       "detachedIndex: 6, "
                    +       "detachedElement: +E, "
                    +       "cause: { "
                    +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +           "fromIndex : 2, "
                    +           "toIndex : 4 "
                    +         "}, "
                    +         "event: ListElementEvent{ "
                    +           "detachedIndex: 4, "
                    +           "detachedElement: +E, "
                    +           "cause: { "
                    +             "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$ListIteratorByIndex{ "
                    +               "index : 2 "
                    +             "}, "
                    +             "event: ListElementEvent{ "
                    +               "detachedIndex: 4, "
                    +               "detachedElement: +E, "
                    +               "modification: org.babyfish.collection.event.modification.ListIteratorModifications$AddByElement{ "
                    +                 "element : -C "
                    +               "} "
                    +             "} "
                    +           "} "
                    +         "} "
                    +       "} "
                    +     "} "
                    +   "} "
                    + "}"
                    + "ListElementEvent{ "
                    +   "attachedIndex: 2, "
                    +   "attachedElement: -C, "
                    +   "cause: { "
                    +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +       "fromIndex : 2, "
                    +       "toIndex : 8 "
                    +     "}, "
                    +     "event: ListElementEvent{ "
                    +       "attachedIndex: 1, "
                    +       "attachedElement: -C, "
                    +       "cause: { "
                    +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +           "fromIndex : 2, "
                    +           "toIndex : 4 "
                    +         "}, "
                    +         "event: ListElementEvent{ "
                    +           "attachedIndex: 0, "
                    +           "attachedElement: -C, "
                    +           "cause: { "
                    +             "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$ListIteratorByIndex{ "
                    +               "index : 2 "
                    +             "}, "
                    +             "event: ListElementEvent{ "
                    +               "attachedIndex: 0, "
                    +               "attachedElement: -C, "
                    +               "modification: org.babyfish.collection.event.modification.ListIteratorModifications$AddByElement{ "
                    +                 "element : -C "
                    +               "} "
                    +             "} "
                    +           "} "
                    +         "} "
                    +       "} "
                    +     "} "
                    +   "} "
                    + "}",
                    this.listenerImpl.toString()
            );
        }

        public void testIteratorSet() {
            
            ListIterator<String> itr = this.subList2.listIterator(2);
            itr.previous();
            itr.set("-C");
            
            assertCollection(this.list, "A", "B", "-C", "D", "E");
            
            assertCollection(subList1, "B", "-C", "D");
            assertSubListBound(this.subList1, 1, 4);
            
            assertCollection(subList2, "-C");
            assertSubListBound(this.subList2, 1, 2);
            
            if (this.listenerImpl == null) {
                return;
            }
            Assert.assertEquals(
                    "ListElementEvent{ "
                    +   "detachedIndex: 0, "
                    +   "detachedElement: +A, "
                    +   "cause: { "
                    +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +       "fromIndex : 2, "
                    +       "toIndex : 8 "
                    +     "}, "
                    +     "event: ListElementEvent{ "
                    +       "detachedIndex: -2, "
                    +       "detachedElement: +A, "
                    +       "cause: { "
                    +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +           "fromIndex : 2, "
                    +           "toIndex : 4 "
                    +         "}, "
                    +         "event: ListElementEvent{ "
                    +           "detachedIndex: -4, "
                    +           "detachedElement: +A, "
                    +           "cause: { "
                    +             "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$ListIteratorByIndex{ "
                    +               "index : 2 "
                    +             "}, "
                    +             "event: ListElementEvent{ "
                    +               "detachedIndex: -4, "
                    +               "detachedElement: +A, "
                    +               "modification: org.babyfish.collection.event.modification.ListIteratorModifications$SetByElement{ "
                    +                 "element : -C "
                    +               "} "
                    +             "} "
                    +           "} "
                    +         "} "
                    +       "} "
                    +     "} "
                    +   "} "
                    + "}"
                    + "ListElementEvent{ "
                    +   "detachedIndex: 2, "
                    +   "detachedElement: +B, "
                    +   "cause: { "
                    +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +       "fromIndex : 2, "
                    +       "toIndex : 8 "
                    +     "}, "
                    +     "event: ListElementEvent{ "
                    +       "detachedIndex: 0, "
                    +       "detachedElement: +B, "
                    +       "cause: { "
                    +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +           "fromIndex : 2, "
                    +           "toIndex : 4 "
                    +         "}, "
                    +         "event: ListElementEvent{ "
                    +           "detachedIndex: -2, "
                    +           "detachedElement: +B, "
                    +           "cause: { "
                    +             "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$ListIteratorByIndex{ "
                    +              "index : 2 "
                    +             "}, "
                    +             "event: ListElementEvent{ "
                    +               "detachedIndex: -2, "
                    +               "detachedElement: +B, "
                    +               "modification: org.babyfish.collection.event.modification.ListIteratorModifications$SetByElement{ "
                    +                 "element : -C "
                    +               "} "
                    +             "} "
                    +           "} "
                    +         "} "
                    +       "} "
                    +     "} "
                    +   "} "
                    + "}"
                    + "ListElementEvent{ "
                    +   "detachedIndex: 4, "
                    +   "detachedElement: +C, "
                    +   "cause: { "
                    +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +       "fromIndex : 2, "
                    +       "toIndex : 8 "
                    +     "}, "
                    +     "event: ListElementEvent{ "
                    +       "detachedIndex: 2, "
                    +       "detachedElement: +C, "
                    +       "cause: { "
                    +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +           "fromIndex : 2, "
                    +           "toIndex : 4 "
                    +         "}, "
                    +         "event: ListElementEvent{ "
                    +           "detachedIndex: 0, "
                    +           "detachedElement: +C, "
                    +           "cause: { "
                    +             "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$ListIteratorByIndex{ "
                    +               "index : 2 "
                    +             "}, "
                    +             "event: ListElementEvent{ "
                    +               "detachedIndex: 0, "
                    +               "detachedElement: +C, "
                    +               "modification: org.babyfish.collection.event.modification.ListIteratorModifications$SetByElement{ "
                    +                 "element : -C "
                    +               "} "
                    +             "} "
                    +           "} "
                    +         "} "
                    +       "} "
                    +     "} "
                    +   "} "
                    + "}"
                    + "ListElementEvent{ "
                    +   "detachedIndex: 6, "
                    +   "detachedElement: +D, "
                    +   "cause: { "
                    +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +       "fromIndex : 2, "
                    +       "toIndex : 8 "
                    +     "}, "
                    +     "event: ListElementEvent{ "
                    +       "detachedIndex: 4, "
                    +       "detachedElement: +D, "
                    +       "cause: { "
                    +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +           "fromIndex : 2, "
                    +           "toIndex : 4 "
                    +         "}, "
                    +         "event: ListElementEvent{ "
                    +           "detachedIndex: 2, "
                    +           "detachedElement: +D, "
                    +           "cause: { "
                    +             "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$ListIteratorByIndex{ "
                    +               "index : 2 "
                    +             "}, "
                    +             "event: ListElementEvent{ "
                    +               "detachedIndex: 2, "
                    +               "detachedElement: +D, "
                    +               "modification: org.babyfish.collection.event.modification.ListIteratorModifications$SetByElement{ "
                    +                 "element : -C "
                    +               "} "
                    +             "} "
                    +           "} "
                    +         "} "
                    +       "} "
                    +     "} "
                    +   "} "
                    + "}"
                    + "ListElementEvent{ "
                    +   "detachedIndex: 8, "
                    +   "detachedElement: +E, "
                    +   "cause: { "
                    +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +       "fromIndex : 2, "
                    +       "toIndex : 8 "
                    +     "}, "
                    +     "event: ListElementEvent{ "
                    +       "detachedIndex: 6, "
                    +       "detachedElement: +E, "
                    +       "cause: { "
                    +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +           "fromIndex : 2, "
                    +           "toIndex : 4 "
                    +         "}, "
                    +         "event: ListElementEvent{ "
                    +           "detachedIndex: 4, "
                    +           "detachedElement: +E, "
                    +           "cause: { "
                    +             "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$ListIteratorByIndex{ "
                    +               "index : 2 "
                    +             "}, "
                    +             "event: ListElementEvent{ "
                    +               "detachedIndex: 4, "
                    +               "detachedElement: +E, "
                    +               "modification: org.babyfish.collection.event.modification.ListIteratorModifications$SetByElement{ "
                    +                 "element : -C "
                    +               "} "
                    +             "} "
                    +           "} "
                    +         "} "
                    +       "} "
                    +     "} "
                    +   "} "
                    + "}"
                    + "ListElementEvent{ "
                    +   "detachedIndex: 5, "
                    +   "detachedElement: C, "
                    +   "attachedIndex: 2, "
                    +   "attachedElement: -C, "
                    +   "cause: { "
                    +     "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +       "fromIndex : 2, "
                    +       "toIndex : 8 "
                    +     "}, "
                    +     "event: ListElementEvent{ "
                    +       "detachedIndex: 3, "
                    +       "detachedElement: C, "
                    +       "attachedIndex: 1, "
                    +       "attachedElement: -C, "
                    +       "cause: { "
                    +         "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$SubList{ "
                    +           "fromIndex : 2, "
                    +           "toIndex : 4 "
                    +         "}, "
                    +         "event: ListElementEvent{ "
                    +           "detachedIndex: 1, "
                    +           "detachedElement: C, "
                    +           "attachedIndex: 0, "
                    +           "attachedElement: -C, "
                    +           "cause: { "
                    +             "viewInfo: org.babyfish.collection.viewinfo.ListViewInfos$ListIteratorByIndex{ "
                    +               "index : 2 "
                    +             "}, "
                    +             "event: ListElementEvent{ "
                    +               "detachedIndex: 1, "
                    +               "detachedElement: C, "
                    +               "attachedIndex: 0, "
                    +               "attachedElement: -C, "
                    +               "modification: org.babyfish.collection.event.modification.ListIteratorModifications$SetByElement{ "
                    +                 "element : -C "
                    +               "} "
                    +             "} "
                    +           "} "
                    +         "} "
                    +       "} "
                    +     "} "
                    +   "} "
                    + "}",
                    this.listenerImpl.toString()
            );
        }
        
        @SafeVarargs
        private static <E> void assertCollection(Collection<E> c, E ... elements) {
            Assert.assertEquals(elements.length, c.size());
            int index = 0;
            for (E element : c) {
                Assert.assertEquals(elements[index++], element);
            }
        }
        
        private static <E> void assertSubListBound(List<?> list, int fromIndex, int toIndex) {
            Assert.assertTrue(list instanceof XList.XListView<?>);
            XList.XListView<?> listView = (XList.XListView<?>)list;
            ListViewInfos.SubList subListViewInfo = (ListViewInfos.SubList)listView.viewInfo();
            Assert.assertEquals(fromIndex, subListViewInfo.getFromIndex());
            Assert.assertEquals(toIndex, subListViewInfo.getToIndex());
        }
    }
    
    private static class ListenerImpl<E> extends ListElementAdapter<E> {
        
        private StringBuilder builder = new StringBuilder();

        @Override
        public void modified(ListElementEvent<E> e) throws Throwable {
            this.append(e);
        }
        
        @SuppressWarnings("unchecked")
        private void append(ListElementEvent<E> e) throws Throwable {
            StringBuilder builder = this.builder;
            builder.append("ListElementEvent{ ");
            if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                builder
                .append("detachedIndex: ")
                .append(e.getIndex(PropertyVersion.DETACH))
                .append(", detachedElement: ")
                .append(e.getElement(PropertyVersion.DETACH));
            }
            if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                builder
                .append(e.getModificationType().contains(PropertyVersion.DETACH) ? ", attachedIndex: " : "attachedIndex: ")
                .append(e.getIndex(PropertyVersion.ATTACH))
                .append(", attachedElement: ")
                .append(e.getElement(PropertyVersion.ATTACH));
            }
            if (e.getModification() != null) {
                builder
                .append(", modification: ")
                .append(e.getModification());
            } else if (e.getCause() != null) {
                builder
                .append(", cause: { ")
                .append("viewInfo: ")
                .append(e.getCause().getViewInfo())
                .append(", event: ");
                this.append((ListElementEvent<E>)e.getCause().getEvent());
                builder.append(" }");
            }
            builder.append(" }");
        }
        
        @Override
        public String toString() {
            return this.builder.toString();
        }
    }
    
    private static class SimpleWrapperXList<E> extends AbstractWrapperXList<E> {

        public SimpleWrapperXList(XList<E> base) {
            super(base);
        }
    }
    
    private static class SimpleWrapperMAList<E> extends AbstractWrapperMAList<E> {

        public SimpleWrapperMAList(MAList<E> base) {
            super(base);
        }
    }
    
    private static class SimpleLazyXList<E> extends AbstractLazyXList<E> {

        public SimpleLazyXList(XList<E> base) {
            super(base);
        }

        @Override
        protected RootData<E> createRootData() {
            
            return new RootData<E>() {

                private static final long serialVersionUID = 8738367515325831611L;

                @Override
                public boolean isLoaded() {
                    return true;
                }

                @Override
                public boolean isLoading() {
                    return false;
                }

                @Override
                protected void setLoaded(boolean loaded) {
                    
                }

                @Override
                protected void setLoading(boolean loading) {
                    
                }

                @Override
                public boolean isLoadable() {
                    return false;
                }

                @Override
                protected void onLoad() {
                    
                }
            };
        }
    }
    
    private static class SimpleLazyMAList<E> extends AbstractLazyMAList<E> {

        public SimpleLazyMAList(MAList<E> base) {
            super(base);
        }

        @Override
        protected RootData<E> createRootData() {
            
            return new RootData<E>() {

                private static final long serialVersionUID = -3016396585393290798L;

                @Override
                public boolean isLoaded() {
                    return true;
                }

                @Override
                public boolean isLoading() {
                    return false;
                }

                @Override
                protected void setLoaded(boolean loaded) {
                    
                }

                @Override
                protected void setLoading(boolean loading) {
                    
                }

                @Override
                public boolean isLoadable() {
                    return false;
                }

                @Override
                protected void onLoad() {
                    
                }
            };
        }
    }
}
