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
package org.babyfish.collection.spi.wrapper;

import org.babyfish.modificationaware.ModificationAware;
import org.babyfish.view.View;

/**
 * @author Tao Chen
 */
interface CommonResource {
    
    String illegalViewInfo();
    
    String createRootDataMustReturnNonNull(Class<?> runtimeType);
    
    String createBaseViewMustReturnView(Class<?> runtimeType, Class<View> viewType);
    
    String createEventDispatcherMustReturnNonNull(Class<?> runtimeType);
    
    String createDefaultBaseMustReturnNonNull(Class<?> runtimeType);
    
    String ownerOfReturnedValueOfCreateEventDispatcherMustBeThis(Class<?> runtimeType);
    
    String concurrentModification();
    
    String invokeGetRootOwnerTooEarlySoThatTheRootOwnerIsNull();

    String canNotSetNullBaseWhenTheRootDataIsContructorOnlyRootData(Class<ConstructOnlyRootData> constructorOnlyRootDataType);

    String canNotSetBaseTwiceWhenTheRootDataIsSetOnceOnlyRootData(Class<SetOnceOnlyRootData> setOnceOnlyRootDataType);

    String canNotSetBaseDuringSerializing();
    
    String mustSetTheBaseInContstructorWhenTheRootDataImplementsRootData(Class<ConstructOnlyRootData> rootDataType);
    
    String mustOverrideCreateEventDispatcher(Class<?> runtimeType, Class<?> illegalOperationType);
    
    String currentCollectionIsDisabled(Class<?> thisType);
    
    String whenThisIsModificationAware(Class<ModificationAware> modificationAwareType);
    
    String whenDefaultUnifiedComparatorIsNotNull();
    
    String whenDefaultKeyUnifiedComparatorIsNotNull();
    
    String whenDefaultValueUnifiedComparatorIsNotNull();
}
