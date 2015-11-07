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
package org.babyfish.test.hibernate.model.hibernate;

import java.util.List;
import java.util.Set;

import org.babyfish.hibernate.model.metadata.HibernateMetadatas;
import org.babyfish.lang.IllegalProgramException;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.Metadatas;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.ObjectModelMetadata;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.persistence.model.metadata.Inverse;
import org.babyfish.reference.IndexedReference;
import org.babyfish.reference.Reference;
import org.babyfish.test.model.entities.AnnualLeave;
import org.babyfish.test.model.entities.Company;
import org.babyfish.test.model.entities.Department;
import org.babyfish.test.model.entities.Employee;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class HibernateMetadataTest {

    @Test
    public void testReferenceCyle() {
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
                companyMetadata
                .getDeclaredAssociationProperty("departments")
                .getOppositeProperty());
        
        Assert.assertSame(
                companyMetadata
                .getDeclaredProperty("departments"), 
                departmentMetadata
                .getDeclaredAssociationProperty("companyReference")
                .getOppositeProperty());
        Assert.assertSame(
                employeeMetadata
                .getDeclaredProperty("departmentReference"), 
                departmentMetadata
                .getDeclaredAssociationProperty("employees")
                .getOppositeProperty());
        
        Assert.assertSame(
                departmentMetadata
                .getDeclaredProperty("employees"), 
                employeeMetadata
                .getDeclaredAssociationProperty("departmentReference")
                .getOppositeProperty());
        Assert.assertSame(
                annualLeaveMetadata
                .getDeclaredProperty("ownerReference"), 
                employeeMetadata
                .getDeclaredAssociationProperty("annualLeaves")
                .getOppositeProperty());
        Assert.assertSame(
                employeeMetadata
                .getDeclaredProperty("managerReference"), 
                employeeMetadata
                .getDeclaredAssociationProperty("subordinates")
                .getOppositeProperty());
        Assert.assertSame(
                employeeMetadata
                .getDeclaredProperty("subordinates"), 
                employeeMetadata
                .getDeclaredAssociationProperty("managerReference")
                .getOppositeProperty());
        
        Assert.assertSame(
                employeeMetadata
                .getDeclaredProperty("annualLeaves"), 
                annualLeaveMetadata
                .getDeclaredAssociationProperty("ownerReference")
                .getOppositeProperty());
    }
    
    @Test(expected = IllegalProgramException.class)
    public void testBadInverses() {
        HibernateMetadatas.of(BadInversesEmployee.class);
    }
    
    @Test(expected = IllegalProgramException.class)
    public void testTooManyInverses() {
        HibernateMetadatas.of(TooManyInversesEmployee.class);
    }
    
    @Test(expected = IllegalProgramException.class)
    public void testNoInverse() {
        HibernateMetadatas.of(NoInverseEmployee.class);
    }
    
    private static class BadInversesEmployee {
        
        @StaticMethodToGetObjectModel
        static OM om() {
            throw new UnsupportedOperationException();
        }
        
        @ObjectModelDeclaration(provider = "jpa")
        private interface OM {
            
            @Association(opposite = "managerReference")
            @Inverse
            List<BadInversesEmployee> subordinates();
            
            @Association(opposite = "subordinates")
            IndexedReference<BadInversesEmployee> managerReference();
        }
    }
    
    private static class TooManyInversesEmployee {
        
        @StaticMethodToGetObjectModel
        static OM om() {
            throw new UnsupportedOperationException();
        }
        
        @ObjectModelDeclaration(provider = "jpa")
        private interface OM {
            
            @Association(opposite = "managerReference")
            @Inverse
            Set<TooManyInversesEmployee> subordinates();
            
            @Association(opposite = "subordinates")
            @Inverse
            Reference<TooManyInversesEmployee> managerReference();
        }
    }
    
    private static class NoInverseEmployee {
        
        @StaticMethodToGetObjectModel
        static OM om() {
            throw new UnsupportedOperationException();
        }
        
        @ObjectModelDeclaration(provider = "jpa")
        private interface OM {
            
            @Association(opposite = "managerReference")
            Set<NoInverseEmployee> subordinates();
            
            @Association(opposite = "subordinates")
            Reference<NoInverseEmployee> managerReference();
        }
    }

}
