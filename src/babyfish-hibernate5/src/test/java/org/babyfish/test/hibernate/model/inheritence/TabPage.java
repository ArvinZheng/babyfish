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
package org.babyfish.test.hibernate.model.inheritence;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

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
@Entity
@Table(name = "TAB_PAGE")
@DiscriminatorValue("2")
public class TabPage extends Control {
    
    private static final long serialVersionUID = -3666968138270565256L;

    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(TabPage tabPage) {
        return tabPage.om;
    }
    
    @Column(name = "TITLE")
    public String getTitle() {
        return this.om.getTitle();
    }

    public void setTitle(String title) {
        this.om.setTitle(title);
    }
    
    @Transient
    public TabControl getTabControl() {
        return this.om.getTabControlReference().get();
    }

    public void setTabControl(TabControl tabControl) {
        this.om.getTabControlReference().set(tabControl);
    }

    @ObjectModelDeclaration(provider = "jpa")
    private interface OM {
        
        @Scalar
        String getTitle();
        void setTitle(String title);
        
        @Contravariance("parentReference")
        Reference<TabControl> getTabControlReference();
    }
}
