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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.babyfish.association.AssociatedEndpointType;
import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.HashMap;
import org.babyfish.collection.HashSet;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.ReferenceEqualityComparator;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.lang.Nulls;
import org.babyfish.lang.Singleton;
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.metadata.AssociationProperty;
import org.babyfish.model.metadata.ObjectModelMetadata;
import org.babyfish.model.metadata.ObjectModelMode;
import org.babyfish.model.metadata.Property;
import org.babyfish.model.metadata.ScalarProperty;
import org.babyfish.model.spi.ObjectModelFactoryProvider;
import org.babyfish.util.LazyResource;

/**
 * @author Tao Chen
 */
public abstract class AbstractWrapperMetadatas extends Singleton implements Serializable {
    
    private static final long serialVersionUID = -6812485723457835930L;
    
    private static final Class<?>[] EMPTY_CLASSES = new Class[0];
    
    private static final String[] EMPTY_STRINGS = new String[0];
    
    private static final LazyResource<Resource> LAZY_RESOURCE = LazyResource.of(Resource.class);
    
    private final Map<Class<?>, AbstractWrapperObjectModelMetadataImpl> cache = new WeakHashMap<>();
    
    private final ReadWriteLock lock = new ReentrantReadWriteLock(); 

    protected AbstractWrapperMetadatas() {
        super();
    }
    
    protected abstract AbstractWrapperObjectModelMetadataImpl createWrapperObjectModelMetadataImpl(
            Class<?> rawClass, PreContext preContext);
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected final AbstractWrapperObjectModelMetadataImpl getWrapperObjectModelMetadataImpl(Class<?> ownerClass) {
        
        Map<Class<?>, AbstractWrapperObjectModelMetadataImpl> cache = this.cache;
        Object wrapperObjectModelMetadataImpl;
        Lock lock;
        
        (lock = this.lock.readLock()).lock();
        try {
            wrapperObjectModelMetadataImpl = cache.get(ownerClass); //1st reading
        } finally {
            lock.unlock();
        }
        
        if (wrapperObjectModelMetadataImpl == null) { //1st checking
            (lock = this.lock.writeLock()).lock();
            try {
                wrapperObjectModelMetadataImpl = cache.get(ownerClass); //2nd reading
                if (wrapperObjectModelMetadataImpl == null) { //2nd checking
                    Map<Class<?>, AbstractWrapperObjectModelMetadataImpl> map;
                    PreContext preContext = new PreContext(this);
                    wrapperObjectModelMetadataImpl = this.createWrapperObjectModelMetadataClassImpl(ownerClass, preContext);
                    map = (Map)preContext.wrapperObjectModelMetadatas;
                    cache.putAll(map);
                }
            } finally {
                lock.unlock();
            }
        }
        
        return (AbstractWrapperObjectModelMetadataImpl)wrapperObjectModelMetadataImpl;
    }
    
    private AbstractWrapperObjectModelMetadataImpl createWrapperObjectModelMetadataClassImpl(
            Class<?> rawClass, 
            PreContext preContext) {
        
        AbstractWrapperObjectModelMetadataImpl toReturn = preContext.getOrCreateWrapperObjectModelMetadata(rawClass);
        Collection<AbstractWrapperObjectModelMetadataImpl> wrapperObjectModelMetadatas = 
            preContext.wrapperObjectModelMetadatas.values();
        
        for (AbstractWrapperObjectModelMetadataImpl wrapperObjectModelMetadata : wrapperObjectModelMetadatas) {
            for (WrapperAssociationPropertyImpl wrapperAssociationProperty : wrapperObjectModelMetadata.declaredAssociationProperties.values()) {
                WrapperAssociationPropertyImpl oppositeWrapperProperty = 
                        preContext.tranformProperty(
                                ((AssociationProperty)wrapperAssociationProperty.baseProperty).getOppositeProperty()
                        );
                WrapperAssociationPropertyImpl covarianceWrapperProperty = 
                        preContext.tranformProperty(
                                ((AssociationProperty)wrapperAssociationProperty.baseProperty).getCovarianceProperty()
                        );
                wrapperAssociationProperty.setOppositeAndCovariance(
                        oppositeWrapperProperty,
                        covarianceWrapperProperty);
                if (wrapperAssociationProperty.getAssociatedEndpointType().isCollection()) {
                    XOrderedMap<String, ScalarProperty> comparatorProperties = 
                            ((AssociationProperty)wrapperAssociationProperty.baseProperty).getComparatorProperties();
                    XOrderedMap<String, WrapperScalarPropertyImpl> wrappedComparatorProperties =
                            new LinkedHashMap<>((comparatorProperties.size() * 4 + 2) / 3);
                    for (Entry<String, ScalarProperty> entry : comparatorProperties.entrySet()) {
                        wrappedComparatorProperties.put(
                                entry.getKey(), 
                                (WrapperScalarPropertyImpl)preContext.propertyTransfromMap.get(entry.getValue()));
                    }
                    wrapperAssociationProperty.comparatorProperties = MACollections.unmodifiable(wrappedComparatorProperties);
                }
            }
        }
        
        for (AbstractWrapperObjectModelMetadataImpl wrapperObjectModelMetadata : wrapperObjectModelMetadatas) {
            wrapperObjectModelMetadata.beforeParsing();
        }
        for (AbstractWrapperObjectModelMetadataImpl wrapperObjectModelMetadata : wrapperObjectModelMetadatas) {
            for (WrapperAssociationPropertyImpl wrapperAssociationProperty : 
                wrapperObjectModelMetadata.declaredAssociationProperties.values()) {
                wrapperAssociationProperty.beforeParsing();
            }
        }
        for (AbstractWrapperObjectModelMetadataImpl wrapperObjectModelMetadata : wrapperObjectModelMetadatas) {
            for (WrapperScalarPropertyImpl wrapperScalarProperty : wrapperObjectModelMetadata.declaredScalarProperties.values()) {
                wrapperScalarProperty.beforeParsing();
            }
        }
        for (AbstractWrapperObjectModelMetadataImpl wrapperObjectModelMetadata : wrapperObjectModelMetadatas) {
            for (WrapperAssociationPropertyImpl wrapperAssociationProperty : 
                wrapperObjectModelMetadata.declaredAssociationProperties.values()) {
                wrapperAssociationProperty.parse();
            }
        }
        PostContext postContext = new PostContext(preContext);
        for (AbstractWrapperObjectModelMetadataImpl wrapperObjectModelMetadata : wrapperObjectModelMetadatas) {
            for (WrapperScalarPropertyImpl wrapperScalarProperty : wrapperObjectModelMetadata.declaredScalarProperties.values()) {
                wrapperScalarProperty.afterParsing(postContext);
            }
        }
        for (AbstractWrapperObjectModelMetadataImpl wrapperObjectModelMetadata : wrapperObjectModelMetadatas) {
            for (WrapperAssociationPropertyImpl wrapperAssociationProperty : 
                wrapperObjectModelMetadata.declaredAssociationProperties.values()) {
                wrapperAssociationProperty.afterParsing(postContext);
            }
        }
        while (true) {
            boolean morePhase = false;
            for (AbstractWrapperObjectModelMetadataImpl wrapperObjectModelMetadata : wrapperObjectModelMetadatas) {
                morePhase |= wrapperObjectModelMetadata.afterParsing(postContext);
            }
            if (!morePhase) {
                break;
            }
            postContext.phase++;
        }
        
        for (AbstractWrapperObjectModelMetadataImpl wrapperObjectModelMetadata : wrapperObjectModelMetadatas) {
            wrapperObjectModelMetadata.finished = true;
        }
        
        return toReturn;
    }
    
    protected static abstract class AbstractWrapperObjectModelMetadataImpl implements ObjectModelMetadata {
        
        private static final long serialVersionUID = 2619867679074049238L;

        private AbstractWrapperMetadatas metadatas;
        
        private ObjectModelMetadata baseObjectModelMetadata;
        
        private AbstractWrapperObjectModelMetadataImpl superMetadata;
        
        private XOrderedMap<String, AbstractWrapperPropertyImpl> declaredProperties;
        
        private XOrderedMap<String, WrapperScalarPropertyImpl> declaredScalarProperties;
        
        private XOrderedMap<String, WrapperAssociationPropertyImpl> declaredAssociationProperties;
        
        private XOrderedMap<String, WrapperScalarPropertyImpl> comparatorProperties;
        
        private boolean finished;
        
        private transient AbstractWrapperPropertyImpl[] declaredPropertyArr;
        
        private transient XOrderedMap<String, AbstractWrapperPropertyImpl> properties;
        
        private transient XOrderedMap<String, WrapperScalarPropertyImpl> scalarProperties;
        
        private transient XOrderedMap<String, WrapperAssociationPropertyImpl> associationProperties;
        
        private transient AbstractWrapperPropertyImpl[] propertyArr;
        
        protected AbstractWrapperObjectModelMetadataImpl(
                ObjectModelMetadata baseObjectModelMetadata, 
                PreContext preContext) {
            Arguments.mustNotBeNull("baseObjectModelMetadata", baseObjectModelMetadata);
            this.metadatas = preContext.declaringMetadatas;
            this.baseObjectModelMetadata = baseObjectModelMetadata;
            /*
             * The metadata of super class or the metadatas or assocaition properties 
             * must be accessed or created after this "preContext.wrapperObjectModelMetadatas.put" statement. 
             * This is very important.
             */
            preContext.wrapperObjectModelMetadatas.put(baseObjectModelMetadata.getOwnerClass(), this);
            
            ObjectModelMetadata baseSuperMetadata = baseObjectModelMetadata.getSuperMetadata();
            if (baseSuperMetadata != null) {
                this.superMetadata = preContext.getOrCreateWrapperObjectModelMetadata(baseSuperMetadata.getOwnerClass());
            }
            
            XOrderedMap<String, ScalarProperty> comparatorProperties = 
                    baseObjectModelMetadata.getComparatorProperties();
            XOrderedMap<String, AbstractWrapperPropertyImpl> declareWrapperProperties =
                new LinkedHashMap<String, AbstractWrapperPropertyImpl>(
                        baseObjectModelMetadata.getProperties().size());
            XOrderedMap<String, WrapperScalarPropertyImpl> declaredWrapperScalarProperties = 
                new LinkedHashMap<String, WrapperScalarPropertyImpl>(
                        baseObjectModelMetadata.getDeclaredScalarProperties().size());
            XOrderedMap<String, WrapperAssociationPropertyImpl> declaredWrapperAssociationProperties = 
                new LinkedHashMap<String, WrapperAssociationPropertyImpl>(
                        baseObjectModelMetadata.getDeclaredAssociationProperties().size());
            XOrderedMap<String, WrapperScalarPropertyImpl> wrapperComparatorProperties =
                    new LinkedHashMap<>((comparatorProperties.size() * 4 + 2) / 3);
            for (Property baseProperty : baseObjectModelMetadata.getDeclaredProperties().values()) {
                AbstractWrapperPropertyImpl wrapperProperty;
                if (baseProperty instanceof AssociationProperty) {
                    wrapperProperty = this.createWrapperAssociationPropertyImpl(
                            (AssociationProperty)baseProperty, preContext);
                    if (wrapperProperty == null) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().methodMustNotReturnNull(
                                        this.getClass(),
                                        "createWrapperAssociationPropertyImpl",
                                        AssociationProperty.class,
                                        PreContext.class
                                )
                        );
                    }
                    if (wrapperProperty.baseProperty != baseProperty) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().methodMustReturnWrapperOfFirstParameter(
                                        this.getClass(),
                                        "createWrapperAssociationPropertyImpl",
                                        AssociationProperty.class,
                                        PreContext.class
                                )
                        );
                    }
                    declaredWrapperAssociationProperties.put(
                            wrapperProperty.getName(), 
                            (WrapperAssociationPropertyImpl)wrapperProperty);
                } else {
                    wrapperProperty = this.createWrapperScalarPropertyImpl(
                            (ScalarProperty)baseProperty, preContext);
                    if (wrapperProperty == null) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().methodMustNotReturnNull(
                                        this.getClass(),
                                        "createWrapperScalarPropertyImpl",
                                        ScalarProperty.class,
                                        PreContext.class
                                )
                        );
                    }
                    if (wrapperProperty.baseProperty != baseProperty) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().methodMustReturnWrapperOfFirstParameter(
                                        this.getClass(),
                                        "createWrapperScalarPropertyImpl",
                                        ScalarProperty.class,
                                        PreContext.class
                                )
                        );
                    }
                    declaredWrapperScalarProperties.put(
                            wrapperProperty.getName(), 
                            (WrapperScalarPropertyImpl)wrapperProperty);
                }
                declareWrapperProperties.put(wrapperProperty.getName(), wrapperProperty);
            }
            for (Entry<String, ScalarProperty> entry : comparatorProperties.entrySet()) {
                wrapperComparatorProperties.put(
                        entry.getKey(), 
                        (WrapperScalarPropertyImpl)preContext.propertyTransfromMap.get(entry.getValue()));
            }
            this.declaredProperties = MACollections.unmodifiable(declareWrapperProperties);
            this.declaredScalarProperties = MACollections.unmodifiable(declaredWrapperScalarProperties);
            this.declaredAssociationProperties = MACollections.unmodifiable(declaredWrapperAssociationProperties);
            this.comparatorProperties = MACollections.unmodifiable(wrapperComparatorProperties);
        }

        @Override
        public final Class<?> getOwnerClass() {
            return this.baseObjectModelMetadata.getOwnerClass();
        }

        @Override
        public final Class<?> getObjectModelClass() {
            return this.baseObjectModelMetadata.getObjectModelClass();
        }

        @Override
        public int getDeclaredPropertyBaseId() {
            return this.baseObjectModelMetadata.getDeclaredPropertyBaseId();
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public final XOrderedMap<String, Property> getDeclaredProperties() {
            XOrderedMap<String, Property> properties = (XOrderedMap)this.declaredProperties;
            if (properties == null) {
                throw new IllegalStateException(LAZY_RESOURCE.get().invokeMethodTooEarly("getDeclaredProperties"));
            }
            return properties;
        }
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public final XOrderedMap<String, ScalarProperty> getDeclaredScalarProperties() {
            XOrderedMap<String, ScalarProperty> properties = (XOrderedMap)this.declaredScalarProperties;
            if (properties == null) {
                throw new IllegalStateException(LAZY_RESOURCE.get().invokeMethodTooEarly("getDeclaredScalarProperties"));
            }
            return properties;
        }
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public final XOrderedMap<String, AssociationProperty> getDeclaredAssociationProperties() {
            XOrderedMap<String, AssociationProperty> properties = (XOrderedMap)this.declaredAssociationProperties;
            if (properties == null) {
                throw new IllegalStateException(LAZY_RESOURCE.get().invokeMethodTooEarly("getDeclaredAssociationProperties"));
            }
            return properties;
        }
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public final XOrderedMap<String, ScalarProperty> getComparatorProperties() {
            return (XOrderedMap)this.comparatorProperties();
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public final XOrderedMap<String, Property> getProperties() {
            XOrderedMap<String, AbstractWrapperPropertyImpl> properties = 
                this.properties;
            if (properties == null) {
                AbstractWrapperObjectModelMetadataImpl superMetadata = this.superMetadata;
                if (superMetadata == null) {
                    properties = this.declaredProperties; 
                } else {
                    properties = new LinkedHashMap<String, AbstractWrapperPropertyImpl>();
                    properties.putAll((Map)superMetadata.getProperties());
                    properties.putAll(this.declaredProperties);
                }
                this.properties = MACollections.unmodifiable(properties);
            }
            return (XOrderedMap)properties;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public final XOrderedMap<String, ScalarProperty> getScalarProperties() {
            XOrderedMap<String, WrapperScalarPropertyImpl> scalarProperty = 
                this.scalarProperties;
            if (scalarProperty == null) {
                AbstractWrapperObjectModelMetadataImpl superMetadata = this.superMetadata;
                if (superMetadata == null) {
                    scalarProperty = this.declaredScalarProperties; 
                } else {
                    scalarProperty = new LinkedHashMap<String, WrapperScalarPropertyImpl>();
                    scalarProperty.putAll((Map)superMetadata.getScalarProperties());
                    scalarProperty.putAll(this.declaredScalarProperties);
                }
                this.scalarProperties = MACollections.unmodifiable(scalarProperty);
            }
            return (XOrderedMap)scalarProperty;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public final XOrderedMap<String, AssociationProperty> getAssociationProperties() {
            XOrderedMap<String, WrapperAssociationPropertyImpl> associationPropertys = 
                this.associationProperties;
            if (associationPropertys == null) {
                AbstractWrapperObjectModelMetadataImpl superMetadata = this.superMetadata;
                if (superMetadata == null) {
                    associationPropertys = this.declaredAssociationProperties; 
                } else {
                    associationPropertys = new LinkedHashMap<String, WrapperAssociationPropertyImpl>();
                    associationPropertys.putAll((Map)superMetadata.getAssociationProperties());
                    associationPropertys.putAll(this.declaredAssociationProperties);
                }
                this.associationProperties = MACollections.unmodifiable(associationPropertys);
            }
            return (XOrderedMap)associationPropertys;
        }

        @Override
        public final String getStaticMethodName() {
            return this.baseObjectModelMetadata.getStaticMethodName();
        }

        @Override
        public final ObjectModelFactoryProvider getProvider() {
            return this.baseObjectModelMetadata.getProvider();
        }

        @Override
        public final ObjectModelFactory<?> getFactory() {
            return this.baseObjectModelMetadata.getFactory();
        }

        @Override
        public final ObjectModelMode getMode() {
            return this.baseObjectModelMetadata.getMode();
        }
        
        @Override
        public final boolean isDisabilityAllowed() {
            return this.baseObjectModelMetadata.isDisabilityAllowed();
        }

        @Override
        public final <O> Comparator<O> getOwnerComparator() {
            return this.baseObjectModelMetadata.getOwnerComparator();
        }

        @Override
        public final <O> Comparator<O> getOwnerComparator(String... scalarPropertyNames) {
            Arguments.mustNotBeNull("scalarPropertyNames", scalarPropertyNames);
            Arguments.mustNotBeEmpty("scalarPropertyNames", scalarPropertyNames);
            Arguments.mustNotContainNullElements("scalarPropertyNames", scalarPropertyNames);
            Arguments.mustNotContainEmptyElements("scalarPropertyNames", scalarPropertyNames);
            return this.baseObjectModelMetadata.getOwnerComparator(scalarPropertyNames);
        }

        @Override
        public final <O> Comparator<O> getOwnerComparator(int... scalarPropertyIds) {
            Arguments.mustNotBeNull("scalarPropertyIds", scalarPropertyIds);
            Arguments.mustNotBeEmpty("scalarPropertyIds", scalarPropertyIds);
            return this.baseObjectModelMetadata.getOwnerComparator(scalarPropertyIds);
        }

        @Override
        public final <O> EqualityComparator<O> getOwnerEqualityComparator() {
            if (this.baseObjectModelMetadata.getMode() == ObjectModelMode.REFERENCE) {
                return this.getDefaultOwnerEqualityComparatorForReferenceMode();
            }
            return this.baseObjectModelMetadata.getOwnerEqualityComparator();
        }

        @Override
        public final <O> EqualityComparator<O> getOwnerEqualityComparator(String... scalarPropertyNames) {
            if (Nulls.isNullOrEmpty(scalarPropertyNames)) {
                return this.getOwnerEqualityComparator();
            }
            Arguments.mustNotContainNullElements("scalarPropertyNames", scalarPropertyNames);
            Arguments.mustNotContainEmptyElements("scalarPropertyNames", scalarPropertyNames);
            return this.baseObjectModelMetadata.getOwnerEqualityComparator(scalarPropertyNames);
        }

        @Override
        public final <O> EqualityComparator<O> getOwnerEqualityComparator(int... scalarPropertyIds) {
            if (Nulls.isNullOrEmpty(scalarPropertyIds)) {
                return this.getOwnerEqualityComparator();
            }
            return this.baseObjectModelMetadata.getOwnerEqualityComparator(scalarPropertyIds);
        }

        protected final AbstractWrapperObjectModelMetadataImpl superMetadata() {
            return this.superMetadata;
        }

        protected final AbstractWrapperPropertyImpl declaredProperty(String name) {
            Map<String, AbstractWrapperPropertyImpl> properties = this.declaredProperties;
            if (properties == null) {
                throw new IllegalStateException(LAZY_RESOURCE.get().invokeMethodTooEarly("declaredProperty", String.class));
            }
            return properties.get(name);
        }
        
        protected final WrapperScalarPropertyImpl declaredScalarProperty(String name) {
            Map<String, WrapperScalarPropertyImpl> properties = this.declaredScalarProperties;
            if (properties == null) {
                throw new IllegalStateException(LAZY_RESOURCE.get().invokeMethodTooEarly("declaredScalarProperty", String.class));
            }
            return properties.get(name);
        }
        
        protected final WrapperAssociationPropertyImpl declaredAssociationProperty(String name) {
            Map<String, WrapperAssociationPropertyImpl> properties = this.declaredAssociationProperties;
            if (properties == null) {
                throw new IllegalStateException(LAZY_RESOURCE.get().invokeMethodTooEarly("declaredAssociationProperty", String.class));
            }
            return properties.get(name);
        }
        
        protected final AbstractWrapperPropertyImpl declaredProperty(int id) {
            AbstractWrapperPropertyImpl[] declaredPropertyArr = this.declaredPropertyArr;
            if (declaredPropertyArr == null) {
                this.declaredPropertyArr = this.declaredProperties.values().toArray(
                        declaredPropertyArr = new AbstractWrapperPropertyImpl[this.declaredProperties.size()]);
            }
            return declaredPropertyArr[id - this.getDeclaredPropertyBaseId()];
        }
        
        protected final WrapperScalarPropertyImpl declaredScalarProperty(int index) {
            AbstractWrapperPropertyImpl wrapperProperty = this.declaredProperty(index);
            if (wrapperProperty instanceof WrapperScalarPropertyImpl) {
                return (WrapperScalarPropertyImpl)wrapperProperty;
            }
            return null;
        }
        
        protected final WrapperAssociationPropertyImpl declaredAssociationProperty(int index) {
            AbstractWrapperPropertyImpl wrapperProperty = this.declaredProperty(index);
            if (wrapperProperty instanceof WrapperAssociationPropertyImpl) {
                return (WrapperAssociationPropertyImpl)wrapperProperty;
            }
            return null;
        }
        
        protected final AbstractWrapperPropertyImpl property(String name) {
            AbstractWrapperPropertyImpl property = this.declaredProperties.get(name);
            if (property == null) {
                AbstractWrapperObjectModelMetadataImpl superMetadata = this.superMetadata;
                if (superMetadata != null) {
                    return superMetadata.property(name);
                }
                throw new IllegalArgumentException(LAZY_RESOURCE.get().noProperty(this.baseObjectModelMetadata.getObjectModelClass(), name));
            }
            return property;
        }

        protected final WrapperScalarPropertyImpl scalarProperty(String name) {
            WrapperScalarPropertyImpl property = this.declaredScalarProperties.get(name);
            if (property == null) {
                AbstractWrapperObjectModelMetadataImpl superMetadata = this.superMetadata;
                if (superMetadata != null) {
                    return superMetadata.scalarProperty(name);
                }
                throw new IllegalArgumentException(LAZY_RESOURCE.get().noScalarProperty(this.baseObjectModelMetadata.getObjectModelClass(), name));
            }
            return property;
        }

        protected final WrapperAssociationPropertyImpl associationProperty(String name) {
            WrapperAssociationPropertyImpl property = this.declaredAssociationProperties.get(name);
            if (property == null) {
                AbstractWrapperObjectModelMetadataImpl superMetadata = this.superMetadata;
                if (superMetadata != null) {
                    return superMetadata.associationProperty(name);
                }
                throw new IllegalArgumentException(LAZY_RESOURCE.get().nosAssociationProperty(this.baseObjectModelMetadata.getObjectModelClass(), name));
            }
            return property;
        }

        protected final AbstractWrapperPropertyImpl property(int id) {
            AbstractWrapperPropertyImpl[] propertyArr = this.propertyArr;
            if (propertyArr == null) {
                XOrderedMap<String, Property> properties = 
                    this.getProperties();
                this.propertyArr = properties.values().toArray(
                        propertyArr =
                            new AbstractWrapperPropertyImpl[properties.size()]);
            }
            Arguments.mustBetweenValue(
                    "id", 
                    id, 
                    0, 
                    true, 
                    propertyArr.length, 
                    false);
            return propertyArr[id];
        }

        protected final WrapperScalarPropertyImpl scalarProperty(int id) {
            Property property = this.getProperty(id);
            if (property instanceof ScalarProperty) {
                return (WrapperScalarPropertyImpl)property;
            }
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().specifiedPropertyIsNotScalar(this.getObjectModelClass(), id)
            );
        }

        protected final WrapperAssociationPropertyImpl associationProperty(int id) {
            Property property = this.getProperty(id);
            if (property instanceof AssociationProperty) {
                return (WrapperAssociationPropertyImpl)property;
            }
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().specifiedPropertyIsNotAssociation(this.getObjectModelClass(), id)
            );
        }
        
        protected final XOrderedMap<String, WrapperScalarPropertyImpl> comparatorProperties() {
            return this.comparatorProperties;
        }
        
        protected abstract WrapperScalarPropertyImpl createWrapperScalarPropertyImpl(
                ScalarProperty baseScalarProperty, PreContext preContext);
        
        protected abstract WrapperAssociationPropertyImpl createWrapperAssociationPropertyImpl(
                AssociationProperty baseAssociationProperty, PreContext preContext);
        
        protected <O> EqualityComparator<O> getDefaultOwnerEqualityComparatorForReferenceMode() {
            return this.baseObjectModelMetadata.getOwnerEqualityComparator();
        }
        
        protected void beforeParsing() {}
        
        protected boolean afterParsing(PostContext postContext) { return false; }
        
        protected final Object writeReplace() throws ObjectStreamException {
            return new ObjectModelMetadataReplacement(this);
        }
    }
    
    protected static abstract class AbstractWrapperPropertyImpl implements Property {
        
        private static final long serialVersionUID = 6274757411332873289L;

        private final AbstractWrapperObjectModelMetadataImpl declaringObjectModelMetadata;
        
        protected final Property baseProperty;
        
        private transient Method getterMethod;
        
        AbstractWrapperPropertyImpl(
                Property baseProperty, PreContext preContext) {
            Arguments.mustNotBeNull("baseProperty", baseProperty);
            Arguments.mustNotBeNull("preContext", preContext);
            preContext.propertyTransfromMap.put(baseProperty, this);
            
            this.baseProperty = baseProperty;
            this.declaringObjectModelMetadata = 
                    preContext.getOrCreateWrapperObjectModelMetadata(
                            baseProperty.getDeclaringObjectModelMetadata().getOwnerClass());
        }

        @Override
        public final boolean isDisabilityAllowed() {
            return this.baseProperty.isDisabilityAllowed();
        }

        @Override
        public final String getName() {
            return this.baseProperty.getName();
        }

        @Override
        public String getGetterName() {
            return this.baseProperty.getGetterName();
        }

        @Override
        public String getSetterName() {
            return this.baseProperty.getSetterName();
        }

        @Override
        public final Class<?> getReturnClass() {
            return this.baseProperty.getReturnClass();
        }

        @Override
        public final int getId() {
            return this.baseProperty.getId();
        }

        protected final AbstractWrapperObjectModelMetadataImpl declaringWrapperObjectModelMetadata() {
            return this.declaringObjectModelMetadata;
        }
        
        protected final Property getBaseProperty() {
            return this.baseProperty;
        }
        
        public final Method getGetterMethod() {
            Method getterMethod = this.getterMethod;
            if (getterMethod == null) {
                try {
                    getterMethod = 
                            this
                            .getDeclaringObjectModelMetadata()
                            .getObjectModelClass()
                            .getMethod(
                                    this.baseProperty.getGetterName(), 
                                    EMPTY_CLASSES);
                } catch (NoSuchMethodException ex) {
                    throw new AssertionError();
                }
                this.getterMethod = getterMethod;
            }
            return getterMethod;
        }
        
        @Override
        public String toString() {
            return this.baseProperty.toString();
        }

        protected void beforeParsing() {}
        
        protected void afterParsing(PostContext postContext) {}
        
        protected final Object writeReplace() throws ObjectStreamException {
            return new PropertyReplacement(this);
        }
    }
    
    protected static class WrapperScalarPropertyImpl 
    extends AbstractWrapperPropertyImpl
    implements ScalarProperty {

        private static final long serialVersionUID = 3259674327155828095L;
        
        private AbstractWrapperObjectModelMetadataImpl returnObjectModelMetadata;

        public WrapperScalarPropertyImpl(ScalarProperty baseProperty, PreContext preContext) {
            super(baseProperty, preContext);
            ObjectModelMetadata baseDependentMetadata = baseProperty.getReturnObjectModelMetadata();
            if (baseDependentMetadata != null) {
                this.returnObjectModelMetadata = 
                        preContext
                        .getOrCreateWrapperObjectModelMetadata(
                                baseDependentMetadata.getOwnerClass()
                        );
            }
        }

        @Override
        public ObjectModelMetadata getDeclaringObjectModelMetadata() {
            return this.declaringWrapperObjectModelMetadata();
        }

        @Override
        public final Type getReturnType() {
            return this.baseProperty.getReturnType();
        }

        @Override
        public final boolean isDeferrable() {
            return ((ScalarProperty)this.baseProperty).isDeferrable();
        }

        @Override
        public final boolean isEmbeded() {
            return ((ScalarProperty)this.baseProperty).isEmbeded();
        }

        @Override
        public ObjectModelMetadata getReturnObjectModelMetadata() {
            return this.returnObjectModelMetadata();
        }

        protected final AbstractWrapperObjectModelMetadataImpl returnObjectModelMetadata() {
            return this.returnObjectModelMetadata;
        }
    }
    
    protected static class WrapperAssociationPropertyImpl
    extends AbstractWrapperPropertyImpl
    implements AssociationProperty {
        
        private static final long serialVersionUID = 3719819749233863350L;

        private AbstractWrapperObjectModelMetadataImpl keyObjectModelMetadata;
        
        private AbstractWrapperObjectModelMetadataImpl returnObjectModelMetadata;
        
        private WrapperAssociationPropertyImpl oppositeProperty;
        
        private WrapperAssociationPropertyImpl covarianceProperty;
        
        private XOrderedMap<String, WrapperScalarPropertyImpl> comparatorProperties;
        
        protected WrapperAssociationPropertyImpl(
                AssociationProperty baseAssociationProperty, PreContext preContext) {
            
            super(baseAssociationProperty, preContext);
            
            /*
             * Create and cache the opposite AbstractWrapperAssociatedClasses/AbstractWrapperProperty
             * to prepare to build the relationship between the AbstractWrapperProperties
             */
            Class<?> oppositeOwnerClass = baseAssociationProperty.getReturnObjectModelMetadata().getOwnerClass();
                
            preContext.getOrCreateWrapperObjectModelMetadata(oppositeOwnerClass);
            
            ObjectModelMetadata keyObjectModelMetadata = 
                    baseAssociationProperty.getKeyObjectModelMetadata();
            if (keyObjectModelMetadata != null) {
                this.keyObjectModelMetadata = preContext.getOrCreateWrapperObjectModelMetadata(
                        keyObjectModelMetadata.getOwnerClass()
                );
            }
            this.returnObjectModelMetadata = preContext.getOrCreateWrapperObjectModelMetadata(
                    baseAssociationProperty.getReturnObjectModelMetadata().getOwnerClass()
            );
        }
        
        protected void beforeParsing() {}
        
        protected void parse() {}
        
        protected final AbstractWrapperObjectModelMetadataImpl keyObjectModelMetadata() {
            return this.keyObjectModelMetadata;
        }
        
        protected final AbstractWrapperObjectModelMetadataImpl returnObjectModelMetadata() {
            return this.returnObjectModelMetadata;
        }
        
        protected final WrapperAssociationPropertyImpl oppositeProperty() {
            return this.oppositeProperty;
        }
        
        protected final WrapperAssociationPropertyImpl covarianceProperty() {
            return this.covarianceProperty;
        }
        
        protected final XOrderedMap<String, WrapperScalarPropertyImpl> comparatorProperties() {
            return this.comparatorProperties;
        }

        @Override
        public final AssociatedEndpointType getAssociatedEndpointType() {
            return ((AssociationProperty)this.baseProperty).getAssociatedEndpointType();
        }

        @Override
        public final Class<?> getStandardReturnClass() {
            return ((AssociationProperty)this.baseProperty).getStandardReturnClass();
        }
        
        @Override
        public final boolean isCollection() {
            return ((AssociationProperty)this.baseProperty).isCollection();
        }

        @Override
        public final boolean isReference() {
            return ((AssociationProperty)this.baseProperty).isReference();
        }

        @Override
        public final ParameterizedType getReturnType() {
            return ((AssociationProperty)this.baseProperty).getReturnType();
        }

        @Override
        public final Class<?> getKeyClass() {
            return ((AssociationProperty)this.baseProperty).getKeyClass();
        }

        @Override
        public ObjectModelMetadata getKeyObjectModelMetadata() {
            return this.keyObjectModelMetadata();
        }

        @Override
        public ObjectModelMetadata getReturnObjectModelMetadata() {
            return this.returnObjectModelMetadata();
        }

        @Override
        public final Class<?> getElementClass() {
            return ((AssociationProperty)this.baseProperty).getElementClass();
        }

        @Override
        public ObjectModelMetadata getDeclaringObjectModelMetadata() {
            return this.declaringWrapperObjectModelMetadata();
        }

        @Override
        public AssociationProperty getOppositeProperty() {
            return this.oppositeProperty();
        }
        
        @Override
        public AssociationProperty getCovarianceProperty() {
            return this.covarianceProperty();
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public XOrderedMap<String, ScalarProperty> getComparatorProperties() {
            return (XOrderedMap)this.comparatorProperties();
        }

        @Override
        public final UnifiedComparator<?> getKeyUnifiedComparator() {
            return ((AssociationProperty)this.baseProperty).getKeyUnifiedComparator();
        }

        @Override
        public final UnifiedComparator<?> getCollectionUnifiedComparator() {
            AssociationProperty oppositeProperty = this.getOppositeProperty();
            if (oppositeProperty == null) {
                return UnifiedComparator.empty();
            }
            String[] keyPropertyNames = 
                    ((AssociationProperty)this.baseProperty)
                    .getComparatorProperties()
                    .keySet()
                    .toArray(EMPTY_STRINGS);
            if (SortedSet.class.isAssignableFrom(this.baseProperty.getReturnClass())) {
                Comparator<?> comparator = 
                        oppositeProperty
                        .getDeclaringObjectModelMetadata()
                        .getOwnerComparator(keyPropertyNames);
                return UnifiedComparator.of(comparator);
            }
            EqualityComparator<?> equalityComparator = 
                    oppositeProperty
                    .getDeclaringObjectModelMetadata()
                    .getOwnerEqualityComparator(keyPropertyNames);
            return UnifiedComparator.of(equalityComparator);
        }
        
        protected void onOppositeAndCovarianceSetted() {
            
        }
        
        private void setOppositeAndCovariance(
                WrapperAssociationPropertyImpl oppositeProperty,
                WrapperAssociationPropertyImpl covarianceProperty) {
            this.oppositeProperty = oppositeProperty;
            this.covarianceProperty = covarianceProperty;
            this.onOppositeAndCovarianceSetted();
        }
        
    }
    
    protected static class PreContext {
        
        private AbstractWrapperMetadatas declaringMetadatas;
        
        private final Map<Class<?>, AbstractWrapperObjectModelMetadataImpl> wrapperObjectModelMetadatas =
            new LinkedHashMap<Class<?>, AbstractWrapperObjectModelMetadataImpl>();
        
        private final Map<Class<?>, AbstractWrapperObjectModelMetadataImpl> oldWrapperObjectModelMetadatas;
        
        private final Map<Property, AbstractWrapperPropertyImpl> propertyTransfromMap =
            new HashMap<Property, AbstractWrapperPropertyImpl>();
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        private PreContext(AbstractWrapperMetadatas declaringMetadatas) {
            this.declaringMetadatas = declaringMetadatas;
            this.oldWrapperObjectModelMetadatas = (Map)MACollections.unmodifiable(declaringMetadatas.cache);
        }
        
        final AbstractWrapperObjectModelMetadataImpl getWrapperObjectModelMetadata(Class<?> ownerClass) {
            AbstractWrapperObjectModelMetadataImpl ret = this.wrapperObjectModelMetadatas.get(ownerClass);
            if (ret == null) {
                ret = this.oldWrapperObjectModelMetadatas.get(ownerClass);
            }
            return ret;
        }
        
        final AbstractWrapperObjectModelMetadataImpl getOrCreateWrapperObjectModelMetadata(Class<?> ownerClass) {
            AbstractWrapperObjectModelMetadataImpl ret = this.wrapperObjectModelMetadatas.get(ownerClass);
            if (ret != null) {
                return ret;
            }
            ret = this.oldWrapperObjectModelMetadatas.get(ownerClass);
            if (ret != null) {
                return ret;
            }
            ret = this.declaringMetadatas.createWrapperObjectModelMetadataImpl(ownerClass, this);
            if (ret == null) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().methodMustNotReturnNull(
                                this.declaringMetadatas.getClass(), 
                                "createWrapperObjectModelMetadataImpl", 
                                Class.class, 
                                PreContext.class)
                );
            }
            if (!ret.baseObjectModelMetadata.getOwnerClass().isAssignableFrom(ownerClass)) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().methodMustReturnWrapperOfFirstParameter(
                                this.declaringMetadatas.getClass(), 
                                "createWrapperObjectModelMetadataImpl", 
                                Class.class, 
                                PreContext.class)
                );
            }
            return ret;
        }
        
        final AbstractWrapperPropertyImpl tranformProperty(Property baseProperty) {
            if (baseProperty == null) {
                return null;
            }
            AbstractWrapperPropertyImpl transformed = this.propertyTransfromMap.get(baseProperty);
            if (transformed == null) {
                transformed =
                (AbstractWrapperPropertyImpl)
                this
                .oldWrapperObjectModelMetadatas
                .get(baseProperty.getDeclaringObjectModelMetadata().getOwnerClass())
                .getDeclaredProperty(baseProperty.getId());
            }
            return transformed;
        }
        
        final WrapperScalarPropertyImpl tranformProperty(ScalarProperty baseProperty) {
            return (WrapperScalarPropertyImpl)this.tranformProperty((Property)baseProperty);
        }
        
        final WrapperAssociationPropertyImpl tranformProperty(AssociationProperty baseProperty) {
            return (WrapperAssociationPropertyImpl)this.tranformProperty((Property)baseProperty);
        }
    }
    
    protected static class PostContext {
        
        final PreContext preContext;
        
        Map<Object, Set<AbstractWrapperObjectModelMetadataImpl>> proccessedMetadataMap = new HashMap<>();
        
        Map<Object, Set<AbstractWrapperPropertyImpl>> proccessedPropertyMap = new HashMap<>();
        
        int phase;
        
        private PostContext(PreContext preContext) {
            this.preContext = preContext;
        }
        
        @SuppressWarnings("unchecked")
        public <P extends Property> P getWrapperProperty(Property property) {
            AbstractWrapperPropertyImpl wrapper = 
                    this.preContext.propertyTransfromMap.get(property);
            if (wrapper == null) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().isNotBaseProperty(
                                "property",
                                property.getDeclaringObjectModelMetadata().getObjectModelClass(),
                                property.getName()
                        )
                );
            }
            return (P)wrapper;
        }
        
        @SuppressWarnings("unchecked")
        public <M extends ObjectModelMetadata> M getWrapperObjectMetadata(Class<?> ownerClass) {
            AbstractWrapperObjectModelMetadataImpl wrapper = 
                    this.preContext.getWrapperObjectModelMetadata(ownerClass);
            if (wrapper == null) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().noWrapperObjectModelMetadata("ownerClass", ownerClass)
                );
            }
            return (M)wrapper;
        }
        
        @SuppressWarnings("unchecked")
        public <M extends ObjectModelMetadata> M getBaseObjectMetadata(Class<?> ownerClass) {
            return (M)this.<AbstractWrapperObjectModelMetadataImpl>getWrapperObjectMetadata(ownerClass).baseObjectModelMetadata;
        }
        
        @SuppressWarnings("unchecked")
        public <P extends Property> P getBaseProperty(AbstractWrapperPropertyImpl wrapperProperty) {
            return (P)wrapperProperty.baseProperty;
        }
        
        public int getPhase() {
            return this.phase;
        }
        
        public boolean isProcessed(Object processingIdentifier, AbstractWrapperObjectModelMetadataImpl metadata) {
            if (metadata.finished) {
                return true;
            }
            Set<AbstractWrapperObjectModelMetadataImpl> set = this.proccessedMetadataMap.get(processingIdentifier);
            if (set != null && set.contains(metadata)) {
                return true;
            }
            if (set == null) {
                set = new HashSet<>(ReferenceEqualityComparator.getInstance());
                this.proccessedMetadataMap.put(processingIdentifier, set);
            }
            set.add(metadata);
            return false;
        }
        
        public boolean isProcessed(Object processingIdentifier, AbstractWrapperPropertyImpl property) {
            if (property.declaringObjectModelMetadata.finished) {
                return true;
            }
            Set<AbstractWrapperPropertyImpl> set = this.proccessedPropertyMap.get(processingIdentifier);
            if (set != null && set.contains(property)) {
                return true;
            }
            if (set == null) {
                set = new HashSet<>(ReferenceEqualityComparator.getInstance());
                this.proccessedPropertyMap.put(processingIdentifier, set);
            }
            set.add(property);
            return false;
        }
    }
    
    private static class ObjectModelMetadataReplacement implements Serializable {
        
        private static final long serialVersionUID = 7166724599329991041L;
        private AbstractWrapperObjectModelMetadataImpl target;
        
        ObjectModelMetadataReplacement(AbstractWrapperObjectModelMetadataImpl target) {
            this.target = target;
        }
        
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.writeObject(this.target.metadatas);
            out.writeObject(this.target.getOwnerClass());
        }
        
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            AbstractWrapperMetadatas metadatas = (AbstractWrapperMetadatas)in.readObject();
            Class<?> ownerClass = (Class<?>)in.readObject();
            this.target = metadatas.getWrapperObjectModelMetadataImpl(ownerClass);
        }
        
        private Object readResolve() throws ObjectStreamException {
            return this.target;
        }
    }
    
    private static class PropertyReplacement implements Serializable {
        
        private static final long serialVersionUID = -6897346867589046044L;
        
        private Property target;
        
        PropertyReplacement(AbstractWrapperPropertyImpl target) {
            this.target = target;
        }
        
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.writeObject(this.target.getDeclaringObjectModelMetadata());
            out.writeInt(this.target.getId());
        }
        
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            ObjectModelMetadata objectModelMetadata = (ObjectModelMetadata)in.readObject();
            this.target = objectModelMetadata.getProperty(in.readInt());
        }
        
        private Object readResolve() throws ObjectStreamException {
            return this.target;
        }
    }
    
    private interface Resource {

        String methodMustNotReturnNull(
                Class<?> runtimeType,
                String methodName, 
                Class<?> ... parameterTypes);
        
        String methodMustReturnWrapperOfFirstParameter(
                Class<?> runtimeType,
                String methodName, 
                Class<?> ... parameterTypes);
        
        String invokeMethodTooEarly(
                String methodName,
                Class<?> ... parameterTypes);

        String noProperty(Class<?> objectModelClass, String name);
        
        String noScalarProperty(Class<?> objectModelClass, String name);
        
        String nosAssociationProperty(Class<?> objectModelClass, String name);
        
        String specifiedPropertyIsNotScalar(Class<?> objectModelClass, int id);
        
        String specifiedPropertyIsNotAssociation(Class<?> objectModelClass, int id);
        
        String isNotBaseProperty(
                String parameterName, 
                Class<?> objectModelClass, 
                String propertyName);
        
        String noWrapperObjectModelMetadata(
                String paramaterName, 
                Class<?> ownerClass);
    }
    
}
