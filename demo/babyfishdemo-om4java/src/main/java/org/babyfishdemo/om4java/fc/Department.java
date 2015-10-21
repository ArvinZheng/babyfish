package org.babyfishdemo.om4java.fc;

import java.util.NavigableSet;

import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ReferenceComparisonRule;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
 
/**
 * @author Tao Chen
 */
public class Department {
 
    private static final ObjectModelFactory<OM> OM_FACTORY = ObjectModelFactoryFactory.factoryOf(OM.class);
        
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
    
    public NavigableSet<Employee> getEmployees() {
        return this.om.getEmployees();
    }
        
    @ObjectModelDeclaration
    private interface OM {
    
        @Scalar
        String getName();
        void setName(String name);
        
        /*
         * Use 
         * @ReferenceComparisonRule("firstName, lastName")
         * to specify the
         * "org.babyfish.collection.ForzenComparator" to this SortedSet
         * so that 
         * 
         * (1) This collection uses the property "firstName" and "lastName" 
         * to  compare two Employee objects. 
         * (2) The employee objects are consider as "Unstable Collection Elements" 
         * by this collection so that this collection will be adjusted automatically 
         * when the property "firstName" or "lastName" of those employee objects 
         * are changed by the program.
         * 
         * 
         * 
         * (A) @ReferenceComparisonRule can let BabyFish to generate byte-code in runtime
         * to create a new comparator class,
         * The comparator interface that's implemented by this new class is
         * "org.babyfish.collection.FronzenComparator<E>", 
         * not "org.babyfish.collection.FrozenEqualityComparator<E>" 
         * because the type of current property is 
         * compatible with "java.util.Sorted<E>".
         * 
         * (B) You can also write @ReferenceComparisonRule({ "firstName", "lastName" })
         * 
         * (C) This demo only show how to use @ReferenceComparisonRule on the Association-level,
         * the document has discussed how to use it on the Interface-level. 
         */
        @ReferenceComparisonRule("firstName, lastName")
        @Association(opposite = "departmentReference")
        NavigableSet<Employee> getEmployees();
    }
}
