package org.babyfishdemo.spring.model;

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
