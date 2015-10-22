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

import java.io.Serializable;
import java.util.Comparator;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.spi.ObjectModelFactoryProvider;

/**
 * @author Tao Chen
 */
public interface ObjectModelMetadata extends Serializable {

    Class<?> getOwnerClass();
    
    Class<?> getObjectModelClass();
    
    ObjectModelMetadata getSuperMetadata();
    
    String getStaticMethodName();
    
    ObjectModelFactoryProvider getProvider();
    
    ObjectModelFactory<?> getFactory();
    
    ObjectModelMode getMode();
    
    boolean isDisabilityAllowed();
    
    int getDeclaredPropertyBaseId();
    
    Property getDeclaredProperty(String name);
    
    ScalarProperty getDeclaredScalarProperty(String name);
    
    AssociationProperty getDeclaredAssociationProperty(String name);
    
    Property getDeclaredProperty(int id);
    
    ScalarProperty getDeclaredScalarProperty(int id);
    
    AssociationProperty getDeclaredAssociationProperty(int id);
    
    XOrderedMap<String, Property> getDeclaredProperties();
    
    XOrderedMap<String, ScalarProperty> getDeclaredScalarProperties();
    
    XOrderedMap<String, AssociationProperty> getDeclaredAssociationProperties();

    Property getProperty(String name);
    
    ScalarProperty getScalarProperty(String name);
    
    AssociationProperty getAssociationProperty(String name);
    
    Property getProperty(int id);
    
    ScalarProperty getScalarProperty(int id);
    
    AssociationProperty getAssociationProperty(int id);
    
    XOrderedMap<String, Property> getProperties();
    
    XOrderedMap<String, ScalarProperty> getScalarProperties();
    
    XOrderedMap<String, AssociationProperty> getAssociationProperties();
    
    XOrderedMap<String, ScalarProperty> getComparatorProperties();
    
    <O> Comparator<O> getOwnerComparator();
    
    <O> Comparator<O> getOwnerComparator(String ... scalarPropertyNames);
    
    <O> Comparator<O> getOwnerComparator(int ... scalarPropertyIds);
    
    <O> EqualityComparator<O> getOwnerEqualityComparator();
    
    <O> EqualityComparator<O> getOwnerEqualityComparator(String ... scalarPropertyNames);
    
    <O> EqualityComparator<O> getOwnerEqualityComparator(int ... scalarPropertyIds);
}
