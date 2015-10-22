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
package org.babyfish.collection.spi;

import java.util.Comparator;
import java.util.NoSuchElementException;

import org.babyfish.collection.MANavigableSet;
import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.event.modification.NavigableSetModifications;
import org.babyfish.collection.spi.base.BaseEntry;
import org.babyfish.collection.spi.base.NavigableBaseEntries;
import org.babyfish.collection.viewinfo.NavigableSetViewInfos;
import org.babyfish.collection.viewinfo.NavigableSetViewInfos.DescendingIterator;
import org.babyfish.collection.viewinfo.NavigableSetViewInfos.DescendingSet;
import org.babyfish.collection.viewinfo.NavigableSetViewInfos.HeadSetByToElementAndInclusive;
import org.babyfish.collection.viewinfo.NavigableSetViewInfos.SubSetByFromElementAndFromInclusiveAndToElementAndToInclusive;
import org.babyfish.collection.viewinfo.NavigableSetViewInfos.TailSetByFromElementAndInclusive;
import org.babyfish.modificationaware.event.Cause;
import org.babyfish.util.LazyResource;
import org.babyfish.view.View;

/**
 * @author Tao Chen
 */
public abstract class AbstractMANavigableSet<E> 
extends AbstractMASet<E> 
implements MANavigableSet<E> {
    
    private static final LazyResource<Resource> LAZY_RESOURCE = LazyResource.of(Resource.class);

    protected AbstractMANavigableSet(
            NavigableBaseEntries<E, Object> navigableBaseEntries) {
        super(navigableBaseEntries);
    }
    
    /**
     * This method should not be invoked by the customer immediately.
     * 
     * <p>
     * It is used to create the instance during the when 
     * {@link java.io.ObjectInputStream} reads this object from a stream.
     * Although the derived classes of this class may implement {@link java.io.Serializable},
     * but this abstract super class does not implement {@link java.io.Serializable}
     * because it have some derived class that implements {@link View} which can 
     * not be implement {@link java.io.Serializable}
     * </p>
     * 
     * <p>
     * If the derived class is still a class does not implement {@link java.io.Serializable},
     * please support a no arguments constructor and mark it with {@link Deprecated}  too, 
     * like this method.
     * </p>
     */
    @Deprecated
    protected AbstractMANavigableSet() {
        
    }
    
    @Override
    public MAIterator<E> descendingIterator() {
        return new DescendingIteratorImpl<E>(this);
    }

    @Override
    public MANavigableSetView<E> descendingSet() {
        return new DescendingSetImpl<E>(this);
    }

    @Override
    public MANavigableSetView<E> headSet(E toElement) {
        return this.headSet(toElement, false);
    }

    @Override
    public MANavigableSetView<E> headSet(E toElement, boolean inclusive) {
        return new HeadSetImpl<E>(this, toElement, inclusive);
    }

    @Override
    public MANavigableSetView<E> tailSet(E fromElement) {
        return this.tailSet(fromElement, true);
    }

    @Override
    public MANavigableSetView<E> tailSet(E fromElement, boolean inclusive) {
        return new TailSetImpl<E>(this, fromElement, inclusive);
    }

    @Override
    public MANavigableSetView<E> subSet(E fromElement, E toElement) {
        return this.subSet(fromElement, true, toElement, false);
    }

    @Override
    public MANavigableSetView<E> subSet(
            E fromElement, boolean fromInclusive,
            E toElement, boolean toInclusive) {
        return new SubSetImpl<E>(this, fromElement, fromInclusive, toElement, toInclusive);
    }

    @Override
    public E first() {
        BaseEntry<E, Object> be =
            ((NavigableBaseEntries<E, Object>)this.baseEntries).first();
        if (be != null) {
            return be.getKey();
        }
        throw new NoSuchElementException(LAZY_RESOURCE.get().noFirstElement(this.getClass()));
    }

    @Override
    public E last() {
        BaseEntry<E, Object> be =
            ((NavigableBaseEntries<E, Object>)this.baseEntries).last();
        if (be != null) {
            return be.getKey();
        }
        throw new NoSuchElementException(LAZY_RESOURCE.get().noLastElement(this.getClass()));
    }

    @Override
    public E floor(E e) {
        BaseEntry<E, Object> be =
            ((NavigableBaseEntries<E, Object>)this.baseEntries).floor(e);
        return be == null ? null : be.getKey();
    }

    @Override
    public E ceiling(E e) {
        BaseEntry<E, Object> be =
            ((NavigableBaseEntries<E, Object>)this.baseEntries).ceiling(e);
        return be == null ? null : be.getKey();
    }

    @Override
    public E lower(E e) {
        BaseEntry<E, Object> be =
            ((NavigableBaseEntries<E, Object>)this.baseEntries).lower(e);
        return be == null ? null : be.getKey();
    }

    @Override
    public E higher(E e) {
        BaseEntry<E, Object> be =
            ((NavigableBaseEntries<E, Object>)this.baseEntries).higher(e);
        return be == null ? null : be.getKey();
    }

    @Override
    public E pollFirst() {
        BaseEntry<E, Object> be = 
            ((NavigableBaseEntries<E, Object>)this.baseEntries).pollFirst(
                    this.new HandlerImpl4Set(
                            NavigableSetModifications.<E>pollFirst()));
        return be == null ? null : be.getKey();
    }

    @Override
    public E pollLast() {
        BaseEntry<E, Object> be = 
            ((NavigableBaseEntries<E, Object>)this.baseEntries).pollLast(
                    this.new HandlerImpl4Set(
                            NavigableSetModifications.<E>pollLast()));
        return be == null ? null : be.getKey();
    }

    @Override
    public Comparator<? super E> comparator() {
        return ((NavigableBaseEntries<E, Object>)this.baseEntries).comparator();
    }
    
    protected static abstract class AbstractSubSetImpl<E> extends AbstractMANavigableSet<E> implements MANavigableSetView<E> {
        
        private AbstractMANavigableSet<E> parentSet;
        
        AbstractSubSetImpl(
                AbstractMANavigableSet<E> parentSet, 
                NavigableBaseEntries<E, Object> baseEntries) {
            super(baseEntries);
            this.parentSet = parentSet;
        }
        
        @Override
        protected void bubbleModifying(ElementEvent<E> e) {
            AbstractMANavigableSet<E> parentSet = this.parentSet;
            ElementEvent<E> event = ElementEvent.bubbleEvent(
                    parentSet, 
                    new Cause(this.viewInfo(), e), 
                    null);
            parentSet.executeModifying(event);
        }

        @Override
        protected void bubbleModified(ElementEvent<E> e) {
            ElementEvent<E> bubbleEvent = e.getBubbledEvent();
            this.parentSet.executeModified(bubbleEvent);
        }
    }
    
    protected static class HeadSetImpl<E> extends AbstractSubSetImpl<E> {
        
        protected HeadSetByToElementAndInclusive viewInfo;
        
        protected HeadSetImpl(
                AbstractMANavigableSet<E> parentSet, 
                E to, 
                boolean inclusive) {
            super(
                    parentSet,
                    ((NavigableBaseEntries<E, Object>)parentSet.baseEntries)
                    .subEntries(false, null, false,  true, to, inclusive));
            this.viewInfo = NavigableSetViewInfos.headSet(to, inclusive);
        }

        @Override
        public HeadSetByToElementAndInclusive viewInfo() {
            return this.viewInfo;
        }
        
    }
    
    protected static class TailSetImpl<E> extends AbstractSubSetImpl<E> {
        
        protected TailSetByFromElementAndInclusive viewInfo;
        
        protected TailSetImpl(
                AbstractMANavigableSet<E> parentSet, 
                E from, 
                boolean inclusive) {
            super(
                    parentSet,
                    ((NavigableBaseEntries<E, Object>)parentSet.baseEntries)
                    .subEntries(true, from, inclusive, false, null, false));
            this.viewInfo = NavigableSetViewInfos.tailSet(from, inclusive);
        }

        @Override
        public TailSetByFromElementAndInclusive viewInfo() {
            return this.viewInfo;
        }
        
    }
    
    protected static class SubSetImpl<E> extends AbstractSubSetImpl<E> {
        
        protected SubSetByFromElementAndFromInclusiveAndToElementAndToInclusive viewInfo;
        
        protected SubSetImpl(
                AbstractMANavigableSet<E> parentSet, 
                E from, 
                boolean fromInclusive, 
                E to, 
                boolean toInclusive) {
            super(
                    parentSet,
                    ((NavigableBaseEntries<E, Object>)parentSet.baseEntries)
                    .subEntries(true, from, fromInclusive, true, to, toInclusive));
            this.viewInfo = NavigableSetViewInfos.subSet(from, fromInclusive, to, toInclusive);
        }

        @Override
        public SubSetByFromElementAndFromInclusiveAndToElementAndToInclusive viewInfo() {
            return this.viewInfo;
        }
        
    }
    
    protected static class DescendingSetImpl<E> extends AbstractSubSetImpl<E> {

        protected DescendingSetImpl(AbstractMANavigableSet<E> parentSet) {
            super(
                    parentSet,
                    ((NavigableBaseEntries<E, Object>)parentSet.baseEntries)
                    .descendingEntries());
        }

        @Override
        public DescendingSet viewInfo() {
            return NavigableSetViewInfos.descendingSet();
        }
        
    }
    
    protected static class DescendingIteratorImpl<E> extends AbstractMASet.AbstractIteratorImpl<E> { 
        
        protected DescendingIteratorImpl(AbstractMANavigableSet<E> parentSet) {
            super(parentSet, true);
        }

        @Override
        public DescendingIterator viewInfo() {
            return NavigableSetViewInfos.descendingIterator();
        }
        
    }
    
    private interface Resource {
        
        String noFirstElement(Class<?> thisType);
        
        String noLastElement(Class<?> thisType);
    }
}
