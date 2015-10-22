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
package org.babyfish.collection.viewinfo;

import org.babyfish.immutable.Parameters;
import org.babyfish.view.ViewInfo;

/**
 * @author Tao Chen
 */
public class NavigableMapViewInfos extends SortedMapViewInfos {
    
    private static final Factory FACTORY = getViewInfoFactory(Factory.class);

    protected NavigableMapViewInfos() {
        
    }
    
    public static HeadMapByToKeyAndInclusive headMap(Object toKey, boolean inclusive) {
        return FACTORY.headMap(toKey, inclusive);
    }
    
    public static TailMapByFromKeyAndInclusive tailMap(Object fromKey, boolean inclusive) {
        return FACTORY.tailMap(fromKey, inclusive);
    }
    
    public static SubMapByFromKeyAndFromInclusiveAndToKeyAndToInclusive subMap(
            Object fromKey, boolean fromInclusive,
            Object toKey, boolean toInclusive) {
        return FACTORY.subMap(fromKey, fromInclusive, toKey, toInclusive);
    }
    
    public static DescendingMap descendingMap() {
        return FACTORY.descendingMap();
    }
    
    public static NavigableKeySet navigableKeySet() {
        return FACTORY.navigableKeySet();
    }
    
    public static DescendingKeySet descendingKeySet() {
        return FACTORY.descendingKeySet();
    }
    
    public static FirstEntry firstEntry() {
        return FACTORY.firstEntry();
    }
    
    public static LastEntry lastEntry() {
        return FACTORY.lastEntry();
    }
    
    public static FloorEntryByKey floorEntry(Object key) {
        return FACTORY.floorEntry(key);
    }
    
    public static CeilingEntryByKey ceilingEntry(Object key) {
        return FACTORY.ceilingEntry(key);
    }
    
    public static LowerEntryByKey lowerEntry(Object key) {
        return FACTORY.lowerEntry(key);
    }
    
    public static HigherEntryByKey higherEntry(Object key) {
        return FACTORY.higherEntry(key);
    }
    
    @Parameters("toKey, inclusive")
    public interface HeadMapByToKeyAndInclusive extends HeadMapByToKey {
        
        boolean isInclusive();
        
    }
    
    @Parameters("fromKey, inclusive")
    public interface TailMapByFromKeyAndInclusive extends TailMapByFromKey {
        
        boolean isInclusive();
        
    }
    
    @Parameters("fromKey, fromInclusive, toKey, toInclusive")
    public interface SubMapByFromKeyAndFromInclusiveAndToKeyAndToInclusive 
    extends SubMapByFromKeyAndToKey {

        boolean isFromInclusive();
        
        boolean isToInclusive();
        
    }
    
    @Parameters
    public interface DescendingMap extends ViewInfo {
        
    }
    
    @Parameters
    public interface NavigableKeySet extends KeySet {
        
    }
    
    @Parameters
    public interface DescendingKeySet extends ViewInfo {
        
    }
    
    @Parameters
    public interface FirstEntry extends Entry {
        
    }
    
    @Parameters
    public interface LastEntry extends Entry {
        
    }
    
    @Parameters("key")
    public interface FloorEntryByKey extends Entry {
        
        Object getKey();
        
    }
    
    @Parameters("key")
    public interface CeilingEntryByKey extends Entry {
        
        Object getKey();
        
    }
    
    @Parameters("key")
    public interface LowerEntryByKey extends Entry {
        
        Object getKey();
        
    }
    
    @Parameters("key")
    public interface HigherEntryByKey extends Entry {
        
        Object getKey();
        
    }
    
    private interface Factory {
        
        HeadMapByToKeyAndInclusive headMap(Object toKey, boolean inclusive);
        
        TailMapByFromKeyAndInclusive tailMap(Object fromKey, boolean inclusive);
        
        SubMapByFromKeyAndFromInclusiveAndToKeyAndToInclusive subMap(
                Object fromKey, boolean fromInclusive,
                Object toKey, boolean toInclusive);
        
        DescendingMap descendingMap();
        
        NavigableKeySet navigableKeySet();
        
        DescendingKeySet descendingKeySet();
        
        FirstEntry firstEntry();
        
        LastEntry lastEntry();
        
        FloorEntryByKey floorEntry(Object key);
        
        CeilingEntryByKey ceilingEntry(Object key);
        
        LowerEntryByKey lowerEntry(Object key);
        
        HigherEntryByKey higherEntry(Object key);
        
    }
    
}
