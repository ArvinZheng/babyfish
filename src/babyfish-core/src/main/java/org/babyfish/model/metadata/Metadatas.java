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
package org.babyfish.model.metadata;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

import javax.swing.text.View;

import org.babyfish.association.AssociatedEndpointType;
import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.FrozenComparator;
import org.babyfish.collection.FrozenContext;
import org.babyfish.collection.FrozenEqualityComparator;
import org.babyfish.collection.HashCalculator;
import org.babyfish.collection.HashMap;
import org.babyfish.collection.HashSet;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.MACollection;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.MAList;
import org.babyfish.collection.MAMap;
import org.babyfish.collection.MANavigableMap;
import org.babyfish.collection.MANavigableSet;
import org.babyfish.collection.MAOrderedMap;
import org.babyfish.collection.MAOrderedSet;
import org.babyfish.collection.MASet;
import org.babyfish.collection.MASortedMap;
import org.babyfish.collection.MASortedSet;
import org.babyfish.collection.ReferenceEqualityComparator;
import org.babyfish.collection.TreeSet;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XCollection;
import org.babyfish.collection.XList;
import org.babyfish.collection.XMap;
import org.babyfish.collection.XNavigableMap;
import org.babyfish.collection.XNavigableSet;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.collection.XSet;
import org.babyfish.collection.XSortedMap;
import org.babyfish.collection.XSortedSet;
import org.babyfish.lang.Action;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.Func;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.lang.Nulls;
import org.babyfish.lang.Ref;
import org.babyfish.lang.UncheckedException;
import org.babyfish.lang.reflect.ClassInfo;
import org.babyfish.lang.reflect.GenericTypes;
import org.babyfish.lang.reflect.Strings;
import org.babyfish.lang.reflect.asm.ASM;
import org.babyfish.lang.reflect.asm.SlotAllocator;
import org.babyfish.lang.reflect.asm.XMethodVisitor;
import org.babyfish.model.ObjectModel;
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.metadata.spi.AssociationOnly;
import org.babyfish.model.metadata.spi.NonDeferrableOnly;
import org.babyfish.model.metadata.spi.NonDisabledOnly;
import org.babyfish.model.metadata.spi.OwnerComparatorWritingReplacement;
import org.babyfish.model.metadata.spi.OwnerEqualityComparatorWritingReplacement;
import org.babyfish.model.metadata.spi.OwnerReferenceOnly;
import org.babyfish.model.metadata.spi.ScalarOnly;
import org.babyfish.model.spi.ObjectModelFactoryProvider;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.Label;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.reference.IndexedReference;
import org.babyfish.reference.KeyedReference;
import org.babyfish.reference.MAIndexedReference;
import org.babyfish.reference.MAKeyedReference;
import org.babyfish.reference.MAReference;
import org.babyfish.reference.Reference;
import org.babyfish.util.Joins;
import org.babyfish.util.LazyResource;

/**
 * @author Tao Chen
 */
public class Metadatas {
    
    private static final LazyResource<Resource> LAZY_RESOURCE = LazyResource.of(Resource.class);
    
    private static final Map<Class<?>, ObjectModelMetadataImpl> CACHE = new WeakHashMap<>();
    
    private static final ReadWriteLock CACHE_LOCK = new ReentrantReadWriteLock();
    
    private static Class<?>[] EMPTY_CLASSES = new Class[0];
    
    private static String[] EMPTY_STRINGS = new String[0];

    private static final String NAME_POSTFIX = "92B8C17E_BF4E_4135_B596_5A76E0FEBF4E";
    
    private static final String IDENTIFIY_REGEX = "\\s*([\\$A-Za-z])([\\$A-Za-z0-9])*\\s*";
    
    private static final Pattern PROPERTY_NAMES_PATTERN = 
            Pattern.compile(IDENTIFIY_REGEX + "(," + IDENTIFIY_REGEX + ")*");
    
    private static final Pattern COMMA_PATTERN = Pattern.compile(",");
    
    private static final Set<Class<?>> VALID_ENDPOINT_TYPES;
    
    private static final Set<AssociatedEndpointTypePair> VALID_ASSOCIATED_END_TYPE_PAIRS;
    
    private static final String VALID_ASSOCIATED_END_TYPE_PAIRS_TEXT;
    
    private static final Set<String> INVALID_METHOD_POSTFIXS;
    
    private static final String INVALID_METHOD_POSTFIXS_TEXT;
    
    private static final Set<Class<?>> SIMPLE_TYPES;
    
    private static final Comparator<ScalarProperty> SCALAR_PROPERTY_COMPARATOR = new Comparator<ScalarProperty>() {
        @Override
        public int compare(ScalarProperty scalarProperty1, ScalarProperty scalarProperty2) {
            return scalarProperty1.getName().compareTo(scalarProperty2.getName());
        }
    };
    
    private static final EqualityComparator<ScalarProperty> SCALAR_PROPERTY_EQUALITY_COMPARATOR = 
            new EqualityComparator<ScalarProperty>() {
                @Override
                public int hashCode(ScalarProperty scalarProperty) {
                    return scalarProperty.getName().hashCode();
                }
        
                @Override
                public boolean equals(ScalarProperty scalarProperty1, ScalarProperty scalarProperty2) {
                    return scalarProperty1.getName().equals(scalarProperty2.getName());
                }
            };

    private Metadatas() {
        
    }
    
    public static ObjectModelFactoryProvider getObjectModelFactoryProvider(Class<?> ownerClass) {
        Lock lock;
        (lock = CACHE_LOCK.readLock()).lock();
        try {
            ObjectModelMetadata objectModelMetadata = CACHE.get(ownerClass);
            if (objectModelMetadata != null) {
                return ((ObjectModelMetadata)objectModelMetadata).getProvider();
            }
        } finally {
            lock.unlock();
        }
        
        OwnerEntry oe = getOwnerEntry(ownerClass);
        if (oe != null) {
            return oe.objectModelFactoryProvider;
        }
        return null;
    }
    
    public static boolean has(Class<?> ownerClass) {
        Lock lock;
        (lock = CACHE_LOCK.readLock()).lock();
        try {
            if (CACHE.containsKey(ownerClass)) {
                return true;
            }
        } finally {
            lock.unlock();
        }
        
        return getOwnerEntry(ownerClass) != null;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static ObjectModelMetadata of(Class<?> ownerClass) {
        
        Arguments.mustNotBeNull("ownerClass", ownerClass);
        ObjectModelMetadataImpl objectModelMetadataImpl;
        Lock lock;
        
        (lock = CACHE_LOCK.readLock()).lock();
        try {
            objectModelMetadataImpl = CACHE.get(ownerClass);
        } finally {
            lock.unlock();
        }
        
        if (objectModelMetadataImpl == null) { //1st checking
            (lock = CACHE_LOCK.writeLock()).lock();
            try {
                objectModelMetadataImpl = CACHE.get(ownerClass);
                if (objectModelMetadataImpl == null) { //2nd checking
                    Map<Class<?>, ObjectModelMetadataImpl> map;
                    /*
                     * The LinkedHashMap can keep the order of the parsing so that
                     * the error message is better to be understood.
                     */
                    Context context = new Context(CACHE);
                    objectModelMetadataImpl = cascadeCreateMetadatas(ownerClass, context);
                    map = (Map)context.metadatas;
                    CACHE.putAll(map);
                }
            } finally {
                lock.unlock();
            }
        }
        return (ObjectModelMetadata)objectModelMetadataImpl;
    }
    
    private static ObjectModelMetadataImpl cascadeCreateMetadatas(
            Class<?> ownerClass, 
            Context context) {
        
        Arguments.mustBeClass("ownerClass", ownerClass);
        OwnerEntry ownerEntry = Metadatas.getOwnerEntry(ownerClass);
        if (ownerEntry == null) {
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().noObjectModelClasses(
                            ownerClass, 
                            ObjectModelDeclaration.class));
        }
        
        ObjectModelMetadataImpl cachedObjectModelMetadataImpl = CACHE.get(ownerEntry.getDeclaringClass());
        if (cachedObjectModelMetadataImpl != null) {
            return cachedObjectModelMetadataImpl;
        }
        
        ObjectModelMetadataImpl metadataToReturn = 
            new ObjectModelMetadataImpl(ownerEntry, context);
        
        for (int i = 0; i < 1; i++) {
            for (ObjectModelMetadataImpl objectModelMetadataImpl : context.metadatas.values()) {
                if (objectModelMetadataImpl.parse(context)) {
                    i = -1;
                    break;
                }
            }
        }
        for (ObjectModelMetadataImpl objectModelMetadataImpl : context.metadatas.values()) {
            objectModelMetadataImpl.secondParse();
        }
        for (ObjectModelMetadataImpl objectModelMetadataImpl : context.metadatas.values()) {
            objectModelMetadataImpl.thirdParse();
        }
        for (ObjectModelMetadataImpl objectModelMetadataImpl : context.metadatas.values()) {
            objectModelMetadataImpl.fourthParse(context);
        }
        for (ObjectModelMetadataImpl objectModelMetadataImpl : context.metadatas.values()) {
            objectModelMetadataImpl.fifthParse(context);
        }
        return metadataToReturn;
    }
    
    private static void tryCollectJava8Type(Collection<Class<?>> c, String java8TypeName) {
        // If the JDK version >= 8, consider the types declared in "java.time" as simple types
        Class<?> clazz;
        try {
            clazz = Class.forName(java8TypeName);
        } catch (ClassNotFoundException ex) {
            return;
        }
        c.add(clazz);
    }
    
    @SuppressWarnings("unchecked")
    private static OwnerEntry getOwnerEntry(Class<?> ownerClass) {
        if (ownerClass == null || ownerClass == Object.class) {
            return null;
        }
        Arguments.mustBeClass("ownerClass", ownerClass);
        
        Class<? extends ObjectModel> objectModelInterface = null;
        ObjectModelFactoryProvider objectModelFactoryProvider = null;
        ObjectModelMode objectModelType = null;
        String declaredPropertiesOrder = null;
        for (Class<?> nestedClass : ownerClass.getDeclaredClasses()) {
            ObjectModelDeclaration declaration = nestedClass.getAnnotation(ObjectModelDeclaration.class);
            if (declaration != null) {
                if (!nestedClass.isInterface()) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().objectModelClassMustBeInterface(
                                    nestedClass, 
                                    ObjectModelDeclaration.class));
                }
                if (objectModelInterface != null) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().tooManyObjectModelClasses(
                                    ownerClass, 
                                    ObjectModelDeclaration.class));
                }
                for (Class<?> superInterface : nestedClass.getInterfaces()) {
                    if (superInterface != ObjectModel.class) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().badSuperInterfaceOfObjectModel(
                                        nestedClass,
                                        ObjectModel.class));
                    }
                }
                objectModelInterface = (Class<? extends ObjectModel>)nestedClass;
                objectModelFactoryProvider = ObjectModelFactoryProvider.of(declaration.provider());
                objectModelType = declaration.mode();
                declaredPropertiesOrder = Joins.join(declaration.declaredPropertiesOrder()).trim();
            }
        }
        
        Method staticMethodToGetObjectModel = null;
        for (Method method : ownerClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(StaticMethodToGetObjectModel.class)) {
                if (staticMethodToGetObjectModel != null) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().duplicatedStaticMethodToGetOM(
                                    staticMethodToGetObjectModel,
                                    method, 
                                    StaticMethodToGetObjectModel.class));
                }
                if (!Modifier.isStatic(method.getModifiers())) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().methodToGetOMMustBeStatic(
                                    method, 
                                    StaticMethodToGetObjectModel.class));
                }
                if (Modifier.isPrivate(method.getModifiers()) || 
                        Modifier.isProtected(method.getModifiers()) || 
                        Modifier.isPublic(method.getModifiers())) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().staticMethodToGetOMMustNotBeDefault(
                                    method, 
                                    StaticMethodToGetObjectModel.class));
                }
                staticMethodToGetObjectModel = method;
            }
        }
        if (objectModelInterface != null) {
            if (staticMethodToGetObjectModel == null) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().noStaticMethodToGetOM(
                                ownerClass, 
                                StaticMethodToGetObjectModel.class));
            }
            if (staticMethodToGetObjectModel.getParameterTypes().length != 1 ||
                    staticMethodToGetObjectModel.getParameterTypes()[0] != ownerClass ||
                    (ObjectModel.class != staticMethodToGetObjectModel.getReturnType() &&
                    objectModelInterface != staticMethodToGetObjectModel.getReturnType())) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().badStaticMethodToGetOM(
                                staticMethodToGetObjectModel, 
                                StaticMethodToGetObjectModel.class,
                                ownerClass,
                                ObjectModel.class,
                                objectModelInterface));
            }
            return new OwnerEntry(
                    staticMethodToGetObjectModel, 
                    objectModelInterface, 
                    objectModelFactoryProvider,
                    objectModelType,
                    declaredPropertiesOrder);
        }
        
        return getOwnerEntry(ownerClass.getSuperclass());
    }
    
    private static boolean isUnstableAndUncloneableScalarType(Class<?> scalarType) {
        return scalarType.isArray() || scalarType == Serializable.class;
    }
    
    private static class ObjectModelMetadataImpl implements ObjectModelMetadata {
        
        private static final long serialVersionUID = 2758237872834178789L;
        
        private static final int PS_FIRST = 1 << 0;
        
        private static final int PS_THIRD = 1 << 2;
        
        private Class<?> ownerClass;
        
        private Class<?> objectModelClass;
        
        private ObjectModelMetadataImpl superMetadata;
        
        private String staticMethodName;
        
        private ObjectModelFactoryProvider provider;
        
        private transient ObjectModelFactory<?> factory;
        
        private ObjectModelMode mode;
        
        private boolean disabilityAllowed;
        
        private int declaredPropertyBaseId;
        
        private XOrderedMap<String, AbstractPropertyImpl> declaredProperties;
        
        private XOrderedMap<String, ScalarPropertyImpl> declaredScalarProperties;
        
        private XOrderedMap<String, AbstractAssociationPropertyImpl> declaredAssociationProperties;
        
        private transient AbstractPropertyImpl[] declaredPropertyArr;
        
        private transient XOrderedMap<String, ScalarProperty> comparatorProperties;
        
        private transient XOrderedMap<String, AbstractPropertyImpl> properties;
        
        private transient XOrderedMap<String, ScalarPropertyImpl> scalarProperties;
        
        private transient XOrderedMap<String, AbstractAssociationPropertyImpl> associationProperties;
        
        private transient AbstractPropertyImpl[] propertyArr;
        
        private Map<int[], Comparator<?>> ownerComparatorMap;
        
        private ReadWriteLock ownerComparatorMapLock;
        
        private Map<int[], EqualityComparator<?>> ownerEqualityComparatorMap;
        
        private ReadWriteLock ownerEqualityComparatorMapLock;
        
        private Comparator<?> embeddableComparator;
        
        private EqualityComparator<?> embeddableEqualityComparator;
        
        private int parsedStates;
        
        ObjectModelMetadataImpl(Class<?> ownerClass, Context context) {
            Arguments.mustBeClass("ownerClass", ownerClass);
            OwnerEntry ownerEntry = Metadatas.getOwnerEntry(ownerClass);
            if (ownerEntry == null) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().noObjectModelClasses(
                                ownerClass, 
                                ObjectModelDeclaration.class));
            }
            if (ownerEntry.getDeclaringClass() != ownerClass) {
                throw new AssertionError();
            }
            this.init(ownerEntry, context);
        }
        
        public ObjectModelMetadataImpl(OwnerEntry ownerEntry, Context context) {
            this.init(ownerEntry, context);
        }
        
        private void init(OwnerEntry ownerEntry, Context context) {
            this.ownerClass = ownerEntry.getDeclaringClass();
            this.staticMethodName = ownerEntry.staticMethodToGetObjectModel.getName();
            this.objectModelClass = ownerEntry.objectModelInterface;
            this.provider = ownerEntry.objectModelFactoryProvider;
            this.mode = ownerEntry.objectModelType;
            
            /*
             * The metadata of super class or the metadatas or assocaition properties 
             * must be accessed or created after this "contextMap.put" statement. 
             * This is very important.
             */
            context.metadatas.put(ownerClass, this);
            
            ObjectModelMetadataImpl superMetadata = null;
            for (Class<?> superClass = ownerClass.getSuperclass(); 
            superClass != null && superClass != Object.class; 
            superClass = superClass.getSuperclass()) {
                if (Metadatas.getObjectModelFactoryProvider(superClass) != null) {
                    superMetadata = context.getMetadata(superClass);
                    if (superMetadata == null) {
                        superMetadata = new ObjectModelMetadataImpl(superClass, context);
                    }
                    if (this.provider != superMetadata.getProvider()) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().badDerivedProvider(
                                        ownerEntry.objectModelInterface, 
                                        ownerEntry.objectModelFactoryProvider.getClass(), 
                                        superMetadata.getObjectModelClass(), 
                                        superMetadata.getProvider().getClass()
                                )
                        );
                    }
                    if (superMetadata.getMode() != ObjectModelMode.ABSTRACT &&
                            superMetadata.getMode() != this.mode) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().badDerivedMode(
                                        ownerEntry.objectModelInterface, 
                                        this.mode,
                                        superMetadata.getObjectModelClass(),
                                        superMetadata.getMode()
                                )
                        );
                    }
                    this.superMetadata = superMetadata;
                    int declaredPropertyBaseId = 0;
                    while (superMetadata != null) {
                        declaredPropertyBaseId += superMetadata.declaredProperties.size();
                        superMetadata = superMetadata.superMetadata;
                    }
                    this.declaredPropertyBaseId = declaredPropertyBaseId;
                    break;
                }
            }
            if (this.superMetadata != null) {
                if (this.superMetadata.isDisabilityAllowed()) {
                    this.disabilityAllowed = this.superMetadata.disabilityAllowed;
                } else if (this.objectModelClass.isAnnotationPresent(AllowDisability.class)) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().noDisabilityInSuperObjectModel(
                                    this.objectModelClass,
                                    AllowDisability.class,
                                    this.superMetadata.getObjectModelClass()
                            )
                    );
                }
            } else {
                this.disabilityAllowed = this.objectModelClass.isAnnotationPresent(AllowDisability.class);
            }
            
            XOrderedMap<String, Method> declaredGetters = new LinkedHashMap<>();
            for (Method method : this.objectModelClass.getMethods()) {
                if (!method.getDeclaringClass().isAssignableFrom(ObjectModel.class)) {
                    if (method.getTypeParameters().length != 0) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().mustHaveNoTypeParameters(method));
                    }
                    for (Class<?> exceptionType : method.getExceptionTypes()) {
                        if (!RuntimeException.class.isAssignableFrom(exceptionType) &&
                                Error.class.isAssignableFrom(exceptionType)) {
                            throw new IllegalProgramException(
                                    LAZY_RESOURCE.get().canNotThrowCheckedException(method));
                        }
                    }
                    Type[] parameterTypes = method.getGenericParameterTypes();
                    if (parameterTypes.length > 1) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().badParameterCountOfMethod(method));
                    }
                    if (parameterTypes.length != 0) {
                        if (method.getReturnType() != void.class) {
                            throw new IllegalProgramException(
                                    LAZY_RESOURCE.get().mustReturnVoidBecauseThereIsOneParameter(method)
                            );
                        }
                        if (!method.getName().startsWith("set")) {
                            throw new IllegalProgramException(
                                    LAZY_RESOURCE.get().mustStartWithSet(method)
                            );
                        }
                        String postfix = method.getName().substring(3);
                        try {
                            method.getDeclaringClass().getDeclaredMethod("get" + postfix, EMPTY_CLASSES);
                        } catch (NoSuchMethodException ex) {
                            Class<?> scalarType = method.getParameterTypes()[0];
                            Method booleanGetter = null;
                            boolean isBoolean = scalarType == boolean.class || scalarType == Boolean.class;
                            if (isBoolean) {
                                try {
                                    booleanGetter = method.getDeclaringClass().getDeclaredMethod("is" + postfix, EMPTY_CLASSES);
                                } catch (NoSuchMethodException exAgain) {
                                    //don' nothing
                                }
                            }
                            if (booleanGetter == null) {
                                throw new IllegalProgramException(
                                        LAZY_RESOURCE.get().noGetterForScalarSetter(
                                                method, 
                                                parameterTypes[0], 
                                                method.getDeclaringClass(),
                                                (isBoolean ? "is(get)" : "get") + postfix
                                        )
                                );
                            }
                        }
                        if (method.getAnnotations().length != 0) {
                            throw new IllegalProgramException(
                                    LAZY_RESOURCE.get().invalidAnnotationsOfScalarSetter(method));
                        }
                    } else {
                        if (method.getReturnType() == void.class) {
                            throw new IllegalProgramException(
                                    LAZY_RESOURCE.get().mustNotReturnVoidBecauseThereIsNoParameters(method));
                        }
                        boolean isBooleanProperty = method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class;
                        if (!method.getName().startsWith("get") && 
                                (!isBooleanProperty || !method.getName().startsWith("is"))) {
                            throw new IllegalProgramException(
                                    LAZY_RESOURCE.get().mustStartWithGetOrIs(method));
                        }
                        String propertyName = 
                                method.getName().startsWith("get") ?
                                method.getName().substring(3) : 
                                method.getName().substring(2);
                        propertyName = Strings.toCamelCase(propertyName);
                        if (superMetadata != null && 
                                superMetadata.declaredProperties.containsKey(propertyName)) {
                            throw new IllegalProgramException(
                                    LAZY_RESOURCE.get().propertyHasBeenDeclarationInSuperMetadata(
                                            propertyName, 
                                            superMetadata.objectModelClass
                                    )
                            );
                        }
                        declaredGetters.put(propertyName, method);
                    }
                }
            }
            
            declaredGetters = this.orderedDeclaredGetters(declaredGetters, ownerEntry.declaredPropertiesOrder);
            XOrderedMap<String, AbstractPropertyImpl> declaredProperties = 
                new LinkedHashMap<>();
            XOrderedMap<String, ScalarPropertyImpl> declaredScalarProperties = 
                new LinkedHashMap<String, ScalarPropertyImpl>();
            XOrderedMap<String, AbstractAssociationPropertyImpl> declaredAssociationProperties = 
                new LinkedHashMap<>();
            int idSequence = this.declaredPropertyBaseId;
            for (Entry<String, Method> entry : declaredGetters.entrySet()) {
                AbstractPropertyImpl property = AbstractPropertyImpl.of(
                                context, 
                                this, 
                                entry.getValue(), 
                                idSequence++);
                String propertyName = entry.getKey();
                declaredProperties.put(propertyName, property);
                if (property instanceof ScalarPropertyImpl) {
                    declaredScalarProperties.put(propertyName, (ScalarPropertyImpl)property);
                } else {
                    declaredAssociationProperties.put(propertyName, (AbstractAssociationPropertyImpl)property);
                }
            }
                
            XOrderedMap<String, ScalarProperty> comparatorProperties = new LinkedHashMap<>();
            ReferenceComparisonRule referenceComparisonRuleAnnotation = 
                    this
                    .objectModelClass
                    .getAnnotation(ReferenceComparisonRule.class);
            if (referenceComparisonRuleAnnotation != null) {
                if (this.mode != ObjectModelMode.REFERENCE) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().invalidTypeLevelReferenceComparasionRule(
                                    this.objectModelClass, 
                                    this.mode, 
                                    ReferenceComparisonRule.class
                            )
                    );
                }
                String keyPropertyNames = Joins.join(referenceComparisonRuleAnnotation.value()).trim();
                if (!keyPropertyNames.isEmpty()) {
                    if (!PROPERTY_NAMES_PATTERN.matcher(keyPropertyNames).matches()) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().invalidTypeLevelReferenceComparasionRuleValue(
                                        this.objectModelClass, 
                                        ReferenceComparisonRule.class, 
                                        keyPropertyNames, 
                                        PROPERTY_NAMES_PATTERN.pattern()
                                )
                        );
                    }
                    for (String propertyName : COMMA_PATTERN.split(keyPropertyNames)) {
                        propertyName = propertyName.trim();
                        Property property = declaredProperties.get(propertyName);
                        /*
                         * Used loop, only access local variable "declaredProperties"
                         * because the field "this.properties" is not ready
                         */
                        for (ObjectModelMetadataImpl superOmm = this.superMetadata; superOmm != null; superOmm = superOmm.superMetadata) {
                            property = superOmm.declaredProperties.get(propertyName);
                            if (property != null) {
                                break;
                            }
                        }
                        if (property == null) {
                            throw new IllegalProgramException(
                                    LAZY_RESOURCE.get().objectModelKeyPropertyIsNotExisting(
                                            ownerEntry.objectModelInterface, 
                                            ReferenceComparisonRule.class, 
                                            keyPropertyNames, 
                                            propertyName));
                        }
                        if (!(property instanceof ScalarProperty)) {
                            throw new IllegalProgramException(
                                    LAZY_RESOURCE.get().objectModelKeyPropertyIsNotScalar(
                                            ownerEntry.objectModelInterface, 
                                            ReferenceComparisonRule.class, 
                                            keyPropertyNames, 
                                            propertyName
                                    )
                            );
                        }
                        if (((ScalarProperty)property).isDeferrable()) {
                            throw new IllegalProgramException(
                                    LAZY_RESOURCE.get().objectModelKeyPropertyIsDeferrable(
                                            ownerEntry.objectModelInterface, 
                                            ReferenceComparisonRule.class, 
                                            keyPropertyNames, 
                                            propertyName,
                                            Deferrable.class)
                            );
                        }
                        if (isUnstableAndUncloneableScalarType(property.getReturnClass())) {
                            throw new IllegalProgramException(
                                    LAZY_RESOURCE.get().objectModelKeyPropertyHasBadType(
                                            ownerEntry.objectModelInterface, 
                                            ReferenceComparisonRule.class, 
                                            keyPropertyNames, 
                                            propertyName,
                                            property.getReturnClass())
                            );
                        }
                        if (comparatorProperties.put(property.getName(), (ScalarProperty)property) != null) {
                            throw new IllegalProgramException(
                                    LAZY_RESOURCE.get().objectModelComparatorPropertiesAreDuplicated(
                                            ownerEntry.objectModelInterface, 
                                            ReferenceComparisonRule.class, 
                                            keyPropertyNames, 
                                            propertyName));
                        }
                    }
                }
            }
            
            this.declaredProperties = 
                    MACollections.unmodifiable(declaredProperties);
            this.declaredScalarProperties = 
                MACollections.unmodifiable(declaredScalarProperties);
            this.declaredAssociationProperties = 
                MACollections.unmodifiable(declaredAssociationProperties);
            this.comparatorProperties = 
                MACollections.unmodifiable(comparatorProperties);
            
            EqualityComparator<int[]> ec = new EqualityComparator<int[]>() {

                @Override
                public int hashCode(int[] o) {
                    return Arrays.hashCode(o);
                }

                @Override
                public boolean equals(int[] o1, int[] o2) {
                    return Arrays.equals(o1, o2);
                }
                
            };
            this.ownerComparatorMap = new HashMap<>(ec, (EqualityComparator<Comparator<?>>)null);
            this.ownerComparatorMapLock = new ReentrantReadWriteLock();
            this.ownerEqualityComparatorMap = new HashMap<>(ec, (EqualityComparator<EqualityComparator<?>>)null);
            this.ownerEqualityComparatorMapLock = new ReentrantReadWriteLock();
        }
        
        private XOrderedMap<String, Method> orderedDeclaredGetters(
                XOrderedMap<String, Method> declaredGetters, 
                String declaredPropertiesOrder) {
            declaredPropertiesOrder = declaredPropertiesOrder.trim();
            if (Nulls.isNullOrEmpty(declaredPropertiesOrder)) {
                if (declaredGetters.size() > 1) {
                    if (this.mode == ObjectModelMode.ABSTRACT || this.mode == ObjectModelMode.EMBEDDABLE) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().requireEmbeddableComparasionRule(
                                        this.objectModelClass, 
                                        this.mode, 
                                        "declaredPropertiesOrder"
                                )
                        );
                    }
                }
                return declaredGetters;
            }
            if (!PROPERTY_NAMES_PATTERN.matcher(declaredPropertiesOrder).matches()) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().invalidEmbeddableComparasionRule(
                                this.objectModelClass, 
                                "declaredPropertiesOrder",
                                declaredPropertiesOrder, 
                                PROPERTY_NAMES_PATTERN.pattern()
                        )
                );
            }
            XOrderedMap<String, Method> orderedPropertyNames = new LinkedHashMap<>((declaredGetters.size() * 4 + 2) / 3);
            for (String propertyName : COMMA_PATTERN.split(declaredPropertiesOrder)) {
                propertyName = propertyName.trim();
                orderedPropertyNames.put(propertyName, null);
            }
            if (orderedPropertyNames.size() != declaredGetters.size()) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().invalidEmbeddablePropertyCount(
                                this.objectModelClass,
                                "declaredPropertiesOrder",
                                orderedPropertyNames.size(),
                                declaredGetters.size()
                        )
                );
            }
            for (Entry<String, Method> entry : orderedPropertyNames.entrySet()) {
                Method method = declaredGetters.get(entry.getKey());
                if (method == null) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().invalidEmbeddableComparasionPropertyName(
                                    this.objectModelClass,
                                    "declaredPropertiesOrder",
                                    entry.getKey()
                            )
                    );
                }
                entry.setValue(method);
            }
            return orderedPropertyNames;
        }
        
        @Override
        public Class<?> getOwnerClass() {
            return this.ownerClass;
        }
        
        @Override
        public Class<?> getObjectModelClass() {
            return this.objectModelClass;
        }
        
        @Override
        public ObjectModelMetadata getSuperMetadata() {
            return this.superMetadata;
        }

        @Override
        public String getStaticMethodName() {
            return this.staticMethodName;
        }

        @Override
        public ObjectModelFactoryProvider getProvider() {
            return this.provider;
        }

        @Override
        public ObjectModelFactory<?> getFactory() {
            /*
             * This lazy initialization is not thread-safe
             * BUT it doesn't matter!
             * 
             * This is a special optimization!
             */
            ObjectModelFactory<?> factory = this.factory;
            if (factory == null) {
                this.factory = factory = this.provider.getFactory(this.objectModelClass);
            }
            return factory;
        }

        @Override
        public ObjectModelMode getMode() {
            return this.mode;
        }

        @Override
        public boolean isDisabilityAllowed() {
            return this.disabilityAllowed;
        }

        @Override
        public int getDeclaredPropertyBaseId() {
            return this.declaredPropertyBaseId;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public XOrderedMap<String, Property> getDeclaredProperties() {
            return (XOrderedMap)this.declaredProperties;
        }
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public XOrderedMap<String, ScalarProperty> getDeclaredScalarProperties() {
            return (XOrderedMap)this.declaredScalarProperties;
        }
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public XOrderedMap<String, AssociationProperty> getDeclaredAssociationProperties() {
            return (XOrderedMap)this.declaredAssociationProperties;
        }
        
        @Override
        public AbstractPropertyImpl getDeclaredProperty(String name) {
            return this.declaredProperties.get(name);
        }
        
        @Override
        public ScalarPropertyImpl getDeclaredScalarProperty(String name) {
            return this.declaredScalarProperties.get(name);
        }
        
        @Override
        public AbstractAssociationPropertyImpl getDeclaredAssociationProperty(String name) {
            return this.declaredAssociationProperties.get(name);
        }
        
        @Override
        public AbstractPropertyImpl getDeclaredProperty(int id) {
            AbstractPropertyImpl[] declaredPropertyArr = this.declaredPropertyArr;
            if (declaredPropertyArr == null) {
                this.declaredPropertyArr = this.declaredProperties.values().toArray(
                        declaredPropertyArr = 
                            new AbstractPropertyImpl[this.declaredProperties.size()]); 
            }
            id -= this.declaredPropertyBaseId;
            Arguments.mustBetweenValue("id", id, 0, true, declaredPropertyArr.length, false);
            return declaredPropertyArr[id];
        }
        
        @Override
        public ScalarPropertyImpl getDeclaredScalarProperty(int id) {
            AbstractPropertyImpl propertyImpl = this.getDeclaredProperty(id);
            if (propertyImpl instanceof ScalarPropertyImpl) {
                return (ScalarPropertyImpl)propertyImpl;
            }
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().specifiedPropertyIsNotScalarProperty(this.objectModelClass, id)
            );
        }

        @Override
        public StandardAssociationPropertyImpl getDeclaredAssociationProperty(int id) {
            AbstractPropertyImpl propertyImpl = this.getDeclaredProperty(id);
            if (propertyImpl instanceof StandardAssociationPropertyImpl) {
                return (StandardAssociationPropertyImpl)propertyImpl;
            }
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().specifiedPropertyIsNotAssociationProperty(this.objectModelClass, id)
            );
        }

        @Override
        public XOrderedMap<String, ScalarProperty> getComparatorProperties() {
            return this.comparatorProperties;
        }

        @Override
        public Property getProperty(String name) {
            Property property = this.declaredProperties.get(name);
            if (property == null) {
                ObjectModelMetadataImpl superMetadata = this.superMetadata;
                if (superMetadata != null) {
                    return superMetadata.getProperty(name);
                }
                throw new IllegalArgumentException(LAZY_RESOURCE.get().noProperty(this.objectModelClass, name));
            }
            return property;
        }

        @Override
        public ScalarProperty getScalarProperty(String name) {
            ScalarProperty property = this.declaredScalarProperties.get(name);
            if (property == null) {
                ObjectModelMetadataImpl superMetadata = this.superMetadata;
                if (superMetadata != null) {
                    return superMetadata.getScalarProperty(name);
                }
                throw new IllegalArgumentException(LAZY_RESOURCE.get().noScalarProperty(this.objectModelClass, name));
            }
            return property;
        }

        @Override
        public AssociationProperty getAssociationProperty(String name) {
            AssociationProperty property = this.declaredAssociationProperties.get(name);
            if (property == null) {
                ObjectModelMetadataImpl superMetadata = this.superMetadata;
                if (superMetadata != null) {
                    return superMetadata.getAssociationProperty(name);
                }
                throw new IllegalArgumentException(LAZY_RESOURCE.get().noAssociationProperty(this.objectModelClass, name));
            }
            return property;
        }

        @Override
        public Property getProperty(int id) {
            return this.getPropertyArr()[id];
        }

        @Override
        public ScalarProperty getScalarProperty(int id) {
            Property property = this.getProperty(id);
            if (property instanceof ScalarProperty) {
                return (ScalarProperty)property;
            }
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().specifiedPropertyIsNotScalarProperty(this.objectModelClass, id)
            );
        }

        @Override
        public AssociationProperty getAssociationProperty(int id) {
            Property property = this.getProperty(id);
            if (property instanceof AssociationProperty) {
                return (AssociationProperty)property;
            }
            throw new IllegalArgumentException(
                    LAZY_RESOURCE.get().specifiedPropertyIsNotAssociationProperty(this.objectModelClass, id)
            );
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public XOrderedMap<String, Property> getProperties() {
            return (XOrderedMap)this.properties;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public XOrderedMap<String, ScalarProperty> getScalarProperties() {
            return (XOrderedMap)this.scalarProperties;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public XOrderedMap<String, AssociationProperty> getAssociationProperties() {
            return (XOrderedMap)this.associationProperties;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <O> Comparator<O> getOwnerComparator() {
            if (this.mode == ObjectModelMode.REFERENCE) {
                throw new UnsupportedOperationException(
                        LAZY_RESOURCE.get().referenceDoesNotSupportDefaultComparator(
                                this.objectModelClass,
                                "getOwnerComparator",
                                ObjectModelMode.REFERENCE
                        )
                );
            }
            return (Comparator<O>)this.embeddableComparator;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <O> Comparator<O> getOwnerComparator(String ... scalarPropertyNames) {
            Arguments.mustNotBeNull("scalarPropertyNames", scalarPropertyNames);
            Arguments.mustNotBeEmpty("scalarPropertyNames", scalarPropertyNames);
            Arguments.mustNotContainNullElements("scalarPropertyNames", scalarPropertyNames);
            Arguments.mustNotContainEmptyElements("scalarPropertyNames", scalarPropertyNames);
            XOrderedSet<ScalarProperty> scalarProperties = new LinkedHashSet<>(
                    SCALAR_PROPERTY_EQUALITY_COMPARATOR,
                    (scalarPropertyNames.length * 4 + 2) / 3);
            for (String scalarPropertyName : scalarPropertyNames) {
                ScalarProperty scalarProperty;
                try {
                    scalarProperty = this.getScalarProperty(scalarPropertyName);
                } catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException(
                            LAZY_RESOURCE.get().noScalarProperty(this.objectModelClass, scalarPropertyName)
                    );
                }
                scalarProperties.add(scalarProperty);
            }
            return (Comparator<O>)this.getOwnerComparator(scalarProperties);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <O> Comparator<O> getOwnerComparator(int ... scalarPropertyIds) {
            Arguments.mustNotBeNull("scalarPropertyIds", scalarPropertyIds);
            Arguments.mustNotBeEmpty("scalarPropertyIds", scalarPropertyIds);
            XOrderedSet<ScalarProperty> scalarProperties = new LinkedHashSet<>(
                    SCALAR_PROPERTY_EQUALITY_COMPARATOR,
                    (scalarPropertyIds.length * 4 + 2) / 3);
            for (int scalarPropertyId : scalarPropertyIds) {
                ScalarProperty scalarProperty;
                scalarProperty = this.getScalarProperty(scalarPropertyId);
                scalarProperties.add(scalarProperty);
            }
            return (Comparator<O>)this.getOwnerComparator(scalarProperties);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <O> EqualityComparator<O> getOwnerEqualityComparator() {
            if (this.mode == ObjectModelMode.REFERENCE) {
                return ReferenceEqualityComparator.getInstance();
            }
            return (EqualityComparator<O>)this.embeddableEqualityComparator;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <O> EqualityComparator<O> getOwnerEqualityComparator(String ... scalarPropertyNames) {
            if (Nulls.isNullOrEmpty(scalarPropertyNames)) {
                return this.getOwnerEqualityComparator();
            }
            Arguments.mustNotContainNullElements("scalarPropertyNames", scalarPropertyNames);
            Arguments.mustNotContainEmptyElements("scalarPropertyNames", scalarPropertyNames);
            NavigableSet<ScalarProperty> scalarProperties = new TreeSet<>(SCALAR_PROPERTY_COMPARATOR);
            for (String scalarPropertyName : scalarPropertyNames) {
                ScalarProperty scalarProperty;
                try {
                    scalarProperty = this.getScalarProperty(scalarPropertyName);
                } catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException(
                            LAZY_RESOURCE.get().noScalarProperty(objectModelClass, scalarPropertyName)
                    );
                }
                scalarProperties.add(scalarProperty);
            }
            return (EqualityComparator<O>)this.getOwnerEqualityComparator(scalarProperties);
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <O> EqualityComparator<O> getOwnerEqualityComparator(int ... scalarPropertyIds) {
            if (Nulls.isNullOrEmpty(scalarPropertyIds)) {
                return this.getOwnerEqualityComparator();
            }
            NavigableSet<ScalarProperty> scalarProperties = new TreeSet<>(SCALAR_PROPERTY_COMPARATOR);
            for (int scalarPropertyId : scalarPropertyIds) {
                ScalarProperty scalarProperty;
                scalarProperty = this.getScalarProperty(scalarPropertyId);
                scalarProperties.add(scalarProperty);
            }
            return (EqualityComparator<O>)this.getOwnerEqualityComparator(scalarProperties);
        }

        @Override
        public String toString() {
            return this.getClass().getName() + '[' + this.ownerClass.getName() + ']';
        }
        
        private AbstractPropertyImpl[] getPropertyArr() {
            AbstractPropertyImpl[] arr = this.propertyArr;
            if (arr == null) {
                if (this.superMetadata == null) {
                    arr = new AbstractPropertyImpl[this.declaredProperties.size()];
                    this.declaredProperties.values().toArray(arr);
                } else {
                    AbstractPropertyImpl[] superArr = this.superMetadata.getPropertyArr();
                    int superLen = superArr.length;
                    arr = new AbstractPropertyImpl[superLen + this.declaredProperties.size()];
                    int index = 0;
                    for (AbstractPropertyImpl prop : this.declaredProperties.values()) {
                        arr[superLen + index++] = prop;
                    }
                }
                this.propertyArr = arr;
            }
            return arr;
        }
        
        private Comparator<?> getOwnerComparator(XOrderedSet<ScalarProperty> scalarProperties) {
            int[] key = new int[scalarProperties.size()];
            int index = 0;
            for (ScalarProperty scalarProperty : scalarProperties) {
                key[index++] = scalarProperty.getId();
            }
            
            Comparator<?> comparator;
            Lock lock;
            Map<int[], Comparator<?>> map = this.ownerComparatorMap;
            ReadWriteLock mapLock = this.ownerComparatorMapLock;
            
            (lock = mapLock.readLock()).lock(); // 1st locking
            try {
                comparator = map.get(key); // 1st reading
            } finally {
                lock.unlock();
            }
            
            if (comparator == null) { // 1st checking
                ((lock = mapLock.writeLock())).lock(); // 2nd locking
                try {
                    comparator = map.get(key); // 2nd reading
                    if (comparator == null) { // 2nd checking
                        comparator = this.createComparator(scalarProperties);
                        map.put(key, comparator);
                    }
                } finally {
                    lock.unlock();
                }
            }
            return comparator;
        }

        private EqualityComparator<?> getOwnerEqualityComparator(NavigableSet<ScalarProperty> scalarProperties) {
            int[] key = new int[scalarProperties.size()];
            int index = 0;
            for (ScalarProperty scalarProperty : scalarProperties) {
                key[index++] = scalarProperty.getId();
            }
            
            EqualityComparator<?> equalityComparator;
            Lock lock;
            Map<int[], EqualityComparator<?>> map = this.ownerEqualityComparatorMap;
            ReadWriteLock mapLock = this.ownerEqualityComparatorMapLock;
            
            ((lock = mapLock.readLock())).lock(); // 1st locking
            try {
                equalityComparator = map.get(key); // 1st reading
            } finally {
                lock.unlock();
            }
            
            if (equalityComparator == null) { // 1st checking
                ((lock = mapLock.writeLock())).lock(); // 2nd locking
                try {
                    equalityComparator = map.get(key); // 2nd reading
                    if (equalityComparator == null) { // 2nd checking
                        equalityComparator = this.createEqualityComparator(scalarProperties);
                        map.put(key, equalityComparator);
                    }
                } finally {
                    lock.unlock();
                }
            }
            return equalityComparator;
        }
        
        private Comparator<?> createComparator(XOrderedSet<ScalarProperty> scalarProperties) {
            return new FrozenComparatorGenerator(this, scalarProperties).getFrozenComparator();
        }
        
        private EqualityComparator<?> createEqualityComparator(NavigableSet<ScalarProperty> scalarProperties) {
            return new FrozenEqualityComparatorGenerator(this, scalarProperties).getFrozenEqualityComparator();
        }
        
        boolean parse(Context context) {
            if ((this.parsedStates & PS_FIRST) != 0) {
                return false;
            }
            this.parsedStates |= PS_FIRST;
            boolean contextChanged = false;
            for (ScalarPropertyImpl scalarPropertyImpl : this.declaredScalarProperties.values()) {
                if (scalarPropertyImpl.parse(context)) {
                    contextChanged = true;
                }
            }
            for (AbstractAssociationPropertyImpl abstractAssociationPropertyImpl : this.declaredAssociationProperties.values()) {
                if (abstractAssociationPropertyImpl.parse(context)) {
                    contextChanged = true;
                }
            }
            return contextChanged;
        }
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        void secondParse() {
            ObjectModelMetadataImpl superMetadata = this.superMetadata;
            
            XOrderedMap<String, AbstractPropertyImpl> properties = this.properties;
            if (properties == null) {
                if (superMetadata == null) {
                    properties = this.declaredProperties; 
                } else {
                    superMetadata.secondParse();
                    properties = new LinkedHashMap<String, AbstractPropertyImpl>();
                    properties.putAll((Map)superMetadata.getProperties());
                    properties.putAll(this.declaredProperties);
                }
                this.properties = MACollections.unmodifiable(properties);
            }
            
            XOrderedMap<String, ScalarPropertyImpl> scalarProperties = this.scalarProperties;
            if (scalarProperties == null) {
                if (superMetadata == null) {
                    scalarProperties = this.declaredScalarProperties; 
                } else {
                    superMetadata.secondParse();
                    scalarProperties = new LinkedHashMap<String, ScalarPropertyImpl>();
                    scalarProperties.putAll((Map)superMetadata.getScalarProperties());
                    scalarProperties.putAll(this.declaredScalarProperties);
                }
                this.scalarProperties = MACollections.unmodifiable(scalarProperties);
            }
            
            XOrderedMap<String, AbstractAssociationPropertyImpl> associationProperties = this.associationProperties;
            if (associationProperties == null) {
                if (superMetadata == null) {
                    associationProperties = this.declaredAssociationProperties; 
                } else {
                    superMetadata.secondParse();
                    associationProperties = new LinkedHashMap<String, AbstractAssociationPropertyImpl>();
                    associationProperties.putAll((Map)superMetadata.getAssociationProperties());
                    associationProperties.putAll(this.declaredAssociationProperties);
                }
                this.associationProperties = MACollections.unmodifiable(associationProperties);
            }
        }

        void thirdParse() {
            if ((this.parsedStates & PS_THIRD) != 0) {
                return;
            }
            this.parsedStates |= PS_THIRD;
            if (this.mode != ObjectModelMode.REFERENCE && this.embeddableComparator == null) {
                for (ObjectModelMetadataImpl objectModelMetadata = this;
                        objectModelMetadata != null;
                        objectModelMetadata = objectModelMetadata.superMetadata) {
                    for (ScalarPropertyImpl scalarProperty : objectModelMetadata.declaredScalarProperties.values()) {
                        if (scalarProperty.isEmbeded()) {
                            scalarProperty.returnObjectModelMetadata.thirdParse();
                        }
                    }
                }
                this.embeddableComparator = 
                        new FrozenComparatorGenerator(
                                this, 
                                new LinkedHashSet<>(SCALAR_PROPERTY_EQUALITY_COMPARATOR)
                        ).getFrozenComparator();
                this.embeddableEqualityComparator =
                        new FrozenEqualityComparatorGenerator(
                                this, 
                                new TreeSet<>(SCALAR_PROPERTY_COMPARATOR)
                        ).getFrozenEqualityComparator();
            }
        }

        void fourthParse(Context context) {
            for (AbstractAssociationPropertyImpl abstractAssociationPropertyImpl : this.declaredAssociationProperties.values()) {
                abstractAssociationPropertyImpl.fourthParse(context);
            }
        }
        
        void fifthParse(Context context) {
            for (AbstractAssociationPropertyImpl abstractAssociationPropertyImpl : this.declaredAssociationProperties.values()) {
                if (abstractAssociationPropertyImpl instanceof StandardAssociationPropertyImpl) {
                    ((StandardAssociationPropertyImpl)abstractAssociationPropertyImpl).finishCreateOpposite(context);
                }
            }
        }
        
        protected final Object writeReplace() throws ObjectStreamException {
            return new ObjectModelMetadataReplacement(this);
        }
    }
    
    private static abstract class AbstractPropertyImpl implements Property {
        
        private static final long serialVersionUID = 7235743456782897458L;
        
        protected final ObjectModelMetadataImpl declaringObjectModelMetadata;
        
        protected final String name;
        
        protected final String getterName;
        
        protected final String setterName;
        
        protected final int id;
        
        protected final Class<?> returnClass;
        
        protected final Type returnType;
        
        boolean disabilityAllowed;
        
        AbstractPropertyImpl(
                ObjectModelMetadataImpl declaringObjectModelMetadata,
                Method method,
                int id) {
            this.declaringObjectModelMetadata = declaringObjectModelMetadata;
            String postfix = method.getName();
            if (postfix.startsWith("is")) {
                postfix = postfix.substring(2);
            } else {
                postfix = postfix.substring(3);
            }
            if (INVALID_METHOD_POSTFIXS.contains(postfix)) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().propertyNameCanNotBeAnyOf(INVALID_METHOD_POSTFIXS_TEXT)
                );
            }
            this.name = Strings.toCamelCase(postfix);
            this.getterName = method.getName();
            this.setterName = "set" + postfix;
            
            this.id = id;
            this.returnClass = method.getReturnType();
            Type genericReturnType = method.getGenericReturnType();
            this.returnType = genericReturnType;
            
            this.disabilityAllowed = declaringObjectModelMetadata.isDisabilityAllowed();
            if (this.disabilityAllowed) {
                for (Annotation annotation : method.getAnnotations()) {
                    if (annotation.annotationType().isAnnotationPresent(NonDisabledOnly.class)) {
                        this.disabilityAllowed = false;
                        break;
                    }
                }
            }
        }
        
        static AbstractPropertyImpl of(
                Context context,
                ObjectModelMetadataImpl declaringObjectModelMetadataImpl,
                Method method,
                int id) {
            Annotation association = getRestrictedAnnotation(method, declaringObjectModelMetadataImpl.getMode());
            if (association.annotationType() == Association.class) {
                return new StandardAssociationPropertyImpl(context, declaringObjectModelMetadataImpl, method, id);
            }
            if (association.annotationType() == Contravariance.class) {
                return new ContravarianceAssociationPropertyImpl(declaringObjectModelMetadataImpl, method, id);
            }
            return new ScalarPropertyImpl(declaringObjectModelMetadataImpl, method, id);
        }

        @Override
        public ObjectModelMetadata getDeclaringObjectModelMetadata() {
            return this.declaringObjectModelMetadata;
        }

        @Override
        public boolean isDisabilityAllowed() {
            return this.disabilityAllowed;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getGetterName() {
            return this.getterName;
        }

        @Override
        public String getSetterName() {
            return this.setterName;
        }

        @Override
        public int getId() {
            return this.id;
        }

        @Override
        public Class<?> getReturnClass() {
            return this.returnClass;
        }

        @Override
        public Type getReturnType() {
            return this.returnType;
        }
        
        @Override
        public String toString() {
            return this.declaringObjectModelMetadata.getObjectModelClass().getName() + '.' + this.name;
        }

        protected ObjectModelMetadataImpl getRelatedObjectModelMetadata(
                Context context,
                boolean key,
                Ref<Boolean> newMetadataCreatedRef) {
            Class<?> relatedType;
            if (this instanceof ScalarProperty) {
                relatedType = this.getReturnClass();
            } else if (key) {
                relatedType = ((AssociationProperty)this).getKeyClass();
            } else {
                relatedType = ((AssociationProperty)this).getElementClass();
            }
            ObjectModelMetadataImpl relatedObjectModelMetadata = context.getMetadata(relatedType);
            if (relatedObjectModelMetadata == null) {
                if (relatedType.isInterface() || relatedType.isAnnotation()) {
                    if (key) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().keyTypeMustBeClass((AssociationProperty)this, relatedType)
                        );
                    } else {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().relatedTypeMustBeClass((AssociationProperty)this, relatedType)
                        );
                    }
                }
                OwnerEntry ownerEntry = Metadatas.getOwnerEntry(relatedType);
                if (ownerEntry == null) {
                    if (key) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().keyTypeMustBeObjectModelOwnerClass(this, relatedType)
                        );
                    } else {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().relatedTypeMustBeObjectModelOwnerClass(this, relatedType)
                        );
                    }
                }
                relatedObjectModelMetadata = new ObjectModelMetadataImpl(ownerEntry, context);
                newMetadataCreatedRef.set(true);
            }
            
            ObjectModelMetadataImpl declaringObjectModelMetadata = this.declaringObjectModelMetadata;
            if (declaringObjectModelMetadata.getProvider() !=
                    relatedObjectModelMetadata.getProvider()) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().badRelatedObjectModelProvider(
                                declaringObjectModelMetadata.getObjectModelClass(), 
                                declaringObjectModelMetadata.getProvider().getClass(),
                                this.getterName,
                                relatedObjectModelMetadata.getOwnerClass(), 
                                relatedObjectModelMetadata.getObjectModelClass(),  
                                relatedObjectModelMetadata.getProvider().getClass()
                        )
                );
            }
            return relatedObjectModelMetadata;
        }
        
        private static Annotation getRestrictedAnnotation(Method method, ObjectModelMode objectModelMode) {
            Annotation resultAnnotation = null;
            Annotation prevAnnotation = null;
            Annotation prevAnnotationAnnotation = null;
            for (Annotation annotation : method.getAnnotations()) {
                if (objectModelMode != ObjectModelMode.REFERENCE && 
                        annotation.annotationType().isAnnotationPresent(OwnerReferenceOnly.class)) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().ownerMustBeReference(
                                    method, 
                                    annotation.annotationType(), 
                                    OwnerReferenceOnly.class, 
                                    ObjectModelMode.REFERENCE, 
                                    objectModelMode
                            )
                    );
                }
                if (method.isAnnotationPresent(Deferrable.class) && 
                        annotation.annotationType().isAnnotationPresent(NonDeferrableOnly.class)) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().propertyMustNotBeDeferrable(
                                    method, 
                                    annotation.annotationType(), 
                                    NonDeferrableOnly.class, 
                                    Deferrable.class
                            )
                    );
                }
                ScalarOnly scalarOnly = annotation.annotationType().getAnnotation(ScalarOnly.class);
                AssociationOnly associationOnly = annotation.annotationType().getAnnotation(AssociationOnly.class);
                Annotation annotationAnnotation = scalarOnly != null ? scalarOnly : associationOnly;
                if (scalarOnly == null && associationOnly == null) {
                    continue;
                }
                if (scalarOnly != null && associationOnly != null) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().conflictAnnotationAnnotations(
                                    method,
                                    annotation.annotationType(),
                                    ScalarOnly.class,
                                    AssociationOnly.class));
                }
                if (scalarOnly != null) {
                    validateScalarOnly(scalarOnly, annotation, method);
                    if (resultAnnotation == null) {
                        resultAnnotation = annotation;
                    }
                } else {
                    validateAssociationOnly(associationOnly, annotation, method);
                    if (annotation.annotationType() == Association.class ||
                            annotation.annotationType() == Contravariance.class) {
                        if (resultAnnotation != null) {
                            throw new IllegalProgramException(
                                    LAZY_RESOURCE.get().bothAssociationOnlyAndContravariance(
                                            method,
                                            Association.class,
                                            Contravariance.class
                                    )
                            );
                        }
                        resultAnnotation = annotation;
                    }
                }
                if (prevAnnotationAnnotation != null &&
                        prevAnnotationAnnotation.annotationType() != annotationAnnotation.annotationType()) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().conflictAnnotations(
                                    method, 
                                    prevAnnotation.annotationType(),
                                    prevAnnotationAnnotation.annotationType(),
                                    annotation.annotationType(), 
                                    annotationAnnotation.annotationType()));
                }
                prevAnnotation = annotation;
                prevAnnotationAnnotation = annotationAnnotation;
            }
            if (resultAnnotation == null) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().propertyIsNeigthScalarNorAssociation(
                                method, 
                                ScalarOnly.class, 
                                AssociationOnly.class));
            }
            if (prevAnnotationAnnotation.annotationType() == AssociationOnly.class && 
                    resultAnnotation.annotationType() != Association.class &&
                    resultAnnotation.annotationType() != Contravariance.class) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().noAssociationAnnotationForAssociationProperty(
                                method,
                                Association.class,
                                AssociationOnly.class));
            }
            return resultAnnotation;
        }
        
        private static void validateScalarOnly(ScalarOnly scalarOnly, Annotation scalarAnnotation, Method method) {
            Class<?>[] lowerClassArr = scalarOnly.lowerTypes();
            Class<?> upperClass = scalarOnly.upperType() == void.class ? null : scalarOnly.upperType();
            for (int i = 0; i < lowerClassArr.length; i++) {
                Class<?> lowerClass = lowerClassArr[i];
                if (i != 0) {
                    if (lowerClass.isPrimitive() || lowerClass.isArray() || lowerClass.isEnum() ||
                            (!lowerClass.isInterface() && !lowerClass.isAnnotation())) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().classIsNotFirstLowerTypeInScalarOnly(
                                        method, 
                                        scalarAnnotation.annotationType(), 
                                        ScalarOnly.class, 
                                        "lowerTypes"));
                    }
                }
            }
            if (upperClass != null) {
                for (Class<?> lowerClass : lowerClassArr) {
                    if (!lowerClass.isAssignableFrom(upperClass)) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().badLowerAndUpperInScalarOnly(
                                        method, 
                                        scalarAnnotation.annotationType(), 
                                        ScalarOnly.class, 
                                        "lowerTypes", 
                                        lowerClass, 
                                        "upperType", 
                                        upperClass));
                    }
                }
            }
            Class<?> returnClass = method.getReturnType();
            Class<?> returnBoxClass = getBoxClass(returnClass);
            for (Class<?> lowerClass : lowerClassArr) {
                if (!lowerClass.isAssignableFrom(returnBoxClass)) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().scalarTypeOutOfLowerBound(
                                    method, 
                                    scalarAnnotation.annotationType(), 
                                    ScalarOnly.class, 
                                    "lowerTypes", 
                                    lowerClass, 
                                    returnClass));
                }
            }
            if (upperClass != null && !returnClass.isAssignableFrom(upperClass)) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().scalarTypeOutOfUpperBound(
                                method, 
                                scalarAnnotation.annotationType(), 
                                ScalarOnly.class, 
                                "upperType", 
                                upperClass, 
                                returnClass));
            }
        }
        
        private static void validateAssociationOnly(
                AssociationOnly associationOnly, 
                Annotation associationAnnotation, 
                Method method) {
            AssociatedEndpointType[] associatedEndpointTypes = associationOnly.associatedEndpointTypes();
            if (associatedEndpointTypes.length == 0) {
                return;
            }
            AssociatedEndpointType associatedEndpointType;
            try {
                associatedEndpointType = AssociatedEndpointType.of(method.getReturnType());
            } catch (IllegalArgumentException ex) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().invalidAssociationClass(
                                method, 
                                VALID_ENDPOINT_TYPES));
            }
            for (AssociatedEndpointType allowedAssociatedEndpointType : associatedEndpointTypes) {
                if (associatedEndpointType == allowedAssociatedEndpointType) {
                    return;
                }
            }
            throw new IllegalProgramException(
                    LAZY_RESOURCE.get().associatedEndpointTypeOutOfBound(
                            method, 
                            associationAnnotation.annotationType(), 
                            AssociationOnly.class, 
                            "associationEndTypes", 
                            associatedEndpointTypes, 
                            associatedEndpointType));
        }
        
        private static Class<?> getBoxClass(Class<?> clazz) {
            if (clazz.isPrimitive()) {
                if (clazz == boolean.class) {
                    return Boolean.class; 
                }
                if (clazz == char.class) {
                    return Character.class; 
                }
                if (clazz == byte.class) {
                    return Byte.class; 
                }
                if (clazz == short.class) {
                    return Short.class; 
                }
                if (clazz == int.class) {
                    return Integer.class; 
                }
                if (clazz == long.class) {
                    return Long.class; 
                }
                if (clazz == float.class) {
                    return Float.class; 
                }
                if (clazz == double.class) {
                    return Double.class; 
                }
            }
            return clazz;
        }
        
        protected final Object writeReplace() throws ObjectStreamException {
            return new PropertyReplacement(this);
        }
    }
    
    private static class ScalarPropertyImpl extends AbstractPropertyImpl implements ScalarProperty {
        
        private static final long serialVersionUID = 2358654967839721343L;
        
        private boolean deferrable;
        
        private boolean embedded;
        
        private ObjectModelMetadataImpl returnObjectModelMetadata;
        
        ScalarPropertyImpl(
                ObjectModelMetadataImpl declaringObjectModelMetadata,
                Method method, 
                int id) {
            super(declaringObjectModelMetadata, method, id);
            String setterName = 
                    method.getName().startsWith("is") ?
                    "set" + method.getName().substring(2) :
                    "set" + method.getName().substring(3);
            try {
                method.getDeclaringClass().getDeclaredMethod(
                        setterName, 
                        method.getReturnType());
            } catch (NoSuchMethodException ex) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().noSetterForScalarGetter(
                                method, 
                                method.getDeclaringClass(), 
                                setterName, 
                                method.getGenericReturnType()));
            }
            if (method.isAnnotationPresent(Deferrable.class)) {
                if (declaringObjectModelMetadata.getMode() != ObjectModelMode.REFERENCE) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().deferrablePropertyRequiredReferenceModel(
                                    this,
                                    Deferrable.class,
                                    ObjectModelMode.REFERENCE,
                                    declaringObjectModelMetadata.getMode()
                            )
                    );
                }
                this.deferrable = true;
            }
            if (isUnstableAndUncloneableScalarType(this.returnClass)) {
                if (declaringObjectModelMetadata.getMode() != ObjectModelMode.REFERENCE) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().unstablePropertyRequiredReferenceModel(
                                    this,
                                    this.returnClass,
                                    ObjectModelMode.REFERENCE,
                                    declaringObjectModelMetadata.getMode()
                            )
                    );
                }
            }
        }

        @Override
        public boolean isDeferrable() {
            return this.deferrable;
        }

        @Override
        public boolean isEmbeded() {
            return this.embedded;
        }

        @Override
        public ObjectModelMetadata getReturnObjectModelMetadata() {
            return this.returnObjectModelMetadata;
        }

        boolean parse(Context context) {
            Class<?> returnClass = this.getReturnClass();
            if (SIMPLE_TYPES.contains(returnClass) || returnClass.isEnum()) {
                return false;
            }
            Ref<Boolean> newMetadataCreatedRef = new Ref<>(false);
            this.returnObjectModelMetadata = this.getRelatedObjectModelMetadata(context, false, newMetadataCreatedRef);
            if (this.returnObjectModelMetadata != null) {
                ObjectModelMode mode = this.returnObjectModelMetadata.getMode();
                if (mode == ObjectModelMode.REFERENCE) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().scalarCanNotBeReference(
                                    this.declaringObjectModelMetadata.getObjectModelClass(), 
                                    this.name, 
                                    ObjectModelMode.REFERENCE
                            )
                    );
                } else if (mode == ObjectModelMode.EMBEDDABLE) {
                    this.embedded = true;
                } else {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().scalarCanNotReturnAbstractObjectModel(
                                    this, 
                                    this.returnClass, 
                                    this.returnObjectModelMetadata.getObjectModelClass(), 
                                    ObjectModelMode.ABSTRACT
                            )
                    );
                }
                if (this.deferrable) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().complexScalarCanNotBeDeferrable(this, Deferrable.class)
                    );
                }
            }
            return newMetadataCreatedRef.get().booleanValue();
        }
    }
    
    private static abstract class AbstractAssociationPropertyImpl
    extends AbstractPropertyImpl
    implements AssociationProperty {
        
        private static final long serialVersionUID = -3845802375897389457L;

        AbstractAssociationPropertyImpl(
                ObjectModelMetadataImpl declaringObjectModelMetadata,
                Method associationMethod, 
                int id) {
            super(declaringObjectModelMetadata, associationMethod, id);
            if (associationMethod.getParameterTypes().length != 0) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().associationMethodMustHaveNoParameters(associationMethod)
                );
            }
            if (associationMethod.getExceptionTypes().length != 0) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().associationMethodMustThrowNoExceptions(associationMethod)
                );
            }
        }
        
        public abstract ParameterizedType getReturnType();

        abstract boolean parse(Context context);
        
        abstract void fourthParse(Context context);
    }

    private static class StandardAssociationPropertyImpl extends AbstractAssociationPropertyImpl {
        
        private static final long serialVersionUID = -8978926734523452311L;
        
        private Class<?> standardReturnClass;
        
        private AssociatedEndpointType associatedEndpointType;
        
        private Class<?> keyClass;
        
        private ObjectModelMetadata keyObjectModelMetadata;
        
        private ObjectModelMetadata returnObjectModelMetadata;
        
        private Class<?> elementClass;
        
        private UnifiedComparator<?> keyUnifiedCompartor;
        
        private UnifiedComparator<?> unifiedCompartor;
        
        private XOrderedMap<String, ScalarProperty> comparatorProperties;
        
        private Object variant;
        
        StandardAssociationPropertyImpl(
                Context context,
                ObjectModelMetadataImpl declaredObjectModelMetadata,
                Method associationMethod,
                int id) {
            
            super(declaredObjectModelMetadata, associationMethod, id);
            
            Class<?> associatedEndpointClass = associationMethod.getReturnType();
            if (View.class.isAssignableFrom(associatedEndpointClass)) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().associationCanNotBeView(associationMethod, View.class)
                );
            }
            if (!VALID_ENDPOINT_TYPES.contains(associatedEndpointClass)) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().invalidAssociationClass(
                                associationMethod, 
                                VALID_ENDPOINT_TYPES
                        )
                );
            }
            if (NavigableMap.class.isAssignableFrom(associatedEndpointClass)) {
                this.standardReturnClass = NavigableMap.class;
            } else if (SortedMap.class.isAssignableFrom(associatedEndpointClass)) {
                this.standardReturnClass = SortedMap.class;
            } else if (XOrderedMap.class.isAssignableFrom(associatedEndpointClass)) {
                this.standardReturnClass = XOrderedMap.class;
            } else if (Map.class.isAssignableFrom(associatedEndpointClass)) {
                this.standardReturnClass = Map.class;
            } else if (NavigableSet.class.isAssignableFrom(associatedEndpointClass)) {
                this.standardReturnClass = NavigableSet.class;
            } else if (SortedSet.class.isAssignableFrom(associatedEndpointClass)) {
                this.standardReturnClass = SortedSet.class;
            } else if (XOrderedSet.class.isAssignableFrom(associatedEndpointClass)) {
                this.standardReturnClass = XOrderedSet.class;
            } else if (Set.class.isAssignableFrom(associatedEndpointClass)) {
                this.standardReturnClass = Set.class;
            } else if (List.class.isAssignableFrom(associatedEndpointClass)) {
                this.standardReturnClass = List.class;
            } else if (Collection.class.isAssignableFrom(associatedEndpointClass)) {
                this.standardReturnClass = Collection.class;
            } else if (KeyedReference.class.isAssignableFrom(associatedEndpointClass)) {
                this.standardReturnClass = KeyedReference.class;
            } else if (IndexedReference.class.isAssignableFrom(associatedEndpointClass)) {
                this.standardReturnClass = IndexedReference.class;
            } else if (Reference.class.isAssignableFrom(associatedEndpointClass)) {
                this.standardReturnClass = Reference.class;
            } else {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().associationMustBeAnyOf(
                                associatedEndpointClass,
                                Reference.class,
                                IndexedReference.class,
                                KeyedReference.class,
                                Collection.class,
                                List.class,
                                Set.class,
                                XOrderedSet.class,
                                SortedSet.class,
                                NavigableSet.class,
                                Map.class,
                                XOrderedMap.class,
                                SortedMap.class,
                                NavigableMap.class
                        )
                );
            }
            this.associatedEndpointType = AssociatedEndpointType.of(associatedEndpointClass);
            
            ClassInfo<?> classInfo = 
                ClassInfo
                .of(this.returnType)
                .getAncestor(standardReturnClass);
            if (!(classInfo.getRawType() instanceof ParameterizedType)) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().associationMethodMissGenericArguments(associationMethod)
                );
            }
            Type[] types = ((ParameterizedType)classInfo.getRawType()).getActualTypeArguments();
            this.elementClass = GenericTypes.eraseGenericType(types[types.length - 1]);
            if (Map.class.isAssignableFrom(standardReturnClass) ||
                    KeyedReference.class.isAssignableFrom(standardReturnClass)) {
                Class<?> keyClass = GenericTypes.eraseGenericType(types[0]);
                this.keyClass = keyClass;
            }
            
            this.variant = this.new Variant(
                    associationMethod,
                    this.elementClass);
        }
        
        @Override
        public Class<?> getStandardReturnClass() {
            return this.standardReturnClass;
        }

        @Override
        public Class<?> getKeyClass() {
            return this.keyClass;
        }

        @Override
        public ObjectModelMetadata getKeyObjectModelMetadata() {
            return this.keyObjectModelMetadata;
        }

        @Override
        public ObjectModelMetadata getReturnObjectModelMetadata() {
            return this.returnObjectModelMetadata;
        }

        @Override
        public UnifiedComparator<?> getKeyUnifiedComparator() {
            return this.keyUnifiedCompartor;
        }

        @Override
        public Class<?> getElementClass() {
            return this.elementClass;
        }

        @Override
        public AssociatedEndpointType getAssociatedEndpointType() {
            return this.associatedEndpointType;
        }
        
        @Override
        public XOrderedMap<String, ScalarProperty> getComparatorProperties() {
            return this.comparatorProperties;
        }

        @Override
        public UnifiedComparator<?> getCollectionUnifiedComparator() {
            return this.unifiedCompartor;
        }

        @Override
        public ParameterizedType getReturnType() {
            return (ParameterizedType)this.returnType;
        }

        @Override
        public AssociationProperty getOppositeProperty() {
            return (AssociationProperty)this.variant;
        }
        
        @Override
        public AssociationProperty getCovarianceProperty() {
            return null;
        }

        @Override
        boolean parse(Context context) {
            Ref<Boolean> newMetadataCreatedRef = new Ref<>(false);
            if (this.keyClass != null && !SIMPLE_TYPES.contains(this.keyClass)) {
                ObjectModelMetadataImpl keyObjectModelMetadata = 
                        StandardAssociationPropertyImpl
                        .this
                        .getRelatedObjectModelMetadata(context, true, newMetadataCreatedRef);
                if (keyObjectModelMetadata.getMode() != ObjectModelMode.EMBEDDABLE) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().keyTypeMustBeEmbeddable(
                                    this, 
                                    this.keyClass, 
                                    keyObjectModelMetadata.getObjectModelClass(), 
                                    ObjectModelMode.EMBEDDABLE, 
                                    keyObjectModelMetadata.getMode()
                            )
                    );
                }
                this.keyObjectModelMetadata = keyObjectModelMetadata;
            }
            return newMetadataCreatedRef.get().booleanValue() | // "|", Not "||" 
                    ((Variant)this.variant).parse(context);
        }
        
        @Override
        void fourthParse(Context context) {
            ((Variant)this.variant).fourthParse(context);
        }
        
        void finishCreateOpposite(Context context) {
            Variant variant = (Variant)this.variant;
            this.returnObjectModelMetadata = variant.oppositeObjectModelMetadata;
            this.variant = variant.oppositeAssociationPropertyImpl;
            this.comparatorProperties = MACollections.unmodifiable(variant.comparatorProperties);
            this.keyUnifiedCompartor = variant.keyUnifiedComparator;
            this.unifiedCompartor = variant.unifiedComparator;
        }
        
        private class Variant {
            
            final Method associationMethod;
            
            ObjectModelMetadataImpl oppositeObjectModelMetadata;
            
            StandardAssociationPropertyImpl oppositeAssociationPropertyImpl;
            
            StandardAssociationPropertyImpl expectedOppositeAssociationPropertyImpl;
            
            XOrderedMap<String, ScalarProperty> comparatorProperties;
            
            UnifiedComparator<?> keyUnifiedComparator;
            
            UnifiedComparator<?> unifiedComparator;

            Variant(Method associationMethod, Class<?> elementClass) {
                this.associationMethod = associationMethod;
            }
            
            boolean parse(Context context) {
                Ref<Boolean> newMetadataCreatedRef = new Ref<>(false);
                this.oppositeObjectModelMetadata = StandardAssociationPropertyImpl.this.getRelatedObjectModelMetadata(context, false, newMetadataCreatedRef);
                if (this.oppositeObjectModelMetadata.getMode() != ObjectModelMode.REFERENCE) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().oppositeOwnwerTypeMustBeReference(
                                    StandardAssociationPropertyImpl.this, 
                                    this.oppositeObjectModelMetadata.getOwnerClass(), 
                                    this.oppositeObjectModelMetadata.getObjectModelClass(), 
                                    ObjectModelMode.REFERENCE, 
                                    this.oppositeObjectModelMetadata.getMode()
                            )
                    );
                }
                Association association = this.associationMethod.getAnnotation(Association.class);
                if (association == null) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().associationMethodMissAnnotation(
                                    this.associationMethod,
                                    Association.class
                            )
                    );
                }
                
                if (association.opposite().isEmpty()) {
                    if (StandardAssociationPropertyImpl.this.standardReturnClass != Reference.class) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().onlyReferenceCanMissOpposite(
                                        this.associationMethod, 
                                        Association.class, 
                                        Reference.class
                                )
                        );
                    }
                } else {
                    AbstractPropertyImpl oppositePropertyImpl =
                        this.oppositeObjectModelMetadata.getDeclaredProperty(association.opposite());
                    if (oppositePropertyImpl == null) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().invalidOppositeAssociationName(
                                        this.associationMethod,
                                        Association.class,
                                        association.opposite(),
                                        this.oppositeObjectModelMetadata.getObjectModelClass()
                                )
                        );
                    }
                    if (!(oppositePropertyImpl instanceof StandardAssociationPropertyImpl)) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().invalidOppositeAssociationMethod(
                                        this.associationMethod,
                                        Association.class,
                                        association.opposite(),
                                        this.oppositeObjectModelMetadata.getObjectModelClass()));
                    }
                    //TODO: Object model use association to refer itself 
                    //but the runtime instance can not refer itself, how to guarantee it?
                    this.oppositeAssociationPropertyImpl = (StandardAssociationPropertyImpl)oppositePropertyImpl;
                    
                    Variant oppositeVariant = (Variant)this.oppositeAssociationPropertyImpl.variant;
                    if (oppositeVariant.expectedOppositeAssociationPropertyImpl != null) {
                        Variant conflictOpCreator = 
                            (Variant)oppositeVariant.expectedOppositeAssociationPropertyImpl.variant;
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().oppositeReferenceConflict(
                                        this.associationMethod,
                                        conflictOpCreator.associationMethod,
                                        Association.class,
                                        oppositeVariant.associationMethod));
                    }
                    oppositeVariant.expectedOppositeAssociationPropertyImpl = StandardAssociationPropertyImpl.this;
                }
                return newMetadataCreatedRef.get().booleanValue();
            }
            
            void fourthParse(Context context) {
                if (this.oppositeAssociationPropertyImpl != null) {
                    if (this.oppositeAssociationPropertyImpl != this.expectedOppositeAssociationPropertyImpl) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().invalidBidirectionalAssociations(
                                        this.associationMethod,
                                        ((Variant)this.oppositeAssociationPropertyImpl.variant).associationMethod,
                                        Association.class));
                    }
                    if (!VALID_ASSOCIATED_END_TYPE_PAIRS.contains(
                            new AssociatedEndpointTypePair(
                                    StandardAssociationPropertyImpl.this.associatedEndpointType, 
                                    this.oppositeAssociationPropertyImpl.associatedEndpointType))) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().invalidAssociationEndTypePair(
                                        this.associationMethod,
                                        ((Variant)this.oppositeAssociationPropertyImpl.variant).associationMethod,
                                        VALID_ASSOCIATED_END_TYPE_PAIRS_TEXT));
                    }
                }
                
                ReferenceComparisonRule referenceComparisonRuleAnnotation = this.associationMethod.getAnnotation(ReferenceComparisonRule.class);
                Class<?> associationReturnType = this.associationMethod.getReturnType();
                XOrderedMap<String, ScalarProperty> comparatorProperties;
                UnifiedComparator<?> keyUnifiedComparator = null;
                UnifiedComparator<?> unifiedComparator;
                if (referenceComparisonRuleAnnotation != null) {
                    String keyPropertyNames = Joins.join(referenceComparisonRuleAnnotation.value()).trim();
                    if (!keyPropertyNames.isEmpty()) {
                        if (!PROPERTY_NAMES_PATTERN.matcher(keyPropertyNames).matches()) {
                            throw new IllegalProgramException(
                                    LAZY_RESOURCE.get().invalidPropertyLevelReferenceComparasionRuleValue(
                                            StandardAssociationPropertyImpl.this, 
                                            ReferenceComparisonRule.class, 
                                            keyPropertyNames, 
                                            PROPERTY_NAMES_PATTERN.pattern()
                                    )
                            );
                        }
                        ObjectModelMetadata oppositeObjectModelMetadata = this.oppositeObjectModelMetadata;
                        comparatorProperties = new LinkedHashMap<>();
                        for (String propertyName : COMMA_PATTERN.split(keyPropertyNames)) {
                            propertyName = propertyName.trim();
                            Property referencedProperty;
                            try {
                                referencedProperty = oppositeObjectModelMetadata.getProperty(propertyName);
                            } catch (IllegalArgumentException ex) {
                                throw new IllegalProgramException(
                                        LAZY_RESOURCE.get().associationKeyPropertyIsNotExisting(
                                                StandardAssociationPropertyImpl.this.name,
                                                this.associationMethod.getDeclaringClass(), 
                                                ReferenceComparisonRule.class, 
                                                keyPropertyNames, 
                                                propertyName,
                                                oppositeObjectModelMetadata.getObjectModelClass()
                                        )
                                );
                            }
                            if (!(referencedProperty instanceof ScalarProperty)) {
                                throw new IllegalProgramException(
                                        LAZY_RESOURCE.get().associationKeyPropertyIsNotScalar(
                                                StandardAssociationPropertyImpl.this.name,
                                                this.associationMethod.getDeclaringClass(),
                                                ReferenceComparisonRule.class, 
                                                keyPropertyNames, 
                                                propertyName,
                                                oppositeObjectModelMetadata.getObjectModelClass()
                                        )
                                );
                            }
                            if (((ScalarProperty)referencedProperty).isDeferrable()) {
                                throw new IllegalProgramException(
                                        LAZY_RESOURCE.get().associationKeyPropertyIsDeferrable(
                                                StandardAssociationPropertyImpl.this.name,
                                                this.associationMethod.getDeclaringClass(),
                                                ReferenceComparisonRule.class, 
                                                keyPropertyNames, 
                                                propertyName,
                                                oppositeObjectModelMetadata.getObjectModelClass(),
                                                Deferrable.class
                                        )
                                );
                            }
                            if (isUnstableAndUncloneableScalarType(referencedProperty.getReturnClass())) {
                                throw new IllegalProgramException(
                                        LAZY_RESOURCE.get().associationKeyPropertyHasBadType(
                                                StandardAssociationPropertyImpl.this.name,
                                                this.associationMethod.getDeclaringClass(),
                                                ReferenceComparisonRule.class, 
                                                keyPropertyNames, 
                                                propertyName,
                                                oppositeObjectModelMetadata.getObjectModelClass(),
                                                referencedProperty.getReturnClass()
                                        )
                                );
                            }
                            if (comparatorProperties.put(referencedProperty.getName(), (ScalarProperty)referencedProperty) != null) {
                                throw new IllegalProgramException(
                                        LAZY_RESOURCE.get().associationComparatorPropertiesAreDuplicated(
                                                StandardAssociationPropertyImpl.this.name,
                                                this.associationMethod.getDeclaringClass(), 
                                                ReferenceComparisonRule.class, 
                                                keyPropertyNames, 
                                                propertyName));
                            }
                        }
                        comparatorProperties = MACollections.unmodifiable(comparatorProperties);
                    } else {
                        comparatorProperties = MACollections.emptyOrderedMap();
                    }
                } else if (Collection.class.isAssignableFrom(associationReturnType) || 
                        Map.class.isAssignableFrom(associationReturnType)) {
                    comparatorProperties = 
                        this
                        .oppositeObjectModelMetadata
                        .getComparatorProperties();
                } else {
                    if (SortedSet.class.isAssignableFrom(this.associationMethod.getReturnType())) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().noComparatorForSortedSet(
                                        StandardAssociationPropertyImpl.this.name,
                                        this.associationMethod.getDeclaringClass(),
                                        SortedSet.class,
                                        ReferenceComparisonRule.class,
                                        this.oppositeObjectModelMetadata.getObjectModelClass()
                                )
                        );
                    }
                    comparatorProperties = MACollections.emptyOrderedMap();
                }
                
                if (StandardAssociationPropertyImpl.this.keyObjectModelMetadata != null) {
                    if (SortedMap.class.isAssignableFrom(this.associationMethod.getReturnType())) {
                        keyUnifiedComparator = UnifiedComparator.of(StandardAssociationPropertyImpl.this.keyObjectModelMetadata.getOwnerComparator());
                    } else {
                        keyUnifiedComparator = UnifiedComparator.of(StandardAssociationPropertyImpl.this.keyObjectModelMetadata.getOwnerEqualityComparator());
                    }
                }
                
                String[] keyPropertyNames = comparatorProperties.keySet().toArray(EMPTY_STRINGS);
                if (SortedSet.class.isAssignableFrom(associationReturnType)) {                      
                    Comparator<?> comparator = this.oppositeObjectModelMetadata.getOwnerComparator(keyPropertyNames);
                    unifiedComparator = UnifiedComparator.of(comparator);
                } else {
                    EqualityComparator<?> equalityComparator = this.oppositeObjectModelMetadata.getOwnerEqualityComparator(keyPropertyNames);
                    unifiedComparator = UnifiedComparator.of(equalityComparator);
                }
                this.comparatorProperties = comparatorProperties;
                this.keyUnifiedComparator = UnifiedComparator.nullToEmpty(keyUnifiedComparator);
                this.unifiedComparator = UnifiedComparator.nullToEmpty(unifiedComparator);
            }
        }

        @Override
        public boolean isCollection() {
            return !(Reference.class.isAssignableFrom(this.standardReturnClass));
        }

        @Override
        public boolean isReference() {
            return Reference.class.isAssignableFrom(this.standardReturnClass);
        }
    }
    
    private static class ContravarianceAssociationPropertyImpl extends AbstractAssociationPropertyImpl {

        private static final long serialVersionUID = 7354567829334543905L;
        
        private StandardAssociationPropertyImpl covarianceProperty;
        
        private ObjectModelMetadataImpl returnObjectModelMetadata;
        
        private AbstractAssociationPropertyImpl oppositeProperty;
        
        private Class<?> elementClass;
        
        ContravarianceAssociationPropertyImpl(
                ObjectModelMetadataImpl objectModelMetadata,
                Method associationMethod, 
                int id) {
            super(objectModelMetadata, associationMethod, id);
            boolean metContravariance = false;
            for (Annotation annotation : associationMethod.getAnnotations()) {
                if (annotation.annotationType() == Contravariance.class) {
                    metContravariance = true;
                    break;
                }
            }
            if (!metContravariance) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().propertyMustMarkedByContravariance(associationMethod, Contravariance.class)
                );
            }
            ObjectModelMetadataImpl superMetadata = objectModelMetadata.superMetadata;
            if (superMetadata == null) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().mustHaveSuperObjectModel(
                                objectModelMetadata.getObjectModelClass(),
                                associationMethod,
                                Contravariance.class
                        )
                );
            }
            Contravariance contravariance = associationMethod.getAnnotation(Contravariance.class);
            String basePropertyName = contravariance.value();
            if (basePropertyName.isEmpty()) {
                basePropertyName = this.name;
            }
            AbstractAssociationPropertyImpl baseAssociationProperty = null;
            for (ObjectModelMetadataImpl sm = superMetadata; sm != null; sm = sm.superMetadata) {
                baseAssociationProperty = sm.declaredAssociationProperties.get(basePropertyName);
                if (baseAssociationProperty != null) {
                    break;
                }
            }
            if (baseAssociationProperty == null) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().noBaseProperty(
                                associationMethod,
                                Contravariance.class,
                                basePropertyName,
                                superMetadata.getObjectModelClass()
                        )
                );
            }
            if (baseAssociationProperty.getReturnClass() != associationMethod.getReturnType()) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().invalidBasePropertyType(
                                associationMethod,
                                Contravariance.class,
                                basePropertyName,
                                superMetadata.getObjectModelClass(),
                                baseAssociationProperty.getReturnClass()
                        )
                );
            }
            ClassInfo<?> classInfo = 
                    ClassInfo
                    .of(this.returnType)
                    .getAncestor(baseAssociationProperty.getStandardReturnClass());
            if (!(classInfo.getRawType() instanceof ParameterizedType)) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().associationMethodMissGenericArguments(associationMethod)
                );
            }
            Type[] types = ((ParameterizedType)classInfo.getRawType()).getActualTypeArguments();
            if (baseAssociationProperty.getKeyClass() != null && 
                    baseAssociationProperty.getKeyClass() != GenericTypes.eraseGenericType(types[0])) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().invalidBasePropertyKeyType(
                                associationMethod,
                                Contravariance.class,
                                basePropertyName,
                                Map.class,
                                GenericTypes.eraseGenericType(types[0]),
                                superMetadata.getObjectModelClass(),
                                baseAssociationProperty.getKeyClass()
                        )
                );
            }
            Class<?> elementClass = GenericTypes.eraseGenericType(types[types.length - 1]);
            Class<?> baseElementClass = baseAssociationProperty.getElementClass();
            if (baseElementClass == elementClass || !baseElementClass.isAssignableFrom(elementClass)) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().invalidBasePropertyElementType(
                                associationMethod,
                                Contravariance.class,
                                basePropertyName,
                                elementClass,
                                superMetadata.getObjectModelClass(),
                                baseElementClass
                        )
                );
            }
            if (baseAssociationProperty instanceof ContravarianceAssociationPropertyImpl) {
                this.covarianceProperty = ((ContravarianceAssociationPropertyImpl)baseAssociationProperty).covarianceProperty;
            } else {
                this.covarianceProperty = (StandardAssociationPropertyImpl)baseAssociationProperty;
            }
            this.elementClass = elementClass;
        }

        @Override
        public ObjectModelMetadata getReturnObjectModelMetadata() {
            return this.returnObjectModelMetadata;
        }

        @Override
        public AssociationProperty getOppositeProperty() {
            return this.oppositeProperty;
        }

        @Override
        public AssociatedEndpointType getAssociatedEndpointType() {
            return this.covarianceProperty.getAssociatedEndpointType();
        }

        @Override
        public XOrderedMap<String, ScalarProperty> getComparatorProperties() {
            return this.covarianceProperty.getComparatorProperties();
        }

        @Override
        public UnifiedComparator<?> getCollectionUnifiedComparator() {
            return this.covarianceProperty.getCollectionUnifiedComparator();
        }

        @Override
        public Class<?> getStandardReturnClass() {
            return this.covarianceProperty.getStandardReturnClass();
        }

        @Override
        public Class<?> getKeyClass() {
            return this.covarianceProperty.getKeyClass();
        }

        @Override
        public ObjectModelMetadata getKeyObjectModelMetadata() {
            return this.covarianceProperty.getKeyObjectModelMetadata();
        }

        @Override
        public UnifiedComparator<?> getKeyUnifiedComparator() {
            return this.covarianceProperty.getKeyUnifiedComparator();
        }

        @Override
        public Class<?> getElementClass() {
            return this.elementClass;
        }

        @Override
        public ParameterizedType getReturnType() {
            return this.covarianceProperty.getReturnType();
        }

        @Override
        public AssociationProperty getCovarianceProperty() {
            return this.covarianceProperty;
        }

        @Override
        public boolean isCollection() {
            return this.covarianceProperty.isCollection();
        }

        @Override
        public boolean isReference() {
            return this.covarianceProperty.isReference();
        }

        @Override
        boolean parse(Context context) {
            Ref<Boolean> newMetadataCreatedRef = new Ref<>(false);
            this.returnObjectModelMetadata = 
                    this.getRelatedObjectModelMetadata(context, false, newMetadataCreatedRef);
            
            return newMetadataCreatedRef.get();
        }

        @Override
        void fourthParse(Context context) {
            
            Object variant = this.covarianceProperty.variant;
            if (variant instanceof StandardAssociationPropertyImpl.Variant) {
                this.oppositeProperty = ((StandardAssociationPropertyImpl.Variant)variant).oppositeAssociationPropertyImpl;
            } else {
                this.oppositeProperty = (StandardAssociationPropertyImpl)variant;
            }
            if (this.oppositeProperty == null) {
                return;
            }
            
            /*
             * The secondary parse have not been executed, 
             * Don't inovke this.returnObjectMetadata.getProperties() here
             */
            for (ObjectModelMetadataImpl oppositeObjectModelMetadata = this.returnObjectModelMetadata;
                    oppositeObjectModelMetadata != null;
                    oppositeObjectModelMetadata = oppositeObjectModelMetadata.superMetadata) {
                for (AbstractAssociationPropertyImpl associationProperty : oppositeObjectModelMetadata.declaredAssociationProperties.values()) {
                    StandardAssociationPropertyImpl oppositeCovarianceProperty = (StandardAssociationPropertyImpl)associationProperty.getCovarianceProperty();
                    if (oppositeCovarianceProperty == this.oppositeProperty) {
                        this.oppositeProperty = associationProperty;
                        return;
                    }
                }
            }
        }
        
        
    }
    
    private final static class OwnerEntry {
        
        final Method staticMethodToGetObjectModel;
        
        final Class<?> objectModelInterface;
        
        final ObjectModelFactoryProvider objectModelFactoryProvider;
        
        final ObjectModelMode objectModelType;
        
        final String declaredPropertiesOrder;

        public OwnerEntry(
                Method staticMethodToGetObjectModel, 
                Class<?> objectModelInterface,
                ObjectModelFactoryProvider objectModelFactoryProvider,
                ObjectModelMode objectModelType,
                String declaredPropertiesOrder) {
            if (staticMethodToGetObjectModel.getDeclaringClass() != 
                objectModelInterface.getDeclaringClass()) {
                throw new AssertionError();
            }
            this.staticMethodToGetObjectModel = staticMethodToGetObjectModel;
            this.objectModelInterface = objectModelInterface;
            this.objectModelFactoryProvider = objectModelFactoryProvider;
            this.objectModelType = objectModelType;
            this.declaredPropertiesOrder = declaredPropertiesOrder;
        }

        public Class<?> getDeclaringClass() {
            return this.staticMethodToGetObjectModel.getDeclaringClass();
        }
    }
    
    private static class AssociatedEndpointTypePair {
        
        final AssociatedEndpointType type1;
        
        final AssociatedEndpointType type2;

        public AssociatedEndpointTypePair(AssociatedEndpointType endType1,
                AssociatedEndpointType endType2) {
            this.type1 = endType1;
            this.type2 = endType2;
        }

        @Override
        public int hashCode() {
            return this.type1.ordinal() ^ this.type2.ordinal();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof AssociatedEndpointTypePair)) {
                return false;
            }
            AssociatedEndpointTypePair other = (AssociatedEndpointTypePair)obj;
            return 
                this.type1 == other.type1 &&
                this.type2 == other.type2;
        }
        
    } 

    private static class Context {
        
        private final Map<Class<?>, ObjectModelMetadataImpl> metadatas;
        
        private final Map<Class<?>, ObjectModelMetadataImpl> oldMetadatas;
        
        Context(Map<Class<?>, ObjectModelMetadataImpl> oldMetadatas) {
            this.metadatas = new HashMap<>();
            this.oldMetadatas = MACollections.unmodifiable(oldMetadatas);
        }
        
        ObjectModelMetadataImpl getMetadata(Class<?> ownerClass) {
            ObjectModelMetadataImpl metadata = this.metadatas.get(ownerClass);
            if (metadata == null) {
                metadata = this.oldMetadatas.get(ownerClass);
            }
            return metadata;
        }
    }
    
    private static class ObjectModelMetadataReplacement implements Serializable {

        private static final long serialVersionUID = -1231034686860968131L;
        
        private ObjectModelMetadata target;
        
        ObjectModelMetadataReplacement(ObjectModelMetadataImpl target) {
            this.target = Arguments.mustNotBeNull("target", target);
        }
        
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.writeObject(this.target.getOwnerClass());
        }
        
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            Class<?> ownerClass = (Class<?>)in.readObject();
            this.target = Metadatas.of(ownerClass);
        }
        
        private Object readResolve() throws ObjectStreamException {
            return this.target;
        }
    }
    
    private static class PropertyReplacement implements Serializable {
        
        private static final long serialVersionUID = -3167729057124212062L;
        private Property target;
        
        PropertyReplacement(Property target) {
            this.target = target;
        }
        
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.writeObject(this.target.getDeclaringObjectModelMetadata());
            out.writeInt(this.target.getId());
        }
        
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            ObjectModelMetadata objectModelMetadata = (ObjectModelMetadata)in.readObject();
            this.target = objectModelMetadata.getProperty(in.readInt());
        }
        
        private Object readResolve() throws ObjectStreamException {
            return this.target;
        }
    }
    
    private static abstract class AbstractComparatorGenerator {
        
        private static final String REPLACEMENT = "{REPLACEMENT}";
        
        protected static final String INSTANCE = "{INSTANCE}";
        
        protected Class<?> interfaceType;
        
        protected ObjectModelMetadata objectModelMetadata;
        
        protected Collection<ScalarProperty> scalarProperties;
        
        protected NavigableSet<ScalarProperty> orderFixedScalarProperties;
        
        protected String className;
        
        protected String internalName;
        
        protected String ownerInternalName;
        
        protected String ownerDescriptor;
        
        protected String objectModelInternalName;
        
        private Class<?> replacementClass;
        
        private Object generatedObject;
        
        AbstractComparatorGenerator(
                Class<?> interfaceType,
                ObjectModelMetadata objectModelMetadata, 
                Collection<ScalarProperty> scalarProperties) {
            this.interfaceType = Arguments.mustBeAnyOfValue(
                    "interfaceType", 
                    interfaceType, 
                    FrozenComparator.class, 
                    FrozenEqualityComparator.class
            );
            Arguments.mustBeAnyOfValue(
                    "UnifiedComparator.of(scalarProperties).unwrap()", 
                    UnifiedComparator.of(scalarProperties).unwrap(), 
                    SCALAR_PROPERTY_COMPARATOR,
                    SCALAR_PROPERTY_EQUALITY_COMPARATOR);
            this.objectModelMetadata = objectModelMetadata;
            if (scalarProperties.isEmpty()) {
                scalarProperties.addAll(objectModelMetadata.getScalarProperties().values());
                this.scalarProperties = scalarProperties;
                this.className = embeddableComparatorClassName(this.interfaceType, objectModelMetadata);
            } else {
                for (ScalarProperty scalarProperty : scalarProperties) {
                    if (scalarProperty.isDeferrable()) {
                        throw new IllegalArgumentException(
                                LAZY_RESOURCE.get().deferrablePropertyCanNotBeComparatorField(
                                        scalarProperty, 
                                        Deferrable.class,
                                        this.interfaceType
                                )
                        );
                    }
                    Class<?> returnType = scalarProperty.getReturnClass();
                    if (isUnstableAndUncloneableScalarType(returnType)) {
                        throw new IllegalArgumentException(
                                LAZY_RESOURCE.get().badTypescalarPropertyCanNotBeComparatorField(
                                        scalarProperty,
                                        returnType,
                                        this.interfaceType
                                )
                        );
                    }
                }
                this.scalarProperties = Arguments.mustNotBeEmpty("scalarProperties", scalarProperties);
                this.className = 
                        objectModelMetadata.getOwnerClass().getName() + 
                        '{' +
                        this.interfaceType.getSimpleName() +
                        "=>" +
                        Joins.join(
                                scalarProperties, 
                                "+",
                                new Func<ScalarProperty, String>() {
                                    @Override
                                    public String run(ScalarProperty scalarProperty) {
                                        return Integer.toString(scalarProperty.getId(), Character.MIN_RADIX);
                                    }
                                }
                        ) +
                        ':' +
                        NAME_POSTFIX +
                        "}";
            }
            if (scalarProperties instanceof NavigableSet<?>) {
                this.orderFixedScalarProperties = (NavigableSet<ScalarProperty>)scalarProperties;
            } else {
                NavigableSet<ScalarProperty> orderFixedScalarProperties = new TreeSet<>(SCALAR_PROPERTY_COMPARATOR);
                orderFixedScalarProperties.addAll(scalarProperties);
                this.orderFixedScalarProperties = orderFixedScalarProperties;
            }
            this.internalName = this.className.replace('.', '/');
            this.ownerInternalName = ASM.getInternalName(objectModelMetadata.getOwnerClass());
            this.ownerDescriptor = ASM.getDescriptor(objectModelMetadata.getOwnerClass());
            this.objectModelInternalName = ASM.getInternalName(objectModelMetadata.getObjectModelClass());
            this.replacementClass = 
                    this.interfaceType == FrozenEqualityComparator.class ? 
                            OwnerEqualityComparatorWritingReplacement.class : 
                            OwnerComparatorWritingReplacement.class;
            this.generatedObject = this.generate();
        }

        protected abstract void generateMethods(ClassVisitor cv);
        
        protected final Object getGeneratedObject() {
            return this.generatedObject;
        }
        
        protected Map<ObjectModelMetadata, Integer> allocateObjectModelSlots(
                SlotAllocator slotAllocator, String variableNamePostfix) {
            Map<ObjectModelMetadata, Integer> map = new HashMap<>();
            for (ScalarProperty scalarProperty : this.orderFixedScalarProperties) {
                ObjectModelMetadata objectModelMetadata = scalarProperty.getDeclaringObjectModelMetadata();
                String variableName =
                        objectModelMetadata
                        .getObjectModelClass()
                        .getName()+
                        ':' +
                        variableNamePostfix;
                if (!map.containsKey(objectModelMetadata)) {
                    map.put(objectModelMetadata, slotAllocator.aSlot(variableName));
                }
            }
            return map;
        }
        
        protected static String embeddableComparatorClassName(
                Class<?> interfaceType,
                ObjectModelMetadata objectModelMetadata) {
            return 
                    objectModelMetadata.getOwnerClass().getName() + 
                    "{Embeddable" +
                    interfaceType.getSimpleName() +
                    ':' +
                    NAME_POSTFIX +
                    "}";
        }

        private Object generate() {
            Action<ClassVisitor> cvAction = new Action<ClassVisitor>() {
                @Override
                public void run(ClassVisitor cv) {
                    AbstractComparatorGenerator.this.generate(cv);
                }
            };
            Class<?> clazz = ASM.loadDynamicClass(
                    this.objectModelMetadata.getObjectModelClass().getClassLoader(), 
                    this.className, 
                    this.objectModelMetadata.getObjectModelClass().getProtectionDomain(),
                    cvAction);
            try {
                return clazz.getDeclaredField(INSTANCE).get(null);
            } catch (IllegalAccessException | NoSuchFieldException ex) {
                throw UncheckedException.rethrow(ex);
            }
        }
        
        private void generate(ClassVisitor cv) {
            cv.visit(
                    Opcodes.V1_7, 
                    Opcodes.ACC_PUBLIC, 
                    this.internalName, 
                    null, 
                    "java/lang/Object", 
                    new String[] {
                            ASM.getInternalName(this.interfaceType),
                            ASM.getInternalName(HashCalculator.class),
                            ASM.getInternalName(Serializable.class)
                    });
            this.generateSerialVersionUID(cv);
            this.generateReplacement(cv);
            this.generateInstance(cv);
            this.generateClinit(cv);
            this.generateInit(cv);
            this.generateWriteReplace(cv);
            this.generateFreezeBridge(cv, true);
            this.generateFreeze(cv, true);
            this.generateFreezeBridge(cv, false);
            this.generateFreeze(cv, false);
            this.generateHashCodeBridge(cv);
            this.generateHashCode(cv);
            this.generateMethods(cv);
            cv.visitEnd();
        }
        
        private void generateSerialVersionUID(ClassVisitor cv) {
            long serialVersionUID = 0;
            for (ScalarProperty scalarProperty : this.scalarProperties) {
                serialVersionUID = 
                        31 * 31 * serialVersionUID + 
                        31 * scalarProperty.getName().hashCode() +
                        scalarProperty.getReturnClass().hashCode();
            }
            cv.visitField(
                    Opcodes.ACC_STATIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, 
                    "serialVersionUID", 
                    "J", 
                    null, 
                    serialVersionUID
            ).visitEnd();
        }
        
        private void generateReplacement(ClassVisitor cv) {
            cv.visitField(
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, 
                    REPLACEMENT, 
                    "Ljava/lang/Object;", 
                    null,
                    null
            ).visitEnd();
        }
        
        private void generateInstance(ClassVisitor cv) {
            cv.visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, 
                    INSTANCE, 
                    ASM.getDescriptor(this.interfaceType), 
                    null,
                    null
            ).visitEnd();
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
            
            mv.visitTypeInsn(Opcodes.NEW, ASM.getInternalName(this.replacementClass));
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn(org.babyfish.org.objectweb.asm.Type.getType(this.objectModelMetadata.getOwnerClass()));
            mv.visitLdcInsn(this.scalarProperties.size());
            mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
            int index = 0;
            for (ScalarProperty scalarProperty : this.scalarProperties) {
                mv.visitInsn(Opcodes.DUP);
                mv.visitLdcInsn(index++);
                mv.visitLdcInsn(scalarProperty.getId());
                mv.visitInsn(Opcodes.IASTORE);
            }
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    ASM.getInternalName(this.replacementClass),
                    "<init>",
                    "(Ljava/lang/Class;[I)V",
                    false);
            mv.visitFieldInsn(
                    Opcodes.PUTSTATIC, 
                    this.internalName, 
                    REPLACEMENT, 
                    "Ljava/lang/Object;");
            
            ASM.visitNewObjectWithoutParameters(mv, this.internalName);
            mv.visitFieldInsn(
                    Opcodes.PUTSTATIC, 
                    this.internalName, 
                    INSTANCE, 
                    ASM.getDescriptor(this.interfaceType));
            
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateInit(ClassVisitor cv) {
            XMethodVisitor mv = ASM.visitMethod(
                    cv,
                    Opcodes.ACC_PRIVATE, 
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
        
        private void generateWriteReplace(ClassVisitor cv) {
            XMethodVisitor mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PROTECTED, 
                    "writeReplace", 
                    "()Ljava/lang/Object;", 
                    null, 
                    new String[] { ASM.getInternalName(ObjectStreamException.class) });
            mv.visitCode();
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    this.internalName, 
                    REPLACEMENT, 
                    "Ljava/lang/Object;");
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateHashCodeBridge(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_BRIDGE, 
                    "hashCode", 
                    "(Ljava/lang/Object;)I", 
                    null,
                    null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitTypeInsn(Opcodes.CHECKCAST, this.ownerInternalName);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    internalName, 
                    "hashCode", 
                    '(' +  this.ownerDescriptor + ")I",
                    false);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateHashCode(ClassVisitor cv) {
            XMethodVisitor mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PUBLIC, 
                    "hashCode", 
                    '(' + this.ownerDescriptor + ")I", 
                    null,
                    null);
            mv.visitCode();
            
            Label nonNullLabel = new Label();
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitJumpInsn(Opcodes.IFNONNULL, nonNullLabel);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitLabel(nonNullLabel);
            
            final int hashIndex = mv.iSlot("hash");
            /*
             * Use all the super ObjectModels because some scalar properties 
             * may belong to the super ObjectModelMetadatas
             */
            final Map<ObjectModelMetadata, Integer> objectModelIndexes = 
                    this.allocateObjectModelSlots(mv, "ObjectModel");
            for (Entry<ObjectModelMetadata, Integer> entry : objectModelIndexes.entrySet()) {
                ObjectModelFactoryProvider.visitGetObjectModel(mv, entry.getKey(), 1);
                mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(entry.getKey().getObjectModelClass()));
                mv.visitVarInsn(Opcodes.ASTORE, entry.getValue());
            }
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitVarInsn(Opcodes.ISTORE, hashIndex);
            for (final ScalarProperty scalarProperty : this.orderFixedScalarProperties) {
                mv.visitVarInsn(Opcodes.ILOAD, hashIndex);
                mv.visitLdcInsn(31);
                mv.visitInsn(Opcodes.IMUL);
                if (scalarProperty.isEmbeded()) {
                    mv.visitFieldInsn(
                            Opcodes.GETSTATIC, 
                            embeddableComparatorClassName(
                                    FrozenEqualityComparator.class, 
                                    scalarProperty.getReturnObjectModelMetadata()
                            ).replace('.', '/'), 
                            INSTANCE, 
                            ASM.getDescriptor(FrozenEqualityComparator.class));
                }
                mv.visitVarInsn(Opcodes.ALOAD, objectModelIndexes.get(scalarProperty.getDeclaringObjectModelMetadata()));
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        ASM.getInternalName(scalarProperty.getDeclaringObjectModelMetadata().getObjectModelClass()), 
                        scalarProperty.getGetterName(), 
                        "()" + ASM.getDescriptor(scalarProperty.getReturnClass()),
                        true);
                if (scalarProperty.isEmbeded()) {
                    mv.visitMethodInsn(
                            Opcodes.INVOKEINTERFACE, 
                            ASM.getInternalName(EqualityComparator.class), 
                            "hashCode", 
                            "(Ljava/lang/Object;)I",
                            true);
                } else {
                    mv.visitHashCode(scalarProperty.getReturnClass(), true);
                }
                mv.visitInsn(Opcodes.IADD);
                mv.visitVarInsn(Opcodes.ISTORE, hashIndex);
            }
            mv.visitVarInsn(Opcodes.ILOAD, hashIndex);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateFreezeBridge(ClassVisitor cv, boolean freeze) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_BRIDGE, 
                    freeze ? "freeze" : "unfreeze", 
                    "(Ljava/lang/Object;" +
                    ASM.getDescriptor(FrozenContext.class) +
                    ")V", 
                    null,
                    null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitTypeInsn(Opcodes.CHECKCAST, this.ownerInternalName);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    internalName, 
                    freeze ? "freeze" : "unfreeze", 
                    '(' +
                    this.ownerDescriptor +
                    ASM.getDescriptor(FrozenContext.class) + 
                    ")V",
                    false);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateFreeze(ClassVisitor cv, boolean freeze) {  
            XMethodVisitor mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PUBLIC, 
                    freeze ? "freeze" : "unfreeze", 
                    '(' +
                    this.ownerDescriptor +
                    ASM.getDescriptor(FrozenContext.class) + 
                    ")V", 
                    null,
                    null);
            mv.visitCode();
            /* 
             * This method depend on ObjectModel.freeScalar(int, FrozenContext)
             * or ObjectModel.unfreeze(int, FrozenContext) that already
             * handle the inheritance, so we need not and can not
             * use all the super ObjectModels here, we need only 
             * and can only use the current ObjectModel here.
             */
            final int objectModelIndex = mv.aSlot("objectModel");
            ObjectModelFactoryProvider.visitGetObjectModel(mv, this.objectModelMetadata, 1);
            mv.visitTypeInsn(Opcodes.CHECKCAST, this.objectModelInternalName);
            mv.visitVarInsn(Opcodes.ASTORE, objectModelIndex);
            for (ScalarProperty scalarProperty : this.scalarProperties) {
                mv.visitVarInsn(Opcodes.ALOAD, objectModelIndex);
                mv.visitLdcInsn(scalarProperty.getId());
                mv.visitVarInsn(Opcodes.ALOAD, 2);
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        ASM.getInternalName(ObjectModel.class),
                        freeze ? "freezeScalar" : "unfreezeScalar",
                        "(I" + ASM.getDescriptor(FrozenContext.class) + ")V",
                        true);
            }
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }
    
    private static class FrozenComparatorGenerator extends AbstractComparatorGenerator {

        FrozenComparatorGenerator(
                ObjectModelMetadata objectModelMetadata,
                XOrderedSet<ScalarProperty> scalarProperties) {
            super(FrozenComparator.class, objectModelMetadata, scalarProperties);
        }
        
        public FrozenComparator<?> getFrozenComparator() {
            return (FrozenComparator<?>)this.getGeneratedObject();
        }

        @Override
        protected void generateMethods(ClassVisitor cv) {
            this.generateCompareBridge(cv);
            this.generateCompare(cv);
        }
        
        private void generateCompareBridge(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_BRIDGE | Opcodes.ACC_SYNTHETIC, 
                    "compare", 
                    "(Ljava/lang/Object;Ljava/lang/Object;)I", 
                    null,
                    null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitTypeInsn(Opcodes.CHECKCAST, this.ownerInternalName);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitTypeInsn(Opcodes.CHECKCAST, this.ownerInternalName);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    internalName, 
                    "compare", 
                    '(' + this.ownerDescriptor + this.ownerDescriptor + ")I",
                    false);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateCompare(ClassVisitor cv) {
            XMethodVisitor mv = ASM.visitMethod(
                    cv,
                    Opcodes.ACC_PUBLIC, 
                    "compare", 
                    '(' + this.ownerDescriptor + this.ownerDescriptor + ")I", 
                    null,
                    null);
            mv.visitCode();
            
            Label aIsNotNullLabel = new Label();
            Label isSameLabel = new Label();
            Label bIsNotNullLabel = new Label();
            
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitJumpInsn(Opcodes.IFNONNULL, aIsNotNullLabel);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitJumpInsn(Opcodes.IF_ACMPEQ, isSameLabel);
            mv.visitInsn(Opcodes.ICONST_M1);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitLabel(isSameLabel);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitLabel(aIsNotNullLabel);
            
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitJumpInsn(Opcodes.IFNONNULL, bIsNotNullLabel);
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitLabel(bIsNotNullLabel);
            
            int cmpIndex = mv.iSlot("cmp");
            /*
             * Use all the super ObjectModels because some scalar properties 
             * may belong to the super ObjectModelMetadatas
             */
            final Map<ObjectModelMetadata, Integer> objectModel1Indexes = 
                    this.allocateObjectModelSlots(mv, "ObjectModel1");
            for (Entry<ObjectModelMetadata, Integer> entry : objectModel1Indexes.entrySet()) {
                ObjectModelFactoryProvider.visitGetObjectModel(mv, entry.getKey(), 1);
                mv.visitTypeInsn(Opcodes.CHECKCAST, this.objectModelInternalName);
                mv.visitVarInsn(Opcodes.ASTORE, entry.getValue());
            }
            final Map<ObjectModelMetadata, Integer> objectModel2Indexes = 
                    this.allocateObjectModelSlots(mv, "ObjectModel2");
            for (Entry<ObjectModelMetadata, Integer> entry : objectModel2Indexes.entrySet()) {
                ObjectModelFactoryProvider.visitGetObjectModel(mv, entry.getKey(), 2);
                mv.visitTypeInsn(Opcodes.CHECKCAST, this.objectModelInternalName);
                mv.visitVarInsn(Opcodes.ASTORE, entry.getValue());
            }
            for (final ScalarProperty scalarProperty : scalarProperties) {
                if (scalarProperty.isEmbeded()) {
                    mv.visitFieldInsn(
                            Opcodes.GETSTATIC, 
                            embeddableComparatorClassName(
                                    FrozenComparator.class, 
                                    scalarProperty.getReturnObjectModelMetadata()
                            ).replace('.', '/'), 
                            INSTANCE, 
                            ASM.getDescriptor(FrozenComparator.class));
                }
                for (int i = 0; i < 2; i++) {
                    final Map<ObjectModelMetadata, Integer> objectModelIndexes = i == 0 ? objectModel1Indexes : objectModel2Indexes;
                    mv.visitVarInsn(Opcodes.ALOAD, objectModelIndexes.get(scalarProperty.getDeclaringObjectModelMetadata()));
                    mv.visitMethodInsn(
                            Opcodes.INVOKEINTERFACE, 
                            FrozenComparatorGenerator.this.objectModelInternalName, 
                            scalarProperty.getGetterName(), 
                            "()" + ASM.getDescriptor(scalarProperty.getReturnClass()),
                            true);
                }
                if (scalarProperty.isEmbeded()) {
                    mv.visitMethodInsn(
                            Opcodes.INVOKEINTERFACE, 
                            ASM.getInternalName(FrozenComparator.class), 
                            "compare", 
                            "(Ljava/lang/Object;Ljava/lang/Object;)I",
                            true);
                } else {
                    mv.visitCompare(
                        scalarProperty.getReturnObjectModelMetadata() != null ?
                                scalarProperty.getReturnObjectModelMetadata().getObjectModelClass() :
                                scalarProperty.getReturnClass(),
                        false
                    );
                }
                mv.visitVarInsn(Opcodes.ISTORE, cmpIndex);
                mv.visitVarInsn(Opcodes.ILOAD, cmpIndex);
                Label continueLabel = new Label();
                mv.visitJumpInsn(Opcodes.IFEQ, continueLabel);
                mv.visitVarInsn(Opcodes.ILOAD, cmpIndex);
                mv.visitInsn(Opcodes.IRETURN);
                mv.visitLabel(continueLabel);
            }
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }
    
    private static class FrozenEqualityComparatorGenerator extends AbstractComparatorGenerator {
        
        FrozenEqualityComparatorGenerator(
                ObjectModelMetadata objectModelMetadata,
                NavigableSet<ScalarProperty> scalarProperties) {
            super(FrozenEqualityComparator.class, objectModelMetadata, scalarProperties);
        }
        
        public FrozenEqualityComparator<?> getFrozenEqualityComparator() {
            return (FrozenEqualityComparator<?>)this.getGeneratedObject();
        }

        @Override
        protected void generateMethods(ClassVisitor cv) {
            this.generateEqualsBridge(cv);
            this.generateEquals(cv);
        }
        
        private void generateEqualsBridge(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_BRIDGE, 
                    "equals", 
                    "(Ljava/lang/Object;Ljava/lang/Object;)Z", 
                    null,
                    null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitTypeInsn(Opcodes.CHECKCAST, this.ownerInternalName);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitTypeInsn(Opcodes.CHECKCAST, this.ownerInternalName);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    internalName, 
                    "equals", 
                    '(' + this.ownerDescriptor + this.ownerDescriptor + ")Z",
                    false);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateEquals(ClassVisitor cv) {
            XMethodVisitor mv = ASM.visitMethod(
                    cv,
                    Opcodes.ACC_PUBLIC, 
                    "equals", 
                    '(' + this.ownerDescriptor + this.ownerDescriptor + ")Z", 
                    null,
                    null);
            mv.visitCode();
            
            Label aIsNotNullLabel = new Label();
            Label isSameLabel = new Label();
            Label bIsNotNullLabel = new Label();
            
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitJumpInsn(Opcodes.IFNONNULL, aIsNotNullLabel);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitJumpInsn(Opcodes.IF_ACMPEQ, isSameLabel);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitLabel(isSameLabel);
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitLabel(aIsNotNullLabel);
            
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitJumpInsn(Opcodes.IFNONNULL, bIsNotNullLabel);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitLabel(bIsNotNullLabel);
            
            /*
             * Use all the super ObjectModels because some scalar properties 
             * may belong to the super ObjectModelMetadatas
             */
            final Map<ObjectModelMetadata, Integer> objectModel1Indexes = 
                    this.allocateObjectModelSlots(mv, "ObjectModel1");
            for (Entry<ObjectModelMetadata, Integer> entry : objectModel1Indexes.entrySet()) {
                ObjectModelFactoryProvider.visitGetObjectModel(mv, entry.getKey(), 1);
                mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(entry.getKey().getObjectModelClass()));
                mv.visitVarInsn(Opcodes.ASTORE, entry.getValue());
            }
            final Map<ObjectModelMetadata, Integer> objectModel2Indexes = 
                    this.allocateObjectModelSlots(mv, "ObjectModel2");
            for (Entry<ObjectModelMetadata, Integer> entry : objectModel2Indexes.entrySet()) {
                ObjectModelFactoryProvider.visitGetObjectModel(mv, entry.getKey(), 2);
                mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(entry.getKey().getObjectModelClass()));
                mv.visitVarInsn(Opcodes.ASTORE, entry.getValue());
            }
            for (final ScalarProperty scalarProperty : this.scalarProperties) {
                if (scalarProperty.isEmbeded()) {
                    mv.visitFieldInsn(
                            Opcodes.GETSTATIC, 
                            embeddableComparatorClassName(
                                    FrozenEqualityComparator.class, 
                                    scalarProperty.getReturnObjectModelMetadata()
                            ).replace('.', '/'), 
                            INSTANCE, 
                            ASM.getDescriptor(FrozenEqualityComparator.class));
                }
                for (int i = 0; i < 2; i++) {
                    final Map<ObjectModelMetadata, Integer> objectModelIndexes = i == 0 ? objectModel1Indexes : objectModel2Indexes;
                    mv.visitVarInsn(Opcodes.ALOAD, objectModelIndexes.get(scalarProperty.getDeclaringObjectModelMetadata()));
                    mv.visitMethodInsn(
                            Opcodes.INVOKEINTERFACE, 
                            ASM.getInternalName(ASM.getInternalName(scalarProperty.getDeclaringObjectModelMetadata().getObjectModelClass())), 
                            scalarProperty.getGetterName(), 
                            "()" + ASM.getDescriptor(scalarProperty.getReturnClass()),
                            true);
                }
                if (scalarProperty.isEmbeded()) {
                    mv.visitMethodInsn(
                            Opcodes.INVOKEINTERFACE, 
                            ASM.getInternalName(EqualityComparator.class), 
                            "equals", 
                            "(Ljava/lang/Object;Ljava/lang/Object;)Z",
                            true);
                } else {
                    mv.visitEquals(
                            scalarProperty.getReturnObjectModelMetadata() != null ?
                                    scalarProperty.getReturnObjectModelMetadata().getObjectModelClass() :
                                    scalarProperty.getReturnClass(), 
                            true);
                }
                Label continueLabel = new Label();
                mv.visitJumpInsn(Opcodes.IFNE, continueLabel);
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitInsn(Opcodes.IRETURN);
                mv.visitLabel(continueLabel);
            }
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitMaxs(0, 0);
        }
    }
    
    private interface Resource {
        
        String badDerivedProvider(
                Class<?> objectModelType,
                Class<? extends ObjectModelFactoryProvider> providerType,
                Class<?> superObjectModelType,
                Class<? extends ObjectModelFactoryProvider> superProviderType);

        String badDerivedMode(
                Class<?> objectModelType,
                ObjectModelMode mode,
                Class<?> superObjectModelType,
                ObjectModelMode superMode);
        
        String badRelatedObjectModelProvider(
                Class<?> objectModelType,
                Class<? extends ObjectModelFactoryProvider> providerType,
                String relationMethodName,
                Class<?> relatedType,
                Class<?> relationObjectModelType,
                Class<? extends ObjectModelFactoryProvider> associatiedProviderType);
        
        String duplicatedStaticMethodToGetOM(
                Method method,
                Method annotherMethod,
                Class<StaticMethodToGetObjectModel> staticMethodToGetObjectModelType);
        
        String methodToGetOMMustBeStatic(
                Method method,
                Class<StaticMethodToGetObjectModel> staticMethodToGetObjectModelType);
        
        String staticMethodToGetOMMustNotBeDefault(
                Method method,
                Class<StaticMethodToGetObjectModel> staticMethodToGetObjectModelType);
        
        String badStaticMethodToGetOM(
                Method method,
                Class<StaticMethodToGetObjectModel> staticMethodToGetObjectModelType,
                Class<?> ownerClass,
                Class<ObjectModel> objectModelType,
                Class<?> customizedOMType);
        
        String noStaticMethodToGetOM(
                Class<?> ownerType,
                Class<StaticMethodToGetObjectModel> staticMethodToGetObjectModelType);
        
        String objectModelClassMustBeInterface(
                Class<?> objectModelType, 
                Class<ObjectModelDeclaration> objectModelDeclarationType);
        
        String badSuperInterfaceOfObjectModel(
                Class<?> objectModelType,
                Class<ObjectModel> objectModelTypeConstant);
        
        String tooManyObjectModelClasses(
                Class<?> objectModelType, 
                Class<ObjectModelDeclaration> objectModelDeclarationType);
        
        String noObjectModelClasses(
                Class<?> objectModelType, 
                Class<ObjectModelDeclaration> objectModelDeclarationType);
        
        String invalidAssociationClass(
                Method associationMethod,
                Collection<Class<?>> validReturnTypes);
        
        String associationMethodMissGenericArguments(
                Method associationMethod);
        
        String associationMethodMustHaveNoParameters(
                Method associationMethod);
        
        String associationMethodMustThrowNoExceptions(
                Method associationMethod);
        
        String associationMethodMissAnnotation(
                Method associationMethod, 
                Class<Association> associationType);
        
        String invalidOppositeAssociationName(
                Method associationMethod,
                Class<Association> associationType,
                String oppositeAssociationName,
                Class<?> oppositeObjectModelType);
        
        String invalidOppositeAssociationMethod(
                Method associationMethod,
                Class<Association> associationType,
                String oppositeAssociationName,
                Class<?> oppositeObjectModelType);
        
        String oppositeReferenceConflict(
                Method associationMethod,
                Method conflictAssociationMethod,
                Class<Association> associationType,
                Method oppositeAssociationMethod);
        
        String invalidBidirectionalAssociations(
                Method associationMethod1,
                Method associationMethod2,
                Class<Association> associationType);
        
        String invalidAssociationEndTypePair(
                Method associationMethod1,
                Method associationMethod2,
                String validateAssociatedEndpointTypePairs/* private type, just use string. */);
        
        String mustHaveNoTypeParameters(Method method);
        
        String canNotThrowCheckedException(Method method);
        
        String badParameterCountOfMethod(Method method);
        
        String mustReturnVoidBecauseThereIsOneParameter(Method method);
        
        String mustStartWithSet(Method method);
        
        String mustNotReturnVoidBecauseThereIsNoParameters(Method method);
        
        String mustStartWithGetOrIs(Method method);
        
        String propertyHasBeenDeclarationInSuperMetadata(
                String propertyName,
                Class<?> superObjectModelType);
        
        String noGetterForScalarSetter(
                Method setter, 
                Type returnType, 
                Class<?> declaringtype, 
                String methodName);
        
        String noSetterForScalarGetter(
                Method getter, 
                Class<?> declaringType, 
                String methodName, 
                Type parameterType);
        
        String invalidAnnotationsOfScalarSetter(Method setter);
        
        String conflictAnnotationAnnotations(
                Method method,
                Class<? extends Annotation> annotationType,
                Class<? extends Annotation> annotationAnnotationType1,
                Class<? extends Annotation> annotationAnnotationType2);
        
        String conflictAnnotations(
                Method method1,
                Class<? extends Annotation> annotationType1,
                Class<? extends Annotation> annotationType2,
                Class<? extends Annotation> annotationAnnotationType1,
                Class<? extends Annotation> annotationAnnotationType2);
        
        String classIsNotFirstLowerTypeInScalarOnly(
                Method method, 
                Class<? extends Annotation> annotation, 
                Class<ScalarOnly> scalarOnlyType, 
                String lowerTypesContant);
        
        String badLowerAndUpperInScalarOnly(
                Method method, 
                Class<? extends Annotation> annotationType, 
                Class<ScalarOnly> scalarOnlyType, 
                String lowerTypesContant,
                Class<?> lowerClass,
                String upperTypeConstant,
                Class<?> upperClass);
        
        String scalarTypeOutOfLowerBound(
                Method method,
                Class<? extends Annotation> annotationType, 
                Class<ScalarOnly> scalarOnlyType, 
                String lowerTypesContant,
                Class<?> lowerClass,
                Class<?> propertyType);
        
        String scalarTypeOutOfUpperBound(
                Method method,
                Class<? extends Annotation> annotationType, 
                Class<ScalarOnly> scalarOnlyType, 
                String upperClassContant,
                Class<?> upperClass,
                Class<?> propertyType);
        
        String associatedEndpointTypeOutOfBound(
                Method method,
                Class<? extends Annotation> annotationType,
                Class<AssociationOnly> associationOnlyType,
                String associationEndTypes,
                AssociatedEndpointType[] associatedEndpointTypes,
                AssociatedEndpointType associatedEndpointType);
        
        String propertyIsNeigthScalarNorAssociation(
                Method method, 
                Class<ScalarOnly> scalarOnlyType, 
                Class<AssociationOnly> associationOnlyType);
        
        String noAssociationAnnotationForAssociationProperty(
                Method method, 
                Class<Association> associationType, 
                Class<AssociationOnly> associationOnlyType);
        
        String objectModelComparatorPropertiesAreDuplicated(
                Class<?> objectModelType,
                Class<ReferenceComparisonRule> referenceComparisonRuleTypeConstant,
                String keyPropertyNames,
                String duplicatedKeyPropertyName);
        
        String objectModelKeyPropertyIsNotExisting(
                Class<?> objectModelType,
                Class<ReferenceComparisonRule> referenceComparisonRuleTypeConstant,
                String keyPropertyNames,
                String notExistingKeyPropertyName);
        
        String objectModelKeyPropertyIsNotScalar(
                Class<?> objectModelType,
                Class<ReferenceComparisonRule> referenceComparisonRuleTypeConstant,
                String keyPropertyNames,
                String notExistingKeyPropertyName);
        
        String objectModelKeyPropertyIsDeferrable(
                Class<?> objectModelType,
                Class<ReferenceComparisonRule> referenceComparisonRuleTypeConstant,
                String keyPropertyNames,
                String deferrableKeyPropertyName,
                Class<Deferrable> deferrableTypeConstant);
        
        String objectModelKeyPropertyHasBadType(
                Class<?> objectModelType,
                Class<ReferenceComparisonRule> referenceComparisonRuleTypeConstant,
                String keyPropertyNames,
                String badTypeKeyPropertyName,
                Class<?> returnType);
        
        String associationComparatorPropertiesAreDuplicated(
                String associationName,
                Class<?> objectModelType,
                Class<ReferenceComparisonRule> referenceComparisonRuleTypeConstant,
                String keyPropertyNames,
                String duplicatedKeyPropertyName);
        
        @SuppressWarnings("rawtypes")
        String noComparatorForSortedSet(
                String associationName,
                Class<?> objectModelType,
                Class<SortedSet> sortedSetType,
                Class<ReferenceComparisonRule> referenceComparisonRuleType,
                Class<?> oppositeObjectModelInterface
        );
        
        String associationKeyPropertyIsNotExisting(
                String associationName,
                Class<?> objectModelType,
                Class<ReferenceComparisonRule> referenceComparisonRuleType,
                String keyPropertyNames,
                String notExistingKeyPropertyName,
                Class<?> oppositeObjectModelClass);
        
        String associationKeyPropertyIsNotScalar(
                String associationName,
                Class<?> objectModelType,
                Class<ReferenceComparisonRule> referenceComparisonRuleType,
                String keyPropertyNames,
                String notNotScalarKeyPropertyName,
                Class<?> oppositeObjectModelClass);
        
        String associationKeyPropertyIsDeferrable(
                String associationName,
                Class<?> objectModelType,
                Class<ReferenceComparisonRule> referenceComparisonRuleType,
                String keyPropertyNames,
                String deferrableKeyPropertyName,
                Class<?> oppositeObjectModelClass,
                Class<Deferrable> deferrableTypeConstant);
        
        String associationKeyPropertyHasBadType(
                String associationName,
                Class<?> objectModelType,
                Class<ReferenceComparisonRule> referenceComparisonRuleType,
                String keyPropertyNames,
                String badTypeKeyPropertyName,
                Class<?> oppositeObjectModelClass,
                Class<?> badType);
        
        String specifiedPropertyIsNotScalarProperty(Class<?> objectModelClass, int id);
        
        String specifiedPropertyIsNotAssociationProperty(Class<?> objectModelClass, int id);
        
        String noProperty(Class<?> objectModelClass, String name);
        
        String noScalarProperty(Class<?> objectModelClass, String name);
        
        String noAssociationProperty(Class<?> objectModelClass, String name);
        
        String propertyNameCanNotBeAnyOf(String invalidMethodPostfixsText);
        
        String bothAssociationOnlyAndContravariance(
                Method method, 
                Class<Association> associationType, 
                Class<Contravariance> contravarianceType);
        
        String associationCanNotBeView(Method associationMethod, Class<View> viewType);
        
        String associationMustBeAnyOf(Class<?> associationClass, Class<?> ... classes);
        
        String propertyMustMarkedByContravariance(
                Method associationMethod, 
                Class<Contravariance> contravarianceType);
        
        String mustHaveSuperObjectModel(
                Class<? extends Object> objectModelClass, 
                Method associationMethod, 
                Class<Contravariance> contravarianceType);
        
        String noBaseProperty(
                Method associationMethod,
                Class<Contravariance> contravarianceType, 
                String basePropertyName,
                Class<?> objectModelClass);
        
        String invalidBasePropertyType(
                Method associationMethod,
                Class<Contravariance> contravarianceType, 
                String basePropertyName,
                Class<?> objectModelClass,
                Class<?> returnClass);
        
        @SuppressWarnings("rawtypes")
        String invalidBasePropertyKeyType(
                Method associationMethod,
                Class<Contravariance> contravarianceType,
                String basePropertyName,
                Class<Map> mapType,
                Class<?> keyType, 
                Class<?> objectModelClass,
                Class<?> baseKeyType);
        
        String invalidBasePropertyElementType(
                Method associationMethod,
                Class<Contravariance> contravarianceType,
                String basePropertyName,
                Class<?> elementClass,
                Class<?> objectModelClass,
                Class<?> baseElementClass
        );
        
        String ownerMustBeReference(
                Method method,
                Class<? extends Annotation> annotationType,
                Class<OwnerReferenceOnly> ownerReferenceOnlyConstant,
                ObjectModelMode expectedObjectModelMode,
                ObjectModelMode actualObjectModelMode
        );
        
        String propertyMustNotBeDeferrable(
                Method method, 
                Class<? extends Annotation> annotationType, 
                Class<NonDeferrableOnly> nonDeferrableOnlyConstant,
                Class<Deferrable> deferrableConstant);
        
        String scalarCanNotBeReference(
                Class<?> objectModelType,
                String propertyName,
                ObjectModelMode referenceModeConstant);
        
        String noDisabilityInSuperObjectModel(
                Class<?> objectModelType,
                Class<AllowDisability> allowDisabilityTypeConstant,
                Class<?> sueprObjectModeType);
        
        String invalidTypeLevelReferenceComparasionRule(
                Class<?> objectModelType,
                ObjectModelMode mode,
                Class<ReferenceComparisonRule> referenceComparsionTypeConstant);
        
        String invalidTypeLevelReferenceComparasionRuleValue(
                Class<?> objectModelType,
                Class<ReferenceComparisonRule> referenceComparsionTypeConstant,
                String value,
                String expectedPattern);
        
        String invalidPropertyLevelReferenceComparasionRuleValue(
                AssociationProperty associationProperty,
                Class<ReferenceComparisonRule> referenceComparsionTypeConstant,
                String value,
                String expectedPattern);
        
        String requireEmbeddableComparasionRule(
                Class<?> objectModelType,
                ObjectModelMode mode,
                String embeddableComparasionRuleConstant);
        
        String invalidEmbeddableComparasionRule(
                Class<?> objectModelType,
                String embeddableComparasionRuleConstant,
                String value,
                String expectedPattern);
        
        String invalidEmbeddablePropertyCount(
                Class<?> objectModelClass,
                String embeddableComparasionRuleConstant,
                int comparasionPropertyNameCount,
                int declaredGetterCount);
        
        String invalidEmbeddableComparasionPropertyName(
                Class<?> objectModelClass,
                String embeddableComparasionRuleConstant,
                String comparasionPropertyName);
        
        String referenceDoesNotSupportDefaultComparator(
                Class<?> objectModelType,
                String getOwnerComparatorConstant,
                ObjectModelMode referenceModeConstant);
        
        String scalarCanNotReturnAbstractObjectModel(
                ScalarProperty scalarProerty,
                Class<?> returnType,
                Class<?> returnObjectModelType,
                ObjectModelMode abstractModeConstant);
        
        String complexScalarCanNotBeDeferrable(
                ScalarProperty scalarProperty, 
                Class<Deferrable> deferrableTypeConstant);
        
        String keyTypeMustBeEmbeddable(
                AssociationProperty associationProperty,
                Class<?> keyType,
                Class<?> keyObjectModelType,
                ObjectModelMode embeddableModeConstant,
                ObjectModelMode actualMode);
        
        String oppositeOwnwerTypeMustBeReference(
                AssociationProperty associationProperty,
                Class<?> oppositeType,
                Class<?> oppositeObjectModelType,
                ObjectModelMode referenceModeConstant,
                ObjectModelMode actualMode);
        
        String keyTypeMustBeClass(AssociationProperty associationProperty, Class<?> keyType);
        
        String keyTypeMustBeObjectModelOwnerClass(Property property, Class<?> keyType);
        
        String relatedTypeMustBeClass(AssociationProperty associationProperty, Class<?> keyType);
        
        String relatedTypeMustBeObjectModelOwnerClass(Property property, Class<?> keyType);
        
        @SuppressWarnings("rawtypes")
        String onlyReferenceCanMissOpposite(
                Method associationMethod, 
                Class<Association> associationTypeConstant,
                Class<Reference> referenceTypeConstant);
        
        String deferrablePropertyCanNotBeComparatorField(
                ScalarProperty deferrableProperty,
                Class<Deferrable> deferrableTypeConstant,
                Class<?> comparatorType);
        
        String badTypescalarPropertyCanNotBeComparatorField(
                ScalarProperty scalarProperty,
                Class<?> badType,
                Class<?> comparatorType);
        
        String deferrablePropertyRequiredReferenceModel(
                ScalarPropertyImpl scalarPropertyImpl,
                Class<Deferrable> deferrableTypeConstant,
                ObjectModelMode expectedMode, 
                ObjectModelMode actualMode
        );
        
        String unstablePropertyRequiredReferenceModel(
                ScalarPropertyImpl scalarPropertyImpl,
                Class<?> unstableType,
                ObjectModelMode expectedMode, 
                ObjectModelMode actualMode
        );
    }

    static {
        /*
         * Initialize VALID_ENDPOINT_TYPES
         */
        Set<Class<?>> validReturnTypes = new LinkedHashSet<Class<?>>();
        
        validReturnTypes.add(Reference.class);
        validReturnTypes.add(IndexedReference.class);
        validReturnTypes.add(KeyedReference.class);
        
        validReturnTypes.add(Collection.class);
        validReturnTypes.add(List.class);
        validReturnTypes.add(Set.class);
        validReturnTypes.add(SortedSet.class);
        validReturnTypes.add(NavigableSet.class);
        validReturnTypes.add(Map.class);
        validReturnTypes.add(SortedMap.class);
        validReturnTypes.add(NavigableMap.class);
        validReturnTypes.add(XCollection.class);
        validReturnTypes.add(XList.class);
        validReturnTypes.add(XSet.class);
        validReturnTypes.add(XSortedSet.class);
        validReturnTypes.add(XNavigableSet.class);
        validReturnTypes.add(XOrderedSet.class);
        validReturnTypes.add(XMap.class);
        validReturnTypes.add(XSortedMap.class);
        validReturnTypes.add(XNavigableMap.class);
        validReturnTypes.add(XOrderedMap.class);
        
        validReturnTypes.add(MAReference.class);
        validReturnTypes.add(MAIndexedReference.class);
        validReturnTypes.add(MAKeyedReference.class);
        
        validReturnTypes.add(MACollection.class);
        validReturnTypes.add(MAList.class);
        validReturnTypes.add(MASet.class);
        validReturnTypes.add(MASortedSet.class);
        validReturnTypes.add(MANavigableSet.class);
        validReturnTypes.add(MAOrderedSet.class);
        validReturnTypes.add(MAMap.class);
        validReturnTypes.add(MASortedMap.class);
        validReturnTypes.add(MANavigableMap.class);
        validReturnTypes.add(MAOrderedMap.class);
        
        VALID_ENDPOINT_TYPES = MACollections.unmodifiable(validReturnTypes);
        
        /*
         * Initialize VALID_ASSOCIATED_END_TYPE_PAIRS
         */
        Set<AssociatedEndpointTypePair> validAssociatedEndpointTypePairs = new LinkedHashSet<AssociatedEndpointTypePair>();
        
        validAssociatedEndpointTypePairs.add(
                new AssociatedEndpointTypePair(
                        AssociatedEndpointType.REFERENCE, 
                        AssociatedEndpointType.REFERENCE));
        validAssociatedEndpointTypePairs.add(
                new AssociatedEndpointTypePair(
                        AssociatedEndpointType.REFERENCE, 
                        AssociatedEndpointType.MAP));
        validAssociatedEndpointTypePairs.add(
                new AssociatedEndpointTypePair(
                        AssociatedEndpointType.REFERENCE, 
                        AssociatedEndpointType.SET));
        validAssociatedEndpointTypePairs.add(
                new AssociatedEndpointTypePair(
                        AssociatedEndpointType.REFERENCE, 
                        AssociatedEndpointType.LIST));
        validAssociatedEndpointTypePairs.add(
                new AssociatedEndpointTypePair(
                        AssociatedEndpointType.REFERENCE, 
                        AssociatedEndpointType.COLLECTION));
        
        validAssociatedEndpointTypePairs.add(
                new AssociatedEndpointTypePair(
                        AssociatedEndpointType.INDEXED_REFERENCE, 
                        AssociatedEndpointType.LIST));
        
        validAssociatedEndpointTypePairs.add(
                new AssociatedEndpointTypePair(
                        AssociatedEndpointType.KEYED_REFERENCE, 
                        AssociatedEndpointType.MAP));
        
        validAssociatedEndpointTypePairs.add(
                new AssociatedEndpointTypePair(
                        AssociatedEndpointType.SET, 
                        AssociatedEndpointType.REFERENCE));
        validAssociatedEndpointTypePairs.add(
                new AssociatedEndpointTypePair(
                        AssociatedEndpointType.SET, 
                        AssociatedEndpointType.SET));
        validAssociatedEndpointTypePairs.add(
                new AssociatedEndpointTypePair(
                        AssociatedEndpointType.SET, 
                        AssociatedEndpointType.LIST));
        validAssociatedEndpointTypePairs.add(
                new AssociatedEndpointTypePair(
                        AssociatedEndpointType.SET, 
                        AssociatedEndpointType.COLLECTION));
        validAssociatedEndpointTypePairs.add(
                new AssociatedEndpointTypePair(
                        AssociatedEndpointType.SET, 
                        AssociatedEndpointType.MAP));
        
        validAssociatedEndpointTypePairs.add(
                new AssociatedEndpointTypePair(
                        AssociatedEndpointType.LIST, 
                        AssociatedEndpointType.INDEXED_REFERENCE));
        validAssociatedEndpointTypePairs.add(
                new AssociatedEndpointTypePair(
                        AssociatedEndpointType.LIST, 
                        AssociatedEndpointType.REFERENCE));
        validAssociatedEndpointTypePairs.add(
                new AssociatedEndpointTypePair(
                        AssociatedEndpointType.LIST, 
                        AssociatedEndpointType.SET));
        validAssociatedEndpointTypePairs.add(
                new AssociatedEndpointTypePair(
                        AssociatedEndpointType.LIST, 
                        AssociatedEndpointType.COLLECTION));
        
        validAssociatedEndpointTypePairs.add(
                new AssociatedEndpointTypePair(
                        AssociatedEndpointType.COLLECTION, 
                        AssociatedEndpointType.REFERENCE));
        validAssociatedEndpointTypePairs.add(
                new AssociatedEndpointTypePair(
                        AssociatedEndpointType.COLLECTION, 
                        AssociatedEndpointType.COLLECTION));
        validAssociatedEndpointTypePairs.add(
                new AssociatedEndpointTypePair(
                        AssociatedEndpointType.COLLECTION, 
                        AssociatedEndpointType.SET));
        validAssociatedEndpointTypePairs.add(
                new AssociatedEndpointTypePair(
                        AssociatedEndpointType.COLLECTION, 
                        AssociatedEndpointType.LIST));
        validAssociatedEndpointTypePairs.add(
                new AssociatedEndpointTypePair(
                        AssociatedEndpointType.COLLECTION, 
                        AssociatedEndpointType.MAP));
        
        validAssociatedEndpointTypePairs.add(
                new AssociatedEndpointTypePair(
                        AssociatedEndpointType.MAP, 
                        AssociatedEndpointType.KEYED_REFERENCE));
        validAssociatedEndpointTypePairs.add(
                new AssociatedEndpointTypePair(
                        AssociatedEndpointType.MAP, 
                        AssociatedEndpointType.REFERENCE));
        validAssociatedEndpointTypePairs.add(
                new AssociatedEndpointTypePair(
                        AssociatedEndpointType.MAP, 
                        AssociatedEndpointType.SET));
        validAssociatedEndpointTypePairs.add(
                new AssociatedEndpointTypePair(
                        AssociatedEndpointType.MAP, 
                        AssociatedEndpointType.COLLECTION));
        
        VALID_ASSOCIATED_END_TYPE_PAIRS = MACollections.unmodifiable(validAssociatedEndpointTypePairs);
        
        /*
         * Initialize VALID_ASSOCIATED_END_TYPE_PAIRS_TEXT
         */
        StringBuilder builder = new StringBuilder();
        boolean addComma = false;
        for (AssociatedEndpointTypePair associatedEndpointTypePair : validAssociatedEndpointTypePairs) {
            if (addComma) {
                builder.append(", ");
            } else {
                addComma = true;
            }
            builder.append(associatedEndpointTypePair.type1.toClass().getSimpleName());
            builder.append(" => ");
            builder.append(associatedEndpointTypePair.type2.toClass().getSimpleName());
        }
        VALID_ASSOCIATED_END_TYPE_PAIRS_TEXT = builder.toString();
        
        /*
         * Initialize INVALID_PROPERTY_POSTFIXS
         */
        Set<String> invalidPropertyPostfixs = new HashSet<>();
        invalidPropertyPostfixs.add("Owner");
        invalidPropertyPostfixs.add("Metadata");
        invalidPropertyPostfixs.add("ObjectModelFactory");
        INVALID_METHOD_POSTFIXS = MACollections.unmodifiable(invalidPropertyPostfixs);
        
        /*
         * Initialize INVALID_PROPERTY_POSTFIXS_TEXT
         */
        builder = new StringBuilder();
        addComma = false;
        for (String invalidPropertyPostfix : invalidPropertyPostfixs) {
            if (addComma) {
                builder.append(", ");
            } else {
                addComma = true;
            }
            builder.append(Strings.toCamelCase(invalidPropertyPostfix));
        }
        INVALID_METHOD_POSTFIXS_TEXT = builder.toString();
        
        /*
         * Initialized SIMPLE_TYPES
         */
        Set<Class<?>> simpleScalarTypes = new HashSet<>();
        simpleScalarTypes.add(boolean.class);
        simpleScalarTypes.add(char.class);
        simpleScalarTypes.add(byte.class);
        simpleScalarTypes.add(short.class);
        simpleScalarTypes.add(int.class);
        simpleScalarTypes.add(long.class);
        simpleScalarTypes.add(float.class);
        simpleScalarTypes.add(double.class);
        simpleScalarTypes.add(Boolean.class);
        simpleScalarTypes.add(Character.class);
        simpleScalarTypes.add(Byte.class);
        simpleScalarTypes.add(Short.class);
        simpleScalarTypes.add(Integer.class);
        simpleScalarTypes.add(Long.class);
        simpleScalarTypes.add(Float.class);
        simpleScalarTypes.add(Double.class);
        simpleScalarTypes.add(BigInteger.class);
        simpleScalarTypes.add(BigDecimal.class);
        simpleScalarTypes.add(String.class);
        simpleScalarTypes.add(java.util.Date.class);
        simpleScalarTypes.add(java.util.Calendar.class);
        simpleScalarTypes.add(java.util.GregorianCalendar.class);
        simpleScalarTypes.add(java.sql.Date.class);
        simpleScalarTypes.add(java.sql.Time.class);
        simpleScalarTypes.add(java.sql.Timestamp.class);
        simpleScalarTypes.add(byte[].class);
        simpleScalarTypes.add(char[].class);
        simpleScalarTypes.add(Serializable.class);
        tryCollectJava8Type(simpleScalarTypes, "java.time.Clock");
        tryCollectJava8Type(simpleScalarTypes, "java.time.Duration");
        tryCollectJava8Type(simpleScalarTypes, "java.time.Instant");
        tryCollectJava8Type(simpleScalarTypes, "java.time.LocalDate");
        tryCollectJava8Type(simpleScalarTypes, "java.time.LocalDateTime");
        tryCollectJava8Type(simpleScalarTypes, "java.time.LocalTime");
        tryCollectJava8Type(simpleScalarTypes, "java.time.MonthDay");
        tryCollectJava8Type(simpleScalarTypes, "java.time.OffsetDateTime");
        tryCollectJava8Type(simpleScalarTypes, "java.time.OffsetTime");
        tryCollectJava8Type(simpleScalarTypes, "java.time.Period");
        tryCollectJava8Type(simpleScalarTypes, "java.time.Year");
        tryCollectJava8Type(simpleScalarTypes, "java.time.YearMonth");
        tryCollectJava8Type(simpleScalarTypes, "java.time.ZonedDateTime");
        tryCollectJava8Type(simpleScalarTypes, "java.time.ZoneId");
        tryCollectJava8Type(simpleScalarTypes, "java.time.ZoneOffset");
        // Any Enum is considered as valid simple type, need not to retain any enum type in the SIMPLE_TYPES
        SIMPLE_TYPES = MACollections.unmodifiable(simpleScalarTypes);
    }
}
