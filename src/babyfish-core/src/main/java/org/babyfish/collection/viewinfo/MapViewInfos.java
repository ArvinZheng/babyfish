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
import org.babyfish.view.ViewInfos;

/**
 * @author Tao Chen
 */
public class MapViewInfos extends ViewInfos {

    private static final Factory FACTORY = getViewInfoFactory(Factory.class);
    
    protected MapViewInfos() {
        
    }
    
    public static EntrySet entrySet() {
        return FACTORY.entrySet();
    }
    
    public static KeySet keySet() {
        return FACTORY.keySet();
    }
    
    public static Values values() {
        return FACTORY.values();
    }
    
    public static Entry entry() {
        return FACTORY.entry();
    }
    
    public static RealByKey real(Object key) {
        return FACTORY.real(key);
    }
    
    @Parameters
    public interface EntrySet extends ViewInfo {
        
    }
    
    @Parameters
    public interface KeySet extends ViewInfo {
        
    }
    
    @Parameters
    public interface Values extends ViewInfo {
        
    }
    
    @Parameters
    public interface Entry extends ViewInfo {
        
    }
    
    @Parameters("key")
    public interface RealByKey extends ViewInfo {
        Object getKey();
    }
    
    private interface Factory {
        
        EntrySet entrySet();
        
        KeySet keySet();
        
        Values values();
        
        Entry entry();
        
        RealByKey real(Object key);
    }
}
