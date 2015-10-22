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
package org.babyfish.test.model.metadata;

import java.util.List;

import junit.framework.Assert;

import org.babyfish.lang.IllegalProgramException;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.AssociationProperty;
import org.babyfish.model.metadata.Metadatas;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.ObjectModelMetadata;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.reference.Reference;
import org.babyfish.test.model.entities.AnnualLeave;
import org.babyfish.test.model.entities.Company;
import org.babyfish.test.model.entities.Department;
import org.babyfish.test.model.entities.Employee;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class MetadataTest {
    
    @Test(expected = IllegalProgramException.class)
    public void testBadSelfAssociation() {
        Metadatas.of(SelfAssociation.class);
    }
    
    @Test
    public void testBadAssociationCycle() {
        try {
            Metadatas.of(BadAssociationCycleA.class);
            Assert.fail();
        } catch (IllegalProgramException ex) {
            
        }
        try {
            Metadatas.of(BadAssociationCycleB.class);
            Assert.fail();
        } catch (IllegalProgramException ex) {
            
        }
        try {
            Metadatas.of(BadAssociationCycleC.class);
            Assert.fail();
        } catch (IllegalProgramException ex) {
            
        }
    }
    
    @Test
    public void testInvalidProperty() {
        try {
            Metadatas.of(InvalidPropertyParent.class);
            Assert.fail();
        } catch (IllegalProgramException ex) {
            
        }
        try {
            Metadatas.of(InvalidPropertyChild.class);
            Assert.fail();
        } catch (IllegalProgramException ex) {
            
        }
    }
    
    @Test
    public void testReferenceCycle() {
        ObjectModelMetadata companyMetadata = 
            Metadatas.of(Company.class);
        ObjectModelMetadata departmentMetadata = 
            Metadatas.of(Department.class);
        ObjectModelMetadata employeeMetadata = 
            Metadatas.of(Employee.class);
        ObjectModelMetadata annualLeaveMetadata = 
            Metadatas.of(AnnualLeave.class);
        
        Assert.assertSame(
                departmentMetadata
                .getDeclaredProperty("companyReference"), 
                ((AssociationProperty)
                companyMetadata
                .getDeclaredProperty("departments"))
                .getOppositeProperty());
        
        Assert.assertSame(
                companyMetadata
                .getDeclaredProperty("departments"),
                ((AssociationProperty)
                departmentMetadata.getDeclaredProperty("companyReference"))
                .getOppositeProperty());
        Assert.assertSame(
                employeeMetadata
                .getDeclaredProperty("departmentReference"), 
                ((AssociationProperty)
                departmentMetadata.getDeclaredProperty("employees"))
                .getOppositeProperty());
        
        Assert.assertSame(
                departmentMetadata
                .getDeclaredProperty("employees"), 
                ((AssociationProperty)
                employeeMetadata.getDeclaredProperty("departmentReference"))
                .getOppositeProperty());
        Assert.assertSame(
                annualLeaveMetadata
                .getDeclaredProperty("ownerReference"), 
                ((AssociationProperty)
                employeeMetadata.getDeclaredProperty("annualLeaves"))
                .getOppositeProperty());
        Assert.assertSame(
                employeeMetadata
                .getDeclaredProperty("managerReference"), 
                ((AssociationProperty)
                employeeMetadata.getDeclaredProperty("subordinates"))
                .getOppositeProperty());
        Assert.assertSame(
                employeeMetadata
                .getDeclaredProperty("subordinates"), 
                ((AssociationProperty)
                employeeMetadata.getDeclaredProperty("managerReference"))
                .getOppositeProperty());
        
        Assert.assertSame(
                employeeMetadata
                .getDeclaredProperty("annualLeaves"), 
                ((AssociationProperty)
                annualLeaveMetadata.getDeclaredProperty("ownerReference"))
                .getOppositeProperty());
    }
    
    private static class SelfAssociation {
        
        @StaticMethodToGetObjectModel
        static OM om() {
            throw new UnsupportedOperationException();
        }
        
        @ObjectModelDeclaration
        private interface OM {
            @Association(opposite = "self")
            Reference<SelfAssociation> self();
        }
    }
    
    private static class BadAssociationCycleA {
        
        @StaticMethodToGetObjectModel
        static OM om() {
            throw new UnsupportedOperationException();
        }
        
        @ObjectModelDeclaration
        private interface OM {
            @Association(opposite = "nextC")
            Reference<BadAssociationCycleB> nextB();
        }
    }
    
    private static class BadAssociationCycleB {
        
        @StaticMethodToGetObjectModel
        static OM om() {
            throw new UnsupportedOperationException();
        }
        
        @ObjectModelDeclaration
        private interface OM {
            @Association(opposite = "nextA")
            Reference<BadAssociationCycleC> nextC();
        }
    }

    private static class BadAssociationCycleC {
        
        @StaticMethodToGetObjectModel
        static OM om() {
            throw new UnsupportedOperationException();
        }
        
        @ObjectModelDeclaration
        private interface OM {
            @Association(opposite = "nextB")
            Reference<BadAssociationCycleA> nextA();
        }
    }
    
    private static class InvalidPropertyParent {
        
        @StaticMethodToGetObjectModel
        static OM om() {
            throw new UnsupportedOperationException();
        }
        
        @ObjectModelDeclaration
        private interface OM {
            @Association(opposite = "parent")
            List<InvalidPropertyChild> childs();
        }
    }
    
    private static class InvalidPropertyChild {
        
        @StaticMethodToGetObjectModel
        static OM om() {
            throw new UnsupportedOperationException();
        }
        
        @ObjectModelDeclaration
        private interface OM {
            @Association(opposite = "childs")
            Reference<InvalidPropertyParent> parent();
        }
    }
    
}
