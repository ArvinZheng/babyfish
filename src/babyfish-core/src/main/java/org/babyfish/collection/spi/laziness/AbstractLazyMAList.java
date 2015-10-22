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
package org.babyfish.collection.spi.laziness;

import java.util.NavigableSet;

import org.babyfish.collection.MAArrayList;
import org.babyfish.collection.MAList;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XList;
import org.babyfish.collection.event.ElementListener;
import org.babyfish.collection.event.ListElementEvent;
import org.babyfish.collection.event.ListElementListener;
import org.babyfish.collection.spi.wrapper.event.AbstractListElementEventDispatcher;
import org.babyfish.collection.viewinfo.CollectionViewInfos;
import org.babyfish.collection.viewinfo.ListViewInfos;
import org.babyfish.lang.Combiner;
import org.babyfish.lang.Combiners;
import org.babyfish.modificationaware.event.AttributeScope;
import org.babyfish.modificationaware.event.BubbledProperty;
import org.babyfish.modificationaware.event.BubbledPropertyConverter;
import org.babyfish.modificationaware.event.Cause;
import org.babyfish.modificationaware.event.ModificationEventHandleException;
import org.babyfish.modificationaware.event.PropertyVersion;
import org.babyfish.view.View;
import org.babyfish.view.ViewInfo;

/**
 * @author Tao Chen
 */
public abstract class AbstractLazyMAList<E> extends AbstractLazyXList<E> implements MAList<E> {
    
    private static final Object AK_ELEMENT_LISTENER = new Object();
    
    private static final Object AK_LIST_ELEMENT_LISTENER = new Object();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static final Combiner<ElementListener<?>> ELEMENT_LISTENER_COMBINER = 
        (Combiner)Combiners.of(ElementListener.class);
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static final Combiner<ListElementListener<?>> LIST_ELEMENT_LISTENER_COMBINER = 
        (Combiner)Combiners.of(ListElementListener.class);
    
    private transient ElementListener<E> elementListener;
    
    private transient ListElementListener<E> listElementListener;
    
    protected AbstractLazyMAList(MAList<E> base) {
        super(base);
    }

    protected AbstractLazyMAList(
            AbstractLazyMAList<E> parent,
            ViewInfo viewInfo) {
        super(parent, viewInfo);
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
    protected AbstractLazyMAList() {
        
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void addElementListener(ElementListener<? super E> listener) {
        this.elementListener = 
                (ElementListener)ELEMENT_LISTENER_COMBINER.combine(
                        this.elementListener, listener);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void removeElementListener(ElementListener<? super E> listener) {
        this.elementListener = 
                (ElementListener)ELEMENT_LISTENER_COMBINER.remove(
                        this.elementListener, listener);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void addListElementListener(ListElementListener<? super E> listener) {
        this.listElementListener = 
                (ListElementListener)LIST_ELEMENT_LISTENER_COMBINER.combine(
                        this.listElementListener, listener);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void removeListElementListener(ListElementListener<? super E> listener) {
        this.listElementListener = 
                (ListElementListener)LIST_ELEMENT_LISTENER_COMBINER.remove(
                        this.listElementListener, listener);
    }
    
    protected void executeModifying(ListElementEvent<E> e) {
        Throwable finalThrowable = null;
        try {
            this.onModifying(e);    
        } catch (Throwable ex) {
            finalThrowable = ex;
        }
        try {
            this.raiseModifying(e);
        } catch (Throwable ex) {
            if (finalThrowable == null) {
                finalThrowable = ex;
            }
        }
        try {
            this.bubbleModifying(e);
        } catch (Throwable ex) {
            if (finalThrowable == null) {
                finalThrowable = ex;
            }
        }
        if (finalThrowable != null) {
            throw new ModificationEventHandleException(false, e, finalThrowable);
        }
    }
    
    protected void executeModified(ListElementEvent<E> e) {
        Throwable finalThrowable = null;
        try {
            this.bubbleModified(e);     
        } catch (Throwable ex) {
            finalThrowable = ex;
        }
        try {
            this.raiseModified(e);
        } catch (Throwable ex) {
            if (finalThrowable == null) {
                finalThrowable = ex;
            }
        }
        try {
            this.onModified(e);
        } catch (Throwable ex) {
            if (finalThrowable == null) {
                finalThrowable = ex;
            }
        }
        if (finalThrowable != null) {
            throw new ModificationEventHandleException(true, e, finalThrowable);
        }
    }

    protected void onModifying(ListElementEvent<E> e) throws Throwable {
        
    }
    
    protected void onModified(ListElementEvent<E> e) throws Throwable {
        
    }

    protected void raiseModifying(ListElementEvent<E> e) throws Throwable {
        Throwable finalThrowable = null;
        try {
            ElementListener<E> elementListener = this.elementListener;
            if (elementListener != null) {
                e
                .getAttributeContext(AttributeScope.LOCAL)
                .addAttribute(AK_ELEMENT_LISTENER, elementListener);
                elementListener.modifying(e);
            }
        } catch (Throwable ex) {
            finalThrowable = ex;
        }
        try {
            ListElementListener<E> listElementListener = this.listElementListener;
            if (listElementListener != null) {
                e
                .getAttributeContext(AttributeScope.LOCAL)
                .addAttribute(AK_LIST_ELEMENT_LISTENER, listElementListener);
                listElementListener.modifying(e);
            }
        } catch (Throwable ex) {
            if (finalThrowable == null) {
                finalThrowable = ex;
            }
        }
        if (finalThrowable != null) {
            throw finalThrowable;
        }
    }
    
    @SuppressWarnings("unchecked")
    protected void raiseModified(ListElementEvent<E> e) throws Throwable {
        Throwable finalThrowable = null;
        try {
            ListElementListener<E> listElementListener = 
                (ListElementListener<E>)
                e
                .getAttributeContext(AttributeScope.LOCAL)
                .removeAttribute(AK_LIST_ELEMENT_LISTENER);
            if (listElementListener != null) {
                listElementListener.modified(e);
            }
        } catch (Throwable ex) {
            finalThrowable = ex;
        }
        try {
            ElementListener<E> elementListener = 
                (ElementListener<E>)
                e
                .getAttributeContext(AttributeScope.LOCAL)
                .removeAttribute(AK_ELEMENT_LISTENER);
            if (elementListener != null) {
                elementListener.modified(e);
            }
        } catch (Throwable ex) {
            if (finalThrowable == null) {
                finalThrowable = ex;
            }
        }
        if (finalThrowable != null) {
            throw finalThrowable;
        }
    }

    protected void bubbleModifying(ListElementEvent<E> e) {
        
    }
    
    protected void bubbleModified(ListElementEvent<E> e) {
        
    }

    @Override
    protected abstract RootData<E> createRootData();

    @Deprecated
    @Override
    protected final XList<E> createBaseView(
            XList<E> parentBase,
            ViewInfo viewInfo) {
        return this.createBaseView((MAList<E>)parentBase, viewInfo);
    }
    
    protected MAList<E> createBaseView(
            MAList<E> parentBase,
            ViewInfo viewInfo) {
        throw new IllegalArgumentException(LAZY_COMMON_RESOURCE.get().illegalViewInfo());
    }

    @Override
    protected AbstractListElementEventDispatcher<E> createEventDispatcher() {
        return new AbstractListElementEventDispatcher<E>(this) {

            @Override
            protected boolean isDispatchable() {
                return this
                        .<AbstractLazyMAList<E>>getOwner()
                        .getRootData()
                        .isDispatchable();
            }

            @Override
            protected void executePreDispatchedEvent(ListElementEvent<E> dispatchedEvent) {
                this.<AbstractLazyMAList<E>>getOwner().executeModifying(dispatchedEvent);
            }

            @Override
            protected void executePostDispatchedEvent(ListElementEvent<E> dispatchedEvent) {
                this.<AbstractLazyMAList<E>>getOwner().executeModified(dispatchedEvent);
            }
            
        };
    }

    @Override
    public MAListView<E> subList(int fromIndex, int toIndex) {
        return new SubListImpl<E>(this, fromIndex, toIndex);
    }

    @Override
    public MAListIterator<E> iterator() {
        return this.listIterator(0);
    }

    @Override
    public MAListIterator<E> listIterator() {
        return this.listIterator(0);
    }

    @Override
    public MAListIterator<E> listIterator(int index) {
        return new IteratorImpl<E>(this, index);
    }
    
    protected static abstract class RootData<E> extends AbstractLazyXList.RootData<E> {

        private static final long serialVersionUID = -398916867444148237L;
        
        public RootData() {
            
        }

        @Deprecated
        @Override
        protected final void setBase(XList<E> base) {
            this.setBase((MAList<E>)base);
        }
        
        protected void setBase(MAList<E> base) {
            super.setBase(base);
        }

        @Override
        protected MAList<E> createDefaultBase(
                UnifiedComparator<? super E> unifiedComparator) {
            return new MAArrayList<E>(unifiedComparator);
        }

    }
    
    protected static class SubListImpl<E> extends AbstractLazyMAList<E> implements MAListView<E> {

        public SubListImpl(
                AbstractLazyMAList<E> parent,
                int fromIndex,
                int toIndex) {
            super(parent, ListViewInfos.subList(fromIndex, toIndex));
        }

        @Deprecated
        @Override
        protected final RootData<E> createRootData() {
            throw new UnsupportedOperationException();
        }

        protected MAList<E> createBaseView(
                MAList<E> parentBase,
                ViewInfo viewInfo) {
            if (viewInfo instanceof ListViewInfos.SubList) {
                ListViewInfos.SubList subListViewInfo = 
                        (ListViewInfos.SubList)viewInfo;
                return parentBase.subList(
                        subListViewInfo.getFromIndex(), 
                        subListViewInfo.getToIndex());
            }
            return super.createBaseView(parentBase, viewInfo);
        }
        
        protected void bubbleModifying(final ListElementEvent<E> e) {
            
            ListElementEvent<E> bubbledEvent = ListElementEvent.bubbleEvent(
                    this.getParent(), 
                    new Cause(this.viewInfo(), e), 
                    null, 
                    new BubbledPropertyConverter<Integer>() {
                        @Override
                        public void convert(BubbledProperty<Integer> bubbledProperty) {
                            int offset = ((ListViewInfos.SubList)SubListImpl.this.viewInfo()).getFromIndex();
                            if (bubbledProperty.contains(PropertyVersion.DETACH)) {
                                bubbledProperty.setValueToDetach(e.getIndex(PropertyVersion.DETACH) + offset);
                            }
                            if (bubbledProperty.contains(PropertyVersion.ATTACH)) {
                                NavigableSet<Integer> conflictAbsIndexes = e.getConflictAbsoluteIndexes();
                                if (!conflictAbsIndexes.isEmpty()) {
                                    SubListImpl<E> that = SubListImpl.this;
                                    int headHide = that.getHeadHide();
                                    if (headHide != 0) {
                                        int fromIndex = ((ListViewInfos.SubList)that.viewInfo()).getFromIndex();
                                        offset -= conflictAbsIndexes.subSet(headHide - fromIndex, headHide).size();
                                    }
                                }
                                bubbledProperty.setValueToAttach(e.getIndex(PropertyVersion.ATTACH) + offset);
                            }
                        }
                    });
            this.<AbstractLazyMAList<E>>getParent().executeModifying(bubbledEvent);
        }
        
        protected void bubbleModified(ListElementEvent<E> e) {
            ListElementEvent<E> bubbleEvent = e.getBubbledEvent();
            this.<AbstractLazyMAList<E>>getParent().executeModified(bubbleEvent);
        }

        @Override
        public ViewInfo viewInfo() {
            return this.<XListView<E>>getBase().viewInfo();
        }
    }
    
    protected static abstract class AbstractIteratorImpl<E> 
    extends AbstractLazyXList.AbstractIteratorImpl<E> 
    implements MAListIterator<E> {
        
        private transient ElementListener<E> elementListener;
        
        private transient ListElementListener<E> listElementListener;

        public AbstractIteratorImpl(AbstractLazyMAList<E> parent, ViewInfo viewInfo) {
            super(parent, viewInfo);
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public void addElementListener(ElementListener<? super E> listener) {
            this.elementListener = 
                    (ElementListener)ELEMENT_LISTENER_COMBINER.combine(
                            this.elementListener, listener);
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public void removeElementListener(ElementListener<? super E> listener) {
            this.elementListener = 
                    (ElementListener)ELEMENT_LISTENER_COMBINER.remove(
                            this.elementListener, listener);
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public void addListElementListener(ListElementListener<? super E> listener) {
            this.listElementListener = 
                    (ListElementListener)LIST_ELEMENT_LISTENER_COMBINER.combine(
                            this.listElementListener, listener);
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public void removeListElementListener(ListElementListener<? super E> listener) {
            this.listElementListener = 
                    (ListElementListener)LIST_ELEMENT_LISTENER_COMBINER.remove(
                            this.listElementListener, listener);
        }
        
        protected void executeModifying(ListElementEvent<E> e) {
            Throwable finalThrowable = null;
            try {
                this.onModifying(e);    
            } catch (Throwable ex) {
                finalThrowable = ex;
            }
            try {
                this.raiseModifying(e);
            } catch (Throwable ex) {
                if (finalThrowable == null) {
                    finalThrowable = ex;
                }
            }
            try {
                this.bubbleModifying(e);
            } catch (Throwable ex) {
                if (finalThrowable == null) {
                    finalThrowable = ex;
                }
            }
            if (finalThrowable != null) {
                throw new ModificationEventHandleException(false, e, finalThrowable);
            }
        }
        
        protected void executeModified(ListElementEvent<E> e) {
            Throwable finalThrowable = null;
            try {
                this.bubbleModified(e);     
            } catch (Throwable ex) {
                finalThrowable = ex;
            }
            try {
                this.raiseModified(e);
            } catch (Throwable ex) {
                if (finalThrowable == null) {
                    finalThrowable = ex;
                }
            }
            try {
                this.onModified(e);
            } catch (Throwable ex) {
                if (finalThrowable == null) {
                    finalThrowable = ex;
                }
            }
            if (finalThrowable != null) {
                throw new ModificationEventHandleException(true, e, finalThrowable);
            }
        }

        protected void onModifying(ListElementEvent<E> e) throws Throwable {
            
        }
        
        protected void onModified(ListElementEvent<E> e) throws Throwable {
            
        }

        protected void raiseModifying(ListElementEvent<E> e) throws Throwable {
            Throwable finalThrowable = null;
            try {
                ElementListener<E> elementListener = this.elementListener;
                if (elementListener != null) {
                    e
                    .getAttributeContext(AttributeScope.LOCAL)
                    .addAttribute(AK_ELEMENT_LISTENER, elementListener);
                    elementListener.modifying(e);
                }
            } catch (Throwable ex) {
                finalThrowable = ex;
            }
            try {
                ListElementListener<E> listElementListener = this.listElementListener;
                if (listElementListener != null) {
                    e
                    .getAttributeContext(AttributeScope.LOCAL)
                    .addAttribute(AK_LIST_ELEMENT_LISTENER, listElementListener);
                    listElementListener.modifying(e);
                }
            } catch (Throwable ex) {
                if (finalThrowable == null) {
                    finalThrowable = ex;
                }
            }
            if (finalThrowable != null) {
                throw finalThrowable;
            }
        }
        
        @SuppressWarnings("unchecked")
        protected void raiseModified(ListElementEvent<E> e) throws Throwable {
            Throwable finalThrowable = null;
            try {
                ListElementListener<E> listElementListener = 
                    (ListElementListener<E>)
                    e
                    .getAttributeContext(AttributeScope.LOCAL)
                    .removeAttribute(AK_LIST_ELEMENT_LISTENER);
                if (listElementListener != null) {
                    listElementListener.modified(e);
                }
            } catch (Throwable ex) {
                finalThrowable = ex;
            }
            try {
                ElementListener<E> elementListener = 
                    (ElementListener<E>)
                    e
                    .getAttributeContext(AttributeScope.LOCAL)
                    .removeAttribute(AK_ELEMENT_LISTENER);
                if (elementListener != null) {
                    elementListener.modified(e);
                }
            } catch (Throwable ex) {
                if (finalThrowable == null) {
                    finalThrowable = ex;
                }
            }
            if (finalThrowable != null) {
                throw finalThrowable;
            }
        }

        protected void bubbleModifying(ListElementEvent<E> e) {
            ListElementEvent<E> bubbledEvent = ListElementEvent.bubbleEvent(
                    this.getParent(), new Cause(this.viewInfo(), e), null, null);
            this.<AbstractLazyMAList<E>>getParent().executeModifying(bubbledEvent);
        }
        
        protected void bubbleModified(ListElementEvent<E> e) {
            ListElementEvent<E> bubbleEvent = e.getBubbledEvent();
            this.<AbstractLazyMAList<E>>getParent().executeModified(bubbleEvent);
        }
        
        @Override
        protected AbstractListElementEventDispatcher<E> createEventDispatcher() {
            return new AbstractListElementEventDispatcher<E>(this) {

                @Override
                protected boolean isDispatchable() {
                    return this
                            .<AbstractIteratorImpl<E>>getOwner()
                            .<AbstractLazyMAList<E>>getParent()
                            .getRootData()
                            .isDispatchable();
                }

                @Override
                protected void executePreDispatchedEvent(ListElementEvent<E> dispatchedEvent) {
                    this.<AbstractIteratorImpl<E>>getOwner().executeModifying(dispatchedEvent);
                }

                @Override
                protected void executePostDispatchedEvent(ListElementEvent<E> dispatchedEvent) {
                    this.<AbstractIteratorImpl<E>>getOwner().executeModified(dispatchedEvent);
                }
                
            };
        }

        @Override
        protected XListIterator<E> createBaseView(
                XList<E> baseParent, ViewInfo viewInfo) {
            return this.createBaseView((MAList<E>)baseParent, viewInfo);
        }
        
        protected abstract MAListIterator<E> createBaseView(
                MAList<E> baseParent, ViewInfo viewInfo);
        
    }
    
    protected static class IteratorImpl<E> extends AbstractIteratorImpl<E> {

        public IteratorImpl(
                AbstractLazyMAList<E> parent, 
                int index) {
            super(parent, ListViewInfos.listIterator(index));
        }

        @Override
        protected MAListIterator<E> createBaseView(
                MAList<E> baseParent, ViewInfo viewInfo) {
            if (viewInfo instanceof ListViewInfos.ListIteratorByIndex) {
                ListViewInfos.ListIteratorByIndex listIteratorByIndexViewInfo =
                        (ListViewInfos.ListIteratorByIndex)viewInfo;
                return baseParent.listIterator(listIteratorByIndexViewInfo.getIndex());
            }
            if (viewInfo instanceof CollectionViewInfos.Iterator) {
                return baseParent.listIterator();
            }
            throw new IllegalArgumentException(LAZY_COMMON_RESOURCE.get().illegalViewInfo());
        }
        
    }

}
