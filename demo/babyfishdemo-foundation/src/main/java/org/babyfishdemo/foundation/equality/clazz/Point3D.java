package org.babyfishdemo.foundation.equality.clazz;

import org.babyfish.lang.Equality;
import org.babyfish.lang.OverrideEquality;

/**
 * @author Tao Chen
 */
public class Point3D extends Point2D {
    
    private int z;

    public Point3D() {}

    public Point3D(int x, int y, int z) {
        super(x, y);
        this.z = z;
    }

    public int getZ() {
        return this.z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 31 + this.z;
    }

    /*
     * The "@OverrideEquality" means this "equals" method 
     * overrides the real rule of equality.
     */
    @OverrideEquality
    @Override
    public boolean equals(Object obj) {
        Equality<Point3D> equality = Equality.of(Point3D.class, this, obj);
        Point3D other = equality.other();
        if (other == null) {
            return equality.returnValue();
        }
        
        /*
         * Notes: 
         * (1) Left side of "==" can use field to optimize the performance
         * (2) Right side of "==" must use getter because "other" may be proxy
         */
        return super.equals(obj) && 
                this.z == other.getZ();
    }
    
}
