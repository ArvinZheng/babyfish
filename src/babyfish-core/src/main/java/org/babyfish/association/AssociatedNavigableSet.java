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

import org.babyfish.collection.MANavigableSet;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.XCollection;
import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.spi.wrapper.AbstractWrapperMANavigableSet;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.state.LazinessManageable;
import org.babyfish.util.LazyResource;
import org.babyfish.validator.Validator;

/**
 * @author Tao Chen
 */
public abstract class AssociatedNavigableSet<O, E> 
extends AbstractWrapperMANavigableSet<E>
implements AssociatedEndpoint<O, E>, Serializable {

    private static final long serialVersionUID = -6477156082715116768L;
    
    private static final LazyResource<CommonResource> LAZY_COMMON_RESOURCE = LazyResource.of(CommonResource.class);
    
    private static final EnumSet<AssociatedEndpointType> OPPOSITE_TYPES =
            EnumSet.of(
                    AssociatedEndpointType.REFERENCE,
                    AssociatedEndpointType.COLLECTION,
                    AssociatedEndpointType.SET,
                    AssociatedEndpointType.LIST,
                    AssociatedEndpointType.MAP);
    
    private transient boolean suspended;
    
    protected AssociatedNavigableSet() {
        super(null);
    }

    @Override
    public final boolean isLoaded() {
        MANavigableSet<E> base = this.getBase();
        if (base instanceof LazinessManageable) {
            return ((LazinessManageable)base).isLoaded();
        }
        return true;
    }

    @Override
    public final boolean isLoadable() {
        MANavigableSet<E> base = this.getBase();
        if (base instanceof LazinessManageable) {
            return ((LazinessManageable)base).isLoadable();
        }
        return true;
    }

    @Override
    public final void load() {
        MANavigableSet<E> base = this.getBase();
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
        return AssociatedEndpointType.SET;
    }
    
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
    
    protected boolean isLoadedElement(E element) {
        return true;
    }
    
    protected boolean isAbandonableElement(E element) {
        return false;
    }
    
    protected void loadElement(E element) {
        
    }

    @Override
    protected void onModifying(ElementEvent<E> e) throws Throwable {
        this.handler().preHandle(e);
    }
    
    @Override
    protected void onModified(ElementEvent<E> e) throws Throwable {
        this.handler().postHandle(e);
    }
    
    private ElementEventHandler<O, E> handler() {
        return new ElementEventHandler<O, E>() {

            @Override
            protected boolean isSuspended() {
                return AssociatedNavigableSet.this.suspended;
            }

            @Override
            protected void setSuspended(boolean suspended) {
                AssociatedNavigableSet.this.suspended = suspended;
            }

            @Override
            protected O getOwner() {
                return AssociatedNavigableSet.this.getOwner();
            }
            
            @Override
            protected boolean isLoadedObject(E opposite) {
                return AssociatedNavigableSet.this.isLoadedElement(opposite);
            }

            @Override
            protected boolean isAbandonableObject(E opposite) {
                return AssociatedNavigableSet.this.isAbandonableElement(opposite);
            }

            @Override
            protected void loadObject(E opposite) {
                AssociatedNavigableSet.this.loadElement(opposite);
            }

            @Override
            protected AssociatedEndpointType getOppositeEndpointType() {
                return AssociatedNavigableSet.this.getOppositeEndpointType();
            }

            @Override
            protected AssociatedEndpoint<E, O> getOppositeEndpoint(E opposite) {
                return AssociatedNavigableSet.this.getOppositeEndpoint(opposite);
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

    protected static class RootData<E> extends AbstractWrapperMANavigableSet.RootData<E> {

        private static final long serialVersionUID = -6184184662628611484L;
        
        public RootData() {
            
        }

        @Override
        protected void setBase(MANavigableSet<E> base) {
            if (this.getBase(true) != base) {
                if (base != null && base.replacementRule() != ReplacementRule.NEW_REFERENCE_WIN) {
                    throw new IllegalArgumentException(
                            LAZY_COMMON_RESOURCE.get().baseReplacementRuleMustBe(
                                    this.getClass(), 
                                    base.replacementRule()
                            )
                    );
                }
                super.setBase(base);
            }
        }
        
        @Override
        protected void onLoadTransientData() {
            XCollection<E> base = this.getBase();
            Validator<E> validator = new AssociatedCollectionValidator<E>(
                    this.<AssociatedNavigableSet<?, E>>getRootWrapper());
            base.addValidator(validator);
        }

        @Override
        protected void onUnloadTranisentData() {
            XCollection<E> base = this.getBase();
            Validator<E> validator = new AssociatedCollectionValidator<E>(
                    this.<AssociatedNavigableSet<?, E>>getRootWrapper());
            base.removeValidator(validator);
        }
        
    }
}
