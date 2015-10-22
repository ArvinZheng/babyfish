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
package org.babyfish.collection.spi.base;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.NoSuchElementException;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XList;
import org.babyfish.collection.conflict.ListConflictVoter;
import org.babyfish.collection.conflict.spi.ListConflictVoterManager;
import org.babyfish.collection.conflict.spi.ListSource;
import org.babyfish.collection.spi.AbstractXList;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.Nulls;
import org.babyfish.lang.Ref;
import org.babyfish.lang.UncheckedException;
import org.babyfish.modificationaware.event.ModificationType;
import org.babyfish.util.LazyResource;
import org.babyfish.validator.Validator;
import org.babyfish.validator.Validators;

/**
 * @author Tao Chen
 */
public abstract class AbstractBaseElementsImpl<E> implements BaseElements<E>, Serializable {

    private static final long serialVersionUID = 503477015077759509L;
    
    private static final LazyResource<Resource> LAZY_RESOURCE = LazyResource.of(Resource.class);
    
    private static final LazyResource<IteratorResource> LAZY_ITERATOR_RESOURCE = LazyResource.of(IteratorResource.class);

    private static final Object TRIGGER_NIL_ELEMENT = new Object();
    
    private static final byte ATTACH_PROCESSOR_NEW = 0;
    
    private static final byte ATTACH_PROCESSOR_INITIALIZED = 1;
    
    private static final byte ATTACH_PROCESSOR_RESOLVED = 2;
    
    private static final byte ATTACH_PROCESSOR_EXECUTING = 3;
    
    private static final byte ATTACH_PROCESSOR_EXECUTED = 4;
    
    private static final byte ATTACH_PROCESSOR_FLUSHED = 5;
    
    private static final int TRIGGER_NEW = 0;
    
    private static final int TRIGGER_EXECUTING = 1;
    
    private static final int TRIGGER_EXECUTED = 2;
    
    private static final int TRIGGER_FLUSHED = 3;
    
    protected Object comparatorOrEqualityComparator;
    
    protected Validator<E> validator;
    
    protected ListConflictVoterManager<E> voterManager;
    
    private transient int frozenCount;
    
    protected AbstractBaseElementsImpl(Object comparatorOrEqualityComparatorOrUnifiedComparator) {
        this.comparatorOrEqualityComparator = 
            UnifiedComparator.unwrap(
                    comparatorOrEqualityComparatorOrUnifiedComparator);
    }

    @SuppressWarnings("unchecked")
    @Override
    public UnifiedComparator<? super E> unifiedComparator() {
        Object comparatorOrEqualityComparator = this.comparatorOrEqualityComparator;
        if (comparatorOrEqualityComparator instanceof Comparator<?>) {
            return UnifiedComparator.nullToEmpty(
                    UnifiedComparator.of((Comparator<E>)comparatorOrEqualityComparator));
        }
        return UnifiedComparator.nullToEmpty(
                UnifiedComparator.of((EqualityComparator<E>)comparatorOrEqualityComparator));
    }
    
    @Override
    public void combineValidator(Validator<E> validator) {
        this.validator = Validators.combine(this.validator, validator);
    }

    @Override
    public void removeValidator(Validator<E> validator) {
        this.validator = Validators.remove(this.validator, validator);
    }

    @Override
    public void validate(E element) {
        Validator<E> validator = this.validator;
        if (validator != null) {
            validator.validate(element);
        }
    }

    @Override
    public void combineConflictVoter(ListConflictVoter<E> voter) {
        this.voterManager = ListConflictVoterManager.combine(
                this.voterManager, 
                voter, 
                this.new ListSourceImpl());
    }

    @Override
    public void removeConflictVoter(ListConflictVoter<E> voter) {
        this.voterManager = ListConflictVoterManager.remove(
                this.voterManager, 
                voter, 
                this.new ListSourceImpl());
    }

    @Override
    public final void add(
            int subListHeadHide, 
            int subListTailHide, 
            int index,
            E element, 
            BaseElementsHandler<E> handler,
            BaseElementsConflictHandler rangeChangeHandler) {
        this.add(
                subListHeadHide, 
                subListTailHide, 
                index, 
                element, 
                this.triggerOf(subListHeadHide, handler), 
                rangeChangeHandler);
    }

    @Override
    public final boolean addAll(
            int subListHeadHide, 
            int subListTailHide, 
            int index,
            Collection<? extends E> c, 
            BaseElementsHandler<E> handler,
            BaseElementsConflictHandler rangeChangeHandler) {
        return this.addAll(
                subListHeadHide, 
                subListTailHide, 
                index, 
                c, 
                this.triggerOf(subListHeadHide, handler, c.size()),
                rangeChangeHandler);
    }

    @Override
    public final E set(
            int subListHeadHide, 
            int subListTailHide, 
            int index, 
            E element, 
            BaseElementsHandler<E> handler, 
            BaseElementsConflictHandler rangeChangeHandler) {
        return this.set(
                subListHeadHide, 
                subListTailHide, 
                index, 
                element, 
                this.triggerOf(subListHeadHide, handler), 
                rangeChangeHandler);
    }

    @Override
    public final void clear(int subListHeadHide, int subListTailHide, BaseElementsHandler<E> handler) {
        this.clear(
                subListHeadHide, 
                subListTailHide, 
                this.triggerOf(subListHeadHide, handler, this.allSize() - subListHeadHide - subListTailHide));
    }

    @Override
    public final boolean remove(int subListHeadHide, int subListTailHide, Object o, BaseElementsHandler<E> handler) {
        return this.remove(subListHeadHide, subListTailHide, o, this.triggerOf(subListHeadHide, handler));
    }

    @Override
    public final E removeAt(int subListHeadHide, int subListTailHide, int index, BaseElementsHandler<E> handler) {
        return this.removeAt(subListHeadHide, subListTailHide, index, this.triggerOf(subListHeadHide, handler));
    }

    @Override
    public final boolean removeAll(int subListHeadHide, int subListTailHide, Collection<?> c, BaseElementsHandler<E> handler) {
        return this.removeAll(
                subListHeadHide, 
                subListTailHide, 
                c, 
                this.triggerOf(subListHeadHide, handler, this.allSize() - subListHeadHide - subListTailHide));
    }

    @Override
    public final boolean retainAll(int subListHeadHide, int subListTailHide, Collection<?> c, BaseElementsHandler<E> handler) {
        return this.retainAll(
                subListHeadHide, 
                subListTailHide, 
                c, 
                this.triggerOf(subListHeadHide, handler, this.allSize() - subListHeadHide - subListTailHide));
    }

    @Override
    public final boolean isFrozen() {
        return this.frozenCount != 0;
    }

    protected void add(
            int subListHeadHide, 
            int subListTailHide, 
            int index,
            E element, 
            Trigger<E> trigger,
            BaseElementsConflictHandler conflictHandler) {
        this.checkSubListRange(subListHeadHide, subListTailHide);
        Arguments.indexMustBetweenValue(
                "index", 
                index, 
                0, 
                true, 
                this.allSize() - subListHeadHide - subListTailHide, 
                true //For adding and iterator, it is true, for other, it is false
        );
        AttachProcessor<E> attachProcessor = this.attachProcessorOf(
                false,
                subListHeadHide, 
                index, 
                conflictHandler, 
                trigger);
        attachProcessor.initialize(element);
        this.addImpl(element, attachProcessor);
        attachProcessor.flush();
    }

    protected boolean addAll(
            int subListHeadHide, 
            int subListTailHide, 
            int index,
            Collection<? extends E> c, 
            Trigger<E> trigger,
            BaseElementsConflictHandler conflictHandler) {
        
        int oldAllSize = this.allSize();
        this.checkSubListRange(subListHeadHide, subListTailHide);
        Arguments.indexMustBetweenValue(
                "index", 
                index, 
                0, 
                true, 
                oldAllSize - subListHeadHide - subListTailHide, 
                true //For adding and iterator, this is true, otherwise it is false
        );
        AttachProcessor<E> attachProcessor = this.attachProcessorOf(
                false,
                subListHeadHide, 
                index, 
                conflictHandler, 
                trigger);
        c = attachProcessor.intialize(c);
        this.addAllImpl(c, attachProcessor);
        attachProcessor.flush();
        return !c.isEmpty();
    }

    protected E set(
            int subListHeadHide, 
            int subListTailHide, 
            int index,
            E element, 
            Trigger<E> trigger,
            BaseElementsConflictHandler conflictHandler) {
        
        int oldAbsSize = this.allSize();
        this.checkSubListRange(subListHeadHide, subListTailHide);
        Arguments.indexMustBetweenValue(
                "index", 
                index, 
                0, 
                true, 
                oldAbsSize - subListHeadHide - subListTailHide, 
                false);
        AttachProcessor<E> attachProcessor = 
            this.attachProcessorOf(
                    true,
                    subListHeadHide, 
                    index, 
                    conflictHandler, 
                    trigger);
        attachProcessor.initialize(element);
        E retval = this.setImpl(element, attachProcessor);
        attachProcessor.flush();
        return retval;
    }

    protected abstract void clear(int subListHeadHide, int subListTailHide, Trigger<E> trigger);
    
    protected abstract boolean remove(int subListHeadHide, int subListTailHide, Object o, Trigger<E> trigger);

    protected abstract E removeAt(int subListHeadHide, int subListTailHide, int index, Trigger<E> trigger);

    protected abstract boolean removeAll(int subListHeadHide, int subListTailHide, Collection<?> c, Trigger<E> trigger);

    protected abstract boolean retainAll(int subListHeadHide, int subListTailHide, Collection<?> c, Trigger<E> trigger);

    protected abstract void addImpl(E element, AttachProcessor<E> attachProcessor);

    protected abstract void addAllImpl(Collection<? extends E> c, AttachProcessor<E> attachProcessor);

    protected abstract E setImpl(E element, AttachProcessor<E> attachProcessor);
    
    protected boolean isNodeSupported() {
        return false;
    }
    
    protected final AttachProcessor<E> attachProcessorOf(
            boolean settingOperation,
            int headHide, 
            int index, 
            BaseElementsConflictHandler conflictHandler, 
            Trigger<E> trigger) {
        return this.new AttachProcessorImpl(settingOperation, headHide, index, conflictHandler, trigger);
    }

    protected final Trigger<E> triggerOf(int headHide, BaseElementsHandler<E> handler) {
        return this.triggerOf(headHide, handler, 0);
    }

    protected final Trigger<E> triggerOf(int headHide, BaseElementsHandler<E> handler, int capacity) {
        if (this.isFrozen()) {
            throw new IllegalStateException(LAZY_RESOURCE.get().canNotBeModifiedBecauseThisIsFrozen());
        }
        if (handler != null) {
            return this.new TriggerImpl(headHide, handler, capacity);
        }
        return null;
    }

    protected final void checkSubListRange(int subListHeadHide, int subListTailHide) {
        Arguments.mustBeGreaterThanOrEqualToValue("subListHeadHide", subListHeadHide, 0);
        Arguments.mustBeGreaterThanOrEqualToValue("subListTailHide", subListTailHide, 0);
        Arguments.mustBeLessThanOrEqualToValue(
                "subListHeadHide + subListTailHide", 
                subListHeadHide + subListTailHide, 
                this.allSize());
    }
    
    protected final void freeze() {
        this.frozenCount++;
    }
    
    protected final void unfreeze() {
        if (this.frozenCount == 0) {
            throw new IllegalStateException(LAZY_RESOURCE.get().canNotBeUnfreezonBecauseThisIsNotFrozen());
        }
        this.frozenCount--;
    }

    private static String attachProcessorState(int attachProcessState) {
        switch (attachProcessState) {
        case ATTACH_PROCESSOR_FLUSHED:
            return "FLUSHED";
        case ATTACH_PROCESSOR_EXECUTED:
            return "EXECUTED";
        case ATTACH_PROCESSOR_EXECUTING:
            return "EXECUTING";
        case ATTACH_PROCESSOR_RESOLVED:
            return "RESOLVED";
        case ATTACH_PROCESSOR_INITIALIZED:
            return "INITIALIZED";
        default:
            return "NEW";
        }
    }
    
    private static String triggerState(int triggerState) {
        switch (triggerState) {
        case TRIGGER_FLUSHED:
            return "FLUSHED";
        case TRIGGER_EXECUTED:
            return "EXECUTED";
        case TRIGGER_EXECUTING:
            return "EXECUTING";
        default:
            return "NEW";
        }
    }
    
    protected abstract class AbstractBaseElementIteratorImpl implements BaseListIterator<E> {
        
        private int subListHeadHide;
        
        private int subListTailHide;
        
        private int index;
        
        private int expectedModCount;
        
        private int lastRetIndex;
        
        private BaseElementsConflictHandler conflictHandler;
                
        AbstractBaseElementIteratorImpl(
                int subListHeadHide, 
                int subListTailHide, 
                int index,
                BaseElementsConflictHandler conflictHandler) {
            AbstractBaseElementsImpl.this.checkSubListRange(subListHeadHide, subListTailHide);
            Arguments.indexMustBetweenValue(
                    "index", 
                    index, 
                    0, 
                    true, 
                    AbstractBaseElementsImpl.this.allSize() - subListHeadHide - subListTailHide, 
                    true //for adding, and iterator, it is true; for other, it is false
            ); 
            this.subListHeadHide = subListHeadHide;
            this.subListTailHide = subListTailHide;
            this.index = index;
            this.conflictHandler = conflictHandler;
            this.expectedModCount = AbstractBaseElementsImpl.this.modCount();
            this.lastRetIndex = -1;
        }

        protected Trigger<E> trigger(BaseElementsHandler<E> handler) {
            return AbstractBaseElementsImpl.this.triggerOf(this.subListHeadHide, handler);
        }
        
        protected final int lastReturnedIndex() {
            return this.lastRetIndex;
        }
        
        protected abstract E get(int absoluteIndex);
        
        protected abstract void reset();
        
        @Override
        public boolean hasNext() {
            return 
                this.index 
                < 
                AbstractBaseElementsImpl.this.allSize() - 
                this.subListHeadHide - 
                this.subListTailHide;
        }
        
        @Override
        public E next() {
            if (this.expectedModCount != AbstractBaseElementsImpl.this.modCount()) {
                throw new ConcurrentModificationException(LAZY_ITERATOR_RESOURCE.get().concurrentModifcation());
            }
            if (this.index >= AbstractBaseElementsImpl.this.allSize() - this.subListHeadHide - this.subListTailHide) {
                throw new NoSuchElementException(LAZY_ITERATOR_RESOURCE.get().noSuchElement());
            }
            E e = this.get(this.subListHeadHide + this.index);
            this.lastRetIndex = this.index++;
            return e;
        }
        
        @Override
        public int nextIndex() {
            return this.index;
        }
        
        @Override
        public boolean hasPrevious() {
            return this.index > 0;
        }
        
        @Override
        public E previous() {
            if (this.expectedModCount != AbstractBaseElementsImpl.this.modCount()) {
                throw new ConcurrentModificationException(LAZY_ITERATOR_RESOURCE.get().concurrentModifcation());
            }
            if (index <= 0) {
                throw new NoSuchElementException(LAZY_ITERATOR_RESOURCE.get().noSuchElement());
            }
            E e = this.get(this.subListHeadHide + this.index - 1);
            this.lastRetIndex = --this.index;
            return e;
        }
        
        @Override
        public int previousIndex() {
            return this.index - 1;
        }
        
        @Override
        public void remove(BaseElementsHandler<E> handler) {
            if (this.expectedModCount != AbstractBaseElementsImpl.this.modCount()) {
                throw new ConcurrentModificationException(LAZY_ITERATOR_RESOURCE.get().concurrentModifcation());
            }
            int originalLastRetIndex = this.lastRetIndex;
            if (originalLastRetIndex == -1) {
                throw new IllegalStateException(LAZY_ITERATOR_RESOURCE.get().removeNoExtractedElement());
            }
            Trigger<E> trigger = this.trigger(handler);
            if (trigger != null) {
                trigger.preRemove(
                        originalLastRetIndex, 
                        this.get(this.subListHeadHide + originalLastRetIndex));
            }
            if (trigger == null || trigger.beginExecute()) {
                try {
                    AbstractBaseElementsImpl.this.removeAt(
                            this.subListHeadHide, 
                            this.subListTailHide, 
                            originalLastRetIndex, 
                            (Trigger<E>)null);
                    if (originalLastRetIndex < this.index) {
                        this.index--;
                    }
                    this.expectedModCount = AbstractBaseElementsImpl.this.modCount();
                    this.reset();
                    this.lastRetIndex = -1;
                    if (trigger != null) {
                        trigger.endExecute(null);
                    }
                } catch (RuntimeException | Error ex) {
                    if (trigger == null) {
                        throw ex;
                    }
                    trigger.endExecute(ex);
                }
            }
            if (trigger != null) {
                trigger.flush();
            }
        }

        @Override
        public void add(E e, BaseElementsHandler<E> handler) {
            if (this.expectedModCount != AbstractBaseElementsImpl.this.modCount()) {
                throw new ConcurrentModificationException(LAZY_ITERATOR_RESOURCE.get().concurrentModifcation());
            }
            AbstractBaseElementsImpl.this.add(
                    this.subListHeadHide, 
                    this.subListTailHide, 
                    this.index, 
                    e,
                    this.trigger(handler),
                    this.new WrappedConflictHandler(this.conflictHandler, true));
            this.index++;
            this.expectedModCount = AbstractBaseElementsImpl.this.modCount();
            this.reset();
            this.lastRetIndex = -1;
        }

        @Override
        public void set(E e, BaseElementsHandler<E> handler) {
            if (this.expectedModCount != AbstractBaseElementsImpl.this.modCount()) {
                throw new ConcurrentModificationException(LAZY_ITERATOR_RESOURCE.get().concurrentModifcation());
            }
            int originalLastRetIndex = this.lastRetIndex;
            if (originalLastRetIndex == -1) {
                throw new IllegalStateException(LAZY_RESOURCE.get().setNoExtractedElement());
            }
            AbstractBaseElementsImpl.this.set(
                    this.subListHeadHide, 
                    this.subListTailHide, 
                    this.lastRetIndex, 
                    e, 
                    this.trigger(handler), 
                    this.new WrappedConflictHandler(this.conflictHandler, false));
            //Here, invoke get to update some information during reading(see LinkedElements)
            this.get(this.lastRetIndex = this.subListHeadHide + this.lastRetIndex);
            this.expectedModCount = AbstractBaseElementsImpl.this.modCount();
        }
        
        private class WrappedConflictHandler implements BaseElementsConflictHandler {
            
            private BaseElementsConflictHandler rawConflictHandler;
            
            private boolean add;
            
            WrappedConflictHandler(BaseElementsConflictHandler rawConflictHandler, boolean add) {
                this.rawConflictHandler = rawConflictHandler;
                this.add = add;
            }

            @Override
            public Object resovling(int absSize, NavigableSet<Integer> conflictAbsIndexes) {
                AbstractBaseElementIteratorImpl that = AbstractBaseElementIteratorImpl.this;
                int headHide = that.subListHeadHide;
                int index = that.index;
                int lastRetIndex = that.lastRetIndex;
                if (index >= headHide) {
                    that.index -= conflictAbsIndexes.subSet(headHide, true, index, this.add).size();
                }
                if (lastRetIndex > headHide) {
                    that.lastRetIndex -= conflictAbsIndexes.subSet(headHide, lastRetIndex).size();
                }
                that.subListHeadHide -= conflictAbsIndexes.headSet(headHide).size();
                that.subListTailHide -= conflictAbsIndexes.tailSet(AbstractBaseElementsImpl.this.allSize() - that.subListTailHide).size();
                return this.rawConflictHandler == null ?
                            null :
                            this.rawConflictHandler.resovling(absSize, conflictAbsIndexes);
            }

            @Override
            public void resolved(Object retValOfResolving) {
                if (this.rawConflictHandler != null) {
                    this.rawConflictHandler.resolved(retValOfResolving);
                }
            }
            
        }
        
    }
    
    protected interface Node<E> {
        
        E get();
        
        void set(E value);
        
    }
    
    protected interface Trigger<E> {
        
        int getHeadHide();
        
        void setConflictAbsIndexes(NavigableSet<Integer> conflictAbsIndexes);
        
        void preAdd(int index, E element);
        
        void preChange(int oldIndex, int newIndex, E oldElement, E newElement);
        
        void preChange(int oldIndex, int newIndex, Node<E> node, E newElement);
        
        void preRemove(int index, E element);
        
        void preRemove(int index, Node<E> node);
        
        boolean flush();

        boolean flush(int limit);
        
        boolean beginExecute();
        
        void endExecute(Throwable nullOrThrowable);
        
        int getLength();
        
        int getFlushedLength();
        
        History<E> getHistory(int offset);
        
        interface History<E> {
            
            int getOffset();
            
            ModificationType getModificationType(int index);
            
            int getOldIndex(int index);
            
            int getNewIndex(int index);
            
            E getOldElement(int index);
            
            E getNewElement(int index);
            
            Node<E> getNode(int index);
            
        }
        
    }
    
    private class TriggerImpl implements Trigger<E> {
        
        private int headHide;
        
        private BaseElementsHandler<E> handler;
        
        private int len;
        
        private int flushedLen;
        
        private Object[] arr;
        
        private int[] indexArr;
        
        private int elementItemCount;
        
        private History<E> baseHistory;
        
        private int handlerItem = -1;
        
        private int nodeItem = -1;
        
        private int state = TRIGGER_NEW;
        
        private Throwable finalThrowable;
        
        private TriggerImpl(int headHide, BaseElementsHandler<E> handler, int capacity) {
            Arguments.mustNotBeNull("handler", handler);
            this.headHide = headHide;
            AbstractBaseElementsImpl<E> owner = AbstractBaseElementsImpl.this;
            this.handler = handler;
            int elementCount = 2;
            if (handler != null) {
                this.handlerItem = elementCount++;
            }
            if (owner.isNodeSupported()) {
                this.nodeItem = elementCount++;
            }
            this.elementItemCount = elementCount;
            this.baseHistory = this.new HistoryImpl();
            if (capacity > 0) {
                this.arr = new Object[capacity * this.elementItemCount];
                this.indexArr = new int[capacity << 1];
            }
        }

        @Override
        public int getHeadHide() {
            return this.headHide;
        }
        
        @Override
        public void setConflictAbsIndexes(NavigableSet<Integer> conflictAbsIndexes) {
            this.handler.setConflictAbsIndexes(conflictAbsIndexes);
        }

        @Override
        public void preAdd(int index, E element) {
            if (this.state != TRIGGER_NEW) {
                throw new IllegalStateException(
                        LAZY_RESOURCE.get().invalidOperationOnTrigger(
                                "preAdd", 
                                triggerState(TRIGGER_NEW), 
                                triggerState(this.state)
                        )
                );
            }
            BaseElementsHandler<E> handler = this.handler;
            int len = this.len;
            this.expand();
            Object[] arr = this.arr;
            int[] indexArr = this.indexArr;
            int indexOffset = len << 1;
            int elementItemOffset = this.elementItemCount * len;
            indexArr[indexOffset + 1] = index;
            arr[elementItemOffset] = TRIGGER_NIL_ELEMENT;
            arr[elementItemOffset + 1] = element;
            int handlerItem = this.handlerItem;
            if (handlerItem != -1) {
                AbstractBaseElementsImpl<E> owner = AbstractBaseElementsImpl.this;
                owner.freeze();
                try {
                    Object argument = handler.createAddingArgument(index, element);
                    if (argument != null) {
                        try {
                            handler.adding(index, element, argument);
                        } catch (RuntimeException | Error ex) {
                            if (this.finalThrowable == null) {
                                this.finalThrowable = ex;
                            }
                            handler.setPreThrowable(argument, ex);
                        }
                        arr[elementItemOffset + handlerItem] = argument;
                    }
                } finally {
                    owner.unfreeze();
                }
            }
            this.len = len + 1;
        }
        
        @Override
        public void preChange(int detachedIndex, int attachedIndex, E oldElement, E newElement) {
            if (this.state != TRIGGER_NEW) {
                throw new IllegalStateException(
                        LAZY_RESOURCE.get().invalidOperationOnTrigger(
                                "preChange", 
                                triggerState(TRIGGER_NEW), 
                                triggerState(this.state)
                        )
                );
            }
            BaseElementsHandler<E> handler = this.handler;
            int len = this.len;
            this.expand();
            Object[] arr = this.arr;
            int[] indexArr = this.indexArr;
            int indexOffset = len << 1;
            int elementItemOffset = this.elementItemCount * len;
            indexArr[indexOffset] = detachedIndex;
            indexArr[indexOffset + 1] = attachedIndex;
            arr[elementItemOffset] = oldElement;
            arr[elementItemOffset + 1] = newElement;
            int handlerItem = this.handlerItem;
            if (handlerItem != -1) {
                AbstractBaseElementsImpl<E> owner = AbstractBaseElementsImpl.this;
                owner.freeze();
                try {
                    Object argument = handler.createChangingArgument(detachedIndex, attachedIndex, oldElement, newElement);
                    if (argument != null) {
                        try {
                            handler.changing(detachedIndex, attachedIndex, oldElement, newElement, argument);
                        } catch (RuntimeException | Error ex) {
                            if (this.finalThrowable == null) {
                                this.finalThrowable = ex;
                            }
                            handler.setPreThrowable(argument, ex);
                        }
                        arr[elementItemOffset + handlerItem] = argument;
                    }
                } finally {
                    owner.unfreeze();
                }
            }
            this.len = len + 1;
        }
        
        @Override
        public void preChange(int detachedIndex, int attachedIndex, Node<E> node, E newElement) {
            if (this.state != TRIGGER_NEW) {
                throw new IllegalStateException(
                        LAZY_RESOURCE.get().invalidOperationOnTrigger(
                                "preChange", 
                                triggerState(TRIGGER_NEW), 
                                triggerState(this.state)
                        )
                );
            }
            Arguments.mustNotBeNull("node", node);
            if (this.nodeItem == -1) {
                throw new UnsupportedOperationException();
            }
            BaseElementsHandler<E> handler = this.handler;
            int len = this.len;
            this.expand();
            Object[] arr = this.arr;
            int[] indexArr = this.indexArr;
            int indexOffset = len << 1;
            int elementItemOffset = this.elementItemCount * len;
            E oldElement = node.get();
            indexArr[indexOffset] = detachedIndex;
            indexArr[indexOffset + 1] = attachedIndex;
            arr[elementItemOffset] = oldElement;
            arr[elementItemOffset + 1] = newElement;
            int handlerItem = this.handlerItem;
            if (handlerItem != -1) {
                AbstractBaseElementsImpl<E> owner = AbstractBaseElementsImpl.this;
                owner.freeze();
                try {
                    Object argument = handler.createChangingArgument(detachedIndex, attachedIndex, oldElement, newElement);
                    if (argument != null) {
                        try {
                            handler.changing(detachedIndex, attachedIndex, oldElement, newElement, argument);
                        } catch (RuntimeException | Error ex) {
                            if (this.finalThrowable == null) {
                                this.finalThrowable = ex;
                            }
                            handler.setPreThrowable(argument, ex);
                        }
                        arr[elementItemOffset + handlerItem] = argument;
                    }
                } finally {
                    owner.unfreeze();
                }
            }
            arr[elementItemOffset + this.nodeItem] = node;
            this.len = len + 1;
        }

        @Override
        public void preRemove(int index, E element) {
            if (this.state != TRIGGER_NEW) {
                throw new IllegalStateException(
                        LAZY_RESOURCE.get().invalidOperationOnTrigger(
                                "preRemove", 
                                triggerState(TRIGGER_NEW), 
                                triggerState(this.state)
                        )
                );
            }
            BaseElementsHandler<E> handler = this.handler;
            int len = this.len;
            this.expand();
            Object[] arr = this.arr;
            int[] indexArr = this.indexArr;
            int indexOffset = len << 1;
            int elementItemOffset = this.elementItemCount * len;
            indexArr[indexOffset] = index;
            arr[elementItemOffset] = element;
            arr[elementItemOffset + 1] = TRIGGER_NIL_ELEMENT;
            int handlerItem = this.handlerItem;
            if (handlerItem != -1) {
                AbstractBaseElementsImpl<E> owner = AbstractBaseElementsImpl.this;
                owner.freeze();
                try {
                    Object argument = handler.createRemovingArgument(index, element);
                    try {
                        handler.removing(index, element, argument);
                    } catch (RuntimeException | Error ex) {
                        if (this.finalThrowable == null) {
                            this.finalThrowable = ex;
                        }
                        handler.setPreThrowable(argument, ex);
                    }
                    arr[elementItemOffset + handlerItem] = argument;
                } finally {
                    owner.unfreeze();
                }
            }
            this.len = len + 1;
        }
        
        @Override
        public void preRemove(int index, Node<E> node) {
            if (this.state != TRIGGER_NEW) {
                throw new IllegalStateException(
                        LAZY_RESOURCE.get().invalidOperationOnTrigger(
                                "preRemove", 
                                triggerState(TRIGGER_NEW), 
                                triggerState(this.state)
                        )
                );
            }
            Arguments.mustNotBeNull("node", node);
            if (this.nodeItem == -1) {
                throw new UnsupportedOperationException();
            }
            BaseElementsHandler<E> handler = this.handler;
            int len = this.len;
            this.expand();
            Object[] arr = this.arr;
            int[] indexArr = this.indexArr;
            int indexOffset = len << 1;
            int elementItemOffset = this.elementItemCount * len;
            E element = node.get();
            indexArr[indexOffset] = index;
            arr[elementItemOffset] = element;
            arr[elementItemOffset + 1] = TRIGGER_NIL_ELEMENT;
            int handlerItem = this.handlerItem;
            if (handlerItem != -1) {
                AbstractBaseElementsImpl<E> owner = AbstractBaseElementsImpl.this;
                owner.freeze();
                try {
                    Object argument = handler.createRemovingArgument(index, element);
                    try {
                        handler.removing(index, element, argument);
                    } catch (RuntimeException | Error ex) {
                        if (this.finalThrowable == null) {
                            this.finalThrowable = ex;
                        }
                        handler.setPreThrowable(argument, ex);
                    }
                    arr[elementItemOffset + handlerItem] = argument;
                } finally {
                    owner.unfreeze();
                }
            }
            arr[elementItemOffset + this.nodeItem] = node;
            this.len = len + 1;
        }
        
        @Override
        public boolean beginExecute() {
            if (this.state != TRIGGER_NEW) {
                throw new IllegalStateException(
                        LAZY_RESOURCE.get().invalidOperationOnTrigger(
                                "beginExecute", 
                                triggerState(TRIGGER_NEW), 
                                triggerState(this.state)
                        )
                );
            }
            if (this.finalThrowable != null) {
                this.state = TRIGGER_EXECUTED;
                return false;
            }
            this.state = TRIGGER_EXECUTING;
            return true;
        }

        @Override
        public void endExecute(Throwable nullOrThrowable) {
            if (this.state != TRIGGER_EXECUTING) {
                throw new IllegalStateException(
                        LAZY_RESOURCE.get().invalidOperationOnTrigger(
                                "endExcute", 
                                triggerState(TRIGGER_EXECUTING), 
                                triggerState(this.state)
                        )
                );
            }
            if (nullOrThrowable != null) {
                if (this.finalThrowable == null) {
                    this.finalThrowable = nullOrThrowable;
                }
            }
            this.handler.setNullOrThrowable(nullOrThrowable);
            this.state = TRIGGER_EXECUTED;
        }

        @Override
        public boolean flush() {
            return this.flush(Integer.MAX_VALUE);
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean flush(int limit) {
            if (this.state < TRIGGER_EXECUTED) {
                throw new IllegalStateException(
                        LAZY_RESOURCE.get().invalidOperationOnTrigger(
                                "flush", 
                                triggerState(TRIGGER_EXECUTED) +
                                LAZY_RESOURCE.get().orBetweenWordsWithQuotes() +
                                triggerState(TRIGGER_FLUSHED), 
                                triggerState(this.state)
                        )
                );
            }
            AbstractBaseElementsImpl<E> owner = AbstractBaseElementsImpl.this;
            int len = this.len;
            limit = Math.min(limit, len);
            int flushedLen = this.flushedLen;
            boolean retval = false;
            if (len != 0 && limit > flushedLen) {
                int[] indexArr = this.indexArr;
                Object[] arr = this.arr;
                int elementItemCount = this.elementItemCount;
                BaseElementsHandler<E> handler = this.handler;
                owner.freeze();
                try {
                    if (handler != null) {
                        int handlerItem = this.handlerItem;
                        for (int i = flushedLen; i < limit; i++) {
                            int indexOffset = i << 1;
                            int elementItemOffset = i * elementItemCount;
                            int detachedIndex = indexArr[indexOffset];
                            int attachedIndex = indexArr[indexOffset + 1];
                            E oldElement = (E)arr[elementItemOffset];
                            E newElement = (E)arr[elementItemOffset + 1];
                            Object argument = arr[elementItemOffset + handlerItem];
                            try {
                                if (newElement == TRIGGER_NIL_ELEMENT) {
                                    handler.removed(detachedIndex, oldElement, argument);
                                } else if (oldElement == TRIGGER_NIL_ELEMENT) {
                                    handler.added(attachedIndex, newElement, argument);
                                } else {
                                    handler.changed(detachedIndex, attachedIndex, oldElement, newElement, argument);
                                }
                            } catch (RuntimeException | Error ex) {
                                if (this.finalThrowable == null) {
                                    this.finalThrowable = ex;
                                }
                            }
                        }
                    }
                } finally {
                    owner.unfreeze();
                }
                this.flushedLen = limit;
                retval = true;
            }
            this.state = TRIGGER_FLUSHED;
            if (this.flushedLen == len && this.finalThrowable != null) {
                //Actually, finalThrowable can only be RuntimeException or Error
                UncheckedException.rethrow(this.finalThrowable);
            } 
            return retval;
        }

        @Override
        public int getLength() {
            return this.len;
        }

        @Override
        public int getFlushedLength() {
            return this.flushedLen;
        }

        @Override
        public History<E> getHistory(int offset) {
            if (offset == 0) {
                return this.baseHistory;
            }
            return this.new OffsetHistoryImpl(offset);
        }
        
        private void expand() {
            int elementItemCount = this.elementItemCount;
            int offset = elementItemCount * this.len;
            Object[] arr = this.arr;
            int[] indexArr = this.indexArr;
            if (arr == null) {
                this.arr = new Object[elementItemCount];
                this.indexArr = new int[2];
            } else if (arr.length == offset) {
                Object[] newArr = new Object[arr.length << 1];
                int[] newIndexArr = new int[indexArr.length << 1];
                System.arraycopy(arr, 0, newArr, 0, arr.length);
                System.arraycopy(indexArr, 0, newIndexArr, 0, indexArr.length);
                this.arr = newArr;
                this.indexArr = newIndexArr;
            }
        }
        
        private class HistoryImpl implements History<E> {

            @Override
            public int getOffset() {
                return 0;
            }

            @Override
            public ModificationType getModificationType(int index) {
                TriggerImpl owner = TriggerImpl.this;
                int offset = index * owner.elementItemCount;
                Object[] arr = owner.arr;
                if (arr[offset] == TRIGGER_NIL_ELEMENT) {
                    return ModificationType.ATTACH;
                }
                if (arr[offset + 1] == TRIGGER_NIL_ELEMENT) {
                    return ModificationType.DETACH;
                }
                return ModificationType.REPLACE;
            }

            @Override
            public int getOldIndex(int index) {
                return TriggerImpl.this.indexArr[index << 1];
            }
            
            @Override
            public int getNewIndex(int index) {
                return TriggerImpl.this.indexArr[(index << 1) + 1];
            }

            @SuppressWarnings("unchecked")
            @Override
            public E getOldElement(int index) {
                TriggerImpl owner = TriggerImpl.this;
                Object o = owner.arr[index * owner.elementItemCount];
                return o == TRIGGER_NIL_ELEMENT ? null : (E)o;
            }

            @SuppressWarnings("unchecked")
            @Override
            public E getNewElement(int index) {
                TriggerImpl owner = TriggerImpl.this;
                Object o = owner.arr[index * owner.elementItemCount + 1];
                return o == TRIGGER_NIL_ELEMENT ? null : (E)o;
            }
            
            @SuppressWarnings("unchecked")
            @Override
            public Node<E> getNode(int index) {
                TriggerImpl owner = TriggerImpl.this;
                int nodeItem = owner.nodeItem;
                if (nodeItem != -1) {
                    return (Node<E>)owner.arr[index * owner.elementItemCount + nodeItem];
                }
                return null;
            }
        }
        
        private class OffsetHistoryImpl extends HistoryImpl {
            
            private int offset;
            
            OffsetHistoryImpl(int offset) {
                this.offset = offset;
            }

            @Override
            public int getOffset() {
                return this.offset;
            }

            @Override
            public int getOldIndex(int index) {
                return super.getOldIndex(index + this.offset);
            }
            
            @Override
            public int getNewIndex(int index) {
                return super.getNewIndex(index + this.offset);
            }

            @Override
            public E getOldElement(int index) {
                return super.getOldElement(index + this.offset);
            }

            @Override
            public E getNewElement(int index) {
                return super.getNewElement(index + this.offset);
            }
            
            @Override
            public Node<E> getNode(int index) {
                return super.getNode(index + this.offset);
            }
            
        }
        
    }
    
    protected interface AttachProcessor<E> {
        
        Trigger<E> getTrigger();

        void initialize(E element);
        
        Collection<? extends E> intialize(Collection<? extends E> c);
        
        boolean beginExecute();
        
        void endExecute(Throwable nullOrThrowable);
        
        void flush();
        
        int getHeadHide();
        
        int getExpectedIndex(boolean absolute);
        
        int getActualIndex(boolean absolute);
        
    }
    
    private class AttachProcessorImpl implements AttachProcessor<E> {
        
        private BaseElementsConflictHandler conflictHandler;
        
        private Trigger<E> trigger;
        
        private int headHide;
        
        private int actualHeadHide;
        
        private int expectedIndex;
        
        private int actualIndex;
        
        private NavigableSet<Integer> conflictIndexes;
        
        private Object retvalOfResolving;
        
        private int state = ATTACH_PROCESSOR_NEW;
        
        private int triggerLenBeforeAddChangeExecute;
        
        private boolean settingOperation;
        
        AttachProcessorImpl(
                boolean settingOperation,
                int headHide, 
                int index,
                BaseElementsConflictHandler conflictHandler, 
                Trigger<E> trigger) {
            if (trigger != null) {
                Arguments.mustBeEqualToOtherWhen(
                        LAZY_RESOURCE.get().whenTheTriggerIsNotNull(),
                        "headHide", 
                        headHide, 
                        "trigger.getHeadHide()", 
                        trigger.getHeadHide());
            }
            this.settingOperation = settingOperation;
            this.headHide = headHide;
            this.actualHeadHide = headHide;
            this.expectedIndex = index;
            this.actualIndex = index;
            this.conflictHandler = conflictHandler;
            this.trigger = trigger;
        }

        @Override
        public Trigger<E> getTrigger() {
            if (this.trigger == null) {
                return null;
            }
            return new Trigger<E>() {

                @Override
                public int getHeadHide() {
                    return AttachProcessorImpl.this.trigger.getHeadHide();
                }

                @Override
                public void setConflictAbsIndexes(NavigableSet<Integer> conflictAbsIndexes) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void preAdd(int index, E element) {
                    AttachProcessorImpl owner = AttachProcessorImpl.this;
                    owner.beforeExecuteOrGetTrigger();
                    owner.trigger.preAdd(index, element);
                }

                @Override
                public void preChange(int oldIndex, int newIndex, E oldElement, E newElement) {
                    AttachProcessorImpl owner = AttachProcessorImpl.this;
                    owner.beforeExecuteOrGetTrigger();
                    owner.trigger.preChange(oldIndex, newIndex, oldElement, newElement);
                }

                @Override
                public void preChange(int oldIndex, int newIndex, Node<E> node, E newElement) {
                    AttachProcessorImpl owner = AttachProcessorImpl.this;
                    owner.beforeExecuteOrGetTrigger();
                    owner.trigger.preChange(oldIndex, newIndex, node, newElement);
                }

                @Override
                public void preRemove(int index, E element) {
                    AttachProcessorImpl owner = AttachProcessorImpl.this;
                    owner.beforeRemove();
                    owner.trigger.preRemove(index, element);
                }

                @Override
                public void preRemove(int index, Node<E> node) {
                    AttachProcessorImpl owner = AttachProcessorImpl.this;
                    owner.beforeRemove();
                    owner.trigger.preRemove(index, node);
                }

                @Override
                public boolean beginExecute() {
                    throw new UnsupportedOperationException(
                            "This trigger is belong to " +
                            AttachProcessor.class.getName() +
                            ", please invoke the " +
                            AttachProcessor.class.getName() +
                            ".beginExecute");
                }

                @Override
                public void endExecute(Throwable nullOrThrowable) {
                    throw new UnsupportedOperationException(
                            "This trigger is belong to " +
                            AttachProcessor.class.getName() +
                            ", please invoke the " +
                            AttachProcessor.class.getName() +
                            ".endExecute");
                }

                @Override
                public boolean flush() {
                    throw new UnsupportedOperationException(
                            "This trigger is belong to " +
                            AttachProcessor.class.getName() +
                            ", please invoke the " +
                            AttachProcessor.class.getName() +
                            ".flush");
                }

                @Override
                public boolean flush(int limit) {
                    throw new UnsupportedOperationException(
                            "This trigger is belong to " +
                            AttachProcessor.class.getName() +
                            ", please invoke the " +
                            AttachProcessor.class.getName() +
                            ".flush");
                }

                @Override
                public int getLength() {
                    return AttachProcessorImpl.this.trigger.getLength();
                }

                @Override
                public int getFlushedLength() {
                    return AttachProcessorImpl.this.trigger.getFlushedLength();
                }

                @Override
                public History<E> getHistory(int offset) {
                    return AttachProcessorImpl.this.trigger.getHistory(offset);
                }
                
            };
        }

        @Override
        public void initialize(E element) {
            if (this.state != ATTACH_PROCESSOR_NEW) {
                throw new IllegalStateException(
                        LAZY_RESOURCE.get().invalidOperationOnAttachProcessor(
                                "initialize",
                                attachProcessorState(ATTACH_PROCESSOR_NEW),
                                attachProcessorState(this.state)
                        )
                );
            }
            AbstractBaseElementsImpl<E> owner = AbstractBaseElementsImpl.this;
            Validator<E> validator = owner.validator;
            if (validator != null) {
                validator.validate(element);
            }
            ListConflictVoterManager<E> voterManager = owner.voterManager;
            if (voterManager != null) {
                this.initalize0(voterManager.vote(element));
            }
            this.state = ATTACH_PROCESSOR_INITIALIZED;
        }

        @Override
        public Collection<? extends E> intialize(Collection<? extends E> c) {
            if (this.settingOperation) {
                throw new IllegalStateException(LAZY_RESOURCE.get().canNotInitWithCollectionWhenSetting());
            }
            if (this.state != ATTACH_PROCESSOR_NEW) {
                throw new IllegalStateException(
                        LAZY_RESOURCE.get().invalidOperationOnAttachProcessor(
                                "initialize",
                                attachProcessorState(ATTACH_PROCESSOR_NEW),
                                attachProcessorState(this.state)
                        )
                );
            }
            Arguments.mustNotBeNull("c", c);
            AbstractBaseElementsImpl<E> owner = AbstractBaseElementsImpl.this;
            Validator<E> validator = owner.validator;
            if (validator != null) {
                for (E e : c) {
                    validator.validate(e);
                }
            }
            ListConflictVoterManager<E> voterManager = owner.voterManager;
            if (voterManager != null) {
                Ref<Collection<? extends E>> cRef = new Ref<Collection<? extends E>>(c);
                this.initalize0(voterManager.voteAll(cRef));
                c = cRef.get();
            }
            this.state = ATTACH_PROCESSOR_INITIALIZED;
            return c;
        }
        
        @Override
        public boolean beginExecute() {
            this.beforeExecuteOrGetTrigger();
            if (this.trigger != null && !this.trigger.beginExecute()) {
                this.state = ATTACH_PROCESSOR_EXECUTED;
                return false;
            }
            AbstractBaseElementsImpl<E> owner = AbstractBaseElementsImpl.this;
            NavigableSet<Integer> conflictIndexes = this.conflictIndexes;
            if (conflictIndexes != null && !conflictIndexes.isEmpty()) {
                if (owner.randomAccess() || conflictIndexes.size() < 3) {
                    for (Integer conflictIndex : conflictIndexes.descendingSet()) {
                        owner.removeAt(0, 0, conflictIndex, (Trigger<E>)null);
                    }
                } else {
                    Iterator<Integer> descendingConflictIndexItr = conflictIndexes.descendingIterator();
                    int conflictIndex = descendingConflictIndexItr.next();
                    BaseListIterator<E> itr = owner.listIterator(0, 0, owner.allSize(), null);
                    while (itr.hasPrevious()) {
                        itr.previous();
                        if (itr.previousIndex() + 1 == conflictIndex) {
                            itr.remove(null);
                            if (descendingConflictIndexItr.hasNext()) {
                                conflictIndex = descendingConflictIndexItr.next();
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
            this.state = ATTACH_PROCESSOR_EXECUTING;
            return true;
        }
        
        @Override
        public void endExecute(Throwable nullOrThrowable) {
            if (this.state != ATTACH_PROCESSOR_EXECUTING) {
                throw new IllegalStateException(
                        LAZY_RESOURCE.get().invalidOperationOnAttachProcessor(
                                "endExecute",
                                attachProcessorState(ATTACH_PROCESSOR_EXECUTING),
                                attachProcessorState(this.state)
                        )
                );
            }
            Trigger<E> trigger = this.trigger;
            if (trigger != null) {
                trigger.endExecute(nullOrThrowable);
            }
            this.state = ATTACH_PROCESSOR_EXECUTED;
        }

        @Override
        public void flush() {
            if (this.state == ATTACH_PROCESSOR_FLUSHED) {
                return;
            }
            if (this.state != ATTACH_PROCESSOR_EXECUTED) {
                throw new IllegalStateException(
                        LAZY_RESOURCE.get().invalidOperationOnAttachProcessor(
                                "flush",
                                attachProcessorState(ATTACH_PROCESSOR_EXECUTED) +
                                LAZY_RESOURCE.get().orBetweenWordsWithQuotes() +
                                attachProcessorState(ATTACH_PROCESSOR_FLUSHED),
                                attachProcessorState(this.state)
                        )
                );
            }
            BaseElementsConflictHandler conflictHandler = this.conflictHandler;
            Trigger<E> trigger = this.trigger;
            if (trigger != null) {
                trigger.flush(this.triggerLenBeforeAddChangeExecute);
            }
            if (conflictHandler != null && this.conflictIndexes != null) {
                AbstractBaseElementsImpl<E> owner = AbstractBaseElementsImpl.this;
                owner.freeze();
                try {
                    conflictHandler.resolved(this.retvalOfResolving);
                } finally {
                    owner.unfreeze();
                }
            }
            if (trigger != null) {
                trigger.flush();
            }
            this.state = ATTACH_PROCESSOR_FLUSHED;
        }
        
        @Override
        public int getHeadHide() {
            return this.headHide;
        }

        @Override
        public int getExpectedIndex(boolean absolute) {
            return absolute ? this.headHide + this.expectedIndex : this.expectedIndex; 
        }

        @Override
        public int getActualIndex(boolean absolute) {
            return absolute ? this.actualHeadHide + this.actualIndex : this.actualIndex;
        }

        private void initalize0(NavigableSet<Integer> conflictIndexes) {
            if (this.settingOperation) {
                conflictIndexes.remove(this.headHide + this.expectedIndex);
            }
            Trigger<E> trigger = this.trigger;
            AbstractBaseElementsImpl<E> owner = AbstractBaseElementsImpl.this;
            if (trigger != null && conflictIndexes != null && !conflictIndexes.isEmpty()) {
                int headHide = this.headHide;
                if (owner.randomAccess() || conflictIndexes.size() < 3) {
                    for (Integer conflictIndex : conflictIndexes) {
                        trigger.preRemove(conflictIndex - headHide, owner.get(0, 0, conflictIndex));
                    }
                } else {
                    Iterator<Integer> conflictIndexItr = conflictIndexes.iterator();
                    int conflictIndex = conflictIndexItr.next();
                    BaseListIterator<E> itr = owner.listIterator(0, 0, 0, null);
                    while (itr.hasNext()) {
                        int index = itr.nextIndex();
                        E element = itr.next();
                        if (conflictIndex == index) {
                            trigger.preRemove(index - headHide, element);
                            if (conflictIndexItr.hasNext()) {
                                conflictIndex = conflictIndexItr.next();
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
            this.actualIndex -= conflictIndexes.subSet(this.headHide, this.headHide + expectedIndex).size();
            this.actualHeadHide -= conflictIndexes.headSet(this.headHide).size();
            if (conflictIndexes != null) {
                this.conflictIndexes = conflictIndexes;
            }
            if (this.trigger != null) {
                this.trigger.setConflictAbsIndexes(conflictIndexes);
            }
        }

        private void beforeExecuteOrGetTrigger() {
            if (this.state == ATTACH_PROCESSOR_RESOLVED) {
                return;
            }
            if (this.state != ATTACH_PROCESSOR_INITIALIZED) {
                throw new IllegalStateException(
                        LAZY_RESOURCE.get().invalidOperationOnAttachProcessor(
                                "beforeExecute" +
                                LAZY_RESOURCE.get().orBetweenWordsWithQuotes() +
                                "getTrigger",
                                attachProcessorState(ATTACH_PROCESSOR_INITIALIZED) +
                                LAZY_RESOURCE.get().orBetweenWordsWithQuotes() +
                                attachProcessorState(ATTACH_PROCESSOR_RESOLVED),
                                attachProcessorState(this.state)
                        )
                );
            }
            BaseElementsConflictHandler conflictHandler = this.conflictHandler;
            NavigableSet<Integer> conflictIndexes = this.conflictIndexes;
            if (conflictHandler != null && !Nulls.isNullOrEmpty(this.conflictIndexes)) {
                AbstractBaseElementsImpl<E> owner = AbstractBaseElementsImpl.this;
                owner.freeze();
                try {
                    this.retvalOfResolving = 
                        conflictHandler.resovling(
                                AbstractBaseElementsImpl.this.allSize(),
                                conflictIndexes);
                } finally {
                    owner.unfreeze();
                }
            }
            Trigger<E> trigger = this.trigger;
            if (trigger != null) {
                this.triggerLenBeforeAddChangeExecute = trigger.getLength();
            }
            this.state = ATTACH_PROCESSOR_RESOLVED;
        }
        
        private void beforeRemove() {
            if (this.state != ATTACH_PROCESSOR_INITIALIZED) {
                throw new IllegalStateException(
                        LAZY_RESOURCE.get().invalidOperationOnAttachProcessor(
                                "beforeRemove",
                                attachProcessorState(ATTACH_PROCESSOR_INITIALIZED),
                                attachProcessorState(this.state)
                        )
                );
            }
        }
        
    }
    
    private class ListSourceImpl implements ListSource<E> {
        
        private static final long serialVersionUID = -431407971137982837L;

        @Override
        public XList<E> getList() {
            return new AbstractXList.SimpleImpl<E>(AbstractBaseElementsImpl.this);
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(AbstractBaseElementsImpl.this);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof AbstractBaseElementsImpl<?>.ListSourceImpl)) {
                return false;
            }
            AbstractBaseElementsImpl<?>.ListSourceImpl other = 
                    (AbstractBaseElementsImpl<?>.ListSourceImpl)obj;
            return AbstractBaseElementsImpl.this == other.owner();
        }
        
        private AbstractBaseElementsImpl<E> owner() {
            return AbstractBaseElementsImpl.this;
        }
    }

    private interface Resource {
        
        String canNotBeModifiedBecauseThisIsFrozen();
        
        String canNotBeUnfreezonBecauseThisIsNotFrozen();
        
        String setNoExtractedElement();
        
        String invalidOperationOnAttachProcessor(
                String operation, 
                String expectedAttachProcessorState, 
                String actualAttachProcessorState);
        
        String invalidOperationOnTrigger(
                String operation, 
                String expectedTriggerState, 
                String actualTriggerState);
        
        String canNotInitWithCollectionWhenSetting();
        
        String whenTheTriggerIsNotNull();
        
        String orBetweenWordsWithQuotes();
    }
}
