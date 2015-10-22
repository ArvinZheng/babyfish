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
package org.babyfish.view;

import org.babyfish.immutable.ImmutableObjects;
import org.babyfish.lang.Arguments;

/**
 * @author Tao Chen
 */
public class ViewInfos extends ImmutableObjects {

    private static final ViewInfos INSTANCE = new ViewInfos();

    protected ViewInfos() {
        
    }
    
    protected static <F> F getViewInfoFactory(Class<F> factoryType) {
        return INSTANCE.getFactory(factoryType);
    }
    
    @Override
    protected void validateInterfaceType(Class<?> interfaceType) {
        Arguments.mustBeCompatibleWithValue("interfaceType", interfaceType, ViewInfo.class);
    }
}
