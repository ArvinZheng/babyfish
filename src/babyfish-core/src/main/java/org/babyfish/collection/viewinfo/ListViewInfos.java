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
public class ListViewInfos extends CollectionViewInfos {

    private static final Factory FACTORY = getViewInfoFactory(Factory.class);
    
    protected ListViewInfos() {
        
    }
    
    public static ListIteratorByIndex listIterator() {
        return FACTORY.listIterator(0);
    }
    
    public static ListIteratorByIndex listIterator(int index) {
        return FACTORY.listIterator(index);
    }
    
    public static SubList subList(int fromIndex, int toIndex) {
        return FACTORY.subList(fromIndex, toIndex);
    }
    
    @Parameters("index")
    public interface ListIteratorByIndex extends ViewInfo, Iterator {
        
        int getIndex();
        
    }
    
    @Parameters("fromIndex, toIndex")
    public interface SubList extends ViewInfo {
        
        int getFromIndex();
        
        int getToIndex();
    }
    
    private interface Factory {
        
        ListIteratorByIndex listIterator(int index);
        
        SubList subList(int fromIndex, int toIndex);
        
    }
}
