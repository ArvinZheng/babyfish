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
package org.babyfish.model.metadata;

import java.lang.reflect.ParameterizedType;

import org.babyfish.association.AssociatedEndpointType;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XOrderedMap;

/**
 * @author Tao Chen
 */
public interface AssociationProperty extends Property {

    AssociationProperty getOppositeProperty();
    
    AssociationProperty getCovarianceProperty();

    AssociatedEndpointType getAssociatedEndpointType();
    
    XOrderedMap<String, ScalarProperty> getComparatorProperties();
    
    UnifiedComparator<?> getKeyUnifiedComparator();
    
    UnifiedComparator<?> getCollectionUnifiedComparator();
    
    @Override
    ParameterizedType getReturnType();
    
    Class<?> getStandardReturnClass();
    
    Class<?> getKeyClass();
    
    ObjectModelMetadata getKeyObjectModelMetadata();
    
    Class<?> getElementClass();
    
    boolean isCollection();
    
    boolean isReference();
    
}
