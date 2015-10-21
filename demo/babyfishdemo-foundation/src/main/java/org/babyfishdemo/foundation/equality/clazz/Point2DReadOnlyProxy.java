package org.babyfishdemo.foundation.equality.clazz;


/**
 * @author Tao Chen
 */
public class Point2DReadOnlyProxy extends Point2D {

    /*
     * This class is proxy. 
     * The field "x" and "y" of itself inherited from suepr class is NOT used, 
     * it uses the data of another object "this.target".
     */
    private Point2D target;

    public Point2DReadOnlyProxy(Point2D target) {
        this.target = target;
    }

    @Override
    public int getX() {
        return this.target.getX();
    }

    @Deprecated
    @Override
    public final void setX(int x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getY() {
        return this.target.getY();
    }

    @Deprecated
    @Override
    public final void setY(int y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        return this.target.hashCode();
    }

    /*
     * Note:
     * The "equals" of proxy class should 
     * NOT 
     * use "@OverrideEquality"
     */
    @Override
    public boolean equals(Object obj) {
        return this.target.equals(obj);
    }
}
