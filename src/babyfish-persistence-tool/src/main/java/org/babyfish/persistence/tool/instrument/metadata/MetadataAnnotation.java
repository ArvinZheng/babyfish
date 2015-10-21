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
package org.babyfish.persistence.tool.instrument.metadata;

import java.util.List;

import org.babyfish.collection.XOrderedMap;

/**
 * @author Tao Chen
 */
public interface MetadataAnnotation {
    
    String getDescriptor();
    
    XOrderedMap<String, Value> getValues();
    
    interface Value {
        Object get();
    }
    
    interface SimpleValue extends Value {
        @Override
        Object get();
    }
    
    interface EnumValue extends Value {
        
        String getDescriptor();
        
        @Override
        String get();
    }
    
    interface AnnotationValue extends Value {
        @Override
        MetadataAnnotation get();
    }
    
    interface ArrayValue extends Value {
        @Override
        List<Value> get();
    }
}
