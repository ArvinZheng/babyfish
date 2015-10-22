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
package org.babyfish.test.model.embedded.entities;

import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.ObjectModelMode;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;

/**
 * @author Tao Chen
 */
public class Rectangle {

    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    public Rectangle(int width, int height) {
        this.setWidth(width);
        this.setHeight(height);
    }
    
    @StaticMethodToGetObjectModel
    static OM om(Rectangle rectangle) {
        return rectangle.om;
    }
    
    public int getWidth() {
        return om.getWidth();
    }

    public void setWidth(int width) {
        om.setWidth(width);
    }

    public int getHeight() {
        return om.getHeight();
    }

    public void setHeight(int height) {
        om.setHeight(height);
    }

    @ObjectModelDeclaration(
            mode = ObjectModelMode.EMBEDDABLE,
            declaredPropertiesOrder = "width, height")
    private interface OM {
        
        @Scalar
        int getWidth();
        void setWidth(int width);
        
        @Scalar
        int getHeight();
        void setHeight(int height);
    }
}
