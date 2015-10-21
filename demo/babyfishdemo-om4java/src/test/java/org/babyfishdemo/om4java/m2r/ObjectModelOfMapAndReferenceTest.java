package org.babyfishdemo.om4java.m2r;

import org.junit.Test;
 
/**
 * @author Tao Chen
 */
public class ObjectModelOfMapAndReferenceTest {
 
    /*
     * The Map-Reference association demonstrated by this test class
     * is very similar with the Map-KeyedReference association
     * demonstrated by the another test class "org.babyfishdemo.om4java.m2kr.ObjectModelOfMapAndKeyedReferenceTest".
     * 
     * My time is not enough, so this test class 
     * ONLY demonstrates the difference between this test class and that class.
     * 
     * This test method throws UnsupportedOperationException,
     * for Map-Reference, Reference side can only to remove the association by setting null,
     * only Map side can be used to create association.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testDiffWithMapAndKeyedReference() {
        
        Airplane airplane = new Airplane();
        Engine engine = new Engine();
        
        /*
         * Be DIFFERENT with Map-KeyedReference, for Map-Reference association,  
         * You can NOT create the association by changing the Reference of child object to be non-null value,
         * because Reference(NOT KeyedReference) can not specify what the key in the Map of parent object is.
         * So babyfish throws exception when you want to assign the value the Reference to be non-null.
         * 
         * But you can set the reference to null to destroy the association :)
         */
        engine.setAirplane(airplane);
    }
}
