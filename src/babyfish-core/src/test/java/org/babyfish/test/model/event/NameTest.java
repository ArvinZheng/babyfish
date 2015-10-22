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
package org.babyfish.test.model.event;

import junit.framework.Assert;

import org.babyfish.model.ObjectModel;
import org.babyfish.model.event.ScalarEvent;
import org.babyfish.model.event.ScalarListener;
import org.babyfish.model.metadata.ScalarProperty;
import org.babyfish.modificationaware.event.PropertyVersion;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class NameTest {

    @Test
    public void testScalarEvent() {
        Name name = new Name();
        ScalarListener listener = new ScalarListener() {

            private StringBuilder builder = new StringBuilder();
            
            @Override
            public void modifying(ScalarEvent e) {
                ScalarProperty scalarProperty = e.getProperty();
                this
                .builder
                .append("pre(")
                .append(scalarProperty.getName())
                .append(':')
                .append(e.getScalar(PropertyVersion.DETACH))
                .append("->")
                .append(e.getScalar(PropertyVersion.ATTACH))
                .append(");");
            }

            @Override
            public void modified(ScalarEvent e) {
                ScalarProperty scalarProperty = e.getProperty();
                this
                .builder
                .append("post(")
                .append(scalarProperty.getName())
                .append(':')
                .append(e.getScalar(PropertyVersion.DETACH))
                .append("->")
                .append(e.getScalar(PropertyVersion.ATTACH))
                .append(");");
            }
            
            @Override
            public String toString() {
                return this.builder.toString();
            }
            
        };
        
        name.setFirstName("Jim");
        Assert.assertEquals("", listener.toString());
        name.setLastName("Green");
        Assert.assertEquals("", listener.toString());
        
        ((ObjectModel)Name.om(name)).addScalarListener(listener);
        name.setFirstName("Jim2");
        Assert.assertEquals(
                "pre(firstName:Jim->Jim2);" +
                "post(firstName:Jim->Jim2);", 
                listener.toString());
        name.setLastName("Green2");
        Assert.assertEquals(
                "pre(firstName:Jim->Jim2);" +
                "post(firstName:Jim->Jim2);" +
                "pre(lastName:Green->Green2);" +
                "post(lastName:Green->Green2);", 
                listener.toString());
        
        name.setFirstName("Jim3");
        Assert.assertEquals(
                "pre(firstName:Jim->Jim2);" +
                "post(firstName:Jim->Jim2);" +
                "pre(lastName:Green->Green2);" +
                "post(lastName:Green->Green2);" +
                "pre(firstName:Jim2->Jim3);" +
                "post(firstName:Jim2->Jim3);", 
                listener.toString());
        name.setLastName("Green3");
        Assert.assertEquals(
                "pre(firstName:Jim->Jim2);" +
                "post(firstName:Jim->Jim2);" +
                "pre(lastName:Green->Green2);" +
                "post(lastName:Green->Green2);" +
                "pre(firstName:Jim2->Jim3);" +
                "post(firstName:Jim2->Jim3);" +
                "pre(lastName:Green2->Green3);" +
                "post(lastName:Green2->Green3);", 
                listener.toString());
        
        ((ObjectModel)Name.om(name)).removeScalarListener(listener);
        name.setFirstName("Jim4");
        Assert.assertEquals(
                "pre(firstName:Jim->Jim2);" +
                "post(firstName:Jim->Jim2);" +
                "pre(lastName:Green->Green2);" +
                "post(lastName:Green->Green2);" +
                "pre(firstName:Jim2->Jim3);" +
                "post(firstName:Jim2->Jim3);" +
                "pre(lastName:Green2->Green3);" +
                "post(lastName:Green2->Green3);", 
                listener.toString());
        name.setLastName("Green4");
        Assert.assertEquals(
                "pre(firstName:Jim->Jim2);" +
                "post(firstName:Jim->Jim2);" +
                "pre(lastName:Green->Green2);" +
                "post(lastName:Green->Green2);" +
                "pre(firstName:Jim2->Jim3);" +
                "post(firstName:Jim2->Jim3);" +
                "pre(lastName:Green2->Green3);" +
                "post(lastName:Green2->Green3);", 
                listener.toString());
    }
}
