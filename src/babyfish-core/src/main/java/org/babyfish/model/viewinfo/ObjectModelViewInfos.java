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
package org.babyfish.model.viewinfo;

import org.babyfish.immutable.Parameters;
import org.babyfish.view.ViewInfo;
import org.babyfish.view.ViewInfos;

/**
 * @author Tao Chen
 */
public class ObjectModelViewInfos extends ViewInfos {
    
    private static final Factory FACTORY = getViewInfoFactory(Factory.class);
    
    protected ObjectModelViewInfos() {
        
    }
    
    public static Scalar scalar(int scalarPropertyId) {
        return FACTORY.scalar(scalarPropertyId);
    }

    @Parameters("scalarPropertyId")
    public interface Scalar extends ViewInfo {
    
        int getScalarPropertyId();
    }
    
    private interface Factory {
        
        Scalar scalar(int scalarPropertyId);
    }
}
