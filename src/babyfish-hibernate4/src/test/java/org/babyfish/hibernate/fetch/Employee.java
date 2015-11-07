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

import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.babyfish.hibernate.common.ObjectModelScalarLoaderBuilder;
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.Deferrable;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.model.spi.ObjectModelImplementor;
import org.babyfish.persistence.model.metadata.EntityId;
import org.babyfish.persistence.model.metadata.Inverse;
import org.babyfish.persistence.model.metadata.LazyBehavior;
import org.babyfish.reference.Reference;
import org.hibernate.bytecode.internal.javassist.FieldHandled;
import org.hibernate.bytecode.internal.javassist.FieldHandler;

/**
 * @author Tao Chen
 */
@Entity
@Table(name = "EMPLOYEE")
@SequenceGenerator(
        name = "employeeSequence", 
        sequenceName = "EMPLOYEE_ID_SEQ", 
        initialValue = 1, 
        allocationSize = 1
)
@NamedQueries({
    @NamedQuery(
            name = "getEmployeesByDepartmentName1",
            query = "select e from Employee e inner join e.department d where d.name = :departmentName"
    ),
    @NamedQuery(
            name = "getEmployeesByDepartmentName2",
            query = "select e from Employee e inner join e.department d where d.name = ?"
    )
})
public class Employee extends ObjectModelScalarLoaderBuilder implements FieldHandled {
    
    private static final ObjectModelFactory<OM> OM_FACTORY =
            ObjectModelFactoryFactory.factoryOf(OM.class);
    
    private OM om = OM_FACTORY.create(this);
    
    @StaticMethodToGetObjectModel
    static OM om(Employee employee) {
        return employee.om;
    }
    
    @Id
    @Column(name = "EMPLOYEE_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "employeeSequence")
    public Long getId() {
        return this.om.getId();
    }
    
    @SuppressWarnings("unused")
    private void setId(Long id) {
        this.om.setId(id);
    }
    
    @Embedded
    public Name getName() {
        return this.om.getName();
    }
    
    public void setName(Name name) {
        this.om.setName(name);
    }
    
    @Basic(fetch = FetchType.LAZY)
    @Lob
    @Column(name = "IMAGE")
    public byte[] getImage() {
        return this.om.getImage();
    }
    
    public void setImage(byte[] image) {
        this.om.setImage(image);
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPARTMENT_ID")
    public Department getDepartment() {
        return this.om.getDepartmentReference().get();
    }
    
    public void setDepartment(Department department) {
        this.om.getDepartmentReference().set(department);
    }
    
    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    @OrderColumn(name = "LIST_INDEX")
    public List<AnnualLeave> getAnnualLeaves() {
        return this.om.getAnnualLeaves();
    }
    
    @OneToMany(mappedBy = "supervisor", fetch = FetchType.LAZY)
    public Set<Employee> getSubordinates() {
        return this.om.getSubordinates();
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SUPERVISOR_ID")
    public Employee getSupervisor() {
        return this.om.getSupervisorReference().get();
    }
    
    public void setSupervisor(Employee supervisor) {
        this.om.getSupervisorReference().set(supervisor);
    }

    @Override
    public FieldHandler getFieldHandler() {
        return (FieldHandler)((ObjectModelImplementor)this.om).getScalarLoader();
    }

    @Override
    public void setFieldHandler(FieldHandler handler) {
        ((ObjectModelImplementor)this.om).setScalarLoader(ObjectModelScalarLoaderBuilder.build(this.om, handler));
    }

    @ObjectModelDeclaration(provider = "jpa")
    private interface OM {
    
        @EntityId
        Long getId();
        void setId(Long id);
        
        @Scalar
        Name getName();
        void setName(Name name);
        
        @Deferrable
        byte[] getImage();
        void setImage(byte[] image);
        
        @Association(opposite = "employees")
        Reference<Department> getDepartmentReference();
        
        @Association(opposite = "supervisorReference")
        @Inverse
        @LazyBehavior(rowLimit = 16, countLimit = 2)
        Set<Employee> getSubordinates();
        
        @Association(opposite = "subordinates")
        Reference<Employee> getSupervisorReference();
        
        @Association(opposite = "employeeReference")
        @Inverse
        List<AnnualLeave> getAnnualLeaves();
    }
    
}
