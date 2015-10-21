package org.babyfishdemo.foundation.combiner;

import java.math.BigDecimal;

import junit.framework.Assert;

import org.babyfishdemo.foundation.combiner.event.PropertyChanagedEvent;
import org.babyfishdemo.foundation.combiner.event.PropertyChangedListener;
import org.junit.Test;

/*
 * (1) Please learn "CombinerTest" before learn this one
 * (2) This functionality has C++ version, 
 *     please see 
 *     ${babyfish-home}/c++/src/event.h
 *     and
 *     ${babyfish-home}/c++/demo/EventTest.cpp
 */
/**
 * @author Tao Chen
 */
public class EventTest {

    @Test
    public void test() {
        
        Book book = new Book("NodeJS", new BigDecimal(40));
        
        final StringBuilder builder = new StringBuilder();
        book.addPropertyChangedListener(new PropertyChangedListener() {
            @Override
            public void propertyChanged(PropertyChanagedEvent e) {
                /*
                 * For the classic event implementation of Java Bean(or GWT),
                 * you can NOT change the event during the handling of the event itself,
                 * because the event listener queue are retained by java.util.List so that 
                 * "java.util.ConcurrentModificationException" will be raised if you do that.
                 * 
                 * For the event implementation of babyfish,
                 * you can change the event anywhere, even if during the handling
                 * of the event itself.
                 * 
                 * The current listener can only be triggered ONCE!
                 */
                e.getSource().removePropertyChangedListener(this);
                builder.append("[ONLY-ONCE-HANDLER]: " + e + '\n');
            }
        });
        book.addPropertyChangedListener(new PropertyChangedListener() {
            @Override
            public void propertyChanged(PropertyChanagedEvent e) {
                builder.append("[FOREVER-HANDLER]: " + e + '\n');
            }
        });
        
        book.setName("AngularJS");
        Assert.assertEquals( // Those two handers are triggered
                "[ONLY-ONCE-HANDLER]: { "
                +   "propertyName: \"name\", "
                +   "oldValue: \"NodeJS\", "
                +   "newValue: \"AngularJS\" "
                + "}\n"
                + "[FOREVER-HANDLER]: { "
                +   "propertyName: \"name\", "
                +   "oldValue: \"NodeJS\", "
                +   "newValue: \"AngularJS\" " 
                + "}\n",
                builder.toString()
        );
        builder.setLength(0); //Clear the StringBuilder
        
        book.setPrice(new BigDecimal(43));
        // ONLY the second handler is triggered
        // because the first handler had been removed by itself
        Assert.assertEquals( 
                "[FOREVER-HANDLER]: { "
                + "propertyName: \"price\", "
                + "oldValue: \"40\", "
                + "newValue: \"43\" " 
                + "}\n",
                builder.toString()
        );
    }
}
