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
package org.babyfish.test.util;

import java.util.Collection;

import junit.framework.Assert;

import org.babyfish.collection.MACollections;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.util.Resources;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class ResourcesTest {

    private interface Resource {
        
        String helloWorld();
        
        String helloPerson(String person);
        
        String helloPatient(String patient, double weight, short bloodPressure, char gender, int number);
        
        String helloParentAndChild(String parent, String child);
        
        String helloClass(Class<?> type);
        
        String helloEnum(ReplacementRule replacementRule);
        
        String helloArray(Object ... args);
        
        String helloCollection(Collection<?> args);
    }
    
    @Test
    public void testResource() {
        Resource resource = Resources.of(Resource.class);
        Assert.assertEquals(
                "Hello, world, welcome to babyfish's internationalization", 
                resource.helloWorld());
        Assert.assertEquals(
                "Hello, Jack, welcome to babyfish's internationalization", 
                resource.helloPerson("Jack"));
        Assert.assertEquals(
                "Hello, Tom, your weight is 139.47, your bloodPressure is 182, your gender is M and your number is 255", 
                resource.helloPatient("Tom", 139.47, (short)182, 'M', 255));
        Assert.assertEquals(
                "Hello, Smith, your child is Kate; Hello Kate, your parent is Smith", 
                resource.helloParentAndChild("Smith", "Kate"));
        Assert.assertEquals(
                "Hello, the class is \"java.lang.Object\"", 
                resource.helloClass(Object.class));
        Assert.assertEquals(
                "Hello, the enumeration value is \"" +
                ReplacementRule.class.getName() +
                '.' +
                ReplacementRule.OLD_REFERENCE_WIN.name() +
                "\"", 
                resource.helloEnum(ReplacementRule.OLD_REFERENCE_WIN));
        Assert.assertEquals(
                "Hello, the array elements are \"" +
                ReplacementRule.class.getName() +
                '.' +
                ReplacementRule.NEW_REFERENCE_WIN.name() +    
                ", java.lang.String, null, Bad one\"", 
                resource.helloArray(
                        ReplacementRule.NEW_REFERENCE_WIN,
                        String.class,
                        null,
                        "Bad one"));
        Assert.assertEquals(
                "Hello, the collection elements are \"" +
                ReplacementRule.class.getName() +
                '.' +
                ReplacementRule.NEW_REFERENCE_WIN.name() +    
                ", java.lang.String, null, Bad one\"", 
                resource.helloCollection(
                        MACollections.wrap(
                            ReplacementRule.NEW_REFERENCE_WIN,
                            String.class,
                            null,
                            "Bad one"
                        )
                    )
                );
    }
    
}
