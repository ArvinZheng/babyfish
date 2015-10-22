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
package org.babyfish.model.metadata.spi;

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.babyfish.lang.Arguments;
import org.babyfish.model.metadata.Metadatas;
import org.babyfish.model.metadata.ObjectModelMetadata;

/**
 * @author Tao Chen
 */
public class OwnerComparatorWritingReplacement implements Serializable {
    
    private static final long serialVersionUID = -5612556363201452040L;

    protected Class<?> ownerClass;
    
    protected int[] scalarPropertyIds;

    public OwnerComparatorWritingReplacement(Class<?> ownerClass, int[] scalarPropertyIds) {
        this.ownerClass = Arguments.mustNotBeNull("ownerClass", ownerClass);
        this.scalarPropertyIds = Arguments.mustNotBeNull("scalarPropertyIds", scalarPropertyIds).clone();
    }
    
    protected final Object readResolve() throws ObjectStreamException {
        return this
                .getObjectModelMetadata(this.ownerClass)
                .getOwnerComparator(this.scalarPropertyIds);
    }
    
    protected ObjectModelMetadata getObjectModelMetadata(Class<?> ownerClass) {
        return Metadatas.of(ownerClass);
    }
}
