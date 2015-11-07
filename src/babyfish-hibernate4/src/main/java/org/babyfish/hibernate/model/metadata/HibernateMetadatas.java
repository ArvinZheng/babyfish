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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.babyfish.collection.HashMap;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.MACollections;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.lang.reflect.PropertyInfo;
import org.babyfish.model.metadata.AssociationProperty;
import org.babyfish.model.metadata.ObjectModelMode;
import org.babyfish.model.metadata.ScalarProperty;
import org.babyfish.model.metadata.spi.AbstractWrapperMetadatas;
import org.babyfish.persistence.model.metadata.IndexMapping;
import org.babyfish.persistence.model.metadata.JPAAssociationProperty;
import org.babyfish.persistence.model.metadata.JPAMetadatas;
import org.babyfish.persistence.model.metadata.JPAObjectModelMetadata;
import org.babyfish.persistence.model.metadata.JPAProperty;
import org.babyfish.persistence.model.metadata.JPAScalarProperty;
import org.babyfish.persistence.model.metadata.KeyMapping;
import org.babyfish.persistence.model.metadata.LazyBehavior;
import org.babyfish.persistence.model.metadata.Mapping;
import org.babyfish.util.LazyResource;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.proxy.HibernateProxy;

/**
 * @author Tao Chen
 */
public class HibernateMetadatas extends AbstractWrapperMetadatas {
    
    private static final long serialVersionUID = -8219409173458921806L;
    
    private static final HibernateMetadatas INSTANCE = getInstance(HibernateMetadatas.class);
    
    private static final LazyResource<Resource> LAZY_RESOURCE = LazyResource.of(Resource.class);
    
    private static final Map<String, JPAAssociationProperty> ROLE_ASSOCIATION_CACHE =
        new HashMap<String, JPAAssociationProperty>();
    
    private static final ReadWriteLock ROLE_ASSOCIATION_CACHE_LOCK = 
        new ReentrantReadWriteLock();

    protected HibernateMetadatas() {
        
    }
    
    public static HibernateObjectModelMetadata of(Class<?> rawClass) {
        return (HibernateObjectModelMetadata)INSTANCE.getWrapperObjectModelMetadataImpl(rawClass);
    }
    
    public static HibernateObjectModelMetadata of(String entityName) {
        try {
            return of(Class.forName(entityName));
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("entityName: " + entityName);
        }
    }
    
    public static JPAAssociationProperty ofRole(String role) {
        JPAAssociationProperty property = null;
        Lock lock;
        
        (lock = ROLE_ASSOCIATION_CACHE_LOCK.readLock()).lock();
        try {
            property = ROLE_ASSOCIATION_CACHE.get(role); //1st reading
        } finally {
            lock.unlock();
        }
        
        if (property == null) { //1st checking
            (lock = ROLE_ASSOCIATION_CACHE_LOCK.writeLock()).lock();
            try {
                property = ROLE_ASSOCIATION_CACHE.get(role); //2nd reading
                if (property == null) { //2nd checking
                    int lastIndexOfDot = role.lastIndexOf('.');
                    String ownerClassName = role.substring(0, lastIndexOfDot);
                    String hibernatePropertyName = role.substring(lastIndexOfDot + 1);
                    HibernateObjectModelMetadata objectModelMetadata = 
                        HibernateMetadatas.of(ownerClassName);
                    property =
                        (JPAAssociationProperty)
                        objectModelMetadata
                        .getDeclaredMappingSources()
                        .get(hibernatePropertyName);
                    ROLE_ASSOCIATION_CACHE.put(role, property);
                }
            } finally {
                lock.unlock();
            } 
        }
        
        return property;
    }

    @Override
    protected AbstractWrapperObjectModelMetadataImpl createWrapperObjectModelMetadataImpl(
            Class<?> rawClass, PreContext context) {
        if (HibernateProxy.class.isAssignableFrom(rawClass)) {
            throw new IllegalProgramException(
                    LAZY_RESOURCE.get().entityClassIsHibernateProxy(
                            rawClass.getName(), 
                            HibernateProxy.class.getName()));
        }
        return new HibernateObjectModelMetadataImpl(
                JPAMetadatas.of(rawClass), 
                context);
    }
    
    private static class HibernateObjectModelMetadataImpl 
    extends AbstractWrapperObjectModelMetadataImpl
    implements HibernateObjectModelMetadata {
        
        private static final long serialVersionUID = -7593678549095034523L;
        
        private Map<SessionFactory, PersistentClass> persistenceClassMap =
                new WeakHashMap<SessionFactory, PersistentClass>();
        
        private ReadWriteLock persistenceClassMapLock = 
                new ReentrantReadWriteLock();
        
        private HibernateScalarProperty declaredEntityIdProperty;
        
        private HibernateScalarProperty declaredOptimisticLockProperty;
        
        private Map<String, HibernateProperty> declaredMappingSources;
        
        private Map<String, HibernateAssociationProperty> declaredIndexMappingSources;
        
        private Map<String, HibernateAssociationProperty> declaredKeyMappingSources;
        
        private Map<String, HibernateProperty> mappingSources;
        
        private Map<String, HibernateAssociationProperty> indexMappingSources;
        
        private Map<String, HibernateAssociationProperty> keyMappingSources;
        
        protected HibernateObjectModelMetadataImpl(
                JPAObjectModelMetadata baseObjectModelMetadata, 
                PreContext context) {
            super(baseObjectModelMetadata, context);
        }

        @Override
        public HibernateObjectModelMetadata getSuperMetadata() {
            return (HibernateObjectModelMetadata)this.superMetadata();
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public Map<String, JPAProperty> getDeclaredMappingSources() {
            return (Map)this.declaredMappingSources;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public Map<String, JPAAssociationProperty> getDeclaredIndexMappingSources() {
            return (Map)this.declaredIndexMappingSources;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public Map<String, JPAAssociationProperty> getDeclaredKeyMappingSources() {
            return (Map)this.declaredKeyMappingSources;
        }
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public Map<String, JPAProperty> getMappingSources() {
            Map<String, HibernateProperty> mappingSources = this.mappingSources;
            if (mappingSources == null) {
                JPAObjectModelMetadata superMetadata = this.getSuperMetadata();
                if (superMetadata == null) {
                    this.mappingSources = mappingSources = this.declaredMappingSources;
                } else {
                    mappingSources = new LinkedHashMap<>(
                            ((superMetadata.getMappingSources().size() + this.declaredMappingSources.size()) * 4 + 2)/ 3);
                    mappingSources.putAll((Map)superMetadata.getMappingSources());
                    mappingSources.putAll(this.declaredMappingSources);
                    this.mappingSources = mappingSources = MACollections.unmodifiable(mappingSources);
                }
            }
            return (Map)mappingSources;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public Map<String, JPAAssociationProperty> getIndexMappingSources() {
            Map<String, HibernateAssociationProperty> indexMappingSources = this.indexMappingSources;
            if (indexMappingSources == null) {
                JPAObjectModelMetadata superMetadata = this.getSuperMetadata();
                if (superMetadata == null) {
                    this.indexMappingSources = indexMappingSources = this.declaredIndexMappingSources;
                } else {
                    indexMappingSources = new LinkedHashMap<>(
                            ((superMetadata.getIndexMappingSources().size() + this.declaredIndexMappingSources.size()) * 4 + 2)/ 3);
                    indexMappingSources.putAll((Map)superMetadata.getIndexMappingSources());
                    indexMappingSources.putAll(this.declaredIndexMappingSources);
                    this.indexMappingSources = indexMappingSources = MACollections.unmodifiable(indexMappingSources);
                }
            }
            return (Map)indexMappingSources;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public Map<String, JPAAssociationProperty> getKeyMappingSources() {
            Map<String, HibernateAssociationProperty> keyMappingSources = this.keyMappingSources;
            if (keyMappingSources == null) {
                JPAObjectModelMetadata superMetadata = this.getSuperMetadata();
                if (superMetadata == null) {
                    this.keyMappingSources = keyMappingSources = this.declaredKeyMappingSources;
                } else {
                    keyMappingSources = new LinkedHashMap<>(
                            ((superMetadata.getKeyMappingSources().size() + this.declaredKeyMappingSources.size()) * 4 + 2)/ 3);
                    keyMappingSources.putAll((Map)superMetadata.getKeyMappingSources());
                    keyMappingSources.putAll(this.declaredKeyMappingSources);
                    this.keyMappingSources = keyMappingSources = MACollections.unmodifiable(keyMappingSources);
                }
            }
            return (Map)keyMappingSources;
        }

        @Override
        public HibernateScalarProperty getDeclaredEntityIdProperty() {
            return this.declaredEntityIdProperty;
        }

        @Override
        public HibernateScalarProperty getDeclaredOptimisticLockProperty() {
            return this.declaredOptimisticLockProperty;
        }

        @Override
        public HibernateProperty getDeclaredProperty(String name) {
            return (HibernateProperty)this.declaredProperty(name);
        }

        @Override
        public HibernateScalarProperty getDeclaredScalarProperty(String name) {
            return (HibernateScalarProperty)this.declaredScalarProperty(name);
        }

        @Override
        public HibernateAssociationProperty getDeclaredAssociationProperty(String name) {
            return (HibernateAssociationProperty)this.declaredAssociationProperty(name);
        }

        @Override
        public HibernateProperty getDeclaredProperty(int id) {
            return (HibernateProperty)this.declaredProperty(id);
        }

        @Override
        public HibernateScalarProperty getDeclaredScalarProperty(int id) {
            return (HibernateScalarProperty)this.declaredScalarProperty(id);
        }

        @Override
        public HibernateAssociationProperty getDeclaredAssociationProperty(int id) {
            return (HibernateAssociationProperty)this.declaredAssociationProperty(id);
        }

        @Override
        public HibernateScalarProperty getEntityIdProperty() {
            if (this.declaredEntityIdProperty != null) { 
                return this.declaredEntityIdProperty; 
            }
            HibernateObjectModelMetadata superMetadata = this.getSuperMetadata();
            if (superMetadata != null) {
                return superMetadata.getEntityIdProperty();
            }
            return null;
        }

        @Override
        public HibernateScalarProperty getOptimisticLockProperty() {
            if (this.declaredOptimisticLockProperty != null) {
                return this.declaredOptimisticLockProperty;
            }
            HibernateObjectModelMetadata superMetadata = this.getSuperMetadata();
            if (superMetadata != null) {
                return superMetadata.getOptimisticLockProperty();
            }
            return null;
        }

        @Override
        public HibernateProperty getProperty(String name) {
            return (HibernateProperty)this.property(name);
        }

        @Override
        public HibernateScalarProperty getScalarProperty(String name) {
            return (HibernateScalarProperty)this.scalarProperty(name);
        }

        @Override
        public HibernateAssociationProperty getAssociationProperty(String name) {
            return (HibernateAssociationProperty)this.associationProperty(name);
        }

        @Override
        public HibernateProperty getProperty(int id) {
            return (HibernateProperty)this.property(id);
        }

        @Override
        public HibernateScalarProperty getScalarProperty(int id) {
            return (HibernateScalarProperty)this.scalarProperty(id);
        }

        @Override
        public HibernateAssociationProperty getAssociationProperty(int id) {
            return (HibernateAssociationProperty)this.associationProperty(id);
        }
        
        @Override
        public PersistentClass getPersistentClass(SessionFactory sessionFactory) {
            
            if (this.getMode() != ObjectModelMode.REFERENCE) {
                return null;
            }
            
            PersistentClass persistentClass = null;
            Map<SessionFactory, PersistentClass> map = this.persistenceClassMap;
            Lock lock;
            
            (lock = this.persistenceClassMapLock.readLock()).lock();
            
            try {
                persistentClass = map.get(sessionFactory); //1st reading
            } finally {
                lock.unlock();
            }
            
            //GC for closed sessionFactory
            if (sessionFactory.isClosed()) {
                if (persistentClass != null) {
                    (lock = this.persistenceClassMapLock.writeLock()).lock();
                    try {
                        map.remove(sessionFactory);
                    } finally {
                        lock.unlock();
                    }
                }
                throw new IllegalArgumentException(LAZY_RESOURCE.get().sessionFactoryIsClosed(SessionFactory.class));
            }
            
            if (persistentClass == null) { //1st checking
                (lock = this.persistenceClassMapLock.writeLock()).lock();
                try {
                    persistentClass = map.get(sessionFactory);
                    if (persistentClass == null) {
                        persistentClass = this.createPersistentClass(sessionFactory);
                        if (persistentClass == null) {
                            throw new IllegalProgramException(
                                    LAZY_RESOURCE.get().createPersistentClassCanNotReturnNull(
                                            this.getClass(),
                                            "createPersistenceClass",
                                            SessionFactory.class)
                            );
                        }
                        //GC for closed sessionFactory
                        Iterator<SessionFactory> iterator = map.keySet().iterator();
                        while (iterator.hasNext()) {
                            SessionFactory sf = iterator.next();
                            if (sf.isClosed()) {
                                iterator.remove();
                            }
                        }
                        map.put(sessionFactory, persistentClass);
                    }
                } finally {
                    lock.unlock();
                }
            }
            return persistentClass;
        }
        
        @Override
        protected WrapperScalarPropertyImpl createWrapperScalarPropertyImpl(
                ScalarProperty baseScalarProperty, PreContext context) {
            return new HibernateScalarPropertyImpl(baseScalarProperty, context);
        }

        @Override
        protected WrapperAssociationPropertyImpl createWrapperAssociationPropertyImpl(
                AssociationProperty baseAssociationProperty, PreContext context) {
            return new HibernateAssociationPropertyImpl(baseAssociationProperty, context);
        }

        @Override
        protected boolean afterParsing(PostContext context) {
            JPAObjectModelMetadata baseObjectModelMetadata = 
                    context.getBaseObjectMetadata(this.getOwnerClass());
            JPAScalarProperty baseDeclaredEntityIdProperty = 
                    baseObjectModelMetadata.getDeclaredEntityIdProperty();
            if (baseDeclaredEntityIdProperty != null) {
                this.declaredEntityIdProperty = 
                        (HibernateScalarProperty)
                        context
                        .getWrapperProperty(baseDeclaredEntityIdProperty);
            }
            JPAScalarProperty baseDeclaredOptimisticLockProperty = 
                    baseObjectModelMetadata.getDeclaredOptimisticLockProperty();
            if (baseDeclaredOptimisticLockProperty != null) {
                this.declaredOptimisticLockProperty = 
                        (HibernateScalarProperty)
                        context
                        .getWrapperProperty(baseDeclaredOptimisticLockProperty);
            }
            Map<String, HibernateProperty> declaredMappingSources =
                    new LinkedHashMap<>((baseObjectModelMetadata.getDeclaredMappingSources().size() * 4 + 2) / 3);
            for (Entry<String, JPAProperty> entry : baseObjectModelMetadata.getDeclaredMappingSources().entrySet()) {
                declaredMappingSources.put(
                        entry.getKey(), 
                        context.<HibernateProperty>getWrapperProperty(entry.getValue()));
            }
            this.declaredMappingSources = MACollections.unmodifiable(declaredMappingSources);
            
            Map<String, HibernateAssociationProperty> declaredIndexMappingSources =
                    new LinkedHashMap<>((baseObjectModelMetadata.getDeclaredIndexMappingSources().size() * 4 + 2) / 3);
            for (Entry<String, JPAAssociationProperty> entry : baseObjectModelMetadata.getDeclaredIndexMappingSources().entrySet()) {
                declaredIndexMappingSources.put(
                        entry.getKey(), 
                        context.<HibernateAssociationProperty>getWrapperProperty(entry.getValue()));
            }
            this.declaredIndexMappingSources = MACollections.unmodifiable(declaredIndexMappingSources);
            
            Map<String, HibernateAssociationProperty> declaredKeyMappingSources =
                    new LinkedHashMap<>((baseObjectModelMetadata.getDeclaredKeyMappingSources().size() * 4 + 2) / 3);
            for (Entry<String, JPAAssociationProperty> entry : baseObjectModelMetadata.getDeclaredKeyMappingSources().entrySet()) {
                declaredKeyMappingSources.put(
                        entry.getKey(), 
                        context.<HibernateAssociationProperty>getWrapperProperty(entry.getValue()));
            }
            this.declaredKeyMappingSources = MACollections.unmodifiable(declaredKeyMappingSources);
            
            return false;
        }

        protected PersistentClass createPersistentClass(final SessionFactory sessionFactory) {
            return this.new PeristenceClassImpl(sessionFactory);
        }
        
        private class PeristenceClassImpl implements PersistentClass {

            private SessionFactory sessionFactory;
            
            private PersistentProperty persistentIdProperty;
            
            private List<PersistentProperty> persistenceProperties;
            
            private transient Map<Integer, PersistentProperty> jpaPersistentMap;
            
            private transient Map<String, PersistentProperty> persistentMap;
            
            private EntityPersister entityPersister;
            
            PeristenceClassImpl(SessionFactory sessionFactory) {
                HibernateObjectModelMetadataImpl owner = HibernateObjectModelMetadataImpl.this;
                this.sessionFactory = sessionFactory;
                this.entityPersister = ((SessionFactoryImplementor)sessionFactory).getEntityPersister(HibernateObjectModelMetadataImpl.this.getOwnerClass().getName());
                Class<?> ownerClass = owner.getOwnerClass();
                ClassMetadata classMetadata = sessionFactory.getClassMetadata(ownerClass);
                String[] propertyNames = classMetadata.getPropertyNames();
                org.hibernate.type.Type[] propertyTypes = classMetadata.getPropertyTypes();
                boolean[] propertyLaziness = classMetadata.getPropertyLaziness();
                this.persistentIdProperty = this.new PersistencePropertyImpl(
                        owner.getDeclaredEntityIdProperty(), 
                        -1, 
                        classMetadata.getIdentifierPropertyName(), 
                        PersistentPropertyCategory.ID);
                PersistentProperty[] arr = new PersistentProperty[propertyNames.length];
                HibernateObjectModelMetadata superObjectModelMetadata = HibernateObjectModelMetadataImpl.this.getSuperMetadata();
                List<PersistentProperty> superPersistenceProperties = 
                        superObjectModelMetadata != null && superObjectModelMetadata.getMode() == ObjectModelMode.REFERENCE ?
                                superObjectModelMetadata.getPersistentClass(sessionFactory).getPersistenceProperties() :
                                null;
                for (int i = arr.length - 1; i >= 0; i--) {
                    String name = propertyNames[i];
                    if (superPersistenceProperties != null) {
                        if (superObjectModelMetadata.getMappingSources().containsKey(name) ||
                                superObjectModelMetadata.getIndexMappingSources().containsKey(name) ||
                                superObjectModelMetadata.getKeyMappingSources().containsKey(name)) {
                            arr[i] = superPersistenceProperties.get(i);
                            continue;
                        }
                    }
                    HibernateProperty hibernateProperty;
                    PersistentPropertyCategory category;
                    if ((hibernateProperty = (HibernateProperty)owner.getDeclaredIndexMappingSources().get(name)) != null) {
                        category = PersistentPropertyCategory.REFERENCE_INDEX;
                    } else if ((hibernateProperty = (HibernateProperty)owner.getDeclaredKeyMappingSources().get(name)) != null) {
                        category = PersistentPropertyCategory.REFERENCE_KEY;
                    } else {
                        hibernateProperty = (HibernateProperty)owner.getMappingSources().get(name);
                        if (hibernateProperty == null) {
                            throw new IllegalProgramException(
                                    LAZY_RESOURCE.get().illegalJPAProperty(
                                            name,
                                            ownerClass,
                                            owner.getObjectModelClass(),
                                            Mapping.class,
                                            IndexMapping.class,
                                            KeyMapping.class
                                    )
                            );
                        }
                        if (hibernateProperty instanceof AssociationProperty) {
                            if (!propertyTypes[i].isAssociationType()) {
                                throw new IllegalProgramException(
                                        LAZY_RESOURCE.get().persistentPropertyMustBeAssociation(
                                                hibernateProperty.getDeclaringObjectModelMetadata().getObjectModelClass(),
                                                hibernateProperty.getName(),
                                                hibernateProperty.getDeclaringObjectModelMetadata().getOwnerClass(),
                                                name)
                                );
                            }
                            if (((AssociationProperty)hibernateProperty).getAssociatedEndpointType().isCollection()) {
                                if (!propertyTypes[i].isCollectionType()) {
                                    throw new IllegalProgramException(
                                            LAZY_RESOURCE.get().persistentPropertyMustBeCollection(
                                                    hibernateProperty.getDeclaringObjectModelMetadata().getObjectModelClass(),
                                                    hibernateProperty.getName(),
                                                    hibernateProperty.getDeclaringObjectModelMetadata().getOwnerClass(),
                                                    name)
                                    );
                                }
                                category = PersistentPropertyCategory.COLLECTION;
                            } else {
                                if (propertyTypes[i].isCollectionType()) {
                                    throw new IllegalProgramException(
                                            LAZY_RESOURCE.get().persistentPropertyMustNotBeCollection(
                                                    hibernateProperty.getDeclaringObjectModelMetadata().getObjectModelClass(),
                                                    hibernateProperty.getName(),
                                                    hibernateProperty.getDeclaringObjectModelMetadata().getOwnerClass(),
                                                    name)
                                    );
                                }
                                category = PersistentPropertyCategory.REFERENCE;
                            }
                        } else {
                            if (propertyTypes[i].isAssociationType()) {
                                throw new IllegalProgramException(
                                        LAZY_RESOURCE.get().persistentPropertyMustNotBeAssociation(
                                                hibernateProperty.getDeclaringObjectModelMetadata().getObjectModelClass(),
                                                hibernateProperty.getName(),
                                                hibernateProperty.getDeclaringObjectModelMetadata().getOwnerClass(),
                                                name)
                                );
                            }
                            boolean isEmbededHibernateProperty = 
                                    ((HibernateScalarProperty)hibernateProperty).getReturnObjectModelMetadata() != null &&
                                    ((HibernateScalarProperty)hibernateProperty).isEmbeded(); 
                            if (propertyTypes[i].isComponentType()) {
                                if (!isEmbededHibernateProperty) {
                                    throw new IllegalProgramException(
                                            LAZY_RESOURCE.get().persistentPropertyMustNotBeComponent(
                                                    hibernateProperty.getDeclaringObjectModelMetadata().getObjectModelClass(),
                                                    hibernateProperty.getName(),
                                                    hibernateProperty.getDeclaringObjectModelMetadata().getOwnerClass(),
                                                    name
                                            )
                                    );
                                }
                                category = PersistentPropertyCategory.NON_ID_EMBEDED;
                            } else {
                                if (isEmbededHibernateProperty) {
                                    throw new IllegalProgramException(
                                            LAZY_RESOURCE.get().persistentPropertyMustBeComponent(
                                                    hibernateProperty.getDeclaringObjectModelMetadata().getObjectModelClass(),
                                                    hibernateProperty.getName(),
                                                    hibernateProperty.getDeclaringObjectModelMetadata().getOwnerClass(),
                                                    name
                                            )
                                    );
                                }
                                boolean isDeferrable = ((HibernateScalarProperty)hibernateProperty).isDeferrable();
                                if (propertyLaziness[i]) {
                                    if (!isDeferrable) {
                                        throw new IllegalProgramException(
                                                LAZY_RESOURCE.get().persistentScalarPropertyMustNotBeLazy(
                                                        hibernateProperty.getDeclaringObjectModelMetadata().getObjectModelClass(),
                                                        hibernateProperty.getName(),
                                                        hibernateProperty.getDeclaringObjectModelMetadata().getOwnerClass(),
                                                        name
                                                )
                                        );
                                    }
                                } else {
                                    if (isDeferrable) {
                                        throw new IllegalProgramException(
                                                LAZY_RESOURCE.get().persistentScalarPropertyMustBeLazy(
                                                        hibernateProperty.getDeclaringObjectModelMetadata().getObjectModelClass(),
                                                        hibernateProperty.getName(),
                                                        hibernateProperty.getDeclaringObjectModelMetadata().getOwnerClass(),
                                                        name
                                                )
                                        );
                                    }
                                }
                                category = PersistentPropertyCategory.BASIC;
                            }
                        }
                    } 
                    arr[i] = this.new PersistencePropertyImpl(
                            hibernateProperty, 
                            i, 
                            name, 
                            category);
                }
                this.persistenceProperties = MACollections.wrap(arr);
            }

            @Override
            public HibernateObjectModelMetadata getObjectModelMetadata() {
                return HibernateObjectModelMetadataImpl.this;
            }

            @Override
            public SessionFactory getSessionFactory() {
                return this.sessionFactory;
            }
            
            @Override
            public EntityPersister getEntityPersister() {
                return this.entityPersister;
            }

            @Override
            public List<PersistentProperty> getPersistenceProperties() {
                return this.persistenceProperties;
            }
            
            @Override
            public PersistentProperty getPersistentPropertyByJPAPropertyId(int id) {
                PersistentProperty persistentIdProperty = this.persistentIdProperty;
                if (id == persistentIdProperty.getObjectModelProperty().getId()) {
                    return persistentIdProperty;
                }
                Map<Integer, PersistentProperty> map = this.jpaPersistentMap;
                if (map == null) {
                    List<PersistentProperty> persistenceProperties = this.persistenceProperties;
                    map = new LinkedHashMap<Integer, PersistentProperty>(persistenceProperties.size() + 1, 1.F);
                    for (PersistentProperty property : persistenceProperties) {
                        map.put(property.getObjectModelProperty().getId(), property);
                    }
                    map = MACollections.unmodifiable(map);
                    this.jpaPersistentMap = map;
                }
                return map.get(id);
            }
            
            public PersistentProperty getPersistentPropertyByName(String name) {
                Map<String, PersistentProperty> map = this.persistentMap;
                if (map == null) {
                    List<PersistentProperty> persistenceProperties = this.persistenceProperties;
                    map = new LinkedHashMap<String, PersistentProperty>(persistenceProperties.size() + 1, 1.F);
                    for (PersistentProperty property : persistenceProperties) {
                        map.put(property.getName(), property);
                    }
                    map = MACollections.unmodifiable(map);
                    this.persistentMap = map;
                }
                return map.get(name);
            }
            
            private class PersistencePropertyImpl implements PersistentProperty {
                
                private HibernateProperty objectModelProperty;
                
                private int index;
                
                private String name;
                
                private PersistentPropertyCategory category;
                
                private org.hibernate.type.Type type;
                
                PersistencePropertyImpl(
                        HibernateProperty objectModelProperty, 
                        int index, 
                        String name, 
                        PersistentPropertyCategory category) {
                    this.objectModelProperty = objectModelProperty;
                    this.index = index;
                    this.name = name;
                    this.category = category;
                    if (index == -1) {
                        this.type = PeristenceClassImpl.this.entityPersister.getIdentifierType();
                    } else {
                        this.type = PeristenceClassImpl.this.entityPersister.getPropertyTypes()[index];
                    }
                    this.validateAssociationAction();
                }

                @Override
                public PersistentClass getPersistenceClass() {
                    return PeristenceClassImpl.this;
                }
                
                @Override
                public HibernateProperty getObjectModelProperty() {
                    return this.objectModelProperty;
                }

                @Override
                public int getIndex() {
                    return this.index;
                }

                @Override
                public String getName() {
                    return this.name;
                }

                @Override
                public org.hibernate.type.Type getType() {
                    return this.type;
                }

                @Override
                public PersistentPropertyCategory getCategory() {
                    return this.category;
                }

                private void validateAssociationAction() {
                    SessionFactoryImplementor sfi = (SessionFactoryImplementor)PeristenceClassImpl.this.sessionFactory;
                    if (this.category == PersistentPropertyCategory.COLLECTION) {
                        boolean omInverse = ((JPAAssociationProperty)this.objectModelProperty).isInverse();
                        CollectionPersister cp = sfi.getCollectionPersister(
                                PeristenceClassImpl.this.getObjectModelMetadata().getOwnerClass().getName() + 
                                '.' + 
                                this.name);
                        if (omInverse) {
                            if (!cp.isInverse()) {
                                throw new IllegalProgramException(
                                        LAZY_RESOURCE.get().persistentPropertyMustBeInverse(
                                                HibernateObjectModelMetadataImpl.this.getObjectModelClass(), 
                                                this.objectModelProperty.getName(), 
                                                HibernateObjectModelMetadataImpl.this.getOwnerClass(), 
                                                this.name
                                        )
                                );
                            }
                        } else {
                            if (cp.isInverse()) {
                                throw new IllegalProgramException(
                                        LAZY_RESOURCE.get().persistentPropertyMustNotBeInverse(
                                                HibernateObjectModelMetadataImpl.this.getObjectModelClass(), 
                                                this.objectModelProperty.getName(), 
                                                HibernateObjectModelMetadataImpl.this.getOwnerClass(), 
                                                this.name
                                        )
                                );
                            }
                        }
                    } else if (this.category == PersistentPropertyCategory.REFERENCE ||
                            this.category == PersistentPropertyCategory.REFERENCE_INDEX ||
                            this.category == PersistentPropertyCategory.REFERENCE_KEY) {
                        if (((JPAAssociationProperty)this.objectModelProperty).isInverse()) {
                            // Checking whether opposite is collection is helpful for @OneToOne(mappedBy = "... ...")
                            if (((AssociationProperty)this.objectModelProperty).getOppositeProperty().getAssociatedEndpointType().isCollection()) {
                                if (PeristenceClassImpl.this.entityPersister.getPropertyInsertability()[this.index] ||
                                        PeristenceClassImpl.this.entityPersister.getPropertyUpdateability()[this.index]) {
                                    throw new IllegalProgramException(
                                            LAZY_RESOURCE.get().persistentPropertyMustNotBeInsertableAndUpdateable(
                                                    HibernateObjectModelMetadataImpl.this.getObjectModelClass(), 
                                                    this.objectModelProperty.getName(), 
                                                    HibernateObjectModelMetadataImpl.this.getOwnerClass(), 
                                                    this.name
                                            )
                                    );
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private static class HibernateScalarPropertyImpl
    extends WrapperScalarPropertyImpl
    implements HibernateScalarProperty {
        
        private static final long serialVersionUID = -2878093486349567814L;
        
        private PropertyInfo mappingProperty;
        
        private boolean entityId;
        
        private boolean optimisticLock;
        
        public HibernateScalarPropertyImpl(
                ScalarProperty baseScalarProperty, 
                PreContext context) {
            super(baseScalarProperty, context);
        }
        
        @Override
        public boolean isEntityId() {
            return this.entityId;
        }

        @Override
        public boolean isOptimisticLock() {
            return this.optimisticLock;
        }

        @Override
        public HibernateObjectModelMetadata getDeclaringObjectModelMetadata() {
            return (HibernateObjectModelMetadata)this.declaringWrapperObjectModelMetadata();
        }

        @Override
        public PropertyInfo getOwnerProperty() {
            return this.mappingProperty;
        }

        @Override
        protected void afterParsing(PostContext postContext) {
            JPAScalarProperty baseScalarProperty = postContext.getBaseProperty(this);
            this.entityId = baseScalarProperty.isEntityId();
            this.optimisticLock = baseScalarProperty.isOptimisticLock();
            this.mappingProperty = baseScalarProperty.getOwnerProperty();
        }

        @Override
        public HibernateObjectModelMetadata getReturnObjectModelMetadata() {
            return (HibernateObjectModelMetadata)this.returnObjectModelMetadata();
        }
    }
    
    private static class HibernateAssociationPropertyImpl 
    extends WrapperAssociationPropertyImpl
    implements HibernateAssociationProperty {
        
        private static final long serialVersionUID = -9127482356273634406L;
        
        private boolean inverse;
        
        private PropertyInfo mappingProperty;
        
        private PropertyInfo indexMappingProperty;
        
        private PropertyInfo keyMappingProperty;
        
        protected HibernateAssociationPropertyImpl(AssociationProperty baseAssociationProperty, PreContext context) {
            super(baseAssociationProperty, context);
        }

        @Override
        public HibernateObjectModelMetadata getDeclaringObjectModelMetadata() {
            return (HibernateObjectModelMetadata)this.declaringWrapperObjectModelMetadata();
        }
        
        @Override
        public HibernateAssociationProperty getOppositeProperty() {
            return (HibernateAssociationProperty)this.oppositeProperty();
        }

        @Override
        public HibernateAssociationProperty getCovarianceProperty() {
            return (HibernateAssociationProperty)this.covarianceProperty();
        }

        @Override
        public HibernateObjectModelMetadata getKeyObjectModelMetadata() {
            return (HibernateObjectModelMetadata)this.keyObjectModelMetadata();
        }

        @Override
        public HibernateObjectModelMetadata getReturnObjectModelMetadata() {
            return (HibernateObjectModelMetadata)this.returnObjectModelMetadata();
        }

        @Override
        public PropertyInfo getOwnerProperty() {
            return this.mappingProperty;
        }

        @Override
        public PropertyInfo getOwnerIndexProperty() {
            return this.indexMappingProperty;
        }

        @Override
        public PropertyInfo getOwnerKeyProperty() {
            return this.keyMappingProperty;
        }

        @Override
        public boolean isInverse() {
            return this.inverse;
        }

        @Override
        public LazyBehavior getLazyBehavior() {
            return ((JPAAssociationProperty)this.baseProperty).getLazyBehavior();
        }

        @Override
        protected void afterParsing(PostContext postContext) {
            JPAAssociationProperty baseAssociationProperty = postContext.getBaseProperty(this);
            this.mappingProperty = baseAssociationProperty.getOwnerProperty();
            this.indexMappingProperty = baseAssociationProperty.getOwnerIndexProperty();
            this.keyMappingProperty = baseAssociationProperty.getOwnerKeyProperty();
            this.inverse = baseAssociationProperty.isInverse();
        }
    }
    
    private interface Resource {
        
        String entityClassIsHibernateProxy(
                String associatedClassName,
                String hibernateProxyInterfaceNameConstant);

        String sessionFactoryIsClosed(Class<SessionFactory> sessionFactory);
        
        String createPersistentClassCanNotReturnNull(
                Class<? extends HibernateObjectModelMetadataImpl> thisType,
                String methodName, 
                Class<?> ... parameterTypes);
        
        String illegalJPAProperty(
                String objectModelPropertyName,
                Class<?> ownerClass,
                Class<?> objectModelClass,
                Class<Mapping> mappinType,
                Class<IndexMapping> indexMappinType,
                Class<KeyMapping> keyMappinType);
        
        String persistentScalarPropertyMustBeLazy(
                Class<?> objectModelType,
                String objectModelPropertyName,
                Class<?> entityType,
                String entityPropertyName);
        
        String persistentScalarPropertyMustNotBeLazy(
                Class<?> objectModelType,
                String objectModelPropertyName,
                Class<?> entityType,
                String entityPropertyName);
        
        String persistentPropertyMustBeComponent(
                Class<?> objectModelType,
                String objectModelPropertyName,
                Class<?> entityType,
                String entityPropertyName);
        
        String persistentPropertyMustNotBeComponent(
                Class<?> objectModelType,
                String objectModelPropertyName,
                Class<?> entityType,
                String entityPropertyName);
        
        String persistentPropertyMustBeAssociation(
                Class<?> objectModelType,
                String objectModelPropertyName,
                Class<?> entityType,
                String entityPropertyName);
        
        String persistentPropertyMustNotBeAssociation(
                Class<?> objectModelType,
                String objectModelPropertyName,
                Class<?> entityType,
                String entityPropertyName);
        
        String persistentPropertyMustBeCollection(
                Class<?> objectModelType,
                String objectModelPropertyName,
                Class<?> entityType,
                String entityPropertyName);
        
        String persistentPropertyMustNotBeCollection(
                Class<?> objectModelType,
                String objectModelPropertyName,
                Class<?> entityType,
                String entityPropertyName);
        
        String persistentPropertyMustBeInverse(
                Class<?> objectModelType,
                String objectModelPropertyName,
                Class<?> entityType,
                String entityPropertyName);
        
        String persistentPropertyMustNotBeInverse(
                Class<?> objectModelType,
                String objectModelPropertyName,
                Class<?> entityType,
                String entityPropertyName);
        
        String persistentPropertyMustNotBeInsertableAndUpdateable(
                Class<?> objectModelType,
                String objectModelPropertyName,
                Class<?> entityType,
                String entityPropertyName);
    } 
}
