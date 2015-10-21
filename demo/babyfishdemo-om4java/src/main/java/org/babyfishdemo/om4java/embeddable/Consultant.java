package org.babyfishdemo.om4java.embeddable;

import java.util.NavigableSet;

import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.ReferenceComparisonRule;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;

/**
 * @author Tao Chen
 */
public class Consultant {
    
    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Consultant consultant) {
        return consultant.om;
    }
    
    public NavigableSet<Customer> getCustomers() {
        return this.om.getCustomers();
    }

    @ObjectModelDeclaration
    private interface OM {
        /*
         * In order to make the demo to be easy to learn,
         * I decided to let this association to be NavigableSet<E>
         * and use Customer.contact to generate its comparator. 
         */
        @Association(opposite = "consultantReference")
        @ReferenceComparisonRule("contact")
        NavigableSet<Customer> getCustomers();
    }
}
