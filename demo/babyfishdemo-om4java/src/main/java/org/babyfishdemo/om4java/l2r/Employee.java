package org.babyfishdemo.om4java.l2r;

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

    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Employee employee) {
        return employee.om;
    }
    
    public Employee(String name) {
        this.setName(name);
    }
    
    public String getName() {
        return this.om.getName();
    }
    
    public void setName(String name) {
        this.om.setName(name);
    }
    
    public Department getDepartment() {
        return this.om.getDepartmentReference().get();
    }
    
    /*
     * The bidirectional association is "List-Reference", not "List-IndexedReference",
     * so this setter of reference side can only be used to remove relationships by null argument,
     * only the opposite-side "Department.getEmployees()" can be used to create relationships.
     * Trying to use this setter to set non-null value can cause exception.
     */
    public void setDepartment(Department department) {
        this.om.getDepartmentReference().set(department);
    }
    
    @ObjectModelDeclaration
    private interface OM {
        
        @Scalar
        String getName();
        void setName(String name);
        
        @Association(opposite = "employees")
        Reference<Department> getDepartmentReference();
    }
}
