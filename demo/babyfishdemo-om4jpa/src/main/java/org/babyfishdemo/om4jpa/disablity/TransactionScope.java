package org.babyfishdemo.om4jpa.disablity;

import javax.persistence.EntityManager;

import org.babyfish.hibernate.jpa.HibernatePersistenceProvider;
import org.babyfish.persistence.XEntityManager;
import org.babyfish.persistence.XEntityManagerFactory;

/**
 * @author Tao Chen
 */
public class TransactionScope implements AutoCloseable {
    
    private static XEntityManagerFactory entityManagerFactory;
    
    private static final ThreadLocal<XEntityManager> ENTITY_MANAGER_LOCAL = 
            new ThreadLocal<>();
            
    private XEntityManager oldEntityManager;
    
    private boolean readonly;
    
    private boolean completed;
    
    private boolean closed;
    
    public TransactionScope() {
        this(false);
    }
    
    public TransactionScope(boolean readonly) {
        XEntityManager entityManager = getEntityManagerFactory().createEntityManager();
        if (!readonly) {
            entityManager.getTransaction().begin();
        }
        this.readonly = readonly;
        this.oldEntityManager = ENTITY_MANAGER_LOCAL.get();
        ENTITY_MANAGER_LOCAL.set(entityManager);
    }
    
    public static XEntityManager getEntityManager() {
        return ENTITY_MANAGER_LOCAL.get();
    }
    
    public static synchronized void recreateDatabase() {
        entityManagerFactory = null;
    }
    
    public void compete() {
        this.completed = true;
    }
    
    public <T> T complete(T value) {
        this.completed = true;
        return value;
    }

    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        EntityManager entityManager = ENTITY_MANAGER_LOCAL.get();
        try {
            if (!this.readonly) {
                if (this.completed) {
                    entityManager.getTransaction().commit();
                } else {
                    entityManager.getTransaction().rollback();
                }
            }
        } finally {
            ENTITY_MANAGER_LOCAL.set(this.oldEntityManager);
            entityManager.close();
        }
    }
    
    private synchronized static XEntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null) {
            entityManagerFactory =
                    new HibernatePersistenceProvider(
                            TransactionScope.class.getPackage().getName().replace('.', '/') +
                            "/persistence.xml"
                    )
                    .createEntityManagerFactory(null, null);
        }
        return entityManagerFactory;
    }
}
