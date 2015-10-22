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
package org.babyfish.hibernate.tool.entities;

import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.babyfish.persistence.instrument.JPAObjectModelInstrument;

/**
 * @author Tao Chen
 */
@JPAObjectModelInstrument
@Entity
@Table(name = "SKILL")
@Access(AccessType.FIELD)
@SequenceGenerator(
        name = "skillSequence",
        sequenceName = "SKILL_ID_SEQ",
        initialValue = 1,
        allocationSize = 1
)
public class Skill extends NamedEntity {

    @Id
    @Column(name = "SKILL_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "skillSequence")
    private Long id;
    
    @Column(name = "START_DATE")
    @Temporal(TemporalType.DATE)
    private Date startDate;
    
    @Column(name = "CODE", insertable = false, updatable = false)
    private String code;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(
            name = "EMPLOYEE_SKILL",
            joinColumns = @JoinColumn(name = "SKILL_ID", referencedColumnName = "SKILL_ID", insertable = false, updatable = false),
            inverseJoinColumns = {
                    @JoinColumn(name = "EMPLOYEE_PRIARY_ID", referencedColumnName = "EMPLOYEE_PRIARY_ID", insertable = false, updatable = false),
                    @JoinColumn(name = "ENITY_ID_PREFIX", referencedColumnName = "ENITY_ID_PREFIX", insertable = false, updatable = false),
                    @JoinColumn(name = "ENITY_ID_MAIN", referencedColumnName = "ENITY_ID_MAIN", insertable = false, updatable = false),
                    @JoinColumn(name = "ENITY_ID_POSTFIX", referencedColumnName = "ENITY_ID_POSTFIX", insertable = false, updatable = false)
            }
    )
    private Employee employee;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}
