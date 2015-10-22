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

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.persistence.Transient;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.FrozenContext;
import org.babyfish.collection.FrozenEqualityComparator;
import org.babyfish.collection.HashMap;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.MACollection;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.MAList;
import org.babyfish.collection.MAMap;
import org.babyfish.collection.MANavigableMap;
import org.babyfish.collection.MANavigableSet;
import org.babyfish.collection.MAOrderedMap;
import org.babyfish.collection.MAOrderedSet;
import org.babyfish.collection.MASet;
import org.babyfish.collection.MASortedMap;
import org.babyfish.collection.MASortedSet;
import org.babyfish.collection.XCollection;
import org.babyfish.collection.XList;
import org.babyfish.collection.XMap;
import org.babyfish.collection.XNavigableMap;
import org.babyfish.collection.XNavigableSet;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.collection.XSet;
import org.babyfish.collection.XSortedMap;
import org.babyfish.collection.XSortedSet;
import org.babyfish.lang.Action;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.lang.reflect.ClassInfo;
import org.babyfish.lang.reflect.GenericTypes;
import org.babyfish.lang.reflect.NoSuchPropertyInfoException;
import org.babyfish.lang.reflect.PropertyInfo;
import org.babyfish.lang.reflect.asm.ASM;
import org.babyfish.lang.reflect.asm.XMethodVisitor;
import org.babyfish.model.ObjectModel;
import org.babyfish.model.metadata.AssociationProperty;
import org.babyfish.model.metadata.Contravariance;
import org.babyfish.model.metadata.Metadatas;
import org.babyfish.model.metadata.ObjectModelMetadata;
import org.babyfish.model.metadata.ObjectModelMode;
import org.babyfish.model.metadata.Property;
import org.babyfish.model.metadata.ScalarProperty;
import org.babyfish.model.metadata.spi.AbstractWrapperMetadatas;
import org.babyfish.model.spi.ObjectModelFactoryProvider;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.Label;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.persistence.model.metadata.spi.JPADefaultOwnerEqualityComparatorWritingReplacement;
import org.babyfish.reference.Reference;
import org.babyfish.util.LazyResource;

/**
 * @author Tao Chen
 */
public class JPAMetadatas extends AbstractWrapperMetadatas {
    
    private static final long serialVersionUID = 7028574829375482316L;
    
    private static final LazyResource<Resource> LAZY_RESOURCE = LazyResource.of(Resource.class);
    
    private static final JPAMetadatas INSTANCE = getInstance(JPAMetadatas.class);
    
    private static final String NAME_POSTFIX = "92B8C17E_BF4E_4135_B596_5A76E0FEBF4E";
    
    private static final String REPLACEMENT = "{REPLACEMENT}";
    
    private static final Map<String, JPAAssociationProperty> ROLE_ASSOCIATION_CACHE =
        new HashMap<String, JPAAssociationProperty>();
    
    private static final ReadWriteLock ROLE_ASSOCIATION_CACHE_LOCK = new ReentrantReadWriteLock();

    protected JPAMetadatas() {
        
    }
    
    public static JPAObjectModelMetadata of(Class<?> ownerClass) {
        return (JPAObjectModelMetadata)INSTANCE.getWrapperObjectModelMetadataImpl(ownerClass);
    }
    
    public static JPAObjectModelMetadata of(String entityName) {
        try {
            return of(Class.forName(entityName));
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().illegalEntityName(entityName), ex);
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
                    JPAObjectModelMetadata objectModelMetadata = 
                        JPAMetadatas.of(ownerClassName);
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
        return new JPAObjectModelMetadataImpl(
                Metadatas.of(rawClass), 
                context);
    }
    
    private static class JPAObjectModelMetadataImpl 
    extends AbstractWrapperObjectModelMetadataImpl
    implements JPAObjectModelMetadata {
        
        private static final long serialVersionUID = 8078938452378452380L;
        
        private static final Object ENTITY_ID_RESOLVED_METATATAS = new Object();
        
        private static final Object OPTIMISTIC_ID_RESOLVED_METATATAS = new Object();
        
        private static final Object DEFAULT_OWNER_FROZEN_EQUALITY_COMPARATOR_RESOLVED_METATATAS = new Object();
        
        JPAScalarProperty declaredEntityIdProperty;
        
        JPAScalarProperty declaredOptimisticLockProperty;
        
        private FrozenEqualityComparator<?> defaultOwnerFrozenEqualityComparator;
        
        private Map<String, JPAProperty> declaredMappingSources;
        
        private Map<String, JPAAssociationProperty> declaredIndexMappingSources;
        
        private Map<String, JPAAssociationProperty> declaredKeyMappingSources;
        
        private Map<String, JPAProperty> mappingSources;
        
        private Map<String, JPAAssociationProperty> indexMappingSources;
        
        private Map<String, JPAAssociationProperty> keyMappingSources;

        protected JPAObjectModelMetadataImpl(
                ObjectModelMetadata baseObjectModelMetadata, PreContext context) {
            super(baseObjectModelMetadata, context);
        }

        @Override
        public JPAObjectModelMetadata getSuperMetadata() {
            return (JPAObjectModelMetadata)this.superMetadata();
        }

        @Override
        public Map<String, JPAProperty> getDeclaredMappingSources() {
            return this.declaredMappingSources;
        }

        @Override
        public Map<String, JPAAssociationProperty> getDeclaredIndexMappingSources() {
            return this.declaredIndexMappingSources;
        }

        @Override
        public Map<String, JPAAssociationProperty> getDeclaredKeyMappingSources() {
            return this.declaredKeyMappingSources;
        }

        @Override
        public JPAScalarProperty getDeclaredEntityIdProperty() {
            return this.declaredEntityIdProperty;
        }

        @Override
        public JPAScalarProperty getDeclaredOptimisticLockProperty() {
            return this.declaredOptimisticLockProperty;
        }

        @Override
        public Map<String, JPAProperty> getMappingSources() {
            Map<String, JPAProperty> mappingSources = this.mappingSources;
            if (mappingSources == null) {
                JPAObjectModelMetadata superMetadata = this.getSuperMetadata();
                if (superMetadata == null) {
                    this.mappingSources = mappingSources = this.declaredMappingSources;
                } else {
                    mappingSources = new LinkedHashMap<>(
                            ((superMetadata.getMappingSources().size() + this.declaredMappingSources.size()) * 4 + 2)/ 3);
                    mappingSources.putAll(superMetadata.getMappingSources());
                    mappingSources.putAll(this.declaredMappingSources);
                    this.mappingSources = mappingSources = MACollections.unmodifiable(mappingSources);
                }
            }
            return mappingSources;
        }

        @Override
        public Map<String, JPAAssociationProperty> getIndexMappingSources() {
            Map<String, JPAAssociationProperty> indexMappingSources = this.indexMappingSources;
            if (indexMappingSources == null) {
                JPAObjectModelMetadata superMetadata = this.getSuperMetadata();
                if (superMetadata == null) {
                    this.indexMappingSources = indexMappingSources = this.declaredIndexMappingSources;
                } else {
                    indexMappingSources = new LinkedHashMap<>(
                            ((superMetadata.getIndexMappingSources().size() + this.declaredIndexMappingSources.size()) * 4 + 2)/ 3);
                    indexMappingSources.putAll(superMetadata.getIndexMappingSources());
                    indexMappingSources.putAll(this.declaredIndexMappingSources);
                    this.indexMappingSources = indexMappingSources = MACollections.unmodifiable(indexMappingSources);
                }
            }
            return indexMappingSources;
        }

        @Override
        public Map<String, JPAAssociationProperty> getKeyMappingSources() {
            Map<String, JPAAssociationProperty> keyMappingSources = this.keyMappingSources;
            if (keyMappingSources == null) {
                JPAObjectModelMetadata superMetadata = this.getSuperMetadata();
                if (superMetadata == null) {
                    this.keyMappingSources = keyMappingSources = this.declaredKeyMappingSources;
                } else {
                    keyMappingSources = new LinkedHashMap<>(
                            ((superMetadata.getKeyMappingSources().size() + this.declaredKeyMappingSources.size()) * 4 + 2)/ 3);
                    keyMappingSources.putAll(superMetadata.getKeyMappingSources());
                    keyMappingSources.putAll(this.declaredKeyMappingSources);
                    this.keyMappingSources = keyMappingSources = MACollections.unmodifiable(keyMappingSources);
                }
            }
            return keyMappingSources;
        }

        @Override
        public JPAProperty getDeclaredProperty(String name) {
            return (JPAProperty)this.declaredProperty(name);
        }

        @Override
        public JPAScalarProperty getDeclaredScalarProperty(String name) {
            return (JPAScalarProperty)this.declaredScalarProperty(name);
        }

        @Override
        public JPAAssociationProperty getDeclaredAssociationProperty(String name) {
            return (JPAAssociationProperty)this.declaredAssociationProperty(name);
        }

        @Override
        public JPAProperty getDeclaredProperty(int id) {
            return (JPAProperty)this.declaredProperty(id);
        }

        @Override
        public JPAScalarProperty getDeclaredScalarProperty(int id) {
            return (JPAScalarProperty)this.declaredScalarProperty(id);
        }

        @Override
        public JPAAssociationProperty getDeclaredAssociationProperty(int id) {
            return (JPAAssociationProperty)this.declaredAssociationProperty(id);
        }

        @Override
        public JPAScalarProperty getEntityIdProperty() {
            if (this.declaredEntityIdProperty != null) { 
                return this.declaredEntityIdProperty; 
            }
            JPAObjectModelMetadata superMetadata = this.getSuperMetadata();
            if (superMetadata != null) {
                return superMetadata.getEntityIdProperty();
            }
            return null;
        }

        @Override
        public JPAScalarProperty getOptimisticLockProperty() {
            if (this.declaredOptimisticLockProperty != null) {
                return this.declaredOptimisticLockProperty;
            }
            JPAObjectModelMetadata superMetadata = this.getSuperMetadata();
            if (superMetadata != null) {
                return superMetadata.getOptimisticLockProperty();
            }
            return null;
        }

        @Override
        public JPAProperty getProperty(String name) {
            return (JPAProperty)this.property(name);
        }

        @Override
        public JPAScalarProperty getScalarProperty(String name) {
            return (JPAScalarProperty)this.scalarProperty(name);
        }

        @Override
        public JPAAssociationProperty getAssociationProperty(String name) {
            return (JPAAssociationProperty)this.associationProperty(name);
        }

        @Override
        public JPAProperty getProperty(int id) {
            return (JPAProperty)this.property(id);
        }

        @Override
        public JPAScalarProperty getScalarProperty(int id) {
            return (JPAScalarProperty)this.scalarProperty(id);
        }

        @Override
        public JPAAssociationProperty getAssociationProperty(int id) {
            return (JPAAssociationProperty)this.associationProperty(id);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected <O> EqualityComparator<O> getDefaultOwnerEqualityComparatorForReferenceMode() {
            return (EqualityComparator<O>)this.defaultOwnerFrozenEqualityComparator;
        }

        @Override
        protected WrapperScalarPropertyImpl createWrapperScalarPropertyImpl(
                ScalarProperty baseScalarProperty, PreContext context) {
            return new JPAScalarPropertyImpl(baseScalarProperty, context);
        }

        @Override
        protected WrapperAssociationPropertyImpl createWrapperAssociationPropertyImpl(
                AssociationProperty baseAssociationProperty, PreContext context) {
            return new JPAAssociationPropertyImpl(baseAssociationProperty, context);
        }

        @Override
        protected boolean afterParsing(PostContext postContext) {
            switch (postContext.getPhase()) {
            case 0:
                this.buildEntityId(postContext);
                this.buildOptimisticLock(postContext);
                return true;
            default:
                this.buildDeclaredMappingSourceMaps();
                this.buildDefaultOwnerFrozenEqualityComparatorForReferenceMode(postContext);
                return false;
            }
        }
        
        private void buildEntityId(PostContext postContext) {
            if (postContext.isProcessed(ENTITY_ID_RESOLVED_METATATAS, this)) {
                return;
            }
            
            JPAObjectModelMetadataImpl superMetadata = (JPAObjectModelMetadataImpl)this.superMetadata();
            if (superMetadata != null) {
                superMetadata.buildEntityId(postContext);
            }
            
            if (superMetadata != null && superMetadata.getEntityIdProperty() != null) {
                for (ScalarProperty scalarProperty : this.getDeclaredScalarProperties().values()) {
                    JPAScalarPropertyImpl jpaScalarProperty = 
                        (JPAScalarPropertyImpl)scalarProperty;
                    if (jpaScalarProperty.isEntityId()) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().entityIdHasBeenDeclaredInSuperMetadata(
                                        jpaScalarProperty.getGetterMethod(), 
                                        EntityId.class,
                                        superMetadata.getObjectModelClass()
                                )
                        );
                    }
                }
            } else {
                JPAScalarPropertyImpl entityIdPropertyImpl = null;
                for (ScalarProperty scalarProperty : this.getDeclaredScalarProperties().values()) {
                    JPAScalarPropertyImpl jpaScalarProperty = 
                        (JPAScalarPropertyImpl)scalarProperty;
                    if (jpaScalarProperty.isEntityId()) {
                        if (entityIdPropertyImpl != null) {
                            throw new IllegalProgramException(
                                    LAZY_RESOURCE.get().duplicatedEntityIdAnnotation(
                                            this.getObjectModelClass(), 
                                            entityIdPropertyImpl.getGetterMethod(), 
                                            jpaScalarProperty.getGetterMethod(), 
                                            EntityId.class
                                    )
                            );
                        }
                        entityIdPropertyImpl = jpaScalarProperty;
                    }
                }
                if (this.getMode() == ObjectModelMode.REFERENCE) {
                    if (entityIdPropertyImpl == null) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().noEntityIdAnnotation(
                                        this.getObjectModelClass(), 
                                        EntityId.class
                                )
                        );
                    }
                    if (entityIdPropertyImpl.getReturnClass().isPrimitive()) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().entityIdIsPrimitive(
                                        this.getObjectModelClass(),
                                        entityIdPropertyImpl.getGetterMethod(),
                                        EntityId.class
                                )
                        );
                    }
                }
                this.declaredEntityIdProperty = entityIdPropertyImpl;
            }
        }
        
        private void buildOptimisticLock(PostContext postContext) {
            if (postContext.isProcessed(OPTIMISTIC_ID_RESOLVED_METATATAS, this)) {
                return;
            }
            
            JPAObjectModelMetadataImpl superMetadata = (JPAObjectModelMetadataImpl)this.superMetadata();
            if (superMetadata != null) {
                superMetadata.buildOptimisticLock(postContext);
            }
            
            if (superMetadata != null && superMetadata.getOptimisticLockProperty() != null) {
                for (ScalarProperty scalarProperty : this.getDeclaredScalarProperties().values()) {
                    JPAScalarPropertyImpl jpaScalarProperty = 
                        (JPAScalarPropertyImpl)scalarProperty;
                    if (jpaScalarProperty.isOptimisticLock()) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().optimisticLockHasBeenDeclaredInSuperMetadata(
                                        jpaScalarProperty.getGetterMethod(), 
                                        OptimisticLock.class,
                                        superMetadata.getObjectModelClass()));
                    }
                }
            } else {
                JPAScalarPropertyImpl optimisticLockPropertyImpl = null;
                for (ScalarProperty scalarProperty : this.getDeclaredScalarProperties().values()) {
                    JPAScalarPropertyImpl jpaScalarProperty = 
                        (JPAScalarPropertyImpl)scalarProperty;
                    if (jpaScalarProperty.isOptimisticLock()) {
                        if (optimisticLockPropertyImpl != null) {
                            throw new IllegalProgramException(
                                    LAZY_RESOURCE.get().duplicatedOptimisticLockAnnotation(
                                            this.getObjectModelClass(), 
                                            optimisticLockPropertyImpl.getGetterMethod(), 
                                            jpaScalarProperty.getGetterMethod(), 
                                            OptimisticLock.class));
                        }
                        optimisticLockPropertyImpl = jpaScalarProperty;
                    }
                }
                this.declaredOptimisticLockProperty = optimisticLockPropertyImpl;
            }
        }
        
        private void buildDeclaredMappingSourceMaps() {
            Map<String, JPAProperty> declaredMappingSources = new LinkedHashMap<>();
            Map<String, JPAAssociationProperty> declaredIndexMappingSources = new LinkedHashMap<>();
            Map<String, JPAAssociationProperty> declaredKeyMappingSources = new LinkedHashMap<>();
            for (Property property : this.getDeclaredProperties().values()) {
                JPAProperty jpaProperty = (JPAProperty)property;
                if (jpaProperty.getOwnerProperty() != null) {
                    String name = jpaProperty.getOwnerProperty().getName();
                    JPAProperty conflictJPAProperty = declaredMappingSources.put(name, jpaProperty);
                    if (conflictJPAProperty != null) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().duplicatedJAPPropertyMapping(
                                        this.getObjectModelClass(),
                                        jpaProperty.getName(),
                                        conflictJPAProperty.getName(),
                                        Mapping.class,
                                        name
                                )
                        );
                    }
                }
                if (property instanceof JPAAssociationProperty) {
                    JPAAssociationProperty jpaAssociationProperty =
                        (JPAAssociationProperty)property;
                    if (jpaAssociationProperty.getOwnerIndexProperty() != null) {
                        String indexName = jpaAssociationProperty.getOwnerIndexProperty().getName();
                        JPAProperty conflictJPAProperty = declaredIndexMappingSources.put(indexName, jpaAssociationProperty);
                        if (conflictJPAProperty != null) {
                            throw new IllegalProgramException(
                                    LAZY_RESOURCE.get().duplicatedJAPPropertyMapping(
                                            this.getObjectModelClass(), 
                                            jpaProperty.getName(), 
                                            conflictJPAProperty.getName(),
                                            IndexMapping.class,
                                            indexName
                                    )
                            );
                        }
                    }
                    if (jpaAssociationProperty.getOwnerKeyProperty() != null) {
                        String keyName = jpaAssociationProperty.getOwnerKeyProperty().getName();
                        JPAProperty conflictJPAProperty = declaredKeyMappingSources.put(keyName, jpaAssociationProperty);
                        if (conflictJPAProperty != null) {
                            throw new IllegalProgramException(
                                    LAZY_RESOURCE.get().duplicatedJAPPropertyMapping(
                                            this.getObjectModelClass(), 
                                            jpaProperty.getName(), 
                                            conflictJPAProperty.getName(),
                                            KeyMapping.class,
                                            keyName
                                    )
                            );
                        }
                    }
                }
            }
            for (Entry<String, JPAProperty> entry : declaredMappingSources.entrySet()) {
                String name = entry.getKey();
                JPAProperty conflictProperty = declaredIndexMappingSources.get(name);
                if (conflictProperty != null) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().duplicatedJAPPropertyMappings(
                                    this.getObjectModelClass(), 
                                    entry.getValue().getName(), 
                                    Mapping.class,
                                    conflictProperty.getName(),
                                    IndexMapping.class,
                                    name
                            )
                    );
                }
            }
            for (Entry<String, JPAAssociationProperty> entry : declaredIndexMappingSources.entrySet()) {
                String name = entry.getKey();
                JPAProperty conflictProperty = declaredKeyMappingSources.get(name);
                if (conflictProperty != null) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().duplicatedJAPPropertyMappings(
                                    this.getObjectModelClass(), 
                                    entry.getValue().getName(), 
                                    IndexMapping.class,
                                    conflictProperty.getName(),
                                    KeyMapping.class,
                                    name
                            )
                    );
                }
            }
            for (Entry<String, JPAAssociationProperty> entry : declaredKeyMappingSources.entrySet()) {
                String name = entry.getKey();
                JPAProperty conflictProperty = declaredMappingSources.get(name);
                if (conflictProperty != null) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().duplicatedJAPPropertyMappings(
                                    this.getObjectModelClass(), 
                                    entry.getValue().getName(), 
                                    KeyMapping.class,
                                    conflictProperty.getName(),
                                    Mapping.class,
                                    name
                            )
                    );
                }
            }
            this.declaredMappingSources = MACollections.unmodifiable(declaredMappingSources);
            this.declaredIndexMappingSources = MACollections.unmodifiable(declaredIndexMappingSources);
            this.declaredKeyMappingSources = MACollections.unmodifiable(declaredKeyMappingSources);
        }

        private void buildDefaultOwnerFrozenEqualityComparatorForReferenceMode(PostContext postContext) {
            if (postContext.isProcessed(DEFAULT_OWNER_FROZEN_EQUALITY_COMPARATOR_RESOLVED_METATATAS, this)) {
                return;
            }
            
            JPAObjectModelMetadataImpl superMetadata = (JPAObjectModelMetadataImpl)this.superMetadata();
            if (superMetadata != null) {
                superMetadata.buildDefaultOwnerFrozenEqualityComparatorForReferenceMode(postContext);
            }
            
            if (this.getMode() == ObjectModelMode.REFERENCE) {
                if (superMetadata != null && superMetadata.getMode() == ObjectModelMode.REFERENCE) {
                    this.defaultOwnerFrozenEqualityComparator = superMetadata.defaultOwnerFrozenEqualityComparator;
                } else {
                    this.defaultOwnerFrozenEqualityComparator = 
                            new DefaultOwnerEqualityComparatorForReferenceModeGenerator(this)
                            .getFrozenEqualityComparator();
                }
            }
        }
    }
    
    private static class JPAScalarPropertyImpl
    extends WrapperScalarPropertyImpl
    implements JPAScalarProperty {
        
        private static final long serialVersionUID = -2384590689306754913L;
        
        private PropertyInfo ownerProperty;
        
        private boolean entityId;
        
        private boolean optimisticLock;
        
        public JPAScalarPropertyImpl(
                ScalarProperty baseProperty, 
                PreContext context) {
            super(baseProperty, context);
            this.processMapping();
            this.entityId = this.getGetterMethod().isAnnotationPresent(EntityId.class);
            this.optimisticLock = this.getGetterMethod().isAnnotationPresent(OptimisticLock.class);
            if (this.entityId && this.optimisticLock) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().conflictScalarAnnotation(
                                baseProperty.getDeclaringObjectModelMetadata().getObjectModelClass(),
                                this.getGetterMethod(),
                                EntityId.class,
                                OptimisticLock.class
                        )
                );
            }
        }
        
        @Override
        public JPAObjectModelMetadata getReturnObjectModelMetadata() {
            return (JPAObjectModelMetadata)this.returnObjectModelMetadata();
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
        public JPAObjectModelMetadata getDeclaringObjectModelMetadata() {
            return (JPAObjectModelMetadata)this.declaringWrapperObjectModelMetadata();
        }

        @Override
        public PropertyInfo getOwnerProperty() {
            return this.ownerProperty;
        }
        
        private void processMapping() {
            Mapping mapping = this.getGetterMethod().getAnnotation(Mapping.class);
            ClassInfo<?> classInfo = ClassInfo.of(this.getDeclaringObjectModelMetadata().getOwnerClass());
            String mappingValue = mapping != null ? mapping.value() : "";
            if (mappingValue.length() == 0) {
                mappingValue = this.getName();
            }
            PropertyInfo property;
            try {
                property = classInfo.getDeclaredErasedProperty(mappingValue);
            } catch (NoSuchPropertyInfoException ex) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().noOwnerScalarProperty(
                                this.getGetterMethod(),
                                Mapping.class,
                                mappingValue,
                                classInfo
                        )
                );
            }
            if (property.getModifiers().isStatic()) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().ownerScalarPropertyIsStatic(
                                this.getGetterMethod(),
                                Mapping.class,
                                mappingValue,
                                this.getDeclaringObjectModelMetadata().getOwnerClass()
                        )
                );
            }
            if (property.getGetter() == null || property.getSetter() == null) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().ownerScalarPropertyMissGetterOrSetter(
                                this.getGetterMethod(),
                                Mapping.class,
                                mappingValue,
                                this.getDeclaringObjectModelMetadata().getOwnerClass()
                        )
                );
            }
            if (!property.getResolvedGenericReturnType().equals(this.getReturnType())) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().ownerScalarPropertyReturnIllegalType(
                                this.getGetterMethod(),
                                Mapping.class,
                                mappingValue,
                                this.getDeclaringObjectModelMetadata().getOwnerClass(),
                                this.getReturnType(),
                                property.getResolvedGenericReturnType()
                        )
                );
            }
            this.ownerProperty = property;
        }
        
    }
    
    private static class JPAAssociationPropertyImpl 
    extends WrapperAssociationPropertyImpl
    implements JPAAssociationProperty {
        
        private static final long serialVersionUID = -3175893847806034042L;
        
        private static final String REFERENCE_POSTFIX = "Reference";
        
        private PropertyInfo ownerProperty;
        
        private PropertyInfo ownerIndexProperty;
        
        private PropertyInfo ownerKeyProperty;
        
        private boolean inverse;
        
        protected JPAAssociationPropertyImpl(AssociationProperty baseAssociationProperty, PreContext context) {
            super(baseAssociationProperty, context);
        }

        @Override
        public JPAObjectModelMetadata getDeclaringObjectModelMetadata() {
            return (JPAObjectModelMetadata)this.declaringWrapperObjectModelMetadata();
        }
        
        @Override
        public JPAAssociationProperty getOppositeProperty() {
            return (JPAAssociationProperty)this.oppositeProperty();
        }

        @Override
        public JPAAssociationProperty getCovarianceProperty() {
            return (JPAAssociationProperty)this.covarianceProperty();
        }

        @Override
        public JPAObjectModelMetadata getKeyObjectModelMetadata() {
            return (JPAObjectModelMetadata)this.keyObjectModelMetadata();
        }

        @Override
        public JPAObjectModelMetadata getReturnObjectModelMetadata() {
            return (JPAObjectModelMetadata)this.returnObjectModelMetadata();
        }

        @Override
        public PropertyInfo getOwnerProperty() {
            return this.ownerProperty;
        }

        @Override
        public PropertyInfo getOwnerIndexProperty() {
            return this.ownerIndexProperty;
        }

        @Override
        public PropertyInfo getOwnerKeyProperty() {
            return this.ownerKeyProperty;
        }

        @Override
        public boolean isInverse() {
            return this.inverse;
        }

        @Override
        public LazyBehavior getLazyBehavior() {
            return this.getGetterMethod().getAnnotation(LazyBehavior.class);
        }

        @Override
        protected void parse() {
            AssociationProperty baseAssociationProperty = (AssociationProperty)this.baseProperty;
            Method getterMethod = this.getGetterMethod();
            
            // The mapping of contravariance property must be processed too
            this.processMapping();
            
            if (baseAssociationProperty.getCovarianceProperty() != null) {
                if (getterMethod.isAnnotationPresent(IndexMapping.class)) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().contravarianceCanNotBeUsedWith(
                                    getterMethod,
                                    Contravariance.class,
                                    IndexMapping.class
                            )
                    );
                }
                if (getterMethod.isAnnotationPresent(KeyMapping.class)) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().contravarianceCanNotBeUsedWith(
                                    getterMethod,
                                    Contravariance.class,
                                    KeyMapping.class
                            )
                    );
                }
            } else {    
                this.processIndexMapping();
                this.processKeyMapping();
                this.inverse = getterMethod.isAnnotationPresent(Inverse.class);
                LazyBehavior lazyBehavior = getterMethod.getAnnotation(LazyBehavior.class);
                if (this.inverse) {
                    int rowLimit;
                    int countLimit;
                    if (lazyBehavior != null) {
                        rowLimit = lazyBehavior.rowLimit();
                        if (rowLimit < 0) {
                            throw new IllegalProgramException(
                                    LAZY_RESOURCE.get().lazyRowLimitMustBeGEZero(
                                            getterMethod,
                                            LazyBehavior.class
                                    )
                            );
                        }
                        countLimit = lazyBehavior.countLimit();
                        if (countLimit < 0) {
                            throw new IllegalProgramException(
                                    LAZY_RESOURCE.get().lazyCountLimitMustBeGEZero(
                                            getterMethod,
                                            LazyBehavior.class
                                    )
                            );
                        }
                        if (countLimit > rowLimit) {
                            throw new IllegalProgramException(
                                    LAZY_RESOURCE.get().lazyCountLimitMustBeLElazyRowLimit(
                                            getterMethod,
                                            LazyBehavior.class
                                    )
                            );
                        }
                    }
                } else {
                    if (lazyBehavior != null) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().lazyBehaviorCanOnlyBeUsedOnInverseProperty(
                                        getterMethod,
                                        LazyBehavior.class
                                )
                        );
                    }
                }
            }
        }
        
        @Override
        protected void afterParsing(PostContext postContext) {
            if (this.getCovarianceProperty() != null) {
                this.inverse = this.getCovarianceProperty().isInverse();
            } else {
                JPAAssociationProperty oppositeAssociationProperty = this.getOppositeProperty();
                if (this.inverse) {
                    if (oppositeAssociationProperty == null) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().unidirectionalAssociationCanNotBeInverse(
                                        this.getGetterMethod(),
                                        Inverse.class
                                )
                        ); 
                    }
                    if (oppositeAssociationProperty.isInverse()) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().tooManyInverseAnnotations(
                                        this.getGetterMethod(), 
                                        this.oppositeProperty().getGetterMethod(), 
                                        Inverse.class));
                    }
                } else {
                    if (oppositeAssociationProperty != null && !oppositeAssociationProperty.isInverse()) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().noInverseAnnotation(
                                        this.getGetterMethod(), 
                                        this.oppositeProperty().getGetterMethod(), 
                                        Inverse.class
                                )
                        );
                    }
                }
            }
        }

        private void processMapping() {
            Mapping mapping = this.getGetterMethod().getAnnotation(Mapping.class);
            Class<?> standardReturnClass = this.getStandardReturnClass();
            ClassInfo<?> classInfo = ClassInfo.of(this.getDeclaringObjectModelMetadata().getOwnerClass());
            String mappingValue = mapping != null ? mapping.value() : "";
            if (mappingValue.length() == 0) {
                mappingValue = this.getName();
                if (mappingValue.endsWith(REFERENCE_POSTFIX) && 
                        Reference.class.isAssignableFrom(standardReturnClass)) {
                    mappingValue = mappingValue.substring(0, mappingValue.length() - REFERENCE_POSTFIX.length());
                }
            }
            PropertyInfo ownerProperty;
            try {
                ownerProperty = classInfo.getDeclaredErasedProperty(mappingValue);
            } catch (NoSuchPropertyInfoException ex) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().noOwnerAssociationProperty(
                                this.getGetterMethod(),
                                Mapping.class,
                                mappingValue, 
                                classInfo
                        )
                );
            }
            if (ownerProperty.getModifiers().isStatic()) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().ownerAssociationPropertyIsStatic(
                                this.getGetterMethod(),
                                Mapping.class,
                                mappingValue, 
                                this.getDeclaringObjectModelMetadata().getOwnerClass()
                        )
                );
            }
            if (ownerProperty.getGetter() == null) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().ownerAssociationPropertyMissGetter(
                                this.getGetterMethod(),
                                Mapping.class,
                                mappingValue, 
                                this.getDeclaringObjectModelMetadata().getOwnerClass()
                        )
                );
            }
            
            Class<?> ownerPropertyClass = GenericTypes.eraseGenericType(ownerProperty.getGenericReturnType());
            if (Reference.class.isAssignableFrom(standardReturnClass)) {
                if (ownerPropertyClass != this.getElementClass()) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().ownerReferencePropertyReturnIllegalType(
                                    this.getGetterMethod(),
                                    Mapping.class,
                                    mappingValue, 
                                    this.getDeclaringObjectModelMetadata().getOwnerClass(),
                                    this.getElementClass(),
                                    ownerPropertyClass
                            )
                    );
                }
                if (ownerProperty.getSetter() == null) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().ownerReferencePropertyMissSetter(
                                    this.getGetterMethod(),
                                    Mapping.class,
                                    mappingValue, 
                                    this.getDeclaringObjectModelMetadata().getOwnerClass()
                            )
                    );
                }
            } else {
                if (!(ownerProperty.getGenericReturnType() instanceof ParameterizedType)) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().ownerCollectionPropertyMustReturnParameterizedType(
                                    this.getGetterMethod(),
                                    Mapping.class,
                                    mappingValue, 
                                    this.getDeclaringObjectModelMetadata().getOwnerClass()
                            )
                    );
                }
                if (!ownerPropertyClass.isAssignableFrom(this.getReturnClass())) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().ownerCollectionPropertyReturnIllegalType(
                                    this.getGetterMethod(),
                                    Mapping.class,
                                    mappingValue, 
                                    this.getDeclaringObjectModelMetadata().getOwnerClass(),
                                    this.getReturnClass(),
                                    ownerPropertyClass
                            )
                    );
                }
                if (ownerPropertyClass != Collection.class && 
                        ownerPropertyClass != Set.class &&
                        ownerPropertyClass != List.class &&
                        ownerPropertyClass != Map.class) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().ownerCollectionPropertyReturnNotAllowedType(
                                    this.getGetterMethod(),
                                    Mapping.class,
                                    mappingValue, 
                                    this.getDeclaringObjectModelMetadata().getOwnerClass(),
                                    new Class[] {
                                        Collection.class,
                                        Set.class,
                                        List.class,
                                        Map.class
                                    },
                                    new Class[] {
                                        Collection.class,
                                        List.class,
                                        Set.class,
                                        SortedSet.class,
                                        NavigableSet.class,
                                        Map.class,
                                        SortedMap.class,
                                        NavigableMap.class,
                                        XCollection.class,
                                        XList.class,
                                        XSet.class,
                                        XOrderedSet.class,
                                        XSortedSet.class,
                                        XNavigableSet.class,
                                        XMap.class,
                                        XOrderedMap.class,
                                        XSortedMap.class,
                                        XNavigableMap.class,
                                        MACollection.class,
                                        MAList.class,
                                        MASet.class,
                                        MAOrderedSet.class,
                                        MASortedSet.class,
                                        MANavigableSet.class,
                                        MAMap.class,
                                        MAOrderedMap.class,
                                        MASortedMap.class,
                                        MANavigableMap.class
                                    }
                            )
                    );
                }
                if (ownerPropertyClass == Collection.class && this.getStandardReturnClass() == List.class) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().ownerCollectionPropertyMustReturnList(
                                    this.getGetterMethod(), 
                                    Mapping.class, 
                                    mappingValue, 
                                    this.getDeclaringObjectModelMetadata().getOwnerClass()
                            )
                    );
                }
                Type[] ownerReturnTypeArguments = ((ParameterizedType)ownerProperty.getGenericReturnType()).getActualTypeArguments();
                Class<?> ownerReturnElementClass = GenericTypes.eraseGenericType(ownerReturnTypeArguments[ownerReturnTypeArguments.length - 1]);
                if (ownerReturnElementClass != this.getElementClass()) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().ownerCollectionPropertyReturnIllegalElementType(
                                    this.getGetterMethod(),
                                    Mapping.class,
                                    mappingValue, 
                                    this.getDeclaringObjectModelMetadata().getOwnerClass(),
                                    this.getElementClass(),
                                    ownerReturnElementClass
                            )
                    );
                }
                if (Map.class.isAssignableFrom(standardReturnClass)) {
                    Class<?> ownerReturnKeyClass = GenericTypes.eraseGenericType(ownerReturnTypeArguments[0]);
                    if (ownerReturnKeyClass != this.getKeyClass()) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().ownerCollectionPropertyReturnIllegalKeyType(
                                        this.getGetterMethod(),
                                        Mapping.class,
                                        mappingValue, 
                                        this.getDeclaringObjectModelMetadata().getOwnerClass(),
                                        this.getKeyClass(),
                                        ownerReturnKeyClass
                                )
                        );
                    }
                }
            }
            this.ownerProperty = ownerProperty;
            if (this.covarianceProperty() != null && !ownerProperty.isAnnotationPresent(Transient.class)) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().contravarianceMustBeMappedAsTransient(
                                this.getGetterMethod(), 
                                Contravariance.class, 
                                ownerProperty.getGetter().getRawMethod(),
                                Transient.class
                        )
                );
            }
        }
        
        private void processIndexMapping() {
            IndexMapping indexMapping = this.getGetterMethod().getAnnotation(IndexMapping.class);
            if (indexMapping == null) {
                return;
            }
            ClassInfo<?> classInfo = ClassInfo.of(this.getDeclaringObjectModelMetadata().getOwnerClass());
            PropertyInfo ownerProperty;
            String indexMappingValue = indexMapping.value();
            try {
                ownerProperty = classInfo.getDeclaredErasedProperty(indexMappingValue);
            } catch (NoSuchPropertyInfoException ex) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().noOwnerIndexProperty(
                                this.getGetterMethod(),
                                IndexMapping.class,
                                indexMappingValue, 
                                classInfo
                        )
                );
            }
            if (ownerProperty.getModifiers().isStatic()) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().ownerIndexPropertyIsStatic(
                                this.getGetterMethod(),
                                IndexMapping.class,
                                indexMappingValue, 
                                this.getDeclaringObjectModelMetadata().getOwnerClass()
                        )
                );
            }
            if (ownerProperty.getGetter() == null || ownerProperty.getSetter() == null) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().ownerIndexPropertyMissGetterOrSetter(
                                this.getGetterMethod(),
                                IndexMapping.class,
                                indexMappingValue,
                                this.getDeclaringObjectModelMetadata().getOwnerClass()
                        )
                );
            }
            if (ownerProperty.getReturnType() != int.class) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().ownerIndexPropertyReturnIllegalType(
                                this.getGetterMethod(),
                                IndexMapping.class,
                                indexMappingValue,
                                this.getDeclaringObjectModelMetadata().getOwnerClass(),
                                ownerProperty.getReturnType()
                        )
                );
            }
            this.ownerIndexProperty = ownerProperty;
        }
        
        private void processKeyMapping() {
            KeyMapping keyMapping = this.getGetterMethod().getAnnotation(KeyMapping.class);
            if (keyMapping == null) {
                return;
            }
            ClassInfo<?> classInfo = ClassInfo.of(this.getDeclaringObjectModelMetadata().getOwnerClass());
            PropertyInfo ownerProperty;
            String keyMappingValue = keyMapping.value();
            try {
                ownerProperty = classInfo.getDeclaredErasedProperty(keyMappingValue);
            } catch (NoSuchPropertyInfoException ex) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().noOwnerKeyProperty(
                                this.getGetterMethod(),
                                KeyMapping.class,
                                keyMappingValue, 
                                classInfo
                        )
                );
            }
            if (ownerProperty.getModifiers().isStatic()) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().ownerKeyPropertyIsStatic(
                                this.getGetterMethod(),
                                KeyMapping.class,
                                keyMappingValue, 
                                this.getDeclaringObjectModelMetadata().getOwnerClass()
                        )
                );
            }
            if (ownerProperty.getGetter() == null || ownerProperty.getSetter() == null) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().ownerKeyPropertyMissGetterOrSetter(
                                this.getGetterMethod(),
                                KeyMapping.class,
                                keyMappingValue,
                                this.getDeclaringObjectModelMetadata().getOwnerClass()
                        )
                );
            }
            if (ownerProperty.getReturnType() != this.getKeyClass()) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().ownerKeyPropertyReturnIllegalType(
                                this.getGetterMethod(),
                                KeyMapping.class,
                                keyMappingValue,
                                this.getDeclaringObjectModelMetadata().getOwnerClass(),
                                this.getKeyClass(),
                                ownerProperty.getReturnType()
                        )
                );
            }
            this.ownerKeyProperty = ownerProperty;
        }
    }
    
    private static class DefaultOwnerEqualityComparatorForReferenceModeGenerator {
        
        private JPAObjectModelMetadata objectModelMetadata;
        
        private JPAScalarProperty idProperty;
        
        private String className;
        
        private String internalName;
        
        private String ownerInternalName;
        
        private String ownerDescriptor;
        
        private String objectModelInternalName;
        
        private FrozenEqualityComparator<?> frozenEqualityComparator; 

        public DefaultOwnerEqualityComparatorForReferenceModeGenerator(JPAObjectModelMetadata jpaObjectModelMetadata) {
            Arguments.mustNotBeNull(
                    "objectModelMetadata.getEntityIdProperty()", 
                    jpaObjectModelMetadata.getEntityIdProperty());
            JPAObjectModelMetadata superObjectModelMetadata = jpaObjectModelMetadata.getSuperMetadata();
            if (superObjectModelMetadata != null && 
                    superObjectModelMetadata.getMode() == ObjectModelMode.REFERENCE) {
                throw new AssertionError();
            }
            this.objectModelMetadata = jpaObjectModelMetadata;
            this.idProperty = jpaObjectModelMetadata.getEntityIdProperty();
            this.className =
                    this
                    .objectModelMetadata
                    .getOwnerClass()
                    .getName() + 
                    "{defaultJPAFrozenEqualityComparator" +
                    ':' +
                    NAME_POSTFIX +
                    '}';
            this.internalName = this.className.replace('.', '/');
            this.ownerInternalName = ASM.getInternalName(jpaObjectModelMetadata.getOwnerClass());
            this.ownerDescriptor = ASM.getDescriptor(jpaObjectModelMetadata.getOwnerClass());
            this.objectModelInternalName = ASM.getInternalName(jpaObjectModelMetadata.getObjectModelClass());
            this.frozenEqualityComparator = this.generate();
        }
        
        public FrozenEqualityComparator<?> getFrozenEqualityComparator() {
            return this.frozenEqualityComparator;
        }

        @SuppressWarnings("unchecked")
        private FrozenEqualityComparator<?> generate() {
            Action<ClassVisitor> cvAction = new Action<ClassVisitor>() {
                @Override
                public void run(ClassVisitor cv) {
                    DefaultOwnerEqualityComparatorForReferenceModeGenerator.this.generate(cv);
                }
            };
            Class<FrozenEqualityComparator<?>> clazz = 
                    (Class<FrozenEqualityComparator<?>>)
                    ASM.loadDynamicClass(
                            this.objectModelMetadata.getObjectModelClass().getClassLoader(), 
                            className, 
                            this.objectModelMetadata.getObjectModelClass().getProtectionDomain(), 
                            cvAction);
            try {
                return clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                throw new AssertionError(ex);
            }
        }
        
        private void generate(ClassVisitor cv) {
            cv.visit(
                    Opcodes.V1_7, 
                    Opcodes.ACC_PUBLIC, 
                    className.replace('.', '/'), 
                    null, 
                    "java/lang/Object", 
                    new String[] { 
                        ASM.getInternalName(FrozenEqualityComparator.class),
                        ASM.getInternalName(Serializable.class)
                    });
            this.generateSerialVersionUID(cv);
            this.generateReplacement(cv);
            this.generateClinit(cv);
            this.generateInit(cv);
            this.generateHashCodeBridge(cv);
            this.generateHashCode(cv);
            this.generateEqualsBridge(cv);
            this.generateEquals(cv);
            this.generateFreezeBridge(cv, true);
            this.generateFreeze(cv, true);
            this.generateFreezeBridge(cv, false);
            this.generateFreeze(cv, false);
            this.generateWriteReplace(cv);
            cv.visitEnd();
        }

        private void generateSerialVersionUID(ClassVisitor cv) {
            long serialVersionUID = 0;
            for (int i = this.internalName.length() - 1; i >= 0; i--) {
                serialVersionUID = serialVersionUID * 31 + this.internalName.charAt(i);
            }
            cv.visitField(
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, 
                    "serialVersionUID", 
                    "J", 
                    null, 
                    serialVersionUID
            ).visitEnd();
        }

        private void generateReplacement(ClassVisitor cv) {
            cv.visitField(
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, 
                    REPLACEMENT, 
                    "Ljava/lang/Object;", 
                    null,
                    null
            ).visitEnd();
        }

        private void generateClinit(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_STATIC, 
                    "<clinit>", 
                    "()V", 
                    null,
                    null);
            mv.visitCode();
            mv.visitTypeInsn(Opcodes.NEW, ASM.getInternalName(JPADefaultOwnerEqualityComparatorWritingReplacement.class));
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn(org.babyfish.org.objectweb.asm.Type.getType(this.ownerDescriptor));
            mv.visitLdcInsn(0);
            mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    ASM.getInternalName(JPADefaultOwnerEqualityComparatorWritingReplacement.class),
                    "<init>",
                    "(Ljava/lang/Class;[I)V",
                    false);
            mv.visitFieldInsn(
                    Opcodes.PUTSTATIC, 
                    this.internalName, 
                    REPLACEMENT, 
                    "Ljava/lang/Object;");
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateInit(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "<init>", 
                    "()V", 
                    null,
                    null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    "java/lang/Object", 
                    "<init>", 
                    "()V",
                    false);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateHashCodeBridge(ClassVisitor cv) {
            XMethodVisitor mv;
            mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_BRIDGE | Opcodes.ACC_SYNTHETIC, 
                    "hashCode", 
                    "(Ljava/lang/Object;)I", 
                    null,
                    null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitTypeInsn(Opcodes.CHECKCAST, this.ownerInternalName);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    className.replace('.', '/'),
                    "hashCode", 
                    '(' +
                    this.ownerDescriptor +
                    ")I",
                    false);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateHashCode(ClassVisitor cv) {
            XMethodVisitor mv;
            mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PUBLIC, 
                    "hashCode", 
                    '(' +
                    this.ownerDescriptor +
                    ")I", 
                    null,
                    null);
            mv.visitCode();
            final int omIndex = mv.aSlot("om");
            final int idIndex = mv.aSlot("id");
            ObjectModelFactoryProvider.visitGetObjectModel(mv, this.objectModelMetadata, 1);
            mv.visitTypeInsn(Opcodes.CHECKCAST, objectModelInternalName);
            mv.visitVarInsn(Opcodes.ASTORE, omIndex);
            Action<MethodVisitor> idAction = new Action<MethodVisitor>() {
                @Override
                public void run(MethodVisitor mv) {
                    mv.visitVarInsn(Opcodes.ALOAD, omIndex);
                    mv.visitMethodInsn(
                            Opcodes.INVOKEINTERFACE, 
                            DefaultOwnerEqualityComparatorForReferenceModeGenerator.this.objectModelInternalName, 
                            DefaultOwnerEqualityComparatorForReferenceModeGenerator.this.idProperty.getGetterName(), 
                            "()" + ASM.getDescriptor(idProperty.getReturnClass()),
                            true);
                }
            };
            if (this.idProperty.getReturnObjectModelMetadata() != null) {
                ObjectModelFactoryProvider.visitGetObjectModel(
                        mv, 
                        this.idProperty.getReturnObjectModelMetadata(), 
                        idAction);  
            } else {
                idAction.run(mv);
            }
            mv.visitVarInsn(Opcodes.ASTORE, idIndex);
        
            /*
             * if (id != null) {
             *      return id.hashCode();
             * }
             * return System.identityHashCode(om);
             */
            Label idIsNullLabel = new Label();
            mv.visitVarInsn(Opcodes.ALOAD, idIndex);
            mv.visitJumpInsn(Opcodes.IFNULL, idIsNullLabel);
            mv.visitVarInsn(Opcodes.ALOAD, idIndex);
            mv.visitHashCode(Object.class, false);
            mv.visitInsn(Opcodes.IRETURN);
            
            mv.visitLabel(idIsNullLabel);
            mv.visitVarInsn(Opcodes.ALOAD, omIndex);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASM.getInternalName(System.class), 
                    "identityHashCode", 
                    "(Ljava/lang/Object;)I",
                    false);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateEqualsBridge(ClassVisitor cv) {
            XMethodVisitor mv;
            mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_BRIDGE | Opcodes.ACC_SYNTHETIC, 
                    "equals", 
                    "(Ljava/lang/Object;Ljava/lang/Object;)Z", 
                    null,
                    null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitTypeInsn(Opcodes.CHECKCAST, this.ownerInternalName);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitTypeInsn(Opcodes.CHECKCAST, this.ownerInternalName);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    className.replace('.', '/'), 
                    "equals", 
                    '(' +
                    this.ownerDescriptor +
                    this.ownerDescriptor +
                    ")Z",
                    false);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private XMethodVisitor generateEquals(ClassVisitor cv) {
            XMethodVisitor mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_BRIDGE | Opcodes.ACC_SYNTHETIC, 
                    "equals", 
                    '(' +
                    this.ownerDescriptor +
                    this.ownerDescriptor +
                    ")Z", 
                    null,
                    null);
            mv.visitCode();
            
            final int om1Index = mv.aSlot("om1");
            final int om2Index = mv.aSlot("om2");
            final int id1Index = mv.aSlot("id1");
            final int id2Index = mv.aSlot("id2");
            
            for (int i = 0; i < 2; i++) {
                final int owIndex = i == 0 ? 1 : 2;
                final int omIndex = i == 0 ? om1Index : om2Index;
                final int idIndex = i == 0 ? id1Index : id2Index;
                ObjectModelFactoryProvider.visitGetObjectModel(
                        mv, 
                        DefaultOwnerEqualityComparatorForReferenceModeGenerator.this.objectModelMetadata, 
                        owIndex);
                mv.visitTypeInsn(Opcodes.CHECKCAST, DefaultOwnerEqualityComparatorForReferenceModeGenerator.this.objectModelInternalName);
                mv.visitVarInsn(Opcodes.ASTORE, omIndex);
                Action<MethodVisitor> idAction = new Action<MethodVisitor>() {
                    @Override
                    public void run(MethodVisitor mv) {
                        mv.visitVarInsn(Opcodes.ALOAD, omIndex);
                        mv.visitMethodInsn(
                                Opcodes.INVOKEINTERFACE, 
                                objectModelInternalName, 
                                DefaultOwnerEqualityComparatorForReferenceModeGenerator.this.idProperty.getGetterName(), 
                                "()" + ASM.getDescriptor(idProperty.getReturnClass()),
                                true);
                    }
                };
                if (this.idProperty.getReturnObjectModelMetadata() != null) {
                    ObjectModelFactoryProvider.visitGetObjectModel(
                            mv, 
                            this.idProperty.getReturnObjectModelMetadata(), 
                            idAction);
                } else {
                    idAction.run(mv);
                }
                mv.visitVarInsn(Opcodes.ASTORE, idIndex);
            }
            
            /*
             * if (id1 == null && id == null) {
             *      return om1 == om2;
             * }
             * return Nulls.equals(id1, id2);
             */
            Label notAllNullLabel = new Label();
            mv.visitVarInsn(Opcodes.ALOAD, id1Index);
            mv.visitJumpInsn(Opcodes.IFNONNULL, notAllNullLabel);
            mv.visitVarInsn(Opcodes.ALOAD, id2Index);
            mv.visitJumpInsn(Opcodes.IFNONNULL, notAllNullLabel);
            mv.visitVarInsn(Opcodes.ALOAD, om1Index);
            mv.visitVarInsn(Opcodes.ALOAD, om2Index);
            Label sameOMRefLabel = new Label();
            mv.visitJumpInsn(Opcodes.IF_ACMPEQ, sameOMRefLabel);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitLabel(sameOMRefLabel);
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitInsn(Opcodes.IRETURN);
            
            mv.visitLabel(notAllNullLabel);
            mv.visitVarInsn(Opcodes.ALOAD, id1Index);
            mv.visitVarInsn(Opcodes.ALOAD, id2Index);
            mv.visitEquals(this.idProperty.getReturnClass(), true);
            mv.visitInsn(Opcodes.IRETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
            return mv;
        }

        private void generateFreezeBridge(ClassVisitor cv, boolean freeze) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_BRIDGE | Opcodes.ACC_SYNTHETIC, 
                    freeze ? "freeze" : "unfreeze", 
                    "(Ljava/lang/Object;" +
                    ASM.getDescriptor(FrozenContext.class) +
                    ")V", 
                    null,
                    null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitTypeInsn(Opcodes.CHECKCAST, this.ownerInternalName);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    className.replace('.', '/'), 
                    freeze ? "freeze" : "unfreeze", 
                    '(' +
                    this.ownerDescriptor +
                    ASM.getDescriptor(FrozenContext.class) +
                    ")V",
                    false);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateFreeze(ClassVisitor cv, boolean freeze) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_BRIDGE | Opcodes.ACC_SYNTHETIC, 
                    freeze ? "freeze" : "unfreeze", 
                    '(' +
                    this.ownerDescriptor +
                    ASM.getDescriptor(FrozenContext.class) +
                    ")V", 
                    null,
                    null);
            mv.visitCode();
            ObjectModelFactoryProvider.visitGetObjectModel(mv, this.objectModelMetadata, 1);
            mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(ObjectModel.class));
            mv.visitLdcInsn(this.objectModelMetadata.getEntityIdProperty().getId());
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    ASM.getInternalName(ObjectModel.class), 
                    freeze ? "freezeScalar" : "unfreezeScalar", 
                    "(I" +
                    ASM.getDescriptor(FrozenContext.class) +
                    ")V",
                    true);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateWriteReplace(ClassVisitor cv) {
            MethodVisitor mv;
            mv = cv.visitMethod(
                    Opcodes.ACC_PROTECTED, 
                    "writeReplace", 
                    "()Ljava/lang/Object;", 
                    null, 
                    new String[] { ASM.getInternalName(ObjectStreamException.class) });
            mv.visitCode();
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    internalName, 
                    REPLACEMENT, 
                    "Ljava/lang/Object;");
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }
    
    private interface Resource {
        
        String unidirectionalAssociationCanNotBeInverse(
                Method associationMethod,
                Class<Inverse> inverseTypeConstant);

        String tooManyInverseAnnotations(
                Method associationMethod1,
                Method associationMethod2,
                Class<Inverse> inverseTypeConstant);
        
        String noInverseAnnotation(
                Method associationMethod1,
                Method associationMethod2,
                Class<Inverse> inverseTypeConstant);
        
        String conflictScalarAnnotation(
                Class<?> objectModelType,
                Method propertyMethod,
                Class<?> scalarAnnotationType1,
                Class<?> scalarAnnotationType2);
        
        String entityIdHasBeenDeclaredInSuperMetadata(
                Method propertyMethod,
                Class<EntityId> entityIdTypeConstant,
                Class<?> superObjectModelClass);

        String duplicatedEntityIdAnnotation(
                Class<?> objectModelClass,
                Method propertyMethod1,
                Method propertyMethod2,
                Class<EntityId> entityIdTypeConstant);
        
        String noEntityIdAnnotation(
                Class<?> objectModelClass,
                Class<EntityId> entityIdTypeConstant);
        
        String entityIdIsPrimitive(
                Class<?> objectModelClass,
                Method entityIdPropertyMethod,
                Class<EntityId> entityIdTypeConstant);
        
        String optimisticLockHasBeenDeclaredInSuperMetadata(
                Method propertyMethod,
                Class<OptimisticLock> optimisticLockTypeConstant,
                Class<?> superObjectModelClass);

        String duplicatedOptimisticLockAnnotation(
                Class<?> objectModelClass,
                Method propertyMethod1,
                Method propertyMethod2,
                Class<OptimisticLock> optimisticLockTypeConstant);
        
        String illegalEntityName(String entityName);
        
        String duplicatedJAPPropertyMapping(
                Class<?> objectModelClass, 
                String jpaPropertyName1,
                String jpaPropertyName2, 
                Class<?> mappingType,
                String mappingPropertyName);
        
        String duplicatedJAPPropertyMappings(
                Class<?> objectModelClass, 
                String jpaPropertyName1,
                Class<?> mappingType1,
                String jpaPropertyName2,            
                Class<?> mappingType2,
                String indexMappingPropertyName);
        
        String noOwnerScalarProperty(
                Method omGetterMethod, 
                Class<Mapping> mappingTypeConstant,
                String mappingValue,
                ClassInfo<?> ownerClassInfo);

        String ownerScalarPropertyIsStatic(
                Method omGetterMethod,
                Class<Mapping> mappingTypeConstant,
                String mappingValue,
                Class<?> jpaEntityClass);
        
        String ownerScalarPropertyMissGetterOrSetter(
                Method omGetterMethod,
                Class<Mapping> mappingTypeConstant,
                String mappingValue, 
                Class<?> jpaEntityClass);
        
        String ownerScalarPropertyReturnIllegalType(
                Method omGetterMethod,
                Class<Mapping> mappingTypeConstant,
                String mappingValue, 
                Class<?> jpaEntityClass,
                Type expectedReturnType, 
                Type actualReturnType);
        
        String noOwnerAssociationProperty(
                Method omGetterMethod, 
                Class<Mapping> mappingTypeConstant,
                String mappingValue,
                ClassInfo<?> ownerClassInfo);

        String ownerAssociationPropertyIsStatic(
                Method omGetterMethod, 
                Class<Mapping> mappingTypeConstant,
                String mappingValue,
                Class<?> jpaEntityClass);
        
        String ownerAssociationPropertyMissGetter(
                Method omGetterMethod,
                Class<Mapping> mappingTypeConstant,
                String mappingValue, 
                Class<?> jpaEntityClass);
        
        String ownerReferencePropertyReturnIllegalType(
                Method omGetterMethod,
                Class<Mapping> mappingTypeConstant, 
                String mappingValue,
                Class<?> jpaEntityClass, 
                Class<?> expectedType,
                Class<?> actualType);

        String ownerReferencePropertyMissSetter(
                Method omGetterMethod,
                Class<Mapping> mappingTypeConstant,
                String mappingValue, 
                Class<?> jpaEntityClass);
        
        String ownerCollectionPropertyMustReturnList(
                Method omGetterMethod,
                Class<Mapping> mappingTypeConstant,
                String mappingValue, 
                Class<?> jpaEntityClass);
        
        String ownerCollectionPropertyMustReturnParameterizedType(
                Method omGetterMethod,
                Class<Mapping> mappingTypeConstant,
                String mappingValue, 
                Class<?> jpaEntityClass);
        
        String ownerCollectionPropertyReturnNotAllowedType(
                Method omGetterMethod,
                Class<Mapping> mappingTypeConstant,
                String mappingValue, 
                Class<?> jpaEntityClass,
                Class<?>[] jpaOwnerAllowedReturnTypes,
                Class<?>[] objectModelPropertyReturnTypes);

        String ownerCollectionPropertyReturnIllegalType(
                Method omGetterMethod,
                Class<Mapping> mappingTypeConstant,
                String mappingValue, 
                Class<?> jpaEntityClass,
                Class<?> expectedBoundType,
                Class<?> actualType);
        
        String ownerCollectionPropertyReturnIllegalElementType(
                Method omGetterMethod, 
                Class<Mapping> mappingTypeConstant,
                String mappingValue, 
                Class<?> jpaEntityClass,
                Class<?> exceptedElementClass, 
                Class<?> actualElemntClass);

        String ownerCollectionPropertyReturnIllegalKeyType(
                Method omGetterMethod,
                Class<Mapping> mappingTypeConstant, 
                String mappingValue,
                Class<?> jpaEntityClass, 
                Class<?> expectedKeyClass, 
                Class<?> actualKeyClass);
        
        String noOwnerIndexProperty(
                Method omGetterMethod,
                Class<IndexMapping> indexMappingTypeConstant, 
                String indexMappingValue,
                ClassInfo<?> classInfo);

        String ownerIndexPropertyIsStatic(
                Method omGetterMethod,
                Class<IndexMapping> indexMappingTypeConstant, 
                String indexMappingValue,
                Class<?> jpaEntityClass);

        String ownerIndexPropertyMissGetterOrSetter(
                Method omGetterMethod,
                Class<IndexMapping> indexMappingTypeConstant, 
                String indexMappingValue,
                Class<?> jpaEntityClass);

        String ownerIndexPropertyReturnIllegalType(
                Method omGetterMethod,
                Class<IndexMapping> indexMappingTypeConstant, 
                String indexMappingValue,
                Class<?> jpaEntityClass, 
                Class<?> actualType);
        
        String noOwnerKeyProperty(
                Method omGetterMethod,
                Class<KeyMapping> keyMappingTypeConstant, 
                String keyMappingValue,
                ClassInfo<?> classInfo);

        String ownerKeyPropertyIsStatic(
                Method omGetterMethod,
                Class<KeyMapping> keyMappingTypeConstant, 
                String keyMappingValue,
                Class<?> jpaEntityClass);

        String ownerKeyPropertyMissGetterOrSetter(
                Method omGetterMethod,
                Class<KeyMapping> keyMappingTypeConstant, 
                String keyMappingValue,
                Class<?> jpaEntityClass);

        String ownerKeyPropertyReturnIllegalType(
                Method omGetterMethod,
                Class<KeyMapping> keyMappingTypeConstant, 
                String keyMappingValue,
                Class<?> jpaEntityClass, 
                Class<?> expectedKeyClass,
                Class<?> actualKeyClass);
        
        String lazyRowLimitMustBeGEZero(
                Method omGettMethod, 
                Class<LazyBehavior> lazyBehaviorTypeConstant);
        
        String lazyCountLimitMustBeGEZero(
                Method omGettMethod, 
                Class<LazyBehavior> lazyBehaviorTypeConstant);
        
        String lazyCountLimitMustBeLElazyRowLimit(
                Method omGettMethod, 
                Class<LazyBehavior> lazyBehaviorTypeConstant);
        
        String lazyBehaviorCanOnlyBeUsedOnInverseProperty(
                Method omGettMethod, 
                Class<LazyBehavior> lazyBehaviorTypeConstant);
        
        String contravarianceMustBeMappedAsTransient(
                Method omGetterMethod,
                Class<Contravariance> contravarianceTypeConstant,
                Method ownerGetterMethod,
                Class<Transient> transientTypeConstant);
        
        String contravarianceCanNotBeUsedWith(
                Method omGetterMethod,
                Class<Contravariance> contravarianceTypeConstant,
                Class<? extends Annotation> annotationType);
    }
    
}
