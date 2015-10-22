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
package org.babyfish.persistence.tool.instrument.metadata;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.MACollections;
import org.babyfish.lang.Nulls;
import org.babyfish.lang.reflect.Strings;
import org.babyfish.lang.reflect.asm.ASM;
import org.babyfish.org.objectweb.asm.ClassReader;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.org.objectweb.asm.tree.AnnotationNode;
import org.babyfish.org.objectweb.asm.tree.ClassNode;
import org.babyfish.org.objectweb.asm.tree.FieldNode;
import org.babyfish.org.objectweb.asm.tree.MethodNode;
import org.babyfish.persistence.instrument.JPAObjectModelInstrument;

/**
 * @author Tao Chen
 */
class Context {
    
    private static final int CLASS_MAGIC = 0xCAFEBABE;
    
    private static final int ACC_NOT_CLASS = 
            Opcodes.ACC_INTERFACE | Opcodes.ACC_ENUM | Opcodes.ACC_ANNOTATION;
    
    private Class<? extends Annotation> anyAnnotationType;
    
    // The class nodes do not contain method-code INSNs to get 
    // the highest performance because I need nothing except 
    // the meta-data information of classes.
    private Map<File, ClassNode> classNodes = new LinkedHashMap<>();
    
    private Map<String, MetadataClassImpl> metadataClasses = new LinkedHashMap<>();
    
    public Context(Iterable<File> bytecodeFiles, Class<? extends Annotation> anyAnnotationType) {
        this.anyAnnotationType = anyAnnotationType;
        this.initClassNodes(bytecodeFiles);
        this.initModelClasses();
        this.resolveDeclaredProperties();
        this.resolveEntityInheritences();
        this.resolveEmbeddedProperties();
        this.resolveJoins();
        this.resolveExplicitOppositeProperties();
        this.resolveImplicitOppositeProperties();
        this.resolveReferenceProperties();
        this.resolveContravarianceProperties();
        this.finishResolving();
    }
    
    public Class<? extends Annotation> getAnyAnnotationType() {
        return this.anyAnnotationType;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Map<String, MetadataClass> getModelClasses() {
        return (Map)MACollections.unmodifiable(this.metadataClasses);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T getAnnotationValue(AnnotationNode annotationNode, String name) {
        if (annotationNode.values != null) {
            Iterator<Object> itr = annotationNode.values.iterator();
            while (itr.hasNext()) {
                String n = (String)itr.next();
                Object v = itr.next();
                if (n.equals(name)) {
                    return (T)v;
                }
            }
        }
        return null;
    }
    
    public static AnnotationNode getAnnotationNode(ClassNode classNode, Class<?> annotationType) {
        String desc = ASM.getDescriptor(annotationType);
        for (AnnotationNode annotationNode : (List<AnnotationNode>)classNode.visibleAnnotations) {
            if (annotationNode.desc.equals(desc)) {
                return annotationNode;
            }
        }
        return null;
    }
    
    public static AnnotationNode getAnnotationNode(Object propertyNode, Class<?> annotationType) {
        String desc = ASM.getDescriptor(annotationType);
        if (getVisibleAnnotations(propertyNode) != null) {
            for (AnnotationNode annotationNode : getVisibleAnnotations(propertyNode)) {
                if (annotationNode.desc.equals(desc)) {
                    return annotationNode;
                }
            }
        }
        return null;
    }
    
    public static List<AnnotationNode> getVisibleAnnotations(Object memberNode) {
        return memberNode instanceof FieldNode ?
                ((FieldNode)memberNode).visibleAnnotations :
                ((MethodNode)memberNode).visibleAnnotations;
    }
    
    public static List<AnnotationNode> getInvisibleAnnotations(Object memberNode) {
        return memberNode instanceof FieldNode ?
                ((FieldNode)memberNode).invisibleAnnotations :
                ((MethodNode)memberNode).invisibleAnnotations;
    }
    
    public static String propertyName(MethodNode methodNode) {
        if ((methodNode.access & (Opcodes.ACC_STATIC | Opcodes.ACC_BRIDGE)) != 0) {
            return null;
        }
        if (!methodNode.desc.startsWith("()")) {
            return null;
        }
        if (methodNode.desc.endsWith(")V")) {
            return null;
        }
        if (checkMethodPrefix(methodNode.name, "get")) {
            return Strings.toCamelCase(methodNode.name.substring(3));
        }
        if (checkMethodPrefix(methodNode.name, "is")) {
            if (methodNode.desc.endsWith(")Z") || methodNode.desc.endsWith(")Ljava/lang/Boolean;")) {
                return Strings.toCamelCase(methodNode.name.substring(2));
            }
        }
        return null;
    }
    
    private static boolean checkMethodPrefix(String name, String prefix) {
        return name.length() > prefix.length() &&
                name.startsWith(prefix) &&
                Character.isUpperCase(name.charAt(prefix.length()));
    }

    private void initClassNodes(Iterable<File> files) {
        try {
            String jpaObjectModelInstrumentDesc = ASM.getDescriptor(JPAObjectModelInstrument.class);
            String entityDesc = ASM.getDescriptor(Entity.class);
            String embeddableDesc = ASM.getDescriptor(Embeddable.class);
            String mappedSuperclassDesc = ASM.getDescriptor(MappedSuperclass.class);
            for (File file : files) {
                if (file.getName().endsWith(".class") && checkMagic(file, CLASS_MAGIC)) {
                    try (InputStream inputStream = new FileInputStream(file.getAbsolutePath())) {
                        ClassNode classNode = createClassNode(inputStream);
                        if ((classNode.access & (ACC_NOT_CLASS)) == 0) {
                            if (!Nulls.isNullOrEmpty(classNode.visibleAnnotations)) {
                                boolean instrument = false;
                                for (AnnotationNode annotationNode : (List<AnnotationNode>)classNode.visibleAnnotations) {
                                    if (annotationNode.desc.equals(jpaObjectModelInstrumentDesc)) {
                                        instrument = true;
                                        break;
                                    }
                                }
                                if (instrument) {
                                    for (AnnotationNode annotationNode : (List<AnnotationNode>)classNode.visibleAnnotations) {
                                        if (annotationNode.desc.equals(entityDesc) ||
                                                annotationNode.desc.equals(embeddableDesc) ||
                                                annotationNode.desc.equals(mappedSuperclassDesc)) {
                                            this.classNodes.put(file, classNode);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            throw new MetadataException(ex);
        }
    }
    
    private static ClassNode createClassNode(InputStream byteCodeInputStream) throws IOException {
        ClassReader cr = new ClassReader(byteCodeInputStream);
        ClassNode classNode = new ClassNode();
        // Very important to specify the ClassReader.SKIP_CODE
        // to get the highest performance because I need nothing 
        // except the meta-data information of classes.
        cr.accept(classNode, ClassReader.SKIP_CODE);
        return classNode;
    }

    private static  boolean checkMagic(File file, long magic) throws IOException {
        try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {
            return magic == in.readInt();
        }
    }
    
    private void initModelClasses() {
        Map<String, MetadataClassImpl> metadataClasses = this.metadataClasses;
        for (Entry<File, ClassNode> e : this.classNodes.entrySet()) {
            MetadataClassImpl metadataClassImpl = new MetadataClassImpl(e.getKey(), e.getValue());
            metadataClasses.put(metadataClassImpl.getName(), metadataClassImpl);
        }
    }
    
    private void resolveDeclaredProperties() {
        for (MetadataClassImpl metadataClassImpl : this.metadataClasses.values()) {
            metadataClassImpl.resolveDeclaredProperties(this);
        }
    }
    
    private void resolveEntityInheritences() {
        for (MetadataClassImpl metadataClassImpl : this.metadataClasses.values()) {
            metadataClassImpl.resolveInheritence(this);
        }
    }

    private void resolveEmbeddedProperties() {
        for (MetadataClassImpl metadataClassImpl : this.metadataClasses.values()) {
            metadataClassImpl.resolveEmbeddedProeprties(this);
        }
    }

    private void resolveJoins() {
        for (MetadataClassImpl metadataClassImpl : this.metadataClasses.values()) {
            metadataClassImpl.resolveJoins(this);
        }
    }
    
    private void resolveExplicitOppositeProperties() {
        for (MetadataClassImpl metadataClassImpl : this.metadataClasses.values()) {
            metadataClassImpl.resolveExplicitOppositeProperties(this);
        }
    }
    
    private void resolveImplicitOppositeProperties() {
        for (MetadataClassImpl metadataClassImpl : this.metadataClasses.values()) {
            metadataClassImpl.resolveImplicitOppositeProperties(this);
        }
    }
    
    private void resolveReferenceProperties() {
        for (MetadataClassImpl metadataClassImpl : this.metadataClasses.values()) {
            metadataClassImpl.resolveReferenceProperties(this);
        }
    }
    
    private void resolveContravarianceProperties() {
        for (MetadataClassImpl metadataClassImpl : this.metadataClasses.values()) {
            metadataClassImpl.resolveContravarianceProeprties(this);
        }
    }

    private void finishResolving() {
        for (MetadataClassImpl metadataClassImpl : this.metadataClasses.values()) {
            metadataClassImpl.finishResolving();
        }
    }
}
