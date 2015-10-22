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
package org.babyfish.util;

import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Tao Chen
 */
public class LazyResource<R> implements Serializable {
    
    private static final long serialVersionUID = -5178502521153401586L;

    private static final boolean LOAD_LAZY_RESOURCE_IMMEDIATELY =
            "true".equals(System.getProperty(
                    LazyResource.class.getName() +
                    ".LOAD_LAZY_RESOURCE_IMMEDIATELY")
            );
    
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    
    private Class<R> resourceType;
    
    private transient R resource;

    private LazyResource(Class<R> resourceType) {
        this.resourceType = resourceType;
        if (LOAD_LAZY_RESOURCE_IMMEDIATELY) {
            this.resource = Resources.of(resourceType);
        }
    }
    
    public static <R> LazyResource<R> of(Class<R> resourceType) {
        return new LazyResource<>(resourceType);
    }
    
    public R get() {
        R resource;
        Lock lock;
        
        (lock = this.readWriteLock.readLock()).lock(); //1st locking
        try {
            resource = this.resource; //1st reading
        } finally {
            lock.unlock();
        }
        
        if (resource == null) { //1st checking
            (lock = this.readWriteLock.writeLock()).lock(); //2nd locking
            try {
                resource = this.resource; //2nd reading
                if (resource == null) { //2nd checking
                    this.resource = resource = Resources.of(this.resourceType);
                }
            } finally {
                lock.unlock();
            }
        }
        return resource;
    }
}
