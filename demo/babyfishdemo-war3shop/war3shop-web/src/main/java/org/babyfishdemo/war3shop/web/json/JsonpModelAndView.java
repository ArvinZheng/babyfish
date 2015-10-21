package org.babyfishdemo.war3shop.web.json;

import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.babyfish.lang.Nulls;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * @author Tao Chen
 */
public class JsonpModelAndView extends ModelAndView {
    
    public JsonpModelAndView(Object model) {
        super(new ViewImpl(model));
    }
    
    private static class ViewImpl implements View {
        
        private static final Object NIL_MODEL = new Object();
        
        private Object model;

        public ViewImpl(Object model) {
            this.model = model != null ? model : NIL_MODEL;
        }
        
        @Override
        public String getContentType() {
            return "text/jsonp";
        }
        
        @Override
        public void render(
                Map<String, ?> unused, 
                HttpServletRequest request,
                HttpServletResponse response) throws Exception {
            response.setContentType("text/jsonp");
            response.setHeader("Cache-Control", "no-cache, no-store");
            response.setHeader("Pragma", "no-cache");
            long time = System.currentTimeMillis();
            response.setDateHeader("Last-Modified", time);
            response.setDateHeader("Date", time);
            response.setDateHeader("Expires", time);
            try (PrintWriter writer = response.getWriter()) {
                String jsonpCallback = request.getParameter("callback");
                if (!Nulls.isNullOrEmpty(jsonpCallback)) {
                    writer.write(jsonpCallback);
                    writer.write('(');
                }
                try (SerializeWriter serializeWriter = new SerializeWriter(writer)) {
                    KendoUIJSONSerializer kendoUIJSONSerializer = new KendoUIJSONSerializer(serializeWriter);
                    kendoUIJSONSerializer.getPropertyPreFilters().add(HibernateLazyScalarFilter.INSTANCE);
                    kendoUIJSONSerializer.getPropertyFilters().add(HibernateLazyAssociationFilter.INSTANCE);
                    kendoUIJSONSerializer.getAfterFilters().add(PolymorphismFilter.INSTANCE);
                    kendoUIJSONSerializer.config(SerializerFeature.WriteEnumUsingToString, false);
                    kendoUIJSONSerializer.config(SerializerFeature.WriteDateUseDateFormat, true);
                    kendoUIJSONSerializer.write(this.model);
                }
                if (!Nulls.isNullOrEmpty(jsonpCallback)) {
                    writer.write(')');
                }
            }
        }
    }
}
