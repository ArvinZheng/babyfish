package org.babyfishdemo.war3shop.web.json;

import org.hibernate.bytecode.instrumentation.spi.FieldInterceptor;
import org.hibernate.bytecode.internal.javassist.FieldHandled;
import org.hibernate.bytecode.internal.javassist.FieldHandler;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.PropertyPreFilter;

/**
 * @author Tao Chen
 */
public class HibernateLazyScalarFilter implements PropertyPreFilter {
    
    public static final HibernateLazyScalarFilter INSTANCE = 
            new HibernateLazyScalarFilter();
    
    private static final String FIELD_HANDLER = "fieldHandler";
    
    private HibernateLazyScalarFilter() {}
    
    @Override
    public boolean apply(JSONSerializer serializer, Object object, String name) {
        if (object instanceof FieldHandled) {
            if (FIELD_HANDLER.equals(name)) {
                return false;
            }
            FieldHandled fieldHandled = (FieldHandled)object;
            FieldHandler fieldHandler = fieldHandled.getFieldHandler();
            if (fieldHandler != null) {
                FieldInterceptor fieldInterceptor = (FieldInterceptor)fieldHandler;
                if (!fieldInterceptor.isInitialized(name)) {
                    return false;
                }
            }
        }
        return true;
    }
}
