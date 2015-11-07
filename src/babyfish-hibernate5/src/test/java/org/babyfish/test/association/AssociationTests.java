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
package org.babyfish.test.association;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import junit.framework.Assert;

import org.babyfish.association.AssociatedMap;
import org.babyfish.association.AssociatedSet;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.collection.XOrderedSet;

/**
 * @author Tao Chen
 */
public class AssociationTests {
    
    private AssociationTests() {
        
    }
    
    private static int sequence;
    
    private static final Field BASE_COLLECTION;
    
    private static final Field BASE_MAP;
    
    public static int nextValOfSequence() {
        return sequence++;
    }
    
    @SuppressWarnings("unchecked")
    public static <E> void assertSet(Set<E> set, E ... elements) {
        Assert.assertEquals(elements.length, set.size());
        if (set instanceof AssociatedSet<?, ?>) {
            try {
                set = (Set<E>)BASE_COLLECTION.get(set);
            } catch (IllegalAccessException ex) {
                throw new AssertionError();
            }
        }
        if (set instanceof XOrderedSet<?> || set instanceof SortedSet<?>) {
            int index = 0;
            for (E e : set) {
                Assert.assertEquals(elements[index++], e);
            }
        } else {
            for (E element : elements) {
                Assert.assertTrue(set.contains(element));
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <E> void assertList(List<E> list, E ... elements) {
        Assert.assertEquals(elements.length, list.size());
        int index = 0;
        for (E e : list) {
            Assert.assertEquals(elements[index++], e);
        }
        Assert.assertEquals(elements.length, index);
    }
    
    @SuppressWarnings("unchecked")
    public static <E> void assertCollection(Collection<E> collection, E ... elements) {
        Assert.assertEquals(elements.length, collection.size());
        int index = 0;
        for (E e : collection) {
            Assert.assertEquals(e, elements[index++]);
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <K, V> void assertMap(Map<K, V> map, K[] keys, V[] values) {
        if (keys.length != values.length) {
            throw new IllegalArgumentException();
        }
        Assert.assertEquals(keys.length, map.size());
        if (map instanceof AssociatedMap<?, ?, ?>) {
            try {
                map = (Map<K, V>)BASE_MAP.get(map);
            } catch (IllegalAccessException ex) {
                throw new AssertionError();
            }
        }
        if (map instanceof XOrderedMap<?, ?> || map instanceof SortedMap<?, ?>) {
            int index = 0;
            for (Entry<K, V> entry : map.entrySet()) {
                Assert.assertEquals(keys[index], entry.getKey());
                Assert.assertEquals(values[index], entry.getValue());
                index++;
            }
            Assert.assertEquals(keys.length, index);
        } else {
            for (int i = keys.length - 1; i >= 0; i--) {
                Assert.assertEquals(values[i], map.get(keys[i]));
            }
        }
    }
    
    public static <E> Iterator<E> getIteratorAndMoveTo(Collection<E> c, E e) {
        Iterator<E> iterator = c.iterator();
        while (iterator.hasNext()) {
            E itrE = iterator.next();
            if (e == null ? itrE == null : e.equals(itrE)) {
                return iterator;
            }
        }
        return null;
    }
    
    public static <K, V> Iterator<Entry<K, V>> getIteratorAndMoveTo(Map<K, V> map, K key) {
        Iterator<Entry<K, V>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<K, V> entry = iterator.next();
            if (key == null ? entry.getKey() == null : key.equals(entry.getKey())) {
                return iterator;
            }
        }
        return null;
    }
    
    public static <K, V> Entry<K, V> getEntry(Map<K, V> map, K key) {
        Iterator<Entry<K, V>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<K, V> entry = iterator.next();
            if (key == null ? entry.getKey() == null : key.equals(entry.getKey())) {
                return entry;
            }
        }
        return null;
    }
    
    static {
        try {
            BASE_COLLECTION = getPrivateField(AssociatedSet.class, "base");
            BASE_COLLECTION.setAccessible(true);
            BASE_MAP = getPrivateField(AssociatedMap.class, "base");
            BASE_MAP.setAccessible(true);
        } catch (NoSuchFieldException ex) {
            throw new AssertionError(ex);
        }
    }
    
    private static Field getPrivateField(Class<?> clazz, String name) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException ex) {
            if (clazz.getSuperclass() != null) {
                return getPrivateField(clazz.getSuperclass(), name);
            }
            throw ex;
        }
    }
    
}
