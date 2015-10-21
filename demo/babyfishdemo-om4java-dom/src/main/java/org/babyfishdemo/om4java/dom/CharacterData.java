package org.babyfishdemo.om4java.dom;

import java.util.List;

import org.babyfish.collection.MACollections;
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;

/**
 * @author Tao Chen
 */
public abstract class CharacterData extends Node {
    
    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(CharacterData text) {
        return text.om;
    }
    
    public CharacterData(String data) {
        this.setData(data);
    }

    public String getData() {
        return this.om.getData();
    }

    public void setData(String data) {
        this.om.setData(data);
    }
    
    @Override
    public List<Node> getChildNodes() {
        return MACollections.emptyList();
    }

    @ObjectModelDeclaration
    private interface OM {
        
        @Scalar
        String getData();
        void setData(String data);
    }
}
