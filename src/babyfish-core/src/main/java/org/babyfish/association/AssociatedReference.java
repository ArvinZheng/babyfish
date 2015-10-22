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

import java.util.EnumSet;

import org.babyfish.lang.IllegalProgramException;
import org.babyfish.reference.IndexedReference;
import org.babyfish.reference.KeyedReference;
import org.babyfish.reference.MAReferenceImpl;
import org.babyfish.reference.event.ValueEvent;
import org.babyfish.util.LazyResource;

/**
 * @author Tao Chen
 */
public abstract class AssociatedReference<O, T> 
extends MAReferenceImpl<T> 
implements AssociatedEndpoint<O, T> {
    
    private static final long serialVersionUID = -9058759202938343593L;
    
    private static final LazyResource<CommonResource> LAZY_COMMON_RESOURCE = LazyResource.of(CommonResource.class);
    
    private static final EnumSet<AssociatedEndpointType> OPPOSITE_TYPES =
            EnumSet.of(
                    AssociatedEndpointType.REFERENCE,
                    AssociatedEndpointType.COLLECTION,
                    AssociatedEndpointType.SET,
                    AssociatedEndpointType.LIST,
                    AssociatedEndpointType.MAP);
    
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
        if (this instanceof IndexedReference<?>) {
            return AssociatedEndpointType.INDEXED_REFERENCE;
        }
        if (this instanceof KeyedReference<?, ?>) {
            return AssociatedEndpointType.KEYED_REFERENCE;
        }
        return AssociatedEndpointType.REFERENCE;
    }

    @Override
    public boolean isSuspended() {
        return this.suspended;
    }
    
    @Override
    public T get(boolean absolute) {
        this.requiredEnabled();
        return super.get(absolute);
    }

    @Override
    public T set(T value) {
        if (value != null && !(this instanceof KeyedReference<?, ?>)) {
            AssociatedEndpoint<T, O> oppositeEndpoint = this.getOppositeEndpoint(value);
            if (oppositeEndpoint != null &&
                    !oppositeEndpoint.isSuspended() && 
                    oppositeEndpoint.getEndpointType() == AssociatedEndpointType.MAP) {
                throw new UnsupportedOperationException(
                        LAZY_COMMON_RESOURCE.get().invalidModificationWhenOppositeEndpointIs(
                                this.getClass(), 
                                this.getEndpointType(), 
                                oppositeEndpoint.getEndpointType()
                        )
                );
            }
        }
        this.enable();
        return super.set(value);
    }
    
    @Override
    public final AssociatedEndpointType getOppositeEndpointType() {
        AssociatedEndpointType oppoisteType = this.onGetOppositeEndpointType();
        if (oppoisteType == null) {
            return null;
        }
        if (!OPPOSITE_TYPES.contains(oppoisteType)) {
            throw new IllegalProgramException(
                    LAZY_COMMON_RESOURCE.get().invalidImplemententOfOnGetOppositeEndpointType(
                            this.getClass(), 
                            OPPOSITE_TYPES
                    )
            );
        }
        return oppoisteType;
    }

    @Override
    public final AssociatedEndpoint<T, O> getOppositeEndpoint(T opposite) {
        AssociatedEndpointType expectedOppositeEndpointType = this.getOppositeEndpointType();
        if (expectedOppositeEndpointType == null) {
            return null;
        }
        AssociatedEndpoint<T, O> oppositeEndpoint = this.onGetOppositeEndpoint(opposite);
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
    
    protected abstract AssociatedEndpointType onGetOppositeEndpointType();
    
    protected abstract AssociatedEndpoint<T, O> onGetOppositeEndpoint(T opposite);

    protected boolean isLoadedValue(T value) {
        return true;
    }
    
    protected boolean isAbandonableValue(T value) {
        return false;
    }
    
    protected void loadValue(T value) {
        
    }

    @Override
    protected void onModifying(ValueEvent<T> e) throws Throwable {
        this.handler().preHandle(e);
    }

    private void requiredEnabled() {
        if (this.disabled) {
            throw new IllegalStateException(
                    LAZY_COMMON_RESOURCE.get().currentReferenceIsDisabled(this.getClass())
            );
        }
    }
    
    private ValueEventHandler<O, T> handler() {
        return new ValueEventHandler<O, T>() {

            @Override
            protected boolean isSuspended() {
                return AssociatedReference.this.suspended;
            }

            @Override
            protected void setSuspended(boolean suspended) {
                AssociatedReference.this.suspended = suspended;
            }

            @Override
            protected O getOwner() {
                return AssociatedReference.this.getOwner();
            }
            
            @Override
            protected boolean isLoadedObject(T opposite) {
                return AssociatedReference.this.isLoadedValue(opposite);
            }

            @Override
            protected boolean isAbandonableObject(T opposite) {
                return AssociatedReference.this.isAbandonableValue(opposite);
            }

            @Override
            protected void loadObject(T opposite) {
                AssociatedReference.this.loadValue(opposite);
            }
            
            @Override
            protected AssociatedEndpointType getOppositeEndpointType() {
                return AssociatedReference.this.getOppositeEndpointType();
            }

            @Override
            protected AssociatedEndpoint<T, O> getOppositeEndpoint(T opposite) {
                return AssociatedReference.this.getOppositeEndpoint(opposite);
            }
            
        };
    }
}
