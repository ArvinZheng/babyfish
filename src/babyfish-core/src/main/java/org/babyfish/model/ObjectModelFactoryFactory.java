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
package org.babyfish.model;

import org.babyfish.lang.IllegalProgramException;
import org.babyfish.model.metadata.Metadatas;
import org.babyfish.model.spi.ObjectModelFactoryProvider;
import org.babyfish.util.LazyResource;

/**
 * @author Tao Chen
 */
public final class ObjectModelFactoryFactory {
    
    private static final LazyResource<Resource> LAZY_RESOURCE = LazyResource.of(Resource.class);

    private ObjectModelFactoryFactory() {
        throw new UnsupportedOperationException();
    }
    
    public static <M> ObjectModelFactory<M> factoryOf(Class<M> objectModelType) {
        Class<?> declaringClass = objectModelType.getDeclaringClass();
        if (declaringClass == null) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().objectModelTypeIsNotNestedType(objectModelType));
        }
        ObjectModelFactoryProvider objectModelFactoryProvider = Metadatas.getObjectModelFactoryProvider(declaringClass);
        if (objectModelFactoryProvider == null) {
            throw new IllegalProgramException();
        }
        return objectModelFactoryProvider.getFactory(objectModelType);
    }
    
    private interface Resource {
        
        String objectModelTypeIsNotNestedType(Class<?> objectModelType);
    }
}
