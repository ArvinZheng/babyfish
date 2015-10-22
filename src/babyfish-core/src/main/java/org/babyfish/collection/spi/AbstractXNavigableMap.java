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

import java.util.Collection;
import java.util.Comparator;

import org.babyfish.collection.XNavigableMap;
import org.babyfish.collection.spi.base.BaseEntry;
import org.babyfish.collection.spi.base.NavigableBaseEntries;
import org.babyfish.collection.spi.base.NoEntryException;
import org.babyfish.collection.viewinfo.NavigableMapViewInfos;
import org.babyfish.collection.viewinfo.NavigableMapViewInfos.CeilingEntryByKey;
import org.babyfish.collection.viewinfo.NavigableMapViewInfos.DescendingKeySet;
import org.babyfish.collection.viewinfo.NavigableMapViewInfos.DescendingMap;
import org.babyfish.collection.viewinfo.NavigableMapViewInfos.FirstEntry;
import org.babyfish.collection.viewinfo.NavigableMapViewInfos.FloorEntryByKey;
import org.babyfish.collection.viewinfo.NavigableMapViewInfos.HeadMapByToKeyAndInclusive;
import org.babyfish.collection.viewinfo.NavigableMapViewInfos.HigherEntryByKey;
import org.babyfish.collection.viewinfo.NavigableMapViewInfos.LastEntry;
import org.babyfish.collection.viewinfo.NavigableMapViewInfos.LowerEntryByKey;
import org.babyfish.collection.viewinfo.NavigableMapViewInfos.NavigableKeySet;
import org.babyfish.collection.viewinfo.NavigableMapViewInfos.SubMapByFromKeyAndFromInclusiveAndToKeyAndToInclusive;
import org.babyfish.collection.viewinfo.NavigableMapViewInfos.TailMapByFromKeyAndInclusive;
import org.babyfish.collection.viewinfo.NavigableSetViewInfos;
import org.babyfish.collection.viewinfo.NavigableSetViewInfos.DescendingSet;
import org.babyfish.collection.viewinfo.NavigableSetViewInfos.HeadSetByToElementAndInclusive;
import org.babyfish.collection.viewinfo.NavigableSetViewInfos.SubSetByFromElementAndFromInclusiveAndToElementAndToInclusive;
import org.babyfish.collection.viewinfo.NavigableSetViewInfos.TailSetByFromElementAndInclusive;
import org.babyfish.view.View;

/**
 * @author Tao Chen
 */
public class AbstractXNavigableMap<K, V> extends AbstractXMap<K, V> implements XNavigableMap<K, V> {

    protected AbstractXNavigableMap(NavigableBaseEntries<K, V> baseEntries) {
        super(baseEntries);
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
    protected AbstractXNavigableMap() {
        
    }

    @Override
    public Comparator<? super K> comparator() {
        return this.<NavigableBaseEntries<K, V>>getBaseEntries().comparator();
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
        return new DescendingKeySetImpl<K>(this);
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
    public XNavigableMapView<K, V> subMap(K fromKey, boolean fromInclusive, K toKey,
            boolean toInclusive) {
        return new SubMapImpl<K, V>(this, fromKey, fromInclusive, toKey, toInclusive);
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
    public K firstKey() {
        BaseEntry<K, V> be = this.<NavigableBaseEntries<K, V>>getBaseEntries().first();
        return be == null ? null : be.getKey();
    }

    @Override
    public K lastKey() {
        BaseEntry<K, V> be = this.<NavigableBaseEntries<K, V>>getBaseEntries().last();
        return be == null ? null : be.getKey();
    }

    @Override
    public K floorKey(K key) {
        BaseEntry<K, V> be = this.<NavigableBaseEntries<K, V>>getBaseEntries().floor(key);
        return be == null ? null : be.getKey();
    }

    @Override
    public K ceilingKey(K key) {
        BaseEntry<K, V> be = this.<NavigableBaseEntries<K, V>>getBaseEntries().ceiling(key);
        return be == null ? null : be.getKey();
    }

    @Override
    public K lowerKey(K key) {
        BaseEntry<K, V> be = this.<NavigableBaseEntries<K, V>>getBaseEntries().lower(key);
        return be == null ? null : be.getKey();
    }

    @Override
    public K higherKey(K key) {
        BaseEntry<K, V> be = this.<NavigableBaseEntries<K, V>>getBaseEntries().higher(key);
        return be == null ? null : be.getKey();
    }

    @Override
    public Entry<K, V> pollFirstEntry() {
        return this.<NavigableBaseEntries<K, V>>getBaseEntries().pollFirst(null);
    }

    @Override
    public Entry<K, V> pollLastEntry() {
        return this.<NavigableBaseEntries<K, V>>getBaseEntries().pollLast(null);
    }
    
    @Override
    protected XNavigableKeySetView<K> createKeySet() {
        return new KeySetImpl<K>(this);
    }

    protected static abstract class AbstractSubMapImpl<K, V> 
    extends AbstractXNavigableMap<K, V> 
    implements XNavigableMapView<K, V> {
        
        AbstractSubMapImpl(NavigableBaseEntries<K, V> baseEntries) {
            super(baseEntries);
        }   
    }
    
    protected static class HeadMapImpl<K, V> extends AbstractSubMapImpl<K, V> {
        
        private HeadMapByToKeyAndInclusive viewInfo;
        
        protected HeadMapImpl(AbstractXNavigableMap<K, V> parent, K toKey, boolean inclusive) {
            super (((NavigableBaseEntries<K, V>)parent.baseEntries)
                    .subEntries(
                            false, null, false, 
                            true, toKey, inclusive));
            this.viewInfo = NavigableMapViewInfos.headMap(toKey, inclusive);
        }

        @Override
        public HeadMapByToKeyAndInclusive viewInfo() {
            return this.viewInfo;
        }
        
    }
    
    protected static class TailMapImpl<K, V> extends AbstractSubMapImpl<K, V> {
        
        private TailMapByFromKeyAndInclusive viewInfo;
        
        protected TailMapImpl(AbstractXNavigableMap<K, V> parent, K fromKey, boolean inclusive) {
            super (((NavigableBaseEntries<K, V>)parent.baseEntries)
                    .subEntries(
                            true, fromKey, inclusive, 
                            false, null, false));
            this.viewInfo = NavigableMapViewInfos.tailMap(fromKey, inclusive);
        }

        @Override
        public TailMapByFromKeyAndInclusive viewInfo() {
            return this.viewInfo;
        }
    }
    
    protected static class SubMapImpl<K, V> extends AbstractSubMapImpl<K, V> {
        
        private SubMapByFromKeyAndFromInclusiveAndToKeyAndToInclusive viewInfo;
        
        protected SubMapImpl(
                AbstractXNavigableMap<K, V> parentMap,
                K fromKey, boolean fromInclusive,
                K toKey, boolean toInclusive) {
            super (((NavigableBaseEntries<K, V>)parentMap.baseEntries)
                    .subEntries(
                            true, fromKey, fromInclusive, 
                            true, toKey, toInclusive));
            this.viewInfo = NavigableMapViewInfos.subMap(
                    fromKey, fromInclusive, toKey, toInclusive);
        }

        @Override
        public SubMapByFromKeyAndFromInclusiveAndToKeyAndToInclusive viewInfo() {
            return this.viewInfo;
        }
    }
    
    protected static class DescendingMapImpl<K, V> extends AbstractSubMapImpl<K, V> {
        
        protected DescendingMapImpl(AbstractXNavigableMap<K, V> parentMap) {
            super (((NavigableBaseEntries<K, V>)parentMap.baseEntries).descendingEntries());
        }

        @Override
        public DescendingMap viewInfo() {
            return NavigableMapViewInfos.descendingMap();
        }
    }
    

    protected static abstract class AbstractNavigableKeySetImpl<K> 
    extends AbstractXNavigableSet<K> 
    implements XNavigableKeySetView<K> {

        AbstractNavigableKeySetImpl(NavigableBaseEntries<K, Object> baseEntries) {
            super(baseEntries);
        }
        
        @Deprecated
        @Override
        public final boolean add(K e) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Deprecated
        @Override
        public final boolean addAll(Collection<? extends K> c) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public XNavigableKeySetView<K> descendingSet() {
            return new DescendingSetImpl<K>(this);
        }
        
        @Override
        public XNavigableKeySetView<K> headSet(K toElement) {
            return this.headSet(toElement, false);
        }
        
        @Override
        public XNavigableKeySetView<K> tailSet(K fromElement) {
            return this.tailSet(fromElement, true);
        }
        
        @Override
        public XNavigableKeySetView<K> subSet(K fromElement, K toElement) {
            return this.subSet(fromElement, true, toElement, false);
        }
        
        @Override
        public XNavigableKeySetView<K> headSet(K toElement, boolean inclusive) {
            return new HeadSetImpl<K>(this, toElement, inclusive);
        }
        
        @Override
        public XNavigableKeySetView<K> tailSet(K fromElement, boolean inclusive) {
            return new TailSetImpl<K>(this, fromElement, inclusive);
        }
        
        @Override
        public XNavigableKeySetView<K> subSet(
                K fromElement, boolean fromInclusive, 
                K toElement, boolean toInclusive) {
            return new SubSetImpl<K>(this, fromElement, fromInclusive, toElement, toInclusive);
        }
        
        protected static class DescendingSetImpl<K> extends AbstractNavigableKeySetImpl<K> {
            
            protected DescendingSetImpl(AbstractNavigableKeySetImpl<K> parent) {
                super(((NavigableBaseEntries<K, Object>)parent.baseEntries).descendingEntries());
            }
        
            @Override
            public DescendingSet viewInfo() {
                return NavigableSetViewInfos.descendingSet();
            }
            
        }

        protected static class HeadSetImpl<K> extends AbstractNavigableKeySetImpl<K> {

            private HeadSetByToElementAndInclusive viewInfo;
            
            HeadSetImpl(AbstractNavigableKeySetImpl<K> parent, K toElement, boolean inclusive) {
                super(((NavigableBaseEntries<K, Object>)parent.baseEntries)
                        .subEntries(
                                false, null, false, 
                                true, toElement, inclusive));
                this.viewInfo = NavigableSetViewInfos.headSet(toElement, inclusive);
            }
            
            @Override
            public HeadSetByToElementAndInclusive viewInfo() {
                return this.viewInfo;
            }
        }
        
        protected static class TailSetImpl<K> extends AbstractNavigableKeySetImpl<K> {

            private TailSetByFromElementAndInclusive viewInfo;
            
            TailSetImpl(AbstractNavigableKeySetImpl<K> parent, K fromElement, boolean inclusive) {
                super(((NavigableBaseEntries<K, Object>)parent.baseEntries)
                        .subEntries(
                                true, fromElement, inclusive, 
                                false, null, false));
                this.viewInfo = NavigableSetViewInfos.tailSet(fromElement, inclusive);
            }
            
            @Override
            public TailSetByFromElementAndInclusive viewInfo() {
                return this.viewInfo;
            }
        }
        
        protected static class SubSetImpl<K> extends AbstractNavigableKeySetImpl<K> {

            private SubSetByFromElementAndFromInclusiveAndToElementAndToInclusive viewInfo;
            
            SubSetImpl(
                    AbstractNavigableKeySetImpl<K> parent, 
                    K fromElement, boolean fromInclusive, 
                    K toElement, boolean toInclusive) {
                super(
                        ((NavigableBaseEntries<K, Object>)parent.baseEntries)
                        .subEntries(
                                true, fromElement, fromInclusive, 
                                true, toElement, toInclusive));
                this.viewInfo = NavigableSetViewInfos.subSet(
                        fromElement, fromInclusive, toElement, toInclusive);
            }
            
            @Override
            public SubSetByFromElementAndFromInclusiveAndToElementAndToInclusive viewInfo() {
                return this.viewInfo;
            }
        }
        
    }
    
    protected static class KeySetImpl<K> extends AbstractNavigableKeySetImpl<K> {

        @SuppressWarnings("unchecked")
        protected KeySetImpl(AbstractXNavigableMap<K, ?> parentMap) {
            super((NavigableBaseEntries<K, Object>)parentMap.getBaseEntries());
        }

        @Override
        public NavigableKeySet viewInfo() {
            return NavigableMapViewInfos.navigableKeySet();
        }
        
    }
    
    protected static class DescendingKeySetImpl<K> extends AbstractNavigableKeySetImpl<K> {

        @SuppressWarnings("unchecked")
        protected DescendingKeySetImpl(AbstractXNavigableMap<K, ?> parentMap) {
            super(((NavigableBaseEntries<K, Object>)parentMap.getBaseEntries()).descendingEntries());
        }

        @Override
        public DescendingKeySet viewInfo() {
            return NavigableMapViewInfos.descendingKeySet();
        }
        
    }
    
    protected static class FirstEntryImpl<K, V> extends AbstractEntryImpl<K, V> {

        public FirstEntryImpl(AbstractXNavigableMap<K, V> parentMap)
                throws NoEntryException {
            super(((NavigableBaseEntries<K, V>)parentMap.baseEntries).first());
        }

        @Override
        public FirstEntry viewInfo() {
            return NavigableMapViewInfos.firstEntry();
        }
        
    }
    
    protected static class LastEntryImpl<K, V> extends AbstractEntryImpl<K, V> {

        public LastEntryImpl(AbstractXNavigableMap<K, V> parentMap)
                throws NoEntryException {
            super(((NavigableBaseEntries<K, V>)parentMap.baseEntries).last());
        }

        @Override
        public LastEntry viewInfo() {
            return NavigableMapViewInfos.lastEntry();
        }
        
    }
    
    protected static class FloorEntryImpl<K, V> extends AbstractEntryImpl<K, V> {

        private FloorEntryByKey viewInfo;
        
        public FloorEntryImpl(AbstractXNavigableMap<K, V> parentMap, K key)
                throws NoEntryException {
            super(((NavigableBaseEntries<K, V>)parentMap.baseEntries).floor(key));
            this.viewInfo = NavigableMapViewInfos.floorEntry(key);
        }

        @Override
        public FloorEntryByKey viewInfo() {
            return this.viewInfo;
        }
        
    }
    
    protected static class CeilingEntryImpl<K, V> extends AbstractEntryImpl<K, V> {

        private CeilingEntryByKey viewInfo;
        
        public CeilingEntryImpl(AbstractXNavigableMap<K, V> parentMap, K key)
                throws NoEntryException {
            super(((NavigableBaseEntries<K, V>)parentMap.baseEntries).ceiling(key));
            this.viewInfo = NavigableMapViewInfos.ceilingEntry(key);
        }

        @Override
        public CeilingEntryByKey viewInfo() {
            return this.viewInfo;
        }
        
    }
    
    protected static class LowerEntryImpl<K, V> extends AbstractEntryImpl<K, V> {

        private LowerEntryByKey viewInfo;
        
        public LowerEntryImpl(AbstractXNavigableMap<K, V> parentMap, K key)
                throws NoEntryException {
            super(((NavigableBaseEntries<K, V>)parentMap.baseEntries).lower(key));
            this.viewInfo = NavigableMapViewInfos.lowerEntry(key);
        }

        @Override
        public LowerEntryByKey viewInfo() {
            return this.viewInfo;
        }
        
    }
    
    protected static class HigherEntryImpl<K, V> extends AbstractEntryImpl<K, V> {

        private HigherEntryByKey viewInfo;
        
        public HigherEntryImpl(AbstractXNavigableMap<K, V> parentMap, K key)
                throws NoEntryException {
            super(((NavigableBaseEntries<K, V>)parentMap.baseEntries).higher(key));
            this.viewInfo = NavigableMapViewInfos.higherEntry(key);
        }

        @Override
        public HigherEntryByKey viewInfo() {
            return this.viewInfo;
        }
        
    }

}
