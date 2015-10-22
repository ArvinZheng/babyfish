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
package org.babyfish.association;

import org.babyfish.state.DisablityManageable;
import org.babyfish.state.LazinessManageable;

/**
 * @author Tao Chen
 */
public interface AssociatedEndpoint<O, T> extends DisablityManageable, LazinessManageable {
    
    O getOwner();
    
    boolean isSuspended();
    
    AssociatedEndpointType getEndpointType();
    
    AssociatedEndpointType getOppositeEndpointType();
    
    AssociatedEndpoint<T, O> getOppositeEndpoint(T opposite);
}
