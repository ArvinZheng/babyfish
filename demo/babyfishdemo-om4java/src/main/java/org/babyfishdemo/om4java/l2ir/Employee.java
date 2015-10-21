package org.babyfishdemo.om4java.l2ir;

import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.reference.IndexedReference;
 
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
    
    public String getName() {
        return this.om.getName();
    }
    
    public void setName(String name) {
        this.om.setName(name);
    }
    
    public int getIndex() { //The position in the employees list of parent department, -1 means belong to no department
        return this.om.getDepartmentReference().getIndex();
    }
    
    public void setIndex(int index) {
        this.om.getDepartmentReference().setIndex(index);
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
        String getName();
        void setName(String name);
        
        @Association(opposite = "employees") // Refers Department.OM.getEmployees()
        IndexedReference<Department> getDepartmentReference();
    }
}
