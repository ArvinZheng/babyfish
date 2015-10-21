package org.babyfishdemo.foundation.typedi18n.generic;

import org.babyfish.util.Resources;

/**
 * Shows how to use "org.babyfish.util.Resources"
 *
 * @author Tao Chen
 */
public class ComplexDemo {
    
    /*
     * Resources.of(...) can load the resource file whose locale is same with the current JVM, 
     * if that file does not exists, load the default resource file without locale. the other
     * resource files will NOT be loaded and validated.
     * 
     * For product mode, this behavior is nice, but for unit test, it means typed-i18n
     * can ONLY validate one resource file, this is not a good behavior for unit test.
     * 
     * typed-i18n support another functionality "Auto Testing", it can test all the resource files 
     * with any locale automatically. But, unfortunately, I don't use it in this demo because I 
     * want to let you see the correct behavior of typed-i18n when you use break point to debug 
     * this demo step by step. 
     * 
     * So, please learn another project "babyfishdemo-foundation-wrong" to know how to use 
     * "Auto Testing" to check whether all the resource files with any locale are correct in 
     * unit test.
     */
    private static final Resource RESOURCE = Resources.of(Resource.class);

    /*
     * The result of this program is
     * 
     * The summation of "16.0 + 9.0i" and "3.0 - 4.0i" is "19.0 + 5.0i"
     * The difference of "16.0 + 9.0i" and "3.0 - 4.0i" is "13.0 + 13.0i"
     * The product of "16.0 + 9.0i" and "3.0 - 4.0i" is "84.0 - 37.0i"
     * The quotient of "16.0 + 9.0i" and "3.0 - 4.0i" is "0.48 + 3.64i"
     * 
     * This class uses the methods of "System.out", don't worry, 
     * the powerful aspectj load-time weaver can help use to test this class.
     */
    public static void main(String[] args) {
        Complex a = new Complex(16, 9);
        Complex b = new Complex(3, -4);
        System.out.println(RESOURCE.summation(a, b, a.add(b)));
        System.out.println(RESOURCE.difference(a, b, a.subtract(b)));
        System.out.println(RESOURCE.product(a, b, a.multiply(b)));
        System.out.println(RESOURCE.quotient(a, b, a.divide(b)));
    }
    
    /* 
     * All the methods of typed-i18n interface must return String.
     * 
     * The interface can be mapped to some resource files such as
     * 
     *      org.babyfishdemo.foundation.typedi18n.generic.ComplexDemo$Resource.properties
     * or
     *      org.babyfishdemo.foundation.typedi18n.generic.ComplexDemo$Resource_zh_CN.properties,
     *      org.babyfishdemo.foundation.typedi18n.generic.ComplexDemo$Resource_en_US.properties,
     *      org.babyfishdemo.foundation.typedi18n.generic.ComplexDemo$Resource_de_DE.properties,
     *      ... ...
     * 
     * Please see src/main/resources/org/babyfishdemo/foundation/typedi18n/ComplexDemo$Resource.properties
     * 
     * If no resource can be found, "Resources.of(Resource.class)" throws this exception
     * java.lang.IllegalArgumentException: 
     *      Can not find the resource: org/babyfishdemo/foundation/typedi18n/ComplexDemo$Resource
     */
    private interface Resource {
        
        String summation(Complex a, Complex b, Complex result);
        
        String difference(Complex a, Complex b, Complex result);
        
        String product(Complex a, Complex b, Complex result);
        
        String quotient(Complex a, Complex b, Complex result);
    }
}
