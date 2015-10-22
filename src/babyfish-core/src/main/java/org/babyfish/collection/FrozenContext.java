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
package org.babyfish.collection;

import java.lang.ref.WeakReference;

import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.event.ElementEvent.Modification;
import org.babyfish.collection.event.MapElementEvent;
import org.babyfish.collection.event.MapElementEvent.MapModification;
import org.babyfish.collection.event.modification.MapModifications;
import org.babyfish.collection.event.modification.SetModifications;
import org.babyfish.collection.spi.AbstractMAMap;
import org.babyfish.collection.spi.AbstractMASet;
import org.babyfish.collection.spi.base.BaseEntries;
import org.babyfish.collection.spi.base.BaseEntriesHandler;
import org.babyfish.collection.spi.base.FrozenContextSuspending;
import org.babyfish.lang.Action;
import org.babyfish.lang.BinaryAction;
import org.babyfish.lang.reflect.asm.ASM;
import org.babyfish.modificationaware.ModificationAware;
import org.babyfish.modificationaware.event.AttributeScope;
import org.babyfish.modificationaware.event.spi.GlobalAttributeContext;
import org.babyfish.modificationaware.event.spi.InAllChainAttributeContext;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.util.LazyResource;

/**
 * @author Tao Chen
 */
public abstract class FrozenContext<T> {
    
    private static final LazyResource<Resource> LAZY_RESOURCE = LazyResource.of(Resource.class);
    
    private static final String NAME_POSTFIX = "92B8C17E_BF4E_4135_B596_5A76E0FEBF4E";
    
    @SuppressWarnings("rawtypes")
    private static final BinaryAction ABSTRACT_MA_SET_EXECUTE_MODIFYING;
    
    @SuppressWarnings("rawtypes")
    private static final BinaryAction ABSTRACT_MA_SET_EXECUTE_MODIFIED;
    
    @SuppressWarnings("rawtypes")
    private static final BinaryAction ABSTRACT_MA_MAP_EXECUTE_MODIFYING;
    
    @SuppressWarnings("rawtypes")
    private static final BinaryAction ABSTRACT_MA_MAP_EXECUTE_MODIFIED;
    
    private FrozenContext() {
        
    }
    
    public static <T> void suspendFreezing(FrozenContext<T> ctx, T obj) {
        if (ctx != null) {
            ctx.suspendFreezing(obj);
        }
    }
    
    public static <T> void resumeFreezing(FrozenContext<T> ctx) {
        if (ctx != null) {
            ctx.resumeFreezing();
        }
    }
    
    abstract void suspendFreezing(T obj);
    
    abstract void resumeFreezing();
    
    abstract boolean isAlive();
    
    public static <K> FrozenContext<K> create(BaseEntries<K, ?> baseEntries) {
        if (!baseEntries.isRoot()) {
            throw new IllegalArgumentException(LAZY_RESOURCE.get().baseEntriesMustBeRoot());
        }
        return new SingleImpl<>(baseEntries);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> FrozenContext<T> combine(FrozenContext<T> ctx1, FrozenContext<T> ctx2) {
        if (ctx1 == null || !ctx1.isAlive()) {
            return ctx2 != null && ctx2.isAlive() ? ctx2 : null;
        }
        if (ctx2 == null || !ctx2.isAlive()) {
            return ctx1 != null && ctx1.isAlive() ? ctx1 : null;
        }
        int len1 = len(ctx1);
        int len2 = len(ctx2);
        SingleImpl<T>[] arr = new SingleImpl[len1 + len2];
        int len = copyTo(ctx1, arr, 0);
        SingleImpl<T>[] addArr = toArray(ctx2);
        for (int i = 0; i < len2; i++) {
            SingleImpl<T> addCtx = addArr[i];
            if (addCtx != null) {
                boolean duplicated = false;
                for (int ii = len - 1; ii >= 0; ii--) {
                    if (arr[ii].same(addCtx)) {
                        duplicated = true;
                        break;
                    }
                }
                if (!duplicated) {
                    arr[len++] = addCtx;
                }
            }
        }
        if (len << 1 <= len1 + len2) {
            Trim<T> trim = new Trim<T>(arr, len);
            trim.trim(len);
            arr = trim.arr;
            len = trim.len;
        }
        if (len == 0) {
            return null;
        }
        if (len == 1) {
            return arr[0];
        }
        return new CombinedImpl<T>(arr, len);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> FrozenContext<T> remove(FrozenContext<T> ctx1, FrozenContext<T> ctx2) {
        if (ctx1 == null || !ctx1.isAlive()) {
            return null;
        }
        if (ctx2 == null || !ctx2.isAlive()) {
            return ctx1 != null && ctx1.isAlive() ? ctx1 : null;
        }
        int len1 = len(ctx1);
        int len2 = len(ctx2);
        SingleImpl<T>[] arr = new SingleImpl[len1];
        int oldLen = copyTo(ctx1, arr, 0);
        int len = oldLen;
        SingleImpl<T>[] removeArr = toArray(ctx2);
        for (int i = oldLen - 1; i >= 0; i--) {
            for (int ii = len2 - 1; ii >= 0; ii--) {
                SingleImpl<T> removeCtx = removeArr[ii];
                if (removeCtx != null) {
                    if (arr[i].same(removeCtx)) {
                        arr[i] = null;
                        len--;
                    }
                }
            }
        }
        if (len << 1 <= oldLen) {
            Trim<T> trim = new Trim<T>(arr, len);
            trim.trim(len);
            arr = trim.arr;
            len = trim.len;
        }
        if (len == 0) {
            return null;
        }
        if (len == 1) {
            return arr[0];
        }
        return new CombinedImpl<T>(arr, len);
    }
    
    private static int len(FrozenContext<?> ctx) {
        return ctx instanceof CombinedImpl<?> ? ((CombinedImpl<?>)ctx).len : 1;
    }
    
    private static <T> int copyTo(FrozenContext<T> ctx, SingleImpl<T>[] arr, int offset) {
        int index = offset;
        if (ctx instanceof CombinedImpl<?>) {
            CombinedImpl<T> combinedImpl = (CombinedImpl<T>)ctx;
            SingleImpl<T>[] src = combinedImpl.arr;
            int srcLen = combinedImpl.len;
            for (int i = 0; i < srcLen; i++) {
                SingleImpl<T> srcItem = src[i];
                if (srcItem != null && srcItem.isAlive()) {
                    arr[index++] = srcItem;
                }
            }
        } else if (ctx.isAlive()) {
            arr[index++] = (SingleImpl<T>)ctx;
        }
        return index - offset;
    }
    
    @SuppressWarnings("unchecked")
    private static <T> SingleImpl<T>[] toArray(FrozenContext<T> ctx) {
        return ctx instanceof CombinedImpl<?> ? 
                ((CombinedImpl<T>)ctx).arr : 
                new SingleImpl[] { (SingleImpl<T>)ctx };
    }
    
    @SuppressWarnings("rawtypes")
    private static BinaryAction createEventInvocationAction(boolean map, boolean post) {
        final Class<?> collectionType = map ? AbstractMAMap.class : AbstractMASet.class;
        final Class<?> eventType = map ? MapElementEvent.class : ElementEvent.class;
        final String methodName = post ? "executeModified" : "executeModifying";
        final String className = collectionType.getName() + '{' + methodName + ':' + NAME_POSTFIX + '}';
        final String internalName = className.replace('.', '/');
        Action<ClassVisitor> cvAction = new Action<ClassVisitor>() {
            @Override
            public void run(ClassVisitor cv) {
                cv.visit(
                        Opcodes.V1_7, 
                        Opcodes.ACC_PUBLIC, 
                        internalName, 
                        null, 
                        "java/lang/Object", 
                        new String[] { ASM.getInternalName(BinaryAction.class) }
                );
                
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
                
                mv = cv.visitMethod(
                        Opcodes.ACC_PUBLIC, 
                        "run", 
                        "(Ljava/lang/Object;Ljava/lang/Object;)V", 
                        null,
                        null);
                mv.visitCode();
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(collectionType));
                mv.visitVarInsn(Opcodes.ALOAD, 2);
                mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(eventType));
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        internalName, 
                        "run", 
                        '(' +
                        ASM.getDescriptor(collectionType) +
                        ASM.getDescriptor(eventType) +
                        ")V",
                        false);
                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
                
                mv = cv.visitMethod(
                        Opcodes.ACC_PUBLIC, 
                        "run", 
                        '(' +
                        ASM.getDescriptor(collectionType) +
                        ASM.getDescriptor(eventType) +
                        ")V", 
                        null,
                        null);
                mv.visitCode();
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitVarInsn(Opcodes.ALOAD, 2);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        ASM.getInternalName(collectionType), 
                        methodName, 
                        '(' +
                        ASM.getDescriptor(eventType) +
                        ")V",
                        false);
                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
                
                cv.visitEnd();
            }
        };
        Class<?> actionType = ASM.loadDynamicClass(
                collectionType.getClassLoader(), 
                className, 
                collectionType.getProtectionDomain(), 
                cvAction);
        try {
            return (BinaryAction)actionType.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new AssertionError();
        }
    }
    
    private static class SingleImpl<K> extends FrozenContext<K> {
        
        private WeakReference<BaseEntries<K, Object>> baseEntriesReference;
        
        private FrozenContextSuspending<K, Object> suspending;
        
        private int suspendCount;
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        private SingleImpl(BaseEntries<K, ?> baseEntries) {
            this.baseEntriesReference = new WeakReference<BaseEntries<K, Object>>((BaseEntries)baseEntries); 
        }
        
        @SuppressWarnings("unchecked")
        @Override
        void suspendFreezing(K obj) {
            if (this.suspendCount++ == 0) {
                BaseEntries<K, Object> baseEntries = this.baseEntriesReference.get();
                if (baseEntries != null) {
                    BaseEntriesHandler<K, Object> handler = null;
                    ModificationAware modificationAware = baseEntries.getMAOwner();
                    if (modificationAware instanceof AbstractMASet<?>) {
                        handler = new HandlerImpl4Set<>(
                                (AbstractMASet<K>)modificationAware, 
                                SetModifications.suspendViaFrozenContext(obj)
                        );
                    } else if (modificationAware instanceof AbstractMAMap<?, ?>) {
                        handler = new HandlerImpl4Map<>(
                                (AbstractMAMap<K, Object>)modificationAware, 
                                MapModifications.suspendViaFrozenContext(obj)
                        );
                    }
                    this.suspending = baseEntries.suspendByKeyViaFrozenContext(obj, handler);
                }
            }
        }
        
        @SuppressWarnings("unchecked")
        @Override
        void resumeFreezing() {
            int suspendCount = this.suspendCount - 1;
            if (suspendCount < 0) {
                throw new IllegalStateException(LAZY_RESOURCE.get().canNotResume());
            }
            this.suspendCount = suspendCount;
            if (suspendCount == 0) {
                BaseEntries<K, Object> baseEntries = this.baseEntriesReference.get();
                if (baseEntries != null) {
                    BaseEntriesHandler<K, Object> handler = null;
                    ModificationAware modificationAware = baseEntries.getMAOwner();
                    if (modificationAware instanceof AbstractMASet<?>) {
                        handler = new HandlerImpl4Set<>(
                                (AbstractMASet<K>)modificationAware, 
                                SetModifications.<K>resumeViaFrozenContext()
                        );
                    } else if (modificationAware instanceof AbstractMAMap<?, ?>) {
                        handler = new HandlerImpl4Map<>(
                                (AbstractMAMap<K, Object>)modificationAware, 
                                MapModifications.<K, Object>resumeViaFrozenContext()
                        );
                    }
                    FrozenContextSuspending<K, Object> suspending = this.suspending;
                    this.suspending = null;
                    baseEntries.resumeViaFronzeContext(suspending, handler);
                }
            }
        }
        
        @Override
        boolean isAlive() {
            return this.baseEntriesReference.get() != null;
        }
        
        boolean same(SingleImpl<K> other) {
            BaseEntries<K, ?> baseEntries = this.baseEntriesReference.get();
            if (baseEntries != null) {
                return baseEntries == other.baseEntriesReference.get();
            }
            return false;
        }
    }
    
    private static class CombinedImpl<T> extends FrozenContext<T> {
        
        private SingleImpl<T>[] arr;
        
        private int len;
        
        CombinedImpl(SingleImpl<T>[] arr, int len) {
            this.arr = arr;
            this.len = len;
        }

        @Override
        void suspendFreezing(T obj) {
            SingleImpl<T>[] arr = this.arr;
            int len = this.len;
            int missCount = 0;
            for (int i = 0; i < len; i++) {
                SingleImpl<T> ctx = arr[i];
                if (ctx == null) {
                    missCount++;
                } else if (!ctx.isAlive()) {
                    arr[i] = null;
                    missCount++;
                } else {
                    ctx.suspendFreezing(obj);
                }
            }
            if (missCount << 1 >= len) {
                Trim<T> trim = new Trim<T>(arr, len);
                trim.trim(len - missCount);
                this.arr = trim.arr;
                this.len = trim.len;
            }
        }

        @Override
        void resumeFreezing() {
            SingleImpl<T>[] arr = this.arr;
            int len = this.len;
            int missCount = 0;
            for (int i = 0; i < len; i++) {
                SingleImpl<T> ctx = arr[i];
                if (ctx == null) {
                    missCount++;
                } else if (!ctx.isAlive()) {
                    arr[i] = null;
                    missCount++;
                } else {
                    ctx.resumeFreezing();
                }
            }
            if (missCount << 1 >= len) {
                Trim<T> trim = new Trim<T>(arr, len);
                trim.trim(len - missCount);
                this.arr = trim.arr;
                this.len = trim.len;
            }
        }

        @Override
        boolean isAlive() {
            return this.len != 0;
        }
    }
    
    private static class Trim<T> {
        
        private static final SingleImpl<?>[] EMPTY_SINGLE_IMPLS = new SingleImpl[0];
        
        SingleImpl<T>[] arr;
        
        int len;
        
        Trim(SingleImpl<T>[] arr, int len) {
            this.arr = arr;
            this.len = len;
        }
        
        @SuppressWarnings("unchecked")
        void trim(int trimLen) {
            if (trimLen == 0) {
                this.arr = (SingleImpl<T>[])EMPTY_SINGLE_IMPLS;
                this.len = 0;
            } else {
                SingleImpl<T>[] arr = this.arr;
                int len = this.len;
                SingleImpl<T>[] newArr = new SingleImpl[trimLen];
                int index = 0;
                for (int i = 0; i < len; i++) {
                    SingleImpl<T> ctx = arr[i];
                    if (ctx != null && ctx.isAlive()) {
                        newArr[index++] = ctx;
                    }
                }
                this.arr = newArr;
                this.len = index;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static class HandlerImpl4Set<K> implements BaseEntriesHandler<K, Object> {
        
        private AbstractMASet<K> source;
        
        private Modification<K> modification;

        public HandlerImpl4Set(AbstractMASet<K> source, Modification<K> modification) {
            this.source = source;
            this.modification = modification;
        }

        @Override
        public Object createAddingArgument(K key, Object value) {
            return ElementEvent.createAttachEvent(this.source, this.modification, key);
        }

        @Override
        public void adding(K key, Object value, Object argument) {
            ElementEvent<K> event = (ElementEvent<K>)argument;
            ABSTRACT_MA_SET_EXECUTE_MODIFYING.run(this.source, event);
        }

        @Override
        public void added(K key, Object value, Object argument) {
            ElementEvent<K> event = (ElementEvent<K>)argument;
            ABSTRACT_MA_SET_EXECUTE_MODIFIED.run(this.source, event);
        }

        @Override
        public Object createChangingArgument(K oldKey, Object oldValue, K newKey, Object newValue) {
            return ElementEvent.createReplaceEvent(this.source, modification, oldKey, newKey);
        }

        @Override
        public void changing(K oldKey, Object oldValue, K newKey, Object newValue, Object argument) {
            ElementEvent<K> event = (ElementEvent<K>)argument;
            ABSTRACT_MA_SET_EXECUTE_MODIFYING.run(this.source, event);
        }

        @Override
        public void changed(K oldKey, Object oldValue, K newKey, Object newValue, Object argument) {
            ElementEvent<K> event = (ElementEvent<K>)argument;
            ABSTRACT_MA_SET_EXECUTE_MODIFIED.run(this.source, event);
        }

        @Override
        public Object createRemovingArgument(K oldKey, Object oldValue) {
            return ElementEvent.createDetachEvent(this.source, this.modification, oldKey);
        }

        @Override
        public void removing(K oldKey, Object oldValue, Object argument) {
            ElementEvent<K> event = (ElementEvent<K>)argument;
            ABSTRACT_MA_SET_EXECUTE_MODIFYING.run(this.source, event);
        }

        @Override
        public void removed(K oldKey, Object oldValue, Object argument) {
            ElementEvent<K> event = (ElementEvent<K>)argument;
            ABSTRACT_MA_SET_EXECUTE_MODIFIED.run(this.source, event);
        }

        @Override
        public void setPreThrowable(Object argument, Throwable throwable) {
            ElementEvent<K> event = (ElementEvent<K>)argument;
            ((InAllChainAttributeContext)event.getAttributeContext(AttributeScope.IN_ALL_CHAIN))
            .setPreThrowable(throwable);
        }

        @Override
        public void setNullOrThrowable(Throwable throwable) {
            if (throwable != null) {
                ((GlobalAttributeContext)this.modification.getAttributeContext()).setThrowable(throwable);
            } else {
                ((GlobalAttributeContext)this.modification.getAttributeContext()).success();    
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private static class HandlerImpl4Map<K> implements BaseEntriesHandler<K, Object> {
        
        private AbstractMAMap<K, Object> source;
        
        private final MapModification<K, Object> modification;

        public HandlerImpl4Map(AbstractMAMap<K, Object> source, MapModification<K, Object> modification) {
            this.source = source;
            this.modification = modification;
        }

        @Override
        public Object createAddingArgument(K key, Object value) {
            return MapElementEvent.createAttachEvent(
                    this.source, 
                    this.modification,
                    key,
                    value);
        }

        @Override
        public void adding(K key, Object value, Object argument) {
            MapElementEvent<K, Object> event = (MapElementEvent<K, Object>)argument; 
            ABSTRACT_MA_MAP_EXECUTE_MODIFYING.run(this.source, event);
        }

        @Override
        public void added(K key, Object value, Object argument) {
            MapElementEvent<K, Object> event = (MapElementEvent<K, Object>)argument;
            ABSTRACT_MA_MAP_EXECUTE_MODIFIED.run(this.source, event);
        }
        
        @Override
        public Object createChangingArgument(K oldKey, Object oldValue, K newKey,
                Object newValue) {
            return MapElementEvent.createReplaceEvent(
                    this.source, 
                    this.modification,
                    oldKey,
                    newKey,
                    oldValue, 
                    newValue);
        }

        @Override
        public void changing(K oldKey, Object oldValue, K newKey, Object newValue, Object argument) {
            MapElementEvent<K, Object> event = (MapElementEvent<K, Object>)argument;
            ABSTRACT_MA_MAP_EXECUTE_MODIFYING.run(this.source, event);
        }

        @Override
        public void changed(K oldKey, Object oldValue, K newKey, Object newValue, Object argument) {
            MapElementEvent<K, Object> event = (MapElementEvent<K, Object>)argument;
            ABSTRACT_MA_MAP_EXECUTE_MODIFIED.run(this.source, event);
        }
        
        @Override
        public Object createRemovingArgument(K oldKey, Object oldValue) {
            return MapElementEvent.createDetachEvent(
                    this.source, 
                    this.modification, 
                    oldKey,
                    oldValue);
        }

        @Override
        public void removing(K oldKey, Object oldValue, Object argument) {
            MapElementEvent<K, Object> event = (MapElementEvent<K, Object>)argument;
            ABSTRACT_MA_MAP_EXECUTE_MODIFYING.run(this.source, event);
        }

        @Override
        public void removed(K oldKey, Object oldValue, Object argument) {
            MapElementEvent<K, Object> event = (MapElementEvent<K, Object>)argument;
            ABSTRACT_MA_MAP_EXECUTE_MODIFIED.run(this.source, event);
        }
        
        @Override
        public void setPreThrowable(Object argument, Throwable throwable) {
            MapElementEvent<K, Object> event = (MapElementEvent<K, Object>)argument;
            ((InAllChainAttributeContext)event.getAttributeContext(AttributeScope.IN_ALL_CHAIN))
            .setPreThrowable(throwable);
        }

        @Override
        public void setNullOrThrowable(Throwable throwable) {
            if (throwable != null) {
                ((GlobalAttributeContext)this.modification.getAttributeContext()).setThrowable(throwable);
            } else {
                ((GlobalAttributeContext)this.modification.getAttributeContext()).success();    
            }
        }
    }
    
    private interface Resource {
        
        String baseEntriesMustBeRoot();
        
        String canNotResume();
    }
    
    static  {
        ABSTRACT_MA_SET_EXECUTE_MODIFYING = createEventInvocationAction(false, false);
        ABSTRACT_MA_SET_EXECUTE_MODIFIED = createEventInvocationAction(false, true);
        ABSTRACT_MA_MAP_EXECUTE_MODIFYING = createEventInvocationAction(true, false);
        ABSTRACT_MA_MAP_EXECUTE_MODIFIED = createEventInvocationAction(true, true);
    }
}
