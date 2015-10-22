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
public class SortedMapViewInfos extends MapViewInfos {

    private static final Factory FACTORY = getViewInfoFactory(Factory.class);

    protected SortedMapViewInfos() {
        
    }
    
    public static HeadMapByToKey headMap(Object toKey) {
        return FACTORY.headMap(toKey);
    }
    
    public static TailMapByFromKey tailMap(Object fromKey) {
        return FACTORY.tailMap(fromKey);
    }
    
    public static SubMapByFromKeyAndToKey subMap(Object fromKey, Object toKey) {
        return FACTORY.subMap(fromKey, toKey);
    }

    @Parameters("toKey")
    public interface HeadMapByToKey extends ViewInfo {
    
        Object getToKey();
        
    }
    
    @Parameters("fromKey")
    public interface TailMapByFromKey extends ViewInfo {
        
        Object getFromKey();
        
    }
    
    @Parameters("fromKey, toKey")
    public interface SubMapByFromKeyAndToKey extends ViewInfo {
        
        Object getFromKey();
        
        Object getToKey();
        
    }
    
    private interface Factory {

        HeadMapByToKey headMap(Object toKey);
        
        TailMapByFromKey tailMap(Object fromKey);
        
        SubMapByFromKeyAndToKey subMap(Object fromKey, Object toKey);
        
    }
    
}
