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
package org.babyfish.association;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.babyfish.lang.Arguments;
import org.babyfish.reference.IndexedReference;
import org.babyfish.reference.KeyedReference;
import org.babyfish.reference.Reference;

/**
 * @author Tao Chen
 */
public enum AssociatedEndpointType {
    
    REFERENCE {
        @Override
        public Class<?> toClass() {
            return Reference.class;
        }
        @Override
        public boolean isCollection() {
            return false;
        }
        @Override
        public boolean isReference() {
            return true;
        }
    },
    
    INDEXED_REFERENCE {
        @Override
        public Class<?> toClass() {
            return IndexedReference.class;
        }
        @Override
        public boolean isCollection() {
            return false;
        }
        @Override
        public boolean isReference() {
            return true;
        }
    },
    
    KEYED_REFERENCE {
        @Override
        public Class<?> toClass() {
            return KeyedReference.class;
        }
        @Override
        public boolean isCollection() {
            return false;
        }
        @Override
        public boolean isReference() {
            return true;
        }
    },
    
    SET {
        @Override
        public Class<?> toClass() {
            return Set.class;
        }
        @Override
        public boolean isCollection() {
            return true;
        }
        @Override
        public boolean isReference() {
            return false;
        }
    },
    
    COLLECTION {
        @Override
        public Class<?> toClass() {
            return List.class;
        }
        @Override
        public boolean isCollection() {
            return true;
        }
        @Override
        public boolean isReference() {
            return false;
        }
    },
    
    LIST {
        @Override
        public Class<?> toClass() {
            return List.class;
        }
        @Override
        public boolean isCollection() {
            return true;
        }
        @Override
        public boolean isReference() {
            return false;
        }
    },
    
    MAP {
        @Override
        public Class<?> toClass() {
            return Map.class;
        }
        @Override
        public boolean isCollection() {
            return true;
        }
        @Override
        public boolean isReference() {
            return false;
        }
    };
        
    public abstract Class<?> toClass();
    
    public abstract boolean isCollection();
    
    public abstract boolean isReference();
    
    /**
     * @param associatedEndpointJavaType The java type of associated end point
     * @return An constant of this enumeration type.
     * @exception IllegalArgumentException
     * This parameter is not instance of any one of these types:
     * <ul>
     *    <li>{@link Map}</li>
     *    <li>{@link Set}</li>
     *    <li>{@link List}</li>
     *    <li>{@link Collection}</li>
     *    <li>{@link KeyedReference}</li>
     *    <li>{@link IndexedReference}</li>
     *    <li>{@link KeyedReference}</li>
     * </ul>
     */
    public static AssociatedEndpointType of(Class<?> associatedEndpointJavaType) {
        Arguments.mustBeCompatibleWithAnyOfValue(
                "associatedEndpointJavaType", 
                associatedEndpointJavaType, 
                Map.class,
                Collection.class,
                Reference.class);
        if (Map.class.isAssignableFrom(associatedEndpointJavaType)) {
            return MAP;
        } else if (Set.class.isAssignableFrom(associatedEndpointJavaType)) {
            return SET;
        } else if (List.class.isAssignableFrom(associatedEndpointJavaType)) {
            return LIST;
        } else if (Collection.class.isAssignableFrom(associatedEndpointJavaType)) {
            return COLLECTION;
        } else if (KeyedReference.class.isAssignableFrom(associatedEndpointJavaType)) {
            return KEYED_REFERENCE;
        } else if (IndexedReference.class.isAssignableFrom(associatedEndpointJavaType)) {
            return INDEXED_REFERENCE;
        }
        return REFERENCE;
    }
    
}
