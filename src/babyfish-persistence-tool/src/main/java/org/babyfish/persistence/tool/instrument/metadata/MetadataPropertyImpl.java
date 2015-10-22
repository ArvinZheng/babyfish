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
package org.babyfish.persistence.tool.instrument.metadata;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.MapKeyColumn;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.MapKeyJoinColumns;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.HashMap;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.lang.Nulls;
import org.babyfish.lang.reflect.asm.ASM;
import org.babyfish.org.objectweb.asm.tree.AnnotationNode;
import org.babyfish.org.objectweb.asm.tree.FieldNode;
import org.babyfish.org.objectweb.asm.tree.MethodNode;
import org.babyfish.persistence.instrument.ContravarianceInstrument;
import org.babyfish.persistence.instrument.JPAObjectModelInstrument;
import org.babyfish.persistence.instrument.NavigableInstrument;
import org.babyfish.persistence.instrument.ReferenceComparisonRuleInstrument;
import org.babyfish.reference.IndexedReference;
import org.babyfish.reference.KeyedReference;
import org.babyfish.reference.Reference;
import org.babyfish.util.Joins;
import org.babyfish.util.LazyResource;

/**
 * @author Tao Chen
 */
class MetadataPropertyImpl implements MetadataProperty {
    
    private static final LazyResource<Resource> LAZY_RESOURCE = LazyResource.of(Resource.class);

    private MetadataClassImpl declaringMetadataClass;
    
    private String name;
    
    private AccessType accessType;
    
    private String typeDescriptor;
    
    private String keyTypeDescriptor;
    
    private String getterName;
    
    private String setterName;
    
    private List<MetadataAnnotation> visibleAnnotations;
    
    private List<MetadataAnnotation> invisibleAnnotations;
    
    private Class<? extends Annotation> primaryAnnotationType;
    
    private boolean lob;
    
    private FetchType fetchType;
    
    private boolean any;
    
    private String columnName;
    
    private Map<MetadataProperty, String> embeddedColumnNames;
    
    private MetadataClassImpl relatedClass;
    
    private MetadataJoin join;
    
    private Class<?> standardAssocationType;
    
    private String referenceComparisonRule;
    
    private MetadataPropertyImpl oppositeIndexProperty;
    
    private MetadataPropertyImpl oppositeKeyProperty;
    
    private MetadataPropertyImpl oppositeProperty;
    
    private MetadataPropertyImpl indexProperty;
    
    private MetadataPropertyImpl keyProperty;
    
    private MetadataPropertyImpl referenceProperty;
    
    private MetadataPropertyImpl covarianceProperty;
    
    private boolean inverse;
    
    private boolean overridding;
    
    private Unresolved unresolved;
    
    MetadataPropertyImpl(MetadataClassImpl declaringModelClass, Object memberNode) {
        this.declaringMetadataClass = declaringModelClass;
        this.unresolved = new Unresolved();
        this.name = 
                memberNode instanceof FieldNode ? 
                        ((FieldNode)memberNode).name : 
                        Context.propertyName(((MethodNode)memberNode)
                );
        this.attach(memberNode);
    }
    
    MetadataPropertyImpl(MetadataClassImpl declaringModelClass, MetadataPropertyImpl property, String columnName) {
        this.declaringMetadataClass = declaringModelClass;
        this.name = property.name;
        this.getterName = property.getterName;
        this.setterName = property.setterName;
        this.typeDescriptor = property.typeDescriptor;
        this.columnName = columnName;
        this.primaryAnnotationType = property.primaryAnnotationType;
        this.overridding = true;
        this.unresolved = property.unresolved.clone();
    }

    @Override
    public MetadataClass getDeclaringClass() {
        return this.declaringMetadataClass;
    }

    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public AccessType getAccessType() {
        return this.accessType;
    }

    @Override
    public String getGetterName() {
        return this.getterName;
    }

    @Override
    public String getSetterName(boolean canReturnNull) {
        if (!canReturnNull && this.setterName == null) {
            String getterName = this.getGetterName();
            return "set" + getterName.substring(getterName.startsWith("is") ? 2 : 3);
        }
        return this.setterName;
    }
    
    @Override
    public String getTypeDescriptor() {
        return this.typeDescriptor;
    }
    
    @Override
    public String getKeyTypeDescriptor() {
        return this.keyTypeDescriptor;
    }

    public List<MetadataAnnotation> getVisibleAnnotations() {
        return this.visibleAnnotations;
    }

    public List<MetadataAnnotation> getInvisibleAnnotations() {
        return this.invisibleAnnotations;
    }

    @Override
    public String getColumnName() {
        return this.columnName;
    }

    @Override
    public Map<MetadataProperty, String> getEmbeddedColumnNames() {
        return this.embeddedColumnNames;
    }
    
    @Override
    public boolean isOverriding() {
        return this.overridding;
    }

    @Override
    public boolean isBasic() {
        return this.primaryAnnotationType == Basic.class;
    }

    @Override
    public boolean isLob() {
        return this.lob;
    }

    @Override
    public boolean isId() {
        return this.primaryAnnotationType == Id.class || 
                this.primaryAnnotationType == EmbeddedId.class;
    }

    @Override
    public boolean isVersion() {
        return this.primaryAnnotationType == Version.class;
    }

    @Override
    public boolean isEmbedded() {
        return this.primaryAnnotationType == Embedded.class ||
                this.primaryAnnotationType == EmbeddedId.class;
    }

    @Override
    public boolean isAssociation() {
        return this.isReference() || this.isCollection();
    }

    @Override
    public boolean isReference() {
        return this.primaryAnnotationType == OneToOne.class ||
                this.primaryAnnotationType == ManyToOne.class ||
                this.any;
    }
    
    @Override
    public boolean isOneToOne() {
        return this.primaryAnnotationType == OneToOne.class;
    }
    
    @Override
    public boolean isManyToOne() {
        return this.primaryAnnotationType == ManyToOne.class;
    }
    
    @Override
    public boolean isAny() {
        return this.any;
    }

    @Override
    public boolean isCollection() {
        return this.primaryAnnotationType == OneToMany.class ||
                this.primaryAnnotationType == ManyToMany.class;
    }
    
    @Override
    public boolean isOneToMany() {
        return this.primaryAnnotationType == OneToMany.class;
    }
    
    @Override
    public boolean isManyToMany() {
        return this.primaryAnnotationType == ManyToMany.class;
    }

    @Override
    public FetchType getFetchType() {
        return this.fetchType;
    }

    @Override
    public MetadataClass getRelatedClass() {
        return this.relatedClass;
    }

    @Override
    public MetadataJoin getJoin() {
        return this.join;
    }

    @Override
    public Class<?> getStandardAssocationType() {
        return this.standardAssocationType;
    }

    @Override
    public String getReferenceComparisonRule() {
        return this.referenceComparisonRule;
    }

    @Override
    public MetadataProperty getOppositeProperty() {
        return this.oppositeProperty;
    }
    
    @Override
    public MetadataProperty getOppositeIndexProperty() {
        return this.oppositeIndexProperty;
    }

    @Override
    public MetadataProperty getOppositeKeyProperty() {
        return this.oppositeKeyProperty;
    }

    @Override
    public MetadataProperty getIndexProperty() {
        return this.indexProperty;
    }

    @Override
    public MetadataProperty getKeyProperty() {
        return this.keyProperty;
    }

    @Override
    public MetadataProperty getReferenceProperty() {
        return this.referenceProperty;
    }

    @Override
    public MetadataProperty getCovarianceProperty() {
        return this.covarianceProperty;
    }

    @Override
    public boolean isInverse() {
        return this.inverse;
    }

    Object getPropertyNode() {
        return this.unresolved.propertyNode;
    }
    
    void attach(Object memberNode) {
        if (memberNode instanceof FieldNode) {
            this.unresolved.fieldNode = (FieldNode)memberNode;
        } else {
            this.unresolved.methodNode = (MethodNode)memberNode;
        }
    }
    
    void resolveSelf(Context context) {
        Unresolved unresolved = this.unresolved;
        AccessType fieldAccessType = accessType(unresolved.fieldNode);
        AccessType methodAccessType = accessType(unresolved.methodNode);
        if (fieldAccessType != null && methodAccessType != null && fieldAccessType != methodAccessType) {
            throw new MetadataException(
                    LAZY_RESOURCE.get().conflictAccessTypes(
                            this.declaringMetadataClass.getName(),
                            this.name,
                            Access.class
                    )
            );
        }
        AccessType accessType = 
                fieldAccessType != null ?
                fieldAccessType : (
                        methodAccessType != null ?
                        methodAccessType :
                        this.declaringMetadataClass.getAccessType()
                );
        if (accessType == AccessType.FIELD) {
            if (unresolved.fieldNode == null) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().noPropertyField(
                                this.declaringMetadataClass.getName(),
                                this.name,
                                AccessType.FIELD
                        )
                );
            }
            if (isJPAAnnotationPresent(unresolved.methodNode)) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().badMethodAnnotation(
                                this.declaringMetadataClass.getName(),
                                this.name,
                                AccessType.FIELD
                        )
                );
            }
            unresolved.propertyNode = unresolved.fieldNode;
        } else if (accessType == AccessType.PROPERTY) {
            if (unresolved.methodNode == null) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().noPropertyMethod(
                                this.declaringMetadataClass.getName(),
                                this.name,
                                AccessType.PROPERTY
                        )
                );
            }
            if (isJPAAnnotationPresent(unresolved.fieldNode)) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().badFieldAnnotation(
                                this.declaringMetadataClass.getName(),
                                this.name,
                                AccessType.PROPERTY
                        )
                );
            }
            unresolved.propertyNode = unresolved.methodNode;
        } else if (unresolved.fieldNode != null && unresolved.methodNode != null) {
            boolean useField = isJPAAnnotationPresent(unresolved.fieldNode);
            boolean useMethod = isJPAAnnotationPresent(unresolved.methodNode);
            if (useField && useMethod) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().unknownAccessType(
                                this.declaringMetadataClass.getName(),
                                this.name,
                                AccessType.class
                        )
                );
            }
            if (useMethod) {
                unresolved.propertyNode = unresolved.methodNode;
            } else {
                unresolved.propertyNode = unresolved.fieldNode;   
            }
        } else {
            unresolved.propertyNode = 
                    unresolved.fieldNode != null ? 
                            unresolved.fieldNode : 
                            unresolved.methodNode;
        }
        this.accessType = unresolved.propertyNode == unresolved.fieldNode ? AccessType.FIELD : AccessType.PROPERTY;
        this.afterAccessTypeDetermined(context);
    }
    
    void resolveEmbedded(Context context) {
        this.resolveEmbedded(context, new LinkedHashSet<MetadataPropertyImpl>());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    void resolveEmbedded(Context context, XOrderedSet<MetadataPropertyImpl> resolvingQueue) {
        if (this.embeddedColumnNames != null || !this.isEmbedded()) {
            return;
        }
        if (!resolvingQueue.add(this)) {
            throw new MetadataException(
                    LAZY_RESOURCE.get().embeddedCircular(
                            this.declaringMetadataClass.getName(),
                            this.name,
                            Embedded.class,
                            resolvingQueue
                    )
            );
        }
        try {
            String descriptor = this.typeDescriptor;
            descriptor = descriptor.substring(1, descriptor.length() - 1).replace('/', '.');
            MetadataClassImpl embeddedClass = (MetadataClassImpl)context.getModelClasses().get(descriptor);
            if (embeddedClass == null) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().unknownRelatedClass(
                                this.declaringMetadataClass.getName(),
                                this.name,
                                Embedded.class,
                                descriptor
                        )
                );
            }
            if (!embeddedClass.isEmbeddable()) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().embeddedTyepIsNotEmbeddable(
                                this.declaringMetadataClass.getName(),
                                this.name,
                                Embedded.class,
                                embeddedClass.getName(),
                                Embeddable.class
                        )
                );
            }
            if (this.declaringMetadataClass.isJPAObjectModelInstrument() != embeddedClass.isJPAObjectModelInstrument()) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().incongruousJPAObjectInstrument(
                                this.declaringMetadataClass.getName(),
                                this.name,
                                embeddedClass.getName(),
                                JPAObjectModelInstrument.class
                        )
                );
            }
            this.relatedClass = embeddedClass;
            Map<MetadataProperty, String> embeddedColumnNames = new LinkedHashMap<>();
            Map<String, String> overrideMap = this.getOverrideMap(); 
            Map<String, MetadataPropertyImpl> deeperProperties = 
                    (Map<String, MetadataPropertyImpl>)(Map)embeddedClass.getProperties();
            if (!overrideMap.isEmpty() && !deeperProperties.keySet().containsAll(overrideMap.keySet())) {
                List<String> unmatchedNames = new ArrayList<>(overrideMap.keySet());
                unmatchedNames.removeAll(deeperProperties.keySet());
                throw new MetadataException(
                        LAZY_RESOURCE.get().canNotOverrideDeepProperties(
                                this.declaringMetadataClass.getName(),
                                this.name,
                                AttributeOverride.class,
                                embeddedClass.getName(),
                                unmatchedNames
                        )
                );
            }
            for (MetadataPropertyImpl deeperProperty : deeperProperties.values()) {
                deeperProperty.resolveEmbedded(context, resolvingQueue);
                Map<MetadataProperty, String> deeperEmbeddedColumnNames = deeperProperty.getEmbeddedColumnNames();
                if (deeperEmbeddedColumnNames != null) {
                    if (overrideMap.containsKey(deeperProperty.getName())) {
                        throw new MetadataException();
                    }
                    embeddedColumnNames.putAll(deeperEmbeddedColumnNames);
                } else {
                    String overrideColumnName = overrideMap.get(deeperProperty.getName());
                    if (overrideColumnName != null) {
                        embeddedColumnNames.put(deeperProperty, overrideColumnName);
                    } else {
                        embeddedColumnNames.put(deeperProperty, deeperProperty.getColumnName());
                    }
                }
            }
            this.embeddedColumnNames = embeddedColumnNames;
        } finally {
            resolvingQueue.remove(this);
        }
    }

    void resolveJoin(Context context) {
        if (this.covarianceProperty != null || !this.isAssociation()) {
            return;
        }
        Unresolved unresolved = this.unresolved;
        String thisTypeDescriptor = typeDescriptor;
        AnnotationNode annotationNode = Context.getAnnotationNode(unresolved.propertyNode, this.primaryAnnotationType);
        unresolved.mappedBy = Nulls.emptyToNull(Context.<String>getAnnotationValue(annotationNode, "mappedBy"));
        this.decideTargetEntity(false, context);
        if (this.isCollection()) {
            if (thisTypeDescriptor.equals(ASM.getDescriptor(Map.class))) {
                if (Context.getAnnotationNode(unresolved.propertyNode, MapKeyJoinColumns.class) != null) {
                    throw new MetadataException(
                            LAZY_RESOURCE.get().annotationIsNotSupportedInObjectModel4JPA(
                                    this.declaringMetadataClass.getName(),
                                    this.name,
                                    MapKeyJoinColumns.class
                            )
                    );
                }
                if (Context.getAnnotationNode(unresolved.propertyNode, MapKeyJoinColumn.class) != null) {
                    throw new MetadataException(
                            LAZY_RESOURCE.get().annotationIsNotSupportedInObjectModel4JPA(
                                    this.declaringMetadataClass.getName(),
                                    this.name,
                                    MapKeyJoinColumn.class
                            )
                    );
                }
                AnnotationNode mapKeyNode = Context.getAnnotationNode(unresolved.propertyNode, MapKey.class);
                AnnotationNode mapKeyColumnNode = Context.getAnnotationNode(unresolved.propertyNode, MapKeyColumn.class);
                if (mapKeyNode != null && mapKeyColumnNode != null) {
                    throw new MetadataException(
                            LAZY_RESOURCE.get().conflictAnnotations(
                                    this.declaringMetadataClass.getName(),
                                    this.name,
                                    MapKey.class,
                                    MapKeyColumn.class
                            )
                    );
                }
                if (mapKeyNode != null) {
                    unresolved.mapKey = Nulls.emptyToNull(Context.<String>getAnnotationValue(mapKeyNode, "name"));
                    if (unresolved.mapKey == null) {
                        unresolved.mapKey = this.relatedClass.getIdProperty().getName();
                    }
                } else if (mapKeyColumnNode != null) {
                    unresolved.mapKeyColumn = Nulls.emptyToNull(Context.<String>getAnnotationValue(mapKeyColumnNode, "name"));
                    if (unresolved.mapKeyColumn == null) {
                        unresolved.mapKeyColumn = this.relatedClass.getIdProperty().getName();
                    }
                }
            } else if (typeDescriptor.equals(ASM.getDescriptor(List.class))) {
                AnnotationNode orderColumnNode = Context.getAnnotationNode(unresolved.propertyNode, OrderColumn.class);
                if (orderColumnNode == null) {
                    throw new MetadataException(
                            LAZY_RESOURCE.get().listRequiresOrderColumnInObjectModel4JPA(
                                    this.declaringMetadataClass.getName(),
                                    this.name,
                                    List.class,
                                    OrderColumn.class
                            )
                    );
                }
                unresolved.orderColumn = Nulls.emptyToNull(Context.<String>getAnnotationValue(orderColumnNode, "name"));
                if (unresolved.orderColumn == null) {
                    throw new MetadataException(
                            LAZY_RESOURCE.get().listRequiresOrderColumnValueInObjectModel4JPA(
                                    this.declaringMetadataClass.getName(),
                                    this.name,
                                    OrderColumn.class
                            )
                    );
                }
            }
        }
        this.validateJoin();
        this.join = this.createEntityJoin();
    }
    
    void resolveExplicitOppositeProperty(Context context) {
        if (this.covarianceProperty != null) {
            return;
        }
        if (this.unresolved.mapKey != null) {
            MetadataPropertyImpl oppositeKeyProperty = 
                    (MetadataPropertyImpl)
                    this
                    .relatedClass
                    .getDeclaredProperties()
                    .get(this.unresolved.mapKey);
            if (oppositeKeyProperty == null) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().unknownRelatedMapKey(
                                this.declaringMetadataClass.getName(),
                                this.name,
                                MapKey.class,
                                this.unresolved.mapKey,
                                this.relatedClass.getName()
                        )
                );
            }
            if (oppositeKeyProperty.isBasic() &&
                    !oppositeKeyProperty.isEmbedded() &&
                    !oppositeKeyProperty.isId()) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().relatedMapKeyMustBe(
                                this.declaringMetadataClass.getName(),
                                this.name,
                                MapKey.class,
                                this.unresolved.mapKey,
                                this.relatedClass.getName(),
                                new Class[] { Basic.class, Id.class, Embedded.class }
                        )
                );
            }
            if (!oppositeKeyProperty.getTypeDescriptor().equals(this.keyTypeDescriptor)) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().illlegalRelatedMapKeyType(
                                this.declaringMetadataClass.getName(),
                                this.name,
                                MapKey.class,
                                this.unresolved.mapKey,
                                this.relatedClass.getName(),
                                this.keyTypeDescriptor,
                                oppositeKeyProperty.getTypeDescriptor()
                        )
                );
            }
            this.oppositeKeyProperty = oppositeKeyProperty;
        }
        if (this.unresolved.mapKeyColumn != null) {
            String mapKeyColumn = this.unresolved.mapKeyColumn;
            for (MetadataProperty targetEntityProperty : this.relatedClass.getDeclaredProperties().values()) {
                if (mapKeyColumn.equals(targetEntityProperty.getColumnName())) {
                    if (!targetEntityProperty.getTypeDescriptor().equals(this.keyTypeDescriptor)) {
                        throw new MetadataException(
                                LAZY_RESOURCE.get().illlegalRelatedMapKeyColumnType(
                                        this.declaringMetadataClass.getName(),
                                        this.name,
                                        MapKeyColumn.class,
                                        this.unresolved.mapKeyColumn,
                                        this.relatedClass.getName(),
                                        this.keyTypeDescriptor,
                                        oppositeKeyProperty.getTypeDescriptor()
                                )
                        );
                    }
                    this.oppositeKeyProperty = (MetadataPropertyImpl)targetEntityProperty;
                    break;
                }
            }
            if (this.oppositeKeyProperty == null && this.unresolved.mappedBy != null) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().unknownRelatedMapKeyColumn(
                                this.declaringMetadataClass.getName(),
                                this.name,
                                MapKeyColumn.class,
                                this.unresolved.mapKeyColumn,
                                this.primaryAnnotationType,
                                "mappedBy",
                                this.relatedClass.getName()
                        )
                );
            }
        }
        if (this.unresolved.orderColumn != null) {
            String orderColumn = this.unresolved.orderColumn;
            for (MetadataProperty targetEntityProperty : this.relatedClass.getDeclaredProperties().values()) {
                if (orderColumn.equals(targetEntityProperty.getColumnName())) {
                    if (!targetEntityProperty.getTypeDescriptor().equals("I")) {
                        throw new MetadataException(
                                LAZY_RESOURCE.get().illlegalRelatedOrderColumnType(
                                        this.declaringMetadataClass.getName(),
                                        this.name,
                                        OrderColumn.class,
                                        this.unresolved.orderColumn,
                                        this.relatedClass.getName()
                                )
                        );
                    }
                    this.oppositeIndexProperty = (MetadataPropertyImpl)targetEntityProperty;
                    break;
                }
            }
            if (this.oppositeIndexProperty == null && this.unresolved.mappedBy != null) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().unknownRelatedOrderColumn(
                                this.declaringMetadataClass.getName(),
                                this.name,
                                OrderColumn.class,
                                this.unresolved.orderColumn,
                                this.primaryAnnotationType,
                                "mappedBy",
                                this.relatedClass.getName()
                        )
                );
            }
        }
        if (this.unresolved.mappedBy != null) {
            MetadataPropertyImpl oppositeProperty = 
                    (MetadataPropertyImpl)
                    this
                    .relatedClass
                    .getDeclaredProperties()
                    .get(this.unresolved.mappedBy);
            if (oppositeProperty == null) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().unknownMappedBy(
                                this.declaringMetadataClass.getName(),
                                this.name,
                                this.primaryAnnotationType,
                                this.unresolved.mappedBy,
                                this.relatedClass.getName()
                        )
                );
            }
            if (!oppositeProperty.isAssociation()) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().mappedByIsNotAssociation(
                                this.declaringMetadataClass.getName(),
                                this.name,
                                this.primaryAnnotationType,
                                this.unresolved.mappedBy,
                                oppositeProperty.declaringMetadataClass.getName()
                        )
                );
            }
            if (oppositeProperty.relatedClass != this.declaringMetadataClass) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().mappedByOwnerIsWrong(
                                this.declaringMetadataClass.getName(),
                                this.name,
                                this.primaryAnnotationType,
                                this.unresolved.mappedBy,
                                oppositeProperty.getDeclaringClass().getName()
                        )
                );
            }
            if (oppositeProperty.unresolved.mappedBy != null) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().targetOfMappedByCanNotUseMappedBy(
                                this.declaringMetadataClass.getName(),
                                this.name,
                                this.primaryAnnotationType,
                                this.unresolved.mappedBy,
                                oppositeProperty.getDeclaringClass().getName(),
                                oppositeProperty.primaryAnnotationType
                        )
                );
            }
            if (oppositeProperty.oppositeProperty != null) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().conflictBidirectionalAssociation(
                                this.declaringMetadataClass.getName(),
                                this.name,
                                this.primaryAnnotationType,
                                this.unresolved.mappedBy,
                                oppositeProperty.getDeclaringClass().getName(),
                                oppositeProperty.oppositeProperty.getDeclaringClass().getName(),
                                oppositeProperty.oppositeProperty.getName()
                        )
                );
            }
            this.oppositeProperty = oppositeProperty;
            oppositeProperty.oppositeProperty = this;
            this.inverse = true;
        }
    }
    
    void resolveImplicitOppositeProperty(Context context) {
        if (this.covarianceProperty != null) {
            return;
        }
        if (!this.isAssociation() || this.oppositeProperty != null) {
            return;
        }
        MetadataPropertyImpl oppositeProperty = null;
        MetadataJoin reversedJoin = this.join.reversedJoin();
        for (MetadataProperty metadataProperty : this.relatedClass.getDeclaredProperties().values()) {
            if (reversedJoin.equals(metadataProperty.getJoin())) {
                oppositeProperty = (MetadataPropertyImpl)metadataProperty;
                break;
            }
        }
        if (oppositeProperty == null) {
            if (this.isCollection()) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().unidirectionalCanNotBeCollection(
                                this.declaringMetadataClass.getName(),
                                this.name
                        )
                );
            }
            return;
        }
        if (oppositeProperty.oppositeProperty != null) {
            throw new MetadataException(
                    LAZY_RESOURCE.get().conflictBidirectionalAssociation(
                            this.declaringMetadataClass.getName(),
                            this.name,
                            this.primaryAnnotationType,
                            this.unresolved.mappedBy,
                            oppositeProperty.declaringMetadataClass.getName(),
                            oppositeProperty.oppositeProperty.getDeclaringClass().getName(),
                            oppositeProperty.oppositeProperty.getName()
                    )
            );
        }
        Boolean inverse = isInverse(this.join);
        if (inverse == null) {
            throw new MetadataException(
                    LAZY_RESOURCE.get().canNotCollectInverse(
                            this.declaringMetadataClass.getName(),
                            this.name,
                            this.join
                    )
            );
        }
        Boolean oppositeInverse = isInverse(oppositeProperty.getJoin());
        if (oppositeInverse == null) {
            throw new MetadataException(
                    LAZY_RESOURCE.get().canNotCollectInverse(
                            oppositeProperty.declaringMetadataClass.getName(),
                            oppositeProperty.name,
                            oppositeProperty.join
                    )
            );
        }
        if (inverse.booleanValue() == oppositeInverse.booleanValue()) {
            throw new MetadataException(
                    LAZY_RESOURCE.get().implicitInverseMustBeDifferent(
                            this.declaringMetadataClass.getName(), 
                            this.name,
                            this.inverse,
                            oppositeProperty.getDeclaringClass().getName(),
                            oppositeProperty.getName(),
                            oppositeProperty.isInverse()
                    )
            );
        }
        if (inverse && this.isCollection()) {
            throw new MetadataException(
                    LAZY_RESOURCE.get().inverseCollectionMustUseMappedBy(
                            this.declaringMetadataClass.getName(),
                            this.name,
                            this.primaryAnnotationType
                    )
            );
        }
        if (oppositeInverse && oppositeProperty.isCollection()) {
            throw new MetadataException(
                    LAZY_RESOURCE.get().inverseCollectionMustUseMappedBy(
                            oppositeProperty.declaringMetadataClass.getName(),
                            oppositeProperty.name,
                            oppositeProperty.primaryAnnotationType
                    )
            );
        }
        this.inverse = inverse;
        oppositeProperty.inverse = oppositeInverse;
        this.oppositeProperty = oppositeProperty;
        oppositeProperty.oppositeProperty = this;
    }
    
    void resolveReference(Context context) {
        if (this.covarianceProperty != null || !this.isReference()) {
            return;
        }
        this.standardAssocationType = Reference.class;
        MetadataPropertyImpl oppositeProperty = this.oppositeProperty;
        if (oppositeProperty != null) {
            if (oppositeProperty.oppositeIndexProperty != null) {
                this.standardAssocationType = IndexedReference.class;
                oppositeProperty.oppositeIndexProperty.referenceProperty = this;
                this.indexProperty = oppositeProperty.oppositeIndexProperty;
            } else if (oppositeProperty.oppositeKeyProperty != null) {
                this.standardAssocationType = KeyedReference.class;
                oppositeProperty.oppositeKeyProperty.referenceProperty = this;
                this.keyProperty = oppositeProperty.oppositeKeyProperty;
                this.keyTypeDescriptor = oppositeProperty.keyTypeDescriptor;
            }
        }
    }

    @SuppressWarnings("unchecked")
    void resolveContravariance(Context context) {
        if (this.covarianceProperty != null) {
            return;
        }
        AnnotationNode contravarianceInstrumentNode = Context.getAnnotationNode(
                this.unresolved.propertyNode, 
                ContravarianceInstrument.class
        );
        if (contravarianceInstrumentNode == null) {
            return;
        }
        if (Context.getAnnotationNode(this.unresolved.propertyNode, Transient.class) == null) {
            throw new MetadataException(
                    LAZY_RESOURCE.get().annotationCanOnlyBeUsedWith(
                            this.declaringMetadataClass.getName(), 
                            this.name, 
                            ContravarianceInstrument.class, 
                            Transient.class
                    )
            );
        }
        MetadataClass superMetadataClass = this.declaringMetadataClass.getSuperMetadataClass();
        if (superMetadataClass == null) {
            throw new MetadataException(
                    LAZY_RESOURCE.get().contravarianceRequiresSuperMetaClass(
                            this.declaringMetadataClass.getName(),
                            this.name,
                            ContravarianceInstrument.class
                    )
            );
        }
        String value = Context.getAnnotationValue(
                contravarianceInstrumentNode, 
                "value"
        );
        if (Nulls.isNullOrEmpty(value)) {
            value = this.name;
        }
        MetadataPropertyImpl covarianceProperty = (MetadataPropertyImpl)superMetadataClass.getProperties().get(value);
        if (covarianceProperty == null) {
            throw new MetadataException(
                    LAZY_RESOURCE.get().noCovarianceProperty(
                            this.declaringMetadataClass.getName(), 
                            this.name, 
                            ContravarianceInstrument.class, 
                            value, 
                            superMetadataClass.getName()
                    )
            );
        }
        
        covarianceProperty.resolveContravariance(context);
        if (!covarianceProperty.isAssociation()) {
            throw new MetadataException(
                    LAZY_RESOURCE.get().covariancePropertyMustBeAssociation(
                            this.declaringMetadataClass.getName(), 
                            this.name, 
                            ContravarianceInstrument.class, 
                            value, 
                            covarianceProperty.declaringMetadataClass.getName()
                    )
            );
        }
        if (covarianceProperty.isCollection() && !covarianceProperty.typeDescriptor.equals(this.typeDescriptor)) {
            throw new MetadataException(
                    LAZY_RESOURCE.get().illegalCovariancePropertyType(
                            this.declaringMetadataClass.getName(), 
                            this.name, 
                            ContravarianceInstrument.class, 
                            value, 
                            covarianceProperty.declaringMetadataClass.getName(),
                            this.typeDescriptor,
                            covarianceProperty.typeDescriptor
                    )
            );
        }
        
        this.covarianceProperty = covarianceProperty;
        this.primaryAnnotationType = covarianceProperty.primaryAnnotationType;
        this.any = covarianceProperty.any;
        this.fetchType = this.covarianceProperty.fetchType;
        this.inverse = this.covarianceProperty.inverse;
        this.standardAssocationType = this.covarianceProperty.standardAssocationType;
        this.keyProperty = this.covarianceProperty.keyProperty;
        this.indexProperty = this.covarianceProperty.indexProperty;
        this.keyTypeDescriptor = this.covarianceProperty.keyTypeDescriptor;
        this.oppositeProperty = this.covarianceProperty.oppositeProperty;
        this.oppositeKeyProperty = this.covarianceProperty.oppositeKeyProperty;
        this.oppositeIndexProperty = this.covarianceProperty.oppositeIndexProperty;
        this.join = this.covarianceProperty.join;
        this.decideTargetEntity(true, context);
    }

    void finishResolving() {
        this.visibleAnnotations = this.visibleAnnotations == null ?
                MACollections.<MetadataAnnotation>emptyList() :
                MACollections.unmodifiable(this.visibleAnnotations);
        this.invisibleAnnotations = this.invisibleAnnotations == null ?
                MACollections.<MetadataAnnotation>emptyList() :
                MACollections.unmodifiable(this.invisibleAnnotations);
        this.embeddedColumnNames = this.embeddedColumnNames == null ?
                MACollections.<MetadataProperty, String>emptyMap() :
                MACollections.unmodifiable(this.embeddedColumnNames);
        this.unresolved = null;
    }

    @SuppressWarnings("unchecked")
    private void afterAccessTypeDetermined(Context context) {
        if (this.accessType == AccessType.FIELD) {
            this.typeDescriptor = this.unresolved.fieldNode.desc;
            this.unresolved.typeSignature = this.unresolved.fieldNode.signature;
        } else {
            String desc = this.unresolved.methodNode.desc;
            this.typeDescriptor = desc.substring(desc.lastIndexOf(')') + 1);
            String signature = this.unresolved.methodNode.signature;
            if (signature != null) {
                this.unresolved.typeSignature = signature.substring(signature.lastIndexOf(')') + 1);
            }
        }
        if (unresolved.methodNode != null) {
            this.getterName = unresolved.methodNode.name;
        } else {
            String getterName = unresolved.fieldNode.name;
            getterName = (unresolved.fieldNode.desc.equals("Z") ? "is" : "get") + Character.toUpperCase(getterName.charAt(0)) + getterName.substring(1);
            this.getterName = getterName;
        }
        this.visibleAnnotations = createAnnotations(Context.getVisibleAnnotations(this.unresolved.propertyNode));
        this.invisibleAnnotations = createAnnotations(Context.getInvisibleAnnotations(this.unresolved.propertyNode));
        if (this.declaringMetadataClass.getClassNode().methods != null) {
            String name = "set" + this.getterName.substring(this.getterName.startsWith("is") ? 2 : 3);
            String desc = '(' + this.typeDescriptor + ")V";
            for (MethodNode methodNode : (List<MethodNode>)this.declaringMetadataClass.getClassNode().methods) {
                if (methodNode.name.equals(name) && methodNode.desc.equals(desc)) {
                    this.setterName = name;
                    break;
                }
            }
        }
        if (Context.getAnnotationNode(this.unresolved.propertyNode, Id.class) != null) {
            this.setPrimaryAnnotationType(Id.class);
        }
        if (Context.getAnnotationNode(this.unresolved.propertyNode, EmbeddedId.class) != null) {
            this.setPrimaryAnnotationType(EmbeddedId.class);
        }
        if (Context.getAnnotationNode(this.unresolved.propertyNode, Version.class) != null) {
            this.setPrimaryAnnotationType(Version.class);
        }
        if (Context.getAnnotationNode(this.unresolved.propertyNode, Basic.class) != null) {
            this.setPrimaryAnnotationType(Basic.class);
            this.setFetchType(Context.getAnnotationNode(this.unresolved.propertyNode, Basic.class));
        }
        if (Context.getAnnotationNode(this.unresolved.propertyNode, Embedded.class) != null) {
            this.setPrimaryAnnotationType(Embedded.class);
        }
        if (Context.getAnnotationNode(this.unresolved.propertyNode, ElementCollection.class) != null) {
            this.setPrimaryAnnotationType(ElementCollection.class);
        }
        if (Context.getAnnotationNode(this.unresolved.propertyNode, OneToOne.class) != null) {
            this.setPrimaryAnnotationType(OneToOne.class);
            this.setFetchType(Context.getAnnotationNode(this.unresolved.propertyNode, OneToOne.class));
        }
        if (Context.getAnnotationNode(this.unresolved.propertyNode, ManyToOne.class) != null) {
            this.setPrimaryAnnotationType(ManyToOne.class);
            this.setFetchType(Context.getAnnotationNode(this.unresolved.propertyNode, ManyToOne.class));
        }
        if (Context.getAnnotationNode(this.unresolved.propertyNode, OneToMany.class) != null) {
            this.setPrimaryAnnotationType(OneToMany.class);
            this.setFetchType(Context.getAnnotationNode(this.unresolved.propertyNode, OneToMany.class));
        }
        if (Context.getAnnotationNode(this.unresolved.propertyNode, ManyToMany.class) != null) {
            this.setPrimaryAnnotationType(ManyToMany.class);
            this.setFetchType(Context.getAnnotationNode(this.unresolved.propertyNode, ManyToMany.class));
        }
        if (context.getAnyAnnotationType() != null && 
                Context.getAnnotationNode(
                        this.unresolved.propertyNode, 
                        context.getAnyAnnotationType()
                ) != null) {
            this.setPrimaryAnnotationType(context.getAnyAnnotationType());
            this.setFetchType(Context.getAnnotationNode(this.unresolved.propertyNode, context.getAnyAnnotationType()));
            this.any = true;
        }
        if (this.primaryAnnotationType == null) {
            this.primaryAnnotationType = Basic.class;
        }
        if (this.fetchType == null) {
            this.fetchType = FetchType.EAGER;
        }
        
        this.lob = Context.getAnnotationNode(this.unresolved.propertyNode, Lob.class) != null;
        if (this.lob && this.primaryAnnotationType != Basic.class) {
            throw new MetadataException(
                    LAZY_RESOURCE.get().conflictAnnotations(
                            this.declaringMetadataClass.getName(), 
                            this.name, 
                            Lob.class,
                            this.primaryAnnotationType 
                    )
            );
        }
        if (this.fetchType == FetchType.EAGER) {
            if (this.primaryAnnotationType == ManyToOne.class && !this.declaringMetadataClass.isEagerReferenceAllowed()) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().mustNotBeEager(
                                this.declaringMetadataClass.getName(), 
                                this.name, 
                                FetchType.EAGER,
                                JPAObjectModelInstrument.class,
                                "allowEagerReferences",
                                FetchType.LAZY
                        )
                );
            }
            if (this.lob && !this.declaringMetadataClass.isEagerLobAllowed()) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().mustNotBeEager(
                                this.declaringMetadataClass.getName(), 
                                this.name, 
                                FetchType.EAGER,
                                JPAObjectModelInstrument.class,
                                "allowEagerLobs",
                                FetchType.LAZY
                        )
                );
            }
        }
        
        AnnotationNode columnNode = Context.getAnnotationNode(this.unresolved.propertyNode, Column.class);
        if (this.primaryAnnotationType == Basic.class ||
                this.primaryAnnotationType == Id.class ||
                this.primaryAnnotationType == Version.class) {
            if (columnNode != null) {
                this.columnName = Nulls.emptyToNull(Context.<String>getAnnotationValue(columnNode, "name"));
                if (this.columnName == null) {
                    this.columnName = name;
                }
            }
        } else if (columnNode != null) {
            throw new MetadataException(
                    LAZY_RESOURCE.get().annotationCanOnlyBeUsedWith(
                            this.declaringMetadataClass.getName(),
                            this.name,
                            Column.class,
                            new Class[] { Basic.class, Id.class, Version.class }
                    )               
            );
        }
        if (!this.isEmbedded()) {
            if (Context.getAnnotationNode(this.unresolved.propertyNode, AttributeOverride.class) != null) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().annotationCanOnlyBeUsedWith(
                                this.declaringMetadataClass.getName(),
                                this.name,
                                AttributeOverride.class,
                                Embedded.class
                        )
                );
            } 
            if (Context.getAnnotationNode(this.unresolved.propertyNode, AttributeOverrides.class) != null) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().annotationCanOnlyBeUsedWith(
                                this.declaringMetadataClass.getName(),
                                this.name,
                                AttributeOverrides.class,
                                Embedded.class
                        ));
            }
        }
        
        AnnotationNode referenceComparsionInstrumentNode = 
                Context.getAnnotationNode(
                        this.unresolved.propertyNode, 
                        ReferenceComparisonRuleInstrument.class
                );
        if (referenceComparsionInstrumentNode != null) {
            if (!this.isCollection()) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().annotationCanOnlyBeUsedWith(
                                this.declaringMetadataClass.getName(), 
                                this.name, 
                                ReferenceComparisonRuleInstrument.class, 
                                OneToMany.class,
                                ManyToMany.class)
                );
            }
            this.referenceComparisonRule = 
                    Joins.join(
                            Context.<List<String>>getAnnotationValue(
                                    referenceComparsionInstrumentNode, 
                                    "value"
                            )
                    )
                    .trim();
        }
        
        AnnotationNode navigableInstrumentNode =
                Context.getAnnotationNode(
                        this.unresolved.propertyNode, 
                        NavigableInstrument.class
                );
        if (navigableInstrumentNode != null) {
            String desc;
            if (this.unresolved.propertyNode == this.unresolved.fieldNode) {
                desc = this.unresolved.fieldNode.desc;
            } else {
                int index = this.unresolved.methodNode.desc.lastIndexOf(')');
                desc = this.unresolved.methodNode.desc.substring(index + 1);
            }
            if (!desc.equals(ASM.getDescriptor(Set.class)) && !desc.equals(ASM.getDescriptor(Map.class))) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().annotationRequiresAnyOneType(
                                this.declaringMetadataClass.getName(), 
                                this.name, 
                                NavigableInstrument.class,
                                Set.class,
                                Map.class
                        )
                );
            }
            this.unresolved.navigableCollection = true;
        }
    }
    
    @SuppressWarnings("unchecked")
    private static List<MetadataAnnotation> createAnnotations(List<?> annotationNodes) {
        if (Nulls.isNullOrEmpty(annotationNodes)) {
            return MACollections.emptyList();
        }
        List<MetadataAnnotation> visibleAnnotations = new ArrayList<>(annotationNodes.size());
        for (AnnotationNode annotationNode : (List<AnnotationNode>)annotationNodes) {
            visibleAnnotations.add(new ModelAnnotationImpl(annotationNode));
        }
        return visibleAnnotations;
    }
    
    private void decideTargetEntity(boolean contravariance, Context context) {
        String targetEntity = null;
        if (!contravariance) {
            AnnotationNode annotationNode = Context.getAnnotationNode(unresolved.propertyNode, this.primaryAnnotationType);
            targetEntity = Nulls.emptyToNull(Context.<String>getAnnotationValue(annotationNode, "targetEntity"));
        }
        if (targetEntity == null) {
            String thisTypeDescriptor = this.typeDescriptor;
            if (this.isReference()) {
                if (!thisTypeDescriptor.startsWith("L") || !thisTypeDescriptor.endsWith(";")) {
                    throw new MetadataException(
                            LAZY_RESOURCE.get().typeDescriptorMustBeClass(
                                    this.declaringMetadataClass.getName(),
                                    this.name,
                                    thisTypeDescriptor
                            )
                    );
                }
                targetEntity = thisTypeDescriptor.substring(1, thisTypeDescriptor.length() - 1).replace('/', '.');
            } else {
                if (thisTypeDescriptor.equals(ASM.getDescriptor(Collection.class))) {
                    this.standardAssocationType = Collection.class;
                } else if (thisTypeDescriptor.equals(ASM.getDescriptor(Set.class))) {
                    this.standardAssocationType = this.unresolved.navigableCollection ? NavigableSet.class : Set.class;
                } else if (thisTypeDescriptor.equals(ASM.getDescriptor(List.class))) {
                    this.standardAssocationType = List.class;
                } else if (thisTypeDescriptor.equals(ASM.getDescriptor(Map.class))) {
                    this.standardAssocationType = this.unresolved.navigableCollection ? NavigableMap.class : Map.class;
                } else {
                    throw new MetadataException(
                            LAZY_RESOURCE.get().badCollectionType(
                                    this.declaringMetadataClass.getName(),
                                    this.name,
                                    this.primaryAnnotationType,
                                    new Class[] { 
                                        Collection.class,
                                        List.class,
                                        Set.class,
                                        Map.class 
                                    }
                            )
                    );
                }
                if (unresolved.typeSignature == null) {
                    throw new MetadataException(
                            LAZY_RESOURCE.get().collectionMustBeGenericType(
                                    this.declaringMetadataClass.getName(),
                                    this.name,
                                    this.primaryAnnotationType
                            )
                    );
                }
                String[] typeArguments = this.getTypeArguments(unresolved.typeSignature);
                if (typeArguments.length == 2) {
                    this.keyTypeDescriptor = 'L' + typeArguments[0].replace('.', '/') + ';';
                }
                targetEntity = typeArguments[typeArguments.length - 1];
            }
        }
        this.relatedClass = (MetadataClassImpl)context.getModelClasses().get(targetEntity);
        if (this.relatedClass == null) {
            throw new MetadataException(
                    LAZY_RESOURCE.get().unknownRelatedClass(
                            this.getDeclaringClass().getName(), 
                            this.name, 
                            this.primaryAnnotationType, 
                            targetEntity
                    )
            );
        }
        if (!this.relatedClass.isEntity()) {
            throw new MetadataException(
                    LAZY_RESOURCE.get().relatedClassMustBeEntity(
                            this.getDeclaringClass().getName(), 
                            this.name, 
                            this.primaryAnnotationType, 
                            targetEntity,
                            Entity.class
                    )
            );
        }
        if (this.declaringMetadataClass.isJPAObjectModelInstrument() != this.relatedClass.isJPAObjectModelInstrument()) {
            throw new MetadataException(
                    LAZY_RESOURCE.get().incongruousJPAObjectInstrument(
                            this.getDeclaringClass().getName(),
                            this.name, 
                            this.relatedClass.getName(), 
                            JPAObjectModelInstrument.class
                    )
            );
        }
    }
    
    private Map<String, String> getOverrideMap() {
        Map<String, String> map = new HashMap<>();
        AnnotationNode attributeOverrideNode = Context.getAnnotationNode(this.unresolved.propertyNode, AttributeOverride.class);
        AnnotationNode attributeOverridesNode = Context.getAnnotationNode(this.unresolved.propertyNode, AttributeOverrides.class);
        if (attributeOverrideNode == null && attributeOverridesNode == null) {
            return MACollections.emptyMap();
        }
        if (attributeOverrideNode != null && attributeOverridesNode != null) {
            throw new MetadataException(
                    LAZY_RESOURCE.get().conflictAnnotations(
                            this.declaringMetadataClass.getName(), 
                            this.name, 
                            AttributeOverride.class,
                            AttributeOverrides.class
                    )
            );
        }
        List<AnnotationNode> attributeOverrideNodes;
        if (attributeOverrideNode != null) {
            attributeOverrideNodes = MACollections.wrap(attributeOverrideNode);
        } else {
            attributeOverrideNodes = Context.getAnnotationValue(attributeOverridesNode, "value");
        }
        for (AnnotationNode attributeOverrideNode_ : attributeOverrideNodes) {
            String key = Context.getAnnotationValue(attributeOverrideNode_, "name");
            AnnotationNode columnNode = Context.getAnnotationValue(attributeOverrideNode_, "column");
            String value = Nulls.emptyToNull(Context.<String>getAnnotationValue(columnNode, "name"));
            if (value == null) {
                value = key;
            }
            map.put(key, value);
        }
        return map;
    }

    private void validateJoin() {
        Object propertyNode = this.unresolved.propertyNode;
        Class<? extends Annotation> joinAnnotationType = null; 
        if (Context.getAnnotationNode(propertyNode, JoinColumn.class) != null) {
            joinAnnotationType = JoinColumn.class;
        }
        if (Context.getAnnotationNode(propertyNode, JoinColumns.class) != null) {
            if (joinAnnotationType != null) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().conflictAnnotations(
                                this.declaringMetadataClass.getName(), 
                                this.name, 
                                joinAnnotationType, 
                                JoinColumns.class)
                );
            }
            joinAnnotationType = JoinColumns.class;
        }
        if (Context.getAnnotationNode(propertyNode, JoinTable.class) != null) {
            if (joinAnnotationType != null) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().conflictAnnotations(
                                this.declaringMetadataClass.getName(), 
                                this.name, 
                                joinAnnotationType, 
                                JoinTable.class)
                );
            }
            joinAnnotationType = JoinTable.class;
        }
        if (joinAnnotationType != null && this.unresolved.mappedBy != null) {
            throw new MetadataException(
                    LAZY_RESOURCE.get().canNotUseMappedByWith(
                            this.declaringMetadataClass.getName(),
                            this.name,
                            this.primaryAnnotationType,
                            joinAnnotationType
                    )
            );
        }
    }
    
    private MetadataJoin createEntityJoin() {
        
        MetadataClassImpl thisEntity = this.declaringMetadataClass;
        MetadataClassImpl targetEntity = this.relatedClass;

        AnnotationNode singleJoinColumnNode = Context.getAnnotationNode(this.unresolved.propertyNode, JoinColumn.class);
        if (singleJoinColumnNode != null) {
            String name = Nulls.emptyToNull(Context.<String>getAnnotationValue(singleJoinColumnNode, "name"));
            String referencedColumnName = Nulls.emptyToNull(Context.<String>getAnnotationValue(singleJoinColumnNode, "referencedColumnName"));
            String parentPKColumnName = null;
            if (name == null || referencedColumnName == null) {
                MetadataProperty idProperty = this.isReference() ? targetEntity.getIdProperty() : thisEntity.getIdProperty();
                if (idProperty.isEmbedded() && idProperty.getEmbeddedColumnNames().size() > 1) {
                    throw new MetadataException(
                            LAZY_RESOURCE.get().illegalJoinColumnOnMultipColumns(
                                    this.declaringMetadataClass.getName(),
                                    this.name,
                                    JoinColumn.class,
                                    idProperty.getDeclaringClass().getName(),
                                    idProperty.getName()
                            )
                    );
                }
                parentPKColumnName = idProperty.isEmbedded() ? idProperty.getName() : "";
            }
            if (name == null) {
                name = (this.isReference() ? this.name : thisEntity.getName()) + '_' + parentPKColumnName;
            }
            if (referencedColumnName == null) {
                referencedColumnName = parentPKColumnName;
            }
            return new MetadataJoinImpl(
                    MACollections.<MetadataJoin.Column>wrap(
                            new MetadataJoinImpl.ColumnImpl(
                                    name, 
                                    referencedColumnName,
                                    Context.<Boolean>getAnnotationValue(singleJoinColumnNode, "insertable"),
                                    Context.<Boolean>getAnnotationValue(singleJoinColumnNode, "updatable")
                            )
                    )
            );
        }
        
        AnnotationNode joinColumnsNode = Context.getAnnotationNode(this.unresolved.propertyNode, JoinColumns.class);
        if (joinColumnsNode != null) {
            List<AnnotationNode> joinColumnNodes = Nulls.emptyToNull(Context.<List<AnnotationNode>>getAnnotationValue(joinColumnsNode, "value"));
            if (joinColumnNodes == null) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().emptyJoinColumns(
                                this.declaringMetadataClass.getName(),
                                this.name,
                                JoinColumns.class,
                                JoinColumn.class
                        )
                );
            }
            List<MetadataJoin.Column> columns = this.createJoinColumnWithoutDefaultValue(joinColumnNodes);
            return new MetadataJoinImpl(columns);
        }
        
        AnnotationNode joinTableNode = Context.getAnnotationNode(this.unresolved.propertyNode, JoinTable.class);
        String tableName = null;
        List<MetadataJoin.Column> columns = null;
        List<MetadataJoin.Column> inverseColumns = null;
        if (joinTableNode != null) {
            tableName = Nulls.emptyToNull(Context.<String>getAnnotationValue(joinTableNode, "name"));
            List<AnnotationNode> joinColumnNodes = Nulls.emptyToNull(Context.<List<AnnotationNode>>getAnnotationValue(joinTableNode, "joinColumns"));
            if (joinColumnNodes != null) {
                columns = this.createJoinColumnWithoutDefaultValue(joinColumnNodes);
            }
            List<AnnotationNode> inverseJoinColumnNodes = Nulls.emptyToNull(Context.<List<AnnotationNode>>getAnnotationValue(joinTableNode, "inverseJoinColumns"));
            if (inverseJoinColumnNodes != null) {
                inverseColumns = this.createJoinColumnWithoutDefaultValue(inverseJoinColumnNodes);
            }
        }
        if (tableName == null) {
            tableName = thisEntity.getTableName() + '_' + targetEntity.getTableName();
        }
        if (columns == null) {
            columns = this.createJoinColumnWithDefaultValue(false);
        }
        if (inverseColumns == null) {
            inverseColumns = this.createJoinColumnWithDefaultValue(true);
        }
        return new MetadataJoinImpl(tableName, columns, inverseColumns);
    }
    
    private List<MetadataJoin.Column> createJoinColumnWithoutDefaultValue(List<AnnotationNode> joinColumnNodes) {
        List<MetadataJoin.Column> columns = new ArrayList<>();
        if (joinColumnNodes != null) {
            for (AnnotationNode joinColumnNode : joinColumnNodes) {
                String name = Nulls.emptyToNull(Context.<String>getAnnotationValue(joinColumnNode, "name"));
                if (name == null) {
                    throw new MetadataException(
                            LAZY_RESOURCE.get().emptyJonColumnName(
                                    this.declaringMetadataClass.getName(),
                                    this.name,
                                    JoinColumn.class
                            )
                    );
                }
                String referencedColumnName = Nulls.emptyToNull(Context.<String>getAnnotationValue(joinColumnNode, "referencedColumnName"));
                if (referencedColumnName == null) {
                    referencedColumnName = this.relatedClass.getIdProperty().getColumnName();
                    if (referencedColumnName == null) {
                        throw new MetadataException();
                    }
                }
                columns.add(
                        new MetadataJoinImpl.ColumnImpl(
                                name, 
                                referencedColumnName,
                                Context.<Boolean>getAnnotationValue(joinColumnNode, "insertable"),
                                Context.<Boolean>getAnnotationValue(joinColumnNode, "updatable")
                        )
                );
            }
        }
        return columns;
    }
    
    private List<MetadataJoin.Column> createJoinColumnWithDefaultValue(boolean inverse) {
        MetadataClass metadataClass;
        boolean isReference;
        if (inverse) {
            metadataClass = this.relatedClass;
            isReference = false;
        } else {
            metadataClass = this.declaringMetadataClass;
            isReference = this.primaryAnnotationType == OneToOne.class || this.primaryAnnotationType == ManyToOne.class;
        }
        Collection<String> idColumns;
        if (metadataClass.getIdProperty().isEmbedded()) {
            idColumns = metadataClass.getIdProperty().getEmbeddedColumnNames().values();
        } else {
            idColumns = MACollections.wrap(metadataClass.getIdProperty().getColumnName());
        }
        List<MetadataJoin.Column> columns = new ArrayList<>();
        String prefix = (isReference ? this.name : metadataClass.getTableName()) + '_';
        for (String idColumn : idColumns) {
            columns.add(
                    new MetadataJoinImpl.ColumnImpl(
                            prefix + idColumn, 
                            idColumn,
                            null,
                            null
                    )
            );
        }
        return columns;
    }

    private static AccessType accessType(Object memberNode) {
        if (memberNode != null && Context.getVisibleAnnotations(memberNode) != null) {
            for (AnnotationNode annotationNode : Context.getVisibleAnnotations(memberNode)) {
                if (annotationNode.desc.equals(ASM.getDescriptor(Access.class))) {
                    return AccessType.valueOf(Context.<String[]>getAnnotationValue(annotationNode, "value")[1]);
                }
            }
        }
        return null;
    }

    private static boolean isJPAAnnotationPresent(Object memberNode) {
        if (memberNode != null && Context.getVisibleAnnotations(memberNode) != null) {
            for (AnnotationNode annotationNode : Context.getVisibleAnnotations(memberNode)) {
                if (annotationNode.desc.startsWith("Ljavax/persistence/")) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private String[] getTypeArguments(String signature) {
        // SignatureReader is too complex...
        int depth = 0;
        int sigLen = signature.length();
        int len = 0;
        char[] arr = new char[signature.length()];
        for (int i = 0; i < sigLen; i++) {
            char c = signature.charAt(i);
            if (c == '<') {
                depth++;
            } else if (c == '>') {
                depth--;
            } else if (depth == 1) {
                arr[len++] = c == '/' ? '.' : c;
            }
        }
        if (arr[0] != 'L') {
            throw new MetadataException(
                    LAZY_RESOURCE.get().typeDescriptorMustBeClass(
                            this.declaringMetadataClass.getName(), 
                            this.name, 
                            this.typeDescriptor
                    )
            );
        }
        int speratorIndex = -1;
        for (int i = 0; i < len; i++) {
            if (arr[i] == ';') {
                speratorIndex = i;
                break;
            }
        }
        if (speratorIndex + 1 == len) {
            return new String[] { new String(arr, 1, len - 2) };
        }
        String[] pair = new String[2];
        pair[0] = new String(arr, 1, speratorIndex - 1);
        if (arr[speratorIndex + 1] != 'L' || arr[len - 1] != ';') {
            throw new MetadataException(
                    LAZY_RESOURCE.get().typeArgumentMustBeClass(
                            this.declaringMetadataClass.getName(), 
                            this.name, 
                            this.typeDescriptor,
                            new String(arr, speratorIndex + 1, len)
                    )
            );
        }
        pair[1] = new String(arr, speratorIndex + 2, len - speratorIndex - 3);
        return pair;
    }
    
    private void setPrimaryAnnotationType(Class<? extends Annotation> primaryAnnotationType) {
        if (this.primaryAnnotationType != null) {
            throw new MetadataException(
                    LAZY_RESOURCE.get().conflictAnnotations(
                            this.declaringMetadataClass.getName(), 
                            this.name, 
                            this.primaryAnnotationType, 
                            primaryAnnotationType)
            );
        }
        this.primaryAnnotationType = primaryAnnotationType;
    }
    
    private void setFetchType(AnnotationNode annotationNode) {
        String[] values = Context.getAnnotationValue(annotationNode, "fetch");
        if (values != null) {
            this.fetchType = FetchType.valueOf(values[1]);
        }
    }
    
    private static Boolean isInverse(MetadataJoin join) {
        Boolean inverse = null;
        for (MetadataJoin.Column column : join.getColumns().values()) {
            if ((inverse = collectInverse(inverse, !column.isInsertable())) == null) {
                return null;
            }
            if ((inverse = collectInverse(inverse, !column.isUpdatable())) == null) {
                return null;
            }
        }
        if (join.getInverseColumns() != null) {
            for (MetadataJoin.Column inverseColumn : join.getInverseColumns().values()) {
                if ((inverse = collectInverse(inverse, !inverseColumn.isInsertable())) == null) {
                    return null;
                }
                if ((inverse = collectInverse(inverse, !inverseColumn.isUpdatable())) == null) {
                    return null;
                }
            }
        }
        return inverse;
    }
    
    private static Boolean collectInverse(Boolean inverse, boolean newInverse) {
        if (inverse == null) {
            return newInverse;
        }
        if (inverse.booleanValue() != newInverse) {
            return null;
        }
        return inverse;
    }

    private static class Unresolved implements Cloneable {
        FieldNode fieldNode;
        MethodNode methodNode;
        Object propertyNode;
        String typeSignature;
        String mappedBy;
        String mapKey;
        String mapKeyColumn;
        String orderColumn;
        boolean navigableCollection;
        @Override
        public Unresolved clone() {
            Unresolved cloned = new Unresolved();
            cloned.fieldNode = this.fieldNode;
            cloned.methodNode = this.methodNode;
            cloned.propertyNode = this.propertyNode;
            cloned.typeSignature = this.typeSignature;
            cloned.mappedBy = this.mappedBy;
            cloned.mapKey = this.mapKey;
            cloned.mapKeyColumn = this.mapKeyColumn;
            cloned.orderColumn = this.orderColumn;
            return cloned;
        }
    }
    
    private interface Resource {

        String conflictAccessTypes(
                String className, 
                String propertyName, 
                Class<Access> accessType);

        String typeArgumentMustBeClass(
                String className, 
                String propertyName,
                String typeDescriptor,
                String typeArgument);

        String emptyJonColumnName(
                String className, 
                String propertyName,
                Class<JoinColumn> joinColumnTypeConstant);

        String emptyJoinColumns(
                String className, 
                String propertyName,
                Class<JoinColumns> joinColumnsTypeConstant, 
                Class<JoinColumn> joinColumnTypeConstant);

        String illegalJoinColumnOnMultipColumns(
                String className, 
                String propertyName,
                Class<JoinColumn> joinColumn,
                String targetClassName, 
                String targetIdPropertyName);

        String canNotUseMappedByWith(
                String className, 
                String propertyName,
                Class<? extends Annotation> primaryAnnotationType,
                Class<? extends Annotation> annotationType);

        String annotationRequiresAnyOneType(
                String className, 
                String propertyName,
                Class<? extends Annotation> annoatinType, 
                Class<?>... requiredTypes);

        @SuppressWarnings("unchecked") 
        String annotationCanOnlyBeUsedWith(
                String className, 
                String propertyName,
                Class<? extends Annotation> annotationType, 
                Class<? extends Annotation>... otherAnnotationTypes);

        String inverseCollectionMustUseMappedBy(
                String className, 
                String propertyName,
                Class<? extends Annotation> primaryAnnotationType);

        String implicitInverseMustBeDifferent(
                String className, 
                String propertyName,
                boolean inverse, 
                String oppositeClassName, 
                String oppositePropertyName, 
                boolean oppositeInverse);

        String canNotCollectInverse(
                String className, 
                String propertyName, 
                MetadataJoin join);

        String unidirectionalCanNotBeCollection(
                String className, 
                String propertyName);

        String conflictBidirectionalAssociation(
                String classname, 
                String propertyName,
                Class<? extends Annotation> primaryAnnotationType,
                String mappedBy, 
                String oppositeClassName, 
                String oppositeOppositeClassName, 
                String oppositeOppositePropertyName);

        String targetOfMappedByCanNotUseMappedBy(
                String className, 
                String propertyName,
                Class<? extends Annotation> primaryAnnotationType,
                String mappedBy, 
                String oppositeClassName, 
                Class<? extends Annotation> oppositePrimaryAnnotationType);

        String mappedByOwnerIsWrong(
                String className, 
                String propertyName,
                Class<? extends Annotation> primaryAnnotationType,
                String mappedBy, 
                String oppositeClassName);

        String mappedByIsNotAssociation(
                String className, 
                String propertyName,
                Class<? extends Annotation> primaryAnnotationType,
                String mappedBy, 
                String oppositeClassName);

        String unknownMappedBy(
                String className, 
                String propertyName,
                Class<? extends Annotation> primaryAnnotationType,
                String mappedBy, 
                String relatedClassName);

        String illlegalRelatedOrderColumnType(
                String className, 
                String propertyName,
                Class<OrderColumn> orderColumnTypeConstant,
                String orderColumn,
                String relatedClassName);

        String unknownRelatedOrderColumn(
                String className, 
                String propertyName,
                Class<OrderColumn> orderColumnTypeConstant,
                String orderColumn,
                Class<? extends Annotation> primaryAnnotationType,
                String mappedByConstant,
                String relatedClassName);

        String illlegalRelatedMapKeyColumnType(
                String className, 
                String propertyName,
                Class<MapKeyColumn> mapKeyColumnTypeConstant,
                String mapKeyColumn,
                String targetClassName,
                String expectedTypeDescriptor, 
                String actualTypeDescriptor);

        String unknownRelatedMapKeyColumn(
                String className, 
                String propertyName,
                Class<MapKeyColumn> mapKeyColumnTypeConstant,
                String mapKeyColumn,
                Class<? extends Annotation> primaryAnnotationType,
                String mappedByConstant,
                String targetClassName);

        String illlegalRelatedMapKeyType(
                String className, 
                String propertyName,
                Class<MapKey> mapKeyTypeConstant,
                String mapKeyColumn,
                String targetClassName,
                String expectedTypeDescriptor, 
                String actualTypeDescriptor);

        String relatedMapKeyMustBe(
                String className, 
                String propertyName,
                Class<MapKey> mapKeyTypeConstant, 
                String mapKey,
                String targetClassName,
                Class<?>... classes);

        String unknownRelatedMapKey(
                String className, 
                String propertyName,
                Class<MapKey> mapKeyTypeConstant, 
                String mapKey,
                String targetClassName);

        String listRequiresOrderColumnValueInObjectModel4JPA(
                String className, 
                String propertyName,
                Class<OrderColumn> orderColumnTypeConstant);

        @SuppressWarnings("rawtypes")
        String listRequiresOrderColumnInObjectModel4JPA(
                String className, 
                String propertyName,
                Class<List> listTypeConstant, 
                Class<OrderColumn> orderColumnTypeConstant);

        String relatedClassMustBeEntity(
                String className, 
                String property,
                Class<? extends Annotation> primaryAnnotationType,
                String targetClassName, 
                Class<Entity> entityTypeDescriptor);

        String conflictAnnotations(
                String className, 
                String propertyName,
                Class<? extends Annotation> annotationType1, 
                Class<? extends Annotation> annotationType2);

        String annotationIsNotSupportedInObjectModel4JPA(
                String className,
                String propertyName, 
                Class<? extends Annotation> annotationType);

        String collectionMustBeGenericType(
                String className, 
                String propertyName,
                Class<? extends Annotation> primaryAnnotationType);

        String badCollectionType(
                String className, 
                String propertyName,
                Class<? extends Annotation> primaryAnnotationType,
                Class<?>... classes);

        String typeDescriptorMustBeClass(
                String className, 
                String propertyName,
                String typeDescriptor);

        String canNotOverrideDeepProperties(
                String className, 
                String propertyName,
                Class<AttributeOverride> attributeOverrideTypeConstant,
                String embeddedClassName, 
                Collection<String> unmatchedNames);

        String incongruousJPAObjectInstrument(
                String className,
                String propertyName,
                String targetClassName, 
                Class<JPAObjectModelInstrument> jpaObjectModelInstrumentType);

        String embeddedTyepIsNotEmbeddable(
                String className, 
                String propertyName,
                Class<Embedded> embeddedTypeConstant, 
                String targetClassName,
                Class<Embeddable> embeddableTypeConstant);

        String unknownRelatedClass(
                String className, 
                String propertyName,
                Class<? extends Annotation> annotationType, 
                String targeClassName);

        String embeddedCircular(
                String className, 
                String property,
                Class<Embedded> embeddedTypeConstant, 
                XOrderedSet<MetadataPropertyImpl> resolvingQueue);

        String unknownAccessType(
                String className, 
                String propertyName,
                Class<AccessType> accessTypeConstant);

        String badFieldAnnotation(
                String className, 
                String propertyName, 
                AccessType accessType);

        String noPropertyMethod(
                String className, 
                String propertyName, 
                AccessType accessType);

        String badMethodAnnotation(
                String className, 
                String propertyName, 
                AccessType accessType);

        String noPropertyField(
                String className, 
                String propertyName, 
                AccessType accessType);
        
        String contravarianceRequiresSuperMetaClass(
                String className, 
                String propertyName,
                Class<ContravarianceInstrument> contravarianceInstrumentTypeConstant);
        
        String noCovarianceProperty(
                String className, 
                String propertyName,
                Class<ContravarianceInstrument> contravarianceInstrumentTypeConstant,
                String covariancePropertyName,
                String superClassName);
        
        String covariancePropertyMustBeAssociation(
                String className, 
                String propertyName,
                Class<ContravarianceInstrument> contravarianceInstrumentTypeConstant,
                String covariancePropertyName,
                String covariancePropertyOwnerClassName);
        
        String illegalCovariancePropertyType(
                String className, 
                String propertyName,
                Class<ContravarianceInstrument> contravarianceInstrumentTypeConstant,
                String covariancePropertyName,
                String covariancePropertyOwnerClassName,
                String thisTypeDescriptor,
                String covariancePropertyTypeDescriptor);
        
        String mustNotBeEager(
                String className, 
                String propertyName, 
                FetchType eagerConstant,
                Class<JPAObjectModelInstrument> jpaObjectModelInstrumentTypeConstant, 
                String attributeName,
                FetchType lazyConstant);
    }
}
