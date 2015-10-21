package org.babyfishdemo.war3shop.web.aop;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.babyfishdemo.war3shop.web.json.JsonpModelAndView;

/**
 * @author Tao Chen
 */
@Aspect
public class JsonAspect {
    
    private static final Log log = LogFactory.getLog(JsonAspect.class);
    
    @Resource(name = "specialExceptionMessageMap")
    private Map<Class<?>, String> specialExceptionMessageMap;
    
    @Pointcut(
            "execution(" +
            "public " +
            "org.babyfishdemo.war3shop.web.json.JsonpModelAndView+ " +
            "org.babyfishdemo.war3shop.web.*.*(..))"
    )
    public void returnModelAndView() {}

    @Around("returnModelAndView()")
    public JsonpModelAndView aroundJsonpMethod(ProceedingJoinPoint pjp) {
        JsonpModelAndView jsonModelAndView = null;
        try {
            jsonModelAndView = (JsonpModelAndView)pjp.proceed();
        } catch (Throwable ex) {
            ExceptionDescriptor descriptor = this.descriptor(ex);
            jsonModelAndView = new JsonpModelAndView(descriptor);
            log.error("Failed to handle the web request " + pjp.getSignature(), ex);
        }
        return jsonModelAndView;
    }
    
    private ExceptionDescriptor descriptor(Throwable throwable) {
        String message = throwable.getMessage();
        for (Class<?> type = throwable.getClass(); type != Throwable.class; type = type.getSuperclass()) {
            String specialMessage = this.specialExceptionMessageMap.get(type);
            if (specialMessage != null) {
                message = specialMessage;
                break;
            }
        }
        return new ExceptionDescriptor(throwable.getClass(), message);
    }
    
    private static class ExceptionDescriptor {
        
        private Class<?> exceptionClass;
        
        private String exceptionMessage;
        
        private ExceptionDescriptor(Class<?> exceptionClass, String exceptionMessage) {
            this.exceptionClass = exceptionClass;
            this.exceptionMessage = exceptionMessage;
        }
        
        @SuppressWarnings("unused")
        public Class<?> getExceptionClass() {
            return this.exceptionClass;
        }
        
        @SuppressWarnings("unused")
        public String getExceptionMessage() {
            return this.exceptionMessage;
        }
    }
}
