package org.babyfishdemo.om4java.m2r;

import java.util.Map;

import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
 
/**
 * @author Tao Chen
 */
public class Airplane {
 
    private static final ObjectModelFactory<OM> OM_FACTORY = ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Airplane airplane) {
        return airplane.om;
    }
    
    public Map<String, Engine> getEngines() {
        return this.om.getEngines();
    }
 
    @ObjectModelDeclaration
    private interface OM {
    
        @Association(opposite = "airplaneReference")
        Map<String, Engine> getEngines();
    }
}
