package org.babyfishdemo.om4java.dom;

import java.util.List;

import org.babyfish.collection.MACollections;
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.reference.KeyedReference;

/**
 * @author Tao Chen
 */
public class Attribute extends Node {

    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Attribute attribute) {
        return attribute.om;
    }
    
    public Attribute(String value) {
        this.setValue(value);
    }
    
    public Attribute(String localName, String value) {
        this(null, localName, value);
    }
    
    public Attribute(String namespaceURI, String localName, String value) {
        this(new QuanifiedName(namespaceURI, localName), value);
    }
    
    public Attribute(QuanifiedName quanifiedName, String value) {
        this.setQuanifiedName(quanifiedName);
        this.setValue(value);
    }
    
    public QuanifiedName getQuanifiedName() {
        // The parameter true means return the key even if the parent is null
        // otherwise, means return null when the parent is null
        return this.om.getParentReference().getKey(true);
    }
    
    public void setQuanifiedName(QuanifiedName quanifiedName) {
        if (quanifiedName == null) {
            quanifiedName = new QuanifiedName(null, null);
        }
        this.om.getParentReference().setKey(quanifiedName);
    }
    
    public String getValue() {
        return this.om.getValue();
    }
    
    public void setValue(String value) {
        this.om.setValue(value);
    }
    
    @Override
    public Element getParent() {
        return this.om.getParentReference().get();
    }
    
    public void setParent(Element parent) {
        this.om.getParentReference().set(parent);
    }
    
    @Override
    public final void setParent(Node parent) {
        this.setParent((Element)parent);
    }

    @Override
    public int getIndex() {
        Element element = this.getParent();
        if (element == null) {
            return -1;
        }
        int index = 0;
        // element.getAttributes() is XOrderedMap, NOT Map.
        for (Attribute attribute : element.getAttributes().values()) {
            if (attribute == this) {
                return index;
            }
            index++;
        }
        throw new AssertionError("Absolutely impssible");
    }

    @Deprecated
    @Override
    public final void setIndex(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Node> getChildNodes() {
        return MACollections.emptyList();
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.ATTRIBUTE;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAttribute(this);
    }

    @ObjectModelDeclaration
    private interface OM {
    
        @Scalar
        String getValue();
        void setValue(String value);
        
        // Hide Node.OM.getParentReference, never use it.
        @Association(opposite = "attributes")
        KeyedReference<QuanifiedName, Element> getParentReference();
    }
}
