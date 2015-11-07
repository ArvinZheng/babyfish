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
package org.babyfish.test.association;

import java.util.Set;

import org.babyfish.collection.XNavigableSet;
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ReferenceComparisonRule;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.persistence.model.metadata.EntityId;
import org.babyfish.persistence.model.metadata.Inverse;
import org.babyfish.reference.Reference;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class ObjectModelNavigableSetTest {
    
    @Test
    public void testChangeId() {
        Node node = new Node();
        Node node1 = new Node();
        Node node2 = new Node();
        Node node3 = new Node();
        node1.setId(1L);
        node2.setId(2L);
        node3.setId(3L);
        node.getChildNodes().add(node1);
        node.getChildNodes().add(node2);
        node.getChildNodes().add(node3);
        
        assertChildNodeIds(node, 1L, 2L, 3L);
        Assert.assertSame(node, node1.getParentNode());
        Assert.assertSame(node, node2.getParentNode());
        Assert.assertSame(node, node3.getParentNode());
        
        node1.setId(-1L);
        assertChildNodeIds(node, -1L, 2L, 3L);
        Assert.assertSame(node, node1.getParentNode());
        Assert.assertSame(node, node2.getParentNode());
        Assert.assertSame(node, node3.getParentNode());
        
        node2.setId(-1L);
        assertChildNodeIds(node, -1L, 3L);
        Assert.assertSame(null, node1.getParentNode());
        Assert.assertSame(node, node2.getParentNode());
        Assert.assertSame(node, node3.getParentNode());
        
        node3.setId(-1L);
        assertChildNodeIds(node, -1L);
        Assert.assertSame(null, node1.getParentNode());
        Assert.assertSame(null, node2.getParentNode());
        Assert.assertSame(node, node3.getParentNode());
    }
    
    private static void assertChildNodeIds(Node node, Long ... ids) {
        Assert.assertEquals(ids.length, node.getChildNodes().size());
        int index = 0;
        for (Node childNode : node.getChildNodes()) {
            Assert.assertEquals(ids[index++], childNode.getId());
        }
    }

    static class Node {
        
        private static final ObjectModelFactory<OM> OM_FACTORY =
                ObjectModelFactoryFactory.factoryOf(OM.class);
        
        private OM om = OM_FACTORY.create(this);
        
        public Long getId() {
            return this.om.getId();
        }
        
        public void setId(Long id) {
            this.om.setId(id);
        }
        
        public Set<Node> getChildNodes() {
            return this.om.getChildNodes();
        }
        
        public Node getParentNode() {
            return this.om.getParentNodeReference().get();
        }
        
        public void setParentNode(Node parentNode) {
            this.om.getParentNodeReference().set(parentNode);
        }
        
        @StaticMethodToGetObjectModel
        static OM om(Node node) {
            return node.om;
        }
        
        @ObjectModelDeclaration(provider = "jpa")
        private interface OM {
            
            @EntityId
            Long getId();
            void setId(Long id);
        
            @ReferenceComparisonRule("id")
            @Association(opposite = "parentNodeReference")
            @Inverse
            XNavigableSet<Node> getChildNodes();
            
            @Association(opposite = "childNodes")
            Reference<Node> getParentNodeReference();
        }
    }
}
