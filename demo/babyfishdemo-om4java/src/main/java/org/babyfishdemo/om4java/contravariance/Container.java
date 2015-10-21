package org.babyfishdemo.om4java.contravariance;

import java.util.List;
 
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
 
/**
 * @author Tao Chen
 */
public class Container extends Component {
    
    private static final ObjectModelFactory<OM> OM_FACTORY = ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Container container) {
        return container.om;
    }
    
    public List<Component> getComponents() {
        return this.om.getComponents();
    }
    
    @ObjectModelDeclaration
    private interface OM {
        
        @Association(opposite = "parentReference")
        List<Component> getComponents();
    }
}
