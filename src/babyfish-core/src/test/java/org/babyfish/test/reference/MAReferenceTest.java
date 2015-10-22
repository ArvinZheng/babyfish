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
package org.babyfish.test.reference;

import junit.framework.Assert;

import org.babyfish.modificationaware.event.PropertyVersion;
import org.babyfish.reference.MAReference;
import org.babyfish.reference.MAReferenceImpl;
import org.babyfish.reference.Reference;
import org.babyfish.reference.ReferenceImpl;
import org.babyfish.reference.event.ValueEvent;
import org.babyfish.reference.event.ValueListener;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class MAReferenceTest {

    @Test
    public void testSet() {
        MAReference<String> ref = new MAReferenceImpl<String>();
        final Reference<StringBuilder> events = 
            new ReferenceImpl<StringBuilder>(new StringBuilder());
        ref.addValueListener(
                new ValueListener<String>() {
                    @Override
                    public void modifying(ValueEvent<String> e) throws Throwable {
                        if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                            events.get().append("@pre[");
                            events.get().append(e.getValue(PropertyVersion.DETACH));
                        }
                        if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                            events.get().append("=>");
                            events.get().append(e.getValue(PropertyVersion.ATTACH));
                            events.get().append("]");
                        }
                    }
                    @Override
                    public void modified(ValueEvent<String> e) throws Throwable {
                        if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                            events.get().append("@post[");
                            events.get().append(e.getValue(PropertyVersion.DETACH));
                        }
                        if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                            events.get().append("=>");
                            events.get().append(e.getValue(PropertyVersion.ATTACH));
                            events.get().append("]");
                        }
                    }
                });
        Assert.assertNull(ref.set("Value1"));
        Assert.assertEquals("Value1", ref.set("Value2"));
        Assert.assertEquals("Value2", ref.set(null));
        Assert.assertEquals(
                "@pre[null=>Value1]@post[null=>Value1]" +
                "@pre[Value1=>Value2]@post[Value1=>Value2]" +
                "@pre[Value2=>null]@post[Value2=>null]", 
                events.get().toString());
    }
    
}
