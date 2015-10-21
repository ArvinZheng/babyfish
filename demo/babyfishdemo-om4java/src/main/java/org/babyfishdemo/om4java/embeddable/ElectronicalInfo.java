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
public class ElectronicalInfo {
    
    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(ElectronicalInfo electronicalInfo) {
        return electronicalInfo.om;
    }
    
    public ElectronicalInfo(String phone, String email) {
        this.setPhone(phone);
        this.setEmail(email);
    }

    public String getPhone() {
        return this.om.getPhone();
    }

    public void setPhone(String phone) {
        this.om.setPhone(phone);
    }

    public String getEmail() {
        return this.om.getEmail();
    }

    public void setEmail(String email) {
        this.om.setEmail(email);
    }

    @Override
    public String toString() {
        return String.format(
                "{ phone: %s, email: %s }", 
                this.getPhone(),
                this.getEmail()
        );
    }

    @ObjectModelDeclaration(
            mode = ObjectModelMode.EMBEDDABLE,
            declaredPropertiesOrder = "phone, email"
    )
    private interface OM {
        @Scalar
        String getPhone();
        void setPhone(String phone);
        
        @Scalar
        String getEmail();
        void setEmail(String email);
    }
}
