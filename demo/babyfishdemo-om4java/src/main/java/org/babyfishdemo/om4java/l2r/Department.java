package org.babyfishdemo.om4java.l2r;

import java.util.List;

import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;

/**
 * @author Tao Chen(&#38472;&#28059;)
 */
public class Department {

    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Department department) {
        return department.om;
    }
    
    public String getName() {
        return this.om.getName();
    }

    public void setName(String name) {
        this.om.setName(name);
    }

    public List<Employee> getEmployees() {
        return this.om.getEmployees();
    }

    @ObjectModelDeclaration
    private interface OM {
        
        @Scalar
        String getName();
        void setName(String name);
        
        @Association(opposite = "departmentReference")
        List<Employee> getEmployees();
    }
}
