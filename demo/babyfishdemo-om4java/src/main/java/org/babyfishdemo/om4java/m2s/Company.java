package org.babyfishdemo.om4java.m2s;

import java.util.Map;

import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
 
/**
 * @author Tao Chen
 */
public class Company {
 
    private static final ObjectModelFactory<OM> OM_FACTORY = ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Company company) {
        return company.om;
    }
    
    public String getName() {
        return this.om.getName();
    }
    
    public void setName(String name) {
        this.om.setName(name);
    }
    
    public Map<String, Investor> getInvestors() {
        return this.om.getInvestors();
    }
 
    @ObjectModelDeclaration
    private interface OM {
    
        @Scalar
        String getName();
        void setName(String name);
        
        @Association(opposite = "companys")
        Map<String, Investor> getInvestors();
    }
}
