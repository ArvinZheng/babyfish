package org.babyfishdemo.om4java.m2kr;

import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.reference.KeyedReference;
 
/**
 * @author Tao Chen
 */
public class Attribute {
 
    private static final ObjectModelFactory<OM> OM_FACTORY = ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Attribute attribute) {
        return attribute.om;
    }
    
    public String getName() {
        return this.om.getElementReference().getKey();
    }
    
    public void setName(String name) {
        this.om.getElementReference().setKey(name);
    }
    
    public Element getElement() {
        return this.om.getElementReference().get();
    }
    
    public void setElement(Element element) {
        this.om.getElementReference().set(element);
    }
 
    @ObjectModelDeclaration
    private interface OM {
    
        @Scalar
        String getValue();
        void setValue(String value);
        
        @Association(opposite = "attributes")
        KeyedReference<String, Element> getElementReference();
    }
}
