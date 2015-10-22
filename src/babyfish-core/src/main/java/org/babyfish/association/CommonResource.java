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

import org.babyfish.collection.ReplacementRule;
import org.babyfish.reference.Reference;

/**
 * @author Tao Chen
 */
interface CommonResource {

    String invalidImplemententOfOnGetOppositeEndpointType(
            Class<?> thisType,
            Iterable<AssociatedEndpointType> validAssociatedEndpointTypes);
    
    String invalidImplemententOfOnGetOppositeEndpoint(
            Class<?> thisType,
            AssociatedEndpointType expectedOppositeEndpointType,
            AssociatedEndpointType actualOppositeEndpointType);
    
    String baseReplacementRuleMustBe(
            Class<?> thisType,
            ReplacementRule replacementRule);
    
    String invalidModificationWhenOppositeEndpointIs(
            Class<?> thisType,
            AssociatedEndpointType thisAssociatedEndpointType,
            AssociatedEndpointType ... unmodifableOppositeAssociatedEndpointTypes);
    
    @SuppressWarnings("rawtypes")
    String currentReferenceIsDisabled(
            Class<? extends Reference> thisType);
}
