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

import org.babyfish.lang.IllegalProgramException;
import org.babyfish.reference.MAIndexedReferenceImpl;
import org.babyfish.reference.event.IndexedValueEvent;
import org.babyfish.util.LazyResource;

/**
 * @author Tao Chen
 */
public abstract class AssociatedIndexedReference<O, T>
extends MAIndexedReferenceImpl<T>
implements AssociatedEndpoint<O, T> {
    
    private static final long serialVersionUID = -4695991056750918549L;
    
    private static final LazyResource<CommonResource> LAZY_COMMON_RESOURCE = LazyResource.of(CommonResource.class);
    
    private boolean disabled;
    
    private transient boolean suspended;

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public boolean isLoadable() {
        return true;
    }

    @Override
    public void load() {
        
    }

    @Override
    public final AssociatedEndpointType getEndpointType() {
        return AssociatedEndpointType.INDEXED_REFERENCE;
    }

    @Override
    public boolean isSuspended() {
        return this.suspended;
    }
    
    @Override
    public final AssociatedEndpointType getOppositeEndpointType() {
        return AssociatedEndpointType.LIST;
    }

    /**
     * @exception IllegalProgramException The {@link #onGetOppositeEndpoint(Object)}
     * returns null or an end point that is not restricted by the return value of 
     * {@link #getOppositeEndpointType()}
     */
    @Override
    public final AssociatedEndpoint<T, O> getOppositeEndpoint(T opposite) {
        AssociatedEndpoint<T, O> oppositeEndpoint = this.onGetOppositeEndpoint(opposite);
        AssociatedEndpointType expectedOppositeEndpointType = this.getOppositeEndpointType();
        if (oppositeEndpoint == null) {
            throw new IllegalProgramException(
                    LAZY_COMMON_RESOURCE.get().invalidImplemententOfOnGetOppositeEndpoint(
                            this.getClass(),
                            expectedOppositeEndpointType, 
                            null));
        }
        if (oppositeEndpoint.getEndpointType() != expectedOppositeEndpointType) {
            throw new IllegalProgramException(
                    LAZY_COMMON_RESOURCE.get().invalidImplemententOfOnGetOppositeEndpoint(
                            this.getClass(), 
                            expectedOppositeEndpointType, 
                            oppositeEndpoint.getEndpointType()));
        }
        return oppositeEndpoint;
    }
    
    @Override
    public final boolean isDisabled() {
        return this.disabled;
    }

    @Override
    public final void disable() {
        this.disabled = true;
    }

    @Override
    public final void enable() {
        this.disabled = false;
    }
    
    @Override
    public int getIndex() {
        this.requiredEnabled();
        return super.getIndex();
    }

    @Override
    public T get(boolean absolute) {
        this.requiredEnabled();
        return super.get(absolute);
    }

    @Override
    public int setIndex(int index) {
        this.enable();
        return super.setIndex(index);
    }

    @Override
    public T set(T value) {
        this.enable();
        return super.set(value);
    }

    @Override
    public T set(int index, T value) {
        this.enable();
        return super.set(index, value);
    }

    protected abstract AssociatedEndpoint<T, O> onGetOppositeEndpoint(T opposite);
    
    @Override
    protected void onModifying(IndexedValueEvent<T> e) throws Throwable {
        this.handler().preHandle(e);
    }
    
    private IndexedValueEventHandler<O, T> handler() {
        return new IndexedValueEventHandler<O, T>() {

            @Override
            protected boolean isSuspended() {
                return AssociatedIndexedReference.this.suspended;
            }

            @Override
            protected void setSuspended(boolean suspended) {
                AssociatedIndexedReference.this.suspended = suspended;
            }

            @Override
            protected O getOwner() {
                return AssociatedIndexedReference.this.getOwner();
            }

            @Override
            protected AssociatedEndpointType getOppositeEndpointType() {
                return AssociatedIndexedReference.this.getOppositeEndpointType();
            }

            @Override
            protected AssociatedEndpoint<T, O> getOppositeEndpoint(T opposite) {
                return AssociatedIndexedReference.this.getOppositeEndpoint(opposite);
            }
        };
    }
    
    private void requiredEnabled() {
        if (this.disabled) {
            throw new IllegalStateException(
                    LAZY_COMMON_RESOURCE.get().currentReferenceIsDisabled(this.getClass())
            );
        }
    }
}
