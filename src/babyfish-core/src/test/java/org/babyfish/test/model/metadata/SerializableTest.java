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
package org.babyfish.test.model.metadata;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;

import junit.framework.Assert;

import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.AssociationProperty;
import org.babyfish.model.metadata.Metadatas;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.ObjectModelMetadata;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.ScalarProperty;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.reference.Reference;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class SerializableTest {
    
    @Test
    public void test() throws ClassNotFoundException, IOException {
        ObjectModelMetadata objectModelMetadata = Metadatas.of(Node.class);
        ScalarProperty name = objectModelMetadata.getScalarProperty("name");
        AssociationProperty childNodes = objectModelMetadata.getAssociationProperty("childNodes");
        AssociationProperty parentReference = objectModelMetadata.getAssociationProperty("parentReference");
        Assert.assertSame(objectModelMetadata, serializingClone(objectModelMetadata));
        Assert.assertSame(name, serializingClone(name));
        Assert.assertSame(childNodes, serializingClone(childNodes));
        Assert.assertSame(parentReference, serializingClone(parentReference));
    }
    
    @SuppressWarnings("unchecked")
    private static <T> T serializingClone(T obj) throws IOException, ClassNotFoundException {
        byte[] buf;
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
                ObjectOutputStream oout = new ObjectOutputStream(bout)) {
            oout.writeObject(obj);
            oout.flush();
            buf = bout.toByteArray();
        }
        try (ByteArrayInputStream bin = new ByteArrayInputStream(buf);
                ObjectInputStream oin = new ObjectInputStream(bin)) {
            return (T)oin.readObject();
        }
    }

    static class Node {
        
        private static final ObjectModelFactory<OM> OM_FACTORY =
                ObjectModelFactoryFactory.factoryOf(OM.class);
        
        private OM om = OM_FACTORY.create(this);
        
        @StaticMethodToGetObjectModel
        static OM om(Node node) {
            return node.om;
        }
        
        public String getName() {
            return this.om.getName();
        }

        public void setName(String name) {
            this.om.setName(name);
        }

        public Set<Node> getChildNodes() {
            return this.om.getChildNodes();
        }

        public Node getParent() {
            return this.om.getParentReference().get();
        }
        
        public void setParent(Node node) {
            this.om.getParentReference().set(node);
        }

        @ObjectModelDeclaration
        private interface OM {
            
            @Scalar
            String getName();
            void setName(String name);
            
            @Association(opposite = "parentReference")
            Set<Node> getChildNodes();
            
            @Association(opposite = "childNodes")
            Reference<Node> getParentReference();
        }
    }
    
}
