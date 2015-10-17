/*
 * BabyFish, Object Model Framework for Java and JPA.
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
package org.babyfish.collection.spi.laziness;

import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.OrderAdjustMode;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XCollection;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.collection.XSet;
import org.babyfish.collection.viewinfo.OrderedSetViewInfos;
import org.babyfish.view.View;
import org.babyfish.view.ViewInfo;

/**
 * @author Tao Chen(&#38472;&#28059;)
 */
public abstract class AbstractLazyXOrderedSet<E> extends AbstractLazyXSet<E> implements XOrderedSet<E> {

    public AbstractLazyXOrderedSet(
            AbstractLazyXOrderedSet<E> parent,
            ViewInfo viewInfo) {
        super(parent, viewInfo);
    }

    public AbstractLazyXOrderedSet(XOrderedSet<E> base) {
        super(base);
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
    protected AbstractLazyXOrderedSet() {
        
    }

    @Override
    protected abstract RootData<E> createRootData();
    
    @Deprecated
    @Override
    protected final XSet<E> createBaseView(
            XSet<E> parentBase, 
            ViewInfo viewInfo) {
        return this.createBaseView(
                (XOrderedSet<E>)parentBase, 
                viewInfo);
    }
    
    protected XOrderedSet<E> createBaseView(
            XOrderedSet<E> parentBase, 
            ViewInfo viewInfo) {
        throw new IllegalArgumentException(LAZY_COMMON_RESOURCE.get().illegalViewInfo());
    }

    @Override
    public XOrderedSetView<E> descendingSet() {
        return new DescendingSetImpl<E>(this);
    }

    @Override
    public XIterator<E> descendingIterator() {
        return new DescendingIteratorImpl<E>(this);
    }

    @Override
    public boolean headAppend() {
        return this.<XOrderedSet<E>>getBase().headAppend();
    }

    @Override
    public OrderAdjustMode replaceMode() {
        return this.<XOrderedSet<E>>getBase().replaceMode();
    }

    @Override
    public E first() {
        this.requiredEnabled();
        return this.<XOrderedSet<E>>getBase().first();
    }

    @Override
    public E last() {
        this.requiredEnabled();
        return this.<XOrderedSet<E>>getBase().last();
    }

    @Override
    public E pollFirst() {
        this.enable();
        return this.<XOrderedSet<E>>getBase().pollFirst();
    }

    @Override
    public E pollLast() {
        this.enable();
        return this.<XOrderedSet<E>>getBase().pollLast();
    }
    
    protected static class DescendingSetImpl<E> extends AbstractLazyXOrderedSet<E> implements XOrderedSetView<E> {

        public DescendingSetImpl(
                AbstractLazyXOrderedSet<E> parent) {
            super(parent, OrderedSetViewInfos.descendingSet());
        }

        @Override
        public ViewInfo viewInfo() {
            return this.<XOrderedSetView<E>>getBase().viewInfo();
        }

        @Deprecated
        @Override
        protected final RootData<E> createRootData() {
            throw new UnsupportedOperationException();
        }

        @Override
        protected XOrderedSet<E> createBaseView(
                XOrderedSet<E> parentBase,
                ViewInfo viewInfo) {
            if (viewInfo instanceof OrderedSetViewInfos.DescendingSet) {
                return parentBase.descendingSet();
            }
            throw new IllegalArgumentException(LAZY_COMMON_RESOURCE.get().illegalViewInfo());
        }
        
    }
    
    protected static abstract class AbstractIteratorImpl<E> extends AbstractLazyXSet.AbstractIteratorImpl<E> {

        public AbstractIteratorImpl(
                AbstractLazyXOrderedSet<E> parent,
                ViewInfo viewInfo) {
            super(parent, viewInfo);
        }

        @Deprecated
        @Override
        protected final XIterator<E> createBaseView(
                XCollection<E> baseParent, 
                ViewInfo viewInfo) {
            return this.createBaseView((XOrderedSet<E>)baseParent, viewInfo);
        }
        
        protected abstract XIterator<E> createBaseView(
                XOrderedSet<E> baseParent, 
                ViewInfo viewInfo);
    }
    
    protected static class DescendingIteratorImpl<E> extends AbstractIteratorImpl<E> {

        public DescendingIteratorImpl(
                AbstractLazyXOrderedSet<E> parent) {
            super(parent, OrderedSetViewInfos.descendingSet());
        }

        @Override
        protected XIterator<E> createBaseView(
                XOrderedSet<E> baseParent, 
                ViewInfo viewInfo) {
            if (viewInfo instanceof OrderedSetViewInfos.DescendingIterator) {
                return baseParent.descendingIterator();
            }
            throw new IllegalArgumentException(LAZY_COMMON_RESOURCE.get().illegalViewInfo());
        }
    }

    protected static abstract class RootData<E> extends AbstractLazyXSet.RootData<E> {

        private static final long serialVersionUID = -2950992665657331984L;

        protected RootData() {
            
        }
        
        @Deprecated
        @Override
        protected final void setBase(XSet<E> base) {
            this.setBase((XOrderedSet<E>)base);
        }
        
        protected void setBase(XOrderedSet<E> base) {
            super.setBase(base);
        }
        
        @Override
        protected XOrderedSet<E> createDefaultBase(
                UnifiedComparator<? super E> unifiedComparator) {
            return new LinkedHashSet<>(unifiedComparator.equalityComparator(true));
        }
    }
}
