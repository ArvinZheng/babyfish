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

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.babyfish.collection.MACollections;
import org.babyfish.util.LazyResource;

/**
 * @author Tao Chen
 */
public class Arguments {
    
    private static final LazyResource<Resource> LAZY_RESOURCE = LazyResource.of(Resource.class);
    
    private static final List<Class<?>> BOX_TYPES = 
            MACollections.<Class<?>>wrap(
                    boolean.class, 
                    char.class, 
                    byte.class, 
                    short.class, 
                    int.class, 
                    long.class, 
                    float.class, 
                    double.class);

    protected Arguments() {
        throw new UnsupportedOperationException();
    }
    
    public static <T> T mustBeNull(
            String parameterName, 
            T argument) {
        if (argument != null) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeNull(parameterName));
        }
        return argument;
    }
    
    public static <T> T mustNotBeNull(
            String parameterName, 
            T argument) {
        if (argument == null) {
            throw new NullArgumentException(
                    LAZY_RESOURCE.get().mustNotBeNull(parameterName));
        }
        return argument;
    }
    
    public static boolean[] mustNotBeEmpty(
            String parameterName, 
            boolean[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEmpty(parameterName));
        }
        return argument;
    }
    
    public static char[] mustNotBeEmpty(
            String parameterName, 
            char[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEmpty(parameterName));
        }
        return argument;
    }
    
    public static byte[] mustNotBeEmpty(
            String parameterName, 
            byte[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEmpty(parameterName));
        }
        return argument;
    }
    
    public static short[] mustNotBeEmpty(
            String parameterName, 
            short[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEmpty(parameterName));
        }
        return argument;
    }
    
    public static int[] mustNotBeEmpty(
            String parameterName, 
            int[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEmpty(parameterName));
        }
        return argument;
    }
    
    public static long[] mustNotBeEmpty(
            String parameterName, 
            long[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEmpty(parameterName));
        }
        return argument;
    }
    
    public static float[] mustNotBeEmpty(
            String parameterName, 
            float[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEmpty(parameterName));
        }
        return argument;
    }
    
    public static double[] mustNotBeEmpty(
            String parameterName, 
            double[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEmpty(parameterName));
        }
        return argument;
    }
    
    public static <T> T[] mustNotBeEmpty(
            String parameterName, 
            T[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEmpty(parameterName));
        }
        return argument;
    }
    
    public static String mustNotBeEmpty(
            String parameterName, 
            String argument) {
        if (argument != null && argument.isEmpty()) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEmpty(parameterName));
        }
        return argument;
    }
    
    public static <E, I extends Iterable<E>> I mustNotBeEmpty(
            String parameterName, 
            I argument) {
        if (argument != null && isEmpty(argument)) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEmpty(parameterName));
        }
        return argument;
    }
    
    public static <K, V, M extends Map<K, V>> M mustNotBeEmpty(
            String parameterName, 
            M argument) {
        if (argument != null && argument.isEmpty()) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEmpty(parameterName));
        }
        return argument;
    }
    
    public static <E> E[] mustNotContainNullElements(
            String parameterName,
            E[] argument) {
        if (argument != null) {
            for (Object o : argument) {
                if (o == null) {
                    throw new IllegalArgumentException(
                            LAZY_RESOURCE.get().mustNotContainNullElements(parameterName));
                }
            }
        }
        return argument;
    }
    
    public static String[] mustNotContainEmptyElements(
            String parameterName,
            String[] argument) {
        if (argument != null) {
            for (String s : argument) {
                if (s != null && s.isEmpty()) {
                    throw new IllegalArgumentException(
                            LAZY_RESOURCE.get().mustNotContainEmptyElements(parameterName));
                }
            }
        }
        return argument;
    }
    
    public static <E> E[] mustNotContainSpecialElements(
            String parameterName,
            E[] argument,
            Class<?> classValue) {
        if (argument != null) {
            for (Object o : argument) {
                if (o != null && classValue.isAssignableFrom(o.getClass())) {
                    throw new IllegalArgumentException(
                            LAZY_RESOURCE.get().mustNotContainSpecialElements(parameterName, classValue));
                }
            }
        }
        return argument;
    }
    
    public static <E, I extends Iterable<E>> I mustNotContainNullElements(
            String parameterName,
            I argument) {
        if (argument != null) {
            for (Object o : argument) {
                if (o == null) {
                    throw new IllegalArgumentException(
                            LAZY_RESOURCE.get().mustNotContainNullElements(parameterName));
                }
            }
        }
        return argument;
    }
    
    public static <I extends Iterable<String>> I mustNotContainEmptyElements(
            String parameterName,
            I argument) {
        if (argument != null) {
            for (String s : argument) {
                if (s != null && s.isEmpty()) {
                    throw new IllegalArgumentException(
                            LAZY_RESOURCE.get().mustNotContainEmptyElements(parameterName));
                }
            }
        }
        return argument;
    }
    
    public static <E, I extends Iterable<E>> I mustNotContainSpecialElements(
            String parameterName,
            I argument,
            Class<?> classValue) {
        if (argument != null) {
            for (Object o : argument) {
                if (o != null && classValue.isAssignableFrom(o.getClass())) {
                    throw new IllegalArgumentException(
                            LAZY_RESOURCE.get().mustNotContainSpecialElements(parameterName, classValue));
                }
            }
        }
        return argument;
    }
    
    public static <K, V, M extends Map<K, V>> M mustNotContainNullKeys(
            String parameterName,
            M argument) {
        if (argument != null) {
            for (Object o : argument.keySet()) {
                if (o == null) {
                    throw new IllegalArgumentException(
                            LAZY_RESOURCE.get().mustNotContainNullKeys(parameterName));
                }
            }
        }
        return argument;
    }
    
    public static <V> Map<String, V> mustNotContainEmptyElements(
            String parameterName,
            Map<String, V> argument) {
        if (argument != null) {
            for (String s : argument.keySet()) {
                if (s != null && s.isEmpty()) {
                    throw new IllegalArgumentException(
                            LAZY_RESOURCE.get().mustNotContainEmptyElements(parameterName));
                }
            }
        }
        return argument;
    }
    
    public static <K, V, M extends Map<K, V>> M mustNotContainSpecialKeys(
            String parameterName,
            M argument,
            Class<?> classValue) {
        if (argument != null) {
            for (Object o : argument.keySet()) {
                if (o != null && classValue.isAssignableFrom(o.getClass())) {
                    throw new IllegalArgumentException(
                            LAZY_RESOURCE.get().mustNotContainSpecialKeys(parameterName, classValue));
                }
            }
        }
        return argument;
    }
    
    public static <K, V, M extends Map<K, V>> M mustNotContainNullValues(
            String parameterName,
            M argument) {
        if (argument != null) {
            for (Object o : argument.values()) {
                if (o == null) {
                    throw new IllegalArgumentException(
                            LAZY_RESOURCE.get().mustNotContainNullValues(parameterName));
                }
            }
        }
        return argument;
    }
    
    public static <K> Map<K, String> mustNotContainEmptyValues(
            String parameterName,
            Map<K, String> argument) {
        if (argument != null) {
            for (String s : argument.values()) {
                if (s != null && s.isEmpty()) {
                    throw new IllegalArgumentException(
                            LAZY_RESOURCE.get().mustNotContainEmptyElements(parameterName));
                }
            }
        }
        return argument;
    }
    
    public static <K, V, M extends Map<K, V>> M mustNotContainSpecialValues(
            String parameterName,
            M argument,
            Class<?> classValue) {
        if (argument != null) {
            for (Object o : argument.values()) {
                if (o != null && classValue.isAssignableFrom(o.getClass())) {
                    throw new IllegalArgumentException(
                            LAZY_RESOURCE.get().mustNotContainSpecialValues(parameterName, classValue));
                }
            }
        }
        return argument;
    }
    
    public static <T> Class<T> mustBeClass(String parameterName, Class<T> argument) {
        if (argument != null) {
            if (argument.isInterface() ||
                    argument.isEnum() ||
                    argument.isAnnotation() ||
                    argument.isArray() ||
                    argument.isPrimitive()) {
                throw new IllegalArgumentException(LAZY_RESOURCE.get().mustBeClass(parameterName));
            }
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeClass(String parameterName, Class<T> argument) {
        if (argument != null) {
            if (!argument.isInterface() &&
                    !argument.isEnum() &&
                    !argument.isAnnotation() &&
                    !argument.isArray() &&
                    !argument.isPrimitive()) {
                throw new IllegalArgumentException(LAZY_RESOURCE.get().mustNotBeClass(parameterName));
            }
        }
        return argument;
    }
    
    public static <T> Class<T> mustBeInterface(String parameterName, Class<T> argument) {
        if (argument != null && !argument.isInterface()) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().mustBeInterface(parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeInterface(String parameterName, Class<T> argument) {
        if (argument != null && argument.isInterface()) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().mustNotBeInterface(parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustBeEnum(String parameterName, Class<T> argument) {
        if (argument != null && !argument.isEnum()) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().mustBeEnum(parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeEnum(String parameterName, Class<T> argument) {
        if (argument != null && argument.isEnum()) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().mustNotBeEnum(parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustBeAnnotation(String parameterName, Class<T> argument) {
        if (argument != null && !argument.isAnnotation()) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().mustBeAnnotation(parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeAnnotation(String parameterName, Class<T> argument) {
        if (argument != null && argument.isAnnotation()) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().mustNotBeAnnotation(parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustBeArray(String parameterName, Class<T> argument) {
        if (argument != null && !argument.isArray()) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().mustBeArray(parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeArray(String parameterName, Class<T> argument) {
        if (argument != null && argument.isArray()) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().mustNotBeArray(parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustBePrimitive(String parameterName, Class<T> argument) {
        if (argument != null && !argument.isPrimitive()) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().mustBePrimitive(parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustNotBePrimitive(String parameterName, Class<T> argument) {
        if (argument != null && argument.isPrimitive()) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().mustNotBePrimitive(parameterName));
        }
        return argument;
    }
    
    public static <T> Class<T> mustBeBox(String parameterName, Class<T> argument) {
        if (argument != null && !BOX_TYPES.contains(argument)) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().mustBeBox(parameterName, BOX_TYPES));
        }
        return argument;
    }
    
    public static <T> Class<T> mustNotBeBox(String parameterName, Class<T> argument) {
        if (argument != null && !BOX_TYPES.contains(argument)) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().mustNotBeBox(parameterName, BOX_TYPES));
        }
        return argument;
    }
    
    public static <T> Class<T> mustBeAbstract(String parameterName, Class<T> argument) {
        if (argument != null && !Modifier.isAbstract(argument.getModifiers())) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().mustBeAbstract(parameterName));
        }
        return argument;
    }
    
    public static <T> Class<T> mustNotBeAbstract(String parameterName, Class<T> argument) {
        if (argument != null && Modifier.isAbstract(argument.getModifiers())) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().mustNotBeAbstract(parameterName));
        }
        return argument;
    }
    
    public static <T> Class<T> mustBeFinal(String parameterName, Class<T> argument) {
        if (argument != null && !Modifier.isFinal(argument.getModifiers())) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().mustBeFinal(parameterName));
        }
        return argument;
    }
    
    public static <T> Class<T> mustNotBeFinal(String parameterName, Class<T> argument) {
        if (argument != null && Modifier.isFinal(argument.getModifiers())) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().mustNotBeFinal(parameterName));
        }
        return argument;
    }
    
    public static char mustBeEqualToValue(
            String parameterName,
            char argument,
            char value) {
        if (argument != value) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEqualToValue(parameterName, Character.toString(value)));
        }
        return argument;
    }
    
    public static char mustNotBeEqualToValue(
            String parameterName,
            char argument,
            char value) {
        if (argument == value) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToValue(parameterName, Character.toString(value)));
        }
        return argument;
    }
    
    public static char mustBeGreaterThanValue(
            String parameterName,
            char argument,
            char minimumValue) {
        if (argument <= minimumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanValue(parameterName, Character.toString(minimumValue)));
        }
        return argument;
    }
    
    public static char mustBeGreaterThanOrEqualToValue(
            String parameterName,
            char argument,
            char minimumValue) {
        if (argument < minimumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToValue(parameterName, Character.toString(minimumValue)));
        }
        return argument;
    }
    
    public static char mustBeLessThanValue(
            String parameterName,
            char argument,
            char maximumValue) {
        if (argument >= maximumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanValue(parameterName, Character.toString(maximumValue)));
        }
        return argument;
    }
    
    public static char mustBeLessThanOrEqualToValue(
            String parameterName,
            char argument,
            char maximumValue) {
        if (argument > maximumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToValue(parameterName, Character.toString(maximumValue)));
        }
        return argument;
    }
    
    public static char mustBetweenValue(
            String parameterName,
            char argument,
            char minimumValue,
            boolean minimumInclusive,
            char maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenValue(
                            parameterName, 
                            minimumOp,
                            Character.toString(minimumValue),
                            maximumOp,
                            Character.toString(maximumValue)));
        }
        return argument;
    }
    
    public static byte mustBeEqualToValue(
            String parameterName,
            byte argument,
            byte value) {
        if (argument != value) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEqualToValue(parameterName, Byte.toString(value)));
        }
        return argument;
    }

    public static byte mustNotBeEqualToValue(
            String parameterName,
            byte argument,
            byte value) {
        if (argument == value) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToValue(parameterName, Byte.toString(value)));
        }
        return argument;
    }
    
    public static byte mustBeGreaterThanValue(
            String parameterName,
            byte argument,
            byte minimumValue) {
        if (argument <= minimumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanValue(parameterName, Byte.toString(minimumValue)));
        }
        return argument;
    }
    
    public static byte mustBeGreaterThanOrEqualToValue(
            String parameterName,
            byte argument,
            byte minimumValue) {
        if (argument < minimumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToValue(parameterName, Byte.toString(minimumValue)));
        }
        return argument;
    }
    
    public static byte mustBeLessThanValue(
            String parameterName,
            byte argument,
            byte maximumValue) {
        if (argument >= maximumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanValue(parameterName, Byte.toString(maximumValue)));
        }
        return argument;
    }
    
    public static byte mustBeLessThanOrEqualToValue(
            String parameterName,
            byte argument,
            byte maximumValue) {
        if (argument > maximumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToValue(parameterName, Byte.toString(maximumValue)));
        }
        return argument;
    }
    
    public static byte mustBetweenValue(
            String parameterName,
            byte argument,
            byte minimumValue,
            boolean minimumInclusive,
            byte maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenValue(
                            parameterName, 
                            minimumOp,
                            Byte.toString(minimumValue),
                            maximumOp,
                            Byte.toString(maximumValue)));
        }
        return argument;
    }
    
    public static short mustBeEqualToValue(
            String parameterName,
            short argument,
            short value) {
        if (argument != value) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEqualToValue(parameterName, Short.toString(value)));
        }
        return argument;
    }
    
    public static short mustNotBeEqualToValue(
            String parameterName,
            short argument,
            short value) {
        if (argument == value) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToValue(parameterName, Short.toString(value)));
        }
        return argument;
    }
    
    public static short mustBeGreaterThanValue(
            String parameterName,
            short argument,
            short minimumValue) {
        if (argument <= minimumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanValue(parameterName, Short.toString(minimumValue)));
        }
        return argument;
    }
    
    public static short mustBeGreaterThanOrEqualToValue(
            String parameterName,
            short argument,
            short minimumValue) {
        if (argument < minimumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToValue(parameterName, Short.toString(minimumValue)));
        }
        return argument;
    }
    
    public static short mustBeLessThanValue(
            String parameterName,
            short argument,
            short maximumValue) {
        if (argument >= maximumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanValue(parameterName, Short.toString(maximumValue)));
        }
        return argument;
    }
    
    public static short mustBeLessThanOrEqualToValue(
            String parameterName,
            short argument,
            short maximumValue) {
        if (argument > maximumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToValue(parameterName, Short.toString(maximumValue)));
        }
        return argument;
    }
    
    public static short mustBetweenValue(
            String parameterName,
            short argument,
            short minimumValue,
            boolean minimumInclusive,
            short maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenValue(
                            parameterName, 
                            minimumOp,
                            Short.toString(minimumValue),
                            maximumOp,
                            Short.toString(maximumValue)));
        }
        return argument;
    }
    
    public static int mustBeEqualToValue(
            String parameterName,
            int argument,
            int value) {
        if (argument != value) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEqualToValue(parameterName, Integer.toString(value)));
        }
        return argument;
    }
    
    public static int mustNotBeEqualToValue(
            String parameterName,
            int argument,
            int value) {
        if (argument == value) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToValue(parameterName, Integer.toString(value)));
        }
        return argument;
    }
    
    public static int mustBeGreaterThanValue(
            String parameterName,
            int argument,
            int minimumValue) {
        if (argument <= minimumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanValue(parameterName, Integer.toString(minimumValue)));
        }
        return argument;
    }
    
    public static int mustBeGreaterThanOrEqualToValue(
            String parameterName,
            int argument,
            int minimumValue) {
        if (argument < minimumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToValue(parameterName, Integer.toString(minimumValue)));
        }
        return argument;
    }
    
    public static int mustBeLessThanValue(
            String parameterName,
            int argument,
            int maximumValue) {
        if (argument >= maximumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanValue(parameterName, Integer.toString(maximumValue)));
        }
        return argument;
    }
    
    public static int mustBeLessThanOrEqualToValue(
            String parameterName,
            int argument,
            int maximumValue) {
        if (argument > maximumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToValue(parameterName, Integer.toString(maximumValue)));
        }
        return argument;
    }
    
    public static int mustBetweenValue(
            String parameterName,
            int argument,
            int minimumValue,
            boolean minimumInclusive,
            int maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenValue(
                            parameterName, 
                            minimumOp,
                            Integer.toString(minimumValue),
                            maximumOp,
                            Integer.toString(maximumValue)));
        }
        return argument;
    }
    
    public static int indexMustBeGreaterThanValue(
            String parameterName,
            int argument,
            int minimumValue) {
        if (argument <= minimumValue) {
            throw new IndexOutOfBoundsException(
                    LAZY_RESOURCE.get().mustBeGreaterThanValue(parameterName, Integer.toString(minimumValue)));
        }
        return argument;
    }
    
    public static int indexMustBeGreaterThanOrEqualToValue(
            String parameterName,
            int argument,
            int minimumValue) {
        if (argument < minimumValue) {
            throw new IndexOutOfBoundsException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToValue(parameterName, Integer.toString(minimumValue)));
        }
        return argument;
    }
    
    public static int indexMustBeLessThanValue(
            String parameterName,
            int argument,
            int maximumValue) {
        if (argument >= maximumValue) {
            throw new IndexOutOfBoundsException(
                    LAZY_RESOURCE.get().mustBeLessThanValue(parameterName, Integer.toString(maximumValue)));
        }
        return argument;
    }
    
    public static int indexMustBeLessThanOrEqualToValue(
            String parameterName,
            int argument,
            int maximumValue) {
        if (argument > maximumValue) {
            throw new IndexOutOfBoundsException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToValue(parameterName, Integer.toString(maximumValue)));
        }
        return argument;
    }
    
    public static int indexMustBetweenValue(
            String parameterName,
            int argument,
            int minimumValue,
            boolean minimumInclusive,
            int maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IndexOutOfBoundsException(
                    LAZY_RESOURCE.get().mustBetweenValue(
                            parameterName, 
                            minimumOp,
                            Integer.toString(minimumValue),
                            maximumOp,
                            Integer.toString(maximumValue)));
        }
        return argument;
    }
    
    public static long mustBeEqualToValue(
            String parameterName,
            long argument,
            long value) {
        if (argument != value) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEqualToValue(parameterName, Long.toString(value)));
        }
        return argument;
    }
    
    public static long mustNotBeEqualToValue(
            String parameterName,
            long argument,
            long value) {
        if (argument == value) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToValue(parameterName, Long.toString(value)));
        }
        return argument;
    }
    
    public static long mustBeGreaterThanValue(
            String parameterName,
            long argument,
            long minimumValue) {
        if (argument <= minimumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanValue(parameterName, Long.toString(minimumValue)));
        }
        return argument;
    }
    
    public static long mustBeGreaterThanOrEqualToValue(
            String parameterName,
            long argument,
            long minimumValue) {
        if (argument < minimumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToValue(parameterName, Long.toString(minimumValue)));
        }
        return argument;
    }
    
    public static long mustBeLessThanValue(
            String parameterName,
            long argument,
            long maximumValue) {
        if (argument >= maximumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanValue(parameterName, Long.toString(maximumValue)));
        }
        return argument;
    }
    
    public static long mustBeLessThanOrEqualToValue(
            String parameterName,
            long argument,
            long maximumValue) {
        if (argument > maximumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToValue(parameterName, Long.toString(maximumValue)));
        }
        return argument;
    }
    
    public static long mustBetweenValue(
            String parameterName,
            long argument,
            long minimumValue,
            boolean minimumInclusive,
            long maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenValue(
                            parameterName, 
                            minimumOp,
                            Long.toString(minimumValue),
                            maximumOp,
                            Long.toString(maximumValue)));
        }
        return argument;
    }
    
    public static float mustBeEqualToValue(
            String parameterName,
            float argument,
            float value) {
        if (argument != value) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEqualToValue(parameterName, Float.toString(value)));
        }
        return argument;
    }
    
    public static float mustNotBeEqualToValue(
            String parameterName,
            float argument,
            float value) {
        if (argument == value) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToValue(parameterName, Float.toString(value)));
        }
        return argument;
    }
    
    public static float mustBeGreaterThanValue(
            String parameterName,
            float argument,
            float minimumValue) {
        if (argument <= minimumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanValue(parameterName, Float.toString(minimumValue)));
        }
        return argument;
    }
    
    public static float mustBeGreaterThanOrEqualToValue(
            String parameterName,
            float argument,
            float minimumValue) {
        if (argument < minimumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToValue(parameterName, Float.toString(minimumValue)));
        }
        return argument;
    }
    
    public static float mustBeLessThanValue(
            String parameterName,
            float argument,
            float maximumValue) {
        if (argument >= maximumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanValue(parameterName, Float.toString(maximumValue)));
        }
        return argument;
    }
    
    public static float mustBeLessThanOrEqualToValue(
            String parameterName,
            float argument,
            float maximumValue) {
        if (argument > maximumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToValue(parameterName, Float.toString(maximumValue)));
        }
        return argument;
    }
    
    public static float mustBetweenValue(
            String parameterName,
            float argument,
            float minimumValue,
            boolean minimumInclusive,
            float maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenValue(
                            parameterName, 
                            minimumOp,
                            Float.toString(minimumValue),
                            maximumOp,
                            Float.toString(maximumValue)));
        }
        return argument;
    }
    
    public static double mustBeEqualToValue(
            String parameterName,
            double argument,
            double value) {
        if (argument != value) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEqualToValue(parameterName, Double.toString(value)));
        }
        return argument;
    }
    
    public static double mustNotBeEqualToValue(
            String parameterName,
            double argument,
            double value) {
        if (argument == value) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToValue(parameterName, Double.toString(value)));
        }
        return argument;
    }
    
    public static double mustBeGreaterThanValue(
            String parameterName,
            double argument,
            double minimumValue) {
        if (argument <= minimumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanValue(parameterName, Double.toString(minimumValue)));
        }
        return argument;
    }
    
    public static double mustBeGreaterThanOrEqualToValue(
            String parameterName,
            double argument,
            double minimumValue) {
        if (argument < minimumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToValue(parameterName, Double.toString(minimumValue)));
        }
        return argument;
    }
    
    public static double mustBeLessThanValue(
            String parameterName,
            double argument,
            double maximumValue) {
        if (argument >= maximumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanValue(parameterName, Double.toString(maximumValue)));
        }
        return argument;
    }
    
    public static double mustBeLessThanOrEqualToValue(
            String parameterName,
            double argument,
            double maximumValue) {
        if (argument > maximumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToValue(parameterName, Double.toString(maximumValue)));
        }
        return argument;
    }
    
    public static double mustBetweenValue(
            String parameterName,
            double argument,
            double minimumValue,
            boolean minimumInclusive,
            double maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenValue(
                            parameterName, 
                            minimumOp,
                            Double.toString(minimumValue),
                            maximumOp,
                            Double.toString(maximumValue)));
        }
        return argument;
    }
    
    public static <T> T mustBeEqualToValue(
            String parameterName,
            T argument,
            T value) {
        if (argument != null && value != null && !argument.equals(value)) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEqualToValue(parameterName, value.toString()));
        }
        return argument;
    }
    
    public static <T> T mustNotBeEqualToValue(
            String parameterName,
            T argument,
            T value) {
        if (argument != null && value != null && argument.equals(value)) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToValue(parameterName, value.toString()));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeGreaterThanValue(
            String parameterName,
            T argument,
            T minimumValue) {
        if (argument != null && minimumValue != null && argument.compareTo(minimumValue) <= 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanValue(parameterName, minimumValue.toString()));
        }
        return argument;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T mustBeGreaterThanValue(
            String parameterName,
            T argument,
            T minimumValue,
            Comparator<? super T> comparator) {
        if (comparator == null) {
            return (T)mustBeGreaterThanValue(parameterName, (Comparable)argument, (Comparable)minimumValue);
        }
        if (argument != null && minimumValue != null && comparator.compare(argument, minimumValue) <= 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanValue(parameterName, minimumValue.toString()));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeGreaterThanOrEqualToValue(
            String parameterName,
            T argument,
            T minimumValue) {
        if (argument != null && minimumValue != null && argument.compareTo(minimumValue) < 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToValue(parameterName, minimumValue.toString()));
        }
        return argument;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T mustBeGreaterThanOrEqualToValue(
            String parameterName,
            T argument,
            T minimumValue,
            Comparator<? super T> comparator) {
        if (comparator == null) {
            return (T)mustBeGreaterThanOrEqualToValue(parameterName, (Comparable)argument, (Comparable)minimumValue);
        }
        if (argument != null && minimumValue != null && comparator.compare(argument, minimumValue) < 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanValue(parameterName, minimumValue.toString()));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeLessThanValue(
            String parameterName,
            T argument,
            T maximumValue) {
        if (argument != null && maximumValue != null && argument.compareTo(maximumValue) >= 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanValue(parameterName, maximumValue.toString()));
        }
        return argument;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T mustBeLessThanValue(
            String parameterName,
            T argument,
            T maximumValue,
            Comparator<? super T> comparator) {
        if (comparator == null) {
            return (T)mustBeLessThanValue(parameterName, (Comparable)argument, (Comparable)maximumValue);
        }
        if (argument != null && maximumValue != null && comparator.compare(argument, maximumValue) >= 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanValue(parameterName, maximumValue.toString()));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeLessThanOrEqualToValue(
            String parameterName,
            T argument,
            T maximumValue) {
        if (argument != null && maximumValue != null && argument.compareTo(maximumValue) > 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToValue(parameterName, maximumValue.toString()));
        }
        return argument;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T mustBeLessThanOrEqualToValue(
            String parameterName,
            T argument,
            T maximumValue,
            Comparator<? super T> comparator) {
        if (comparator == null) {
            return (T)mustBeLessThanOrEqualToValue(parameterName, (Comparable)argument, (Comparable)maximumValue);
        }
        if (argument != null && maximumValue != null && comparator.compare(argument, maximumValue) > 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanValue(parameterName, maximumValue.toString()));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBetweenValue(
            String parameterName,
            T argument,
            T minimumValue,
            boolean minimumInclusive,
            T maximumValue,
            boolean maximumInclusive) {
        if (argument == null) {
            return null;
        }
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumValue != null) {
            if (minimumInclusive) {
                if (argument.compareTo(minimumValue) < 0) {
                    throwable = true;
                }
            } else {
                if (argument.compareTo(minimumValue) <= 0) {
                    throwable = true;
                }
            }
        }
        if (!throwable && maximumValue != null) {
            if (maximumInclusive) {
                if (argument.compareTo(maximumValue) > 0) {
                    throwable = true;
                }
            } else {
                if (argument.compareTo(maximumValue) >= 0) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenValue(
                            parameterName, 
                            minimumOp,
                            minimumValue == null ? null : maximumValue.toString(),
                            maximumOp,
                            maximumValue == null ? null : maximumValue.toString()));
        }
        return argument;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T mustBetweenValue(
            String parameterName,
            T argument,
            T minimumValue,
            boolean minimumInclusive,
            T maximumValue,
            boolean maximumInclusive,
            Comparator<? super T> comparator) {
        if (argument == null) {
            return null;
        }
        if (comparator == null) {
            return (T)mustBetweenValue(
                    parameterName,
                    (Comparable)argument,
                    (Comparable)minimumValue,
                    minimumInclusive,
                    (Comparable)maximumValue,
                    maximumInclusive);
        }
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumValue != null) {
            if (minimumInclusive) {
                if (comparator.compare(argument, minimumValue) < 0) {
                    throwable = true;
                }
            } else {
                if (comparator.compare(argument, minimumValue) <= 0) {
                    throwable = true;
                }
            }
        }
        if (!throwable && maximumValue != null) {
            if (maximumInclusive) {
                if (comparator.compare(argument, maximumValue) > 0) {
                    throwable = true;
                }
            } else {
                if (comparator.compare(argument, maximumValue) >= 0) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenValue(
                            parameterName, 
                            minimumOp,
                            minimumValue == null ? null : maximumValue.toString(),
                            maximumOp,
                            maximumValue == null ? null : maximumValue.toString()));
        }
        return argument;
    }
    
    public static <T> T mustBeInstanceOfValue(
            String parameterName,
            T argument,
            Class<?> classValue) {
        if (argument != null && !classValue.isAssignableFrom(argument.getClass())) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeInstanceOfValue(parameterName, classValue));
        }
        return argument;
    }

    public static <T> T mustBeInstanceOfAllOfValue(
            String parameterName,
            T argument,
            Class<?> ... classesValue) {
        if  (argument != null) {
            Class<? extends Object> argumentClass = argument.getClass();
            for (Class<?> clazz : classesValue) {
                if (clazz != null) {
                    if (!clazz.isAssignableFrom(argumentClass)) {
                        throw new IllegalArgumentException(
                                LAZY_RESOURCE.get().mustBeInstanceOfAllOfValue(parameterName, classesValue)
                        );
                    }
                }
            }
        }
        return argument;
    }

    public static <T> T mustBeInstanceOfAnyOfValue(
            String parameterName,
            T argument,
            Class<?> ... classesValue) {
        if  (argument != null && classesValue.length != 0) {
            Class<? extends Object> argumentClass = argument.getClass();
            boolean throwable = false;
            for (Class<?> clazz : classesValue) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argumentClass)) {
                        return argument;
                    }
                    throwable = true;
                }
            }
            if (throwable) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustBeInstanceOfAnyOfValue(parameterName, classesValue)
                );
            }
        }
        return argument;
    }

    public static <T> T mustNotBeInstanceOfValue(
            String parameterName,
            T argument,
            Class<?> classValue) {
        if (argument != null && classValue.isAssignableFrom(argument.getClass())) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeInstanceOfValue(parameterName, classValue));
        }
        return argument;
    }

    public static <T> T mustNotBeInstanceOfAnyOfValue(
            String parameterName,
            T argument,
            Class<?> ... classesValue) {
        if  (argument != null && classesValue.length != 0) {
            Class<? extends Object> argumentClass = argument.getClass();
            for (Class<?> clazz : classesValue) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argumentClass)) {
                        throw new IllegalArgumentException(
                                LAZY_RESOURCE.get().mustNotBeInstanceOfAnyOfValue(parameterName, classesValue)
                        );
                    }
                }
            }
        }
        return argument;
    }

    public static <T> Class<T> mustBeCompatibleWithValue(
            String parameterName,
            Class<T> argument,
            Class<?> classValue) {
        if (argument != null && !classValue.isAssignableFrom(argument)) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeCompatibleWithValue(parameterName, classValue));
        }
        return argument;
    }

    public static <T> Class<T> mustBeCompatibleWithAllOfValue(
            String parameterName,
            Class<T> argument,
            Class<?> ... classesValue) {
        if  (argument != null) {
            for (Class<?> clazz : classesValue) {
                if (clazz != null) {
                    if (!clazz.isAssignableFrom(argument)) {
                        throw new IllegalArgumentException(
                                LAZY_RESOURCE.get().mustBeCompatibleWithAllOfValue(parameterName, classesValue)
                        );
                    }
                }
            }
        }
        return argument;
    }

    public static <T> Class<T> mustBeCompatibleWithAnyOfValue(
            String parameterName,
            Class<T> argument,
            Class<?> ... classesValue) {
        if  (argument != null && classesValue.length != 0) {
            boolean throwable = false;
            for (Class<?> clazz : classesValue) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argument)) {
                        return argument;
                    }
                    throwable = true;
                }
            }
            if (throwable) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustBeCompatibleWithAnyOfValue(parameterName, classesValue)
                );
            }
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeCompatibleWithValue(
            String parameterName,
            Class<T> argument,
            Class<?> classValue) {
        if (argument != null && classValue.isAssignableFrom(argument)) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeCompatibleWithValue(parameterName, classValue));
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeCompatibleWithAnyOfValue(
            String parameterName,
            Class<T> argument,
            Class<?> ... classesValue) {
        if  (argument != null && classesValue.length != 0) {
            for (Class<?> clazz : classesValue) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argument)) {
                        throw new IllegalArgumentException(
                                LAZY_RESOURCE.get().mustNotBeCompatibleWithAnyOfValue(parameterName, classesValue)
                        );
                    }
                }
            }
        }
        return argument;
    }
    
    public static char mustBeAnyOfValue(
            String parameterName, 
            char argument, 
            char ... charactersValue) {
        for (char c : charactersValue) {
            if (c == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfValue(parameterName, MACollections.wrap(charactersValue))
        );
    }
    
    public static char mustNotBeAnyOfValue(
            String parameterName, 
            char argument, 
            char ... charactersValue) {
        for (char c : charactersValue) {
            if (c == argument) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfValue(parameterName, MACollections.wrap(charactersValue))
                );
            }
        }
        return argument;
    }
    
    public static byte mustBeAnyOfValue(
            String parameterName, 
            byte argument, 
            byte ... bytesValue) {
        for (byte b : bytesValue) {
            if (b == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfValue(parameterName, MACollections.wrap(bytesValue))
        );
    }
    
    public static byte mustNotBeAnyOfValue(
            String parameterName, 
            byte argument, 
            byte ... bytesValue) {
        for (byte b : bytesValue) {
            if (b == argument) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfValue(parameterName, MACollections.wrap(bytesValue))
                );
            }
        }
        return argument;
    }
    
    public static short mustBeAnyOfValue(
            String parameterName, 
            short argument, 
            short ... shortsValue) {
        for (short s : shortsValue) {
            if (s == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfValue(parameterName, MACollections.wrap(shortsValue))
        );
    }
    
    public static short mustNotBeAnyOfValue(
            String parameterName, 
            short argument, 
            short ... shortsValue) {
        for (short s : shortsValue) {
            if (s == argument) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfValue(parameterName, MACollections.wrap(shortsValue))
                );
            }
        }
        return argument;
    }
    
    public static int mustBeAnyOfValue(
            String parameterName, 
            int argument, 
            int ... intsValue) {
        for (int i : intsValue) {
            if (i == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfValue(parameterName, MACollections.wrap(intsValue))
        );
    }
    
    public static int mustNotBeAnyOfValue(
            String parameterName, 
            int argument, 
            int ... intsValue) {
        for (int i : intsValue) {
            if (i == argument) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfValue(parameterName, MACollections.wrap(intsValue))
                );
            }
        }
        return argument;
    }
    
    public static long mustBeAnyOfValue(
            String parameterName, 
            long argument, 
            long ... longsValue) {
        for (long l : longsValue) {
            if (l == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfValue(parameterName, MACollections.wrap(longsValue))
        );
    }
    
    public static long mustNotBeAnyOfValue(
            String parameterName, 
            long argument, 
            long ... longsValue) {
        for (long l : longsValue) {
            if (l == argument) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfValue(parameterName, MACollections.wrap(longsValue))
                );
            }
        }
        return argument;
    }
    
    public static float mustBeAnyOfValue(
            String parameterName, 
            float argument, 
            float ... floatsValue) {
        for (float f : floatsValue) {
            if (f == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfValue(parameterName, MACollections.wrap(floatsValue))
        );
    }
    
    public static float mustNotBeAnyOfValue(
            String parameterName, 
            float argument, 
            float ... floatsValue) {
        for (float f : floatsValue) {
            if (f == argument) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfValue(parameterName, MACollections.wrap(floatsValue))
                );
            }
        }
        return argument;
    }
    
    public static double mustBeAnyOfValue(
            String parameterName, 
            double argument, 
            double ... doublesValue) {
        for (double d : doublesValue) {
            if (d == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfValue(parameterName, MACollections.wrap(doublesValue))
        );
    }

    public static double mustNotBeAnyOfValue(
            String parameterName, 
            double argument, 
            double ... doublesValue) {
        for (double d : doublesValue) {
            if (d == argument) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfValue(parameterName, MACollections.wrap(doublesValue))
                );
            }
        }
        return argument;
    }

    @SafeVarargs
    public static <T> T mustBeAnyOfValue(
            String parameterName, 
            T argument, 
            T ... objectsValue) {
        if (argument == null) {
            return null;
        }
        for (T o : objectsValue) {
            if (argument.equals(o)) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfValue(parameterName, MACollections.wrap(objectsValue))
        );
    }
    
    @SafeVarargs
    public static <T> T mustNotBeAnyOfValue(
            String parameterName, 
            T argument, 
            T ... objectsValue) {
        if (argument == null) {
            return null;
        }
        for (T o : objectsValue) {
            if (argument.equals(o)) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfValue(parameterName, MACollections.wrap(objectsValue))
                );
            }
        }
        return argument;
    }
    
    public static char mustBeEqualToOther(
            String parameterName,
            char argument,
            String valueParameterName,
            char valueArgument) {
        if (argument != valueArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEqualToOther(parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static char mustNotBeEqualToOther(
            String parameterName,
            char argument,
            String valueParameterName,
            char valueArgument) {
        if (argument == valueArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToOther(parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static char mustBeGreaterThanOther(
            String parameterName,
            char argument,
            String minimumParameterName,
            char minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOther(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static char mustBeGreaterThanOrEqualToOther(
            String parameterName,
            char argument,
            String minimumParameterName, 
            char minimumArgument) {
        if (argument < minimumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToOther(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static char mustBeLessThanOther(
            String parameterName,
            char argument,
            String maximumParameterName,
            char maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOther(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static char mustBeLessThanOrEqualToOther(
            String parameterName,
            char argument,
            String maximumParameterName,
            char maximumArgument) {
        if (argument > maximumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToOther(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static char mustBetweenOther(
            String parameterName,
            char argument,
            String minimumParameterName,
            char minimumArgument,
            String maximumParameterName,
            boolean minimumInclusive,
            char maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenOther(
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static byte mustBeEqualToOther(
            String parameterName,
            byte argument,
            String valueParameterName,
            byte valueArgument) {
        if (argument != valueArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEqualToOther(parameterName, valueParameterName));
        }
        return argument;
    }

    public static byte mustNotBeEqualToOther(
            String parameterName,
            byte argument,
            String valueParamterName,
            byte valueArgument) {
        if (argument == valueArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToOther(parameterName, valueParamterName));
        }
        return argument;
    }
    
    public static byte mustBeGreaterThanOther(
            String parameterName,
            byte argument,
            String minimumArgumentName,
            byte minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOther(parameterName, minimumArgumentName));
        }
        return argument;
    }
    
    public static byte mustBeGreaterThanOrEqualToOther(
            String parameterName,
            byte argument,
            String minmumParameterName,
            byte minimumArgument) {
        if (argument < minimumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToOther(parameterName, minmumParameterName));
        }
        return argument;
    }
    
    public static byte mustBeLessThanOther(
            String parameterName,
            byte argument,
            String maximumParameterName,
            byte maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOther(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static byte mustBeLessThanOrEqualToOther(
            String parameterName,
            byte argument,
            String maximumParameterName,
            byte maximumArgument) {
        if (argument > maximumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToOther(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static byte mustBetweenOther(
            String parameterName,
            byte argument,
            String minimumParameterName,
            byte minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            byte maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenOther(
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static short mustBeEqualToOther(
            String parameterName,
            short argument,
            String valueParameterName,
            short valueArgument) {
        if (argument != valueArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEqualToOther(parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static short mustNotBeEqualToOther(
            String parameterName,
            short argument,
            String valueParameterName,
            short valueArgument) {
        if (argument == valueArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToOther(parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static short mustBeGreaterThanOther(
            String parameterName,
            short argument,
            String minimumParameterName,
            short minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOther(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static short mustBeGreaterThanOrEqualToOther(
            String parameterName,
            short argument,
            String minimumParameterName,
            short minimumArgument) {
        if (argument < minimumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToOther(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static short mustBeLessThanOther(
            String parameterName,
            short argument,
            String maximumParameterName,
            short maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOther(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static short mustBeLessThanOrEqualToOther(
            String parameterName,
            short argument,
            String maximumParameterName,
            short maximumArgument) {
        if (argument > maximumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToOther(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static short mustBetweenOther(
            String parameterName,
            short argument,
            String minimumParameterName,
            short minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            short maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenOther(
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static int mustBeEqualToOther(
            String parameterName,
            int argument,
            String valueParameterName,
            int valueArgument) {
        if (argument != valueArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEqualToOther(parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static int mustNotBeEqualToOther(
            String parameterName,
            int argument,
            String valueParameterName,
            int valueArgument) {
        if (argument == valueArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToOther(parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static int mustBeGreaterThanOther(
            String parameterName,
            int argument,
            String minimumParameterName,
            int minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOther(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static int mustBeGreaterThanOrEqualToOther(
            String parameterName,
            int argument,
            String minimumParameterName,
            int minimumArgument) {
        if (argument < minimumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToOther(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static int mustBeLessThanOther(
            String parameterName,
            int argument,
            String maximumParameterName,
            int maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOther(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static int mustBeLessThanOrEqualToOther(
            String parameterName,
            int argument,
            String maximumParameterName,
            int maximumArgument) {
        if (argument > maximumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToOther(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static int mustBetweenOther(
            String parameterName,
            int argument,
            String minimumParameterName,
            int minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            int maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenOther(
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static int indexMustBeGreaterThanOther(
            String parameterName,
            int argument,
            String minimumParameterName,
            int minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IndexOutOfBoundsException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOther(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static int indexMustBeGreaterThanOrEqualToOther(
            String parameterName,
            int argument,
            String minimumParameterName,
            int minimumArgument) {
        if (argument < minimumArgument) {
            throw new IndexOutOfBoundsException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToOther(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static int indexMustBeLessThanOther(
            String parameterName,
            int argument,
            String maximumParameterName,
            int maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IndexOutOfBoundsException(
                    LAZY_RESOURCE.get().mustBeLessThanOther(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static int indexMustBeLessThanOrEqualToOther(
            String parameterName,
            int argument,
            String maximumParameterName,
            int maximumArgument) {
        if (argument > maximumArgument) {
            throw new IndexOutOfBoundsException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToOther(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static int indexMustBetweenOther(
            String parameterName,
            int argument,
            String minimumParameterName,
            int minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            int maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IndexOutOfBoundsException(
                    LAZY_RESOURCE.get().mustBetweenOther(
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static long mustBeEqualToOther(
            String parameterName,
            long argument,
            String valueParameterName,
            long valueArgument) {
        if (argument != valueArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEqualToOther(parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static long mustNotBeEqualToOther(
            String parameterName,
            long argument,
            String valueParameterName,
            long valueArgument) {
        if (argument == valueArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToOther(parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static long mustBeGreaterThanOther(
            String parameterName,
            long argument,
            String minimumParmeterName,
            long minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOther(parameterName, minimumParmeterName));
        }
        return argument;
    }
    
    public static long mustBeGreaterThanOrEqualToOther(
            String parameterName,
            long argument,
            String minimumParameterName,
            long minimumArgument) {
        if (argument < minimumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToOther(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static long mustBeLessThanOther(
            String parameterName,
            long argument,
            String maximumParameterName,
            long maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOther(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static long mustBeLessThanOrEqualToOther(
            String parameterName,
            long argument,
            String maximumParameterName,
            long maximumArgument) {
        if (argument > maximumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToOther(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static long mustBetweenOther(
            String parameterName,
            long argument,
            String minimumParameterName,
            long minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            long maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenOther(
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static float mustBeEqualToOther(
            String parameterName,
            float argument,
            String valueParameterName,
            float valueArgument) {
        if (argument != valueArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEqualToOther(parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static float mustNotBeEqualToOther(
            String parameterName,
            float argument,
            String valueParameterName,
            float valueArgument) {
        if (argument == valueArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToOther(parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static float mustBeGreaterThanOther(
            String parameterName,
            float argument,
            String minimumParameterName,
            float minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOther(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static float mustBeGreaterThanOrEqualToOther(
            String parameterName,
            float argument,
            String minimumParameterName,
            float minimumArgument) {
        if (argument < minimumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToOther(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static float mustBeLessThanOther(
            String parameterName,
            float argument,
            String maximumParameterName,
            float maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOther(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static float mustBeLessThanOrEqualToOther(
            String parameterName,
            float argument,
            String maximumParameterName,
            float maximumArgument) {
        if (argument > maximumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToOther(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static float mustBetweenOther(
            String parameterName,
            float argument,
            String minimumParameterName,
            float minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            float maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenOther(
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static double mustBeEqualToOther(
            String parameterName,
            double argument,
            String valueParameterName,
            double valueArgument) {
        if (argument != valueArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEqualToOther(parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static double mustNotBeEqualToOther(
            String parameterName,
            double argument,
            String valueParameterName,
            double valueArgument) {
        if (argument == valueArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToOther(parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static double mustBeGreaterThanOther(
            String parameterName,
            double argument,
            String minimumParameterName,
            double minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOther(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static double mustBeGreaterThanOrEqualToOther(
            String parameterName,
            double argument,
            String minimumParameterName,
            double minimumArgument) {
        if (argument < minimumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToOther(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static double mustBeLessThanOther(
            String parameterName,
            double argument,
            String maximumParameterName,
            double maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOther(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static double mustBeLessThanOrEqualToOther(
            String parameterName,
            double argument,
            String maximumParameterName,
            double maximumArgument) {
        if (argument > maximumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToOther(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static double mustBetweenOther(
            String parameterName,
            double argument,
            String minimumParameterName,
            double minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            double maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenOther(
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static <T> T mustBeEqualToOther(
            String parameterName,
            T argument,
            String valueParameterName,
            T valueArgument) {
        if (argument != null && valueArgument != null && !argument.equals(valueArgument)) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEqualToOther(parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static <T> T mustNotBeEqualToOther(
            String parameterName,
            T argument,
            String valueParameterName,
            T valueArgument) {
        if (argument != null && valueArgument != null && argument.equals(valueArgument)) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToOther(parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeGreaterThanOther(
            String parameterName,
            T argument,
            String minimumParameterName,
            T minimumArgument) {
        if (argument != null && minimumArgument != null && argument.compareTo(minimumArgument) <= 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOther(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T mustBeGreaterThanOther(
            String parameterName,
            T argument,
            String minimumParameterName,
            T minimumValue,
            Comparator<? super T> comparator) {
        if (comparator == null) {
            return (T)mustBeGreaterThanOther(
                    parameterName, 
                    (Comparable)argument, 
                    minimumParameterName, 
                    (Comparable)minimumValue);
        }
        if (argument != null && minimumValue != null && comparator.compare(argument, minimumValue) <= 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOther(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeGreaterThanOrEqualToOther(
            String parameterName,
            T argument,
            String minimumParameterName,
            T minimumArgument) {
        if (argument != null && minimumArgument != null && argument.compareTo(minimumArgument) < 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToOther(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T mustBeGreaterThanOrEqualToOther(
            String parameterName,
            T argument,
            String minimumParameterName,
            T minimumValue,
            Comparator<? super T> comparator) {
        if (comparator == null) {
            return (T)mustBeGreaterThanOrEqualToOther(
                    parameterName, 
                    (Comparable)argument, 
                    minimumParameterName, 
                    (Comparable)minimumValue);
        }
        if (argument != null && minimumValue != null && comparator.compare(argument, minimumValue) < 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOther(parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeLessThanOther(
            String parameterName,
            T argument,
            String maximumParameterName,
            T maximumArgument) {
        if (argument != null && maximumArgument != null && argument.compareTo(maximumArgument) >= 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOther(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T mustBeLessThanOther(
            String parameterName,
            T argument,
            String maximumParameterName,
            T maximumValue,
            Comparator<? super T> comparator) {
        if (comparator == null) {
            return (T)mustBeLessThanOther(
                    parameterName, 
                    (Comparable)argument, 
                    maximumParameterName, 
                    (Comparable)maximumValue);
        }
        if (argument != null && maximumValue != null && comparator.compare(argument, maximumValue) >= 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOther(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeLessThanOrEqualToOther(
            String parameterName,
            T argument,
            String maximumParameterName,
            T maximumArgument) {
        if (argument != null && maximumArgument != null && argument.compareTo(maximumArgument) > 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToOther(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T mustBeLessThanOrEqualToOther(
            String parameterName,
            T argument,
            String maximumParameterName,
            T maximumValue,
            Comparator<? super T> comparator) {
        if (comparator == null) {
            return (T)mustBeLessThanOrEqualToOther(
                    parameterName, 
                    (Comparable)argument, 
                    maximumParameterName, 
                    (Comparable)maximumValue);
        }
        if (argument != null && maximumValue != null && comparator.compare(argument, maximumValue) > 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOther(parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBetweenOther(
            String parameterName,
            T argument,
            String minimumParameterName,
            T minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            T maximumArgument,
            boolean maximumInclusive) {
        if (argument == null) {
            return null;
        }
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumArgument != null) {
            if (minimumInclusive) {
                if (argument.compareTo(minimumArgument) < 0) {
                    throwable = true;
                }
            } else {
                if (argument.compareTo(minimumArgument) <= 0) {
                    throwable = true;
                }
            }
        }
        if (!throwable && maximumArgument != null) {
            if (maximumInclusive) {
                if (argument.compareTo(maximumArgument) > 0) {
                    throwable = true;
                }
            } else {
                if (argument.compareTo(maximumArgument) >= 0) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenOther(
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T mustBetweenOther(
            String parameterName,
            T argument,
            String minimumParameterName,
            T minimumValue,
            boolean minimumInclusive,
            String maximumParameterName,
            T maximumValue,
            boolean maximumInclusive,
            Comparator<? super T> comparator) {
        if (argument == null) {
            return null;
        }
        if (comparator == null) {
            return (T)mustBetweenOther(
                    parameterName,
                    (Comparable)argument,
                    minimumParameterName,
                    (Comparable)minimumValue,
                    minimumInclusive,
                    maximumParameterName,
                    (Comparable)maximumValue,
                    maximumInclusive);
        }
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumValue != null) {
            if (minimumInclusive) {
                if (comparator.compare(argument, minimumValue) < 0) {
                    throwable = true;
                }
            } else {
                if (comparator.compare(argument, minimumValue) <= 0) {
                    throwable = true;
                }
            }
        }
        if (!throwable && maximumValue != null) {
            if (maximumInclusive) {
                if (comparator.compare(argument, maximumValue) > 0) {
                    throwable = true;
                }
            } else {
                if (comparator.compare(argument, maximumValue) >= 0) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenValue(
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static <T> T mustBeInstanceOfOther(
            String parameterName,
            T argument,
            String classParameterName,
            Class<?> classArgument) {
        if (argument != null && !classArgument.isAssignableFrom(argument.getClass())) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeInstanceOfOther(parameterName, classParameterName));
        }
        return argument;
    }

    public static <T> T mustBeInstanceOfAllOfOther(
            String parameterName,
            T argument,
            String classesParameterName,
            Class<?> ... classesArgument) {
        if  (argument != null) {
            Class<? extends Object> argumentClass = argument.getClass();
            for (Class<?> clazz : classesArgument) {
                if (clazz != null) {
                    if (!clazz.isAssignableFrom(argumentClass)) {
                        throw new IllegalArgumentException(
                                LAZY_RESOURCE.get().mustBeInstanceOfAllOfOther(parameterName, classesParameterName)
                        );
                    }
                }
            }
        }
        return argument;
    }

    public static <T> T mustBeInstanceOfAnyOfOther(
            String parameterName,
            T argument,
            String classesParameterName,
            Class<?> ... classesArgument) {
        if  (argument != null && classesArgument.length != 0) {
            Class<? extends Object> argumentClass = argument.getClass();
            boolean throwable = false;
            for (Class<?> clazz : classesArgument) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argumentClass)) {
                        return argument;
                    }
                    throwable = true;
                }
            }
            if (throwable) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustBeInstanceOfAnyOfOther(parameterName, classesParameterName)
                );
            }
        }
        return argument;
    }

    public static <T> T mustNotBeInstanceOfOther(
            String parameterName,
            T argument,
            String classParameterName,
            Class<?> classArgument) {
        if (argument != null && classArgument.isAssignableFrom(argument.getClass())) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeInstanceOfOther(parameterName, classParameterName));
        }
        return argument;
    }

    public static <T> T mustNotBeInstanceOfAnyOfOther(
            String parameterName,
            T argument,
            String classesParameterName,
            Class<?> ... classesArgument) {
        if  (argument != null && classesArgument.length != 0) {
            Class<? extends Object> argumentClass = argument.getClass();
            for (Class<?> clazz : classesArgument) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argumentClass)) {
                        throw new IllegalArgumentException(
                                LAZY_RESOURCE.get().mustNotBeInstanceOfAnyOfOther(parameterName, classesParameterName)
                        );
                    }
                }
            }
        }
        return argument;
    }

    public static <T> Class<T> mustBeCompatibleWithOther(
            String parameterName,
            Class<T> argument,
            String classParameterName,
            Class<?> classArgument) {
        if (argument != null && !classArgument.isAssignableFrom(argument)) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeCompatibleWithOther(parameterName, classParameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustBeCompatibleWithAllOfOther(
            String parameterName,
            Class<T> argument,
            String classesParameterName,
            Class<?> ... classesArgument) {
        if  (argument != null) {
            for (Class<?> clazz : classesArgument) {
                if (clazz != null) {
                    if (!clazz.isAssignableFrom(argument)) {
                        throw new IllegalArgumentException(
                                LAZY_RESOURCE.get().mustBeCompatibleWithAllOfOther(parameterName,classesParameterName)
                        );
                    }
                }
            }
        }
        return argument;
    }

    public static <T> Class<T> mustBeCompatibleWithAnyOfOther(
            String parameterName,
            Class<T> argument,
            String classesParameterName,
            Class<?> ... classesArgument) {
        if  (argument != null && classesArgument.length != 0) {
            boolean throwable = false;
            for (Class<?> clazz : classesArgument) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argument)) {
                        return argument;
                    }
                    throwable = true;
                }
            }
            if (throwable) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustBeCompatibleWithAnyOfOther(parameterName, classesParameterName)
                );
            }
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeCompatibleWithOther(
            String parameterName,
            Class<T> argument,
            String classParameterName,
            Class<?> classArgument) {
        if (argument != null && classArgument.isAssignableFrom(argument)) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeCompatibleWithOther(parameterName, classParameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeCompatibleWithAnyOfOther(
            String parameterName,
            Class<T> argument,
            String classesParameterName,
            Class<?> ... classesArgument) {
        if  (argument != null && classesArgument.length != 0) {
            for (Class<?> clazz : classesArgument) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argument)) {
                        throw new IllegalArgumentException(
                                LAZY_RESOURCE.get().mustNotBeCompatibleWithAnyOfOther(parameterName, classesParameterName)
                        );
                    }
                }
            }
        }
        return argument;
    }
    
    public static char mustBeAnyOfOther(
            String parameterName, 
            char argument, 
            String valueParameterName,
            char ... charactersValueArgument) {
        for (char c : charactersValueArgument) {
            if (c == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfOther(parameterName, valueParameterName)
        );
    }
    
    public static char mustNotBeAnyOfOther(
            String parameterName, 
            char argument, 
            String valueParameterName,
            char ... charactersValueArgument) {
        for (char c : charactersValueArgument) {
            if (c == argument) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfOther(parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }
    
    public static byte mustBeAnyOfOther(
            String parameterName, 
            byte argument, 
            String valueParameterName,
            byte ... bytesValueArgument) {
        for (byte b : bytesValueArgument) {
            if (b == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfOther(parameterName, valueParameterName)
        );
    }
    
    public static byte mustNotBeAnyOfOther(
            String parameterName, 
            byte argument, 
            String valueParameterName,
            byte ... bytesValueArgument) {
        for (byte b : bytesValueArgument) {
            if (b == argument) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfOther(parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }
    
    public static short mustBeAnyOfOther(
            String parameterName, 
            short argument, 
            String valueParameterName,
            short ... shortsValueArgument) {
        for (short s : shortsValueArgument) {
            if (s == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfOther(parameterName, valueParameterName)
        );
    }
    
    public static short mustNotBeAnyOfOther(
            String parameterName, 
            short argument, 
            String valueParameterName,
            short ... shortsValueArgument) {
        for (short s : shortsValueArgument) {
            if (s == argument) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfOther(parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }
    
    public static int mustBeAnyOfOther(
            String parameterName, 
            int argument, 
            String valueParameterName,
            int ... intsValueArgument) {
        for (int i : intsValueArgument) {
            if (i == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfOther(parameterName, valueParameterName)
        );
    }
    
    public static int mustNotBeAnyOfOther(
            String parameterName, 
            int argument, 
            String valueParameterName,
            int ... intsValueArgument) {
        for (int i : intsValueArgument) {
            if (i == argument) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfOther(parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }
    
    public static long mustBeAnyOfOther(
            String parameterName, 
            long argument, 
            String valueParameterName,
            long ... longsValueArgument) {
        for (long l : longsValueArgument) {
            if (l == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfOther(parameterName, valueParameterName)
        );
    }
    
    public static long mustNotBeAnyOfOther(
            String parameterName, 
            long argument, 
            String valueParameterName,
            long ... longsValueArgument) {
        for (long l : longsValueArgument) {
            if (l == argument) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfOther(parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }
    
    public static float mustBeAnyOfOther(
            String parameterName, 
            float argument, 
            String valueParameterName,
            float ... floatsValueArgument) {
        for (float f : floatsValueArgument) {
            if (f == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfOther(parameterName, valueParameterName)
        );
    }
    
    public static float mustNotBeAnyOfOther(
            String parameterName, 
            float argument, 
            String valueParameterName,
            float ... floatsValueArgument) {
        for (float f : floatsValueArgument) {
            if (f == argument) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfOther(parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }
    
    public static double mustBeAnyOfOther(
            String parameterName, 
            double argument, 
            String valueParameterName,
            double ... doublesValueArgument) {
        for (double d : doublesValueArgument) {
            if (d == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfOther(parameterName, valueParameterName)
        );
    }

    public static double mustNotBeAnyOfOther(
            String parameterName, 
            double argument, 
            String valueParameterName,
            double ... doublesValueArgument) {
        for (double d : doublesValueArgument) {
            if (d == argument) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfOther(parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }

    @SafeVarargs
    public static <T> T mustBeAnyOfOther(
            String parameterName, 
            T argument, 
            String valueParameterName,
            T ... objectsValueArgument) {
        if (argument == null) {
            return null;
        }
        for (T o : objectsValueArgument) {
            if (argument.equals(o)) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfOther(parameterName, valueParameterName)
        );
    }
    
    @SafeVarargs
    public static <T> T mustNotBeAnyOfOther(
            String parameterName, 
            T argument, 
            String valueParameterName,
            T ... objectsValueArgument) {
        if (argument == null) {
            return null;
        }
        for (T o : objectsValueArgument) {
            if (argument.equals(o)) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfOther(parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }
    
    public static <T> T mustBeNullWhen(
            String whenCondition, 
            String parameterName, 
            T argument) {
        if (argument != null) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeNullWhen(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static <T> T mustNotBeNullWhen(
            String whenCondition, 
            String parameterName, 
            T argument) {
        if (argument == null) {
            throw new NullArgumentException(
                    LAZY_RESOURCE.get().mustNotBeNullWhen(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static boolean[] mustBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            boolean[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEmptyWhen(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static char[] mustBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            char[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEmptyWhen(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static byte[] mustBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            byte[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEmptyWhen(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static short[] mustBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            short[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEmptyWhen(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static int[] mustBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            int[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEmptyWhen(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static long[] mustBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            long[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEmptyWhen(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static float[] mustBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            float[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEmptyWhen(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static double[] mustBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            double[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEmptyWhen(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static <T> T[] mustBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            T[] argument) {
        if (argument != null && argument.length != 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEmptyWhen(whenCondition, parameterName));
        }
        return argument;
    }

    public static boolean[] mustNotBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            boolean[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEmptyWhen(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static char[] mustNotBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            char[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEmptyWhen(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static byte[] mustNotBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            byte[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEmptyWhen(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static short[] mustNotBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            short[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEmptyWhen(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static int[] mustNotBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            int[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEmptyWhen(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static long[] mustNotBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            long[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEmptyWhen(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static float[] mustNotBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            float[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEmptyWhen(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static double[] mustNotBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            double[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEmptyWhen(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static <T> T[] mustNotBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            T[] argument) {
        if (argument != null && argument.length == 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEmptyWhen(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static String mustBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            String argument) {
        if (argument != null && !argument.isEmpty()) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEmptyWhen(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static String mustNotBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            String argument) {
        if (argument != null && argument.isEmpty()) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEmptyWhen(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static <E, I extends Iterable<E>> I mustBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            I argument) {
        if (argument != null && !isEmpty(argument)) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEmptyWhen(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static <E, I extends Iterable<E>> I mustNotBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            I argument) {
        if (argument != null && isEmpty(argument)) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEmptyWhen(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static <K, V, M extends Map<K, V>> M mustBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            M argument) {
        if (argument != null && !argument.isEmpty()) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEmptyWhen(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static <K, V, M extends Map<K, V>> M mustNotBeEmptyWhen(
            String whenCondition, 
            String parameterName, 
            M argument) {
        if (argument != null && argument.isEmpty()) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEmptyWhen(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static <E> E[] mustNotContainNullElementsWhen(
            String whenCondition, 
            String parameterName,
            E[] argument) {
        if (argument != null) {
            for (Object o : argument) {
                if (o == null) {
                    throw new IllegalArgumentException(
                            LAZY_RESOURCE.get().mustNotContainNullElementsWhen(whenCondition, parameterName));
                }
            }
        }
        return argument;
    }
    
    public static String[] mustNotContainEmptyElementsWhen(
            String whenCondition, 
            String parameterName,
            String[] argument) {
        if (argument != null) {
            for (String s : argument) {
                if (s != null && s.isEmpty()) {
                    throw new IllegalArgumentException(
                            LAZY_RESOURCE.get().mustNotContainEmptyElementsWhen(whenCondition, parameterName));
                }
            }
        }
        return argument;
    }
    
    public static <E> E[] mustNotContainSpecialElementsWhen(
            String whenCondition, 
            String parameterName,
            E[] argument,
            Class<?> clazz) {
        if (argument != null) {
            for (Object o : argument) {
                if (o != null && clazz.isAssignableFrom(o.getClass())) {
                    throw new IllegalArgumentException(
                            LAZY_RESOURCE.get().mustNotContainSpecialElementsWhen(whenCondition, parameterName, clazz));
                }
            }
        }
        return argument;
    }
    
    public static <E, I extends Iterable<E>> I mustNotContainNullElementsWhen(
            String whenCondition, 
            String parameterName,
            I argument) {
        if (argument != null) {
            for (Object o : argument) {
                if (o == null) {
                    throw new IllegalArgumentException(
                            LAZY_RESOURCE.get().mustNotContainNullElementsWhen(whenCondition, parameterName));
                }
            }
        }
        return argument;
    }
    
    public static <I extends Iterable<String>> I mustNotContainEmptyElementsWhen(
            String whenCondition, 
            String parameterName,
            I argument) {
        if (argument != null) {
            for (String s : argument) {
                if (s != null && s.isEmpty()) {
                    throw new IllegalArgumentException(
                            LAZY_RESOURCE.get().mustNotContainEmptyElementsWhen(whenCondition, parameterName));
                }
            }
        }
        return argument;
    }
    
    public static <E, I extends Iterable<E>> I mustNotContainSpecialElementsWhen(
            String whenCondition, 
            String parameterName,
            I argument,
            Class<?> clazz) {
        if (argument != null) {
            for (Object o : argument) {
                if (o != null && clazz.isAssignableFrom(o.getClass())) {
                    throw new IllegalArgumentException(
                            LAZY_RESOURCE.get().mustNotContainSpecialElementsWhen(whenCondition, parameterName, clazz));
                }
            }
        }
        return argument;
    }
    
    public static <K, V, M extends Map<K, V>> M mustNotContainNullKeysWhen(
            String whenCondition, 
            String parameterName,
            M argument) {
        if (argument != null) {
            for (Object o : argument.keySet()) {
                if (o == null) {
                    throw new IllegalArgumentException(
                            LAZY_RESOURCE.get().mustNotContainNullKeysWhen(whenCondition, parameterName));
                }
            }
        }
        return argument;
    }
    
    public static <V> Map<String, V> mustNotContainEmptyElementsWhen(
            String whenCondition, 
            String parameterName,
            Map<String, V> argument) {
        if (argument != null) {
            for (String s : argument.keySet()) {
                if (s != null && s.isEmpty()) {
                    throw new IllegalArgumentException(
                            LAZY_RESOURCE.get().mustNotContainEmptyElementsWhen(whenCondition, parameterName));
                }
            }
        }
        return argument;
    }
    
    public static <K, V, M extends Map<K, V>> M mustNotContainSpecialKeysWhen(
            String whenCondition, 
            String parameterName,
            M argument,
            Class<?> clazz) {
        if (argument != null) {
            for (Object o : argument.keySet()) {
                if (o != null && clazz.isAssignableFrom(o.getClass())) {
                    throw new IllegalArgumentException(
                            LAZY_RESOURCE.get().mustNotContainSpecialKeysWhen(whenCondition, parameterName, clazz));
                }
            }
        }
        return argument;
    }
    
    public static <K, V, M extends Map<K, V>> M mustNotContainNullValuesWhen(
            String whenCondition, 
            String parameterName,
            M argument) {
        if (argument != null) {
            for (Object o : argument.values()) {
                if (o == null) {
                    throw new IllegalArgumentException(
                            LAZY_RESOURCE.get().mustNotContainNullValuesWhen(whenCondition, parameterName));
                }
            }
        }
        return argument;
    }
    
    public static <K> Map<K, String> mustNotContainEmptyValuesWhen(
            String whenCondition, 
            String parameterName,
            Map<K, String> argument) {
        if (argument != null) {
            for (String s : argument.values()) {
                if (s != null && s.isEmpty()) {
                    throw new IllegalArgumentException(
                            LAZY_RESOURCE.get().mustNotContainEmptyElementsWhen(whenCondition, parameterName));
                }
            }
        }
        return argument;
    }
    
    public static <K, V, M extends Map<K, V>> M mustNotContainSpecialValuesWhen(
            String whenCondition, 
            String parameterName,
            M argument,
            Class<?> clazz) {
        if (argument != null) {
            for (Object o : argument.values()) {
                if (o != null && clazz.isAssignableFrom(o.getClass())) {
                    throw new IllegalArgumentException(
                            LAZY_RESOURCE.get().mustNotContainSpecialValuesWhen(whenCondition, parameterName, clazz));
                }
            }
        }
        return argument;
    }
    
    public static <T> Class<T> mustBeClassWhen(String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null) {
            if (argument.isInterface() ||
                    argument.isEnum() ||
                    argument.isAnnotation() ||
                    argument.isArray() ||
                    argument.isPrimitive()) {
                throw new IllegalArgumentException(LAZY_RESOURCE.get().mustBeClassWhen(whenCondition, parameterName));
            }
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeClassWhen(String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null) {
            if (!argument.isInterface() &&
                    !argument.isEnum() &&
                    !argument.isAnnotation() &&
                    !argument.isArray() &&
                    !argument.isPrimitive()) {
                throw new IllegalArgumentException(LAZY_RESOURCE.get().mustNotBeClassWhen(whenCondition, parameterName));
            }
        }
        return argument;
    }
    
    public static <T> Class<T> mustBeInterfaceWhen(
            String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null && !argument.isInterface()) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().mustBeInterfaceWhen(whenCondition, parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeInterfaceWhen(
            String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null && argument.isInterface()) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().mustNotBeInterfaceWhen(whenCondition, parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustBeEnumWhen(
            String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null && !argument.isEnum()) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().mustBeEnumWhen(whenCondition, parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeEnumWhen(
            String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null && argument.isEnum()) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().mustNotBeEnumWhen(whenCondition, parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustBeAnnotationWhen(
            String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null && !argument.isAnnotation()) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().mustBeAnnotationWhen(whenCondition, parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeAnnotationWhen(
            String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null && argument.isAnnotation()) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().mustNotBeAnnotationWhen(whenCondition, parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustBeArrayWhen(
            String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null && !argument.isArray()) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().mustBeArrayWhen(whenCondition, parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeArrayWhen(
            String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null && argument.isArray()) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().mustNotBeArrayWhen(whenCondition, parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustBePrimitiveWhen(
            String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null && !argument.isPrimitive()) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().mustBePrimitiveWhen(whenCondition, parameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustNotBePrimitiveWhen(
            String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null && argument.isPrimitive()) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().mustNotBePrimitiveWhen(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static <T> Class<T> mustBeAbstractWhen(
            String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null && !Modifier.isAbstract(argument.getModifiers())) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().mustBeAbstractWhen(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static <T> Class<T> mustNotBeAbstractWhen(
            String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null && Modifier.isAbstract(argument.getModifiers())) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().mustNotBeAbstractWhen(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static <T> Class<T> mustBeFinalWhen(
            String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null && !Modifier.isFinal(argument.getModifiers())) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().mustBeFinalWhen(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static <T> Class<T> mustNotBeFinalWhen(
            String whenCondition, String parameterName, Class<T> argument) {
        if (argument != null && Modifier.isFinal(argument.getModifiers())) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().mustNotBeFinalWhen(whenCondition, parameterName));
        }
        return argument;
    }
    
    public static char mustNotBeEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            char argument,
            char value) {
        if (argument == value) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToValueWhen(whenCondition, parameterName, Character.toString(value)));
        }
        return argument;
    }
    
    public static char mustBeGreaterThanValueWhen(
            String whenCondition, 
            String parameterName,
            char argument,
            char minimumValue) {
        if (argument <= minimumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanValueWhen(whenCondition, parameterName, Character.toString(minimumValue)));
        }
        return argument;
    }
    
    public static char mustBeGreaterThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            char argument,
            char minimumValue) {
        if (argument < minimumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToValueWhen(whenCondition, parameterName, Character.toString(minimumValue)));
        }
        return argument;
    }
    
    public static char mustBeLessThanValueWhen(
            String whenCondition, 
            String parameterName,
            char argument,
            char maximumValue) {
        if (argument >= maximumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanValueWhen(whenCondition, parameterName, Character.toString(maximumValue)));
        }
        return argument;
    }
    
    public static char mustBeLessThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            char argument,
            char maximumValue) {
        if (argument > maximumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToValueWhen(whenCondition, parameterName, Character.toString(maximumValue)));
        }
        return argument;
    }
    
    public static char mustBetweenValueWhen(
            String whenCondition, 
            String parameterName,
            char argument,
            char minimumValue,
            boolean minimumInclusive,
            char maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenValueWhen(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            Character.toString(minimumValue),
                            maximumOp,
                            Character.toString(maximumValue)));
        }
        return argument;
    }

    public static byte mustNotBeEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            byte argument,
            byte value) {
        if (argument == value) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToValueWhen(whenCondition, parameterName, Byte.toString(value)));
        }
        return argument;
    }
    
    public static byte mustBeGreaterThanValueWhen(
            String whenCondition, 
            String parameterName,
            byte argument,
            byte minimumValue) {
        if (argument <= minimumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanValueWhen(whenCondition, parameterName, Byte.toString(minimumValue)));
        }
        return argument;
    }
    
    public static byte mustBeGreaterThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            byte argument,
            byte minimumValue) {
        if (argument < minimumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToValueWhen(whenCondition, parameterName, Byte.toString(minimumValue)));
        }
        return argument;
    }
    
    public static byte mustBeLessThanValueWhen(
            String whenCondition, 
            String parameterName,
            byte argument,
            byte maximumValue) {
        if (argument >= maximumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanValueWhen(whenCondition, parameterName, Byte.toString(maximumValue)));
        }
        return argument;
    }
    
    public static byte mustBeLessThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            byte argument,
            byte maximumValue) {
        if (argument > maximumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToValueWhen(whenCondition, parameterName, Byte.toString(maximumValue)));
        }
        return argument;
    }
    
    public static byte mustBetweenValueWhen(
            String whenCondition, 
            String parameterName,
            byte argument,
            byte minimumValue,
            boolean minimumInclusive,
            byte maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenValueWhen(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            Byte.toString(minimumValue),
                            maximumOp,
                            Byte.toString(maximumValue)));
        }
        return argument;
    }
    
    public static short mustNotBeEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            short argument,
            short value) {
        if (argument == value) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToValueWhen(whenCondition, parameterName, Short.toString(value)));
        }
        return argument;
    }
    
    public static short mustBeGreaterThanValueWhen(
            String whenCondition, 
            String parameterName,
            short argument,
            short minimumValue) {
        if (argument <= minimumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanValueWhen(whenCondition, parameterName, Short.toString(minimumValue)));
        }
        return argument;
    }
    
    public static short mustBeGreaterThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            short argument,
            short minimumValue) {
        if (argument < minimumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToValueWhen(whenCondition, parameterName, Short.toString(minimumValue)));
        }
        return argument;
    }
    
    public static short mustBeLessThanValueWhen(
            String whenCondition, 
            String parameterName,
            short argument,
            short maximumValue) {
        if (argument >= maximumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanValueWhen(whenCondition, parameterName, Short.toString(maximumValue)));
        }
        return argument;
    }
    
    public static short mustBeLessThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            short argument,
            short maximumValue) {
        if (argument > maximumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToValueWhen(whenCondition, parameterName, Short.toString(maximumValue)));
        }
        return argument;
    }
    
    public static short mustBetweenValueWhen(
            String whenCondition, 
            String parameterName,
            short argument,
            short minimumValue,
            boolean minimumInclusive,
            short maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenValueWhen(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            Short.toString(minimumValue),
                            maximumOp,
                            Short.toString(maximumValue)));
        }
        return argument;
    }
    
    public static int mustNotBeEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            int value) {
        if (argument == value) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToValueWhen(whenCondition, parameterName, Integer.toString(value)));
        }
        return argument;
    }
    
    public static int mustBeGreaterThanValueWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            int minimumValue) {
        if (argument <= minimumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanValueWhen(whenCondition, parameterName, Integer.toString(minimumValue)));
        }
        return argument;
    }
    
    public static int mustBeGreaterThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            int minimumValue) {
        if (argument < minimumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToValueWhen(whenCondition, parameterName, Integer.toString(minimumValue)));
        }
        return argument;
    }
    
    public static int mustBeLessThanValueWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            int maximumValue) {
        if (argument >= maximumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanValueWhen(whenCondition, parameterName, Integer.toString(maximumValue)));
        }
        return argument;
    }
    
    public static int mustBeLessThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            int maximumValue) {
        if (argument > maximumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToValueWhen(whenCondition, parameterName, Integer.toString(maximumValue)));
        }
        return argument;
    }
    
    public static int mustBetweenValueWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            int minimumValue,
            boolean minimumInclusive,
            int maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenValueWhen(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            Integer.toString(minimumValue),
                            maximumOp,
                            Integer.toString(maximumValue)));
        }
        return argument;
    }
    
    public static int indexMustBeGreaterThanValueWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            int minimumValue) {
        if (argument <= minimumValue) {
            throw new IndexOutOfBoundsException(
                    LAZY_RESOURCE.get().mustBeGreaterThanValueWhen(whenCondition, parameterName, Integer.toString(minimumValue)));
        }
        return argument;
    }
    
    public static int indexMustBeGreaterThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            int minimumValue) {
        if (argument < minimumValue) {
            throw new IndexOutOfBoundsException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToValueWhen(whenCondition, parameterName, Integer.toString(minimumValue)));
        }
        return argument;
    }
    
    public static int indexMustBeLessThanValueWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            int maximumValue) {
        if (argument >= maximumValue) {
            throw new IndexOutOfBoundsException(
                    LAZY_RESOURCE.get().mustBeLessThanValueWhen(whenCondition, parameterName, Integer.toString(maximumValue)));
        }
        return argument;
    }
    
    public static int indexMustBeLessThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            int maximumValue) {
        if (argument > maximumValue) {
            throw new IndexOutOfBoundsException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToValueWhen(whenCondition, parameterName, Integer.toString(maximumValue)));
        }
        return argument;
    }
    
    public static int indexMustBetweenValueWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            int minimumValue,
            boolean minimumInclusive,
            int maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IndexOutOfBoundsException(
                    LAZY_RESOURCE.get().mustBetweenValueWhen(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            Integer.toString(minimumValue),
                            maximumOp,
                            Integer.toString(maximumValue)));
        }
        return argument;
    }
    
    public static long mustNotBeEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            long argument,
            long value) {
        if (argument == value) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToValueWhen(whenCondition, parameterName, Long.toString(value)));
        }
        return argument;
    }
    
    public static long mustBeGreaterThanValueWhen(
            String whenCondition, 
            String parameterName,
            long argument,
            long minimumValue) {
        if (argument <= minimumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanValueWhen(whenCondition, parameterName, Long.toString(minimumValue)));
        }
        return argument;
    }
    
    public static long mustBeGreaterThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            long argument,
            long minimumValue) {
        if (argument < minimumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToValueWhen(whenCondition, parameterName, Long.toString(minimumValue)));
        }
        return argument;
    }
    
    public static long mustBeLessThanValueWhen(
            String whenCondition, 
            String parameterName,
            long argument,
            long maximumValue) {
        if (argument >= maximumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanValueWhen(whenCondition, parameterName, Long.toString(maximumValue)));
        }
        return argument;
    }
    
    public static long mustBeLessThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            long argument,
            long maximumValue) {
        if (argument > maximumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToValueWhen(whenCondition, parameterName, Long.toString(maximumValue)));
        }
        return argument;
    }
    
    public static long mustBetweenValueWhen(
            String whenCondition, 
            String parameterName,
            long argument,
            long minimumValue,
            boolean minimumInclusive,
            long maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenValueWhen(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            Long.toString(minimumValue),
                            maximumOp,
                            Long.toString(maximumValue)));
        }
        return argument;
    }
    
    public static float mustNotBeEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            float argument,
            float value) {
        if (argument == value) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToValueWhen(whenCondition, parameterName, Float.toString(value)));
        }
        return argument;
    }
    
    public static float mustBeGreaterThanValueWhen(
            String whenCondition, 
            String parameterName,
            float argument,
            float minimumValue) {
        if (argument <= minimumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanValueWhen(whenCondition, parameterName, Float.toString(minimumValue)));
        }
        return argument;
    }
    
    public static float mustBeGreaterThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            float argument,
            float minimumValue) {
        if (argument < minimumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToValueWhen(whenCondition, parameterName, Float.toString(minimumValue)));
        }
        return argument;
    }
    
    public static float mustBeLessThanValueWhen(
            String whenCondition, 
            String parameterName,
            float argument,
            float maximumValue) {
        if (argument >= maximumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanValueWhen(whenCondition, parameterName, Float.toString(maximumValue)));
        }
        return argument;
    }
    
    public static float mustBeLessThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            float argument,
            float maximumValue) {
        if (argument > maximumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToValueWhen(whenCondition, parameterName, Float.toString(maximumValue)));
        }
        return argument;
    }
    
    public static float mustBetweenValueWhen(
            String whenCondition, 
            String parameterName,
            float argument,
            float minimumValue,
            boolean minimumInclusive,
            float maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenValueWhen(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            Float.toString(minimumValue),
                            maximumOp,
                            Float.toString(maximumValue)));
        }
        return argument;
    }
    
    public static double mustNotBeEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            double argument,
            double value) {
        if (argument == value) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToValueWhen(whenCondition, parameterName, Double.toString(value)));
        }
        return argument;
    }
    
    public static double mustBeGreaterThanValueWhen(
            String whenCondition, 
            String parameterName,
            double argument,
            double minimumValue) {
        if (argument <= minimumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanValueWhen(whenCondition, parameterName, Double.toString(minimumValue)));
        }
        return argument;
    }
    
    public static double mustBeGreaterThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            double argument,
            double minimumValue) {
        if (argument < minimumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToValueWhen(whenCondition, parameterName, Double.toString(minimumValue)));
        }
        return argument;
    }
    
    public static double mustBeLessThanValueWhen(
            String whenCondition, 
            String parameterName,
            double argument,
            double maximumValue) {
        if (argument >= maximumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanValueWhen(whenCondition, parameterName, Double.toString(maximumValue)));
        }
        return argument;
    }
    
    public static double mustBeLessThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            double argument,
            double maximumValue) {
        if (argument > maximumValue) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToValueWhen(whenCondition, parameterName, Double.toString(maximumValue)));
        }
        return argument;
    }
    
    public static double mustBetweenValueWhen(
            String whenCondition, 
            String parameterName,
            double argument,
            double minimumValue,
            boolean minimumInclusive,
            double maximumValue,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumValue) {
                throwable = true;
            }
        } else {
            if (argument <= minimumValue) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumValue) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumValue) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenValueWhen(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            Double.toString(minimumValue),
                            maximumOp,
                            Double.toString(maximumValue)));
        }
        return argument;
    }
    
    public static <T> T mustNotBeEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            T argument,
            T value) {
        if (argument != null && value != null && argument.equals(value)) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToValueWhen(whenCondition, parameterName, value.toString()));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeGreaterThanValueWhen(
            String whenCondition, 
            String parameterName,
            T argument,
            T minimumValue) {
        if (argument != null && minimumValue != null && argument.compareTo(minimumValue) <= 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanValueWhen(whenCondition, parameterName, minimumValue.toString()));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeGreaterThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            T argument,
            T minimumValue) {
        if (argument != null && minimumValue != null && argument.compareTo(minimumValue) < 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToValueWhen(whenCondition, parameterName, minimumValue.toString()));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeLessThanValueWhen(
            String whenCondition, 
            String parameterName,
            T argument,
            T maximumValue) {
        if (argument != null && maximumValue != null && argument.compareTo(maximumValue) >= 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanValueWhen(whenCondition, parameterName, maximumValue.toString()));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeLessThanOrEqualToValueWhen(
            String whenCondition, 
            String parameterName,
            T argument,
            T maximumValue) {
        if (argument != null && maximumValue != null && argument.compareTo(maximumValue) > 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToValueWhen(whenCondition, parameterName, maximumValue.toString()));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBetweenValueWhen(
            String whenCondition, 
            String parameterName,
            T argument,
            T minimumValue,
            boolean minimumInclusive,
            T maximumValue,
            boolean maximumInclusive) {
        if (argument == null) {
            return null;
        }
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumValue != null) {
            if (minimumInclusive) {
                if (argument.compareTo(minimumValue) < 0) {
                    throwable = true;
                }
            } else {
                if (argument.compareTo(minimumValue) <= 0) {
                    throwable = true;
                }
            }
        }
        if (!throwable && maximumValue != null) {
            if (maximumInclusive) {
                if (argument.compareTo(maximumValue) > 0) {
                    throwable = true;
                }
            } else {
                if (argument.compareTo(maximumValue) >= 0) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenValueWhen(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            minimumValue == null ? null : maximumValue.toString(),
                            maximumOp,
                            maximumValue == null ? null : maximumValue.toString()));
        }
        return argument;
    }
    
    public static <T> T mustBeInstanceOfValueWhen(
            String whenCondition, 
            String parameterName,
            T argument,
            Class<?> classValue) {
        if (argument != null && !classValue.isAssignableFrom(argument.getClass())) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeInstanceOfValueWhen(whenCondition, parameterName, classValue));
        }
        return argument;
    }

    public static <T> T mustBeInstanceOfAllOfValueWhen(
            String whenCondition, 
            String parameterName,
            T argument,
            Class<?> ... classesValue) {
        if  (argument != null) {
            Class<? extends Object> argumentClass = argument.getClass();
            for (Class<?> clazz : classesValue) {
                if (clazz != null) {
                    if (!clazz.isAssignableFrom(argumentClass)) {
                        throw new IllegalArgumentException(
                                LAZY_RESOURCE.get().mustBeInstanceOfAllOfValueWhen(whenCondition, parameterName, classesValue)
                        );
                    }
                }
            }
        }
        return argument;
    }

    public static <T> T mustBeInstanceOfAnyOfValueWhen(
            String whenCondition, 
            String parameterName,
            T argument,
            Class<?> ... classesValue) {
        if  (argument != null && classesValue.length != 0) {
            Class<? extends Object> argumentClass = argument.getClass();
            boolean throwable = false;
            for (Class<?> clazz : classesValue) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argumentClass)) {
                        return argument;
                    }
                    throwable = true;
                }
            }
            if (throwable) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustBeInstanceOfAnyOfValueWhen(whenCondition, parameterName, classesValue)
                );
            }
        }
        return argument;
    }

    public static <T> T mustNotBeInstanceOfValueWhen(
            String whenCondition, 
            String parameterName,
            T argument,
            Class<?> classValue) {
        if (argument != null && classValue.isAssignableFrom(argument.getClass())) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeInstanceOfValueWhen(whenCondition, parameterName, classValue));
        }
        return argument;
    }

    public static <T> T mustNotBeInstanceOfAnyOfValueWhen(
            String whenCondition, 
            String parameterName,
            T argument,
            Class<?> ... classesValue) {
        if  (argument != null && classesValue.length != 0) {
            Class<? extends Object> argumentClass = argument.getClass();
            for (Class<?> clazz : classesValue) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argumentClass)) {
                        throw new IllegalArgumentException(
                                LAZY_RESOURCE.get().mustNotBeInstanceOfAnyOfValueWhen(whenCondition, parameterName, classesValue)
                        );
                    }
                }
            }
        }
        return argument;
    }

    public static <T> Class<T> mustBeCompatibleWithValueWhen(
            String whenCondition, 
            String parameterName,
            Class<T> argument,
            Class<?> classValue) {
        if (argument != null && !classValue.isAssignableFrom(argument)) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeCompatibleWithValueWhen(whenCondition, parameterName, classValue));
        }
        return argument;
    }

    public static <T> Class<T> mustBeCompatibleWithAllOfValueWhen(
            String whenCondition, 
            String parameterName,
            Class<T> argument,
            Class<?> ... classesValue) {
        if  (argument != null) {
            for (Class<?> clazz : classesValue) {
                if (clazz != null) {
                    if (!clazz.isAssignableFrom(argument)) {
                        throw new IllegalArgumentException(
                                LAZY_RESOURCE.get().mustBeCompatibleWithAllOfValueWhen(whenCondition, parameterName, classesValue)
                        );
                    }
                }
            }
        }
        return argument;
    }

    public static <T> Class<T> mustBeCompatibleWithAnyOfValueWhen(
            String whenCondition, 
            String parameterName,
            Class<T> argument,
            Class<?> ... classesValue) {
        if  (argument != null && classesValue.length != 0) {
            boolean throwable = false;
            for (Class<?> clazz : classesValue) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argument)) {
                        return argument;
                    }
                    throwable = true;
                }
            }
            if (throwable) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustBeCompatibleWithAnyOfValueWhen(whenCondition, parameterName, classesValue)
                );
            }
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeCompatibleWithValueWhen(
            String whenCondition, 
            String parameterName,
            Class<T> argument,
            Class<?> classValue) {
        if (argument != null && classValue.isAssignableFrom(argument)) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeCompatibleWithValueWhen(whenCondition, parameterName, classValue));
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeCompatibleWithAnyOfValueWhen(
            String whenCondition, 
            String parameterName,
            Class<T> argument,
            Class<?> ... classesValue) {
        if  (argument != null && classesValue.length != 0) {
            for (Class<?> clazz : classesValue) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argument)) {
                        throw new IllegalArgumentException(
                                LAZY_RESOURCE.get().mustNotBeCompatibleWithAnyOfValueWhen(whenCondition, parameterName, classesValue)
                        );
                    }
                }
            }
        }
        return argument;
    }
    
    public static char mustBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            char argument, 
            char ... charactersValue) {
        for (char c : charactersValue) {
            if (c == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfValueWhen(whenCondition, parameterName, MACollections.wrap(charactersValue))
        );
    }
    
    public static char mustNotBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            char argument, 
            char ... charactersValue) {
        for (char c : charactersValue) {
            if (c == argument) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfValueWhen(whenCondition, parameterName, MACollections.wrap(charactersValue))
                );
            }
        }
        return argument;
    }
    
    public static byte mustBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            byte argument, 
            byte ... bytesValue) {
        for (byte b : bytesValue) {
            if (b == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfValueWhen(whenCondition, parameterName, MACollections.wrap(bytesValue))
        );
    }
    
    public static byte mustNotBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            byte argument, 
            byte ... bytesValue) {
        for (byte b : bytesValue) {
            if (b == argument) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfValueWhen(whenCondition, parameterName, MACollections.wrap(bytesValue))
                );
            }
        }
        return argument;
    }
    
    public static short mustBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            short argument, 
            short ... shortsValue) {
        for (short s : shortsValue) {
            if (s == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfValueWhen(whenCondition, parameterName, MACollections.wrap(shortsValue))
        );
    }
    
    public static short mustNotBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            short argument, 
            short ... shortsValue) {
        for (short s : shortsValue) {
            if (s == argument) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfValueWhen(whenCondition, parameterName, MACollections.wrap(shortsValue))
                );
            }
        }
        return argument;
    }
    
    public static int mustBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            int argument, 
            int ... intsValue) {
        for (int i : intsValue) {
            if (i == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfValueWhen(whenCondition, parameterName, MACollections.wrap(intsValue))
        );
    }
    
    public static int mustNotBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            int argument, 
            int ... intsValue) {
        for (int i : intsValue) {
            if (i == argument) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfValueWhen(whenCondition, parameterName, MACollections.wrap(intsValue))
                );
            }
        }
        return argument;
    }
    
    public static long mustBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            long argument, 
            long ... longsValue) {
        for (long l : longsValue) {
            if (l == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfValueWhen(whenCondition, parameterName, MACollections.wrap(longsValue))
        );
    }
    
    public static long mustNotBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            long argument, 
            long ... longsValue) {
        for (long l : longsValue) {
            if (l == argument) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfValueWhen(whenCondition, parameterName, MACollections.wrap(longsValue))
                );
            }
        }
        return argument;
    }
    
    public static float mustBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            float argument, 
            float ... floatsValue) {
        for (float f : floatsValue) {
            if (f == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfValueWhen(whenCondition, parameterName, MACollections.wrap(floatsValue))
        );
    }
    
    public static float mustNotBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            float argument, 
            float ... floatsValue) {
        for (float f : floatsValue) {
            if (f == argument) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfValueWhen(whenCondition, parameterName, MACollections.wrap(floatsValue))
                );
            }
        }
        return argument;
    }
    
    public static double mustBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            double argument, 
            double ... doublesValue) {
        for (double d : doublesValue) {
            if (d == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfValueWhen(whenCondition, parameterName, MACollections.wrap(doublesValue))
        );
    }

    public static double mustNotBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            double argument, 
            double ... doublesValue) {
        for (double d : doublesValue) {
            if (d == argument) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfValueWhen(whenCondition, parameterName, MACollections.wrap(doublesValue))
                );
            }
        }
        return argument;
    }

    @SafeVarargs
    public static <T> T mustBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            T argument, 
            T ... objectsValue) {
        if (argument == null) {
            return null;
        }
        for (T o : objectsValue) {
            if (argument.equals(o)) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfValueWhen(whenCondition, parameterName, MACollections.wrap(objectsValue))
        );
    }
    
    @SafeVarargs
    public static <T> T mustNotBeAnyOfValueWhen(
            String whenCondition,
            String parameterName, 
            T argument, 
            T ... objectsValue) {
        if (argument == null) {
            return null;
        }
        for (T o : objectsValue) {
            if (argument.equals(o)) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfValueWhen(whenCondition, parameterName, MACollections.wrap(objectsValue))
                );
            }
        }
        return argument;
    }
    
    public static char mustBeEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            char argument,
            String valueParameterName,
            char valueArgument) {
        if (argument != valueArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEqualToOtherWhen(whenCondition, parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static char mustNotBeEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            char argument,
            String valueParameterName,
            char valueArgument) {
        if (argument == valueArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToOtherWhen(whenCondition, parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static char mustBeGreaterThanOtherWhen(
            String whenCondition, 
            String parameterName,
            char argument,
            String minimumParameterName,
            char minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOtherWhen(whenCondition, parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static char mustBeGreaterThanOrEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            char argument,
            String minimumParameterName, 
            char minimumArgument) {
        if (argument < minimumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToOtherWhen(whenCondition, parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static char mustBeLessThanOtherWhen(
            String whenCondition, 
            String parameterName,
            char argument,
            String maximumParameterName,
            char maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOtherWhen(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static char mustBeLessThanOrEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            char argument,
            String maximumParameterName,
            char maximumArgument) {
        if (argument > maximumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToOtherWhen(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static char mustBetweenOtherWhen(
            String whenCondition, 
            String parameterName,
            char argument,
            String minimumParameterName,
            char minimumArgument,
            String maximumParameterName,
            boolean minimumInclusive,
            char maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenOtherWhen(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static byte mustBeEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            byte argument,
            String valueParameterName,
            byte valueArgument) {
        if (argument != valueArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEqualToOtherWhen(whenCondition, parameterName, valueParameterName));
        }
        return argument;
    }

    public static byte mustNotBeEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            byte argument,
            String valueParamterName,
            byte valueArgument) {
        if (argument == valueArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToOtherWhen(whenCondition, parameterName, valueParamterName));
        }
        return argument;
    }
    
    public static byte mustBeGreaterThanOtherWhen(
            String whenCondition, 
            String parameterName,
            byte argument,
            String minimumArgumentName,
            byte minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOtherWhen(whenCondition, parameterName, minimumArgumentName));
        }
        return argument;
    }
    
    public static byte mustBeGreaterThanOrEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            byte argument,
            String minmumParameterName,
            byte minimumArgument) {
        if (argument < minimumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToOtherWhen(whenCondition, parameterName, minmumParameterName));
        }
        return argument;
    }
    
    public static byte mustBeLessThanOtherWhen(
            String whenCondition, 
            String parameterName,
            byte argument,
            String maximumParameterName,
            byte maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOtherWhen(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static byte mustBeLessThanOrEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            byte argument,
            String maximumParameterName,
            byte maximumArgument) {
        if (argument > maximumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToOtherWhen(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static byte mustBetweenOtherWhen(
            String whenCondition, 
            String parameterName,
            byte argument,
            String minimumParameterName,
            byte minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            byte maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenOtherWhen(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static short mustBeEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            short argument,
            String valueParameterName,
            short valueArgument) {
        if (argument != valueArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEqualToOtherWhen(whenCondition, parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static short mustNotBeEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            short argument,
            String valueParameterName,
            short valueArgument) {
        if (argument == valueArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToOtherWhen(whenCondition, parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static short mustBeGreaterThanOtherWhen(
            String whenCondition, 
            String parameterName,
            short argument,
            String minimumParameterName,
            short minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOtherWhen(whenCondition, parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static short mustBeGreaterThanOrEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            short argument,
            String minimumParameterName,
            short minimumArgument) {
        if (argument < minimumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToOtherWhen(whenCondition, parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static short mustBeLessThanOtherWhen(
            String whenCondition, 
            String parameterName,
            short argument,
            String maximumParameterName,
            short maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOtherWhen(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static short mustBeLessThanOrEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            short argument,
            String maximumParameterName,
            short maximumArgument) {
        if (argument > maximumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToOtherWhen(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static short mustBetweenOtherWhen(
            String whenCondition, 
            String parameterName,
            short argument,
            String minimumParameterName,
            short minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            short maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenOtherWhen(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static int mustBeEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            String valueParameterName,
            int valueArgument) {
        if (argument != valueArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEqualToOtherWhen(whenCondition, parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static int mustNotBeEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            String valueParameterName,
            int valueArgument) {
        if (argument == valueArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToOtherWhen(whenCondition, parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static int mustBeGreaterThanOtherWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            String minimumParameterName,
            int minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOtherWhen(whenCondition, parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static int mustBeGreaterThanOrEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            String minimumParameterName,
            int minimumArgument) {
        if (argument < minimumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToOtherWhen(whenCondition, parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static int mustBeLessThanOtherWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            String maximumParameterName,
            int maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOtherWhen(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static int mustBeLessThanOrEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            String maximumParameterName,
            int maximumArgument) {
        if (argument > maximumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToOtherWhen(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static int mustBetweenOtherWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            String minimumParameterName,
            int minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            int maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenOtherWhen(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static int indexMustBeGreaterThanOtherWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            String minimumParameterName,
            int minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IndexOutOfBoundsException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOtherWhen(whenCondition, parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static int indexMustBeGreaterThanOrEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            String minimumParameterName,
            int minimumArgument) {
        if (argument < minimumArgument) {
            throw new IndexOutOfBoundsException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToOtherWhen(whenCondition, parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static int indexMustBeLessThanOtherWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            String maximumParameterName,
            int maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IndexOutOfBoundsException(
                    LAZY_RESOURCE.get().mustBeLessThanOtherWhen(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static int indexMustBeLessThanOrEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            String maximumParameterName,
            int maximumArgument) {
        if (argument > maximumArgument) {
            throw new IndexOutOfBoundsException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToOtherWhen(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static int indexMustBetweenOtherWhen(
            String whenCondition, 
            String parameterName,
            int argument,
            String minimumParameterName,
            int minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            int maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IndexOutOfBoundsException(
                    LAZY_RESOURCE.get().mustBetweenOtherWhen(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static long mustBeEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            long argument,
            String valueParameterName,
            long valueArgument) {
        if (argument != valueArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEqualToOtherWhen(whenCondition, parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static long mustNotBeEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            long argument,
            String valueParameterName,
            long valueArgument) {
        if (argument == valueArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToOtherWhen(whenCondition, parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static long mustBeGreaterThanOtherWhen(
            String whenCondition, 
            String parameterName,
            long argument,
            String minimumParmeterName,
            long minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOtherWhen(whenCondition, parameterName, minimumParmeterName));
        }
        return argument;
    }
    
    public static long mustBeGreaterThanOrEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            long argument,
            String minimumParameterName,
            long minimumArgument) {
        if (argument < minimumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToOtherWhen(
                            whenCondition, parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static long mustBeLessThanOtherWhen(
            String whenCondition, 
            String parameterName,
            long argument,
            String maximumParameterName,
            long maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOtherWhen(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static long mustBeLessThanOrEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            long argument,
            String maximumParameterName,
            long maximumArgument) {
        if (argument > maximumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToOtherWhen(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static long mustBetweenOtherWhen(
            String whenCondition, 
            String parameterName,
            long argument,
            String minimumParameterName,
            long minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            long maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenOtherWhen(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static float mustBeEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            float argument,
            String valueParameterName,
            float valueArgument) {
        if (argument != valueArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEqualToOtherWhen(whenCondition, parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static float mustNotBeEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            float argument,
            String valueParameterName,
            float valueArgument) {
        if (argument == valueArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToOtherWhen(whenCondition, parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static float mustBeGreaterThanOtherWhen(
            String whenCondition, 
            String parameterName,
            float argument,
            String minimumParameterName,
            float minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOtherWhen(whenCondition, parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static float mustBeGreaterThanOrEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            float argument,
            String minimumParameterName,
            float minimumArgument) {
        if (argument < minimumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToOtherWhen(whenCondition, parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static float mustBeLessThanOtherWhen(
            String whenCondition, 
            String parameterName,
            float argument,
            String maximumParameterName,
            float maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOtherWhen(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static float mustBeLessThanOrEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            float argument,
            String maximumParameterName,
            float maximumArgument) {
        if (argument > maximumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToOtherWhen(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static float mustBetweenOtherWhen(
            String whenCondition, 
            String parameterName,
            float argument,
            String minimumParameterName,
            float minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            float maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenOtherWhen(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static double mustBeEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            double argument,
            String valueParameterName,
            double valueArgument) {
        if (argument != valueArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEqualToOtherWhen(whenCondition, parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static double mustNotBeEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            double argument,
            String valueParameterName,
            double valueArgument) {
        if (argument == valueArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToOtherWhen(whenCondition, parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static double mustBeGreaterThanOtherWhen(
            String whenCondition, 
            String parameterName,
            double argument,
            String minimumParameterName,
            double minimumArgument) {
        if (argument <= minimumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOtherWhen(whenCondition, parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static double mustBeGreaterThanOrEqualToOtherWhen(
            String whenCondition, 
            String parameterName,
            double argument,
            String minimumParameterName,
            double minimumArgument) {
        if (argument < minimumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToOtherWhen(whenCondition, parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static double mustBeLessThanOtherWhen(String whenCondition, 
            String parameterName,
            double argument,
            String maximumParameterName,
            double maximumArgument) {
        if (argument >= maximumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOtherWhen(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static double mustBeLessThanOrEqualToOtherWhen(String whenCondition, 
            String parameterName,
            double argument,
            String maximumParameterName,
            double maximumArgument) {
        if (argument > maximumArgument) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToOtherWhen(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static double mustBetweenOtherWhen(String whenCondition, 
            String parameterName,
            double argument,
            String minimumParameterName,
            double minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            double maximumArgument,
            boolean maximumInclusive) {
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumInclusive) {
            if (argument < minimumArgument) {
                throwable = true;
            }
        } else {
            if (argument <= minimumArgument) {
                throwable = true;
            }
        }
        if (!throwable) {
            if (maximumInclusive) {
                if (argument > maximumArgument) {
                    throwable = true;
                }
            } else {
                if (argument >= maximumArgument) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenOtherWhen(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static <T> T mustBeEqualToOtherWhen(String whenCondition, 
            String parameterName,
            T argument,
            String valueParameterName,
            T valueArgument) {
        if (argument != null && valueArgument != null && !argument.equals(valueArgument)) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeEqualToOtherWhen(whenCondition, parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static <T> T mustNotBeEqualToOtherWhen(String whenCondition, 
            String parameterName,
            T argument,
            String valueParameterName,
            T valueArgument) {
        if (argument != null && valueArgument != null && argument.equals(valueArgument)) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeEqualToOtherWhen(whenCondition, parameterName, valueParameterName));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeGreaterThanOtherWhen(String whenCondition, 
            String parameterName,
            T argument,
            String minimumParameterName,
            T minimumArgument) {
        if (argument != null && minimumArgument != null && argument.compareTo(minimumArgument) <= 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOtherWhen(whenCondition, parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeGreaterThanOrEqualToOtherWhen(String whenCondition, 
            String parameterName,
            T argument,
            String minimumParameterName,
            T minimumArgument) {
        if (argument != null && minimumArgument != null && argument.compareTo(minimumArgument) < 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeGreaterThanOrEqualToOtherWhen(whenCondition, parameterName, minimumParameterName));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeLessThanOtherWhen(String whenCondition, 
            String parameterName,
            T argument,
            String maximumParameterName,
            T maximumArgument) {
        if (argument != null && maximumArgument != null && argument.compareTo(maximumArgument) >= 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOtherWhen(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBeLessThanOrEqualToOtherWhen(String whenCondition, 
            String parameterName,
            T argument,
            String maximumParameterName,
            T maximumArgument) {
        if (argument != null && maximumArgument != null && argument.compareTo(maximumArgument) > 0) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeLessThanOrEqualToOtherWhen(whenCondition, parameterName, maximumParameterName));
        }
        return argument;
    }
    
    public static <T extends Comparable<T>> T mustBetweenOtherWhen(String whenCondition, 
            String parameterName,
            T argument,
            String minimumParameterName,
            T minimumArgument,
            boolean minimumInclusive,
            String maximumParameterName,
            T maximumArgument,
            boolean maximumInclusive) {
        if (argument == null) {
            return null;
        }
        String minimumOp, maximumOp;
        if (minimumInclusive) {
            minimumOp = ">=";
        } else {
            minimumOp = ">";
        }
        if (maximumInclusive) {
            maximumOp = "<=";
        } else {
            maximumOp = "<";
        }
        boolean throwable = false;
        if (minimumArgument != null) {
            if (minimumInclusive) {
                if (argument.compareTo(minimumArgument) < 0) {
                    throwable = true;
                }
            } else {
                if (argument.compareTo(minimumArgument) <= 0) {
                    throwable = true;
                }
            }
        }
        if (!throwable && maximumArgument != null) {
            if (maximumInclusive) {
                if (argument.compareTo(maximumArgument) > 0) {
                    throwable = true;
                }
            } else {
                if (argument.compareTo(maximumArgument) >= 0) {
                    throwable = true;
                }
            }
        }
        if (throwable) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBetweenOtherWhen(
                            whenCondition, 
                            parameterName, 
                            minimumOp,
                            minimumParameterName,
                            maximumOp,
                            maximumParameterName));
        }
        return argument;
    }
    
    public static <T> T mustBeInstanceOfOtherWhen(String whenCondition, 
            String parameterName,
            T argument,
            String classParameterName,
            Class<?> classArgument) {
        if (argument != null && !classArgument.isAssignableFrom(argument.getClass())) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeInstanceOfOtherWhen(whenCondition, parameterName, classParameterName));
        }
        return argument;
    }

    public static <T> T mustBeInstanceOfAllOfOtherWhen(String whenCondition, 
            String parameterName,
            T argument,
            String classesParameterName,
            Class<?> ... classesArgument) {
        if  (argument != null) {
            Class<? extends Object> argumentClass = argument.getClass();
            for (Class<?> clazz : classesArgument) {
                if (clazz != null) {
                    if (!clazz.isAssignableFrom(argumentClass)) {
                        throw new IllegalArgumentException(
                                LAZY_RESOURCE.get().mustBeInstanceOfAllOfOtherWhen(whenCondition, parameterName, classesParameterName)
                        );
                    }
                }
            }
        }
        return argument;
    }

    public static <T> T mustBeInstanceOfAnyOfOtherWhen(String whenCondition, 
            String parameterName,
            T argument,
            String classesParameterName,
            Class<?> ... classesArgument) {
        if  (argument != null && classesArgument.length != 0) {
            Class<? extends Object> argumentClass = argument.getClass();
            boolean throwable = false;
            for (Class<?> clazz : classesArgument) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argumentClass)) {
                        return argument;
                    }
                    throwable = true;
                }
            }
            if (throwable) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustBeInstanceOfAnyOfOtherWhen(whenCondition, parameterName, classesParameterName)
                );
            }
        }
        return argument;
    }

    public static <T> T mustNotBeInstanceOfOtherWhen(String whenCondition, 
            String parameterName,
            T argument,
            String classParameterName,
            Class<?> classArgument) {
        if (argument != null && classArgument.isAssignableFrom(argument.getClass())) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeInstanceOfOtherWhen(whenCondition, parameterName, classParameterName));
        }
        return argument;
    }

    public static <T> T mustNotBeInstanceOfAnyOfOtherWhen(String whenCondition, 
            String parameterName,
            T argument,
            String classesParameterName,
            Class<?> ... classesArgument) {
        if  (argument != null && classesArgument.length != 0) {
            Class<? extends Object> argumentClass = argument.getClass();
            for (Class<?> clazz : classesArgument) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argumentClass)) {
                        throw new IllegalArgumentException(
                                LAZY_RESOURCE.get().mustNotBeInstanceOfAnyOfOtherWhen(whenCondition, parameterName, classesParameterName)
                        );
                    }
                }
            }
        }
        return argument;
    }

    public static <T> Class<T> mustBeCompatibleWithOtherWhen(String whenCondition, 
            String parameterName,
            Class<T> argument,
            String classParameterName,
            Class<?> classArgument) {
        if (argument != null && !classArgument.isAssignableFrom(argument)) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustBeCompatibleWithOtherWhen(whenCondition, parameterName, classParameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustBeCompatibleWithAllOfOtherWhen(String whenCondition, 
            String parameterName,
            Class<T> argument,
            String classesParameterName,
            Class<?> ... classesArgument) {
        if  (argument != null) {
            for (Class<?> clazz : classesArgument) {
                if (clazz != null) {
                    if (!clazz.isAssignableFrom(argument)) {
                        throw new IllegalArgumentException(
                                LAZY_RESOURCE.get().mustBeCompatibleWithAllOfOtherWhen(whenCondition, parameterName,classesParameterName)
                        );
                    }
                }
            }
        }
        return argument;
    }

    public static <T> Class<T> mustBeCompatibleWithAnyOfOtherWhen(String whenCondition, 
            String parameterName,
            Class<T> argument,
            String classesParameterName,
            Class<?> ... classesArgument) {
        if  (argument != null && classesArgument.length != 0) {
            boolean throwable = false;
            for (Class<?> clazz : classesArgument) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argument)) {
                        return argument;
                    }
                    throwable = true;
                }
            }
            if (throwable) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustBeCompatibleWithAnyOfOtherWhen(whenCondition, parameterName, classesParameterName)
                );
            }
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeCompatibleWithOtherWhen(String whenCondition, 
            String parameterName,
            Class<T> argument,
            String classParameterName,
            Class<?> classArgument) {
        if (argument != null && classArgument.isAssignableFrom(argument)) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().mustNotBeCompatibleWithOtherWhen(whenCondition, parameterName, classParameterName));
        }
        return argument;
    }

    public static <T> Class<T> mustNotBeCompatibleWithAnyOfOtherWhen(String whenCondition, 
            String parameterName,
            Class<T> argument,
            String classesParameterName,
            Class<?> ... classesArgument) {
        if  (argument != null && classesArgument.length != 0) {
            for (Class<?> clazz : classesArgument) {
                if (clazz != null) {
                    if (clazz.isAssignableFrom(argument)) {
                        throw new IllegalArgumentException(
                                LAZY_RESOURCE.get().mustNotBeCompatibleWithAnyOfOtherWhen(whenCondition, parameterName, classesParameterName)
                        );
                    }
                }
            }
        }
        return argument;
    }
    
    public static char mustBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            char argument, 
            String valueParameterName,
            char ... charactersValueArgument) {
        for (char c : charactersValueArgument) {
            if (c == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfOtherWhen(whenCondition, parameterName, valueParameterName)
        );
    }
    
    public static char mustNotBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            char argument, 
            String valueParameterName,
            char ... charactersValueArgument) {
        for (char c : charactersValueArgument) {
            if (c == argument) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfOtherWhen(whenCondition, parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }
    
    public static byte mustBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            byte argument, 
            String valueParameterName,
            byte ... bytesValueArgument) {
        for (byte b : bytesValueArgument) {
            if (b == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfOtherWhen(whenCondition, parameterName, valueParameterName)
        );
    }
    
    public static byte mustNotBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            byte argument, 
            String valueParameterName,
            byte ... bytesValueArgument) {
        for (byte b : bytesValueArgument) {
            if (b == argument) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfOtherWhen(whenCondition, parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }
    
    public static short mustBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            short argument, 
            String valueParameterName,
            short ... shortsValueArgument) {
        for (short s : shortsValueArgument) {
            if (s == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfOtherWhen(whenCondition, parameterName, valueParameterName)
        );
    }
    
    public static short mustNotBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            short argument, 
            String valueParameterName,
            short ... shortsValueArgument) {
        for (short s : shortsValueArgument) {
            if (s == argument) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfOtherWhen(whenCondition, parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }
    
    public static int mustBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            int argument, 
            String valueParameterName,
            int ... intsValueArgument) {
        for (int i : intsValueArgument) {
            if (i == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfOtherWhen(whenCondition, parameterName, valueParameterName)
        );
    }
    
    public static int mustNotBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            int argument, 
            String valueParameterName,
            int ... intsValueArgument) {
        for (int i : intsValueArgument) {
            if (i == argument) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfOtherWhen(whenCondition, parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }
    
    public static long mustBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            long argument, 
            String valueParameterName,
            long ... longsValueArgument) {
        for (long l : longsValueArgument) {
            if (l == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfOtherWhen(whenCondition, parameterName, valueParameterName)
        );
    }
    
    public static long mustNotBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            long argument, 
            String valueParameterName,
            long ... longsValueArgument) {
        for (long l : longsValueArgument) {
            if (l == argument) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfOtherWhen(whenCondition, parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }
    
    public static float mustBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            float argument, 
            String valueParameterName,
            float ... floatsValueArgument) {
        for (float f : floatsValueArgument) {
            if (f == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfOtherWhen(whenCondition, parameterName, valueParameterName)
        );
    }
    
    public static float mustNotBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            float argument, 
            String valueParameterName,
            float ... floatsValueArgument) {
        for (float f : floatsValueArgument) {
            if (f == argument) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfOtherWhen(whenCondition, parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }
    
    public static double mustBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            double argument, 
            String valueParameterName,
            double ... doublesValueArgument) {
        for (double d : doublesValueArgument) {
            if (d == argument) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfOtherWhen(whenCondition, parameterName, valueParameterName)
        );
    }

    public static double mustNotBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            double argument, 
            String valueParameterName,
            double ... doublesValueArgument) {
        for (double d : doublesValueArgument) {
            if (d == argument) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfOtherWhen(whenCondition, parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }

    @SafeVarargs
    public static <T> T mustBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            T argument, 
            String valueParameterName,
            T ... objectsValueArgument) {
        if (argument == null) {
            return null;
        }
        for (T o : objectsValueArgument) {
            if (argument.equals(o)) {
                return argument;
            }
        }
        throw new IllegalArgumentException(
                LAZY_RESOURCE.get().mustBeAnyOfOtherWhen(whenCondition, parameterName, valueParameterName)
        );
    }
    
    @SafeVarargs
    public static <T> T mustNotBeAnyOfOtherWhen(
            String whenCondition,
            String parameterName, 
            T argument, 
            String valueParameterName,
            T ... objectsValueArgument) {
        if (argument == null) {
            return null;
        }
        for (T o : objectsValueArgument) {
            if (argument.equals(o)) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().mustNotBeAnyOfOtherWhen(whenCondition, parameterName, valueParameterName)
                );
            }
        }
        return argument;
    }
    
    private static boolean isEmpty(Iterable<?> iterable) {
        if (iterable instanceof Collection<?>) {
            return ((Collection<?>)iterable).isEmpty();
        }
        Iterator<?> itr = iterable.iterator();
        return !itr.hasNext();
    } 
    
    private interface Resource {
        
        String mustBeNull(String parameterName);
        
        String mustNotBeNull(String parameterName);
        
        String mustNotBeEmpty(String parameterName);
        
        String mustNotContainNullElements(String parameterName);
        
        String mustNotContainEmptyElements(String parameterName);
        
        String mustNotContainSpecialElements(String parameterName, Class<?> classValue);
        
        String mustNotContainNullKeys(String parameterName);
        
        String mustNotContainEmptyKeys(String parameterName);
        
        String mustNotContainSpecialKeys(String parameterName, Class<?> classValue);
        
        String mustNotContainNullValues(String parameterName);
        
        String mustNotContainEmptyValues(String parameterName);
        
        String mustNotContainSpecialValues(String parameterName, Class<?> classValue);
        
        String mustBeClass(String parameter);
        
        String mustNotBeClass(String parameter);
        
        String mustBeInterface(String parameter);
        
        String mustNotBeInterface(String parameter);
        
        String mustBeEnum(String parameter);
        
        String mustNotBeEnum(String parameter);
        
        String mustBeAnnotation(String parameter);
        
        String mustNotBeAnnotation(String parameter);
        
        String mustBeArray(String parameter);
        
        String mustNotBeArray(String parameter);
        
        String mustBePrimitive(String parameter);
        
        String mustNotBePrimitive(String parameter);
        
        String mustBeBox(String parameter, Collection<Class<?>> boxTypes);
        
        String mustNotBeBox(String parameter, Collection<Class<?>> boxTypes);
        
        String mustBeAbstract(String parameter);
        
        String mustNotBeAbstract(String parameter);
        
        String mustBeFinal(String parameter);
        
        String mustNotBeFinal(String parameter);
        
        String mustBeEqualToValue(String parameterName, String value);
        
        String mustNotBeEqualToValue(String parameterName, String value);
        
        String mustBeGreaterThanValue(String parameterName, String value);
        
        String mustBeGreaterThanOrEqualToValue(String parameterName, String value);
        
        String mustBeLessThanValue(String parameterName, String value);
        
        String mustBeLessThanOrEqualToValue(String parameterName, String value);
        
        String mustBetweenValue(
                String parameterName, 
                String minimumOp,
                String minimumValue,
                String maximumOp,
                String maximumValue);
        
        String mustBeInstanceOfValue(String parameterName, Class<?> classValue);
        
        String mustBeInstanceOfAllOfValue(String parameterName, Class<?>[] classesValue);
        
        String mustBeInstanceOfAnyOfValue(String parameterName, Class<?>[] classesValue);
        
        String mustNotBeInstanceOfValue(String parameterName, Class<?> classValue);
        
        String mustNotBeInstanceOfAnyOfValue(String parameterName, Class<?>[] classesValue);
        
        String mustBeCompatibleWithValue(String parameterName, Class<?> classValue);
        
        String mustBeCompatibleWithAllOfValue(String parameterName, Class<?>[] classesValue);
        
        String mustBeCompatibleWithAnyOfValue(String parameterName, Class<?>[] classesValue);
        
        String mustNotBeCompatibleWithValue(String parameterName, Class<?> classValue);
        
        String mustNotBeCompatibleWithAnyOfValue(String parameterName, Class<?>[] classesValue);
        
        String mustBeAnyOfValue(String parameterName, Collection<?> collectionValue);
        
        String mustNotBeAnyOfValue(String parameterName, Collection<?> collectionValue);
        
        String mustBeEqualToOther(String parameterName, String valueParameterName);
        
        String mustNotBeEqualToOther(String parameterName, String valueParameterName);
        
        String mustBeGreaterThanOther(String parameterName, String valueParameterName);
        
        String mustBeGreaterThanOrEqualToOther(String parameterName, String valueParameterName);
        
        String mustBeLessThanOther(String parameterName, String valueParameterName);
        
        String mustBeLessThanOrEqualToOther(String parameterName, String valueParameterName);
        
        String mustBetweenOther(
                String parameterName, 
                String minimumOp,
                String minimumParameterName,
                String maximumOp,
                String maximumParameterName);
        
        String mustBeInstanceOfOther(String parameterName, String classParameterName);
        
        String mustBeInstanceOfAllOfOther(String parameterName, String classesParameterName);
        
        String mustBeInstanceOfAnyOfOther(String parameterName, String classesParameterName);
        
        String mustNotBeInstanceOfOther(String parameterName, String classParameterName);
        
        String mustNotBeInstanceOfAnyOfOther(String parameterName, String classesParameterName);
        
        String mustBeCompatibleWithOther(String parameterName, String classParameterName);
        
        String mustBeCompatibleWithAllOfOther(String parameterName, String classesParameterName);
        
        String mustBeCompatibleWithAnyOfOther(String parameterName, String classesParameterName);
        
        String mustNotBeCompatibleWithOther(String parameterName, String classParameterName);
        
        String mustNotBeCompatibleWithAnyOfOther(String parameterName, String classesParameterName);
        
        String mustBeAnyOfOther(String parameterName, String valueParameterName);
        
        String mustNotBeAnyOfOther(String parameterName, String valueParameterName);
        
        String mustBeNullWhen(String whenCondition, String parameterName);
        
        String mustNotBeNullWhen(String whenCondition, String parameterName);
        
        String mustBeEmptyWhen(String whenCondition, String parameterName);
        
        String mustNotBeEmptyWhen(String whenCondition, String parameterName);
        
        String mustNotContainNullElementsWhen(String whenCondition, String parameterName);
        
        String mustNotContainEmptyElementsWhen(String whenCondition, String parameterName);
        
        String mustNotContainSpecialElementsWhen(String whenCondition, String parameterName, Class<?> classValue);
        
        String mustNotContainNullKeysWhen(String whenCondition, String parameterName);
        
        String mustNotContainEmptyKeysWhen(String whenCondition, String parameterName);
        
        String mustNotContainSpecialKeysWhen(String whenCondition, String parameterName, Class<?> classValue);
        
        String mustNotContainNullValuesWhen(String whenCondition, String parameterName);
        
        String mustNotContainEmptyValuesWhen(String whenCondition, String parameterName);
        
        String mustNotContainSpecialValuesWhen(String whenCondition, String parameterName, Class<?> classValue);
        
        String mustBeClassWhen(String widthCondition, String parameter);
        
        String mustNotBeClassWhen(String widthCondition, String parameter);
        
        String mustBeInterfaceWhen(String whenCondition, String parameter);
        
        String mustNotBeInterfaceWhen(String whenCondition, String parameter);
        
        String mustBeEnumWhen(String whenCondition, String parameter);
        
        String mustNotBeEnumWhen(String whenCondition, String parameter);
        
        String mustBeAnnotationWhen(String whenCondition, String parameter);
        
        String mustNotBeAnnotationWhen(String whenCondition, String parameter);
        
        String mustBeArrayWhen(String whenCondition, String parameter);
        
        String mustNotBeArrayWhen(String whenCondition, String parameter);
        
        String mustBePrimitiveWhen(String whenCondition, String parameter);
        
        String mustNotBePrimitiveWhen(String whenCondition, String parameter);
        
        String mustBeAbstractWhen(String whenCondition, String parameter);
        
        String mustNotBeAbstractWhen(String whenCondition, String parameter);
        
        String mustBeFinalWhen(String whenCondition, String parameter);
        
        String mustNotBeFinalWhen(String whenCondition, String parameter);
        
        String mustNotBeEqualToValueWhen(String whenCondition, String parameterName, String value);
        
        String mustBeGreaterThanValueWhen(String whenCondition, String parameterName, String value);
        
        String mustBeGreaterThanOrEqualToValueWhen(String whenCondition, String parameterName, String value);
        
        String mustBeLessThanValueWhen(String whenCondition, String parameterName, String value);
        
        String mustBeLessThanOrEqualToValueWhen(String whenCondition, String parameterName, String value);
        
        String mustBetweenValueWhen(String whenCondition, 
                String parameterName, 
                String minimumOp,
                String minimumValue,
                String maximumOp,
                String maximumValue);
        
        String mustBeInstanceOfValueWhen(String whenCondition, String parameterName, Class<?> classValue);
        
        String mustBeInstanceOfAllOfValueWhen(String whenCondition, String parameterName, Class<?>[] classesValue);
        
        String mustBeInstanceOfAnyOfValueWhen(String whenCondition, String parameterName, Class<?>[] classesValue);
        
        String mustNotBeInstanceOfValueWhen(String whenCondition, String parameterName, Class<?> classValue);
        
        String mustNotBeInstanceOfAnyOfValueWhen(String whenCondition, String parameterName, Class<?>[] classesValue);
        
        String mustBeCompatibleWithValueWhen(String whenCondition, String parameterName, Class<?> classValue);
        
        String mustBeCompatibleWithAllOfValueWhen(String whenCondition, String parameterName, Class<?>[] classesValue);
        
        String mustBeCompatibleWithAnyOfValueWhen(String whenCondition, String parameterName, Class<?>[] classesValue);
        
        String mustNotBeCompatibleWithValueWhen(String whenCondition, String parameterName, Class<?> classValue);
        
        String mustNotBeCompatibleWithAnyOfValueWhen(String whenCondition, String parameterName, Class<?>[] classesValue);
        
        String mustBeAnyOfValueWhen(String whenCondition, String parameterName, Collection<?> collectionValue);
        
        String mustNotBeAnyOfValueWhen(String whenCondition, String parameterName, Collection<?> collectionValue);
        
        String mustBeEqualToOtherWhen(String whenCondition, String parameterName, String valueParameterName);
        
        String mustNotBeEqualToOtherWhen(String whenCondition, String parameterName, String valueParameterName);
        
        String mustBeGreaterThanOtherWhen(String whenCondition, String parameterName, String valueParameterName);
        
        String mustBeGreaterThanOrEqualToOtherWhen(String whenCondition, String parameterName, String valueParameterName);
        
        String mustBeLessThanOtherWhen(String whenCondition, String parameterName, String valueParameterName);
        
        String mustBeLessThanOrEqualToOtherWhen(String whenCondition, String parameterName, String valueParameterName);
        
        String mustBetweenOtherWhen(String whenCondition, 
                String parameterName, 
                String minimumOp,
                String minimumParameterName,
                String maximumOp,
                String maximumParameterName);
        
        String mustBeInstanceOfOtherWhen(String whenCondition, String parameterName, String classParameterName);
        
        String mustBeInstanceOfAllOfOtherWhen(String whenCondition, String parameterName, String classesParameterName);
        
        String mustBeInstanceOfAnyOfOtherWhen(String whenCondition, String parameterName, String classesParameterName);
        
        String mustNotBeInstanceOfOtherWhen(String whenCondition, String parameterName, String classParameterName);
        
        String mustNotBeInstanceOfAnyOfOtherWhen(String whenCondition, String parameterName, String classesParameterName);
        
        String mustBeCompatibleWithOtherWhen(String whenCondition, String parameterName, String classParameterName);
        
        String mustBeCompatibleWithAllOfOtherWhen(String whenCondition, String parameterName, String classesParameterName);
        
        String mustBeCompatibleWithAnyOfOtherWhen(String whenCondition, String parameterName, String classesParameterName);
        
        String mustNotBeCompatibleWithOtherWhen(String whenCondition, String parameterName, String classParameterName);
        
        String mustNotBeCompatibleWithAnyOfOtherWhen(String whenCondition, String parameterName, String classesParameterName);
        
        String mustBeAnyOfOtherWhen(String whenCondition, String parameterName, String valueParameterName);
        
        String mustNotBeAnyOfOtherWhen(String whenCondition, String parameterName, String valueParameterName);
    }
}
