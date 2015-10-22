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
public class SortedSetViewInfos extends CollectionViewInfos {
    
    private static final Factory FACTORY = getViewInfoFactory(Factory.class);

    protected SortedSetViewInfos() {
        
    }
    
    public static HeadSetByToElement headSet(Object toElement) {
        return FACTORY.headSet(toElement);
    }
    
    public static TailSetByFromElement tailSet(Object fromElement) {
        return FACTORY.tailSet(fromElement);
    }
    
    public static SubSetByFromElementAndToElement subSet(Object fromElement, Object toElement) {
        return FACTORY.subSet(fromElement, toElement);
    }

    @Parameters("toElement")
    public interface HeadSetByToElement extends ViewInfo {
    
        Object getToElement();
        
    }
    
    @Parameters("fromElement")
    public interface TailSetByFromElement extends ViewInfo {
        
        Object getFromElement();
        
    }
    
    @Parameters("fromElement, toElement")
    public interface SubSetByFromElementAndToElement extends ViewInfo {
        
        Object getFromElement();
        
        Object getToElement();
        
    }
    
    private interface Factory {

        HeadSetByToElement headSet(Object toElement);
        
        TailSetByFromElement tailSet(Object fromElement);
        
        SubSetByFromElementAndToElement subSet(Object fromElement, Object toElement);
        
    }
    
}
