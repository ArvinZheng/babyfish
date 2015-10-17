/*
 * BabyFish, Object Model Framework for Java and JPA.
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

import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.metadata.AssociationProperty;

/**
 * @author Tao Chen(&#38472;&#28059;)
 */
public interface AssociatedEndpointDescriptor {
    
    AssociationProperty getAssociationProperty();

    ObjectModelFactory<?> getOwnerObjectModelFactory();
    
    ObjectModelFactory<?> getKeyObjectModelFactory();
    
    ObjectModelFactory<?> getReturnObjectModelFactory();
}
