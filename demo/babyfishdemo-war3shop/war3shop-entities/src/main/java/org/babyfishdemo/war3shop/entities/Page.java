package org.babyfishdemo.war3shop.entities;

import java.util.List;

/**
 * @author Tao Chen
 */
public interface Page<T> {

    List<T> getEntities();
    
    int getExpectedPageIndex();
    
    int getActualPageIndex();
    
    int getPageSize();
    
    int getTotalRowCount();
    
    int getTotalPageCount();
}
