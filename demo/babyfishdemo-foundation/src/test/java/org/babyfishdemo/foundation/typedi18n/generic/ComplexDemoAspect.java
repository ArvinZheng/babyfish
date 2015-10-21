package org.babyfishdemo.foundation.typedi18n.generic;

import java.io.PrintStream;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.babyfish.lang.Arguments;
import org.babyfish.util.Stack;

/**
 * @author Tao Chen
 */
@Aspect
public class ComplexDemoAspect {

	/* 
	 * Uses this aspect to replace "System.out.println" in the 
	 * "org.babyfishdemo.foundation.typedi18n.ComplexDemo"
	 * at bytecode-loading-time, 
	 * because "System.out.println" is not easy to be tested.
	 * 
	 * Please see 
	 * src/test/resources/MATA-INF/aop.xml
	 * and
	 * the configuration for plugin "maven-surefire-plugin" in pom.xml
	 */
	
	
	
    private static final Stack<PrintStreamHandler> HANDLER_STACK = new Stack<>();
    
    @Around(
            value = 
                "within(org.babyfishdemo.foundation.typedi18n.generic.ComplexDemo) &&" +
                "call(void java.io.PrintStream.println(java.lang.String)) && " +
                "target(this_) && " +
                "args(x)",
            argNames = "this_, x"
    )
    public void aroundPrintStreamPrintln(
            ProceedingJoinPoint pjp, 
            PrintStream this_, 
            String x) throws Throwable {
        PrintStreamHandler handler = HANDLER_STACK.peek();
        if (handler == null) {
            pjp.proceed();
        } else {
            handler.println(this_, x);
        }
    }

    public interface PrintStreamHandler {
        
        void println(PrintStream this_, String x);
    }
    
    public static class Scope implements AutoCloseable {
        
        public Scope(PrintStreamHandler handler) {
            HANDLER_STACK.push(Arguments.mustNotBeNull("handler", handler));
        }

        @Override
        public void close() {
            HANDLER_STACK.pop();
        }
    }
}
