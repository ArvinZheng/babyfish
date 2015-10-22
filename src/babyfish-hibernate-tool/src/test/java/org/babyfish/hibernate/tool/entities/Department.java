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

import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.babyfish.persistence.instrument.JPAObjectModelInstrument;
import org.babyfish.persistence.instrument.NavigableInstrument;
import org.babyfish.persistence.instrument.ReferenceComparisonRuleInstrument;

/**
 * @author Tao Chen
 */
@JPAObjectModelInstrument
@Entity
@Table(name = "DEPARTMENT")
@Access(AccessType.FIELD)
@AttributeOverrides(
        @AttributeOverride(name = "name", column = @Column(name = "DEPARTMENT_NAME"))
)
public class Department extends NamedEntity {

    @EmbeddedId
    @AttributeOverride(name = "primaryId", column = @Column(name = "DEPARTMENT_PRIARY_ID"))
    private EntityId id;
    
    @Version
    @Column(name = "VERSION")
    private int version;
    
    @OneToMany(mappedBy = "department")
    @ReferenceComparisonRuleInstrument("name")
    @NavigableInstrument
    private Set<Employee> employees;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CITY_ID")
    private City city;
    
    public EntityId getId() {
        return id;
    }

    public void setId(EntityId id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Set<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(Set<Employee> employees) {
        this.employees = employees;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }
}
