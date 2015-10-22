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

import org.babyfish.collection.MAList;
import org.babyfish.collection.XList;
import org.babyfish.collection.event.ListElementEvent;
import org.babyfish.collection.spi.wrapper.AbstractWrapperMAList;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.state.LazinessManageable;
import org.babyfish.util.LazyResource;
import org.babyfish.validator.Validator;

/**
 * @author Tao Chen
 */
public abstract class AssociatedList<O, E> 
extends AbstractWrapperMAList<E> 
implements AssociatedEndpoint<O, E>, Serializable {
    
    private static final long serialVersionUID = 7541738276478793792L;
    
    private static final LazyResource<CommonResource> LAZY_COMMON_RESOURCE = LazyResource.of(CommonResource.class);
    
    private static final EnumSet<AssociatedEndpointType> OPPOSITE_TYPES =
            EnumSet.of(
                    AssociatedEndpointType.REFERENCE,
                    AssociatedEndpointType.INDEXED_REFERENCE,
                    AssociatedEndpointType.COLLECTION,
                    AssociatedEndpointType.SET);
    
    private transient boolean suspended;
    
    public AssociatedList() {
        super(null);
    }
    
    @Override
    public final boolean isLoaded() {
        MAList<E> base = this.getBase();
        if (base instanceof LazinessManageable) {
            return ((LazinessManageable)base).isLoaded();
        }
        return true;
    }

    @Override
    public final boolean isLoadable() {
        MAList<E> base = this.getBase();
        if (base instanceof LazinessManageable) {
            return ((LazinessManageable)base).isLoadable();
        }
        return true;
    }

    @Override
    public final void load() {
        MAList<E> base = this.getBase();
        if (base instanceof LazinessManageable) {
            ((LazinessManageable)base).load();
        }
    }

    @Override
    public final boolean isSuspended() {
        return this.suspended;
    }

    @Override
    public final AssociatedEndpointType getEndpointType() {
        return AssociatedEndpointType.LIST;
    }
    
    /**
     * @exception IllegalProgramException {@link #getOppositeEndpointType()} returns invalid value.
     * the valid return value must be one of these values
     * <ul>
     *  <li>{@link AssociatedEndpointType#REFERENCE}
     *  <li>{@link AssociatedEndpointType#INDEXED_REFERENCE}</li>
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
    public final AssociatedEndpoint<E, O> getOppositeEndpoint(E opposite) {
        AssociatedEndpoint<E, O> oppositeEndpoint = this.onGetOppositeEndpoint(opposite);
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
    
    protected abstract AssociatedEndpoint<E, O> onGetOppositeEndpoint(E opposite);
    
    @Override
    protected void onModifying(ListElementEvent<E> e) {
        this.handler().preHandle(e);
    }

    @Override
    protected void onModified(ListElementEvent<E> e) {
        this.handler().postHandle(e);
    }
    
    private ListElementEventHandler<O, E> handler() {
        return new ListElementEventHandler<O, E>() {
            
            @Override
            protected MAList<E> getBase() {
                return AssociatedList.this.getBase();
            }

            @Override
            protected boolean isSuspended() {
                return AssociatedList.this.suspended;
            }

            @Override
            protected void setSuspended(boolean suspended) {
                AssociatedList.this.suspended = suspended;
            }

            @Override
            protected O getOwner() {
                return AssociatedList.this.getOwner();
            }

            @Override
            protected AssociatedEndpointType getOppositeEndpointType() {
                return AssociatedList.this.getOppositeEndpointType();
            }

            @Override
            protected AssociatedEndpoint<E, O> getOppositeEndpoint(E opposite) {
                return AssociatedList.this.getOppositeEndpoint(opposite);
            }
        };
    }
    
    @Override
    protected RootData<E> createRootData() {
        return new RootData<E>();
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        this.writeState(out);
    }
    
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        this.readState(in);
    }

    protected static class RootData<E> extends AbstractWrapperMAList.RootData<E> {

        private static final long serialVersionUID = 3662444674024868889L;
        
        public RootData() {
            
        }

        @Override
        protected void onLoadTransientData() {
            XList<E> base = this.getBase();
            Validator<E> validator = new AssociatedCollectionValidator<E>(
                    this.<AssociatedList<?, E>>getRootWrapper());
            base.addValidator(validator);
            AssociatedListConflictVoter<E> voter = 
                    new AssociatedListConflictVoter<E>(this);
            base.addConflictVoter(voter);
        }

        @Override
        protected void onUnloadTranisentData() {
            XList<E> base = this.getBase();
            Validator<E> validator = new AssociatedCollectionValidator<E>(
                    this.<AssociatedList<?, E>>getRootWrapper());
            base.removeValidator(validator);
            AssociatedListConflictVoter<E> voter = 
                    new AssociatedListConflictVoter<E>(this);
            base.removeConflictVoter(voter);
        }
    }
}
