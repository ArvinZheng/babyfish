package org.babyfishdemo.foundation.combiner;

import org.babyfish.lang.ChainInvocationExceptionHandleMode;
import org.babyfish.lang.Combiner;
import org.babyfish.lang.Combiners;
import org.junit.Assert;
import org.junit.Test;

/*
 * A part of this functionality has C++ version, 
 * please see 
 * ${babyfish-home}/c++/src/delegate.h
 * and
 * ${babyfish-home}/c++/demo/DelegateTest.cpp
 */
/**
 * @author Tao Chen
 */
public class CombinberTest {
    
    private static final Combiner<Runnable> RUNNABLE_BREAK_COMBINER =
            Combiners.of(Runnable.class, ChainInvocationExceptionHandleMode.BREAK);
    
    private static final Combiner<Runnable> RUNNABLE_CONTINUE_COMBINER =
            Combiners.of(Runnable.class, ChainInvocationExceptionHandleMode.CONTINUE);
    
    /*
     * If the second argument of "Combiners.of" is not specified,
     * babyfish checks whether that interface is marked by the
     * annotation @"org.babyfish.lang.DefaultChainInvocationExceptionHandleMode"
     * and use its argument value if the annotation is used.
     * otherwise, it uses "ChainInvocationExceptionHandleMode.BREAK"
     */
    private static final Combiner<ArithmeticHandler> ARITHMETIC_COMBINER = 
            Combiners.of(ArithmeticHandler.class);
    
    @Test
    public void testSimpleCombiner() {
        
        final StringBuilder builder = new StringBuilder();
        
        ArithmeticHandler sum = new ArithmeticHandler() {
            @Override
            public double calculate(double x, double y) {
                builder.append(x + " + " + y + " = " + (x + y) + '\n');
                return x + y;
            }
        };
        ArithmeticHandler diff = new ArithmeticHandler() {
            @Override
            public double calculate(double x, double y) {
                builder.append(x + " - " + y + " = " + (x - y) + '\n');
                return x - y;
            }
        };
        ArithmeticHandler prod = new ArithmeticHandler() {
            @Override
            public double calculate(double x, double y) {
                builder.append(x + " * " + y + " = " + (x * y) + '\n');
                return x * y;
            }
        };
        ArithmeticHandler quot = new ArithmeticHandler() {
            @Override
            public double calculate(double x, double y) {
                builder.append(x + " / " + y + " = " + (x / y) + '\n');
                return x / y;
            }
        };
        
        ArithmeticHandler arithmetic = null;
        arithmetic = ARITHMETIC_COMBINER.combine(arithmetic, sum);
        arithmetic = ARITHMETIC_COMBINER.combine(arithmetic, diff);
        arithmetic = ARITHMETIC_COMBINER.combine(arithmetic, prod);
        arithmetic = ARITHMETIC_COMBINER.combine(arithmetic, quot);
        
        // The returned of combined object is the returned value of LAST original object.
        Assert.assertEquals(5D, arithmetic.calculate(60, 12), 1E-7D);
        // But the methods of all the child objects are called
        Assert.assertEquals(
                "60.0 + 12.0 = 72.0\n" +
                "60.0 - 12.0 = 48.0\n" +
                "60.0 * 12.0 = 720.0\n" +
                "60.0 / 12.0 = 5.0\n",
                builder.toString()
        );
        builder.setLength(0);
        
        arithmetic = ARITHMETIC_COMBINER.remove(arithmetic, prod);
        arithmetic = ARITHMETIC_COMBINER.remove(arithmetic, quot);
        Assert.assertEquals(48D, arithmetic.calculate(60, 12), 1E-7D);
        Assert.assertEquals(
                "60.0 + 12.0 = 72.0\n" +
                "60.0 - 12.0 = 48.0\n",
                builder.toString()
        );
        
        arithmetic = ARITHMETIC_COMBINER.remove(arithmetic, sum);
        arithmetic = ARITHMETIC_COMBINER.remove(arithmetic, diff);
        Assert.assertNull(arithmetic);
    }
    
    @Test
    public void testNull() {
        
        Assert.assertNull(ARITHMETIC_COMBINER.combine(null, null));
        
        ArithmeticHandler arithmetic = new ArithmeticHandler() {
            @Override
            public double calculate(double x, double y) {
                throw new UnsupportedOperationException();
            }
        };
        Assert.assertSame(
                arithmetic, 
                ARITHMETIC_COMBINER.combine(arithmetic, null)
        );
        Assert.assertSame(
                arithmetic,
                ARITHMETIC_COMBINER.remove(
                        ARITHMETIC_COMBINER.combine(arithmetic, null),
                        null
                )
        );
        Assert.assertNull(
                ARITHMETIC_COMBINER.remove(
                        ARITHMETIC_COMBINER.combine(arithmetic, null),
                        arithmetic
                )
        );
    }
    
    @Test
    public void testBreakWhenExceptionRaised() {
        final StringBuilder builder = new StringBuilder();
        Runnable runnable = RUNNABLE_BREAK_COMBINER.combine(
                new Runnable() {
                    @Override
                    public void run() {
                        builder.append("The first runnable is executed\n");
                        throw new UnsupportedOperationException("First exception");
                    }
                }, 
                new Runnable() {
                    @Override
                    public void run() {
                        builder.append("The second runnable is executed\n");
                        throw new UnsupportedOperationException("Second exception");
                    }
                }
        );
        try {
            runnable.run();
            Assert.fail("UnsupportedOperationException is expected");
        } catch (UnsupportedOperationException ex) {
            Assert.assertEquals("First exception", ex.getMessage());
        }
        // Only the first original runnable is executed
        Assert.assertEquals(
                "The first runnable is executed\n", 
                builder.toString()
        );
    }
    
    @Test
    public void testContinueWhenExceptionRaised() {
        final StringBuilder builder = new StringBuilder();
        Runnable runnable = RUNNABLE_CONTINUE_COMBINER.combine(
                new Runnable() {
                    @Override
                    public void run() {
                        builder.append("The first runnable is executed\n");
                        throw new UnsupportedOperationException("First exception");
                    }
                }, 
                new Runnable() {
                    @Override
                    public void run() {
                        builder.append("The second runnable is executed\n");
                        throw new UnsupportedOperationException("Second exception");
                    }
                }
        );
        try {
            runnable.run();
            Assert.fail("UnsupportedOperationException is expected");
        } catch (UnsupportedOperationException ex) {
            Assert.assertEquals("First exception", ex.getMessage());
        }
        // All the original runnable objects are executed
        Assert.assertEquals(
                "The first runnable is executed\n" +
                "The second runnable is executed\n", 
                builder.toString()
        );
    }

    private interface ArithmeticHandler {
        
        double calculate(double x, double y);
    }
}
