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
package org.babyfish.hibernate.tool.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.babyfish.persistence.instrument.JPAObjectModelInstrument;

/**
 * @author Tao Chen
 */
@JPAObjectModelInstrument(declaredPropertiesOrder = "postfix, prefix, main")
@Embeddable
public class SecondaryEntityId implements Serializable {
    
    private static final long serialVersionUID = 4422743439769645655L;

    @Column(name = "PREFIX")
    private String prefix;
    
    @Column(name = "MAIN")
    private String main;
    
    @Column(name = "POSTFIX")
    private String postfix;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }

    public String getPostfix() {
        return postfix;
    }

    public void setPostfix(String postfix) {
        this.postfix = postfix;
    }
}
