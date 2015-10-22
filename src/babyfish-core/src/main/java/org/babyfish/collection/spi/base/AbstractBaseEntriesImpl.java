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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.babyfish.collection.ElementMatcher;
import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.HashSet;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.OrderAdjustMode;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.TreeMap;
import org.babyfish.collection.TreeSet;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.UnknownUnifiedComparatorException;
import org.babyfish.collection.XMap;
import org.babyfish.collection.conflict.MAMapConflictVoter;
import org.babyfish.collection.conflict.MapConflictVoter;
import org.babyfish.collection.conflict.spi.MapConflictVoterManager;
import org.babyfish.collection.conflict.spi.MapSource;
import org.babyfish.collection.spi.AbstractMAMap;
import org.babyfish.collection.spi.AbstractMASet;
import org.babyfish.collection.spi.AbstractXMap;
import org.babyfish.collection.spi.base.AbstractBaseEntriesImpl.Trigger.History;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.IllegalOperationException;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.lang.Ref;
import org.babyfish.lang.UncheckedException;
import org.babyfish.modificationaware.ModificationAware;
import org.babyfish.modificationaware.event.ModificationType;
import org.babyfish.util.LazyResource;
import org.babyfish.validator.Validator;
import org.babyfish.validator.Validators;
import org.babyfish.view.View;

/**
 * @author Tao Chen
 */
public abstract class AbstractBaseEntriesImpl<K, V> implements BaseEntries<K, V> {
    
    private static final Object TRIGGER_NIL_KEY = new Object();
    
    private static final LazyResource<Resource> LAZY_RESOURCE = LazyResource.of(Resource.class);
    
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
    
    /*
     * For removeAllByEntryCollection and removeAllByKeyCollection.
     * both "this.contains(parameterCollectionElement)" and "parameterCollection.conatins(thisElement)"
     * can be used to decide which elements should be removed(If the unified comparator of this and
     * parameterCollection are not equals, OverriddenContainsBehavior can change the logic of
     * "parameterCollection.contains(thisElement)" to resolve this problem.
     * )
     * 
     * How to decide to use "this.contains(parameterCollectionElement)" and "parameterCollection.conatins(thisElement)"?
     * 
     * By reading the source code of java.util.AbstractSet, I found that the JDK's solution is to use the first one 
     * when "this.size() > parameterCollection.size()", otherwise, use the second one.
     * 
     * When this.size() <= parameterCollection.size(), uses the second choice, if the parameterCollection is not Set 
     * or its unified comparator is not equals the unique comparator of this collection(OverriddenContainsBehavior uses 
     * a slow algorithm to replace the default algorithms of "parameterCollection.contains()"), the 
     * "parameterCollection.contains()" is not a fast choice. So I used condition
     * "this.size * NO_SAME_UNIFIED_COMPARATOR_SET_CAPACITY_FACTORY <= parameterCollection.size()" to decide 
     * to use the "parameterCollection.contains()" choice, that means uses 
     * "this.size * NO_SAME_UNIFIED_COMPARATOR_SET_CAPACITY_FACTORY >= parameterCollection.size()" to decide 
     * to use the "this.contains()" choice.
     */
    private static final int CAPACITY_FACTORY_WHEN_PARAMETER_IS_NOT_SET_WITH_SAME_UNIFIED_COMPARATOR = 10;
    
    private Object rootEntriesOrRootData;
    
    /**
     * Create an instance that is
     */
    protected AbstractBaseEntriesImpl() {
        AbstractBaseEntriesImpl<K, V> parent = this.getParent();
        while (parent != null) {
            AbstractBaseEntriesImpl<K, V> parentParent = parent.getParent();
            if (parentParent == null) {
                break;
            }
            parent = parentParent;
        }
        this.rootEntriesOrRootData = parent == null ? new RootData<K, V>() : parent;
    }
    
    protected AbstractBaseEntriesImpl(
            ReplacementRule keyReplacementRule,
            Object keyComparatorOrEqualityComparatorOrUnifiedComparator,
            Object valueComparatorOrEqualityComparatorOrUnifiedComparator) {
        
        /*
         * It is very important to invoke this()!!!
         */
        this();
        
        Object o = this.rootEntriesOrRootData;
        if (!(o instanceof RootData<?, ?>)) {
            throw new IllegalProgramException(
                    LAZY_RESOURCE.get().constructorRequiresRootBaseEntries(
                            AbstractBaseEntriesImpl.class,
                            ReplacementRule.class,
                            Object.class,
                            Object.class
                    )
            );
        }
        @SuppressWarnings("unchecked")
        RootData<K, V> rootData = (RootData<K, V>)o;
        rootData.keyRestrict = keyReplacementRule != ReplacementRule.OLD_REFERENCE_WIN;
        rootData.keyComparatorOrEqualityComparator = 
            UnifiedComparator.unwrap(
                    keyComparatorOrEqualityComparatorOrUnifiedComparator);
        rootData.valueComparatorOrEqualityComparator = 
            UnifiedComparator.unwrap(
                    valueComparatorOrEqualityComparatorOrUnifiedComparator);
    }
    
    protected AbstractBaseEntriesImpl<K, V> getParent() {
        return null;
    }
    
    @SuppressWarnings("unchecked")
    protected final AbstractBaseEntriesImpl<K, V> getRoot() {
        Object o = this.rootEntriesOrRootData;
        if (o instanceof RootData<?, ?>) {
            return this;
        }
        return (AbstractBaseEntriesImpl<K, V>)o;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public final ModificationAware getMAOwner() {
        Object o = this.rootEntriesOrRootData;
        if (!(o instanceof RootData<?, ?>)) {
            throw new IllegalStateException(LAZY_RESOURCE.get().getMAOwnerCanOnlyBeInvokedByRootBaseEntries());
        }
        RootData<K, V> rootData = (RootData<K, V>)this.rootEntriesOrRootData;
        return rootData.owner;
    }
    
    @Override
    public boolean isRoot() {
        return this.rootEntriesOrRootData instanceof RootData<?, ?>;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initMAOwner(ModificationAware owner) {
        Arguments.mustNotBeNull("owner", owner);
        Arguments.mustNotBeInstanceOfValue("owner", owner, View.class);
        Arguments.mustBeInstanceOfAnyOfValue("owner", owner, AbstractMASet.class, AbstractMAMap.class);
        Object o = this.rootEntriesOrRootData;
        if (!(o instanceof RootData<?, ?>)) {
            throw new IllegalStateException(LAZY_RESOURCE.get().initMAOwnerCanOnlyBeInvokedByRootBaseEntries());
        }
        RootData<K, V> rootData = (RootData<K, V>)o;
        if (rootData.owner != null) {
            throw new IllegalStateException(LAZY_RESOURCE.get().initMAOwnerCanOnlyBeInvokedOnce());
        }
        rootData.owner = owner;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final ReplacementRule keyReplacementRule() {
        Object o = this.rootEntriesOrRootData;
        if (o instanceof RootData<?, ?>) {
            return ((RootData<K, V>)o).keyRestrict ? 
                    ReplacementRule.NEW_REFERENCE_WIN : 
                    ReplacementRule.OLD_REFERENCE_WIN;
        }
        return ((AbstractBaseEntriesImpl<K, V>)o).keyReplacementRule();
    }

    @SuppressWarnings("unchecked")
    @Override
    public final UnifiedComparator<? super K> keyUnifiedComparator() {
        Object o = this.rootEntriesOrRootData;
        if (o instanceof RootData<?, ?>) {
            return UnifiedComparator.nullToEmpty(
                    UnifiedComparator.<K>of(
                            ((RootData<K, V>)o).keyComparatorOrEqualityComparator));
        }
        return ((AbstractBaseEntriesImpl<K, V>)o).keyUnifiedComparator();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public final UnifiedComparator<? super V> valueUnifiedComparator() {
        Object o = this.rootEntriesOrRootData;
        if (o instanceof RootData<?, ?>) {
            return UnifiedComparator.nullToEmpty(
                    UnifiedComparator.<V>of(
                            ((RootData<K, V>)o).valueComparatorOrEqualityComparator));
        }
        return ((AbstractBaseEntriesImpl<K, V>)o).valueUnifiedComparator();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public final UnifiedComparator<? super Entry<K, V>> entryUnifiedComparator() {
        Object o = this.rootEntriesOrRootData;
        if (o instanceof RootData<?, ?>) {
            RootData<K, V> rootData = (RootData<K, V>)o;
            UnifiedComparator<? super Entry<K, V>> entryUnifiedComparator = rootData.entryUnifiedComparator;
            if (entryUnifiedComparator == null) {
                rootData.entryUnifiedComparator = 
                        entryUnifiedComparator =
                        UnifiedComparator.nullToEmpty(
                                UnifiedComparator.of(
                                        EntryEqualityComparator.<K, V>of(
                                            rootData.keyComparatorOrEqualityComparator, 
                                            rootData.valueComparatorOrEqualityComparator
                                        )
                                )
                        );
            }
            return entryUnifiedComparator;
        }
        return ((AbstractBaseEntriesImpl<K, V>)o).entryUnifiedComparator();
    }
    
    protected final Object keyComparatorOrEqualityComparator() {
        Object o = this.rootEntriesOrRootData;
        if (o instanceof RootData<?, ?>) {
            RootData<?, ?> rootData = (RootData<?, ?>)o;
            return rootData.keyComparatorOrEqualityComparator;
        }
        return ((AbstractBaseEntriesImpl<?, ?>)o).keyComparatorOrEqualityComparator();
    }
    
    protected final Object valueComparatorOrEqualityComparator() {
        Object o = this.rootEntriesOrRootData;
        if (o instanceof RootData<?, ?>) {
            RootData<?, ?> rootData = (RootData<?, ?>)o;
            return rootData.valueComparatorOrEqualityComparator;
        }
        return ((AbstractBaseEntriesImpl<?, ?>)o).valueComparatorOrEqualityComparator();
    }

    @SuppressWarnings("unchecked")
    protected final Validator<K> keyValidator() {
        Object o = this.rootEntriesOrRootData;
        if (o instanceof RootData<?, ?>) {
            return ((RootData<K, V>)o).keyValidator;
        }
        return ((AbstractBaseEntriesImpl<K, V>)o).keyValidator();
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void combineKeyValidator(Validator<K> validator) {
        Object o = this.rootEntriesOrRootData;
        if (o instanceof RootData<?, ?>) {
            RootData<K, V> rootData = (RootData<K, V>)o;
            rootData.keyValidator =
                Validators.combine(
                        rootData.keyValidator, 
                        validator);
        } else {
            ((AbstractBaseEntriesImpl<K, V>)o).combineKeyValidator(validator);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void removeKeyValidator(Validator<K> validator) {
        Object o = this.rootEntriesOrRootData;
        if (o instanceof RootData<?, ?>) {
            RootData<K, V> rootData = (RootData<K, V>)o;
            rootData.keyValidator =
                Validators.remove(
                        rootData.keyValidator, 
                        validator);
        } else {
            ((AbstractBaseEntriesImpl<K, V>)o).removeKeyValidator(validator);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void validateKey(K key) {
        Object o = this.rootEntriesOrRootData;
        if (o instanceof RootData<?, ?>) {
            RootData<K, V> rootData = (RootData<K, V>)o;
            Validator<K> keyValidator = rootData.keyValidator;
            if (keyValidator != null) {
                keyValidator.validate(key);
            }
        } else {
            ((AbstractBaseEntriesImpl<K, V>)o).validateKey(key);
        }
    }

    @SuppressWarnings("unchecked")
    protected Validator<V> valueValidator() {
        Object o = this.rootEntriesOrRootData;
        if (o instanceof RootData<?, ?>) {
            return ((RootData<K, V>)o).valueValidator;
        }
        return ((AbstractBaseEntriesImpl<K, V>)o).valueValidator();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void combineValueValidator(Validator<V> validator) {
        Object o = this.rootEntriesOrRootData;
        if (o instanceof RootData<?, ?>) {
            RootData<K, V> rootData = (RootData<K, V>)o;
            rootData.valueValidator =
                Validators.combine(
                        rootData.valueValidator, 
                        validator);
        } else {
            ((AbstractBaseEntriesImpl<K, V>)o).combineValueValidator(validator);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeValueValidator(Validator<V> validator) {
        Object o = this.rootEntriesOrRootData;
        if (o instanceof RootData<?, ?>) {
            RootData<K, V> rootData = (RootData<K, V>)o;
            rootData.valueValidator =
                Validators.combine(
                        rootData.valueValidator, 
                        validator);
        } else {
            ((AbstractBaseEntriesImpl<K, V>)o).removeValueValidator(validator);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void validateValue(V value) {
        Object o = this.rootEntriesOrRootData;
        if (o instanceof RootData<?, ?>) {
            RootData<K, V> rootData = (RootData<K, V>)o;
            Validator<V> valueValidator = rootData.valueValidator;
            if (valueValidator != null) {
                valueValidator.validate(value);
            }
        } else {
            ((AbstractBaseEntriesImpl<K, V>)o).validateValue(value);
        }
    }

    @SuppressWarnings("unchecked")
    protected final MapConflictVoterManager<K, V> voterManager() {
        Object o = this.rootEntriesOrRootData;
        if (o instanceof RootData<?, ?>) {
            return ((RootData<K, V>)o).voterManager;
        }
        return ((AbstractBaseEntriesImpl<K, V>)o).voterManager();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void combineConflictVoter(MapConflictVoter<K, V> voter) {
        Object o = this.rootEntriesOrRootData;
        if (o instanceof RootData<?, ?>) {
            RootData<K, V> rootData = (RootData<K, V>)o;
            rootData.voterManager =
                MapConflictVoterManager.combine(
                        rootData.voterManager, 
                        voter,
                        this.new MapSourceImpl());
        } else {
            ((AbstractBaseEntriesImpl<K, V>)o).combineConflictVoter(voter);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeConflictVoter(MapConflictVoter<K, V> voter) {
        Object o = this.rootEntriesOrRootData;
        if (o instanceof RootData<?, ?>) {
            RootData<K, V> rootData = (RootData<K, V>)o;
            rootData.voterManager =
                MapConflictVoterManager.remove(
                        rootData.voterManager, 
                        voter,
                        this.new MapSourceImpl());
        } else {
            ((AbstractBaseEntriesImpl<K, V>)o).removeConflictVoter(voter);
        }
    }

    protected abstract void deleteBaseEntry(BaseEntry<K, V> be);
    
    @SuppressWarnings("unchecked")
    protected final boolean keyEquals(K k1, K k2) {
        if (k1 == null) {
            return k2 == null;
        }
        Object valueComparatorOrEqualityComparator = 
            this.valueComparatorOrEqualityComparator();
        if (valueComparatorOrEqualityComparator instanceof Comparator<?>) {
            return ((Comparator<? super K>)valueComparatorOrEqualityComparator).compare(k1, k2) == 0;
        }
        if (valueComparatorOrEqualityComparator instanceof EqualityComparator<?>) {
            return ((EqualityComparator<? super K>)valueComparatorOrEqualityComparator).equals(k1, k2);
        }
        return k1.equals(k2);
    }
    
    @SuppressWarnings("unchecked")
    protected final boolean valueEquals(V v1, V v2) {
        if (v1 == null) {
            return v2 == null;
        }
        Object valueComparatorOrEqualityComparator = 
            this.valueComparatorOrEqualityComparator();
        if (valueComparatorOrEqualityComparator instanceof Comparator<?>) {
            return ((Comparator<? super V>)valueComparatorOrEqualityComparator).compare(v1, v2) == 0;
        }
        if (valueComparatorOrEqualityComparator instanceof EqualityComparator<?>) {
            return ((EqualityComparator<? super V>)valueComparatorOrEqualityComparator).equals(v1, v2);
        }
        return v1.equals(v2);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean containsEntry(Object o, ElementMatcher<? super Entry<K, V>> entryMatcher) {
        if (!(o instanceof Entry<?, ?>)) {
            return false;
        }
        Entry<K, V> e = (Entry<K, V>)o;
        K k = e.getKey();
        BaseEntry<K, V> be = this.getBaseEntry(k);
        if (be == null) {
            return false;
        }
        V v = e.getValue();
        V actualV = be.getValue();
        if (this.valueEquals(v, actualV)) {
            if (entryMatcher == null || entryMatcher.match(e, be)) {
                return true;
            }       
        } 
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean containsKey(Object k, ElementMatcher<? super K> keyMatcher) {
        BaseEntry<K, V> be = this.getBaseEntry(k);
        if (be != null) {
            if (k == null || keyMatcher == null || keyMatcher.match((K)k, be.getKey())) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean containsValue(Object v, ElementMatcher<? super V> valueMatcher) {
        BaseEntryIterator<K, V> iterator = this.iterator();
        while (iterator.hasNext()) {
            BaseEntry<K, V> e = iterator.next();
            V actualV = e.getValue();
            if (this.valueEquals((V)v, actualV)) { 
                if (valueMatcher == null || valueMatcher.match((V)v, actualV)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Ref<K> keyOf(V value, ElementMatcher<? super V> valueMatcher) {
        BaseEntryIterator<K, V> iterator = this.iterator();
        while (iterator.hasNext()) {
            BaseEntry<K, V> be = iterator.next();
            V actualV = be.getValue();
            if (this.valueEquals(value, actualV)) {
                if (valueMatcher == null || valueMatcher.match((V)value, actualV)) {
                    return new Ref<>(be.getKey());
                }
            }
        }
        return null;
    }

    @Override
    public final V put(K key, V value, BaseEntriesHandler<K, V> handler) {
        if (this.getRoot() instanceof TransientValueEntries && value != PRESENT) {
            Arguments.mustBeEqualToOtherWhen(
                    LAZY_RESOURCE.get().whenRootBaseEntriesIsInstanceof(TransientValueEntries.class),
                    "value", 
                    value, 
                    BaseEntries.class.getName() + ".PRESENT",
                    BaseEntries.PRESENT);
        }
        return this.put(key, value, this.triggerOf(handler), null);
    }
    
    @Override
    public final void putAll(
            Map<? extends K, ? extends V> m,
            BaseEntriesHandler<K, V> handler) {
        if (this.getRoot() instanceof TransientValueEntries) {
            throw new IllegalOperationException(
                    LAZY_RESOURCE.get().putAllIsNotSupportedForTransientValueEntries(TransientValueEntries.class)
            );
        }
        this.putAll(m, this.triggerOf(handler, m.size()));
    }

    @Override
    public final void clear(BaseEntriesHandler<K, V> handler) {
        this.clear(this.triggerOf(handler, this.size()));
    }

    @Override
    public final boolean addAll(
            Collection<? extends K> kc, 
            BaseEntriesHandler<K, V> handler) {
        if (!(this.getRoot() instanceof TransientValueEntries)) {
            throw new UnsupportedOperationException(
                    LAZY_RESOURCE.get().addAllIsOnlySupportedForTransientValueEntries(TransientValueEntries.class)
            );
        }
        return this.addAll(kc, this.triggerOf(handler, kc.size()));
    }

    @Override
    public final BaseEntry<K, V> removeByEntry(
            Object o, BaseEntriesHandler<K, V> handler) {
        return this.removeByEntry(o, this.triggerOf(handler));
    }
    
    @Override
    public final BaseEntry<K, V> removeByKey(
            Object o, BaseEntriesHandler<K, V> handler) {
        return this.removeByKey(o, this.triggerOf(handler));
    }

    @Override
    public final BaseEntry<K, V> removeByValue(
            Object o, BaseEntriesHandler<K, V> handler) {
        return this.removeByValue(o, this.triggerOf(handler));
    }

    @Override
    public final boolean removeAllByEntryCollection(
            Collection<?> ec, BaseEntriesHandler<K, V> handler) {
        return this.removeAllByEntryCollection(ec, this.triggerOf(handler));
    }

    @Override
    public final boolean removeAllByKeyCollection(
            Collection<?> kc, BaseEntriesHandler<K, V> handler) {
        return this.removeAllByKeyCollection(kc, this.triggerOf(handler));
    }

    @Override
    public final boolean removeAllByValueCollection(
            Collection<?> vc, BaseEntriesHandler<K, V> handler) {
        return this.removeAllByValueCollection(vc, this.triggerOf(handler));
    }

    @Override
    public final boolean retainAllByEntryCollection(
            Collection<?> ec, BaseEntriesHandler<K, V> handler) {
        return this.retainAllByEntryCollection(ec, this.triggerOf(handler));
    }
    
    @Override
    public final boolean retainAllByKeyCollection(
            Collection<?> kc, BaseEntriesHandler<K, V> handler) {
        return this.retainAllByKeyCollection(kc, this.triggerOf(handler));
    }

    @Override
    public final boolean retainAllByValueCollection(
            Collection<?> vc, BaseEntriesHandler<K, V> handler) {
        return this.retainAllByValueCollection(vc, this.triggerOf(handler));
    }

    @Override
    public final FrozenContextSuspending<K, V> suspendByKeyViaFrozenContext(K key, BaseEntriesHandler<K, V> handler) {
        BaseEntry<K, V> be = this.removeByKey(key, handler);
        if (be != null) {
            return this.createFrozenContextSuspending(be);
        }
        return null;
    }

    @Override
    public final void resumeViaFronzeContext(FrozenContextSuspending<K, V> suspending, BaseEntriesHandler<K, V> handler) {
        if (suspending != null) {
            try {
                this.put(suspending.getKey(), suspending.getValue(), this.triggerOf(handler), suspending);
            } finally {
                suspending.resume();
            }
        }
    }

    protected V put(
            K key, 
            V value, 
            Trigger<K, V> trigger,
            FrozenContextSuspending<K, V> suspending) {
        AttachProcessor<K, V> attachProcessor = this.attachProcessorOf(trigger, suspending);
        attachProcessor.intialize(key, value);
        V retval = this.putWithoutTriggerFlushing(key, value, attachProcessor);
        if (suspending != null) {
            this.onFrozenContextResumed(suspending);
        }
        attachProcessor.flush();
        return retval;
    }

    protected void putAll(
            Map<? extends K, ? extends V> m,
            Trigger<K, V> trigger) {
        if (m.isEmpty()) {
            return;
        }
        AttachProcessor<K, V> attachProcessor = this.attachProcessorOf(trigger, null);
        m = attachProcessor.intialize(m);
        if (trigger != null) {
            m = this.getMapWithSameUnifiedComparator(m);
        }
        this.putAllWithoutTriggerFlushing(m, attachProcessor);
        attachProcessor.flush();
    }

    protected boolean addAll(
            Collection<? extends K> kc, 
            Trigger<K, V> trigger) {
        if (!this.valueUnifiedComparator().isEmpty()) {
            throw new IllegalStateException(
                    LAZY_RESOURCE.get().addAllCanNotWorkWithValueUnifiedComparator(
                            Comparator.class, EqualityComparator.class
                    )
            );
        }
        if (this.voterManager() != null) {
            throw new IllegalStateException(
                    LAZY_RESOURCE.get().addAllCanNotWorkWithValueConflictVoter(MapConflictVoter.class)
            );
        }
        if (kc.isEmpty()) {
            return false;
        }
        Validator<K> keyValidator = 
            this.keyValidator();
        if (keyValidator != null) {
            for (K k : kc) {
                keyValidator.validate(k);
            }
        }
        if (trigger != null) {
            kc = this.getSetWithSameUnifiedComparator(kc);
        }
        boolean retval = this.addAllWithoutTriggerFlushing(kc, trigger);
        if (trigger != null) {
            trigger.flush();
        }
        return retval;
    }
    
    protected abstract void clear(Trigger<K, V> trigger);

    @SuppressWarnings("unchecked")
    protected BaseEntry<K, V> removeByEntry(Object o, Trigger<K, V> trigger) {
        if (!(o instanceof Entry<?, ?>)) {
            return null;
        }
        Entry<K, V> entry = (Entry<K, V>)o;
        BaseEntry<K, V> be = this.getBaseEntry(entry.getKey());
        if (be == null) {
            return null;
        }
        V expectedV = entry.getValue();
        V actualV = be.getValue();
        if (!this.valueEquals(expectedV, actualV)) {
            return null;
        }
        if (trigger != null) {
            trigger.preRemove(be);
            if (trigger.beginExecute()) {
                try {
                    this.deleteBaseEntry(be);
                    trigger.endExecute(null);
                } catch (RuntimeException | Error ex) {
                    trigger.endExecute(ex);
                }
            }
            trigger.flush();
        }
        else {
            this.deleteBaseEntry(be);
        }
        return be;
    }

    @SuppressWarnings("unchecked")
    protected BaseEntry<K, V> removeByKey(Object o, Trigger<K, V> trigger) {
        K key = (K)o;
        BaseEntry<K, V> be = this.getBaseEntry(key);
        if (be == null) {
            return null;
        }
        if (trigger != null) {
            trigger.preRemove(be);
            if (trigger.beginExecute()) {
                try {
                    this.deleteBaseEntry(be);
                    trigger.endExecute(null);
                } catch (RuntimeException | Error ex) {
                    trigger.endExecute(ex);
                }
            }
            trigger.flush();
        } else {
            this.deleteBaseEntry(be);
        }
        return be;
    }

    @SuppressWarnings("unchecked")
    protected BaseEntry<K, V> removeByValue(Object o, Trigger<K, V> trigger) {
        V value = (V)o;
        BaseEntryIterator<K, V> iterator = this.iterator();
        while (iterator.hasNext()) {
            BaseEntry<K, V> be = iterator.next();
            V expectedV = value;
            V actualV = be.getValue();
            if (this.valueEquals(expectedV, actualV)) {
                if (trigger != null) {
                    trigger.preRemove(be);
                    if (trigger.beginExecute()) {
                        try {
                            this.deleteBaseEntry(be);
                            trigger.endExecute(null);
                        } catch (RuntimeException | Error ex) {
                            trigger.endExecute(ex);
                        } 
                    }
                    trigger.flush();
                } else {
                    this.deleteBaseEntry(be);
                }
                return be;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected boolean removeAllByEntryCollection(Collection<?> ec, Trigger<K, V> trigger) {

        int ecSize = ec.size();
        if (ecSize == 0) {
            return false;
        }
        
        //Not same with JDK's implementation, subTree.size() is not slow.
        int size = this.size();
        OverriddenContainsBehavior entryOcb = OverriddenContainsBehavior.of(
                (Collection<? extends Entry<K, V>>)ec, this.entryUnifiedComparator());
        boolean isEcSetWithSameUnifiedComparator = ec instanceof Set<?> && !entryOcb.isOverridden();
        boolean calculateByThis = 
            isEcSetWithSameUnifiedComparator? 
                    size > ecSize : 
                    size * CAPACITY_FACTORY_WHEN_PARAMETER_IS_NOT_SET_WITH_SAME_UNIFIED_COMPARATOR > ecSize;
                    
        if (trigger == null) {
            boolean removed = false;
            if (calculateByThis) {
                for (Object o : ec) {
                    if (o instanceof Entry<?, ?>) {
                        Entry<K, V> e = (Entry<K, V>)o;
                        K k = e.getKey();
                        BaseEntry<K, V> be = this.getBaseEntry(k);
                        if (be != null) {
                            V expectedV = e.getValue();
                            V actualV = be.getValue();
                            if (this.valueEquals(expectedV, actualV)) {
                                this.deleteBaseEntry(be);
                                removed = true;
                            }
                        }
                    }
                }
            } else {
                BaseEntryIterator<K, V> iterator = this.iterator();
                while (iterator.hasNext()) {
                    BaseEntry<K, V> be = iterator.next();
                    if (entryOcb.contains(be))
                    if (ec.contains(be)) {
                        iterator.remove(null);
                        removed = true;
                    }
                }
            }
            return removed;
        }

        /*
         * In this implementation, method "deleteBaseEntry" can not broke any
         * other entries. so we can cache several BaseEntry<K, V> safely here.
         * after notify the prepositive event, I can delete them one by one.
         */
        if (calculateByThis) {
            Set<K> removedKeysWhenEcIsNotSetWithSameUnifiedComparator = null;
            if (!isEcSetWithSameUnifiedComparator) {
                UnifiedComparator<? super K> keyUnifiedComparator = this.keyUnifiedComparator();
                if (keyUnifiedComparator.comparator() != null) {
                    removedKeysWhenEcIsNotSetWithSameUnifiedComparator =
                        new TreeSet<K>(
                                ReplacementRule.OLD_REFERENCE_WIN,
                                keyUnifiedComparator.comparator());
                } else {
                    removedKeysWhenEcIsNotSetWithSameUnifiedComparator =
                        new HashSet<K>(
                                ReplacementRule.OLD_REFERENCE_WIN,
                                keyUnifiedComparator.equalityComparator(),
                                ec.size() + 1, 
                                1.0F);
                }
            }
            for (Object o : ec) {
                if (o instanceof Entry<?, ?>) {
                    Entry<K, V> e = (Entry<K, V>)o;
                    K k = e.getKey();
                    BaseEntry<K, V> be = this.getBaseEntry(k);
                    if (removedKeysWhenEcIsNotSetWithSameUnifiedComparator == null
                            || removedKeysWhenEcIsNotSetWithSameUnifiedComparator.add(k)) {
                        if (be != null) {
                            V expectedV = e.getValue();
                            V actualV = be.getValue();
                            if (this.valueEquals(expectedV, actualV)) {
                                trigger.preRemove(be);
                            }
                        }
                    }
                }
            }
        } else {
            BaseEntryIterator<K, V> iterator = this.iterator();
            while (iterator.hasNext()) {
                BaseEntry<K, V> be = iterator.next();
                if (entryOcb.contains(be)) {
                    trigger.preRemove(be);
                }
            }
        }
        if (trigger.beginExecute()) {
            try {
                int historyCount = trigger.getLength();
                History<K, V> history = trigger.getHistory(0);
                for (int i = 0; i < historyCount; i++) {
                    this.deleteBaseEntry(history.getBaseEntry(i));
                }
                trigger.endExecute(null);
            } catch (RuntimeException | Error ex) {
                trigger.endExecute(ex);
            }
        }
        return trigger.flush();
    }

    @SuppressWarnings("unchecked")
    protected boolean removeAllByKeyCollection(Collection<?> kc, Trigger<K, V> trigger) {
        
        int kcSize = kc.size();
        if (kcSize == 0) {
            return false;
        }
        
        //Not same with JDK's implementation, subTree.size() is not slow.
        int size = this.size();
        
        OverriddenContainsBehavior keyOcb =
            OverriddenContainsBehavior.of(
                    (Collection<? extends K>)kc, this.keyUnifiedComparator());
        boolean isKcSetWithSameUnifiedComparator = kc instanceof Set<?> && !keyOcb.isOverridden();
        boolean calculateByThis = 
                isKcSetWithSameUnifiedComparator? 
                        size > kcSize : 
                        size * CAPACITY_FACTORY_WHEN_PARAMETER_IS_NOT_SET_WITH_SAME_UNIFIED_COMPARATOR > kcSize;
        
        if (trigger == null) {
            boolean removed = false;
            if (calculateByThis) {
                for (Object o : kc) {
                    K k = (K)o;
                    BaseEntry<K, V> be = this.getBaseEntry(k);
                    if (be != null) {
                        this.deleteBaseEntry(be);
                        removed = true;
                    }
                }
            } else {
                BaseEntryIterator<K, V> iterator = this.iterator();
                while (iterator.hasNext()) {
                    BaseEntry<K, V> be = iterator.next();
                    if (keyOcb.contains(be.getKey())) {
                        iterator.remove(null);
                        removed = true;
                    }
                }
            }
            return removed;
        }

        /*
         * In this implementation, method "deleteBaseEntry" can not broke any
         * other entries. so we can cacheEntryModelImplEntryImpl<K, V> safely here.
         * after notify the prepositive event, I can delete them one by one.
         */
        if (calculateByThis) {
            Set<K> removedKeysWhenKcIsNotSetWithSameUnifiedComparator = null;
            UnifiedComparator<? super K> keyUnifiedComparator = this.keyUnifiedComparator();
            if (!isKcSetWithSameUnifiedComparator) {
                if (keyUnifiedComparator.comparator() != null) {
                    removedKeysWhenKcIsNotSetWithSameUnifiedComparator =
                        new TreeSet<K>(
                                ReplacementRule.OLD_REFERENCE_WIN,
                                keyUnifiedComparator.comparator());
                } else {
                    removedKeysWhenKcIsNotSetWithSameUnifiedComparator =
                        new HashSet<K>(
                                ReplacementRule.OLD_REFERENCE_WIN,
                                keyUnifiedComparator.equalityComparator(),
                                kc.size() + 1, 
                                1.0F);
                }
            }
            for (Object o : kc) {
                K k = (K)o;
                BaseEntry<K, V> be = this.getBaseEntry(k);
                if (removedKeysWhenKcIsNotSetWithSameUnifiedComparator == null
                        || removedKeysWhenKcIsNotSetWithSameUnifiedComparator.add(k)) {
                    if (be != null) {
                        trigger.preRemove(be);
                    }
                }
            }
        } else {
            BaseEntryIterator<K, V> iterator = this.iterator();
            while (iterator.hasNext()) {
                BaseEntry<K, V> be = iterator.next();
                if (keyOcb.contains(be.getKey())) {
                    trigger.preRemove(be);
                }
            }
        }
        if (trigger.beginExecute()) {
            try {
                int historyCount = trigger.getLength();
                History<K, V> history = trigger.getHistory(0);
                for (int i = 0; i < historyCount; i++) {
                    this.deleteBaseEntry(history.getBaseEntry(i));
                }
                trigger.endExecute(null);
            } catch (RuntimeException | Error ex) {
                trigger.endExecute(ex);
            }
        }
        return trigger.flush();
    }

    @SuppressWarnings("unchecked")
    protected boolean removeAllByValueCollection(Collection<?> vc, Trigger<K, V> trigger) {

        int vcSize = vc.size();

        if (vcSize == 0) {
            return false;
        }
        
        OverriddenContainsBehavior valueOcb = 
            OverriddenContainsBehavior.of(
                    (Collection<? extends V>)vc, this.valueUnifiedComparator());
        if (trigger == null) {
            boolean removed = false;
            BaseEntryIterator<K, V> iterator = this.iterator();
            while (iterator.hasNext()) {
                BaseEntry<K, V> be = iterator.next();
                if (valueOcb.contains(be.getValue())) {
                    iterator.remove(null);
                    removed = true;
                }
            }
            return removed;
        }

        /*
         * In this implementation, method "deleteBaseEntry" can not broke any
         * other entries. so we can cache several BaseEntry<K, V> safely here.
         * after notify the prepositive event, I can delete them one by one.
         */
        BaseEntryIterator<K, V> iterator = this.iterator();
        while (iterator.hasNext()) {
            BaseEntry<K, V> be = iterator.next();
            if (valueOcb.contains(be.getValue())) {
                trigger.preRemove(be);
            }
        }
        if (trigger.beginExecute()) {
            try {
                int historyCount = trigger.getLength();
                History<K, V> history = trigger.getHistory(0);
                for (int i = 0; i < historyCount; i++) {
                    this.deleteBaseEntry(history.getBaseEntry(i));
                }
                trigger.endExecute(null);
            } catch (RuntimeException | Error ex) {
                trigger.endExecute(ex);
            }
        }
        return trigger.flush();
    }

    @SuppressWarnings("unchecked")
    protected boolean retainAllByEntryCollection(Collection<?> ec, Trigger<K, V> trigger) {
        
        OverriddenContainsBehavior entryOcb = 
            OverriddenContainsBehavior.of(
                    (Collection<? extends Entry<K, V>>)ec, this.entryUnifiedComparator());
        if (trigger == null) {
            boolean removed = false;
            BaseEntryIterator<K, V> iterator = this.iterator();
            while (iterator.hasNext()) {
                BaseEntry<K, V> be = iterator.next();
                if (!entryOcb.contains(be)) {
                    iterator.remove(null);
                    removed = true;
                }
            }
            return removed;
        }

        /*
         * In this implementation, method "deleteBaseEntry" can not broke any
         * other entries. so we can cache several BaseEntry<K, V> safely here.
         * after notify the prepositive event, I can delete them one by one.
         */
        BaseEntryIterator<K, V> iterator = this.iterator();
        while (iterator.hasNext()) {
            BaseEntry<K, V> be = iterator.next();
            if (!entryOcb.contains(be)) {
                trigger.preRemove(be);
            }
        }
        if (trigger.beginExecute()) {
            try {
                int historyCount = trigger.getLength();
                History<K, V> history = trigger.getHistory(0);
                for (int i = 0; i < historyCount; i++) {
                    this.deleteBaseEntry(history.getBaseEntry(i));
                }
                trigger.endExecute(null);
            } catch (RuntimeException | Error ex) {
                trigger.endExecute(ex);
            }
        }
        return trigger.flush();
    }

    @SuppressWarnings("unchecked")
    protected boolean retainAllByKeyCollection(Collection<?> kc, Trigger<K, V> trigger) {
        
        OverriddenContainsBehavior keyOcb = 
            OverriddenContainsBehavior.of(
                    (Collection<? extends K>)kc, this.keyUnifiedComparator());
        if (trigger == null) {
            boolean removed = false;
            BaseEntryIterator<K, V> iterator = this.iterator();
            while (iterator.hasNext()) {
                BaseEntry<K, V> be = iterator.next();
                if (!keyOcb.contains(be.getKey())) {
                    iterator.remove(null);
                    removed = true;
                }
            }
            return removed;
        }

        /*
         * In this implementation, method "deleteBaseEntry" can not broke any
         * other entries. so we can cache several BaseEntry<K, V> safely here.
         * after notify the prepositive event, I can delete them one by one.
         */
        BaseEntryIterator<K, V> iterator = this.iterator();
        while (iterator.hasNext()) {
            BaseEntry<K, V> be = iterator.next();
            if (!keyOcb.contains(be.getKey())) {
                trigger.preRemove(be);
            }
        }
        if (trigger.beginExecute()) {
            try {
                int historyCount = trigger.getLength();
                History<K, V> history = trigger.getHistory(0);
                for (int i = 0; i < historyCount; i++) {
                    this.deleteBaseEntry(history.getBaseEntry(i));
                }
                trigger.endExecute(null);
            } catch (RuntimeException | Error ex) {
                trigger.endExecute(ex);
            }
        }
        return trigger.flush();
    }

    @SuppressWarnings("unchecked")
    protected boolean retainAllByValueCollection(Collection<?> vc, Trigger<K, V> trigger) {
        
        OverriddenContainsBehavior valueOcb = 
            OverriddenContainsBehavior.of(
                    (Collection<? extends V>)vc, this.valueUnifiedComparator());
        if (trigger == null) {
            boolean removed = false;
            BaseEntryIterator<K, V> iterator = this.iterator();
            while (iterator.hasNext()) {
                BaseEntry<K, V> be = iterator.next();
                if (!valueOcb.contains(be.getValue())) {
                    iterator.remove(null);
                    removed = true;
                }
            }
            return removed;
        }

        /*
         * In this implementation, method "deleteBaseEntry" can not broke any
         * other entries. so we can cache several BaseEntry<K, V> safely here.
         * after notify the prepositive event, I can delete them one by one.
         */
        BaseEntryIterator<K, V> iterator = this.iterator();
        while (iterator.hasNext()) {
            BaseEntry<K, V> be = iterator.next();
            if (!valueOcb.contains(be.getValue())) {
                trigger.preRemove(be);
            }
        }
        if (trigger.beginExecute()) {
            try {
                int historyCount = trigger.getLength();
                History<K, V> history = trigger.getHistory(0);
                for (int i = 0; i < historyCount; i++) {
                    this.deleteBaseEntry(history.getBaseEntry(i));
                }
                trigger.endExecute(null);
            } catch (RuntimeException | Error ex) {
                trigger.endExecute(ex);
            }
        }
        return trigger.flush();
    }
    
    protected FrozenContextSuspending<K, V> createFrozenContextSuspending(BaseEntry<K, V> be) {
        return new FrozenContextSuspending<K, V>(be);
    }
    
    protected void onFrozenContextResumed(FrozenContextSuspending<K, V> suspending) {
        
    }
    
    protected V putWithoutTriggerFlushing(
            K key, 
            V value, 
            AttachProcessor<K, V> attachProcessor) {
        throw new UnsupportedOperationException(LAZY_RESOURCE.get().pleaseOverrideTheMethod(
                "putWithoutTriggerFlushing", Object.class, Object.class, AttachProcessor.class)
        );
    }

    protected void putAllWithoutTriggerFlushing(Map<? extends K, ? extends V> m, AttachProcessor<K, V> attachProcessor) {
        throw new UnsupportedOperationException(LAZY_RESOURCE.get().pleaseOverrideTheMethod(
                "putAllWithoutTriggerFlushing", Map.class, Object.class, AttachProcessor.class)
        );
    }

    protected boolean addAllWithoutTriggerFlushing(
            Collection<? extends K> kc, 
            Trigger<K, V> trigger) {
        throw new UnsupportedOperationException(LAZY_RESOURCE.get().pleaseOverrideTheMethod(
                "addAllWithoutTriggerFlushing", Collection.class, Trigger.class)
        );
    }
    
    @Override
    public final boolean isFrozen() {
        Object o = this.rootEntriesOrRootData;
        if (o instanceof RootData<?, ?>) {
            return ((RootData<?, ?>)o).frozenCount != 0;
        }
        return ((AbstractBaseEntriesImpl<?, ?>)o).isFrozen();
    }
    
    protected Map<? extends K, ? extends V> getMapWithSameUnifiedComparator(Map<? extends K, ? extends V> m) {
        UnifiedComparator<? super K> keyUnifiedComparator = this.keyUnifiedComparator();
        boolean recreateM = false;
        try {
            if (!UnifiedComparator.nullToEmpty(
                    UnifiedComparator.of(m.keySet(), true))
                .equals(this.keyUnifiedComparator())) {
                recreateM = true;
            }
        } catch (UnknownUnifiedComparatorException ex) {
            recreateM = true;
        }
        if (recreateM) {
            Map<K, V> newM = 
                this instanceof NavigableBaseEntries<?, ?> ?
                new TreeMap<K, V>(
                        this.keyReplacementRule(), 
                        keyUnifiedComparator.comparator()) :
                new LinkedHashMap<K, V>(
                        this.keyReplacementRule(),
                        keyUnifiedComparator.equalityComparator());
            newM.putAll(m);
            m = newM;
        }
        return m;
    }
    
    protected Collection<? extends K> getSetWithSameUnifiedComparator(Collection<? extends K> c) {
        UnifiedComparator<? super K> keyUnifiedComparator = this.keyUnifiedComparator();
        boolean recreateC;
        try {
            recreateC = 
                !(c instanceof Set<?>) || 
                !keyUnifiedComparator.equals(UnifiedComparator.of(c, true));
        } catch (UnknownUnifiedComparatorException ex) {
            recreateC = true;
        }
        if (recreateC) {
            c =
                this instanceof NavigableBaseEntries<?, ?> ?
                new TreeSet<K>(
                        this.keyReplacementRule(), 
                        keyUnifiedComparator.comparator(), 
                        c) :
                new LinkedHashSet<K>(
                        this.keyReplacementRule(),
                        keyUnifiedComparator.equalityComparator(),
                        10,
                        null, 
                        false,
                        OrderAdjustMode.NONE,
                        OrderAdjustMode.NONE,
                        c);
        }
        return c;
    }
    
    protected final AttachProcessor<K, V> attachProcessorOf(Trigger<K, V> trigger, FrozenContextSuspending<K, V> suspending) {
        return this.new AttachProcessorImpl(trigger, suspending);
    }
    
    protected static <K, V> AttachProcessor<K, V> emptyAttachProcessor() {
        return new AttachProcessor<K, V>() {
            
            private int state = ATTACH_PROCESSOR_NEW;
            
            @Override
            public Trigger<K, V> getTrigger() {
                return null;
            }

            @Override
            public FrozenContextSuspending<K, V> getSuspending() {
                return null;
            }

            @Override
            public void intialize(K key, V value) {
                if (this.state != ATTACH_PROCESSOR_NEW) {
                    throw new IllegalStateException(
                            LAZY_RESOURCE.get().invalidOperationOnAttachProcessor(
                                    "initialize",
                                    attachProcessorState(ATTACH_PROCESSOR_NEW),
                                    attachProcessorState(this.state)
                            )
                    );
                }
                this.state = ATTACH_PROCESSOR_INITIALIZED;
            }

            @Override
            public Map<? extends K, ? extends V> intialize(Map<? extends K, ? extends V> m) {
                if (this.state != ATTACH_PROCESSOR_NEW) {
                    throw new IllegalStateException(
                            LAZY_RESOURCE.get().invalidOperationOnAttachProcessor(
                                    "initialize",
                                    attachProcessorState(ATTACH_PROCESSOR_NEW),
                                    attachProcessorState(this.state)
                            )
                    );
                }
                Arguments.mustNotBeNull("m", m);
                this.state = ATTACH_PROCESSOR_INITIALIZED;
                return m;
            }
            
            @Override
            public boolean beginExcute() {
                /*
                 * Not very strict for empty AttachProcessor.
                 */
                if (this.state >= ATTACH_PROCESSOR_EXECUTING) {
                    throw new IllegalStateException(
                            LAZY_RESOURCE.get().invalidOperationOnAttachProcessor(
                                    "beginExecute",
                                    attachProcessorState(ATTACH_PROCESSOR_NEW) +
                                    LAZY_RESOURCE.get().orBetweenWordsWithQuotes() +
                                    attachProcessorState(ATTACH_PROCESSOR_INITIALIZED),
                                    attachProcessorState(this.state)
                            )
                    );
                }
                this.state = ATTACH_PROCESSOR_EXECUTING;
                return true;
            }

            @Override
            public void endExecute(Throwable throwable) {
                if (this.state != ATTACH_PROCESSOR_EXECUTING) {
                    throw new IllegalStateException(
                            LAZY_RESOURCE.get().invalidOperationOnAttachProcessor(
                                    "endExcute",
                                    attachProcessorState(ATTACH_PROCESSOR_EXECUTING),
                                    attachProcessorState(this.state)
                            )
                    );
                }
                this.state = ATTACH_PROCESSOR_EXECUTED;
            }

            @Override
            public void flush() {
                if (this.state != ATTACH_PROCESSOR_EXECUTED) {
                    throw new IllegalStateException(
                            LAZY_RESOURCE.get().invalidOperationOnAttachProcessor(
                                    "flush",
                                    attachProcessorState(ATTACH_PROCESSOR_EXECUTED),
                                    attachProcessorState(this.state)
                            )
                    );
                }
                this.state = ATTACH_PROCESSOR_FLUSHED;
            }

            @Override
            public boolean isNew() {
                return this.state == ATTACH_PROCESSOR_NEW;
            }
            
            @Override
            public boolean isInitialized() {
                return this.state == ATTACH_PROCESSOR_INITIALIZED;
            }
            
            @Override
            public boolean isResolved() {
                return this.state == ATTACH_PROCESSOR_RESOLVED;
            }

            @Override
            public boolean isExecuted() {
                return this.state == ATTACH_PROCESSOR_EXECUTED;
            }
            
            @Override
            public boolean isFlushed() {
                return this.state == ATTACH_PROCESSOR_FLUSHED;
            }
        };
    }

    protected final Trigger<K, V> triggerOf(BaseEntriesHandler<K, V> handler) {
        return this.triggerOf(handler, 0);
    }
    
    protected final Trigger<K, V> triggerOf(BaseEntriesHandler<K, V> handler, int capacity) {
        if (this.isFrozen()) {
            throw new IllegalStateException(
                    LAZY_RESOURCE.get().canNotCreateTriggerBecauseCurrentObjectIsFrozen(Trigger.class)
            );
        }
        MapConflictVoterManager<K, V> voterManager = this.voterManager();
        if (MapConflictVoterManager.isModificationAware(voterManager) || handler != null) {
            return this.new TriggerImpl(handler, capacity);
        }
        return null;
    }
    
    protected final void freeze() {
        Object o = this.rootEntriesOrRootData;
        if (o instanceof RootData<?, ?>) {
            ((RootData<?, ?>)o).frozenCount++;
        } else {
            ((AbstractBaseEntriesImpl<?, ?>)o).freeze();
        }
    }
    
    protected final void unfreeze() {
        Object o = this.rootEntriesOrRootData;
        if (o instanceof RootData<?, ?>) {
            RootData<?, ?> rootData = (RootData<?, ?>)o;
            if (rootData.frozenCount == 0) {
                throw new IllegalStateException(
                        LAZY_RESOURCE.get().canNotUnfreezeCurrentObjectBecauseItIsNotFrozen()
                );
            }
            rootData.frozenCount--;
        } else {
            ((AbstractBaseEntriesImpl<?, ?>)o).unfreeze();
        }
    }
    
    final void write(ObjectOutputStream out) throws IOException {
        if (!(this.rootEntriesOrRootData instanceof RootData<?, ?>)) {
            throw new UnsupportedOperationException(LAZY_RESOURCE.get().onlyRootBaseEntriesCanBeSerialized());
        }
        out.writeObject(this.rootEntriesOrRootData);
    }
    
    final void read(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.rootEntriesOrRootData = in.readObject();
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
    
    protected interface Trigger<K, V> {
        
        void preAdd(K key, V value);
        
        void preChange(BaseEntry<K, V> be, K newKey, V newValue);

        void preRemove(BaseEntry<K, V> be);
        
        boolean beginExecute();
        
        void endExecute(Throwable throwable);
        
        boolean flush();
        
        boolean flush(int limit);
        
        int getLength();
        
        int getFlushedLength();
        
        History<K, V> getHistory(int offset);
        
        interface History<K, V> {
            
            int getOffset();
            
            ModificationType getModificationType(int index);
            
            BaseEntry<K, V> getBaseEntry(int index);
            
            K getOldKey(int index);
            
            V getOldValue(int index);
            
            K getNewKey(int index);
            
            V getNewValue(int index); 
            
        }
    }
    
    protected interface AttachProcessor<K, V> {
        
        Trigger<K, V> getTrigger();
        
        FrozenContextSuspending<K, V> getSuspending();
    
        void intialize(K key, V value);
        
        Map<? extends K, ? extends V> intialize(Map<? extends K, ? extends V> m);
        
        boolean beginExcute();
        
        void endExecute(Throwable throwable);
        
        void flush();
        
        boolean isNew();
        
        boolean isInitialized();
        
        boolean isResolved();
        
        boolean isExecuted();
    
        boolean isFlushed();
        
    }
    
    protected static abstract class AbstractBaseEntryImpl<K, V> implements BaseEntry<K, V> {
        
        @Deprecated
        @Override
        public final V setValue(V value) throws UnsupportedOperationException {
            if (this.getOwner() != null) {
                throw new UnsupportedOperationException();
            }
            // Dead BaseEntry can be used as normal Entry.
            V oldValue = this.getValue();
            this.setRawValue(value);
            return oldValue;
        }
        
        @Override
        public V setValue(V value, BaseEntriesHandler<K, V> handler) {
            V oldV = this.getValue();
            AbstractBaseEntriesImpl<K, V> owner = (AbstractBaseEntriesImpl<K, V>)this.getOwner();
            if (owner != null) {
                Trigger<K, V> trigger = 
                    ((AbstractBaseEntriesImpl<K, V>)this.getOwner()).triggerOf(handler);
                AttachProcessor<K, V> attachProcessor = owner.attachProcessorOf(trigger, null);
                attachProcessor.intialize(this.getKey(), value);
                if (trigger != null) {
                    trigger.preChange(this, this.getKey(), value);
                }
                if (attachProcessor.beginExcute()) {
                    try {
                        this.setRawValue(value);
                        attachProcessor.endExecute(null);
                    } catch (RuntimeException | Error ex) {
                        attachProcessor.endExecute(ex);
                    }
                }
                attachProcessor.flush();
            } else {
                this.setRawValue(value);
            }
            return oldV;
        }

        @Override
        public int hashCode() {
            UnifiedComparator<? super Entry<K, V>> unifiedComparator = 
                    UnifiedComparator.emptyToNull(this.unifiedComparator());
            if (unifiedComparator != null) {
                return unifiedComparator.hashCode(this);
            }
            K k = this.getKey();
            V v = this.getValue();
            return 
                (k == null ? 0 : k.hashCode()) ^ 
                (v == null ? 0 : v.hashCode());
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Entry<?, ?>)) {
                return false;
            }
            Entry<K, V> other = (Entry<K, V>)obj;
            UnifiedComparator<? super Entry<K, V>> unifiedComparator = 
                    UnifiedComparator.emptyToNull(this.unifiedComparator());
            if (unifiedComparator != null) {
                return unifiedComparator.equals(this, other);
            }
            K k1 = this.getKey();
            V v1 = this.getValue();
            Object k2 = other.getKey();
            Object v2 = other.getValue();
            return 
                (k1 == null ? k2 == null : k1.equals(k2)) &&
                (v1 == null ? v2 == null : v1.equals(v2));
        }
        
        @Override
        public String toString() {
            return this.getKey() + "=" + this.getValue();
        }
        
        protected abstract UnifiedComparator<? super Entry<K, V>> unifiedComparator();
        
        protected abstract void setRawValue(V value);
    }

    private class TriggerImpl implements Trigger<K, V> {
        
        private BaseEntriesHandler<K, V> handler;
        
        private Object[] arr;
        
        private int len;
        
        private int flushedLen;
        
        private int elementItemCount;
        
        private History<K, V> baseHistory;
        
        private Throwable finalThrowable;
        
        private int state = TRIGGER_NEW;
        
        private TriggerImpl(BaseEntriesHandler<K, V> handler, int capacity) {
            MapConflictVoterManager<K, V> voterManager = AbstractBaseEntriesImpl.this.voterManager();
            boolean notifyVoterManager = MapConflictVoterManager.isModificationAware(voterManager);
            if (!notifyVoterManager && handler == null) {
                throw new IllegalOperationException(
                        LAZY_RESOURCE.get().uncessaryTrigger(MAMapConflictVoter.class, BaseEntriesHandler.class)
                );
            }
            this.handler = handler;
            this.elementItemCount = this.handler == null ? 5 : 6;
            this.baseHistory = this.new HistoryImpl();
            if (capacity > 0) {
                this.arr = new Object[this.elementItemCount * capacity];
            }
        }
        
        @Override
        public void preAdd(K key, V value) {
            if (this.state != TRIGGER_NEW) {
                throw new IllegalStateException(
                        LAZY_RESOURCE.get().invalidOperationOnTrigger(
                                "preAdd", 
                                triggerState(TRIGGER_NEW), 
                                triggerState(this.state)
                        )
                );
            }
            BaseEntriesHandler<K, V> handler = this.handler;
            int len = this.len;
            this.expand();
            Object[] arr = this.arr;
            int elementItemCount = this.elementItemCount;
            int offset = len * elementItemCount;
            arr[offset + 1] = TRIGGER_NIL_KEY;
            arr[offset + 3] = key;
            arr[offset + 4] = value;
            if (handler != null) {
                AbstractBaseEntriesImpl<K, V> owner = AbstractBaseEntriesImpl.this;
                owner.freeze();
                try {
                    Object argument = handler.createAddingArgument(key, value);
                    if (argument != null) {
                        try {
                            handler.adding(key, value, argument);
                        } catch (RuntimeException | Error ex) {
                            if (this.finalThrowable == null) {
                                this.finalThrowable = ex;
                            }
                            handler.setPreThrowable(argument, ex);
                        }
                        arr[offset + 5] = argument;
                    }
                } finally {
                    owner.unfreeze();
                }
            }
            this.len = len + 1;
        }
        
        @Override
        public void preChange(BaseEntry<K, V> be, K newKey, V newValue) {
            if (this.state != TRIGGER_NEW) {
                throw new IllegalStateException(
                        LAZY_RESOURCE.get().invalidOperationOnTrigger(
                                "preChange", 
                                triggerState(TRIGGER_NEW), 
                                triggerState(this.state)
                        )
                );
            }
            BaseEntriesHandler<K, V> handler = this.handler;
            int len = this.len;
            this.expand();
            Object[] arr = this.arr;
            int elementItemCount = this.elementItemCount;
            int offset = len * elementItemCount;
            arr[offset] = be;
            arr[offset + 1] = be.getKey();
            arr[offset + 2] = be.getValue();
            arr[offset + 3] = newKey;
            arr[offset + 4] = newValue;
            if (handler != null) {
                AbstractBaseEntriesImpl<K, V> owner = AbstractBaseEntriesImpl.this;
                owner.freeze();
                try {
                    Object argument = handler.createChangingArgument(be.getKey(), be.getValue(), newKey, newValue);
                    if (argument != null) {
                        try {
                            handler.changing(be.getKey(), be.getValue(), newKey, newValue, argument);
                        } catch (RuntimeException | Error ex) {
                            if (this.finalThrowable == null) {
                                this.finalThrowable = ex;
                            }
                            handler.setPreThrowable(argument, ex);
                        }
                        arr[offset + 5] = argument;
                    }
                } finally {
                    owner.unfreeze();
                }
            }
            this.len = len + 1;
        }

        @Override
        public void preRemove(BaseEntry<K, V> be) {
            if (this.state != TRIGGER_NEW) {
                throw new IllegalStateException(
                        LAZY_RESOURCE.get().invalidOperationOnTrigger(
                                "preRemove", 
                                triggerState(TRIGGER_NEW), 
                                triggerState(this.state)
                        )
                );
            }
            BaseEntriesHandler<K, V> handler = this.handler;
            int len = this.len;
            this.expand();
            Object[] arr = this.arr;
            int elementItemCount = this.elementItemCount;
            int offset = len * elementItemCount;
            arr[offset] = be;
            arr[offset + 1] = be.getKey();
            arr[offset + 2] = be.getValue();
            arr[offset + 3] = TRIGGER_NIL_KEY;
            if (handler != null) {
                AbstractBaseEntriesImpl<K, V> owner = AbstractBaseEntriesImpl.this;
                owner.freeze();
                try {
                    Object argument = handler.createRemovingArgument(be.getKey(), be.getValue());
                    if (argument != null) {
                        try {
                            handler.removing(be.getKey(), be.getValue(), argument);
                        } catch (RuntimeException | Error ex) {
                            if (this.finalThrowable == null) {
                                this.finalThrowable = ex;
                            }
                            handler.setPreThrowable(argument, ex);
                        }
                        arr[offset + 5] = argument;
                    }
                } finally {
                    owner.unfreeze();
                }
            }
            this.len = len + 1;
        }
        
        @Override
        public boolean beginExecute() {
            if (this.state != TRIGGER_NEW) {
                throw new IllegalStateException(
                        LAZY_RESOURCE.get().invalidOperationOnTrigger(
                                "beforeExecute", 
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
                                "endExecute", 
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
            if (this.handler != null) {
                this.handler.setNullOrThrowable(nullOrThrowable);
            }
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
            AbstractBaseEntriesImpl<K, V> owner = AbstractBaseEntriesImpl.this;
            int len = this.len;
            limit = Math.min(limit, len);
            int flushedLen = this.flushedLen;
            boolean retval = false;
            if (len != 0 && limit > flushedLen) {
                MapConflictVoterManager<K, V> voterManager = owner.voterManager();
                BaseEntriesHandler<K, V> handler = this.handler;
                Object[] arr = this.arr;
                int elementItemCount = this.elementItemCount;
                owner.freeze();
                try {
                    if (MapConflictVoterManager.isModificationAware(voterManager)) {
                        for (int i = flushedLen; i < limit; i++) {
                            int offset = i * elementItemCount;
                            K oldKey = (K)arr[offset + 1];
                            V oldValue = (V)arr[offset + 2];
                            K key = (K)arr[offset + 3];
                            V value = (V)arr[offset + 4];
                            if (oldKey == TRIGGER_NIL_KEY) {
                                voterManager.added(key, value);
                            } else if (key == TRIGGER_NIL_KEY) {
                                voterManager.removed(oldKey, oldValue);
                            } else {
                                voterManager.changed(oldKey, oldValue, key, value);
                            }
                        }
                    }
                    if (handler != null) {
                        for (int i = flushedLen; i < limit; i++) {
                            int offset = i * elementItemCount;
                            BaseEntry<K, V> be = (BaseEntry<K, V>)arr[offset];
                            K oldKey = (K)arr[offset + 1];
                            V oldValue = (V)arr[offset + 2];
                            K key = (K)arr[offset + 3];
                            V value = (V)arr[offset + 4];
                            try {
                                Object argument = arr[offset + 5];
                                if (be == null) {
                                    handler.added(key, value, argument);
                                } else if (key == TRIGGER_NIL_KEY) {
                                    handler.removed(oldKey, oldValue, argument);
                                } else {
                                    handler.changed(oldKey, oldValue, key, value, argument);
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
        public History<K, V> getHistory(int offset) {
            if (offset == 0) {
                return this.baseHistory;
            }
            return new OffsetHistoryImpl(offset);
        }
        
        private void expand() {
            int elementItemCount = this.elementItemCount;
            int offset = len * elementItemCount;
            Object[] arr = this.arr;
            if (arr == null) {
                this.arr = new Object[elementItemCount];
            } else if (offset == arr.length) {
                Object[] newArr = new Object[arr.length << 1];
                System.arraycopy(arr, 0, newArr, 0, arr.length);
                this.arr = newArr;
            }
        }

        private class HistoryImpl implements History<K, V> {
            
            @Override
            public int getOffset() {
                return 0;
            }
            
            @Override
            public ModificationType getModificationType(int index) {
                TriggerImpl owner = TriggerImpl.this;
                int offset = index * owner.elementItemCount;
                Object[] arr = owner.arr;
                if (arr[offset + 1] == TRIGGER_NIL_KEY) {
                    return ModificationType.ATTACH;
                }
                if (arr[offset + 3] == TRIGGER_NIL_KEY) {
                    return ModificationType.DETACH;
                }
                return ModificationType.REPLACE;
            }

            @SuppressWarnings("unchecked")
            @Override
            public BaseEntry<K, V> getBaseEntry(int index) {
                return (BaseEntry<K, V>)TriggerImpl.this.arr[index * TriggerImpl.this.elementItemCount];
            }
            
            @SuppressWarnings("unchecked")
            @Override
            public K getOldKey(int index) {
                K k = (K)TriggerImpl.this.arr[index * TriggerImpl.this.elementItemCount + 1];
                return k == TRIGGER_NIL_KEY ? null : k;
            }
            
            @SuppressWarnings("unchecked")
            @Override
            public V getOldValue(int index) {
                int offset = index * TriggerImpl.this.elementItemCount;
                Object[] arr = TriggerImpl.this.arr;
                K k = (K)arr[offset + 1];
                if (k == TRIGGER_NIL_KEY) {
                    return null;
                }
                return (V)arr[offset + 2];
            }
            
            @SuppressWarnings("unchecked")
            @Override
            public K getNewKey(int index) {
                K k = (K)TriggerImpl.this.arr[index * TriggerImpl.this.elementItemCount + 3];
                return k == TRIGGER_NIL_KEY ? null : k;
            }
            
            @SuppressWarnings("unchecked")
            @Override
            public V getNewValue(int index) {
                int offset = index * TriggerImpl.this.elementItemCount;
                Object[] arr = TriggerImpl.this.arr;
                K k = (K)arr[offset + 3];
                if (k == TRIGGER_NIL_KEY) {
                    return null;
                }
                return (V)arr[offset + 4];
            }
    
        }
        
        private class OffsetHistoryImpl extends HistoryImpl {
            
            private int offset;
            
            public OffsetHistoryImpl(int offset) {
                this.offset = offset;
            }

            @Override
            public int getOffset() {
                return this.offset;
            }

            @Override
            public BaseEntry<K, V> getBaseEntry(int index) {
                index += this.offset;
                return super.getBaseEntry(index);
            }

            @Override
            public K getOldKey(int index) {
                index += this.offset;
                return super.getOldKey(index);
            }

            @Override
            public V getOldValue(int index) {
                index += this.offset;
                return super.getOldValue(index);
            }

            @Override
            public K getNewKey(int index) {
                index += this.offset;
                return super.getNewKey(index);
            }

            @Override
            public V getNewValue(int index) {
                index += this.offset;
                return super.getNewValue(index);
            }
            
        }
    }
    
    private class AttachProcessorImpl implements AttachProcessor<K, V> {
        
        private Trigger<K, V> trigger;
        
        private FrozenContextSuspending<K, V> suspending;
        
        private BaseEntry<K, V>[] conflictEntries;
        
        private int state = ATTACH_PROCESSOR_NEW;
        
        private AttachProcessorImpl(Trigger<K, V> trigger, FrozenContextSuspending<K, V> suspending) {
            this.trigger = trigger;
            this.suspending = suspending;
        }
        
        @Override
        public Trigger<K, V> getTrigger() {
            if (this.trigger == null) {
                return null;
            }
            return new Trigger<K, V>() {
        
                @Override
                public void preAdd(K key, V value) {
                    AttachProcessorImpl owner = AttachProcessorImpl.this;
                    owner.beforeAddChangeExecute();
                    owner.trigger.preAdd(key, value);
                }
        
                @Override
                public void preChange(BaseEntry<K, V> be, K newKey, V newValue) {
                    AttachProcessorImpl owner = AttachProcessorImpl.this;
                    owner.beforeAddChangeExecute();
                    owner.trigger.preChange(be, newKey, newValue);
                }
        
                @Override
                public void preRemove(BaseEntry<K, V> be) {
                    AttachProcessorImpl owner = AttachProcessorImpl.this;
                    owner.beforeRemove();
                    owner.trigger.preRemove(be);
                }
        
                @Override
                public boolean beginExecute() {
                    throw new UnsupportedOperationException(
                            "This trigger is belong to the AttachProcessor, please invoke AttachProcessor.beginExecute");
                }

                @Override
                public void endExecute(Throwable throwable) {
                    throw new UnsupportedOperationException(
                            "This trigger is belong to the AttachProcessor, please invoke AttachProcessor.endExecute");
                }

                @Override
                public boolean flush() {
                    throw new UnsupportedOperationException(
                            "This trigger is belong to the AttachProcessor, please invoke AttachProcessor.flush");
                }
                
                @Override
                public boolean flush(int limit) {
                    throw new UnsupportedOperationException(
                        "This trigger is belong to the AttachProcessor, please invoke AttachProcessor.flush");
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
                public History<K, V> getHistory(int offset) {
                    return AttachProcessorImpl.this.trigger.getHistory(offset);
                }
            };
        }

        @Override
        public FrozenContextSuspending<K, V> getSuspending() {
            return this.suspending;
        }

        @Override
        public void intialize(K key, V value) {
            if (this.state != ATTACH_PROCESSOR_NEW) {
                throw new IllegalStateException(
                        LAZY_RESOURCE.get().invalidOperationOnAttachProcessor(
                                "initialize",
                                attachProcessorState(ATTACH_PROCESSOR_NEW),
                                attachProcessorState(this.state)
                        )
                );
            }
            AbstractBaseEntriesImpl<K, V> owner = AbstractBaseEntriesImpl.this;
            Validator<K> keyValidator = owner.keyValidator();
            Validator<V> valueValidator = owner.valueValidator();
            if (keyValidator != null) {
                keyValidator.validate(key);
            }
            if (valueValidator != null) {
                valueValidator.validate(value);
            }
            MapConflictVoterManager<K, V> voterManager = owner.voterManager();
            if (voterManager != null) {
                Set<K> conflictKeys = voterManager.vote(key, value);
                this.intialize0(conflictKeys);
            }
            this.state = ATTACH_PROCESSOR_INITIALIZED;
        }
        
        @Override
        public Map<? extends K, ? extends V> intialize(Map<? extends K, ? extends V> m) {
            if (this.state != ATTACH_PROCESSOR_NEW) {
                throw new IllegalStateException(
                        LAZY_RESOURCE.get().invalidOperationOnAttachProcessor(
                                "initialize",
                                attachProcessorState(ATTACH_PROCESSOR_NEW),
                                attachProcessorState(this.state)
                        )
                );
            }
            Arguments.mustNotBeNull("m", m);
            AbstractBaseEntriesImpl<K, V> owner = AbstractBaseEntriesImpl.this;
            Validator<K> keyValidator = owner.keyValidator();
            Validator<V> valueValidator = owner.valueValidator();
            if (keyValidator != null || valueValidator != null) {
                for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
                    if (keyValidator != null) {
                        keyValidator.validate(entry.getKey());
                    }
                    if (valueValidator != null) {
                        valueValidator.validate(entry.getValue());
                    }
                }
            }
            MapConflictVoterManager<K, V> voterManager = owner.voterManager();
            if (voterManager != null) {
                Ref<Map<? extends K, ? extends V>> mRef = 
                    new Ref<Map<? extends K, ? extends V>>(m);
                Set<K> conflictKeys = voterManager.voteAll(mRef);
                this.intialize0(conflictKeys);
                m = mRef.get();
            }
            this.state = ATTACH_PROCESSOR_INITIALIZED;
            return m;
        }
        
        @Override
        public boolean beginExcute() {
            this.beforeAddChangeExecute();
            Trigger<K, V> trigger = this.trigger;
            FrozenContextSuspending<K, V> suspending = this.suspending;
            if (trigger != null && !trigger.beginExecute()) {
                this.state = ATTACH_PROCESSOR_EXECUTED;
                return false;
            }
            if (conflictEntries != null && conflictEntries.length != 0) {
                BaseEntry<K, V>[] conflictEntries = this.conflictEntries;
                AbstractBaseEntriesImpl<K, V> owner = AbstractBaseEntriesImpl.this;
                for (BaseEntry<K, V> conflictEntry : conflictEntries) {
                    owner.deleteBaseEntry(conflictEntry);
                    if (suspending != null) {
                        suspending.onConflictBaseEntryDeleted(conflictEntry);
                    }
                }
            }
            this.state = ATTACH_PROCESSOR_EXECUTING;
            return true;
        }
        
        @Override
        public void endExecute(Throwable throwable) {
            if (this.state != ATTACH_PROCESSOR_EXECUTING) {
                throw new IllegalStateException(
                        LAZY_RESOURCE.get().invalidOperationOnAttachProcessor(
                                "endExecute",
                                attachProcessorState(ATTACH_PROCESSOR_EXECUTING),
                                attachProcessorState(this.state)
                        )
                );
            }
            Trigger<K, V> trigger = this.trigger;
            if (trigger != null) {
                trigger.endExecute(throwable);
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
                                attachProcessorState(ATTACH_PROCESSOR_EXECUTED),
                                attachProcessorState(this.state)
                        )
                );
            }
            Trigger<K, V> trigger = this.trigger;
            if (trigger != null) {
                trigger.flush();
            }
            this.state = ATTACH_PROCESSOR_FLUSHED;
        }

        @Override
        public boolean isNew() {
            return this.state == ATTACH_PROCESSOR_NEW;
        }

        @Override
        public boolean isInitialized() {
            return this.state == ATTACH_PROCESSOR_INITIALIZED;
        }

        @Override
        public boolean isResolved() {
            return this.state == ATTACH_PROCESSOR_RESOLVED;
        }

        @Override
        public boolean isExecuted() {
            return this.state == ATTACH_PROCESSOR_EXECUTED;
        }

        @Override
        public boolean isFlushed() {
            return this.state == ATTACH_PROCESSOR_FLUSHED;
        }

        @SuppressWarnings("unchecked")
        private void intialize0(Set<K> conflictKeys) {
            if (conflictKeys != null && !conflictKeys.isEmpty()) { 
                BaseEntry<K, V>[] conflictEntries = new BaseEntry[conflictKeys.size()];
                if (conflictEntries.length != 0) {
                    AbstractBaseEntriesImpl<K, V> owner = AbstractBaseEntriesImpl.this;
                    int conflictIndex = 0;
                    for (K conflictKey : conflictKeys) {
                        BaseEntry<K, V> conflictEntry = owner.getBaseEntry(conflictKey);
                        if (conflictEntry == null) {
                            throw new IllegalProgramException(
                                    LAZY_RESOURCE.get().illegalBaseEntriesAlgorithm(owner.getClass(), conflictKey)
                            );
                        }
                        if (trigger != null) {
                            trigger.preRemove(conflictEntry);
                        }
                        conflictEntries[conflictIndex++] = conflictEntry;
                    }
                }
                this.conflictEntries = conflictEntries;
            }
        }
        
        private void beforeAddChangeExecute() {
            if (this.state == ATTACH_PROCESSOR_RESOLVED) {
                return;
            }
            if (this.state != ATTACH_PROCESSOR_INITIALIZED) {
                throw new IllegalStateException(
                        LAZY_RESOURCE.get().invalidOperationOnAttachProcessor(
                                "preAdd" +
                                LAZY_RESOURCE.get().orBetweenWordsWithQuotes() +
                                "preChange" +
                                LAZY_RESOURCE.get().orBetweenWordsWithQuotes() +
                                "beginExecute",
                                attachProcessorState(ATTACH_PROCESSOR_INITIALIZED),
                                attachProcessorState(this.state)
                        )
                );
            }
            this.state = ATTACH_PROCESSOR_RESOLVED;
        }
        
        private void beforeRemove() {
            if (this.state != ATTACH_PROCESSOR_NEW) {
                throw new IllegalStateException(
                        LAZY_RESOURCE.get().invalidOperationOnAttachProcessor(
                                "preRemove",
                                attachProcessorState(ATTACH_PROCESSOR_NEW),
                                attachProcessorState(this.state)
                        )
                );
            }
        }
        
    }
    
    private static class RootData<K, V> implements Serializable {
        
        private static final long serialVersionUID = 1402260612765762132L;

        ModificationAware owner;
        
        Object keyComparatorOrEqualityComparator;
        
        Object valueComparatorOrEqualityComparator;
        
        Validator<K> keyValidator;
        
        Validator<V> valueValidator;
        
        MapConflictVoterManager<K, V> voterManager;
        
        boolean keyRestrict;

        transient int frozenCount;
        
        transient UnifiedComparator<? super Entry<K, V>> entryUnifiedComparator;
    }
    
    private class MapSourceImpl implements MapSource<K, V> {
        
        private static final long serialVersionUID = 6322520372442262044L;

        @Override
        public XMap<K, V> getMap() {
            return new AbstractXMap.SimpleImpl<K, V>(AbstractBaseEntriesImpl.this);
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(AbstractBaseEntriesImpl.this);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof AbstractBaseEntriesImpl<?, ?>.MapSourceImpl)) {
                return false;
            }
            AbstractBaseEntriesImpl<?, ?>.MapSourceImpl other =
                    (AbstractBaseEntriesImpl<?, ?>.MapSourceImpl)obj;
            return AbstractBaseEntriesImpl.this == other.owner();
        }
        
        private Object owner() {
            return AbstractBaseEntriesImpl.this;
        }
        
    }
    
    private interface Resource {
        
        @SuppressWarnings("rawtypes")
        String constructorRequiresRootBaseEntries(
                Class<AbstractBaseEntriesImpl> abstractBaseEntriesType,
                Class<?> ... parameterTypes);
        
        String getMAOwnerCanOnlyBeInvokedByRootBaseEntries();
        
        String initMAOwnerCanOnlyBeInvokedByRootBaseEntries();
        
        String initMAOwnerCanOnlyBeInvokedOnce();
        
        String putAllIsNotSupportedForTransientValueEntries(
                Class<TransientValueEntries> transientValueEntriesType);
        
        String addAllIsOnlySupportedForTransientValueEntries(
                Class<TransientValueEntries> transientValueEntriesType);
        
        String pleaseOverrideTheMethod(String methodName, Class<?> ... parameterTypes);
        
        @SuppressWarnings("rawtypes")
        String addAllCanNotWorkWithValueUnifiedComparator(
                Class<Comparator> comparatorType,
                Class<EqualityComparator> equalityComparatorType);
        
        @SuppressWarnings("rawtypes")
        String addAllCanNotWorkWithValueConflictVoter(Class<MapConflictVoter> coflictVoterType);
        
        String invalidOperationOnAttachProcessor(
                String operationName,
                String expectedSate,
                String actualState);
        
        String invalidOperationOnTrigger(
                String operationName,
                String expectedSate,
                String actualState);
        
        String whenRootBaseEntriesIsInstanceof(
                Class<TransientValueEntries> transientValueEntriesType);
        
        @SuppressWarnings("rawtypes")
        String canNotCreateTriggerBecauseCurrentObjectIsFrozen(
                Class<Trigger> triggerType);
        
        String canNotUnfreezeCurrentObjectBecauseItIsNotFrozen();
        
        String onlyRootBaseEntriesCanBeSerialized();
        
        @SuppressWarnings("rawtypes")
        String uncessaryTrigger(
                Class<MAMapConflictVoter> maMapConflictVoterType,
                Class<BaseEntriesHandler> baseEntriesHandlerType);
        
        String illegalBaseEntriesAlgorithm(
                Class<?> runtimeType,
                Object unfoundConflictKey);
        
        String orBetweenWordsWithQuotes();
    }
    
}
