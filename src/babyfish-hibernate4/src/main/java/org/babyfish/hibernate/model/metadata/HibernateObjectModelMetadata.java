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
package org.babyfish.hibernate.model.metadata;

import java.util.List;
import java.util.Map;

import org.babyfish.persistence.model.metadata.JPAAssociationProperty;
import org.babyfish.persistence.model.metadata.JPAObjectModelMetadata;
import org.babyfish.persistence.model.metadata.JPAProperty;
import org.hibernate.SessionFactory;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.Type;

/**
 * @author Tao Chen
 */
public interface HibernateObjectModelMetadata extends JPAObjectModelMetadata {
    
    @Override
    HibernateObjectModelMetadata getSuperMetadata();
    
    @Override
    HibernateScalarProperty getDeclaredEntityIdProperty();
    
    @Override
    HibernateScalarProperty getDeclaredOptimisticLockProperty();
    
    @Override
    HibernateProperty getDeclaredProperty(String name);
    
    @Override
    HibernateScalarProperty getDeclaredScalarProperty(String name);
    
    @Override
    HibernateAssociationProperty getDeclaredAssociationProperty(String name);
    
    @Override
    HibernateProperty getDeclaredProperty(int id);
    
    @Override
    HibernateScalarProperty getDeclaredScalarProperty(int id);
    
    @Override
    HibernateAssociationProperty getDeclaredAssociationProperty(int id);
    
    @Override
    HibernateScalarProperty getEntityIdProperty();
    
    @Override
    HibernateScalarProperty getOptimisticLockProperty();
    
    @Override
    HibernateProperty getProperty(String name);
    
    @Override
    HibernateScalarProperty getScalarProperty(String name);
    
    @Override
    HibernateAssociationProperty getAssociationProperty(String name);
    
    @Override
    HibernateProperty getProperty(int id);
    
    @Override
    HibernateScalarProperty getScalarProperty(int id);
    
    @Override
    HibernateAssociationProperty getAssociationProperty(int id);
    
    Map<String, JPAProperty> getDeclaredMappingSources();
    
    Map<String, JPAAssociationProperty> getDeclaredIndexMappingSources();
    
    Map<String, JPAAssociationProperty> getDeclaredKeyMappingSources();
    
    PersistentClass getPersistentClass(SessionFactory sessionFactory);
    
    interface PersistentClass {
        
        HibernateObjectModelMetadata getObjectModelMetadata();
        
        SessionFactory getSessionFactory();
        
        EntityPersister getEntityPersister();
        
        List<PersistentProperty> getPersistenceProperties();
        
        PersistentProperty getPersistentPropertyByJPAPropertyId(int id);
        
        PersistentProperty getPersistentPropertyByName(String name);
    
    }
    
    interface PersistentProperty {

        PersistentClass getPersistenceClass();
        
        HibernateProperty getObjectModelProperty();
        
        int getIndex();
        
        String getName();
        
        Type getType();
        
        PersistentPropertyCategory getCategory();
    }
    
    static enum PersistentPropertyCategory {
        ID,
        BASIC,
        NON_ID_EMBEDED,
        REFERENCE_INDEX,
        REFERENCE_KEY,
        REFERENCE,
        COLLECTION
    }
}
