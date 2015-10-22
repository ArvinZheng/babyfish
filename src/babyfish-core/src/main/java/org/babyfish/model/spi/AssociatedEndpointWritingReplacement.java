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
package org.babyfish.model.spi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

import org.babyfish.association.AssociatedEndpoint;
import org.babyfish.lang.Arguments;
import org.babyfish.model.ObjectModel;
import org.babyfish.model.ObjectModelFactory;

/**
 * @author Tao Chen
 */
public final class AssociatedEndpointWritingReplacement implements Serializable {

    private static final long serialVersionUID = 5436612985087546448L;
    
    private AssociatedEndpoint<?, ?> target;
    
    public AssociatedEndpointWritingReplacement(AssociatedEndpoint<?, ?> endpoint) {
        this.target =
                Arguments.mustBeInstanceOfValue(
                        "endpoint",
                        Arguments.mustNotBeNull("endpoint", endpoint),
                        AssociatedEndpointDescriptor.class);
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        AssociatedEndpoint<?, ?> target = this.target;
        AssociatedEndpointDescriptor desc = (AssociatedEndpointDescriptor)target;
        out.writeObject(desc.getOwnerObjectModelFactory());
        out.writeObject(target.getOwner());
        out.writeInt(desc.getAssociationProperty().getId());
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        ObjectModelFactory<?> objectModelFactory = (ObjectModelFactory<?>)in.readObject();
        Object owner = in.readObject();
        int propertyId = in.readInt();
        this.target = ((ObjectModel)objectModelFactory.get(owner)).getAssociation(propertyId);
    }
    
    protected Object readResolve() throws ObjectStreamException {
        return this.target;
    }
    
}
