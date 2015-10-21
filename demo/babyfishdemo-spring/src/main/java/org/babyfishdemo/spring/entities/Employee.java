package org.babyfishdemo.spring.entities;

import java.util.Date;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.babyfish.persistence.instrument.JPAObjectModelInstrument;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/*
 * This class is marked by the annotation 
 * @org.babyfish.persistence.instrument.JPAObjectModelInstrument
 * and the ant task "org.babyfish.hibernate.tool.InstrumentTask" is
 * declared in pom.xml, so the byte-code of this class will be 
 * instrumented.
 * 
 * You can use some java decompile tools such as jd-gui.exe to
 * decompile the jar file under the target directory if you 
 * have doubt or you want to research it.
 * 
 * The disadvantage of the byte-code instrument mechanism is, every 
 * time you change the source code of the entity classes, you must 
 * use maven to recompile them because the default compilation 
 * behavior of eclipse will NOT do this byte-code instrument.
 * 
 * Finally, after instrument, this actual code of this class should be
 * (
 *      In java language, java identifiers can not contains invalid characters 
 *      such as "{" and "}", but they can be accepted by JVM, so I generate 
 *      the identifiers with "{" and "}" so that they can not be conflicted by 
 *      the user defined identifiers absolutely.
 * ):
 *
 * package org.babyfishdemo.spring.entities;
 * 
 * import java.util.Collection;
 * import java.util.Date;
 * import java.util.Set;
 * import javax.persistence.Basic;
 * import javax.persistence.Column;
 * import javax.persistence.Embedded;
 * import javax.persistence.Entity;
 * import javax.persistence.EnumType;
 * import javax.persistence.Enumerated;
 * import javax.persistence.FetchType;
 * import javax.persistence.GeneratedValue;
 * import javax.persistence.GenerationType;
 * import javax.persistence.Id;
 * import javax.persistence.JoinColumn;
 * import javax.persistence.Lob;
 * import javax.persistence.ManyToOne;
 * import javax.persistence.OneToMany;
 * import javax.persistence.SequenceGenerator;
 * import javax.persistence.Table;
 * import javax.persistence.Temporal;
 * import javax.persistence.TemporalType;
 * import javax.persistence.Version;
 * import org.babyfish.hibernate.model.loader.HibernateObjectModelScalarLoader;
 * import org.babyfish.model.ObjectModelFactory;
 * import org.babyfish.model.ObjectModelFactoryFactory;
 * import org.babyfish.model.metadata.AllowDisability;
 * import org.babyfish.model.metadata.Association;
 * import org.babyfish.model.metadata.Deferrable;
 * import org.babyfish.model.metadata.ObjectModelDeclaration;
 * import org.babyfish.model.metadata.ObjectModelMode;
 * import org.babyfish.model.metadata.Scalar;
 * import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
 * import org.babyfish.model.spi.ObjectModelImplementor;
 * import org.babyfish.persistence.instrument.JPAObjectModelInstrument;
 * import org.babyfish.persistence.model.metadata.EntityId;
 * import org.babyfish.persistence.model.metadata.Inverse;
 * import org.babyfish.persistence.model.metadata.OptimisticLock;
 * import org.babyfish.reference.Reference;
 * import org.hibernate.annotations.OnDelete;
 * import org.hibernate.annotations.OnDeleteAction;
 * import org.hibernate.bytecode.internal.javassist.FieldHandled;
 * import org.hibernate.bytecode.internal.javassist.FieldHandler;
 * 
 * @JPAObjectModelInstrument
 * @Entity
 * @Table(name="EMPLOYEE")
 * @SequenceGenerator(
 *     name="employeeSequence", 
 *     sequenceName="EMPLOYEE_ID_SEQ", 
 *     initialValue=1, 
 *     allocationSize=1
 * )
 * public class Employee implements FieldHandled {
 *     
 *     public static final boolean {INSTRUMENTED_92B8C17E_BF4E_4135_B596_5A76E0FEBF4E} = true;
 *     
 *     private static final ObjectModelFactory<{OM}> {OM_FACTORY} = ObjectModelFactoryFactory.factoryOf({OM}.class);
 *     
 *     private {OM} {om};
 * 
 *     public Employee() {
 *         {om} = {OM_FACTORY}.create(this);
 *     }
 * 
 *     @Id
 *     @Column(name="EMPLOYEE_ID", nullable=false)
 *     @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="employeeSequence")
 *     public Long getId() {
 *         return {om}.getId();
 *     }
 * 
 *     public void setId(Long paramLong) {
 *         {om}.setId(paramLong); 
 *     }
 *      
 *     @Version
 *     @Column(name="VERSION", nullable=false)
 *     public int getVersion() { 
 *         return {om}.getVersion(); 
 *     }
 * 
 *     public void setVersion(int paramInt) {
 *         {om}.setVersion(paramInt);
 *     }
 *     
 *     @Embedded
 *     public Name getName() {
 *         return {om}.getName();
 *     }
 * 
 *     public void setName(Name paramName) {
 *         {om}.setName(paramName); 
 *     }
 *      
 *     @Enumerated(EnumType.ORDINAL)
 *     @Column(name="GENDER")
 *     public Gender getGender() { 
 *         return {om}.getGender(); 
 *     }
 * 
 *     public void setGender(Gender paramGender) {
 *         {om}.setGender(paramGender); 
 *     }
 *      
 *     @Column(name="BIRTHDAY", nullable=false)
 *     @Temporal(TemporalType.DATE)
 *     public Date getBirthday() { 
 *         return {om}.getBirthday(); 
 *     }
 * 
 *     public void setBirthday(Date paramDate) {
 *         {om}.setBirthday(paramDate); 
 *     }
 *      
 *     @Lob
 *     @Basic(fetch=FetchType.LAZY)
 *     @Column(name="DESCRIPTION", nullable=false)
 *     public String getDescription() { 
 *         return {om}.getDescription(); 
 *     }
 * 
 *     public void setDescription(String paramString) {
 *         {om}.setDescription(paramString); 
 *     }
 *      
 *     @Lob
 *     @Basic(fetch=FetchType.LAZY)
 *     @Column(name="IMAGE", nullable=false)
 *     public byte[] getImage() { 
 *         return {om}.getImage(); 
 *     }
 * 
 *     public void setImage(byte[] paramArrayOfByte) {
 *         {om}.setImage(paramArrayOfByte); 
 *     }
 *      
 *     @ManyToOne(fetch=FetchType.LAZY)
 *     @JoinColumn(name="DEPARTMENT_ID")
 *     public Department getDepartment() {
 *         return (Department){om}.getDepartmentReference().get(); 
 *     }
 * 
 *     public void setDepartment(Department paramDepartment) {
 *         {om}.getDepartmentReference().set(paramDepartment); 
 *     }
 *      
 *     @OneToMany(mappedBy="employee", cascade={javax.persistence.CascadeType.ALL})
 *     @OnDelete(action=OnDeleteAction.CASCADE)
 *     public Set<AnnualLeave> getAnnualLeaves() { 
 *         return {om}.getAnnualLeaves(); 
 *     }
 * 
 *     public void setAnnualLeaves(Set<AnnualLeave> paramSet) {
 *         Set localSet = {om}.getAnnualLeaves(); 
 *         localSet.clear(); 
 *         if (paramSet != null) { 
 *             localSet.addAll(paramSet); 
 *         } 
 *     }
 *      
 *     @ManyToOne(fetch=FetchType.LAZY)
 *     @JoinColumn(name="SUPERVISOR_ID")
 *     public Employee getSupervisor() { 
 *         return (Employee){om}.getSupervisorReference().get(); 
 *     }
 * 
 *     public void setSupervisor(Employee paramEmployee) {
 *         {om}.getSupervisorReference().set(paramEmployee);
 *     }
 *     
 *     @OneToMany(mappedBy="supervisor")
 *     public Set<Employee> getSubordinates() {
 *         return {om}.getSubordinates();
 *     }
 * 
 *     public void setSubordinates(Set<Employee> paramSet) {
 *         Set localSet = {om}.getSubordinates(); 
 *         localSet.clear(); 
 *         if (paramSet != null) { 
 *             localSet.addAll(paramSet);  
 *         }
 *     }
 *     
 *     @Override 
 *     public FieldHandler getFieldHandler() { 
 *         return (FieldHandler)((ObjectModelImplementor){om}).getScalarLoader(); 
 *     }
 *     
 *     @Override
 *     public void setFieldHandler(FieldHandler paramFieldHandler) { 
 *         ObjectModelImplementor localObjectModelImplementor = (ObjectModelImplementor){om};
 *         localObjectModelImplementor.setScalarLoader(
 *             new HibernateObjectModelScalarLoader(localObjectModelImplementor, paramFieldHandler)
 *         ); 
 *     } 
 *     
 *     @StaticMethodToGetObjectModel
 *     static {OM} {om}(Employee paramEmployee) { 
 *         return paramEmployee.{om}; 
 *     }
 *      
 *     public String toString() { 
 *         return {om}.toString(); 
 *     }
 *      
 *     public static enum Gender { 
 *         MALE, 
 *         FEMALE;
 *     }
 * 
 *     @ObjectModelDeclaration(provider="jpa", mode=ObjectModelMode.REFERENCE)
 *     @AllowDisability
 *     private static abstract interface {OM} {
 *     
 *         @EntityId
 *         Long getId();
 *         void setId(Long paramLong);
 * 
 *         @OptimisticLock
 *         int getVersion();
 *         void setVersion(int paramInt);
 * 
 *         @Scalar
 *         Name getName();
 *         void setName(Name paramName);
 * 
 *         @Scalar
 *         Employee.Gender getGender();
 *         void setGender(Employee.Gender paramGender);
 * 
 *         @Scalar
 *         Date getBirthday();
 *         void setBirthday(Date paramDate);
 * 
 *         @Deferrable
 *         String getDescription();
 *         void setDescription(String paramString);
 * 
 *         @Deferrable
 *         byte[] getImage();
 *         void setImage(byte[] paramArrayOfByte);
 * 
 *         @Association(opposite="employees")
 *         Reference<Department> getDepartmentReference();
 * 
 *         @Association(opposite="employeeReference")
 *         @Inverse
 *         Set<AnnualLeave> getAnnualLeaves();
 * 
 *         @Association(opposite="subordinates")
 *         Reference<Employee> getSupervisorReference();
 * 
 *         @Association(opposite="supervisorReference")
 *         @Inverse
 *         Set<Employee> getSubordinates();
 *     }
 * }
 */
/**
 * @author Tao Chen
 */
@JPAObjectModelInstrument
@Entity
@Table(name = "EMPLOYEE")
@SequenceGenerator(
        name = "employeeSequence",
        sequenceName = "EMPLOYEE_ID_SEQ",
        initialValue = 1,
        allocationSize = 1
)
public class Employee {

    @Id
    @Column(name = "EMPLOYEE_ID", nullable = false)
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE, 
            generator = "employeeSequence"
    )
    private Long id;
    
    @Version
    @Column(name = "VERSION", nullable = false)
    private int version;
    
    @Embedded
    private Name name;
    
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "GENDER")
    private Gender gender;
    
    @Column(name = "BIRTHDAY", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date birthday;
    
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "DESCRIPTION", nullable = false)
    private String description;
    
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "IMAGE", nullable = false)
    private byte[] image;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPARTMENT_ID")
    private Department department;
    
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL) //JPA cascade
    @OnDelete(action = OnDeleteAction.CASCADE) //database cascade
    private Set<AnnualLeave> annualLeaves;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SUPERVISOR_ID")
    private Employee supervisor;
    
    @OneToMany(mappedBy = "supervisor")
    private Set<Employee> subordinates;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Set<AnnualLeave> getAnnualLeaves() {
        return annualLeaves;
    }

    public void setAnnualLeaves(Set<AnnualLeave> annualLeaves) {
        this.annualLeaves = annualLeaves;
    }
    
    public Employee getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(Employee supervisor) {
        this.supervisor = supervisor;
    }

    public Set<Employee> getSubordinates() {
        return subordinates;
    }

    public void setSubordinates(Set<Employee> subordinates) {
        this.subordinates = subordinates;
    }

    public static enum Gender {
        MALE,
        FEMALE
    }
}
