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

import java.lang.reflect.Field;

import junit.framework.Assert;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.spi.base.BaseEntries;
import org.babyfish.collection.spi.base.BaseEntry;
import org.babyfish.collection.spi.base.HashEntries;

/**
 * @author Tao Chen
 */
public class HashEntriesTest extends AbstractBaseEntriesSerializableTest {

    private static final Field BUCKETS = 
            fieldOf(HashEntries.class, "buckets");
    
    @Override
    protected BaseEntries<String, String> createBaseEntries(boolean withUnifiedComparator) {
        EqualityComparator<String> equalityComparator;
        if (withUnifiedComparator) {
            equalityComparator = new InsenstiveEqualityComparatorImpl();
        } else {
            equalityComparator = null;
        }
        return new HashEntries<String, String>(
                ReplacementRule.NEW_REFERENCE_WIN,
                equalityComparator,
                equalityComparator,
                16,
                .75F);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void compare(
            BaseEntries<String, String> baseEntries, 
            BaseEntries<String, String> deserializedBaseEntries) {
        BaseEntry<String, String>[] arr = (BaseEntry<String, String>[])getField(baseEntries, BUCKETS);
        BaseEntry<String, String>[] deserializableArr = (BaseEntry<String, String>[])getField(deserializedBaseEntries, BUCKETS);
        Assert.assertEquals(arr.length, deserializableArr.length);
        Assert.assertTrue(arr != deserializableArr);
        for (int i = arr.length - 1; i >= 0; i--) {
            Assert.assertEquals(arr[i] != null, deserializableArr[i] != null);
        }
    }

}
