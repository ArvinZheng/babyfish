package org.babyfishdemo.om4java.embeddable;

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
public class Customer {
    
    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Customer customer) {
        return customer.om;
    }

    public Contact getContact() {
        return this.om.getContact();
    }

    public void setContact(Contact contact) {
        this.om.setContact(contact);
    }
    
    public Consultant getConsultant() {
        return this.om.getConsultantReference().get();
    }
    
    public void setConsultant(Consultant consultant) {
        this.om.getConsultantReference().set(consultant);
    }

    @Override
    public String toString() {
        return String.format("{ contact: %s }", this.getContact());
    }

    @ObjectModelDeclaration
    private interface OM {
        
        @Scalar
        Contact getContact();
        void setContact(Contact contact);
        
        @Association(opposite = "customers")
        Reference<Consultant> getConsultantReference();
    }
}
