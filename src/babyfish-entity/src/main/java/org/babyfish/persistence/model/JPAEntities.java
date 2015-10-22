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
package org.babyfish.persistence.model;

import java.lang.reflect.Array;
import java.lang.reflect.Member;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.HashMap;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.Nulls;
import org.babyfish.model.ObjectModel;
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.metadata.Metadatas;
import org.babyfish.model.metadata.ObjectModelMetadata;
import org.babyfish.model.metadata.ObjectModelMode;
import org.babyfish.model.metadata.Property;
import org.babyfish.model.metadata.ScalarProperty;
import org.babyfish.persistence.model.metadata.JPAMetadatas;
import org.babyfish.persistence.model.metadata.JPAObjectModelMetadata;
import org.babyfish.persistence.model.metadata.JPAProperty;
import org.babyfish.persistence.model.metadata.JPAScalarProperty;
import org.babyfish.reference.Reference;
import org.babyfish.util.LazyResource;

/**
 * @author Tao Chen
 */
public class JPAEntities {

    private static final LazyResource<Resource> LAZY_RESOURCE = LazyResource.of(Resource.class);
    
    private static final String LINE_PREFIX = System.getProperty("line.separator") + '\t';
    
    private static final int FG_REQURED = 1 << 0;
    
    private static final int FG_NOT_NULL = 1 << 1;
    
    private static final int FG_NOT_EMPTY = 1 << 2 | FG_NOT_NULL;
    
    private static final String VALIDATE_METHOD = "validateMaxEnabledRange";
    
    private static final String SPECIAL_ATTRIBUTE_METHODS = 
            "required, notNull, notEmpty";

    @Deprecated
    protected JPAEntities() {
        throw new UnsupportedOperationException();
    }
    
    public static void disableAll(Object entity) {
        validateArgument("entity", entity);
        ObjectModelFactory<?> objectModelFactory = Metadatas.of(entity.getClass()).getFactory();
        JPAObjectModelMetadata objectModelMetadata = objectModelFactory.getObjectModelMetadata();
        if (objectModelMetadata.getMode() != ObjectModelMode.REFERENCE) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeReferenceObjectModel(
                            "entity", 
                            ObjectModelMode.REFERENCE
                    )
            );
        }
        ObjectModel objectModel = (ObjectModel)objectModelFactory.get(entity);
        for (Property property : objectModelMetadata.getProperties().values()) {
            objectModel.disable(property.getId());
        }
    }
    
    @SafeVarargs
    public static <E> void enable(E entity, Attribute<? super E, ?> ... attributes) {
        validateArgument("entity", entity);
        ObjectModelFactory<?> objectModelFactory = Metadatas.of(entity.getClass()).getFactory();
        JPAObjectModelMetadata objectModelMetadata = objectModelFactory.getObjectModelMetadata();
        if (objectModelMetadata.getMode() != ObjectModelMode.REFERENCE) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeReferenceObjectModel(
                            "entity", 
                            ObjectModelMode.REFERENCE
                    )
            );
        }
        ObjectModel objectModel = (ObjectModel)objectModelFactory.get(entity);
        for (Attribute<? super E, ?> attribute : attributes) {
            if (attribute instanceof SpecialAttribute<?>) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotContainSpecialAttribute(
                                "attributes", 
                                VALIDATE_METHOD, 
                                Attribute.class, 
                                SPECIAL_ATTRIBUTE_METHODS
                        )
                );
            }
            objectModel.enable(getProperty(objectModelMetadata, attribute).getId());
        }
    }
    
    @SafeVarargs
    public static <E> void disable(E entity, Attribute<? super E, ?> ... attributes) {
        validateArgument("entity", entity);
        ObjectModelFactory<?> objectModelFactory = Metadatas.of(entity.getClass()).getFactory();
        JPAObjectModelMetadata objectModelMetadata = objectModelFactory.getObjectModelMetadata();
        if (objectModelMetadata.getMode() != ObjectModelMode.REFERENCE) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeReferenceObjectModel(
                            "entity", 
                            ObjectModelMode.REFERENCE
                    )
            );
        }
        ObjectModel objectModel = (ObjectModel)objectModelFactory.get(entity);
        for (Attribute<? super E, ?> attribute : attributes) {
            if (attribute instanceof SpecialAttribute<?>) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotContainSpecialAttribute(
                                "attributes", 
                                VALIDATE_METHOD, 
                                Attribute.class, 
                                SPECIAL_ATTRIBUTE_METHODS
                        )
                );
            }
            objectModel.disable(getProperty(objectModelMetadata, attribute).getId());
        }
    }
    
    @SafeVarargs
    public static <E> Comparator<E> comparator(Class<E> entityType, Attribute<? super E, ?> ... attributes) {
        Arguments.mustNotBeNull("entityType", entityType);
        Arguments.mustNotBeNull("attributes", attributes);
        Arguments.mustNotBeEmpty("attributes", attributes);
        JPAObjectModelMetadata objectModelMetadata = JPAMetadatas.of(entityType);
        Set<Integer> propertyIdSet = new LinkedHashSet<>();
        for (Attribute<? super E, ?> attribute : attributes) {
            Arguments.mustNotBeNull("attributes[?]", attribute);
            if (attribute instanceof SpecialAttribute<?>) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotContainSpecialAttribute(
                                "attributes", 
                                VALIDATE_METHOD, 
                                Attribute.class, 
                                SPECIAL_ATTRIBUTE_METHODS
                        )
                );
            }
            JPAProperty property = getProperty(objectModelMetadata, attribute);
            if (!(property instanceof ScalarProperty)) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustBeScalarAttribute(
                                "attributes", 
                                attribute.getName(),
                                property,
                                ScalarProperty.class
                        )
                );
            }
            propertyIdSet.add(property.getId());
        }
        int[] propertyIds = MACollections.toIntArray(propertyIdSet);
        return objectModelMetadata.getOwnerComparator(propertyIds);
    }
    
    @SafeVarargs
    public static <E> EqualityComparator<E> equalityComparator(Class<E> entityType, Attribute<? super E, ?> ... attributes) {
        Arguments.mustNotBeNull("entityType", entityType);
        JPAObjectModelMetadata objectModelMetadata = JPAMetadatas.of(entityType);
        if (Nulls.isNullOrEmpty(attributes)) {
            return objectModelMetadata.getOwnerEqualityComparator(); 
        }
        Set<Integer> propertyIdSet = new LinkedHashSet<>();
        for (Attribute<? super E, ?> attribute : attributes) {
            Arguments.mustNotBeNull("attributes[?]", attribute);
            if (attribute instanceof SpecialAttribute<?>) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotContainSpecialAttribute(
                                "attributes", 
                                VALIDATE_METHOD, 
                                Attribute.class, 
                                SPECIAL_ATTRIBUTE_METHODS
                        )
                );
            }
            JPAProperty property = getProperty(objectModelMetadata, attribute);
            if (!(property instanceof ScalarProperty)) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustBeScalarAttribute(
                                "attributes", 
                                attribute.getName(),
                                property,
                                ScalarProperty.class
                        )
                );
            }
            propertyIdSet.add(property.getId());
        }
        int[] propertyIds = MACollections.toIntArray(propertyIdSet);
        return objectModelMetadata.getOwnerEqualityComparator(propertyIds);
    }
    
    @SafeVarargs
    public static <E> void validateMaxEnabledRange(E entity, Attribute<? super E, ?> ... maxEnabledAttributeRange) {
        validateArgument("entity", entity);
        ObjectModelFactory<?> objectModelFactory = Metadatas.of(entity.getClass()).getFactory();
        JPAObjectModelMetadata objectModelMetadata = objectModelFactory.getObjectModelMetadata();
        JPAScalarProperty entityIdProperty = objectModelMetadata.getEntityIdProperty();
        Map<Integer, Integer> map = new HashMap<>((maxEnabledAttributeRange.length * 4 + 2) / 3);
        for (Attribute<? super E, ?> attribute : maxEnabledAttributeRange) {
            int flags = 0;
            if (attribute instanceof SpecialAttribute<?>) {
                SpecialAttribute<?> sa = (SpecialAttribute<?>)attribute;
                if (sa != null) {
                    flags = sa.flags;
                }
            }
            map.put(getProperty(objectModelMetadata, attribute).getId(), flags);
        }
        ObjectModel objectModel = (ObjectModel)objectModelFactory.get(entity);
        StringBuilder builder = null;
        for (Property property : objectModel.getObjectModelMetadata().getProperties().values()) {
            if (property == entityIdProperty) {
                continue;
            }
            JPAProperty jpaProperty = (JPAProperty)property;
            int propertyId = property.getId();
            Integer flags = map.get(property.getId());
            if (flags == null) {
                if (!objectModel.isDisabled(propertyId)) {
                    builder = lazyAppend(
                            builder, 
                            objectModelMetadata, 
                            LAZY_RESOURCE.get().mustBeDisabled(jpaProperty.getOwnerProperty().getName())
                    );
                }
            } else {
                if (objectModel.isDisabled(propertyId)) {
                    if ((flags & FG_REQURED) != 0) {
                        builder = lazyAppend(
                                builder, 
                                objectModelMetadata, 
                                LAZY_RESOURCE.get().mustBeEnabled(jpaProperty.getOwnerProperty().getName())
                        );
                    }
                    continue;
                }
                if ((flags & ~FG_REQURED) == 0) {
                    continue;
                }
                Object propertyValue;
                if (property instanceof ScalarProperty) {
                    propertyValue = objectModel.getScalar(propertyId);
                } else {
                    propertyValue = objectModel.getAssociation(propertyId);
                }
                if ((flags & FG_NOT_NULL) != 0) {
                    if (propertyValue == null) {
                        builder = lazyAppend(
                                builder, 
                                objectModelMetadata, 
                                LAZY_RESOURCE.get().mustNotBeNull(jpaProperty.getOwnerProperty().getName())
                        );
                    }
                }
                if ((flags & FG_NOT_EMPTY) != 0) {
                    boolean isEmpty = false;
                    if (propertyValue instanceof String) {
                        isEmpty = ((String)propertyValue).isEmpty();
                    } else if (propertyValue instanceof Collection<?>) {
                        isEmpty = ((Collection<?>)propertyValue).isEmpty();
                    } else if (propertyValue instanceof Map<?, ?>) {
                        isEmpty = ((Map<?, ?>)propertyValue).isEmpty();
                    } else if (propertyValue != null && propertyValue.getClass().isArray()) {
                        isEmpty = Array.getLength(propertyValue) == 0;
                    }
                    if (isEmpty) {
                        builder = lazyAppend(
                                builder, 
                                objectModelMetadata, 
                                LAZY_RESOURCE.get().mustNotBeEmpty(jpaProperty.getOwnerProperty().getName())
                        );
                    }
                }
            }
        }
        if (builder != null) {
            throw new IllegalArgumentException(builder.toString());
        }
    }
    
    private static StringBuilder lazyAppend(
            StringBuilder builder, 
            ObjectModelMetadata objectModelMetadata,
            String message) { 
        if (builder == null) {
            builder = new StringBuilder(LAZY_RESOURCE.get().validationErrorMessagePrefix(objectModelMetadata.getOwnerClass()));
        }
        builder
        .append(LINE_PREFIX)
        .append(message);
        return builder;
    }
    
    public static <E> boolean isEnabled(E entity, Attribute<? super E, ?> attribute) {
        return !isDisabled(entity, attribute);
    }
    
    public static <E> boolean isDisabled(E entity, Attribute<? super E, ?> attribute) {
        validateArgument("entity", entity);
        ObjectModelFactory<?> objectModelFactory = Metadatas.of(entity.getClass()).getFactory();
        JPAObjectModelMetadata objectModelMetadata = objectModelFactory.getObjectModelMetadata();
        ObjectModel objectModel = (ObjectModel)objectModelFactory.get(entity);
        return objectModel.isDisabled(getProperty(objectModelMetadata, attribute).getId());
    }
    
    public static <E> boolean isIdEquals(E entity1, E entity2) {
        validateArgument("entity1", entity1);
        validateArgument("entity2", entity2);
        ObjectModelFactory<?> objectModelFactory = Metadatas.of(entity1.getClass()).getFactory();
        ObjectModelFactory<?> objectModelFactory2 = Metadatas.of(entity2.getClass()).getFactory();
        if (objectModelFactory != objectModelFactory2) {
            return false;
        }
        JPAObjectModelMetadata objectModelMetadata = objectModelFactory.getObjectModelMetadata();
        ObjectModel objectModel1 = (ObjectModel)objectModelFactory.get(entity1);
        ObjectModel objectModel2 = (ObjectModel)objectModelFactory.get(entity2);
        int entityIdPropertyId = objectModelMetadata.getEntityIdProperty().getId();
        Object id1 = objectModel1.getScalar(entityIdPropertyId);
        Object id2 = objectModel2.getScalar(entityIdPropertyId);
        return Nulls.equals(id1, id2);
    }
    
    public static <E> E createFakeEntity(Class<E> entityType, Object id) {
        Arguments.mustNotBeNull("entityType", entityType);
        Arguments.mustNotBeNull("id", id);
        ObjectModelFactory<?> objectModelFactory = Metadatas.of(entityType).getFactory();
        JPAObjectModelMetadata objectModelMetadata = objectModelFactory.getObjectModelMetadata();
        JPAScalarProperty entityIdProperty = objectModelMetadata.getEntityIdProperty();
        E entity;
        try {
            entity  = entityType.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().newObjectFailed(entityType),
                    ex
            );
        }
        disableAll(entity);
        ((ObjectModel)objectModelFactory.get(entity)).setScalar(entityIdProperty.getId(), id);
        return entity;
    }
    
    public static <E> Set<E> createFakeEntities(Class<E> entityType, Iterable<?> ids) {
        Arguments.mustNotBeNull("entityType", entityType);
        if (Nulls.isNullOrEmpty(ids)) {
            return MACollections.emptySet();
        }
        Set<E> entities;
        if (ids instanceof Iterable<?>) {
            entities = new LinkedHashSet<>((((Collection<?>)ids).size() * 4 + 2) / 3);
        } else {
            entities = new LinkedHashSet<>();
        }
        ObjectModelFactory<?> objectModelFactory = Metadatas.of(entityType).getFactory();
        JPAObjectModelMetadata objectModelMetadata = objectModelFactory.getObjectModelMetadata();
        JPAScalarProperty entityIdProperty = objectModelMetadata.getEntityIdProperty();
        int entityIdPropertyId = entityIdProperty.getId();
        for (Object id : ids) {
            E entity;
            try {
                entity  = entityType.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().newObjectFailed(entityType),
                        ex
                );
            }
            disableAll(entity);
            ((ObjectModel)objectModelFactory.get(entity)).setScalar(entityIdPropertyId, id);
            entities.add(entity);
        }
        return entities;
    }
    
    public static <E> Set<E> createFakeEntities(Class<E> entityType, Object ... ids) {
        return createFakeEntities(entityType, MACollections.wrap(ids));
    }
    
    @SuppressWarnings("unchecked")
    public static <E, A> XOrderedSet<A> extractAttribute(Collection<E> entities, SingularAttribute<? super E, A> attribute) {
        XOrderedSet<A> values;
        if (entities.size() < 1024) {
             values = new LinkedHashSet<>((entities.size() * 4 + 2) / 3);
        } else {
            values = new LinkedHashSet<>();
        }
        for (E entity : entities) {
            validateArgument("entity", entity);
            ObjectModelFactory<?> objectModelFactory = Metadatas.of(entity.getClass()).getFactory();
            JPAObjectModelMetadata objectModelMetadata = objectModelFactory.getObjectModelMetadata();
            ObjectModel objectModel = (ObjectModel)objectModelFactory.get(entity);
            JPAProperty property = getProperty(objectModelMetadata, attribute);
            if (property instanceof ScalarProperty) {
                values.add((A)objectModel.getScalar(property.getId()));   
            } else {
                Reference<?> reference = (Reference<?>)objectModel.getAssociation(property.getId());
                values.add((A)reference.get());
            }
        }
        return values;
    }

    public static <E> Attribute<E, ?> required(Attribute<E, ?> attribute) {
        return specialAttribute(attribute, FG_REQURED);
    }

    public static <E> Attribute<E, ?> notNull(Attribute<E, ?> attribute) {
        return specialAttribute(attribute, FG_NOT_NULL);
    }
    
    public static <E> Attribute<E, ?> notEmpty(Attribute<E, ?> attribute) {
        return specialAttribute(attribute, FG_NOT_EMPTY);
    }
    
    private static <E> Attribute<E, ?> specialAttribute(Attribute<E, ?> attribute, int flags) {
        Arguments.mustNotBeNull("attribute", attribute);
        int oldFlags = 
                attribute instanceof SpecialAttribute<?> ? 
                        ((SpecialAttribute<?>)attribute).flags : 
                        0;
        return new SpecialAttribute<>(attribute, flags | oldFlags);
    }
    
    private static void validateArgument(String name, Object entity) {
        Arguments.mustNotBeNull(name, entity);
        Arguments.mustNotBeArray(name, entity.getClass());
        Arguments.mustNotBePrimitive(name, entity.getClass());
    }
    
    private static JPAProperty getProperty(JPAObjectModelMetadata objectModelMetadata, Attribute<?, ?> attribute) {
        String attributeName = attribute.getName();
        JPAProperty property = objectModelMetadata.getMappingSources().get(attributeName);
        if (property == null) {
            property = objectModelMetadata.getIndexMappingSources().get(attributeName);
            if (property == null) {
                property = objectModelMetadata.getKeyMappingSources().get(attributeName);
                if (property == null) {
                    throw new IllegalArgumentException(
                            LAZY_RESOURCE.get().noAttribute(objectModelMetadata.getOwnerClass(), attribute.getName())
                    );
                }
            }
        }
        return property;
    }
    
    private static class SpecialAttribute<E> implements Attribute<E, Object> {
        
        Attribute<E, ?> raw;
        
        int flags;

        @SuppressWarnings("unchecked")
        public SpecialAttribute(Attribute<E, ?> raw, int flags) {
            if (raw instanceof SpecialAttribute<?>) {
                SpecialAttribute<E> sa = (SpecialAttribute<E>)raw;
                this.raw = sa.raw;
                this.flags = sa.flags | flags;
            } else {
                this.raw = raw;
                this.flags = flags;
            }
        }

        public ManagedType<E> getDeclaringType() {
            return this.raw.getDeclaringType();
        }

        public Member getJavaMember() {
            return this.raw.getJavaMember();
        }

        @SuppressWarnings("unchecked")
        public Class<Object> getJavaType() {
            return (Class<Object>)this.raw.getJavaType();
        }

        public String getName() {
            return this.raw.getName();
        }

        public javax.persistence.metamodel.Attribute.PersistentAttributeType getPersistentAttributeType() {
            return this.raw.getPersistentAttributeType();
        }

        public boolean isAssociation() {
            return this.raw.isAssociation();
        }

        public boolean isCollection() {
            return this.raw.isCollection();
        }
    }
    
    private interface Resource {
        
        String validationErrorMessagePrefix(Class<?> ownerType);
        
        String mustBeDisabled(String attributeName);
        
        String mustBeEnabled(String attributeName);
        
        String mustNotBeNull(String attributeName);
        
        String mustNotBeEmpty(String attributeName);
        
        String mustBeReferenceObjectModel(
                String argumentName, 
                ObjectModelMode referenceObjectModelMode);
        
        @SuppressWarnings("rawtypes")
        String mustNotContainSpecialAttribute(
                String argumentName,
                String validateMethodName,
                Class<Attribute> attributeTypeConstant,
                String specialAttributeMethods);
        
        String mustBeScalarAttribute(
                String argumentName,
                String attributeName,
                Property property,
                Class<ScalarProperty> scalarPropertyTypeConstant);
        
        String newObjectFailed(Class<?> type);
        
        String noAttribute(Class<?> ownerType, String attributeName);
    }
}
