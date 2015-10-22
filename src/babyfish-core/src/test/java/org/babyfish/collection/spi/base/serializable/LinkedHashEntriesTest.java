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

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.OrderAdjustMode;
import org.babyfish.collection.spi.base.BaseEntries;
import org.babyfish.collection.spi.base.LinkedHashEntries;

/**
 * @author Tao Chen
 */
public class LinkedHashEntriesTest extends HashEntriesTest {

    @Override
    protected BaseEntries<String, String> createBaseEntries(boolean withUnifiedComparator) {
        EqualityComparator<String> equalityComparator;
        if (withUnifiedComparator) {
            equalityComparator = new InsenstiveEqualityComparatorImpl();
        } else {
            equalityComparator = null;
        }
        return new LinkedHashEntries<String, String>(
                ReplacementRule.NEW_REFERENCE_WIN,
                equalityComparator,
                equalityComparator,
                16,
                .75F,
                false,
                OrderAdjustMode.NONE,
                OrderAdjustMode.NONE);
    }

}
