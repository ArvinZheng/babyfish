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
package org.babyfish.model.event;

import org.babyfish.lang.Arguments;
import org.babyfish.lang.reflect.ClassInfo;
import org.babyfish.model.ObjectModel;
import org.babyfish.model.event.modification.ObjectModelModifications.SetScalarModification;
import org.babyfish.model.metadata.ScalarProperty;
import org.babyfish.modificationaware.event.BubbledPropertyConverter;
import org.babyfish.modificationaware.event.BubbledSharedPropertyConverter;
import org.babyfish.modificationaware.event.Cause;
import org.babyfish.modificationaware.event.EventDeclaration;
import org.babyfish.modificationaware.event.EventFactory;
import org.babyfish.modificationaware.event.EventProperty;
import org.babyfish.modificationaware.event.ModificationEvent;
import org.babyfish.modificationaware.event.PropertyVersion;

/**
 * @author Tao Chen
 */
@EventDeclaration(properties = {
        @EventProperty(name = "property", shared = true),
        @EventProperty(name = "scalar", shared = false)
})
public abstract class ScalarEvent extends ModificationEvent {
    
    private static final long serialVersionUID = 5844521357302533973L;
    
    private static final Factory FACTORY = getFactory(Factory.class);

    protected ScalarEvent(Object source) {
        super(source);
    }
    
    protected ScalarEvent(Object source, Cause cause) {
        super(source, cause);
    }
    
    protected ScalarEvent(Object source, ScalarEvent dispatchSourceEvent) {
        super(source, dispatchSourceEvent);
    }
    
    @Override
    public abstract ScalarEvent dispatch(Object source);
    
    @Override
    public abstract SetScalarModification getModification();
    
    public abstract Object getScalar(PropertyVersion version);
    
    public abstract ScalarProperty getProperty();
    
    public static ScalarEvent createReplaceEvent(
            ObjectModel source, 
            SetScalarModification modification,
            Object detachedScalar,
            Object attachedScalar) {
        final ScalarProperty property =
                Arguments.mustNotBeNull("source", source)
                .getObjectModelMetadata()
                .getDeclaredScalarProperty(modification.getScalarPropertyId());
        Class<?> expectedType = ClassInfo.box(property.getReturnClass());
        return FACTORY.createReplaceEvent(
                source, 
                modification, 
                property,
                Arguments.mustBeInstanceOfValue(
                        "detachedScalar", 
                        detachedScalar, 
                        expectedType), 
                Arguments.mustBeInstanceOfValue(
                        "attachedScalar", 
                        attachedScalar, 
                        expectedType));
    }
    
    public static ScalarEvent bubbleEvent(
            Object source, 
            Cause cause, 
            BubbledSharedPropertyConverter<ScalarProperty> propertyConverter,
            BubbledPropertyConverter<Object> elementConverter) {
        return FACTORY.bubbleEvent(source, cause, propertyConverter, elementConverter);
    }
    
    @EventFactory
    private interface Factory {
        
        ScalarEvent createReplaceEvent(
                Object source, 
                SetScalarModification modification, 
                ScalarProperty property,
                Object detachedScalar, 
                Object attachedScalar);
        
        ScalarEvent bubbleEvent(
                Object source, 
                Cause cause, 
                BubbledSharedPropertyConverter<ScalarProperty> propertyConverter, 
                BubbledPropertyConverter<Object> scalarConverter);
    }
}
