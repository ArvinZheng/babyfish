package org.babyfishdemo.querypath;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.babyfish.hibernate.jpa.HibernatePersistenceProvider;
import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.XEntityManagerFactory;

/**
 * @author Tao Chen
 */
public final class JPAContext {
    
    private static final ReadWriteLock ENTITY_MANAGER_FACTORY_LOCK =
            new ReentrantReadWriteLock();
    
    private static XEntityManagerFactory entityManagerFactory;

    private JPAContext() {
        
    }
    
    public static XEntityManager createEntityManager() {
        XEntityManagerFactory emf;
        Lock lock;
        
        (lock = ENTITY_MANAGER_FACTORY_LOCK.readLock()).lock(); //1st locking
        try {
            emf = entityManagerFactory; //1st reading
        } finally {
            lock.unlock();
        }
        
        if (emf == null) { //1st checking
            (lock = ENTITY_MANAGER_FACTORY_LOCK.writeLock()).lock(); //2nd locking
            try {
                emf = entityManagerFactory; //2nd reading
                if (emf == null) { // 2nd checking
                    entityManagerFactory = 
                            emf = 
                            new HibernatePersistenceProvider()
                            .createEntityManagerFactory(null, null);
                }
            } finally {
                lock.unlock();
            }
        }
        
        return emf.createEntityManager();
    }
    
    public static void stop() {
        XEntityManagerFactory emf;
        Lock lock;
        
        (lock = ENTITY_MANAGER_FACTORY_LOCK.readLock()).lock(); //1st locking
        try {
            emf = entityManagerFactory; //1st reading
        } finally {
            lock.unlock();
        }
        
        if (emf != null) { //1st checking
            (lock = ENTITY_MANAGER_FACTORY_LOCK.writeLock()).lock(); //2nd locking
            try {
                emf = entityManagerFactory; //2nd reading
                if (emf != null) { // 2nd checking
                    entityManagerFactory = null;
                    emf.close();
                }
            } finally {
                lock.unlock();
            }
        }
    }
}
