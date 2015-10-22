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
package org.babyfish.model.unidirectional;

import java.util.Set;

import junit.framework.Assert;

import org.babyfish.lang.IllegalProgramException;
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.reference.Reference;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class BadObjectModelTest {
    
    @Test
    public void testBadBidirectionalAssociation() {
        try {
            new Parent();
            Assert.fail("Except exception");
        } catch (ExceptionInInitializerError ex) {
            Assert.assertSame(IllegalProgramException.class, ex.getCause().getClass());
        }
    }
    
    @Test
    public void testBadUnidirectionalAssociation() {
        try {
            new Self();
            Assert.fail("Except exception");
        } catch (ExceptionInInitializerError ex) {
            Assert.assertSame(IllegalProgramException.class, ex.getCause().getClass());
        }
    }

    static class Parent {
        
        private static final ObjectModelFactory<OM> OM_FACTORY =
                ObjectModelFactoryFactory.factoryOf(OM.class);
        
        private OM om = OM_FACTORY.create(this);
        
        @StaticMethodToGetObjectModel
        static OM om(Parent parent) {
            return parent.om;
        }
    
        @ObjectModelDeclaration
        private interface OM {
            
            @Association(opposite = "parentReference")
            Set<Child> getChildren();
        }
    }
    
    static class Child {
        
        private static final ObjectModelFactory<OM> OM_FACTORY =
                ObjectModelFactoryFactory.factoryOf(OM.class);
        
        private OM om = OM_FACTORY.create(this);
        
        @StaticMethodToGetObjectModel
        static OM om(Child child) {
            return child.om;
        }
        
        @ObjectModelDeclaration
        private interface OM {
            
            @Association
            Reference<Parent> getParentReference();
        }
    }
    
    static class Self {
        
        private static final ObjectModelFactory<OM> OM_FACTORY =
                ObjectModelFactoryFactory.factoryOf(OM.class);
        
        private OM om = OM_FACTORY.create(this);
        
        @StaticMethodToGetObjectModel
        static OM om(Self self) {
            return self.om;
        }
        
        @ObjectModelDeclaration
        private interface OM {
            
            @Association
            Set<Self> getChildren();
        }
    }
}
