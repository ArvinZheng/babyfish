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
package org.babyfish.util;

import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.NavigableSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.babyfish.lang.Action;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.lang.IllegalResourceException;
import org.babyfish.lang.NullArgumentException;
import org.babyfish.lang.Nulls;
import org.babyfish.lang.UncheckedException;
import org.babyfish.lang.reflect.ClassInfo;
import org.babyfish.lang.reflect.MemberInfo;
import org.babyfish.lang.reflect.asm.ASM;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.Label;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;

//This class can not depends on babyfish collections, 
//becasue babyfish collection depends on this class, 
//so the it used java.util..., not org.babyfish.collection...
//It can not use XMethodVisitor, Arguments too
/**
 * @author Tao Chen
 */
public class Resources {
    
    private static final Map<Class<?>, Map<Locale, SoftReference<?>>> INSTANCE_CACHE = new WeakHashMap<>();
    
    private static final Map<Class<?>, Class<?>> TYPE_CACHE = new WeakHashMap<>();
            
    private static final ReadWriteLock CACHE_LOCK = new ReentrantReadWriteLock();
    
    private static final String PROXY_CLASS_NAME_PREFIX = 
            "{Resouece:F3AC4D52_5740_4b19_9FAE_2D2C15EBDD82_Resource}";
    
    private static final String RESOURCE_BUNDLE_NAME = "{resourceBundle}";
    
    private static final String TO_STRING_NAME = "{toString}";
    
    private static final String PARAMTER_PATTERN_NAME = "{PARAMTER_PATTERN}";
    
    private static final String PARAMETER_REGEXP = "\\{\\d+\\}";
    
    private static final Pattern PARAMTER_PATTERN = Pattern.compile(PARAMETER_REGEXP);
    
    private static final Pattern SEMI_PATTERN = Pattern.compile(";");
    
    private static final Pattern COMMA_PATTERN = Pattern.compile("\\,");
    
    private static final Pattern DOT_PATTERN = Pattern.compile("\\.");
    
    private static final String LOAD_RESOURCE_LOCALE = 
            Resources.class.getName() + 
            ".LOAD_RESOURCE_LOCALE";
    
    private static final LoadLocaleNode ROOT_LOAD_LOCALE_NODE;
        
    protected Resources() {
        throw new UnsupportedOperationException();
    }
    
    public static <R> R of(Class<R> resourceType) {
        return of(resourceType, Locale.getDefault());
    }
    
    public static <R> R of(Class<R> resourceType, Locale locale) {
        if (ROOT_LOAD_LOCALE_NODE != null) {
            String packageName = resourceType.getPackage() != null ? resourceType.getPackage().getName() : null;
            Set<Locale> locales = ROOT_LOAD_LOCALE_NODE.get(packageName).getLocales();
            for (Locale eachLocale : locales) {
                getResource(resourceType, eachLocale);
            }
        }
        return getResource(resourceType, locale);
    }
    
    @SuppressWarnings("unchecked")
    private static <R> R getResource(Class<R> resourceType, Locale locale) {
        ExceptionUtil.mustNotBeNull("resourceType", resourceType);
        ExceptionUtil.mustNotBeNull("locale", "locale");
        Lock lock;
        Map<Locale, SoftReference<?>> map;
        Object resource = null;
        (lock = CACHE_LOCK.readLock()).lock(); // 1st locking
        try {
            map = INSTANCE_CACHE.get(resourceType);
            if (map != null) {
                SoftReference<?> softReference = map.get(locale);
                if (softReference != null) {
                    resource = softReference.get();
                }
            }
        } finally {
            lock.unlock();
        }
        
        if (resource == null) { // 1st checking
            (lock = CACHE_LOCK.writeLock()).lock();
            try {
                map = INSTANCE_CACHE.get(resourceType);
                if (map != null) {
                    SoftReference<?> softReference = map.get(locale);
                    if (softReference != null) {
                        resource = softReference.get();
                    }
                }
                if (resource == null) {
                    validate(resourceType, locale);
                    Class<?> resourceImplType = getResourceImplType(resourceType);
                    Constructor<R> constructor;
                    try {
                        constructor = (Constructor<R>)resourceImplType.getConstructor(Locale.class);
                    } catch (NoSuchMethodException ex) {
                        throw UncheckedException.rethrow(ex);
                    }
                    try {
                        resource = constructor.newInstance(locale);
                    } catch (InvocationTargetException ex) {
                        throw UncheckedException.rethrow(ex.getTargetException());
                    } catch (InstantiationException | IllegalAccessException ex) {
                        throw UncheckedException.rethrow(ex);
                    }
                    if (map == null) {
                        map = new HashMap<>();
                        INSTANCE_CACHE.put(resourceType, map);
                    }
                    map.put(locale, new SoftReference<>(resource));
                }
            } finally {
                lock.unlock();
            }
        }
        
        return (R)resource;
    }
    
    private static Class<?> getResourceImplType(Class<?> resourceType) {
        Class<?> resourceImplType = TYPE_CACHE.get(resourceType); //2nd reading
        if (resourceImplType == null) { //2nd checking
            resourceImplType = createResourceImplType(resourceType);
            TYPE_CACHE.put(resourceType, resourceImplType);
        }
        
        return resourceImplType;
    }
    
    private static void validate(Class<?> resourceType, Locale locale) {
        if (!resourceType.isInterface()) {
            throw new IllegalArgumentException("resourceType must be interface");
        }
        if (resourceType.getTypeParameters().length != 0) {
            throw new IllegalProgramException(
                    ExceptionUtil.resourceTypeMustHaveNoTypeParameters(resourceType)
            );
        }
        String resourceName = resourceType.getName().replace('.', '/');
        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle(resourceName, locale);
        } catch (MissingResourceException ex) {
            throw new IllegalArgumentException("Can not find the resource: " + resourceName);
        }
        
        Locale actualLocale = bundle.getLocale();
        if (!actualLocale.equals(locale)) {
            Map<Locale, ?> map = INSTANCE_CACHE.get(resourceType);
            if (map != null && map.containsKey(actualLocale)) {
                //Important optimization!
                return;
            }
        }
        
        Set<String> methodNames = new HashSet<>();
        for (Method method : resourceType.getMethods()) {
            if (!bundle.containsKey(method.getName())) {
                throw new IllegalResourceException(
                        "Failed to resolve the method \"" +
                        method.getName() +
                        "\" of the resoruce interface \"" +
                        resourceType.getName() + 
                        "\" because the key that equals the method name is not declared in the resource file \"" +
                        bundle.getLocale() +
                        "\".");
            }
            if (!methodNames.add(method.getName())) {
                throw new IllegalResourceException(
                        "Failed to resolve the the resoruce interface \"" +
                        "because it has serveral methods with a same name \"" +
                        method.getName() +
                        "\"");
            }
        }
        Set<String> keyNames = new HashSet<>();
        for (Enumeration<String> keyEnumeration = bundle.getKeys(); keyEnumeration.hasMoreElements();) {
            String key = keyEnumeration.nextElement();
            if (!methodNames.contains(key)) {
                throw new IllegalResourceException(
                        "Failed to resolve the key \"" +
                        key +
                        "\" of the resoruce file \"" +
                        resouceFileName(resourceName, bundle.getLocale()) +
                        "\" because the method whose name equals the key is not declared in the resoruce interface.");
            }
            if (!keyNames.add(key)) {
                throw new IllegalResourceException(
                        "Failed to the resoruce file \"" +
                        resouceFileName(resourceName, bundle.getLocale()) +
                        "\" because it has several message with a same key \"" +
                        key +
                        "\"");
            }
        }
        for (Method method : resourceType.getMethods()) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            NavigableSet<Integer> parameterIndexes = new TreeSet<Integer>();
            String message = bundle.getString(method.getName());
            Matcher matcher = PARAMTER_PATTERN.matcher(message);
            while (matcher.find()) {
                int parameterIndex = Integer.parseInt(message.substring(matcher.start() + 1, matcher.end() - 1));
                parameterIndexes.add(parameterIndex);
            }
            if (!parameterIndexes.isEmpty()) {
                Integer prev = parameterIndexes.first();
                if (prev.intValue() != 0) {
                    throw new IllegalResourceException(
                            "Failed to resolve the key \"" +
                            method.getName() +
                            "\" of the resoruce file \"" +
                            resouceFileName(resourceName, locale) +
                            "\" because it miss the parameter \"{0}\"");
                }
                while (true) {
                    Integer cur = parameterIndexes.higher(prev);
                    if (cur == null) {
                        break;
                    }
                    if (cur.intValue() != prev.intValue() + 1) {
                        throw new IllegalResourceException(
                                "Failed to resolve the key \"" +
                                method.getName() +
                                "\" of the resoruce file \"" +
                                resouceFileName(resourceName, locale) +
                                "\" because it contains the parameter \"" +
                                cur.intValue() +
                                "\" but does not contain the parameter \"{" +
                                (prev.intValue() + 1) +
                                "}\"");
                    }
                    prev = cur;
                }
            }
            if (parameterIndexes.size() != parameterTypes.length) {
                throw new IllegalResourceException(
                        "Failed to resolve the key \"" +
                        method.getName() +
                        "\" of the resoruce file \"" +
                        resouceFileName(resourceName, locale) +
                        "\" because " +
                        " the parameter count of resoruce file is " +
                        parameterIndexes.size() +
                        " but the parameter count of the resource interface is " +
                        parameterTypes.length);
            }
        }
    }
    
    private static Class<?> createResourceImplType(final Class<?> resourceType) {
        final String proxyClassName = resourceType.getName() + PROXY_CLASS_NAME_PREFIX;
        Action<ClassVisitor> cvAction = new Action<ClassVisitor>() {
            @Override
            public void run(ClassVisitor cv) {
                generateResource(cv, resourceType, proxyClassName);
            }
            
        };
        return ASM.loadDynamicClass(
                    resourceType.getClassLoader(), 
                    proxyClassName, 
                    resourceType.getProtectionDomain(),
                    cvAction);
    }
    
    private static void generateResource(
            ClassVisitor cw, 
            Class<?> resourceType, 
            String proxyClassName) {
        if (!resourceType.isInterface()) {
            throw new IllegalArgumentException("resourceType must be interface");
        }
        if (resourceType.getTypeParameters().length != 0) {
            throw new IllegalProgramException(
                    ExceptionUtil.resourceTypeMustHaveNoTypeParameters(resourceType)
            );
        }
        String resourceName = resourceType.getName().replace('.', '/');
        cw.visit(
                Opcodes.V1_7,
                Opcodes.ACC_PUBLIC, 
                proxyClassName.replace('.', '/'), 
                null,
                "java/lang/Object",
                new String[] { resourceType.getName().replace('.', '/') });
        
        /*
         * private static final Pattern {PARAMTER_PATTERN};
         */
        cw.visitField(
                Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, 
                PARAMTER_PATTERN_NAME, 
                ASM.getDescriptor(Pattern.class), 
                null,
                null);
        
        /*
         * private ResourceBundle {resoruceBundle};
         */
        cw.visitField(
                Opcodes.ACC_PRIVATE, 
                RESOURCE_BUNDLE_NAME, 
                ASM.getDescriptor(ResourceBundle.class), 
                null,
                null
        ).visitEnd();
        
        /*
         * static {
         *      {PARAMTER_PATTERN} = Pattern.compile("\\{\\d+\\}");
         * }
         */
        MethodVisitor mv = cw.visitMethod(
                Opcodes.ACC_STATIC, 
                "<clinit>", 
                "()V", 
                null, 
                null);
        mv.visitCode();
        mv.visitLdcInsn(PARAMETER_REGEXP);
        mv.visitMethodInsn(
                Opcodes.INVOKESTATIC, 
                ASM.getInternalName(Pattern.class), 
                "compile", 
                "(Ljava/lang/String;)" +
                ASM.getDescriptor(Pattern.class),
                false);
        mv.visitFieldInsn(
                Opcodes.PUTSTATIC, 
                proxyClassName.replace('.', '/'), 
                PARAMTER_PATTERN_NAME, 
                ASM.getDescriptor(Pattern.class));
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        
        /*
         * public ??(Locale locale) {
         *      super();
         *      this.{resourceBundle} = ResourceBundle.getBundle(???, locale);
         * }
         */
        mv = cw.visitMethod(
                Opcodes.ACC_PUBLIC, 
                "<init>", 
                '(' +
                ASM.getDescriptor(Locale.class) +
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
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitLdcInsn(resourceName);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(
                Opcodes.INVOKESTATIC, 
                ASM.getInternalName(ResourceBundle.class), 
                "getBundle", 
                "(Ljava/lang/String;" +
                ASM.getDescriptor(Locale.class) +
                ')' + ASM.getDescriptor(ResourceBundle.class),
                false);
        mv.visitFieldInsn(
                Opcodes.PUTFIELD,
                proxyClassName.replace('.', '/'), 
                RESOURCE_BUNDLE_NAME, 
                ASM.getDescriptor(ResourceBundle.class));
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        
        generateToString(cw, proxyClassName);
        
        for (Method method : resourceType.getMethods()) {
            generateProperty(cw, method, resourceName, proxyClassName);
        }
        cw.visitEnd();
    }
    
    private static void generateToString(ClassVisitor cv, String className) {
        /*
         * private static String {toString}(Object o) {
         *      if (o == null) {
         *          return "null";
         *      }
         *      if (o instanceof String) {
         *          return (String)o;
         *      }
         *      if (o.getClass().isArray()) {
         *          Object[] arr = (Object)o;
         *          int len = arr.length;
         *          bool addComma = false;
         *          StringBuilder builder = new StringBuilder();
         *          for (int i = 0; i < len; i++) {
         *              if (!addComma) {
         *                  addComma = true;
         *              } else {
         *                  builder.append(", ");
         *              }
         *              builder.append({toString}(arr[i]));
         *          }
         *          return builder.toString();
         *      }
         *      if (o instanceof Iterable<?>) {
         *          bool addComma = false;
         *          StringBuilder builder = new StringBuilder();
         *          for (Object e : (Iterable<?>)o) {
         *              if (!addComma) {
         *                  addComma = true;
         *              } else {
         *                  builder.append(", ");
         *              }
         *              builder.append({toString}(e));
         *          }
         *          return builder.toString();
         *      }
         *      if (o instanceof Enum<?>) {
         *          return o.getClass().getName() + '.' + ((Enum<?>)o).name();
         *      }
         *      if (o instanceof Class<?>) {
         *          return ((Class<?>)o).getName();
         *      }
         *      if (o instanceof Constructor) {
         *          return ((Constructor)o).toGenericString();
         *      }
         *      if (o instanceof Method) {
         *          return ((Method)o).toGenericString();
         *      }
         *      if (o instanceof ClassInfo<?>) {
         *          return ((ClassInfo<?>)o).toGenericString();
         *      }
         *      if (o instanceof Field) {
         *          return ((Field)o).toGenericString();
         *      }
         *      return o.toString();
         * }
         */ 
        
        /*
         * Don't use XMethodVisitor mv = ASM.visitMethod...
         * because ASM depends on babyfish collection framework that depends on 
         * this module(Bad cycle dependency)
         */
        MethodVisitor mv = cv.visitMethod(
                Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, 
                TO_STRING_NAME, 
                "(Ljava/lang/Object;)Ljava/lang/String;", 
                null,
                null);
        mv.visitCode();
        
        Label isNotNullLabel = new Label();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitJumpInsn(Opcodes.IFNONNULL, isNotNullLabel);
        mv.visitLdcInsn("null");
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitLabel(isNotNullLabel);
        
        Label isNotStringLabel = new Label();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitTypeInsn(Opcodes.INSTANCEOF, "java/lang/String");
        mv.visitJumpInsn(Opcodes.IFEQ, isNotStringLabel);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/String");
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitLabel(isNotStringLabel);
        
        {
            final int arrIndex = 1;
            final int lenIndex = 2;
            final int addCommaIndex = 3;
            final int builderIndex = 4;
            Label isNotArrayLabel = new Label();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    "java/lang/Object", 
                    "getClass", 
                    "()Ljava/lang/Class;",
                    false);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    "java/lang/Class", 
                    "isArray", 
                    "()Z",
                    false);
            mv.visitJumpInsn(Opcodes.IFEQ, isNotArrayLabel);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitTypeInsn(Opcodes.CHECKCAST, "[Ljava/lang/Object;");
            mv.visitVarInsn(Opcodes.ASTORE, arrIndex);
            mv.visitVarInsn(Opcodes.ALOAD, arrIndex);
            mv.visitInsn(Opcodes.ARRAYLENGTH);
            mv.visitVarInsn(Opcodes.ISTORE, lenIndex);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitVarInsn(Opcodes.ISTORE, addCommaIndex);
            mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    "java/lang/StringBuilder", 
                    "<init>", 
                    "()V",
                    false);
            mv.visitVarInsn(Opcodes.ASTORE, builderIndex);
            {
                Label beginLoopLabel = new Label();
                Label endLoopLabel = new Label();
                final int iIndex = 5;
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitVarInsn(Opcodes.ISTORE, iIndex);
                mv.visitLabel(beginLoopLabel);
                mv.visitVarInsn(Opcodes.ILOAD, iIndex);
                mv.visitVarInsn(Opcodes.ILOAD, lenIndex);
                mv.visitJumpInsn(Opcodes.IF_ICMPGE, endLoopLabel);
                {
                    Label addCommaLabel = new Label();
                    Label endAddCommaLabel = new Label();
                    mv.visitVarInsn(Opcodes.ILOAD, addCommaIndex);
                    mv.visitJumpInsn(Opcodes.IFNE, addCommaLabel);
                    mv.visitInsn(Opcodes.ICONST_1);
                    mv.visitVarInsn(Opcodes.ISTORE, addCommaIndex);
                    mv.visitJumpInsn(Opcodes.GOTO, endAddCommaLabel);
                    mv.visitLabel(addCommaLabel);
                    mv.visitVarInsn(Opcodes.ALOAD, builderIndex);
                    mv.visitLdcInsn(", ");
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL, 
                            "java/lang/StringBuilder", 
                            "append", 
                            "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                            false);
                    mv.visitInsn(Opcodes.POP);
                    mv.visitLabel(endAddCommaLabel);
                }
                mv.visitVarInsn(Opcodes.ALOAD, builderIndex);
                mv.visitVarInsn(Opcodes.ALOAD, arrIndex);
                mv.visitVarInsn(Opcodes.ILOAD, iIndex);
                mv.visitInsn(Opcodes.AALOAD);
                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC, 
                        className.replace('.', '/'),
                        TO_STRING_NAME,
                        "(Ljava/lang/Object;)Ljava/lang/String;",
                        false);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        "java/lang/StringBuilder", 
                        "append", 
                        "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                        false);
                mv.visitInsn(Opcodes.POP);
                mv.visitIincInsn(iIndex, 1);
                mv.visitJumpInsn(Opcodes.GOTO, beginLoopLabel);
                mv.visitLabel(endLoopLabel);
            }
            mv.visitVarInsn(Opcodes.ALOAD, builderIndex);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    "java/lang/Object",
                    "toString",
                    "()Ljava/lang/String;",
                    false);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitLabel(isNotArrayLabel);
        }
        
        {
            final int itrIndex = 1;
            final int addCommaIndex = 3;
            final int builderIndex = 4;
            Label isNotIterableLabel = new Label();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitTypeInsn(Opcodes.INSTANCEOF, ASM.getInternalName(Iterable.class));
            mv.visitJumpInsn(Opcodes.IFEQ, isNotIterableLabel);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(Iterable.class));
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    ASM.getInternalName(Iterable.class), 
                    "iterator", 
                    "()" + ASM.getDescriptor(Iterator.class),
                    true);
            mv.visitVarInsn(Opcodes.ASTORE, itrIndex);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitVarInsn(Opcodes.ISTORE, addCommaIndex);
            mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    "java/lang/StringBuilder", 
                    "<init>", 
                    "()V",
                    false);
            mv.visitVarInsn(Opcodes.ASTORE, builderIndex);
            {
                Label beginLoopLabel = new Label();
                Label endLoopLabel = new Label();
                mv.visitLabel(beginLoopLabel);
                mv.visitVarInsn(Opcodes.ALOAD, itrIndex);
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        ASM.getInternalName(Iterator.class), 
                        "hasNext", 
                        "()Z",
                        true);
                mv.visitJumpInsn(Opcodes.IFEQ, endLoopLabel);
                {
                    Label addCommaLabel = new Label();
                    Label endAddCommaLabel = new Label();
                    mv.visitVarInsn(Opcodes.ILOAD, addCommaIndex);
                    mv.visitJumpInsn(Opcodes.IFNE, addCommaLabel);
                    mv.visitInsn(Opcodes.ICONST_1);
                    mv.visitVarInsn(Opcodes.ISTORE, addCommaIndex);
                    mv.visitJumpInsn(Opcodes.GOTO, endAddCommaLabel);
                    mv.visitLabel(addCommaLabel);
                    mv.visitVarInsn(Opcodes.ALOAD, builderIndex);
                    mv.visitLdcInsn(", ");
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL, 
                            "java/lang/StringBuilder", 
                            "append", 
                            "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                            false);
                    mv.visitInsn(Opcodes.POP);
                    mv.visitLabel(endAddCommaLabel);
                }
                mv.visitVarInsn(Opcodes.ALOAD, builderIndex);
                mv.visitVarInsn(Opcodes.ALOAD, itrIndex);
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        ASM.getInternalName(Iterator.class),
                        "next",
                        "()Ljava/lang/Object;",
                        true);
                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC, 
                        className.replace('.', '/'),
                        TO_STRING_NAME,
                        "(Ljava/lang/Object;)Ljava/lang/String;",
                        false);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        "java/lang/StringBuilder", 
                        "append", 
                        "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                        false);
                mv.visitInsn(Opcodes.POP);
                mv.visitJumpInsn(Opcodes.GOTO, beginLoopLabel);
                mv.visitLabel(endLoopLabel);
            }
            mv.visitVarInsn(Opcodes.ALOAD, builderIndex);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    "java/lang/Object",
                    "toString",
                    "()Ljava/lang/String;",
                    false);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitLabel(isNotIterableLabel);
        }
        
        Label isNotEnumLabel = new Label();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitTypeInsn(Opcodes.INSTANCEOF, "java/lang/Enum");
        mv.visitJumpInsn(Opcodes.IFEQ, isNotEnumLabel);
        mv.visitTypeInsn(
                Opcodes.NEW, 
                "java/lang/StringBuilder");
        mv.visitInsn(Opcodes.DUP);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL, 
                "java/lang/Object", 
                "getClass", 
                "()Ljava/lang/Class;",
                false);
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL, 
                "java/lang/Class", 
                "getName", 
                "()Ljava/lang/String;",
                false);
        mv.visitMethodInsn(
                Opcodes.INVOKESPECIAL, 
                "java/lang/StringBuilder", 
                "<init>", 
                "(Ljava/lang/String;)V",
                false);
        mv.visitLdcInsn((int)'.');
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL, 
                "java/lang/StringBuilder", 
                "append", 
                "(C)Ljava/lang/StringBuilder;",
                false);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Enum");
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL, 
                "java/lang/Enum", 
                "name", 
                "()Ljava/lang/String;",
                false);
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL, 
                "java/lang/StringBuilder", 
                "append", 
                "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                false);
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL, 
                "java/lang/StringBuilder", 
                "toString", 
                "()Ljava/lang/String;",
                false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitLabel(isNotEnumLabel);
        
        Label isNotClassLabel = new Label();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitTypeInsn(Opcodes.INSTANCEOF, "java/lang/Class");
        mv.visitJumpInsn(Opcodes.IFEQ, isNotClassLabel);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Class");
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL, 
                "java/lang/Class", 
                "getName", 
                "()Ljava/lang/String;",
                false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitLabel(isNotClassLabel);
        
        Label isNotConstructorLabel = new Label();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitTypeInsn(Opcodes.INSTANCEOF, ASM.getInternalName(Constructor.class));
        mv.visitJumpInsn(Opcodes.IFEQ, isNotConstructorLabel);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(Constructor.class));
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL, 
                ASM.getInternalName(Constructor.class),
                "toGenericString", 
                "()Ljava/lang/String;",
                false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitLabel(isNotConstructorLabel);
        
        Label isNotMethodLabel = new Label();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitTypeInsn(Opcodes.INSTANCEOF, ASM.getInternalName(Method.class));
        mv.visitJumpInsn(Opcodes.IFEQ, isNotMethodLabel);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(Method.class));
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL, 
                ASM.getInternalName(Method.class),
                "toGenericString", 
                "()Ljava/lang/String;",
                false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitLabel(isNotMethodLabel);
        
        Label isNotFieldLabel = new Label();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitTypeInsn(Opcodes.INSTANCEOF, ASM.getInternalName(Field.class));
        mv.visitJumpInsn(Opcodes.IFEQ, isNotFieldLabel);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(Field.class));
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL, 
                ASM.getInternalName(Field.class),
                "toGenericString", 
                "()Ljava/lang/String;",
                false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitLabel(isNotFieldLabel);
        
        Label isNotClassInfoLabel = new Label();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitTypeInsn(Opcodes.INSTANCEOF, ASM.getInternalName(ClassInfo.class));
        mv.visitJumpInsn(Opcodes.IFEQ, isNotClassInfoLabel);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(ClassInfo.class));
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL, 
                ASM.getInternalName(ClassInfo.class), 
                "toGenericString", 
                "()Ljava/lang/String;",
                false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitLabel(isNotClassInfoLabel);
        
        Label isNotMemberInfoLabel = new Label();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitTypeInsn(Opcodes.INSTANCEOF, ASM.getInternalName(MemberInfo.class));
        mv.visitJumpInsn(Opcodes.IFEQ, isNotMemberInfoLabel);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(MemberInfo.class));
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL, 
                ASM.getInternalName(MemberInfo.class),
                "toGenericString", 
                "()Ljava/lang/String;",
                false);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitLabel(isNotMemberInfoLabel);
        
        mv.visitVarInsn(Opcodes.ALOAD, 0);
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
    
    private static void generateProperty(
            ClassVisitor cv, 
            Method method,
            String resourceName,
            String className) {
        if (method.getTypeParameters().length != 0) {
            throw new IllegalProgramException(
                    ExceptionUtil.resourceMethodMustHaveNoParameters(method)
            );
        }
        if (String.class != method.getReturnType()) {
            throw new IllegalProgramException(
                    ExceptionUtil.resourceMethodMustReturnString(method)
            );
        }
        String keyName = method.getName();
        MethodVisitor mv = cv.visitMethod(
                Opcodes.ACC_PUBLIC, 
                keyName, 
                ASM.getDescriptor(method), 
                null,
                null);
        mv.visitCode();
        
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 0) {
            /*
             * return this.{resourceBundle}.getString(??);
             */
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    className.replace('.', '/'), 
                    RESOURCE_BUNDLE_NAME, 
                    ASM.getDescriptor(ResourceBundle.class));
            mv.visitLdcInsn(keyName);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    ASM.getInternalName(ResourceBundle.class), 
                    "getString",
                    "(Ljava/lang/String;)Ljava/lang/String;",
                    false);
            mv.visitInsn(Opcodes.ARETURN);
        } else {
            final int argumentsIndex = ASM.getSlotCount(method);
            final int messageIndex = argumentsIndex + 1;
            final int matcherIndex = messageIndex + 1;
            final int builderIndex = matcherIndex + 1;
            final int posIndex = builderIndex + 1;
            final int startIndex = posIndex + 1;
            final int endIndex = startIndex + 1;
            
            /*
             * Object[] arguments = new Object[] { argument1, argument2, ... argumentN };
             */
             
            mv.visitLdcInsn(parameterTypes.length);
            mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
            mv.visitVarInsn(Opcodes.ASTORE, argumentsIndex);
            int parameterSlot = 1;
            for (int i = 0; i < parameterTypes.length; i++) {
                mv.visitVarInsn(Opcodes.ALOAD, argumentsIndex);
                mv.visitLdcInsn(i);
                final Class<?> parameterType = parameterTypes[i];
                final int slot = parameterSlot;
                if (parameterType.isPrimitive()) {
                    String boxInternalName;
                    if (parameterType == char.class) {
                        boxInternalName = "java/lang/Character";
                    } else if (parameterType == int.class) {
                        boxInternalName = "java/lang/Integer";
                    } else {
                        boxInternalName =
                                "java/lang/" + 
                                Character.toUpperCase(parameterType.getName().charAt(0)) + 
                                parameterType.getName().substring(1);
                    }
                    mv.visitTypeInsn(Opcodes.NEW, boxInternalName);
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitVarInsn(ASM.getLoadCode(parameterType), slot);
                    mv.visitMethodInsn(
                            Opcodes.INVOKESPECIAL,
                            boxInternalName,
                            "<init>",
                            '(' +
                            ASM.getDescriptor(parameterType) +
                            ")V",
                            false);
                } else {
                    mv.visitVarInsn(ASM.getLoadCode(parameterType), slot);
                }
                mv.visitInsn(Opcodes.AASTORE);
                parameterSlot += ASM.getSlotCount(parameterType);
            }
            
            /*
             * String message = this.{resourceBundle}.getString(??);
             */
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    className.replace('.', '/'), 
                    RESOURCE_BUNDLE_NAME, 
                    ASM.getDescriptor(ResourceBundle.class));
            mv.visitLdcInsn(keyName);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    ASM.getInternalName(ResourceBundle.class), 
                    "getString",
                    "(Ljava/lang/String;)Ljava/lang/String;",
                    false);
            mv.visitVarInsn(Opcodes.ASTORE, messageIndex);
            
            /*
             * Matcher matcher = {PARAMTER_PATTERN}.matcher(message);
             */
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    className.replace('.', '/'), 
                    PARAMTER_PATTERN_NAME, 
                    ASM.getDescriptor(Pattern.class));
            mv.visitVarInsn(Opcodes.ALOAD, messageIndex);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    ASM.getInternalName(Pattern.class),
                    "matcher",
                    '(' +
                    ASM.getDescriptor(CharSequence.class) +
                    ')' +
                    ASM.getDescriptor(Matcher.class),
                    false);
            mv.visitVarInsn(Opcodes.ASTORE, matcherIndex);
            
            /*
             * StringBuilder builder = new StringBuilder();
             */
            mv.visitTypeInsn(Opcodes.NEW, ASM.getInternalName(StringBuilder.class));
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    ASM.getInternalName(StringBuilder.class),
                    "<init>",
                    "()V",
                    false);
            mv.visitVarInsn(Opcodes.ASTORE, builderIndex);
            
            /*
             * int pos = 0;
             */
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitVarInsn(Opcodes.ISTORE, posIndex);
            
            /*
             * while (matcher.find()) {
             *      int start = matcher.start();
             *      int end = matcher.end();
             *      if (pos != start) {
             *          builder.append(message.substring(pos, start));
             *      }
             *      builder.append({toString}(arguments[Integer.parseInt(message.substring(start, end))]));
             *      pos = end;
             * }
             */ {
                Label beginLoopLabel = new Label();
                Label endLoopLevel = new Label();
                mv.visitLabel(beginLoopLabel);
                mv.visitVarInsn(Opcodes.ALOAD, matcherIndex);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        ASM.getInternalName(Matcher.class), 
                        "find", 
                        "()Z",
                        false);
                mv.visitJumpInsn(Opcodes.IFEQ, endLoopLevel);
                
                //int start = matcher.start();
                mv.visitVarInsn(Opcodes.ALOAD, matcherIndex);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        ASM.getInternalName(Matcher.class), 
                        "start", 
                        "()I",
                        false);
                mv.visitVarInsn(Opcodes.ISTORE, startIndex);
                
                //int end = matcher.end();
                mv.visitVarInsn(Opcodes.ALOAD, matcherIndex);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        ASM.getInternalName(Matcher.class), 
                        "end", 
                        "()I",
                        false);
                mv.visitVarInsn(Opcodes.ISTORE, endIndex);
                
                //if (pos != start) {
                //      builder.append(message.substring(pos, start));
                //}
                mv.visitVarInsn(Opcodes.ILOAD, posIndex);
                mv.visitVarInsn(Opcodes.ILOAD, startIndex);
                Label posEqStartLabel = new Label();
                mv.visitJumpInsn(Opcodes.IF_ICMPEQ, posEqStartLabel);
                mv.visitVarInsn(Opcodes.ALOAD, builderIndex);
                mv.visitVarInsn(Opcodes.ALOAD, messageIndex);
                mv.visitVarInsn(Opcodes.ILOAD, posIndex);
                mv.visitVarInsn(Opcodes.ILOAD, startIndex);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        "java/lang/String", 
                        "substring", 
                        "(II)Ljava/lang/String;",
                        false);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        ASM.getInternalName(StringBuilder.class), 
                        "append", 
                        "(Ljava/lang/String;)" + ASM.getDescriptor(StringBuilder.class),
                        false);
                mv.visitInsn(Opcodes.POP);
                mv.visitLabel(posEqStartLabel);
                
                //builder.append({toString}(arguments[Integer.parseInt(message.substring(start, end))]));
                mv.visitVarInsn(Opcodes.ALOAD, builderIndex);
                mv.visitVarInsn(Opcodes.ALOAD, argumentsIndex);
                mv.visitVarInsn(Opcodes.ALOAD, messageIndex);
                mv.visitVarInsn(Opcodes.ILOAD, startIndex);
                mv.visitInsn(Opcodes.ICONST_1);
                mv.visitInsn(Opcodes.IADD);
                mv.visitVarInsn(Opcodes.ILOAD, endIndex);
                mv.visitInsn(Opcodes.ICONST_1);
                mv.visitInsn(Opcodes.ISUB);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        "java/lang/String", 
                        "substring", 
                        "(II)Ljava/lang/String;",
                        false);
                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC, 
                        "java/lang/Integer", 
                        "parseInt", 
                        "(Ljava/lang/String;)I",
                        false);
                mv.visitInsn(Opcodes.AALOAD);
                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC, 
                        className.replace('.', '/'), 
                        TO_STRING_NAME, 
                        "(Ljava/lang/Object;)Ljava/lang/String;",
                        false);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        ASM.getInternalName(StringBuilder.class), 
                        "append", 
                        "(Ljava/lang/String;)" + ASM.getDescriptor(StringBuilder.class),
                        false);
                mv.visitInsn(Opcodes.POP);

                //pos = end;
                mv.visitVarInsn(Opcodes.ILOAD, endIndex);
                mv.visitVarInsn(Opcodes.ISTORE, posIndex);
                
                mv.visitJumpInsn(Opcodes.GOTO, beginLoopLabel);
                mv.visitLabel(endLoopLevel);
            }
             
            /*
             * return builder.append(message.substring(pos)).toString();
             */
            mv.visitVarInsn(Opcodes.ALOAD, builderIndex);
            mv.visitVarInsn(Opcodes.ALOAD, messageIndex);
            mv.visitVarInsn(Opcodes.ILOAD, posIndex);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    "java/lang/String", 
                    "substring", 
                    "(I)Ljava/lang/String;",
                    false);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    ASM.getInternalName(StringBuilder.class), 
                    "append", 
                    "(Ljava/lang/String;)" + ASM.getDescriptor(StringBuilder.class),
                    false);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    ASM.getInternalName(StringBuilder.class), 
                    "toString", 
                    "()Ljava/lang/String;",
                    false);
            mv.visitInsn(Opcodes.ARETURN);
        }
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private static String resouceFileName(String resourceName, Locale locale) {
        if (locale != null) {
            return resourceName + '_' + locale + ".properties";
        }
        return resourceName + ".properties";
    }
    
    private static class LoadLocaleNode {
        
        /*
         * Before finish() is invoked, if it is not null
         *      It only contains the locales of itself
         *      It is mutable
         * After finish() is invoked, if it is not null
         *      It contains the locales of itself and the locales inherited from parent nodes
         *      It is immutable
         */
        Set<Locale> locales;
        
        /*
         * Before finish() is invoked, if it is not null
         *      It is mutable
         * After finish() is invoked, if it is not null
         *      It is immutable
         */
        Map<String, LoadLocaleNode> childNodes;
        
        private LoadLocaleNode() {
        }
        
        static LoadLocaleNode of(String loadResourceLocale) {
            loadResourceLocale = Nulls.trim(loadResourceLocale);
            if (Nulls.isNullOrEmpty(loadResourceLocale)) {
                return null;
            }
            LoadLocaleNode rootNode = new LoadLocaleNode();
            for (String assignment : SEMI_PATTERN.split(loadResourceLocale)) {
                assignment = assignment.trim();
                if (!assignment.isEmpty()) {
                    int eqIndex = assignment.indexOf('=');
                    String packageName = assignment.substring(0, eqIndex).trim();
                    String locales = assignment.substring(eqIndex + 1).trim();
                    rootNode.get(false, packageName).setLocales(locales);
                }
            }
            rootNode.finish(null);
            return rootNode;
        }
        
        LoadLocaleNode get(String packageName) {
            return this.get(true, packageName);
        }
        
        Set<Locale> getLocales() {
            if (this.locales == null) {
                return Collections.emptySet();
            }
            return this.locales;
        }
        
        private LoadLocaleNode get(boolean readOnly, String packageName) {
            packageName = Nulls.trim(packageName);
            if (Nulls.isNullOrEmpty(packageName)) {
                return this;
            }
            String[] packageNodeNames = DOT_PATTERN.split(packageName);
            return this.get(readOnly, packageNodeNames, 0);
        }
        
        private LoadLocaleNode get(boolean readOnly, String[] arr, int index) {
            if (index >= arr.length) {
                return this;
            }
            String packageNodeName = arr[index].trim();
            LoadLocaleNode childNode = null;
            if (this.childNodes == null) {
                if (readOnly) {
                    return this;
                }
                this.childNodes = new HashMap<>();
            } else {
                childNode = this.childNodes.get(packageNodeName);
            }
            if (childNode == null) {
                if (readOnly) {
                    return this;
                }
                childNode = new LoadLocaleNode();
                this.childNodes.put(packageNodeName, childNode);
            }
            return childNode.get(readOnly, arr, index + 1);
        }
        
        private void setLocales(String locales) {
            locales = Nulls.trim(locales);
            if (Nulls.isNullOrEmpty(locales)) {
                return;
            }
            for (String str : COMMA_PATTERN.split(locales)) {
                str = str.trim();
                if (str.equals("null")) {
                    this.addLocale(new Locale("", ""));
                } else {
                    String language;
                    String country;
                    int ulIndex = str.indexOf('_');
                    if (ulIndex == -1) {
                        language = str.toLowerCase();
                        country = "";
                    } else {
                        language = str.substring(0, ulIndex).toLowerCase();
                        if (ulIndex == str.length() - 1) {
                            country = "";
                        } else {
                            country = str.substring(ulIndex + 1).toUpperCase();   
                        }
                    }
                    this.addLocale(new Locale(language, country));
                }
            }
        }
        
        private void addLocale(Locale locale) {
            Set<Locale> set = this.locales;
            if (set == null) {
                this.locales = set = new HashSet<>();
            }
            set.add(locale);
        }
        
        private void finish(LoadLocaleNode parentNode) {
            int localeCount = this.locales != null ? this.locales.size() : 0;
            if (parentNode != null && parentNode.locales != null) {
                localeCount += parentNode.locales.size();
            }
            if (localeCount != 0) {
                Set<Locale> set = new HashSet<>((localeCount * 4 + 2) / 3);
                if (this.locales != null) {
                    set.addAll(this.locales);
                }
                if (parentNode != null && parentNode.locales != null) {
                    set.addAll(parentNode.locales);
                }
                this.locales = Collections.unmodifiableSet(set);
            }
            if (this.childNodes != null) {
                this.childNodes = Collections.unmodifiableMap(this.childNodes);
                for (LoadLocaleNode childNode : this.childNodes.values()) {
                    childNode.finish(this);
                }
            }
        }
  }
    
    /*
     * This class can not use org.babyfish.lang.Arguments, so use this class
     */
    private static class ExceptionUtil {
        
        private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(ExceptionUtil.class.getName()); 
        
        static void mustNotBeNull(String parameterName, Object argument) {
            if (argument == null) {
                throw new NullArgumentException(get("mustNotBeNull", parameterName));
            }
        }
        
        public static String resourceTypeMustHaveNoTypeParameters(Class<?> resourceType) {
            return get("resourceTypeMustHaveNoTypeParameters", resourceType);
        }
        
        public static String resourceMethodMustHaveNoParameters(Method method) {
            return get("resourceMethodMustHaveNoParameters", method);
        }

        public static String resourceMethodMustReturnString(Method method) {
            return get("resourceMethodMustReturnString", method);
        }

        private static String get(String key, Object ... args) {
            int size = args.length;
            String value = BUNDLE.getString(key);
            for (int i = 0; i < size; i++) {
                Object o = args[i];
                value = value.replaceAll("\\{" + i + "\\}", o != null ? o.toString() : "null");
            }
            return value;
        }
    }
    
    static {
        String loadResourceLocale = Nulls.trim(System.getProperty(LOAD_RESOURCE_LOCALE));
        if (Nulls.isNullOrEmpty(loadResourceLocale)) {
            ROOT_LOAD_LOCALE_NODE = null;
        } else {
            String comma = "\\s*\\,\\s*";
            String semi = "\\s*;\\s*";
            String dot = "\\s*\\.\\s*";
            String identifier = "[\\$A-Za-z][\\$A-Za-z0-9]*";
            String package_ = identifier + '(' + dot + identifier + ")*";
            String locale = identifier + "(_" + identifier + ")?";
            String localeList = locale + '(' + comma + locale + ")*";
            String assignment = package_ + "\\s*=\\s*" + localeList;
            String assignmentList = assignment + '(' + semi + assignment + ")*(" + semi + ")?";
            Pattern pattern = Pattern.compile(assignmentList);
            if (!pattern.matcher(loadResourceLocale).matches()) {
                throw new IllegalArgumentException(
                        "The JVM parameter \"-D" +
                        LOAD_RESOURCE_LOCALE +
                        "\" is invalid because of bad format, you should do it like this: \n" +
                        "-Dorg.babyfish.util.Resources.LOAD_RESOURCE_LOCALE=\"<<locale-assignment-list>>\"" +
                        "The grammar of <<locale-assignment-list>> is: \n" +
                        "<<locale-assignment-list>> = <<locale-assignment>> ('; ' <<locale-assignment>>)* ';'?\n" +
                        "<<locale-assignment>>      = ::package-name:: '=' <<locale-list>>;\n" +
                        "<<locale-list>>            = <<locale>> (',' <<locale>>)*\n" +
                        "<<locale>>                 = 'null' | ::locale-name::\n" +
                        "For example:\n" +
                        "-Dorg.babyfish.util.Resources.LOAD_RESOURCE_LOCALE=\"" +
                        "com.yourcompany.product1 = null, zh_CN; " +
                        "com.yourcompany.product2 = null, de_DE, fr_FR;\""
                );
            }
            ROOT_LOAD_LOCALE_NODE = LoadLocaleNode.of(loadResourceLocale);
        }
    }
}
