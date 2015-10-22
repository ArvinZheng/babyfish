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

import java.util.Iterator;
import java.util.NavigableSet;

import junit.framework.Assert;

import org.babyfish.collection.MACollections;
import org.babyfish.collection.conflict.ListConflictVoter;
import org.babyfish.collection.conflict.ListConflictVoterArgs;
import org.babyfish.collection.conflict.ListReader;
import org.babyfish.collection.spi.base.BaseElements;
import org.babyfish.collection.spi.base.BaseElementsHandler;
import org.babyfish.collection.spi.base.BaseListIterator;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public abstract class AbstractBaseElementsVoterTest {

    protected abstract BaseElements<String> createBaseElements();
    
    private BaseElements<String> baseElements;
    
    @Before
    public void initBaseElements() {
        this.baseElements = this.createBaseElements();
        this.baseElements.combineConflictVoter(new ListConflictVoter<String>() {
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
    }
    
    @Test
    public void testAdd() {
        this.baseElements.addAll(
                0, 
                0, 
                0, 
                MACollections.wrap("+A", "A", "+B", "B", "+C", "C", "+D", "D", "+E", "E"), 
                null, 
                null
        );
        assertIterator(this.baseElements.listIterator(0, 0, 0, null), "+A", "A", "+B", "B", "+C", "C", "+D", "D", "+E", "E");
        
        BaseElementsHandlerImpl handlerImpl = new BaseElementsHandlerImpl();
        this.baseElements.add(
                4, 
                4, 
                1, 
                "-C", 
                handlerImpl, 
                null
        );
        
        assertIterator(handlerImpl.getConflictAbsIndexes().iterator(), 0, 2, 4, 6, 8);
        assertIterator(this.baseElements.listIterator(0, 0, 0, null), "A", "B", "-C", "C", "D", "E");
        Assert.assertEquals(
                "{detach(-4, +A)}"
                + "{detach(-2, +B)}"
                + "{detach(0, +C)}"
                + "{detach(2, +D)}"
                + "{detach(4, +E)}"
                + "{attach(0, -C)}",
                handlerImpl.getString()
        );
    }
    
    @Test
    public void testAddAll() {
        this.baseElements.addAll(
                0, 
                0, 
                0, 
                MACollections.wrap("+A", "A", "+B", "B", "+C", "C", "+D", "D", "+E", "E"), 
                null, 
                null
        );
        assertIterator(this.baseElements.listIterator(0, 0, 0, null), "+A", "A", "+B", "B", "+C", "C", "+D", "D", "+E", "E");
        
        BaseElementsHandlerImpl handlerImpl = new BaseElementsHandlerImpl();
        this.baseElements.addAll(
                4, 
                4, 
                1, 
                MACollections.wrap("+C", "+C", "-C"), 
                handlerImpl, 
                null
        );
        assertIterator(handlerImpl.getConflictAbsIndexes().iterator(), 0, 2, 4, 6, 8);
        assertIterator(this.baseElements.listIterator(0, 0, 0, null), "A", "B", "-C", "C", "D", "E");
        Assert.assertEquals(
                "{detach(-4, +A)}"
                + "{detach(-2, +B)}"
                + "{detach(0, +C)}"
                + "{detach(2, +D)}"
                + "{detach(4, +E)}"
                + "{attach(0, -C)}",
                handlerImpl.getString()
        );
    }
    
    @Test
    public void testSet() {
        this.baseElements.addAll(
                0, 
                0, 
                0, 
                MACollections.wrap("+A", "A", "+B", "B", "+C", "C", "+D", "D", "+E", "E"), 
                null, 
                null
        );
        assertIterator(this.baseElements.listIterator(0, 0, 0, null), "+A", "A", "+B", "B", "+C", "C", "+D", "D", "+E", "E");
        
        BaseElementsHandlerImpl handlerImpl = new BaseElementsHandlerImpl();
        this.baseElements.set(
                4, 
                4, 
                1, 
                "-C", 
                handlerImpl, 
                null
        );
        assertIterator(handlerImpl.getConflictAbsIndexes().iterator(), 0, 2, 4, 6, 8);
        assertIterator(this.baseElements.listIterator(0, 0, 0, null), "A", "B", "-C", "D", "E");
        Assert.assertEquals(
                "{detach(-4, +A)}"
                + "{detach(-2, +B)}"
                + "{detach(0, +C)}"
                + "{detach(2, +D)}"
                + "{detach(4, +E)}"
                + "{detach(1, C)attach(0, -C)}",
                handlerImpl.getString()
        );
    }
    
    private final class BaseElementsHandlerImpl implements BaseElementsHandler<String> {
        
        private StringBuilder builder = new StringBuilder();
        
        private NavigableSet<Integer> conflictAbsIndexes;
        
        @Override
        public Object createAddingArgument(int index, String element) {
            this.builder
            .append("{attach(")
            .append(index)
            .append(", ")
            .append(element)
            .append(")}");
            return null;
        }
        
        @Override
        public void adding(int index, String element, Object argument) {}
        
        @Override
        public void added(int index, String element, Object argument) {}
        
        @Override
        public Object createChangingArgument(int oldIndex, int newIndex, String oldElement, String newElement) {
            this.builder
            .append("{detach(")
            .append(oldIndex)
            .append(", ")
            .append(oldElement)
            .append(')')
            .append("attach(")
            .append(newIndex)
            .append(", ")
            .append(newElement)
            .append(")}");
            return null;
        }
        
        @Override
        public void changing(int oldIndex, int newIndex, String oldElement, String newElement, Object argument) {}
        
        @Override
        public void changed(int oldIndex, int newIndex, String oldElement, String newElement, Object argument) {}
        
        @Override
        public Object createRemovingArgument(int index, String element) {
            this.builder
            .append("{detach(")
            .append(index)
            .append(", ")
            .append(element)
            .append(")}");
            return null;
        }
        
        @Override
        public void removing(int index, String element, Object argument) {}
        
        @Override
        public void removed(int index, String element, Object argument) {}
        
        @Override
        public void setPreThrowable(Object argument, Throwable throwable) {}
        
        @Override
        public void setNullOrThrowable(Throwable nullOrThrowable) {}
        
        @Override
        public void setConflictAbsIndexes(NavigableSet<Integer> conflictAbsIndexes) {
            this.conflictAbsIndexes = conflictAbsIndexes;
        }
        
        public String getString() {
            return this.builder.toString();
        }
        
        public NavigableSet<Integer> getConflictAbsIndexes() {
            return this.conflictAbsIndexes;
        }
    }
    
    @SuppressWarnings("unchecked")
    private static <E> void assertIterator(Iterator<E> itr, E ... elements) {
        int index = 0;
        while (itr.hasNext()) {
            if (index + 1 > elements.length) {
                break;
            }
            E element = itr.next();
            Assert.assertEquals(elements[index++], element);
        }
        Assert.assertEquals(elements.length, index);
    }
    
    @SuppressWarnings("unchecked")
    private static <E> void assertIterator(BaseListIterator<E> itr, E ... elements) {
        int index = 0;
        while (itr.hasNext()) {
            if (index + 1 > elements.length) {
                break;
            }
            E element = itr.next();
            Assert.assertEquals(elements[index++], element);
        }
        Assert.assertEquals(elements.length, index);
    }
}
