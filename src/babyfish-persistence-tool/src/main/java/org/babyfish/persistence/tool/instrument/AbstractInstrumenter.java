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
package org.babyfish.persistence.tool.instrument;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.lang.Nulls;
import org.babyfish.lang.reflect.asm.ASM;
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.AllowDisability;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.Contravariance;
import org.babyfish.model.metadata.Deferrable;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.ObjectModelMetadata;
import org.babyfish.model.metadata.ObjectModelMode;
import org.babyfish.model.metadata.ReferenceComparisonRule;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.org.objectweb.asm.AnnotationVisitor;
import org.babyfish.org.objectweb.asm.ClassReader;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.ClassWriter;
import org.babyfish.org.objectweb.asm.FieldVisitor;
import org.babyfish.org.objectweb.asm.Label;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.org.objectweb.asm.Type;
import org.babyfish.persistence.instrument.JPAObjectModelInstrument;
import org.babyfish.persistence.model.metadata.EntityId;
import org.babyfish.persistence.model.metadata.IndexMapping;
import org.babyfish.persistence.model.metadata.Inverse;
import org.babyfish.persistence.model.metadata.KeyMapping;
import org.babyfish.persistence.model.metadata.OptimisticLock;
import org.babyfish.persistence.tool.instrument.metadata.MetadataAnnotation;
import org.babyfish.persistence.tool.instrument.metadata.MetadataClass;
import org.babyfish.persistence.tool.instrument.metadata.MetadataClasses;
import org.babyfish.persistence.tool.instrument.metadata.MetadataProperty;
import org.babyfish.reference.IndexedReference;
import org.babyfish.util.LazyResource;

/**
 * @author Tao Chen
 */
public class AbstractInstrumenter {
    
    private static final LazyResource<Resource> LAZY_RESOURCE = LazyResource.of(Resource.class);
    
    private Map<String, MetadataClass> temporaryMetadataClasses;
    
    private Map<MetadataClass, InstrumentedResult> instrumentedResults;
    
    protected AbstractInstrumenter(Iterable<File> bytecodeFiles, Class<? extends Annotation> anyAnnotationType) {
        this.temporaryMetadataClasses = MetadataClasses.anyAnnotationType(bytecodeFiles, anyAnnotationType);
        Map<MetadataClass, InstrumentedResult> instrumentedResults = new LinkedHashMap<>((temporaryMetadataClasses.size() * 4 + 2) / 3);
        for (MetadataClass metadataClass : temporaryMetadataClasses.values()) {
            if (!metadataClass.isInstrumented()) {
                InstrumentedResult instrumentedResult = this.instrument(metadataClass);
                instrumentedResults.put(instrumentedResult.getMetadataClass(), instrumentedResult);
            }
        }
        this.instrumentedResults = MACollections.unmodifiable(instrumentedResults);
        this.temporaryMetadataClasses = null;
    }
    
    public final Map<MetadataClass, InstrumentedResult> getInstrumentedResults() {
        return this.instrumentedResults;
    }
    
    protected OwnerAdapter createOwnerAdapter(ClassVisitor cv) {
        return this.new OwnerAdapter(cv);
    }

    private InstrumentedResult instrument(MetadataClass metadataClass) {
        try (InputStream inputStream = new FileInputStream(metadataClass.getBytecodeFile().getPath())) {
            ClassReader ownerClassReader = new ClassReader(inputStream);
            ClassWriter ownerClassWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            OwnerAdapter ownerAdapter = this.createOwnerAdapter(ownerClassWriter);
            ownerClassReader.accept(ownerAdapter, 0);
            byte[] objectModelBytecode = this.createObjectModel(ownerAdapter.getMetadataClass(), ownerAdapter.getVersion());
            return new InstrumentedResult(metadataClass, ownerClassWriter.toByteArray(), objectModelBytecode);
        } catch (IOException ex) {
            throw new InstrumentException(
                    LAZY_RESOURCE.get().canNotReadBytecode(
                            metadataClass.getBytecodeFile().getPath()
                    ),
                    ex
            );
        }
    }

    private byte[] createObjectModel(MetadataClass metadataClass, int version) {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(
                version, 
                Opcodes.ACC_INTERFACE | Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_ABSTRACT, 
                metadataClass.getName().replace('.', '/') + "${OM}", 
                null, 
                "java/lang/Object", 
                null
        );
        
        AnnotationVisitor av = cw.visitAnnotation(ASM.getDescriptor(ObjectModelDeclaration.class), true);
        av.visit("provider", "jpa");
        if (metadataClass.isEmbeddable()) {
            av.visitEnum("mode", ASM.getDescriptor(ObjectModelMode.class), ObjectModelMode.EMBEDDABLE.name());
        } else if (metadataClass.isMappedSuperclass()) {
            av.visitEnum("mode", ASM.getDescriptor(ObjectModelMode.class), ObjectModelMode.ABSTRACT.name());
        } else {
            av.visitEnum("mode", ASM.getDescriptor(ObjectModelMode.class), ObjectModelMode.REFERENCE.name());
        }
        if (metadataClass.isEmbeddable() || metadataClass.isMappedSuperclass()) {
            AnnotationVisitor arrVisitor = av.visitArray("declaredPropertiesOrder");
            for (String declaredParopertyName : metadataClass.getDeclaredProperties().keySet()) {
                arrVisitor.visit(null, declaredParopertyName);
            }
            arrVisitor.visitEnd();
        }
        av.visitEnd();
        
        if (metadataClass.getSuperMetadataClass() == null) {
            cw.visitAnnotation(ASM.getDescriptor(AllowDisability.class), true).visitEnd();
        }
        
        if (!Nulls.isNullOrEmpty(metadataClass.getReferenceComparisonRule())) {
            av = cw.visitAnnotation(ASM.getDescriptor(ReferenceComparisonRule.class), true);
            AnnotationVisitor arrVisitor = av.visitArray("value");
            arrVisitor.visit(null, metadataClass.getReferenceComparisonRule());
            arrVisitor.visitEnd();
            av.visitEnd();
        }
        
        cw.visitInnerClass(
                metadataClass.getName().replace('.', '/') + "${OM}", 
                metadataClass.getName().replace('.', '/'), 
                "{OM}", 
                Opcodes.ACC_INTERFACE | Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_ABSTRACT
        );
        for (MetadataProperty metadataProperty : metadataClass.getDeclaredProperties().values()) {
            if (!metadataProperty.isOverriding() && metadataProperty.getReferenceProperty() == null) {
                if (metadataProperty.isAssociation()) {
                    this.visitObjectModelAssociationPropertyGetter(cw, metadataProperty);
                } else {
                    this.visitObjectModelScalarPropertyGetter(cw, metadataProperty);
                    this.visitObjectModelScalarPropertySetter(cw, metadataProperty);
                }
            }
        }
        cw.visitEnd();
        return cw.toByteArray();
    }
    
    private void visitObjectModelScalarPropertyGetter(ClassVisitor cv, MetadataProperty metadataProperty) {
        
        MethodVisitor mv;
        try {
        mv = cv.visitMethod(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, 
                metadataProperty.getGetterName(), 
                "()" + metadataProperty.getTypeDescriptor(), 
                null,
                null
        );
        } catch (RuntimeException ex) {
            throw ex;
        }
        if (metadataProperty.isId()) {
            mv.visitAnnotation(ASM.getDescriptor(EntityId.class), true).visitEnd();
        } else if (metadataProperty.isVersion()) {
            mv.visitAnnotation(ASM.getDescriptor(OptimisticLock.class), true).visitEnd();
        } else if (metadataProperty.getFetchType() == FetchType.LAZY && !metadataProperty.isEmbedded()) {
            mv.visitAnnotation(ASM.getDescriptor(Deferrable.class), true).visitEnd();
        } else {
            mv.visitAnnotation(ASM.getDescriptor(Scalar.class), true).visitEnd();
        }
        mv.visitEnd();
    }
    
    private void visitObjectModelScalarPropertySetter(ClassVisitor cv, MetadataProperty metadataProperty) {
        String setterName = metadataProperty.getSetterName(false);
        cv
        .visitMethod(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, 
                setterName, 
                '(' + metadataProperty.getTypeDescriptor() + ")V", 
                null,
                null
        )
        .visitEnd();
    }
    
    private void visitObjectModelAssociationPropertyGetter(ClassVisitor cv, MetadataProperty metadataProperty) {
        String signature = 
                "()L" + 
                ASM.getInternalName(metadataProperty.getStandardAssocationType()) + 
                '<' +
                (metadataProperty.getKeyTypeDescriptor() != null ? metadataProperty.getKeyTypeDescriptor() : "") +
                'L' +
                metadataProperty.getRelatedClass().getName().replace('.', '/') +
                ";>;";
        MethodVisitor mv = cv.visitMethod(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, 
                objectModelAssociationName(metadataProperty, true), 
                "()" + ASM.getDescriptor(metadataProperty.getStandardAssocationType()), 
                signature, 
                null);
        if (metadataProperty.getCovarianceProperty() != null) {
            AnnotationVisitor av = mv.visitAnnotation(ASM.getDescriptor(Contravariance.class), true);
            av.visit("value", objectModelAssociationName(metadataProperty.getCovarianceProperty(), false));
            av.visitEnd();
        } else {
            AnnotationVisitor av;
            av = mv.visitAnnotation(ASM.getDescriptor(Association.class), true);
            if (metadataProperty.getOppositeProperty() != null) {
                av.visit("opposite", objectModelAssociationName(metadataProperty.getOppositeProperty(), false));
            }
            av.visitEnd();
            if (metadataProperty.isInverse()) {
                mv.visitAnnotation(ASM.getDescriptor(Inverse.class), true).visitEnd();
            }
            if (metadataProperty.getIndexProperty() != null) {
                av = mv.visitAnnotation(ASM.getDescriptor(IndexMapping.class), true);
                av.visit("value", metadataProperty.getIndexProperty().getName());
                av.visitEnd();
            }
            if (metadataProperty.getKeyProperty() != null) {
                av = mv.visitAnnotation(ASM.getDescriptor(KeyMapping.class), true);
                av.visit("value", metadataProperty.getKeyProperty().getName());
                av.visitEnd();
            }
            if (!Nulls.isNullOrEmpty(metadataProperty.getReferenceComparisonRule())) {
                av = mv.visitAnnotation(ASM.getDescriptor(ReferenceComparisonRule.class), true);
                AnnotationVisitor arrVisitor = av.visitArray("value");
                arrVisitor.visit(null, metadataProperty.getReferenceComparisonRule());
                arrVisitor.visitEnd();
                av.visitEnd();
            }
            mv.visitEnd();
        }
    }
    
    private static String objectModelAssociationName(MetadataProperty metadataProperty, boolean method) {
        String name = method ? metadataProperty.getGetterName() : metadataProperty.getName();
        return 
                metadataProperty.isReference() ? 
                name + "Reference" : 
                name;
    }
    
    protected class OwnerAdapter extends ClassVisitor {
        
        protected static final String OWNER_OM = "{om}";

        protected static final String OWNER_OM_FACTORY = "{OM_FACTORY}";
        
        private String name;
        
        private String superName;

        private MetadataClass metadataClass;
        
        private int version;
        
        private String objectModelInternalName;
        
        private String objectModelDescriptor;
        
        private boolean hasOriginalClinit;
        
        private boolean hasOriginalToString;
        
        public OwnerAdapter(ClassVisitor cv) {
            super(Opcodes.ASM5, cv);
        }

        protected final MetadataClass getMetadataClass() {
            if (this.metadataClass == null) {
                throw new IllegalStateException(
                        LAZY_RESOURCE.get().metdataClassIsNotReady(this.getClass())
                );
            }
            return this.metadataClass;
        }

        protected final int getVersion() {
            return this.version;
        }

        protected final String getObjectModelInternalName() {
            return this.objectModelInternalName;
        }
        
        protected final String getObjectModelDescriptor() {
            return this.objectModelDescriptor;
        }

        protected final boolean isHasOriginalClinit() {
            return this.hasOriginalClinit;
        }
        
        protected void onInit() {
            
        }

        protected String[] getAdditionalInterfaces() {
            return null;
        }

        protected void addAdditionalMembers() {
            
        }

        @Override
        public void visit(
                int version, 
                int access, 
                String name, 
                String signature, 
                String superName, 
                String[] interfaces) {
            this.name = name;
            this.superName = superName;
            this.metadataClass = AbstractInstrumenter.this.temporaryMetadataClasses.get(name.replace('/', '.'));
            if (this.metadataClass == null) {
                throw new InstrumentException(
                        LAZY_RESOURCE.get().canNotFindMetdataClass(name.replace('.', '/'))
                );
            }
            if (!this.metadataClass.isJPAObjectModelInstrument()) {
                throw new InstrumentException(
                        LAZY_RESOURCE.get().isNotJPAObjectModelInstrument(
                                this.metadataClass.getName(), 
                                JPAObjectModelInstrument.class
                        )
                );
            }
            this.version = version;
            this.objectModelInternalName = this.metadataClass.getName().replace('.', '/') + "${OM}";
            this.objectModelDescriptor = 'L' + this.objectModelInternalName + ';';
            this.onInit();
            String[] additionalInterfaceNames = this.getAdditionalInterfaces();
            if (!Nulls.isNullOrEmpty(additionalInterfaceNames)) {
                int capacity = interfaces != null ? interfaces.length : 0;
                capacity += additionalInterfaceNames.length;
                capacity = (capacity * 4 + 2) / 3;
                XOrderedSet<String> interfaceNames = new LinkedHashSet<>(capacity);
                for (String interfaze : interfaces) {
                    interfaceNames.add(interfaze);
                }
                for (String interfaceName : additionalInterfaceNames) {
                    if (!Nulls.isNullOrEmpty(interfaceName)) {
                        interfaceNames.add(interfaceName);
                    }
                }
                interfaces = interfaceNames.toArray(new String[ interfaceNames.size()]);
            }
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            if (desc.equals(ASM.getDescriptor(Access.class))) {
                return null;
            }
            return super.visitAnnotation(desc, visible);
        }

        @Override
        public FieldVisitor visitField(
                int access, 
                String name, 
                String desc,
                String signature, 
                Object value) {
            if (this.metadataClass.getDeclaredProperties().containsKey(name)) {
                if ((access & Opcodes.ACC_PRIVATE) == 0) {
                    // After instrument, the field will be deleted, make sure the field is private 
                    // to guarantee the other classes don't contains the code that uses this field.
                    throw new InstrumentException(
                            LAZY_RESOURCE.get().fieldOfInstrumentedClassMustBePrivate(
                                    this.metadataClass.getName(),
                                    JPAObjectModelInstrument.class,
                                    name
                            )
                    );
                }
                return null;
            }
            return super.visitField(access, name, desc, signature, value);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            
            // Change the all the code of the method
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            if ((access & Opcodes.ACC_STATIC) == 0) {
                for (MetadataProperty metadataProperty : this.metadataClass.getDeclaredProperties().values()) {
                    if (name.equals(metadataProperty.getGetterName()) && desc.equals("()" + metadataProperty.getTypeDescriptor())) {
                        return new GetterAdapter(mv, metadataProperty);
                    }
                    if (name.equals(metadataProperty.getSetterName(true)) && desc.equals('(' + metadataProperty.getTypeDescriptor() + ")V")) {
                        return new SetterAdapter(mv, metadataProperty);
                    }
                }
                if (this.metadataClass.isEmbeddable()) {
                    if (name.equals("hashCode") && desc.equals("()I")) {
                        throw new InstrumentException(
                                LAZY_RESOURCE.get().instrumentedEmbeddableMustNotDeclareHashCode(
                                        this.metadataClass.getName(),
                                        JPAObjectModelInstrument.class,
                                        Embeddable.class
                                )
                        );
                    }
                    if (name.equals("equals") && desc.equals("(Ljava/lang/Object;)Z")) {
                        throw new InstrumentException(
                                LAZY_RESOURCE.get().instrumentedEmbeddableMustNotDeclareEquals(
                                        this.metadataClass.getName(),
                                        JPAObjectModelInstrument.class,
                                        Embeddable.class
                                )
                        );
                    }
                }
            }
            
            // Change some code of the method
            if ((access & Opcodes.ACC_STATIC) != 0 && name.equals("<clinit>") && desc.equals("()V")) {
                this.hasOriginalClinit = true;
                mv = new ClinitAdapter(mv);
            }
            if ((access & Opcodes.ACC_STATIC) == 0 && name.equals("<init>") && desc.endsWith(")V")) {
                mv = new InitAdapter(mv);
            }
            if ((access & Opcodes.ACC_STATIC) == 0 && name.equals("toString") && desc.equals("()Ljava/lang/String;")) {
                this.hasOriginalToString = true;
            }
            return mv;
        }

        @Override
        public void visitEnd() {
            this.addAdditionalMembers();
            this.visitInstrumentedField();
            this.visitObjectModelFactoryField();
            this.visitObjectModelField();
            this.visitGetObjectModelMethod();
            this.visitHashCodeMethod();
            this.visitEqualsMethod();
            this.visitObjectModel();
            if (!this.hasOriginalClinit) {
                MethodVisitor mv = this.cv.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
                mv.visitCode();
                this.visitInitObjectModelFactoryInsns(mv);
                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
            if (!this.hasOriginalToString) {
                this.visitToStringMethod();
            }
            super.visitEnd();
        }
        
        private void visitInstrumentedField() {
            this
            .cv
            .visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, 
                    "{INSTRUMENTED_92B8C17E_BF4E_4135_B596_5A76E0FEBF4E}", 
                    "Z", 
                    null, 
                    true
            )
            .visitEnd();
        }
        
        private void visitObjectModelFactoryField() {
            this
            .cv
            .visitField(
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, 
                    OWNER_OM_FACTORY, 
                    ASM.getDescriptor(ObjectModelFactory.class), 
                    ASM.getDescriptor(ObjectModelFactory.class) + "<L" + this.objectModelInternalName + ";>", 
                    null
            )
            .visitEnd();
        }
        
        private void visitObjectModelField() {
            this
            .cv
            .visitField(
                    Opcodes.ACC_PRIVATE, 
                    OWNER_OM, 
                    'L' + this.objectModelInternalName + ';', 
                    null, 
                    null
            )
            .visitEnd();
        }
        
        private void visitGetObjectModelMethod() {
            MethodVisitor mv = this.cv.visitMethod(
                    Opcodes.ACC_STATIC, 
                    OWNER_OM, 
                    "(L" +
                    this.metadataClass.getName().replace('.', '/') +
                    ";)L" + 
                    this.objectModelInternalName + 
                    ';', 
                    null,
                    null);
            mv.visitAnnotation(ASM.getDescriptor(StaticMethodToGetObjectModel.class), true).visitEnd();
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.metadataClass.getName().replace('.', '/'),
                    OWNER_OM, 
                    'L' + this.objectModelInternalName + ';'
            );
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void visitHashCodeMethod() {
            if (!this.metadataClass.isEmbeddable()) {
                return;
            }
            MethodVisitor mv = this.cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "hashCode", 
                    "()I", 
                    null, 
                    null);
            mv.visitCode();
            
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    this.metadataClass.getName().replace('.', '/'), 
                    OWNER_OM_FACTORY, 
                    ASM.getDescriptor(ObjectModelFactory.class));
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    ASM.getInternalName(ObjectModelFactory.class), 
                    "getObjectModelMetadata", 
                    "()" + ASM.getDescriptor(ObjectModelMetadata.class),
                    true);
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    ASM.getInternalName(ObjectModelMetadata.class), 
                    "getOwnerEqualityComparator", 
                    "()" + ASM.getDescriptor(EqualityComparator.class),
                    true);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE,
                    ASM.getInternalName(EqualityComparator.class),
                    "hashCode",
                    "(Ljava/lang/Object;)I",
                    true);
            mv.visitInsn(Opcodes.IRETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void visitEqualsMethod() {
            if (!this.metadataClass.isEmbeddable()) {
                return;
            }
            MethodVisitor mv = this.cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "equals", 
                    "(Ljava/lang/Object;)Z", 
                    null, 
                    null);
            mv.visitCode();
            
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    this.metadataClass.getName().replace('.', '/'), 
                    OWNER_OM_FACTORY, 
                    ASM.getDescriptor(ObjectModelFactory.class));
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    ASM.getInternalName(ObjectModelFactory.class), 
                    "getObjectModelMetadata", 
                    "()" + ASM.getDescriptor(ObjectModelMetadata.class),
                    true);
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    ASM.getInternalName(ObjectModelMetadata.class), 
                    "getOwnerEqualityComparator", 
                    "()" + ASM.getDescriptor(EqualityComparator.class),
                    true);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE,
                    ASM.getInternalName(EqualityComparator.class),
                    "equals",
                    "(Ljava/lang/Object;Ljava/lang/Object;)Z",
                    true);
            mv.visitInsn(Opcodes.IRETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void visitToStringMethod() {
            MethodVisitor mv = this.cv.visitMethod(Opcodes.ACC_PUBLIC, "toString", "()Ljava/lang/String;", null, null);
            mv.visitCode();
            
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.metadataClass.getName().replace('.', '/'), 
                    OWNER_OM, 
                    this.objectModelDescriptor);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    "java/lang/Object", 
                    "toString", 
                    "()Ljava/lang/String;",
                    false);
            mv.visitInsn(Opcodes.ARETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void visitObjectModel() {
            this
            .cv
            .visitInnerClass(
                    this.objectModelInternalName, 
                    this.metadataClass.getName().replace('.', '/'), 
                    "{OM}", 
                    Opcodes.ACC_INTERFACE | Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_ABSTRACT
            );
        }
        
        private void visitInitObjectModelFactoryInsns(MethodVisitor mv) {
            if (this.metadataClass.isJPAObjectModelInstrument()) {
                mv.visitLdcInsn(Type.getType('L' + this.objectModelInternalName + ';'));
                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC, 
                        ASM.getInternalName(ObjectModelFactoryFactory.class), 
                        "factoryOf", 
                        "(Ljava/lang/Class;)" + ASM.getDescriptor(ObjectModelFactory.class),
                        false
                );
                mv.visitFieldInsn(
                        Opcodes.PUTSTATIC, 
                        this.metadataClass.getName().replace('.', '/'), 
                        OWNER_OM_FACTORY, 
                        ASM.getDescriptor(ObjectModelFactory.class));
            }
        }
        
        private void visitInitObjectModelInsns(MethodVisitor mv) {
            if (this.metadataClass.isJPAObjectModelInstrument()) {
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(
                        Opcodes.GETSTATIC, 
                        this.metadataClass.getName().replace('.', '/'), 
                        OWNER_OM_FACTORY, 
                        ASM.getDescriptor(ObjectModelFactory.class));
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        ASM.getInternalName(ObjectModelFactory.class), 
                        "create", 
                        "(Ljava/lang/Object;)Ljava/lang/Object;",
                        true);
                mv.visitTypeInsn(Opcodes.CHECKCAST, this.objectModelInternalName);
                mv.visitFieldInsn(
                        Opcodes.PUTFIELD, 
                        this.metadataClass.getName().replace('.', '/'),
                        OWNER_OM, 
                        'L' + this.objectModelInternalName + ';'
                );
            }
        }
        
        private void visitGetterInsns(MethodVisitor mv, MetadataProperty metadataProperty) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.metadataClass.getName().replace('.', '/'),
                    OWNER_OM, 
                    'L' + this.objectModelInternalName + ';'
            );
            if (metadataProperty.getReferenceProperty() != null) {
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        this.objectModelInternalName, 
                        metadataProperty.getReferenceProperty().getGetterName() + "Reference", 
                        "()" + ASM.getDescriptor(metadataProperty.getReferenceProperty().getStandardAssocationType()),
                        true);
                if (metadataProperty.getReferenceProperty().getStandardAssocationType() == IndexedReference.class) {
                    mv.visitMethodInsn(
                            Opcodes.INVOKEINTERFACE, 
                            ASM.getInternalName(metadataProperty.getReferenceProperty().getStandardAssocationType()), 
                            "getIndex", 
                            "()I",
                            true);
                } else {
                    mv.visitMethodInsn(
                            Opcodes.INVOKEINTERFACE, 
                            ASM.getInternalName(metadataProperty.getReferenceProperty().getStandardAssocationType()), 
                            "getKey", 
                            "()Ljava/lang/Object;",
                            true);
                    mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(metadataProperty.getTypeDescriptor()));
                }
            } else if (metadataProperty.isReference()) {
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        this.objectModelInternalName, 
                        metadataProperty.getGetterName() + "Reference", 
                        "()" + ASM.getDescriptor(metadataProperty.getStandardAssocationType()),
                        true);
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        ASM.getInternalName(metadataProperty.getStandardAssocationType()), 
                        "get", 
                        "()Ljava/lang/Object;",
                        true);
                mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(metadataProperty.getTypeDescriptor()));
            } else {
                String objectModelPropertyTypeDescriptor;
                if (metadataProperty.getStandardAssocationType() == null) {
                    objectModelPropertyTypeDescriptor = metadataProperty.getTypeDescriptor();
                } else {
                    objectModelPropertyTypeDescriptor = ASM.getDescriptor(metadataProperty.getStandardAssocationType());
                }
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        this.objectModelInternalName, 
                        metadataProperty.getGetterName(), 
                        "()" + objectModelPropertyTypeDescriptor,
                        true);
            }
            mv.visitInsn(ASM.getReturnCode(metadataProperty.getTypeDescriptor()));
        }
        
        private void visitSetterInsns(MethodVisitor mv, MetadataProperty metadataProperty) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.metadataClass.getName().replace('.', '/'),
                    OWNER_OM, 
                    this.objectModelDescriptor
            );
            if (metadataProperty.isCollection()) {
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        this.objectModelInternalName, 
                        metadataProperty.getGetterName(), 
                        "()" + metadataProperty.getTypeDescriptor(),
                        true);
                mv.visitVarInsn(Opcodes.ASTORE, 2);
                mv.visitVarInsn(Opcodes.ALOAD, 2);
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        ASM.getInternalName(metadataProperty.getTypeDescriptor()), 
                        "clear", 
                        "()V",
                        true);
                Label isParameterNullLabel = new Label();
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitJumpInsn(Opcodes.IFNULL, isParameterNullLabel);
                mv.visitVarInsn(Opcodes.ALOAD, 2);
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                if (Map.class.isAssignableFrom(metadataProperty.getStandardAssocationType())) {
                    mv.visitMethodInsn(
                            Opcodes.INVOKEINTERFACE, 
                            ASM.getInternalName(Map.class), 
                            "putAll", 
                            '(' +
                            ASM.getDescriptor(Map.class) +
                            ")Ljava/lang/Object;",
                            true);
                } else {
                    mv.visitMethodInsn(
                            Opcodes.INVOKEINTERFACE, 
                            ASM.getInternalName(Collection.class), 
                            "addAll", 
                            '(' +
                            ASM.getDescriptor(Collection.class) +
                            ")Z",
                            true);
                }
                mv.visitInsn(Opcodes.POP);
                mv.visitLabel(isParameterNullLabel);
            } else if (metadataProperty.isReference()) {
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        this.objectModelInternalName, 
                        metadataProperty.getGetterName() + "Reference", 
                        "()" + ASM.getDescriptor(metadataProperty.getStandardAssocationType()),
                        true);
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        ASM.getInternalName(metadataProperty.getStandardAssocationType()), 
                        "set", 
                        "(Ljava/lang/Object;)Ljava/lang/Object;",
                        true);
                mv.visitInsn(Opcodes.POP);
            } else if (metadataProperty.getReferenceProperty() != null) {
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        this.objectModelInternalName, 
                        metadataProperty.getGetterName() + "Reference", 
                        "()" + ASM.getDescriptor(metadataProperty.getReferenceProperty().getStandardAssocationType()),
                        true);
                mv.visitVarInsn(ASM.getLoadCode(metadataProperty.getTypeDescriptor()), 1);
                if (metadataProperty.getReferenceProperty().getStandardAssocationType() == IndexedReference.class) {
                    mv.visitMethodInsn(
                            Opcodes.INVOKEINTERFACE, 
                            ASM.getInternalName(metadataProperty.getReferenceProperty().getStandardAssocationType()), 
                            "setIndex", 
                            "(I)V",
                            true);
                } else {
                    mv.visitMethodInsn(
                            Opcodes.INVOKEINTERFACE, 
                            ASM.getInternalName(metadataProperty.getReferenceProperty().getStandardAssocationType()), 
                            "setKey", 
                            "(Ljava/lang/Object;)V",
                            true);
                }
            } else {
                mv.visitVarInsn(ASM.getLoadCode(metadataProperty.getTypeDescriptor()), 1);
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        this.objectModelInternalName, 
                        metadataProperty.getSetterName(false), 
                        '(' + metadataProperty.getTypeDescriptor() + ")V",
                        true);
            }
            mv.visitInsn(Opcodes.RETURN);
        }
        
        private class NonPropertyMethodAdapter extends MethodVisitor {

            public NonPropertyMethodAdapter(MethodVisitor mv) {
                super(Opcodes.ASM5, mv);
            }

            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                if (opcode == Opcodes.GETFIELD || opcode == Opcodes.PUTFIELD) {
                    OwnerAdapter that = OwnerAdapter.this;
                    MetadataProperty metadataProperty = that.metadataClass.getProperties().get(name);
                    if (metadataProperty != null) {
                        if (opcode == Opcodes.GETFIELD) {
                            this.mv.visitMethodInsn(
                                    Opcodes.INVOKEVIRTUAL, 
                                    metadataProperty.getDeclaringClass().getName().replace('.', '/'), 
                                    metadataProperty.getGetterName(), 
                                    "()" + metadataProperty.getTypeDescriptor(),
                                    false);
                        } else {
                            this.mv.visitMethodInsn(
                                    Opcodes.INVOKEVIRTUAL, 
                                    metadataProperty.getDeclaringClass().getName().replace('.', '/'), 
                                    metadataProperty.getSetterName(false), 
                                    '(' + metadataProperty.getTypeDescriptor() + ")V",
                                    false);
                        }
                        return;
                    }
                }
                super.visitFieldInsn(opcode, owner, name, desc);
            }
        }
        
        private class ClinitAdapter extends NonPropertyMethodAdapter {

            public ClinitAdapter(MethodVisitor mv) {
                super(mv);
            }

            @Override
            public void visitCode() {
                super.visitCode();
                OwnerAdapter.this.visitInitObjectModelFactoryInsns(this.mv);
            }
        }
        
        private class InitAdapter extends NonPropertyMethodAdapter {
            
            private boolean objectModelCreated;
            
            public InitAdapter(MethodVisitor mv) {
                super(mv);
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                if (!this.objectModelCreated && !itf && opcode == Opcodes.INVOKESPECIAL && name.equals("<init>")) {
                    OwnerAdapter that = OwnerAdapter.this;
                    if (that.superName.equals(owner)) {
                        that.visitInitObjectModelInsns(mv);
                        this.objectModelCreated = true;
                    }
                    if (that.name.equals(owner)) {
                        this.objectModelCreated = true;
                    }
                }
            }
        }
        
        private abstract class PropertyAdapter extends MethodVisitor {
            
            protected MetadataProperty metadataProperty;
            
            private boolean firstLinedVisted;

            protected PropertyAdapter(MethodVisitor mv, MetadataProperty metadataProperty) {
                super(Opcodes.ASM5, mv);
                this.metadataProperty = metadataProperty;
            }

            @Override
            public void visitMaxs(int maxStack, int maxLocals) {
                super.visitMaxs(0, 0);
            }
            
            @Override
            public void visitLineNumber(int line, Label start) {
                if (!this.firstLinedVisted) {
                    super.visitLineNumber(line, start);
                    this.firstLinedVisted = true;
                }
            }

            @Override
            public void visitFrame(
                    int type, 
                    int nLocal, 
                    Object[] local,
                    int nStack, 
                    Object[] stack) {
            }

            @Override
            public void visitInsn(int opcode) {}

            @Override
            public void visitIntInsn(int opcode, int operand) {}

            @Override
            public void visitVarInsn(int opcode, int var) {}

            @Override
            public void visitTypeInsn(int opcode, String type) {}

            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String desc) {}

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String desc) {}
            
            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {}

            @Override
            public void visitJumpInsn(int opcode, Label label) {}

            @Override
            public void visitLabel(Label label) {}

            @Override
            public void visitLdcInsn(Object cst) {}

            @Override
            public void visitIincInsn(int var, int increment) {}

            @Override
            public void visitTableSwitchInsn(int min, int max, Label dflt, Label ... labels) {}

            @Override
            public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {}

            @Override
            public void visitMultiANewArrayInsn(String desc, int dims) {}

            @Override
            public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {}

            @Override
            public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {}
        }
        
        private class GetterAdapter extends PropertyAdapter {
            
            protected GetterAdapter(MethodVisitor mv, MetadataProperty metadataProperty) {
                super(mv, metadataProperty);
                if (metadataProperty.getAccessType() == AccessType.FIELD) {
                    this.visitAnnotations(metadataProperty.getVisibleAnnotations(), true);
                    this.visitAnnotations(metadataProperty.getInvisibleAnnotations(), false);
                }
            }

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if (desc.equals(ASM.getDescriptor(Access.class))) {
                    return null;
                }
                return super.visitAnnotation(desc, visible);
            }

            @Override
            public void visitCode() {
                super.visitCode();
                OwnerAdapter.this.visitGetterInsns(this.mv, this.metadataProperty);
            }
            
            private void visitAnnotations(List<MetadataAnnotation> modelAnnotations, boolean visible) {
                for (MetadataAnnotation modelAnnotation : modelAnnotations) {
                    this.visitAnnotation(modelAnnotation, visible);
                }
            }
            
            private void visitAnnotation(MetadataAnnotation modelAnnotation, boolean visible) {
                AnnotationVisitor av = this.mv.visitAnnotation(modelAnnotation.getDescriptor(), visible);
                this.visitAnnotationProperties(av, modelAnnotation);
                av.visitEnd();
            }
            
            private void visitAnnotationProperties(AnnotationVisitor av, MetadataAnnotation modelAnnotation) {
                for (Entry<String, MetadataAnnotation.Value> entry : modelAnnotation.getValues().entrySet()) {
                    String name = entry.getKey();
                    MetadataAnnotation.Value value = entry.getValue();
                    this.visitAnnotationProperty(av, name, value);
                }
            }
            
            private void visitAnnotationProperty(AnnotationVisitor av, String name, MetadataAnnotation.Value value) {
                if (value instanceof MetadataAnnotation.ArrayValue) {
                    MetadataAnnotation.ArrayValue arrayValue = (MetadataAnnotation.ArrayValue)value;
                    AnnotationVisitor arv = av.visitArray(name);
                    for (MetadataAnnotation.Value subValue : arrayValue.get()) {
                        this.visitAnnotationProperty(arv, null, subValue);
                    }
                    arv.visitEnd();
                } else if (value instanceof MetadataAnnotation.AnnotationValue) {
                    MetadataAnnotation.AnnotationValue annotationValue = (MetadataAnnotation.AnnotationValue)value;
                    AnnotationVisitor sav = av.visitAnnotation(name, annotationValue.get().getDescriptor());
                    this.visitAnnotationProperties(sav, annotationValue.get());
                    sav.visitEnd();
                } else if (value instanceof MetadataAnnotation.EnumValue) {
                    MetadataAnnotation.EnumValue enumValue = (MetadataAnnotation.EnumValue)value;
                    av.visitEnum(name, enumValue.getDescriptor(), enumValue.get());
                } else {
                    MetadataAnnotation.SimpleValue simpleValue = (MetadataAnnotation.SimpleValue)value;
                    av.visit(name, simpleValue.get());
                }
            }
        }
        
        private class SetterAdapter extends PropertyAdapter {
            
            protected SetterAdapter(MethodVisitor mv, MetadataProperty metadataProperty) {
                super(mv, metadataProperty);
            }

            @Override
            public void visitCode() {
                super.visitCode();
                OwnerAdapter.this.visitSetterInsns(this.mv, this.metadataProperty);
            }
        }
    }
    
    private interface Resource {

        String canNotReadBytecode(String filePath);

        String instrumentedEmbeddableMustNotDeclareEquals(
                String className,
                Class<JPAObjectModelInstrument> jpaObjectModelInstrumentTypeConstant, 
                Class<Embeddable> embeddableTypeConstant);

        String instrumentedEmbeddableMustNotDeclareHashCode(
                String className,
                Class<JPAObjectModelInstrument> jpaObjectModelInstrumentTypeConstant, 
                Class<Embeddable> embeddableTypeConstant);

        String fieldOfInstrumentedClassMustBePrivate(
                String className, 
                Class<JPAObjectModelInstrument> jpaObjectModelInstrumentTypeConstant, 
                String fieldName);

        String isNotJPAObjectModelInstrument(
                String className, 
                Class<JPAObjectModelInstrument> jpaObjectModelInstrumentTypeConstant);

        String canNotFindMetdataClass(String className);

        String metdataClassIsNotReady(Class<? extends OwnerAdapter> thisOAType);
    }
}
