/*
 * BabyFish, Object Model Framework for Java and JPA.
 * https://github.com/babyfish-ct/babyfish
 *
 * Copyright (c) 2008-2015, Tao Chen
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * Please visit "http://opensource.org/licenses/LGPL-3.0" to know more.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 */
package org.babyfish.model.metadata;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Tao Chen
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ObjectModelDeclaration {

    /** 
     * @return The ObjectModelProvider name
     * 
     * <ul>
     *    <li>If this argument is not specified, it means ObjectModel4Java</li>
     *  <li>If this argument is "jpa", it means ObjectModel4JPA</li>
     * </ul>
     * 
     * In the future, maybe more value can be used.
     */
    String provider() default "";
    
    /**
     * @return The mode of ObjectModel
     * <ul>
     *  <li>
     *      {@link ObjectModelMode#REFERENCE},
     *      Reference Object, can contain both scalar and association properties.
     *  </li>
     *  <li>
     *      {@link ObjectModelMode#EMBEDDABLE},
     *      Embeddable Object, can only contain scalar properties
     *  </li>
     *  <li>
     *      {@link ObjectModelMode#ABSTRACT},
     *      Super Object interface of other ObjectModel interfaces,
     *      can only contain scalar properties
     *  </li>
     * </ul>
     */
    ObjectModelMode mode() default ObjectModelMode.REFERENCE;
    
    /**
     * @return The order of the declared property of ObjectModel
     * 
     * <p>
     *  If {@link #mode()} is {@link ObjectModelMode#REFERENCE}, it is unnecessary; 
     *  otherwise, this argument to specify the order of declared properties of 
     *  current ObjectModelInterface is required.
     * </p>
     * <p>
     *  Two ways to use it
     * </p>
     *  <ul>
     *      <li>&#64;ObjectModelDeclaration(declaredPropertiesOrder = "a, b, c")</li>
     *      <li>&#64;ObjectModelDeclaration(declaredPropertiesOrder = { "a", "b", "c" })</li>
     *  </ul>
     */
    String[] declaredPropertiesOrder() default {};
}
