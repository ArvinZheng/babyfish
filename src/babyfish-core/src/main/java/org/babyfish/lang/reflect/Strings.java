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
package org.babyfish.lang.reflect;

import org.babyfish.lang.Nulls;

/**
 * @author Tao Chen
 */
public class Strings {

    @Deprecated
    protected Strings() {
        throw new UnsupportedOperationException();
    }
    
    public static String toCamelCase(String str) {
        if (Nulls.isNullOrEmpty(str) || Character.isLowerCase(str.charAt(0))) {
            return str;
        }
        int index = 0;
        int len = str.length();
        char[] buf = new char[len];
        while (index < len) {
            char c = str.charAt(index);
            if (!Character.isUpperCase(c)) {
                break;
            }
            buf[index++] = Character.toLowerCase(c);
        }
        if (index == 0) {
            return str;
        }
        if (index == len) {
            return new String(buf);
        }
        if (index == 1) {
            return Character.toLowerCase(buf[0]) + str.substring(1);
        }
        return new String(buf, 0, index - 1) + str.substring(index - 1);
    }
    
    public static String toPascalCase(String str) {
        if (Nulls.isNullOrEmpty(str) || Character.isUpperCase(str.charAt(0))) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
