package org.babyfishdemo.om4java.contravariance;

import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
 
/**
 * @author Tao Chen
 */
public class Button extends Component {
 
    private static final ObjectModelFactory<OM> OM_FACTORY = ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Button button) {
        return button.om;
    }
    
    public String getText() {
        return this.om.getText();
    }
    
    public void setText(String text) {
        this.om.setText(text);
    }
    
    @ObjectModelDeclaration
    private interface OM {
        
        @Scalar
        String getText();
        void setText(String text);
    }
}
