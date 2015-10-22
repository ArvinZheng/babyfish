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

import org.babyfish.collection.MACollection;
import org.babyfish.collection.MAList;
import org.babyfish.collection.MASet;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.XCollection;
import org.babyfish.collection.XList;
import org.babyfish.collection.XSet;
import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.spi.wrapper.AbstractWrapperMACollection;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.state.LazinessManageable;
import org.babyfish.util.LazyResource;
import org.babyfish.validator.Validator;

/**
 * @author Tao Chen
 */
public abstract class AssociatedCollection<O, E> 
extends AbstractWrapperMACollection<E> 
implements AssociatedEndpoint<O, E>, Serializable {

    private static final long serialVersionUID = -2326578967727210545L;

    private static final LazyResource<CommonResource> LAZY_COMMON_RESOURCE = LazyResource.of(CommonResource.class);
    
    private static final EnumSet<AssociatedEndpointType> OPPOSITE_TYPES =
            EnumSet.of(
                    AssociatedEndpointType.REFERENCE,
                    AssociatedEndpointType.COLLECTION,
                    AssociatedEndpointType.SET,
                    AssociatedEndpointType.LIST,
                    AssociatedEndpointType.MAP);

    private transient boolean suspended;
    
    protected AssociatedCollection() {
        super(null);
    }

    @Override
    public final boolean isLoaded() {
        XCollection<E> base = this.getBase();
        if (base instanceof LazinessManageable) {
            return ((LazinessManageable)base).isLoaded();
        }
        return true;
    }

    @Override
    public final boolean isLoadable() {
        XCollection<E> base = this.getBase();
        if (base instanceof LazinessManageable) {
            return ((LazinessManageable)base).isLoadable();
        }
        return true;
    }

    @Override
    public final void load() {
        XCollection<E> base = this.getBase();
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
        return AssociatedEndpointType.COLLECTION;
    }

    /**
     * @exception IllegalProgramException {@link #getOppositeEndpointType()} returns invalid value.
     * the valid return value must be one of these values
     * <ul>
     *  <li>{@link AssociatedEndpointType#REFERENCE}</li>
     *  <li>{@link AssociatedEndpointType#COLLECTION}</li>
     *  <li>{@link AssociatedEndpointType#SET}</li>
     *  <li>{@link AssociatedEndpointType#LIST}</li>
     *  <li>{@link AssociatedEndpointType#MAP}</li>
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

    protected boolean isLoadedElement(E element) {
        return true;
    }
    
    protected boolean isAbandonableElement(E element) {
        return false;
    }
    
    protected void loadElement(E element) {
        
    }

    @Override
    protected void onModified(ElementEvent<E> e) throws Throwable {
        new ElementEventHandler<O, E>() {

            @Override
            protected boolean isSuspended() {
                return AssociatedCollection.this.suspended;
            }

            @Override
            protected void setSuspended(boolean suspended) {
                AssociatedCollection.this.suspended = suspended;
            }

            @Override
            protected O getOwner() {
                return AssociatedCollection.this.getOwner();
            }

            @Override
            protected boolean isLoadedObject(E opposite) {
                return AssociatedCollection.this.isLoadedElement(opposite);
            }

            @Override
            protected boolean isAbandonableObject(E opposite) {
                return AssociatedCollection.this.isAbandonableElement(opposite);
            }

            @Override
            protected void loadObject(E opposite) {
                AssociatedCollection.this.loadElement(opposite);
            }

            @Override
            protected AssociatedEndpointType getOppositeEndpointType() {
                return AssociatedCollection.this.getOppositeEndpointType();
            }

            @Override
            protected AssociatedEndpoint<E, O> getOppositeEndpoint(E opposite) {
                return AssociatedCollection.this.getOppositeEndpoint(opposite);
            }
            
        }.postHandle(e);
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

    protected static class RootData<E> extends AbstractWrapperMACollection.RootData<E> {

        private static final long serialVersionUID = -35659177477240502L;
        
        public RootData() {
            
        }

        /**
         * @exception IllegalArgumentException 
         * <ul>
         *    <li> base is neither instance of {@link MASet} nor instance of {@link MAList}</li>
         *    <li> 
         *      base is XSet and its {@link XSet#replacementRule()} 
         *      does not return {@link ReplacementRule#NEW_REFERENCE_WIN}
         *    </li>
         * </ul>
         */
        @Override
        protected void setBase(MACollection<E> base) {
            if (this.getBase(true) != base) {
                Arguments.mustBeInstanceOfAnyOfValue("base", base, MASet.class, MAList.class);
                if (base instanceof XSet<?> && 
                        ((XSet<?>)base).replacementRule() != ReplacementRule.NEW_REFERENCE_WIN) {
                    throw new IllegalArgumentException(
                            LAZY_COMMON_RESOURCE.get().baseReplacementRuleMustBe(
                                    this.getClass(), 
                                    ReplacementRule.NEW_REFERENCE_WIN)
                            );
                }
                super.setBase(base);
            }
        }

        @Override
        protected void onLoadTransientData() {
            XCollection<E> base = this.getBase();
            Validator<E> validator = new AssociatedCollectionValidator<E>(
                    this.<AssociatedCollection<?, E>>getRootWrapper());
            base.addValidator(validator);
            if (base instanceof XList<?>) {
                AssociatedListConflictVoter<E> voter = 
                        new AssociatedListConflictVoter<E>(this);
                ((XList<E>)base).addConflictVoter(voter);
            }
        }

        @Override
        protected void onUnloadTranisentData() {
            XCollection<E> base = this.getBase();
            Validator<E> validator = new AssociatedCollectionValidator<E>(
                    this.<AssociatedCollection<?, E>>getRootWrapper());
            base.removeValidator(validator);
            if (base instanceof XList<?>) {
                AssociatedListConflictVoter<E> voter = 
                        new AssociatedListConflictVoter<E>(this);
                ((XList<E>)base).removeConflictVoter(voter);
            }
        }
    }
    
}
