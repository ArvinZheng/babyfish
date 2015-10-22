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
package org.babyfish.test.model.entities;

import java.util.Date;

import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.reference.MAIndexedReference;

/**
 * @author Tao Chen
 */
public class AnnualLeave {
    
    private static final ObjectModelFactory<OM> ASSOCIATION_MANAGER_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = 
        ASSOCIATION_MANAGER_FACTORY.create(this);

    @StaticMethodToGetObjectModel
    static OM om(AnnualLeave annualLeave) {
        return annualLeave.om;
    }

    private Date startDate;
    
    private Date endDate;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    
    public int getIndexInOwner() {
        return this.om.getOwnerReference().getIndex();
    }
    
    public void setIndexInOwner(int index) {
        this.om.getOwnerReference().setIndex(index);
    }
    
    public Employee getOwner() {
        return this.om.getOwnerReference().get();
    }
    
    public void setOwner(Employee employee) {
        this.om.getOwnerReference().set(employee);
    }
    
    @ObjectModelDeclaration
    private interface OM {
    
        @Association(opposite = "annualLeaves")
        MAIndexedReference<Employee> getOwnerReference();
        
    }
    
}
