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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.EnumSet;

import org.babyfish.collection.MAOrderedMap;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.conflict.MapConflictVoter;
import org.babyfish.collection.event.MapElementEvent;
import org.babyfish.collection.spi.wrapper.AbstractWrapperMAOrderedMap;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.state.LazinessManageable;
import org.babyfish.util.LazyResource;

/**
 * @author Tao Chen
 */
public abstract class AssociatedOrderedMap<O, K, V> 
extends AbstractWrapperMAOrderedMap<K, V> 
implements AssociatedEndpoint<O, V>, Serializable {

    private static final long serialVersionUID = -5009326562013053974L;
    
    private static final LazyResource<CommonResource> LAZY_COMMON_RESOURCE = LazyResource.of(CommonResource.class);
    
    private static final EnumSet<AssociatedEndpointType> OPPOSITE_TYPES =
            EnumSet.of(
                    AssociatedEndpointType.REFERENCE,
                    AssociatedEndpointType.KEYED_REFERENCE,
                    AssociatedEndpointType.COLLECTION,
                    AssociatedEndpointType.SET);
    
    private transient boolean suspended;
    
    protected AssociatedOrderedMap() {
        super(null);
    }
    
    @Override
    public final boolean isLoaded() {
        MAOrderedMap<K, V> base = this.getBase();
        if (base instanceof LazinessManageable) {
            return ((LazinessManageable)base).isLoaded();
        }
        return true;
    }

    @Override
    public final boolean isLoadable() {
        MAOrderedMap<K, V> base = this.getBase();
        if (base instanceof LazinessManageable) {
            return ((LazinessManageable)base).isLoadable();
        }
        return true;
    }

    @Override
    public final void load() {
        MAOrderedMap<K, V> base = this.getBase();
        if (base instanceof LazinessManageable) {
            ((LazinessManageable)base).load();
        }
    }

    @Override
    public final AssociatedEndpointType getEndpointType() {
        return AssociatedEndpointType.MAP;
    }

    @Override
    public boolean isSuspended() {
        return this.suspended;
    }

    @Override
    protected RootData<K, V> createRootData() {
        return new RootData<K, V>();
    }
    
    /**
     * @exception IllegalProgramException {@link #getOppositeEndpointType()} returns invalid value.
     * the valid return value must be one of these values
     * <ul>
     *  <li>{@link AssociatedEndpointType#REFERENCE}</li>
     *  <li>{@link AssociatedEndpointType#KEYED_REFERENCE}</li>
     *  <li>{@link AssociatedEndpointType#COLLECTION}</li>
     *  <li>{@link AssociatedEndpointType#SET}</li>
     * </ul>
     */
    @Override
    public final AssociatedEndpointType getOppositeEndpointType() {
        AssociatedEndpointType oppoisteType = this.onGetOppositeEndpointType();
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

    /**
     * @exception IllegalProgramException The {@link #onGetOppositeEndpoint(Object)}
     * returns null or an end point that is not restricted by the return value of 
     * {@link #getOppositeEndpointType()}
     */
    @Override
    public final AssociatedEndpoint<V, O> getOppositeEndpoint(V opposite) {
        AssociatedEndpoint<V, O> oppositeEndpoint = this.onGetOppositeEndpoint(opposite);
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
    
    protected abstract AssociatedEndpointType onGetOppositeEndpointType();
    
    protected abstract AssociatedEndpoint<V, O> onGetOppositeEndpoint(V opposite);
    
    protected boolean isLoadedValue(V value) {
        return true;
    }
    
    protected boolean isAbandonableValue(V value) {
        return false;
    }
    
    protected void loadValue(V value) {
        
    }
    
    @Override
    protected void onModifying(MapElementEvent<K, V> e) throws Throwable {
        this.handler().preHandle(e);
    }
    
    @Override
    protected void onModified(MapElementEvent<K, V> e) throws Throwable {
        this.handler().postHandle(e);
    }
    
    private MapElementEventHandler<O, K, V> handler() {
        return new MapElementEventHandler<O, K, V>() {

            @Override
            protected boolean isSuspended() {
                return AssociatedOrderedMap.this.suspended;
            }

            @Override
            protected void setSuspended(boolean suspended) {
                AssociatedOrderedMap.this.suspended = suspended;
            }

            @Override
            protected O getOwner() {
                return AssociatedOrderedMap.this.getOwner();
            }
            
            @Override
            protected boolean isLoadedObject(V opposite) {
                return AssociatedOrderedMap.this.isLoadedValue(opposite);
            }

            @Override
            protected boolean isAbandonableObject(V opposite) {
                return AssociatedOrderedMap.this.isAbandonableValue(opposite);
            }

            @Override
            protected void loadObject(V opposite) {
                AssociatedOrderedMap.this.loadValue(opposite);
            }
            
            @Override
            protected AssociatedEndpointType getOppositeEndpointType() {
                return AssociatedOrderedMap.this.getOppositeEndpointType();
            }

            @Override
            protected AssociatedEndpoint<V, O> getOppositeEndpoint(V opposite) {
                return AssociatedOrderedMap.this.getOppositeEndpoint(opposite);
            }
        };
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        this.writeState(out);
    }
    
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        this.readState(in);
    }
    
    protected static class RootData<K, V> extends AbstractWrapperMAOrderedMap.RootData<K, V> {
        
        private static final long serialVersionUID = -6128129267074846438L;

        public RootData() {
            
        }

        /**
         * @exception IllegalArgumentException The parameter base's 
         * {@link MAOrderedMap#keyReplacementRule()} does not return {@link ReplacementRule#NEW_REFERENCE_WIN}
         */
        @Override
        protected void setBase(MAOrderedMap<K, V> base) {
            if (this.getBase(true) != base) {
                if (base != null && base.keyReplacementRule() != ReplacementRule.NEW_REFERENCE_WIN) {
                    throw new IllegalArgumentException(
                            LAZY_COMMON_RESOURCE.get().baseReplacementRuleMustBe(
                                    this.getClass(), 
                                    base.keyReplacementRule()
                            )
                    );
                }
                super.setBase(base);
            }
        }
        
        @Override
        protected void onLoadTransientData() {
            MapConflictVoter<K, V> voter = new AssociatedMapConflictVoter<K, V>(this);
            this.getBase().addConflictVoter(voter);
        }

        @Override
        protected void onUnloadTranisentData() {
            MapConflictVoter<K, V> voter = new AssociatedMapConflictVoter<K, V>(this);
            this.getBase().removeConflictVoter(voter);
        }
    }
    
}
