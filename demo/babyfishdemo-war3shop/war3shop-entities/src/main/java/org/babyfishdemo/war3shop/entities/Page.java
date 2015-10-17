package org.babyfishdemo.war3shop.entities;

import java.util.List;

/**
 * @author Tao Chen(&#38472;&#28059;)
 */
public interface Page<T> {

    List<T> getEntities();
    
    int getExpectedPageIndex();
    
    int getActualPageIndex();
    
    int getPageSize();
    
    int getTotalRowCount();
    
    int getTotalPageCount();
}
