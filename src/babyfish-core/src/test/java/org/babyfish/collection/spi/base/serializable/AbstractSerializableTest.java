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
package org.babyfish.collection.spi.base.serializable;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Comparator;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.lang.Arguments;
import org.babyfish.validator.Validator;

/**
 * @author Tao Chen
 */
public abstract class AbstractSerializableTest {

    @SuppressWarnings("unchecked")
    protected static <T> T getField(Object o, Field field) {
        try {
            return (T)field.get(o);
        } catch (IllegalAccessException ex) {
            throw new AssertionError(ex);
        }
    }
    
    protected static Class<?> classOf(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ex) {
            throw new AssertionError(ex);
        }
    }
    
    protected static Field fieldOf(Class<?> clazz, String name) {
        Field field;
        try {
            field = clazz.getDeclaredField(name);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new AssertionError(ex);
        }
        field.setAccessible(true);
        return field;
    }
    
    protected static final class InsenstiveEqualityComparatorImpl implements EqualityComparator<String>, Serializable {
        
        private static final long serialVersionUID = 1559061679825217828L;

        @Override
        public int hashCode(String obj) {
            return obj.toUpperCase().hashCode();
        }
        
        @Override
        public boolean equals(String obj1, String obj2) {
            return obj1.toUpperCase().equals(obj2.toUpperCase());
        }

        @Override
        public int hashCode() {
            return this.getClass().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            return this.getClass() == obj.getClass();
        }
    }
    
    protected static final class InsenstiveComparatorImpl implements Comparator<String>, Serializable {
        
        private static final long serialVersionUID = 8326928807517744859L;

        @Override
        public int compare(String o1, String o2) {
            return o1.toLowerCase().compareTo(o2.toLowerCase());
        }

        @Override
        public int hashCode() {
            return this.getClass().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            return this.getClass() == obj.getClass();
        }
    }
    
    protected static class NotNullValidator implements Validator<String>, Serializable {

        private static final long serialVersionUID = 8594496209767014638L;

        @Override
        public void validate(String e) {
            Arguments.mustNotBeNull("e", e);
        }
        
    }
    
    protected static class NotEmptyValidator implements Validator<String>, Serializable {

        private static final long serialVersionUID = 871159193337397819L;

        @Override
        public void validate(String e) {
            Arguments.mustNotBeEmpty("e", e);
        }
        
    }
    
}
