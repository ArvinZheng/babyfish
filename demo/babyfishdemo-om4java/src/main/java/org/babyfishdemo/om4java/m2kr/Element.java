package org.babyfishdemo.om4java.m2kr;

import java.util.Map;

import org.babyfish.collection.XOrderedMap;
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
 
/**
 * @author Tao Chen
 */
public class Element {
     
    private static final ObjectModelFactory<OM> OM_FACTORY = ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Element element) {
        return element.om;
    }
    
    public Map<String, Attribute> getAttributes() {
        return this.om.getAttributes();
    }
    
    @ObjectModelDeclaration
    private interface OM {
    
        @Association(opposite = "elementReference")
        XOrderedMap<String, Attribute> getAttributes();
    }
}
