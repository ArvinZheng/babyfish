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
package org.babyfish.persistence.model.metadata.spi;

import org.babyfish.model.metadata.ObjectModelMetadata;
import org.babyfish.model.metadata.spi.OwnerEqualityComparatorWritingReplacement;
import org.babyfish.persistence.model.metadata.JPAMetadatas;

/**
 * @author Tao Chen
 */
public class JPADefaultOwnerEqualityComparatorWritingReplacement extends OwnerEqualityComparatorWritingReplacement {

    private static final long serialVersionUID = 3761347965012595147L;

    public JPADefaultOwnerEqualityComparatorWritingReplacement(
            Class<?> ownerClass,
            int[] scalarPropertyIds) {
        super(ownerClass, scalarPropertyIds);
    }

    @Override
    protected ObjectModelMetadata getObjectModelMetadata(Class<?> ownerClass) {
        return JPAMetadatas.of(ownerClass);
    }

}
