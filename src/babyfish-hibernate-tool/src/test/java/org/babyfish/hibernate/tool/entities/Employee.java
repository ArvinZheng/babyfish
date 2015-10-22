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

import java.util.Map;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.babyfish.persistence.instrument.JPAObjectModelInstrument;
import org.babyfish.persistence.instrument.NavigableInstrument;

/**
 * @author Tao Chen
 */
@JPAObjectModelInstrument
@Entity
@Table(name = "Employee")
@Access(AccessType.FIELD)
@AttributeOverride(name = "name", column = @Column(name = "EMPLOYEE_NAME"))
public class Employee extends NamedEntity {

    @EmbeddedId
    @AttributeOverrides(
            @AttributeOverride(name = "primaryId", column = @Column(name = "EMPLOYEE_PRIARY_ID"))
    )
    private EntityId id;
    
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "IMAGE")
    @Lob
    private byte[] image;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Department department;
    
    @OneToMany
    @JoinTable(
            name = "EMPLOYEE_SKILL",
            joinColumns = {
                    @JoinColumn(name = "EMPLOYEE_PRIARY_ID", referencedColumnName = "EMPLOYEE_PRIARY_ID"),
                    @JoinColumn(name = "ENITY_ID_PREFIX", referencedColumnName = "ENITY_ID_PREFIX"),
                    @JoinColumn(name = "ENITY_ID_MAIN", referencedColumnName = "ENITY_ID_MAIN"),
                    @JoinColumn(name = "ENITY_ID_POSTFIX", referencedColumnName = "ENITY_ID_POSTFIX")
            },
            inverseJoinColumns = @JoinColumn(name = "SKILL_ID", referencedColumnName = "SKILL_ID")
    )
    @MapKeyColumn(name = "CODE")
    @NavigableInstrument
    private Map<String, Skill> skills;

    public EntityId getId() {
        return id;
    }

    public void setId(EntityId id) {
        this.id = id;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Map<String, Skill> getSkills() {
        return skills;
    }

    public void setSkills(Map<String, Skill> skills) {
        this.skills = skills;
    }
}
