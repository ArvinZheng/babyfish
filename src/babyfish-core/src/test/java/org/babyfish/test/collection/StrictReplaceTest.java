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
package org.babyfish.test.collection;

import junit.framework.Assert;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.MAHashSet;
import org.babyfish.collection.MASet;
import org.babyfish.collection.event.ElementAdapter;
import org.babyfish.collection.event.ElementEvent;
import org.babyfish.modificationaware.event.PropertyVersion;
import org.babyfish.reference.Reference;
import org.babyfish.reference.ReferenceImpl;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class StrictReplaceTest {

    @Test
    public void testReplaceByDefaultEqualityComparator() {
        MASet<String> set = new MAHashSet<String>();
        String a = "A";
        String b = a;
        Assert.assertTrue(set.add(a));
        final Reference<String> eventRecord = new ReferenceImpl<String>("");
        set.addElementListener(
                new ElementAdapter<String>() {
                    @Override
                    public void modified(ElementEvent<String> e) throws Throwable {
                        eventRecord.set(
                                eventRecord.get() +
                                e.getElement(PropertyVersion.DETACH) +
                                "=>" +
                                e.getElement(PropertyVersion.ATTACH));
                    }
                });
        Assert.assertFalse(set.add(b));
        Assert.assertEquals("A=>A", eventRecord.get());
    }
    
    @Test
    public void testReplaceBySpecialEqualityComparator() {
        MASet<String> set = new MAHashSet<String>(
                new EqualityComparator<String>() {
                    @Override
                    public int hashCode(String o) {
                        return o.toLowerCase().hashCode();
                    }
                    @Override
                    public boolean equals(String o1, String o2) {
                        return o1.toLowerCase().equals(o2.toLowerCase());
                    }
                });
        String a = "A";
        String b = "a";
        Assert.assertTrue(set.add(a));
        final Reference<String> eventRecord = new ReferenceImpl<String>("");
        set.addElementListener(
                new ElementAdapter<String>() {
                    @Override
                    public void modified(ElementEvent<String> e) throws Throwable {
                        eventRecord.set(
                                eventRecord.get() +
                                e.getElement(PropertyVersion.DETACH) +
                                "=>" +
                                e.getElement(PropertyVersion.ATTACH));
                    }
                });
        Assert.assertFalse(set.add(b));
        Assert.assertEquals("A=>a", eventRecord.get());
    }
    
}
