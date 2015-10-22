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
public class OrderedMapViewInfos extends MapViewInfos {
    
    private static final Factory FACTORY = getViewInfoFactory(Factory.class);

    protected OrderedMapViewInfos() {
        
    }
    
    public static DescendingMap descendingMap() {
        return FACTORY.descendingMap();
    }
    
    public static OrderedKeySet orderedKeySet() {
        return FACTORY.orderedKeySet();
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
    
    @Parameters
    public interface DescendingMap extends ViewInfo {
        
    }
    
    @Parameters
    public interface OrderedKeySet extends KeySet {
        
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
    
    private interface Factory {
        
        DescendingMap descendingMap();
        
        OrderedKeySet orderedKeySet();
        
        DescendingKeySet descendingKeySet();
        
        FirstEntry firstEntry();
        
        LastEntry lastEntry();
        
    }
    
}
