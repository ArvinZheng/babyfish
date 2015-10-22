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
package org.babyfish.persistence.tool.instrument.metadata;

/**
 * @author Tao Chen
 */
public class MetadataException extends RuntimeException {

    private static final long serialVersionUID = 2675127746202670630L;

    public MetadataException() {
        super();
    }

    public MetadataException(String message) {
        super(message);
    }

    public MetadataException(Throwable cause) {
        super(cause);
    }

    public MetadataException(String message, Throwable cause) {
        super(message, cause);
    }
}
