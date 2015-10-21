package org.babyfishdemo.foundation.typedi18n.wrong;

import org.babyfish.lang.IllegalResourceException;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class WrongTest {
    
    @Test
    public void testValidateResourcexx_YYWithoutLaziness() throws ClassNotFoundException {
    	/*
    	 * If you use maven to execute this unit test, it is OK;
    	 * but if you want to use eclipse to execute it, please set
    	 * the VM argument of the run/debug configuration to be
    	 * 
    	 * -Dorg.babyfish.util.LazyResource.LOAD_LAZY_RESOURCE_IMMEDIATELY=true -Dorg.babyfish.util.Resources.LOAD_RESOURCE_LOCALE="org.babyfishdemo.foundation.typedi18n.wrong = null, xx_YY"
    	 */
    	
        /*
         * Need NOT to do anything except loading the tested class.
         * 
         * The pom.xml configures the <argLine/> of "maven-surefire-plugin" with two JVM arguments:
         * 
         * (1) "-Dorg.babyfish.util.LazyResource.LOAD_LAZY_RESOURCE_IMMEDIATELY=true" 
         * ignores the laziness of org.babyfish.util.LazyResource
         * (2) "-Dorg.babyfishdemo.foundation.typedi18n.wrong = null, xx_YY" 
         * ignores the default locale of current JVM and validate 
         * "org.babyfishdemo.foundation.typedi18n.wrong.Wrong$Resource_xx_YY.properties"
         * immediately.
         * 
         * So errors of that resource file will be reported when the test class "Wrong" is loaded.
         * 
         * This functionality can be used to test all the typed-18n functionalities automatically, 
         * please use your Precious time to test your business logic, don't waste your time to test the boring i18n.
         */
        try {
            Class.forName("org.babyfishdemo.foundation.typedi18n.wrong.Wrong");
            Assert.fail(ExceptionInInitializerError.class.getName() + " is required");
        } catch (ExceptionInInitializerError err) {
            Assert.assertTrue(err.getCause() instanceof IllegalResourceException);
            Assert.assertEquals(
                    "Failed to resolve the key \"unknownName\" of the resoruce file "
                    + "\"org/babyfishdemo/foundation/typedi18n/wrong/Wrong$Resource_xx_YY.properties\" "
                    + "because  the parameter count of resoruce file is 0 "
                    + "but the parameter count of the resource interface is 1", 
                    err.getCause().getMessage()
            );
        }
    }
}
