package org.babyfishdemo.xcollection.v;

/**
 * @author Tao Chen
 */
public class Complex {
    
    private double real;
    
    private double image;
    
    public Complex(double real, double image) {
        this.real = real;
        this.image = image;
    }
    
    public double getReal() {
        return this.real;
    }

    public double getImage() {
        return this.image;
    }
    
    public double getAbs() {
        return Math.sqrt(this.real * this.real + this.image * this.image);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.real);
        if (this.image < 0) {
            builder.append(" - ").append(-this.image);
        } else {
            builder.append(" + ").append(+this.image);
        }
        builder.append('i');
        return builder.toString();
    }
}
