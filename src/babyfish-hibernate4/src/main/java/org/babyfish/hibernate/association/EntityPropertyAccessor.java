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

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import org.babyfish.association.AssociatedEndpoint;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.hibernate.model.metadata.HibernateMetadatas;
import org.babyfish.hibernate.model.metadata.HibernateObjectModelMetadata;
import org.babyfish.lang.reflect.MethodInfo;
import org.babyfish.model.ObjectModel;
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.metadata.ObjectModelMetadata;
import org.babyfish.persistence.model.metadata.JPAAssociationProperty;
import org.babyfish.persistence.model.metadata.JPAProperty;
import org.babyfish.persistence.model.metadata.JPAScalarProperty;
import org.babyfish.reference.IndexedReference;
import org.babyfish.reference.KeyedReference;
import org.babyfish.reference.Reference;
import org.hibernate.HibernateException;
import org.hibernate.PropertyNotFoundException;
import org.hibernate.bytecode.instrumentation.spi.LazyPropertyInitializer;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.property.Getter;
import org.hibernate.property.PropertyAccessor;
import org.hibernate.property.PropertyAccessorFactory;
import org.hibernate.property.Setter;

/**
 * @author Tao Chen
 */
public class EntityPropertyAccessor implements PropertyAccessor {
    
    private PropertyAccessor baseAccessor;
    
    public EntityPropertyAccessor() {
        this.baseAccessor = PropertyAccessorFactory.getPropertyAccessor("property");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Getter getGetter(Class theClass, String propertyName) throws PropertyNotFoundException {
        for (HibernateObjectModelMetadata objectModelMetadata = HibernateMetadatas.of(theClass);
                objectModelMetadata != null;
                objectModelMetadata = objectModelMetadata.getSuperMetadata()) {
            JPAAssociationProperty jpaAssociationProperty = 
                objectModelMetadata.getDeclaredIndexMappingSources().get(propertyName);
            if (jpaAssociationProperty != null && jpaAssociationProperty.getCovarianceProperty() == null) {
                return new IndexGetterForEntityIndexedReference(jpaAssociationProperty);
            }
            jpaAssociationProperty = 
                objectModelMetadata.getDeclaredKeyMappingSources().get(propertyName);
            if (jpaAssociationProperty != null && jpaAssociationProperty.getCovarianceProperty() == null) {
                return new KeyGetterForEntityKeyedReference(jpaAssociationProperty);
            }
            JPAProperty jpaProperty = objectModelMetadata.getDeclaredMappingSources().get(propertyName);
            if (jpaProperty instanceof JPAScalarProperty) {
                return new ScalarGetter((JPAScalarProperty)jpaProperty);
            } else if (jpaProperty instanceof JPAAssociationProperty) {
                jpaAssociationProperty = (JPAAssociationProperty)jpaProperty;
                if (jpaAssociationProperty.getCovarianceProperty() == null) {
                    return AbstractAssociationGetter.of(jpaAssociationProperty);
                }
            } // else null
        }
        return this.baseAccessor.getGetter(theClass, propertyName);
    }

    @SuppressWarnings("rawtypes") 
    @Override
    public Setter getSetter(Class theClass, String propertyName)
            throws PropertyNotFoundException {
        for (HibernateObjectModelMetadata objectModelMetadata = HibernateMetadatas.of(theClass);
                objectModelMetadata != null;
                objectModelMetadata = objectModelMetadata.getSuperMetadata()) {
            JPAAssociationProperty jpaAssociationProperty = 
                objectModelMetadata.getDeclaredIndexMappingSources().get(propertyName);
            if (jpaAssociationProperty != null && jpaAssociationProperty.getCovarianceProperty() == null) {
                return new IndexSetterForEntityIndexedReference(jpaAssociationProperty);
            }
            jpaAssociationProperty = 
                objectModelMetadata.getDeclaredKeyMappingSources().get(propertyName);
            if (jpaAssociationProperty != null && jpaAssociationProperty.getCovarianceProperty() == null) {
                return new KeySetterForEntityKeyedReference(jpaAssociationProperty);
            }
            JPAProperty jpaProperty = objectModelMetadata.getDeclaredMappingSources().get(propertyName);
            if (jpaProperty instanceof JPAScalarProperty) {
                return new ScalarSetter((JPAScalarProperty)jpaProperty);
            } else if (jpaProperty instanceof JPAAssociationProperty) {
                jpaAssociationProperty = (JPAAssociationProperty)jpaProperty;
                if (jpaAssociationProperty.getCovarianceProperty() == null) {
                    return AbstractAssociationSetter.of(jpaAssociationProperty);
                }
            } // else null
        }
        return this.baseAccessor.getSetter(theClass, propertyName);
    }
    
    private static abstract class AbstractOperator {
        
        protected final JPAProperty property;
        
        protected final Method method;
        
        protected final ObjectModelFactory<ObjectModel> objectModelFactory;
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        protected AbstractOperator(JPAProperty property) {
            this.property = property;
            if (this instanceof Getter) {
                this.method = property.getOwnerProperty().getGetter().getRawMethod();
            } else {
                MethodInfo setterInfo = property.getOwnerProperty().getSetter();
                this.method = setterInfo != null ? setterInfo.getRawMethod() : null;
            }
            ObjectModelMetadata objectModelMetadata = this.property.getDeclaringObjectModelMetadata();
            this.objectModelFactory = 
                    (ObjectModelFactory)
                    objectModelMetadata
                    .getProvider()
                    .getFactory(objectModelMetadata.getObjectModelClass());
        }
        
        public String getMethodName() {
            return this.method != null ? this.method.getName() : null;
        }

        public Method getMethod() {
            return this.method;
        }
    }
    
    private static class ScalarGetter extends AbstractOperator implements Getter {

        private static final long serialVersionUID = 4222091132253940930L;

        protected ScalarGetter(JPAScalarProperty jpaScalarProperty) {
            super(jpaScalarProperty);
        }

        @Override
        public Object get(Object owner) throws HibernateException {
            int propertyId = this.property.getId();
            ObjectModel objectModel = this.objectModelFactory.get(owner);
            JPAScalarProperty jpaScalarProperty = (JPAScalarProperty)this.property;
            if (!jpaScalarProperty.isEntityId() && !jpaScalarProperty.isOptimisticLock()) {
                if (objectModel.isDisabled(propertyId)) {
                    return LazyPropertyInitializer.UNFETCHED_PROPERTY;
                }
                if (objectModel.isUnloaded(propertyId)) {
                    return LazyPropertyInitializer.UNFETCHED_PROPERTY;
                }
            }
            return objectModel.getScalar(propertyId);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Object getForInsert(Object owner, Map mergeMap, SessionImplementor session) throws HibernateException {
            return this.get(owner);
        }

        @Override
        public Member getMember() {
            return this.method;
        }

        @Override
        public Class<?> getReturnType() {
            return this.method.getReturnType();
        }
    }
    
    private static class ScalarSetter extends AbstractOperator implements Setter {

        private static final long serialVersionUID = 3102970103802016628L;

        protected ScalarSetter(JPAScalarProperty jpaScalarProperty) {
            super(jpaScalarProperty);
        }

        @Override
        public void set(Object target, Object value, SessionFactoryImplementor factory) throws HibernateException {
            this.objectModelFactory.get(target).setScalar(this.property.getId(), value);
        }
    }
    
    private static abstract class AbstractAssocaitionOperator extends AbstractOperator {
        
        protected AbstractAssocaitionOperator(JPAAssociationProperty jpaAssociationProperty) {
            super(jpaAssociationProperty);
        }

        protected final AssociatedEndpoint<?, ?> getAssociatedEndpoint(Object owner) {
            ObjectModel objectModel = this.objectModelFactory.get(owner);
            return objectModel.getAssociation(this.property.getId());
        }
    }

    private static abstract class AbstractAssociationGetter extends AbstractAssocaitionOperator implements Getter {
        
        private static final long serialVersionUID = -2994547749543192616L;
        
        protected AbstractAssociationGetter(
                JPAAssociationProperty jpaAssociationProperty) {
            super(jpaAssociationProperty);
        }
        
        public static Getter of(
                JPAAssociationProperty jpaAssociationProperty) {
            Class<?> standardReturnClass = jpaAssociationProperty.getStandardReturnClass();
            if (IndexedReference.class == standardReturnClass) {
                return new GetterForEntityIndexedReference(jpaAssociationProperty);
            }
            if (KeyedReference.class == standardReturnClass) {
                return new GetterForEntityKeyedReference(jpaAssociationProperty);
            }
            if (Reference.class == standardReturnClass) {
                return new GetterForEntityReference(jpaAssociationProperty);
            }
            if (NavigableMap.class == standardReturnClass ||
                    SortedMap.class == standardReturnClass) {
                return new GetterForEntityNavigableMap(jpaAssociationProperty);
            }
            if (XOrderedMap.class == standardReturnClass ||
                    Map.class == standardReturnClass) {
                return new GetterForEntityOrderedMap(jpaAssociationProperty);
            }
            if (NavigableSet.class == standardReturnClass ||
                    SortedSet.class == standardReturnClass) {
                return new GetterForEntityNavigableSet(jpaAssociationProperty);
            }
            if (XOrderedSet.class == standardReturnClass || 
                    Set.class == standardReturnClass) {
                return new GetterForEntityOrderedSet(jpaAssociationProperty);
            }
            if (List.class == standardReturnClass) {
                return new GetterForEntityList(jpaAssociationProperty);
            }
            if (Collection.class == standardReturnClass) {
                return new GetterForEntityCollection(jpaAssociationProperty);
            }
            throw new AssertionError();
        }

        @Override
        public Object get(Object owner) throws HibernateException {
            AssociatedEndpoint<?, ?> associatedEndpoint = this.getAssociatedEndpoint(owner);
            return this.getValueFromAssociatedEndpoint(associatedEndpoint);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Object getForInsert(
                Object owner,  
                Map mergeMap,
                SessionImplementor session) 
        throws HibernateException {
            AssociatedEndpoint<?, ?> associatedEndpoint = this.getAssociatedEndpoint(owner);
            return this.getValueFromAssociatedEndpoint(associatedEndpoint);
        }
        
        @Override
        public Member getMember() {
            return this.method;
        }

        @Override
        public Class<?> getReturnType() {
            return this.method.getReturnType();
        }
        
        private Object getValueFromAssociatedEndpoint(AssociatedEndpoint<?, ?> associatedEndpoint) {
            if (associatedEndpoint.isDisabled()) {
                return LazyPropertyInitializer.UNFETCHED_PROPERTY;
            }
            return this.onGetValueFromAssociatedEndpoint(associatedEndpoint);
        }

        protected abstract Object onGetValueFromAssociatedEndpoint(AssociatedEndpoint<?, ?> associatedEndpoint);
    }
    
    private static abstract class AbstractAssociationSetter extends AbstractAssocaitionOperator implements Setter {

        private static final long serialVersionUID = -7638043944533603461L;
        
        protected AbstractAssociationSetter(
                JPAAssociationProperty jpaAssociationProperty) {
            super(jpaAssociationProperty);
        }
        
        public static Setter of(
                JPAAssociationProperty jpaAssociationProperty) {
            Class<?> standardReturnClass = jpaAssociationProperty.getStandardReturnClass();
            if (IndexedReference.class == standardReturnClass) {
                return new SetterForEntityIndexedReference(jpaAssociationProperty);
            }
            if (KeyedReference.class == standardReturnClass) {
                return new SetterForEntityKeyedReference(jpaAssociationProperty);
            }
            if (Reference.class == standardReturnClass) {
                return new SetterForEntityReference(jpaAssociationProperty);
            }
            if (NavigableMap.class == standardReturnClass ||
                    SortedMap.class == standardReturnClass) {
                return new SetterForEntityNavigableMap(jpaAssociationProperty);
            }
            if (XOrderedMap.class == standardReturnClass ||
                    Map.class == standardReturnClass) {
                return new SetterForEntityOrderedMap(jpaAssociationProperty);
            }
            if (NavigableSet.class == standardReturnClass ||
                    SortedSet.class == standardReturnClass) {
                return new SetterForEntityNavigableSet(jpaAssociationProperty);
            }
            if (XOrderedSet.class == standardReturnClass ||
                    Set.class == standardReturnClass) {
                return new SetterForEntityOrderedSet(jpaAssociationProperty);
            }
            if (List.class == standardReturnClass) {
                return new SetterForEntityList(jpaAssociationProperty);
            }
            if (Collection.class == standardReturnClass) {
                return new SetterForEntityCollection(jpaAssociationProperty);
            }
            throw new AssertionError();
        }

        @Override
        public void set(
                Object target, 
                Object value, 
                SessionFactoryImplementor factory) throws HibernateException {
            Object associatedEndpoint = this.getAssociatedEndpoint(target);
            this.setValueIntoAssociatedEndpoint(associatedEndpoint, value);
        }

        protected abstract void setValueIntoAssociatedEndpoint(
                Object associatedEndpoint, 
                Object value);
    }
    
    private static class GetterForEntityReference extends AbstractAssociationGetter {

        private static final long serialVersionUID = -7176784305324018689L;

        protected GetterForEntityReference(
                JPAAssociationProperty jpaAssocationProperty) {
            super(jpaAssocationProperty);
        }

        @Override
        protected Object onGetValueFromAssociatedEndpoint(AssociatedEndpoint<?, ?> associatedEndpoint) {
            return ((EntityReference<?, ?>)associatedEndpoint).hibernateGet();
        }
        
    }
    
    private static class SetterForEntityReference extends AbstractAssociationSetter {

        private static final long serialVersionUID = -1669320418033256201L;

        protected SetterForEntityReference(
                JPAAssociationProperty jpaAssocationProperty) {
            super(jpaAssocationProperty);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void setValueIntoAssociatedEndpoint(Object associatedEndpoint, Object value) {
            ((EntityReference<?, Object>)associatedEndpoint).hibernateSet(value);
        }
        
    }
    
    private static class GetterForEntityIndexedReference extends AbstractAssociationGetter {

        private static final long serialVersionUID = -3921571047589868383L;

        protected GetterForEntityIndexedReference(
                JPAAssociationProperty jpaAssociationProperty) {
            super(jpaAssociationProperty);
        }

        @Override
        protected Object onGetValueFromAssociatedEndpoint(AssociatedEndpoint<?, ?> associatedEndpoint) {
            return ((EntityIndexedReference<?, ?>)associatedEndpoint).hibernateGet();
        }
        
    }
    
    private static class SetterForEntityIndexedReference extends AbstractAssociationSetter {

        private static final long serialVersionUID = -5443978794770279L;

        protected SetterForEntityIndexedReference(
                JPAAssociationProperty jpaAssocationProperty) {
            super(jpaAssocationProperty);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void setValueIntoAssociatedEndpoint(Object associatedEndpoint, Object value) {
            ((EntityIndexedReference<?, Object>)associatedEndpoint).hibernateSet(value);
        }
        
    }
    
    private static class IndexGetterForEntityIndexedReference extends AbstractAssociationGetter {
        
        private static final long serialVersionUID = 2918198720341652272L;

        protected IndexGetterForEntityIndexedReference(
                JPAAssociationProperty jpaAssocationProperty) {
            super(jpaAssocationProperty);
        }

        @Override
        public Class<?> getReturnType() {
            return int.class;
        }

        @Override
        protected Object onGetValueFromAssociatedEndpoint(AssociatedEndpoint<?, ?> associatedEndpoint) {
            return ((EntityIndexedReference<?, ?>)associatedEndpoint).hibernateGetIndex();
        }
    }
    
    private static class IndexSetterForEntityIndexedReference extends AbstractAssociationSetter {

        private static final long serialVersionUID = 4929980723044956764L;

        protected IndexSetterForEntityIndexedReference(
                JPAAssociationProperty jpaAssocationProperty) {
            super(jpaAssocationProperty);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void setValueIntoAssociatedEndpoint(Object associatedEndpoint, Object value) {
            ((EntityIndexedReference<?, Object>)associatedEndpoint).hibernateSetIndex(value);
        }
        
    }
    
    private static class GetterForEntityKeyedReference extends AbstractAssociationGetter {

        private static final long serialVersionUID = 1872058564112204734L;

        protected GetterForEntityKeyedReference(
                JPAAssociationProperty jpaAssociationProperty) {
            super(jpaAssociationProperty);
        }

        @Override
        protected Object onGetValueFromAssociatedEndpoint(AssociatedEndpoint<?, ?> associatedEndpoint) {
            return ((EntityKeyedReference<?, ?, ?>)associatedEndpoint).hibernateGet();
        }
        
    }
    
    private static class SetterForEntityKeyedReference extends AbstractAssociationSetter {

        private static final long serialVersionUID = -7241720678328668657L;

        protected SetterForEntityKeyedReference(
                JPAAssociationProperty jpaAssocationProperty) {
            super(jpaAssocationProperty);
        }

        @Override
        protected void setValueIntoAssociatedEndpoint(
                Object associatedEndpoint, Object value) {
            ((EntityKeyedReference<?, ?, ?>)associatedEndpoint).hibernateSet(value);
        }
        
    }
    
    private static class KeyGetterForEntityKeyedReference extends AbstractAssociationGetter {

        private static final long serialVersionUID = -1007047137153949626L;

        protected KeyGetterForEntityKeyedReference(
                JPAAssociationProperty jpaAssocationProperty) {
            super(jpaAssocationProperty);
        }
        
        @Override
        protected Object onGetValueFromAssociatedEndpoint(AssociatedEndpoint<?, ?> associatedEndpoint) {
            return ((EntityKeyedReference<?, ?, ?>)associatedEndpoint).hibernateGetKey();
        }
        
    }
    
    private static class KeySetterForEntityKeyedReference extends AbstractAssociationSetter {

        private static final long serialVersionUID = 9016719822236240751L;

        protected KeySetterForEntityKeyedReference(
                JPAAssociationProperty jpaAssocationProperty) {
            super(jpaAssocationProperty);
        }

        @Override
        protected void setValueIntoAssociatedEndpoint(
                Object associatedEndpoint, Object value) {
            ((EntityKeyedReference<?, ?, ?>)associatedEndpoint).hibernateSetKey(value);
        }
        
    }
    
    private static class GetterForEntityCollection extends AbstractAssociationGetter {
    
        private static final long serialVersionUID = -7004127605450643374L;
    
        protected GetterForEntityCollection(
                JPAAssociationProperty jpaAssociationProperty) {
            super(jpaAssociationProperty);
        }
    
        @Override
        public Class<?> getReturnType() {
            return Collection.class;
        }
    
        @Override
        protected Object onGetValueFromAssociatedEndpoint(AssociatedEndpoint<?, ?> associatedEndpoint) {
            return ((EntityCollection<?, ?>)associatedEndpoint).hibernateGet();
        }
        
    }

    private static class SetterForEntityCollection extends AbstractAssociationSetter {
    
        private static final long serialVersionUID = 1344522075257123131L;
    
        protected SetterForEntityCollection(
                JPAAssociationProperty jpaAssocationProperty) {
            super(jpaAssocationProperty);
        }
    
        @Override
        protected void setValueIntoAssociatedEndpoint(
                Object associatedEndpoint,
                Object value) {
            ((EntityCollection<?, ?>)associatedEndpoint).hibernateSet(value);
        }
        
    }

    private static class GetterForEntityList extends AbstractAssociationGetter {
    
        private static final long serialVersionUID = -6546405961617367703L;
    
        protected GetterForEntityList(
                JPAAssociationProperty jpaAssociationProperty) {
            super(jpaAssociationProperty);
        }
    
        @Override
        public Class<?> getReturnType() {
            return List.class;
        }
    
        @Override
        protected Object onGetValueFromAssociatedEndpoint(AssociatedEndpoint<?, ?> associatedEndpoint) {
            return ((EntityList<?, ?>)associatedEndpoint).hibernateGet();
        }
        
    }

    private static class SetterForEntityList extends AbstractAssociationSetter {
    
        private static final long serialVersionUID = 4134946238034440203L;
    
        protected SetterForEntityList(
                JPAAssociationProperty jpaAssocationProperty) {
            super(jpaAssocationProperty);
        }
    
        @Override
        protected void setValueIntoAssociatedEndpoint(
                Object associatedEndpoint,
                Object value) {
            ((EntityList<?, ?>)associatedEndpoint).hibernateSet(value);
        }
        
    }
    
    private static class GetterForEntityOrderedSet extends AbstractAssociationGetter {

        private static final long serialVersionUID = -781589628646049140L;

        protected GetterForEntityOrderedSet(
                JPAAssociationProperty jpaAssociationProperty) {
            super(jpaAssociationProperty);
        }

        @Override
        public Class<?> getReturnType() {
            return XOrderedSet.class;
        }

        @Override
        protected Object onGetValueFromAssociatedEndpoint(AssociatedEndpoint<?, ?> associatedEndpoint) {
            return ((EntityOrderedSet<?, ?>)associatedEndpoint).hibenrateGet();
        }
        
    }
    
    private static class SetterForEntityOrderedSet extends AbstractAssociationSetter {

        private static final long serialVersionUID = -2940680064006830229L;

        protected SetterForEntityOrderedSet(
                JPAAssociationProperty jpaAssociationProperty) {
            super(jpaAssociationProperty);
        }

        @Override
        protected void setValueIntoAssociatedEndpoint(
                Object associatedEndpoint, Object value) {
            ((EntityOrderedSet<?, ?>)associatedEndpoint).hibernateSet(value);
        }
        
    }
    
    private static class GetterForEntityNavigableSet extends AbstractAssociationGetter {

        private static final long serialVersionUID = 5416227565672593106L;

        protected GetterForEntityNavigableSet(
                JPAAssociationProperty jpaAssociationProperty) {
            super(jpaAssociationProperty);
        }

        @Override
        public Class<?> getReturnType() {
            return NavigableSet.class;
        }

        @Override
        protected Object onGetValueFromAssociatedEndpoint(AssociatedEndpoint<?, ?> associatedEndpoint) {
            return ((EntityNavigableSet<?, ?>)associatedEndpoint).hibernateGet();
        }
        
    }
    
    private static class SetterForEntityNavigableSet extends AbstractAssociationSetter {

        private static final long serialVersionUID = 1485563829925770284L;

        protected SetterForEntityNavigableSet(
                JPAAssociationProperty jpaAssociationProperty) {
            super(jpaAssociationProperty);
        }

        @Override
        protected void setValueIntoAssociatedEndpoint(
                Object associatedEndpoint, Object value) {
            ((EntityNavigableSet<?, ?>)associatedEndpoint).hibernateSet(value);
        }
        
    }
    
    private static class GetterForEntityOrderedMap extends AbstractAssociationGetter {

        private static final long serialVersionUID = 2639065008066802338L;

        protected GetterForEntityOrderedMap(
                JPAAssociationProperty jpaAssociationProperty) {
            super(jpaAssociationProperty);
        }

        @Override
        public Class<?> getReturnType() {
            return XOrderedMap.class;
        }

        @Override
        protected Object onGetValueFromAssociatedEndpoint(AssociatedEndpoint<?, ?> associatedEndpoint) {
            return ((EntityOrderedMap<?, ?, ?>)associatedEndpoint).hibernateGet();
        }
        
    }
    
    private static class SetterForEntityOrderedMap extends AbstractAssociationSetter {

        private static final long serialVersionUID = -1850017763212308627L;

        protected SetterForEntityOrderedMap(
                JPAAssociationProperty jpaAssociationProperty) {
            super(jpaAssociationProperty);
        }

        @Override
        protected void setValueIntoAssociatedEndpoint(
                Object associatedEndpoint, Object value) {
            ((EntityOrderedMap<?, ?, ?>)associatedEndpoint).hibernateSet(value);
        }
        
    }
    
    private static class GetterForEntityNavigableMap extends AbstractAssociationGetter {

        private static final long serialVersionUID = 755384633166058171L;

        protected GetterForEntityNavigableMap(
                JPAAssociationProperty jpaAssociationProperty) {
            super(jpaAssociationProperty);
        }

        @Override
        public Class<?> getReturnType() {
            return NavigableMap.class;
        }

        @Override
        protected Object onGetValueFromAssociatedEndpoint(AssociatedEndpoint<?, ?> associatedEndpoint) {
            return ((EntityNavigableMap<?, ?, ?>)associatedEndpoint).hibernateGet();
        }
        
    }
    
    private static class SetterForEntityNavigableMap extends AbstractAssociationSetter {

        private static final long serialVersionUID = 2223559722320875229L;

        protected SetterForEntityNavigableMap(
                JPAAssociationProperty jpaAssociationProperty) {
            super(jpaAssociationProperty);
        }

        @Override
        protected void setValueIntoAssociatedEndpoint(
                Object associatedEndpoint, Object value) {
            ((EntityNavigableMap<?, ?, ?>)associatedEndpoint).hibernateSet(value);
        }
        
    }
}
