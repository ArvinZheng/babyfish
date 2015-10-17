/*
 * BabyFish, Object Model Framework for Java and JPA.
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
package org.babyfish.test.hibernate.model.contravariance;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Contravariance;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.persistence.model.metadata.Mapping;
import org.babyfish.reference.Reference;

/**
 * @author Tao Chen(&#38472;&#28059;)
 */
@Entity
@Table(name = "TAB_PAGE")
public class TabPage extends Container {
 
    private static final long serialVersionUID = 2446668116158475548L;

    private static final ObjectModelFactory<OM> OM_FACTORY = ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(TabPage tabPage) {
        return tabPage.om;
    }
    
    @Column(name = "title", length = 20)
    public String getTitle() {
        return this.om.getTitle();
    }
    
    public void setTitle(String title) {
        this.om.setTitle(title);
    }
    
    @Transient
    public TabControl getTabControl() {
        return this.om.getParentReference().get();
    }
    
    public void setTabControl(TabControl parent) {
        this.om.getParentReference().set(parent);
    }
    
    @ObjectModelDeclaration(provider = "jpa")
    private interface OM {
        
        @Scalar
        String getTitle();
        void setTitle(String title);
    
        @Contravariance
        @Mapping("tabControl")
        Reference<TabControl> getParentReference();
    }
}
