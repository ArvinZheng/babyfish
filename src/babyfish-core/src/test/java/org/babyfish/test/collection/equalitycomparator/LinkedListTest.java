/*
 * BabyFish, Object Model Framework for Java and JPA.
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
package org.babyfish.test.collection.equalitycomparator;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.LinkedList;
import org.babyfish.collection.XList;

/**
 * @author Tao Chen(&#38472;&#28059;)
 */
public class LinkedListTest extends AbstractXListTest {

    @Override
    protected XList<Element> createXList(
            EqualityComparator<Element> equalityComparator) {
        return new LinkedList<Element>(equalityComparator);
    }

}
