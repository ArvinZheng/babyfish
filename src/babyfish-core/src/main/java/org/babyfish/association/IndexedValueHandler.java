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

import java.util.List;

import org.babyfish.modificationaware.event.PropertyVersion;
import org.babyfish.reference.IndexedReference;
import org.babyfish.reference.event.IndexedValueEvent;

/**
 * @author Tao Chen
 */
abstract class IndexedValueEventHandler<O, T> extends EventHandler<O, T> {
    
    /*
     * Keep the consistency with KeyedValueEventHandler, 
     * use preHandle to notify opposite end point.
     */
    @SuppressWarnings("unchecked")
    public void preHandle(IndexedValueEvent<T> e) {
        if (!this.isSuspended()) {
            this.setSuspended(true);
            try {
                T detachedValue = e.getValue(PropertyVersion.DETACH);
                T attachedValue = e.getValue(PropertyVersion.ATTACH);
                int detachedIndex = e.getIndex(PropertyVersion.DETACH);
                int attachedIndex = e.getIndex(PropertyVersion.ATTACH);
                if (detachedValue == attachedValue && detachedIndex == attachedIndex) {
                    return;
                }
                if (detachedValue != null) {
                    AssociatedEndpoint<T, O> oppositeEndpoint = this.getOppositeEndpoint(detachedValue);
                    if (!oppositeEndpoint.isSuspended()) {
                        if (detachedIndex != IndexedReference.INVALID_INDEX) {
                            boolean isOppositeEndpointDisabled = oppositeEndpoint.isDisabled();
                            try {
                                List<O> list = (List<O>)oppositeEndpoint;
                                list.remove(detachedIndex);
                            } finally {
                                if (isOppositeEndpointDisabled) {
                                    oppositeEndpoint.disable();
                                }
                            }
                        }
                    }
                }
                if (attachedValue != null) {
                    AssociatedEndpoint<T, O> oppositeEndpoint = this.getOppositeEndpoint(attachedValue);
                    if (!oppositeEndpoint.isSuspended()) {
                        if (attachedIndex != IndexedReference.INVALID_INDEX) {
                            boolean isOppositeEndpointDisabled = oppositeEndpoint.isDisabled();
                            try {
                                List<O> list = (List<O>)oppositeEndpoint;
                                list.add(attachedIndex, this.getOwner());
                            } finally {
                                if (isOppositeEndpointDisabled) {
                                    oppositeEndpoint.disable();
                                }
                            }
                        }
                    }
                }
            } finally {
                this.setSuspended(false);
            }
        }
    }
}
