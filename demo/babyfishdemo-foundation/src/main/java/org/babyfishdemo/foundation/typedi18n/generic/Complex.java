package org.babyfishdemo.foundation.typedi18n.generic;

import org.babyfish.lang.Arguments;

/**
 * @author Tao Chen
 */
public final class Complex {
    
    public static final Complex ZERO = new Complex(0, 0);

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
    
    public Complex add(Complex complex) {
        Arguments.mustNotBeNull("complex", complex);
        return new Complex(this.real + complex.real, this.image + complex.image);
    }
    
    public Complex subtract(Complex complex) {
        Arguments.mustNotBeNull("complex", complex);
        return new Complex(this.real - complex.real, this.image - complex.image);
    }
    
    public Complex multiply(Complex complex) {
        Arguments.mustNotBeNull("complex", complex);
        return new Complex(
                this.real * complex.real - this.image * complex.image,
                this.real * complex.image + this.image * complex.real
        );
    }
    
    public Complex divide(Complex complex) {
        Arguments.mustNotBeEqualToValue(
                "complex", 
                Arguments.mustNotBeNull("complex", complex), 
                ZERO
        );
        double squareOfComplexAbs = complex.real * complex.real + complex.image * complex.image;
        return new Complex(
                (this.real * complex.real + this.image * complex.image) / squareOfComplexAbs,
                (this.image * complex.real - this.real * complex.image) / squareOfComplexAbs
        );
    }
    
/**
 * @author Tao Chen
 */
    @Override
    public int hashCode() {
        long bits = 
                Double.doubleToLongBits(this.real) * 31 + 
                Double.doubleToLongBits(this.image);
        return (int)(bits ^ (bits >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Complex)) {
            return false;
        }
        Complex other = (Complex)obj;
        return Double.doubleToLongBits(this.real) == Double.doubleToLongBits(other.real) &&
                Double.doubleToLongBits(this.image) == Double.doubleToLongBits(other.image);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.real);
        if (this.image >= 0) {
            builder.append(" + ");
            builder.append(this.image);
        } else {
            builder.append(" - ");
            builder.append(-this.image);
        }
        builder.append('i');
        return builder.toString();
    }
}
