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
package org.babyfish.hibernate.tool;

import java.io.File;
import java.util.Set;

import org.babyfish.hibernate.model.loader.HibernateObjectModelScalarLoader;
import org.babyfish.lang.reflect.asm.ASM;
import org.babyfish.lang.reflect.asm.XMethodVisitor;
import org.babyfish.model.spi.ObjectModelImplementor;
import org.babyfish.model.spi.ObjectModelScalarLoader;
import org.babyfish.org.objectweb.asm.ClassVisitor;
import org.babyfish.org.objectweb.asm.MethodVisitor;
import org.babyfish.org.objectweb.asm.Opcodes;
import org.babyfish.persistence.tool.instrument.AbstractInstrumentTask;
import org.babyfish.persistence.tool.instrument.AbstractInstrumenter;
import org.hibernate.annotations.Any;
import org.hibernate.bytecode.internal.javassist.FieldHandled;
import org.hibernate.bytecode.internal.javassist.FieldHandler;

/**
 * @author Tao Chen
 */
public class InstrumentTask extends AbstractInstrumentTask {

    @Override
    protected AbstractInstrumenter createInstrumenter(Set<File> bytecodeFiles) {
        return new HibernateInstrument(bytecodeFiles);
    }
    
    private static class HibernateInstrument extends AbstractInstrumenter {

        public HibernateInstrument(Iterable<File> bytecodeFiles) {
            super(bytecodeFiles, Any.class);
        }

        @Override
        protected OwnerAdapter createOwnerAdapter(ClassVisitor cv) {
            return this.new OwnerAdapter(cv);
        }

        protected class OwnerAdapter extends AbstractInstrumenter.OwnerAdapter {
            
            private boolean requiredImplementFieldHandled;
            
            public OwnerAdapter(ClassVisitor cv) {
                super(cv);
            }

            @Override
            protected void onInit() {
                this.requiredImplementFieldHandled = 
                        this.getMetadataClass().isEntity() && 
                        this.getMetadataClass().hasLazyScalarProperties();
            }

            @Override
            protected String[] getAdditionalInterfaces() {
                if (this.requiredImplementFieldHandled) {
                    return new String[] { ASM.getInternalName(FieldHandled.class) };
                }
                return null;
            }

            @Override
            protected void addAdditionalMembers() {
                if (this.requiredImplementFieldHandled) {
                    this.visitGetFieldHandler();
                    this.visitSetFieldHandler();
                }
            }
            
            private void visitGetFieldHandler() {
                MethodVisitor mv = this.cv.visitMethod(
                        Opcodes.ACC_PUBLIC, 
                        "getFieldHandler", 
                        "()" + ASM.getDescriptor(FieldHandler.class), 
                        null,
                        null);
                mv.visitCode();
                
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        this.getMetadataClass().getName().replace('.', '/'), 
                        OWNER_OM, 
                        this.getObjectModelDescriptor());
                mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(ObjectModelImplementor.class));
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        ASM.getInternalName(ObjectModelImplementor.class), 
                        "getScalarLoader", 
                        "()" + ASM.getDescriptor(ObjectModelScalarLoader.class),
                        true);
                mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(FieldHandler.class));
                mv.visitInsn(Opcodes.ARETURN);
                
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
            
            private void visitSetFieldHandler() {
                XMethodVisitor mv = ASM.visitMethod(
                        this.cv,
                        Opcodes.ACC_PUBLIC, 
                        "setFieldHandler", 
                        '(' + ASM.getDescriptor(FieldHandler.class) + ")V", 
                        null,
                        null);
                mv.visitCode();
                
                final int objectModelImplementorIndex = mv.aSlot("objectModelImplementor");
                
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(
                        Opcodes.GETFIELD, 
                        this.getMetadataClass().getName().replace('.', '/'), 
                        OWNER_OM, 
                        this.getObjectModelDescriptor());
                mv.visitTypeInsn(Opcodes.CHECKCAST, ASM.getInternalName(ObjectModelImplementor.class));
                mv.visitVarInsn(Opcodes.ASTORE, objectModelImplementorIndex);
                
                mv.visitVarInsn(Opcodes.ALOAD, objectModelImplementorIndex);
                mv.visitTypeInsn(Opcodes.NEW, ASM.getInternalName(HibernateObjectModelScalarLoader.class));
                mv.visitInsn(Opcodes.DUP);
                mv.visitVarInsn(Opcodes.ALOAD, objectModelImplementorIndex);
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitMethodInsn(
                        Opcodes.INVOKESPECIAL, 
                        ASM.getInternalName(HibernateObjectModelScalarLoader.class), 
                        "<init>", 
                        "(Ljava/lang/Object;" +
                        ASM.getDescriptor(FieldHandler.class) +
                        ")V",
                        false);
                mv.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE, 
                        ASM.getInternalName(ObjectModelImplementor.class), 
                        "setScalarLoader", 
                        '(' + ASM.getDescriptor(ObjectModelScalarLoader.class) + ")V",
                        true);
                mv.visitInsn(Opcodes.RETURN);
                
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
        }
    }
}
