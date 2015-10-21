package org.babyfishdemo.foundation.typedi18n.generic;

import java.io.PrintStream;

import junit.framework.Assert;

import org.babyfishdemo.foundation.typedi18n.generic.ComplexDemo;
import org.babyfishdemo.foundation.typedi18n.generic.ComplexDemoAspect.PrintStreamHandler;
import org.babyfishdemo.foundation.typedi18n.generic.ComplexDemoAspect.Scope;
import org.junit.Test;

/**
 * <p>
	 * Notes: If you use maven to execute this test, it's OK.
	 * but if you use eclipse to execute it, please support
	 * the VM argument to specify the aspectj agent for the 
	 * junit run configuration of eclipse.
 * <p>
 * 
 * <p>
 * 	Use my machine to be the example, the vm argument must be
 * <p>
 * 
 * <div>"-javaagent:C:/MavenLocal/org/aspectj/aspectjweaver/1.8.5/aspectjweaver-1.8.5.jar"</div>
 *
 * @author Tao Chen
 */
public class ComplexDemoTest {

    @Test
    public void testMain() {
        
        final StringBuilder builder = new StringBuilder();
        
        // If you use Java8, change it to be lambda to make code to be simpler
        PrintStreamHandler handler = new PrintStreamHandler() {
            @Override
            public void println(PrintStream this_, String x) {
                builder.append(x).append('\n');
            }
        };
        
        /*
         * During this scope, 
         * "System.out.println(String)" 
         * is replaced to 
         * "handler.println(PrintStream, String)"
         */
        try (Scope scope = new Scope(handler)) {
            ComplexDemo.main(new String[0]);
        }
        
        Assert.assertEquals(
                "The summation of \"16.0 + 9.0i\" and \"3.0 - 4.0i\" is \"19.0 + 5.0i\"\n" +
                "The difference of \"16.0 + 9.0i\" and \"3.0 - 4.0i\" is \"13.0 + 13.0i\"\n" +
                "The product of \"16.0 + 9.0i\" and \"3.0 - 4.0i\" is \"84.0 - 37.0i\"\n" +
                "The quotient of \"16.0 + 9.0i\" and \"3.0 - 4.0i\" is \"0.48 + 3.64i\"\n", 
                builder.toString()
        );
    }
}
