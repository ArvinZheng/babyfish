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
package org.babyfish.persistence.model.metadata;

import java.util.Map;

import org.babyfish.model.metadata.ObjectModelMetadata;

/**
 * @author Tao Chen
 */
public interface JPAObjectModelMetadata extends ObjectModelMetadata {
    
    @Override
    JPAObjectModelMetadata getSuperMetadata();
    
    JPAScalarProperty getDeclaredEntityIdProperty();
    
    JPAScalarProperty getDeclaredOptimisticLockProperty();
    
    @Override
    JPAProperty getDeclaredProperty(String name);
    
    @Override
    JPAScalarProperty getDeclaredScalarProperty(String name);
    
    @Override
    JPAAssociationProperty getDeclaredAssociationProperty(String name);
    
    @Override
    JPAProperty getDeclaredProperty(int id);
    
    @Override
    JPAScalarProperty getDeclaredScalarProperty(int id);
    
    @Override
    JPAAssociationProperty getDeclaredAssociationProperty(int id);
    
    JPAScalarProperty getEntityIdProperty();
    
    JPAScalarProperty getOptimisticLockProperty();
    
    @Override
    JPAProperty getProperty(String name);
    
    @Override
    JPAScalarProperty getScalarProperty(String name);
    
    @Override
    JPAAssociationProperty getAssociationProperty(String name);
    
    @Override
    JPAProperty getProperty(int id);
    
    @Override
    JPAScalarProperty getScalarProperty(int id);
    
    @Override
    JPAAssociationProperty getAssociationProperty(int id);
    
    Map<String, JPAProperty> getDeclaredMappingSources();
    
    Map<String, JPAAssociationProperty> getDeclaredIndexMappingSources();
    
    Map<String, JPAAssociationProperty> getDeclaredKeyMappingSources();
    
    Map<String, JPAProperty> getMappingSources();
    
    Map<String, JPAAssociationProperty> getIndexMappingSources();
    
    Map<String, JPAAssociationProperty> getKeyMappingSources();
}
