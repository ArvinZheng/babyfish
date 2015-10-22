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

import java.util.Map.Entry;
import java.util.Set;

import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.immutable.ImmutableObjects;
import org.babyfish.lang.Action;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.Func;
import org.babyfish.lang.reflect.ClassInfo;
import org.babyfish.lang.reflect.MethodInfo;
import org.babyfish.lang.reflect.asm.ASM;
import org.babyfish.lang.reflect.asm.XMethodVisitor;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.Label;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.util.Joins;

/**
 * @author Tao Chen
 */
public class Modifications extends ImmutableObjects {
    
    private static final Modifications INSTANCE = new Modifications(); 
    
    private static final String GET_ATTRIBUTE_CONTEXT = "getAttributeContext";
    
    private static final String GET_ARGUMENTS = "getArguments";

    protected Modifications() {
        
    }
    
    protected static <F> F getModificationFactory(Class<F> modificationFactoryType) {
        return INSTANCE.getFactory(modificationFactoryType);
    }
    
    @Override
    protected void validateInterfaceType(Class<?> interfaceType) {
        Arguments.mustBeCompatibleWithValue("interfaceType", interfaceType, Modification.class);
    }

    @Override
    protected void generateAutonomyMethod(
            ClassVisitor cv, 
            Context context, 
            MethodInfo methodInfo) {
        String methodName = methodInfo.getName();
        if (GET_ATTRIBUTE_CONTEXT.equals(methodName)) {
            generateModificationGetAttributeContext(cv, context);
        } else if (GET_ARGUMENTS.equals(methodName)) {
            generateModificationGetArguments(cv, context);
        } else {
            super.generateAutonomyMethod(cv, context, methodInfo);
        }
    }

    private static void generateModificationGetAttributeContext(
            ClassVisitor cv, Context context) {
        String attributeContextInternalName = EventAttributeContext.class.getName().replace('.', '/');
        String attributeContextDesc = ASM.getDescriptor(EventAttributeContext.class);
        String attributeScopeDesc = ASM.getDescriptor(AttributeScope.class);
        cv
        .visitField(
                Opcodes.ACC_PRIVATE, 
                GET_ATTRIBUTE_CONTEXT, 
                attributeContextDesc, 
                null, 
                null)
        .visitEnd();
        MethodVisitor mv = cv.visitMethod(
                Opcodes.ACC_PUBLIC, 
                GET_ATTRIBUTE_CONTEXT, 
                "()" + attributeContextDesc, 
                null,
                null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(
                Opcodes.GETFIELD, 
                context.getClassInternalName(), 
                GET_ATTRIBUTE_CONTEXT, 
                attributeContextDesc);
        mv.visitInsn(Opcodes.DUP);
        Label notNullLabel = new Label();
        mv.visitJumpInsn(Opcodes.IFNONNULL, notNullLabel);
        mv.visitInsn(Opcodes.POP);
        mv.visitFieldInsn(
                Opcodes.GETSTATIC, 
                AttributeScope.class.getName().replace('.', '/'), 
                AttributeScope.GLOBAL.name(), 
                attributeScopeDesc);
        mv.visitMethodInsn(
                Opcodes.INVOKESTATIC, 
                attributeContextInternalName, 
                "of", 
                '(' + attributeScopeDesc + ')' + attributeContextDesc,
                false);
        mv.visitInsn(Opcodes.DUP);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitInsn(Opcodes.SWAP);
        mv.visitFieldInsn(
                Opcodes.PUTFIELD, 
                context.getClassInternalName(), 
                GET_ATTRIBUTE_CONTEXT, 
                attributeContextDesc);
        mv.visitLabel(notNullLabel);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void generateModificationGetArguments(
            ClassVisitor cv, 
            final Context context) {
        XMethodVisitor mv = ASM.visitMethod(
                cv,
                Opcodes.ACC_PUBLIC, 
                GET_ARGUMENTS, 
                "(Ljava/lang/Class;)[Ljava/lang/Object;", 
                null, 
                null);
        mv.visitCode();
        
        Label notNullLabel = new Label();
        Label endIfLabel = new Label();
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitJumpInsn(Opcodes.IFNONNULL, notNullLabel);
        mv.visitLdcInsn(org.babyfish.org.objectweb.asm.Type.getType(context.getInterfaceRawClass()));
        mv.visitVarInsn(Opcodes.ASTORE, 1);
        mv.visitJumpInsn(Opcodes.GOTO, endIfLabel);
        mv.visitLabel(notNullLabel);
        
        mv.visitLdcInsn("modificationType");
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitLdcInsn(org.babyfish.org.objectweb.asm.Type.getType(Modification.class));
        mv.visitMethodInsn(
                Opcodes.INVOKESTATIC, 
                ASM.getInternalName(Arguments.class), 
                "mustBeCompatibleWithValue", 
                "(Ljava/lang/String;Ljava/lang/Class;Ljava/lang/Class;)Ljava/lang/Class;",
                false);
        mv.visitInsn(Opcodes.POP);
        mv.visitLabel(endIfLabel);
        
        //Use LinkedHashSet to make sure that the most popular interface type is the first one.
        Set<ClassInfo<?>> interfaceClassInfos = new LinkedHashSet<ClassInfo<?>>();
        addModificationInterfaceClassInfos(context.getInterfaceClassInfo(), interfaceClassInfos);
        for (ClassInfo<?> interfaceClassInfo : interfaceClassInfos) {
            XOrderedMap<String, MethodInfo> properties = new Context(interfaceClassInfo.getRawType()).getProperties();
            Label notMatchLabel = new Label();
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitLdcInsn(org.babyfish.org.objectweb.asm.Type.getType(interfaceClassInfo.getRawClass()));
            mv.visitJumpInsn(Opcodes.IF_ACMPNE, notMatchLabel);
            mv.visitLdcInsn(properties.size());
            mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
            int index = 0;
            for (final Entry<String, MethodInfo> entry : properties.entrySet()) {
                final Class<?> propertyType = entry.getValue().getReturnType();
                mv.visitInsn(Opcodes.DUP);
                mv.visitLdcInsn(index++);
                mv.visitBox(propertyType, new Action<XMethodVisitor>() {
                    @Override
                    public void run(XMethodVisitor mv) {
                        mv.visitVarInsn(Opcodes.ALOAD, 0);
                        mv.visitFieldInsn(
                                Opcodes.GETFIELD, 
                                context.getClassInternalName(), 
                                entry.getKey(), 
                                ASM.getDescriptor(propertyType));
                    }
                });
                mv.visitInsn(Opcodes.AASTORE);
            }
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitLabel(notMatchLabel);
        }
        String message = 
                "The parameter \"modificationType\" must be any one of \"" +
                Joins.join(interfaceClassInfos, 
                        new Func<ClassInfo<?>, String>() {
                            @Override
                            public String run(ClassInfo<?> x) {
                                return x.getName();
                            }
                        }
                ) +
                "\"";
        mv.visitTypeInsn(Opcodes.NEW, ASM.getInternalName(IllegalArgumentException.class));
        mv.visitInsn(Opcodes.DUP);
        mv.visitLdcInsn(message);
        mv.visitMethodInsn(
                Opcodes.INVOKESPECIAL, 
                ASM.getInternalName(IllegalArgumentException.class), 
                "<init>", 
                "(Ljava/lang/String;)V",
                false);
        mv.visitInsn(Opcodes.ATHROW);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void addModificationInterfaceClassInfos(
            ClassInfo<?> interfaceClassInfo, 
            Set<ClassInfo<?>> interfaceClassInfos) {
        if (Modification.class.isAssignableFrom(interfaceClassInfo.getRawClass())) {
            interfaceClassInfos.add(interfaceClassInfo);
            for (ClassInfo<?> superInterfaceType : interfaceClassInfo.getInterfaces()) {
                addModificationInterfaceClassInfos(superInterfaceType, interfaceClassInfos);
            }
        }
    }
    
}
