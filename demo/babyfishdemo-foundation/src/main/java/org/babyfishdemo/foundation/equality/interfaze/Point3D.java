package org.babyfishdemo.foundation.equality.interfaze;

import org.babyfish.lang.OverrideEquality;

/**
 * @author Tao Chen
 */
public interface Point3D extends Point2D {

    int getZ();
    
    void setZ(int z);
    
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
