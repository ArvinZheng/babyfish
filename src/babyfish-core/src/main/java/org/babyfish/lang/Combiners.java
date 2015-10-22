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
package org.babyfish.lang;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.babyfish.lang.reflect.asm.ASM;
import org.babyfish.lang.reflect.asm.Catch;
import org.babyfish.lang.reflect.asm.XMethodVisitor;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.Label;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.util.LazyResource;

/**
 * @author Tao Chen
 */
public class Combiners {
    
    private static final Map<Class<?>, Combiner<?>> DEFAULT_CACHE = new WeakHashMap<>();
    
    private static final ReadWriteLock DEFAULT_CACHE_LOCK = new ReentrantReadWriteLock();
    
    private static final Map<Class<?>, Combiner<?>> BREAK_CACHE = new WeakHashMap<>();
    
    private static final ReadWriteLock BREAK_CACHE_LOCK = new ReentrantReadWriteLock();
    
    private static final Map<Class<?>, Combiner<?>> CONTINUE_CACHE = new WeakHashMap<>();
    
    private static final ReadWriteLock CONTINUE_CACHE_LOCK = new ReentrantReadWriteLock();
    
    private static final LazyResource<Resource> LAZY_RESOURCE = LazyResource.of(Resource.class);
    
    private static final String NAME_POSTFIX = "92B8C17E_BF4E_4135_B596_5A76E0FEBF4E";
    
    protected Combiners() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
    
    /**
     * <p>
     *  This invocation of this method is same with the 
     *  the invocation of the other method {@link #of(Class, ChainInvocationExceptionHandleMode)}
     *  using null to its second parameter.
     * </p>
     * @param <I> The interface Type
     * @param interfaceType The interface type
     * @return The combiner of the specified interface.
     * @exception NullArgumentException The parameter "interfaceType" is null
     * @exception IllegalArgumentException
     * <ul>
     *    <li> The parameter "interfaceType" is not an interface</li>
     *    <li> The parameter "interfaceType" is an interface that extends {@link CanNotBeCombined}</li>
     *    <li> The parameter "interfaceType" is an interface that extends {@link HasBeenCombined}</li>
     * </ul>
     * @exception IllegalProgramException The parameter 
     * "interfaceType" is an interface that extends from two interfaces, one uses the annotation
     * {@link DefaultChainInvocationExceptionHandleMode} with the value 
     * {@link ChainInvocationExceptionHandleMode#BREAK} and the another one uses 
     * the annotation {@link DefaultChainInvocationExceptionHandleMode} with the value 
     * {@link ChainInvocationExceptionHandleMode#CONTINUE}.
     * @see #of(Class, ChainInvocationExceptionHandleMode)
     */
    @SuppressWarnings("unchecked")
    public static <I> Combiner<I> of(Class<I> interfaceType) {
        
        Combiner<?> combiner;
        Lock lock;
        
        (lock = DEFAULT_CACHE_LOCK.readLock()).lock(); //1st locking
        try {
            combiner = DEFAULT_CACHE.get(interfaceType); //1st reading
        } finally {
            lock.unlock();
        }
        
        if (combiner == null) {
            (lock = DEFAULT_CACHE_LOCK.writeLock()).lock(); //2nd locking
            try {
                combiner = DEFAULT_CACHE.get(interfaceType); //2nd reading
                if (combiner == null) { //2nd checking
                    combiner = createCombiner(interfaceType);
                    DEFAULT_CACHE.put(interfaceType, combiner);
                }
            } finally {
                lock.unlock();
            }
        }
        return (Combiner<I>)combiner;
    }
    
    /**
     * <p>
     *    When the parameter "chainInvocationExceptionHandleMode" is null,
     *  This method is same to {@link #of(Class)}
     * </p>
     * @param <I> The interface Type
     * @param interfaceType The interface Type
     * @param mode Decide should continue to execute when one sub interface raise exception or not.
     * @return The combiner of the specified interface.
     * @exception NullArgumentException The parameter "interfaceType" is null
     * @exception IllegalArgumentException
     * <ul>
     *    <li> The parameter "interfaceType" is not an interface</li>
     *    <li> The parameter "interfaceType" is an interface that extends {@link CanNotBeCombined}</li>
     *    <li> The parameter "interfaceType" is an interface that extends {@link HasBeenCombined}</li>
     * </ul>
     * @exception IllegalProgramException This exception will be thrown only when the 
     * parameter "chainInvocationExceptionHandleMode" is null, that means the parameter 
     * "interfaceType" is an interface that extends from two interfaces, one uses the annotation
     * {@link DefaultChainInvocationExceptionHandleMode} with the value 
     * {@link ChainInvocationExceptionHandleMode#BREAK} and the another one uses 
     * the annotation {@link DefaultChainInvocationExceptionHandleMode} with the value 
     * {@link ChainInvocationExceptionHandleMode#CONTINUE}.
     * @see #of(Class)
     */
    @SuppressWarnings("unchecked")
    public static <I> Combiner<I> of(Class<I> interfaceType, ChainInvocationExceptionHandleMode mode) {
        
        if (mode == null) {
            return of(interfaceType);
        }
        
        Combiner<?> combiner;
        Lock lock;
        
        Map<Class<?>, Combiner<?>> cache;
        ReadWriteLock cacheLock;
        if (mode == ChainInvocationExceptionHandleMode.BREAK) {
            cache = BREAK_CACHE;
            cacheLock = BREAK_CACHE_LOCK;
        } else {
            cache = CONTINUE_CACHE;
            cacheLock = CONTINUE_CACHE_LOCK;
        }
        
        (lock = cacheLock.readLock()).lock(); //1st locking
        try {
            combiner = cache.get(interfaceType); //1st reading
        } finally {
            lock.unlock();
        }
        
        if (combiner == null) { //1st checking
            (lock = cacheLock.writeLock()).lock(); //2nd locking
            try {
                combiner = cache.get(interfaceType); //2nd reading
                if (combiner == null) { //2nd checking
                    combiner = createCombiner(interfaceType, mode);
                    cache.put(interfaceType, combiner);
                }
            } finally {
                lock.unlock();
            }
        }
        
        return (Combiner<I>)combiner;
    }
    
    private static Combiner<?> createCombiner(Class<?> interfaceType) {
        ChainInvocationExceptionHandleMode mode = getDefaultChainInvocationExceptionHandleMode(interfaceType);
        return createCombiner(interfaceType, mode);
    }
    
    private static Combiner<?> createCombiner(Class<?> interfaceType, ChainInvocationExceptionHandleMode mode) {
        Arguments.mustNotBeNull("interfaceType", interfaceType);
        Arguments.mustBeInterface("interfaceType", interfaceType);
        Arguments.mustNotBeCompatibleWithValue("interfaceType", interfaceType, CanNotBeCombined.class);
        Arguments.mustNotBeCompatibleWithValue("interfaceType", interfaceType, HasBeenCombined.class);
        new ImplementationGenerator(interfaceType, mode);
        Class<?> combinerClass = new CombinerImplGenerator(interfaceType, mode).getResultClass();
        try {
            return (Combiner<?>)combinerClass.newInstance();
        } catch (InstantiationException canNotHappen) {
            throw new AssertionError();
        } catch (IllegalAccessException canNotHappen) {
            throw new AssertionError();
        }
    }
    
    private static ChainInvocationExceptionHandleMode getDefaultChainInvocationExceptionHandleMode(
            Class<?> interfaceType) {
        Annotation[] annotations = interfaceType.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == DefaultChainInvocationExceptionHandleMode.class) {
                DefaultChainInvocationExceptionHandleMode dciehm = 
                        (DefaultChainInvocationExceptionHandleMode)annotation;
                return dciehm.value();
            }
        }
        ChainInvocationExceptionHandleMode mergedMode = null;
        Class<?> preSuperInterfaceType = null;
        for (Class<?> superInterfaceType : interfaceType.getInterfaces()) {
            ChainInvocationExceptionHandleMode ciehm = 
                    getDefaultChainInvocationExceptionHandleMode(superInterfaceType);
            if (ciehm != null) {
                if (mergedMode == null) {
                    mergedMode = ciehm;
                    preSuperInterfaceType = superInterfaceType;
                } else if (mergedMode != ciehm) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().defaultChainInvocationExceptionHandleModesAreConflict(
                                    interfaceType,
                                    DefaultChainInvocationExceptionHandleMode.class,
                                    preSuperInterfaceType, 
                                    superInterfaceType
                            )
                    );
                }
            }
        }
        if (mergedMode == null) {
            mergedMode = ChainInvocationExceptionHandleMode.BREAK;
        }
        return mergedMode;
    }
    
    private static String combinerImplClassName(Class<?> interfaceType, ChainInvocationExceptionHandleMode mode) {
        String typeNamePrefix = interfaceType.getName();
        Package pkg = interfaceType.getPackage();
        if (pkg != null) {
            if (typeNamePrefix.startsWith("java.") ||
                    typeNamePrefix.startsWith("javax.")) {
                typeNamePrefix = 
                        Combiners.class.getPackage().getName() +
                        typeNamePrefix.substring(pkg.getName().length());
            }
        }
        return typeNamePrefix + 
                "{combiner:" +
                mode.name() +
                "_" + 
                NAME_POSTFIX +
                '}';
    }
    
    private static String implementationClassName(Class<?> interfaceType, ChainInvocationExceptionHandleMode mode, boolean simpleName) {
        if (simpleName) {
            return "Implementation";
        }
        return combinerImplClassName(interfaceType, mode) + '$' + implementationClassName(interfaceType, mode, true);
    }
    
    private static class CombinerImplGenerator {
        
        private Class<?> interfaceType;
        
        private ChainInvocationExceptionHandleMode mode;
        
        private String className;
        
        private String internalName;
        
        private String interfaceInternalName;
        
        private String interfaceImplInternalName;
        
        private String interfaceDescriptor;
        
        private Class<?> resultClass;
    
        CombinerImplGenerator(Class<?> interfaceType, ChainInvocationExceptionHandleMode mode) {
            this.interfaceType = interfaceType;
            this.mode = mode;
            this.className = combinerImplClassName(interfaceType, mode);
            this.internalName = this.className.replace('.', '/');
            this.interfaceInternalName = ASM.getInternalName(interfaceType);
            this.interfaceDescriptor = ASM.getDescriptor(interfaceType);
            this.interfaceImplInternalName = implementationClassName(interfaceType, mode, false).replace('.', '/');
            this.resultClass = this.generate();
        }
        
        public Class<?> getResultClass() {
            return resultClass;
        }

        private Class<?> generate() {
            Action<ClassVisitor> cvAction = new Action<ClassVisitor>() {
                @Override
                public void run(ClassVisitor cv) {
                    CombinerImplGenerator.this.generate(cv);
                }
            };
            return ASM.loadDynamicClass(
                    interfaceType.getClassLoader(), 
                    this.className, 
                    interfaceType.getProtectionDomain(),
                    cvAction);
        }
        
        private void generate(ClassVisitor cv) {
            cv.visit(
                    Opcodes.V1_7, 
                    Opcodes.ACC_PUBLIC, 
                    this.internalName, 
                    null, 
                    "java/lang/Object", 
                    new String[] { Combiner.class.getName().replace('.', '/') });
            cv.visitInnerClass(
                    this.interfaceImplInternalName, 
                    this.internalName, 
                    implementationClassName(interfaceType, mode, true), 
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC);
            this.generateConstructor(cv);
            this.generateCombineBridge(cv);
            this.generateCombine(cv);
            this.generateRemoveBridge(cv);
            this.generateRemove(cv);
            this.generateSize(cv);
            this.generateGet(cv);
            cv.visitEnd();
        }
        
        private void generateConstructor(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateCombineBridge(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_BRIDGE, 
                    "combine", 
                    "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", 
                    null, 
                    null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitTypeInsn(Opcodes.CHECKCAST, this.interfaceInternalName);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitTypeInsn(Opcodes.CHECKCAST, this.interfaceInternalName);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    this.internalName, 
                    "combine", 
                    "(" +
                    this.interfaceDescriptor +
                    this.interfaceDescriptor +
                    ")" +
                    this.interfaceDescriptor,
                    false);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateCombine(ClassVisitor cv) {
            
            Label aIsNotNull = new Label();
            Label bIsNotNull = new Label();
            Label beginALoop = new Label();
            Label endALoop = new Label();
            Label beginBLoop = new Label();
            Label endBLoop = new Label();
            int newArrIndex = 3;
            int iIndex = 4;
            int aSizeIndex = 5;
            int bSizeIndex = 6;
            
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "combine", 
                    "(" +
                    this.interfaceDescriptor +
                    this.interfaceDescriptor +
                    ")" +
                    this.interfaceDescriptor, 
                    null, 
                    null);
            mv.visitCode();
            
            // if (a == null) { return b; }
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitJumpInsn(Opcodes.IFNONNULL, aIsNotNull);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitLabel(aIsNotNull);
            
            // if (b == null) { return a; }
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitJumpInsn(Opcodes.IFNONNULL, bIsNotNull);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitLabel(bIsNotNull);
            
            /*
             * aSize = size(a);
             * bSize = size(b);
             * newArr = new ?[aSize + bSize];
             */
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    this.internalName, 
                    "size", 
                    "(" + this.interfaceDescriptor + ")I",
                    false);
            mv.visitVarInsn(Opcodes.ISTORE, aSizeIndex);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    this.internalName, 
                    "size", 
                    "(" + this.interfaceDescriptor + ")I",
                    false);
            mv.visitVarInsn(Opcodes.ISTORE, bSizeIndex);
            mv.visitVarInsn(Opcodes.ILOAD, aSizeIndex);
            mv.visitVarInsn(Opcodes.ILOAD, bSizeIndex);
            mv.visitInsn(Opcodes.IADD);
            mv.visitTypeInsn(Opcodes.ANEWARRAY, this.interfaceInternalName);
            mv.visitVarInsn(Opcodes.ASTORE, newArrIndex);
            
            /*
             * for (int i = 0; i < aSize; i++) {
             *      newArr[i] = get(a, i);
             * }
             */
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitVarInsn(Opcodes.ISTORE, iIndex);
            mv.visitLabel(beginALoop);
            mv.visitVarInsn(Opcodes.ILOAD, iIndex);
            mv.visitVarInsn(Opcodes.ILOAD, aSizeIndex);
            mv.visitJumpInsn(Opcodes.IF_ICMPGE, endALoop);
            mv.visitVarInsn(Opcodes.ALOAD, newArrIndex);
            mv.visitVarInsn(Opcodes.ILOAD, iIndex);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ILOAD, iIndex);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    this.internalName, 
                    "get", 
                    "(" + this.interfaceDescriptor + "I)" + this.interfaceDescriptor,
                    false);
            mv.visitInsn(Opcodes.AASTORE);
            mv.visitIincInsn(iIndex, 1);
            mv.visitJumpInsn(Opcodes.GOTO, beginALoop);
            mv.visitLabel(endALoop);
            
            /*
             * for (int i = 0; i < bSize; i++) {
             *      newArr[aSize + i] = get(b, i);
             * }
             */
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitVarInsn(Opcodes.ISTORE, iIndex);
            mv.visitLabel(beginBLoop);
            mv.visitVarInsn(Opcodes.ILOAD, iIndex);
            mv.visitVarInsn(Opcodes.ILOAD, bSizeIndex);
            mv.visitJumpInsn(Opcodes.IF_ICMPGE, endBLoop);
            mv.visitVarInsn(Opcodes.ALOAD, newArrIndex);
            mv.visitVarInsn(Opcodes.ILOAD, iIndex);
            mv.visitVarInsn(Opcodes.ILOAD, aSizeIndex);
            mv.visitInsn(Opcodes.IADD);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitVarInsn(Opcodes.ILOAD, iIndex);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    this.internalName, 
                    "get", 
                    "(" + this.interfaceDescriptor + "I)" + this.interfaceDescriptor,
                    false);
            mv.visitInsn(Opcodes.AASTORE);
            mv.visitIincInsn(iIndex, 1);
            mv.visitJumpInsn(Opcodes.GOTO, beginBLoop);
            mv.visitLabel(endBLoop);
            
            mv.visitTypeInsn(Opcodes.NEW, this.interfaceImplInternalName);
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ALOAD, newArrIndex);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    this.interfaceImplInternalName, 
                    "<init>", 
                    "([" + this.interfaceDescriptor +")V",
                    false);
            mv.visitInsn(Opcodes.ARETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateRemoveBridge(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_BRIDGE, 
                    "remove", 
                    "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", 
                    null, 
                    null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitTypeInsn(Opcodes.CHECKCAST, this.interfaceInternalName);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitTypeInsn(Opcodes.CHECKCAST, this.interfaceInternalName);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    this.internalName, 
                    "remove", 
                    "(" +
                    this.interfaceDescriptor +
                    this.interfaceDescriptor +
                    ")" +
                    this.interfaceDescriptor,
                    false);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateRemove(ClassVisitor cv) {
            
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "remove", 
                    "(" +
                    this.interfaceDescriptor +
                    this.interfaceDescriptor +
                    ")" +
                    this.interfaceDescriptor, 
                    null, 
                    null);
            mv.visitCode();
            
            Label aIsNotNull = new Label();
            Label bIsNotNull = new Label();
            Label beginALoop = new Label();
            Label endALoop = new Label();
            Label beginBLoop = new Label();
            Label endBLoop = new Label();
            Label jIsDisable = new Label();
            Label notMatch = new Label();
            Label newSizeIsNotASize = new Label();
            Label newSizeIsNot0 = new Label();
            Label newSizeIsNot1 = new Label();
            Label beginCheckLoop = new Label();
            Label endCheckLoop = new Label();
            Label isDisable = new Label();
            Label beginCheckLoop2 = new Label();
            Label endCheckLoop2 = new Label();
            Label isDisable2 = new Label();
            int aSizeIndex = 4;
            int bSizeIndex = 5;
            int aDFsIndex = 6;
            int bDFsIndex = 7;
            int iIndex = 8;
            int jIndex = 9;
            int newSizeIndex = 10;
            int newArrIndex = 11;
            
            // if (a == null) { return null; }
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitJumpInsn(Opcodes.IFNONNULL, aIsNotNull);
            mv.visitInsn(Opcodes.ACONST_NULL);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitLabel(aIsNotNull);
            
            // if (b == null) { return a; }
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitJumpInsn(Opcodes.IFNONNULL, bIsNotNull);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitLabel(bIsNotNull);
            
            /*
             * aSize = size(a);
             * aDFs = new boolean[aSize];
             */
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    this.internalName, 
                    "size", 
                    "(" + this.interfaceDescriptor + ")I",
                    false);
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ISTORE, aSizeIndex);
            mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BOOLEAN);
            mv.visitVarInsn(Opcodes.ASTORE, aDFsIndex);
            
            /*
             * bSize = size(b);
             * bDFs = new boolean[bSize];
             */
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    this.internalName, 
                    "size", 
                    "(" + this.interfaceDescriptor + ")I",
                    false);
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ISTORE, bSizeIndex);
            mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BOOLEAN);
            mv.visitVarInsn(Opcodes.ASTORE, bDFsIndex);
            
            /*
             * newSize = aSize;
             * for (int i = 0; i < aSize; i++) {
             *      for (int j = 0; j < bSize; j++) {
             *          if(!bDFs[j]) {
             */
            mv.visitVarInsn(Opcodes.ILOAD, aSizeIndex);
            mv.visitVarInsn(Opcodes.ISTORE, newSizeIndex);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitVarInsn(Opcodes.ISTORE, iIndex);
            mv.visitLabel(beginALoop);
            mv.visitVarInsn(Opcodes.ILOAD, iIndex);
            mv.visitVarInsn(Opcodes.ILOAD, aSizeIndex);
            mv.visitJumpInsn(Opcodes.IF_ICMPGE, endALoop);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitVarInsn(Opcodes.ISTORE, jIndex);
            mv.visitLabel(beginBLoop);
            mv.visitVarInsn(Opcodes.ILOAD, jIndex);
            mv.visitVarInsn(Opcodes.ILOAD, bSizeIndex);
            mv.visitJumpInsn(Opcodes.IF_ICMPGE, endBLoop);
            mv.visitVarInsn(Opcodes.ALOAD, bDFsIndex);
            mv.visitVarInsn(Opcodes.ILOAD, jIndex);
            mv.visitInsn(Opcodes.BALOAD);
            mv.visitJumpInsn(Opcodes.IFNE, jIsDisable);
            
            /*
             * if (get(a, i).equals(get(b, i))) {
             *      newSize--;
             *      aDFs[i] = true;
             *      bDFs[j] = true;
             *      break;
             * }
             */
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ILOAD, iIndex);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    this.internalName,
                    "get",
                    "(" + this.interfaceDescriptor + "I)" + this.interfaceDescriptor,
                    false);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitVarInsn(Opcodes.ILOAD, jIndex);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    this.internalName,
                    "get",
                    "(" + this.interfaceDescriptor + "I)" + this.interfaceDescriptor,
                    false);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/Object",
                    "equals",
                    "(Ljava/lang/Object;)Z",
                    false);
            mv.visitJumpInsn(Opcodes.IFEQ, notMatch);
            mv.visitVarInsn(Opcodes.ALOAD, aDFsIndex);
            mv.visitVarInsn(Opcodes.ILOAD, iIndex);
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitInsn(Opcodes.BASTORE);
            mv.visitIincInsn(newSizeIndex, -1);
            mv.visitVarInsn(Opcodes.ALOAD, bDFsIndex);
            mv.visitVarInsn(Opcodes.ILOAD, jIndex);
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitInsn(Opcodes.BASTORE);
            mv.visitJumpInsn(Opcodes.GOTO, endBLoop); //break;
            mv.visitLabel(notMatch);
            
            /*
             *          }
             *      }
             * }
             */
            mv.visitLabel(jIsDisable);
            mv.visitIincInsn(jIndex, 1);
            mv.visitJumpInsn(Opcodes.GOTO, beginBLoop);
            mv.visitLabel(endBLoop);
            mv.visitIincInsn(iIndex, 1);
            mv.visitJumpInsn(Opcodes.GOTO, beginALoop);
            mv.visitLabel(endALoop);
            
            /*
             * if (newSize == aSize) {
             *      return a;
             * }
             */
            mv.visitVarInsn(Opcodes.ILOAD, newSizeIndex);
            mv.visitVarInsn(Opcodes.ILOAD, aSizeIndex);
            mv.visitJumpInsn(Opcodes.IF_ICMPNE, newSizeIsNotASize);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitLabel(newSizeIsNotASize);
            
            /*
             * if (newSize == 0) {
             *      return null;
             * }
             */
            mv.visitVarInsn(Opcodes.ILOAD, newSizeIndex);
            mv.visitJumpInsn(Opcodes.IFNE, newSizeIsNot0);
            mv.visitInsn(Opcodes.ACONST_NULL);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitLabel(newSizeIsNot0);
            
            /*
             * if (newSize == 1) {
             *      for (int i = 0; i < aSize; i++) {
             *          if (!aDFs[i]) {
             *              return get(a, i);
             *          }
             *      }
             * }
             */
            mv.visitVarInsn(Opcodes.ILOAD, newSizeIndex);
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitJumpInsn(Opcodes.IF_ICMPNE, newSizeIsNot1);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitVarInsn(Opcodes.ISTORE, iIndex);
            mv.visitLabel(beginCheckLoop);
            mv.visitVarInsn(Opcodes.ILOAD, iIndex);
            mv.visitVarInsn(Opcodes.ILOAD, aSizeIndex);
            mv.visitJumpInsn(Opcodes.IF_ICMPGE, endCheckLoop);
            mv.visitVarInsn(Opcodes.ALOAD, aDFsIndex);
            mv.visitVarInsn(Opcodes.ILOAD, iIndex);
            mv.visitInsn(Opcodes.BALOAD);
            mv.visitJumpInsn(Opcodes.IFNE, isDisable);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ILOAD, iIndex);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    this.internalName, 
                    "get", 
                    "(" + this.interfaceDescriptor + "I)" + this.interfaceDescriptor,
                    false);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitLabel(isDisable);
            mv.visitIincInsn(iIndex, 1);
            mv.visitJumpInsn(Opcodes.GOTO, beginCheckLoop);
            mv.visitLabel(endCheckLoop);
            // Impossible to return null
            mv.visitInsn(Opcodes.ACONST_NULL);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitLabel(newSizeIsNot1);
            
            /*
             * i = 0;
             * j = 0;
             * newArr = new ?[newSize];
             * for (int i = 0; i < aSize; i++) {
             *      if (!aDFs[i]) {
             *          newArr[j++] = get(a, i);
             *      }
             * }
             * return new ?[delete...]$implementation(newArr);
             */
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitVarInsn(Opcodes.ISTORE, iIndex);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitVarInsn(Opcodes.ISTORE, jIndex);
            mv.visitVarInsn(Opcodes.ILOAD, newSizeIndex);
            mv.visitVarInsn(Opcodes.ILOAD, newSizeIndex);
            mv.visitTypeInsn(Opcodes.ANEWARRAY, interfaceType.getName().replace('.', '/'));
            mv.visitVarInsn(Opcodes.ASTORE, newArrIndex);
            mv.visitLabel(beginCheckLoop2);
            mv.visitVarInsn(Opcodes.ILOAD, iIndex);
            mv.visitVarInsn(Opcodes.ILOAD, aSizeIndex);
            mv.visitJumpInsn(Opcodes.IF_ICMPGE, endCheckLoop2);
            mv.visitVarInsn(Opcodes.ALOAD, aDFsIndex);
            mv.visitVarInsn(Opcodes.ILOAD, iIndex);
            mv.visitInsn(Opcodes.BALOAD);
            mv.visitJumpInsn(Opcodes.IFNE, isDisable2);
            mv.visitVarInsn(Opcodes.ALOAD, newArrIndex);
            mv.visitVarInsn(Opcodes.ILOAD, jIndex);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ILOAD, iIndex);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    this.internalName, 
                    "get", 
                    "(" + this.interfaceDescriptor + "I)" + this.interfaceDescriptor,
                    false);
            mv.visitInsn(Opcodes.AASTORE);
            mv.visitIincInsn(jIndex, 1);
            mv.visitLabel(isDisable2);
            mv.visitIincInsn(iIndex, 1);
            mv.visitJumpInsn(Opcodes.GOTO, beginCheckLoop2);
            mv.visitLabel(endCheckLoop2);
            mv.visitTypeInsn(Opcodes.NEW, this.interfaceImplInternalName);
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ALOAD, newArrIndex);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    this.interfaceImplInternalName,
                    "<init>", 
                    "([" + this.interfaceDescriptor + ")V",
                    false);
            mv.visitInsn(Opcodes.ARETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateSize(ClassVisitor cv) {
            String hasBeeanCombinedInternalName = HasBeenCombined.class.getName().replace('.', '/');
            Label isCombinedLabel = new Label();
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, 
                    "size", 
                    "(" +
                    this.interfaceDescriptor +
                    ")I", 
                    null, 
                    null);
            mv.visitCode();
            
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitTypeInsn(Opcodes.INSTANCEOF, hasBeeanCombinedInternalName);
            mv.visitJumpInsn(Opcodes.IFNE, isCombinedLabel);
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitLabel(isCombinedLabel);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitTypeInsn(Opcodes.CHECKCAST, hasBeeanCombinedInternalName);
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    hasBeeanCombinedInternalName, 
                    "size", 
                    "()I", 
                    true);
            mv.visitInsn(Opcodes.IRETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateGet(ClassVisitor cv) {
            String hasBeeanCombinedInternalName = HasBeenCombined.class.getName().replace('.', '/');
            Label isCombinedLabel = new Label();
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, 
                    "get", 
                    "(" + this.interfaceDescriptor + "I)" + this.interfaceDescriptor, 
                    null, 
                    null);
            mv.visitCode();
            
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitTypeInsn(Opcodes.INSTANCEOF, hasBeeanCombinedInternalName);
            mv.visitJumpInsn(Opcodes.IFNE, isCombinedLabel);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitLabel(isCombinedLabel);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitTypeInsn(Opcodes.CHECKCAST, hasBeeanCombinedInternalName);
            mv.visitVarInsn(Opcodes.ILOAD, 1);
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    hasBeeanCombinedInternalName, 
                    "get", 
                    "(I)Ljava/lang/Object;",
                    true);
            mv.visitTypeInsn(Opcodes.CHECKCAST, this.interfaceInternalName);
            mv.visitInsn(Opcodes.ARETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }
    
    private static class ImplementationGenerator {
        
        private Class<?> interfaceType;
        
        private ChainInvocationExceptionHandleMode mode;
        
        private String className;
        
        private String internalName;
        
        private String interfaceDescriptor;
        
        ImplementationGenerator(Class<?> interfaceType, ChainInvocationExceptionHandleMode mode) {
            this.interfaceType = interfaceType;
            this.mode = mode;
            this.className = implementationClassName(interfaceType, mode, false);
            this.internalName = this.className.replace('.', '/');
            this.interfaceDescriptor = ASM.getDescriptor(interfaceType);
            this.generate();
        }

        private Class<?> generate() {
            Action<ClassVisitor> cvAction = new Action<ClassVisitor>() {
                @Override
                public void run(ClassVisitor cv) {
                    ImplementationGenerator.this.generate(cv);
                }
            };
            return ASM.loadDynamicClass(
                    this.interfaceType.getClassLoader(), 
                    this.className, 
                    this.interfaceType.getProtectionDomain(),
                    cvAction);
        }
        
        private void generate(ClassVisitor cv) {
            cv.visit(
                    Opcodes.V1_7, 
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, 
                    this.internalName, 
                    null, 
                    "java/lang/Object", 
                    new String[] { 
                        interfaceType.getName().replace('.', '/'), 
                        HasBeenCombined.class.getName().replace('.', '/') });
            cv.visitOuterClass(this.internalName, null, null);
            cv.visitInnerClass(
                    this.internalName, 
                    combinerImplClassName(this.interfaceType, this.mode).replace('.', '/'), 
                    implementationClassName(this.interfaceType, this.mode, true), 
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC);
            cv
            .visitField(
                    Opcodes.ACC_PRIVATE, 
                    "arr", 
                    '[' + ASM.getDescriptor(interfaceType), 
                    null, 
                    null)
            .visitEnd();
            this.generateConstructor(cv);
            this.generateSize(cv);
            this.generateGet(cv);
            for (Method method : this.interfaceType.getMethods()) {
                this.generateMethod(cv, method);
            }
            this.generateHashCode(cv);
            this.generateEquals(cv);
            this.generateToString(cv);
            cv.visitEnd();
        }
        
        private void generateConstructor(ClassVisitor cv) {
            String arrDesc = "[" + ASM.getDescriptor(interfaceType);
            MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(" + arrDesc + ")V", null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    "java/lang/Object", 
                    "<init>", 
                    "()V",
                    false);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitFieldInsn(Opcodes.PUTFIELD, this.internalName, "arr", arrDesc);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateSize(ClassVisitor cv) {
            String arrDesc = "[" + ASM.getDescriptor(interfaceType);
            MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "size", "()I", null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, this.internalName, "arr", arrDesc);
            mv.visitInsn(Opcodes.ARRAYLENGTH);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateGet(ClassVisitor cv) {
            String arrDesc = "[" + ASM.getDescriptor(interfaceType);
            MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "get", "(I)Ljava/lang/Object;", null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, this.internalName, "arr", arrDesc);
            mv.visitVarInsn(Opcodes.ILOAD, 1);
            mv.visitInsn(Opcodes.AALOAD);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateMethod(final ClassVisitor cv, final Method method) {
            final String arrDesc = "[" + this.interfaceDescriptor;
            final String methodDesc = ASM.getDescriptor(method);
            XMethodVisitor mv = ASM.visitMethod(
                    cv,
                    Opcodes.ACC_PUBLIC, 
                    method.getName(), 
                    methodDesc, 
                    null, 
                    ASM.getExceptionInternalNames(method));
            mv.visitCode();
            
            final Class<?>[] parameterTypes = method.getParameterTypes();
            final Label beginLoopLabel = new Label();
            final Label endLoopLabel = new Label();
            final boolean continueMode = this.mode == ChainInvocationExceptionHandleMode.CONTINUE;
            
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, this.internalName, "arr", arrDesc);
            mv.visitInsn(Opcodes.DUP);
            final int arrIndex = mv.aSlot("arr");
            mv.visitVarInsn(Opcodes.ASTORE, arrIndex);
            mv.visitInsn(Opcodes.ARRAYLENGTH);
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitInsn(Opcodes.ISUB);
            final int limitIndex = mv.iSlot("limit");
            mv.visitVarInsn(Opcodes.ISTORE, limitIndex);
            mv.visitInsn(Opcodes.ICONST_0);
            final int iIndex = mv.iSlot("i");
            mv.visitVarInsn(Opcodes.ISTORE, iIndex);
            final int finalThrowableIndex = continueMode ? mv.aSlot("finalThrowable") : -1;
            if (continueMode) {
                mv.visitInsn(Opcodes.ACONST_NULL);
                mv.visitVarInsn(Opcodes.ASTORE, finalThrowableIndex);
            }
            final int finalResultIndex = 
                    continueMode && method.getReturnType() != void.class ? 
                    mv.slot("finalResult", method.getReturnType()) : 
                    -1;
            if (continueMode && method.getReturnType() != void.class) {
                mv.visitInsn(ASM.getDefaultCode(method.getReturnType()));
                mv.visitVarInsn(ASM.getStoreCode(method.getReturnType()), finalResultIndex);
            }
            mv.visitLabel(beginLoopLabel);
            mv.visitVarInsn(Opcodes.ILOAD, iIndex);
            mv.visitVarInsn(Opcodes.ILOAD, limitIndex);
            mv.visitJumpInsn(Opcodes.IF_ICMPGE, endLoopLabel);
            Action<XMethodVisitor> loopBodyAction = new Action<XMethodVisitor>() {
                @Override
                public void run(XMethodVisitor mv) {
                    mv.visitVarInsn(Opcodes.ALOAD, arrIndex);
                    mv.visitVarInsn(Opcodes.ILOAD, iIndex);
                    mv.visitInsn(Opcodes.AALOAD);
                    int slot = 1;
                    for (Class<?> parameterType : parameterTypes) {
                        mv.visitVarInsn(ASM.getLoadCode(parameterType), slot);
                        slot += ASM.getSlotCount(parameterType);
                    }
                    mv.visitMethodInsn(
                            Opcodes.INVOKEINTERFACE, 
                            interfaceType.getName().replace('.', '/'), 
                            method.getName(), 
                            methodDesc,
                            true);
                    if (method.getReturnType() != void.class) {
                        mv.visitInsn(ASM.getPopCode(method.getReturnType()));
                    }
                }
            };
            if (continueMode) {
                mv.visitTryCatchBlock(
                        loopBodyAction, 
                        new Catch(new Action<XMethodVisitor>() {
                            @Override
                            public void run(XMethodVisitor mv) {
                                mv.visitAStoreInsnIfNull(finalThrowableIndex);
                            }
                        }));
            } else {
                loopBodyAction.run(mv);
            }
            mv.visitIincInsn(iIndex, 1);
            mv.visitJumpInsn(Opcodes.GOTO, beginLoopLabel);
            mv.visitLabel(endLoopLabel);
            
            Action<XMethodVisitor> lastOneAction = new Action<XMethodVisitor>() {
                @Override
                public void run(XMethodVisitor mv) {
                    mv.visitVarInsn(Opcodes.ALOAD, arrIndex);
                    mv.visitVarInsn(Opcodes.ILOAD, iIndex);
                    mv.visitInsn(Opcodes.AALOAD);
                    int slot = 1;
                    for (Class<?> parameterType : parameterTypes) {
                        mv.visitVarInsn(ASM.getLoadCode(parameterType), slot);
                        slot += ASM.getSlotCount(parameterType);
                    }
                    mv.visitMethodInsn(
                            Opcodes.INVOKEINTERFACE, 
                            interfaceType.getName().replace('.', '/'), 
                            method.getName(), 
                            methodDesc,
                            true);
                    if (!continueMode) {
                        mv.visitInsn(ASM.getReturnCode(method.getReturnType()));
                    } else if (finalResultIndex != -1) {
                        mv.visitVarInsn(ASM.getStoreCode(method.getReturnType()), finalResultIndex);
                    }
                }
            };
            if (continueMode) {
                mv.visitTryCatchBlock(
                        lastOneAction, 
                        new Catch(new Action<XMethodVisitor>() {
                            @Override
                            public void run(XMethodVisitor mv) {
                                mv.visitAStoreInsnIfNull(finalThrowableIndex);
                            }
                        })
                );
                Label finalExceptionIsNullLabel = new Label();
                mv.visitVarInsn(Opcodes.ALOAD, finalThrowableIndex);
                mv.visitJumpInsn(Opcodes.IFNULL, finalExceptionIsNullLabel);
                mv.visitVarInsn(Opcodes.ALOAD, finalThrowableIndex);
                mv.visitInsn(Opcodes.ATHROW);
                mv.visitLabel(finalExceptionIsNullLabel);
                if (finalResultIndex != -1) {
                    mv.visitVarInsn(ASM.getLoadCode(method.getReturnType()), finalResultIndex);
                }
                mv.visitInsn(ASM.getReturnCode(method.getReturnType()));
            } else {
                lastOneAction.run(mv);
            }
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateHashCode(ClassVisitor cv) {
            String arrDesc = '[' + this.interfaceDescriptor;
            MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "hashCode", "()I", null, null);
            mv.visitCode();
            
            int hashIndex = 1;
            int arrIndex = 2;
            int sizeIndex = 3;
            int iIndex = 4;
            Label beginLoop = new Label();
            Label endLoop = new Label();
            
            /*
             * int hash = 0;
             * ?[] arr = this.arr;
             * int size = arr.length;
             * for (int i = 0; i < size; i++) {
             *      hash = 31 * hash + arr[i].hashCode();
             * }
             * return hash;
             */
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitVarInsn(Opcodes.ISTORE, hashIndex);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, this.internalName, "arr", arrDesc);
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ASTORE, arrIndex);
            mv.visitInsn(Opcodes.ARRAYLENGTH);
            mv.visitVarInsn(Opcodes.ISTORE, sizeIndex);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitVarInsn(Opcodes.ISTORE, iIndex);
            mv.visitLabel(beginLoop);
            mv.visitVarInsn(Opcodes.ILOAD, iIndex);
            mv.visitVarInsn(Opcodes.ILOAD, sizeIndex);
            mv.visitJumpInsn(Opcodes.IF_ICMPGE, endLoop);
            mv.visitVarInsn(Opcodes.ALOAD, arrIndex);
            mv.visitVarInsn(Opcodes.ILOAD, iIndex);
            mv.visitInsn(Opcodes.AALOAD);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    "java/lang/Object", 
                    "hashCode", 
                    "()I",
                    false);
            mv.visitLdcInsn(31);
            mv.visitVarInsn(Opcodes.ILOAD, hashIndex);
            mv.visitInsn(Opcodes.IMUL);
            mv.visitInsn(Opcodes.IADD);
            mv.visitVarInsn(Opcodes.ISTORE, hashIndex);
            mv.visitIincInsn(iIndex, 1);
            mv.visitJumpInsn(Opcodes.GOTO, beginLoop);
            mv.visitLabel(endLoop);
            mv.visitVarInsn(Opcodes.ILOAD, hashIndex);
            mv.visitInsn(Opcodes.IRETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateEquals(ClassVisitor cv) {
            
            String arrDesc = '[' + this.interfaceDescriptor;
            String hasBeenCombinedInternalName = HasBeenCombined.class.getName().replace('.', '/');
            
            MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "equals", "(Ljava/lang/Object;)Z", null, null);
            mv.visitCode();
            
            Label notSame = new Label();
            Label typeMatched = new Label();
            Label lengthMatched = new Label();
            Label beginLoop = new Label();
            Label endLoop = new Label();
            Label valueMatched = new Label();
            int otherIndex = 2;
            int arrIndex = 3;
            int iIndex = 4;
            
            /*
             * if (this == o) {
             *      return true;
             * }
             */
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitJumpInsn(Opcodes.IF_ACMPNE, notSame);
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitLabel(notSame);
            
            /*
             * if (!(o instanceof HasBeenCombined<?>)) {
             *      return false;
             * }
             * other = (HasBeenCombined<?>)o;
             */
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitTypeInsn(Opcodes.INSTANCEOF, hasBeenCombinedInternalName);
            mv.visitJumpInsn(Opcodes.IFNE, typeMatched);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitLabel(typeMatched);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitTypeInsn(Opcodes.CHECKCAST, hasBeenCombinedInternalName);
            mv.visitVarInsn(Opcodes.ASTORE, otherIndex);
            
            /*
             * arr = this.arr;
             * i = arr.length;
             * if (i != other.size()) {
             *    return fasle;
             * }
             */
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, this.internalName, "arr", arrDesc);
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ASTORE, arrIndex);
            mv.visitInsn(Opcodes.ARRAYLENGTH);
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ISTORE, iIndex);
            mv.visitVarInsn(Opcodes.ALOAD, otherIndex);
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    hasBeenCombinedInternalName, 
                    "size", 
                    "()I",
                    true);
            mv.visitJumpInsn(Opcodes.IF_ICMPEQ, lengthMatched);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitLabel(lengthMatched);
            
            /*
             * while (--i >= 0) {
             *      if (!arr[i].equals(other.get(i)) {
             *          return false;
             *      }
             * }
             * return true;
             */
            mv.visitLabel(beginLoop);
            mv.visitIincInsn(iIndex, -1);
            mv.visitVarInsn(Opcodes.ILOAD, iIndex);
            mv.visitJumpInsn(Opcodes.IFLT, endLoop);
            mv.visitVarInsn(Opcodes.ALOAD, arrIndex);
            mv.visitVarInsn(Opcodes.ILOAD, iIndex);
            mv.visitInsn(Opcodes.AALOAD);
            mv.visitVarInsn(Opcodes.ALOAD, otherIndex);
            mv.visitVarInsn(Opcodes.ILOAD, iIndex);
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    hasBeenCombinedInternalName, 
                    "get", 
                    "(I)Ljava/lang/Object;", 
                    true);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    "java/lang/Object", 
                    "equals", 
                    "(Ljava/lang/Object;)Z", 
                    false);
            mv.visitJumpInsn(Opcodes.IFNE, valueMatched);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitLabel(valueMatched);
            mv.visitJumpInsn(Opcodes.GOTO, beginLoop);
            mv.visitLabel(endLoop);
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitInsn(Opcodes.IRETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateToString(ClassVisitor cv) {
            String arrDesc = '[' + this.interfaceDescriptor;
            String stringBuilderInternalName = StringBuilder.class.getName().replace('.', '/');
            String appendStringDesc = "(Ljava/lang/String;)L" + stringBuilderInternalName + ";";
            String appendObjectDesc = "(Ljava/lang/Object;)L" + stringBuilderInternalName + ";";
            
            MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "toString", "()Ljava/lang/String;", null, null);
            mv.visitCode();
            
            int isFirstIndex = 1;
            int arrIndex = 2;
            int sizeIndex = 3;
            int iIndex = 4;
            Label beginLoop = new Label();
            Label endLoop = new Label();
            Label isFirst = new Label();
            Label endIf = new Label();
            
            /*
             * ?[] arr = this.arr;
             * int size = arr.length;
             * isFirst = true;
             * i = 0;
             */
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, this.internalName, "arr", arrDesc);
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ASTORE, arrIndex);
            mv.visitInsn(Opcodes.ARRAYLENGTH);
            mv.visitVarInsn(Opcodes.ISTORE, sizeIndex);
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitVarInsn(Opcodes.ISTORE, isFirstIndex);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitVarInsn(Opcodes.ISTORE, iIndex);
            
            /*
             * StringBuilder stringBuilder = new StringBuilder(this.getClass().getName() + " {");
             * while (i < size) {
             *      if (isFirst) {
             *          isFirst = false;
             *      } else {
             *          stringBuilder.append(", ");
             *      }
             *      stringBuilder.append(arr[i]);
             *      i++;
             * }
             * return stringBuilder.append(" }").toString();
             */
            mv.visitTypeInsn(Opcodes.NEW, stringBuilderInternalName);
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn(this.className + "{ ");
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    stringBuilderInternalName, 
                    "<init>", 
                    "(Ljava/lang/String;)V",
                    false);
            mv.visitLabel(beginLoop);
            mv.visitVarInsn(Opcodes.ILOAD, iIndex);
            mv.visitVarInsn(Opcodes.ILOAD, sizeIndex);
            mv.visitJumpInsn(Opcodes.IF_ICMPGE, endLoop);
            mv.visitVarInsn(Opcodes.ILOAD, isFirstIndex);
            mv.visitJumpInsn(Opcodes.IFNE, isFirst);
            mv.visitLdcInsn(", ");
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    stringBuilderInternalName, 
                    "append", 
                    appendStringDesc,
                    false);
            mv.visitJumpInsn(Opcodes.GOTO, endIf);
            mv.visitLabel(isFirst);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitVarInsn(Opcodes.ISTORE, isFirstIndex);
            mv.visitLabel(endIf);
            mv.visitVarInsn(Opcodes.ALOAD, arrIndex);
            mv.visitVarInsn(Opcodes.ILOAD, iIndex);
            mv.visitInsn(Opcodes.AALOAD);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    stringBuilderInternalName, 
                    "append", 
                    appendObjectDesc,
                    false);
            mv.visitIincInsn(iIndex, 1);
            mv.visitJumpInsn(Opcodes.GOTO, beginLoop);
            mv.visitLabel(endLoop);
            mv.visitLdcInsn(" }");
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    stringBuilderInternalName, 
                    "append", 
                    appendStringDesc,
                    false);
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
    }
    
    private interface Resource {
        String defaultChainInvocationExceptionHandleModesAreConflict(
                Class<?> interfaceType,
                Class<DefaultChainInvocationExceptionHandleMode> defaultChainInvocationExceptionHandleModeType,
                Class<?> superInterfaceType1,
                Class<?> superInterfaceType2);
    }
    
}
