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
import org.babyfish.reference.MAKeyedReferenceImpl;
import org.babyfish.reference.event.KeyedValueEvent;
import org.babyfish.util.LazyResource;

/**
 * @author Tao Chen
 */
public abstract class AssociatedKeyedReference<O, K, T> 
extends MAKeyedReferenceImpl<K, T> 
implements AssociatedEndpoint<O, T> {
    
    private static final long serialVersionUID = -657466488844239792L;
    
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
        return AssociatedEndpointType.KEYED_REFERENCE;
    }

    @Override
    public boolean isSuspended() {
        return this.suspended;
    }
    
    @Override
    public final AssociatedEndpointType getOppositeEndpointType() {
        return AssociatedEndpointType.MAP;
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
    public K getKey() {
        this.requiredEnabled();
        return super.getKey();
    }

    @Override
    public K getKey(boolean absolute) {
        this.requiredEnabled();
        return super.getKey(absolute);
    }

    @Override
    public K setKey(K key) {
        this.enable();
        return super.setKey(key);
    }

    @Override
    public T set(T value) {
        this.enable();
        return super.set(value);
    }

    @Override
    public T set(K key, T value) {
        this.enable();
        return super.set(key, value);
    }

    protected abstract AssociatedEndpoint<T, O> onGetOppositeEndpoint(T opposite);
    
    protected boolean isLoadedValue(T value) {
        return true;
    }
    
    protected boolean isAbandonableValue(T value) {
        return false;
    }
    
    protected void loadValue(T value) {
        
    }

    /*
     * Specially, be different with other associated end-points, 
     * AssociatiedKeyedReference chooses to change the opposite end-point
     * in the "modifying" event, not the "modified" event. 
     * 
     * For the Map-KeyReference association, if the KeyedReference is 
     * changed to be null, the modifying event with the opposite map removing 
     * happen before the modification of the KeyedRefence itself, so that
     * the flush in the "visionallyRemove" of the map will not update the
     * database and the "visionallyRemove" need not to load the map, because
     * the key to be removed can still be queried from database.
     */
    @Override
    protected void onModifying(KeyedValueEvent<K, T> e) throws Throwable {
        this.handler().preHandle(e);
    }
    
    private KeyedValueEventHandler<O, K, T> handler() {
        return new KeyedValueEventHandler<O, K, T>() {

            @Override
            protected boolean isSuspended() {
                return AssociatedKeyedReference.this.suspended;
            }

            @Override
            protected void setSuspended(boolean suspended) {
                AssociatedKeyedReference.this.suspended = suspended;
            }

            @Override
            protected O getOwner() {
                return AssociatedKeyedReference.this.getOwner();
            }
            
            @Override
            protected boolean isLoadedObject(T opposite) {
                return AssociatedKeyedReference.this.isLoadedValue(opposite);
            }

            @Override
            protected boolean isAbandonableObject(T opposite) {
                return AssociatedKeyedReference.this.isAbandonableValue(opposite);
            }

            @Override
            protected void loadObject(T opposite) {
                AssociatedKeyedReference.this.loadValue(opposite);
            }

            @Override
            protected AssociatedEndpointType getOppositeEndpointType() {
                return AssociatedKeyedReference.this.getOppositeEndpointType();
            }

            @Override
            protected AssociatedEndpoint<T, O> getOppositeEndpoint(T opposite) {
                return AssociatedKeyedReference.this.getOppositeEndpoint(opposite);
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
