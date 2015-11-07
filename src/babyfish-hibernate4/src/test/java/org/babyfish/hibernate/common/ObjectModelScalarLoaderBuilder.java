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
package org.babyfish.hibernate.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.babyfish.model.spi.ObjectModelScalarLoader;
import org.hibernate.bytecode.internal.javassist.FieldHandler;

/*
 * Avoid the hard code of HibernateObjectModelScalarLoader to let 
 * maven-processor-plugin work normally under Java8
 */

/**
 * @author Tao Chen
 */
@SuppressWarnings("unchecked")
public class ObjectModelScalarLoaderBuilder {
    
    private static final Constructor<ObjectModelScalarLoader> HIBERNATE_OBJECT_MODEL_SCALAR_LOADER;

    public static ObjectModelScalarLoader build(Object om, FieldHandler handler) {
        try {
            return HIBERNATE_OBJECT_MODEL_SCALAR_LOADER.newInstance(om, handler);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new AssertionError(ex);
        }
    }

    static {
        try {
            HIBERNATE_OBJECT_MODEL_SCALAR_LOADER = (Constructor<ObjectModelScalarLoader>)
                    Class.forName("org.babyfish.hibernate.model.loader.HibernateObjectModelScalarLoader")
                    .getConstructor(Object.class, FieldHandler.class);
        } catch (NoSuchMethodException | SecurityException | ClassNotFoundException ex) {
            throw new AssertionError(ex);
        }
    }
}
