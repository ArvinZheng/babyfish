package org.babyfishdemo.om4java.contravariance;

import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Contravariance;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.reference.Reference;
 
/**
 * @author Tao Chen
 */
public class TabPage extends Container {
 
    private static final ObjectModelFactory<OM> OM_FACTORY = ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(TabPage tabPage) {
        return tabPage.om;
    }
    
    @Override
    public TabControl getParent() {
        return this.om.getParentReference().get();
    }
    
    public void setParent(TabControl parent) {
        this.om.getParentReference().set(parent);
    }
    
    @Deprecated
    @Override
    public final void setParent(Container parent) {
        this.setParent((TabControl)parent);
    }
    
    @ObjectModelDeclaration
    private interface OM {
    
        /*
         * Override the property "components" of super model interface
         */
        @Contravariance
        Reference<TabControl> getParentReference();
    }
}
