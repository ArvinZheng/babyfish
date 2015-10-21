package org.babyfishdemo.war3shop.web.json;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerialContext;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeWriter;

/*
 * Unfortunately, KendoUI does not support the object graph with circular references,
 * because the kendo.data.ObservableObject and kendo.data.ObservableArray is not smart 
 * enough so that the stack-overflow error will be raised when the kendo UI widgets accept
 * the json data with circular references, so server-side has to guarantee it never generate 
 * the object graph with circular references like this
 * {
 *      "name": "parentObject",
 *      "children": [
 *          { "name": "childObject1", "parent": { "$ref": "$" } },
 *          { "name": "childObject2", "parent": { "$ref": "$" } },
 *          { "name": "childObject3", "parent": { "$ref": "$" } }
 *      ]
 * }
 *  
 * But, this client-side circular reference resolving is still necessary because 
 * this structure is still allowed and need to be resolved, this structure is
 * very useful, especially when the duplicated objects contain lob properties
 * [
 *      { "name": "childObject1", "parent": { "name": "parentObject", "blobField": ....Huge data.... } },
 *      { "name": "childObject2", "parent": { "$ref": "$[0].parent" } },
 *      { "name": "childObject3", "parent": { "$ref": "$[0].parent" } }
 * ]
 * This example shows the "{ $ref: ... }" generated by alibaba fast join is still useful for this project,
 * that's why this project does not use 
 * "com.alibaba.fastjson.serializer.SerializerFeature.DisableCircularReferenceDetect"
 */
/**
 * @author Tao Chen
 */
public class KendoUIJSONSerializer extends JSONSerializer {

    public KendoUIJSONSerializer() {
        super();
    }

    public KendoUIJSONSerializer(SerializeConfig config) {
        super(config);
    }

    public KendoUIJSONSerializer(SerializeWriter out, SerializeConfig config) {
        super(out, config);
    }

    public KendoUIJSONSerializer(SerializeWriter out) {
        super(out);
    }

    @Override
    public void writeReference(Object object) {
        for (SerialContext ctx = this.getContext(); ctx != null; ctx = ctx.getParent()) {
            if (ctx.getObject() == object) {
                this.getWriter().write("{\"forbiddenRef\":true}");
                return;
            }
        }
        super.writeReference(object);
    }
}
