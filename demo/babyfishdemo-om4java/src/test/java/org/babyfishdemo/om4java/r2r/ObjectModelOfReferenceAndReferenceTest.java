package org.babyfishdemo.om4java.r2r;

import org.babyfishdemo.om4java.r2r.Person;
import org.junit.Assert;
import org.junit.Test;
 
/**
 * @author Tao Chen
 */
public class ObjectModelOfReferenceAndReferenceTest {
 
    @Test
    public void test() {
        Person jim = new Person("Jim");
        Person kate = new Person("Kate");
        Person tom = new Person("Tom");
        Person linda = new Person("Linda");
        
        {
	        /*
	         * Validate the initialized state of these objects
	         */
	        Assert.assertNull(jim.getSpouse());
	        Assert.assertNull(kate.getSpouse());
	        Assert.assertNull(tom.getSpouse());
	        Assert.assertNull(linda.getSpouse());
        }
        
        {
	        /*
	         * Change the property "spouse" of jim to be kate.
	         * The property "spouse" of kate will be changed to be jim automatically and implicitly.
	         */
	        jim.setSpouse(kate);
	        
	        Assert.assertSame(kate, jim.getSpouse()); // Changed by you
	        Assert.assertSame(jim, kate.getSpouse()); // Changed automatically
	        Assert.assertNull(tom.getSpouse());
	        Assert.assertNull(linda.getSpouse());
        }
        
        {
	        /*
	         * Change the property "spouse" of linda to be tom.
	         * The property "spouse" of tom will be changed to be linda automatically and implicitly.
	         */
	        linda.setSpouse(tom);
	        
	        Assert.assertSame(kate, jim.getSpouse());
	        Assert.assertSame(jim, kate.getSpouse());
	        Assert.assertSame(linda, tom.getSpouse()); // Changed by you
	        Assert.assertSame(tom, linda.getSpouse()); // Changed automatically
        }
        
        {
	        /*
	         * Change the property "spouse" of jim to be linda.
	         * (1) The property "spouse" of linda will be changed to be jim automatically and implicitly.
	         * (2) The property "spouse" of kate who is the original spouse of jim 
	         * will be changed to be null automatically and implicitly.
	         * (3) The property "spouse" of tom who is the original spouse of linda 
	         * will be changed to be null automatically and implicitly.
	         */
	        jim.setSpouse(linda);
	        
	        Assert.assertSame(linda, jim.getSpouse()); // Changed by you
	        Assert.assertNull(kate.getSpouse()); // Changed automatically
	        Assert.assertNull(tom.getSpouse()); // Changed automatically
	        Assert.assertSame(jim, linda.getSpouse()); // Changed automatically
        }
        
        {
	        /*
	         * Change the property "spouse" of linda to be null.
	         * The property "spouse" of jim will be changed to be null automatically and implicitly.
	         */
	        linda.setSpouse(null);
	        
	        Assert.assertNull(jim.getSpouse()); // Changed automatically
	        Assert.assertNull(kate.getSpouse());
	        Assert.assertNull(tom.getSpouse());
	        Assert.assertNull(linda.getSpouse()); // Changed by you
        }
    }
}
