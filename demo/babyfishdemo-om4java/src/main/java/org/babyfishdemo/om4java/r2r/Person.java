package org.babyfishdemo.om4java.r2r;

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
public class Person {
 
    private static final ObjectModelFactory<OM> OM_FACTORY =
        ObjectModelFactoryFactory.factoryOf(OM.class);
        
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Person person) {
        return person.om;
    }
    
    public Person(String name) {
        this.setName(name);
    }
    
    public String getName() {
        return this.om.getName();
    }
    
    public void setName(String name) {
        this.om.setName(name);
    }
    
    public Person getSpouse() {
        return this.om.getSpouseReference().get();
    }
    
    public void setSpouse(Person spouse) {
        if (spouse == this) {
            throw new IllegalArgumentException("A person can not be the spouse of itself");
        }
        this.om.getSpouseReference().set(spouse);
    }
 
    @ObjectModelDeclaration
    private interface OM {
    
        @Scalar
        String getName();
        void setName(String name);
        
        @Association(opposite = "spouseReference")
        Reference<Person> getSpouseReference();
    }
}
