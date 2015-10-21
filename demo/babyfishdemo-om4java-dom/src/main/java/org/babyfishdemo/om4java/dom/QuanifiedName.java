package org.babyfishdemo.om4java.dom;

import org.babyfish.lang.Nulls;
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.ObjectModelMode;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;

/**
 * Note: This ObjectModeMode of this class is EMBEDDABLE!
 * It is used to be scalar properties of other classes, 
 * not association properties.
 *
 * @author Tao Chen
 */
public class QuanifiedName {
    
    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(QuanifiedName quanifiedName) {
        return quanifiedName.om;
    }
    
    public QuanifiedName(String localName) {
        this(null, localName);
    }
    
    public QuanifiedName(String namespaceURI, String localName) {
        this.setNamespaceURI(namespaceURI);
        this.setLocalName(localName);
    }

    public String getNamespaceURI() {
        return this.om.getNamespaceURI();
    }

    public void setNamespaceURI(String namespaceURI) {
        this.om.setNamespaceURI(Nulls.emptyToNull(namespaceURI));
    }

    public String getLocalName() {
        return this.om.getLocalName();
    }

    public void setLocalName(String localName) {
        this.om.setLocalName(localName);
    }

    @Override
    public String toString() {
        return "{ namespaceURI: '" 
                + this.getNamespaceURI() + 
                "', localName: '" + 
                this.getLocalName() +
                "' }";
    }

    @ObjectModelDeclaration(
            mode = ObjectModelMode.EMBEDDABLE,
            declaredPropertiesOrder = "namespaceURI, localName"
    )
    private interface OM {
        
        @Scalar
        String getNamespaceURI();
        void setNamespaceURI(String namespaceURI);
        
        @Scalar
        String getLocalName();
        void setLocalName(String localName);
    }
}
