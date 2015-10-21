package org.babyfishdemo.om4java.contravariance;

import java.util.List;
 
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Contravariance;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
 
/**
 * @author Tao Chen
 */
public class TabControl extends Container {
 
    private static final ObjectModelFactory<OM> OM_FACTORY = ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(TabControl tabControl) {
        return tabControl.om;
    }
    
    public List<TabPage> getTabPages() {
        return this.om.getTabPages();
    }
    
    @ObjectModelDeclaration
    private interface OM {
    
        /*
         * Override the property "components" of super model interface
         */
        @Contravariance("components")
        List<TabPage> getTabPages();
    }
}
