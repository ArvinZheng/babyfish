package org.babyfishdemo.war3shop.dal;

/**
 * @author Tao Chen
 */
public interface AbstractRepository<E, I> {
    
    E mergeEntity(E entity);
    
    void removeEntity(E entity);
    
    boolean removeEntityById(I id);

    void flush();
}
