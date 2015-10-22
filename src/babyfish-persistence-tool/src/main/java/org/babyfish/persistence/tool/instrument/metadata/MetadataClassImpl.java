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

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.persistence.Version;

import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.lang.Nulls;
import org.babyfish.lang.reflect.asm.ASM;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.org.objectweb.asm.tree.AnnotationNode;
import org.babyfish.org.objectweb.asm.tree.ClassNode;
import org.babyfish.org.objectweb.asm.tree.FieldNode;
import org.babyfish.org.objectweb.asm.tree.MethodNode;
import org.babyfish.persistence.instrument.JPAObjectModelInstrument;
import org.babyfish.persistence.instrument.ReferenceComparisonRuleInstrument;
import org.babyfish.util.Joins;
import org.babyfish.util.LazyResource;

/**
 * @author Tao Chen
 */
class MetadataClassImpl implements MetadataClass {
    
    private static final LazyResource<Resource> LAZY_RESOURCE = LazyResource.of(Resource.class);
    
    private static final String IDENTIFIY_REGEX = "\\s*([\\$A-Za-z])([\\$A-Za-z0-9])*\\s*";
    
    private static final Pattern PROPERTY_NAMES_PATTERN = 
            Pattern.compile(IDENTIFIY_REGEX + "(," + IDENTIFIY_REGEX + ")*");
    
    private static final Pattern COMMA_PATTERN = Pattern.compile("\\s*,\\s*");
    
    private File bytecodeFile;
    
    private boolean instrumented;

    private String name;
    
    private String referenceComparisonRule;
    
    private MetadataClassImpl superMetadataClass;
    
    private Class<? extends Annotation> primaryAnnotationType;
    
    private boolean jpaObjectModelInstrument;
    
    private boolean allowEagerReferences;
    
    private boolean allowEagerLobs;
    
    private AccessType accessType;
    
    private String tableName;
    
    private MetadataPropertyImpl declaredIdProperty;
    
    private MetadataPropertyImpl declaredVersionProperty;
    
    private Map<String, MetadataPropertyImpl> declaredProperties;
    
    private MetadataPropertyImpl idProperty;
    
    private MetadataPropertyImpl versionProperty;
    
    private Map<String, MetadataPropertyImpl> properties;
    
    private transient int hasLazyScalarPropertiesState;
    
    private transient Unresolved unresolved;
    
    MetadataClassImpl(File bytecodeFile, ClassNode classNode) {
        this.unresolved = new Unresolved();
        this.unresolved.classNode = classNode;
        this.bytecodeFile = bytecodeFile;
        for (FieldNode fieldNode : (List<FieldNode>)classNode.fields) {
            if (fieldNode.name.equals("{INSTRUMENTED_92B8C17E_BF4E_4135_B596_5A76E0FEBF4E}")) {
                this.instrumented = true;
                break;
            }
        }
        this.name = classNode.name.replace('/', '.');
        if (classNode.superName != null && !classNode.superName.equals("java/lang/Object")) {
            this.unresolved.superClassName = classNode.superName.replace('/', '.');
        }
        if (Context.getAnnotationNode(classNode, IdClass.class) != null) {
            throw new MetadataException(
                    LAZY_RESOURCE.get().idClassIsNotSupported(this.name, IdClass.class)
            );
        }
        AnnotationNode tableNode = Context.getAnnotationNode(classNode, Table.class);
        if (tableNode != null) {
            this.tableName = Nulls.emptyToNull(Context.<String>getAnnotationValue(tableNode, "name"));
        }
        if (this.tableName == null) {
            String name = classNode.name;
            int index = name.lastIndexOf('/');
            if (index == -1) {
                this.tableName = name;
            } else {
                this.tableName = name.substring(index + 1);
            }
        }
        String declaredPropertiesOrder = null;
        for (AnnotationNode annotationNode : (List<AnnotationNode>)classNode.visibleAnnotations) {
            if (annotationNode.desc.equals(ASM.getDescriptor(Entity.class))) {
                this.setPrimaryAnnotationType(Entity.class);
            } else if (annotationNode.desc.equals(ASM.getDescriptor(MappedSuperclass.class))) {
                this.setPrimaryAnnotationType(MappedSuperclass.class);
            } else if (annotationNode.desc.equals(ASM.getDescriptor(Embeddable.class))) {
                this.setPrimaryAnnotationType(Embeddable.class);
            } else if (annotationNode.desc.equals(ASM.getDescriptor(Access.class))) {
                String[] accessType = Context.getAnnotationValue(annotationNode, "value");
                if (accessType != null) {
                    this.accessType = AccessType.valueOf(accessType[1]);
                }
            } else if (annotationNode.desc.equals(ASM.getDescriptor(JPAObjectModelInstrument.class))) {
                this.jpaObjectModelInstrument = true;
                List<String> nameList = Context.<List<String>>getAnnotationValue(
                        annotationNode, "declaredPropertiesOrder"
                );
                if (!Nulls.isNullOrEmpty(nameList)) {
                    declaredPropertiesOrder = Joins.join(nameList);
                    if (!Nulls.isNullOrEmpty(declaredPropertiesOrder)) {
                        declaredPropertiesOrder = declaredPropertiesOrder.trim();
                    }
                }
                Boolean allowEagerReferences = Context.getAnnotationValue(annotationNode, "allowEagerReferences");
                if (allowEagerReferences != null) {
                    this.allowEagerReferences = allowEagerReferences;
                }
                Boolean allowEagerLobs = Context.getAnnotationValue(annotationNode, "allowEagerLobs");
                if (allowEagerLobs != null) {
                    this.allowEagerLobs = allowEagerLobs;
                }
            }
        }
        for (AnnotationNode annotationNode : (List<AnnotationNode>)classNode.visibleAnnotations) {
            if (annotationNode.desc.equals(ASM.getDescriptor(ReferenceComparisonRuleInstrument.class))) {
                if (!this.isEntity()) {
                    throw new MetadataException(
                            LAZY_RESOURCE.get().referenceComparisonInstrumentRequiredEntity(
                                    this.name,
                                    ReferenceComparisonRuleInstrument.class,
                                    Entity.class
                            )
                    );
                }
                this.referenceComparisonRule = 
                        Joins.join(
                                Context.<List<String>>getAnnotationValue(
                                        annotationNode, 
                                        "value"
                                )
                        )
                        .trim();
                break;
            }
        }
        Map<String, MetadataPropertyImpl> declaredProperties = new LinkedHashMap<>();
        for (FieldNode fieldNode : (List<FieldNode>)classNode.fields) {
            if ((fieldNode.access & (Opcodes.ACC_STATIC | Opcodes.ACC_FINAL)) == 0) {
                declaredProperties.put(fieldNode.name, new MetadataPropertyImpl(this, fieldNode));
            }
        }
        for (MethodNode methodNode : (List<MethodNode>)classNode.methods) {
            String propertyName = Context.propertyName(methodNode);
            if (propertyName != null) {
                MetadataPropertyImpl property = declaredProperties.get(propertyName);
                if (property != null) {
                    property.attach(methodNode);
                } else {
                    declaredProperties.put(propertyName, new MetadataPropertyImpl(this, methodNode));
                }
            }
        }
        if (!Nulls.isNullOrEmpty(declaredPropertiesOrder)) {
            if (!PROPERTY_NAMES_PATTERN.matcher(declaredPropertiesOrder).matches()) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().declaredPropertiesOrderMustMatchPattern(
                                this.name,
                                JPAObjectModelInstrument.class,
                                declaredPropertiesOrder,
                                PROPERTY_NAMES_PATTERN.pattern()
                        )
                );
            }
            Map<String, MetadataPropertyImpl> orderedDeclaredProperties = 
                    new LinkedHashMap<>((declaredProperties.size() * 4 + 2) / 3);
            for (String propertyName : COMMA_PATTERN.split(declaredPropertiesOrder)) {
                orderedDeclaredProperties.put(propertyName, null);
            }
            if (orderedDeclaredProperties.size() != declaredProperties.size()) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().badDeclaredPropertyCount(
                                this.name,
                                JPAObjectModelInstrument.class,
                                declaredPropertiesOrder,
                                orderedDeclaredProperties.size(),
                                declaredProperties.size())
                );
            }
            for (Entry<String, MetadataPropertyImpl> entry : orderedDeclaredProperties.entrySet()) {
                MetadataPropertyImpl property = declaredProperties.get(entry.getKey());
                if (property == null) {
                    throw new MetadataException(
                            LAZY_RESOURCE.get().noDeclaredProperty(
                                    this.name,
                                    JPAObjectModelInstrument.class,
                                    entry.getKey()
                            )
                    );
                }
                entry.setValue(property);
            }
            declaredProperties = orderedDeclaredProperties;
        }
        this.declaredProperties = declaredProperties;
    }

    @Override
    public File getBytecodeFile() {
        return this.bytecodeFile;
    }

    public boolean isInstrumented() {
        return this.instrumented;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getTableName() {
        return this.tableName;
    }

    @Override
    public boolean isEntity() {
        return this.primaryAnnotationType == Entity.class;
    }

    @Override
    public boolean isEmbeddable() {
        return this.primaryAnnotationType == Embeddable.class;
    }

    @Override
    public boolean isMappedSuperclass() {
        return this.primaryAnnotationType == MappedSuperclass.class;
    }

    @Override
    public boolean isJPAObjectModelInstrument() {
        return this.jpaObjectModelInstrument;
    }
    
    @Override
    public boolean isEagerReferenceAllowed() {
        return this.allowEagerReferences;
    }
    
    @Override
    public boolean isEagerLobAllowed() {
        return this.allowEagerLobs;
    }

    @Override
    public boolean hasLazyScalarProperties() {
        int hasLazyScalarPropertiesState = this.hasLazyScalarPropertiesState;
        if (hasLazyScalarPropertiesState == 0) {
            if (this.getSuperMetadataClass() != null &&
                    this.getSuperMetadataClass().hasLazyScalarProperties()) {
                hasLazyScalarPropertiesState = +1;
            }
            if (hasLazyScalarPropertiesState == 0) {
                for (MetadataProperty metadataProperty : this.declaredProperties.values()) {
                    if (!metadataProperty.isAssociation() && metadataProperty.getFetchType() == FetchType.LAZY) {
                        hasLazyScalarPropertiesState = +1;
                        break;
                    }
                }
            }
            if (hasLazyScalarPropertiesState == 0) {
                MetadataClassImpl superMetadataClass = this.superMetadataClass;
                if (superMetadataClass != null && superMetadataClass.hasLazyScalarProperties()) {
                    hasLazyScalarPropertiesState = +1;
                }
            }
            if (hasLazyScalarPropertiesState == 0) {
                hasLazyScalarPropertiesState = -1;
            }
        }
        return hasLazyScalarPropertiesState == +1;
    }

    @Override
    public String getReferenceComparisonRule() {
        return this.referenceComparisonRule;
    }

    @Override
    public MetadataClass getSuperMetadataClass() {
        return this.superMetadataClass;
    }

    @Override
    public MetadataProperty getDeclaredIdProperty() {
        return this.declaredIdProperty;
    }

    @Override
    public MetadataProperty getDeclaredVersionProperty() {
        return this.declaredVersionProperty;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public XOrderedMap<String, MetadataProperty> getDeclaredProperties() {
        return (XOrderedMap)this.declaredProperties;
    }
    
    @Override
    public MetadataProperty getIdProperty() {
        return this.idProperty;
    }

    @Override
    public MetadataProperty getVersionProperty() {
        return this.versionProperty;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public XOrderedMap<String, MetadataProperty> getProperties() {
        return (XOrderedMap)this.properties;
    }
    
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MetadataClass)) {
            return false;
        }
        MetadataClass other = (MetadataClass)obj;
        return this.name.equals(other.getName());
    }

    void resolveDeclaredProperties(Context context) {
        for (MetadataPropertyImpl property : this.declaredProperties.values()) {
            property.resolveSelf(context);
            if (property.isId()) {
                if (this.declaredIdProperty != null) {
                    throw new MetadataException(
                            LAZY_RESOURCE.get().duplicatePropertiesWithAnnotation(
                                    this.name,
                                    property.getName(),
                                    this.declaredIdProperty.getName(),
                                    Id.class
                            )
                    );
                }
                this.declaredIdProperty = property;
            }
            if (property.isVersion()) {
                if (this.declaredVersionProperty != null) {
                    throw new MetadataException(
                            LAZY_RESOURCE.get().duplicatePropertiesWithAnnotation(
                                    this.name,
                                    property.getName(),
                                    this.declaredIdProperty.getName(),
                                    Version.class
                            )
                    );
                }
                this.declaredVersionProperty = property;
            }
        }
        if (this.primaryAnnotationType != Entity.class) {
            for (MetadataPropertyImpl metadataPropertyImpl : declaredProperties.values()) {
                if (!metadataPropertyImpl.isBasic() && !metadataPropertyImpl.isEmbedded()) {
                    throw new MetadataException(
                            LAZY_RESOURCE.get().nonEntityCanOnlyContainBasicOrEmbeded(
                                    this.name,
                                    this.primaryAnnotationType,
                                    Basic.class,
                                    Embedded.class,
                                    metadataPropertyImpl.getName()
                            )
                    );
                }
            }
        }
    }

    void resolveInheritence(Context context) {
        if (this.properties != null) {
            return;
        }
        if (this.unresolved.superClassName != null) {
            this.superMetadataClass = (MetadataClassImpl)context.getModelClasses().get(this.unresolved.superClassName);
            if (this.superMetadataClass == null) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().unknownSuperType(
                                this.name,
                                this.unresolved.superClassName
                        )
                );
            }
            if (this.isJPAObjectModelInstrument() != this.superMetadataClass.isJPAObjectModelInstrument()) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().badInstrumentInheritance(
                                this.name,
                                this.superMetadataClass.name,
                                JPAObjectModelInstrument.class
                        )
                );
            }
            this.superMetadataClass.resolveInheritence(context);
        }
        
        AnnotationNode attributeOverridesNode = Context.getAnnotationNode(this.unresolved.classNode, AttributeOverrides.class);
        AnnotationNode attributeOverrideNode = Context.getAnnotationNode(this.unresolved.classNode, AttributeOverride.class);
        if (attributeOverridesNode != null && attributeOverrideNode != null) {
            throw new MetadataException(
                    LAZY_RESOURCE.get().conflictAnnotations(
                            this.name,
                            AttributeOverride.class,
                            AttributeOverrides.class
                    )
            );
        }
        if (attributeOverridesNode != null) {
            List<AnnotationNode> attributeOverrideNodes = Context.getAnnotationValue(attributeOverridesNode, "value");
            if (!Nulls.isNullOrEmpty(attributeOverrideNodes)) {
                if (this.superMetadataClass == null) {
                    throw new MetadataException(
                            LAZY_RESOURCE.get().needSuperClass(
                                    this.name,
                                    AttributeOverrides.class
                            )
                    );
                }
                for (AnnotationNode attributeOverrideNode_ : attributeOverrideNodes) {
                    this.resolveAttributeOverride(attributeOverrideNode_);
                }
            }
        }
        if (attributeOverrideNode != null) {
            if (this.superMetadataClass == null) {
                throw new MetadataException(
                        LAZY_RESOURCE.get().needSuperClass(
                                this.name,
                                AttributeOverride.class
                        )
                );
            }
            this.resolveAttributeOverride(attributeOverrideNode);
        }
        
        int propertyCount = 
                this.declaredProperties.size() + 
                (this.superMetadataClass == null ? 0 : this.superMetadataClass.properties.size());
        XOrderedMap<String, MetadataPropertyImpl> properties = new LinkedHashMap<>((propertyCount * 4 + 2) / 3);
        if (this.superMetadataClass != null) {
            properties.putAll(this.superMetadataClass.properties);
        }
        properties.putAll(this.declaredProperties);
        this.properties = properties;
        
        if (this.superMetadataClass == null) {
            this.idProperty = this.declaredIdProperty;
            this.versionProperty = this.declaredVersionProperty;
        } else {
            if (this.declaredIdProperty != null) {
                if (this.superMetadataClass.getIdProperty() != null) {
                    throw new MetadataException(
                            LAZY_RESOURCE.get().derivedClassCanNotHaveAnnotation(
                                    this.name,
                                    this.declaredIdProperty.getName(),
                                    Id.class
                            )
                    );
                }
                this.idProperty = this.declaredIdProperty;
            } else {
                this.idProperty = this.superMetadataClass.idProperty;
            }
            if (this.declaredVersionProperty != null) {
                if (this.superMetadataClass.versionProperty != null) {
                    throw new MetadataException(
                            LAZY_RESOURCE.get().derivedClassCanNotHaveAnnotation(
                                    this.name,
                                    this.declaredVersionProperty.getName(),
                                    Version.class
                            )
                    );
                }
                this.versionProperty = this.declaredVersionProperty;
            } else {
                this.versionProperty = this.superMetadataClass.versionProperty;
            }
        }
        
        if (this.isEntity() && this.idProperty == null) {
            throw new MetadataException(
                    LAZY_RESOURCE.get().requireIdProperty(
                            this.name,
                            Entity.class,
                            Id.class
                    )
            );
        }
    }
    
    void resolveEmbeddedProeprties(Context context) {
        for (MetadataPropertyImpl property : this.declaredProperties.values()) {
            property.resolveEmbedded(context);
        }
    }

    void resolveJoins(Context context) {
        for (MetadataPropertyImpl property : this.declaredProperties.values()) {
            property.resolveJoin(context);
        }
    }
    
    void resolveExplicitOppositeProperties(Context context) {
        for (MetadataPropertyImpl property : this.declaredProperties.values()) {
            property.resolveExplicitOppositeProperty(context);
        }
    }
    
    void resolveImplicitOppositeProperties(Context context) {
        for (MetadataPropertyImpl property : this.declaredProperties.values()) {
            property.resolveImplicitOppositeProperty(context);
        }
    }
    
    void resolveReferenceProperties(Context context) {
        for (MetadataPropertyImpl property : this.declaredProperties.values()) {
            property.resolveReference(context);
        }
    }
    
    void resolveContravarianceProeprties(Context context) {
        for (MetadataPropertyImpl property : this.declaredProperties.values()) {
            property.resolveContravariance(context);
        }
    }

    void finishResolving() {
        this.declaredProperties = MACollections.unmodifiable(this.declaredProperties);
        this.properties = MACollections.unmodifiable(this.properties);
        this.unresolved = null;
        for (MetadataPropertyImpl property : this.declaredProperties.values()) {
            property.finishResolving();
        }
    }
    
    AccessType getAccessType() {
        return this.accessType;
    }
    
    ClassNode getClassNode() {
        return this.unresolved.classNode;
    }
    
    private void resolveAttributeOverride(AnnotationNode attributeOverrideNode) {
        String name = Context.getAnnotationValue(attributeOverrideNode, "name");
        MetadataPropertyImpl overriddenProperty = (MetadataPropertyImpl)this.superMetadataClass.getProperties().get(name);
        if (overriddenProperty == null) {
            throw new MetadataException(
                    LAZY_RESOURCE.get().noOverridenProperty(
                            this.name,
                            this.superMetadataClass.name,
                            name
                    )
            );
        }
        if (overriddenProperty.isAssociation()) {
            throw new MetadataException(
                    LAZY_RESOURCE.get().overridenPropertyCanNotBeAssociation(
                            this.name,
                            this.superMetadataClass.name,
                            name
                    ));
        }
        if (this.declaredProperties.containsKey(name)) {
            return;
        }
        AnnotationNode columnNode = Context.getAnnotationValue(attributeOverrideNode, "column");
        String columnName = Context.getAnnotationValue(columnNode, "name");
        if (columnName == null) {
            columnName = name;
        }
        MetadataPropertyImpl overrideProeprty = new MetadataPropertyImpl(this, overriddenProperty, columnName);
        this.declaredProperties.put(overrideProeprty.getName(), overrideProeprty);
    }
    
    private void setPrimaryAnnotationType(Class<? extends Annotation> primaryAnnotationType) {
        if (this.primaryAnnotationType != null) {
            throw new MetadataException(
                    LAZY_RESOURCE.get().conflictAnnotations(
                            this.name, 
                            primaryAnnotationType, 
                            this.primaryAnnotationType)
            );
        }
        this.primaryAnnotationType = primaryAnnotationType;
    }

    private static class Unresolved {
        ClassNode classNode;
        String superClassName;
    }
    
    private interface Resource {

        String idClassIsNotSupported(String className, Class<IdClass> idClassTypeConstant);

        String overridenPropertyCanNotBeAssociation(
                String className, 
                String superClassName,
                String overridenAttributeName);

        String noOverridenProperty(
                String className, 
                String superClassName,
                String overridenAttributeName);

        String requireIdProperty(
                String className, 
                Class<Entity> entityTypeConstant,
                Class<Id> idTypeConstant);

        String derivedClassCanNotHaveAnnotation(
                String className, 
                String propertyName, 
                Class<? extends Annotation> annotationType);

        String needSuperClass(
                String className, 
                Class<? extends Annotation> annotationType);

        String conflictAnnotations(
                String className,
                Class<? extends Annotation> annotationType1,
                Class<? extends Annotation> annotationType2);

        String badInstrumentInheritance(
                String className, 
                String superClassName,
                Class<JPAObjectModelInstrument> jpaObjectModelInstrumentTypeConstant);

        String unknownSuperType(
                String className, 
                String superClassName);

        String nonEntityCanOnlyContainBasicOrEmbeded(
                String className, 
                Class<? extends Annotation> annotationType,
                Class<Basic> basicTypeConstant, 
                Class<Embedded> embeddedTypeConstant, 
                String property);

        String duplicatePropertiesWithAnnotation(
                String className, 
                String propertyName1,
                String propertyName2, 
                Class<? extends Annotation> annotationType);

        String noDeclaredProperty(
                String className,
                Class<JPAObjectModelInstrument> jpaObjectModelInstrumentTypeConstant, 
                String property);

        String badDeclaredPropertyCount(
                String className,
                Class<JPAObjectModelInstrument> jpaObjectModelInstrumentTypeConstant,
                String declaredPropertiesOrder, 
                int expectedCount, 
                int actualCount);

        String declaredPropertiesOrderMustMatchPattern(
                String className,
                Class<JPAObjectModelInstrument> jpaObjectModelInstrumentTypeConstant,
                String declaredPropertiesOrder, 
                String pattern);

        String referenceComparisonInstrumentRequiredEntity(
                String className,
                Class<ReferenceComparisonRuleInstrument> jpaObjectModelInstrumentTypeConstant,
                Class<Entity> entityTypeConstant);
    }
}
