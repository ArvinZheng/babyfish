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
package org.babyfish.test.model.embedded;

import java.util.Comparator;

import junit.framework.Assert;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Metadatas;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.ObjectModelMode;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class NullScalarTest {

    private static final EqualityComparator<Node> EQUALITY_COMPARATOR = 
            Metadatas.of(Node.class).getOwnerEqualityComparator();
    
    private static final Comparator<Node> COMPARATOR = 
            Metadatas.of(Node.class).getOwnerComparator();
    
    @Test
    public void testEq() {
        Node node1 = new Node(new Node(new Node(null)));
        Node node2 = new Node(new Node(new Node(null)));
        Assert.assertTrue(EQUALITY_COMPARATOR.hashCode(node1) == EQUALITY_COMPARATOR.hashCode(node2));
        Assert.assertTrue(EQUALITY_COMPARATOR.equals(node1, node2));
        Assert.assertTrue(COMPARATOR.compare(node1, node2) == 0);
    }
    
    @Test
    public void testLt() {
        Node node1 = new Node(new Node(new Node(null)));
        Node node2 = new Node(new Node(new Node(new Node(null))));
        Assert.assertTrue(EQUALITY_COMPARATOR.hashCode(node1) != EQUALITY_COMPARATOR.hashCode(node2));
        Assert.assertTrue(!EQUALITY_COMPARATOR.equals(node1, node2));
        Assert.assertTrue(COMPARATOR.compare(node1, node2) < 0);
        Assert.assertTrue(COMPARATOR.compare(node2, node1) > 0);
    }
    
    @Test
    public void testGt() {
        Node node1 = new Node(new Node(new Node(new Node(null))));
        Node node2 = new Node(new Node(new Node(null)));
        Assert.assertTrue(EQUALITY_COMPARATOR.hashCode(node1) != EQUALITY_COMPARATOR.hashCode(node2));
        Assert.assertTrue(!EQUALITY_COMPARATOR.equals(node1, node2));
        Assert.assertTrue(COMPARATOR.compare(node1, node2) > 0);
        Assert.assertTrue(COMPARATOR.compare(node2, node1) < 0);
    }
    
    static class Node {
        
        private static final ObjectModelFactory<OM> OM_FACTORY = 
                ObjectModelFactoryFactory.factoryOf(OM.class);
        
        private OM om = OM_FACTORY.create(this);
    
        public Node(Node nextNode) {
            this.setNextNode(nextNode);
        }
        
        public Node getNextNode() {
            return om.getNextNode();
        }

        public void setNextNode(Node nextNode) {
            om.setNextNode(nextNode);
        }

        @StaticMethodToGetObjectModel
        static OM om(Node nextNode) {
            return nextNode.om;
        }
        
        @ObjectModelDeclaration(mode = ObjectModelMode.EMBEDDABLE)
        private interface OM {
            
            @Scalar
            Node getNextNode();
            void setNextNode(Node nextNode);
        }
    }
}
