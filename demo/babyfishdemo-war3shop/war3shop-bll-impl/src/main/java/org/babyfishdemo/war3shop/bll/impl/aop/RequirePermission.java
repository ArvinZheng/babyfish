package org.babyfishdemo.war3shop.bll.impl.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.babyfishdemo.war3shop.bll.Constants;

/**
 * @author Tao Chen
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RequirePermission {

    /**
     * When it is {@link Constants#ACCOUNT_MANAGER_PERMISSION }, that means requires the AccountManager;
     * When it is {@link Constants#CUSTOMER_PERMISSION }, that means requires the the Customer;
     * otherwise, that means requires the privilege configured in the database.
     */
    String[] value();
    
    boolean forGuest() default false;
}
