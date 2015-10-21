package org.babyfishdemo.om4java.l2s;

import java.util.Set;

import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
 
/**
 * @author Tao Chen
 */
public class Investor {
 
    private static final ObjectModelFactory<OM> OM_FACTORY = ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Investor investor) {
        return investor.om;
    }
    
    public String getName() {
        return this.om.getName();
    }
    
    public void setName(String name) {
        this.om.setName(name);
    }
    
    public Set<Company> getCompanys() {
        return this.om.getCompanys();
    }
 
    @ObjectModelDeclaration
    private interface OM {
    
        @Scalar
        String getName();
        void setName(String name);
        
        @Association(opposite = "investors")
        Set<Company> getCompanys();
    }
}
