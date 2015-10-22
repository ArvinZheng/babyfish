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
package org.babyfish.modificationaware.event;

/**
 * @author Tao Chen
 */
public class BubbledProperty<T> {

    private Cause cause;
    
    private T valueToDetach;
    
    private T valueToAttach;
    
    public BubbledProperty(Cause cause) {
        this.cause = cause;
    }
    
    public boolean contains(PropertyVersion version) {
        return this.cause.getEvent().getModificationType().contains(version);
    }
    
    public Cause getCause() {
        return this.cause;
    }
    
    public boolean isValueToDetachEnabled() {
        return ModificationType.ATTACH != 
            this.cause.getEvent().getModificationType();
    }
    
    public boolean isValueToAttachEnabled() {
        return ModificationType.DETACH != 
            this.cause.getEvent().getModificationType();
    }

    public T getValueToDetach() {
        return this.valueToDetach;
    }

    public void setValueToDetach(T valueToDetach) {
        this.valueToDetach = valueToDetach;
    }

    public T getValueToAttach() {
        return this.valueToAttach;
    }

    public void setValueToAttach(T valueToAttach) {
        this.valueToAttach = valueToAttach;
    }
}
