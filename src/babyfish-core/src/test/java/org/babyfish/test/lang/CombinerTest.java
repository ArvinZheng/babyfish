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

import java.io.IOException;
import java.sql.SQLException;

import junit.framework.Assert;

import org.babyfish.lang.HasBeenCombined;
import org.babyfish.lang.Combiner;
import org.babyfish.lang.Combiners;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class CombinerTest {
    
    private int doVoidCount;
    
    private int doStringCount;
    
    private int doBooleanCount;
    
    private int doBoolean2Count;
    
    private int doCharCount;
    
    private int doChar2Count;
    
    private int doByteCount;
    
    private int doByte2Count;
    
    private int doShortCount;
    
    private int doShort2Count;
    
    private int doIntCount;
    
    private int doInt2Count;
    
    private int doLongCount;
    
    private int doLong2Count;
    
    private int doFloatCount;
    
    private int doFloat2Count;
    
    private int doDoubleCount;
    
    private int doDouble2Count;
    
    @Test
    public void testCombine() {
        Combiner<NamedInterface> combiner = Combiners.of(NamedInterface.class);
        assertNamedInterface(combiner.combine(null, null));
        assertNamedInterface(combiner.combine(new NamedInterfaceImpl("one"), null), "one");
        assertNamedInterface(combiner.combine(null, new NamedInterfaceImpl("two")), "two");
        assertNamedInterface(
                combiner.combine(new NamedInterfaceImpl("one"), new NamedInterfaceImpl("two")), 
                "one", "two");
        assertNamedInterface(
                combiner.combine(
                        combiner.combine(new NamedInterfaceImpl("one"), new NamedInterfaceImpl("two")),
                        new NamedInterfaceImpl("three")), 
                "one", "two", "three");
        assertNamedInterface(
                combiner.combine(
                        new NamedInterfaceImpl("one"),
                        combiner.combine(new NamedInterfaceImpl("two"), new NamedInterfaceImpl("three"))), 
                "one", "two", "three");
        assertNamedInterface(
                combiner.combine(
                        combiner.combine(new NamedInterfaceImpl("one"), new NamedInterfaceImpl("two")),
                        combiner.combine(new NamedInterfaceImpl("three"), new NamedInterfaceImpl("four"))), 
                "one", "two", "three", "four");
    }
    
    @Test
    public void testRemove() {
        Combiner<NamedInterface> combiner = Combiners.of(NamedInterface.class);
        assertNamedInterface(combiner.remove(null, null));
        assertNamedInterface(combiner.remove(null, new NamedInterfaceImpl("one")));
        assertNamedInterface(combiner.remove(new NamedInterfaceImpl("one"), null), "one");
        assertNamedInterface(
                combiner.remove(
                        new NamedInterfaceImpl("one"), 
                        new NamedInterfaceImpl("two")), 
                "one");
        assertNamedInterface(
                combiner.remove(
                        new NamedInterfaceImpl("one"), 
                        new NamedInterfaceImpl("one")));
        assertNamedInterface(
                combiner.remove(
                        prepareNamedInterfaces("one", "two"), 
                        null), 
                "one", "two");
        assertNamedInterface(
                combiner.remove(
                        prepareNamedInterfaces("one", "two"), 
                        new NamedInterfaceImpl("two")), 
                "one");
        assertNamedInterface(
                combiner.remove(
                        prepareNamedInterfaces("one", "two"), 
                        prepareNamedInterfaces("one", "two")));
        assertNamedInterface(
                combiner.remove(
                        prepareNamedInterfaces("one", "two", "three", "four", "five", "six", "seven"), 
                        prepareNamedInterfaces("one", "three", "five", "seven")),
                "two", "four", "six");
        assertNamedInterface(
                combiner.remove(
                        prepareNamedInterfaces(
                                "one", "two", "three", "four", "five", "six", "seven",
                                "one", "two", "three", "four", "five", "six", "seven"), 
                        prepareNamedInterfaces(
                                "one", "three", "five", "seven",
                                "one", "five")),
                "two", "four", "six", "two", "three", "four", "six", "seven");
        assertNamedInterface(
                combiner.remove(
                        prepareNamedInterfaces(
                                "one", "two", "three", "four", "five", "six", "seven",
                                "one", "two", "three", "four", "five", "six", "seven"), 
                        prepareNamedInterfaces(
                                "one", "two", "three", "four", "five", "six", "seven",
                                "one", "two", "three", "five", "six", "seven")),
                "four");
    }
    
    @Test
    public void testImpl() throws IOException, SQLException {
        
        Listener combined = 
            Combiners
            .of(Listener.class)
            .combine(this.new ListenerImpl(), this.new ListenerImpl2());
        
        combined.doVoid();
        Assert.assertEquals("tomcat", combined.doString("tom", "cat"));
        Assert.assertEquals(true, combined.doBoolean(true, false));
        Assert.assertEquals(new Boolean(true), combined.doBoolean2(true, false));
        Assert.assertEquals('B', combined.doChar('A', (char)1));
        Assert.assertEquals(new Character('b'), combined.doChar2('a', (char)1));
        Assert.assertEquals((byte)3, combined.doByte((byte)1, (byte)2));
        Assert.assertEquals(new Byte((byte)7), combined.doByte2((byte)3, (byte)4));
        Assert.assertEquals((short)11, combined.doShort((short)5, (short)6));
        Assert.assertEquals(new Short((short)15), combined.doShort2((short)7, (short)8));
        Assert.assertEquals(19, combined.doInt(9, 10));
        Assert.assertEquals(new Integer(23), combined.doInt2(11, 12));
        Assert.assertEquals(27L, combined.doLong(13L, 14L));
        Assert.assertEquals(new Long(31L), combined.doLong2(15L, 16L));
        Assert.assertEquals(35F, combined.doFloat(17F, 18F));
        Assert.assertEquals(new Float(39F), combined.doFloat2(19F, 20F));
        Assert.assertEquals(43D, combined.doDouble(21D, 22D));
        Assert.assertEquals(new Double(47D), combined.doDouble2(23D, 24D));
        
        Assert.assertEquals(2, this.doVoidCount);
        Assert.assertEquals(2, this.doStringCount);
        Assert.assertEquals(2, this.doBooleanCount);
        Assert.assertEquals(2, this.doBoolean2Count);
        Assert.assertEquals(2, this.doCharCount);
        Assert.assertEquals(2, this.doChar2Count);
        Assert.assertEquals(2, this.doByteCount);
        Assert.assertEquals(2, this.doByte2Count);
        Assert.assertEquals(2, this.doShortCount);
        Assert.assertEquals(2, this.doShort2Count);
        Assert.assertEquals(2, this.doIntCount);
        Assert.assertEquals(2, this.doInt2Count);
        Assert.assertEquals(2, this.doLongCount);
        Assert.assertEquals(2, this.doLong2Count);
        Assert.assertEquals(2, this.doFloatCount);
        Assert.assertEquals(2, this.doFloat2Count);
        Assert.assertEquals(2, this.doDoubleCount);
        Assert.assertEquals(2, this.doDouble2Count);
    }
    
    @SuppressWarnings("unchecked")
    private static void assertNamedInterface(NamedInterface namedInterface, String ... names) {
        if (names.length == 0) {
            Assert.assertNull(namedInterface);
        } else if (names.length == 1) {
            Assert.assertFalse(namedInterface instanceof HasBeenCombined<?>);
            Assert.assertEquals(names[0], namedInterface.name());
        } else {
            Assert.assertEquals(names[names.length - 1], namedInterface.name());
            Assert.assertTrue(namedInterface instanceof HasBeenCombined<?>);
            HasBeenCombined<NamedInterface> combined = (HasBeenCombined<NamedInterface>)namedInterface;
            Assert.assertEquals(names.length, combined.size());
            for (int i = 0; i < names.length; i++) {
                Assert.assertEquals(names[i], combined.get(i).name());
            }
            int hash = 0;
            for (String name : names) {
                hash = 31 * hash + name.hashCode();
            }
            Assert.assertEquals(hash, namedInterface.hashCode());
            Assert.assertEquals(prepareNamedInterfaces(names), namedInterface);
        }
        
    }

    private static NamedInterface prepareNamedInterfaces(String ... names) {
        Combiner<NamedInterface> combiner = Combiners.of(NamedInterface.class);
        NamedInterface ni = null;
        for (String name : names) {
            ni = combiner.combine(ni, new NamedInterfaceImpl(name));
        }
        return ni;
    }

    private interface Listener {
        
        void doVoid() throws IOException, SQLException;
        
        String doString(String v1, String v2) throws IOException, SQLException;
        
        boolean doBoolean(boolean v1, Boolean v2) throws IOException, SQLException;
        
        Boolean doBoolean2(boolean v1, Boolean v2) throws IOException, SQLException;
        
        char doChar(char v1, Character v2) throws IOException, SQLException;
        
        Character doChar2(char v1, Character v2) throws IOException, SQLException;
        
        byte doByte(byte v1, Byte v2) throws IOException, SQLException;
        
        Byte doByte2(byte v1, Byte v2) throws IOException, SQLException;
        
        short doShort(short v1, Short v2) throws IOException, SQLException;
        
        Short doShort2(short v1, Short v2) throws IOException, SQLException;
        
        int doInt(int v1, Integer v2) throws IOException, SQLException;
        
        Integer doInt2(int v1, Integer v2) throws IOException, SQLException;
        
        long doLong(long v1, Long v2) throws IOException, SQLException;
        
        Long doLong2(long v1, Long v2) throws IOException, SQLException;
        
        float doFloat(float v1, Float v2) throws IOException, SQLException;
        
        Float doFloat2(float v1, Float v2) throws IOException, SQLException;
        
        double doDouble(double v1, Double v2) throws IOException, SQLException;
        
        Double doDouble2(double v1, Double v2) throws IOException, SQLException;
        
    }
    
    private class ListenerImpl implements Listener {

        @Override
        public void doVoid() throws IOException, SQLException {
            CombinerTest.this.doVoidCount++;
        }
        
        @Override
        public String doString(String v1, String v2) throws IOException, SQLException {
            CombinerTest.this.doStringCount++;
            return null;
        }
        
        @Override
        public boolean doBoolean(boolean v1, Boolean v2) throws IOException, SQLException {
            CombinerTest.this.doBooleanCount++;
            return false;
        }

        @Override
        public Boolean doBoolean2(boolean v1, Boolean v2) throws IOException, SQLException {
            CombinerTest.this.doBoolean2Count++;
            return null;
        }

        @Override
        public byte doByte(byte v1, Byte v2) throws IOException, SQLException {
            CombinerTest.this.doByteCount++;
            return 0;
        }

        @Override
        public Byte doByte2(byte v1, Byte v2) throws IOException, SQLException {
            CombinerTest.this.doByte2Count++;
            return null;
        }

        @Override
        public char doChar(char v1, Character v2) throws IOException, SQLException {
            CombinerTest.this.doCharCount++;
            return 0;
        }

        @Override
        public Character doChar2(char v1, Character v2) throws IOException, SQLException {
            CombinerTest.this.doChar2Count++;
            return null;
        }

        @Override
        public short doShort(short v1, Short v2) throws IOException, SQLException {
            CombinerTest.this.doShortCount++;
            return 0;
        }

        @Override
        public Short doShort2(short v1, Short v2) throws IOException, SQLException {
            CombinerTest.this.doShort2Count++;
            return null;
        }

        @Override
        public int doInt(int v1, Integer v2) throws IOException, SQLException {
            CombinerTest.this.doIntCount++;
            return 0;
        }

        @Override
        public Integer doInt2(int v1, Integer v2) throws IOException, SQLException {
            CombinerTest.this.doInt2Count++;
            return null;
        }

        @Override
        public long doLong(long v1, Long v2) throws IOException, SQLException {
            CombinerTest.this.doLongCount++;
            return 0;
        }

        @Override
        public Long doLong2(long v1, Long v2) throws IOException, SQLException {
            CombinerTest.this.doLong2Count++;
            return null;
        }
        
        @Override
        public float doFloat(float v1, Float v2) throws IOException, SQLException {
            CombinerTest.this.doFloatCount++;
            return 0;
        }

        @Override
        public Float doFloat2(float v1, Float v2) throws IOException, SQLException {
            CombinerTest.this.doFloat2Count++;
            return null;
        }
    
        @Override
        public double doDouble(double v1, Double v2) throws IOException, SQLException {
            CombinerTest.this.doDoubleCount++;
            return 0;
        }

        @Override
        public Double doDouble2(double v1, Double v2) throws IOException, SQLException {
            CombinerTest.this.doDouble2Count++;
            return null;
        }
    }
    
    private class ListenerImpl2 implements Listener {

        @Override
        public void doVoid() throws IOException, SQLException {
            CombinerTest.this.doVoidCount++;
        }
        
        @Override
        public String doString(String v1, String v2) throws IOException, SQLException {
            CombinerTest.this.doStringCount++;
            return v1 + v2;
        }
        
        @Override
        public boolean doBoolean(boolean v1, Boolean v2) throws IOException, SQLException {
            CombinerTest.this.doBooleanCount++;
            return true;
        }

        @Override
        public Boolean doBoolean2(boolean v1, Boolean v2) throws IOException, SQLException {
            CombinerTest.this.doBoolean2Count++;
            return true;
        }

        @Override
        public byte doByte(byte v1, Byte v2) throws IOException, SQLException {
            CombinerTest.this.doByteCount++;
            return (byte)(v1 + v2);
        }

        @Override
        public Byte doByte2(byte v1, Byte v2) throws IOException, SQLException {
            CombinerTest.this.doByte2Count++;
            return (byte)(v1 + v2);
        }

        @Override
        public char doChar(char v1, Character v2) throws IOException, SQLException {
            CombinerTest.this.doCharCount++;
            return (char)(v1 + v2);
        }

        @Override
        public Character doChar2(char v1, Character v2) throws IOException, SQLException {
            CombinerTest.this.doChar2Count++;
            return (char)(v1 + v2);
        }

        @Override
        public short doShort(short v1, Short v2) throws IOException, SQLException {
            CombinerTest.this.doShortCount++;
            return (short)(v1 + v2);
        }

        @Override
        public Short doShort2(short v1, Short v2) throws IOException, SQLException {
            CombinerTest.this.doShort2Count++;
            return (short)(v1 + v2);
        }

        @Override
        public int doInt(int v1, Integer v2) throws IOException, SQLException {
            CombinerTest.this.doIntCount++;
            return v1 + v2;
        }

        @Override
        public Integer doInt2(int v1, Integer v2) throws IOException, SQLException {
            CombinerTest.this.doInt2Count++;
            return v1 + v2;
        }

        @Override
        public long doLong(long v1, Long v2) throws IOException, SQLException {
            CombinerTest.this.doLongCount++;
            return v1 + v2;
        }

        @Override
        public Long doLong2(long v1, Long v2) throws IOException, SQLException {
            CombinerTest.this.doLong2Count++;
            return v1 + v2;
        }
    
        @Override
        public float doFloat(float v1, Float v2) throws IOException, SQLException {
            CombinerTest.this.doFloatCount++;
            return v1 + v2;
        }

        @Override
        public Float doFloat2(float v1, Float v2) throws IOException, SQLException {
            CombinerTest.this.doFloat2Count++;
            return v1 + v2;
        }

        @Override
        public double doDouble(double v1, Double v2) throws IOException, SQLException {
            CombinerTest.this.doDoubleCount++;
            return v1 + v2;
        }

        @Override
        public Double doDouble2(double v1, Double v2) throws IOException, SQLException {
            CombinerTest.this.doDouble2Count++;
            return v1 + v2;
        }
    }
    
    private interface NamedInterface {
        String name();
    }
    
    private static class NamedInterfaceImpl implements NamedInterface {
        
        private String name;
        
        public NamedInterfaceImpl(String name) {
            this.name = name;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public int hashCode() {
            return this.name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof NamedInterface)) {
                return false;
            }
            NamedInterface other = (NamedInterface)obj;
            return this.name.equals(other.name());
        }
        
    }
    
}
