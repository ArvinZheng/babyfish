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
package org.babyfish.hibernate.fetch;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.persistence.model.metadata.EntityId;
import org.babyfish.persistence.model.metadata.IndexMapping;
import org.babyfish.reference.IndexedReference;

/**
 * @author Tao Chen
 */
@Entity
@Table(
        name = "ANNUAL_LEAVE")
@SequenceGenerator(
        name = "annualLeaveSequence",
        sequenceName = "ANNUAL_LEAVE_ID_SEQ",
        allocationSize = 1)
public class AnnualLeave {
    
    private static final ObjectModelFactory<OM> OM_FACTORY = 
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(AnnualLeave annualLeave) {
        return annualLeave.om;
    }
    
    public AnnualLeave() {
        
    }
    
    public AnnualLeave(Date startTime, Date endTime, String reason) {
        this.setStartTime(startTime);
        this.setEndTime(endTime);
        this.setReason(reason);
    }

    @Id
    @Column(name = "ANNUAL_LEAVE_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "annualLeaveSequence")
    public Long getId() {
        return this.om.getId();
    }
    
    void setId(Long id) {
        this.om.setId(id);
    }
    
    @Temporal(TemporalType.DATE)
    @Column(name = "START_TIME")
    public Date getStartTime() {
        return this.om.getStartTime();
    }
    
    public void setStartTime(Date startTime) {
        this.om.setStartTime(startTime);
    }
    
    @Temporal(TemporalType.DATE)
    @Column(name = "END_TIME")
    public Date getEndTime() {
        return this.om.getEndTime();
    }
    
    public void setEndTime(Date endTime) {
        this.om.setEndTime(endTime);
    }
    
    @Basic(fetch = FetchType.LAZY )
    public String getReason() {
        return this.om.getReason();
    }

    public void setReason(String reason) {
        this.om.setReason(reason);
    }
    
    @Column(name = "LIST_INDEX")
    public int getIndex() {
        return this.om.getEmployeeReference().getIndex();
    }

    public void setIndex(int index) {
        this.om.getEmployeeReference().setIndex(index);
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMPLOYEE_ID")
    public Employee getEmployee() {
        return this.om.getEmployeeReference().get();
    }
    
    public void setEmployee(Employee employee) {
        this.om.getEmployeeReference().set(employee);
    }

    @ObjectModelDeclaration(provider = "jpa")
    private interface OM {
        
        @EntityId
        Long getId();
        void setId(Long id);
        
        @Scalar
        Date getStartTime();
        void setStartTime(Date startTime);
        
        @Scalar
        Date getEndTime();
        void setEndTime(Date endTime);
        
        @Scalar
        String getReason();
        void setReason(String reason);
        
        @Association(opposite = "annualLeaves")
        @IndexMapping("index")
        IndexedReference<Employee> getEmployeeReference();
    }
    
}
