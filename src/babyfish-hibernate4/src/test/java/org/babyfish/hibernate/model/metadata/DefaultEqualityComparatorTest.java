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
package org.babyfish.hibernate.model.metadata;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Set;

import junit.framework.Assert;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.FrozenEqualityComparator;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.persistence.model.metadata.JPAMetadatas;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class DefaultEqualityComparatorTest {
    
    private static EqualityComparator<Foo> equalityComparator;
    
    @BeforeClass
    public static void initClass() {
        equalityComparator = JPAMetadatas.of(Foo.class).getOwnerEqualityComparator();
        Assert.assertTrue(equalityComparator instanceof FrozenEqualityComparator<?>);
        Assert.assertEquals(
                Foo.class.getName() +
                "{defaultJPAFrozenEqualityComparator:92B8C17E_BF4E_4135_B596_5A76E0FEBF4E}"
                , 
                equalityComparator.getClass().getName());
    }

    @Test
    public void testSameTransient() {
        Foo foo = new Foo();
        Assert.assertTrue(equalityComparator.equals(foo, foo));
    }
    
    @Test
    public void testTransientAndTransient() {
        Assert.assertFalse(equalityComparator.equals(new Foo(), new Foo()));
    }
    
    @Test
    public void testTransientAndNormal() {
        Assert.assertFalse(equalityComparator.equals(new Foo(), new Foo(0L)));
    }
    
    @Test
    public void testNormalAndTransient() {
        Assert.assertFalse(equalityComparator.equals(new Foo(0L), new Foo()));
    }
    
    @Test
    public void testLt() {
        Assert.assertFalse(equalityComparator.equals(new Foo(-1L), new Foo(1L)));
    }
    
    @Test
    public void testGt() {
        Assert.assertFalse(equalityComparator.equals(new Foo(1L), new Foo(-1L)));
    }
    
    @Test
    public void testEq() {
        Assert.assertTrue(equalityComparator.equals(new Foo(0L), new Foo(0L)));
    }
    
    @Test
    public void testFrozenMechanism() {
        Set<Foo> set = new LinkedHashSet<>(equalityComparator);
        Foo foo1 = new Foo();
        Foo foo2 = new Foo();
        Foo foo3 = new Foo();
        set.add(foo1);
        set.add(foo2);
        set.add(foo3);
        
        assertCollection(set, null, null, null);
        
        foo1.setId(1L);
        assertCollection(set, 1L, null, null);
        
        foo2.setId(2L);
        assertCollection(set, 1L, 2L, null);
        
        foo3.setId(3L);
        assertCollection(set, 1L, 2L, 3L);
        
        foo1.setId(null);
        assertCollection(set, null, 2L, 3L);
        
        foo2.setId(null);
        assertCollection(set, null, null, 3L);
        
        foo3.setId(null);
        assertCollection(set, null, null, null);
        
        foo1.setId(0L);
        assertCollection(set, 0L, null, null);
        
        foo2.setId(0L);
        assertCollection(set, 0L, null);
        
        foo3.setId(0L);
        assertCollection(set, 0L);
        
        foo1.setId(1L);
    }
    
    @Test
    public void testIO() throws ClassNotFoundException, IOException {
        byte[] buf;
        Object[] arr = new Object[] { equalityComparator };
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
                ObjectOutputStream oout = new ObjectOutputStream(bout)) {
            oout.writeObject(arr);
            oout.flush();
            buf = bout.toByteArray();
        }
        Object[] deserializedArr;
        try (ByteArrayInputStream bin = new ByteArrayInputStream(buf);
                ObjectInputStream oin = new ObjectInputStream(bin)) {
            deserializedArr = (Object[])oin.readObject();
        }
        Assert.assertTrue(arr != deserializedArr);
        Assert.assertEquals(arr.length, deserializedArr.length);
        for (int i = arr.length - 1; i >= 0; i--) {
            Assert.assertSame(arr[i], deserializedArr[i]);
        }
    }
    
    private static void assertCollection(Collection<Foo> foos, Long ... ids) {
        Assert.assertEquals(ids.length, foos.size());
        int index = 0;
        for (Foo foo : foos) {
            Assert.assertEquals(foo.getId(), ids[index++]);
        }
    }
}
