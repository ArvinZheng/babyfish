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
package org.babyfish.hibernate.model.loader;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.hibernate.dialect.LimitedListDialect;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.Nulls;
import org.babyfish.model.ObjectModel;
import org.babyfish.model.metadata.ObjectModelMetadata;
import org.babyfish.model.metadata.ScalarProperty;
import org.babyfish.model.spi.ObjectModelLoaderDirtinessAware;
import org.babyfish.model.spi.ObjectModelScalarLoader;
import org.babyfish.persistence.model.metadata.JPAObjectModelMetadata;
import org.babyfish.persistence.model.metadata.JPAProperty;
import org.babyfish.persistence.model.metadata.JPAScalarProperty;
import org.hibernate.FlushMode;
import org.hibernate.LazyInitializationException;
import org.hibernate.bytecode.instrumentation.spi.AbstractFieldInterceptor;
import org.hibernate.bytecode.instrumentation.spi.FieldInterceptor;
import org.hibernate.bytecode.internal.javassist.FieldHandler;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.transform.ResultTransformer;

/**
 * @author Tao Chen
 */
public class HibernateObjectModelScalarLoader implements 
    ObjectModelScalarLoader, 
    ObjectModelLoaderDirtinessAware, 
    FieldInterceptor, 
    FieldHandler, 
    Serializable {
    
    private static final long serialVersionUID = 515855421447825375L;
    
    private static final int STATE_INITIALIZIED = 1;
    
    private static final int STATE_UNINITIALIZIED = 2;

    private ObjectModel defaultObjectModel;
    
    private transient SessionImplementor session;
    
    private boolean dirty;

    @SuppressWarnings("unchecked")
    public HibernateObjectModelScalarLoader(Object objectModel, FieldHandler handler) {
        Arguments.mustBeInstanceOfValue("objectModel", objectModel, ObjectModel.class);
        Arguments.mustBeInstanceOfValue("handler", handler, AbstractFieldInterceptor.class);
        this.defaultObjectModel = (ObjectModel)objectModel;
        AbstractFieldInterceptor abstractFieldInterceptor = (AbstractFieldInterceptor)handler;
        this.session = abstractFieldInterceptor.getSession();
        Set<String> uninitializedFields = abstractFieldInterceptor.getUninitializedFields();
        if (!Nulls.isNullOrEmpty(uninitializedFields)) {
            Map<String, JPAProperty> mappingSources = 
                    ((JPAObjectModelMetadata)this.defaultObjectModel.getObjectModelMetadata()).getMappingSources();
            for (String uninitializedField : uninitializedFields) {
                JPAProperty jpaProperty = mappingSources.get(uninitializedField);
                this.defaultObjectModel.unload(jpaProperty.getId());
            }
        }
    }
    
    public ObjectModel getObjectModel() {
        return this.defaultObjectModel;
    }

    @Override
    public void setSession(SessionImplementor session) {
        this.session = session;
    }

    @Override
    public boolean isInitialized() {
        int state = getInitializationState();
        return (state & STATE_INITIALIZIED) != 0 && (state & STATE_UNINITIALIZIED) == 0;
    }
    
    public boolean isIncompletelyInitialized() {
        int state = getInitializationState();
        return (state & STATE_INITIALIZIED) != 0 && (state & STATE_UNINITIALIZIED) != 0;
    }

    @Override
    public boolean isInitialized(String field) {
        JPAObjectModelMetadata objectModelMetadata = (JPAObjectModelMetadata)this.defaultObjectModel.getObjectModelMetadata();
        JPAProperty property = objectModelMetadata.getMappingSources().get(field);
        return !this.defaultObjectModel.isUnloaded(property.getId());
    }

    @Override
    public void dirty() {
        this.dirty = true;
    }

    @Override
    public boolean isDirty() {
        return this.dirty;
    }

    @Override
    public void clearDirty() {
        this.dirty = false;
    }
    
    private int getInitializationState() {
        int initializationState = 0;
        ObjectModel objectModel = this.defaultObjectModel;
        ObjectModelMetadata objectModelMetadata = objectModel.getObjectModelMetadata();
        for (ScalarProperty scalarProperty : objectModelMetadata.getScalarProperties().values()) {
            if (scalarProperty.isDeferrable()) {
                int propertyId = scalarProperty.getId();
                if (objectModel.isDisabled(propertyId) || objectModel.isUnloaded(propertyId)) {
                    initializationState |= STATE_UNINITIALIZIED;
                } else {
                    initializationState |= STATE_INITIALIZIED;
                }
                if ((initializationState & (STATE_UNINITIALIZIED | STATE_INITIALIZIED)) == (STATE_UNINITIALIZIED | STATE_INITIALIZIED)) {
                    break;
                }
            }
        }
        return initializationState;   
    }

    @Override
    public final void loadScalars(Collection<ObjectModel> objectModels, int[] scalarPropertyIds) {
        
        SessionImplementor session = this.session;
        if (session == null) {
            throw new LazyInitializationException("entity with lazy properties is not associated with a session");
        }
        else if (!session.isOpen() || !session.isConnected()) {
            throw new LazyInitializationException("session is not connected");
        }
        
        int partitionSize = -1;
        Dialect dialect = session.getFactory().getDialect();
        if (dialect instanceof LimitedListDialect) {
            int maxListLength = ((LimitedListDialect)dialect).getMaxListLength();
            if (objectModels.size() > maxListLength) {
                partitionSize = maxListLength;
            }
        }
        
        if (partitionSize == -1) {
            this.loadScalarsImpl(objectModels, scalarPropertyIds);
            return;
        }
        
        List<ObjectModel> objectModelList;
        if (objectModels instanceof List<?>) {
            objectModelList = (List<ObjectModel>)objectModels;
        } else {
            objectModelList = new ArrayList<>(objectModels);
        }
        while (!objectModelList.isEmpty()) {
            if (objectModelList.size() <= partitionSize) {
                this.loadScalarsImpl(objectModelList, scalarPropertyIds);
                break;
            }
            this.loadScalarsImpl(objectModelList.subList(0, partitionSize), scalarPropertyIds);
            objectModelList = objectModelList.subList(partitionSize, objectModelList.size());
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadScalarsImpl(Collection<ObjectModel> objectModels, int[] scalarPropertyIds) {
        boolean batch = objectModels.size() > 1;
        ObjectModel firstObjectModel = objectModels.iterator().next();
        JPAObjectModelMetadata jpaObjectModelMetadata = 
                (JPAObjectModelMetadata)
                firstObjectModel
                .getObjectModelMetadata();
        JPAScalarProperty entityIdProperty = jpaObjectModelMetadata.getEntityIdProperty();
        Map<Object, ObjectModel> idMap = new LinkedHashMap<>();
        for (ObjectModel objectModel : objectModels) {
            idMap.put(objectModel.getScalar(entityIdProperty.getId()), objectModel);
        }
        
        CriteriaImpl criteria = new CriteriaImpl(jpaObjectModelMetadata.getOwnerClass().getName(), session);
        ProjectionList projectionList = Projections.projectionList();
        if (batch) {
            String ownerIdPropertyName =
                    entityIdProperty
                    .getOwnerProperty()
                    .getName();
            projectionList.add(Projections.property(ownerIdPropertyName));
        }
        for (int scalarPropertyId : scalarPropertyIds) {
            String ownerPropertyName = 
                    jpaObjectModelMetadata
                    .getScalarProperty(scalarPropertyId)
                    .getOwnerProperty()
                    .getName();
            projectionList.add(Projections.property(ownerPropertyName));
        }
        if (batch) {
            criteria
            .add(
                    Restrictions.in(
                            entityIdProperty.getOwnerProperty().getName(), 
                            idMap.keySet()
                    )
            );
        } else {
            criteria
            .add(
                    Restrictions.eq(
                            entityIdProperty.getOwnerProperty().getName(), 
                            idMap.keySet().iterator().next()
                    )
            );
        }
        criteria
        .setProjection(projectionList)
        .setResultTransformer(new ResultTransformer() {
            
            private static final long serialVersionUID = -1387181124646452221L;
            
            @Override
            public Object transformTuple(Object[] tuple, String[] aliases) {
                return tuple;
            }
            @SuppressWarnings("rawtypes")
            @Override
            public List transformList(List collection) {
                return collection;
            }
        });
        List<Object[]> tuples;
        FlushMode oldFlushMode = session.getFlushMode();
        session.setFlushMode(FlushMode.MANUAL);
        try {
            tuples = (List<Object[]>)criteria.list();
        } finally {
            session.setFlushMode(oldFlushMode);
        }
        if (batch) {
            for (Object[] tuple : tuples) {
                ObjectModel objectModel = idMap.get(tuple[0]);
                for (int i = scalarPropertyIds.length - 1; i >= 0; i--) {
                    objectModel.setScalar(scalarPropertyIds[i], tuple[i + 1]);
                }
            }
        } else {
            Object[] firstTuple = tuples.get(0);
            for (int i = scalarPropertyIds.length - 1; i >= 0; i--) {
                firstObjectModel.setScalar(scalarPropertyIds[i], firstTuple[i]);
            }
        }
    }
    
    /*
     * All of these read/write interceptor methods are deprecated and final,
     * because 
     * (1) babyfish must keep some compatibilities with hibernate so that this class must 
     *      implement the interface "org.hibernate.bytecode.internal.javassist.FieldHandler"
     * (2) but, actually, babyfish never invokes them, all the functionalities have been
     *      implemented in the dynamically-generated-bytecode of ObjectModel. 
     */
    @Deprecated
    @Override
    public final int writeInt(Object obj, String name, int oldValue, int newValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final char writeChar(Object obj, String name, char oldValue, char newValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final byte writeByte(Object obj, String name, byte oldValue, byte newValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final boolean writeBoolean(Object obj, String name, boolean oldValue, boolean newValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final short writeShort(Object obj, String name, short oldValue, short newValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final float writeFloat(Object obj, String name, float oldValue, float newValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final double writeDouble(Object obj, String name, double oldValue, double newValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final long writeLong(Object obj, String name, long oldValue, long newValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final Object writeObject(Object obj, String name, Object oldValue, Object newValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final int readInt(Object obj, String name, int oldValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final char readChar(Object obj, String name, char oldValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final byte readByte(Object obj, String name, byte oldValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final boolean readBoolean(Object obj, String name, boolean oldValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final short readShort(Object obj, String name, short oldValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final float readFloat(Object obj, String name, float oldValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final double readDouble(Object obj, String name, double oldValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final long readLong(Object obj, String name, long oldValue) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public final Object readObject(Object obj, String name, Object oldValue) {
        throw new UnsupportedOperationException();
    }
}
