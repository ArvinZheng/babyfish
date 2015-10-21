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
package org.babyfish.hibernate.association;

import org.babyfish.association.AssociatedIndexedReference;
import org.hibernate.proxy.HibernateProxy;

/**
 * @author Tao Chen
 */
public abstract class EntityIndexedReference<O, T> extends AssociatedIndexedReference<O, T> {

    private static final long serialVersionUID = 1057206082876178100L;

    @Override
    public boolean isLoaded() {
        if (this.value instanceof HibernateProxy &&
                ((HibernateProxy)this.value).getHibernateLazyInitializer().isUninitialized()) {
            return false;
        }
        return true;
    }
    
    @Override
    public boolean isLoadable() {
        if (this.value instanceof HibernateProxy) {
            HibernateProxy proxy = (HibernateProxy)this.value;
            return proxy.getHibernateLazyInitializer().getSession().isConnected();
        }
        return true;
    }

    @Override
    public void load() {
        if (this.value instanceof HibernateProxy) {
            ((HibernateProxy)this.value).getHibernateLazyInitializer().initialize();
        }
    }
    
    int hibernateGetIndex() {
        return this.index;
    }
    
    protected abstract boolean isInverse();
    
    void hibernateSetIndex(Object value) {
        
        this.index = value instanceof Integer ? (Integer)value : INVALID_INDEX;
    }
    
    T hibernateGet() {
        return this.index == INVALID_INDEX ? null : this.value;
    }
    
    @SuppressWarnings("unchecked")
    void hibernateSet(Object value) {
        this.value = (T)value;
    }
    
}
