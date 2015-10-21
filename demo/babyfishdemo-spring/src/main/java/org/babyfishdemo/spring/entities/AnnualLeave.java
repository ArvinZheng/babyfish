package org.babyfishdemo.spring.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import javax.persistence.Version;

import org.babyfish.persistence.instrument.JPAObjectModelInstrument;

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
 * 
 * package org.babyfishdemo.spring.entities;
 * 
 * import java.util.Date;
 * import javax.persistence.Column;
 * import javax.persistence.Entity;
 * import javax.persistence.EnumType;
 * import javax.persistence.Enumerated;
 * import javax.persistence.FetchType;
 * import javax.persistence.GeneratedValue;
 * import javax.persistence.GenerationType;
 * import javax.persistence.Id;
 * import javax.persistence.JoinColumn;
 * import javax.persistence.ManyToOne;
 * import javax.persistence.SequenceGenerator;
 * import javax.persistence.Table;
 * import javax.persistence.Temporal;
 * import javax.persistence.TemporalType;
 * import javax.persistence.Version;
 * import org.babyfish.model.ObjectModelFactory;
 * import org.babyfish.model.ObjectModelFactoryFactory;
 * import org.babyfish.model.metadata.AllowDisability;
 * import org.babyfish.model.metadata.Association;
 * import org.babyfish.model.metadata.ObjectModelDeclaration;
 * import org.babyfish.model.metadata.ObjectModelMode;
 * import org.babyfish.model.metadata.Scalar;
 * import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
 * import org.babyfish.persistence.instrument.JPAObjectModelInstrument;
 * import org.babyfish.persistence.model.metadata.EntityId;
 * import org.babyfish.persistence.model.metadata.OptimisticLock;
 * import org.babyfish.reference.Reference;
 * 
 * @JPAObjectModelInstrument
 * @Entity
 * @Table(name="ANNUAL_LEAVE")
 * @SequenceGenerator(
 *     name="annualLeaveSequence", 
 *     sequenceName="ANNUAL_LEAVE_ID_SEQ", 
 *     initialValue=1, 
 *     allocationSize=1
 * )
 * public class AnnualLeave {
 * 
 *     public static final boolean {INSTRUMENTED_92B8C17E_BF4E_4135_B596_5A76E0FEBF4E} = true;
 *     
 *     private static final ObjectModelFactory<{OM}> {OM_FACTORY} = 
 *         ObjectModelFactoryFactory.factoryOf({OM}.class);
 *     
 *     private {OM} {om};
 * 
 *     public AnnualLeave() {
 *         {om} = {OM_FACTORY}.create(this);
 *     }
 * 
 *     @Id
 *     @Column(name="ANNUAL_LEAVE_ID", nullable=false)
 *     @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="annualLeaveSequence")
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
 *     @Column(name="START_TIME")
 *     @Temporal(TemporalType.TIMESTAMP)
 *     public Date getStartTime() { 
 *         return {om}.getStartTime(); 
 *     }
 * 
 *     public void setStartTime(Date paramDate) {
 *         {om}.setStartTime(paramDate); 
 *     }
 *     
 *     @Column(name="END_TIME")
 *     @Temporal(TemporalType.TIMESTAMP)
 *     public Date getEndTime() { 
 *         return {om}.getEndTime(); 
 *     }
 * 
 *     public void setEndTime(Date paramDate) {
 *         {om}.setEndTime(paramDate); 
 *     }
 *      
 *     @Column(name="STATE")
 *     @Enumerated(EnumType.ORDINAL)
 *     public State getState() { 
 *         return {om}.getState(); 
 *     }
 * 
 *     public void setState(State paramState) {
 *         {om}.setState(paramState); 
 *     } 
 *     
 *     @ManyToOne(fetch=FetchType.LAZY)
 *     @JoinColumn(name="EMPLOYEE_ID", nullable=false)
 *     public Employee getEmployee() { 
 *         return (Employee){om}.getEmployeeReference().get(); 
 *     }
 * 
 *     public void setEmployee(Employee paramEmployee) {
 *         {om}.getEmployeeReference().set(paramEmployee); 
 *     }
 *      
 *     @StaticMethodToGetObjectModel
 *     static {OM} {om}(AnnualLeave paramAnnualLeave) { 
 *         return paramAnnualLeave.{om}; 
 *     } 
 *     
 *     public String toString() { 
 *         return {om}.toString(); 
 *     }
 *      
 *     public static enum State { 
 *         PENDING, 
 *         APPROVED, 
 *         REJECTED;
 *     }
 * 
 *     @ObjectModelDeclaration(provider="jpa", mode=ObjectModelMode.REFERENCE)
 *     @AllowDisability
 *     private static abstract interface {OM} {
 *         @EntityId
 *         Long getId();
 *         void setId(Long paramLong);
 * 
 *         @OptimisticLock
 *         int getVersion();
 *         void setVersion(int paramInt);
 * 
 *         @Scalar
 *         Date getStartTime();
 *         void setStartTime(Date paramDate);
 * 
 *         @Scalar
 *         Date getEndTime();
 *         void setEndTime(Date paramDate);
 * 
 *         @Scalar
 *         AnnualLeave.State getState();
 *         void setState(AnnualLeave.State paramState);
 * 
 *         @Association(opposite="annualLeaves")
 *         Reference<Employee> getEmployeeReference();
 *     }
 * }
 */
/**
 * @author Tao Chen
 */
@JPAObjectModelInstrument
@Entity
@Table(name = "ANNUAL_LEAVE")
@SequenceGenerator(
        name = "annualLeaveSequence",
        sequenceName = "ANNUAL_LEAVE_ID_SEQ",
        initialValue = 1,
        allocationSize = 1
)
public class AnnualLeave {

    @Id
    @Column(name = "ANNUAL_LEAVE_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "annualLeaveSequence")
    private Long id;
    
    @Version
    @Column(name = "VERSION", nullable = false)
    private int version;
    
    @Column(name = "START_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;
    
    @Column(name = "END_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;
    
    @Column(name = "STATE")
    @Enumerated(EnumType.ORDINAL)
    private State state;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMPLOYEE_ID", nullable = false)
    private Employee employee;
    
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

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public static enum State {
        PENDING,
        APPROVED,
        REJECTED
    }
}
