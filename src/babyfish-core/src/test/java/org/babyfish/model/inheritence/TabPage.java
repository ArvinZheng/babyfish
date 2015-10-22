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

import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Contravariance;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.reference.Reference;

/**
 * @author Tao Chen
 */
public class TabPage extends Control {

    private static final long serialVersionUID = 7348748938978621558L;

    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(TabPage tabPage) {
        return tabPage.om;
    }
    
    public String getText() {
        return this.om.getText();
    }

    public void setText(String text) {
        this.om.setText(text);
    }
    
    @Override
    public TabControl getParent() {
        return this.om.getParentReference().get();
    }
    
    @Override
    @Deprecated
    public final void setParent(Control parent) {
        this.setParent((TabControl)parent);
    }

    public void setParent(TabControl tabControl) {
        this.om.getParentReference().set(tabControl);
    }
    
    @ObjectModelDeclaration
    private interface OM {
        
        @Scalar
        String getText();
        void setText(String text);
        
        @Contravariance
        Reference<TabControl> getParentReference();
    }
}
