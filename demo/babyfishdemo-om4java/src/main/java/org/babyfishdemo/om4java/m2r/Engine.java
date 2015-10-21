package org.babyfishdemo.om4java.m2r;

import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.reference.Reference;
 
/**
 * @author Tao Chen
 */
public class Engine {
 
    private static final ObjectModelFactory<OM> OM_FACTORY = ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Engine engine) {
        return engine.om;
    }
    
    public Airplane getAirplane() {
        return this.om.getAirplaneReference().get();
    }
    
    /*
     * The bidirectional association is "Map-Reference", not "Map-KeyedReference",
     * so this setter of reference side can only be used to remove relationships by null argument,
     * only the opposite-side "Airplane.getEngines()" can be used to create relationships.
     * Trying to use this setter to set non-null value can cause exception.
     */
    public void setAirplane(Airplane airplane) {
        this.om.getAirplaneReference().set(airplane);
    }
 
    @ObjectModelDeclaration
    private interface OM {
    
        @Association(opposite = "engines")
        Reference<Airplane> getAirplaneReference();
    }
}
