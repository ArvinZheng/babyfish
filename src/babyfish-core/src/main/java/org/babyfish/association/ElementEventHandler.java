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
import java.util.Map;

import org.babyfish.collection.XCollection;
import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.event.modification.SetModifications.ResumeViaFrozenContext;
import org.babyfish.collection.event.modification.SetModifications.SuspendByElementViaFrozenContext;
import org.babyfish.modificationaware.event.PropertyVersion;
import org.babyfish.reference.Reference;

/**
 * @author Tao Chen
 */
abstract class ElementEventHandler<O, E> extends AbandonableEventHandler<O, E> {
    
    /*
     * Choose validate in "modifying" event so that the end-point validation 
     * of both two sides can be executed before real data changing.
     */
    @SuppressWarnings("unchecked")
    public void preHandle(ElementEvent<E> e) {
        if (!this.isSuspended()) {
            this.setSuspended(true);
            try {
                E detachedElement = null;
                if (!(e.getPrimitiveModification() instanceof SuspendByElementViaFrozenContext<?>)) {
                    detachedElement = this.loaded(e.getElement(PropertyVersion.DETACH));
                }
                E attachedElement = null;
                if (!(e.getPrimitiveModification() instanceof ResumeViaFrozenContext<?>)) {
                    attachedElement = this.loaded(e.getElement(PropertyVersion.ATTACH));
                }
                if (detachedElement == attachedElement) {
                    return;
                }
                if (attachedElement != null) {
                    O owner = this.getOwner();
                    AssociatedEndpoint<E, O> oppositeEndpoint = this.getOppositeEndpoint(attachedElement);
                    if (!oppositeEndpoint.isSuspended()) {
                        if (oppositeEndpoint instanceof Reference<?>) {
                            Reference<O> reference = (Reference<O>)oppositeEndpoint;
                            reference.validate(owner);
                        } else if (oppositeEndpoint instanceof Collection<?>) {
                            XCollection<O> collection = (XCollection<O>)oppositeEndpoint;
                            collection.validate(owner);
                        } else {
                            throw new AssertionError();
                        }
                    }
                }
            } finally {
                this.setSuspended(false);
            }
        }
    }
    
    /*
     * Choose to modify opposite end-point in the "modified" event, becasue
     * 
     * If do the modification for opposite end-point in "modifying", such as:
     *      parent1.getChildNodes().addAll(parent2.getChildNodes());
     * The java.util.ConconrrentModificationException will be raised.
     */
    @SuppressWarnings("unchecked")
    public void postHandle(ElementEvent<E> e) {
        if (!this.isSuspended() && e.isModificationSuccessed()) {
            this.setSuspended(true);
            try {
                E detachedElement = null;
                if (!(e.getPrimitiveModification() instanceof SuspendByElementViaFrozenContext<?>)) {
                    detachedElement = this.loaded(e.getElement(PropertyVersion.DETACH));
                }
                E attachedElement = null;
                if (!(e.getPrimitiveModification() instanceof ResumeViaFrozenContext<?>)) {
                    attachedElement = this.loaded(e.getElement(PropertyVersion.ATTACH));
                }
                if (detachedElement == attachedElement) {
                    return;
                }
                O owner = this.getOwner();
                if (detachedElement != null) {
                    AssociatedEndpoint<E, O> oppositeEndpoint = this.getOppositeEndpoint(detachedElement);
                    if (!oppositeEndpoint.isSuspended()) {
                        boolean isOppositeEndpointDisabled = oppositeEndpoint.isDisabled();
                        try {
                            if (oppositeEndpoint instanceof Reference<?>) {
                                Reference<?> reference = (Reference<?>)oppositeEndpoint;
                                reference.set(null);
                            } else if (oppositeEndpoint instanceof Collection<?>) {
                                Collection<?> collection = (Collection<?>)oppositeEndpoint;
                                collection.remove(owner);
                            } else if (oppositeEndpoint instanceof Map<?, ?>) {
                                Map<?, ?> map = (Map<?, ?>)oppositeEndpoint;
                                map.values().remove(owner);
                            } else {
                                throw new AssertionError();
                            }
                        } finally {
                            if (isOppositeEndpointDisabled) {
                                oppositeEndpoint.disable();
                            }
                        }
                    }
                }
                if (attachedElement != null) {
                    AssociatedEndpoint<E, O> oppositeEndpoint = this.getOppositeEndpoint(attachedElement);
                    if (!oppositeEndpoint.isSuspended()) {
                        boolean isOppositeEndpointDisabled = oppositeEndpoint.isDisabled();
                        try {
                            if (oppositeEndpoint instanceof Reference<?>) {
                                Reference<O> reference = (Reference<O>)oppositeEndpoint;
                                reference.set(owner);
                            } else if (oppositeEndpoint instanceof Collection<?>) {
                                Collection<O> collection = (Collection<O>)oppositeEndpoint;
                                collection.add(owner);
                            } else {
                                throw new AssertionError();
                            }
                        } finally {
                            if (isOppositeEndpointDisabled) {
                                oppositeEndpoint.disable();
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
