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
package org.babyfish.hibernate.association;

import org.babyfish.association.AssociatedReference;
import org.hibernate.Hibernate;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.proxy.HibernateProxy;

/**
 * @author Tao Chen
 */
public abstract class EntityReference<O, T> extends AssociatedReference<O, T> {
    
    private static final long serialVersionUID = 5952680256606856201L;

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
    
    @Override
    protected boolean isLoadedValue(T value) {
        return Hibernate.isInitialized(value);
    }

    @Override
    protected boolean isAbandonableValue(T value) {
        //This endpoint is not inverse means the opposite end point is inverse.
        if (!this.isInverse() && value instanceof HibernateProxy) {
            HibernateProxy proxy = (HibernateProxy)value;
            SessionImplementor session = proxy.getHibernateLazyInitializer().getSession();
            return session == null ||
                    !session.isOpen() ||
                    !session.isConnected();
        }
        return false;
    }

    @Override
    protected void loadValue(T value) {
        Hibernate.initialize(value);
    }
    
    protected abstract boolean isInverse();

    T hibernateGet() {
        return this.value;
    }
    
    @SuppressWarnings("unchecked")
    void hibernateSet(Object value) {
        this.value = (T)value;
    }
    
}
