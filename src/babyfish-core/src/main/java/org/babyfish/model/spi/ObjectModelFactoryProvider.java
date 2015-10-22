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
package org.babyfish.model.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.babyfish.association.AssociatedCollection;
import org.babyfish.association.AssociatedEndpoint;
import org.babyfish.association.AssociatedEndpointType;
import org.babyfish.association.AssociatedIndexedReference;
import org.babyfish.association.AssociatedKeyedReference;
import org.babyfish.association.AssociatedList;
import org.babyfish.association.AssociatedMap;
import org.babyfish.association.AssociatedNavigableMap;
import org.babyfish.association.AssociatedNavigableSet;
import org.babyfish.association.AssociatedOrderedMap;
import org.babyfish.association.AssociatedOrderedSet;
import org.babyfish.association.AssociatedReference;
import org.babyfish.association.AssociatedSet;
import org.babyfish.collection.FrozenContext;
import org.babyfish.collection.HashMap;
import org.babyfish.collection.HashSet;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XCollection;
import org.babyfish.collection.XMap;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.collection.spi.wrapper.AbstractWrapperXCollection;
import org.babyfish.collection.spi.wrapper.AbstractWrapperXMap;
import org.babyfish.lang.Action;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.AttributeContext;
import org.babyfish.lang.Combiner;
import org.babyfish.lang.Combiners;
import org.babyfish.lang.IllegalConfigurationException;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.lang.Nulls;
import org.babyfish.lang.Singleton;
import org.babyfish.lang.UncheckedException;
import org.babyfish.lang.reflect.asm.ASM;
import org.babyfish.lang.reflect.asm.Catch;
import org.babyfish.lang.reflect.asm.XMethodVisitor;
import org.babyfish.model.NoObjectModelLoaderException;
import org.babyfish.model.ObjectModel;
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.PropertyIsFrozenException;
import org.babyfish.model.event.ScalarEvent;
import org.babyfish.model.event.ScalarListener;
import org.babyfish.model.event.ScalarModificationAware;
import org.babyfish.model.event.modification.ObjectModelModifications;
import org.babyfish.model.event.modification.ObjectModelModifications.SetScalarModification;
import org.babyfish.model.metadata.AssociationProperty;
import org.babyfish.model.metadata.Metadatas;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.ObjectModelMetadata;
import org.babyfish.model.metadata.ObjectModelMode;
import org.babyfish.model.metadata.Property;
import org.babyfish.model.metadata.ScalarProperty;
import org.babyfish.model.viewinfo.ObjectModelViewInfos;
import org.babyfish.modificationaware.event.AttributeScope;
import org.babyfish.modificationaware.event.BubbledProperty;
import org.babyfish.modificationaware.event.BubbledPropertyConverter;
import org.babyfish.modificationaware.event.BubbledSharedProperty;
import org.babyfish.modificationaware.event.BubbledSharedPropertyConverter;
import org.babyfish.modificationaware.event.Cause;
import org.babyfish.modificationaware.event.EventAttributeContext;
import org.babyfish.modificationaware.event.ModificationEvent;
import org.babyfish.modificationaware.event.ModificationEventHandleException;
import org.babyfish.modificationaware.event.spi.GlobalAttributeContext;
import org.babyfish.modificationaware.event.spi.InAllChainAttributeContext;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.Label;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.org.objectweb.asm.Type;
import org.babyfish.reference.IndexedReference;
import org.babyfish.reference.IndexedReferenceImpl;
import org.babyfish.reference.KeyedReference;
import org.babyfish.reference.KeyedReferenceImpl;
import org.babyfish.reference.Reference;
import org.babyfish.reference.ReferenceImpl;
import org.babyfish.state.DisablityManageable;
import org.babyfish.state.LazinessManageable;
import org.babyfish.util.LazyResource;
import org.babyfish.validator.Validator;
import org.babyfish.validator.Validators;
import org.babyfish.view.ViewInfo;

/**
 * @author Tao Chen
 */
public class ObjectModelFactoryProvider extends Singleton implements Serializable {
    
    private static final long serialVersionUID = -3458267123987391046L;
    
    private static final LazyResource<Resource> LAZY_RESOURCE = LazyResource.of(Resource.class);
    
    private static final Map<String, String> PROVIDER_NAME_MAP = createProviderNameMap();
    
    private static final Map<String, ObjectModelFactoryProvider> CACHE = new HashMap<>();
    
    private static final ReadWriteLock CACHE_LOCK = new ReentrantReadWriteLock();
    
    private static final String ROOT_DATA_SIMPLE_NAME = "RootData";
    
    private static final Class<?> SUPER_COLLECTION_ROOT_DATA_TYPE = 
            classOf(AbstractWrapperXCollection.class, ROOT_DATA_SIMPLE_NAME);
    
    private static final Class<?> SUPER_MAP_ROOT_DATA_TYPE = 
            classOf(AbstractWrapperXMap.class, ROOT_DATA_SIMPLE_NAME);
    
    private static final Set<Class<?>> IMMEDIATE_APPENDABLE_SCALAR_TYPES;
    
    private static final Map<Class<?>, Class<?>> ASSOCIATED_END_RESOLVER_MAP;
    
    private static final int OPCODE_CONST_DISABLED = Opcodes.ICONST_1;
    
    private static final int OPCODE_CONST_UNLOADED = Opcodes.ICONST_2;
    
    private static final String OWNER = "{owner}";

    private static final String OBJECT_MODEL_IMPL_AK_SCALAR_LISTENER = "{AK_SCALAR_LISTENER}";
    
    protected static final String OBJECT_MODEL_IMPL_FACTORY = "{FACTORY}";
    
    private static final String OBJECT_MODEL_IMPL_INTERNAL_GET_OBJECT_FACTORY = "{internalGetObjectModelFactory}";
    
    private static final String OBJECT_MODEL_IMPL_DECLARED_PROPERTY_DELEGATE_ARRAY = "{DECLARED_PROPERTY_DELEGATE_ARRAY}";
    
    private static final String OBJECT_MODEL_IMPL_SCALAR_PROPERTY_ID_ARRAY = "{SCALAR_PROPERTY_ID_ARRAY}";
    
    private static final String OBJECT_MODEL_IMPL_DURING_FIRING = "{duringFiring}";
    
    private static final String OBJECT_MODEL_IMPL_INTERNAL_GET_DURING_FIRING = "{internalGetDuringFiring}";
    
    private static final String OBJECT_MODEL_IMPL_INTERNAL_SET_DURING_FIRING = "{internalSetDuringFiring}";
    
    private static final String OBJECT_MODEL_IMPL_INTERNAL_GET_SCALAR_LISTENER = "{internalGetScalarListener}";
    
    private static final String OBJECT_MODEL_IMPL_INTERNAL_SET_LOADING = "{internalSetLoading}";
    
    private static final String OBJECT_MODEL_IMPL_INTERNAL_BATCH_SET_LOADING = "{internalBatchSetLoading}";
    
    private static final String OBJECT_MODEL_IMPL_INTERNAL_LOAD = "{internalLoad}";
    
    private static final String OBJECT_MODEL_IMPL_SCALAR_LISTENER = "{scalarListener}";
    
    private static final String OBJECT_MODEL_IMPL_SCALAR_LISTENER_COMBINER = "{SCALAR_LISTENER_COMBINER}";

    private static final String OBJECT_MODEL_IMPL_FROZEN_CONTEXT_POSTFIX = "{frozenContext}";
    
    private static final String OBJECT_MODEL_IMPL_STATE_POSTFIX = "{state}";
    
    private static final String OBJECT_MODEL_IMPL_EXECUTE_MODIFYING = "{executeModifying}";
    
    private static final String OBJECT_MODEL_IMPL_EXECUTE_MODIFIED = "{executeModified}";
    
    private static final String OBJECT_MODEL_IMPL_OBJECT_MODEL_METADATA = "{OBJECT_MODEL_METADATA}";
    
    private static final String OBJECT_MODEL_IMPL_SCALAR_LOADER = "{scalarLoader}";
    
    private static final String OBJECT_MODEL_IMPL_LOADING = "{loading}";
        
    private static final String ASSOCIATED_END_IMPL_OWNER_OBJECT_MODEL_FACTORY = "{OWNER_OBJECT_MODEL_FACTORY}";
    
    private static final String ASSOCIATED_END_IMPL_KEY_OBJECT_MODEL_FACTORY = "{KEY_OBJECT_MODEL_FACTORY}";
    
    private static final String ASSOCIATED_END_IMPL_RETURN_OBJECT_MODEL_FACTORY = "{ELEMENT_OBJECT_MODEL_FACTORY}";
    
    private static final String ASSOCIATED_END_IMPL_ASSOCIATION_PROPERTY = "{ASSOCIATION_PROPERTY}";
    
    private static final String ASSOCIATED_END_IMPL_KEY_UNIFIED_COMPARATOR = "{KEY_UNIFIED_COMPARATOR}";
    
    private static final String ASSOCIATED_END_IMPL_UNIFIED_COMPARATOR = "{UNIFIED_COMPARATOR}";
    
    private static final String EMBEDDED_SCALAR_LISTENER_IMPL_PARENT_OBJECT_MODEL = "{parentObjectModel}";
    
    private static final String EMBEDDED_BUBBLED_PROPERTY_CONVERTER_IMPL_PARENT_OBJECT_MODEL = "{parentObjectModel}";
    
    private final Map<Class<?>, ObjectModelFactory<?>> cache = new WeakHashMap<>();

    private final ReadWriteLock cacheLock = new ReentrantReadWriteLock();

    protected ObjectModelFactoryProvider() {
        
    }
    
    public static final ObjectModelFactoryProvider of(String providerName) {
        ObjectModelFactoryProvider provider = null;
        Lock lock;
        
        (lock = CACHE_LOCK.readLock()).lock(); //1st locking
        try {
            provider = CACHE.get(providerName); //1st reading
        } finally {
            lock.unlock();
        }
        
        if (provider == null) { //1st checking
            (lock = CACHE_LOCK.readLock()).lock(); //2nd locking
            try {
                provider = CACHE.get(providerName); //2nd reading
                if (provider == null) { //2nd checking
                    provider = create(providerName);
                    CACHE.put(providerName, provider);
                }
            } finally {
                lock.unlock();
            }
        }
        
        return provider;
    }
    
    public static ObjectModelFactoryProvider of(Class<? extends ObjectModelFactoryProvider> providerType) {
        return getInstance(providerType);
    }
    
    @SuppressWarnings("unchecked")
    public final <M> ObjectModelFactory<M> getFactory(Class<M> objectModelClass) {
        Arguments.mustNotBeNull("objectModelType", objectModelClass);
        ObjectModelFactory<?> objectModelFactory;
        Lock lock;
        
        (lock = this.cacheLock.readLock()).lock();
        try {
            objectModelFactory = this.cache.get(objectModelClass); //1st reading
        } finally {
            lock.unlock();
        }
        
        if (objectModelFactory == null) { //1st checking
            (lock = this.cacheLock.writeLock()).lock();
            try {
                objectModelFactory = this.cache.get(objectModelClass); //2nd reading
                if (objectModelFactory == null) { //2nd checking
                    Map<ObjectModelMetadata, ObjectModelFactory<?>> factories = 
                            this.createFactories(objectModelClass);
                    for (Entry<ObjectModelMetadata, ObjectModelFactory<?>> entry : factories.entrySet()) {
                        Class<?> iteratedObjectModelClass = entry.getKey().getObjectModelClass();
                        ObjectModelFactory<?> factory = entry.getValue();
                        this.cache.put(iteratedObjectModelClass, factory);
                        if (iteratedObjectModelClass == objectModelClass) {
                            objectModelFactory = factory;
                        }
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        return (ObjectModelFactory<M>)objectModelFactory;
    }
    
    public static void visitGetObjectModelFactory(
            MethodVisitor mv, 
            ObjectModelMetadata objectModelMetadata) {
        Arguments.mustNotBeNull("mv", mv);
        Arguments.mustNotBeNull("objectModelMetadata", objectModelMetadata);
        mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                objectModelImplClassName(objectModelMetadata).replace('.', '/'),
                OBJECT_MODEL_IMPL_INTERNAL_GET_OBJECT_FACTORY,
                "()" + ASM.getDescriptor(ObjectModelFactory.class),
                false);
    }

    public static void visitGetObjectModel(
            MethodVisitor mv, 
            ObjectModelMetadata objectModelMetadata,
            Action<MethodVisitor> prepareOwnerLambda) {
        Arguments.mustNotBeNull("prepareOwnerLambda", prepareOwnerLambda);
        visitGetObjectModelFactory(mv, objectModelMetadata);
        prepareOwnerLambda.run(mv);
        mv.visitMethodInsn(
                Opcodes.INVOKEINTERFACE, 
                ASM.getInternalName(ObjectModelFactory.class), 
                "get", 
                "(Ljava/lang/Object;)Ljava/lang/Object;",
                true);
    }
    
    public static void visitGetObjectModel(
            MethodVisitor mv, 
            ObjectModelMetadata objectModelMetadata, 
            final int ownerSlotIndex) {
        visitGetObjectModel(mv, objectModelMetadata, new Action<MethodVisitor>() {
            @Override
            public void run(MethodVisitor mv) {
                mv.visitVarInsn(Opcodes.ALOAD, ownerSlotIndex);
            }
        });
    }
    
    private Map<ObjectModelMetadata, ObjectModelFactory<?>> createFactories(Class<?> objectModelClass) {
        Arguments.mustBeInterface("objectModeClass", objectModelClass);
        if (!objectModelClass.isAnnotationPresent(ObjectModelDeclaration.class)) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().objectModelClassMissAnnotation(
                            objectModelClass,
                            ObjectModelDeclaration.class
                    )
            );
        }
        Class<?> ownerClass = objectModelClass.getDeclaringClass();
        if (ownerClass == null) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().objectModelClassMissOwner(objectModelClass)
            );
        }
        ObjectModelMetadata objectModelMetadata = this.getMetadataFactory().getMetadata(ownerClass);
        if (objectModelMetadata.getProvider() != this) {
            // This validation is very important
            throw new IllegalProgramException();
        }
        Map<ObjectModelMetadata, ObjectModelFactory<?>> factories = new HashMap<>();
        this.createFactory(objectModelMetadata, factories);
        return factories;
    }
    
    private void createFactory(
            ObjectModelMetadata objectModelMetadata, 
            Map<ObjectModelMetadata, ObjectModelFactory<?>> factories) {
        if (objectModelMetadata == null || factories.containsKey(objectModelMetadata)) {
            return;
        }
        //It is very important to put the key before the recursion!
        factories.put(objectModelMetadata, null);
        
        /*
         * Super object model factory and key object model factory can not cause cycle dependencies absolutely
         * so that generated byte code consider them as immediately dependencies.
         * That means the dependent object models must be generated before the current object model. 
         */
        createFactory(objectModelMetadata.getSuperMetadata(), factories);
        
        Class<?> clazz = new DefaultObjectModelImplGenerator(objectModelMetadata).getImplementationClass();
        ObjectModelFactory<?> factory;
        try {
            Method method = clazz.getMethod(OBJECT_MODEL_IMPL_INTERNAL_GET_OBJECT_FACTORY);
            factory = (ObjectModelFactory<?>)method.invoke(null);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new AssertionError(ex);
        }
        factories.put(objectModelMetadata, factory);
        
        
        for (ScalarProperty scalarProperty : objectModelMetadata.getDeclaredScalarProperties().values()) {
            createFactory(scalarProperty.getReturnObjectModelMetadata(), factories);
        }
        for (AssociationProperty associationProperty : objectModelMetadata.getDeclaredAssociationProperties().values()) {
            createFactory(associationProperty.getKeyObjectModelMetadata(), factories);
            createFactory(associationProperty.getReturnObjectModelMetadata(), factories);
        }
    }
    
    protected final MetadataFactory getMetadataFactory() {
        MetadataFactory metadataFactory = this.createMetadataFactory();
        if (metadataFactory.provider() != this) {
            throw new IllegalProgramException();
        }
        return metadataFactory;
    }
    
    public MetadataFactory createMetadataFactory() {
        return new MetadataFactory() {
            @Override
            public ObjectModelMetadata onGetMetadata(Class<?> ownerClass) {
                return Metadatas.of(ownerClass);
            }
            @Override
            public void onGenerateGetMetadata(MethodVisitor mv) {
                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC, 
                        Metadatas.class.getName().replace('.', '/'), 
                        "of", 
                        "(Ljava/lang/Class;)" + ASM.getDescriptor(ObjectModelMetadata.class),
                        false);
            }
        };
    }

    protected FactoryImplGenerator createFactoryImplGenerator(ObjectModelMetadata objectModelMetadata) {
        return this.new FactoryImplGenerator(objectModelMetadata);
    }
    
    protected PropertyDelegateImplGenerator createPropertyDelegateImplGenerator(Property property) {
        return this.new PropertyDelegateImplGenerator(property);
    }
    
    protected AssociatedEndpointImplGenerator createAssociatiedEndpointImplGenerator(
            AssociationProperty associationProperty) {
        return this.new AssociatedEndpointImplGenerator(associationProperty);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static ObjectModelFactoryProvider create(String providerName) {
        String objectModelFactoryProviderName = PROVIDER_NAME_MAP.get(providerName);
        if (objectModelFactoryProviderName == null) {
            throw new IllegalConfigurationException(
                    LAZY_RESOURCE.get().illegalProviderName(providerName, PROVIDER_NAME_MAP.keySet(), ObjectModelFactoryProvider.class)
            );
        }
        Class<? extends ObjectModelFactoryProvider> objectModelFactoryProviderType;
        try {
            objectModelFactoryProviderType = (Class)Class.forName(objectModelFactoryProviderName);
        } catch (ClassNotFoundException ex) {
            throw new IllegalConfigurationException(
                    LAZY_RESOURCE.get().notExistingProviderName(providerName, objectModelFactoryProviderName, ObjectModelFactoryProvider.class),
                    ex
            );
        }
        if (!ObjectModelFactoryProvider.class.isAssignableFrom(objectModelFactoryProviderType)) {
            throw new IllegalConfigurationException(
                    LAZY_RESOURCE.get().illegalProviderType(objectModelFactoryProviderType, ObjectModelFactoryProvider.class)
            );
        }
        if (Modifier.isAbstract(objectModelFactoryProviderType.getModifiers())) {
            throw new IllegalConfigurationException(
                    LAZY_RESOURCE.get().abstractProviderType(objectModelFactoryProviderType, ObjectModelFactoryProvider.class)
            );
        }
        return getInstance(objectModelFactoryProviderType);
    }

    private static String objectModelImplClassName(ObjectModelMetadata objectModelMetadata) {
        return 
                objectModelMetadata.getObjectModelClass().getName() +
                "{Implementation:92B8C17E_BF4E_4135_B596_5A76E0FEBF4E}";
    }

    private static String objectModelFactoryImplClassName(ObjectModelMetadata objectModelMetadata, boolean simpleName) {
        String name = "{Factory}";
        if (!simpleName) {
            name = objectModelImplClassName(objectModelMetadata) +
                    '$' +
                    name;
        }
        return name;
    }
    
    private static String propertyDelegateInterfaceName(ObjectModelMetadata objectModelMetadata, boolean simpleName) {
        String name = "{PropertyDelegate}";
        if (!simpleName) {
            name = objectModelImplClassName(objectModelMetadata) +
                    '$' +
                    name;
        }
        return name;
    }
    
    private static String propertyDelegateImplClassName(Property property, boolean simpleName) {
        String name = '{' + property.getName() + ":PropertyDelegateImpl}";
        if (!simpleName) {
            name = objectModelImplClassName(property.getDeclaringObjectModelMetadata()) +
                    '$' +
                    name;
        }
        return name;
    }
    
    private static String associationEndpointImplClassName(AssociationProperty associationProperty, boolean simpleName) {
        String name = '{' + associationProperty.getName() + ":AssociatedEndpointImpl}";
        if (!simpleName) {
            name = objectModelImplClassName(associationProperty.getDeclaringObjectModelMetadata()) +
                    '$' +
                    name;
        }
        return name;
    }
    
    private static String associationEndpointRootDataClassName(AssociationProperty associationProperty, boolean simpleName) {
        String name = ROOT_DATA_SIMPLE_NAME;
        if (!simpleName) {
            name = associationEndpointImplClassName(associationProperty, false) +
                    '$' +
                    name;
        }
        return name;
    }

    private static String embeddedScalarListenerImplClassName(
            ScalarProperty scalarProperty, boolean simpleName) {
        String name = '{' + scalarProperty.getName() + ":EmbeddedScalarListenerImpl}";
        if (!simpleName) {
            name = 
                    objectModelImplClassName(
                            scalarProperty.getDeclaringObjectModelMetadata()
                    ) +
                    "$" +
                    name;
        }
        return name;
    }
    
    private static String embeddedBubbledPropertyConverterImplClassName(
            ScalarProperty scalarProperty, boolean simpleName) {
        String name = "{BubbledPropertyConverterImpl}"; 
        if (!simpleName) {
            name =
                    embeddedScalarListenerImplClassName(scalarProperty, false) +
                    "$" +
                    embeddedBubbledPropertyConverterImplClassName(scalarProperty, true);
        }
        return name;
    }
    
    private static void visitFactoryImplNestClass(ClassVisitor cv, ObjectModelMetadata objectModelMetadata) {
        cv.visitInnerClass(
                objectModelFactoryImplClassName(objectModelMetadata, false).replace('.', '/'), 
                objectModelImplClassName(objectModelMetadata).replace('.', '/'), 
                objectModelFactoryImplClassName(objectModelMetadata, true), 
                Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC);
    }
    
    private static void visitPropertyDelegateNestedClass(ClassVisitor cv, ObjectModelMetadata objectModelMetadata) {
        cv.visitInnerClass(
                propertyDelegateInterfaceName(objectModelMetadata, false).replace('.', '/'), 
                objectModelImplClassName(objectModelMetadata).replace('.', '/'), 
                propertyDelegateInterfaceName(objectModelMetadata, true), 
                Opcodes.ACC_INTERFACE | Opcodes.ACC_PRIVATE | Opcodes.ACC_ABSTRACT | Opcodes.ACC_STATIC);
    }
    
    private static void visitPropertyDelegateImplNestedClass(ClassVisitor cv, Property property) {
        cv.visitInnerClass(
                propertyDelegateImplClassName(property, false).replace('.', '/'), 
                objectModelImplClassName(property.getDeclaringObjectModelMetadata()).replace('.', '/'), 
                propertyDelegateImplClassName(property, true), 
                Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC);
    }
    
    private static void visitAssocationEndpointNestedClasses(ClassVisitor cv, AssociationProperty associationProperty) {
        cv.visitInnerClass(
                associationEndpointImplClassName(associationProperty, false).replace('.', '/'), 
                objectModelImplClassName(associationProperty.getDeclaringObjectModelMetadata()).replace('.', '/'), 
                associationEndpointImplClassName(associationProperty, true), 
                Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC);
        if (associationProperty.isCollection()) {
            cv.visitInnerClass(
                    associationEndpointRootDataClassName(associationProperty, false).replace('.', '/'), 
                    associationEndpointImplClassName(associationProperty, false).replace('.', '/'), 
                    associationEndpointRootDataClassName(associationProperty, true), 
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC);
        }
    }
    
    private static void visitEmbeddedPropertyNestedClasses(ClassVisitor cv, ScalarProperty scalarProperty) {
        if (scalarProperty.isEmbeded()) {
            cv.visitInnerClass(
                    embeddedScalarListenerImplClassName(scalarProperty, false).replace('.', '/'),
                    objectModelImplClassName(
                            scalarProperty.getDeclaringObjectModelMetadata()
                    ).replace('.', '/'), 
                    embeddedScalarListenerImplClassName(scalarProperty, true), 
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC);
            cv.visitInnerClass(
                    embeddedBubbledPropertyConverterImplClassName(scalarProperty, false).replace('.', '/'),
                    embeddedScalarListenerImplClassName(scalarProperty, false).replace('.', '/'), 
                    embeddedBubbledPropertyConverterImplClassName(scalarProperty, true), 
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC);
        }
    }
    
    private static void generateThrowIllegalStateExceptionInsns(MethodVisitor mv, Property metadata) {
        String illegalStateExceptionInternalName = 
            IllegalStateException.class.getName().replace('.', '/');
        mv.visitTypeInsn(Opcodes.NEW, illegalStateExceptionInternalName);
        mv.visitInsn(Opcodes.DUP);
        mv.visitLdcInsn(
                "The current method can not work normally because the metadata of this property is \"" +
                (metadata instanceof AssociationProperty ?
                        AssociationProperty.class.getName() :
                        ScalarProperty.class.getName()) +
                "\"");
        mv.visitMethodInsn(
                Opcodes.INVOKESPECIAL, 
                illegalStateExceptionInternalName, 
                "<init>", 
                "(Ljava/lang/String;)V",
                false);
        mv.visitInsn(Opcodes.ATHROW);
    }

    private static Class<?> classOf(Class<?> ownerClass, String simpleName) {
        for (Class<?> clazz : ownerClass.getDeclaredClasses()) {
            if (clazz.getSimpleName().equals(simpleName)) {
                return clazz;
            }
        }
        throw new AssertionError(
                "Can not found \"" +
                simpleName +
                "\" in \"" +
                ownerClass.getName() +
                "\"");
    }
    
    private static boolean isStateRequired(Property property) {
        if (property instanceof ScalarProperty) {
            return property.isDisabilityAllowed() || ((ScalarProperty)property).isDeferrable();
        }
        return false;
    }
    
    private static boolean isDeferrableScalarProperty(Property property) {
        if (property instanceof ScalarProperty) {
            return ((ScalarProperty)property).isDeferrable();
        }
        return false;
    }
    
    private static boolean isContravarianceAssociationProperty(Property property) {
        if (property instanceof AssociationProperty) {
            return ((AssociationProperty)property).getCovarianceProperty() != null;
        }
        return false;
    }
    
    private static ObjectModelMetadata getRootObjectModelMetadata(ObjectModelMetadata objectModelMetadata) {
        ObjectModelMetadata superMetadata = objectModelMetadata.getSuperMetadata();
        if (superMetadata == null) {
            return objectModelMetadata;
        }
        return getRootObjectModelMetadata(superMetadata);
    }
    
    private class DefaultObjectModelImplGenerator {
        
        private static final int MT_GET_SCALAR = 0;
        
        private static final int MT_SET_SCALAR = 1;
        
        private static final int MT_GET_ASSOCIATION = 2;
        
        private static final int MT_FREEZE_SCALAR = 3;
        
        private static final int MT_UNFREEZE_SCALAR = 4;
        
        private static final int MT_IS_DISABLED = 5;
        
        private static final int MT_DISABLE = 6;
        
        private static final int MT_ENABLE = 7;
        
        private static final int MT_IS_UNLOADED = 8;
        
        private static final int MT_UNLOAD = 9;
        
        private ObjectModelMetadata objectModelMetadata;
        
        private String className;
        
        private String internalName;
        
        private String descriptor;
        
        private String ownerInternalName;
        
        private String ownerDescriptor;
        
        private String propertyDelegateInternalName;
        
        private String propertyDelegateDescriptor;
        
        private Class<?> implementationClass;
        
        protected DefaultObjectModelImplGenerator(ObjectModelMetadata objectModelMetadata) {
            this.objectModelMetadata = objectModelMetadata;
            this.className = objectModelImplClassName(objectModelMetadata);
            this.internalName = this.className.replace('.', '/');
            this.descriptor = 'L' + this.internalName + ';';
            this.ownerInternalName = this.objectModelMetadata.getOwnerClass().getName().replace('.', '/');
            this.ownerDescriptor = 'L' + this.ownerInternalName + ';';
            this.propertyDelegateInternalName = 
                    propertyDelegateInterfaceName(objectModelMetadata, false).replace('.', '/');
            this.propertyDelegateDescriptor = 'L' + this.propertyDelegateInternalName + ';';
            Action<ClassVisitor> cvAction = new Action<ClassVisitor>() {
                @Override
                public void run(ClassVisitor cv) {
                    DefaultObjectModelImplGenerator.this.generate(cv);
                }
            };
            this.implementationClass = ASM.loadDynamicClass(
                    this.objectModelMetadata.getObjectModelClass().getClassLoader(), 
                    this.className, 
                    this.objectModelMetadata.getObjectModelClass().getProtectionDomain(),
                    cvAction);
        }
        
        private void generate(ClassVisitor cv) {
            cv.visit(
                    Opcodes.V1_7, 
                    Opcodes.ACC_PUBLIC, 
                    this.internalName, 
                    null, 
                    "java/lang/Object", 
                    new String[] {
                            ASM.getInternalName(objectModelMetadata.getObjectModelClass()), 
                            ASM.getInternalName(ObjectModelImplementor.class),
                            ASM.getInternalName(ObjectModelAppender.class),
                            ASM.getInternalName(ObjectModelIO.class)
                    });
            
            visitFactoryImplNestClass(cv, objectModelMetadata);
            createFactoryImplGenerator(this.objectModelMetadata);
            visitPropertyDelegateNestedClass(cv, this.objectModelMetadata);
            new PropertyDelegateGenerator(this.objectModelMetadata);
            for (Property property : this.objectModelMetadata.getDeclaredProperties().values()) {
                visitPropertyDelegateImplNestedClass(cv, property);
                ObjectModelFactoryProvider.this.createPropertyDelegateImplGenerator(property);
            }
            for (ScalarProperty scalarProperty : this.objectModelMetadata.getDeclaredScalarProperties().values()) {
                if (scalarProperty.isEmbeded()) {
                    visitEmbeddedPropertyNestedClasses(cv, scalarProperty);
                    new EmbeddedScalarListenerImplGenerator(scalarProperty);
                }
            }
            for (AssociationProperty associationProperty : this.objectModelMetadata.getDeclaredAssociationProperties().values()) {
                visitAssocationEndpointNestedClasses(cv, associationProperty);
                createAssociatiedEndpointImplGenerator(associationProperty);
            }
            
            cv.visitField(
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, 
                    OBJECT_MODEL_IMPL_SCALAR_LISTENER_COMBINER, 
                    ASM.getDescriptor(Combiner.class), 
                    null, 
                    null
            ).visitEnd();
            
            cv.visitField(
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, 
                    OBJECT_MODEL_IMPL_AK_SCALAR_LISTENER, 
                    "Ljava/lang/Object;", 
                    null, 
                    null
            ).visitEnd();
            
            cv.visitField(
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, 
                    OBJECT_MODEL_IMPL_FACTORY, 
                    ASM.getDescriptor(ObjectModelFactory.class), 
                    null,
                    null
            ).visitEnd();
            
            cv.visitField(
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, 
                    OBJECT_MODEL_IMPL_DECLARED_PROPERTY_DELEGATE_ARRAY, 
                    '[' + this.propertyDelegateDescriptor, 
                    null,
                    null
            ).visitEnd();
            
            cv.visitField(
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, 
                    OBJECT_MODEL_IMPL_SCALAR_PROPERTY_ID_ARRAY, 
                    "[I", 
                    null,
                    null
            ).visitEnd();
            
            cv.visitField(
                    Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, 
                    OBJECT_MODEL_IMPL_OBJECT_MODEL_METADATA, 
                    ASM.getDescriptor(ObjectModelMetadata.class), 
                    null,
                    null
            ).visitEnd();
            
            cv.visitField(
                    Opcodes.ACC_PRIVATE, 
                    OWNER, 
                    this.ownerDescriptor,
                    null,
                    null
            ).visitEnd();
            
            if (this.objectModelMetadata.getSuperMetadata() == null) {
                cv.visitField(
                        Opcodes.ACC_PRIVATE, 
                        OBJECT_MODEL_IMPL_DURING_FIRING, 
                        "Z", 
                        null,
                        null
                ).visitEnd();
                cv.visitField(
                        Opcodes.ACC_PRIVATE, 
                        OBJECT_MODEL_IMPL_SCALAR_LISTENER, 
                        ASM.getDescriptor(ScalarListener.class), 
                        null,
                        null
                ).visitEnd();
                cv.visitField(
                        Opcodes.ACC_PRIVATE, 
                        OBJECT_MODEL_IMPL_SCALAR_LOADER, 
                        ASM.getDescriptor(ObjectModelScalarLoader.class), 
                        null,
                        null
                ).visitEnd();
                cv.visitField(
                        Opcodes.ACC_PRIVATE | Opcodes.ACC_TRANSIENT, 
                        OBJECT_MODEL_IMPL_LOADING, 
                        "Z", 
                        null,
                        null
                ).visitEnd();
            }
            
            XMethodVisitor mv;
            generateClinit(cv);
            
            this.generateInit(cv);
            this.generateGetOwner(cv);
            this.generateGetScalar(cv);
            this.generateSetScalar(cv);
            this.generateGetAssociation(cv);
            this.generateFreezeScalarOrUnfreezeScalar(cv, true);
            this.generateFreezeScalarOrUnfreezeScalar(cv, false);
            this.generateIsDisabled(cv);
            this.generateDisable(cv);
            this.generateEnable(cv);
            this.generateIsUnloaded(cv);
            this.generateUnload(cv);
            this.generateLoad(cv);
            this.generateBatchLoad(cv);
            this.generateLoadImpl(cv);
            this.generateIsLoading(cv);
            this.generateGetScalarLoader(cv);
            this.generateSetScalarLoader(cv);
            this.generateAddScalarListenerOrRemoveScalarListener(cv, true);
            this.generateAddScalarListenerOrRemoveScalarListener(cv, false);
            this.generateExcuteScalarEvent(cv, true);
            this.generateExcuteScalarEvent(cv, false);
            this.generateGetObjectModelFactory(cv);
            this.generateGetObjectModelMetadata(cv);
            this.generateInernalGetObjectModelFactory(cv);
            this.generateInternalGetDuringFiring(cv);
            this.generateInternalSetDuringFiring(cv);
            this.generateInternalGetScalarListener(cv);
            this.generateInternalBatchSetLoading(cv);
            this.generateInternalSetLoading(cv);
            this.generateInternalInterceptor(cv);
            this.generateReadPropertiesOrWriteProperties(cv, true);
            this.generateReadPropertiesOrWriteProperties(cv, false);
            this.generateAppendTo(cv);
            this.generateToString(cv);
            
            for (Property property : this.objectModelMetadata.getDeclaredProperties().values()) {
                this.generateProperty(cv, property);
            }
            
            mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PROTECTED, 
                    "writeReplace", 
                    "()Ljava/lang/Object;", 
                    null, 
                    new String[] { ASM.getInternalName(ObjectStreamException.class) } );
            mv.visitCode();
            mv.visitTypeInsn(Opcodes.NEW, ASM.getInternalName(ObjectModelWritingReplacement.class));
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    ASM.getInternalName(ObjectModelWritingReplacement.class), 
                    "<init>", 
                    '(' + ASM.getDescriptor(ObjectModel.class) + ")V",
                    false);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
            
            cv.visitEnd();
        }
    
        private void generateClinit(ClassVisitor cv) {
            XMethodVisitor mv = ASM.visitMethod(
                    cv,
                    Opcodes.ACC_STATIC, 
                    "<clinit>", 
                    "()V", 
                    null,
                    null);
            mv.visitCode();
            
            final int scalarPropertyIdArrayIndex = mv.aSlot("scalarPropertyIdArray");
            final int declaredPropertyDelegeteArrayIndex = mv.aSlot("declaredPropertyDelegateArray");
            
            /*
             * {SCALAR_LISTENER_COMBINER} = Combiners.of(ScalarListener.class);
             */
            mv.visitLdcInsn(Type.getType(ScalarListener.class));
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASM.getInternalName(Combiners.class), 
                    "of", 
                    "(Ljava/lang/Class;)" + ASM.getDescriptor(Combiner.class),
                    false);
            mv.visitFieldInsn(
                    Opcodes.PUTSTATIC,
                    this.internalName,
                    OBJECT_MODEL_IMPL_SCALAR_LISTENER_COMBINER,
                    ASM.getDescriptor(Combiner.class));
            
            /*
             * { AK_SCALAR_LISTENER } = new Object();
             */
            mv.visitTypeInsn(Opcodes.NEW, "java/lang/Object");
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    "java/lang/Object", 
                    "<init>", 
                    "()V",
                    false);
            mv.visitFieldInsn(
                    Opcodes.PUTSTATIC,
                    this.internalName,
                    OBJECT_MODEL_IMPL_AK_SCALAR_LISTENER,
                    "Ljava/lang/Object;");
            
            /*
             * {FACTORY} = new ...FactoryImpl();
             */
            ASM.visitNewObjectWithoutParameters(
                    mv, 
                    objectModelFactoryImplClassName(this.objectModelMetadata, false).replace('.', '/'));
            mv.visitFieldInsn(
                    Opcodes.PUTSTATIC, 
                    this.internalName, 
                    OBJECT_MODEL_IMPL_FACTORY, 
                    ASM.getDescriptor(ObjectModelFactory.class));
            
            /*
             * {OBJECT_MODEL_MATADATA} = ...Metadatas.of(...);
             */
            mv.visitLdcInsn(org.babyfish.org.objectweb.asm.Type.getType(this.objectModelMetadata.getOwnerClass()));
            ObjectModelFactoryProvider.this.getMetadataFactory().generateGetMetadata(mv);
            mv.visitFieldInsn(
                    Opcodes.PUTSTATIC, 
                    this.internalName, 
                    OBJECT_MODEL_IMPL_OBJECT_MODEL_METADATA,
                    ASM.getDescriptor(ObjectModelMetadata.class));
            
            /*
             * {SCALAR_PROPERTY_ID_ARRAY} = new int[] { ... };
             */
            mv.visitLdcInsn(this.objectModelMetadata.getScalarProperties().size());
            mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
            mv.visitVarInsn(Opcodes.ASTORE, scalarPropertyIdArrayIndex);
            int index = 0;
            for (ScalarProperty scalarProperty : this.objectModelMetadata.getScalarProperties().values()) {
                if (scalarProperty.isDeferrable()) {
                    mv.visitVarInsn(Opcodes.ALOAD, scalarPropertyIdArrayIndex);
                    mv.visitLdcInsn(index++);
                    mv.visitLdcInsn(scalarProperty.getId());
                    mv.visitInsn(Opcodes.IASTORE);
                }
            }
            mv.visitVarInsn(Opcodes.ALOAD, scalarPropertyIdArrayIndex);
            mv.visitFieldInsn(
                    Opcodes.PUTSTATIC, 
                    this.internalName, 
                    OBJECT_MODEL_IMPL_SCALAR_PROPERTY_ID_ARRAY, 
                    "[I");
            
            /*
             * {PROPERTY_DELEGATE_ARRAY} = new PropertyDelegate[] {
             *      new ...ProeprtyDelegateImpl(),
             *      new ...ProeprtyDelegateImpl(),
             *      ...
             *      new ...ProeprtyDelegateImpl()
             * };
             */
            mv.visitLdcInsn(this.objectModelMetadata.getDeclaredProperties().size());
            mv.visitTypeInsn(Opcodes.ANEWARRAY, this.propertyDelegateInternalName);
            mv.visitVarInsn(Opcodes.ASTORE, declaredPropertyDelegeteArrayIndex);
            for (Property property : this.objectModelMetadata.getDeclaredProperties().values()) {
                mv.visitVarInsn(Opcodes.ALOAD, declaredPropertyDelegeteArrayIndex);
                mv.visitLdcInsn(property.getId() - this.objectModelMetadata.getDeclaredPropertyBaseId());
                ASM.visitNewObjectWithoutParameters(
                        mv, 
                        propertyDelegateImplClassName(property, false).replace('.', '/')
                );
                mv.visitInsn(Opcodes.AASTORE);
            }
            mv.visitVarInsn(Opcodes.ALOAD, declaredPropertyDelegeteArrayIndex);
            mv.visitFieldInsn(
                    Opcodes.PUTSTATIC, 
                    this.internalName, 
                    OBJECT_MODEL_IMPL_DECLARED_PROPERTY_DELEGATE_ARRAY, 
                    '[' + this.propertyDelegateDescriptor);
            
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateInit(ClassVisitor cv) {
            XMethodVisitor mv;
            mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PUBLIC, 
                    "<init>", 
                    "(Ljava/lang/Object;)V", 
                    null,
                    null);
            mv.visitCode();
            
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    "java/lang/Object", 
                    "<init>", 
                    "()V",
                    false);
            
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitTypeInsn(Opcodes.CHECKCAST, this.ownerInternalName);
            mv.visitFieldInsn(
                    Opcodes.PUTFIELD, 
                    this.internalName, 
                    OWNER,
                    this.ownerDescriptor);
            
            for (AssociationProperty associationProperty : this.objectModelMetadata.getDeclaredAssociationProperties().values()) {
                this.generateInitAssociationPropertyInsns(mv, associationProperty);
            }
            
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateGetOwner(ClassVisitor cv) {
            XMethodVisitor mv;
            mv = ASM.visitMethod(
                    cv,
                    Opcodes.ACC_PUBLIC, 
                    "getOwner", 
                    "()Ljava/lang/Object;", 
                    null, null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.internalName, 
                    OWNER, 
                    this.ownerDescriptor);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateGetScalar(ClassVisitor cv) {
            XMethodVisitor mv;
            mv = ASM.visitMethod(
                    cv,
                    Opcodes.ACC_PUBLIC, 
                    "getScalar", 
                    "(I)Ljava/lang/Object;", 
                    null, 
                    null);
            mv.visitCode();
            this.generatePreOperationByPropertyId(mv, MT_GET_SCALAR);
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    this.internalName, 
                    OBJECT_MODEL_IMPL_DECLARED_PROPERTY_DELEGATE_ARRAY, 
                    '[' + this.propertyDelegateDescriptor);
            mv.visitVarInsn(Opcodes.ILOAD, 1);
            mv.visitInsn(Opcodes.AALOAD);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    this.propertyDelegateInternalName, 
                    "getScalar", 
                    '(' +
                    ASM.getDescriptor(objectModelMetadata.getObjectModelClass()) +
                    ")Ljava/lang/Object;",
                    true);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateSetScalar(ClassVisitor cv) {
            XMethodVisitor mv;
            mv = ASM.visitMethod(
                    cv,
                    Opcodes.ACC_PUBLIC, 
                    "setScalar", 
                    "(ILjava/lang/Object;)V", 
                    null, 
                    null);
            mv.visitCode();
            this.generatePreOperationByPropertyId(mv, MT_SET_SCALAR);
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    this.internalName, 
                    OBJECT_MODEL_IMPL_DECLARED_PROPERTY_DELEGATE_ARRAY, 
                    '[' + this.propertyDelegateDescriptor);
            mv.visitVarInsn(Opcodes.ILOAD, 1);
            mv.visitInsn(Opcodes.AALOAD);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    this.propertyDelegateInternalName, 
                    "setScalar", 
                    '(' +
                    ASM.getDescriptor(this.objectModelMetadata.getObjectModelClass()) +
                    "Ljava/lang/Object;)V",
                    true);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    
        private void generateGetAssociation(ClassVisitor cv) {
            XMethodVisitor mv;
            mv = ASM.visitMethod(
                    cv,
                    Opcodes.ACC_PUBLIC, 
                    "getAssociation", 
                    "(I)" + ASM.getDescriptor(AssociatedEndpoint.class), 
                    null, 
                    null);
            mv.visitCode();
            this.generatePreOperationByPropertyId(mv, MT_GET_ASSOCIATION);
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    this.internalName, 
                    OBJECT_MODEL_IMPL_DECLARED_PROPERTY_DELEGATE_ARRAY, 
                    '[' + this.propertyDelegateDescriptor);
            mv.visitVarInsn(Opcodes.ILOAD, 1);
            mv.visitInsn(Opcodes.AALOAD);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    this.propertyDelegateInternalName, 
                    "getAssociation", 
                    '(' +
                    ASM.getDescriptor(this.objectModelMetadata.getObjectModelClass()) +
                    ")" + 
                    ASM.getDescriptor(AssociatedEndpoint.class),
                    true);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateFreezeScalarOrUnfreezeScalar(ClassVisitor cv, boolean freeze) {
            String frozenContextDesc = ASM.getDescriptor(FrozenContext.class);
            String methodName = freeze ? "freezeScalar" : "unfreezeScalar";
            XMethodVisitor mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PUBLIC, 
                    methodName, 
                    "(I" + frozenContextDesc + ")V", 
                    null,
                    null);
            mv.visitCode();
            this.generatePreOperationByPropertyId(mv, freeze ? MT_FREEZE_SCALAR : MT_UNFREEZE_SCALAR);
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    this.internalName, 
                    OBJECT_MODEL_IMPL_DECLARED_PROPERTY_DELEGATE_ARRAY, 
                    '[' + this.propertyDelegateDescriptor);
            mv.visitVarInsn(Opcodes.ILOAD, 1);
            mv.visitInsn(Opcodes.AALOAD);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE,
                    this.propertyDelegateInternalName,
                    methodName,
                    '(' +
                    this.descriptor +
                    frozenContextDesc +
                    ")V",
                    true);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateIsDisabled(ClassVisitor cv) {
            XMethodVisitor mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PUBLIC, 
                    "isDisabled", 
                    "(I)Z", 
                    null,
                    null);
            mv.visitCode();
            
            if (this.objectModelMetadata.isDisabilityAllowed()) {
                this.generatePreOperationByPropertyId(mv, MT_IS_DISABLED);
                mv.visitFieldInsn(
                        Opcodes.GETSTATIC, 
                        this.internalName, 
                        OBJECT_MODEL_IMPL_DECLARED_PROPERTY_DELEGATE_ARRAY, 
                        '[' + this.propertyDelegateDescriptor);
                mv.visitVarInsn(Opcodes.ILOAD, 1);
                mv.visitInsn(Opcodes.AALOAD);
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE,
                        this.propertyDelegateInternalName,
                        "isDisabled",
                        '(' +
                        this.descriptor +
                        ")Z",
                        true);
                mv.visitInsn(Opcodes.IRETURN);
            } else {
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitInsn(Opcodes.IRETURN);
            }
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateDisable(ClassVisitor cv) {
            XMethodVisitor mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PUBLIC, 
                    "disable", 
                    "(I)V", 
                    null,
                    null);
            mv.visitCode();
            if (this.objectModelMetadata.isDisabilityAllowed()) {
                this.generatePreOperationByPropertyId(mv, MT_DISABLE);
                mv.visitFieldInsn(
                        Opcodes.GETSTATIC, 
                        this.internalName, 
                        OBJECT_MODEL_IMPL_DECLARED_PROPERTY_DELEGATE_ARRAY, 
                        '[' + this.propertyDelegateDescriptor);
                mv.visitVarInsn(Opcodes.ILOAD, 1);
                mv.visitInsn(Opcodes.AALOAD);
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE,
                        this.propertyDelegateInternalName,
                        "disable",
                        '(' +
                        this.descriptor +
                        ")V",
                        true);
                mv.visitInsn(Opcodes.RETURN);
            } else {
                ASM.visitNewObjectWithoutParameters(mv, ASM.getInternalName(UnsupportedOperationException.class));
                mv.visitInsn(Opcodes.ATHROW);
            }
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateEnable(ClassVisitor cv) {
            XMethodVisitor mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PUBLIC, 
                    "enable", 
                    "(I)V", 
                    null,
                    null);
            mv.visitCode();
            if (this.objectModelMetadata.isDisabilityAllowed()) {
                this.generatePreOperationByPropertyId(mv, MT_ENABLE);
                mv.visitFieldInsn(
                        Opcodes.GETSTATIC, 
                        this.internalName, 
                        OBJECT_MODEL_IMPL_DECLARED_PROPERTY_DELEGATE_ARRAY, 
                        '[' + this.propertyDelegateDescriptor);
                mv.visitVarInsn(Opcodes.ILOAD, 1);
                mv.visitInsn(Opcodes.AALOAD);
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE,
                        this.propertyDelegateInternalName,
                        "enable",
                        '(' +
                        this.descriptor +
                        ")V",
                        true);
            }
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateIsUnloaded(ClassVisitor cv) {
            XMethodVisitor mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PUBLIC, 
                    "isUnloaded", 
                    "(I)Z", 
                    null,
                    null);
            mv.visitCode();
            this.generatePreOperationByPropertyId(mv, MT_IS_UNLOADED);
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    this.internalName, 
                    OBJECT_MODEL_IMPL_DECLARED_PROPERTY_DELEGATE_ARRAY, 
                    '[' + this.propertyDelegateDescriptor);
            mv.visitVarInsn(Opcodes.ILOAD, 1);
            mv.visitInsn(Opcodes.AALOAD);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE,
                    this.propertyDelegateInternalName,
                    "isUnloaded",
                    '(' +
                    this.descriptor +
                    ")Z",
                    true);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateUnload(ClassVisitor cv) {
            XMethodVisitor mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PUBLIC, 
                    "unload", 
                    "(I)V", 
                    null,
                    null);
            mv.visitCode();
            this.generatePreOperationByPropertyId(mv, MT_UNLOAD);
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    this.internalName, 
                    OBJECT_MODEL_IMPL_DECLARED_PROPERTY_DELEGATE_ARRAY, 
                    '[' + this.propertyDelegateDescriptor);
            mv.visitVarInsn(Opcodes.ILOAD, 1);
            mv.visitInsn(Opcodes.AALOAD);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE,
                    this.propertyDelegateInternalName,
                    "unload",
                    '(' +
                    this.descriptor +
                    ")V",
                    true);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateLoad(ClassVisitor cv) {
            
            XMethodVisitor mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PUBLIC, 
                    "load", 
                    "([I)V", 
                    null,
                    null);
            mv.visitCode();
            
            final Label validPropertyIdsLabel = new Label();
            
            /*
             * if (Nulls.isNullOrEmpty(propertyIds)) {
             *      return;
             * }
             */
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASM.getInternalName(Nulls.class), 
                    "isNullOrEmpty", 
                    "([I)Z",
                    false);
            mv.visitJumpInsn(Opcodes.IFEQ, validPropertyIdsLabel);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitLabel(validPropertyIdsLabel);
            
            /*
             * this.loadImpl(Collections.singleton(this), propertyIds);
             */
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASM.getInternalName(Collections.class), 
                    "singleton", 
                    "(Ljava/lang/Object;)" + ASM.getDescriptor(Set.class), 
                    false);         
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    this.internalName, 
                    "loadImpl", 
                    "(Ljava/util/Collection;[I)V", 
                    false);
            
            mv.visitInsn(Opcodes.RETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateBatchLoad(ClassVisitor cv) {
            
            XMethodVisitor mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PUBLIC, 
                    "batchLoad", 
                    "(Ljava/util/Collection;[I)V", 
                    null,
                    null);
            mv.visitCode();
            
            final Label validObjectModelsLabel = new Label();
            final Label validPropertyIdsLabel = new Label();
            
            /*
             * if (Nulls.isNullOrEmpty(objectModels)) {
             *      return;
             * }
             */
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASM.getInternalName(Nulls.class), 
                    "isNullOrEmpty", 
                    "(Ljava/util/Collection;)Z",
                    false);
            mv.visitJumpInsn(Opcodes.IFEQ, validObjectModelsLabel);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitLabel(validObjectModelsLabel);
            
            /*
             * if (Nulls.isNullOrEmpty(propertyIds)) {
             *      return;
             * }
             */
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASM.getInternalName(Nulls.class), 
                    "isNullOrEmpty", 
                    "([I)Z",
                    false);
            mv.visitJumpInsn(Opcodes.IFEQ, validPropertyIdsLabel);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitLabel(validPropertyIdsLabel);
            
            /*
             * this.loadImpl(objectModelModels, propertyIds);
             */
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    this.internalName, 
                    "loadImpl", 
                    "(Ljava/util/Collection;[I)V", 
                    false);
            
            mv.visitInsn(Opcodes.RETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateLoadImpl(ClassVisitor cv) {
            
            XMethodVisitor mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PRIVATE, 
                    "loadImpl", 
                    "(Ljava/util/Collection;[I)V", 
                    null,
                    null);
            mv.visitCode();
            
            final Label hasLoaderLabel = new Label();
            final Label beginLoop1Label = new Label();
            final Label isScalarLabel = new Label();
            final Label isUnloadedLabel = new Label();
            final Label continueLoopLabel = new Label();
            final Label endLoop1Label = new Label();
            final Label loadLabel = new Label();
            final Label beginLoop2Label = new Label();
            final Label endLoop2Label = new Label();
            final Label finalExcutionLabel = new Label();
            
            final int loaderIndex = mv.aSlot("loader");
            final int propertyIdSetIndex = mv.aSlot("propertyIdSet");
            final int propertyIdIndex = mv.iSlot("propertyId");
            final int iIndex = mv.iSlot("i");
            final int newLengthIndex = mv.iSlot("newLength");
            final int propertyIdItrIndex = mv.aSlot("propertyIdItr");
            
            /*
             * ObjectLoader loader = this.getScalarLoader();
             * if (loader == null) {
             *      throw new IllegalStateException();
             * }
             */
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    this.internalName, 
                    "getScalarLoader", 
                    "()" + ASM.getDescriptor(ObjectModelScalarLoader.class),
                    false);
            mv.visitVarInsn(Opcodes.ASTORE, loaderIndex);
            mv.visitVarInsn(Opcodes.ALOAD, loaderIndex);
            mv.visitJumpInsn(Opcodes.IFNONNULL, hasLoaderLabel);
            ASM.visitNewObjectWithoutParameters(mv, ASM.getInternalName(NoObjectModelLoaderException.class));
            mv.visitInsn(Opcodes.ATHROW);
            mv.visitLabel(hasLoaderLabel);
            
            /*
             * XOrderedSet<Integer> propertyIdSet = new LinkedHashSet<>((propertyIds.length * 4 + 2) / 3);
             */
            mv.visitTypeInsn(Opcodes.NEW, ASM.getInternalName(LinkedHashSet.class));
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitInsn(Opcodes.ARRAYLENGTH);
            mv.visitInsn(Opcodes.ICONST_4);
            mv.visitInsn(Opcodes.IMUL);
            mv.visitInsn(Opcodes.ICONST_2);
            mv.visitInsn(Opcodes.IADD);
            mv.visitInsn(Opcodes.ICONST_3);
            mv.visitInsn(Opcodes.IDIV);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    ASM.getInternalName(LinkedHashSet.class),
                    "<init>", 
                    "(I)V",
                    false);
            mv.visitVarInsn(Opcodes.ASTORE, propertyIdSetIndex);
            
            /*
             * int i = propertyIds.length - 1;
             * do {
             *      int propertyId = propertyIds[i];
             *      if (!({OBJECT_MODEL_METADATA}.getProperty(propertyId) instance ScalarProperty)) {
             *          this.getAssociation(propertyId).load();
             *          continue;
             *      }
             *      if (!this.isUnloaded(propertyId)) {
             *          continue;
             *      }
             *      propertyIdSet.add(propertyId);
             * } while(--i >= 0);
             */
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitInsn(Opcodes.ARRAYLENGTH);
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitInsn(Opcodes.ISUB);
            mv.visitVarInsn(Opcodes.ISTORE, iIndex);
            mv.visitLabel(beginLoop1Label);
            {
                mv.visitVarInsn(Opcodes.ALOAD, 2);
                mv.visitVarInsn(Opcodes.ILOAD, iIndex);
                mv.visitInsn(Opcodes.IALOAD);
                mv.visitVarInsn(Opcodes.ISTORE, propertyIdIndex);
                
                mv.visitFieldInsn(
                        Opcodes.GETSTATIC, 
                        this.internalName, 
                        OBJECT_MODEL_IMPL_OBJECT_MODEL_METADATA, 
                        ASM.getDescriptor(ObjectModelMetadata.class));
                mv.visitVarInsn(Opcodes.ILOAD, propertyIdIndex);
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        ASM.getInternalName(ObjectModelMetadata.class), 
                        "getProperty", 
                        "(I)" + ASM.getDescriptor(Property.class),
                        true);
                mv.visitTypeInsn(Opcodes.INSTANCEOF, ASM.getInternalName(ScalarProperty.class));
                mv.visitJumpInsn(Opcodes.IFNE, isScalarLabel);
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitVarInsn(Opcodes.ILOAD, propertyIdIndex);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        this.internalName, 
                        "getAssociation", 
                        "(I)" + ASM.getDescriptor(AssociatedEndpoint.class),
                        false);
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        ASM.getInternalName(LazinessManageable.class), 
                        "load", 
                        "()V",
                        true);
                mv.visitJumpInsn(Opcodes.GOTO, continueLoopLabel);
                mv.visitLabel(isScalarLabel);
                
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitVarInsn(Opcodes.ILOAD, propertyIdIndex);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        this.internalName, 
                        "isUnloaded", 
                        "(I)Z",
                        false);
                mv.visitJumpInsn(Opcodes.IFNE, isUnloadedLabel);
                mv.visitJumpInsn(Opcodes.GOTO, continueLoopLabel);
                mv.visitLabel(isUnloadedLabel);
                
                mv.visitVarInsn(Opcodes.ALOAD, propertyIdSetIndex);
                mv.visitBox(int.class, new Action<XMethodVisitor>() {
                    @Override
                    public void run(XMethodVisitor mv) {
                        mv.visitVarInsn(Opcodes.ILOAD, propertyIdIndex);
                    }
                });
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        ASM.getInternalName(Collection.class), 
                        "add", 
                        "(Ljava/lang/Object;)Z",
                        true);
                mv.visitInsn(Opcodes.POP);
                
                mv.visitLabel(continueLoopLabel);
                mv.visitIincInsn(iIndex, -1);
                mv.visitVarInsn(Opcodes.ILOAD, iIndex);
                mv.visitJumpInsn(Opcodes.IFLT, endLoop1Label);
                mv.visitJumpInsn(Opcodes.GOTO, beginLoop1Label);
            }
            mv.visitLabel(endLoop1Label);
            
            /*
             * if (propertyIdSet.isEmtpty()) {
             *      return;
             * }
             */
            mv.visitVarInsn(Opcodes.ALOAD, propertyIdSetIndex);
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    ASM.getInternalName(Collection.class), 
                    "isEmpty", 
                    "()Z",
                    true);
            mv.visitJumpInsn(Opcodes.IFEQ, loadLabel);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitLabel(loadLabel);
            
            /*
             * int newLength = proeprtyIdSet.size();
             * if (propertyIds.length != newLength) {
             *      propertyIds = new int[newLength];
             *      i = 0;
             *      Iterator<Integer> propertyIdItr = propertyIdSet.iterator();
             *      while (propertyItr.hasNext()) {
             *          propertyIds[i++] = propertyItr.next();
             *      }
             * }
             */
            mv.visitVarInsn(Opcodes.ALOAD, propertyIdSetIndex);
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE,
                    ASM.getInternalName(Collection.class),
                    "size",
                    "()I",
                    true);
            mv.visitVarInsn(Opcodes.ISTORE, newLengthIndex);
            mv.visitVarInsn(Opcodes.ILOAD, newLengthIndex);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitInsn(Opcodes.ARRAYLENGTH);
            mv.visitJumpInsn(Opcodes.IF_ICMPEQ, finalExcutionLabel);
            {
                mv.visitVarInsn(Opcodes.ILOAD, newLengthIndex);
                mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
                mv.visitVarInsn(Opcodes.ASTORE, 2);
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitVarInsn(Opcodes.ISTORE, iIndex);
                mv.visitVarInsn(Opcodes.ALOAD, propertyIdSetIndex);
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        ASM.getInternalName(Collection.class), 
                        "iterator", 
                        "()" + ASM.getDescriptor(Iterator.class),
                        true);
                mv.visitVarInsn(Opcodes.ASTORE, propertyIdItrIndex);
                mv.visitLabel(beginLoop2Label);
                {
                    mv.visitVarInsn(Opcodes.ALOAD, propertyIdItrIndex);
                    mv.visitMethodInsn(
                            Opcodes.INVOKEINTERFACE, 
                            ASM.getInternalName(Iterator.class), 
                            "hasNext", 
                            "()Z",
                            true);
                    mv.visitJumpInsn(Opcodes.IFEQ, endLoop2Label);
                    mv.visitVarInsn(Opcodes.ALOAD, 2);
                    mv.visitVarInsn(Opcodes.ILOAD, iIndex);                 
                    mv.visitVarInsn(Opcodes.ALOAD, propertyIdItrIndex);
                    mv.visitMethodInsn(
                            Opcodes.INVOKEINTERFACE, 
                            ASM.getInternalName(Iterator.class), 
                            "next", 
                            "()Ljava/lang/Object;",
                            true);
                    mv.visitUnbox(int.class, null);
                    mv.visitInsn(Opcodes.IASTORE);
                    mv.visitIincInsn(iIndex, +1);
                    mv.visitJumpInsn(Opcodes.GOTO, beginLoop2Label);
                }
                mv.visitLabel(endLoop2Label);
            }
            mv.visitLabel(finalExcutionLabel);
            
            /*
             * this.{internalSetLoading}(true);
             * try {
             *      loader.loadScalars(propertyIds);
             * } finally {
             *      this.{internalSetLoading}(false);
             * }
             */
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    this.internalName, 
                    OBJECT_MODEL_IMPL_INTERNAL_BATCH_SET_LOADING, 
                    "(Ljava/util/Collection;Z)V",
                    false);
            mv.visitTryFinally(
                    new Action<MethodVisitor>() {
                        @Override
                        public void run(MethodVisitor mv) {
                            mv.visitVarInsn(Opcodes.ALOAD, loaderIndex);
                            mv.visitVarInsn(Opcodes.ALOAD, 1);
                            mv.visitVarInsn(Opcodes.ALOAD, 2);
                            mv.visitMethodInsn(
                                    Opcodes.INVOKEINTERFACE, 
                                    ASM.getInternalName(ObjectModelScalarLoader.class), 
                                    "loadScalars", 
                                    "(Ljava/util/Collection;[I)V",
                                    true);
                        }
                    }, 
                    new Action<MethodVisitor>() {
                        @Override
                        public void run(MethodVisitor mv) {
                            mv.visitVarInsn(Opcodes.ALOAD, 1);
                            mv.visitInsn(Opcodes.ICONST_0);
                            mv.visitMethodInsn(
                                    Opcodes.INVOKESTATIC, 
                                    DefaultObjectModelImplGenerator.this.internalName, 
                                    OBJECT_MODEL_IMPL_INTERNAL_BATCH_SET_LOADING, 
                                    "(Ljava/util/Collection;Z)V",
                                    false);
                        }
                    }
            );
            
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateIsLoading(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "isLoading", 
                    "()Z", 
                    null,
                    null);
            mv.visitCode();
            
            if (this.objectModelMetadata.getSuperMetadata() != null) {
                this.generateGetRootObjectModelInsns(mv, 0);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        this.getRootObjectModelImplInternalName(), 
                        "isLoading", 
                        "()Z",
                        false);
            } else {
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        this.internalName, 
                        OBJECT_MODEL_IMPL_LOADING, 
                        "Z");
            }
            mv.visitInsn(Opcodes.IRETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateGetScalarLoader(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "getScalarLoader", 
                    "()" + ASM.getDescriptor(ObjectModelScalarLoader.class), 
                    null,
                    null);
            mv.visitCode();
            
            if (this.objectModelMetadata.getSuperMetadata() != null) {
                this.generateGetRootObjectModelInsns(mv, 0);
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        ASM.getInternalName(ObjectModelImplementor.class), 
                        "getScalarLoader", 
                        "()" + ASM.getDescriptor(ObjectModelScalarLoader.class),
                        true);
            } else {
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.internalName, 
                    OBJECT_MODEL_IMPL_SCALAR_LOADER, 
                    ASM.getDescriptor(ObjectModelScalarLoader.class));
            }
            
            mv.visitInsn(Opcodes.ARETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateSetScalarLoader(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "setScalarLoader", 
                    '(' + ASM.getDescriptor(ObjectModelScalarLoader.class) + ")V", 
                    null,
                    null);
            mv.visitCode();
            
            if (this.objectModelMetadata.getSuperMetadata() != null) {
                this.generateGetRootObjectModelInsns(mv, 0);
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        ASM.getInternalName(ObjectModelImplementor.class), 
                        "setScalarLoader", 
                        '(' + ASM.getDescriptor(ObjectModelScalarLoader.class) + ")V",
                        true);
            } else {
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitFieldInsn(
                        Opcodes.PUTFIELD, 
                        this.internalName,
                        OBJECT_MODEL_IMPL_SCALAR_LOADER, 
                        ASM.getDescriptor(ObjectModelScalarLoader.class));
            }
            mv.visitInsn(Opcodes.RETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generatePreOperationByPropertyId(XMethodVisitor mv, int methodType) {
            if (this.objectModelMetadata.getDeclaredPropertyBaseId() == 0) {
                return;
            }
            
            ObjectModelMetadata superMetadata = this.objectModelMetadata.getSuperMetadata();
            if (superMetadata != null) { //Super properties
                Label isDeclaredFieldLabel = new Label();
                mv.visitVarInsn(Opcodes.ILOAD, 1);
                mv.visitLdcInsn(this.objectModelMetadata.getDeclaredPropertyBaseId());
                mv.visitJumpInsn(Opcodes.IF_ICMPGE, isDeclaredFieldLabel);
                this.generateGetSuperObjectModelInsns(mv);
                mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(ObjectModel.class));
                mv.visitVarInsn(Opcodes.ILOAD, 1);
                String methodName;
                String methodDesc;
                int returnCode;
                switch (methodType) {
                case MT_GET_SCALAR:
                    methodName = "getScalar";
                    methodDesc = "(I)Ljava/lang/Object;";
                    returnCode = Opcodes.ARETURN;
                    break;
                case MT_SET_SCALAR:
                    mv.visitVarInsn(Opcodes.ALOAD, 2);
                    methodName = "setScalar";
                    methodDesc = "(ILjava/lang/Object;)V";
                    returnCode = Opcodes.RETURN;
                    break;
                case MT_GET_ASSOCIATION:
                    methodName = "getAssociation";
                    methodDesc = "(I)" + ASM.getDescriptor(AssociatedEndpoint.class);
                    returnCode = Opcodes.ARETURN;
                    break;
                case MT_FREEZE_SCALAR:
                    mv.visitVarInsn(Opcodes.ALOAD, 2);
                    methodName = "freezeScalar";
                    methodDesc = "(I" + ASM.getDescriptor(FrozenContext.class) + ")V";
                    returnCode = Opcodes.RETURN;
                    break;
                case MT_UNFREEZE_SCALAR:
                    mv.visitVarInsn(Opcodes.ALOAD, 2);
                    methodName = "unfreezeScalar";
                    methodDesc = "(I" + ASM.getDescriptor(FrozenContext.class) + ")V";
                    returnCode = Opcodes.RETURN;
                    break;
                case MT_IS_DISABLED:
                    methodName = "isDisabled";
                    methodDesc = "(I)Z";
                    returnCode = Opcodes.IRETURN;
                    break;
                case MT_DISABLE:
                    methodName = "disable";
                    methodDesc = "(I)V";
                    returnCode = Opcodes.RETURN;
                    break;
                case MT_ENABLE:
                    methodName = "enable";
                    methodDesc = "(I)V";
                    returnCode = Opcodes.RETURN;
                    break;
                case MT_IS_UNLOADED:
                    methodName = "isUnloaded";
                    methodDesc = "(I)Z";
                    returnCode = Opcodes.IRETURN;
                    break;
                case MT_UNLOAD:
                    methodName = "unload";
                    methodDesc = "(I)V";
                    returnCode = Opcodes.RETURN;
                    break;
                default:
                    throw new AssertionError();
                }
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        ASM.getInternalName(ObjectModel.class), 
                        methodName, 
                        methodDesc,
                        true);
                mv.visitInsn(returnCode);
                mv.visitLabel(isDeclaredFieldLabel);
            }
            
            //Declared properties
            mv.visitVarInsn(Opcodes.ILOAD, 1);
            mv.visitLdcInsn(this.objectModelMetadata.getDeclaredPropertyBaseId());
            mv.visitInsn(Opcodes.ISUB);
            mv.visitVarInsn(Opcodes.ISTORE, 1);
        }
        
        private void generateAddScalarListenerOrRemoveScalarListener(ClassVisitor cv, boolean add) {
            
            String scalarListenerInternalName = ASM.getInternalName(ScalarListener.class);
            String scalarListenerDescriptor = ASM.getDescriptor(ScalarListener.class);
            String methodName = add ? "addScalarListener" : "removeScalarListener";
            
            XMethodVisitor mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PUBLIC, 
                    methodName, 
                    '(' + scalarListenerDescriptor + ")V", 
                    null,
                    null);
            mv.visitCode();
            
            if (this.objectModelMetadata.getSuperMetadata() != null) {
                this.generateGetRootObjectModelInsns(mv, 0);
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        ASM.getInternalName(ScalarModificationAware.class), 
                        methodName, 
                        '(' + scalarListenerDescriptor + ")V",
                        true);
            } else {
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(
                        Opcodes.GETSTATIC, 
                        this.internalName, 
                        OBJECT_MODEL_IMPL_SCALAR_LISTENER_COMBINER, 
                        ASM.getDescriptor(Combiner.class));
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        this.internalName, 
                        OBJECT_MODEL_IMPL_SCALAR_LISTENER, 
                        scalarListenerDescriptor);
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        ASM.getInternalName(Combiner.class), 
                        add ? "combine" : "remove",
                        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                        true);
                mv.visitTypeInsn(
                        Opcodes.CHECKCAST, 
                        scalarListenerInternalName);
                mv.visitFieldInsn(
                        Opcodes.PUTFIELD, 
                        this.internalName, 
                        OBJECT_MODEL_IMPL_SCALAR_LISTENER, 
                        scalarListenerDescriptor);
            }
            mv.visitInsn(Opcodes.RETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateExcuteScalarEvent(ClassVisitor cv, final boolean modifying) {
        /*   
         *  void executeModifying(ScalarEvent e) {
         *      ScalarListener scalarListener = this.{scalarListener};
         *      if (scalarListener == null) {
         *          return;
         *      }
         *      e
         *      .getAttributeContext(AttributeScope.LOCAL)
         *      .addAttribute({AK_SCALAR_LISTENER}, scalarListener);
         *      this.{duringFiring} = true;
         *      try {
         *          scalarListener.executeModified(e);
         *      } catch (Throwable ex) {
         *          throw new ModificationEventHandleException(false, e, ex);
         *      } finally {
         *          this.{duringFiring} = false;
         *      }
         *  }
         *  
         *  void executeModified(ScalarEvent e) {
         *      ScalarListener scalarListener =
         *          (ScalarListener)
         *          e
         *          .getAttributeContext(AttributeScope.LOCAL)
         *          .removeAttribute({AK_SCALAR_LISTENER});
         *      if (scalarListener == null) {
         *          return;
         *      }
         *      this.{duringFiring} = true;
         *      try {
         *          scalarListener.executeModified(e);
         *      } catch (Throwable ex) {
         *          throw new ModificationEventHandleException(true, e, ex);
         *      } finally {
         *          this.{duringFiring} = false;
         *      }
         *  }
         */
            final Label isNotNullLabel = new Label();
            
            XMethodVisitor mv = ASM.visitMethod(
                    cv, 
                    0,
                    modifying ? OBJECT_MODEL_IMPL_EXECUTE_MODIFYING : OBJECT_MODEL_IMPL_EXECUTE_MODIFIED, 
                    '(' +
                    ASM.getDescriptor(ScalarEvent.class) +
                    ")V", 
                    null,
                    null);
            mv.visitCode();
            
            if (modifying) {
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        this.internalName, 
                        OBJECT_MODEL_IMPL_INTERNAL_GET_SCALAR_LISTENER, 
                        "()" + ASM.getDescriptor(ScalarListener.class),
                        false);
            } else {
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitFieldInsn(
                        Opcodes.GETSTATIC, 
                        ASM.getInternalName(AttributeScope.class), 
                        AttributeScope.LOCAL.name(), 
                        ASM.getDescriptor(AttributeScope.class));
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        ASM.getInternalName(ModificationEvent.class), 
                        "getAttributeContext", 
                        '(' +
                        ASM.getDescriptor(AttributeScope.class) +
                        ')' +
                        ASM.getDescriptor(EventAttributeContext.class),
                        false);
                mv.visitFieldInsn(
                        Opcodes.GETSTATIC, 
                        this.internalName,
                        OBJECT_MODEL_IMPL_AK_SCALAR_LISTENER,
                        "Ljava/lang/Object;");
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        ASM.getInternalName(AttributeContext.class), 
                        "removeAttribute", 
                        "(Ljava/lang/Object;)Ljava/lang/Object;",
                        false);
                mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(ScalarListener.class));
            }
            final int scalarListenerIndex = mv.aSlot("scalarListener");
            mv.visitVarInsn(Opcodes.ASTORE, scalarListenerIndex);
            mv.visitVarInsn(Opcodes.ALOAD, scalarListenerIndex);
            mv.visitJumpInsn(Opcodes.IFNONNULL, isNotNullLabel);
            mv.visitInsn(Opcodes.RETURN);
            
            mv.visitLabel(isNotNullLabel);
            if (modifying) {
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitFieldInsn(
                        Opcodes.GETSTATIC, 
                        ASM.getInternalName(AttributeScope.class), 
                        AttributeScope.LOCAL.name(), 
                        ASM.getDescriptor(AttributeScope.class));
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        ASM.getInternalName(ModificationEvent.class), 
                        "getAttributeContext", 
                        '(' +
                        ASM.getDescriptor(AttributeScope.class) +
                        ')' +
                        ASM.getDescriptor(EventAttributeContext.class),
                        false);
                mv.visitFieldInsn(
                        Opcodes.GETSTATIC, 
                        this.internalName,
                        OBJECT_MODEL_IMPL_AK_SCALAR_LISTENER,
                        "Ljava/lang/Object;");
                mv.visitVarInsn(Opcodes.ALOAD, scalarListenerIndex);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        ASM.getInternalName(AttributeContext.class), 
                        "addAttribute", 
                        "(Ljava/lang/Object;Ljava/lang/Object;)V",
                        false);
            }
            
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    this.internalName,
                    OBJECT_MODEL_IMPL_INTERNAL_SET_DURING_FIRING,
                    "(Z)V",
                    false);
            
            Action<XMethodVisitor> tryAction = new Action<XMethodVisitor>() {
                @Override
                public void run(XMethodVisitor mv) {
                    
                    mv.visitVarInsn(Opcodes.ALOAD, scalarListenerIndex);
                    mv.visitVarInsn(Opcodes.ALOAD, 1);
                    mv.visitMethodInsn(
                            Opcodes.INVOKEINTERFACE, 
                            ASM.getInternalName(ScalarListener.class), 
                            modifying ? "modifying" : "modified", 
                            '(' + ASM.getDescriptor(ScalarEvent.class) + ")V",
                            true);
                }
            };
            final Action<XMethodVisitor> finallyAction = new Action<XMethodVisitor>() {
                @Override
                public void run(XMethodVisitor mv) {
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitInsn(Opcodes.ICONST_0);
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL, 
                            DefaultObjectModelImplGenerator.this.internalName,
                            OBJECT_MODEL_IMPL_INTERNAL_SET_DURING_FIRING,
                            "(Z)V",
                            false);
                }       
            };
            
            mv.visitTryCatchBlock(
                    tryAction, 
                    new Catch(new Action<XMethodVisitor>() {
                        @Override
                        public void run(XMethodVisitor mv) {
                            int exIndex = mv.aSlot("ex");
                            mv.visitVarInsn(Opcodes.ASTORE, exIndex);
                            finallyAction.run(mv);
                            mv.visitTypeInsn(Opcodes.NEW, ASM.getInternalName(ModificationEventHandleException.class));
                            mv.visitInsn(Opcodes.DUP);
                            mv.visitInsn(modifying ? Opcodes.ICONST_0 : Opcodes.ICONST_1);
                            mv.visitVarInsn(Opcodes.ALOAD, 1);
                            mv.visitVarInsn(Opcodes.ALOAD, exIndex);
                            mv.visitMethodInsn(
                                    Opcodes.INVOKESPECIAL, 
                                    ASM.getInternalName(ModificationEventHandleException.class), 
                                    "<init>", 
                                    "(Z" +
                                    ASM.getDescriptor(ModificationEvent.class) +
                                    ASM.getDescriptor(Throwable.class) +
                                    ")V",
                                    false);
                            mv.visitInsn(Opcodes.ATHROW);
                        }
                    }));
            
            finallyAction.run(mv);
            mv.visitInsn(Opcodes.RETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateGetObjectModelFactory(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "getObjectModelFactory", 
                    "()" + ASM.getDescriptor(ObjectModelFactory.class), 
                    null,
                    null);
            mv.visitCode();
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    this.internalName, 
                    OBJECT_MODEL_IMPL_FACTORY, 
                    ASM.getDescriptor(ObjectModelFactory.class));
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateInernalGetObjectModelFactory(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, 
                    OBJECT_MODEL_IMPL_INTERNAL_GET_OBJECT_FACTORY, 
                    "()" + ASM.getDescriptor(ObjectModelFactory.class), 
                    null,
                    null);
            mv.visitCode();
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    this.internalName, 
                    OBJECT_MODEL_IMPL_FACTORY, 
                    ASM.getDescriptor(ObjectModelFactory.class));
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateInternalGetDuringFiring(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    OBJECT_MODEL_IMPL_INTERNAL_GET_DURING_FIRING, 
                    "()Z", 
                    null,
                    null);
            mv.visitCode();
            
            if (this.objectModelMetadata.getSuperMetadata() != null) {
                this.generateGetRootObjectModelInsns(mv, 0);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        this.getRootObjectModelImplInternalName(), 
                        OBJECT_MODEL_IMPL_INTERNAL_GET_DURING_FIRING, 
                        "()Z",
                        false);
            } else {
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        this.internalName, 
                        OBJECT_MODEL_IMPL_DURING_FIRING, 
                        "Z");
            }
            mv.visitInsn(Opcodes.IRETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateInternalSetDuringFiring(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    OBJECT_MODEL_IMPL_INTERNAL_SET_DURING_FIRING, 
                    "(Z)V", 
                    null,
                    null);
            mv.visitCode();
            
            if (this.objectModelMetadata.getSuperMetadata() != null) {
                this.generateGetRootObjectModelInsns(mv, 0);
                mv.visitVarInsn(Opcodes.ILOAD, 1);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        this.getRootObjectModelImplInternalName(), 
                        OBJECT_MODEL_IMPL_INTERNAL_SET_DURING_FIRING, 
                        "(Z)V",
                        false);
            } else {
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitVarInsn(Opcodes.ILOAD, 1);
                mv.visitFieldInsn(
                        Opcodes.PUTFIELD, 
                        this.internalName, 
                        OBJECT_MODEL_IMPL_DURING_FIRING, 
                        "Z");
            }
            mv.visitInsn(Opcodes.RETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateInternalGetScalarListener(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    OBJECT_MODEL_IMPL_INTERNAL_GET_SCALAR_LISTENER, 
                    "()" + ASM.getDescriptor(ScalarListener.class), 
                    null,
                    null);
            mv.visitCode();
            
            if (this.objectModelMetadata.getSuperMetadata() != null) {
                this.generateGetRootObjectModelInsns(mv, 0);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        this.getRootObjectModelImplInternalName(), 
                        OBJECT_MODEL_IMPL_INTERNAL_GET_SCALAR_LISTENER, 
                        "()" + ASM.getDescriptor(ScalarListener.class),
                        false);
            } else {
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        this.internalName, 
                        OBJECT_MODEL_IMPL_SCALAR_LISTENER, 
                        ASM.getDescriptor(ScalarListener.class));
            }
            mv.visitInsn(Opcodes.ARETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateInternalBatchSetLoading(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, 
                    OBJECT_MODEL_IMPL_INTERNAL_BATCH_SET_LOADING, 
                    "(Ljava/util/Collection;Z)V", 
                    null,
                    null);
            mv.visitCode();
            
            final int objectModelItrIndex = 2;
            
            final Label beginLoopIndex = new Label();
            final Label endLoopIndex = new Label();
            
            /*
             * Iterator<__THIS_TYPE__> objectModelItr = objectModels.iterator();
             */
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    "java/util/Collection", 
                    "iterator", 
                    "()Ljava/util/Iterator;", 
                    true);
            mv.visitVarInsn(Opcodes.ASTORE, objectModelItrIndex);
            
            /*
             * while (objectItr.hasNext()) {
             *      objectItr.next().{internalSetLoading}(loading);
             * }
             */
            mv.visitLabel(beginLoopIndex);
            mv.visitVarInsn(Opcodes.ALOAD, objectModelItrIndex);
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    "java/util/Iterator", 
                    "hasNext", 
                    "()Z", 
                    true);
            mv.visitJumpInsn(Opcodes.IFEQ, endLoopIndex);
            mv.visitVarInsn(Opcodes.ALOAD, objectModelItrIndex);
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    "java/util/Iterator", 
                    "next", 
                    "()Ljava/lang/Object;", 
                    true);
            mv.visitTypeInsn(Opcodes.CHECKCAST, this.internalName);
            mv.visitVarInsn(Opcodes.ILOAD, 1);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    this.internalName, 
                    OBJECT_MODEL_IMPL_INTERNAL_SET_LOADING, 
                    "(Z)V", 
                    false);
            mv.visitJumpInsn(Opcodes.GOTO, beginLoopIndex);
            mv.visitLabel(endLoopIndex);
            
            mv.visitInsn(Opcodes.RETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateInternalSetLoading(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    OBJECT_MODEL_IMPL_INTERNAL_SET_LOADING, 
                    "(Z)V", 
                    null,
                    null);
            mv.visitCode();
            
            if (this.objectModelMetadata.getSuperMetadata() != null) {
                this.generateGetRootObjectModelInsns(mv, 0);
                mv.visitVarInsn(Opcodes.ILOAD, 1);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        this.getRootObjectModelImplInternalName(), 
                        OBJECT_MODEL_IMPL_INTERNAL_SET_LOADING, 
                        "(Z)V",
                        false);
            } else {
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitVarInsn(Opcodes.ILOAD, 1);
                mv.visitFieldInsn(
                        Opcodes.PUTFIELD, 
                        this.internalName, 
                        OBJECT_MODEL_IMPL_LOADING, 
                        "Z");
            }
            mv.visitInsn(Opcodes.RETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateInternalInterceptor(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PRIVATE, 
                    OBJECT_MODEL_IMPL_INTERNAL_LOAD, 
                    "(I)V", 
                    null,
                    null);
            mv.visitCode();
            
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ILOAD, 1);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    this.internalName,
                    "isUnloaded",
                    "(I)Z",
                    false);
            Label isUnloadedLabel = new Label();
            mv.visitJumpInsn(Opcodes.IFNE, isUnloadedLabel);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitLabel(isUnloadedLabel);
            
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    this.internalName, 
                    OBJECT_MODEL_IMPL_SCALAR_PROPERTY_ID_ARRAY, 
                    "[I");
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    this.internalName, 
                    "load", 
                    "([I)V",
                    false);
            mv.visitInsn(Opcodes.RETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateGetObjectModelMetadata(ClassVisitor cv) {
            XMethodVisitor mv;
            mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PUBLIC, 
                    "getObjectModelMetadata", 
                    "()" + ASM.getDescriptor(ObjectModelMetadata.class), 
                    null,
                    null);
            mv.visitCode();
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    this.internalName, 
                    OBJECT_MODEL_IMPL_OBJECT_MODEL_METADATA,
                    ASM.getDescriptor(ObjectModelMetadata.class));
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    
        private void generateReadPropertiesOrWriteProperties(ClassVisitor cv, boolean write) {
            String streamInternalName = 
                    write ? 
                    ASM.getInternalName(ObjectOutputStream.class) : 
                    ASM.getInternalName(ObjectInputStream.class);
            String methodName = write ? "writeProperties" : "readProperties";
            String methodDesc = '(' + ASM.getDescriptor(write ? ObjectOutputStream.class : ObjectInputStream.class) + ")V";
            XMethodVisitor mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PUBLIC, 
                    methodName, 
                    methodDesc, 
                    null,
                    write ? 
                    new String[] { ASM.getInternalName(IOException.class) } :
                    new String[] { 
                        ASM.getInternalName(IOException.class), 
                        ASM.getInternalName(ClassNotFoundException.class) 
                    });
            mv.visitCode();
            
            ObjectModelMetadata superMetadata = objectModelMetadata.getSuperMetadata();
            if (superMetadata != null) {
                this.generateGetSuperObjectModelInsns(mv);
                mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(ObjectModelIO.class));
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        ASM.getInternalName(ObjectModelIO.class), 
                        methodName,
                        methodDesc,
                        true);
            }
            
            for (Property property : this.objectModelMetadata.getDeclaredProperties().values()) {
                if (!isContravarianceAssociationProperty(property)) {
                    if (isStateRequired(property)) {
                        if (write) {
                            mv.visitVarInsn(Opcodes.ALOAD, 1);
                            mv.visitVarInsn(Opcodes.ALOAD, 0);
                            mv.visitFieldInsn(
                                    Opcodes.GETFIELD, 
                                    this.internalName, 
                                    property.getName() + OBJECT_MODEL_IMPL_STATE_POSTFIX,
                                    "I");
                            mv.visitMethodInsn(
                                    Opcodes.INVOKEVIRTUAL, 
                                    streamInternalName, 
                                    "writeInt", 
                                    "(I)V",
                                    false);
                        } else {
                            mv.visitVarInsn(Opcodes.ALOAD, 0);
                            mv.visitVarInsn(Opcodes.ALOAD, 1);
                            mv.visitMethodInsn(
                                    Opcodes.INVOKEVIRTUAL, 
                                    streamInternalName, 
                                    "readInt", 
                                    "()I",
                                    false);
                            mv.visitFieldInsn(
                                    Opcodes.PUTFIELD, 
                                    this.internalName, 
                                    property.getName() + OBJECT_MODEL_IMPL_STATE_POSTFIX,
                                    "I");
                        }
                    }
                }
                Class<?> clazz = property.getReturnClass();
                String opName;
                String opDesc;
                if (property instanceof AssociationProperty) {
                    if (((AssociationProperty)property).getCovarianceProperty() == null) {
                        mv.visitVarInsn(Opcodes.ALOAD, 1);
                        mv.visitVarInsn(Opcodes.ALOAD, 0);
                        mv.visitFieldInsn(
                                Opcodes.GETFIELD, 
                                this.internalName, 
                                property.getName(), 
                                ASM.getDescriptor(clazz));
                        mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(AssociatedEndpointIO.class));
                        opName = write ? "write" : "read";
                        opDesc = write ? "(Ljava/io/ObjectOutputStream;)V" : "(Ljava/io/ObjectInputStream;)V";
                        mv.visitVarInsn(Opcodes.ALOAD, 1);
                        mv.visitMethodInsn(
                                Opcodes.INVOKEINTERFACE,
                                ASM.getInternalName(AssociatedEndpointIO.class),
                                opName,
                                opDesc,
                                true);
                    }
                } else {
                    if (clazz.isPrimitive()) {
                        opName = clazz.getName();
                        opName = Character.toUpperCase(opName.charAt(0)) + opName.substring(1);
                        opName = write ? "write" + opName : "read" + opName;
                        opDesc = ASM.getDescriptor(clazz);
                        opDesc = write ? '(' + opDesc + ")V" : "()" + opDesc;
                    } else {
                        opName = write ? "writeObject" : "readObject";
                        opDesc = write ? "(Ljava/lang/Object;)V" : "()Ljava/lang/Object;";
                    }
                    if (write) {
                        mv.visitVarInsn(Opcodes.ALOAD, 1);
                        mv.visitVarInsn(Opcodes.ALOAD, 0);
                        mv.visitFieldInsn(
                                Opcodes.GETFIELD, 
                                this.internalName, 
                                property.getName(), 
                                ASM.getDescriptor(clazz));
                        mv.visitMethodInsn(
                                Opcodes.INVOKEVIRTUAL, 
                                streamInternalName, 
                                opName, 
                                opDesc,
                                false);
                    } else {
                        mv.visitVarInsn(Opcodes.ALOAD, 0);
                        mv.visitVarInsn(Opcodes.ALOAD, 1);
                        mv.visitMethodInsn(
                                Opcodes.INVOKEVIRTUAL, 
                                streamInternalName, 
                                opName, 
                                opDesc,
                                false);
                        if (!clazz.isPrimitive() && clazz != Object.class) {
                            mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(clazz));
                        }
                        mv.visitFieldInsn(
                                Opcodes.PUTFIELD, 
                                this.internalName, 
                                property.getName(), 
                                ASM.getDescriptor(clazz));
                    }
                }
            }
            
            mv.visitInsn(Opcodes.RETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateAppendTo(ClassVisitor cv) {
            XMethodVisitor mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PUBLIC, 
                    "appendTo", 
                    '(' + ASM.getDescriptor(AppendingContext.class) + ")V", 
                    null, 
                    new String[] { ASM.getInternalName(IOException.class) });
            mv.visitCode();
            
            Label needAppendLabel = new Label();
            
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    ASM.getInternalName(AppendingContext.class), 
                    "enter", 
                    '(' + ASM.getDescriptor(ObjectModel.class) + ")Z",
                    false);
            mv.visitJumpInsn(Opcodes.IFNE, needAppendLabel);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitLabel(needAppendLabel);
            
            final int stateIndex = mv.iSlot("state");
            final int embeddedIndex = mv.iSlot("embeddedValue");
            
            Action<MethodVisitor> tryAction = new Action<MethodVisitor>() {
                @Override
                public void run(MethodVisitor mv) {
                    
                    DefaultObjectModelImplGenerator that = DefaultObjectModelImplGenerator.this;
                    
                    mv.visitVarInsn(Opcodes.ALOAD, 1);
                    mv.visitLdcInsn("@ownerClass");
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL, 
                            ASM.getInternalName(AppendingContext.class), 
                            "appendPropertyName", 
                            "(Ljava/lang/String;)V",
                            false);
                    mv.visitVarInsn(Opcodes.ALOAD, 1);
                    mv.visitLdcInsn(that.objectModelMetadata.getOwnerClass().getName());
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL, 
                            ASM.getInternalName(AppendingContext.class), 
                            "appendValue", 
                            "(Ljava/lang/String;)V",
                            false);
                    
                    mv.visitVarInsn(Opcodes.ALOAD, 1);
                    mv.visitLdcInsn("@referenceId");
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL, 
                            ASM.getInternalName(AppendingContext.class), 
                            "appendPropertyName", 
                            "(Ljava/lang/String;)V",
                            false);
                    mv.visitVarInsn(Opcodes.ALOAD, 1);
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL, 
                            ASM.getInternalName(AppendingContext.class), 
                            "appendReferenceId", 
                            "()V",
                            false);
                    
                    for (ScalarProperty scalarProperty : that.objectModelMetadata.getDeclaredScalarProperties().values()) {
                        
                        Label nextLabel = new Label();
                        
                        mv.visitVarInsn(Opcodes.ALOAD, 1);
                        mv.visitLdcInsn(scalarProperty.getName());
                        mv.visitMethodInsn(
                                Opcodes.INVOKEVIRTUAL, 
                                ASM.getInternalName(AppendingContext.class), 
                                "appendPropertyName", 
                                "(Ljava/lang/String;)V",
                                false);
                        
                        if (isStateRequired(scalarProperty)) {
                            mv.visitVarInsn(Opcodes.ALOAD, 0);
                            mv.visitFieldInsn(
                                    Opcodes.GETFIELD, 
                                    that.internalName, 
                                    scalarProperty.getName() + OBJECT_MODEL_IMPL_STATE_POSTFIX, 
                                    "I");
                            mv.visitVarInsn(Opcodes.ISTORE, stateIndex);
                            
                            Label isNotDisabledLabel = new Label();
                            mv.visitVarInsn(Opcodes.ILOAD, stateIndex);
                            mv.visitInsn(OPCODE_CONST_DISABLED);
                            mv.visitInsn(Opcodes.IAND);
                            mv.visitJumpInsn(Opcodes.IFEQ, isNotDisabledLabel);
                            mv.visitVarInsn(Opcodes.ALOAD, 1);
                            mv.visitMethodInsn(
                                    Opcodes.INVOKEVIRTUAL, 
                                    ASM.getInternalName(AppendingContext.class), 
                                    "appendDisabledValue", 
                                    "()V",
                                    false);
                            mv.visitJumpInsn(Opcodes.GOTO, nextLabel);
                            mv.visitLabel(isNotDisabledLabel);
                            
                            Label isNotUnloadedLabel = new Label();
                            mv.visitVarInsn(Opcodes.ILOAD, stateIndex);
                            mv.visitInsn(OPCODE_CONST_UNLOADED);
                            mv.visitInsn(Opcodes.IAND);
                            mv.visitJumpInsn(Opcodes.IFEQ, isNotUnloadedLabel);
                            mv.visitVarInsn(Opcodes.ALOAD, 1);
                            mv.visitMethodInsn(
                                    Opcodes.INVOKEVIRTUAL, 
                                    ASM.getInternalName(AppendingContext.class), 
                                    "appendUnloadedValue", 
                                    "()V",
                                    false);
                            mv.visitJumpInsn(Opcodes.GOTO, nextLabel);
                            mv.visitLabel(isNotUnloadedLabel);
                        }
                        
                        ObjectModelMetadata returnObjectModelMetadata = scalarProperty.getReturnObjectModelMetadata();
                        Class<?> type = scalarProperty.getReturnClass();
                        if (returnObjectModelMetadata != null) {
                            Label isNotNullLabel = new Label();
                            mv.visitVarInsn(Opcodes.ALOAD, 0);
                            mv.visitFieldInsn(
                                    Opcodes.GETFIELD, 
                                    that.internalName, 
                                    scalarProperty.getName(), 
                                    ASM.getDescriptor(type));
                            mv.visitVarInsn(Opcodes.ASTORE, embeddedIndex);
                            mv.visitVarInsn(Opcodes.ALOAD, embeddedIndex);
                            mv.visitJumpInsn(Opcodes.IFNONNULL, isNotNullLabel);
                            mv.visitVarInsn(Opcodes.ALOAD, 1);
                            mv.visitMethodInsn(
                                    Opcodes.INVOKEVIRTUAL, 
                                    ASM.getInternalName(AppendingContext.class), 
                                    "appendNullValue", 
                                    "()V",
                                    false);
                            mv.visitJumpInsn(Opcodes.GOTO, nextLabel);
                            mv.visitLabel(isNotNullLabel);
                            visitGetObjectModel(mv, returnObjectModelMetadata, embeddedIndex);
                            mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(ObjectModelAppender.class));
                            mv.visitVarInsn(Opcodes.ALOAD, 1);
                            mv.visitMethodInsn(
                                    Opcodes.INVOKEINTERFACE, 
                                    ASM.getInternalName(ObjectModelAppender.class), 
                                    "appendTo", 
                                    '(' + ASM.getDescriptor(AppendingContext.class) + ")V",
                                    true);
                        } else {
                            mv.visitVarInsn(Opcodes.ALOAD, 1);
                            mv.visitVarInsn(Opcodes.ALOAD, 0);
                            mv.visitFieldInsn(
                                    Opcodes.GETFIELD, 
                                    that.internalName, 
                                    scalarProperty.getName(), 
                                    ASM.getDescriptor(type));
                            if (!IMMEDIATE_APPENDABLE_SCALAR_TYPES.contains(type)) {
                                if (type.isEnum()) {
                                    type = Enum.class;
                                } else {
                                    type = Object.class;
                                }
                            }
                            mv.visitMethodInsn(
                                    Opcodes.INVOKEVIRTUAL, 
                                    ASM.getInternalName(AppendingContext.class), 
                                    "appendValue", 
                                    '(' + ASM.getDescriptor(type) + ")V",
                                    false);
                        }
                        
                        mv.visitLabel(nextLabel);
                    }
                    
                    for (AssociationProperty associationProperty : that.objectModelMetadata.getDeclaredAssociationProperties().values()) {
                        if (associationProperty.getCovarianceProperty() != null) {
                            continue;
                        }
                        mv.visitVarInsn(Opcodes.ALOAD, 1);
                        mv.visitLdcInsn(associationProperty.getName());
                        mv.visitMethodInsn(
                                Opcodes.INVOKEVIRTUAL, 
                                ASM.getInternalName(AppendingContext.class), 
                                "appendPropertyName", 
                                "(Ljava/lang/String;)V",
                                false);
                        Class<?> type;
                        if (Map.class.isAssignableFrom(associationProperty.getReturnClass())) {
                            type = Map.class;
                        } else if (Collection.class.isAssignableFrom(associationProperty.getReturnClass())) {
                            type = Collection.class;
                        } else {
                            type = Reference.class;
                        }
                        mv.visitVarInsn(Opcodes.ALOAD, 1);
                        mv.visitVarInsn(Opcodes.ALOAD, 0);
                        mv.visitFieldInsn(
                                Opcodes.GETFIELD, 
                                that.internalName, 
                                associationProperty.getName(), 
                                ASM.getDescriptor(associationProperty.getReturnClass()));
                        mv.visitMethodInsn(
                                Opcodes.INVOKEVIRTUAL, 
                                ASM.getInternalName(AppendingContext.class), 
                                "appendValue", 
                                '(' + ASM.getDescriptor(type) + ")V",
                                false);
                    }
                    
                    if (that.objectModelMetadata.getSuperMetadata() != null) {
                        mv.visitVarInsn(Opcodes.ALOAD, 1);
                        mv.visitLdcInsn("super");
                        mv.visitMethodInsn(
                                Opcodes.INVOKEVIRTUAL, 
                                ASM.getInternalName(AppendingContext.class), 
                                "appendPropertyName", 
                                "(Ljava/lang/String;)V",
                                false);
                        visitGetObjectModel(
                                mv, 
                                that.objectModelMetadata.getSuperMetadata(), 
                                new Action<MethodVisitor>() {
                            @Override
                            public void run(MethodVisitor mv) {
                                DefaultObjectModelImplGenerator that = DefaultObjectModelImplGenerator.this;
                                mv.visitVarInsn(Opcodes.ALOAD, 0);
                                mv.visitFieldInsn(
                                        Opcodes.GETFIELD, 
                                        that.internalName, 
                                        OWNER, 
                                        that.ownerDescriptor);
                            }
                        });
                        mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(ObjectModelAppender.class));
                        mv.visitVarInsn(Opcodes.ALOAD, 1);
                        mv.visitMethodInsn(
                                Opcodes.INVOKEINTERFACE, 
                                ASM.getInternalName(ObjectModelAppender.class), 
                                "appendTo", 
                                '(' + ASM.getDescriptor(AppendingContext.class) + ")V",
                                true);
                    }
                }
            };
            Action<MethodVisitor> finallyAction = new Action<MethodVisitor>() {
                @Override
                public void run(MethodVisitor mv) {
                    mv.visitVarInsn(Opcodes.ALOAD, 1);
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL, 
                            ASM.getInternalName(AppendingContext.class), 
                            "exit", 
                            '(' + ASM.getDescriptor(ObjectModel.class) + ")V",
                            false);
                    
                }
            };
            mv.visitTryFinally(tryAction, finallyAction);
            mv.visitInsn(Opcodes.RETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateInitAssociationPropertyInsns(
                MethodVisitor mv, AssociationProperty associationProperty) {
            
            AssociationProperty covarianceProperty = associationProperty.getCovarianceProperty();
            Class<?> returnClass = associationProperty.getReturnClass();
            String addValidatorMethodName = 
                    Map.class.isAssignableFrom(returnClass) ?
                    "addValueValidator" :
                    "addValidator";
            String addValidatorOwner;
            if (associationProperty.isReference()) {
                addValidatorOwner = ASM.getInternalName(Reference.class);
            } else if (Map.class.isAssignableFrom(returnClass)) {
                addValidatorOwner = ASM.getInternalName(XMap.class);
            } else {
                addValidatorOwner = ASM.getInternalName(XCollection.class);
            }
            if (covarianceProperty != null) {
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        this.internalName, 
                        associationProperty.getGetterName(), 
                        "()" + ASM.getDescriptor(returnClass),
                        false);
            } else {
                String associatedEndImplInternalName = 
                        associationEndpointImplClassName(associationProperty, false).replace('.', '/');
                String ownerClassDesc = ASM.getDescriptor(associationProperty.getDeclaringObjectModelMetadata().getOwnerClass());
                mv.visitTypeInsn(Opcodes.NEW, associatedEndImplInternalName);
                mv.visitInsn(Opcodes.DUP);
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        this.internalName, 
                        OWNER, 
                        ownerClassDesc);
                mv.visitMethodInsn(
                        Opcodes.INVOKESPECIAL, 
                        associatedEndImplInternalName, 
                        "<init>", 
                        '(' + ownerClassDesc + ")V",
                        false);
                mv.visitInsn(Opcodes.DUP);
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitInsn(Opcodes.SWAP);
                mv.visitFieldInsn(
                        Opcodes.PUTFIELD, 
                        this.internalName, 
                        associationProperty.getName(), 
                        ASM.getDescriptor(returnClass));
            }
            mv.visitLdcInsn(org.babyfish.org.objectweb.asm.Type.getType(associationProperty.getElementClass()));
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASM.getInternalName(Validators.class), 
                    "instanceOf", 
                    "(Ljava/lang/Class;)" + 
                    ASM.getDescriptor(Validator.class),
                    false);
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    addValidatorOwner, 
                    addValidatorMethodName, 
                    '(' + ASM.getDescriptor(Validator.class) + ")V",
                    true);
        }
        
        private void generateProperty(ClassVisitor cv, final Property property) {
            String propertyTypeDesc = ASM.getDescriptor(property.getReturnClass());
            if (!isContravarianceAssociationProperty(property)) {
                if (isStateRequired(property)) {
                    cv.visitField(
                            0, 
                            property.getName() + OBJECT_MODEL_IMPL_STATE_POSTFIX, 
                            "I", 
                            null,
                            null
                    ).visitEnd();
                }
            }
            if (property instanceof ScalarProperty) {
                cv.visitField(
                        0, 
                        property.getName(), 
                        propertyTypeDesc, 
                        null,
                        null
                ).visitEnd();
                cv.visitField(
                        Opcodes.ACC_TRANSIENT,
                        property.getName() + OBJECT_MODEL_IMPL_FROZEN_CONTEXT_POSTFIX, 
                        ASM.getDescriptor(FrozenContext.class),
                        null,
                        null
                ).visitEnd();
                XMethodVisitor mv = ASM.visitMethod(
                        cv,
                        Opcodes.ACC_PUBLIC, 
                        property.getGetterName(), 
                        "()" + propertyTypeDesc, 
                        null,
                        null);
                
                mv.visitCode();
                this.generateScalarGetterInsns(mv, (ScalarProperty)property);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
                
                mv = ASM.visitMethod(
                        cv,
                        Opcodes.ACC_PUBLIC, 
                        property.getSetterName(), 
                        '(' + propertyTypeDesc + ")V", 
                        null,
                        null);
                mv.visitCode();
                this.generateScalarSetterInsns(mv, (ScalarProperty)property);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            } else {
                AssociationProperty associationProperty = (AssociationProperty)property;
                AssociationProperty covarianceProperty = associationProperty.getCovarianceProperty();
                if (covarianceProperty != null) {
                    XMethodVisitor mv = ASM.visitMethod(
                            cv,  
                            Opcodes.ACC_PUBLIC, 
                            property.getGetterName(), 
                            "()" + propertyTypeDesc, 
                            null, 
                            null);
                    mv.visitCode();
                    visitGetObjectModel(mv, covarianceProperty.getDeclaringObjectModelMetadata(), new Action<MethodVisitor>() {
                        @Override
                        public void run(MethodVisitor mv) {
                            mv.visitVarInsn(Opcodes.ALOAD, 0);
                            mv.visitFieldInsn(
                                    Opcodes.GETFIELD, 
                                    DefaultObjectModelImplGenerator.this.internalName, 
                                    OWNER, 
                                    ASM.getDescriptor(property.getDeclaringObjectModelMetadata().getOwnerClass()));
                        }
                    });
                    mv.visitMethodInsn(
                            Opcodes.INVOKEINTERFACE, 
                            ASM.getInternalName(covarianceProperty.getDeclaringObjectModelMetadata().getObjectModelClass()), 
                            covarianceProperty.getGetterName(), 
                            "()" + ASM.getDescriptor(covarianceProperty.getReturnClass()),
                            true);
                    mv.visitInsn(Opcodes.ARETURN);
                    mv.visitMaxs(0, 0);
                    mv.visitEnd();
                } else {
                    cv.visitField(
                            Opcodes.ACC_PRIVATE, 
                            property.getName(), 
                            propertyTypeDesc, 
                            null,
                            null
                    ).visitEnd();
                    
                    XMethodVisitor mv = ASM.visitMethod(
                            cv,
                            Opcodes.ACC_PUBLIC, 
                            property.getGetterName(), 
                            "()" + propertyTypeDesc, 
                            null, 
                            null);
                    mv.visitCode();
                    
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitFieldInsn(
                            Opcodes.GETFIELD, 
                            this.internalName, 
                            property.getName(), 
                            propertyTypeDesc);
                    mv.visitInsn(Opcodes.ARETURN);
                    mv.visitMaxs(0, 0);
                    mv.visitEnd();
                }
            }
        }
        
        private void generateGetSuperObjectModelInsns(MethodVisitor mv) {
            this.generateGetSuperObjectModelInsns(mv, null);
        }
        
        private void generateGetSuperObjectModelInsns(MethodVisitor mv, Action<MethodVisitor> prepareObjectModelLambda) {
            visitGetObjectModelFactory(mv, this.objectModelMetadata.getSuperMetadata());
            if (prepareObjectModelLambda == null) {
                mv.visitVarInsn(Opcodes.ALOAD, 0);
            } else {
                prepareObjectModelLambda.run(mv);
            }
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.internalName,
                    OWNER,
                    this.ownerDescriptor);
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    ASM.getInternalName(ObjectModelFactory.class), 
                    "get", 
                    "(Ljava/lang/Object;)Ljava/lang/Object;",
                    true);
        }

        private void generateScalarGetterInsns(XMethodVisitor mv, ScalarProperty scalarProperty) {
            if (scalarProperty.isDeferrable()) {
                Label loadLabel = new Label();
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        this.internalName, 
                        "isLoading", 
                        "()Z",
                        false);
                mv.visitJumpInsn(Opcodes.IFNE, loadLabel);
                
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitLdcInsn(scalarProperty.getId());
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        this.internalName, 
                        OBJECT_MODEL_IMPL_INTERNAL_LOAD, 
                        "(I)V",
                        false);
                
                mv.visitLabel(loadLabel);
            }
            
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.internalName, 
                    scalarProperty.getName(), 
                    ASM.getDescriptor(scalarProperty.getReturnClass()));
            this.cloneScalar(scalarProperty, mv);
            mv.visitInsn(ASM.getReturnCode(scalarProperty.getReturnClass()));
        }

        @SuppressWarnings("unchecked")
        private void generateScalarSetterInsns(XMethodVisitor mv, final ScalarProperty scalarProperty) {
            
            if (this.cloneScalar(scalarProperty, null)) {
                mv.visitVarInsn(ASM.getLoadCode(scalarProperty.getReturnClass()), 1);
                this.cloneScalar(scalarProperty, mv);
                mv.visitVarInsn(ASM.getStoreCode(scalarProperty.getReturnClass()), 1);
            }
            
            if (scalarProperty.isDeferrable()) {
                
                Label normalSettingLabel = new Label();
                
                /*
                 * Do simple assignment when the current object model is loading.
                 */
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        this.internalName, 
                        "isLoading", 
                        "()Z",
                        false);
                mv.visitJumpInsn(Opcodes.IFEQ, normalSettingLabel);
                this.generateScalarRawSetterInsns(mv, scalarProperty, false);
                mv.visitInsn(Opcodes.RETURN);
                
                /*
                 * Do simple assignment when the current object property is unloaded.
                 * Of course, we can choose to load them at first, but that
                 * may reduce the performance.
                 * So I choose the special optimization, just do simple assignment
                 * when the current property is unloaded, it does not matter that no
                 * event is raised when assign a value to an unloaded scalar property.
                 */
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        this.internalName, 
                        scalarProperty.getName() + OBJECT_MODEL_IMPL_STATE_POSTFIX, 
                        "I");
                mv.visitInsn(OPCODE_CONST_UNLOADED);
                mv.visitInsn(Opcodes.IAND);
                mv.visitJumpInsn(Opcodes.IFEQ, normalSettingLabel);
                this.generateScalarRawSetterInsns(mv, scalarProperty, true);
                mv.visitInsn(Opcodes.RETURN);
                
                mv.visitLabel(normalSettingLabel);
            }
            
            final Label notDuringFireingLabel = new Label();
            final Label notEqualLabel = new Label();
            
            /*
             * if (this.[duringFire]) {
             *      throw new IllegalStateException(
             *          "The object model is fireing scalar event, can not modify its scalar property.");
             * }
             */
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    this.internalName, 
                    OBJECT_MODEL_IMPL_INTERNAL_GET_DURING_FIRING, 
                    "()Z",
                    false);
            mv.visitJumpInsn(Opcodes.IFEQ, notDuringFireingLabel);
            mv.visitTypeInsn(Opcodes.NEW, ASM.getInternalName(IllegalStateException.class));
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn("The object model is fireing scalar event, can not modify its scalar property.");
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    ASM.getInternalName(IllegalStateException.class), 
                    "<init>", 
                    "(Ljava/lang/String;)V",
                    false);
            mv.visitInsn(Opcodes.ATHROW);
            mv.visitLabel(notDuringFireingLabel);
            
            /*
             * if (x == null ? this.x == null : x.equals(this.x)) {
             *      this.?{state = 0} = 0;
             *      return;
             * }
             */
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.internalName, 
                    scalarProperty.getName(), 
                    ASM.getDescriptor(scalarProperty.getReturnClass()));
            mv.visitVarInsn(
                    ASM.getLoadCode(scalarProperty.getReturnClass()), 
                    1);
            mv.visitEquals(scalarProperty.getReturnClass(), true);
            mv.visitJumpInsn(Opcodes.IFEQ, notEqualLabel);
            this.generateScalarStateCleanInsn(mv, scalarProperty);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitLabel(notEqualLabel);
            
            /*
             * FrozenContext<?> ctx = this.?frozenContext;
             * ScalarEvent event = ScalarEvent.createReplaceEvent(
             *      this,
             *      ObjectModelModifications.setScalar(ldc:propertyId, ?),
             *      this.?,
             *      ?);
             * Throwable finalException = null;
             */
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.internalName, 
                    scalarProperty.getName() + OBJECT_MODEL_IMPL_FROZEN_CONTEXT_POSTFIX, 
                    ASM.getDescriptor(FrozenContext.class));
            final int ctxIndex = mv.aSlot("ctx");
            mv.visitVarInsn(Opcodes.ASTORE, ctxIndex);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitLdcInsn(scalarProperty.getId());
            mv.visitBox(
                    scalarProperty.getReturnClass(), 
                    new Action<XMethodVisitor>() {
                        @Override
                        public void run(XMethodVisitor mv) {
                            mv.visitVarInsn(ASM.getLoadCode(scalarProperty.getReturnClass()), 1);
                        }
                    });
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASM.getInternalName(ObjectModelModifications.class), 
                    "setScalar", 
                    "(ILjava/lang/Object;)" + ASM.getDescriptor(SetScalarModification.class),
                    false);
            mv.visitBox(
                    scalarProperty.getReturnClass(), 
                    new Action<XMethodVisitor>() {
                        @Override
                        public void run(XMethodVisitor mv) {
                            mv.visitVarInsn(Opcodes.ALOAD, 0);
                            mv.visitFieldInsn(
                                    Opcodes.GETFIELD, 
                                    DefaultObjectModelImplGenerator.this.internalName, 
                                    scalarProperty.getName(), 
                                    ASM.getDescriptor(scalarProperty.getReturnClass()));
                        }
                    });
            mv.visitBox(
                    scalarProperty.getReturnClass(), 
                    new Action<XMethodVisitor>() {
                        @Override
                        public void run(XMethodVisitor mv) {
                            mv.visitVarInsn(ASM.getLoadCode(scalarProperty.getReturnClass()), 1);
                        }
                    });
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASM.getInternalName(ScalarEvent.class), 
                    "createReplaceEvent", 
                    '(' +
                    ASM.getDescriptor(ObjectModel.class) +
                    ASM.getDescriptor(SetScalarModification.class) +
                    "Ljava/lang/Object;Ljava/lang/Object;)" +
                    ASM.getDescriptor(ScalarEvent.class),
                    false);
            final int eventIndex = mv.aSlot("event");
            mv.visitVarInsn(Opcodes.ASTORE, eventIndex);
            final int finalExceptionIndex = mv.aSlot("finalException");
            mv.visitInsn(Opcodes.ACONST_NULL);
            mv.visitVarInsn(Opcodes.ASTORE, finalExceptionIndex);
            
            /*
             * try {
             *      this.{executeModifying}(event);
             * } catch (RuntimeException | Error ex) {
             *      if (finalException == null) {
             *          finalException = ex;
             *      }
             *      ((InAllChainAttributeContext)event.getAttributeContext(AttributeScope.IN_ALL_CHAIN))
             *      .setPreThrowable(ex);
             * }
             */
            mv.visitTryCatchBlock(
                    new Action<XMethodVisitor>() {
                        @Override
                        public void run(XMethodVisitor mv) {
                            mv.visitVarInsn(Opcodes.ALOAD, 0);
                            mv.visitVarInsn(Opcodes.ALOAD, eventIndex);
                            mv.visitMethodInsn(
                                Opcodes.INVOKEVIRTUAL, 
                                DefaultObjectModelImplGenerator.this.internalName, 
                                OBJECT_MODEL_IMPL_EXECUTE_MODIFYING, 
                                '(' + ASM.getDescriptor(ScalarEvent.class) + ")V",
                                false);
                        }
                    }, 
                    new Catch(
                            new Class[] { RuntimeException.class, Error.class },
                            new Action<XMethodVisitor>() {
                                @Override
                                public void run(XMethodVisitor mv) {
                                    int exIndex = mv.aSlot("ex");
                                    mv.visitVarInsn(Opcodes.ASTORE, exIndex);
                                    mv.visitVarInsn(Opcodes.ALOAD, exIndex);
                                    mv.visitAStoreInsnIfNull(finalExceptionIndex);
                                    mv.visitVarInsn(Opcodes.ALOAD, eventIndex);
                                    mv.visitFieldInsn(
                                            Opcodes.GETSTATIC, 
                                            ASM.getInternalName(AttributeScope.class), 
                                            AttributeScope.IN_ALL_CHAIN.name(), 
                                            ASM.getDescriptor(AttributeScope.class));
                                    mv.visitMethodInsn(
                                            Opcodes.INVOKEVIRTUAL, 
                                            ASM.getInternalName(ModificationEvent.class), 
                                            "getAttributeContext", 
                                            '(' + 
                                            ASM.getDescriptor(AttributeScope.class) + 
                                            ')' + 
                                            ASM.getDescriptor(EventAttributeContext.class),
                                            false);
                                    mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(InAllChainAttributeContext.class));
                                    mv.visitVarInsn(Opcodes.ALOAD, exIndex);
                                    mv.visitMethodInsn(
                                            Opcodes.INVOKEINTERFACE,
                                            ASM.getInternalName(InAllChainAttributeContext.class),
                                            "setPreThrowable",
                                            "(Ljava/lang/Throwable;)V",
                                            true);
                                }
                            }));
            
            /*
             * try {
             *      FrozenContext.suspendFreezing(ctx, this.{owner});
             *      try {
             *          this.? = ?;
             *      } finally {
             *          FrozenContext.resumeFreezing(ctx);
             *      }
             *      ((GlobalAttributeContext)event.getAttributeContext(AttributeScope.IN_ALL_CHAIN))
             *      .success();
             * } catch (RuntimeException | Error ex) {
             *      if (finalException == null) {
             *          finalException = ex;
             *      }
             *      ((GlobalAttributeContext)event.getAttributeContext(AttributeScope.IN_ALL_CHAIN))
             *      .setThrowable(ex);
             * }
             */
            mv.visitTryCatchBlock(
                    new Action<XMethodVisitor>() {
                        @Override
                        public void run(XMethodVisitor mv) {
                            DefaultObjectModelImplGenerator.this.generateScalarSetterWithFrozenContextOperationInsns(
                                    mv, 
                                    scalarProperty,
                                    ctxIndex, 
                                    finalExceptionIndex);
                            
                            mv.visitVarInsn(Opcodes.ALOAD, eventIndex);
                            mv.visitFieldInsn(
                                    Opcodes.GETSTATIC, 
                                    ASM.getInternalName(AttributeScope.class), 
                                    AttributeScope.GLOBAL.name(), 
                                    ASM.getDescriptor(AttributeScope.class));
                            mv.visitMethodInsn(
                                    Opcodes.INVOKEVIRTUAL, 
                                    ASM.getInternalName(ModificationEvent.class), 
                                    "getAttributeContext", 
                                    '(' + 
                                    ASM.getDescriptor(AttributeScope.class) + 
                                    ')' + 
                                    ASM.getDescriptor(EventAttributeContext.class),
                                    false);
                            mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(GlobalAttributeContext.class));
                            mv.visitMethodInsn(
                                    Opcodes.INVOKEINTERFACE,
                                    ASM.getInternalName(GlobalAttributeContext.class),
                                    "success",
                                    "()V",
                                    true);
                        }
                    },  
                    new Catch(
                            new Class[] { RuntimeException.class, Error.class },
                            new Action<XMethodVisitor>() {
                                @Override
                                public void run(XMethodVisitor mv) {
                                    int exIndex = mv.aSlot("ex");
                                    mv.visitVarInsn(Opcodes.ASTORE, exIndex);
                                    mv.visitVarInsn(Opcodes.ALOAD, exIndex);
                                    mv.visitAStoreInsnIfNull(finalExceptionIndex);
                                    mv.visitVarInsn(Opcodes.ALOAD, eventIndex);
                                    mv.visitFieldInsn(
                                            Opcodes.GETSTATIC, 
                                            ASM.getInternalName(AttributeScope.class), 
                                            AttributeScope.GLOBAL.name(), 
                                            ASM.getDescriptor(AttributeScope.class));
                                    mv.visitMethodInsn(
                                            Opcodes.INVOKEVIRTUAL, 
                                            ASM.getInternalName(ModificationEvent.class), 
                                            "getAttributeContext", 
                                            '(' + 
                                            ASM.getDescriptor(AttributeScope.class) + 
                                            ')' + 
                                            ASM.getDescriptor(EventAttributeContext.class),
                                            false);
                                    mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(GlobalAttributeContext.class));
                                    mv.visitVarInsn(Opcodes.ALOAD, exIndex);
                                    mv.visitMethodInsn(
                                            Opcodes.INVOKEINTERFACE,
                                            ASM.getInternalName(GlobalAttributeContext.class),
                                            "setThrowable",
                                            "(Ljava/lang/Throwable;)V",
                                            true);
                                }
                            }
                    ));
            
            /*
             * try {
             *      this.{executeModified}(event);
             * } catch (RuntimeException | Error ex) {
             *      if (finalException == null) {
             *          finalException = ex;
             *      }
             * }
             */
            mv.visitTryCatchBlock(
                    new Action<XMethodVisitor>() {
                        @Override
                        public void run(XMethodVisitor mv) {
                            mv.visitVarInsn(Opcodes.ALOAD, 0);
                            mv.visitVarInsn(Opcodes.ALOAD, eventIndex);
                            mv.visitMethodInsn(
                                Opcodes.INVOKEVIRTUAL, 
                                DefaultObjectModelImplGenerator.this.internalName, 
                                OBJECT_MODEL_IMPL_EXECUTE_MODIFIED, 
                                '(' + ASM.getDescriptor(ScalarEvent.class) + ")V",
                                false);
                        }
                    }, 
                    new Catch(
                            new Class[] { RuntimeException.class, Error.class },
                            new Action<XMethodVisitor>() {
                                @Override
                                public void run(XMethodVisitor mv) {
                                    mv.visitAStoreInsnIfNull(finalExceptionIndex);
                                }
                            }));
            
            /*
             * if (finalException != null) {
             *      throw finalException;
             * }
             */
            mv.visitVarInsn(Opcodes.ALOAD, finalExceptionIndex);
            Label fianlExceptionIsNullLabel = new Label();
            mv.visitJumpInsn(Opcodes.IFNULL, fianlExceptionIsNullLabel);
            mv.visitVarInsn(Opcodes.ALOAD, finalExceptionIndex);
            mv.visitInsn(Opcodes.ATHROW);
            mv.visitLabel(fianlExceptionIsNullLabel);
            
            mv.visitInsn(Opcodes.RETURN);
        }
    
        private void generateScalarSetterWithFrozenContextOperationInsns(
                XMethodVisitor mv,
                final ScalarProperty scalarProperty,
                final int ctxIndex,
                final int finalExceptionIndex) {
            
            // FrozenContext.suspendFreezing(ctx, this.{owner});
            mv.visitVarInsn(Opcodes.ALOAD, ctxIndex);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.internalName, 
                    OWNER, 
                    ASM.getDescriptor(scalarProperty.getDeclaringObjectModelMetadata().getOwnerClass()));
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASM.getInternalName(FrozenContext.class), 
                    "suspendFreezing", 
                    '(' +
                    ASM.getDescriptor(FrozenContext.class) +
                    "Ljava/lang/Object;)V",
                    false);
            
            final Action<XMethodVisitor> finallyAction = new Action<XMethodVisitor>() {
                @Override
                public void run(XMethodVisitor mv) {
                    // FrozenContext.resumeFreezing(ctx);
                    mv.visitVarInsn(Opcodes.ALOAD, ctxIndex);
                    mv.visitMethodInsn(
                            Opcodes.INVOKESTATIC, 
                            ASM.getInternalName(FrozenContext.class), 
                            "resumeFreezing", 
                            '(' +
                            ASM.getDescriptor(FrozenContext.class) +
                            ")V",
                            false);
                }
            };
            mv.visitTryCatchBlock(
                    new Action<XMethodVisitor>() {
                        @Override
                        public void run(XMethodVisitor mv) {
                            DefaultObjectModelImplGenerator.this.generateScalarSetterWithEmbeddedBubbleOperationInsns(
                                    mv, 
                                    scalarProperty);
                        }
                    }, 
                    new Catch(
                            Throwable.class,
                            new Action<XMethodVisitor>() {
                                @Override
                                public void run(XMethodVisitor mv) {
                                    int exIndex = mv.aSlot("ex");
                                    mv.visitVarInsn(Opcodes.ASTORE, exIndex);
                                    Label isNotNullLabel = new Label();
                                    mv.visitVarInsn(Opcodes.ALOAD, finalExceptionIndex);
                                    mv.visitJumpInsn(Opcodes.IFNONNULL, isNotNullLabel);
                                    mv.visitVarInsn(Opcodes.ALOAD, exIndex);
                                    mv.visitVarInsn(Opcodes.ASTORE, finalExceptionIndex);
                                    mv.visitLabel(isNotNullLabel);
                                    finallyAction.run(mv);
                                    mv.visitVarInsn(Opcodes.ALOAD, exIndex);
                                    mv.visitInsn(Opcodes.ATHROW);
                                }
                            })
                );
            finallyAction.run(mv);
        }
        
        private void generateScalarSetterWithEmbeddedBubbleOperationInsns(
                XMethodVisitor mv,
                ScalarProperty scalarProperty) {
            
            ObjectModelMetadata returnObjectModelMetadata = scalarProperty.getReturnObjectModelMetadata();
            boolean isEmbedded = 
                    returnObjectModelMetadata != null && 
                    returnObjectModelMetadata.getMode() == ObjectModelMode.EMBEDDABLE;
            
            if (!isEmbedded) {
                this.generateScalarRawSetterInsns(mv, scalarProperty, true);
            } else {
                /*
                 * ? type oldScalar = this.?;
                 */
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        this.internalName, 
                        scalarProperty.getName(), 
                        ASM.getDescriptor(scalarProperty.getReturnClass()));
                final int oldScalarIndex = mv.aSlot("oldScalar");
                mv.visitVarInsn(Opcodes.ASTORE, oldScalarIndex);
                
                this.generateEmbbededScalarListenerChangeInsns(
                        mv,
                        scalarProperty,
                        oldScalarIndex,
                        false
                );
                
                this.generateScalarRawSetterInsns(mv, scalarProperty, true);
                
                this.generateEmbbededScalarListenerChangeInsns(
                        mv,
                        scalarProperty,
                        1,
                        true
                );
            }
        }
        
        private void generateScalarRawSetterInsns(
                XMethodVisitor mv, 
                ScalarProperty scalarProperty, 
                boolean makeDirty) {
            /*
             * this.? = ?;
             */
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(ASM.getLoadCode(scalarProperty.getReturnClass()), 1);
            mv.visitFieldInsn(
                    Opcodes.PUTFIELD, 
                    this.internalName, 
                    scalarProperty.getName(), 
                    ASM.getDescriptor(scalarProperty.getReturnClass()));
            
            /*
             * this.?{state} = 0;
             */
            this.generateScalarStateCleanInsn(mv, scalarProperty);
            
            if (makeDirty) {
                /*
                 * ObjectModelLoader loader = this.getScalarLoader();
                 * if (loader instanceof ObjectModelLoaderDirtinessAware) {
                 *      ((ObjectModelLoaderDirtinessAware)loader).dirty();
                 * }
                 */
                final int objectModelLoaderIndex = mv.aSlot("objectModelLoader");
                Label nonLoaderDirtinessAwareLabel = new Label();
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        this.internalName, 
                        "getScalarLoader", 
                        "()" + ASM.getDescriptor(ObjectModelScalarLoader.class),
                        false);
                mv.visitVarInsn(Opcodes.ASTORE, objectModelLoaderIndex);
                mv.visitVarInsn(Opcodes.ALOAD, objectModelLoaderIndex);
                mv.visitTypeInsn(Opcodes.INSTANCEOF, ASM.getInternalName(ObjectModelLoaderDirtinessAware.class));
                mv.visitJumpInsn(Opcodes.IFEQ, nonLoaderDirtinessAwareLabel);
                mv.visitVarInsn(Opcodes.ALOAD, objectModelLoaderIndex);
                mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(ObjectModelLoaderDirtinessAware.class));
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        ASM.getInternalName(ObjectModelLoaderDirtinessAware.class), 
                        "dirty", 
                        "()V",
                        true);
                mv.visitLabel(nonLoaderDirtinessAwareLabel);
            }
        }
        
        private void generateScalarStateCleanInsn(MethodVisitor mv, ScalarProperty scalarProperty) {
            /*
             * this.?{state} = 0;
             */
            if (isStateRequired(scalarProperty)) {
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitFieldInsn(
                        Opcodes.PUTFIELD, 
                        this.internalName, 
                        scalarProperty.getName() + OBJECT_MODEL_IMPL_STATE_POSTFIX, 
                        "I");
            }
        }
        
        private void generateEmbbededScalarListenerChangeInsns(
                MethodVisitor mv,
                ScalarProperty scalarProperty,
                int scalarIndex,
                boolean addScalarListener) {
            
            String listenerInterName = embeddedScalarListenerImplClassName(scalarProperty, false).replace('.', '/');
            
            Label scalarIsNullLabel = new Label();
            mv.visitVarInsn(ASM.getLoadCode(scalarProperty.getReturnClass()), scalarIndex);
            mv.visitJumpInsn(Opcodes.IFNULL, scalarIsNullLabel);
            ObjectModelFactoryProvider.visitGetObjectModel(mv, scalarProperty.getReturnObjectModelMetadata(), scalarIndex);
            mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(ScalarModificationAware.class));
            mv.visitTypeInsn(Opcodes.NEW, listenerInterName);
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    listenerInterName, 
                    "<init>", 
                    '(' + this.descriptor + ")V",
                    false);
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    ASM.getInternalName(ScalarModificationAware.class), 
                    addScalarListener ? "addScalarListener" : "removeScalarListener", 
                    '(' + ASM.getDescriptor(ScalarListener.class) + ")V",
                    true);
            mv.visitLabel(scalarIsNullLabel);
        }
        
        private void generateGetRootObjectModelInsns(MethodVisitor mv, final int objectModelIndex) {
            if (this.objectModelMetadata.getSuperMetadata() == null) {
                mv.visitVarInsn(Opcodes.ALOAD, objectModelIndex);
            } else {
                visitGetObjectModel(
                        mv, 
                        getRootObjectModelMetadata(this.objectModelMetadata), 
                        new Action<MethodVisitor>() {
                            @Override
                            public void run(MethodVisitor mv) {
                                mv.visitVarInsn(Opcodes.ALOAD, objectModelIndex);
                                mv.visitFieldInsn(
                                        Opcodes.GETFIELD, 
                                        DefaultObjectModelImplGenerator.this.internalName, 
                                        OWNER, 
                                        DefaultObjectModelImplGenerator.this.ownerDescriptor);
                            }
                        });
                mv.visitTypeInsn(Opcodes.CHECKCAST, this.getRootObjectModelImplInternalName());
            }
        }
        
        private void generateToString(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "toString", 
                    "()Ljava/lang/String;", 
                    null, 
                    null);
            mv.visitCode();
            
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASM.getInternalName(AppendingContext.class), 
                    "toString",
                    '(' + ASM.getDescriptor(ObjectModel.class) + ")Ljava/lang/String;",
                    false);
            mv.visitInsn(Opcodes.ARETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private String getRootObjectModelImplInternalName() {
            return objectModelImplClassName(
                    getRootObjectModelMetadata(this.objectModelMetadata)
            ).replace('.', '/');
        }
        
        private boolean cloneScalar(ScalarProperty scalarProperty, MethodVisitor mv) {
            Class<?> scalarType = scalarProperty.getReturnClass();
            boolean isDate = java.util.Date.class.isAssignableFrom(scalarType);
            boolean isCalendar = java.util.Calendar.class.isAssignableFrom(scalarType);
            if (!isDate && !isCalendar) {
                return false;
            }
            if (mv == null) {
                return true;
            }
            Label finishCloneLabel = new Label();
            mv.visitInsn(Opcodes.DUP);
            mv.visitJumpInsn(Opcodes.IFNULL, finishCloneLabel);
            
            if (isDate) {
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        ASM.getInternalName(java.util.Date.class), 
                        "clone", 
                        "()Ljava/lang/Object;", 
                        false);
                mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(scalarProperty.getReturnClass()));
            } else {
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        ASM.getInternalName(java.util.Calendar.class), 
                        "clone", 
                        "()Ljava/lang/Object;", 
                        false);
                mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(scalarProperty.getReturnClass()));
            }
            
            mv.visitLabel(finishCloneLabel);
            /*
             * (1) Need not to clone Primitive, Character, Derived classes of Number, java.time.* 
             *      because they are immutable.
             * (2) Need not to clone Embedded scalar because they support bubbled modification event
             *      so that its changing can be felt by its owner ObjectModel, this is cool functionality 
             *      of ObjectModel.
             * (3) Need not to clone char[], byte[] and java.io.Serializable because they are 
             *      they are NOT allowed to be used by EqualityComparator and Comparator.
             * (4) Enum is not allowed to be cloned. Theoretically, java allows the developer to create
             *      mutable Enum, but ... I believe most of developers will not do this to smash the feet
             *      of themselves.
             */
            return true;
        }
        
        final Class<?> getImplementationClass() {
            return this.implementationClass;
        }
    }

    protected class FactoryImplGenerator {
    
        private ObjectModelMetadata objectModelMetadata;
        
        private String className;
        
        private String internalName;
        
        private String objectModelImplInternalName;
        
        protected FactoryImplGenerator(ObjectModelMetadata objectModelMetadata) {
            this.objectModelMetadata = objectModelMetadata;
            this.className = 
                    objectModelFactoryImplClassName(this.objectModelMetadata, false);
            this.internalName = className.replace('.', '/');
            this.objectModelImplInternalName = 
                    objectModelImplClassName(this.objectModelMetadata).replace('.', '/');
            this.generate();
        }
        
        protected final ObjectModelMetadata getObjectModelMetadata() {
            return this.objectModelMetadata;
        }
    
        protected final String getClassName() {
            return this.className;
        }
    
        protected final String getInternalName() {
            return this.internalName;
        }
    
        protected final String getObjectModelImplInternalName() {
            return this.objectModelImplInternalName;
        }
        
        protected void generateCreateInsns(MethodVisitor mv) {
            mv.visitTypeInsn(Opcodes.NEW, this.objectModelImplInternalName);
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    this.objectModelImplInternalName, 
                    "<init>", 
                    "(Ljava/lang/Object;)V",
                    false);
            mv.visitInsn(Opcodes.ARETURN);
        }
        
        protected void generateGetInsns(MethodVisitor mv) {
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            Class<?> ownerClass = this.objectModelMetadata.getOwnerClass();
            mv.visitTypeInsn(Opcodes.CHECKCAST, ownerClass.getName().replace('.', '/'));
            Method staticMethodToGetObjectModel;
            try {
                staticMethodToGetObjectModel = ownerClass.getDeclaredMethod(
                        this.objectModelMetadata.getStaticMethodName(), 
                        ownerClass);
            } catch (NoSuchMethodException ex) {
                throw UncheckedException.rethrow(ex);
            }
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ownerClass.getName().replace('.', '/'), 
                    staticMethodToGetObjectModel.getName(), 
                    ASM.getDescriptor(staticMethodToGetObjectModel),
                    false);
            mv.visitInsn(Opcodes.ARETURN);
        }
    
        private void generate() {
            Action<ClassVisitor> cvAction = new Action<ClassVisitor>() {
                @Override
                public void run(ClassVisitor cv) {
                    FactoryImplGenerator.this.generate(cv);
                }
            };
            ASM.loadDynamicClass(
                    this.objectModelMetadata.getObjectModelClass().getClassLoader(), 
                    objectModelFactoryImplClassName(this.objectModelMetadata, false), 
                    this.objectModelMetadata.getObjectModelClass().getProtectionDomain(),
                    cvAction);
        }
        
        private void generate(ClassVisitor cv) {
            cv.visit(
                    Opcodes.V1_7, 
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, 
                    this.internalName, 
                    null, 
                    "java/lang/Object", 
                    new String[] { 
                            ASM.getInternalName(ObjectModelFactory.class),
                            ASM.getInternalName(Serializable.class)
                    });
            
            visitFactoryImplNestClass(cv, this.objectModelMetadata);
            
            this.generateInit(cv);
            this.generateCreate(cv);
            this.generateGet(cv);
            this.generateGetObjectModelMetadata(cv);
            this.generateWriteReplace(cv);
            
            cv.visitEnd();
        }
        
        private void generateInit(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "<init>", 
                    "()V", 
                    null, 
                    null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    "java/lang/Object", 
                    "<init>", 
                    "()V",
                    false);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateCreate(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, 
                    "create", 
                    "(Ljava/lang/Object;)Ljava/lang/Object;", 
                    null,
                    null);
            mv.visitCode();
            
            this.generateValidateOwnerArgumentInsns(mv, false);
            this.generateCreateInsns(mv);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateGet(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, 
                    "get", 
                    "(Ljava/lang/Object;)Ljava/lang/Object;", 
                    null, 
                    null);
            mv.visitCode();
            
            this.generateValidateOwnerArgumentInsns(mv, true);
            this.generateGetInsns(mv);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateGetObjectModelMetadata(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, 
                    "getObjectModelMetadata", 
                    "()" + ASM.getDescriptor(ObjectModelMetadata.class), 
                    null, 
                    null);
            mv.visitCode();
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    this.objectModelImplInternalName, 
                    OBJECT_MODEL_IMPL_OBJECT_MODEL_METADATA, 
                    ASM.getDescriptor(ObjectModelMetadata.class));
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateWriteReplace(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PRIVATE, 
                    "writeReplace", 
                    "()Ljava/lang/Object;", 
                    null, 
                    new String[] { ASM.getInternalName(ObjectStreamException.class) });
            mv.visitCode();
            mv.visitTypeInsn(Opcodes.NEW, ASM.getInternalName(ObjectModelFactoryWritingReplacement.class));
            mv.visitInsn(Opcodes.DUP);
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    this.objectModelImplInternalName, 
                    OBJECT_MODEL_IMPL_OBJECT_MODEL_METADATA, 
                    ASM.getDescriptor(ObjectModelMetadata.class));
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    ASM.getInternalName(ObjectModelMetadata.class), 
                    "getProvider", 
                    "()" + ASM.getDescriptor(ObjectModelFactoryProvider.class),
                    true);
            mv.visitLdcInsn(org.babyfish.org.objectweb.asm.Type.getType(this.objectModelMetadata.getObjectModelClass()));
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    ASM.getInternalName(ObjectModelFactoryWritingReplacement.class), 
                    "<init>", 
                    '(' +
                    ASM.getDescriptor(ObjectModelFactoryProvider.class) +
                    "Ljava/lang/Class;)V",
                    false);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    
        private void generateValidateOwnerArgumentInsns(MethodVisitor mv, boolean acceptNull) {
            
            if (acceptNull) {
                Label isNotNullLabel = new Label();
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitJumpInsn(Opcodes.IFNONNULL, isNotNullLabel);
                mv.visitInsn(Opcodes.ACONST_NULL);
                mv.visitInsn(Opcodes.ARETURN);
                mv.visitLabel(isNotNullLabel);
            } 
            mv.visitLdcInsn("owner");
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    Arguments.class.getName().replace('.', '/'), 
                    "mustNotBeNull", 
                    "(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;",
                    false);
            mv.visitInsn(Opcodes.POP);
            
            mv.visitLdcInsn("owner");
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitLdcInsn(Type.getType(this.objectModelMetadata.getOwnerClass()));
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    Arguments.class.getName().replace('.', '/'), 
                    "mustBeInstanceOfValue", 
                    "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;",
                    false);
            mv.visitInsn(Opcodes.POP);
        }
    }

    private class PropertyDelegateGenerator {
        
        private ObjectModelMetadata objectModelMetadata;
        
        private String interfaceName;
        
        private String internalName;
        
        private String objectModelDescriptor;
        
        private String objectModelImplDescriptor;
        
        public PropertyDelegateGenerator(ObjectModelMetadata objectModelMetadata) {
            this.objectModelMetadata = objectModelMetadata;
            this.interfaceName = propertyDelegateInterfaceName(objectModelMetadata, false);
            this.internalName = this.interfaceName.replace('.', '/');
            this.objectModelDescriptor = ASM.getDescriptor(this.objectModelMetadata.getObjectModelClass());
            this.objectModelImplDescriptor = 
                    'L' + 
                    objectModelImplClassName(objectModelMetadata).replace('.', '/') +
                    ';';
            this.generate();
        }
        
        private void generate() {
            Action<ClassVisitor> cvAction = new Action<ClassVisitor>() {
                @Override
                public void run(ClassVisitor cv) {
                    PropertyDelegateGenerator.this.generate(cv);
                    cv.visitEnd();
                }
            };
            ASM.loadDynamicClass(
                    this.objectModelMetadata.getObjectModelClass().getClassLoader(), 
                    this.interfaceName, 
                    this.objectModelMetadata.getObjectModelClass().getProtectionDomain(),
                    cvAction);
        }
        
        private void generate(ClassVisitor cv) {
            cv.visit(
                    Opcodes.V1_7,
                    Opcodes.ACC_INTERFACE | Opcodes.ACC_PRIVATE | Opcodes.ACC_ABSTRACT | Opcodes.ACC_STATIC, 
                    this.internalName, 
                    null, 
                    "java/lang/Object", 
                    null);
            
            visitPropertyDelegateNestedClass(cv, this.objectModelMetadata);
            cv.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, 
                    "getScalar", 
                    '(' + this.objectModelDescriptor + ")Ljava/lang/Object;", 
                    null,
                    null
            ).visitEnd();
            cv.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, 
                    "setScalar", 
                    '(' + this.objectModelDescriptor + "Ljava/lang/Object;)V", 
                    null,
                    null
            ).visitEnd();
            cv.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, 
                    "getAssociation", 
                    '(' + this.objectModelDescriptor + ')' + ASM.getDescriptor(AssociatedEndpoint.class), 
                    null,
                    null
            ).visitEnd();
            for (int i = 0; i < 2; i++) {
                cv.visitMethod(
                        Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, 
                        i == 0 ? "freezeScalar" : "unfreezeScalar", 
                        '(' + 
                        this.objectModelImplDescriptor + 
                        ASM.getDescriptor(FrozenContext.class) + 
                        ")V", 
                        null,
                        null
                ).visitEnd();
            }
            cv.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, 
                    "isDisabled", 
                    '(' + this.objectModelImplDescriptor + ")Z", 
                    null,
                    null
            ).visitEnd();
            cv.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, 
                    "disable", 
                    '(' + this.objectModelImplDescriptor + ")V", 
                    null,
                    null
            ).visitEnd();
            cv.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, 
                    "enable", 
                    '(' + this.objectModelImplDescriptor + ")V", 
                    null,
                    null
            ).visitEnd();
            cv.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, 
                    "isUnloaded", 
                    '(' + this.objectModelImplDescriptor + ")Z", 
                    null,
                    null
            ).visitEnd();
            cv.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, 
                    "unload", 
                    '(' + this.objectModelImplDescriptor + ")V", 
                    null,
                    null
            ).visitEnd();
        }
    }

    protected class PropertyDelegateImplGenerator {
        
        private static final int MT_IS_DISABLED = 0;
        
        private static final int MT_DISABLE = 1;
        
        private static final int MT_ENABLE = 2;
        
        private Property property;
        
        private String className;
        
        private String internalName;
        
        private String propertyDelegateInternalName;
        
        private String objectModelInternalName;
        
        private String objectModelDescriptor;
        
        private String objectModelImplInternalName;
        
        private String objectModelImplDescriptor;
        
        PropertyDelegateImplGenerator(Property property) {
            this.property = property;
            this.className = propertyDelegateImplClassName(property, false);
            this.internalName = this.className.replace('.', '/');
            this.propertyDelegateInternalName = propertyDelegateInterfaceName(
                    property.getDeclaringObjectModelMetadata(), 
                    false
            ).replace('.', '/');
            this.objectModelInternalName = 
                    ASM
                    .getInternalName(
                            property
                            .getDeclaringObjectModelMetadata()
                            .getObjectModelClass()
                    );
            this.objectModelDescriptor = 'L' + this.objectModelInternalName + ';';
            this.objectModelImplInternalName = 
                    objectModelImplClassName(
                            property.getDeclaringObjectModelMetadata()
                    ).replace('.', '/');
            this.objectModelImplDescriptor = 'L' + this.objectModelImplInternalName + ';';
            this.generate();
        }
        
        protected final Property getProperty() {
            return this.property;
        }

        protected final String getClassName() {
            return this.className;
        }

        protected final String getInternalName() {
            return this.internalName;
        }

        protected final String getPropertyDelegateInternalName() {
            return this.propertyDelegateInternalName;
        }

        protected final String getObjectModelInternalName() {
            return this.objectModelInternalName;
        }

        protected final String getObjectModelDescriptor() {
            return this.objectModelDescriptor;
        }

        protected final String getObjectModelImplInternalName() {
            return this.objectModelImplInternalName;
        }

        protected final String getObjectModelImplDescriptor() {
            return this.objectModelImplDescriptor;
        }
        
        private void generate() {
            Class<?> objectModelClass = this.property.getDeclaringObjectModelMetadata().getObjectModelClass();
            Action<ClassVisitor> cvAction = new Action<ClassVisitor>() {
                @Override
                public void run(ClassVisitor cv) {
                    PropertyDelegateImplGenerator.this.generate(cv);
                    cv.visitEnd();
                }
            };
            ASM.loadDynamicClass(
                    objectModelClass.getClassLoader(), 
                    this.className, 
                    objectModelClass.getProtectionDomain(),
                    cvAction);
        }
    
        private void generate(ClassVisitor cv) {
            visitPropertyDelegateImplNestedClass(cv, this.property);
            cv.visit(
                    Opcodes.V1_7, 
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, 
                    PropertyDelegateImplGenerator.this.internalName, 
                    null, 
                    "java/lang/Object", 
                    new String[] { this.propertyDelegateInternalName }
            );
            this.generateInit(cv);
            this.generateGetScalar(cv);
            this.generateSetScalar(cv);
            this.generateGetAssociation(cv);
            this.generateFreezeScalarOrUnfreezeScalar(cv, true);
            this.generateFreezeScalarOrUnfreezeScalar(cv, false);
            this.generateIsDisabled(cv);
            this.generateDisable(cv);
            this.generateEnable(cv);
            this.generateIsUnloaded(cv);
            this.generateUnload(cv);
        }
    
        private void generateInit(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "<init>", 
                    "()V", 
                    null,
                    null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    "java/lang/Object", 
                    "<init>", 
                    "()V",
                    false);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateGetScalar(ClassVisitor cv) {
            XMethodVisitor mv = ASM.visitMethod(
                    cv,
                    Opcodes.ACC_PUBLIC, 
                    "getScalar", 
                    '(' + this.objectModelDescriptor + ")Ljava/lang/Object;", 
                    null,
                    null);
            mv.visitCode();
            if (this.property instanceof AssociationProperty) {
                generateThrowIllegalStateExceptionInsns(mv, property);
            } else {
                mv.visitBox(this.property.getReturnClass(), new Action<XMethodVisitor>() {
                    @Override
                    public void run(XMethodVisitor mv) {
                        mv.visitVarInsn(Opcodes.ALOAD, 1);
                        mv.visitMethodInsn(
                                Opcodes.INVOKEINTERFACE, 
                                PropertyDelegateImplGenerator.this.objectModelInternalName, 
                                property.getGetterName(), 
                                "()" + ASM.getDescriptor(property.getReturnClass()),
                                true);
                    }
                });
                mv.visitInsn(Opcodes.ARETURN);
            }
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateSetScalar(ClassVisitor cv) {
            XMethodVisitor mv = ASM.visitMethod(
                    cv,
                    Opcodes.ACC_PUBLIC, 
                    "setScalar", 
                    '(' + this.objectModelDescriptor + "Ljava/lang/Object;)V", 
                    null,
                    null);
            mv.visitCode();
            if (this.property instanceof AssociationProperty) {
                generateThrowIllegalStateExceptionInsns(mv, property);
            } else {
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitVarInsn(Opcodes.ALOAD, 2);
                Class<?> returnClass = this.property.getReturnClass();
                if (returnClass.isPrimitive()) {
                    mv.visitUnbox(returnClass, XMethodVisitor.JVM_PRIMTIVIE_DEFAULT_VALUE);
                } else {
                    if (returnClass != Object.class) {
                        mv.visitTypeInsn(Opcodes.CHECKCAST, returnClass.getName().replace('.', '/'));
                    }
                }
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        this.objectModelInternalName, 
                        property.getSetterName(), 
                        '(' + ASM.getDescriptor(returnClass) + ")V",
                        true);
                mv.visitInsn(Opcodes.RETURN);
            }
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateGetAssociation(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "getAssociation", 
                    '(' + this.objectModelDescriptor + ')' + ASM.getDescriptor(AssociatedEndpoint.class), 
                    null,
                    null);
            mv.visitCode();
            if (this.property instanceof ScalarProperty) {
                generateThrowIllegalStateExceptionInsns(mv, property);
            } else {
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        this.objectModelInternalName, 
                        property.getGetterName(), 
                        "()" + ASM.getDescriptor(this.property.getReturnClass()),
                        true);
                mv.visitTypeInsn(Opcodes.CHECKCAST, AssociatedEndpoint.class.getName().replace('.', '/'));
                mv.visitInsn(Opcodes.ARETURN);
            }
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateFreezeScalarOrUnfreezeScalar(ClassVisitor cv, boolean freeze) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    freeze ? "freezeScalar" : "unfreezeScalar", 
                    '(' + this.objectModelImplDescriptor + ASM.getDescriptor(FrozenContext.class) + ")V", 
                    null,
                    null);
            mv.visitCode();
            if (this.property instanceof AssociationProperty) {
                generateThrowIllegalStateExceptionInsns(mv, property);
            } else {
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        this.objectModelImplInternalName,
                        this.property.getName() + OBJECT_MODEL_IMPL_FROZEN_CONTEXT_POSTFIX,
                        ASM.getDescriptor(FrozenContext.class));
                mv.visitVarInsn(Opcodes.ALOAD, 2);
                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC, 
                        ASM.getInternalName(FrozenContext.class), 
                        freeze ? "combine" : "remove", 
                        '(' +
                        ASM.getDescriptor(FrozenContext.class) +
                        ASM.getDescriptor(FrozenContext.class) +
                        ')' +
                        ASM.getDescriptor(FrozenContext.class),
                        false);
                mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(FrozenContext.class));
                mv.visitFieldInsn(
                        Opcodes.PUTFIELD, 
                        this.objectModelImplInternalName,
                        property.getName() + OBJECT_MODEL_IMPL_FROZEN_CONTEXT_POSTFIX,
                        ASM.getDescriptor(FrozenContext.class));
                mv.visitInsn(Opcodes.RETURN);
            }
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateIsDisabled(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "isDisabled", 
                    '(' + this.objectModelImplDescriptor + ")Z", 
                    null,
                    null);
            mv.visitCode();
            if (this.property.isDisabilityAllowed()) {
                if (this.property instanceof ScalarProperty) {
                    mv.visitVarInsn(Opcodes.ALOAD, 1);
                    mv.visitFieldInsn(
                            Opcodes.GETFIELD, 
                            this.objectModelImplInternalName, 
                            this.property.getName() + OBJECT_MODEL_IMPL_STATE_POSTFIX, 
                            "I");
                    mv.visitInsn(OPCODE_CONST_DISABLED);
                    mv.visitInsn(Opcodes.IAND);
                    Label isEnabledLabel = new Label();
                    mv.visitJumpInsn(Opcodes.IFEQ, isEnabledLabel);
                    mv.visitInsn(Opcodes.ICONST_1);
                    mv.visitInsn(Opcodes.IRETURN);
                    mv.visitLabel(isEnabledLabel);
                } else if (isContravarianceAssociationProperty(this.property)) {
                    this.generateContravarianceHandleInsns(mv, MT_IS_DISABLED);
                } else {
                    mv.visitVarInsn(Opcodes.ALOAD, 1);
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL, 
                            this.objectModelImplInternalName, 
                            this.property.getGetterName(), 
                            "()" + ASM.getDescriptor(this.property.getReturnClass()),
                            false);
                    mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(DisablityManageable.class));
                    mv.visitMethodInsn(
                            Opcodes.INVOKEINTERFACE, 
                            ASM.getInternalName(DisablityManageable.class), 
                            "isDisabled", 
                            "()Z",
                            true);
                    mv.visitInsn(Opcodes.IRETURN);
                }
            }
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateDisable(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "disable", 
                    '(' + this.objectModelImplDescriptor + ")V", 
                    null,
                    null);
            mv.visitCode();
            if (this.property.isDisabilityAllowed()) {
                if (this.property instanceof ScalarProperty) {
                    mv.visitVarInsn(Opcodes.ALOAD, 1);
                    mv.visitVarInsn(Opcodes.ALOAD, 1);
                    mv.visitFieldInsn(
                            Opcodes.GETFIELD, 
                            this.objectModelImplInternalName, 
                            this.property.getName() + OBJECT_MODEL_IMPL_STATE_POSTFIX, 
                            "I");
                    mv.visitInsn(OPCODE_CONST_DISABLED);
                    mv.visitInsn(Opcodes.IOR);
                    mv.visitFieldInsn(
                            Opcodes.PUTFIELD, 
                            this.objectModelImplInternalName, 
                            this.property.getName() + OBJECT_MODEL_IMPL_STATE_POSTFIX, 
                            "I");
                } else if (isContravarianceAssociationProperty(this.property)) {
                    this.generateContravarianceHandleInsns(mv, MT_DISABLE);
                } else {
                    mv.visitVarInsn(Opcodes.ALOAD, 1);
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL, 
                            this.objectModelImplInternalName, 
                            this.property.getGetterName(), 
                            "()" + ASM.getDescriptor(this.property.getReturnClass()),
                            false);
                    mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(DisablityManageable.class));
                    mv.visitMethodInsn(
                            Opcodes.INVOKEINTERFACE, 
                            ASM.getInternalName(DisablityManageable.class), 
                            "disable", 
                            "()V",
                            true);
                }
            }
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateEnable(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "enable", 
                    '(' + this.objectModelImplDescriptor + ")V", 
                    null,
                    null);
            mv.visitCode();
            if (this.property.isDisabilityAllowed()) {
                if (this.property instanceof ScalarProperty) {
                    mv.visitVarInsn(Opcodes.ALOAD, 1);
                    mv.visitVarInsn(Opcodes.ALOAD, 1);
                    mv.visitFieldInsn(
                            Opcodes.GETFIELD, 
                            this.objectModelImplInternalName, 
                            this.property.getName() + OBJECT_MODEL_IMPL_STATE_POSTFIX, 
                            "I");
                    mv.visitInsn(OPCODE_CONST_DISABLED);
                    mv.visitInsn(Opcodes.ICONST_M1);
                    mv.visitInsn(Opcodes.IXOR);
                    mv.visitInsn(Opcodes.IAND);
                    mv.visitFieldInsn(
                            Opcodes.PUTFIELD, 
                            this.objectModelImplInternalName, 
                            this.property.getName() + OBJECT_MODEL_IMPL_STATE_POSTFIX, 
                            "I");
                } else if (isContravarianceAssociationProperty(this.property)) {
                    this.generateContravarianceHandleInsns(mv, MT_ENABLE);
                } else {
                    mv.visitVarInsn(Opcodes.ALOAD, 1);
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL, 
                            this.objectModelImplInternalName, 
                            this.property.getGetterName(), 
                            "()" + ASM.getDescriptor(this.property.getReturnClass()),
                            false);
                    mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(DisablityManageable.class));
                    mv.visitMethodInsn(
                            Opcodes.INVOKEINTERFACE, 
                            ASM.getInternalName(DisablityManageable.class), 
                            "enable", 
                            "()V",
                            true);
                }
            }
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateIsUnloaded(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "isUnloaded", 
                    '(' + this.objectModelImplDescriptor + ")Z", 
                    null,
                    null);
            mv.visitCode();
            if (isDeferrableScalarProperty(this.property)) {
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitFieldInsn(
                        Opcodes.GETFIELD,
                        this.objectModelImplInternalName,
                        this.property.getName() + OBJECT_MODEL_IMPL_STATE_POSTFIX,
                        "I");
                mv.visitInsn(OPCODE_CONST_UNLOADED);
                mv.visitInsn(Opcodes.IAND);
                Label isLoadedLabel = new Label();
                mv.visitJumpInsn(Opcodes.IFEQ, isLoadedLabel);
                mv.visitInsn(Opcodes.ICONST_1);
                mv.visitInsn(Opcodes.IRETURN);
                mv.visitLabel(isLoadedLabel);
            } else if (this.property instanceof AssociationProperty) {
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        this.objectModelImplInternalName, 
                        this.property.getGetterName(), 
                        "()" + ASM.getDescriptor(this.property.getReturnClass()),
                        false);
                mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(LazinessManageable.class));
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        ASM.getInternalName(LazinessManageable.class), 
                        "isLoaded", 
                        "()Z",
                        true);
                Label isLoadedLabel = new Label();
                mv.visitJumpInsn(Opcodes.IFNE, isLoadedLabel);
                mv.visitInsn(Opcodes.ICONST_1);
                mv.visitInsn(Opcodes.IRETURN);
                mv.visitLabel(isLoadedLabel);
            }
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateUnload(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "unload", 
                    '(' + this.objectModelImplDescriptor + ")V", 
                    null,
                    null);
            mv.visitCode();
            if (isDeferrableScalarProperty(this.property)) {
                final Label isNotFrozenLabel = new Label();
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        this.objectModelImplInternalName, 
                        this.property.getName() + OBJECT_MODEL_IMPL_FROZEN_CONTEXT_POSTFIX, 
                        ASM.getDescriptor(FrozenContext.class));
                mv.visitJumpInsn(Opcodes.IFNULL, isNotFrozenLabel);
                ASM.visitNewObjectWithoutParameters(mv, ASM.getInternalName(PropertyIsFrozenException.class));
                mv.visitInsn(Opcodes.ATHROW);
                mv.visitLabel(isNotFrozenLabel);
                
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitFieldInsn(
                        Opcodes.GETFIELD,
                        this.objectModelImplInternalName,
                        this.property.getName() + OBJECT_MODEL_IMPL_STATE_POSTFIX,
                        "I");
                mv.visitInsn(OPCODE_CONST_UNLOADED);
                mv.visitInsn(Opcodes.IOR);
                mv.visitFieldInsn(
                        Opcodes.PUTFIELD,
                        this.objectModelImplInternalName,
                        this.property.getName() + OBJECT_MODEL_IMPL_STATE_POSTFIX,
                        "I");
            } else if (this.property instanceof AssociationProperty) {
                ASM.visitNewObjectWithoutParameters(mv, ASM.getInternalName(UnsupportedOperationException.class));
                mv.visitInsn(Opcodes.ATHROW);
            }
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateContravarianceHandleInsns(MethodVisitor mv, int methodType) {
            String methodName;
            String methodDesc;
            int returnCode;
            switch (methodType) {
            case MT_IS_DISABLED:
                methodName = "isDisabled";
                methodDesc = "(I)Z";
                returnCode = Opcodes.IRETURN;
                break;
            case MT_DISABLE:
                methodName = "disable";
                methodDesc = "(I)V";
                returnCode = Opcodes.RETURN;
                break;
            case MT_ENABLE:
                methodName = "enable";
                methodDesc = "(I)V";
                returnCode = Opcodes.RETURN;
                break;
            default:
                throw new AssertionError();
            }
            AssociationProperty covarianceProperty = ((AssociationProperty)this.property).getCovarianceProperty();
            visitGetObjectModel(
                    mv, 
                    covarianceProperty.getDeclaringObjectModelMetadata(),
                    new Action<MethodVisitor>() {
                        @Override
                        public void run(MethodVisitor mv) {
                            mv.visitVarInsn(Opcodes.ALOAD, 1);
                            mv.visitMethodInsn(
                                    Opcodes.INVOKEINTERFACE, 
                                    ASM.getInternalName(ObjectModel.class), 
                                    "getOwner", 
                                    "()Ljava/lang/Object;",
                                    true);
                        }
                    }
            );
            mv.visitLdcInsn(covarianceProperty.getId());
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    ASM.getInternalName(ObjectModel.class), 
                    methodName, 
                    methodDesc,
                    true);
            mv.visitInsn(returnCode);
        }
    }
    
    protected class AssociatedEndpointImplGenerator {
        
        private AssociationProperty associationProperty;
        
        private Class<?> superClass;
        
        private Class<?> superRootDataClass;
        
        private String className;
        
        private String internalName;
        
        private String objectModelImplInternalName;
        
        protected AssociatedEndpointImplGenerator(AssociationProperty associationProperty) {
            this.associationProperty = associationProperty;
            this.superClass = this.safeGetSuperClass();
            this.superRootDataClass = this.getSuperRootDataClass(this.superClass);
            this.className = associationEndpointImplClassName(associationProperty, false);
            this.internalName = this.className.replace('.', '/');
            this.objectModelImplInternalName = 
                    objectModelImplClassName(
                            associationProperty.getDeclaringObjectModelMetadata()
                    ).replace('.', '/');
            this.generate();
        }
        
        protected final AssociationProperty getAssociationProperty() {
            return this.associationProperty;
        }
    
        protected final Class<?> getSuperClass() {
            return this.superClass;
        }
    
        protected final Class<?> getSuperRootDataClass() {
            return superRootDataClass;
        }

        protected final String getClassName() {
            return this.className;
        }
    
        protected final String getInternalName() {
            return this.internalName;
        }
    
        protected final String getObjectModelImplInternalName() {
            return this.objectModelImplInternalName;
        }
    
        protected Class<?> determineSuperClass() {
            return ASSOCIATED_END_RESOLVER_MAP.get(this.associationProperty.getStandardReturnClass());
        }
        
        protected void generateAdditionalMembers(ClassVisitor cv) {
            
        }
        
        private Class<?> safeGetSuperClass() {
            Class<?> clazz = this.determineSuperClass();
            if (clazz == null) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().getSuperClassForAssociatedEndpointMustReturnNonNull(this.getClass())
                );
            }
            if (!AssociatedEndpoint.class.isAssignableFrom(clazz)) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().getSuperClassForAssociatedEndpointMustReturnEndpoint(
                                this.getClass(), AssociatedEndpoint.class
                        )
                );
            }
            return clazz;
        }
        
        private void generate() {
            Action<ClassVisitor> cvAction = new Action<ClassVisitor>() {
                @Override
                public void run(ClassVisitor cv) {
                    AssociatedEndpointImplGenerator.this.generate(cv);
                }
            };
            Class<?> objectModelClass = 
                    this
                    .associationProperty
                    .getDeclaringObjectModelMetadata()
                    .getObjectModelClass();
            ASM.loadDynamicClass(
                    objectModelClass.getClassLoader(), 
                    this.className, 
                    objectModelClass.getProtectionDomain(),
                    cvAction);
        }
        
        private void generate(ClassVisitor cv) {
            
            visitAssocationEndpointNestedClasses(cv, this.associationProperty);
            if (this.associationProperty.isCollection()) {
                new AssociationEndpointRootDataGenerator(this);
            }
            
            String oppositeInternalName = 
                    this.associationProperty
                    .getReturnObjectModelMetadata()
                    .getOwnerClass()
                    .getName()
                    .replace('.', '/');
            Class<?> ownerClass = this.associationProperty.getDeclaringObjectModelMetadata().getOwnerClass();
            
            cv.visit(
                    Opcodes.V1_7, 
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, 
                    this.internalName, 
                    null, 
                    ASM.getInternalName(this.superClass), 
                    new String[] { 
                            ASM.getInternalName(AssociatedEndpointDescriptor.class),
                            ASM.getInternalName(AssociatedEndpointIO.class),
                    });
            
            cv.visitField(
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, 
                    ASSOCIATED_END_IMPL_OWNER_OBJECT_MODEL_FACTORY, 
                    ASM.getDescriptor(ObjectModelFactory.class), 
                    null,
                    null)
                    .visitEnd();
            
            cv.visitField(
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, 
                    ASSOCIATED_END_IMPL_KEY_OBJECT_MODEL_FACTORY, 
                    ASM.getDescriptor(ObjectModelFactory.class), 
                    null,
                    null)
                    .visitEnd();
            
            cv.visitField(
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, 
                    ASSOCIATED_END_IMPL_RETURN_OBJECT_MODEL_FACTORY, 
                    ASM.getDescriptor(ObjectModelFactory.class), 
                    null,
                    null)
                    .visitEnd();
            
            cv.visitField(
                    Opcodes.ACC_PROTECTED | Opcodes.ACC_FINAL, 
                    OWNER, 
                    ASM.getDescriptor(ownerClass), 
                    null,
                    null)
                    .visitEnd();
            
            cv.visitField(
                    Opcodes.ACC_PROTECTED | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, 
                    ASSOCIATED_END_IMPL_ASSOCIATION_PROPERTY, 
                    ASM.getDescriptor(AssociationProperty.class),
                    null,
                    null)
                    .visitEnd();
            
            this.generateInit(cv, ownerClass);
            this.generateGetOwnerBridge(cv, ownerClass);
            this.generateGetOwner(cv, ownerClass);
            this.generateOnGetOppositeEndpointType(cv);
            this.generateOnGetOppositeEndpointBridge(cv, oppositeInternalName);
            this.generateOnGetOppositeEndpoint(cv);
            if (Map.class.isAssignableFrom(this.associationProperty.getReturnClass())) {
                cv.visitField(
                        Opcodes.ACC_PROTECTED | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, 
                        ASSOCIATED_END_IMPL_KEY_UNIFIED_COMPARATOR, 
                        ASM.getDescriptor(UnifiedComparator.class), 
                        null, 
                        null
                ).visitEnd();
            }
            if (this.associationProperty.isCollection()) {
                cv.visitField(
                        Opcodes.ACC_PROTECTED | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, 
                        ASSOCIATED_END_IMPL_UNIFIED_COMPARATOR, 
                        ASM.getDescriptor(UnifiedComparator.class), 
                        null, 
                        null
                ).visitEnd();
            }
            if (this.associationProperty.isCollection()) {
                this.generateCreateRootData(cv);
            }
            this.generateGetAssociationProperty(cv);
            this.generateGetOwnerObjectModelFactory(cv);
            this.generateGetKeyObjectModelFactory(cv);
            this.generateGetReturnObjectModelFactory(cv);
            this.generateAdditionalMembers(cv);         
            this.generateClinit(cv);
            this.generateIOMethods(cv);
            this.generateWriteReplace(cv);
            this.generateKeyObjectModelBridge(cv);
            this.generateKeyObjectModel(cv);
            cv.visitEnd();
        }

        private void generateClinit(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_STATIC, 
                    "<clinit>", 
                    "()V", 
                    null, 
                    null);
            mv.visitCode();
            mv.visitLdcInsn(Type.getType(ObjectModelFactoryProvider.this.getClass()));
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASM.getInternalName(ObjectModelFactoryProvider.class), 
                    "of", 
                    "(Ljava/lang/Class;)" + ASM.getDescriptor(ObjectModelFactoryProvider.class),
                    false);
            mv.visitVarInsn(Opcodes.ASTORE, 0);
            
            visitGetObjectModelFactory(mv, this.associationProperty.getDeclaringObjectModelMetadata());
            mv.visitFieldInsn(
                    Opcodes.PUTSTATIC,
                    this.internalName,
                    ASSOCIATED_END_IMPL_OWNER_OBJECT_MODEL_FACTORY,
                    ASM.getDescriptor(ObjectModelFactory.class));
            
            if (this.associationProperty.getKeyObjectModelMetadata() != null) {
                visitGetObjectModelFactory(
                        mv, 
                        this
                        .associationProperty
                        .getKeyObjectModelMetadata()
                );
                mv.visitFieldInsn(
                        Opcodes.PUTSTATIC,
                        this.internalName,
                        ASSOCIATED_END_IMPL_KEY_OBJECT_MODEL_FACTORY,
                        ASM.getDescriptor(ObjectModelFactory.class));
            }
            
            visitGetObjectModelFactory(
                    mv, 
                    this
                    .associationProperty
                    .getReturnObjectModelMetadata()
            );
            mv.visitFieldInsn(
                    Opcodes.PUTSTATIC,
                    this.internalName,
                    ASSOCIATED_END_IMPL_RETURN_OBJECT_MODEL_FACTORY,
                    ASM.getDescriptor(ObjectModelFactory.class));
            
            mv.visitLdcInsn(Type.getType(this.associationProperty.getDeclaringObjectModelMetadata().getOwnerClass()));
            ObjectModelFactoryProvider.this.getMetadataFactory().generateGetMetadata(mv);
            mv.visitLdcInsn(this.associationProperty.getId());
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    ASM.getInternalName(ObjectModelMetadata.class), 
                    "getAssociationProperty", 
                    "(I)" + ASM.getDescriptor(AssociationProperty.class),
                    true);
            mv.visitFieldInsn(
                    Opcodes.PUTSTATIC, 
                    this.internalName, 
                    ASSOCIATED_END_IMPL_ASSOCIATION_PROPERTY, 
                    ASM.getDescriptor(AssociationProperty.class));
            if (Map.class.isAssignableFrom(this.associationProperty.getReturnClass())) {
                mv.visitFieldInsn(
                        Opcodes.GETSTATIC, 
                        this.internalName, 
                        ASSOCIATED_END_IMPL_ASSOCIATION_PROPERTY, 
                        ASM.getDescriptor(AssociationProperty.class));
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        ASM.getInternalName(AssociationProperty.class), 
                        "getKeyUnifiedComparator", 
                        "()" + ASM.getDescriptor(UnifiedComparator.class),
                        true);
                mv.visitFieldInsn(
                        Opcodes.PUTSTATIC, 
                        this.internalName, 
                        ASSOCIATED_END_IMPL_KEY_UNIFIED_COMPARATOR, 
                        ASM.getDescriptor(UnifiedComparator.class));
            }
            if (this.associationProperty.isCollection()) {
                mv.visitFieldInsn(
                        Opcodes.GETSTATIC, 
                        this.internalName, 
                        ASSOCIATED_END_IMPL_ASSOCIATION_PROPERTY, 
                        ASM.getDescriptor(AssociationProperty.class));
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        ASM.getInternalName(AssociationProperty.class), 
                        "getCollectionUnifiedComparator", 
                        "()" + ASM.getDescriptor(UnifiedComparator.class),
                        true);
                mv.visitFieldInsn(
                        Opcodes.PUTSTATIC, 
                        this.internalName, 
                        ASSOCIATED_END_IMPL_UNIFIED_COMPARATOR, 
                        ASM.getDescriptor(UnifiedComparator.class));
            }
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateInit(ClassVisitor cv, Class<?> ownerClass) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "<init>", 
                    '(' +
                    ASM.getDescriptor(
                        this
                        .associationProperty
                        .getDeclaringObjectModelMetadata()
                        .getOwnerClass()
                    ) +
                    ")V", 
                    null,
                    null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    ASM.getInternalName(this.superClass), 
                    "<init>", 
                    "()V",
                    false);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitFieldInsn(
                    Opcodes.PUTFIELD, 
                    ASM.getInternalName(this.internalName), 
                    OWNER, 
                    ASM.getDescriptor(ownerClass));
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateGetOwnerBridge(ClassVisitor cv, Class<?> ownerClass) {
            MethodVisitor mv;
            mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_BRIDGE, 
                    "getOwner", 
                    "()Ljava/lang/Object;", 
                    null,
                    null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    this.internalName, 
                    "getOwner", 
                    "()" + ASM.getDescriptor(ownerClass),
                    false);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateGetOwner(ClassVisitor cv, Class<?> ownerClass) {
            MethodVisitor mv;
            mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "getOwner", 
                    "()" + ASM.getDescriptor(ownerClass), 
                    null,
                    null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.internalName, 
                    OWNER, 
                    ASM.getDescriptor(ownerClass));
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    
        private void generateGetAssociationProperty(ClassVisitor cv) {
            MethodVisitor mv;
            mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "getAssociationProperty", 
                    "()" + ASM.getDescriptor(AssociationProperty.class), 
                    null,
                    null);
            mv.visitCode();
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    this.internalName, 
                    ASSOCIATED_END_IMPL_ASSOCIATION_PROPERTY, 
                    ASM.getDescriptor(AssociationProperty.class));
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateOnGetOppositeEndpointType(ClassVisitor cv) {
            MethodVisitor mv;
            for (Class<?> clazz = this.superClass; clazz != null; clazz = clazz.getSuperclass()) {
                Method method = null;
                try {
                    method = clazz.getDeclaredMethod("onGetOppositeEndpointType");
                } catch (NoSuchMethodException ex) {
                    // Do nothing
                }
                if (method != null &&
                        method.getReturnType().isAssignableFrom(AssociatedEndpointType.class) &&
                        !Modifier.isStatic(method.getModifiers())) {
                    if (!Modifier.isFinal(method.getModifiers())) {
                        mv = cv.visitMethod(
                                Opcodes.ACC_PUBLIC, 
                                "onGetOppositeEndpointType", 
                                "()" + ASM.getDescriptor(AssociatedEndpointType.class), 
                                null,
                                null);
                        mv.visitCode();
                        if (this.associationProperty.getOppositeProperty() == null) {
                            mv.visitInsn(Opcodes.ACONST_NULL);
                        } else {
                            mv.visitFieldInsn(
                                    Opcodes.GETSTATIC, 
                                    ASM.getInternalName(AssociatedEndpointType.class), 
                                    this.associationProperty.getOppositeProperty().getAssociatedEndpointType().name(), 
                                    ASM.getDescriptor(AssociatedEndpointType.class));
                        }
                        mv.visitInsn(Opcodes.ARETURN);
                        mv.visitMaxs(0, 0);
                    }
                    break;
                }
            }
        }

        private void generateOnGetOppositeEndpointBridge(ClassVisitor cv, String oppositeInternalName) {
            MethodVisitor mv;
            mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_BRIDGE, 
                    "onGetOppositeEndpoint", 
                    "(Ljava/lang/Object;)" + ASM.getDescriptor(AssociatedEndpoint.class), 
                    null,
                    null);
            mv.visitCode();
            if (this.associationProperty.getOppositeProperty() == null) {
                ASM.visitNewObjectWithoutParameters(mv, ASM.getInternalName(UnsupportedOperationException.class));
                mv.visitInsn(Opcodes.ATHROW);
            } else {
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitTypeInsn(
                        Opcodes.CHECKCAST, 
                        oppositeInternalName);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        this.internalName, 
                        "onGetOppositeEndpoint", 
                        "(L" +
                        oppositeInternalName +
                        ";)" +
                        ASM.getDescriptor(AssociatedEndpoint.class),
                        false);
                mv.visitInsn(Opcodes.ARETURN);
            }
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateOnGetOppositeEndpoint(ClassVisitor cv) {
            if (this.associationProperty.getOppositeProperty() == null) {
                return;
            }
            String oppositeInternalName =
                    this.associationProperty
                    .getOppositeProperty()
                    .getDeclaringObjectModelMetadata()
                    .getOwnerClass()
                    .getName()
                    .replace('.', '/');
            MethodVisitor mv;
            mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "onGetOppositeEndpoint", 
                    "(L" +
                    oppositeInternalName +
                    ";)" + 
                    ASM.getDescriptor(AssociatedEndpoint.class), 
                    null,
                    null);
            mv.visitCode();
            ObjectModelFactoryProvider.visitGetObjectModel(
                    mv, 
                    this.associationProperty.getOppositeProperty().getDeclaringObjectModelMetadata(), 
                    1);
            mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(ObjectModel.class));
            mv.visitLdcInsn(this.associationProperty.getOppositeProperty().getId());
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    ObjectModel.class.getName().replace('.', '/'), 
                    "getAssociation", 
                    "(I)" + ASM.getDescriptor(AssociatedEndpoint.class),
                    true);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateCreateRootData(ClassVisitor cv) {
            String rootDataInternalName = 
                    associationEndpointRootDataClassName(this.associationProperty, false).replace('.', '/');
            MethodVisitor mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PROTECTED, 
                    "createRootData", 
                    "()" + ASM.getDescriptor(this.superRootDataClass), 
                    null, 
                    null);
            mv.visitCode();
            mv.visitTypeInsn(Opcodes.NEW, rootDataInternalName);
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    rootDataInternalName, 
                    "<init>", 
                    "()V",
                    false);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateGetOwnerObjectModelFactory(ClassVisitor cv) {
            MethodVisitor mv;
            mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "getOwnerObjectModelFactory", 
                    "()" + ASM.getDescriptor(ObjectModelFactory.class), 
                    null,
                    null);
            mv.visitCode();
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    this.internalName, 
                    ASSOCIATED_END_IMPL_OWNER_OBJECT_MODEL_FACTORY, 
                    ASM.getDescriptor(ObjectModelFactory.class));
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateGetKeyObjectModelFactory(ClassVisitor cv) {
            MethodVisitor mv;
            mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "getKeyObjectModelFactory", 
                    "()" + ASM.getDescriptor(ObjectModelFactory.class), 
                    null,
                    null);
            mv.visitCode();
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    this.internalName, 
                    ASSOCIATED_END_IMPL_KEY_OBJECT_MODEL_FACTORY, 
                    ASM.getDescriptor(ObjectModelFactory.class));
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateGetReturnObjectModelFactory(ClassVisitor cv) {
            MethodVisitor mv;
            mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "getReturnObjectModelFactory", 
                    "()" + ASM.getDescriptor(ObjectModelFactory.class), 
                    null,
                    null);
            mv.visitCode();
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    this.internalName, 
                    ASSOCIATED_END_IMPL_RETURN_OBJECT_MODEL_FACTORY, 
                    ASM.getDescriptor(ObjectModelFactory.class));
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateIOMethods(ClassVisitor cv) {
            
            Class<?> rawRootDataType = 
                    Map.class.isAssignableFrom(this.superClass) ?
                    SUPER_MAP_ROOT_DATA_TYPE : 
                    SUPER_COLLECTION_ROOT_DATA_TYPE;
            
            XMethodVisitor mv = ASM.visitMethod(
                    cv,
                    Opcodes.ACC_PUBLIC, 
                    "write", 
                    "(Ljava/io/ObjectOutputStream;)V", 
                    null,
                    new String[] { ASM.getInternalName(IOException.class) });
            mv.visitCode();
            if (ReferenceImpl.class.isAssignableFrom(this.superClass)) { 
                if (IndexedReferenceImpl.class.isAssignableFrom(this.superClass)) {
                    mv.visitVarInsn(Opcodes.ALOAD, 1);
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitFieldInsn(
                            Opcodes.GETFIELD, 
                            ASM.getInternalName(ReferenceImpl.class), 
                            "index", 
                            "I");
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL,
                            "java/io/ObjectOutputStream", 
                            "writeInt",
                            "(I)V",
                            false);
                } else if (KeyedReferenceImpl.class.isAssignableFrom(this.superClass)) {
                    mv.visitVarInsn(Opcodes.ALOAD, 1);
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitFieldInsn(
                            Opcodes.GETFIELD, 
                            ASM.getInternalName(ReferenceImpl.class), 
                            "key", 
                            "Ljava/lang/Object;");
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL,
                            "java/io/ObjectOutputStream", 
                            "writeObject",
                            "(Ljava/lang/Object;)V",
                            false);
                }
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        ASM.getInternalName(ReferenceImpl.class), 
                        "value", 
                        "Ljava/lang/Object;");
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/io/ObjectOutputStream", 
                        "writeObject",
                        "(Ljava/lang/Object;)V",
                        false);
            } else {
                Class<?> collectionType = 
                        AbstractWrapperXMap.class.isAssignableFrom(this.superClass) ? 
                                XMap.class : 
                                XCollection.class;
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        this.internalName, 
                        "getRootData", 
                        "()" + ASM.getDescriptor(rawRootDataType),
                        false);
                mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(rawRootDataType));
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        ASM.getInternalName(rawRootDataType), 
                        "getBase", 
                        "()" + ASM.getDescriptor(collectionType),
                        false);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/io/ObjectOutputStream", 
                        "writeObject",
                        "(Ljava/lang/Object;)V",
                        false);
            }
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
            
            mv = ASM.visitMethod(
                    cv,
                    Opcodes.ACC_PUBLIC, 
                    "read", 
                    "(Ljava/io/ObjectInputStream;)V", 
                    null,
                    new String[] { 
                            ASM.getInternalName(IOException.class), 
                            ASM.getInternalName(ClassNotFoundException.class) 
                    });
            mv.visitCode();
            if (ReferenceImpl.class.isAssignableFrom(this.superClass)) { 
                if (IndexedReferenceImpl.class.isAssignableFrom(this.superClass)) {
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitVarInsn(Opcodes.ALOAD, 1);
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL,
                            "java/io/ObjectInputStream", 
                            "readInt",
                            "()I",
                            false);
                    mv.visitFieldInsn(
                            Opcodes.PUTFIELD, 
                            ASM.getInternalName(ReferenceImpl.class), 
                            "index", 
                            "I");
                } else if (KeyedReferenceImpl.class.isAssignableFrom(this.superClass)) {
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitVarInsn(Opcodes.ALOAD, 1);
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL,
                            "java/io/ObjectInputStream", 
                            "readObject",
                            "()Ljava/lang/Object;",
                            false);
                    mv.visitFieldInsn(
                            Opcodes.PUTFIELD, 
                            ASM.getInternalName(ReferenceImpl.class), 
                            "key", 
                            "Ljava/lang/Object;");
                }
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/io/ObjectInputStream", 
                        "readObject",
                        "()Ljava/lang/Object;",
                        false);
                mv.visitFieldInsn(
                        Opcodes.PUTFIELD, 
                        ASM.getInternalName(ReferenceImpl.class), 
                        "value", 
                        "Ljava/lang/Object;");
            } else {
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/io/ObjectInputStream", 
                        "readObject",
                        "()Ljava/lang/Object;",
                        false);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        this.internalName, 
                        "replace", 
                        "(Ljava/lang/Object;)V",
                        false);
            }
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateWriteReplace(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PROTECTED, 
                    "writeReplace", 
                    "()Ljava/lang/Object;", 
                    null,
                    new String[] { ASM.getInternalName(ObjectStreamException.class) });
            mv.visitCode();
            mv.visitTypeInsn(Opcodes.NEW, ASM.getInternalName(AssociatedEndpointWritingReplacement.class));
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    ASM.getInternalName(AssociatedEndpointWritingReplacement.class),
                    "<init>",
                    '(' + ASM.getDescriptor(AssociatedEndpoint.class) + ")V",
                    false);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateKeyObjectModelBridge(ClassVisitor cv) {
            ObjectModelMetadata keyObjectModelMetadata = this.associationProperty.getKeyObjectModelMetadata();
            if (keyObjectModelMetadata == null) {
                return;
            }
            
            Class<?> ownerClass = keyObjectModelMetadata.getOwnerClass();
            MethodVisitor mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PROTECTED | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_BRIDGE | Opcodes.ACC_FINAL, 
                    "keyObjectModel", 
                    "(Ljava/lang/Object;)" + ASM.getDescriptor(ObjectModel.class), 
                    null,
                    null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitTypeInsn(
                    Opcodes.CHECKCAST, 
                    ASM.getInternalName(ownerClass)
            );
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    this.internalName, 
                    "keyObjectModel", 
                    '(' +
                    ASM.getDescriptor(ownerClass) +
                    ')' +
                    ASM.getDescriptor(ObjectModel.class),
                    false);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateKeyObjectModel(ClassVisitor cv) {
            ObjectModelMetadata keyObjectModelMetadata = this.associationProperty.getKeyObjectModelMetadata();
            if (keyObjectModelMetadata == null) {
                return;
            }
            MethodVisitor mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PROTECTED, 
                    "keyObjectModel", 
                    '(' +
                    ASM.getDescriptor(keyObjectModelMetadata.getOwnerClass()) +
                    ')' + 
                    ASM.getDescriptor(ObjectModel.class), 
                    null,
                    null);
            mv.visitCode();
            ObjectModelFactoryProvider.visitGetObjectModel(mv, keyObjectModelMetadata, 1);
            mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(ObjectModel.class));
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private Class<?> getSuperRootDataClass(Class<?> superAssociatedEndpointImplType) {
            if (this.associationProperty.isReference()) {
                return null;
            }
            while (AbstractWrapperXMap.class.isAssignableFrom(superAssociatedEndpointImplType)) {
                Class<?> rootDataClass = null;
                for (Class<?> nestedClass : superAssociatedEndpointImplType.getDeclaredClasses()) {
                    if ("RootData".equals(nestedClass.getSimpleName())) {
                        if (rootDataClass == null || rootDataClass.isAssignableFrom(nestedClass)) {
                            rootDataClass = nestedClass;
                        }
                    }
                }
                if (rootDataClass != null) {
                    return rootDataClass;
                }
                superAssociatedEndpointImplType = superAssociatedEndpointImplType.getSuperclass();
            }
            while (AbstractWrapperXCollection.class.isAssignableFrom(superAssociatedEndpointImplType)) {
                Class<?> rootDataClass = null;
                for (Class<?> nestedClass : superAssociatedEndpointImplType.getDeclaredClasses()) {
                    if ("RootData".equals(nestedClass.getSimpleName())) {
                        if (rootDataClass == null || rootDataClass.isAssignableFrom(nestedClass)) {
                            rootDataClass = nestedClass;
                        }
                    }
                }
                if (rootDataClass != null) {
                    return rootDataClass;
                }
                superAssociatedEndpointImplType = superAssociatedEndpointImplType.getSuperclass();
            }
            throw new AssertionError();
        }
    }

    private class AssociationEndpointRootDataGenerator {
        
        private AssociationProperty associationProperty;
        
        private Class<?> superClass;
        
        private String className;
        
        private String internalName;
        
        private String associatedEndpointImplInternalName;
        
        public AssociationEndpointRootDataGenerator(
                AssociatedEndpointImplGenerator associationEndpointImplGenerator) {
            this.associationProperty = associationEndpointImplGenerator.getAssociationProperty();
            this.superClass = associationEndpointImplGenerator.getSuperRootDataClass();
            this.className = 
                    associationEndpointRootDataClassName(associationProperty, false);
            this.internalName = this.className.replace('.', '/');
            this.associatedEndpointImplInternalName =
                    associationEndpointImplGenerator.getInternalName();
            this.generate();
        }
        
        protected void generateAdditionalMembers(ClassVisitor cv) {
            
        }
        
        private void generate() {
            Action<ClassVisitor> cvAction = new Action<ClassVisitor>() {            
                @Override
                public void run(ClassVisitor cv) {
                    AssociationEndpointRootDataGenerator.this.generate(cv);
                }           
            };
            Class<?> objectModelClass = this.associationProperty.getDeclaringObjectModelMetadata().getObjectModelClass();
            ASM.loadDynamicClass(
                    objectModelClass.getClassLoader(), 
                    this.className, 
                    objectModelClass.getProtectionDomain(), 
                    cvAction);
        }
        
        private void generate(ClassVisitor cv) {
            visitAssocationEndpointNestedClasses(cv, this.associationProperty);
            
            cv.visit(
                    Opcodes.V1_7, 
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, 
                    this.internalName, 
                    null, 
                    ASM.getInternalName(this.superClass), 
                    null);
            this.generateInit(cv);
            this.generateGetDefaultKeyUnifiedComparator(cv);
            this.generateGetDefaultUnifiedComparator(cv);
            this.generateAdditionalMembers(cv);
            cv.visitEnd();
        }
    
        private void generateInit(ClassVisitor cv) {
            XMethodVisitor mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PUBLIC, 
                    "<init>", 
                    "()V", 
                    null,
                    null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    ASM.getInternalName(this.superClass), 
                    "<init>", 
                    "()V",
                    false);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateGetDefaultKeyUnifiedComparator(ClassVisitor cv) {
            if (!Map.class.isAssignableFrom(this.associationProperty.getReturnClass())) {
                return;
            }
            
            MethodVisitor mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PROTECTED, 
                    "getDefaultKeyUnifiedComparator", 
                    "()" + ASM.getDescriptor(UnifiedComparator.class), 
                    null,
                    null);
            mv.visitCode();
            
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    this.associatedEndpointImplInternalName, 
                    ASSOCIATED_END_IMPL_KEY_UNIFIED_COMPARATOR, 
                    ASM.getDescriptor(UnifiedComparator.class));
            
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateGetDefaultUnifiedComparator(ClassVisitor cv) {
            if (!this.associationProperty.isCollection()) {
                return;
            }
            
            String defaultUnifiedCoparatorMethodName = 
                    Map.class.isAssignableFrom(this.associationProperty.getReturnClass()) ?
                    "getDefaultValueUnifiedComparator" :
                    "getDefaultUnifiedComparator";
            MethodVisitor mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PROTECTED, 
                    defaultUnifiedCoparatorMethodName, 
                    "()" + ASM.getDescriptor(UnifiedComparator.class), 
                    null,
                    null);
            mv.visitCode();
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    this.associatedEndpointImplInternalName, 
                    ASSOCIATED_END_IMPL_UNIFIED_COMPARATOR, 
                    ASM.getDescriptor(UnifiedComparator.class));
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }

    private class EmbeddedScalarListenerImplGenerator {
        
        private ScalarProperty scalarProperty;
        
        private String className;
        
        private String internalName;
        
        private String objectModelImplInternalName;
        
        private String objectModelImplDescriptor;
        
        public EmbeddedScalarListenerImplGenerator(ScalarProperty scalarProperty) {
            this.scalarProperty = scalarProperty;
            this.className = 
                    embeddedScalarListenerImplClassName(scalarProperty, false);
            this.internalName = this.className.replace('.', '/');
            this.objectModelImplInternalName = 
                    objectModelImplClassName(
                            scalarProperty.getDeclaringObjectModelMetadata()
                    ).replace('.', '/');
            this.objectModelImplDescriptor = 'L' + this.objectModelImplInternalName + ';';
            this.generate();
        }
    
        private void generate() {
            Class<?> objectModelClass = this.scalarProperty.getDeclaringObjectModelMetadata().getObjectModelClass();
            Action<ClassVisitor> cvAction = new Action<ClassVisitor>() {
                @Override
                public void run(ClassVisitor cv) {
                    EmbeddedScalarListenerImplGenerator.this.generate(cv);
                }
            };
            ASM.loadDynamicClass(
                    objectModelClass.getClassLoader(), 
                    this.className, 
                    objectModelClass.getProtectionDomain(),
                    cvAction);
        }
        
        private void generate(ClassVisitor cv) {
            cv.visit(
                    Opcodes.V1_7, 
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, 
                    this.internalName, 
                    null, 
                    "java/lang/Object", 
                    new String[] { ASM.getInternalName(ScalarListener.class) }
            );
            visitEmbeddedPropertyNestedClasses(cv, this.scalarProperty);
            new EmbddedScalarBubbledPropertyConverterImplGenerator(scalarProperty);
            cv.visitField(
                    Opcodes.ACC_PRIVATE, 
                    EMBEDDED_SCALAR_LISTENER_IMPL_PARENT_OBJECT_MODEL, 
                    this.objectModelImplDescriptor,
                    null, 
                    null
            )
            .visitEnd();
            this.generateInit(cv);
            this.generateModifying(cv);
            this.generateModified(cv);
            this.generateHashCode(cv);
            this.generateEquals(cv);
            cv.visitEnd();
        }
        
        private void generateInit(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    0, 
                    "<init>", 
                    '(' + this.objectModelImplDescriptor + ")V", 
                    null, 
                    null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    "java/lang/Object", 
                    "<init>", 
                    "()V",
                    false);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitFieldInsn(
                    Opcodes.PUTFIELD, 
                    this.internalName, 
                    EMBEDDED_SCALAR_LISTENER_IMPL_PARENT_OBJECT_MODEL, 
                    this.objectModelImplDescriptor);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateModifying(ClassVisitor cv) {
            
            XMethodVisitor mv = ASM.visitMethod(
                    cv,
                    Opcodes.ACC_PUBLIC, 
                    "modifying", 
                    '(' + 
                    ASM.getDescriptor(ScalarEvent.class) +
                    ")V", 
                    null, 
                    null);
            mv.visitCode();
            
            final int pomIndex = mv.aSlot("pom");
            final int ctxIndex = mv.aSlot("ctx");
            final int exIndex = mv.aSlot("ex");
            
            Label tryLabel = new Label();
            Label catchLabel = new Label();
            Label finallyLabel = new Label();
            mv.visitTryCatchBlock(tryLabel, catchLabel, catchLabel, null);
            
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.internalName, 
                    EMBEDDED_SCALAR_LISTENER_IMPL_PARENT_OBJECT_MODEL,
                    this.objectModelImplDescriptor);
            mv.visitVarInsn(Opcodes.ASTORE, pomIndex);
            
            mv.visitVarInsn(Opcodes.ALOAD, pomIndex);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD,
                    this.objectModelImplInternalName,
                    this.scalarProperty.getName() + OBJECT_MODEL_IMPL_FROZEN_CONTEXT_POSTFIX,
                    ASM.getDescriptor(FrozenContext.class));
            mv.visitVarInsn(Opcodes.ASTORE, ctxIndex);
            
            Action<MethodVisitor> finallyAction = new Action<MethodVisitor>() {
                @Override
                public void run(MethodVisitor mv) {
                    /*
                     * e
                     * .getAttributeContext(AttributeScope.LOCAL)
                     * .addAttribute({AK_FROZEN_CONTEXT}, ctx);
                     */
                    mv.visitVarInsn(Opcodes.ALOAD, 1);
                    mv.visitFieldInsn(
                            Opcodes.GETSTATIC, 
                            ASM.getInternalName(AttributeScope.class), 
                            AttributeScope.LOCAL.name(), 
                            ASM.getDescriptor(AttributeScope.class));
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL, 
                            ASM.getInternalName(ModificationEvent.class), 
                            "getAttributeContext", 
                            '(' + 
                            ASM.getDescriptor(AttributeScope.class) + 
                            ')' +
                            ASM.getDescriptor(EventAttributeContext.class),
                            false);
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitVarInsn(Opcodes.ALOAD, ctxIndex);
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL, 
                            ASM.getInternalName(AttributeContext.class), 
                            "addAttribute", 
                            "(Ljava/lang/Object;Ljava/lang/Object;)V",
                            false);
                    /*
                     * FrozenContext.resumeFreezing(
                     *      ctx,
                     *      this.{parentObbectModel}.getOwner()
                     * );
                     */
                    mv.visitVarInsn(Opcodes.ALOAD, ctxIndex);
                    mv.visitVarInsn(Opcodes.ALOAD, pomIndex);
                    mv.visitMethodInsn(
                            Opcodes.INVOKEINTERFACE, 
                            ASM.getInternalName(ObjectModel.class), 
                            "getOwner", 
                            "()Ljava/lang/Object;",
                            true);
                    mv.visitMethodInsn(
                            Opcodes.INVOKESTATIC, 
                            ASM.getInternalName(FrozenContext.class), 
                            "suspendFreezing", 
                            '(' +
                            ASM.getDescriptor(FrozenContext.class) +
                            "Ljava/lang/Object;)V",
                            false);
                }
            };
            
            mv.visitLabel(tryLabel);
            this.generateBubbleModifyingInsns(mv, pomIndex);
            mv.visitJumpInsn(Opcodes.GOTO, finallyLabel);
            
            mv.visitLabel(catchLabel);
            mv.visitVarInsn(Opcodes.ASTORE, exIndex);
            finallyAction.run(mv);
            mv.visitVarInsn(Opcodes.ALOAD, exIndex);
            mv.visitInsn(Opcodes.ATHROW);
            
            mv.visitLabel(finallyLabel);
            finallyAction.run(mv);
            
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateModified(ClassVisitor cv) {
            
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "modified", 
                    '(' + 
                    ASM.getDescriptor(ScalarEvent.class) +
                    ")V", 
                    null, 
                    null);
            mv.visitCode();
            
            final int exIndex = 2;
            
            Label tryLabel = new Label();
            Label catchLabel = new Label();
            Label finallyLabel = new Label();
            mv.visitTryCatchBlock(tryLabel, catchLabel, catchLabel, null);
            
            Action<MethodVisitor> finallyAction = new Action<MethodVisitor>() {
                @Override
                public void run(MethodVisitor mv) {
                    EmbeddedScalarListenerImplGenerator.this.generateBubbleModifiedInsns(mv);
                }
            };
            
            mv.visitLabel(tryLabel);
            /*
             * FrozenContext.resumeFreezing(
             *      e
             *      .getAttributeContext(AttributeScope.LOCAL)
             *      .removeAttribute({AK_FROZEN_CONTEXT})
             * );
             */
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    ASM.getInternalName(AttributeScope.class), 
                    AttributeScope.LOCAL.name(), 
                    ASM.getDescriptor(AttributeScope.class));
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    ASM.getInternalName(ModificationEvent.class), 
                    "getAttributeContext", 
                    '(' + 
                    ASM.getDescriptor(AttributeScope.class) + 
                    ')' +
                    ASM.getDescriptor(EventAttributeContext.class),
                    false);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    ASM.getInternalName(AttributeContext.class), 
                    "removeAttribute", 
                    "(Ljava/lang/Object;)Ljava/lang/Object;",
                    false);
            mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(FrozenContext.class));
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASM.getInternalName(FrozenContext.class), 
                    "resumeFreezing", 
                    '(' + ASM.getDescriptor(FrozenContext.class) + ")V",
                    false);
            mv.visitJumpInsn(Opcodes.GOTO, finallyLabel);
            
            mv.visitLabel(catchLabel);
            mv.visitVarInsn(Opcodes.ASTORE, exIndex);
            finallyAction.run(mv);
            mv.visitVarInsn(Opcodes.ALOAD, exIndex);
            mv.visitInsn(Opcodes.ATHROW);
            
            mv.visitLabel(finallyLabel);
            finallyAction.run(mv);
            
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateBubbleModifyingInsns(MethodVisitor mv, int parentObjectModelIndex) {
            String bubbledPropertyConverterImplInternalName =
                    embeddedBubbledPropertyConverterImplClassName(
                            this.scalarProperty, false
                    ).replace('.', '/');
            
            //? parentObjectModel = this.{parentObjectModel}
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.internalName, 
                    EMBEDDED_SCALAR_LISTENER_IMPL_PARENT_OBJECT_MODEL, 
                    this.objectModelImplDescriptor);
            mv.visitVarInsn(Opcodes.ASTORE, parentObjectModelIndex);
            
            
            //parentObjectModel.{executeModifying}(ScalarEvent.bubbleEvent(...));
            {
                //parentObjectModel.
                mv.visitVarInsn(Opcodes.ALOAD, parentObjectModelIndex);
                
                //parentObjectModel
                mv.visitVarInsn(Opcodes.ALOAD, parentObjectModelIndex);
                
                //new Cause(ObjectModelViewInfos.scalar(?), e);
                mv.visitTypeInsn(Opcodes.NEW, ASM.getInternalName(Cause.class));
                mv.visitInsn(Opcodes.DUP);
                mv.visitLdcInsn(this.scalarProperty.getId());
                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC, 
                        ASM.getInternalName(ObjectModelViewInfos.class), 
                        "scalar", 
                        "(I)" + ASM.getDescriptor(ObjectModelViewInfos.Scalar.class),
                        false);
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitMethodInsn(
                        Opcodes.INVOKESPECIAL, 
                        ASM.getInternalName(Cause.class), 
                        "<init>", 
                        '(' + 
                        ASM.getDescriptor(ViewInfo.class) + 
                        ASM.getDescriptor(ModificationEvent.class) + 
                        ")V",
                        false);
                
                //new ?{BubbedPropertyConverterImpl}(this.{parentObjectModel});
                mv.visitTypeInsn(Opcodes.NEW, bubbledPropertyConverterImplInternalName);
                mv.visitInsn(Opcodes.DUP);
                mv.visitVarInsn(Opcodes.ALOAD, parentObjectModelIndex);
                mv.visitMethodInsn(
                        Opcodes.INVOKESPECIAL, 
                        bubbledPropertyConverterImplInternalName, 
                        "<init>", 
                        '(' + ASM.getDescriptor(ObjectModel.class) + ")V",
                        false);
                
                //new ?{BubbedPropertyConverterImpl}(this.{parentObjectModel});
                mv.visitInsn(Opcodes.DUP);
                
                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC, 
                        ASM.getInternalName(ScalarEvent.class), 
                        "bubbleEvent", 
                        "(Ljava/lang/Object;" +
                        ASM.getDescriptor(Cause.class) +
                        ASM.getDescriptor(BubbledSharedPropertyConverter.class) +
                        ASM.getDescriptor(BubbledPropertyConverter.class) +
                        ')' +
                        ASM.getDescriptor(ScalarEvent.class),
                        false);
                
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        this.objectModelImplInternalName, 
                        OBJECT_MODEL_IMPL_EXECUTE_MODIFYING, 
                        '(' + ASM.getDescriptor(ScalarEvent.class) + ")V",
                        false);
            }
        }
        
        private void generateBubbleModifiedInsns(MethodVisitor mv) {
            
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.internalName, 
                    EMBEDDED_SCALAR_LISTENER_IMPL_PARENT_OBJECT_MODEL, 
                    this.objectModelImplDescriptor);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.internalName, 
                    EMBEDDED_SCALAR_LISTENER_IMPL_PARENT_OBJECT_MODEL, 
                    this.objectModelImplDescriptor);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    ASM.getInternalName(ModificationEvent.class), 
                    "getBubbledEvent", 
                    "(Ljava/lang/Object;)" + ASM.getDescriptor(ModificationEvent.class),
                    false);
            mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(ScalarEvent.class));
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    this.objectModelImplInternalName, 
                    OBJECT_MODEL_IMPL_EXECUTE_MODIFIED, 
                    '(' + ASM.getDescriptor(ScalarEvent.class) + ")V",
                    false);
        }
        
        private void generateHashCode(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "hashCode", 
                    "()I", 
                    null,
                    null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.internalName, 
                    EMBEDDED_SCALAR_LISTENER_IMPL_PARENT_OBJECT_MODEL, 
                    this.objectModelImplDescriptor);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASM.getInternalName(System.class), 
                    "identityHashCode", 
                    "(Ljava/lang/Object;)I",
                    false);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateEquals(ClassVisitor cv) {
            Label isNotSameLabel = new Label();
            Label isSameTypeLabel = new Label();
            Label successLabel = new Label();
            
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "equals", 
                    "(Ljava/lang/Object;)Z", 
                    null,
                    null);
            mv.visitCode();
            
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitJumpInsn(Opcodes.IF_ACMPNE, isNotSameLabel);
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitInsn(Opcodes.IRETURN);
            
            mv.visitLabel(isNotSameLabel);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitTypeInsn(Opcodes.INSTANCEOF, this.internalName);
            mv.visitJumpInsn(Opcodes.IFNE, isSameTypeLabel);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.IRETURN);
            
            mv.visitLabel(isSameTypeLabel);
            mv.visitVarInsn(Opcodes.ALOAD, 0);      
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.internalName, 
                    EMBEDDED_SCALAR_LISTENER_IMPL_PARENT_OBJECT_MODEL, 
                    this.objectModelImplDescriptor);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitTypeInsn(Opcodes.CHECKCAST, this.internalName);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.internalName, 
                    EMBEDDED_SCALAR_LISTENER_IMPL_PARENT_OBJECT_MODEL, 
                    this.objectModelImplDescriptor);
            mv.visitJumpInsn(Opcodes.IF_ACMPEQ, successLabel);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.IRETURN);
            
            mv.visitLabel(successLabel);
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitInsn(Opcodes.IRETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }
    
    private class EmbddedScalarBubbledPropertyConverterImplGenerator {
        
        private ScalarProperty scalarProperty;
        
        private String className;
        
        private String internalName;
                
        public EmbddedScalarBubbledPropertyConverterImplGenerator(ScalarProperty scalarProperty) {
            this.scalarProperty = scalarProperty;
            this.className = embeddedBubbledPropertyConverterImplClassName(scalarProperty, false);
            this.internalName = this.className.replace('.', '/');
            this.generate();
        }
        
        private void generate() {
            Class<?> objectModelClass = this.scalarProperty.getDeclaringObjectModelMetadata().getObjectModelClass();
            Action<ClassVisitor> cvAction = new Action<ClassVisitor>() {
                @Override
                public void run(ClassVisitor cv) {
                    EmbddedScalarBubbledPropertyConverterImplGenerator.this.generate(cv);
                }
            };
            ASM.loadDynamicClass(
                    objectModelClass.getClassLoader(), 
                    this.className, 
                    objectModelClass.getProtectionDomain(),
                    cvAction);
        }
        
        private void generate(ClassVisitor cv) {
            cv.visit(
                    Opcodes.V1_7, 
                    Opcodes.ACC_PRIVATE, 
                    this.internalName, 
                    null, 
                    "java/lang/Object", 
                    new String[] {
                        ASM.getInternalName(BubbledSharedPropertyConverter.class),
                        ASM.getInternalName(BubbledPropertyConverter.class) 
                    }
            );
            visitEmbeddedPropertyNestedClasses(cv, this.scalarProperty);
            cv
            .visitField(
                    Opcodes.ACC_PRIVATE, 
                    EMBEDDED_BUBBLED_PROPERTY_CONVERTER_IMPL_PARENT_OBJECT_MODEL, 
                    ASM.getDescriptor(ObjectModel.class), 
                    null, 
                    null
            )
            .visitEnd();
            this.generateInit(cv);
            this.generateConvertProperty(cv);
            this.generateConvertScalar(cv);
            cv.visitEnd();
        }
        
        private void generateInit(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    0,
                    "<init>",
                    '(' + ASM.getDescriptor(ObjectModel.class) + ")V",
                    null,
                    null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    "java/lang/Object",
                    "<init>",
                    "()V",
                    false);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitFieldInsn(
                    Opcodes.PUTFIELD, 
                    this.internalName, 
                    EMBEDDED_BUBBLED_PROPERTY_CONVERTER_IMPL_PARENT_OBJECT_MODEL, 
                    ASM.getDescriptor(ObjectModel.class));
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateConvertProperty(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC,
                    "convert",
                    '(' +
                    ASM.getDescriptor(BubbledSharedProperty.class) +
                    ")V",
                    null,
                    null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.internalName, 
                    EMBEDDED_BUBBLED_PROPERTY_CONVERTER_IMPL_PARENT_OBJECT_MODEL, 
                    ASM.getDescriptor(ObjectModel.class));
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    ASM.getInternalName(ObjectModel.class), 
                    "getObjectModelMetadata", 
                    "()" + ASM.getDescriptor(ObjectModelMetadata.class),
                    true);
            mv.visitLdcInsn(this.scalarProperty.getId());
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    ASM.getInternalName(ObjectModelMetadata.class), 
                    "getDeclaredScalarProperty", 
                    "(I)" + ASM.getDescriptor(ScalarProperty.class),
                    true);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    ASM.getInternalName(BubbledSharedProperty.class), 
                    "setValue", 
                    "(Ljava/lang/Object;)V",
                    false);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateConvertScalar(ClassVisitor cv) {
            final int scalarIndex = 2;
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC,
                    "convert",
                    '(' +
                    ASM.getDescriptor(BubbledProperty.class) +
                    ")V",
                    null,
                    null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.internalName, 
                    EMBEDDED_BUBBLED_PROPERTY_CONVERTER_IMPL_PARENT_OBJECT_MODEL, 
                    ASM.getDescriptor(ObjectModel.class));
            mv.visitLdcInsn(this.scalarProperty.getId());
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    ASM.getInternalName(ObjectModel.class), 
                    "getScalar", 
                    "(I)Ljava/lang/Object;",
                    true);
            mv.visitVarInsn(Opcodes.ASTORE, scalarIndex);
            for (int i = 0; i < 2; i++) {
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitVarInsn(Opcodes.ALOAD, scalarIndex);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        ASM.getInternalName(BubbledProperty.class), 
                        i == 0 ? "setValueToDetach" : "setValueToAttach", 
                        "(Ljava/lang/Object;)V",
                        false);
            }
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }
    
    public abstract class MetadataFactory {
        
        @SuppressWarnings("unchecked")
        public final <M extends ObjectModelMetadata> M getMetadata(Class<?> ownerClass) {
            ObjectModelMetadata objectModelMetadata = this.onGetMetadata(ownerClass);
            if (objectModelMetadata.getProvider() != this.provider()) {
                throw new IllegalArgumentException();
            }
            return (M)objectModelMetadata;
        }
        
        public final void generateGetMetadata(MethodVisitor mv) {
            this.onGenerateGetMetadata(mv);
            
            Label validLabel = new Label();
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    ASM.getInternalName(ObjectModelMetadata.class), 
                    "getProvider", 
                    "()" + ASM.getDescriptor(ObjectModelFactoryProvider.class),
                    true);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    "java/lang/Object", 
                    "getClass", 
                    "()Ljava/lang/Class;",
                    false);
            mv.visitLdcInsn(Type.getType(this.provider().getClass()));
            mv.visitJumpInsn(Opcodes.IF_ACMPEQ, validLabel);
            //TODO: add message
            ASM.visitNewObjectWithoutParameters(mv, ASM.getInternalName(IllegalArgumentException.class));
            mv.visitInsn(Opcodes.ATHROW);
            mv.visitLabel(validLabel);
        }
        
        final ObjectModelFactoryProvider provider() {
            return ObjectModelFactoryProvider.this;
        }
        
        protected abstract ObjectModelMetadata onGetMetadata(Class<?> ownerClass);
        
        protected abstract void onGenerateGetMetadata(MethodVisitor mv);
    }

    private static Map<String, String> createProviderNameMap() {
        
        Properties properties = new Properties();
        String configurationName = ObjectModelFactoryProvider.class.getSimpleName() + ".properties";
        try (InputStream inputStream = ObjectModelFactoryProvider.class.getResourceAsStream(configurationName)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException ex) {
            
        }
        
        Map<String, String> map = new HashMap<>(((properties.size() + 2) * 4 + 2) / 3);
        
        
        // Low priority: babyfish's hard code provider names
        map.put("jpa", "org.babyfish.hibernate.model.spi.HibernateObjectModelFactoryProvider");
        
        // Middle priority: user's global configuration
        for (Entry<Object, Object> entry : properties.entrySet()) {
            map.put((String)entry.getKey(), (String)entry.getValue());
        }
        
        // Highest priorty: empty provider name can NOT be overridden
        map.put("", ObjectModelFactoryProvider.class.getName());
        
        return map;
    }
    
    private interface Resource {
        
        String illegalProviderName(
                String providerName, 
                Collection<String> validProviderNames,
                Class<ObjectModelFactoryProvider> resourceLocation);
        
        String notExistingProviderName(
                String providerName, 
                String providerClassName,
                Class<ObjectModelFactoryProvider> resourceLocation);
        
        String illegalProviderType(
                Class<? extends ObjectModelFactoryProvider> providerType,
                Class<ObjectModelFactoryProvider> thisType);
        
        String abstractProviderType(
                Class<? extends ObjectModelFactoryProvider> providerType,
                Class<ObjectModelFactoryProvider> resourceLocation);

        String objectModelClassMissAnnotation(
                Class<?> objectModelClass,
                Class<ObjectModelDeclaration> objectModelDeclarationType);

        String objectModelClassMissOwner(Class<?> objectModelClass);

        String getSuperClassForAssociatedEndpointMustReturnNonNull(
                Class<? extends AssociatedEndpointImplGenerator> runtimeType);

        @SuppressWarnings("rawtypes")
        String getSuperClassForAssociatedEndpointMustReturnEndpoint(
                Class<? extends AssociatedEndpointImplGenerator> runtimeType,
                Class<AssociatedEndpoint> associatedEndpointType);
    }
    
    static {
        Map<Class<?>, Class<?>> map = new HashMap<>();
        
        map.put(NavigableMap.class, AssociatedNavigableMap.class);
        map.put(SortedMap.class, AssociatedNavigableMap.class);
        map.put(XOrderedMap.class, AssociatedOrderedMap.class);
        map.put(Map.class, AssociatedMap.class);
        
        map.put(NavigableSet.class, AssociatedNavigableSet.class);
        map.put(SortedSet.class, AssociatedNavigableSet.class);
        map.put(XOrderedSet.class, AssociatedOrderedSet.class);
        map.put(Set.class, AssociatedSet.class);
        
        map.put(List.class, AssociatedList.class);
        
        map.put(Collection.class, AssociatedCollection.class);
        
        map.put(KeyedReference.class, AssociatedKeyedReference.class);
        map.put(IndexedReference.class, AssociatedIndexedReference.class);
        map.put(Reference.class, AssociatedReference.class);
        
        ASSOCIATED_END_RESOLVER_MAP = MACollections.unmodifiable(map);
        
        Set<Class<?>> set = new HashSet<>();
        set.add(boolean.class);
        set.add(char.class);
        set.add(byte.class);
        set.add(short.class);
        set.add(int.class);
        set.add(long.class);
        set.add(float.class);
        set.add(double.class);
        set.add(Boolean.class);
        set.add(Character.class);
        set.add(Byte.class);
        set.add(Short.class);
        set.add(Integer.class);
        set.add(Long.class);
        set.add(Float.class);
        set.add(Double.class);
        set.add(BigInteger.class);
        set.add(BigDecimal.class);
        set.add(String.class);
        set.add(char[].class);
        set.add(byte[].class);
        //Enum is not a fixed type, it's a super type of all customized enum type, don't add it
        set.add(Date.class);
        set.add(Time.class);
        set.add(java.sql.Date.class);
        set.add(Timestamp.class);
        IMMEDIATE_APPENDABLE_SCALAR_TYPES = MACollections.unmodifiable(set);
    }
}
