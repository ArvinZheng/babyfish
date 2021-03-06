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
package org.babyfish.collection.spi.wrapper;

import java.util.Comparator;

import org.babyfish.collection.TreeMap;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XMap;
import org.babyfish.collection.XNavigableMap;
import org.babyfish.collection.spi.base.NoEntryException;
import org.babyfish.collection.viewinfo.NavigableMapViewInfos;
import org.babyfish.collection.viewinfo.NavigableSetViewInfos;
import org.babyfish.collection.viewinfo.SortedMapViewInfos;
import org.babyfish.collection.viewinfo.SortedSetViewInfos;
import org.babyfish.view.View;
import org.babyfish.view.ViewInfo;

/**
 * @author Tao Chen
 */
public class AbstractWrapperXNavigableMap<K, V> 
extends AbstractWrapperXMap<K, V> 
implements XNavigableMap<K, V> {

    protected AbstractWrapperXNavigableMap(XNavigableMap<K, V> base) {
        super(base);
    }

    protected AbstractWrapperXNavigableMap(
            AbstractWrapperXNavigableMap<K, V> parent,
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
    protected AbstractWrapperXNavigableMap() {
        
    }

    @Override
    public K firstKey() {
        this.requiredEnabled();
        return this.<XNavigableMap<K, V>>getBase().firstKey();
    }

    @Override
    public K lastKey() {
        this.requiredEnabled();
        return this.<XNavigableMap<K, V>>getBase().lastKey();
    }

    @Override
    public K floorKey(K key) {
        this.requiredEnabled();
        return this.<XNavigableMap<K, V>>getBase().floorKey(key);
    }

    @Override
    public K ceilingKey(K key) {
        this.requiredEnabled();
        return this.<XNavigableMap<K, V>>getBase().ceilingKey(key);
    }

    @Override
    public K lowerKey(K key) {
        this.requiredEnabled();
        return this.<XNavigableMap<K, V>>getBase().lowerKey(key);
    }

    @Override
    public K higherKey(K key) {
        this.requiredEnabled();
        return this.<XNavigableMap<K, V>>getBase().higherKey(key);
    }

    @Override
    public XEntry<K, V> firstEntry() {
        try {
            return new FirstEntryImpl<K, V>(this);
        } catch (NoEntryException ex) {
            return null;
        }
    }

    @Override
    public XEntry<K, V> lastEntry() {
        try {
            return new LastEntryImpl<K, V>(this);
        } catch (NoEntryException ex) {
            return null;
        }
    }

    @Override
    public XEntry<K, V> floorEntry(K key) {
        try {
            return new FloorEntryImpl<K, V>(this, key);
        } catch (NoEntryException ex) {
            return null;
        }
    }
    
    @Override
    public XEntry<K, V> ceilingEntry(K key) {
        try {
            return new CeilingEntryImpl<K, V>(this, key);
        } catch (NoEntryException ex) {
            return null;
        }
    }

    @Override
    public XEntry<K, V> lowerEntry(K key) {
        try {
            return new LowerEntryImpl<K, V>(this, key);
        } catch (NoEntryException ex) {
            return null;
        }
    }

    @Override
    public XEntry<K, V> higherEntry(K key) {
        try {
            return new HigherEntryImpl<K, V>(this, key);
        } catch (NoEntryException ex) {
            return null;
        }
    }

    @Override
    public XNavigableMapView<K, V> descendingMap() {
        return new DescendingMapImpl<K, V>(this);
    }

    @Override
    public XNavigableMapView<K, V> headMap(K toKey) {
        return this.headMap(toKey, false);
    }

    @Override
    public XNavigableMapView<K, V> headMap(K toKey, boolean inclusive) {
        return new HeadMapImpl<K, V>(this, toKey, inclusive);
    }

    @Override
    public XNavigableMapView<K, V> tailMap(K fromKey) {
        return this.tailMap(fromKey, true);
    }

    @Override
    public XNavigableMapView<K, V> tailMap(K fromKey, boolean inclusive) {
        return new TailMapImpl<K, V>(this, fromKey, inclusive);
    }

    @Override
    public XNavigableMapView<K, V> subMap(K fromKey, K toKey) {
        return this.subMap(fromKey, true, toKey, false);
    }

    @Override
    public XNavigableMapView<K, V> subMap(
            K fromKey, boolean fromInclusive, 
            K toKey, boolean toInclusive) {
        return new SubMapImpl<K, V>(this, fromKey, fromInclusive, toKey, toInclusive);
    }

    @Override
    public XNavigableKeySetView<K> keySet() {
        return this.getKeySet();
    }

    @Override
    public XNavigableKeySetView<K> navigableKeySet() {
        return this.getKeySet();
    }

    @Override
    public XNavigableKeySetView<K> descendingKeySet() {
        return new DescendingKeySetImpl<K, V>(this);
    }

    @Override
    public final Comparator<? super K> comparator() {
        return this.keyUnifiedComparator().comparator();
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
        this.enable();
        return this.<XNavigableMap<K, V>>getBase().pollFirstEntry();
    }

    @Override
    public Entry<K, V> pollLastEntry() {
        this.enable();
        return this.<XNavigableMap<K, V>>getBase().pollLastEntry();
    }
    
    @Override
    protected RootData<K, V> createRootData() {
        return new RootData<K, V>();
    }

    @Deprecated
    @Override
    protected final XMap<K, V> createBaseView(
            XMap<K, V> parentBase, ViewInfo viewInfo) {
        return this.createBaseView((XNavigableMap<K, V>)parentBase, viewInfo);
    }
    
    protected XNavigableMap<K, V> createBaseView(
            XNavigableMap<K, V> parentBase, ViewInfo viewInfo) {
        throw new IllegalArgumentException(LAZY_COMMON_RESOURCE.get().illegalViewInfo());
    }
    
    @Override
    protected XNavigableKeySetView<K> createKeySet() {
        return new KeySetImpl<K, V>(this);
    }

    protected static abstract class AbstractSubMapImpl<K, V> 
    extends AbstractWrapperXNavigableMap<K, V>
    implements XNavigableMapView<K, V> {

        private AbstractSubMapImpl(
                AbstractWrapperXNavigableMap<K, V> parent,
                ViewInfo viewInfo) {
            super(parent, viewInfo);
        }

        @Override
        public ViewInfo viewInfo() {
            return this.<XNavigableMapView<K, V>>getBase().viewInfo();
        }
        
    }

    protected static class DescendingMapImpl<K, V> extends AbstractSubMapImpl<K, V> {
        
        protected DescendingMapImpl(
                AbstractWrapperXNavigableMap<K, V> parent) {
            super(parent, NavigableMapViewInfos.descendingMap());
        }
    
        @Override
        protected XNavigableMap<K, V> createBaseView(
                XNavigableMap<K, V> parentBase, 
                ViewInfo viewInfo) {
            if (viewInfo instanceof NavigableMapViewInfos.DescendingMap) {
                return parentBase.descendingMap();
            }
            throw new IllegalArgumentException(LAZY_COMMON_RESOURCE.get().illegalViewInfo());
        }
        
    }

    protected static class HeadMapImpl<K, V> extends AbstractSubMapImpl<K, V> {

        protected HeadMapImpl(
                AbstractWrapperXNavigableMap<K, V> parent,
                K toKey,
                boolean inclusive) {
            super(parent, NavigableMapViewInfos.headMap(toKey, inclusive));
        }

        @SuppressWarnings("unchecked")
        @Override
        protected XNavigableMap<K, V> createBaseView(
                XNavigableMap<K, V> parentBase, 
                ViewInfo viewInfo) {
            if (viewInfo instanceof NavigableMapViewInfos.HeadMapByToKeyAndInclusive) {
                NavigableMapViewInfos.HeadMapByToKeyAndInclusive headMapViewInfo =
                        (NavigableMapViewInfos.HeadMapByToKeyAndInclusive)viewInfo;
                return parentBase.headMap((K)headMapViewInfo.getToKey(), headMapViewInfo.isInclusive());
            }
            if (viewInfo instanceof SortedMapViewInfos.HeadMapByToKey) {
                SortedMapViewInfos.HeadMapByToKey headMapViewInfo =
                        (SortedMapViewInfos.HeadMapByToKey)viewInfo;
                return parentBase.headMap((K)headMapViewInfo.getToKey());
            }
            throw new IllegalArgumentException(LAZY_COMMON_RESOURCE.get().illegalViewInfo());
        }
        
    }
    
    protected static class TailMapImpl<K, V> extends AbstractSubMapImpl<K, V> {

        protected TailMapImpl(
                AbstractWrapperXNavigableMap<K, V> parent,
                K fromKey,
                boolean inclusive) {
            super(parent, NavigableMapViewInfos.tailMap(fromKey, inclusive));
        }

        @SuppressWarnings("unchecked")
        @Override
        protected XNavigableMap<K, V> createBaseView(
                XNavigableMap<K, V> parentBase, 
                ViewInfo viewInfo) {
            if (viewInfo instanceof NavigableMapViewInfos.TailMapByFromKeyAndInclusive) {
                NavigableMapViewInfos.TailMapByFromKeyAndInclusive headMapViewInfo =
                        (NavigableMapViewInfos.TailMapByFromKeyAndInclusive)viewInfo;
                return parentBase.tailMap((K)headMapViewInfo.getFromKey(), headMapViewInfo.isInclusive());
            }
            if (viewInfo instanceof SortedMapViewInfos.TailMapByFromKey) {
                SortedMapViewInfos.TailMapByFromKey tailMapViewInfo =
                        (SortedMapViewInfos.TailMapByFromKey)viewInfo;
                return parentBase.tailMap((K)tailMapViewInfo.getFromKey());
            }
            throw new IllegalArgumentException(LAZY_COMMON_RESOURCE.get().illegalViewInfo());
        }
        
    }
    
    protected static class SubMapImpl<K, V> extends AbstractSubMapImpl<K, V> {

        protected SubMapImpl(
                AbstractWrapperXNavigableMap<K, V> parent,
                K fromKey,
                boolean fromInclusive,
                K toKey,
                boolean toInclusive) {
            super(
                    parent, 
                    NavigableMapViewInfos.subMap(
                            fromKey, 
                            fromInclusive, 
                            toKey, 
                            toInclusive));
        }

        @SuppressWarnings("unchecked")
        @Override
        protected XNavigableMap<K, V> createBaseView(
                XNavigableMap<K, V> parentBase, 
                ViewInfo viewInfo) {
            if (viewInfo instanceof NavigableMapViewInfos.SubMapByFromKeyAndFromInclusiveAndToKeyAndToInclusive) {
                NavigableMapViewInfos.SubMapByFromKeyAndFromInclusiveAndToKeyAndToInclusive subMapViewInfo =
                        (NavigableMapViewInfos.SubMapByFromKeyAndFromInclusiveAndToKeyAndToInclusive)viewInfo;
                return parentBase.subMap(
                        (K)subMapViewInfo.getFromKey(), 
                        subMapViewInfo.isFromInclusive(),
                        (K)subMapViewInfo.getToKey(),
                        subMapViewInfo.isToInclusive());
            }
            if (viewInfo instanceof SortedMapViewInfos.SubMapByFromKeyAndToKey) {
                SortedMapViewInfos.SubMapByFromKeyAndToKey subMapViewInfo =
                        (SortedMapViewInfos.SubMapByFromKeyAndToKey)viewInfo;
                return parentBase.subMap((K)subMapViewInfo.getFromKey(), (K)subMapViewInfo.getToKey());
            }
            throw new IllegalArgumentException(LAZY_COMMON_RESOURCE.get().illegalViewInfo());
        }
        
    }
    
    protected static abstract class AbstractKeySetImpl<K, V> 
    extends AbstractWrapperXMap.AbstractKeySetImpl<K, V> 
    implements XNavigableKeySetView<K> {

        protected AbstractKeySetImpl(
                AbstractWrapperXMap<K, V> parentMap,
                ViewInfo viewInfo) {
            super(parentMap, viewInfo);
        }

        protected AbstractKeySetImpl(
                AbstractKeySetImpl<K, V> parent,
                ViewInfo viewInfo) {
            super(parent, viewInfo);
        }

        @Deprecated
        @Override
        protected final XKeySetView<K> createBaseView(
                XMap<K, V> baseMap, ViewInfo viewInfo) {
            return this.createBaseView((XNavigableMap<K, V>)baseMap, viewInfo);
        }
        
        protected XNavigableKeySetView<K> createBaseView(
                XNavigableMap<K, V> baseMap, ViewInfo viewInfo) {
            throw new IllegalArgumentException(LAZY_COMMON_RESOURCE.get().illegalViewInfo());
        }

        @Deprecated
        @Override
        protected final XKeySetView<K> createBaseView(
                XKeySetView<K> parentBase,
                ViewInfo viewInfo) {
            return this.createBaseView((XNavigableKeySetView<K>)parentBase, viewInfo);
        }
        
        protected XNavigableKeySetView<K> createBaseView(
                XNavigableKeySetView<K> parentBase,
                ViewInfo viewInfo) {
            throw new IllegalArgumentException(LAZY_COMMON_RESOURCE.get().illegalViewInfo());
        }

        @Override
        public XIterator<K> descendingIterator() {
            return new DescendingIteratorImpl<K, V>(this);
        }

        @Override
        public XNavigableKeySetView<K> descendingSet() {
            return new DescendingSetImpl<K, V>(this);
        }

        @Override
        public XNavigableKeySetView<K> headSet(K toElement) {
            return this.headSet(toElement, false);
        }

        @Override
        public XNavigableKeySetView<K> headSet(K toElement, boolean inclusive) {
            return new HeadSetImpl<K, V>(this, toElement, inclusive);
        }

        @Override
        public XNavigableKeySetView<K> tailSet(K fromElement) {
            return this.tailSet(fromElement, true);
        }

        @Override
        public XNavigableKeySetView<K> tailSet(K fromElement, boolean inclusive) {
            return new TailSetImpl<K, V>(this, fromElement, inclusive);
        }

        @Override
        public XNavigableKeySetView<K> subSet(
                K fromElement, K toElement) {
            return this.subSet(
                    fromElement, 
                    true, 
                    toElement, 
                    false);
        }

        @Override
        public XNavigableKeySetView<K> subSet(
                K fromElement, boolean fromInclusive,
                K toElement, boolean toInclusive) {
            return new SubSetImpl<K, V>(
                    this, 
                    fromElement, 
                    fromInclusive, 
                    toElement, 
                    toInclusive);
        }

        @Override
        public K first() {
            this.requiredEnabled();
            return this.<XNavigableKeySetView<K>>getBase().first();
        }

        @Override
        public K last() {
            this.requiredEnabled();
            return this.<XNavigableKeySetView<K>>getBase().last();
        }

        @Override
        public K floor(K e) {
            this.requiredEnabled();
            return this.<XNavigableKeySetView<K>>getBase().floor(e);
        }

        @Override
        public K ceiling(K e) {
            this.requiredEnabled();
            return this.<XNavigableKeySetView<K>>getBase().ceiling(e);
        }

        @Override
        public K lower(K e) {
            this.requiredEnabled();
            return this.<XNavigableKeySetView<K>>getBase().lower(e);
        }

        @Override
        public K higher(K e) {
            this.requiredEnabled();
            return this.<XNavigableKeySetView<K>>getBase().higher(e);
        }

        @Override
        public K pollFirst() {
            this.enable();
            return this.<XNavigableKeySetView<K>>getBase().pollFirst();
        }

        @Override
        public K pollLast() {
            this.enable();
            return this.<XNavigableKeySetView<K>>getBase().pollLast();
        }

        @Override
        public final Comparator<? super K> comparator() {
            return this.unifiedComparator().comparator();
        }
        
        protected static class HeadSetImpl<K, V> extends AbstractKeySetImpl<K, V> {

            protected HeadSetImpl(
                    AbstractKeySetImpl<K, V> parent, 
                    K toElement,
                    boolean inclusive) {
                super(
                        parent, 
                        NavigableSetViewInfos.headSet(toElement, inclusive));
            }

            @SuppressWarnings("unchecked")
            @Override
            protected XNavigableKeySetView<K> createBaseView(
                    XNavigableKeySetView<K> parentBase,
                    ViewInfo viewInfo) {
                if (viewInfo instanceof NavigableSetViewInfos.HeadSetByToElementAndInclusive) {
                    NavigableSetViewInfos.HeadSetByToElementAndInclusive headSetViewInfo =
                            (NavigableSetViewInfos.HeadSetByToElementAndInclusive)viewInfo;
                    return parentBase.headSet((K)headSetViewInfo.getToElement(), headSetViewInfo.isInclusive());
                }
                if (viewInfo instanceof SortedSetViewInfos.HeadSetByToElement) {
                    SortedSetViewInfos.HeadSetByToElement headSetViewInfo =
                            (SortedSetViewInfos.HeadSetByToElement)viewInfo;
                    return parentBase.headSet((K)headSetViewInfo.getToElement());
                }
                throw new IllegalArgumentException(LAZY_COMMON_RESOURCE.get().illegalViewInfo());
            }
            
        }
        
        protected static class TailSetImpl<K, V> extends AbstractKeySetImpl<K, V> {

            protected TailSetImpl(
                    AbstractKeySetImpl<K, V> parent, 
                    K fromElement,
                    boolean inclusive) {
                super(
                        parent, 
                        NavigableSetViewInfos.tailSet(fromElement, inclusive));
            }
            
            @SuppressWarnings("unchecked")
            @Override
            protected XNavigableKeySetView<K> createBaseView(
                    XNavigableKeySetView<K> parentBase,
                    ViewInfo viewInfo) {
                if (viewInfo instanceof NavigableSetViewInfos.TailSetByFromElementAndInclusive) {
                    NavigableSetViewInfos.TailSetByFromElementAndInclusive tailSetViewInfo =
                            (NavigableSetViewInfos.TailSetByFromElementAndInclusive)viewInfo;
                    return parentBase.tailSet((K)tailSetViewInfo.getFromElement(), tailSetViewInfo.isInclusive());
                }
                if (viewInfo instanceof SortedSetViewInfos.TailSetByFromElement) {
                    SortedSetViewInfos.TailSetByFromElement tailSetViewInfo =
                            (SortedSetViewInfos.TailSetByFromElement)viewInfo;
                    return parentBase.tailSet((K)tailSetViewInfo.getFromElement());
                }
                throw new IllegalArgumentException(LAZY_COMMON_RESOURCE.get().illegalViewInfo());
            }
        }
        
        protected static class SubSetImpl<K, V> extends AbstractKeySetImpl<K, V> {

            protected SubSetImpl(
                    AbstractKeySetImpl<K, V> parent, 
                    K fromElement,
                    boolean fromInclusive,
                    K toElement,
                    boolean toInclusive) {
                super(
                        parent, 
                        NavigableSetViewInfos.subSet(
                                fromElement, 
                                fromInclusive, 
                                toElement, 
                                toInclusive));
            }

            @SuppressWarnings("unchecked")
            @Override
            protected XNavigableKeySetView<K> createBaseView(
                    XNavigableKeySetView<K> parentBase,
                    ViewInfo viewInfo) {
                if (viewInfo instanceof NavigableSetViewInfos.SubSetByFromElementAndFromInclusiveAndToElementAndToInclusive) {
                    NavigableSetViewInfos.SubSetByFromElementAndFromInclusiveAndToElementAndToInclusive subSetViewInfo =
                            (NavigableSetViewInfos.SubSetByFromElementAndFromInclusiveAndToElementAndToInclusive)viewInfo;
                    return parentBase.subSet(
                            (K)subSetViewInfo.getFromElement(), 
                            subSetViewInfo.isFromInclusive(), 
                            (K)subSetViewInfo.getToElement(), 
                            subSetViewInfo.isToInclusive());
                }
                if (viewInfo instanceof SortedSetViewInfos.SubSetByFromElementAndToElement) {
                    SortedSetViewInfos.SubSetByFromElementAndToElement subSetViewInfo =
                            (SortedSetViewInfos.SubSetByFromElementAndToElement)viewInfo;
                    return parentBase.subSet(
                            (K)subSetViewInfo.getFromElement(), 
                            (K)subSetViewInfo.getToElement());
                }
                throw new IllegalArgumentException(LAZY_COMMON_RESOURCE.get().illegalViewInfo());
            }
            
        }
        
        protected static class DescendingSetImpl<K, V> extends AbstractKeySetImpl<K, V> {

            protected DescendingSetImpl(AbstractKeySetImpl<K, V> parent) {
                super(
                        parent, 
                        NavigableSetViewInfos.descendingSet());
            }

            @Override
            protected XNavigableKeySetView<K> createBaseView(
                    XNavigableKeySetView<K> parentBase,
                    ViewInfo viewInfo) {
                if (viewInfo instanceof NavigableSetViewInfos.DescendingSet) {
                    return parentBase.descendingSet();
                }
                throw new IllegalArgumentException(LAZY_COMMON_RESOURCE.get().illegalViewInfo());
            }
            
        }
        
        protected static abstract class AbstractIteratorImpl<K, V> 
        extends AbstractWrapperXMap.AbstractKeySetImpl.AbstractIteratorImpl<K, V> {

            protected AbstractIteratorImpl(
                    AbstractKeySetImpl<K, V> parent,
                    ViewInfo viewInfo) {
                super(parent, viewInfo);
            }

            @Deprecated
            @Override
            protected final XIterator<K> createBaseView(
                    XKeySetView<K> baseKeySet,
                    ViewInfo viewInfo) {
                return this.createBaseView((XNavigableKeySetView<K>)baseKeySet, viewInfo);
            }
            
            protected abstract XIterator<K> createBaseView(
                    XNavigableKeySetView<K> baseKeySet,
                    ViewInfo viewInfo);
        }
        
        protected static class DescendingIteratorImpl<K, V> extends AbstractIteratorImpl<K, V> {

            protected DescendingIteratorImpl(AbstractKeySetImpl<K, V> parent) {
                super(parent, NavigableSetViewInfos.descendingIterator());
            }

            @Override
            protected XIterator<K> createBaseView(
                    XNavigableKeySetView<K> baseKeySet,
                    ViewInfo viewInfo) {
                if (viewInfo instanceof NavigableSetViewInfos.DescendingIterator) {
                    return baseKeySet.descendingIterator();
                }
                throw new IllegalArgumentException(LAZY_COMMON_RESOURCE.get().illegalViewInfo());
            }
        }
    }
    
    protected static class KeySetImpl<K, V> extends AbstractKeySetImpl<K, V> {

        protected KeySetImpl(
                AbstractWrapperXNavigableMap<K, V> parentMap) {
            super(parentMap, NavigableMapViewInfos.navigableKeySet());
        }

        @Override
        protected XNavigableKeySetView<K> createBaseView(
                XNavigableMap<K, V> baseMap, 
                ViewInfo viewInfo) {
            if (viewInfo instanceof NavigableMapViewInfos.NavigableKeySet) {
                return baseMap.navigableKeySet();
            }
            throw new IllegalArgumentException(LAZY_COMMON_RESOURCE.get().illegalViewInfo());
        }
        
    }
    
    protected static class DescendingKeySetImpl<K, V> extends AbstractKeySetImpl<K, V> {

        protected DescendingKeySetImpl(AbstractWrapperXNavigableMap<K, V> parentMap) {
            super(parentMap, NavigableMapViewInfos.descendingKeySet());
        }

        @Override
        protected XNavigableKeySetView<K> createBaseView(
                XNavigableMap<K, V> baseMap, 
                ViewInfo viewInfo) {
            if (viewInfo instanceof NavigableMapViewInfos.DescendingKeySet) {
                return baseMap.descendingKeySet();
            }
            throw new IllegalArgumentException(LAZY_COMMON_RESOURCE.get().illegalViewInfo());
        }
        
    }
    
    protected static abstract class AbstractEntryImpl<K, V> extends AbstractWrapperXMap.AbstractEntryImpl<K, V> {

        protected AbstractEntryImpl(
                AbstractWrapperXNavigableMap<K, V> parentMap,
                ViewInfo viewInfo) throws NoEntryException {
            super(parentMap, viewInfo);
        }

        @Deprecated
        @Override
        protected final XEntry<K, V> createBaseView(
                XMap<K, V> map, ViewInfo viewInfo) {
            return this.createBaseView((XNavigableMap<K, V>)map, viewInfo);
        }
        
        protected abstract XEntry<K, V> createBaseView(
                XNavigableMap<K, V> baseMap, ViewInfo viewInfo);
        
    }
    
    protected static class FirstEntryImpl<K, V> extends AbstractEntryImpl<K, V> {

        protected FirstEntryImpl(AbstractWrapperXNavigableMap<K, V> parentMap) throws NoEntryException {
            super(parentMap, NavigableMapViewInfos.firstEntry());
        }

        @Override
        protected XEntry<K, V> createBaseView(
                XNavigableMap<K, V> baseMap, 
                ViewInfo viewInfo) {
            if (viewInfo instanceof NavigableMapViewInfos.FirstEntry) {
                return baseMap.firstEntry();
            }
            throw new IllegalArgumentException(LAZY_COMMON_RESOURCE.get().illegalViewInfo());
        }
        
    }
    
    protected static class LastEntryImpl<K, V> extends AbstractEntryImpl<K, V> {

        protected LastEntryImpl(AbstractWrapperXNavigableMap<K, V> parentMap) throws NoEntryException {
            super(parentMap, NavigableMapViewInfos.lastEntry());
        }

        @Override
        protected XEntry<K, V> createBaseView(
                XNavigableMap<K, V> baseMap, 
                ViewInfo viewInfo) {
            if (viewInfo instanceof NavigableMapViewInfos.LastEntry) {
                return baseMap.lastEntry();
            }
            throw new IllegalArgumentException(LAZY_COMMON_RESOURCE.get().illegalViewInfo());
        }
        
    }
    
    protected static class FloorEntryImpl<K, V> extends AbstractEntryImpl<K, V> {

        protected FloorEntryImpl(
                AbstractWrapperXNavigableMap<K, V> parentMap,
                K key) throws NoEntryException {
            super(parentMap, NavigableMapViewInfos.floorEntry(key));
        }

        @SuppressWarnings("unchecked")
        @Override
        protected XEntry<K, V> createBaseView(
                XNavigableMap<K, V> baseMap, 
                ViewInfo viewInfo) {
            if (viewInfo instanceof NavigableMapViewInfos.FloorEntryByKey) {
                NavigableMapViewInfos.FloorEntryByKey floorEntryViewInfo =
                        (NavigableMapViewInfos.FloorEntryByKey)viewInfo;
                return baseMap.floorEntry((K)floorEntryViewInfo.getKey());
            }
            throw new IllegalArgumentException(LAZY_COMMON_RESOURCE.get().illegalViewInfo());
        }
        
    }
    
    protected static class CeilingEntryImpl<K, V> extends AbstractEntryImpl<K, V> {

        protected CeilingEntryImpl(
                AbstractWrapperXNavigableMap<K, V> parentMap,
                K key) throws NoEntryException {
            super(parentMap, NavigableMapViewInfos.ceilingEntry(key));
        }

        @SuppressWarnings("unchecked")
        @Override
        protected XEntry<K, V> createBaseView(
                XNavigableMap<K, V> baseMap, 
                ViewInfo viewInfo) {
            if (viewInfo instanceof NavigableMapViewInfos.CeilingEntryByKey) {
                NavigableMapViewInfos.CeilingEntryByKey ceilingEntryViewInfo =
                        (NavigableMapViewInfos.CeilingEntryByKey)viewInfo;
                return baseMap.ceilingEntry((K)ceilingEntryViewInfo.getKey());
            }
            throw new IllegalArgumentException(LAZY_COMMON_RESOURCE.get().illegalViewInfo());
        }
        
    }
    
    protected static class LowerEntryImpl<K, V> extends AbstractEntryImpl<K, V> {

        protected LowerEntryImpl(
                AbstractWrapperXNavigableMap<K, V> parentMap,
                K key) throws NoEntryException {
            super(parentMap, NavigableMapViewInfos.lowerEntry(key));
        }

        @SuppressWarnings("unchecked")
        @Override
        protected XEntry<K, V> createBaseView(
                XNavigableMap<K, V> baseMap, 
                ViewInfo viewInfo) {
            if (viewInfo instanceof NavigableMapViewInfos.LowerEntryByKey) {
                NavigableMapViewInfos.LowerEntryByKey lowerEntryViewInfo =
                        (NavigableMapViewInfos.LowerEntryByKey)viewInfo;
                return baseMap.lowerEntry((K)lowerEntryViewInfo.getKey());
            }
            throw new IllegalArgumentException(LAZY_COMMON_RESOURCE.get().illegalViewInfo());
        }
        
    }
    
    protected static class HigherEntryImpl<K, V> extends AbstractEntryImpl<K, V> {

        protected HigherEntryImpl(
                AbstractWrapperXNavigableMap<K, V> parentMap,
                K key) throws NoEntryException {
            super(parentMap, NavigableMapViewInfos.higherEntry(key));
        }

        @SuppressWarnings("unchecked")
        @Override
        protected XEntry<K, V> createBaseView(
                XNavigableMap<K, V> baseMap, 
                ViewInfo viewInfo) {
            if (viewInfo instanceof NavigableMapViewInfos.HigherEntryByKey) {
                NavigableMapViewInfos.HigherEntryByKey higherEntryViewInfo =
                        (NavigableMapViewInfos.HigherEntryByKey)viewInfo;
                return baseMap.higherEntry((K)higherEntryViewInfo.getKey());
            }
            throw new IllegalArgumentException(LAZY_COMMON_RESOURCE.get().illegalViewInfo());
        }
        
    }
    
    protected static class RootData<K, V> extends AbstractWrapperXMap.RootData<K, V> {

        private static final long serialVersionUID = -2689312190744395740L;
        
        public RootData() {
            
        }

        @Deprecated
        @Override
        protected final void setBase(XMap<K, V> base) {
            this.setBase((XNavigableMap<K, V>)base);
        }
        
        protected void setBase(XNavigableMap<K, V> base) {
            super.setBase(base);
        }

        @Override
        protected XNavigableMap<K, V> createDefaultBase(
                UnifiedComparator<? super K> keyUnifiedComparator,
                UnifiedComparator<? super V> valueUnifiedComparator) {
            return new TreeMap<K, V>(
                    keyUnifiedComparator.comparator(true), 
                    valueUnifiedComparator);
        }
        
    }

}
