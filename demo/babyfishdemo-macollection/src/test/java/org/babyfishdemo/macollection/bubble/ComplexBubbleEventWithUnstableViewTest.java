package org.babyfishdemo.macollection.bubble;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.babyfish.collection.MAArrayList;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.MAList;
import org.babyfish.collection.XList;
import org.babyfish.collection.conflict.ListConflictVoter;
import org.babyfish.collection.conflict.ListConflictVoterArgs;
import org.babyfish.collection.conflict.ListReader;
import org.babyfish.collection.event.ListElementAdapter;
import org.babyfish.collection.event.ListElementEvent;
import org.babyfish.collection.viewinfo.ListViewInfos;
import org.babyfish.modificationaware.event.PropertyVersion;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/*
 * Before learn this case, please learn SimpleBubbleEvent at first.
 */
/**
 * @author Tao Chen
 */
public class ComplexBubbleEventWithUnstableViewTest {
        
    private MAList<String> list;
    
    private MAList<String> subList1;
    
    private MAList<String> subList2;
    
    private ListenerImpl<String> listenerImpl;

    @Before
    public void initData() {
        this.list = new MAArrayList<>();
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
    
    @Test
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
    
    @Test
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
    
    @Test
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
    
    @Test
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

    @Test
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
}
