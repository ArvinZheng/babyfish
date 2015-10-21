package org.babyfishdemo.war3shop.dal.impl;

import java.lang.reflect.ParameterizedType;

import javax.persistence.PersistenceContext;

import org.babyfish.lang.IllegalProgramException;
import org.babyfish.lang.reflect.ClassInfo;
import org.babyfish.lang.reflect.GenericTypes;
import org.babyfish.persistence.XEntityManager;
import org.babyfishdemo.war3shop.dal.AbstractRepository;

/**
 * @author Tao Chen
 */
public abstract class AbstractRepositoryImpl<E, I> implements AbstractRepository<E, I> {

    @PersistenceContext
    protected XEntityManager em;
    
    protected Class<E> entityType;
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected AbstractRepositoryImpl() {
        Class<?> thisClass = this.getClass();
        if (thisClass.getTypeParameters().length != 0) {
            throw new IllegalProgramException(
                    "The class \"" +
                    thisClass.getName() +
                    "\" must contain no type parameter");
        }
        ParameterizedType parameterizedType = (ParameterizedType)
                ClassInfo
                .of(thisClass)
                .getAncestor(AbstractRepositoryImpl.class)
                .getRawType();
        this.entityType = (Class)GenericTypes.eraseGenericType(parameterizedType.getActualTypeArguments()[0]);
    }

    @Override
    public E mergeEntity(E entity) {
        return this.em.merge(entity);
    }

    @Override
    public void removeEntity(E entity) {
        this.em.remove(entity);
    }

    @Override
    public boolean removeEntityById(I id) {
        if (id != null) {
            E entity = this.em.find(this.entityType, id);
            if (entity != null) {
                this.em.remove(entity);
                return true;
            }
        }
        return false;
    }

    @Override
    public void flush() {
        this.em.flush();
    }
}
