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
package org.babyfish.immutable;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

import org.babyfish.collection.HashMap;
import org.babyfish.collection.HashSet;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.lang.Action;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.IllegalOperationException;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.lang.Singleton;
import org.babyfish.lang.reflect.ClassInfo;
import org.babyfish.lang.reflect.GenericTypes;
import org.babyfish.lang.reflect.MethodDescriptor;
import org.babyfish.lang.reflect.MethodImplementation;
import org.babyfish.lang.reflect.MethodInfo;
import org.babyfish.lang.reflect.PropertyInfo;
import org.babyfish.lang.reflect.asm.ASM;
import org.babyfish.lang.reflect.asm.XMethodVisitor;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.Label;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.util.LazyResource;

/**
 * @author Tao Chen
 */
public class ImmutableObjects {
    
    private static final LazyResource<Resource> LAZY_RESOURCE = LazyResource.of(Resource.class);
    
    private static final String NAME_POSTFIX = "92B8C17E_BF4E_4135_B596_5A76E0FEBF4E";
    
    private static final String SINGLETON_POSTFIX = "__SIGNLETON_92B8C17E_BF4E_4135_B596_5A76E0FEBF4E";
    
    private static final Pattern ORDER_SPLIT_PARTTERN = Pattern.compile("\\s*,\\s*");
    
    private static final Map<Class<? extends ImmutableObjects>, ImmutableObjects> INSTANCES =
        new HashMap<Class<? extends ImmutableObjects>, ImmutableObjects>();
    
    private final ReadWriteLock cacheLock = new ReentrantReadWriteLock();
    
    private final Map<Class<?>, Object> cache = new WeakHashMap<>();
    
    protected ImmutableObjects() throws IllegalOperationException {
        Map<Class<? extends ImmutableObjects>, ImmutableObjects> instances = INSTANCES;
        Class<? extends ImmutableObjects> thisClass = this.getClass();
        synchronized (instances) {
            if (instances.containsKey(thisClass)) {
                throw new IllegalOperationException(
                        LAZY_RESOURCE.get().singleInstanceForEachDerivedType(
                                ImmutableObjects.class, 
                                this.getClass()
                        )
                );
            }
            instances.put(thisClass, this);
        }
    }
    
    public static boolean isAbstract(Class<?> interfaceType) {
        Arguments.mustNotBeNull("interfaceType", interfaceType);
        Arguments.mustBeInterface("interfaceType", interfaceType);
        return !interfaceType.isAnnotationPresent(Parameters.class);
    }
    
    public static boolean isSingleton(Class<?> interfaceType) {
        if (isAbstract(interfaceType)) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().immutableTypeMustNotBeAbstract(interfaceType, Parameters.class)
            );
        }
        Parameters parameters = interfaceType.getAnnotation(Parameters.class);
        if (!parameters.value().isEmpty()) {
            return false;
        }
        return isSingleton0(interfaceType);
    }
    
    private static boolean isSingleton0(Class<?> interfaceType) {
        if (interfaceType.isAnnotationPresent(Prototype.class)) {
            return false;
        }
        for (Class<?> superInterfaceType : interfaceType.getInterfaces()) {
            if (!isSingleton0(superInterfaceType)) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isCached(Class<?> interfaceType) {
        if (isAbstract(interfaceType)) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().immutableTypeMustNotBeAbstract(interfaceType, Parameters.class)
            );
        }
        Parameters parameters = interfaceType.getAnnotation(Parameters.class);
        if (!parameters.value().isEmpty()) {
            return false;
        }
        return isSingleton0(interfaceType);
    }
    
    protected void validateInterfaceType(Class<?> interfaceType) {
        
    }
    
    @SuppressWarnings("unchecked")
    protected final <F> F getFactory(Class<F> factoryType) {
        Lock lock;
        Object factory;
        (lock = this.cacheLock.readLock()).lock(); //1st locking
        try {
            factory = this.cache.get(factoryType); //1st reading
        } finally {
            lock.unlock();
        }
        if (factory == null) { //1st chekcking
            (lock = this.cacheLock.writeLock()).lock(); //2nd locking
            try {
                factory = this.cache.get(factoryType); //2nd reading
                if (factory == null) { //2nd checking
                    factory = this.createFactory(factoryType);
                    this.cache.put(factoryType, factory);
                }
            } finally {
                lock.unlock();
            }
        }
        return (F)factory;
    }
    
    private Object createFactory(Class<?> factoryType) {
        try {
            return this.getFactoryImplementation(factoryType).newInstance();
        } catch (InstantiationException e) {
            throw new AssertionError(); 
        } catch (IllegalAccessException e) {
            throw new AssertionError();
        }
    }
    
    private Class<?> getFactoryImplementation(final Class<?> factoryType) {
        final String className = getFactoryImplementationClassName(factoryType);
        Action<ClassVisitor> cvAction = new Action<ClassVisitor>() {
            @Override
            public void run(ClassVisitor cv) {
                Arguments.mustNotBeNull("factoryType", factoryType);
                Arguments.mustBeInterface("factoryType", factoryType);
                ImmutableObjects owner = ImmutableObjects.this;
                Method[] methods = factoryType.getMethods();
                int methodCount = methods.length;
                for (int i = 0; i < methodCount; i++) {
                    if (methods[i].getExceptionTypes().length != 0) {
                        throw new IllegalProgramException(LAZY_RESOURCE.get().mustNotThrowCheckedException(methods[i]));
                    }
                    Class<?> returnTypeOfI = methods[i].getReturnType();
                    if (!returnTypeOfI.isInterface()) {
                        throw new IllegalProgramException(LAZY_RESOURCE.get().returnTypeMustBeInterface(methods[i]));
                    }
                    for (int ii = i + 1; ii < methodCount; ii++) {
                        if (returnTypeOfI == methods[ii].getReturnType()) {
                            throw new IllegalProgramException(
                                    LAZY_RESOURCE.get().returnTypeMustBeNotSame(methods[i], methods[ii])
                            );
                        }
                    }
                }
                cv.visit(
                        Opcodes.V1_7, 
                        Opcodes.ACC_PUBLIC, 
                        className.replace('.', '/'), 
                        null, 
                        "java/lang/Object", 
                        new String[] { factoryType.getName().replace('.', '/') });
                generateFactoryConstructor(cv);
                Map<String, Class<?>> singletonFields = new HashMap<String, Class<?>>();
                for (Method method : methods) {
                    owner.generateFactoryCreate(cv, method, className, singletonFields);
                }
                if (!singletonFields.isEmpty()) {
                    owner.generateFactoryStaticBlock(cv, className, singletonFields);
                }
                cv.visitEnd();
            }
        };
        return ASM.loadDynamicClass(
                factoryType.getClassLoader(), 
                className, 
                factoryType.getProtectionDomain(),
                cvAction);
    }

    private Class<?> getObjectImplemntation(final Context context) {
        final Class<?> interfaceType = context.interfaceRawClass;
        Action<ClassVisitor> cvAction = new Action<ClassVisitor>() {
            @Override
            public void run(ClassVisitor cv) {
                if (!interfaceType.isInterface()) {
                    throw new IllegalProgramException(LAZY_RESOURCE.get().immutableTypeMustBeInterface(interfaceType));
                }
                if (isAbstract(interfaceType)) {
                    throw new IllegalArgumentException(LAZY_RESOURCE.get().immutableTypeMustNotBeAbstract(interfaceType, Parameters.class));
                }
                if (isSingleton(interfaceType)) {
                    Parameters parameters = interfaceType.getAnnotation(Parameters.class);
                    if (parameters != null && !parameters.value().isEmpty()) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().immutableTypeMustContainsNoParametersWhenItIsSingleton(
                                        interfaceType,
                                        Parameters.class,
                                        Singleton.class
                                )
                        );
                    }
                }
                ImmutableObjects owner = ImmutableObjects.this;
                owner.validateInterfaceType(interfaceType);
                cv.visit(
                        Opcodes.V1_7, 
                        Opcodes.ACC_PUBLIC, 
                        context.getClassInternalName(),
                        null, 
                        "java/lang/Object", 
                        Serializable.class.isAssignableFrom(interfaceType) ?
                                new String[] { interfaceType.getName().replace('.', '/') } :
                                new String[] { 
                                    interfaceType.getName().replace('.', '/'), 
                                    Serializable.class.getName().replace('.', '/') });
                Collection<MethodImplementation> implementations = 
                    ClassInfo.of(interfaceType).getMethodImplementationMap().values();
                for (MethodImplementation implementation : implementations) {
                    if (implementation.isBridge()) {
                        owner.generateBridge(cv, context, implementation);
                    }
                }
                for (MethodInfo methodInfo : context.getAutonomyMethods()) {
                    owner.generateAutonomyMethod(cv, context, methodInfo);
                }
                for (Entry<String, MethodInfo> entry : context.getProperties().entrySet()) {
                    String propertyName = entry.getKey();
                    Class<?> propertyType = entry.getValue().getReturnType();
                    String propertyDesc = ASM.getDescriptor(propertyType);
                    cv
                    .visitField(
                            Opcodes.ACC_PRIVATE, 
                            propertyName, 
                            propertyDesc, 
                            null, 
                            null)
                    .visitEnd();
                    MethodVisitor mv = cv.visitMethod(
                            Opcodes.ACC_PUBLIC, 
                            entry.getValue().getName(), 
                            "()" + propertyDesc, 
                            null,
                            null);
                    mv.visitCode();
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitFieldInsn(
                            Opcodes.GETFIELD, 
                            context.getClassInternalName(), 
                            propertyName, 
                            propertyDesc);
                    mv.visitInsn(ASM.getReturnCode(propertyType));
                    mv.visitMaxs(0, 0);
                    mv.visitEnd();
                }
                owner.generateObjectSerialVersionUID(cv, context);
                owner.generateObjectConstrucotor(cv, context);
                owner.generateObjectHashCode(cv, context);
                owner.generateObjectEquals(cv, context);
                owner.generateObjectToString(cv, context);
                cv.visitEnd();
            }
        };
        return ASM.loadDynamicClass(
                interfaceType.getClassLoader(), 
                context.getClassName(), 
                interfaceType.getProtectionDomain(),
                cvAction);
    }

    protected void generateAutonomyMethod(
            ClassVisitor cv, 
            Context context, 
            MethodInfo methodInfo) {
        throw new IllegalOperationException(LAZY_RESOURCE.get().mustOverrideGenerateAutomyMethod(this.getClass()));
    }
    
    protected void generateBridge(
            ClassVisitor cv, 
            Context context, 
            MethodImplementation implementation) {
        MethodDescriptor descriptor = implementation.getDescriptor();
        MethodDescriptor targetDescriptor = 
            implementation.getBridgeTargetImplementation().getDescriptor();
        List<Class<?>> parameterTypes = 
            descriptor.getParameterTypes();
        List<Class<?>> targetParameterTypes = 
            targetDescriptor.getParameterTypes();
        int access = Opcodes.ACC_BRIDGE;
        if (implementation.isPublic()) {
            access |= Opcodes.ACC_PUBLIC;
        }
        if (implementation.isProtected()) {
            access |= Opcodes.ACC_PROTECTED;
        }
        if (implementation.isSynthetic()) {
            access |= Opcodes.ACC_SYNTHETIC;
        }
        XMethodVisitor mv = ASM.visitMethod(
                cv,
                access, 
                descriptor.getName(), 
                descriptor.toByteCodeDescriptor(), 
                null,
                null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        for (int i = 0; i < parameterTypes.size(); i++) {
            Class<?> parameterType = (Class<?>)parameterTypes.get(i);
            Class<?> targetParameterType = (Class<?>)targetParameterTypes.get(i);
            mv.visitVarInsn(ASM.getLoadCode(parameterType), mv.paramSlot(i));
            if (parameterType != targetParameterType) {
                mv.visitTypeInsn(Opcodes.CHECKCAST, targetParameterType.getName().replace('.', '/'));
            }
        }
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL, 
                context.getClassInternalName(), 
                descriptor.getName(), 
                targetDescriptor.toByteCodeDescriptor(),
                false);
        mv.visitInsn(ASM.getReturnCode(descriptor.getReturnType()));
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private static void generateFactoryConstructor(ClassVisitor cv) {
        MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private void generateFactoryCreate(
            ClassVisitor cv, 
            Method method, 
            String className,
            Map<String, Class<?>> singletonFields) {
        Context context = new Context(method.getGenericReturnType());
        XOrderedMap<String, MethodInfo> properties = context.getProperties();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Type[] parameterGenericTypes = method.getGenericParameterTypes();
        int index = 0;
        if (properties.size() != parameterTypes.length) {
            throw new IllegalProgramException(
                    LAZY_RESOURCE.get().parameterCountOfFactoryMethodMustBe(method, properties.size()));
        }
        for (MethodInfo methodInfo : properties.values()) {
            if (!methodInfo.getResolvedGenericReturnType().equals(parameterGenericTypes[index])) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().parameterOfFactoryMethodMustBe(
                                method, 
                                index, 
                                methodInfo.getResolvedGenericReturnType()
                        )
                );
            }
            index++;
        }
        Class<?> interfaceType = method.getReturnType();
        boolean singleton = isSingleton(interfaceType);
        Class<?> implementationType = this.getObjectImplemntation(context);
        String implementationInternalName = implementationType.getName().replace('.', '/');
        StringBuilder builder = new StringBuilder();
        for (Class<?> type : parameterTypes) {
            builder.append(ASM.getDescriptor(type));
        }
        if (singleton) {
            String fieldName = getSingletonFieldName(method);
            cv
            .visitField(
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
                    fieldName, 
                    ASM.getDescriptor(implementationType), 
                    null,
                    null)
            .visitEnd();
            singletonFields.put(fieldName, implementationType);
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    method.getName(), 
                    ASM.getDescriptor(method), 
                    null, 
                    null);
            mv.visitCode();
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    className.replace('.', '/'), 
                    fieldName, 
                    ASM.getDescriptor(implementationType));
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        } else  {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    method.getName(), 
                    ASM.getDescriptor(method), 
                    null, 
                    null);
            mv.visitCode();
            mv.visitTypeInsn(Opcodes.NEW, implementationInternalName);
            mv.visitInsn(Opcodes.DUP);
            int slot = 1;
            for (Class<?> parameterType : parameterTypes) {
                mv.visitVarInsn(ASM.getLoadCode(parameterType), slot);
                slot += ASM.getSlotCount(parameterType);
            }
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    implementationInternalName, 
                    "<init>", 
                    '(' + builder.toString() + ")V",
                    false);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }
    
    private void generateFactoryStaticBlock(
            ClassVisitor cv, String className, Map<String, Class<?>> singletonFields) {
        String internalName = className.replace('.', '/');
        MethodVisitor mv = cv.visitMethod(
                Opcodes.ACC_STATIC, 
                "<clinit>", 
                "()V", 
                null,
                null);
        mv.visitCode();
        for (Entry<String, Class<?>> entry : singletonFields.entrySet()) {
            String objectInternalName = entry.getValue().getName().replace('.', '/');
            mv.visitTypeInsn(Opcodes.NEW, objectInternalName);
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, objectInternalName, "<init>", "()V", false);
            mv.visitFieldInsn(
                    Opcodes.PUTSTATIC, 
                    internalName, 
                    entry.getKey(), 
                    'L' + objectInternalName + ';');
        }
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private void generateObjectSerialVersionUID(
            ClassVisitor cv, Context context) {
        long serialVersionUID = context.getClassName().hashCode() * 31 + context.getProperties().hashCode();
        cv
        .visitField(
                Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, 
                "serialVersionUID", 
                "J", 
                null, 
                serialVersionUID)
        .visitEnd();
    }
    
    private void generateObjectConstrucotor(ClassVisitor cv, Context context) {
        StringBuilder desc = new StringBuilder();
        desc.append('(');
        for (MethodInfo methodInfo : context.getProperties().values()) {
            desc.append(ASM.getDescriptor(methodInfo.getResolvedReturnType()));
        }
        desc.append(")V");
        MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "<init>", desc.toString(), null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        int slot = 1;
        for (Entry<String, MethodInfo> entry : context.getProperties().entrySet()) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            Class<?> propertyType = entry.getValue().getReturnType();
            mv.visitVarInsn(ASM.getLoadCode(propertyType), slot);
            slot += ASM.getSlotCount(propertyType);
            mv.visitFieldInsn(
                    Opcodes.PUTFIELD, 
                    context.getClassInternalName(), 
                    entry.getKey(), 
                    ASM.getDescriptor(propertyType));
        }
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private void generateObjectHashCode(ClassVisitor cv, Context context) {
        XMethodVisitor mv = ASM.visitMethod(
                cv,
                Opcodes.ACC_PUBLIC, 
                "hashCode", 
                "()I", 
                null,
                null);
        mv.visitCode();
        mv.visitInsn(Opcodes.ICONST_0);
        if (!context.getProperties().isEmpty()) {
            mv.visitVarInsn(Opcodes.ISTORE, 1);
            for (Entry<String, MethodInfo> entry : context.getProperties().entrySet()) {
                Class<?> propertyType = entry.getValue().getReturnType();
                mv.visitVarInsn(Opcodes.ILOAD, 1);
                mv.visitLdcInsn(31);
                mv.visitInsn(Opcodes.IMUL);
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        context.getClassInternalName(), 
                        entry.getKey(), 
                        ASM.getDescriptor(propertyType));
                mv.visitHashCode(propertyType, true);
                mv.visitInsn(Opcodes.IADD);
                mv.visitVarInsn(Opcodes.ISTORE, 1);
            }
            mv.visitVarInsn(Opcodes.ILOAD, 1);
        }
        mv.visitInsn(Opcodes.IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private void generateObjectEquals(ClassVisitor cv, Context context) {
        XMethodVisitor mv = ASM.visitMethod(
                cv,
                Opcodes.ACC_PUBLIC, 
                "equals", 
                "(Ljava/lang/Object;)Z", 
                null,
                null);
        mv.visitCode();
        
        Label notSameLabel = new Label();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitJumpInsn(Opcodes.IF_ACMPNE, notSameLabel);
        mv.visitInsn(Opcodes.ICONST_1);
        mv.visitInsn(Opcodes.IRETURN);
        mv.visitLabel(notSameLabel);
        
        Label instanceOfLabel = new Label();
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitTypeInsn(Opcodes.INSTANCEOF, context.getInterfaceInternalName());
        mv.visitJumpInsn(Opcodes.IFNE, instanceOfLabel);
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitInsn(Opcodes.IRETURN);
        mv.visitLabel(instanceOfLabel);
        
        if (!context.properties.isEmpty()) {
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitTypeInsn(Opcodes.CHECKCAST, context.getInterfaceInternalName());
            mv.visitVarInsn(Opcodes.ASTORE, 2);
            
            for (Entry<String, MethodInfo> entry : context.getProperties().entrySet()) {
                Label continueLabel = new Label();
                MethodInfo methodInfo = entry.getValue();
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        context.getClassInternalName(), 
                        entry.getKey(), 
                        ASM.getDescriptor(methodInfo.getReturnType()));
                mv.visitVarInsn(Opcodes.ALOAD, 2);
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        context.getInterfaceInternalName(), 
                        methodInfo.getName(), 
                        ASM.getDescriptor(methodInfo.getRawMethod()),
                        true);
                mv.visitEquals(methodInfo.getReturnType(), true);
                mv.visitJumpInsn(Opcodes.IFNE, continueLabel);
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitInsn(Opcodes.IRETURN);
                mv.visitLabel(continueLabel);
            }
        }
        
        mv.visitInsn(Opcodes.ICONST_1);
        mv.visitInsn(Opcodes.IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private void generateObjectToString(ClassVisitor cv, Context context) {
        String stringBuilderInternalName = StringBuilder.class.getName().replace('.', '/');
        String appendStringDesc = "(Ljava/lang/String;)L" + stringBuilderInternalName + ';';
        MethodVisitor mv = cv.visitMethod(
                Opcodes.ACC_PUBLIC, 
                "toString", 
                "()Ljava/lang/String;", 
                null,
                null);
        mv.visitCode();
        mv.visitTypeInsn(Opcodes.NEW, stringBuilderInternalName);
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, stringBuilderInternalName, "<init>", "()V", false);
        mv.visitLdcInsn(context.getInterfaceRawClass().getName());
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, stringBuilderInternalName, "append", appendStringDesc, false);
        if (!context.getProperties().isEmpty()) {
            boolean addComma = false;
            mv.visitLdcInsn("{ ");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, stringBuilderInternalName, "append", appendStringDesc, false);
            for (Entry<String, MethodInfo> entry : context.getProperties().entrySet()) {
                Class<?> propertyType = entry.getValue().getReturnType();
                if (addComma) {
                    mv.visitLdcInsn(", ");
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, stringBuilderInternalName, "append", appendStringDesc, false);
                } else {
                    addComma = true;
                }
                mv.visitLdcInsn(entry.getKey());
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, stringBuilderInternalName, "append", appendStringDesc, false);
                mv.visitLdcInsn(" : ");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, stringBuilderInternalName, "append", appendStringDesc, false);
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        context.getClassInternalName(), 
                        entry.getKey(), 
                        ASM.getDescriptor(propertyType));
                ASM.visitNullSafeToString(mv, propertyType);
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, stringBuilderInternalName, "append", appendStringDesc, false);
            }
            mv.visitLdcInsn(" }");
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, stringBuilderInternalName, "append", appendStringDesc, false);
        }
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private static String getFactoryImplementationClassName(Class<?> factoryType) {
        return
            factoryType.getName() + 
            '{' +
            NAME_POSTFIX +
            '}';
    }
    
    private static String getObjectImplementationClassName(Class<?> interfaceType) {
        return
            interfaceType.getName() + 
            '{' +
            NAME_POSTFIX +
            '}';
    }
    
    private static String getSingletonFieldName(Method method) {
        return method.getName() + SINGLETON_POSTFIX;
    }
    
    protected static final class Context {
        
        private Class<?> interfaceRawClass;
        
        private ClassInfo<?> interfaceClassInfo;
        
        private Map<MethodDescriptor, MethodImplementation> implementationMap;
        
        private String interfaceInternalName;
        
        private String interfaceDesc;
        
        private String className;
        
        private String classInternalName;
        
        private String classDesc;
        
        private XOrderedMap<String, MethodInfo> properties;
        
        private Collection<MethodInfo> autonomyMethods;
        
        public Context(Type interfaceType) {
            this.interfaceRawClass = GenericTypes.eraseGenericType(interfaceType);
            this.interfaceClassInfo = ClassInfo.of(interfaceType);
            this.implementationMap = this.interfaceClassInfo.getMethodImplementationMap();
            this.interfaceInternalName = this.interfaceRawClass.getName().replace('.', '/');
            this.interfaceDesc = 'L' + this.interfaceInternalName + ';';
            this.className = getObjectImplementationClassName(this.interfaceRawClass);
            this.classInternalName = this.className.replace('.', '/');
            this.classDesc = 'L' + this.classInternalName + ';';
        }

        public Class<?> getInterfaceRawClass() {
            return this.interfaceRawClass;
        }
        
        public ClassInfo<?> getInterfaceClassInfo() {
            return this.interfaceClassInfo;
        }

        public String getInterfaceInternalName() {
            return this.interfaceInternalName;
        }

        public String getInterfaceDesc() {
            return this.interfaceDesc;
        }

        public String getClassName() {
            return this.className;
        }

        public String getClassInternalName() {
            return this.classInternalName;
        }

        public String getClassDesc() {
            return this.classDesc;
        }
        
        public XOrderedMap<String, MethodInfo> getProperties() {
            XOrderedMap<String, MethodInfo> properties = this.properties;
            if (properties == null) {
                this.properties = properties = this.createProperties();
            }
            return properties;
        }

        public Collection<MethodInfo> getAutonomyMethods() {
            Collection<MethodInfo> autonomyMethods = this.autonomyMethods;
            if (autonomyMethods == null) {
                this.autonomyMethods = autonomyMethods = this.createAutonomyMethods();
            }
            return autonomyMethods;
        }
        
        private XOrderedMap<String, MethodInfo> createProperties() {
            ClassInfo<?> interfaceClassInfo = this.interfaceClassInfo;
            XOrderedMap<String, MethodInfo> properties = new LinkedHashMap<String, MethodInfo>();
            Parameters parametersAnnotation = interfaceClassInfo.getAnnotation(Parameters.class);
            if (parametersAnnotation != null && !parametersAnnotation.value().isEmpty()) {
                for (String value : ORDER_SPLIT_PARTTERN.split(parametersAnnotation.value())) {
                    if (properties.put(value, null) != null) {
                        throw new IllegalProgramException(
                                "Duplicated properties \"" +
                                value +
                                "\" are declared in the annotation @" +
                                Parameters.class.getName() +
                                "(" +
                                parametersAnnotation.value() +
                                ") of interface " +
                                interfaceClassInfo.getName());
                    }
                }
            }
            for (PropertyInfo property : interfaceClassInfo.getProperties()) {
                if (properties.containsKey(property.getName())) {
                    if (property.getSetter() != null) {
                        throw new IllegalProgramException(
                                "The property \"" +
                                property.toGenericString() +
                                "\" is referenced by the annotation @" +
                                Parameters.class.getName() +
                                "(" + 
                                parametersAnnotation.value() + 
                                ") of interface " +
                                interfaceClassInfo.toGenericString() +
                                ", so it can not support setter");
                    }
                    if (!this.implementationMap.get(property.getGetter().getDescriptor()).isBridge()) {
                        if (property.getGetter().isAnnotationPresent(Autonomy.class)) {
                            throw new IllegalProgramException(
                                    "The property \"" +
                                    property.toGenericString() +
                                    "\" is referenced by the annotation @" +
                                    Parameters.class.getName() +
                                    "(" + 
                                    parametersAnnotation.value() + 
                                    ") of interface " +
                                    interfaceClassInfo.toGenericString() +
                                    ", so it can not be marked by annotation @" +
                                    Autonomy.class.getName());
                        }
                        MethodInfo existing = properties.put(property.getName(), property.getGetter());
                        if (existing != null) {
                            throw new AssertionError();
                        }
                    }
                }
            }
            for (Entry<String, MethodInfo> entry : properties.entrySet()) {
                if (entry.getValue() == null) {
                    throw new IllegalProgramException(
                            "The property \"" +
                            entry.getKey() +
                            "\" is referenced by the annotation @" +
                            Parameters.class.getName() +
                            "(" + 
                            parametersAnnotation.value() + 
                            ") of interface " +
                            interfaceClassInfo.toGenericString() +
                            "so but there is no such property in that interface.");
                }
            }
            return MACollections.unmodifiable(properties);
        }
        
        private Collection<MethodInfo> createAutonomyMethods() {
            Set<MethodInfo> autonomyMethods = new HashSet<MethodInfo>();
            for (MethodImplementation implementation : this.implementationMap.values()) {
                if (!implementation.isBridge()) {
                    for (MethodInfo methodInfo : implementation.getMethods()) {
                        if (methodInfo.isAnnotationPresentByInheritance(Autonomy.class)) {
                            autonomyMethods.add(methodInfo);
                        }
                    }
                }
            }
            return MACollections.unmodifiable(autonomyMethods);
        }
        
    }
    
    private interface Resource {
        
        String mustOverrideGenerateAutomyMethod(
                Class<? extends ImmutableObjects> runtimeType);
        
        String singleInstanceForEachDerivedType(
                Class<ImmutableObjects> immutableType,
                Class<?> runtimeType);
        
        String parameterOfFactoryMethodMustBe(
                Method method, 
                int parameterIndex,
                Type expectedResolvedGenericReturnType);

        String parameterCountOfFactoryMethodMustBe(
                Method method, 
                int expectedParameterCount);

        String immutableTypeMustContainsNoParametersWhenItIsSingleton(
                Class<?> interfaceType, 
                Class<Parameters> parametersType,
                Class<Singleton> singleTonType);

        String immutableTypeMustNotBeAbstract(
                Class<?> interfaceType,
                Class<Parameters> parametersTyps);

        String immutableTypeMustBeInterface(Class<?> interfaceType);

        String returnTypeMustBeNotSame(Method method1, Method method2);

        String returnTypeMustBeInterface(Method method);
        
        String mustNotThrowCheckedException(Method method);
    }
}
