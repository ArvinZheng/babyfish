package org.babyfishdemo.om4java.dom;

import java.util.List;

import org.babyfish.collection.XList;
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.reference.IndexedReference;
import org.babyfish.validator.Validator;

/**
 * In w3c XML demo structure, Node should have 12 non-abstract derived classes: 
 * Element, Attr, Text, CDATASection, EntityReference, Entity, ProcessingInstruction, 
 * Comment, Document, DocumentType, DocumentFragment, Notation.
 * 
 * But, for this demo, in order to make the code to be more simple, Node ONLY 
 * has 4 non-abstract derived classes: Element, Attribute, Text and Comment
 *
 * @author Tao Chen
 */
public abstract class Node {
    
    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Node node) {
        return node.om;
    }
    
    protected Node() {
        /*
         * Specially, Node.OM.getChildNodes() can NOT use Attribute to be its element!
         * 
         * My time is not enough so that this functionality is not supported and the 
         * validation work temporarily must be implemented by your application.
         * 
         * In the future versions, this functionality can be implemented
         * by the babyfish itself and you need do nothing:)
         */
        
        /*
         * Be careful! the expression is 
         * "this.om.getChildNodes()" that can NOT be overridden,
         * NOT "that.getChildNodes()" that may be overridden by derived classes.
         */
        XList<Node> childNodes = (XList<Node>)this.om.getChildNodes();      
        childNodes.addValidator(new Validator<Node>() {
            @Override
            public void validate(Node e) {
                if (e instanceof Attribute) {
                    throw new IllegalArgumentException(
                            "Can not add the instanceof \""
                            + Attribute.class.getName()
                            + "\" into childNodes, \""
                            + Element.class.getName()
                            + "\" support its special behaviors \"getAttributes().put(?, ?)\""
                            + "and \"addAttribute(?)\" to do this work" 
                    );
                }
            }
        });
    }
    
    public int getIndex() {
        return this.om.getParentReference().getIndex();
    }
    
    public void setIndex(int index) {
        this.om.getParentReference().setIndex(index);
    }
    
    public Node getParent() {
        return this.om.getParentReference().get();
    }
    
    public void setParent(Node parent) {
        this.om.getParentReference().set(parent);
    }
    
    public List<Node> getChildNodes() {
        return this.om.getChildNodes();
    }
    
    public Node getFirstChild() {
        List<Node> childNodes = this.om.getChildNodes();
        return childNodes.isEmpty() ? null : childNodes.get(0);
    }
    
    public Node getLastChild() {
        List<Node> childNodes = this.om.getChildNodes();
        return childNodes.isEmpty() ? null : childNodes.get(childNodes.size() - 1);
    }
    
    public Node getPreviousSibling() {
        IndexedReference<Node> parentReference = this.om.getParentReference();
        int index = parentReference.getIndex();
        if (index < 0) {
            return null;
        }
        return parentReference.get().getChildNodes().get(index - 1);
    }
    
    public Node getNextSibling() {
        IndexedReference<Node> parentReference = this.om.getParentReference();
        int index = parentReference.getIndex();
        if (index == -1) {
            return null;
        }
        List<Node> siblings = parentReference.get().getChildNodes();
        if (index >= siblings.size() - 1) {
            return null;
        }
        return siblings.get(index + 1);
    }
    
    public abstract NodeType getNodeType();

    public abstract void accept(Visitor visitor);

    @ObjectModelDeclaration
    private interface OM {
        
        @Association(opposite = "childNodes")
        IndexedReference<Node> getParentReference();
        
        // In future versions, you can do: @Exclusive(Attribute.class)
        @Association(opposite = "parentReference")
        List<Node> getChildNodes();
    }
}
