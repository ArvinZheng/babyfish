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

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.persistence.model.metadata.EntityId;
import org.babyfish.persistence.model.metadata.Inverse;
import org.babyfish.reference.Reference;

/**
 * @author Tao Chen
 */
@Entity
@Table(name = "CONTROL")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "CONTROL_TYPE", discriminatorType = DiscriminatorType.INTEGER)
@DiscriminatorValue("0")
@SequenceGenerator(
        name = "controlSequence", 
        sequenceName = "SEQL_CONTROL_ID", 
        initialValue = 1, 
        allocationSize = 1
)
public class Control implements Serializable {

    private static final long serialVersionUID = -8677832735683209536L;

    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Control control) {
        return control.om;
    }
    
    @Id
    @Column(name = "CONTROL_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "controlSequence")
    public Long getId() {
        return this.om.getId();
    }

    public void setId(Long id) {
        this.om.setId(id);
    }

    @Column(name = "WIDTH")
    public int getWidth() {
        return this.om.getWidth();
    }

    public void setWidth(int width) {
        this.om.setWidth(width);
    }

    @Column(name = "HEIGHT")
    public int getHeight() {
        return this.om.getHeight();
    }

    public void setHeight(int height) {
        this.om.setHeight(height);
    }

    @OneToMany
    @JoinColumn(name = "PARENT_ID")
    @OrderColumn(name = "INDEX_IN_PARENT")
    public List<Control> getControls() {
        return this.om.getControls();
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID", insertable = false, updatable = false)
    public Control getParent() {
        return this.om.getParentReference().get();
    }
    
    public void setParent(Control parent) {
        this.om.getParentReference().set(parent);
    }

    @ObjectModelDeclaration(provider = "jpa")
    private interface OM {
        
        @EntityId
        Long getId();
        void setId(Long id);
        
        @Scalar
        int getWidth();
        void setWidth(int width);
        
        @Scalar
        int getHeight();
        void setHeight(int height);
        
        @Association(opposite = "parentReference")
        List<Control> getControls();
        
        @Association(opposite = "controls")
        @Inverse
        Reference<Control> getParentReference();
    }
}
