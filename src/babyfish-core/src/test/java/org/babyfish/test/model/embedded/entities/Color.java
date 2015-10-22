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
public class Color {
    
    private static final ObjectModelFactory<OM> OM_FACTORY = 
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    public Color(int alpha, int red, int green, int blue) {
        this.setAlpha(alpha);
        this.setRed(red);
        this.setGreen(green);
        this.setBlue(blue);
    }
    
    @StaticMethodToGetObjectModel
    static OM om(Color color) {
        return color.om;
    }

    public int getAlpha() {
        return om.getAlpha();
    }

    public void setAlpha(int alpha) {
        om.setAlpha(alpha);
    }

    public int getRed() {
        return om.getRed();
    }

    public void setRed(int red) {
        om.setRed(red);
    }

    public int getGreen() {
        return om.getGreen();
    }

    public void setGreen(int green) {
        om.setGreen(green);
    }

    public int getBlue() {
        return om.getBlue();
    }

    public void setBlue(int blue) {
        om.setBlue(blue);
    }

    @ObjectModelDeclaration(
            mode = ObjectModelMode.EMBEDDABLE, 
            declaredPropertiesOrder = "alpha, red, green, blue")
    private interface OM {
        
        @Scalar
        int getAlpha();
        void setAlpha(int alpha);
        
        @Scalar
        int getRed();
        void setRed(int red);
        
        @Scalar
        int getGreen();
        void setGreen(int green);
        
        @Scalar
        int getBlue();
        void setBlue(int blue);
    }
}
