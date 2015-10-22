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
package org.babyfish.model;

import org.babyfish.association.AssociatedEndpoint;
import org.babyfish.collection.FrozenContext;
import org.babyfish.model.event.ScalarModificationAware;
import org.babyfish.model.metadata.ObjectModelMetadata;

/**
 * @author Tao Chen
 */
public interface ObjectModel extends ScalarModificationAware {
    
    <D extends ObjectModelMetadata> D getObjectModelMetadata();
    
    ObjectModelFactory<?> getObjectModelFactory();
    
    Object getOwner();
    
    <O, E> AssociatedEndpoint<O, E> getAssociation(int associationPropertyId);
    
    Object getScalar(int scalarPropertyId);
    
    void setScalar(int scalarPropertyId, Object value);
    
    void freezeScalar(int scalarPropertyId, FrozenContext<?> ctx);
    
    void unfreezeScalar(int scalarPropertyId, FrozenContext<?> ctx);
    
    boolean isDisabled(int propertyId);
    
    void disable(int propertyId);
    
    void enable(int propertyId);
    
    boolean isUnloaded(int propertyId);
    
    void unload(int propertyId);
    
    void load(int ... scalarPropertyIds);
    
    boolean isLoading();
}
