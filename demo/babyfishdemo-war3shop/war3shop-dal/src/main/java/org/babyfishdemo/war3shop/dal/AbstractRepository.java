package org.babyfishdemo.war3shop.dal;

/**
 * @author Tao Chen(&#38472;&#28059;)
 */
public interface AbstractRepository<E, I> {
    
    E mergeEntity(E entity);
    
    void removeEntity(E entity);
    
    boolean removeEntityById(I id);

    void flush();
}
