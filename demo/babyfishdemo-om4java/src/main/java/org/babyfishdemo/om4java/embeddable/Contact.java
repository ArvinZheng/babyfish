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
public class Contact {
    
    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Contact contact) {
        return contact.om;
    }
    
    public Contact(Name name, ElectronicalInfo electronicalInfo) {
        this.setName(name);
        this.setElectronicalInfo(electronicalInfo);
    }

    public Name getName() {
        return this.om.getName();
    }

    public void setName(Name name) {
        this.om.setName(name);
    }

    public ElectronicalInfo getElectronicalInfo() {
        return this.om.getElectronicalInfo();
    }

    public void setElectronicalInfo(ElectronicalInfo electronicalInfo) {
        this.om.setElectronicalInfo(electronicalInfo);
    }

    @Override
    public String toString() {
        return String.format(
                "{ name: %s, electronicalInfo: %s }",
                this.getName(),
                this.getElectronicalInfo()
        );
    }
    
    @ObjectModelDeclaration(
            mode = ObjectModelMode.EMBEDDABLE,
            declaredPropertiesOrder = "name, electronicalInfo"
    )
    private interface OM {
    
        @Scalar
        Name getName();
        void setName(Name name);
        
        @Scalar
        ElectronicalInfo getElectronicalInfo();
        void setElectronicalInfo(ElectronicalInfo electronicalInfo);
    }
}
