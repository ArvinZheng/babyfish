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
package org.babyfish.test.lang;

import junit.framework.Assert;

import org.babyfish.lang.ChainInvocationExceptionHandleMode;
import org.babyfish.lang.Combiner;
import org.babyfish.lang.Combiners;
import org.babyfish.lang.DefaultChainInvocationExceptionHandleMode;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class ContinueCombinerTest {
    
    @Test
    public void testNormalAndException() {
        this.test(true);
    }
    
    @Test
    public void testExceptionAndNormal() {
        this.test(false);
    }
    
    private void test(boolean noramlBeforeException) {
        Combiner<ContinueHandler> combiner = Combiners.of(ContinueHandler.class);
        StringBuilder normalBuilder = new StringBuilder();
        StringBuilder errorBuilder = new StringBuilder();
        ContinueHandler handler;
        if (noramlBeforeException) {
            handler = combiner.combine(
                    new HandlerImpl(normalBuilder), 
                    new ErrorHandlerImpl());
        } else {
            handler = combiner.combine(
                    new ErrorHandlerImpl(),
                    new HandlerImpl(normalBuilder));
        }
        try {
            handler.handle();
        } catch (UnsupportedOperationException ex) {
            errorBuilder.append(ex.getMessage());
        }
        try {
            handler.handle(true);
        } catch (UnsupportedOperationException ex) {
            errorBuilder.append(ex.getMessage());
        }
        try {
            handler.handle('A');
        } catch (UnsupportedOperationException ex) {
            errorBuilder.append(ex.getMessage());
        }
        try {
            handler.handle((byte)1);
        } catch (UnsupportedOperationException ex) {
            errorBuilder.append(ex.getMessage());
        }
        try {
            handler.handle((short)2);
        } catch (UnsupportedOperationException ex) {
            errorBuilder.append(ex.getMessage());
        }
        try {
            handler.handle(3);
        } catch (UnsupportedOperationException ex) {
            errorBuilder.append(ex.getMessage());
        }
        try {
            handler.handle(4L);
        } catch (UnsupportedOperationException ex) {
            errorBuilder.append(ex.getMessage());
        }
        try {
            handler.handle(5F);
        } catch (UnsupportedOperationException ex) {
            errorBuilder.append(ex.getMessage());
        }
        try {
            handler.handle(6D);
        } catch (UnsupportedOperationException ex) {
            errorBuilder.append(ex.getMessage());
        }
        try {
            handler.handle("OBJ");
        } catch (UnsupportedOperationException ex) {
            errorBuilder.append(ex.getMessage());
        }
        Assert.assertEquals(
                "V()Z(true)C(A)B(1)S(2)I(3)J(4)F(5.0)D(6.0)A(OBJ)", 
                normalBuilder.toString());
        Assert.assertEquals(
                normalBuilder.toString(), 
                errorBuilder.toString());
    }

    @DefaultChainInvocationExceptionHandleMode(ChainInvocationExceptionHandleMode.CONTINUE)
    private interface ContinueHandler {
        
        void handle();
        
        boolean handle(boolean v);
        
        char handle(char v);
        
        byte handle(byte v);
        
        short handle(short v);
        
        int handle(int v);
        
        long handle(long v);
        
        float handle(float v);
        
        double handle(double v);
        
        Object handle(Object v);
    } 
    
    private static class HandlerImpl implements ContinueHandler {
        
        private StringBuilder builder;
        
        public HandlerImpl(StringBuilder builder) {
            this.builder = builder;
        }

        @Override
        public void handle() {
            this.builder.append("V()");
        }

        @Override
        public boolean handle(boolean v) {
            this.builder.append("Z(" + v + ')');
            return v;
        }

        @Override
        public char handle(char v) {
            this.builder.append("C(" + v + ')');
            return v;
        }

        @Override
        public byte handle(byte v) {
            this.builder.append("B(" + v + ')');
            return v;
        }

        @Override
        public short handle(short v) {
            this.builder.append("S(" + v + ')');
            return v;
        }

        @Override
        public int handle(int v) {
            this.builder.append("I(" + v + ')');
            return v;
        }

        @Override
        public long handle(long v) {
            this.builder.append("J(" + v + ')');
            return v;
        }

        @Override
        public float handle(float v) {
            this.builder.append("F(" + v + ')');
            return v;
        }

        @Override
        public double handle(double v) {
            this.builder.append("D(" + v + ')');
            return v;
        }

        @Override
        public Object handle(Object v) {
            this.builder.append("A(" + v + ')');
            return v;
        }
        
    }
    
    private static class ErrorHandlerImpl implements ContinueHandler {

        @Override
        public void handle() {
            throw new UnsupportedOperationException("V()");
        }

        @Override
        public boolean handle(boolean v) {
            throw new UnsupportedOperationException("Z(" + v + ')');
        }

        @Override
        public char handle(char v) {
            throw new UnsupportedOperationException("C(" + v + ')');
        }

        @Override
        public byte handle(byte v) {
            throw new UnsupportedOperationException("B(" + v + ')');
        }

        @Override
        public short handle(short v) {
            throw new UnsupportedOperationException("S(" + v + ')');
        }

        @Override
        public int handle(int v) {
            throw new UnsupportedOperationException("I(" + v + ')');
        }

        @Override
        public long handle(long v) {
            throw new UnsupportedOperationException("J(" + v + ')');
        }

        @Override
        public float handle(float v) {
            throw new UnsupportedOperationException("F(" + v + ')');
        }

        @Override
        public double handle(double v) {
            throw new UnsupportedOperationException("D(" + v + ')');
        }

        @Override
        public Object handle(Object v) {
            throw new UnsupportedOperationException("A(" + v + ')');
        }
        
    }
}
