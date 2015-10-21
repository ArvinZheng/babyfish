package org.babyfishdemo.om4java.fec;

import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.reference.Reference;
 
/**
 * @author Tao Chen
 */
public class Employee {
 
    private static final ObjectModelFactory<OM> OM_FACTORY = ObjectModelFactoryFactory.factoryOf(OM.class);
        
    private OM om = OM_FACTORY.create(this);
        
    @StaticMethodToGetObjectModel
    static OM om(Employee employee) {
        return employee.om;
    }
    
    public String getFirstName() {
        return this.om.getFirstName();
    }
    
    public void setFirstName(String firstName) {
        this.om.setFirstName(firstName);
    }
    
    public String getLastName() {
        return this.om.getLastName();
    }
    
    public void setLastName(String lastName) {
        this.om.setLastName(lastName);
    }
    
    public Department getDepartment() {
        return this.om.getDepartmentReference().get();
    }
    
    public void setDepartment(Department department) {
        this.om.getDepartmentReference().set(department);
    }
 
    @ObjectModelDeclaration
    private interface OM {
    
        @Scalar
        String getFirstName();
        void setFirstName(String firstName);
        
        @Scalar
        String getLastName();
        void setLastName(String lastName);
        
        @Association(opposite = "employees")
        Reference<Department> getDepartmentReference();
    }
}
