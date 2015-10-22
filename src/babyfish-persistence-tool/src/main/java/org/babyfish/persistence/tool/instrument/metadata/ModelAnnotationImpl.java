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

import java.util.Iterator;
import java.util.List;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.lang.Nulls;
import org.babyfish.org.objectweb.asm.tree.AnnotationNode;

/**
 * @author Tao Chen
 */
class ModelAnnotationImpl implements MetadataAnnotation {
    
    private String descriptor;
    
    private XOrderedMap<String, Value> values;

    public ModelAnnotationImpl(AnnotationNode annotationNode) {
        this.descriptor = annotationNode.desc;
        if (Nulls.isNullOrEmpty(annotationNode.values)) {
            this.values = MACollections.emptyOrderedMap();
        } else {
            XOrderedMap<String, Value> values = new LinkedHashMap<>();
            Iterator<?> itr = annotationNode.values.iterator();
            while (itr.hasNext()) {
                String key = (String)itr.next();
                Object value = itr.next();
                values.put(key, createValue(value));
            }
            this.values = MACollections.unmodifiable(values);
        }
    }

    @Override
    public String getDescriptor() {
        return this.descriptor;
    }

    @Override
    public XOrderedMap<String, Value> getValues() {
        return this.values;
    }

    private static Value createValue(Object value) {
        if (value instanceof List<?>) {
            return new ArrayValueImpl((List<?>)value);
        }
        if (value instanceof AnnotationNode) {
            return new AnnotationValueImpl((AnnotationNode)value);
        }
        if (value instanceof String[]) {
            return new EnumValueImpl((String[])value);
        }
        return new SimpleValueImpl(value);
    }
    
    private static class SimpleValueImpl implements SimpleValue {
        
        private Object value;
        
        public SimpleValueImpl(Object value) {
            this.value = value;
        }

        @Override
        public Object get() {
            return this.value;
        }
    }
    
    private static class EnumValueImpl implements EnumValue {
        
        private String descriptor;
        
        private String value;

        public EnumValueImpl(String[] arr) {
            this.descriptor = arr[0];
            this.value = arr[1];
        }

        @Override
        public String getDescriptor() {
            return this.descriptor;
        }

        @Override
        public String get() {
            return this.value;
        }
    }
    
    private static class AnnotationValueImpl implements AnnotationValue {
        
        private MetadataAnnotation value;
        
        public AnnotationValueImpl(AnnotationNode annotationNode) {
            this.value = new ModelAnnotationImpl(annotationNode);
        }

        @Override
        public MetadataAnnotation get() {
            return this.value;
        }
    }
    
    private static class ArrayValueImpl implements ArrayValue {
        
        private List<Value> values;
        
        public ArrayValueImpl(List<?> values) {
            List<Value> transformedList = new ArrayList<>(values.size());
            for (Object value : values) {
                transformedList.add(createValue(value));
            }
            this.values = MACollections.unmodifiable(transformedList);
        }

        @Override
        public List<Value> get() {
            return this.values;
        }
    }
}
