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
package org.babyfish.hibernate.model.spi;

import java.io.ObjectStreamException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import org.babyfish.collection.FrozenContext;
import org.babyfish.collection.HashMap;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.hibernate.association.EntityCollection;
import org.babyfish.hibernate.association.EntityIndexedReference;
import org.babyfish.hibernate.association.EntityKeyedReference;
import org.babyfish.hibernate.association.EntityList;
import org.babyfish.hibernate.association.EntityNavigableMap;
import org.babyfish.hibernate.association.EntityNavigableSet;
import org.babyfish.hibernate.association.EntityOrderedMap;
import org.babyfish.hibernate.association.EntityOrderedSet;
import org.babyfish.hibernate.association.EntityReference;
import org.babyfish.hibernate.model.metadata.HibernateMetadatas;
import org.babyfish.hibernate.model.metadata.HibernateObjectModelMetadata;
import org.babyfish.hibernate.proxy.FrozenLazyInitializer;
import org.babyfish.hibernate.proxy.FrozenLazyInitializerImpl;
import org.babyfish.lang.Action;
import org.babyfish.lang.reflect.asm.ASM;
import org.babyfish.lang.reflect.asm.XMethodVisitor;
import org.babyfish.model.ObjectModel;
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.event.ScalarListener;
import org.babyfish.model.metadata.AssociationProperty;
import org.babyfish.model.metadata.ObjectModelMetadata;
import org.babyfish.model.metadata.ObjectModelMode;
import org.babyfish.model.spi.AppendingContext;
import org.babyfish.model.spi.ObjectModelAppender;
import org.babyfish.model.spi.ObjectModelFactoryProvider;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.Label;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.org.objectweb.asm.Type;
import org.babyfish.persistence.model.metadata.JPAAssociationProperty;
import org.babyfish.persistence.model.metadata.JPAScalarProperty;
import org.babyfish.persistence.model.spi.JPAObjectModelFactoryProvider;
import org.babyfish.reference.IndexedReference;
import org.babyfish.reference.KeyedReference;
import org.babyfish.reference.Reference;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

/**
 * @author Tao Chen
 */
public class HibernateObjectModelFactoryProvider extends JPAObjectModelFactoryProvider {

    private static final Map<Class<?>, Class<?>> ASSOCIATED_END_RESOLVER_MAP;
    
    private static final String HIBERNATE_PROXY_OWNER = "{owner:hibernateProxy}";
    
    protected static final String HIBERNATE_PROXY_INTERNAL_NAME = 
        HibernateProxy.class.getName().replace('.', '/');
    
    protected static final String HIBERNATE_PROXY_DESC = 
        'L' + HIBERNATE_PROXY_INTERNAL_NAME + ';';
    
    protected HibernateObjectModelFactoryProvider() {
        
    }
    
    @Override
    public MetadataFactory createMetadataFactory() {
        return new MetadataFactory() {
            
            @Override
            public HibernateObjectModelMetadata onGetMetadata(Class<?> ownerClass) {
                return HibernateMetadatas.of(ownerClass);
            }
            
            @Override
            public void onGenerateGetMetadata(MethodVisitor mv) {
                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC, 
                        HibernateMetadatas.class.getName().replace('.', '/'), 
                        "of", 
                        "(Ljava/lang/Class;)" + ASM.getDescriptor(HibernateObjectModelMetadata.class),
                        false);
            }
        };
    }

    @Override
    protected FactoryImplGenerator createFactoryImplGenerator(ObjectModelMetadata objectModelMetadata) {
        return this.new FactoryImplGenerator(objectModelMetadata);
    }

    @Override
    protected AssociatedEndpointImplGenerator createAssociatiedEndpointImplGenerator(
            AssociationProperty associationProperty) {
        return new AssociatedEndpointImplGenerator(associationProperty);
    }

    protected class FactoryImplGenerator extends JPAObjectModelFactoryProvider.FactoryImplGenerator {

        protected FactoryImplGenerator(ObjectModelMetadata objectModelMetadata) {
            super(objectModelMetadata);
        }
    
        @Override
        protected void generateCreateInsns(MethodVisitor mv) {
            if (this.getObjectModelMetadata().getMode() == ObjectModelMode.REFERENCE) {
                Label defaultImplLabel = new Label();
                Class<?> objectModelProxyClass = 
                    HibernateObjectModelFactoryProvider.this.generateObjectModelProxy(this.getObjectModelMetadata().getObjectModelClass());
                
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitTypeInsn(Opcodes.INSTANCEOF, HIBERNATE_PROXY_INTERNAL_NAME);
                mv.visitJumpInsn(Opcodes.IFEQ, defaultImplLabel);
                
                mv.visitTypeInsn(Opcodes.NEW, objectModelProxyClass.getName().replace('.', '/'));
                mv.visitInsn(Opcodes.DUP);
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitTypeInsn(Opcodes.CHECKCAST, HIBERNATE_PROXY_INTERNAL_NAME);
                
                mv.visitMethodInsn(
                        Opcodes.INVOKESPECIAL, 
                        objectModelProxyClass.getName().replace('.', '/'), 
                        "<init>",
                        '(' +
                        ASM.getDescriptor(ObjectModelFactory.class) +
                        HIBERNATE_PROXY_DESC +
                        ")V",
                        false);
                mv.visitInsn(Opcodes.ARETURN);
                
                mv.visitLabel(defaultImplLabel);
            }
            super.generateCreateInsns(mv);
        }
        
        @Override
        protected void generateGetInsns(MethodVisitor mv) {
            Label defaultImplLabel = new Label();
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitTypeInsn(Opcodes.INSTANCEOF, HIBERNATE_PROXY_INTERNAL_NAME);
            mv.visitJumpInsn(Opcodes.IFEQ, defaultImplLabel);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitTypeInsn(Opcodes.CHECKCAST, HIBERNATE_PROXY_INTERNAL_NAME);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASM.getInternalName(FrozenLazyInitializerImpl.class), 
                    "getFrozenLazyInitializer", 
                    '(' + 
                    ASM.getDescriptor(HibernateProxy.class) +
                    ')' + 
                    ASM.getDescriptor(FrozenLazyInitializer.class),
                    false);
            mv.visitInsn(Opcodes.POP);
            mv.visitLabel(defaultImplLabel);
            super.generateGetInsns(mv);
        }
    }

    protected class AssociatedEndpointImplGenerator extends JPAObjectModelFactoryProvider.AssociatedEndpointImplGenerator {
    
        AssociatedEndpointImplGenerator(AssociationProperty associationProperty) {
            super(associationProperty);
        }
    
        @Override
        protected Class<?> determineSuperClass() {
            return ASSOCIATED_END_RESOLVER_MAP.get(
                    this.getAssociationProperty().getStandardReturnClass()
            );
        }
    
        @Override
        protected void generateAdditionalMembers(ClassVisitor cv) {
            super.generateAdditionalMembers(cv);
            XMethodVisitor mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PUBLIC, 
                    "isInverse", 
                    "()Z", 
                    null,
                    null);
            mv.visitCode();
            int ldc = 
                    ((JPAAssociationProperty)this.getAssociationProperty()).isInverse() ? 
                    Opcodes.ICONST_1 : 
                    Opcodes.ICONST_0;
            mv.visitInsn(ldc);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }

    private Class<?> generateObjectModelProxy(final Class<?> objectModelClass) {
        final String objectModelProxyClassName = 
            objectModelClass.getName() +
            "{hibernate_proxy=>" +
            this.getClass().getName().replace('.', ':') +
            '}';
        Action<ClassVisitor> cvAction = new Action<ClassVisitor>() {
            @Override
            public void run(ClassVisitor cv) {
                HibernateObjectModelFactoryProvider.this.generateObjectModelProxy(
                        objectModelClass, 
                        objectModelProxyClassName, 
                        cv);
            }
        };
        return ASM.loadDynamicClass(
                objectModelClass.getClassLoader(), 
                objectModelProxyClassName, 
                objectModelClass.getProtectionDomain(), 
                cvAction);
    }

    private void generateObjectModelProxy(
            Class<?> objectModelClass, 
            String objectModelProxyClassName, 
            ClassVisitor cv) {
        
        Class<?> ownerClass = 
            objectModelClass.getDeclaringClass();
        HibernateObjectModelMetadata hibernateObjectModelMetadata =
            this.getMetadataFactory().getMetadata(ownerClass);
        
        String objectModelInternalName = 
            objectModelClass
            .getName()
            .replace('.', '/');
        String objectModelProxyInternalName =
            objectModelProxyClassName.replace('.', '/');
        cv.visit(
                Opcodes.V1_7, 
                Opcodes.ACC_PUBLIC, 
                objectModelProxyInternalName,
                null, 
                "java/lang/Object", 
                new String[] { 
                        objectModelInternalName, 
                        ASM.getInternalName(ObjectModel.class),
                        ASM.getInternalName(ObjectModelAppender.class)
                }
        );
        
        cv.visitField(
                Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, 
                OBJECT_MODEL_IMPL_FACTORY, 
                ASM.getDescriptor(ObjectModelFactory.class), 
                null, 
                null
        )
        .visitEnd();
        
        cv.visitField(
                Opcodes.ACC_PRIVATE, 
                HIBERNATE_PROXY_OWNER, 
                HIBERNATE_PROXY_DESC, 
                null, 
                null
        )
        .visitEnd();
        
        XMethodVisitor mv;
        
        mv = ASM.visitMethod(
                cv,
                Opcodes.ACC_PUBLIC, 
                "<init>", 
                '(' +
                ASM.getDescriptor(ObjectModelFactory.class) +
                HIBERNATE_PROXY_DESC +
                ")V", 
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
        
        mv.visitLdcInsn(Type.getType(objectModelClass));
        mv.visitMethodInsn(
                Opcodes.INVOKESTATIC, 
                ASM.getInternalName(ObjectModelFactoryFactory.class), 
                "factoryOf", 
                "(Ljava/lang/Class;)" + ASM.getDescriptor(ObjectModelFactory.class),
                false);
        mv.visitFieldInsn(
                Opcodes.PUTSTATIC, 
                objectModelProxyInternalName, 
                OBJECT_MODEL_IMPL_FACTORY, 
                ASM.getDescriptor(ObjectModelFactory.class));
        
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 2);
        mv.visitFieldInsn(
                Opcodes.PUTFIELD, 
                objectModelProxyInternalName, 
                HIBERNATE_PROXY_OWNER, 
                HIBERNATE_PROXY_DESC);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        
        int gernatedMask = 0;
        for (Method method : objectModelClass.getMethods()) {
            gernatedMask |= this.generateProxyMethodForReferenceMode(
                    cv, 
                    method, 
                    objectModelProxyInternalName, 
                    objectModelClass, 
                    hibernateObjectModelMetadata);
        }
        if (!ObjectModel.class.isAssignableFrom(objectModelClass)) {
            for (Method method : ObjectModel.class.getMethods()) {
                gernatedMask |= this.generateProxyMethodForReferenceMode(
                        cv, 
                        method, 
                        objectModelProxyInternalName, 
                        objectModelClass, 
                        hibernateObjectModelMetadata);
            }
        }
        if (gernatedMask != (1 << 8) - (hibernateObjectModelMetadata.getDeclaredEntityIdProperty() == null ? 4 : 1)) {
            throw new AssertionError();
        }
        
        mv = ASM.visitMethod(
                cv, 
                Opcodes.ACC_PROTECTED, 
                "writeReplace", 
                "()Ljava/lang/Object;", 
                null,
                new String[] { ASM.getInternalName(ObjectStreamException.class) });
        mv.visitCode();
        mv.visitTypeInsn(Opcodes.NEW, ASM.getInternalName(HibernateObjectModelProxyWritingReplacment.class));
        mv.visitInsn(Opcodes.DUP);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(
                Opcodes.INVOKESPECIAL, 
                ASM.getInternalName(HibernateObjectModelProxyWritingReplacment.class), 
                "<init>", 
                '(' +
                ASM.getDescriptor(ObjectModel.class) +
                ")V",
                false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        
        this.generateAppendTo(cv, objectModelProxyClassName);
        this.generateToString(cv);
        
        cv.visitEnd();
    }
    
    private int generateProxyMethodForReferenceMode(
            ClassVisitor cv, 
            Method method,
            final String objectModelProxyClassName,
            Class<?> objectModelClass,
            HibernateObjectModelMetadata jpaObjectModelMetadata) {
        int retval;
        final JPAScalarProperty entityIdProperty = jpaObjectModelMetadata.getEntityIdProperty();
        final String desc = ASM.getDescriptor(method);
        XMethodVisitor mv = ASM.visitMethod(
                cv,
                Opcodes.ACC_PUBLIC, 
                method.getName(), 
                desc, 
                null,
                null);
        mv.visitCode();
        if (method.getName().equals(entityIdProperty.getGetterName()) &&
                method.getParameterTypes().length == 0 &&
                method.getReturnType() == entityIdProperty.getReturnClass()) {
            this.generateGetEntityId(
                    mv, 
                    method, 
                    objectModelProxyClassName, 
                    objectModelClass, 
                    jpaObjectModelMetadata);
            retval = 1 << 0;
        } else if (method.getName().equals(entityIdProperty.getSetterName()) &&
                Arrays.equals(method.getParameterTypes(), new Class[] { entityIdProperty.getReturnClass() }) &&
                method.getReturnType() == void.class) {
            this.generateSetEntityId(
                    mv, 
                    method, 
                    objectModelProxyClassName, 
                    objectModelClass, 
                    jpaObjectModelMetadata);
            retval = 1 << 1;
        } else if (method.getName().equals("getScalar") && 
                Arrays.equals(method.getParameterTypes(), new Class[] { int.class }) &&
                method.getReturnType() == Object.class) {
            Label invokeImplemenationLabel = new Label();
            mv.visitVarInsn(Opcodes.ILOAD, 1);
            mv.visitLdcInsn(entityIdProperty.getId());
            mv.visitJumpInsn(Opcodes.IF_ICMPNE, invokeImplemenationLabel);
            mv.visitBox(
                    entityIdProperty.getReturnClass(), 
                    new Action<XMethodVisitor>() {
                        @Override
                        public void run(XMethodVisitor mv) {
                            mv.visitVarInsn(Opcodes.ALOAD, 0);
                            mv.visitMethodInsn(
                                    Opcodes.INVOKEVIRTUAL, 
                                    objectModelProxyClassName.replace('.', '/'), 
                                    entityIdProperty.getGetterName(), 
                                    "()" + ASM.getDescriptor(entityIdProperty.getReturnClass()),
                                    false);
                        }
                    });
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitLabel(invokeImplemenationLabel);
            this.generateDefaultProxyMethodCode(
                    mv, 
                    method, 
                    objectModelProxyClassName, 
                    objectModelClass, 
                    jpaObjectModelMetadata,
                    true);
            retval = 1 << 2;
        } else if (method.getName().equals("setScalar") &&
                Arrays.equals(method.getParameterTypes(), new Class[] { int.class, Object.class }) &&
                method.getReturnType() == void.class) {
            Label invokeImplemenationLabel = new Label();
            mv.visitVarInsn(Opcodes.ILOAD, 1);
            mv.visitLdcInsn(entityIdProperty.getId());
            mv.visitJumpInsn(Opcodes.IF_ICMPNE, invokeImplemenationLabel);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            if (entityIdProperty.getReturnClass().isPrimitive()) {
                mv.visitUnbox(
                        entityIdProperty.getReturnClass(), 
                        XMethodVisitor.JVM_PRIMTIVIE_DEFAULT_VALUE);
            } else {
                mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(entityIdProperty.getReturnClass()));
            }
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    objectModelProxyClassName.replace('.', '/'), 
                    entityIdProperty.getSetterName(), 
                    '(' + ASM.getDescriptor(entityIdProperty.getReturnClass()) + ")V",
                    false);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitLabel(invokeImplemenationLabel);
            this.generateDefaultProxyMethodCode(
                    mv, 
                    method, 
                    objectModelProxyClassName, 
                    objectModelClass, 
                    jpaObjectModelMetadata,
                    true);
            retval = 1 << 3;
        } else if (method.getName().equals("freezeScalar") &&
                Arrays.equals(method.getParameterTypes(), new Class[] { int.class, FrozenContext.class }) &&
                method.getReturnType() == void.class) {
            this.generateFreezeSclarAndUnfreezeScalarCode(
                    mv, 
                    method, 
                    objectModelProxyClassName, 
                    objectModelClass, 
                    jpaObjectModelMetadata, 
                    true);
            retval = 1 << 4;
        } else if (method.getName().equals("unfreezeScalar") &&
                Arrays.equals(method.getParameterTypes(), new Class[] { int.class, FrozenContext.class }) &&
                method.getReturnType() == void.class) {
            this.generateFreezeSclarAndUnfreezeScalarCode(
                    mv, 
                    method, 
                    objectModelProxyClassName, 
                    objectModelClass, 
                    jpaObjectModelMetadata, 
                    false);
            retval = 1 << 5;
        } else if (method.getName().equals("addScalarListener") &&
                Arrays.equals(method.getParameterTypes(), new Class[] { ScalarListener.class }) &&
                method.getReturnType() == void.class) {
            this.generateAddScalarListenerAndRemoveScalarListenerCode(
                    mv, 
                    method, 
                    objectModelProxyClassName, 
                    objectModelClass, 
                    jpaObjectModelMetadata, 
                    true);
            retval = 1 << 6;
            
        } else if (method.getName().equals("removeScalarListener") &&
                Arrays.equals(method.getParameterTypes(), new Class[] { ScalarListener.class }) &&
                method.getReturnType() == void.class) {
            this.generateAddScalarListenerAndRemoveScalarListenerCode(
                    mv, 
                    method, 
                    objectModelProxyClassName, 
                    objectModelClass, 
                    jpaObjectModelMetadata, 
                    false);
            retval = 1 << 7;
            
        } else {
            this.generateDefaultProxyMethodCode(
                    mv, 
                    method, 
                    objectModelProxyClassName, 
                    objectModelClass, 
                    jpaObjectModelMetadata,
                    true);
            retval = 0;
        }
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        return retval;
    }
    
    //TODO: refactor it into the nested class
    private void generateDefaultProxyMethodCode(
            XMethodVisitor mv, 
            Method method,
            final String objectModelProxyClassName,
            Class<?> objectModelClass,
            HibernateObjectModelMetadata jpaObjectModelMetadata,
            boolean generateReturn) {
        final String desc = ASM.getDescriptor(method);
        visitGetObjectModel(mv, jpaObjectModelMetadata, new Action<MethodVisitor>() {
            @Override
            public void run(MethodVisitor mv) {
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        objectModelProxyClassName.replace('.', '/'), 
                        HIBERNATE_PROXY_OWNER, 
                        HIBERNATE_PROXY_DESC);
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        HIBERNATE_PROXY_INTERNAL_NAME, 
                        "getHibernateLazyInitializer", 
                        "()" + ASM.getDescriptor(LazyInitializer.class),
                        true);
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        LazyInitializer.class.getName().replace('.', '/'), 
                        "getImplementation", 
                        "()Ljava/lang/Object;",
                        true);
            }
        });
        String interfaceInternalName = 
                method.getDeclaringClass() == ObjectModel.class ?
                ASM.getInternalName(ObjectModel.class) :
                ASM.getInternalName(objectModelClass);
        mv.visitTypeInsn(Opcodes.CHECKCAST, interfaceInternalName);
        int slot = 1;
        for (Class<?> parameterClass : method.getParameterTypes()) {
            mv.visitVarInsn(ASM.getLoadCode(parameterClass), slot);
            slot += ASM.getSlotCount(parameterClass);
        }
        mv.visitMethodInsn(
                Opcodes.INVOKEINTERFACE, 
                interfaceInternalName, 
                method.getName(), 
                desc,
                true);
        if (generateReturn) {
            mv.visitInsn(ASM.getReturnCode(method.getReturnType()));
        }
    }
    
    private void generateGetEntityId(
            XMethodVisitor mv, 
            Method method,
            String objectModelProxyClassName,
            Class<?> objectModelClass,
            HibernateObjectModelMetadata jpaObjectModelMetadata) {
        final JPAScalarProperty entityIdProperty = jpaObjectModelMetadata.getDeclaredEntityIdProperty();
        
        this.generateLazyInitializer(mv, objectModelProxyClassName);
        mv.visitMethodInsn(
                Opcodes.INVOKEINTERFACE, 
                ASM.getInternalName(LazyInitializer.class), 
                "getIdentifier", 
                "()Ljava/io/Serializable;",
                true);
        if (entityIdProperty.getReturnClass().isPrimitive()) {
            mv.visitUnbox(entityIdProperty.getReturnClass(), XMethodVisitor.JVM_PRIMTIVIE_DEFAULT_VALUE);
        } else {
            mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(entityIdProperty.getReturnClass()));
        }
        mv.visitInsn(ASM.getReturnCode(entityIdProperty.getReturnClass()));
    }
    
    private void generateSetEntityId(
            XMethodVisitor mv, 
            Method method,
            String objectModelProxyClassName,
            Class<?> objectModelClass,
            HibernateObjectModelMetadata jpaObjectModelMetadata) {
        
        final JPAScalarProperty entityIdProperty = jpaObjectModelMetadata.getEntityIdProperty();
        this.generateLazyInitializer(mv, objectModelProxyClassName);
        mv.visitBox(
                entityIdProperty.getReturnClass(), 
                new Action<XMethodVisitor>() {
                    @Override
                    public void run(XMethodVisitor mv) {
                        mv.visitVarInsn(ASM.getLoadCode(entityIdProperty.getReturnClass()), 1);
                    }
                });
        mv.visitMethodInsn(
                Opcodes.INVOKEINTERFACE, 
                ASM.getInternalName(LazyInitializer.class), 
                "setIdentifier", 
                "(Ljava/io/Serializable;)V",
                true);
        mv.visitInsn(Opcodes.RETURN);
    }
    
    private void generateFreezeSclarAndUnfreezeScalarCode(
            XMethodVisitor mv, 
            Method method,
            String objectModelProxyClassName,
            Class<?> objectModelClass,
            HibernateObjectModelMetadata jpaObjectModelMetadata,
            boolean freeze) {
        final JPAScalarProperty entityIdProperty = jpaObjectModelMetadata.getEntityIdProperty();
        Label invokeImplemenationLabel = new Label();
        mv.visitVarInsn(Opcodes.ILOAD, 1);
        mv.visitLdcInsn(entityIdProperty.getId());
        mv.visitJumpInsn(Opcodes.IF_ICMPNE, invokeImplemenationLabel);
        this.generateLazyInitializer(mv, objectModelProxyClassName);
        mv.visitVarInsn(Opcodes.ALOAD, 2);
        mv.visitMethodInsn(
                Opcodes.INVOKEINTERFACE, 
                ASM.getInternalName(FrozenLazyInitializer.class),
                freeze ? "freezeIdentifier" : "unfreezeIdentifier", 
                '(' +
                ASM.getDescriptor(FrozenContext.class) +
                ")V",
                true);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitLabel(invokeImplemenationLabel);
        this.generateDefaultProxyMethodCode(
                mv, 
                method, 
                objectModelProxyClassName, 
                objectModelClass, 
                jpaObjectModelMetadata,
                true);
    }
    
    private void generateAddScalarListenerAndRemoveScalarListenerCode(
            XMethodVisitor mv, 
            Method method,
            String objectModelProxyClassName,
            Class<?> objectModelClass,
            HibernateObjectModelMetadata jpaObjectModelMetadata,
            boolean add) {
        this.generateLazyInitializer(mv, objectModelProxyClassName);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(
                Opcodes.INVOKEINTERFACE, 
                ASM.getInternalName(FrozenLazyInitializer.class), 
                add ? "addScalarListener" : "removeScalarListener", 
                '(' + ASM.getDescriptor(ScalarListener.class) + ")V",
                true);
        mv.visitInsn(Opcodes.RETURN);
    }
    
    private void generateLazyInitializer(
            XMethodVisitor mv,
            String objectModelProxyClassName) {
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(
                Opcodes.GETFIELD, 
                objectModelProxyClassName.replace('.', '/'), 
                HIBERNATE_PROXY_OWNER, 
                HIBERNATE_PROXY_DESC);
        mv.visitMethodInsn(
                Opcodes.INVOKESTATIC, 
                ASM.getInternalName(FrozenLazyInitializerImpl.class), 
                "getFrozenLazyInitializer", 
                '(' +
                ASM.getDescriptor(HibernateProxy.class) +
                ')' + 
                ASM.getDescriptor(FrozenLazyInitializer.class),
                false);
    }

    private void generateAppendTo(ClassVisitor cv, final String objectModelProxyClassName) {
        XMethodVisitor mv = ASM.visitMethod(
                cv, 
                Opcodes.ACC_PUBLIC, 
                "appendTo",
                '(' + ASM.getDescriptor(AppendingContext.class) +")V", 
                null,
                null);
        mv.visitCode();
        
        final int lazyInitializerIndex = mv.aSlot("lazyInitializer");
        final Label needAppendLabel = new Label();
        final Label isInitializedLabel = new Label();
        final Label afterAppendToLabel = new Label();
        
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL, 
                ASM.getInternalName(AppendingContext.class), 
                "enter", 
                '(' + ASM.getDescriptor(ObjectModel.class) + ")Z",
                false);
        mv.visitJumpInsn(Opcodes.IFNE, needAppendLabel);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitLabel(needAppendLabel);
        
        Action<MethodVisitor> tryAction = new Action<MethodVisitor>() {

            @Override
            public void run(MethodVisitor mv) {
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        objectModelProxyClassName.replace('.', '/'), 
                        HIBERNATE_PROXY_OWNER, 
                        HIBERNATE_PROXY_DESC);
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        HIBERNATE_PROXY_INTERNAL_NAME, 
                        "getHibernateLazyInitializer", 
                        "()" + ASM.getDescriptor(LazyInitializer.class),
                        true);
                mv.visitVarInsn(Opcodes.ASTORE, lazyInitializerIndex);
                
                mv.visitVarInsn(Opcodes.ALOAD, lazyInitializerIndex);
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        ASM.getInternalName(LazyInitializer.class), 
                        "isUninitialized", 
                        "()Z",
                        true);
                mv.visitJumpInsn(Opcodes.IFEQ, isInitializedLabel);
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitLdcInsn("$unloadedProxy()");
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        ASM.getInternalName(AppendingContext.class), 
                        "appendValue", 
                        "(Ljava/lang/String;)V",
                        false);
                mv.visitJumpInsn(Opcodes.GOTO, afterAppendToLabel);
                mv.visitLabel(isInitializedLabel);
                
                mv.visitFieldInsn(
                        Opcodes.GETSTATIC, 
                        objectModelProxyClassName.replace('.', '/'), 
                        OBJECT_MODEL_IMPL_FACTORY, 
                        ASM.getDescriptor(ObjectModelFactory.class));
                mv.visitVarInsn(Opcodes.ALOAD, lazyInitializerIndex);
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        ASM.getInternalName(LazyInitializer.class), 
                        "getImplementation", 
                        "()Ljava/lang/Object;",
                        true);
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        ASM.getInternalName(ObjectModelFactory.class), 
                        "get", 
                        "(Ljava/lang/Object;)Ljava/lang/Object;",
                        true);
                mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(ObjectModelAppender.class));
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        ASM.getInternalName(ObjectModelAppender.class), 
                        "appendTo", 
                        '(' + ASM.getDescriptor(AppendingContext.class) + ")V",
                        true);
                mv.visitLabel(afterAppendToLabel);
            }
        };
        Action<MethodVisitor> finallyAction = new Action<MethodVisitor>() {
            @Override
            public void run(MethodVisitor mv) {
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        ASM.getInternalName(AppendingContext.class), 
                        "exit", 
                        '(' + ASM.getDescriptor(ObjectModel.class) + ")V",
                        false);
            }
        };
        mv.visitTryFinally(tryAction, finallyAction);
        
        mv.visitInsn(Opcodes.RETURN);
        
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private void generateToString(ClassVisitor cv) {
        MethodVisitor mv = cv.visitMethod(
                Opcodes.ACC_PUBLIC, 
                "toString", 
                "()Ljava/lang/String;", 
                null, 
                null);
        mv.visitCode();
        
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(
                Opcodes.INVOKESTATIC, 
                ASM.getInternalName(AppendingContext.class), 
                "toString",
                '(' + ASM.getDescriptor(ObjectModel.class) + ")Ljava/lang/String;",
                false);
        mv.visitInsn(Opcodes.ARETURN);
        
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    public abstract class MetadataFactory extends ObjectModelFactoryProvider.MetadataFactory {
        
        @Override
        public abstract HibernateObjectModelMetadata onGetMetadata(Class<?> rawClass);
        
    }
    
    static {
        Map<Class<?>, Class<?>> map = new HashMap<Class<?>, Class<?>>();
        
        map.put(NavigableMap.class, EntityNavigableMap.class);
        map.put(SortedMap.class, EntityNavigableMap.class);
        map.put(XOrderedMap.class, EntityOrderedMap.class);
        map.put(Map.class, EntityOrderedMap.class); 
        
        map.put(NavigableSet.class, EntityNavigableSet.class);
        map.put(SortedSet.class, EntityNavigableSet.class);
        map.put(XOrderedSet.class, EntityOrderedSet.class);
        map.put(Set.class, EntityOrderedSet.class);
        
        map.put(List.class, EntityList.class);
        
        map.put(Collection.class, EntityCollection.class);
        
        map.put(KeyedReference.class, EntityKeyedReference.class);
        map.put(IndexedReference.class, EntityIndexedReference.class); 
        map.put(Reference.class, EntityReference.class);
        
        ASSOCIATED_END_RESOLVER_MAP = MACollections.unmodifiable(map);
    }
    
}
