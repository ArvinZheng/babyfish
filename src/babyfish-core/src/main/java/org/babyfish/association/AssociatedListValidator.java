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

import org.babyfish.collection.MAMap;
import org.babyfish.util.LazyResource;
import org.babyfish.validator.Validator;

/**
 * @author Tao Chen
 */
final class AssociatedListValidator<E> implements Validator<E> {
    
    private static final LazyResource<CommonResource> LAZY_COMMON_RESOURCE = LazyResource.of(CommonResource.class);
    
    private AssociatedEndpoint<?, E> endpoint;
    
    protected  AssociatedListValidator(AssociatedEndpoint<?, E> endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void validate(E e) {
        if (e != null) {
            AssociatedEndpoint<E, ?> oppositeEndpoint = 
                this.endpoint.getOppositeEndpoint(e);
            if (!oppositeEndpoint.isSuspended() && (oppositeEndpoint instanceof MAMap<?, ?>)) {
                throw new UnsupportedOperationException(
                        LAZY_COMMON_RESOURCE.get().invalidModificationWhenOppositeEndpointIs(
                                this.getClass(), 
                                this.endpoint.getOppositeEndpointType(), 
                                oppositeEndpoint.getEndpointType()
                        )
                );
            }
        }
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this.endpoint);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AssociatedListValidator<?>;
    }
    
}
