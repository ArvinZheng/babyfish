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
package org.babyfish.model.inheritence;

import java.io.Serializable;
import java.util.List;

import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.AllowDisability;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.reference.Reference;

/**
 * @author Tao Chen
 */
public class Control implements Serializable {
    
    private static final long serialVersionUID = 7880228781123399946L;

    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Control control) {
        return control.om;
    }

    public OM create(Object owner) {
        return OM_FACTORY.create(owner);
    }

    public OM get(Object owner) {
        return OM_FACTORY.get(owner);
    }

    public int getWidth() {
        return this.om.getWidth();
    }

    public void setWidth(int width) {
        this.om.setWidth(width);
    }

    public int getHeight() {
        return this.om.getHeight();
    }

    public void setHeight(int height) {
        this.om.setHeight(height);
    }

    public List<Control> getControls() {
        return this.om.getControls();
    }

    public Control getParent() {
        return this.om.getParentReference().get();
    }
    
    public void setParent(Control parent) {
        this.om.getParentReference().set(parent);
    }

    @ObjectModelDeclaration
    @AllowDisability
    private interface OM {
        
        @Scalar
        int getWidth();
        void setWidth(int width);
        
        @Scalar
        int getHeight();
        void setHeight(int height);
        
        @Association(opposite = "parentReference")
        List<Control> getControls();
        
        @Association(opposite = "controls")
        Reference<Control> getParentReference();
    }
}
