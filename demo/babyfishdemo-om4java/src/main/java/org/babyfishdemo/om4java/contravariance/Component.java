package org.babyfishdemo.om4java.contravariance;

import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.reference.Reference;
 
/**
 * @author Tao Chen
 */
public abstract class Component {
 
    private static final ObjectModelFactory<OM> OM_FACTORY = ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Component component) {
        return component.om;
    }
    
    public Container getParent() {
        return this.om.getParentReference().get();
    }
    
    public void setParent(Container parent) {
        this.om.getParentReference().set(parent);
    }
    
    @ObjectModelDeclaration
    private interface OM {
        
        @Association(opposite = "components")
        Reference<Container> getParentReference();
    }
}
