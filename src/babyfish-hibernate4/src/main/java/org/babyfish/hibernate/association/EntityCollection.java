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
package org.babyfish.hibernate.association;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import org.babyfish.association.AssociatedCollection;
import org.babyfish.collection.MACollection;
import org.babyfish.collection.MALinkedHashSet;
import org.babyfish.collection.MAOrderedSet;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.hibernate.collection.spi.PersistentCollection;
import org.babyfish.util.LazyResource;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.loader.CollectionAliases;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.Type;

/*
 * In BabyFish ObjectModel4JPA, 
 * MACollection<E> is actually supported as MAOrderedSet<E> so that EntityOrderedSet can be used to to support it,
 * but why does this EntityCollection<E> still wrote here?
 * 
 * The reason is the EntityCollection.getEndpointType() returns AssociatedEndpointType.COLLECTION;
 * but the EntityOrderedSet.getEndpointType() returns AssociatedEndpointType.SET.
 */


/**
 * @author Tao Chen
 */
public abstract class EntityCollection<O, E> 
extends AssociatedCollection<O, E>
implements PersistentCollection<E> {

    private static final long serialVersionUID = -6334579074811986151L;
    
    private static LazyResource<CommonResource> LAZY_COMMON_RESOURCE = LazyResource.of(CommonResource.class);

    @Override
    protected RootData<E> createRootData() {
        return new RootData<E>();
    }

    @Override
    protected boolean isLoadedElement(E element) {
        return Hibernate.isInitialized(element);
    }

    @Override
    protected boolean isAbandonableElement(E element) {
        //This endpoint is not inverse means the opposite end point is inverse.
        if (!this.isInverse() && element instanceof HibernateProxy) {
            HibernateProxy proxy = (HibernateProxy)element;
            SessionImplementor session = proxy.getHibernateLazyInitializer().getSession();
            return session == null ||
                    !session.isOpen() ||
                    !session.isConnected();
        }
        return false;
    }

    @Override
    protected void loadElement(E element) {
        Hibernate.initialize(element);
    }
    
    protected abstract boolean isInverse();

    Collection<E> hibernateGet() {
        return this.getBase();
    }

    void hibernateSet(Object value) {
        this.replace(value);
    }
    
    protected static class RootData<E> extends AssociatedCollection.RootData<E> {
        
        private static final long serialVersionUID = -1813688502791186932L;
    
        /*
         * The EntityCollection of babyfish-JPA is special, it is MAOrderedSet actually. 
         */
        @Override
        protected MAOrderedSet<E> createDefaultBase(UnifiedComparator<? super E> unifiedComparator) {
            return new MALinkedHashSet<>(unifiedComparator.equalityComparator(true));
        }

        @Deprecated
        @Override
        protected final void setBase(MACollection<E> base) {
            this.setBase((MAOrderedSet<E>)base);
        }
        
        /*
         * The EntityCollection of babyfish-JPA is special, it is MAOrderedSet actually. 
         */
        protected void setBase(MAOrderedSet<E> base) {
            super.setBase(base);
        }
        
    }
    
    /*
     * Implements the methods of "org.babyfish.hibernate.collection.spi.Persistence",
     * which is the derived interface of "org.hibernate.collection.PersistentCollection"
     * in order to change the ugly methods to be generic methods.
     * 
     * Actually, the collection is a wrapper of "org.hibernate.collection.PersistentCollection"
     * so it need not to implement that interface.
     * 
     * But Hibernate.isInitialized(Object) and Hibernate.initialize(Object)
     * required that the collections are always implementations of 
     * "org.hibernate.collection.PersistentCollection".
     * 
     * If the class does not implement that interface, the 
     * Hibernate.isInitialized(Object) and Hibernate.initialize(Object)
     * can work normally. This is not a good idea, even if the user
     * can cast the collection to "org.babyfish.model.LazinessManageable" and invoke its
     * isLoaded() and load().
     * 
     * So this class implements "org.hibernate.collection.PersistentCollection"
     * and deprecated all the methods except wasInitialized() and forceInitialization().
     * because
     * Hibernate.isInitialized(Object) invokes 
     * "org.hibernate.collection.PersistentCollection.wasInitialized()"
     * and
     * Hibernate.initialized(Object) invokes 
     * "org.hibernate.collection.PersistentCollection.forceInitialization()"
     */
    @Override
    public final boolean wasInitialized() {
        return this.isLoaded();
    }

    @Override
    public final void forceInitialization() throws HibernateException {
        this.load();
    }

    @Deprecated
    @Override
    public final void setOwner(Object entity) {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final boolean empty() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final void setSnapshot(Serializable key, String role, Serializable snapshot) {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final void postAction() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final Object getValue() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final void beginRead() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final boolean endRead() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final boolean afterInitialize() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final boolean isDirectlyAccessible() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final boolean unsetSession(SessionImplementor currentSession) {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final boolean setCurrentSession(SessionImplementor session)
            throws HibernateException {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final void initializeFromCache(CollectionPersister persister,
            Serializable disassembled, Object owner) throws HibernateException {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final Object getIdentifier(Object entry, int i) {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final Object getIndex(Object entry, int i, CollectionPersister persister) {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final Object getSnapshotElement(Object entry, int i) {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final void beforeInitialize(CollectionPersister persister,
            int anticipatedSize) {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final boolean equalsSnapshot(CollectionPersister persister)
            throws HibernateException {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final boolean isSnapshotEmpty(Serializable snapshot) {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final Serializable disassemble(CollectionPersister persister)
            throws HibernateException {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final boolean needsRecreate(CollectionPersister persister) {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final Serializable getSnapshot(CollectionPersister persister)
            throws HibernateException {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final boolean entryExists(Object entry, int i) {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final boolean needsInserting(Object entry, int i, Type elemType)
            throws HibernateException {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final boolean needsUpdating(Object entry, int i, Type elemType)
            throws HibernateException {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final boolean isRowUpdatePossible() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final boolean isWrapper(Object collection) {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final boolean hasQueuedOperations() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final Serializable getKey() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final String getRole() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final boolean isUnreferenced() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final boolean isDirty() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final void clearDirty() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final Serializable getStoredSnapshot() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final void dirty() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final void preInsert(CollectionPersister persister)
            throws HibernateException {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final void afterRowInsert(CollectionPersister persister, Object entry,
            int i) throws HibernateException {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final E getElement(Object entry) {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final Iterator<E> entries(CollectionPersister persister) {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final E readFrom(ResultSet rs, CollectionPersister persister,
            CollectionAliases descriptor, Object owner)
            throws HibernateException, SQLException {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final Iterator<E> getDeletes(CollectionPersister persister,
            boolean indexIsFormula) throws HibernateException {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final Collection<E> getOrphans(Serializable snapshot, String entityName)
            throws HibernateException {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final Iterator<E> queuedAdditionIterator() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final Collection<E> getQueuedOrphans(String entityName) {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityCollection.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }
}
