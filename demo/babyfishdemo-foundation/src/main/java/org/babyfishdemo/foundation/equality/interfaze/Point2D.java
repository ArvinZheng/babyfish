package org.babyfishdemo.foundation.equality.interfaze;

import org.babyfish.lang.OverrideEquality;

/**
 * @author Tao Chen
 */
public interface Point2D {

    int getX();
    
    void setX(int x);
    
    int getY();
    
    void setY(int y);
    
    @Override
    int hashCode();
    
    /*
     * The "@OverrideEquality" means this "equals" method 
     * overrides the real rule of equality.
     */
    @OverrideEquality
    @Override
    boolean equals(Object obj);
}
