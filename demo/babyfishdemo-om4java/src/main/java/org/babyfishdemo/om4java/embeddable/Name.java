package org.babyfishdemo.om4java.embeddable;

import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.ObjectModelMode;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;

/**
 * @author Tao Chen
 */
public class Name {
    
    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Name name) {
        return name.om;
    }
    
    public Name(String first, String last) {
        this.setFirst(first);
        this.setLast(last);
    }

    public String getFirst() {
        return this.om.getFirst();
    }

    public void setFirst(String first) {
        this.om.setFirst(first);
    }

    public String getLast() {
        return this.om.getLast();
    }

    public void setLast(String last) {
        this.om.setLast(last);
    }

    @Override
    public String toString() {
        return String.format(
                "{ first: %s, last: %s }", 
                this.getFirst(),
                this.getLast()
        );
    }

    @ObjectModelDeclaration(
            mode = ObjectModelMode.EMBEDDABLE,
            declaredPropertiesOrder = "first, last"
    )
    private interface OM {
        
        @Scalar
        String getFirst();
        void setFirst(String first);
        
        @Scalar
        String getLast();
        void setLast(String last);
    }
}
