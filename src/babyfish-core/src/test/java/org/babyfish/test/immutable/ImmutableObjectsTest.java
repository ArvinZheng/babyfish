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
package org.babyfish.test.immutable;

import junit.framework.Assert;

import org.babyfish.immutable.ImmutableObjects;
import org.babyfish.immutable.Parameters;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class ImmutableObjectsTest {

    @Parameters("string")
    private interface OString {
        
        String getString();
        
    }
    
    @Parameters("boolean, char, byte, short, int, long, float, double, string")
    private interface OAll {
        
        boolean isBoolean();
        
        char getChar();
        
        byte getByte();
        
        short getShort();
        
        int getInt();
        
        long getLong();
        
        float getFloat();
        
        double getDouble();
        
        String getString();
        
    }
    
    @Parameters("a, b")
    private interface A {
        
        String getA();
        
        String getB();
        
    }
    
    @Parameters("c, d")
    private interface B {
        
        String getC();
        
        String getD();
        
    }
    
    @Parameters("a, c, b, d")
    private interface AB extends A, B {
        
    }
    
    @Parameters("element")
    private interface A1 {
        
        Object getElement();
        
    }
    
    @Parameters("element")
    private interface B1 extends A1 {
        
        @Override
        Element getElement();
        
    }
    
    @Parameters("element")
    private interface A2<E> {
        
        E getElement();
        
    }
    
    @Parameters("element")
    private interface B2<E extends Element> extends A2<E> {
        
        @Override
        E getElement();
        
    }
    
    @Parameters
    private interface Singleton {
        
    }
    
    @Parameters
    @org.babyfish.immutable.Prototype
    private interface Prototype {
        
    }
    
    private static class Element {
        
        private String value;
        
        public Element(String value) {
            this.value = value == null ? "" : value;
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Element)) {
                return false;
            }
            Element other = (Element)obj;
            return this.value.equals(other.value);
        }

        @Override
        public String toString() {
            return this.value;
        }

    }
    
    private static class MyObjects extends ImmutableObjects {
        
        private static final MyObjects INSTANCE = new MyObjects();
        
        private static final Factory F = INSTANCE.getFactory(Factory.class);
        
        private interface Factory {
            
            OString createOString(String name);
            
            OAll createOAll(
                    boolean z, char c, byte b, short s, int i, long j, float f, double d, String l);
            
            AB createAB(String a, String c, String b, String d);
            
            B1 createB1(Element element);
            
            <E extends Element> B2<E> createB2(E element);
            
            Singleton createSingleton();
            
            Prototype createPrototype();
            
        }
        
        static OString createOString(String name) {
            return F.createOString(name);
        }
        
        static OAll createOAll(
                boolean z, char c, byte b, short s, int i, long j, float f, double d, String l) {
            return F.createOAll(z, c, b, s, i, j, f, d, l);
        }
        
        static AB createAB(
                String a, String c, String b, String d) {
            return F.createAB(a, c, b, d);
        }
        
        static B1 createB1(Element element) {
            return F.createB1(element);
        }
        
        static <E extends Element> B2<E> createB2(E element) {
            return F.createB2(element);
        }
        
        static Singleton createSingleton() {
            return F.createSingleton();
        }
        
        static Prototype createPrototype() {
            return F.createPrototype();
        }
        
    }
    
    @Test
    public void testOString() {
        OString modificatin1 = MyObjects.createOString("abc");
        Assert.assertEquals("abc", modificatin1.getString());
        Assert.assertEquals("abc".hashCode(), modificatin1.hashCode());
        Assert.assertEquals(OString.class.getName() + "{ string : abc }", modificatin1.toString());
    }
    
    @Test
    public void testOAll() {
        OAll modificatin2 = MyObjects.createOAll(
                true, 'X', (byte)1, (short)2, 3, 4L, 5.F, 6.D, "Y");
        Assert.assertEquals(true, modificatin2.isBoolean());
        Assert.assertEquals('X', modificatin2.getChar());
        Assert.assertEquals((byte)1, modificatin2.getByte());
        Assert.assertEquals((short)2, modificatin2.getShort());
        Assert.assertEquals(3, modificatin2.getInt());
        Assert.assertEquals(4L, modificatin2.getLong());
        Assert.assertEquals(5.F, modificatin2.getFloat());
        Assert.assertEquals(6.D, modificatin2.getDouble());
        Assert.assertEquals("Y", modificatin2.getString());
        Object[] arr = new Object[] { true, 'X', (byte)1, (short)2, 3, 4L, 5.F, 6.D, "Y" };
        int hash = 0;
        for (Object value : arr) {
            hash = hash * 31 + value.hashCode();
        }
        Assert.assertEquals(hash, modificatin2.hashCode());
        Assert.assertEquals(
                OAll.class.getName() + 
                "{ " +
                "boolean : true, " +
                "char : X, " +
                "byte : 1, " +
                "short : 2, " +
                "int : 3, " +
                "long : 4, " +
                "float : 5.0, " +
                "double : 6.0, " +
                "string : Y" +
                " }", 
                modificatin2.toString());
    }
    
    @Test
    public void testAB() {
        AB ab = MyObjects.createAB("I", "II", "III", "IV");
        Assert.assertEquals("I", ab.getA());
        Assert.assertEquals("III", ab.getB());
        Assert.assertEquals("II", ab.getC());
        Assert.assertEquals("IV", ab.getD());
        Assert.assertEquals(
                (("I".hashCode() * 31 + "II".hashCode()) * 31 + "III".hashCode()) * 31 + "IV".hashCode(), 
                ab.hashCode());
        Assert.assertEquals(AB.class.getName() + "{ a : I, c : II, b : III, d : IV }", ab.toString());
    }
    
    @Test
    public void testB1() {
        B1 b1 = MyObjects.createB1(new Element("b1"));
        Assert.assertEquals(new Element("b1"), b1.getElement());
        Assert.assertEquals(new Element("b1"), ((A1)b1).getElement());
        Assert.assertEquals("b1".hashCode(), b1.hashCode());
    }
    
    @Test
    public void testB2() {
        B2<Element> b2 = MyObjects.createB2(new Element("b2"));
        Assert.assertEquals(new Element("b2"), b2.getElement());
        Assert.assertEquals(new Element("b2"), ((A2<Element>)b2).getElement());
        Assert.assertEquals("b2".hashCode(), b2.hashCode());
    }
    
    @Test
    public void testSingletonAndPrototype() {
        Assert.assertNotNull(MyObjects.createOString("M1"));
        Assert.assertNotSame(MyObjects.createOString("M1"), MyObjects.createOString("M1"));
        Assert.assertNotNull(MyObjects.createSingleton());
        Assert.assertNotNull(MyObjects.createPrototype());
        Assert.assertSame(MyObjects.createSingleton(), MyObjects.createSingleton());
        Assert.assertNotSame(MyObjects.createPrototype(), MyObjects.createPrototype());
    }
}
