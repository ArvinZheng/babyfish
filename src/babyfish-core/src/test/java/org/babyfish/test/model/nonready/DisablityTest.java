/*
 * BabyFish, Object Model Framework for Java and JPA.
 * https://github.com/babyfish-ct/babyfish
 *
 * Copyright (c) 2008-2015, Tao Chen
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * Please visit "http://opensource.org/licenses/LGPL-3.0" to know more.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 */
package org.babyfish.test.model.nonready;

import java.util.Set;

import junit.framework.Assert;

import org.babyfish.model.ObjectModel;
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.AllowDisability;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.Contravariance;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.reference.Reference;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class DisablityTest {
    
    @Test
    public void testImplicitlyEnable() {
        Person person = new Person();
        person.disableAddress();
        Assert.assertTrue(person.isAddressDisabled());
        person.setAddress("Unknown");
        Assert.assertFalse(person.isAddressDisabled());
        Assert.assertEquals("Unknown", person.getAddress());
    }
    
    @Test
    public void testExplicitlyEnable() {
        Person person = new Person();
        person.disableAddress();
        Assert.assertTrue(person.isAddressDisabled());
        person.enableAddress();
        Assert.assertFalse(person.isAddressDisabled());
        Assert.assertNull(person.getAddress());
    }
    
    @Test
    public void testImplicitlyEnableByOMAPI() {
        Person person = new Person();
        person.disableAddress();
        Assert.assertTrue(Person.om(person).isDisabled(Person.ADDRESS));
        Person.om(person).setScalar(Person.ADDRESS, "Unknown");
        Assert.assertFalse(Person.om(person).isDisabled(Person.ADDRESS));
        Assert.assertEquals("Unknown", Person.om(person).getScalar(Person.ADDRESS));
    }
    
    @Test
    public void testExplicitlyEnableByOMAPI() {
        Person person = new Person();
        person.disableAddress();
        Assert.assertTrue(Person.om(person).isDisabled(Person.ADDRESS));
        Person.om(person).enable(Person.ADDRESS);
        Assert.assertFalse(Person.om(person).isDisabled(Person.ADDRESS));
        Assert.assertNull(Person.om(person).getScalar(Person.ADDRESS));
    }
    
    @Test
    public void testContravariance() {
        Assert.assertTrue(XParentNode.CHILD_NODES != XParentNode.X_CHILD_NODES);
        Assert.assertTrue(XChildNode.PARENT_NODE_REFERENCE != XChildNode.X_PARENT_NODE_REFERENCE);
    }
    
    @Test
    public void testEnableAssociationByParentNode() {
        
        XParentNode parentNode = new XParentNode();
        XChildNode childNode = new XChildNode();
        parentNode.om.disable(ParentNode.CHILD_NODES);
        childNode.om.disable(ChildNode.PARENT_NODE_REFERENCE);
        
        Assert.assertTrue(parentNode.om.isDisabled(ParentNode.CHILD_NODES));
        Assert.assertTrue(parentNode.xom.isDisabled(ParentNode.CHILD_NODES));
        Assert.assertTrue(parentNode.xom.isDisabled(XParentNode.X_CHILD_NODES));
        Assert.assertTrue(childNode.om.isDisabled(ChildNode.PARENT_NODE_REFERENCE));
        Assert.assertTrue(childNode.xom.isDisabled(ChildNode.PARENT_NODE_REFERENCE));
        Assert.assertTrue(childNode.xom.isDisabled(XChildNode.X_PARENT_NODE_REFERENCE));
        
        parentNode.getChildNodes().add(childNode);
        
        Assert.assertFalse(parentNode.om.isDisabled(ParentNode.CHILD_NODES));
        Assert.assertFalse(parentNode.xom.isDisabled(ParentNode.CHILD_NODES));
        Assert.assertFalse(parentNode.xom.isDisabled(XParentNode.X_CHILD_NODES));
        Assert.assertTrue(childNode.om.isDisabled(ChildNode.PARENT_NODE_REFERENCE));
        Assert.assertTrue(childNode.xom.isDisabled(ChildNode.PARENT_NODE_REFERENCE));
        Assert.assertTrue(childNode.xom.isDisabled(XChildNode.X_PARENT_NODE_REFERENCE));
    }
    
    @Test
    public void testEnableAssociationByXParentNode() {
        
        XParentNode parentNode = new XParentNode();
        XChildNode childNode = new XChildNode();
        parentNode.om.disable(ParentNode.CHILD_NODES);
        childNode.om.disable(ChildNode.PARENT_NODE_REFERENCE);
        
        Assert.assertTrue(parentNode.om.isDisabled(ParentNode.CHILD_NODES));
        Assert.assertTrue(parentNode.xom.isDisabled(ParentNode.CHILD_NODES));
        Assert.assertTrue(parentNode.xom.isDisabled(XParentNode.X_CHILD_NODES));
        Assert.assertTrue(childNode.om.isDisabled(ChildNode.PARENT_NODE_REFERENCE));
        Assert.assertTrue(childNode.xom.isDisabled(ChildNode.PARENT_NODE_REFERENCE));
        Assert.assertTrue(childNode.xom.isDisabled(XChildNode.X_PARENT_NODE_REFERENCE));
        
        parentNode.getXChildNodes().add(childNode);
        
        Assert.assertFalse(parentNode.om.isDisabled(ParentNode.CHILD_NODES));
        Assert.assertFalse(parentNode.xom.isDisabled(ParentNode.CHILD_NODES));
        Assert.assertFalse(parentNode.xom.isDisabled(XParentNode.X_CHILD_NODES));
        Assert.assertTrue(childNode.om.isDisabled(ChildNode.PARENT_NODE_REFERENCE));
        Assert.assertTrue(childNode.xom.isDisabled(ChildNode.PARENT_NODE_REFERENCE));
        Assert.assertTrue(childNode.xom.isDisabled(XChildNode.X_PARENT_NODE_REFERENCE));
    }
    
    @Test
    public void testEnableAssociationByChildNode() {
        
        XParentNode parentNode = new XParentNode();
        XChildNode childNode = new XChildNode();
        parentNode.om.disable(ParentNode.CHILD_NODES);
        childNode.om.disable(ChildNode.PARENT_NODE_REFERENCE);
        
        Assert.assertTrue(parentNode.om.isDisabled(ParentNode.CHILD_NODES));
        Assert.assertTrue(parentNode.xom.isDisabled(ParentNode.CHILD_NODES));
        Assert.assertTrue(parentNode.xom.isDisabled(XParentNode.X_CHILD_NODES));
        Assert.assertTrue(childNode.om.isDisabled(ChildNode.PARENT_NODE_REFERENCE));
        Assert.assertTrue(childNode.xom.isDisabled(ChildNode.PARENT_NODE_REFERENCE));
        Assert.assertTrue(childNode.xom.isDisabled(XChildNode.X_PARENT_NODE_REFERENCE));
        
        childNode.setParentNode(parentNode);
        
        Assert.assertTrue(parentNode.om.isDisabled(ParentNode.CHILD_NODES));
        Assert.assertTrue(parentNode.xom.isDisabled(ParentNode.CHILD_NODES));
        Assert.assertTrue(parentNode.xom.isDisabled(XParentNode.X_CHILD_NODES));
        Assert.assertFalse(childNode.om.isDisabled(ChildNode.PARENT_NODE_REFERENCE));
        Assert.assertFalse(childNode.xom.isDisabled(ChildNode.PARENT_NODE_REFERENCE));
        Assert.assertFalse(childNode.xom.isDisabled(XChildNode.X_PARENT_NODE_REFERENCE));
    }
    
    @Test
    public void testEnableAssociationByXChildNode() {
        
        XParentNode parentNode = new XParentNode();
        XChildNode childNode = new XChildNode();
        parentNode.om.disable(ParentNode.CHILD_NODES);
        childNode.om.disable(ChildNode.PARENT_NODE_REFERENCE);
        
        Assert.assertTrue(parentNode.om.isDisabled(ParentNode.CHILD_NODES));
        Assert.assertTrue(parentNode.xom.isDisabled(ParentNode.CHILD_NODES));
        Assert.assertTrue(parentNode.xom.isDisabled(XParentNode.X_CHILD_NODES));
        Assert.assertTrue(childNode.om.isDisabled(ChildNode.PARENT_NODE_REFERENCE));
        Assert.assertTrue(childNode.xom.isDisabled(ChildNode.PARENT_NODE_REFERENCE));
        Assert.assertTrue(childNode.xom.isDisabled(XChildNode.X_PARENT_NODE_REFERENCE));
        
        childNode.setXParentNode(parentNode);
        
        Assert.assertTrue(parentNode.om.isDisabled(ParentNode.CHILD_NODES));
        Assert.assertTrue(parentNode.xom.isDisabled(ParentNode.CHILD_NODES));
        Assert.assertTrue(parentNode.xom.isDisabled(XParentNode.X_CHILD_NODES));
        Assert.assertFalse(childNode.om.isDisabled(ChildNode.PARENT_NODE_REFERENCE));
        Assert.assertFalse(childNode.xom.isDisabled(ChildNode.PARENT_NODE_REFERENCE));
        Assert.assertFalse(childNode.xom.isDisabled(XChildNode.X_PARENT_NODE_REFERENCE));
    }
    
    static class Person {
        
        private static final ObjectModelFactory<OM> OM_FACTORY =
                ObjectModelFactoryFactory.factoryOf(OM.class);
        
        private OM om = OM_FACTORY.create(this);

        private static final int ADDRESS = OM_FACTORY.getObjectModelMetadata().getDeclaredProperty("address").getId();
        
        @StaticMethodToGetObjectModel
        static OM om(Person person) {
            return person.om;
        }
        
        public String getName() {
            return om.getName();
        }

        public void setName(String name) {
            om.setName(name);
        }

        public String getAddress() {
            return om.getAddress();
        }

        public void setAddress(String address) {
            om.setAddress(address);
        }
        
        public boolean isAddressDisabled() {
            return om.isDisabled(Person.ADDRESS);
        }
        
        public void disableAddress() {
            om.disable(Person.ADDRESS);
        }
        
        public void enableAddress() {
            om.enable(Person.ADDRESS);
        }

        @ObjectModelDeclaration
        @AllowDisability
        private interface OM extends ObjectModel {
            
            @Scalar
            String getName();
            void setName(String name);
            
            @Scalar
            String getAddress();
            void setAddress(String address);
        }
    }
    
    static class ParentNode {
        
        private static final ObjectModelFactory<OM> OM_FACTORY = 
                ObjectModelFactoryFactory.factoryOf(OM.class);
        
        public static final int CHILD_NODES = 
                OM_FACTORY
                .getObjectModelMetadata()
                .getDeclaredProperty("childNodes")
                .getId();
        
        OM om = OM_FACTORY.create(this);
        
        @StaticMethodToGetObjectModel
        static OM om(ParentNode parentNode) {
            return parentNode.om;
        }
        
        public Set<ChildNode> getChildNodes() {
            return om.getChildNodes();
        }
        
        @ObjectModelDeclaration
        @AllowDisability
        private interface OM extends ObjectModel {
            @Association(opposite = "parentNodeReference")
            Set<ChildNode> getChildNodes();
        }
    }
    
    static class ChildNode {
        
        private static final ObjectModelFactory<OM> OM_FACTORY = 
                ObjectModelFactoryFactory.factoryOf(OM.class);
        
        public static final int PARENT_NODE_REFERENCE = 
                OM_FACTORY
                .getObjectModelMetadata()
                .getDeclaredProperty("parentNodeReference")
                .getId();
        
        OM om = OM_FACTORY.create(this);
        
        @StaticMethodToGetObjectModel
        static OM om(ChildNode childNode) {
            return childNode.om;
        }
        
        public ParentNode getParentNode() {
            return om.getParentNodeReference().get();
        }
        
        public void setParentNode(ParentNode parentNode) {
            om.getParentNodeReference().set(parentNode);
        }
        
        @ObjectModelDeclaration
        @AllowDisability
        private interface OM extends ObjectModel {
            @Association(opposite = "childNodes")
            Reference<ParentNode> getParentNodeReference();
        }
    }
    
    static class XParentNode extends ParentNode {
        
        private static final ObjectModelFactory<OM> OM_FACTORY = 
                ObjectModelFactoryFactory.factoryOf(OM.class);
        
        public static final int X_CHILD_NODES = 
                OM_FACTORY
                .getObjectModelMetadata()
                .getDeclaredProperty("xChildNodes")
                .getId();
        
        OM xom = OM_FACTORY.create(this);
        
        @StaticMethodToGetObjectModel
        static OM om(XParentNode xParentNode) {
            return xParentNode.xom;
        }
        
        public Set<XChildNode> getXChildNodes() {
            return xom.getXChildNodes();
        }

        @ObjectModelDeclaration
        private interface OM extends ObjectModel {
            
            @Contravariance("childNodes")
            Set<XChildNode> getXChildNodes();
        }
    }
    
    static class XChildNode extends ChildNode {
        
        private static final ObjectModelFactory<OM> OM_FACTORY = 
                ObjectModelFactoryFactory.factoryOf(OM.class);
        
        public static final int X_PARENT_NODE_REFERENCE = 
                OM_FACTORY
                .getObjectModelMetadata()
                .getDeclaredProperty("xParentNodeReference")
                .getId();  
        
        private OM xom = OM_FACTORY.create(this);
        
        @StaticMethodToGetObjectModel
        static OM om(XChildNode xChildNode) {
            return xChildNode.xom;
        }
        
        public XParentNode getXParentNode() {
            return xom.getXParentNodeReference().get();
        }
        
        public void setXParentNode(XParentNode parentNode) {
            xom.getXParentNodeReference().set(parentNode);
        }
        
        @ObjectModelDeclaration
        private interface OM extends ObjectModel {
            
            @Contravariance("parentNodeReference")
            Reference<XParentNode> getXParentNodeReference();
        }
        
        
    }
}
