package org.babyfishdemo.om4java.dom;

import org.babyfish.collection.XOrderedMap;
import org.babyfish.lang.Arguments;
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;

/**
 * @author Tao Chen
 */
public class Element extends Node {
    
    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Element element) {
        return element.om;
    }
    
    public QuanifiedName getQuanifiedName() {
        return this.om.getQuanifiedName();
    }
    
    public Element(String localName) {
        this.setQuanifiedName(new QuanifiedName(localName));
    }
    
    public Element(String namespaceURI, String localName) {
        this(new QuanifiedName(namespaceURI, localName));
    }
    
    public Element(QuanifiedName quanifiedName) {
        this.setQuanifiedName(quanifiedName);
    }
    
    public Element(String localName, Node ... childNodes) {
        this(new QuanifiedName(localName), childNodes);
    }
    
    public Element(String namespaceURI, String localName, Node ... childNodes) {
        this(new QuanifiedName(namespaceURI, localName), childNodes);
    }
    
    public Element(QuanifiedName quanifiedName, Node ... childNodes) {
        this.setQuanifiedName(quanifiedName);
        for (Node childNode : childNodes) {
            if (childNode instanceof Attribute) {
                this.addAttribute((Attribute)childNode);
            } else {
                this.getChildNodes().add(childNode);
            }
        }
    }

    public void setQuanifiedName(QuanifiedName quanifiedName) {
        if (quanifiedName == null) {
            quanifiedName = new QuanifiedName(null, null);
        }
        this.om.setQuanifiedName(quanifiedName);
    }

    public XOrderedMap<QuanifiedName, Attribute> getAttributes() {
        return this.om.getAttributes();
    }
    
    /*
     * Shortcut method of ".getAttributes.put(quanifiedName, attribute)"
     * when the attribute.quanifiedName is ALREADY assigned
     */
    public void addAttribute(Attribute attribute) {
        if (attribute != null) {
            Arguments.mustNotBeNull("attribute.quanifiedName", attribute.getQuanifiedName());
            this.getAttributes().put(attribute.getQuanifiedName(), attribute);
        }
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.ELEMENT;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitElement(this);
        for (Attribute attribute : this.getAttributes().values()) {
            attribute.accept(visitor);
        }
        if (visitor instanceof ChildScopeAwareVisitor) {
            ChildScopeAwareVisitor childScopeAwareVisitor = (ChildScopeAwareVisitor)visitor;
            childScopeAwareVisitor.enterChildScope(this);
            for (Node childNode : this.getChildNodes()) {
                childNode.accept(visitor);
            }
            childScopeAwareVisitor.leaveChildScope(this);
        } else {
            for (Node childNode : this.getChildNodes()) {
                childNode.accept(visitor);
            }
        }
    }

    @ObjectModelDeclaration
    private interface OM {
        
        @Scalar
        QuanifiedName getQuanifiedName();
        void setQuanifiedName(QuanifiedName quanifiedName);
        
        @Association(opposite = "parentReference")
        XOrderedMap<QuanifiedName, Attribute> getAttributes();
    }
}
