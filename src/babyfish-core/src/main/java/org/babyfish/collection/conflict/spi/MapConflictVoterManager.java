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
package org.babyfish.collection.conflict.spi;

import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.TreeMap;
import org.babyfish.collection.TreeSet;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.XMap;
import org.babyfish.collection.conflict.MAMapConflictVoter;
import org.babyfish.collection.conflict.MapConflictVoter;
import org.babyfish.collection.conflict.MapConflictVoterArgs;
import org.babyfish.collection.conflict.MapReader;
import org.babyfish.lang.Action;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.BinaryAction;
import org.babyfish.lang.Func;
import org.babyfish.lang.Nulls;
import org.babyfish.lang.Ref;
import org.babyfish.lang.TernaryAction;
import org.babyfish.lang.reflect.asm.ASM;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.util.LazyResource;

/**
 * @author Tao Chen
 */
public class MapConflictVoterManager<K, V> extends AbstractMapConflictVoterManager<K, V> {
    
    private static final long serialVersionUID = 5134215833220952460L;
    
    private static final LazyResource<CommonResource> LAZY_COMMON_RESOURCE = LazyResource.of(CommonResource.class);
    
    private static final LazyResource<Resource> LAZY_RESOURCE = LazyResource.of(Resource.class);
    
    private static final Integer DETACHED_FAKE_EVENT = 1 << 0;
    
    private static final Integer ATTACHED_FAKE_EVENT = 1 << 1;
    
    private static final Integer REPLACED_FAKE_EVENT = DETACHED_FAKE_EVENT | ATTACHED_FAKE_EVENT;

    @SuppressWarnings("rawtypes")
    private static final Func<MAMapConflictVoter, MapSource> MAP_SOURCE_GETTER;
    
    @SuppressWarnings("rawtypes")
    private static final BinaryAction<MAMapConflictVoter, MapSource> MAP_SOURCE_SETTER;

    @SuppressWarnings("rawtypes")
    private static final TernaryAction<MAMapConflictVoter, Object, Object> DETACH_INVOKER;
    
    @SuppressWarnings("rawtypes")
    private static final TernaryAction<MAMapConflictVoter, Object, Object> ATTACH_INVOKER;
    
    private MapSource<K, V> mapSource;

    @SuppressWarnings("unchecked")
    MapConflictVoterManager(
            MapSource<K, V> mapSource,
            MapConflictVoter<K, V>[] voters, 
            int[] refCounts,
            int len, 
            int maCount) {
        super(voters, refCounts, len, maCount);
        this.mapSource = mapSource;
        if (maCount != 0) {
            for (int i = len - 1; i >= 0; i--) {
                if (voters[i] instanceof MAMapConflictVoter<?, ?>) {
                    MapSource<K, V> ds = MAP_SOURCE_GETTER.run((MAMapConflictVoter<K,V>)voters[i]);
                    if (ds != null && !ds.equals(mapSource)) {
                        throw new IllegalArgumentException(
                                LAZY_RESOURCE.get().maVoterIsNotBelongToCurrentSource(
                                        MAMapConflictVoter.class, 
                                        MapSource.class
                                )
                        );
                    }
                }
            }
            //Use two for statements, all successful or all failed.
            for (int i = len - 1; i >= 0; i--) {
                if (voters[i] instanceof MAMapConflictVoter<?, ?>) {
                    invokeSetMapSource((MAMapConflictVoter<K,V>)voters[i], mapSource);
                }
            }
        }
    }
    
    public static <K, V> MapConflictVoterManager<K, V> combine(
            MapConflictVoterManager<K, V> voterManager, 
            MapConflictVoter<K, V> voter, 
            MapSource<K, V> mapSource) {
        Arguments.mustNotBeNull("mapSource", mapSource);
        if (voterManager != null) {
            Arguments.mustBeEqualToOtherWhen(
                    LAZY_COMMON_RESOURCE.get().whenParameterIsNull("voterManager"),
                    "mapSource", 
                    mapSource,
                    "voterManager.mapSource", 
                    voterManager.mapSource());
        }
        return (MapConflictVoterManager<K, V>)combineImpl(voterManager, of(voter), mapSource);
    }
    
    public static <K, V> MapConflictVoterManager<K, V> remove(
            MapConflictVoterManager<K, V> voterManager, 
            MapConflictVoter<K, V> voter, 
            MapSource<K, V> mapSource) {
        Arguments.mustNotBeNull("mapSource", mapSource);
        if (voterManager != null) {
            Arguments.mustBeEqualToOtherWhen(
                    LAZY_COMMON_RESOURCE.get().whenParameterIsNull("voterManager"),
                    "mapSource", 
                    mapSource,
                    "voterManager.mapSource", 
                    voterManager.mapSource());
        }
        return (MapConflictVoterManager<K, V>)removeImpl(
                voterManager, 
                of(voter), 
                mapSource, 
                new Action<MAMapConflictVoter<K, V>>() {
                    @Override
                    public void run(MAMapConflictVoter<K, V> x) {
                        invokeSetMapSource(x, null);
                    }
                });
    }
    
    public Set<K> voteAll(Ref<Map<? extends K, ? extends V>> mapRef) {
        Arguments.mustNotBeNull("mapRef", mapRef);
        Map<? extends K, ? extends V> deltaMap = mapRef.get();
        Arguments.mustNotBeNull("mapRef.get()", deltaMap);
        VoteAllArgsImpl args = this.new VoteAllArgsImpl(deltaMap.size());
        try {
            for (Entry<? extends K, ? extends V> entry : deltaMap.entrySet()) {
                args.vote(entry.getKey(), entry.getValue());
            }
        } finally {
            args.finish();
        }
        mapRef.set(args.deltaMap());
        return args.conflictKeys();
    }
    
    public Set<K> vote(K key, V value) {
        VoteArgsImpl args = this.new VoteArgsImpl(key, value);
        this.invokeVote(args);
        return args.conflictKeys();
    }
    
    public void removed(K key, V value) {
        MapConflictVoter<K, V>[] voters = MapConflictVoterManager.this.voters;
        for (MapConflictVoter<K, V> voter : voters) {
            if (voter instanceof MAMapConflictVoter<?, ?>) {
                DETACH_INVOKER.run((MAMapConflictVoter<K, V>)voter, key, value);
            }
        }
    }
    
    public void added(K key, V value) {
        MapConflictVoter<K, V>[] voters = this.voters;
        for (MapConflictVoter<K, V> voter : voters) {
            if (voter instanceof MAMapConflictVoter<?, ?>) {
                ATTACH_INVOKER.run((MAMapConflictVoter<K, V>)voter, key, value);
            }
        }
    }
    
    public void changed(K oldKey, V oldValue, K newKey, V newValue) {
        MapConflictVoter<K, V>[] voters = this.voters;
        for (MapConflictVoter<K, V> voter : voters) {
            if (voter instanceof MAMapConflictVoter<?, ?>) {
                DETACH_INVOKER.run((MAMapConflictVoter<K, V>)voter, oldKey, oldValue);
                ATTACH_INVOKER.run((MAMapConflictVoter<K, V>)voter, newKey, newValue);
            }
        }
    }
    
    private void invokeVote(AbstractVoteArgs<K, V> args) {
        MapConflictVoter<K, V>[] voters = this.voters;
        for (MapConflictVoter<K, V> voter : voters) {
            if (voter instanceof MAMapConflictVoter<?, ?>) {
                voter.vote(args.forMAVoter());
            } else {
                voter.vote(args);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private static <K, V> void invokeSetMapSource(MAMapConflictVoter<K, V> voter, MapSource<K, V> mapSource) {
        MapSource<K, V> oldMapSource = MAP_SOURCE_GETTER.run(voter);
        if (!Nulls.equals(oldMapSource, mapSource)) {
            if (oldMapSource != null) {
                Map<K, V> map = oldMapSource.getMap();
                for (Entry<K, V> entry : map.entrySet()) {
                    DETACH_INVOKER.run(voter, entry.getKey(), entry.getValue());
                }
            }
            MAP_SOURCE_SETTER.run(voter, mapSource);
            if (mapSource == null) {
                Map<K, V> map = oldMapSource.getMap();
                for (Entry<K, V> entry : map.entrySet()) {
                    ATTACH_INVOKER.run(voter, entry.getKey(), entry.getValue());
                }
            }
        }
    }
    
    @Override
    public MapSource<K, V> mapSource() {
        return this.mapSource;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Class<Func<MAMapConflictVoter, MapSource>> generateMapSourceGetterType() {
        final String className = MAMapConflictVoter.class.getName() + ":MapSourceGetter{92B8C17E_BF4E_4135_B596_5A76E0FEBF4E}";
        Action<ClassVisitor> cvAction = new Action<ClassVisitor>() {
            @Override
            public void run(ClassVisitor cv) {
                
                String internalName = className.replace('.', '/');
                String runDesc = '(' + 
                        ASM.getDescriptor(MAMapConflictVoter.class) +
                        ')' +
                        ASM.getDescriptor(MapSource.class);
                
                MethodVisitor mv;
                
                cv.visit(
                        Opcodes.V1_7, 
                        Opcodes.ACC_PUBLIC, 
                        className.replace('.', '/'), 
                        null, 
                        "java/lang/Object", 
                        new String[] { ASM.getInternalName(Func.class ) });
                
                mv = cv.visitMethod(
                        Opcodes.ACC_PUBLIC, 
                        "<init>", 
                        "()V", 
                        null,
                        null);
                mv.visitCode();
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
                
                mv = cv.visitMethod(
                        Opcodes.ACC_PUBLIC | Opcodes.ACC_BRIDGE | Opcodes.ACC_SYNTHETIC, 
                        "run", 
                        "(Ljava/lang/Object;)Ljava/lang/Object;",
                        null,
                        null);
                mv.visitCode();
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(MAMapConflictVoter.class));
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        internalName, 
                        "run", 
                        runDesc,
                        false
                );
                mv.visitInsn(Opcodes.ARETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
                
                mv = cv.visitMethod(
                        Opcodes.ACC_PUBLIC, 
                        "run", 
                        runDesc,
                        null,
                        null);
                mv.visitCode();
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        ASM.getInternalName(MAMapConflictVoter.class), 
                        "mapSource", 
                        ASM.getDescriptor(MapSource.class)
                );
                mv.visitInsn(Opcodes.ARETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
                
                cv.visitEnd();
            }
        };
        return (Class)ASM.loadDynamicClass(
                MAMapConflictVoter.class.getClassLoader(), 
                className, 
                MAMapConflictVoter.class.getProtectionDomain(), 
                cvAction);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Class<BinaryAction<MAMapConflictVoter, MapSource>> generateMapSourceSetterType() {
        final String className = MAMapConflictVoter.class.getName() + ":MapSourceSetter{92B8C17E_BF4E_4135_B596_5A76E0FEBF4E}";
        Action<ClassVisitor> cvAction = new Action<ClassVisitor>() {
            @Override
            public void run(ClassVisitor cv) {
                
                String internalName = className.replace('.', '/');
                String runDesc = '(' + 
                        ASM.getDescriptor(MAMapConflictVoter.class) +
                        ASM.getDescriptor(MapSource.class) +
                        ")V";
                
                MethodVisitor mv;
                
                cv.visit(
                        Opcodes.V1_7, 
                        Opcodes.ACC_PUBLIC, 
                        className.replace('.', '/'), 
                        null, 
                        "java/lang/Object", 
                        new String[] { ASM.getInternalName(BinaryAction.class ) });
                
                mv = cv.visitMethod(
                        Opcodes.ACC_PUBLIC, 
                        "<init>", 
                        "()V", 
                        null,
                        null);
                mv.visitCode();
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
                
                mv = cv.visitMethod(
                        Opcodes.ACC_PUBLIC | Opcodes.ACC_BRIDGE | Opcodes.ACC_SYNTHETIC, 
                        "run", 
                        "(Ljava/lang/Object;Ljava/lang/Object;)V",
                        null,
                        null);
                mv.visitCode();
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(MAMapConflictVoter.class));
                mv.visitVarInsn(Opcodes.ALOAD, 2);
                mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(MapSource.class));
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        internalName, 
                        "run", 
                        runDesc,
                        false
                );
                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
                
                mv = cv.visitMethod(
                        Opcodes.ACC_PUBLIC, 
                        "run", 
                        runDesc,
                        null,
                        null);
                mv.visitCode();
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitVarInsn(Opcodes.ALOAD, 2);
                mv.visitFieldInsn(
                        Opcodes.PUTFIELD, 
                        ASM.getInternalName(MAMapConflictVoter.class), 
                        "mapSource", 
                        ASM.getDescriptor(MapSource.class)
                );
                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
                
                cv.visitEnd();
            }
        };
        return (Class)ASM.loadDynamicClass(
                MAMapConflictVoter.class.getClassLoader(), 
                className, 
                MAMapConflictVoter.class.getProtectionDomain(), 
                cvAction);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Class<TernaryAction<MAMapConflictVoter, Object, Object>> generateDetachOrAttachInvokerType(final boolean attach) {
        final String className = 
                MAMapConflictVoter.class.getName() + 
                (attach ? ":Attach" : ":Detach") + 
                "{92B8C17E_BF4E_4135_B596_5A76E0FEBF4E}";
        Action<ClassVisitor> cvAction = new Action<ClassVisitor>() {
            @Override
            public void run(ClassVisitor cv) {
                
                String internalName = className.replace('.', '/');
                String runDesc = '(' + 
                        ASM.getDescriptor(MAMapConflictVoter.class) +
                        "Ljava/lang/Object;Ljava/lang/Object;)V";
                
                MethodVisitor mv;
                
                cv.visit(
                        Opcodes.V1_7, 
                        Opcodes.ACC_PUBLIC, 
                        className.replace('.', '/'), 
                        null, 
                        "java/lang/Object", 
                        new String[] { ASM.getInternalName(TernaryAction.class ) });
                
                mv = cv.visitMethod(
                        Opcodes.ACC_PUBLIC, 
                        "<init>", 
                        "()V", 
                        null,
                        null);
                mv.visitCode();
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
                
                mv = cv.visitMethod(
                        Opcodes.ACC_PUBLIC | Opcodes.ACC_BRIDGE | Opcodes.ACC_SYNTHETIC, 
                        "run", 
                        "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V",
                        null,
                        null);
                mv.visitCode();
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(MAMapConflictVoter.class));
                mv.visitVarInsn(Opcodes.ALOAD, 2);
                mv.visitVarInsn(Opcodes.ALOAD, 3);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        internalName, 
                        "run", 
                        runDesc,
                        false
                );
                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
                
                mv = cv.visitMethod(
                        Opcodes.ACC_PUBLIC, 
                        "run", 
                        '(' +
                        ASM.getDescriptor(MAMapConflictVoter.class) +
                        "Ljava/lang/Object;Ljava/lang/Object;)V",
                        null,
                        null);
                mv.visitCode();
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitVarInsn(Opcodes.ALOAD, 2);
                mv.visitVarInsn(Opcodes.ALOAD, 3);
                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL, 
                        ASM.getInternalName(MAMapConflictVoter.class), 
                        attach ? "attach" : "detach", 
                        "(Ljava/lang/Object;Ljava/lang/Object;)V",
                        false
                );
                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
        };
        return (Class)ASM.loadDynamicClass(
                MAMapConflictVoter.class.getClassLoader(), 
                className, 
                MAMapConflictVoter.class.getProtectionDomain(), 
                cvAction);
    }
    
    private static abstract class AbstractVoteArgs<K, V> implements MapConflictVoterArgs<K, V> {
        
        private MapConflictVoterArgs<K, V> forMAVoter;
        
        MapConflictVoterArgs<K, V> forMAVoter() {
            MapConflictVoterArgs<K, V> forMAVoter = this.forMAVoter;
            if (forMAVoter == null) {
                this.forMAVoter = forMAVoter = new MapConflictVoterArgs<K, V>() {

                    @Override
                    public UnifiedComparator<? super V> valueUnifiedComparator() {
                        return AbstractVoteArgs.this.valueUnifiedComparator();
                    }

                    @Override
                    public MapReader<K, V> reader() {
                        throw new UnsupportedOperationException(
                                LAZY_RESOURCE.get().maVoterArgsDoesNotSupportReader(
                                        MAMapConflictVoter.class,
                                        MapConflictVoterArgs.class
                                )
                        );
                    }

                    @Override
                    public K newKey() {
                        return AbstractVoteArgs.this.newKey();
                    }

                    @Override
                    public V newValue() {
                        return AbstractVoteArgs.this.newValue();
                    }

                    @Override
                    public void conflictFound(K conflictKey) {
                        AbstractVoteArgs.this.conflictFound(conflictKey);
                    }

                    @Override
                    public void conflictFound(Collection<K> conflictKeys) {
                        AbstractVoteArgs.this.conflictFound(conflictKeys);
                    }
                };
            }
            return forMAVoter;
        }
    }

    private class VoteAllArgsImpl extends AbstractVoteArgs<K, V> {
        
        private XMap<K, V> map;
        
        private XMap<K, V> deltaMap;
        
        private int modCount;
        
        private Set<K> conflictKeys;
        
        private Set<K> tempConflictKeys;
        
        private K newKey;
        
        private V newValue;
        
        private Object[] eventBuffer;
        
        private int eventBufferIndex;
        
        VoteAllArgsImpl(int deltaMapSize) {
            XMap<K, V> map = MapConflictVoterManager.this.mapSource.getMap();
            this.map = map;
            UnifiedComparator<? super K> keyUnifiedComparator = map.keyUnifiedComparator();
            Comparator<? super K> keyComparator = keyUnifiedComparator.comparator();
            ReplacementRule keyReplacementRule = map.keyReplacementRule();
            if (keyComparator != null) {
                this.deltaMap = new TreeMap<>(
                        keyReplacementRule,
                        keyComparator,
                        map.valueUnifiedComparator());
                this.conflictKeys = new TreeSet<>(keyReplacementRule.reversedRule(), keyComparator);
                this.tempConflictKeys = new TreeSet<>(keyReplacementRule.reversedRule(), keyComparator);
            } else {
                this.deltaMap = new LinkedHashMap<>(
                        keyReplacementRule,
                        keyUnifiedComparator.equalityComparator(),
                        map.valueUnifiedComparator(),
                        (deltaMapSize * 4 + 2) / 3);
                this.conflictKeys = new LinkedHashSet<>(keyReplacementRule.reversedRule(), keyUnifiedComparator.equalityComparator());
                this.tempConflictKeys = new LinkedHashSet<>(keyReplacementRule.reversedRule(), keyUnifiedComparator.equalityComparator());
            }
            this.deltaMap.putAll(deltaMap);
            if (MapConflictVoterManager.this.maCount != 0) {
                this.eventBuffer = new Object[deltaMapSize * 5];
            }
        }
        
        @Override
        public UnifiedComparator<? super V> valueUnifiedComparator() {
            return this.map.valueUnifiedComparator();
        }

        @Override
        public MapReader<K, V> reader() {
            return this.new Reader();
        }

        @Override
        public K newKey() {
            return this.newKey;
        }

        @Override
        public V newValue() {
            return this.newValue;
        }

        @Override
        public void conflictFound(K conflictKey) {
            if (!this.map.keyUnifiedComparator().equals(conflictKey, this.newKey)) {
                this.tempConflictKeys.add(conflictKey);
            }
        }
        
        @Override
        public void conflictFound(Collection<K> conflictKeys) {
            Set<K> set = this.tempConflictKeys;
            for (K conflictKey : conflictKeys) {
                if (!this.map.keyUnifiedComparator().equals(conflictKey, this.newKey)) {
                    set.add(conflictKey);
                }
            }
        }
        
        Map<? extends K, ? extends V> deltaMap() {
            return this.deltaMap;
        }
        
        void vote(K key, V value) {
            this.newKey = key;
            this.newValue = value;
            XMap<K, V> map = this.map;
            XMap<K, V> deltaMap = this.deltaMap;
            
            MapConflictVoterManager.this.invokeVote(this);
            deltaMap.keySet().removeAll(this.tempConflictKeys);
            Set<K> conflictKeys = this.conflictKeys;
            
            Object[] eventBuffer = this.eventBuffer;
            int eventBufferIndex = this.eventBufferIndex;
            if (eventBuffer != null) {
                try {
                    Entry<K, V> conflictEntry;
                    for (K conflictKey : this.tempConflictKeys) {
                        if (!conflictKeys.contains(conflictKey)) {
                            conflictEntry = deltaMap.real(conflictKey);
                            if (conflictEntry == null) {
                                conflictEntry = map.real(conflictKey);
                            }
                            if (conflictEntry != null) {
                                eventBuffer[eventBufferIndex] = DETACHED_FAKE_EVENT;
                                eventBuffer[eventBufferIndex + 1] = conflictEntry.getKey();
                                eventBuffer[eventBufferIndex + 2] = conflictEntry.getValue();
                                MapConflictVoterManager.this.removed(conflictEntry.getKey(), conflictEntry.getValue());
                            }
                        }
                    }
                    K replacedKey = null;
                    V replacedValue = null;
                    Entry<K, V> replacedEntry = deltaMap.real(key);
                    if (replacedEntry != null) {
                        replacedKey = replacedEntry.getKey();
                        replacedValue = replacedEntry.getValue();
                    }
                    if (replacedEntry == null || map.keyReplacementRule() == ReplacementRule.OLD_REFERENCE_WIN) {
                        replacedEntry = map.real(key);
                        if (replacedEntry != null) {
                            replacedKey = replacedEntry.getKey();
                        }
                    }
                    if (replacedEntry != null && !conflictKeys.contains(key) && !this.tempConflictKeys.contains(key)) {
                        eventBuffer[eventBufferIndex] = REPLACED_FAKE_EVENT;
                        eventBuffer[eventBufferIndex + 1] = replacedKey;
                        eventBuffer[eventBufferIndex + 2] = replacedValue;
                        eventBuffer[eventBufferIndex + 3] = key;
                        eventBuffer[eventBufferIndex + 4] = value;
                        MapConflictVoterManager.this.changed(replacedKey, replacedValue, key, value);
                    } else {
                        eventBuffer[eventBufferIndex] = ATTACHED_FAKE_EVENT;
                        eventBuffer[eventBufferIndex + 3] = key;
                        eventBuffer[eventBufferIndex + 4] = value;
                        MapConflictVoterManager.this.added(key, value);
                    }
                } finally {
                    this.eventBufferIndex = eventBufferIndex + 5;
                }
            }
            deltaMap.put(key, value);
            this.conflictKeys.addAll(this.tempConflictKeys);
            this.tempConflictKeys.clear();
            this.modCount++;
        }
        
        @SuppressWarnings("unchecked")
        void finish() {
            Object[] eventBuffer = this.eventBuffer;
            int eventBufferIndex = this.eventBufferIndex;
            this.eventBufferIndex = 0;
            this.conflictKeys.retainAll(this.map.keySet());
            Throwable throwable = null;
            while (eventBufferIndex > 0) {
                eventBufferIndex -= 5;
                try {
                    //Fake event rollback
                    int type = (Integer)eventBuffer[eventBufferIndex];
                    if ((type & ATTACHED_FAKE_EVENT) != 0) {
                        MapConflictVoterManager.this.removed(
                                (K)eventBuffer[eventBufferIndex + 3], 
                                (V)eventBuffer[eventBufferIndex + 4]
                        );
                    }
                    if ((type & DETACHED_FAKE_EVENT) != 0) {
                        MapConflictVoterManager.this.added(
                                (K)eventBuffer[eventBufferIndex + 1], 
                                (V)eventBuffer[eventBufferIndex + 2]
                        );
                    } 
                } catch (RuntimeException | Error ex) {
                    if (throwable == null) {
                        throwable = ex;
                    }
                }
            }
            if (throwable instanceof Error) {
                throw (Error)throwable;
            }
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException)throwable;
            }
        }
        
        Set<K> conflictKeys() {
            return this.conflictKeys;
        }
        
        private class Reader implements MapReader<K, V> {
            
            private int expectedModCount;
            
            private Iterator<Entry<K, V>> mapItr;
            
            private Iterator<Entry<K, V>> deltaMapItr;
            
            private Entry<K, V> entry;
            
            private boolean newReferenceWin;
            
            Reader() {
                VoteAllArgsImpl owner = VoteAllArgsImpl.this;
                this.expectedModCount = owner.modCount;
                this.mapItr = owner.map.entrySet().iterator();
                this.deltaMapItr = owner.deltaMap.entrySet().iterator();
                this.newReferenceWin = owner.map.keyReplacementRule() == ReplacementRule.NEW_REFERENCE_WIN;
            }
            
            public boolean read() {
                Iterator<Entry<K, V>> itr = this.mapItr;
                VoteAllArgsImpl owner = VoteAllArgsImpl.this;
                Map<K, V> map = owner.map;
                Map<K, V> deltaMap = owner.deltaMap;
                Set<K> conflictKeys = owner.conflictKeys;
                Set<K> tempConflictKeys = owner.tempConflictKeys;
                Entry<K, V> entry;
                while (itr.hasNext()) {
                    entry = itr.next();
                    K key = entry.getKey();
                    if (conflictKeys.contains(key)) {
                        continue;
                    }
                    if (tempConflictKeys.contains(key)) {
                        continue;
                    }
                    if (this.newReferenceWin && deltaMap.containsKey(key)) {
                        continue;
                    }
                    this.entry = entry;
                    return true;
                }
                itr = this.deltaMapItr;
                while (itr.hasNext()) {
                    entry = itr.next();
                    K key = entry.getKey();
                    if (conflictKeys.contains(key)) {
                        continue;
                    }
                    if (tempConflictKeys.contains(key)) {
                        continue;
                    }
                    if (!this.newReferenceWin && map.containsKey(key)) {
                        continue;
                    }
                    this.entry = entry;
                    return true;
                }
                return false;
            }
            
            @Override
            public K key() {
                this.checkModCount();
                return this.entry != null ? this.entry.getKey() : null;
            }
            
            @Override
            public V value() {
                this.checkModCount();
                return this.entry != null ? this.entry.getValue() : null;
            }

            private void checkModCount() {
                if (this.expectedModCount != VoteAllArgsImpl.this.modCount) {
                    throw new ConcurrentModificationException();
                }
            }
        }
    }
    
    private class VoteArgsImpl extends AbstractVoteArgs<K, V> {

        private XMap<K, V> map;
        
        private K newKey;
        
        private V newValue;
        
        private Set<K> conflictKeys;
        
        VoteArgsImpl(K key, V value) {
            this.map = MapConflictVoterManager.this.mapSource.getMap();
            this.newKey = key;
            this.newValue = value;
            UnifiedComparator<? super K> keyUnifiedComparator = this.map.keyUnifiedComparator();
            Comparator<? super K> keyComparator = keyUnifiedComparator.comparator();
            if (keyComparator != null) {
                this.conflictKeys = new TreeSet<>(keyComparator);
            } else {
                this.conflictKeys = new LinkedHashSet<>(keyUnifiedComparator.equalityComparator());
            }
        }
        
        @Override
        public UnifiedComparator<? super V> valueUnifiedComparator() {
            return this.map.valueUnifiedComparator();
        }
        
        @Override
        public MapReader<K, V> reader() {
            return this.new Reader();
        }

        @Override
        public K newKey() {
            return this.newKey;
        }

        @Override
        public V newValue() {
            return this.newValue;
        }

        @Override
        public void conflictFound(K conflictKey) {
            if (!this.map.keyUnifiedComparator().equals(conflictKey, this.newKey)) {
                this.conflictKeys.add(conflictKey);
            }
        }
        
        @Override
        public void conflictFound(Collection<K> conflictKeys) {
            Set<K> set = this.conflictKeys;
            for (K conflictKey : conflictKeys) {
                if (!this.map.keyUnifiedComparator().equals(conflictKey, this.newKey)) {
                    set.add(conflictKey);
                }
            }
        }
        
        Set<K> conflictKeys() {
            return this.conflictKeys;
        }
        
        private class Reader implements MapReader<K, V> {
            
            private Iterator<Entry<K, V>> itr;
            
            private Entry<K, V> entry;
            
            Reader() {
                this.itr = VoteArgsImpl.this.map.entrySet().iterator();
            }
            
            @Override
            public boolean read() {
                if (!this.itr.hasNext()) {
                    return false;
                }
                this.entry = this.itr.next();
                return true;
            }
            
            @Override
            public K key() {
                return this.entry != null ? this.entry.getKey() : null;
            }
            
            @Override
            public V value() {
                return this.entry != null ? this.entry.getValue() : null;
            }
        }
    }
    
    private interface Resource {
        
        @SuppressWarnings("rawtypes")
        String maVoterIsNotBelongToCurrentSource(
                Class<MAMapConflictVoter> maVoterType, 
                Class<MapSource> sourceType);
        
        @SuppressWarnings("rawtypes")
        String maVoterArgsDoesNotSupportReader(
                Class<MAMapConflictVoter> maVoterType,
                Class<MapConflictVoterArgs> voterArgsType);
    }
    
    static {
        try {
            MAP_SOURCE_GETTER = generateMapSourceGetterType().newInstance();
            MAP_SOURCE_SETTER = generateMapSourceSetterType().newInstance();
            DETACH_INVOKER = generateDetachOrAttachInvokerType(false).newInstance();
            ATTACH_INVOKER = generateDetachOrAttachInvokerType(true).newInstance();
            
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new AssertionError(ex);
        }
    }
}
