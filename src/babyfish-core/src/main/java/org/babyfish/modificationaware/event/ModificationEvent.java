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
package org.babyfish.modificationaware.event;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collection;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.HashMap;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.OrderAdjustMode;
import org.babyfish.collection.ReferenceEqualityComparator;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.immutable.Autonomy;
import org.babyfish.lang.Action;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.lang.Nulls;
import org.babyfish.lang.UncheckedException;
import org.babyfish.lang.reflect.ClassInfo;
import org.babyfish.lang.reflect.ConstructorInfo;
import org.babyfish.lang.reflect.FieldInfo;
import org.babyfish.lang.reflect.GenericTypes;
import org.babyfish.lang.reflect.MethodInfo;
import org.babyfish.lang.reflect.NoSuchMethodInfoException;
import org.babyfish.lang.reflect.asm.ASM;
import org.babyfish.lang.reflect.asm.XMethodVisitor;
import org.babyfish.modificationaware.event.spi.GlobalAttributeContext;
import org.babyfish.modificationaware.event.spi.InAllChainAttributeContext;
import org.babyfish.org.objectweb.asm.AnnotationVisitor;
import org.babyfish.org.objectweb.asm.Attribute;
import org.babyfish.org.objectweb.asm.ClassReader;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.FieldVisitor;
import org.babyfish.org.objectweb.asm.Label;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;

import org.babyfish.util.LazyResource;

/**
 * @author Tao Chen
 */
public abstract class ModificationEvent extends EventObject {

    private static final long serialVersionUID = 8314380265433089955L;
    
    private static final String NAME_POSTFIX = "92B8C17E_BF4E_4135_B596_5A76E0FEBF4E";
    
    private static final String CREATE_DETACH_EVENT = "createDetachEvent";
    
    private static final String CREATE_ATTACH_EVENT = "createAttachEvent";
    
    private static final String CREATE_REPLACE_EVENT = "createReplaceEvent";
    
    private static final String BUBBLE_EVENT = "bubbleEvent";
    
    private static final String DISPATCH = "dispatch";
    
    private static final String GET_MODIFICATION = "getModification";
    
    private static final String GET_ATTRIBUTE_CONTEXT = "getAttributeContext";
    
    private static final String GET_PRE_MODIFICATION_THROWABLE = "getPreModificationThrowable";
    
    private static final String GET_MODIFICATION_THROWABLE = "getModificationThrowable";
    
    private static final String IS_MODIFICATION_SUCCESSED = "isModificationSuccessed";
    
    private static final String MODIFICATION = "{modification}";
    
    private static final String CAUSE = "{cause}";
    
    private static final String SOURCE_EVENT = "{source_event}";
    
    private static final String LOCAL_ATTRIBUTE_CONTEXT = "{attribute_context:local}";
    
    private static final String ATTRIBUTE_CONTEXT_IN_BUBBLE_CHAIN = "{attribute_context:in_bubble_chain}";
    
    private static final String ATTRIBUTE_CONTEXT_IN_DISPATCH_CHAIN = "{attribute_context:in_dispatch_chain}";
    
    private static final String ATTRIBUTE_CONTEXT_IN_ALL_CHAIN = "{attribute_context:in_all_chain}";
    
    private static final String SHARED_POSTFIX = ":shared";
    
    private static final String DETACHED_POSTFIX = ":detached";
    
    private static final String ATTACHED_POSTFIX = ":attached";
    
    private static final Map<Class<?>, Class<?>> TYPE_BOX_MAP;
    
    private static final LazyResource<Resource> LAZY_RESOURCE = LazyResource.of(Resource.class);
    
    private static final Map<Class<?>, Object> FACTORY_CACHE =
        new HashMap<Class<?>, Object>();
    
    private static final ReadWriteLock FACTORY_CACHE_LOCK = 
        new ReentrantReadWriteLock();
    
    private Map<Object, ModificationEvent> bubbledEvents;
    
    private Map<Object, ModificationEvent> dispatchedEvents;

    public ModificationEvent(Object source) {
        super(source);
    }
    
    public ModificationEvent(Object source, Cause cause) {
        super(source);
        Arguments.mustNotBeNull("cause", cause);
        ModificationEvent causeEvent = cause.getEvent();
        Arguments.mustBeNull(
                "causeEvent.getBubbledEvent(source, true)", 
                causeEvent.getBubbledEvent(source, true)
        );
        if (causeEvent.bubbledEvents == null) {
            causeEvent.bubbledEvents = new HashMap<>(
                    ReferenceEqualityComparator.getInstance(),
                    ReferenceEqualityComparator.getInstance()
            );
        }
        causeEvent.bubbledEvents.put(source, this);
    }
    
    public ModificationEvent(Object source, ModificationEvent dispatchSourceEvent) {
        super(source);
        Arguments.mustNotBeNull("dispatchSourceEvent", dispatchSourceEvent);
        Arguments.mustBeNull(
                "dispatchSourceEvent.getDispathcedEvent(source, true)", 
                dispatchSourceEvent.getDispathcedEvent(source, true)
        );
        if (dispatchSourceEvent.dispatchedEvents == null) {
            dispatchSourceEvent.dispatchedEvents = new HashMap<>(
                    ReferenceEqualityComparator.getInstance(),
                    ReferenceEqualityComparator.getInstance()
            );
        }
        dispatchSourceEvent.dispatchedEvents.put(source, this);
    }
    
    public abstract ModificationType getModificationType();
    
    public abstract EventType getEventType();
    
    public abstract Modification getModification();
    
    public abstract Cause getCause();
    
    public abstract EventAttributeContext getAttributeContext(AttributeScope scope);
    
    public abstract ModificationEvent dispatch(Object source);
    
    public abstract Throwable getPreModificationThrowable();
    
    public abstract Throwable getModificationThrowable();
    
    public abstract boolean isModificationSuccessed();
    
    public final <E extends ModificationEvent> E getBubbledEvent(Object source) {
        return this.getBubbledEvent(source, false);
    }
    
    public final <E extends ModificationEvent> E getBubbledEvent() {
        return this.getBubbledEvent(false);
    }
    
    @SuppressWarnings("unchecked")
    public final <E extends ModificationEvent> E getBubbledEvent(Object source, boolean nullable) {
        ModificationEvent retval = this.bubbledEvents != null ? this.bubbledEvents.get(source) : null;
        if (retval == null && !nullable) {
            throw new IllegalStateException(LAZY_RESOURCE.get().noBubbledEvent());
        }
        return (E)retval;
    }
    
    @SuppressWarnings("unchecked")
    public final <E extends ModificationEvent> E getBubbledEvent(boolean nullable) {
        if (Nulls.isNullOrEmpty(this.bubbledEvents)) {
            if (nullable) {
                return null;
            }
            throw new IllegalStateException();
        }
        if (this.bubbledEvents.size() > 1) {
            throw new IllegalStateException();
        }
        return (E)this.bubbledEvents.values().iterator().next();
    }
    
    public final <E extends ModificationEvent> E getDispathcedEvent(Object source) {
        return this.getDispathcedEvent(source, false);
    }
    
    public final <E extends ModificationEvent> E getDispathcedEvent() {
        return this.getDispathcedEvent(false);
    }
    
    @SuppressWarnings("unchecked")
    public final <E extends ModificationEvent> E getDispathcedEvent(Object source, boolean nullable) {
        ModificationEvent retval = this.dispatchedEvents != null ? this.dispatchedEvents.get(source) : null;
        if (retval == null && !nullable) {
            throw new IllegalStateException(LAZY_RESOURCE.get().noBubbledEvent());
        }
        return (E)retval;
    }
    
    
    
    @SuppressWarnings("unchecked")
    public final <E extends ModificationEvent> E getDispathcedEvent(boolean nullable) {
        if (Nulls.isNullOrEmpty(this.dispatchedEvents)) {
            if (nullable) {
                return null;
            }
            throw new IllegalStateException(LAZY_RESOURCE.get().noBubbledEvent());
        }
        if (this.dispatchedEvents.size() > 1) {
            throw new IllegalStateException();
        }
        return (E)this.dispatchedEvents.values().iterator().next();
    }
    
    @SuppressWarnings("unchecked")
    public final <M extends Modification> M getPrimitiveModification() {
        ModificationEvent event = this;
        while (true) {
            Modification modification = event.getModification();
            if (modification != null) {
                return (M)modification;
            }
            event = event.getCause().getEvent();
        }
    }
    
    @SuppressWarnings("unchecked")
    protected final static <F> F getFactory(Class<F> factoryType) {
        Arguments.mustNotBeNull("factoryType", factoryType);
        Object factory;
        Lock lock;
        
        (lock = FACTORY_CACHE_LOCK.readLock()).lock();
        try {
            factory = FACTORY_CACHE.get(factoryType); //1st reading
        } finally {
            lock.unlock();
        }
        
        if (factory == null) { //1st checking
            (lock = FACTORY_CACHE_LOCK.writeLock()).lock();
            try {
                factory = FACTORY_CACHE.get(factoryType); //2nd reading
                if (factory == null) { //2nd checking
                    factory = createFactory(factoryType);
                    FACTORY_CACHE.put(factoryType, factory);
                }
            } finally {
                lock.unlock();
            }
        }
        return (F)factory;
    }
    
    private static Object createFactory(Class<?> factoryType) {
        Arguments.mustBeInterface("factoryType", factoryType);
        Class<?> eventClass = factoryType.getDeclaringClass();
        if (eventClass == null) {
            throw new IllegalProgramException(
                    LAZY_RESOURCE.get().factoryTypeMustBeDeclaredInAnotherClass(factoryType.getName()));
        }
        if (!ModificationEvent.class.isAssignableFrom(eventClass)) {
            throw new IllegalProgramException(
                    LAZY_RESOURCE.get().invalidDeclaringClassOfFactory(
                            eventClass, factoryType, ModificationEvent.class
                    )
            );
        }
        if (ClassInfo.isNonStaticMemberClass(eventClass)) {
            throw new IllegalProgramException(
                    LAZY_RESOURCE.get().eventMustBeTopLevelOrStaticClass(eventClass));
        }
        ClassInfo<?> eventClassInfo = ClassInfo.of(eventClass);
        validateDeclaredMembersOfEventClassInfo(eventClassInfo);
        Metadata metadata = new Metadata(eventClass);
        Collection<Property> properties = metadata.properties;
        final Class<?> factoryImplemtation = new FactoryGenerator(
                ClassInfo.of(factoryType), 
                metadata.factory, 
                eventClassInfo, 
                properties).getImplementation();
        return AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                try {
                    return factoryImplemtation.newInstance();
                } catch (InstantiationException | IllegalAccessException ex) {
                    throw UncheckedException.rethrow(ex);
                }
            }
        });
    }
    
    private static Class<?> getRealEventClass(
            Class<?> rawEventClass, 
            EventType eventType, 
            ModificationType modificationType) {
        switch (eventType) {
        case PROTOSOMATIC:
            return new ProtosomaticEventGenerator(
                    ClassInfo.of(rawEventClass), 
                    new Metadata(rawEventClass), 
                    modificationType)
                    .getImplementation();
        case BUBBLED:
            return new BubbledEventGenerator(
                    ClassInfo.of(rawEventClass), 
                    new Metadata(rawEventClass), 
                    modificationType)
            .getImplementation();
        case DISPATCHED:
            return new DispatchedEventGenerator(
                    ClassInfo.of(rawEventClass), 
                    new Metadata(rawEventClass))
            .getImplementation();
        default:
            throw new AssertionError();
        }
    }
    
    private static void validateDeclaredMembersOfEventClassInfo(ClassInfo<?> eventClassInfo) {
        if (eventClassInfo.getRawClass() == ModificationEvent.class) {
            return;
        }
        for (ConstructorInfo<?> constructorInfo : eventClassInfo.getDeclaredConstructors()) {
            if (constructorInfo.getModifiers().isPrivate()) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().invalidEventConstructorAcc(
                                constructorInfo));
            }
            List<Class<?>> parameterTypes = constructorInfo.getParameterTypes();
            if (parameterTypes.size() == 0 || 
                    parameterTypes.size() > 2 || 
                    parameterTypes.get(0) != Object.class ||
                    (
                            parameterTypes.size() == 2 
                            && 
                            (
                                    parameterTypes.get(1) != Cause.class &&
                                    parameterTypes.get(1) != eventClassInfo.getRawClass()
                            )
                    )
                ) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().invalidEventConstructorSignature(
                                constructorInfo,
                                Object.class,
                                Cause.class,
                                eventClassInfo));
            }
        }
        for (FieldInfo fieldInfo : eventClassInfo.getDeclaredFields()) {
            if (!fieldInfo.getModifiers().isStatic()) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().invalidEventField(fieldInfo));
            }
        }
        for (MethodInfo methodInfo : eventClassInfo.getDeclaredMethods()) {
            if (!methodInfo.getModifiers().isStatic()) {
                if (methodInfo.getModifiers().isBridge()) {
                    continue;
                }
                if (methodInfo.getName().equals(DISPATCH)) {
                    if (methodInfo.getReturnType() != eventClassInfo.getRawClass()) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().dispatchMethodMustReturnSelfType(
                                        methodInfo,
                                        eventClassInfo.getRawClass(),
                                        methodInfo.getReturnType()
                                )
                        );
                    }
                    continue;
                }
                if (methodInfo.isAnnotationPresent(Autonomy.class)) {
                    continue;
                }
                if (!methodInfo.getModifiers().isPublic() ||
                        !methodInfo.getModifiers().isAbstract()) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().invalidEventMethodModifiers(methodInfo));
                }
                if (!methodInfo.getTypeParameters().isEmpty()) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().invalidEventMethodTypeParameters(methodInfo));
                }
                if (methodInfo.getReturnType() == void.class) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().invalidEventMethodReturnType(methodInfo));
                }
                if (!methodInfo.getParameterTypes().isEmpty() && methodInfo.getParameterTypes().get(0) != PropertyVersion.class) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().invalidEventMethodParameters(
                                    methodInfo, 
                                    PropertyVersion.class));
                }
                if (!methodInfo.getName().startsWith("get") || methodInfo.getName().length() == 3) {
                    boolean invalid = true;
                    if (methodInfo.getReturnType() == boolean.class || methodInfo.getReturnType() == Boolean.class) {
                        if (!methodInfo.getName().startsWith("is") || methodInfo.getName().length() == 2) {
                            throw new IllegalProgramException(
                                    LAZY_RESOURCE.get().invalidEventMethodNameForBoolean(methodInfo.getName()));
                        }
                        invalid = false;
                    }
                    if (invalid) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().invalidEventMethodNameForNonBoolean(methodInfo.getName()));
                    }
                }
                for (Class<?> exceptionType : methodInfo.getExceptionTypes()) {
                    if (!RuntimeException.class.isAssignableFrom(exceptionType) &&
                            !Error.class.isAssignableFrom(exceptionType)) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().invalidEventMethodThrows(methodInfo, exceptionType)
                        );
                    }
                }
            }
        }
        validateDeclaredMembersOfEventClassInfo(eventClassInfo.getSuperClass());
    }
    
    private static void argumentMustNotBeNull(
            MethodVisitor mv, 
            String parameterName, 
            int slotIndex) {
        mv.visitLdcInsn(parameterName);
        mv.visitVarInsn(Opcodes.ALOAD, slotIndex);
        mv.visitMethodInsn(
                Opcodes.INVOKESTATIC, 
                Arguments.class.getName().replace('.', '/'), 
                "mustNotBeNull", 
                "(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;",
                false);
        mv.visitInsn(Opcodes.POP);
    }

    private static class Metadata {
        
        final Class<?> rawEventClass;
        
        final Class<?> dipstachReturnClass;
        
        final Type modificationType;
        
        final Class<?> modificationClass;
        
        final Factory factory;
        
        final Collection<Property> properties;
        
        public Metadata(Class<?> eventClass) {
            
            validateByteCode(eventClass);
            this.rawEventClass = eventClass;
            
            Class<?> factoryInterface = null;
            for (Class<?> nestedClass : eventClass.getDeclaredClasses()) {
                if (nestedClass.isAnnotationPresent(EventFactory.class)) {
                    if (factoryInterface != null) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().tooManyEventFactoryAnnotations(
                                        eventClass, 
                                        factoryInterface, 
                                        nestedClass, 
                                        EventFactory.class));
                    }
                    if (!nestedClass.isInterface()) {
                        throw new IllegalProgramException(
                                LAZY_RESOURCE.get().invalidEventFactoryAnnotation(
                                        nestedClass, 
                                        EventFactory.class));
                    }
                    factoryInterface = nestedClass;
                }
            }
            if (factoryInterface == null) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().missEventFactoryAnnotations(
                                eventClass,
                                EventFactory.class));
            }
            
            ClassInfo<?> eventClassInfo = ClassInfo.of(eventClass);
            this.dipstachReturnClass = getDispatchReturnClass(eventClassInfo);
            this.modificationType = getModificationType(eventClassInfo);
            this.modificationClass = GenericTypes.eraseGenericType(this.modificationType);
            
            XOrderedMap<String, Property> propertyMap = new LinkedHashMap<String, Property>(
                    false, OrderAdjustMode.TAIL, OrderAdjustMode.TAIL);
            readProperties(eventClassInfo, propertyMap);
            this.properties = MACollections.unmodifiable(propertyMap.values());
        
            this.factory = getFactoryOfEventClass(factoryInterface, eventClassInfo, this);
        }
        
        private static void validateByteCode(Class<?> eventClass) {
            String classFileName = eventClass.getName().replace('.', '/') + ".class";
            try {
                InputStream inputStream = eventClass.getClassLoader().getResourceAsStream(classFileName);
                try {
                    ClassVisitor cv = new EventClassValidator(eventClass);
                    new ClassReader(inputStream).accept(cv, ClassReader.SKIP_DEBUG);
                } finally {
                    inputStream.close();
                }
            } catch (IOException ex) {
                throw new AssertionError();
            }
        }
        
        private static Class<?> getDispatchReturnClass(ClassInfo<?> eventClassInfo) {
            MethodInfo dispatch = null;
            try {
                dispatch = eventClassInfo.getDeclaredResolvedMethod(DISPATCH, Object.class);
            } catch (NoSuchMethodInfoException ex) {
                if (eventClassInfo.getRawClass() != ModificationEvent.class) {
                    return getDispatchReturnClass(eventClassInfo.getSuperClass());
                }
                throw new AssertionError();
            }
            Type returnType = dispatch.getResolvedGenericReturnType();
            Type declaringType = dispatch.getDeclaringClass().getRawType();
            if (!returnType.equals(declaringType)) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().invalidDispatchReturnType(
                                dispatch.toResolvedGenericString(), 
                                ClassInfo.toString(declaringType)));
            }
            return GenericTypes.eraseGenericType(returnType);
        }
        
        private static Type getModificationType(
                ClassInfo<?> eventClassInfo) {
            MethodInfo methodInfo = null; 
            try {
                methodInfo = eventClassInfo.getDeclaredErasedMethod(GET_MODIFICATION);
            } catch (NoSuchMethodInfoException ex) {
                if (eventClassInfo.getRawClass() != ModificationEvent.class) {
                    return getModificationType(eventClassInfo.getSuperClass());
                }
            }
            assert methodInfo != null;
            return methodInfo.getResolvedGenericReturnType();
        }
        
        private static void readProperties(
                ClassInfo<?> eventClassInfo,
                XOrderedMap<String, Property> properties) {
            if (eventClassInfo.getRawClass() == ModificationEvent.class) {
                return;
            }
            readProperties(eventClassInfo.getSuperClass(), properties);
            EventDeclaration annotation = eventClassInfo.getAnnotation(EventDeclaration.class);
            if (annotation != null) {
                EventProperty[] props = annotation.properties();
                for (int i = 0; i < props.length; i++) {
                    EventProperty prop = props[i];
                    if (properties.containsKey(prop.name())) {
                        properties.access(prop.name());
                    } else {
                        MethodInfo getter;
                        try {
                            getter = getGetter(eventClassInfo, prop.name(), false);
                        } catch (NoSuchMethodInfoException ex) {
                            getter = null;
                        }
                        MethodInfo versionedGetter;
                        try {
                            versionedGetter = getGetter(eventClassInfo, prop.name(), true);
                        } catch (NoSuchMethodInfoException ex) {
                            versionedGetter = null;
                        }
                        if (getter == null && versionedGetter == null) {
                            throw new IllegalProgramException(
                                    LAZY_RESOURCE.get().missEventMethod(eventClassInfo, prop.name()));
                        }
                        if (prop.shared()) {
                            if (versionedGetter != null) {
                                throw new IllegalProgramException(
                                        LAZY_RESOURCE.get().invalidEventMethodForShared(versionedGetter)
                                );
                            }
                            properties.put(prop.name(), new Property(prop, getter));
                        } else {
                            if (getter != null) {
                                throw new IllegalProgramException(
                                        LAZY_RESOURCE.get().invalidEventMethodForNonShared(getter, PropertyVersion.class));
                            }
                            properties.put(prop.name(), new Property(prop, versionedGetter));
                        }
                    }
                }
            }
        }
        
        private static MethodInfo getGetter(
                ClassInfo<?> eventClassInfo, String propertyName, boolean versioned) {
            Class<?>[] erasedParameterTypes =
                versioned ? 
                        new Class[] { PropertyVersion.class } : 
                        new Class[0];
            String methodName = 
                "get" + 
                propertyName.substring(0, 1).toUpperCase() + 
                propertyName.substring(1);
            try {
                return eventClassInfo.getErasedMethod(methodName, erasedParameterTypes); 
            } catch (NoSuchMethodInfoException ex) {
                methodName = 
                    "is" + 
                    propertyName.substring(0, 1).toUpperCase() + 
                    propertyName.substring(1);
                return eventClassInfo.getErasedMethod(methodName, erasedParameterTypes);
            }
        }
        
        @SuppressWarnings({"unchecked", "rawtypes"})
        private static Factory getFactoryOfEventClass(
                Class<?> factoryType, 
                ClassInfo<?> eventClassInfo, 
                Metadata metadata) {
            
            TypeVariable<?>[] factoryTypeParameters = factoryType.getTypeParameters();
            List<TypeVariable<?>> eventTypeParameters = (List)eventClassInfo.getTypeParameters();
            if (factoryTypeParameters.length != eventTypeParameters.size()) {
                throw new IllegalProgramException(
                        LAZY_RESOURCE.get().invalidFactoryTypeParameterCount(
                                factoryType, 
                                eventTypeParameters.size(), 
                                eventClassInfo));
            }
            for (int i = factoryTypeParameters.length - 1; i >= 0; i--) {
                TypeVariable<?> factoryTypeParameter = factoryTypeParameters[i];
                TypeVariable<?> eventTypeParameter = eventTypeParameters.get(i);
                if (!factoryTypeParameter.getName().equals(eventTypeParameter.getName()) ||
                        !Arrays.equals(factoryTypeParameter.getBounds(), eventTypeParameter.getBounds())) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().invalidFactoryTypeParameter(
                                    factoryType, 
                                    i, 
                                    eventClassInfo));
                }
            }
            ClassInfo<?> factoryClassInfo = 
                ClassInfo.of(
                        factoryType, 
                        (Type[])eventClassInfo.getTypeParameters().toArray(
                                new Type[eventClassInfo.getTypeParameters().size()]));
            MethodInfo createDetachEvent = null;
            MethodInfo createAttachEvent = null;
            MethodInfo createReplaceEvent = null;
            MethodInfo bubbleEvent = null;
            for (MethodInfo methodInfo : factoryClassInfo.getMethods()) {
                if (!methodInfo.getTypeParameters().isEmpty()) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().invalidFactoryMethodTypeParameters(methodInfo));
                }
                ExpectedSignature expectedSignature = 
                    new ExpectedSignature(methodInfo.getName(), eventClassInfo.getRawType());
                if (CREATE_DETACH_EVENT.equals(methodInfo.getName())) {
                    expectedSignature.addParameterType(Object.class);
                    expectedSignature.addParameterType(metadata.modificationType);
                    for (Property property : metadata.properties) {
                        expectedSignature.addParameterType(property.getter.getResolvedGenericReturnType());
                    }
                } else if (CREATE_ATTACH_EVENT.equals(methodInfo.getName())) {
                    expectedSignature.addParameterType(Object.class);
                    expectedSignature.addParameterType(metadata.modificationType);
                    for (Property property : metadata.properties) {
                        expectedSignature.addParameterType(property.getter.getResolvedGenericReturnType());
                    }
                } else if (CREATE_REPLACE_EVENT.equals(methodInfo.getName())) {
                    expectedSignature.addParameterType(Object.class);
                    expectedSignature.addParameterType(metadata.modificationType);
                    for (Property property : metadata.properties) {
                        expectedSignature.addParameterType(property.getter.getResolvedGenericReturnType());
                        if (!property.annotation.shared()) {
                            expectedSignature.addParameterType(property.getter.getResolvedGenericReturnType());
                        }
                    }
                } else if (BUBBLE_EVENT.equals(methodInfo.getName())) {
                    expectedSignature.addParameterType(Object.class);
                    expectedSignature.addParameterType(Cause.class);
                    for (Property property : metadata.properties) {
                        Type propertyType = property.getter.getResolvedGenericReturnType();
                        if (propertyType instanceof Class<?>) {
                            Class<?> propertyClass = (Class<?>)propertyType;
                            if (propertyClass.isPrimitive()) {
                                propertyType = TYPE_BOX_MAP.get(propertyClass);
                            }
                        }
                        if (property.annotation.shared()) {
                            Type converterType = 
                                GenericTypes.cascadeMakeTypeOrParameterizedType(
                                    BubbledSharedPropertyConverter.class, 
                                    propertyType);
                            expectedSignature.addParameterType(converterType);
                        } else {
                            Type converterType = 
                                GenericTypes.cascadeMakeTypeOrParameterizedType(
                                    BubbledPropertyConverter.class, 
                                    propertyType);
                            expectedSignature.addParameterType(converterType);
                        }
                    }
                } else {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().invalidFactoryMethodName(methodInfo));
                }
                if (!expectedSignature.match(methodInfo)) {
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().invalidFactoryMethodSignature(
                                    methodInfo, 
                                    expectedSignature
                            )
                    );
                }
                if (CREATE_DETACH_EVENT.equals(methodInfo.getName())) {
                    createDetachEvent = methodInfo;
                } else if (CREATE_ATTACH_EVENT.equals(methodInfo.getName())) {
                    createAttachEvent = methodInfo;
                } else if (CREATE_REPLACE_EVENT.equals(methodInfo.getName())) {
                    createReplaceEvent = methodInfo;
                } else if (BUBBLE_EVENT.equals(methodInfo.getName())) {
                    bubbleEvent = methodInfo;
                }
            }
            return new Factory(
                    createDetachEvent,
                    createAttachEvent,
                    createReplaceEvent,
                    bubbleEvent);
        }
    }

    private static class Property {
        
        final EventProperty annotation;
        
        final MethodInfo getter;
        
        final int slotCount;
        
        public Property(
                EventProperty annotation, 
                MethodInfo getter) {
            this.annotation = annotation;
            this.getter = getter;
            this.slotCount = ASM.getSlotCount(getter.getReturnType());
        }
        
    }
    
    private static class Factory {
        
        final MethodInfo createDetachEvent;
        
        final MethodInfo createAttachEvent;
        
        final MethodInfo createReplaceEvent;
        
        final MethodInfo bubbleEvent;

        public Factory(
                MethodInfo createDetachEvent,
                MethodInfo createAttachEvent,
                MethodInfo createReplaceEvent, 
                MethodInfo bubbleEvent) {
            this.createDetachEvent = createDetachEvent;
            this.createAttachEvent = createAttachEvent;
            this.createReplaceEvent = createReplaceEvent;
            this.bubbleEvent = bubbleEvent;
        }
        
    }
    
    private static class PropertyLocations {
        
        private final XOrderedMap<Property, Integer> map;
        
        private final ModificationType modificationType;
        
        private final String totalDesc;
        
        public PropertyLocations(
                ModificationType modificationType, 
                int firstSlotIndex, 
                Collection<Property> properties) {
            Arguments.mustNotBeNull("modificationType", modificationType);
            this.modificationType = modificationType;
            int totalSlotCount = 0;
            StringBuilder totalDescBuilder = new StringBuilder();
            XOrderedMap<Property, Integer> map = new LinkedHashMap<Property, Integer>();
            for (Property property : properties) {
                map.put(property, firstSlotIndex + totalSlotCount);
                int slotCount = property.slotCount;
                if (this.modificationType == ModificationType.REPLACE && !property.annotation.shared()) {
                    slotCount <<= 1;
                }
                totalSlotCount += slotCount;
                String desc = ASM.getDescriptor(property.getter.getReturnType());
                totalDescBuilder.append(desc);
                if (this.modificationType == ModificationType.REPLACE && !property.annotation.shared()) {
                    totalDescBuilder.append(desc);
                }
            }
            this.map = map;
            this.totalDesc = totalDescBuilder.toString();
        }
        
        public XOrderedSet<Property> getProperties() {
            return this.map.keySet();
        }
        
        public String getTotalDesc() {
            return this.totalDesc;
        }
        
        public int getSlotIndex(Property property) {
            return this.map.get(property);
        }
    }
    
    private static class ExpectedSignature {
        
        private String methodName;
        
        private Type returnType;
        
        private List<Type> paremterTypes;
        
        public ExpectedSignature(String methodName, Type returnType) {
            this.methodName = methodName;
            this.returnType = returnType;
            this.paremterTypes = new ArrayList<Type>();
        }
        
        public void addParameterType(Type parameterType) {
            this.paremterTypes.add(parameterType);
        }
        
        public boolean match(MethodInfo methodInfo) {
            if (!this.returnType.equals(methodInfo.getResolvedGenericReturnType())) {
                return false;
            }
            List<Type> parameterTypes = this.paremterTypes;
            List<Type> methodParameterTypes = methodInfo.getResolvedGenericParameterTypes();
            if (parameterTypes.size() != methodParameterTypes.size()) {
                return false;
            }
            for (int i = this.paremterTypes.size() - 1; i >= 0; i--) {
                if (!parameterTypes.get(i).equals(methodParameterTypes.get(i))) {
                    return false;
                }
            }
            return true;
        }
        
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(ClassInfo.toString(this.returnType));
            builder.append(' ');
            builder.append(this.methodName);
            builder.append(" (");
            boolean isFirst = true;
            for (Type parameterType : this.paremterTypes) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    builder.append(", ");
                }
                builder.append(ClassInfo.toString(parameterType));
            }
            builder.append(')');
            return builder.toString();
        }
    }

    private static class FactoryGenerator {
        
        private final ClassInfo<?> factoryClassInfo;
        
        private final Factory factory;
        
        private final ClassInfo<?> eventClassInfo;
        
        private final Collection<Property> properties;
        
        private final String className;
        
        private final Class<?> implementation;
        
        public FactoryGenerator(
                ClassInfo<?> factoryClassInfo, 
                Factory factory,
                ClassInfo<?> eventClassInfo,
                Collection<Property> properties) {
            this.factoryClassInfo = factoryClassInfo;
            this.factory = factory;
            this.eventClassInfo = eventClassInfo;
            this.properties = properties;
            this.className = 
                    factoryClassInfo.getRawClass().getName() + 
                    "{Factory_" +
                    NAME_POSTFIX +
                    "}";
            
            Action<ClassVisitor> cvAction = new Action<ClassVisitor>() {

                @Override
                public void run(ClassVisitor cw) {
                    FactoryGenerator owner = FactoryGenerator.this;
                    
                    cw.visit(
                            Opcodes.V1_7, 
                            Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, 
                            owner.className.replace('.', '/'), 
                            null, 
                            "java/lang/Object", 
                            new String[] { owner.factoryClassInfo.getName().replace('.', '/') });
                    
                    MethodVisitor mv = cw.visitMethod(
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
                    if (owner.factory.createDetachEvent != null) {
                        owner.generateCreateProtosomaticEvent(cw, ModificationType.DETACH);
                    }
                    if (owner.factory.createAttachEvent != null) {
                        owner.generateCreateProtosomaticEvent(cw, ModificationType.ATTACH);
                    }
                    if (owner.factory.createReplaceEvent != null) {
                        owner.generateCreateProtosomaticEvent(cw, ModificationType.REPLACE);
                    }
                    if (owner.factory.bubbleEvent != null) {
                        owner.generateBubbleEvent(cw);
                    }
                    cw.visitEnd();
                }
                
            };
            this.implementation = ASM.loadDynamicClass(
                    this.factoryClassInfo.getRawClass().getClassLoader(), 
                    this.className, 
                    this.factoryClassInfo.getRawClass().getProtectionDomain(),
                    cvAction);
        }
        
        public Class<?> getImplementation() {
            return this.implementation;
        }
        
        private void generateCreateProtosomaticEvent(ClassVisitor cv, ModificationType modificationType) {
            Class<?> implementation = getRealEventClass(
                    this.eventClassInfo.getRawClass(), 
                    EventType.PROTOSOMATIC, 
                    modificationType);
            
            MethodInfo methodInfo; 
            switch (modificationType) {
            case DETACH:
                methodInfo = this.factory.createDetachEvent;
                break;
            case ATTACH:
                methodInfo = this.factory.createAttachEvent;
                break;
            default:
                methodInfo = this.factory.createReplaceEvent;
            }
            
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    methodInfo.getName(), 
                    ASM.getDescriptor(methodInfo, true), 
                    null, 
                    null);
            mv.visitCode();
            
            mv.visitTypeInsn(Opcodes.NEW, implementation.getName().replace('.', '/'));
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            PropertyLocations locations = new PropertyLocations(
                    modificationType, 3, this.properties);
            for (Property property : locations.getProperties()) {
                mv.visitVarInsn(
                        ASM.getLoadCode(property.getter.getReturnType()), 
                        locations.getSlotIndex(property));
                if (modificationType == ModificationType.REPLACE && !property.annotation.shared()) {
                    mv.visitVarInsn(
                            ASM.getLoadCode(property.getter.getReturnType()), 
                            locations.getSlotIndex(property) + property.slotCount);
                }
            }
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    implementation.getName().replace('.', '/'), 
                    "<init>", 
                    '(' +
                    ASM.getDescriptor(methodInfo.getParameterTypes()) +
                    ")V",
                    false);
            mv.visitInsn(Opcodes.ARETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateBubbleEvent(ClassVisitor cv) {
            Class<?> detachImplementation = getRealEventClass(
                    this.eventClassInfo.getRawClass(), 
                    EventType.BUBBLED, 
                    ModificationType.DETACH);
            Class<?> attachImplementation = getRealEventClass(
                    this.eventClassInfo.getRawClass(), 
                    EventType.BUBBLED, 
                    ModificationType.ATTACH);
            Class<?> replaceImplementation = getRealEventClass(
                    this.eventClassInfo.getRawClass(), 
                    EventType.BUBBLED, 
                    ModificationType.REPLACE);
            
            Metadata metadata = new Metadata(this.eventClassInfo.getRawClass());
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    BUBBLE_EVENT, 
                    ASM.getDescriptor(metadata.factory.bubbleEvent, true), 
                    null, 
                    null);
            mv.visitCode();
            
            /*
             * switch (cause.getEvent().getModificationType().ordinal()) {
             */
            final Label caseDetachLbl = new Label();
            final Label caseAttachLbl = new Label();
            final Label defaultLbl = new Label();
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    Cause.class.getName().replace('.', '/'), 
                    "getEvent", 
                    "()" + ASM.getDescriptor(ModificationEvent.class),
                    false);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    ModificationEvent.class.getName().replace('.', '/'), 
                    "getModificationType",
                    "()" + ASM.getDescriptor(ModificationType.class),
                    false);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    Enum.class.getName().replace('.', '/'), 
                    "ordinal", 
                    "()I",
                    false);
            mv.visitLookupSwitchInsn(
                    defaultLbl, 
                    new int[] { 
                            ModificationType.DETACH.ordinal(), 
                            ModificationType.ATTACH.ordinal() }, 
                    new Label[] { 
                            caseDetachLbl, 
                            caseAttachLbl });
            
            /*
             * case ModificationType.DETACH.ordinal:
             *    return new...;
             */
            mv.visitLabel(caseDetachLbl);
            mv.visitTypeInsn(Opcodes.NEW, detachImplementation.getName().replace('.', '/'));
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            for (int i = 0; i < metadata.properties.size(); i++) {
                mv.visitVarInsn(Opcodes.ALOAD, i + 3);
            }
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    detachImplementation.getName().replace('.', '/'), 
                    "<init>", 
                    '(' + 
                    ASM.getDescriptor(metadata.factory.bubbleEvent.getResolvedParameterTypes()) + 
                    ")V",
                    false);
            mv.visitInsn(Opcodes.ARETURN);
            
            /*
             * case ModificationType.DETACH.ordinal:
             *  return new...;
             */
            mv.visitLabel(caseAttachLbl);
            mv.visitTypeInsn(Opcodes.NEW, attachImplementation.getName().replace('.', '/'));
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            for (int i = 0; i < metadata.properties.size(); i++) {
                mv.visitVarInsn(Opcodes.ALOAD, i + 3);
            }
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    attachImplementation.getName().replace('.', '/'), 
                    "<init>", 
                    '(' + 
                    ASM.getDescriptor(metadata.factory.bubbleEvent.getResolvedParameterTypes()) + 
                    ")V",
                    false);
            mv.visitInsn(Opcodes.ARETURN);
            
            /*
             * default:
             *  return new...;
             */
            mv.visitLabel(defaultLbl);
            mv.visitTypeInsn(Opcodes.NEW, replaceImplementation.getName().replace('.', '/'));
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            for (int i = 0; i < metadata.properties.size(); i++) {
                mv.visitVarInsn(Opcodes.ALOAD, i + 3);
            }
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    replaceImplementation.getName().replace('.', '/'), 
                    "<init>", 
                    '(' + 
                    ASM.getDescriptor(metadata.factory.bubbleEvent.getResolvedParameterTypes()) + 
                    ")V",
                    false);
            mv.visitInsn(Opcodes.ARETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
    }
    
    private static abstract class AbstractEventGenerator {
        
        protected final String className;
        
        protected AbstractEventGenerator(String className) {
            this.className = className;
        }
        
        protected final void generateLazyCreateAttributeContext(
                MethodVisitor mv, 
                String attributeContextFieldName,
                int localVariableIndex) {
            
            /*
             * $temporary = @dup(this.<attributeContextFieldName>)
             */
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.className.replace('.', '/'), 
                    attributeContextFieldName, 
                    ASM.getDescriptor(EventAttributeContext.class));
            mv.visitInsn(Opcodes.DUP);
            
            /*
             * if ($temporary != null) {
             *      return $temporary;
             */
            final Label createAttributeContextLbl = new Label();
            mv.visitJumpInsn(Opcodes.IFNULL, createAttributeContextLbl);
            mv.visitInsn(Opcodes.ARETURN);
            
            /*
             * } else {
             *      @pop()
             */
            mv.visitLabel(createAttributeContextLbl);
            mv.visitInsn(Opcodes.POP);
            
            /*
             * EventAttributeContext tmp = EventAttributeContext.of(attributeScope);
             */
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASM.getInternalName(EventAttributeContext.class), 
                    "of", 
                    '(' +
                    ASM.getDescriptor(AttributeScope.class) +
                    ")" +
                    ASM.getDescriptor(EventAttributeContext.class),
                    false);
            mv.visitVarInsn(Opcodes.ASTORE, localVariableIndex);
            
            /*
             * this.<attributeContextFieldName> = tmp;
             */
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, localVariableIndex);
            mv.visitFieldInsn(
                    Opcodes.PUTFIELD, 
                    this.className.replace('.', '/'), 
                    attributeContextFieldName, 
                    ASM.getDescriptor(EventAttributeContext.class));
            
            /*
             *      return tmp;
             * } //end if
             */
            mv.visitVarInsn(Opcodes.ALOAD, localVariableIndex);
            mv.visitInsn(Opcodes.ARETURN);
        }
        
        protected final void generateGetPreModificationExceptionMethod(ClassVisitor cv) {
            XMethodVisitor mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PUBLIC, 
                    GET_PRE_MODIFICATION_THROWABLE, 
                    "()" + ASM.getDescriptor(Throwable.class), 
                    null,
                    null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    ASM.getInternalName(AttributeScope.class), 
                    AttributeScope.IN_ALL_CHAIN.name(), 
                    ASM.getDescriptor(AttributeScope.class));
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    this.className.replace('.', '/'), 
                    GET_ATTRIBUTE_CONTEXT, 
                    '(' +
                    ASM.getDescriptor(AttributeScope.class) +
                    ')' +
                    ASM.getDescriptor(EventAttributeContext.class),
                    false);
            mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(InAllChainAttributeContext.class));
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    ASM.getInternalName(InAllChainAttributeContext.class), 
                    "getPreThrowable", 
                    "()" + ASM.getDescriptor(Throwable.class),
                    true);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        protected final void generateGetModificationExceptionMethod(ClassVisitor cv) {
            XMethodVisitor mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PUBLIC, 
                    GET_MODIFICATION_THROWABLE, 
                    "()" + ASM.getDescriptor(Throwable.class), 
                    null,
                    null);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    ASM.getInternalName(AttributeScope.class), 
                    AttributeScope.GLOBAL.name(), 
                    ASM.getDescriptor(AttributeScope.class));
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    this.className.replace('.', '/'), 
                    GET_ATTRIBUTE_CONTEXT, 
                    '(' +
                    ASM.getDescriptor(AttributeScope.class) +
                    ')' +
                    ASM.getDescriptor(EventAttributeContext.class),
                    false);
            mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(GlobalAttributeContext.class));
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    ASM.getInternalName(GlobalAttributeContext.class), 
                    "getThrowable", 
                    "()" + ASM.getDescriptor(Throwable.class),
                    true);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        protected final void generateIsModificationSuccessed(ClassVisitor cv) {
            XMethodVisitor mv = ASM.visitMethod(
                    cv, 
                    Opcodes.ACC_PUBLIC, 
                    IS_MODIFICATION_SUCCESSED, 
                    "()Z", 
                    null,
                    null);
            mv.visitCode();
            mv.visitInsn(Opcodes.ICONST_0);
            
            final Label isPreModicationExceptionNullLabel = new Label();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    ASM.getInternalName(AttributeScope.class), 
                    AttributeScope.IN_ALL_CHAIN.name(), 
                    ASM.getDescriptor(AttributeScope.class));
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    this.className.replace('.', '/'), 
                    GET_ATTRIBUTE_CONTEXT, 
                    '(' +
                    ASM.getDescriptor(AttributeScope.class) +
                    ')' +
                    ASM.getDescriptor(EventAttributeContext.class),
                    false);
            mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(InAllChainAttributeContext.class));
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    ASM.getInternalName(InAllChainAttributeContext.class), 
                    "getPreThrowable", 
                    "()" + ASM.getDescriptor(Throwable.class),
                    true);
            mv.visitJumpInsn(Opcodes.IFNULL, isPreModicationExceptionNullLabel);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitLabel(isPreModicationExceptionNullLabel);
            
            final Label isModificationExceptionNullLabel = new Label();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    ASM.getInternalName(AttributeScope.class), 
                    AttributeScope.GLOBAL.name(), 
                    ASM.getDescriptor(AttributeScope.class));
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    this.className.replace('.', '/'), 
                    GET_ATTRIBUTE_CONTEXT, 
                    '(' +
                    ASM.getDescriptor(AttributeScope.class) +
                    ')' +
                    ASM.getDescriptor(EventAttributeContext.class),
                    false);
            mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(GlobalAttributeContext.class));
            final int globalACIndex = mv.aSlot("globalAC");
            mv.visitVarInsn(Opcodes.ASTORE, globalACIndex);
            mv.visitVarInsn(Opcodes.ALOAD, globalACIndex);
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    ASM.getInternalName(GlobalAttributeContext.class), 
                    "getThrowable", 
                    "()" + ASM.getDescriptor(Throwable.class),
                    true);
            mv.visitJumpInsn(Opcodes.IFNULL, isModificationExceptionNullLabel);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.IRETURN);
            mv.visitLabel(isModificationExceptionNullLabel);
            
            mv.visitVarInsn(Opcodes.ALOAD, globalACIndex);
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    ASM.getInternalName(GlobalAttributeContext.class), 
                    "isSuccessed", 
                    "()Z",
                    true);
            mv.visitInsn(Opcodes.IRETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        protected final void generateGetModificationBridgeMethods(ClassVisitor cv, Class<?> modificationClass, Class<?> rawEventClass) {
            if (rawEventClass == ModificationEvent.class) {
                return;
            }
            Class<?> eventClass = rawEventClass.getSuperclass();
            while (true) {
                Method method;
                try {
                    method = eventClass.getMethod(GET_MODIFICATION);
                } catch (NoSuchMethodException ex) {
                    break;
                }
                
                Class<?> bridgeModificationClass = method.getReturnType();
                if (bridgeModificationClass != modificationClass) {
                    MethodVisitor mv = cv.visitMethod(
                            Opcodes.ACC_PUBLIC | Opcodes.ACC_BRIDGE, 
                            GET_MODIFICATION, 
                            "()" + ASM.getDescriptor(bridgeModificationClass), 
                            null,
                            null);
                    mv.visitCode();
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL, 
                            this.className.replace('.', '/'), 
                            GET_MODIFICATION, 
                            "()" + ASM.getDescriptor(modificationClass),
                            false);
                    mv.visitInsn(Opcodes.ARETURN);
                    mv.visitMaxs(0, 0);
                    mv.visitEnd();
                }
                eventClass  = method.getDeclaringClass().getSuperclass();
                modificationClass = bridgeModificationClass;
            }
        }
        
        protected final void generateDispatchBridges(
                ClassVisitor cv, 
                Class<?> dipstachReturnClass) {
            for (Class<?> clazz = dipstachReturnClass.getSuperclass();
                    clazz != Object.class;
                    clazz = clazz.getSuperclass()) {
                MethodVisitor mv = cv.visitMethod(
                        Opcodes.ACC_PUBLIC | Opcodes.ACC_BRIDGE, 
                        DISPATCH, 
                        "(Ljava/lang/Object;)" +
                        ASM.getDescriptor(clazz), 
                        null,
                        null);
                mv.visitCode();
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        this.className.replace('.', '/'), 
                        DISPATCH, 
                        "(Ljava/lang/Object;)" + 
                        ASM.getDescriptor(dipstachReturnClass),
                        false);
                mv.visitInsn(Opcodes.ARETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
        }
    }
    
    private static abstract class HoldFieldEventGenerator extends AbstractEventGenerator {
        
        protected final ClassInfo<?> eventClassInfo;
        
        protected final Metadata metadata;
        
        protected final EventType eventType;
        
        protected final ModificationType modificationType;
        
        protected final Class<?> dispatchedImplementation;
        
        protected final Class<?> implementation;
        
        protected HoldFieldEventGenerator(
                ClassInfo<?> eventClassInfo, 
                Metadata metadata,
                EventType eventType,
                ModificationType modificationType) {
            super(eventClassInfo.getRawClass().getName() + 
                '{' +
                eventType.name().toLowerCase() +
                "->" +
                modificationType.name().toLowerCase() +
                '_' +
                NAME_POSTFIX +
                '}');
            Arguments.mustNotBeEqualToValue("eventType", eventType, EventType.DISPATCHED);
            this.eventClassInfo = eventClassInfo;
            this.metadata = metadata;
            this.eventType = eventType;
            this.modificationType = modificationType;
            this.dispatchedImplementation = getRealEventClass(
                    eventClassInfo.getRawClass(), 
                    EventType.DISPATCHED, 
                    null);
            
            Action<ClassVisitor> cvAction = new Action<ClassVisitor>() {
                
                @Override
                public void run(ClassVisitor cw) {
                    HoldFieldEventGenerator owner = HoldFieldEventGenerator.this;
                    cw.visit(
                            Opcodes.V1_7,
                            Opcodes.ACC_PUBLIC,
                            owner.className.replace('.', '/'),
                            null,
                            owner.eventClassInfo.getName().replace('.', '/'),
                            null);
                    
                    owner.generateFields(cw);
                    owner.onGenerateConstructors(cw);
                    owner.generateMethods(cw);
                    
                    cw.visitEnd();
                }
                
            };
            this.implementation = ASM.loadDynamicClass(
                    this.eventClassInfo.getRawClass().getClassLoader(),
                    this.className, 
                    this.eventClassInfo.getRawClass().getProtectionDomain(),
                    cvAction);
        }
        
        public Class<?> getImplementation() {
            return this.implementation;
        }
        
        @SuppressWarnings("unchecked")
        protected final <T> T[] getObjects(
                Property property, 
                T sharedObject, 
                T detachObject, 
                T attachObject) {
            Class<?> itemClass = null;
            for (Object o : new Object[] { sharedObject, detachObject, attachObject }) {
                if (o != null) {
                    Class<?> clazz = o.getClass();
                    if (itemClass != null && clazz != itemClass) {
                        throw new IllegalArgumentException(
                                LAZY_RESOURCE.get().parameterMustBeSameType(
                                        "sharedObject",
                                        "detachObject",
                                        "attachObject"
                                )
                        );
                    }
                    itemClass = clazz;
                }
            }
            if (itemClass == null) {
                throw new IllegalArgumentException(
                        LAZY_RESOURCE.get().atLeastOneParameterMustNotBeNull(
                                "sharedObject",
                                "detachObject",
                                "attachObject"
                        )
                );
            }
            if (property.annotation.shared()) {
                T[] array = (T[])Array.newInstance(itemClass, 1);
                array[0] = sharedObject;
                return array;
            }
            if (this.modificationType == ModificationType.DETACH) {
                T[] array = (T[])Array.newInstance(itemClass, 1);
                array[0] = detachObject;
                return array;
            }
            if (this.modificationType == ModificationType.ATTACH) {
                T[] array = (T[])Array.newInstance(itemClass, 1);
                array[0] = attachObject;
                return array;
            }
            T[] array = (T[])Array.newInstance(itemClass, 2);
            array[0] = detachObject;
            array[1] = attachObject;
            return array;
        }
        
        protected abstract void onGenerateConstructors(ClassVisitor cv);
        
        protected abstract void onGenerateInitByRawDataConstructor(ClassVisitor cv);

        protected abstract void onGenerateOtherFieldsExceptPropertyFields(ClassVisitor cv);
        
        protected void onGenerateGetModification(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    GET_MODIFICATION, 
                    "()" + ASM.getDescriptor(this.metadata.modificationClass), 
                    null,
                    null);
            mv.visitCode();
            
            mv.visitInsn(Opcodes.ACONST_NULL);
            mv.visitInsn(Opcodes.ARETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        protected void onGenerateGetCause(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "getCause", 
                    "()" + ASM.getDescriptor(Cause.class), 
                    null,
                    null);
            mv.visitCode();
            
            mv.visitInsn(Opcodes.ACONST_NULL);
            mv.visitInsn(Opcodes.ARETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        protected abstract void onGenerateGetAttributeContext(ClassVisitor cv);

        protected final void generatePropertyFields(ClassVisitor cv) {
            for (Property property : this.metadata.properties) {
                String[] fieldPostfixes = 
                    this.getObjects(property, SHARED_POSTFIX, DETACHED_POSTFIX, ATTACHED_POSTFIX);
                for (String fieldPostfix : fieldPostfixes) {
                    cv.visitField(
                            Opcodes.ACC_PRIVATE, 
                            property.annotation.name() + fieldPostfix, 
                            ASM.getDescriptor(property.getter.getReturnType()), 
                            null,
                            null)
                            .visitEnd();
                }
            }
        }
        
        protected final void generateAssignPropertiesByRawData(
                MethodVisitor mv, PropertyLocations locations) {
            for (Property property : locations.getProperties()) {
                String[] fieldPostfixes = 
                    this.getObjects(property, SHARED_POSTFIX, DETACHED_POSTFIX, ATTACHED_POSTFIX);
                for (int i = 0; i < fieldPostfixes.length; i++) {
                    int slotIndex = locations.getSlotIndex(property);
                    if (i != 0) {
                        slotIndex += property.slotCount;
                    }
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitVarInsn(
                            ASM.getLoadCode(property.getter.getResolvedReturnType()),
                            slotIndex);
                    mv.visitFieldInsn(
                            Opcodes.PUTFIELD, 
                            this.className.replace('.', '/'), 
                            property.annotation.name() + fieldPostfixes[i], 
                            ASM.getDescriptor(property.getter.getResolvedReturnType()));
                }
            }
        }
        
        protected final void generateReturnAttributeContextOfSourceEvent(MethodVisitor mv) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.className.replace('.', '/'), 
                    CAUSE, 
                    ASM.getDescriptor(Cause.class));
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    Cause.class.getName().replace('.', '/'), 
                    "getEvent", 
                    "()" + ASM.getDescriptor(ModificationEvent.class),
                    false);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    ModificationEvent.class.getName().replace('.', '/'), 
                    "getAttributeContext", 
                    '(' +
                    ASM.getDescriptor(AttributeScope.class) +
                    ')' + 
                    ASM.getDescriptor(EventAttributeContext.class),
                    false);
            mv.visitInsn(Opcodes.ARETURN);
        }

        private void generateFields(ClassVisitor cv) {
            this.onGenerateOtherFieldsExceptPropertyFields(cv);
            this.generatePropertyFields(cv);
        }

        private void generateMethods(ClassVisitor cv) {
            this.onGenerateGetAttributeContext(cv);
            this.onGenerateGetModification(cv);
            this.onGenerateGetCause(cv);
            this.generateGetEventTypeMethod(cv);
            this.generateGetModificationTypeMethod(cv);
            this.generateGetPropertyMethods(cv);
            this.generateDispatchMethod(cv);
            this.generateGetPreModificationExceptionMethod(cv);
            this.generateGetModificationExceptionMethod(cv);
            this.generateIsModificationSuccessed(cv);
            this.generateGetModificationBridgeMethods(cv, this.metadata.modificationClass, this.metadata.rawEventClass);
            this.generateDispatchBridges(cv, this.metadata.dipstachReturnClass);
        }
        
        private void generateGetEventTypeMethod(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "getEventType", 
                    "()" + ASM.getDescriptor(EventType.class), 
                    null, 
                    null);
            mv.visitCode();
            
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    EventType.class.getName().replace('.', '/'), 
                    this.eventType.name(), 
                    ASM.getDescriptor(EventType.class));
            mv.visitInsn(Opcodes.ARETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateGetModificationTypeMethod(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "getModificationType", 
                    "()" + ASM.getDescriptor(ModificationType.class), 
                    null,
                    null);          
            mv.visitCode();
            
            mv.visitFieldInsn(
                    Opcodes.GETSTATIC, 
                    ModificationType.class.getName().replace('.', '/'), 
                    this.modificationType.name(), 
                    ASM.getDescriptor(ModificationType.class));
            mv.visitInsn(Opcodes.ARETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateGetPropertyMethods(ClassVisitor cv) {
            for (Property property : this.metadata.properties) {
                MethodVisitor mv = cv.visitMethod(
                        Opcodes.ACC_PUBLIC, 
                        property.getter.getName(), 
                        ASM.getDescriptor(property.getter, true),
                        null,
                        null);
                mv.visitCode();
                
                if (property.annotation.shared()) {
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitFieldInsn(
                            Opcodes.GETFIELD, 
                            this.className.replace('.', '/'), 
                            property.annotation.name() + SHARED_POSTFIX, 
                            ASM.getDescriptor(property.getter.getReturnType()));
                } else {
                    final Label versionIsAttachLbl = new Label();
                    final Label versionIsAttachEndLbl = new Label();
                    
                    argumentMustNotBeNull(mv, "version", 1);
                    mv.visitVarInsn(Opcodes.ALOAD, 1);
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL, 
                            Enum.class.getName().replace('.', '/'), 
                            "ordinal", 
                            "()I",
                            false);
                    mv.visitLdcInsn(ModificationType.DETACH.ordinal());
                    mv.visitJumpInsn(Opcodes.IF_ICMPNE, versionIsAttachLbl);
                    
                    if (this.modificationType == ModificationType.ATTACH) {
                        mv.visitInsn(ASM.getDefaultCode(property.getter.getReturnType()));
                    } else {
                        mv.visitVarInsn(Opcodes.ALOAD, 0);
                        mv.visitFieldInsn(
                                Opcodes.GETFIELD, 
                                this.className.replace('.', '/'), 
                                property.annotation.name() + DETACHED_POSTFIX, 
                                ASM.getDescriptor(property.getter.getReturnType()));
                    }
                    
                    mv.visitJumpInsn(Opcodes.GOTO, versionIsAttachEndLbl);
                    mv.visitLabel(versionIsAttachLbl);
                    
                    if (this.modificationType == ModificationType.DETACH) {
                        mv.visitInsn(ASM.getDefaultCode(property.getter.getReturnType()));
                    } else {
                        mv.visitVarInsn(Opcodes.ALOAD, 0);
                        mv.visitFieldInsn(
                                Opcodes.GETFIELD, 
                                this.className.replace('.', '/'), 
                                property.annotation.name() + ATTACHED_POSTFIX, 
                                ASM.getDescriptor(property.getter.getReturnType()));
                    }
                    
                    mv.visitLabel(versionIsAttachEndLbl);
                }   
                
                mv.visitInsn(ASM.getReturnCode(property.getter.getReturnType()));
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
        }
        
        private void generateDispatchMethod(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    DISPATCH, 
                    "(Ljava/lang/Object;)" + 
                    ASM.getDescriptor(this.metadata.dipstachReturnClass), 
                    null,
                    null);
            mv.visitCode();
            
            mv.visitTypeInsn(
                    Opcodes.NEW, 
                    this.dispatchedImplementation.getName().replace('.', '/'));
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    this.dispatchedImplementation.getName().replace('.', '/'), 
                    "<init>", 
                    "(Ljava/lang/Object;" +
                    ASM.getDescriptor(this.metadata.rawEventClass) +
                    ")V",
                    false);
            mv.visitInsn(Opcodes.ARETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }
    
    private static class ProtosomaticEventGenerator extends HoldFieldEventGenerator {
        
        public ProtosomaticEventGenerator(
                ClassInfo<?> eventClassInfo, 
                Metadata metadata,
                ModificationType modificationType) {
            super(
                    eventClassInfo, 
                    metadata, 
                    EventType.PROTOSOMATIC, 
                    modificationType);
        }

        @Override
        protected void onGenerateConstructors(ClassVisitor cv) {
            this.onGenerateInitByRawDataConstructor(cv);
        }

        @Override
        protected void onGenerateInitByRawDataConstructor(ClassVisitor cv) {
            PropertyLocations locations = new PropertyLocations(
                    this.modificationType, 3, this.metadata.properties);
            MethodVisitor mv = cv.visitMethod(
                    0, 
                    "<init>", 
                    '(' +
                    ASM.getDescriptor(Object.class) +
                    ASM.getDescriptor(this.metadata.modificationClass) +
                    locations.getTotalDesc() +
                    ")V", 
                    null, 
                    null);
            mv.visitCode();
            
            /*
             * super(source);
             */
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    this.eventClassInfo.getName().replace('.', '/'), 
                    "<init>", 
                    "(Ljava/lang/Object;)V",
                    false);
            
            /*
             * if (modification == null) {
             *      throw new ...;
             * }
             * this["{modfication}"] = modification;
             */
            argumentMustNotBeNull(mv, "modification", 2);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitFieldInsn(
                    Opcodes.PUTFIELD, 
                    this.className.replace('.', '/'), 
                    MODIFICATION, 
                    ASM.getDescriptor(this.metadata.modificationClass));
            
            this.generateAssignPropertiesByRawData(mv, locations);
            
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        @Override
        protected void onGenerateOtherFieldsExceptPropertyFields(ClassVisitor cv) {
            cv.visitField(
                    Opcodes.ACC_PRIVATE, 
                    MODIFICATION,
                    ASM.getDescriptor(this.metadata.modificationClass),
                    null,
                    null)
                    .visitEnd();
            cv.visitField(
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_TRANSIENT, 
                    LOCAL_ATTRIBUTE_CONTEXT,
                    ASM.getDescriptor(EventAttributeContext.class),
                    null,
                    null)
                    .visitEnd();
            cv.visitField(
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_TRANSIENT, 
                    ATTRIBUTE_CONTEXT_IN_BUBBLE_CHAIN,
                    ASM.getDescriptor(EventAttributeContext.class),
                    null,
                    null)
                    .visitEnd();
            cv.visitField(
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_TRANSIENT, 
                    ATTRIBUTE_CONTEXT_IN_DISPATCH_CHAIN,
                    ASM.getDescriptor(EventAttributeContext.class),
                    null,
                    null)
                    .visitEnd();
            cv.visitField(
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_TRANSIENT, 
                    ATTRIBUTE_CONTEXT_IN_ALL_CHAIN,
                    ASM.getDescriptor(EventAttributeContext.class),
                    null,
                    null)
                    .visitEnd();
            //AttributeScope.GLOBAL => this["{modification}"].getAttributeContext();
        }

        @Override
        protected void onGenerateGetModification(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    GET_MODIFICATION, 
                    "()" + ASM.getDescriptor(this.metadata.modificationClass), 
                    null,
                    null);
            mv.visitCode();
            
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.className.replace('.', '/'), 
                    MODIFICATION, 
                    ASM.getDescriptor(this.metadata.modificationClass));
            mv.visitInsn(Opcodes.ARETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        @Override
        protected void onGenerateGetAttributeContext(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "getAttributeContext", 
                    '(' +
                    ASM.getDescriptor(AttributeScope.class) +
                    ')' +
                    ASM.getDescriptor(EventAttributeContext.class), 
                    null,
                    null);
            mv.visitCode();
            
            final int localVariableIndex = 2;
            
            /*
             * $temporary = attributeScope.ordinal()
             */
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    Enum.class.getName().replace('.', '/'), 
                    "ordinal", 
                    "()I",
                    false);
            
            /*
             * switch (@temporary)
             */
            final Label caseLocalLbl = new Label();
            final Label caseInBubbleChainLbl = new Label();
            final Label caseInDispatchChainLbl = new Label();
            final Label caseInAllChainLbl = new Label();
            final Label defaultLbl = new Label();
            mv.visitLookupSwitchInsn(
                    defaultLbl, 
                    new int[]{
                            AttributeScope.LOCAL.ordinal(),
                            AttributeScope.IN_BUBBLE_CHAIN.ordinal(),
                            AttributeScope.IN_DISPATCH_CHAIN.ordinal(),
                            AttributeScope.IN_ALL_CHAIN.ordinal()
                    }, 
                    new Label[]{
                            caseLocalLbl,
                            caseInBubbleChainLbl,
                            caseInDispatchChainLbl,
                            caseInAllChainLbl,
                    });
            
            /*
             * case AttributeScope.LOCAL.ordinal():
             */
            mv.visitLabel(caseLocalLbl);
            this.generateLazyCreateAttributeContext(
                    mv, 
                    LOCAL_ATTRIBUTE_CONTEXT, 
                    localVariableIndex);
            
            /*
             * case AttributeScope.IN_BUBBLE_CHAIN.ordinal():
             */
            mv.visitLabel(caseInBubbleChainLbl);
            this.generateLazyCreateAttributeContext(
                    mv, 
                    ATTRIBUTE_CONTEXT_IN_BUBBLE_CHAIN, 
                    localVariableIndex);
            
            /*
             * case AttributeScope.IN_DISPATCH_CHAIN.ordinal():
             */
            mv.visitLabel(caseInDispatchChainLbl);
            this.generateLazyCreateAttributeContext(
                    mv, 
                    ATTRIBUTE_CONTEXT_IN_DISPATCH_CHAIN, 
                    localVariableIndex);
            
            /*
             * case AttributeScope.IN_ALL_CHAIN.ordinal():
             */
            mv.visitLabel(caseInAllChainLbl);
            this.generateLazyCreateAttributeContext(
                    mv, 
                    ATTRIBUTE_CONTEXT_IN_ALL_CHAIN, 
                    localVariableIndex);
            
            /*
             * default:
             */
            mv.visitLabel(defaultLbl);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.className.replace('.', '/'), 
                    MODIFICATION, 
                    ASM.getDescriptor(this.metadata.modificationClass));
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    this.metadata.modificationClass.getName().replace('.', '/'), 
                    "getAttributeContext", 
                    "()" + ASM.getDescriptor(EventAttributeContext.class),
                    true);
            mv.visitInsn(Opcodes.ARETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
    }
    
    private static class BubbledEventGenerator extends HoldFieldEventGenerator {

        protected BubbledEventGenerator(
                ClassInfo<?> eventClassInfo,
                Metadata metadata, 
                ModificationType modificationType) {
            super(
                    eventClassInfo, 
                    metadata, 
                    EventType.BUBBLED, 
                    modificationType);
        }

        @Override
        protected void onGenerateConstructors(ClassVisitor cv) {
            this.onGenerateInitByRawDataConstructor(cv);
            if (!this.metadata.properties.isEmpty()) {
                this.generateInitByConvertersConstructor(cv);
            }
        }
        
        @Override
        protected void onGenerateInitByRawDataConstructor(ClassVisitor cv) {
            
            PropertyLocations locations = new PropertyLocations(
                    this.modificationType, 3, this.metadata.properties);
            MethodVisitor mv = cv.visitMethod(
                    //This method is only invoked by reflection way
                    //by the SerializedReplacement, so it is private.
                    Opcodes.ACC_PRIVATE, 
                    "<init>", 
                    '(' +
                    ASM.getDescriptor(Object.class) +
                    ASM.getDescriptor(Cause.class) +
                    locations.getTotalDesc() +
                    ")V", 
                    null, 
                    null);
            mv.visitCode();
            
            /*
             * super(source, cause);
             */
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    this.eventClassInfo.getName().replace('.', '/'), 
                    "<init>", 
                    '(' +
                    ASM.getDescriptor(Object.class) +
                    ASM.getDescriptor(Cause.class) +
                    ")V",
                    false);
            
            /*
             * this["{cause}"] = cause;
             */
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitFieldInsn(
                    Opcodes.PUTFIELD, 
                    this.className.replace('.', '/'), 
                    CAUSE, 
                    ASM.getDescriptor(Cause.class));
            
            this.generateAssignPropertiesByRawData(mv, locations);
            
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        @Override
        protected void onGenerateOtherFieldsExceptPropertyFields(ClassVisitor cv) {
            cv.visitField(
                    Opcodes.ACC_PRIVATE, 
                    CAUSE, 
                    ASM.getDescriptor(Cause.class), 
                    null, 
                    null)
                    .visitEnd();
            cv.visitField(
                    Opcodes.ACC_PRIVATE, 
                    LOCAL_ATTRIBUTE_CONTEXT, 
                    ASM.getDescriptor(EventAttributeContext.class), 
                    null, 
                    null)
                    .visitEnd();
            cv.visitField(
                    Opcodes.ACC_PRIVATE, 
                    ATTRIBUTE_CONTEXT_IN_DISPATCH_CHAIN, 
                    ASM.getDescriptor(EventAttributeContext.class), 
                    null, 
                    null)
                    .visitEnd();
        }

        @Override
        protected void onGenerateGetCause(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "getCause", 
                    "()" + ASM.getDescriptor(Cause.class), 
                    null,
                    null);
            mv.visitCode();
            
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.className.replace('.', '/'), 
                    CAUSE, 
                    ASM.getDescriptor(Cause.class));
            mv.visitInsn(Opcodes.ARETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        @Override
        protected void onGenerateGetAttributeContext(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "getAttributeContext", 
                    '(' +
                    ASM.getDescriptor(AttributeScope.class) +
                    ')' +
                    ASM.getDescriptor(EventAttributeContext.class), 
                    null,
                    null);
            mv.visitCode();
            
            final int localVariableIndex = 2;
            
            /*
             * $temporary = attributeScope.ordinal()
             */
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    Enum.class.getName().replace('.', '/'), 
                    "ordinal", 
                    "()I",
                    false);
            
            /*
             * switch (@temporary)
             */
            final Label caseLocalLbl = new Label();
            final Label caseInBubbleChainLbl = new Label();
            final Label caseInDispatchChainLbl = new Label();
            final Label caseInAllChainLbl = new Label();
            final Label defaultLbl = new Label();
            mv.visitLookupSwitchInsn(
                    defaultLbl, 
                    new int[]{
                            AttributeScope.LOCAL.ordinal(),
                            AttributeScope.IN_BUBBLE_CHAIN.ordinal(),
                            AttributeScope.IN_DISPATCH_CHAIN.ordinal(),
                            AttributeScope.IN_ALL_CHAIN.ordinal()
                    }, 
                    new Label[]{
                            caseLocalLbl,
                            caseInBubbleChainLbl,
                            caseInDispatchChainLbl,
                            caseInAllChainLbl,
                    });
            
            /*
             * case AttributeScope.LOCAL.ordinal():
             */
            mv.visitLabel(caseLocalLbl);
            this.generateLazyCreateAttributeContext(
                    mv, 
                    LOCAL_ATTRIBUTE_CONTEXT, 
                    localVariableIndex);
            
            /*
             * case AttributeScope.IN_BUBBLE_CHAIN.ordinal():
             */
            mv.visitLabel(caseInBubbleChainLbl);
            this.generateReturnAttributeContextOfSourceEvent(mv);
            
            /*
             * case AttributeScope.IN_DISPATCH_CHAIN.ordinal():
             */
            mv.visitLabel(caseInDispatchChainLbl);
            this.generateLazyCreateAttributeContext(
                    mv, 
                    ATTRIBUTE_CONTEXT_IN_DISPATCH_CHAIN, 
                    localVariableIndex);
            
            /*
             * case AttributeScope.IN_ALL_CHAIN.ordinal():
             */
            mv.visitLabel(caseInAllChainLbl);
            this.generateReturnAttributeContextOfSourceEvent(mv);
            
            /*
             * default:
             */
            mv.visitLabel(defaultLbl);
            this.generateReturnAttributeContextOfSourceEvent(mv);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateInitByConvertersConstructor(ClassVisitor cv) {
            
            assert this.metadata.factory.bubbleEvent != null;
            
            MethodInfo bubbleEventMethodInfo =
                this
                .metadata
                .factory
                .bubbleEvent;
            
            XMethodVisitor mv = ASM.visitMethod(
                    cv,
                    Opcodes.ACC_PUBLIC, 
                    "<init>",
                    '(' +
                    ASM.getDescriptor(bubbleEventMethodInfo.getResolvedParameterTypes()) +
                    ")V",
                    null,
                    null);
            mv.visitCode();
            
            /*
             * super(source, cause);
             */
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    this.eventClassInfo.getName().replace('.', '/'), 
                    "<init>", 
                    '(' +
                    ASM.getDescriptor(Object.class) +
                    ASM.getDescriptor(Cause.class) +
                    ")V",
                    false);
            
            /*
             * this["{cause}"] = cause;
             */
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitFieldInsn(
                    Opcodes.PUTFIELD, 
                    this.className.replace('.', '/'), 
                    CAUSE, 
                    ASM.getDescriptor(Cause.class));
            
            /*
             * ModificationEvent causeEvent = cause.getEvent();
             */
            final int localSlotIndex = 
                bubbleEventMethodInfo.getParameterTypes().size() + 
                1 /* this */;
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    Cause.class.getName().replace('.', '/'), 
                    "getEvent", 
                    "()" + ASM.getDescriptor(ModificationEvent.class),
                    false);
            mv.visitVarInsn(Opcodes.ASTORE, localSlotIndex);
            
            /*
             * Apply converters
             */
            int converterSlotIndex = 3;
            for (Property property : this.metadata.properties) {
                this.transformAndStorageProperty(
                        mv,
                        property, 
                        converterSlotIndex, 
                        localSlotIndex);
                converterSlotIndex++;
            }
            
            mv.visitInsn(Opcodes.RETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void transformAndStorageProperty(
                XMethodVisitor mv,
                Property property, 
                int converterSlotIndex, 
                int localSlotIndex) {
            
            final Label transfromerIsNotNullLbl = new Label();
            final Label transfromerEndIfLbl = new Label();
            
            /*
             * if (transfromer == null) {
             */
            mv.visitVarInsn(Opcodes.ALOAD, converterSlotIndex);
            mv.visitJumpInsn(Opcodes.IFNONNULL, transfromerIsNotNullLbl);
            
            /*
             * Arguments.mustBeInstanceOfValue("causeEvent", causeEvent, <ThisEventType>);
             */
            mv.visitLdcInsn("causeEvent");
            mv.visitVarInsn(Opcodes.ALOAD, localSlotIndex);
            mv.visitLdcInsn(org.babyfish.org.objectweb.asm.Type.getType(this.metadata.rawEventClass));
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC, 
                    ASM.getInternalName(Arguments.class), 
                    "mustBeInstanceOfValue", 
                    "(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;",
                    false);
            mv.visitInsn(Opcodes.POP);
            
            this.getPropertyAndSetWhenConverterIsNull(
                    mv, 
                    property, 
                    localSlotIndex);
            
            /*
             * } else {
             */
            mv.visitJumpInsn(Opcodes.GOTO, transfromerEndIfLbl);
            mv.visitLabel(transfromerIsNotNullLbl);
            
            this.generateGetAndStorePropertyWhenConverterIsNotNull(
                    mv, 
                    property, 
                    converterSlotIndex,
                    localSlotIndex);
            
            /*
             * } //end "if(converter == null)"
             */
            mv.visitLabel(transfromerEndIfLbl);
        }
        
        private void getPropertyAndSetWhenConverterIsNull(
                MethodVisitor mv,
                Property property,
                int localSlotIndex) {
            
            String[] fieldPostfixs = this.getObjects(
                    property, 
                    SHARED_POSTFIX, 
                    DETACHED_POSTFIX, 
                    ATTACHED_POSTFIX);
            PropertyVersion[] propertyGetterArguments = this.getObjects(
                    property, 
                    null, 
                    PropertyVersion.DETACH, 
                    PropertyVersion.ATTACH);
            for (int i = 0; i < fieldPostfixs.length; i++) {
                /*
                 * load this;
                 */
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                
                /*
                 * $temporary = causeEvent["getter"](...); 
                 */
                mv.visitVarInsn(Opcodes.ALOAD, localSlotIndex);
                mv.visitTypeInsn(
                        Opcodes.CHECKCAST, 
                        this.metadata.rawEventClass.getName().replace('.', '/'));
                if (propertyGetterArguments[i] != null) {
                    ASM.visitEnumLdc(mv, propertyGetterArguments[i]);
                }
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        this.metadata.rawEventClass.getName().replace('.', '/'), 
                        property.getter.getName(), 
                        ASM.getDescriptor(property.getter, true),
                        false);
                
                mv.visitFieldInsn(
                        Opcodes.PUTFIELD, 
                        this.className.replace('.', '/'), 
                        property.annotation.name() + fieldPostfixs[i], 
                        ASM.getDescriptor(property.getter.getResolvedReturnType()));
            }
        }
        
        private void generateGetAndStorePropertyWhenConverterIsNotNull(
                XMethodVisitor mv,
                Property property,
                int converterSlotIndex,
                int localSlotIndex) {
            
            final Class<?> propertyClass = property.getter.getResolvedReturnType();
            final Class<?> propertyType = propertyClass;
            final int bubbledPropertyObjectSlotIndex = localSlotIndex + 1;
            final Class<?> bubbledPropertyConverterClass =
                property.annotation.shared() ?
                        BubbledSharedPropertyConverter.class :
                        BubbledPropertyConverter.class;
            final Class<?> bubbledPropertyClass = 
                property.annotation.shared() ?
                        BubbledSharedProperty.class :
                        BubbledProperty.class;
            final String[] fieldPostfixes = 
                this.getObjects(property, SHARED_POSTFIX, DETACHED_POSTFIX, ATTACHED_POSTFIX);
            final String[] bubbledPropertyObjectMethodPostfixes = 
                this.getObjects(property, "", "ToDetach", "ToAttach");
            
            /*
             * 
             * BubbledSharedProperty bubbledSharedProperty =
             *      new BubbledSharedProperty(cause);
             * or
             * BubbledProperty bubbledProperty =
             *      new BubbledProperty(cause);
             */
            mv.visitTypeInsn(
                    Opcodes.NEW, 
                    bubbledPropertyClass.getName().replace('.', '/'));
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    bubbledPropertyClass.getName().replace('.', '/'),
                    "<init>",
                    '(' + ASM.getDescriptor(Cause.class) + ")V",
                    false);
            if (propertyClass.isPrimitive()) {
                for (String bubbledPropertyObjectMethodPostfix : bubbledPropertyObjectMethodPostfixes) {
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitBox(propertyClass, new Action<XMethodVisitor>() {
                        @Override
                        public void run(XMethodVisitor mv) {
                            mv.visitInsn(ASM.getDefaultCode(propertyClass));
                        }
                    });
                    mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL, 
                            bubbledPropertyClass.getName().replace('.', '/'),
                            "setValue" + bubbledPropertyObjectMethodPostfix, 
                            "(Ljava/lang/Object;)V",
                            false);
                } 
            }
            mv.visitVarInsn(Opcodes.ASTORE, bubbledPropertyObjectSlotIndex);
            
            /*
             * converter.convert(bubbledSharedProperty);
             * or
             * converter.convert(bubbledProperty);
             */
            mv.visitVarInsn(Opcodes.ALOAD, converterSlotIndex);
            mv.visitVarInsn(Opcodes.ALOAD, bubbledPropertyObjectSlotIndex);
            mv.visitMethodInsn(
                    Opcodes.INVOKEINTERFACE, 
                    bubbledPropertyConverterClass.getName().replace('.', '/'), 
                    "convert", 
                    '(' + ASM.getDescriptor(bubbledPropertyClass) + ")V",
                    true);
            
            /*
             * this.["<property_name>:detach"] = bubbledProperty.getValueToDetach();
             * this.["<property_name>:attach"] = bubbledProperty.getValueToAttach();
             */
            
            for (int i = 0; i < fieldPostfixes.length; i++) {
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitVarInsn(Opcodes.ALOAD, bubbledPropertyObjectSlotIndex);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        bubbledPropertyClass.getName().replace('.', '/'), 
                        "getValue" + bubbledPropertyObjectMethodPostfixes[i], 
                        "()Ljava/lang/Object;",
                        false);
                if (propertyClass.isPrimitive()) {
                    mv.visitUnbox(propertyClass, XMethodVisitor.JVM_PRIMTIVIE_DEFAULT_VALUE);
                } else {
                    mv.visitTypeInsn(Opcodes.CHECKCAST, propertyClass.getName().replace('.', '/'));
                }
                mv.visitFieldInsn(
                        Opcodes.PUTFIELD, 
                        this.className.replace('.', '/'), 
                        property.annotation.name() + fieldPostfixes[i], 
                        ASM.getDescriptor(propertyType));
            }
        }
        
    }
    
    private static class DispatchedEventGenerator extends AbstractEventGenerator {

        private final ClassInfo<?> eventClassInfo;
        
        private final Metadata metadata;
        
        private final Class<?> implementation;

        public DispatchedEventGenerator(
                ClassInfo<?> eventClassInfo, Metadata metdata) {
            super( 
                    eventClassInfo.getRawClass().getName() + 
                    '{' + 
                    EventType.DISPATCHED +
                    '_' +
                    NAME_POSTFIX +
                    '}');
            
            this.eventClassInfo = eventClassInfo;
            this.metadata = metdata;
            
            Action<ClassVisitor> cvAction = new Action<ClassVisitor>() {

                @Override
                public void run(ClassVisitor cw) {
                    
                    DispatchedEventGenerator owner = DispatchedEventGenerator.this;
                    cw.visit(
                            Opcodes.V1_7,
                            Opcodes.ACC_PUBLIC,
                            owner.className.replace('.', '/'),
                            null,
                            owner.eventClassInfo.getName().replace('.', '/'),
                            null);
                    
                    owner.generateConstructor(cw);
                    owner.generateFields(cw);
                    owner.generateGetEventTypeMethod(cw);
                    owner.generateGetModificationTypeMethod(cw);
                    owner.generateGetModificationMethod(cw);
                    owner.generateGetCauseMethod(cw);
                    owner.generateGetAttributeContext(cw);
                    owner.generateGetPreModificationExceptionMethod(cw);
                    owner.generateGetModificationExceptionMethod(cw);
                    owner.generateIsModificationSuccessed(cw);
                    for (Property property : owner.metadata.properties) {
                        owner.generateGetPropertyMethod(cw, property);
                    }
                    owner.generateDispatchMethod(cw);
                    owner.generateGetModificationBridgeMethods(cw, owner.metadata.modificationClass, owner.metadata.rawEventClass);
                    owner.generateDispatchBridges(cw, owner.metadata.dipstachReturnClass);
                    
                    cw.visitEnd();
                }
                
            };
            this.implementation = ASM.loadDynamicClass(
                    this.eventClassInfo.getRawClass().getClassLoader(),
                    this.className, 
                    this.eventClassInfo.getRawClass().getProtectionDomain(),
                    cvAction);
        }
        
        public Class<?> getImplementation() {
            return this.implementation;
        }
        
        private void generateFields(ClassVisitor cv) {
            cv.visitField(
                    Opcodes.ACC_PRIVATE, 
                    SOURCE_EVENT, 
                    ASM.getDescriptor(this.metadata.rawEventClass), 
                    null,
                    null)
                    .visitEnd();
            cv.visitField(
                    Opcodes.ACC_PRIVATE, 
                    LOCAL_ATTRIBUTE_CONTEXT, 
                    ASM.getDescriptor(EventAttributeContext.class), 
                    null, 
                    null)
                    .visitEnd();
            cv.visitField(
                    Opcodes.ACC_PRIVATE, 
                    ATTRIBUTE_CONTEXT_IN_BUBBLE_CHAIN, 
                    ASM.getDescriptor(EventAttributeContext.class), 
                    null, 
                    null)
                    .visitEnd();
        }
        
        private void generateConstructor(ClassVisitor cv) {
            
            MethodVisitor mv = cv.visitMethod(
                    0, 
                    "<init>", 
                    "(Ljava/lang/Object;" +
                    ASM.getDescriptor(this.metadata.rawEventClass) +
                    ")V", 
                    null,
                    null);
            mv.visitCode();
            
            /*
             * super(source, dispatchSourceEvent);
             */
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    this.eventClassInfo.getName().replace('.', '/'), 
                    "<init>", 
                    '(' +
                    ASM.getDescriptor(Object.class) +
                    ASM.getDescriptor(this.metadata.rawEventClass) +
                    ")V",
                    false);
            
            /*
             * Arguments.mustNotBeNull("{source_event}", [source_event]);
             * this.[source_event] = [source_event];
             */
            argumentMustNotBeNull(mv, SOURCE_EVENT, 2);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitFieldInsn(
                    Opcodes.PUTFIELD, 
                    this.className.replace('.', '/'), 
                    SOURCE_EVENT, 
                    ASM.getDescriptor(this.metadata.rawEventClass));
            
            mv.visitInsn(Opcodes.RETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateGetEventTypeMethod(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "getEventType", 
                    "()" + ASM.getDescriptor(EventType.class), 
                    null, 
                    null);
            mv.visitCode();
            
            /*
             * return this["{source_event}"].getEventType();
             */
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.className.replace('.', '/'), 
                    SOURCE_EVENT, 
                    ASM.getDescriptor(this.metadata.rawEventClass));
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    this.metadata.rawEventClass.getName().replace('.', '/'),
                    "getEventType", 
                    "()" + ASM.getDescriptor(EventType.class),
                    false);
            mv.visitInsn(Opcodes.ARETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        private void generateGetModificationTypeMethod(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "getModificationType", 
                    "()" + ASM.getDescriptor(ModificationType.class), 
                    null,
                    null);          
            mv.visitCode();
            
            /*
             * return this["{source_event}"].getModificationType();
             */
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.className.replace('.', '/'), 
                    SOURCE_EVENT, 
                    ASM.getDescriptor(this.metadata.rawEventClass));
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    this.metadata.rawEventClass.getName().replace('.', '/'),
                    "getModificationType", 
                    "()" + ASM.getDescriptor(ModificationType.class),
                    false);
            mv.visitInsn(Opcodes.ARETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateGetModificationMethod(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    GET_MODIFICATION, 
                    "()" + ASM.getDescriptor(this.metadata.modificationClass), 
                    null,
                    null);
            mv.visitCode();
            
            /*
             * return this.[source_event].getModification();
             */
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.className.replace('.', '/'), 
                    SOURCE_EVENT, 
                    ASM.getDescriptor(this.metadata.rawEventClass));
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    this.metadata.rawEventClass.getName().replace('.', '/'),
                    GET_MODIFICATION, 
                    "()" + ASM.getDescriptor(this.metadata.modificationClass),
                    false);
            mv.visitInsn(Opcodes.ARETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateGetCauseMethod(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "getCause", 
                    "()" + ASM.getDescriptor(Cause.class), 
                    null,
                    null);
            mv.visitCode();
            
            /*
             * return this["{source_event}"].getCourse();
             */
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.className.replace('.', '/'), 
                    SOURCE_EVENT, 
                    ASM.getDescriptor(this.metadata.rawEventClass));
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    this.metadata.rawEventClass.getName().replace('.', '/'), 
                    "getCause", 
                    "()" + ASM.getDescriptor(Cause.class),
                    false);
            mv.visitInsn(Opcodes.ARETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateGetPropertyMethod(ClassVisitor cv, Property property) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    property.getter.getName(), 
                    ASM.getDescriptor(property.getter, true), 
                    null,
                    null);
            mv.visitCode();
            
            /*
             * return this["{source_event}"].getXXX(???);
             */
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.className.replace('.', '/'), 
                    SOURCE_EVENT, 
                    ASM.getDescriptor(this.metadata.rawEventClass));
            if (!property.annotation.shared()) {
                mv.visitVarInsn(Opcodes.ALOAD, 1);
            }
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    this.metadata.rawEventClass.getName().replace('.', '/'), 
                    property.getter.getName(), 
                    ASM.getDescriptor(property.getter, true),
                    false);
            mv.visitInsn(ASM.getReturnCode(property.getter.getResolvedReturnType()));
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateDispatchMethod(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    DISPATCH, 
                    "(Ljava/lang/Object;)" +
                    ASM.getDescriptor(this.metadata.dipstachReturnClass), 
                    null,
                    null);
            mv.visitCode();
            
            mv.visitTypeInsn(
                    Opcodes.NEW, 
                    this.className.replace('.', '/'));
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL, 
                    this.className.replace('.', '/').replace('.', '/'), 
                    "<init>", 
                    "(Ljava/lang/Object;" +
                    ASM.getDescriptor(this.metadata.rawEventClass) +
                    ")V",
                    false);
            mv.visitInsn(Opcodes.ARETURN);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateGetAttributeContext(ClassVisitor cv) {
            MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_PUBLIC, 
                    "getAttributeContext", 
                    '(' +
                    ASM.getDescriptor(AttributeScope.class) +
                    ')' +
                    ASM.getDescriptor(EventAttributeContext.class), 
                    null,
                    null);
            mv.visitCode();
            
            final int localVariableIndex = 2;
            
            /*
             * $temporary = attributeScope.ordinal()
             */
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    Enum.class.getName().replace('.', '/'), 
                    "ordinal", 
                    "()I",
                    false);
            
            /*
             * switch (@temporary)
             */
            final Label caseLocalLbl = new Label();
            final Label caseInBubbleChainLbl = new Label();
            final Label caseInDispatchChainLbl = new Label();
            final Label caseInAllChainLbl = new Label();
            final Label defaultLbl = new Label();
            mv.visitLookupSwitchInsn(
                    defaultLbl, 
                    new int[]{
                            AttributeScope.LOCAL.ordinal(),
                            AttributeScope.IN_BUBBLE_CHAIN.ordinal(),
                            AttributeScope.IN_DISPATCH_CHAIN.ordinal(),
                            AttributeScope.IN_ALL_CHAIN.ordinal()
                    }, 
                    new Label[]{
                            caseLocalLbl,
                            caseInBubbleChainLbl,
                            caseInDispatchChainLbl,
                            caseInAllChainLbl,
                    });
            
            /*
             * case AttributeScope.LOCAL.ordinal():
             */
            mv.visitLabel(caseLocalLbl);
            this.generateLazyCreateAttributeContext(
                    mv, 
                    LOCAL_ATTRIBUTE_CONTEXT, 
                    localVariableIndex);
            
            /*
             * case AttributeScope.IN_BUBBLE_CHAIN.ordinal():
             */
            mv.visitLabel(caseInBubbleChainLbl);
            this.generateLazyCreateAttributeContext(
                    mv, 
                    ATTRIBUTE_CONTEXT_IN_BUBBLE_CHAIN, 
                    localVariableIndex);
            
            /*
             * case AttributeScope.IN_DISPATCH_CHAIN.ordinal():
             */
            mv.visitLabel(caseInDispatchChainLbl);
            this.generateReturnAttributeContextOfSourceEvent(mv);
            
            /*
             * case AttributeScope.IN_ALL_CHAIN.ordinal():
             */
            mv.visitLabel(caseInAllChainLbl);
            this.generateReturnAttributeContextOfSourceEvent(mv);
            
            /*
             * default:
             */
            mv.visitLabel(defaultLbl);
            this.generateReturnAttributeContextOfSourceEvent(mv);
            
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        
        private void generateReturnAttributeContextOfSourceEvent(MethodVisitor mv) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(
                    Opcodes.GETFIELD, 
                    this.className.replace('.', '/'), 
                    SOURCE_EVENT, 
                    ASM.getDescriptor(this.metadata.rawEventClass));
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, 
                    ModificationEvent.class.getName().replace('.', '/'), 
                    "getAttributeContext", 
                    '(' +
                    ASM.getDescriptor(AttributeScope.class) +
                    ')' + 
                    ASM.getDescriptor(EventAttributeContext.class),
                    false);
            mv.visitInsn(Opcodes.ARETURN);
        }
    }
    
    private static class EventClassValidator extends ClassVisitor {
        
        private Class<?> eventType;
        
        public EventClassValidator(Class<?> eventType) {
            super(Opcodes.ASM5);
            this.eventType = eventType;
        }
        
        @Override
        public void visit(
            int version,
            int access,
            String name,
            String signature,
            String superName,
            String[] interfaces) {
        }
    
        @Override
        public void visitSource(String source, String debug) {}
    
        @Override
        public void visitOuterClass(String owner, String name, String desc) {}
    
        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return null;
        }
    
        @Override
        public void visitAttribute(Attribute attr) {}
    
        @Override
        public void visitInnerClass(
            String name,
            String outerName,
            String innerName,
            int access) {
        }
        
        @Override
        public FieldVisitor visitField(
            int access,
            String name,
            String desc,
            String signature,
            Object value) {
            return null;
        }
        
        @Override
        public MethodVisitor visitMethod(
            int access,
            String name,
            String desc,
            String signature,
            String[] exceptions) {
            if ("<init>".equals(name)) {
                return new EventConstructorValidator(desc, this.eventType);
            }
            return null;
        }
        
        @Override
        public void visitEnd() {}
    
    }

    private static class EventConstructorValidator extends MethodVisitor {
        
        private static final String DESC_SOURCE = 
            '(' +
            ASM.getDescriptor(Object.class) +
            ")V";
        
        private static final String DESC_SOURCE_CAUSE = 
            '(' +
            ASM.getDescriptor(Object.class) +
            ASM.getDescriptor(Cause.class) +
            ")V";
        
        private final int expectArgumentCountForSuperConstructor;
        
        private int argumentCountForSuperConstructor;
        
        private boolean dispatch;
        
        private Class<?> eventType;
        
        public EventConstructorValidator(String desc, Class<?> eventType) {
            super(Opcodes.ASM5);
            Arguments.mustBeAnyOfValue(
                    "desc", 
                    desc, 
                    DESC_SOURCE, 
                    DESC_SOURCE_CAUSE,
                    '(' +
                    ASM.getDescriptor(Object.class) +
                    ASM.getDescriptor(eventType) +
                    ")V");
            if (DESC_SOURCE.equals(desc)) {
                this.expectArgumentCountForSuperConstructor = 2;
            } else {
                this.expectArgumentCountForSuperConstructor = 3;
                this.dispatch = !desc.equals(DESC_SOURCE_CAUSE);
            }
            this.eventType = eventType;
        }
        
        @Override
        public void visitVarInsn(int opcode, int var) {
            int argc = this.argumentCountForSuperConstructor;
            if (argc == 0 && var == 0) {
                this.argumentCountForSuperConstructor = 1;
            } else if (argc == 1 && var == 1) {
                this.argumentCountForSuperConstructor = 2;
            } else if (argc == 2 && var == 2) {
                this.argumentCountForSuperConstructor = 3;
            } else {
                this.argumentCountForSuperConstructor = 0;
            }
        }
        
        @Deprecated
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            this.visitMethodInsn(opcode, owner, name, desc, opcode == Opcodes.INVOKEINTERFACE);
        }
        
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if (Opcodes.INVOKESPECIAL == opcode && "<init>".equals(name)) {
                if (this.argumentCountForSuperConstructor !=
                    this.expectArgumentCountForSuperConstructor) {
                    Class<?>[] parameterTypes = 
                            this.expectArgumentCountForSuperConstructor == 3 ?
                            (
                                    this.dispatch ? 
                                    new Class[] { Object.class, this.eventType } : 
                                    new Class[] { Object.class, Cause.class }
                            )
                            :
                            new Class[] { Object.class };
                    throw new IllegalProgramException(
                            LAZY_RESOURCE.get().invalidSuperConstructorInvocation(
                                    this.eventType,
                                    parameterTypes
                            )
                    );
                }
            }
            this.argumentCountForSuperConstructor = 0;
        }
        
        @Override
        public void visitAttribute(Attribute attr) {
            this.argumentCountForSuperConstructor = 0;
        }
    
        @Override
        public void visitFrame(
            int type,
            int nLocal,
            Object[] local,
            int nStack,
            Object[] stack) {
            this.argumentCountForSuperConstructor = 0;
        }
    
        @Override
        public void visitInsn(int opcode) {
            this.argumentCountForSuperConstructor = 0;
        }
    
        @Override
        public void visitIntInsn(int opcode, int operand) {
            this.argumentCountForSuperConstructor = 0;
        }
    
        @Override
        public void visitTypeInsn(int opcode, String type) {
            this.argumentCountForSuperConstructor = 0;
        }
    
        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            this.argumentCountForSuperConstructor = 0;
        } 
    
        @Override
        public void visitJumpInsn(int opcode, Label label) {
            this.argumentCountForSuperConstructor = 0;
        }
    
        @Override
        public void visitLdcInsn(Object cst) {
            this.argumentCountForSuperConstructor = 0;
        }
    
        @Override
        public void visitIincInsn(int var, int increment) {
            this.argumentCountForSuperConstructor = 0;
        }
    
        @Override
        public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
            this.argumentCountForSuperConstructor = 0;
        }
    
        @Override
        public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
            this.argumentCountForSuperConstructor = 0;
        }
    
        @Override
        public void visitMultiANewArrayInsn(String desc, int dims) {
            this.argumentCountForSuperConstructor = 0;
        }
    
        @Override
        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
            this.argumentCountForSuperConstructor = 0;
        }
    
        @Override
        public void visitLocalVariable(
            String name,
            String desc,
            String signature,
            Label start,
            Label end,
            int index) {}
    
        @Override
        public void visitLineNumber(int line, Label start) {}
    
        @Override
        public void visitCode() {}

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {}

        @Override
        public void visitEnd() {}

        @Override
        public void visitLabel(Label label) {}

        @Override
        public AnnotationVisitor visitAnnotationDefault() {
            return null; 
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) { 
            return null; 
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(
            int parameter,
            String desc,
            boolean visible) { 
            return null; 
        }
    
    }

    private interface Resource {
        
        String dispatchMethodMustReturnSelfType(
                MethodInfo methodInfo,
                Class<?> eventType,
                Class<?> dispatchReturnType);

        String noBubbledEvent();
        
        String noDispatchedEvent();
        
        String tooManyEventFactoryAnnotations(
                Class<?> invalidEventType,
                Class<?> eventFactoryType1,
                Class<?> eventFactoryType2,
                Class<EventFactory> eventFactoryType);
        
        String invalidEventFactoryAnnotation(
                Class<?> invalidFactoryType, 
                Class<EventFactory> eventFactoryType);
        
        String missEventFactoryAnnotations(
                Class<?> invalidEventType,
                Class<EventFactory> eventFactoryType);
                
        String factoryTypeMustBeDeclaredInAnotherClass(String factoryTypeName);
        
        String invalidDeclaringClassOfFactory(
                Class<?> invalidEventType, 
                Class<?> factoryType, 
                Class<?> expectedEventType);
        
        String eventMustBeTopLevelOrStaticClass(Class<?> event);
        
        String invalidEventConstructorAcc(ConstructorInfo<?> constructor);
        
        String invalidEventConstructorSignature(
                ConstructorInfo<?> constructorInfo, 
                Class<Object> objectType, 
                Class<Cause> causeType,
                ClassInfo<?> modificationEventType);
        
        String invalidEventField(FieldInfo field);
        
        String invalidEventMethodModifiers(MethodInfo methodInfo);
        
        String invalidEventMethodTypeParameters(MethodInfo methodInfo);
        
        String invalidEventMethodReturnType(MethodInfo methodInfo);
        
        String invalidEventMethodParameters(
                MethodInfo methodInfo, 
                Class<PropertyVersion> propertyVersionType);
        
        String invalidEventMethodNameForBoolean(String methodName);
        
        String invalidEventMethodNameForNonBoolean(String methodName);  
        
        String invalidEventMethodThrows(
                MethodInfo methodInfo, 
                Class<?> exceptionType);
        
        String invalidEventMethodForShared(MethodInfo methodInfo);
        
        String invalidEventMethodForNonShared(
                MethodInfo methodInfo, 
                Class<PropertyVersion> propertyVersionType);
        
        String missEventMethod(
                ClassInfo<?> eventType, 
                String propertyName);
        
        String invalidFactoryTypeParameterCount(
                Class<?> factoryType, 
                int eventTypeParameterCount, 
                ClassInfo<?> eventType);
        
        String invalidFactoryTypeParameter(
                Class<?> factoryType, 
                int typeParameterIndex, 
                ClassInfo<?> eventType);
        
        String invalidFactoryMethodTypeParameters(MethodInfo factoryMethod);
        
        String invalidFactoryMethodName(MethodInfo factoryMethod);
        
        String invalidDispatchReturnType(String dispatchMethodDescriptor, String expectedReturnType);
        
        String invalidFactoryMethodSignature(MethodInfo factoryMethod, ExpectedSignature expectedSignature);
        
        String parameterMustBeSameType(String ... parameterNames);
        
        String atLeastOneParameterMustNotBeNull(String ... parameterNames);
        
        String invalidSuperConstructorInvocation(
                Class<?> eventType, 
                Class<?>[] constructorParameterTypes);
    }
    
    static {
        Map<Class<?>, Class<?>> typeBoxMap =
            new HashMap<Class<?>, Class<?>>(8, 1.0F);
        typeBoxMap.put(boolean.class, Boolean.class);
        typeBoxMap.put(char.class, Character.class);
        typeBoxMap.put(byte.class, Byte.class);
        typeBoxMap.put(short.class, Short.class);
        typeBoxMap.put(int.class, Integer.class);
        typeBoxMap.put(long.class, Long.class);
        typeBoxMap.put(float.class, Float.class);
        typeBoxMap.put(double.class, Double.class);
        TYPE_BOX_MAP = MACollections.unmodifiable(typeBoxMap);
    }
    
}
