package org.babyfishdemo.om4java.dom;

import junit.framework.Assert;

import org.babyfish.model.ObjectModel;
import org.babyfish.model.metadata.AssociationProperty;
import org.babyfish.model.metadata.Metadatas;
import org.babyfish.model.metadata.ObjectModelMetadata;
import org.babyfish.modificationaware.event.ModificationEventHandleException;
import org.babyfish.reference.IndexedReference;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class ValidatorTest {

    @Test(expected = IllegalArgumentException.class)
    public void testValidatorOfNodeOMChildNodes() {
        /*
         * The constructor Node(), a validator is applied on
         * Node.OM.getChildNodes(), it can not retain the 
         * Attribute as child node.
         * 
         * That means: you can not use "element.getChildNodes().add(attribute)".
         * You must use 
         * "element.getAttributes().put(quanifiedName, attribute)"
         * or
         * "element.addAttribute(attribute)"
         * 
         * (Now this validation work must be done by you because 
         * my time is not enough, in the future version, this functionality 
         * may be embedded in the framework and need only add annotation)
         */
        new Element("div").getChildNodes().add(new Attribute("style", "margin:0px;padding:10px;"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testValidatorOfNodeOMParentReference() {
        /*
         * The constructor Node(), a validator is applied on
         * Node.OM.getParentReference(), it can not retain the 
         * Node as parent node when the current owner is
         * "Attribute".
         * 
         * The "Attribute.setParent()" that's base on "Attribute.OM.parentReference" 
         * hides the "Node.setParent()" that's base on "Node.OM.getParentReference".
         * so we can not use "Node.OM.parentReference()" of an Attribute object by simple way.
         * Fortunately, we can use the ObjectModel API to do it.
         * 
         * (Now this validation work must be done by you because 
         * my time is not enough, in the future version, this functionality 
         * may be embedded in the framework and need only add annotation)
         */
        
        ObjectModelMetadata objectModelMetadata = Metadatas.of(Node.class);
        // This is "Node.OM.paretentReference", not "Attribute.OM.parentReference"
        AssociationProperty parentReferenceProperty = objectModelMetadata.getDeclaredAssociationProperty("parentReference");
        
        Attribute attribute = new Attribute("style", "margin:0px;padding:10px");
        ObjectModel om = (ObjectModel)objectModelMetadata.getFactory().get(attribute);
        IndexedReference<Node> parentReference = (IndexedReference<Node>)om.getAssociation(parentReferenceProperty.getId());
        
        try {
            parentReference.set(0, new Element("div"));
        } catch (ModificationEventHandleException ex) {
            return;
        }
        
        // Don't use the "@Test(expected = UnsupportedOperationException.class)" because
        // I want to show you the exception is thrown by "parentReference.set(?)",
        // not by other statements.
        Assert.fail("The Node.OM.parentReference is not supported by Attribute, an exception should be happend");
    }
}
