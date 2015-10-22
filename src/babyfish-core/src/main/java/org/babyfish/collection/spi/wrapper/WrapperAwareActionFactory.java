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
package org.babyfish.collection.spi.wrapper;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.babyfish.collection.HashMap;
import org.babyfish.collection.XCollection;
import org.babyfish.collection.XMap;
import org.babyfish.lang.Action;
import org.babyfish.lang.BinaryAction;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.lang.reflect.asm.ASM;
import org.babyfish.lang.reflect.asm.XMethodVisitor;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.util.LazyResource;
import org.babyfish.view.View;

/**
 * @author Tao Chen
 */
final class WrapperAwareActionFactory {
    
    private static final LazyResource<Resource> LAZY_RESOURCE = LazyResource.of(Resource.class);

    private static final ReadWriteLock READ_WRITE_LOCK = new ReentrantReadWriteLock();
    
    private static final Map<Class<?>, BinaryAction<Object, Object>> BASE_CLASS_ACTIONS = new HashMap<>();
    
    private static final BinaryAction<Object, Object> NIL_ACTION = new BinaryAction<Object, Object>() {

        @Override
        public void run(Object x, Object y) {
            throw new AssertionError();
        }
        
    };
    
    private WrapperAwareActionFactory() {
        throw new UnsupportedOperationException();
    }
    
    public static BinaryAction<Object, Object> getWrapperAwareAction(Class<?> baseClass) {
        
        Map<Class<?>, BinaryAction<Object, Object>> baseClassActions = BASE_CLASS_ACTIONS;
        ReadWriteLock readWriteLock = READ_WRITE_LOCK;
        
        Lock lock;
        BinaryAction<Object, Object> action;
        
        (lock = readWriteLock.readLock()).lock();
        try {
            action = baseClassActions.get(baseClass); //1st reading
        } finally {
            lock.unlock();
        }
        
        if (action == null) { //1st checking
            (lock = readWriteLock.writeLock()).lock();
            try {
                action = baseClassActions.get(baseClass);
                if (action == null) {       
                    action = createWrapperAwareAction(baseClass);
                    if (action == null) {
                        action = NIL_ACTION;
                    }
                    baseClassActions.put(baseClass, action);
                }
            } finally {
                lock.unlock();
            }
        }
        return action == NIL_ACTION ? null : action;
    }
    
    @SuppressWarnings("unchecked")
    private static BinaryAction<Object, Object> createWrapperAwareAction(Class<?> baseClass) {
        Class<?> collectionInterfaceType = 
                XMap.class.isAssignableFrom(baseClass) ? 
                        XMap.class : 
                        XCollection.class;
        Method awareMethod = null;
        for (Class<?> clazz = baseClass; 
                collectionInterfaceType.isAssignableFrom(clazz); 
                clazz = clazz.getSuperclass()) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(WrapperAware.class)) {
                    if (awareMethod != null) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().severalMethodsWithWrapperAware(
                                        awareMethod, 
                                        method, 
                                        WrapperAware.class
                                )
                        );
                    }
                    awareMethod = method;
                }
            }
        }
        if (awareMethod == null) {
            return null;
        }
        if (View.class.isAssignableFrom(awareMethod.getDeclaringClass())) {
            throw new IllegalProgramException(
                    LAZY_RESOURCE.get().declaringClassOfWrapperAwreMethodMustBeNotView(
                            awareMethod,
                            WrapperAware.class,
                            View.class)
            );
        }
        if (Modifier.isStatic(awareMethod.getModifiers())) {
            throw new IllegalProgramException(
                    LAZY_RESOURCE.get().wrapperAwareMethodMustNotBeStatic(
                            awareMethod,
                            WrapperAware.class)
            );
        }
        if (Modifier.isPrivate(awareMethod.getModifiers())) {
            throw new IllegalProgramException(
                    LAZY_RESOURCE.get().wrapperAwareMethodMustNotBeStatic(
                            awareMethod,
                            WrapperAware.class)
            );
        }
        if (awareMethod.getReturnType() != void.class) {
            throw new IllegalProgramException(
                    LAZY_RESOURCE.get().wrapperAwareMethodMustNotBePrivate(
                            awareMethod,
                            WrapperAware.class)
            );
        }
        Class<?>[] parameterTypes = awareMethod.getParameterTypes();
        if (parameterTypes.length != 1 || parameterTypes[0] != collectionInterfaceType) {
            throw new IllegalProgramException(
                    LAZY_RESOURCE.get().wrapperAwareMethodMustHaveOneParameter(
                            awareMethod,
                            WrapperAware.class,
                            collectionInterfaceType)
            );
        }
        Class<?> actionClass = createWrapperAwareActionClass(awareMethod);
        try {
            return (BinaryAction<Object, Object>)actionClass.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new AssertionError();
        }
    }
    
    private static Class<?> createWrapperAwareActionClass(final Method awareMethod) {
        final String className = 
                awareMethod.getDeclaringClass().getName() + 
                "$WrapperAwareAction{92B8C17E_BF4E_4135_B596_5A76E0FEBF4E}";
        final Class<?> targetClass = awareMethod.getDeclaringClass();
        final Class<?> collectionInterface = 
                XMap.class.isAssignableFrom(targetClass) ? 
                        XMap.class : 
                        XCollection.class;
        Action<ClassVisitor> cvAction = new Action<ClassVisitor>() {

            @Override
            public void run(ClassVisitor cv) {
                
                String internalName = className.replace('.', '/');
                cv.visit(
                        Opcodes.V1_7, 
                        Opcodes.ACC_PUBLIC, 
                        internalName, 
                        null, 
                        "java/lang/Object", 
                        new String[] { ASM.getInternalName(BinaryAction.class) });
                
                XMethodVisitor mv = ASM.visitMethod(
                        cv, 
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
                
                mv = ASM.visitMethod(
                        cv,
                        Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_BRIDGE, 
                        "run", 
                        "(Ljava/lang/Object;Ljava/lang/Object;)V", 
                        null,
                        null);
                mv.visitCode();
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(targetClass));
                mv.visitVarInsn(Opcodes.ALOAD, 2);
                mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(collectionInterface));
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        internalName, 
                        "run", 
                        "(" +
                        ASM.getDescriptor(targetClass) +
                        ASM.getDescriptor(collectionInterface) +
                        ")V",
                        false);
                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
                
                mv = ASM.visitMethod(
                        cv,
                        Opcodes.ACC_PUBLIC, 
                        "run", 
                        "(" +
                        ASM.getDescriptor(targetClass) +
                        ASM.getDescriptor(collectionInterface) +
                        ")V",
                        null,
                        null);
                mv.visitCode();
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitVarInsn(Opcodes.ALOAD, 2);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        ASM.getInternalName(targetClass), 
                        awareMethod.getName(), 
                        ASM.getDescriptor(awareMethod),
                        false);
                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
                
                cv.visitEnd();
            }
            
        };
        return ASM.loadDynamicClass(
                awareMethod.getDeclaringClass().getClassLoader(), 
                className, 
                awareMethod.getDeclaringClass().getProtectionDomain(), 
                cvAction);
    }
    
    private interface Resource {
        
        String severalMethodsWithWrapperAware(
                Method method1,
                Method method2,
                Class<WrapperAware> wrapperAwareType);

        String wrapperAwareMethodMustHaveOneParameter(
                Method awareMethod,
                Class<WrapperAware> wrapperAwareType, 
                Class<?> collectionInterfaceType);

        String wrapperAwareMethodMustNotReturnVoid(
                Method awareMethod,
                Class<WrapperAware> wrapperAwareType);

        String wrapperAwareMethodMustNotBeStatic(
                Method awareMethod,
                Class<WrapperAware> wrapperAwareType);
        
        String wrapperAwareMethodMustNotBePrivate(
                Method awareMethod,
                Class<WrapperAware> wrapperAwareType);

        String declaringClassOfWrapperAwreMethodMustBeNotView(
                Method awareMethod, 
                Class<WrapperAware> wrapperAwareType,
                Class<View> viewType);
    }
}
