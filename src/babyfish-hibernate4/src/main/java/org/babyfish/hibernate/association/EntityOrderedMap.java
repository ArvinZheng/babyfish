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
import java.util.Map;

import org.babyfish.association.AssociatedOrderedMap;
import org.babyfish.hibernate.collection.spi.PersistentCollection;
import org.babyfish.util.LazyResource;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.loader.CollectionAliases;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.Type;

/**
 * @author Tao Chen
 */
public abstract class EntityOrderedMap<O, K, V> extends AssociatedOrderedMap<O, K, V> implements PersistentCollection<V> {

    private static final long serialVersionUID = 6771523680037334618L;
    
    private static LazyResource<CommonResource> LAZY_COMMON_RESOURCE = LazyResource.of(CommonResource.class);
    
    @Override
    protected boolean isLoadedValue(V value) {
        return Hibernate.isInitialized(value);
    }

    @Override
    protected boolean isAbandonableValue(V value) {
        //This endpoint is not inverse means the opposite end point is inverse.
        if (!this.isInverse() && value instanceof HibernateProxy) {
            HibernateProxy proxy = (HibernateProxy)value;
            SessionImplementor session = proxy.getHibernateLazyInitializer().getSession();
            return session == null ||
                    !session.isOpen() ||
                    !session.isConnected();
        }
        return false;
    }

    @Override
    protected void loadValue(V value) {
        Hibernate.initialize(value);
    }
    
    protected abstract boolean isInverse();

    Map<K, V> hibernateGet() {
        return this.getBase();
    }
    
    void hibernateSet(Object value) {
        this.replace(value);
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
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final boolean empty() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final void setSnapshot(Serializable key, String role, Serializable snapshot) {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final void postAction() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final Object getValue() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final void beginRead() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final boolean endRead() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final boolean afterInitialize() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final boolean isDirectlyAccessible() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final boolean unsetSession(SessionImplementor currentSession) {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
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
                        EntityOrderedMap.class, 
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
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final Object getIdentifier(Object entry, int i) {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final Object getIndex(Object entry, int i, CollectionPersister persister) {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final Object getSnapshotElement(Object entry, int i) {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
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
                        EntityOrderedMap.class, 
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
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final boolean isSnapshotEmpty(Serializable snapshot) {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
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
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final boolean needsRecreate(CollectionPersister persister) {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
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
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final boolean entryExists(Object entry, int i) {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
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
                        EntityOrderedMap.class, 
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
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final boolean isRowUpdatePossible() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final boolean isWrapper(Object collection) {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final boolean hasQueuedOperations() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final Serializable getKey() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final String getRole() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final boolean isUnreferenced() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final boolean isDirty() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final void clearDirty() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final Serializable getStoredSnapshot() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final void dirty() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
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
                        EntityOrderedMap.class, 
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
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final V getElement(Object entry) {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final Iterator<V> entries(CollectionPersister persister) {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final V readFrom(ResultSet rs, CollectionPersister persister,
            CollectionAliases descriptor, Object owner)
            throws HibernateException, SQLException {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final Iterator<V> getDeletes(CollectionPersister persister,
            boolean indexIsFormula) throws HibernateException {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final Collection<V> getOrphans(Serializable snapshot, String entityName)
            throws HibernateException {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final Iterator<V> queuedAdditionIterator() {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }

    @Deprecated
    @Override
    public final Collection<V> getQueuedOrphans(String entityName) {
        throw new UnsupportedOperationException(
                LAZY_COMMON_RESOURCE.get().persistentCollectionBehaviorIsInvalid(
                        EntityOrderedMap.class, 
                        org.hibernate.collection.spi.PersistentCollection.class
                )
        );
    }
}
